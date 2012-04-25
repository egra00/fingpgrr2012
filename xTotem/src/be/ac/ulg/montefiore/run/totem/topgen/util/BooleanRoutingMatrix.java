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

import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.*;
import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;

import java.util.List;
import java.util.Iterator;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
 * This class represents a {0,1} routing matrix.
 *
 * The element (i,j) is equal to 0 if link i doesn't belong to the path
 * associated to origin-destination (OD) pair j, and the element (i,j) is equal
 * to 1 otherwise.
 *
 * <p>Creation date: 2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class BooleanRoutingMatrix extends RoutingMatrix {

    private static final Logger logger = Logger.getLogger(BooleanRoutingMatrix.class);

    private SPF routingPolicy;
    private Domain domain;
    private boolean stopOnError = true;

    /**
     * Initialises and computes a newly created routing matrix for the domain
     * <code>domain</code> according to the SPF algorithm.
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException If an error occurred during the routing.
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException If there is no route between a pair of nodes.
     */
    public BooleanRoutingMatrix(Domain domain) throws RoutingException, NoRouteToHostException {
        routingPolicy = new CSPF();
        this.domain = domain;
        recompute();
    }

    /**
     * Initialises and computes a newly created routing matrix for the domain
     * <code>domain</code> according to the SPF algorithm.
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException If an error occurred during the routing.
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException If there is no route between a pair of nodes.
     */
    public BooleanRoutingMatrix(SPF spf,Domain domain, boolean stopOnError) throws RoutingException, NoRouteToHostException {
        routingPolicy  = spf;
        this.domain = domain;
        this.stopOnError = stopOnError;
        recompute();
    }



    /**
     * Recomputes the routing matrix.
     * 
     * @throws RoutingException If an error occured during the routing.
     * @throws NoRouteToHostException If there is no route between a pair of nodes.
     */
    @Override
    public void recompute() throws RoutingException , NoRouteToHostException {
        List<Node> nodes = domain.getUpNodes();
        setSize(domain.getConvertor().getMaxLinkId(), domain.getConvertor().getMaxNodeId() * (domain.getConvertor().getMaxNodeId() -1));

        for(Iterator<Node> it = nodes.iterator(); it.hasNext();) {
            boolean isError = false;  // true if an error occurs in the SPF computation
            Node node = it.next();
            String src = node.getId();
            List<Path> paths = null;
            try {
                paths = routingPolicy.computeSPF(domain, src);
            } catch (NoRouteToHostException e) {
                if (stopOnError) {
                    throw e;
                } else {
                    isError = true;
                    // fallback... try to route (src, dst) separately
                    for(Iterator<Node> iter = nodes.iterator(); iter.hasNext();) {
                        Node dst = iter.next();
                        String dstId = dst.getId();
                        if(dstId.equals(src) || (e.getDst().getId().equals(dstId))) {
                            continue;
                        }
                        try {
                            Path path = routingPolicy.computeSPF(domain, src, dstId);
                            List<Link> linkPath = path.getLinkPath();
                            for(Iterator<Link> it2 = linkPath.iterator(); it2.hasNext();) {
                                Link link = it2.next();
                                try {
                                    set(1, domain.getConvertor().getLinkId(link.getId()), domain.getConvertor().getNodeId(src), domain.getConvertor().getNodeId(dstId));
                                }
                                catch(NodeNotFoundException e1) {
                                    logger.error("NodeNotFoundException in recompute. Message: "+e.getMessage());
                                }
                                catch(LinkNotFoundException e1) {
                                    logger.error("LinkNotFoundException in recompute. Message: "+e.getMessage());
                                }
                            }
                        } catch (RoutingException e1) {
                            // nothing to do: isError is already true and stopOnError is false...
                            logger.debug("RoutingException. Message: "+e1.getMessage());
                        } catch (NoRouteToHostException e1) {
                            // nothing to do: there is another node which is not reachable...
                            logger.debug("NoRouteToHostException. Message: "+e1.getMessage());
                        }
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
                for(Iterator<Path> it1 = paths.iterator(); it1.hasNext();) {
                    Path path = it1.next();
                    List<Link> linkPath = path.getLinkPath();
                    for(Iterator<Link> it2 = linkPath.iterator(); it2.hasNext();) {
                        Link link = it2.next();
                        try {
                            set(1, domain.getConvertor().getLinkId(link.getId()), domain.getConvertor().getNodeId(src), domain.getConvertor().getNodeId(path.getDestinationNode().getId()));
                        }
                        catch(NodeNotFoundException e) {
                            logger.error("NodeNotFoundException in recompute. Message: "+e.getMessage());
                        }
                        catch(LinkNotFoundException e) {
                            logger.error("LinkNotFoundException in recompute. Message: "+e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
