/* TOTEM-v3.2 June 18 2008*/

/*
 * ===========================================================
 * TOTEM : A TOolbox for Traffic Engineering Methods
 * ===========================================================
 *
 * (C) Copyright 2004-2006, by Research Unit in Networking RUN, University of Liege. All Rights Reserved.
 *
 * Project Info:  http://totem.run.montefiore.ulg.ac.be
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License version 2.0 as published by the Free Software Foundation;
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
*/
package be.ac.ulg.montefiore.run.totem.topgen.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF;
import be.ac.ulg.montefiore.run.totem.repository.model.SPF;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;

/*
 * Changes:
 * --------
 *
 * - 18-May-2005: add the ECMP parameter (JL).
 */

/**
 * This class represents a routing matrix whose elements belong to [0,1]. The
 * element (i,j) of this matrix is equal to f if the fraction f of the traffic
 * of the OD pair j is flowing through the link i.
 *
 * <p>Creation date: 04-mai-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class RealRoutingMatrix extends RoutingMatrix {

    private static final Logger logger = Logger.getLogger(RealRoutingMatrix.class);
    
    private SPF routingPolicy;
    private Domain domain;
    private boolean stopOnError, ECMP;
    
    public RealRoutingMatrix(Domain domain) throws RoutingException, NoRouteToHostException {
        this.domain = domain;
        routingPolicy = new CSPF();
        stopOnError = true;
        ECMP = true;
        recompute();
    }

    public RealRoutingMatrix(Domain domain, SPF spf, boolean stopOnError, boolean ECMP) throws RoutingException, NoRouteToHostException {
        this.domain = domain;
        routingPolicy = spf;
        this.stopOnError = stopOnError;
        this.ECMP = ECMP;
        recompute();
    }
    
    /**
     * Recomputes the routing matrix.
     * 
     * @throws RoutingException If an error occured during the routing.
     * @throws NoRouteToHostException If there is no route between a pair of nodes.
     */
    @Override
    public void recompute() throws RoutingException, NoRouteToHostException {
        List<Node> nodes = domain.getUpNodes();
        setSize(domain.getConvertor().getMaxLinkId(), domain.getConvertor().getMaxNodeId() * (domain.getConvertor().getMaxNodeId() -1));
        
        for(Iterator<Node> it = nodes.iterator(); it.hasNext();) {
            boolean isError = false;  // true if an error occurs in the SPF computation
            Node node = it.next();
            String src = node.getId();
            List<Path> paths = null;
            try {
                paths = routingPolicy.computeSPF(domain, src, ECMP);
            } catch (NoRouteToHostException e) {
                isError = true;
                if (stopOnError) {
                    throw e;
                }
                
                // fallback... try to route (src, dst) separately
                for(Iterator<Node> iter = nodes.iterator(); iter.hasNext();) {
                    Node dst = iter.next();
                    String dstId = dst.getId();
                    if(dstId.equals(src) || (e.getDst().getId().equals(dstId))) {
                        continue;
                    }
                    try {
                        List<Path> path = routingPolicy.computeSPF(domain, src, dstId, ECMP);
                        List<List<Link>> links = new ArrayList<List<Link>>();
                        for (Iterator<Path> iterator = path.iterator(); iterator.hasNext();) {
                            Path element = iterator.next();
                            links.add(element.getLinkPath());
                        }
                        calculateFraction(links, 1, domain.getConvertor().getNodeId(src), domain.getConvertor().getNodeId(dstId));
                    } catch (RoutingException e1) {
                        // nothing to do: isError is already true and stopOnError is false...
                        logger.debug("RoutingException. Message: "+e1.getMessage());
                    } catch (NoRouteToHostException e1) {
                        // nothing to do: there is another node which is not reachable...
                        logger.debug("NoRouteToHostException. Message: "+e1.getMessage());
                    } catch(NodeNotFoundException e1) {
                        throw new RoutingException("NodeNotFoundException. Message: "+e1.getMessage());
                    } catch(LinkNotFoundException e1) {
                        throw new RoutingException("LinkNotFoundException. Message: "+e1.getMessage());
                    }
                }
            } catch (RoutingException e) {
                isError = true;
                if (stopOnError) {
                    throw e;
                }
            }
            if (!isError) {
                // only if there is no error in the path computation
                for (ListIterator<Path> iter = paths.listIterator(); iter.hasNext();) {
                    Path path = iter.next();
                    
                    // Iterate over the list to find the equivalent paths
                    List<List<Link>> sameDst = new ArrayList<List<Link>>();
                    sameDst.add(path.getLinkPath());
                    for(; iter.hasNext();) {
                        Path path1 = iter.next();
                        if(path1.getDestinationNode().getId().equals(path.getDestinationNode().getId())) {
                            sameDst.add(path1.getLinkPath());
                        }
                        else {
                            // the last returned element has a different dst
                            // so this element must be handled in another iteration
                            iter.previous();
                            break;
                        }
                    }
                    
                    try {
                        int srcId = domain.getConvertor().getNodeId(path.getSourceNode().getId());
                        int dstId = domain.getConvertor().getNodeId(path.getDestinationNode().getId());
                        calculateFraction(sameDst, 1, srcId, dstId);
                    } catch (NodeNotFoundException e) {
                        throw new RoutingException("NodeNotFoundException. Message: "+e.getMessage());
                    } catch (LinkNotFoundException e) {
                        throw new RoutingException("LinkNotFoundException. Message: "+e.getMessage());
                    }
                }
            }
        }
    }
    
    // This annotation suppresses the warning related to "groups".
    @SuppressWarnings(value={"unchecked"})
    private void calculateFraction(List<List<Link>> links, float fraction, int src, int dst) throws LinkNotFoundException {
        if(links.get(0).size() == 0) {
            return;
        }
        
        int nbGroups = 0;
        List<List<Link>>[] groups = new List[domain.getConvertor().getMaxLinkId()];
        for (Iterator<List<Link>> iter = links.iterator(); iter.hasNext();) {
            List<Link> path = iter.next();
            Link firstLink = path.remove(0);
            int firstLinkId = domain.getConvertor().getLinkId(firstLink.getId());
            if(groups[firstLinkId] == null) {
                groups[firstLinkId] = new ArrayList<List<Link>>();
                ++nbGroups;
            }
            groups[firstLinkId].add(path);
        }
        
        fraction /= nbGroups;
        for (int i = 0; i < groups.length; ++i) {
            if(groups[i] != null) {
                add(fraction, i, src, dst);
                calculateFraction(groups[i], fraction, src, dst);
            }
        }
    }
}
