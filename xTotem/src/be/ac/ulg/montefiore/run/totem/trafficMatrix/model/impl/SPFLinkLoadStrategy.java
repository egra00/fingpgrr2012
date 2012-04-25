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
package be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl;

import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 *
 * - 21-Sep-2005: fixed a beautiful bug (JLE)
 * - 20-Mar-2006: implements equals and hashCode (GMO)
 * - 20-Mar-2006: use parameter stopOnError for SPFCache (GMO)
 * - 28-Feb-2008: adapt to the new interface (GMO)
 */

/**
 * <p>
 * This class implements a link load computation using the SPF algorithms. Only IP load is computed as traffic is not
 * routed on LSPs.
 * 
 * <p>Creation date: 29-Jun-2005 11:49:41
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class SPFLinkLoadStrategy extends AbstractLinkLoadStrategy {

    private static final Logger logger = Logger.getLogger(SPFLinkLoadStrategy.class);

    public SPFLinkLoadStrategy(Domain domain, TrafficMatrix tm) {
        super(domain, tm);
    }

    public void recompute() {
        if (data == null) {
            data = new SettableHybridLoadData(domain);
        }
        data.clear();
        double[] loads = new double[domain.getConvertor().getMaxLinkId()];

        // Should we take all nodes? (JLE)
        List<Node> nodeList = domain.getAllNodes();

        for (Node srcNode : nodeList) {
            for (Node dstNode : nodeList) {
                double traffic = 0;
                try {
                    if (srcNode != dstNode) {
                        traffic = tm.get(domain.getConvertor().getNodeId(srcNode.getId()), domain.getConvertor().getNodeId(dstNode.getId()));
                        if (traffic != 0) {
                            if (srcNode.getNodeStatus() == Node.STATUS_DOWN || dstNode.getNodeStatus() == Node.STATUS_DOWN) {
                                data.addDroppedTraffic(srcNode, dstNode, traffic);
                                continue;
                            }

                            List<Path> paths = computePath(domain,srcNode,dstNode);

                            // Expand paths into link paths
                            List<List<Link>> links = new ArrayList<List<Link>>(paths.size());
                            for(Path path : paths) {
                                links.add(path.getLinkPath());
                            }
                            paths = null; // let GC do his job
                            calculateLoads(domain, tm, loads, links, 1.0f, domain.getConvertor().getNodeId(srcNode.getId()), domain.getConvertor().getNodeId(dstNode.getId()));
                        }
                    }
                } catch (RoutingException e) {
                    logger.error("RoutingException for the path between " + srcNode.getId() + " and " + dstNode.getId());
                    logger.error(e);
                } catch (NoRouteToHostException e) {
                    logger.error("NoRouteToHostException for the path between " + srcNode.getId() + " and " + dstNode.getId());
                    logger.error(e);
                    data.addDroppedTraffic(srcNode, dstNode, traffic);
                } catch (Exception e) {
                    logger.error("Exception during path computation between " + srcNode.getId() + " and " + dstNode.getId());
                    e.printStackTrace();
                    logger.error(e);
                    System.exit(0);
                }

            }
        }

        for (Link link : domain.getAllLinks()) {
            try {
                int id = domain.getConvertor().getLinkId(link.getId());
                // extra check: should not happen
                if (link.getLinkStatus() == Link.STATUS_DOWN && loads[id] != 0) {
                    logger.error("Adding traffic to a down link " + link.getId() + " traffic: " + loads[id]);
                }
                data.addIPTraffic(link, loads[id]);
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }
        dataChanged();
    }

    private List<Path> computePath(Domain domain, Node srcNode, Node dstNode) throws NoRouteToHostException, RoutingException {
        if (spf.getClass().getName().equals("be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF")) {
             return domain.getSPFCache().getPath(srcNode,dstNode,this.ECMP, false);
        } else {
            return spf.computeSPF(domain,srcNode.getId(),dstNode.getId(),this.ECMP);
        }
    }

    static public void calculateLoads(Domain domain, TrafficMatrix tm, double[] loads, List<List<Link>> links, float fraction, int src, int dst) throws LinkNotFoundException, NodeNotFoundException {
        if(links.get(0).size() == 0) {
            return;
        }
        
        int nbGroups = 0;
        HashMap<String, List<List<Link>>> groups = new HashMap<String, List<List<Link>>>();
        for(List<Link> path : links) {
            Link firstLink = path.remove(0);
            String firstLinkId = firstLink.getId();
            if(!groups.containsKey(firstLinkId)) {
                groups.put(firstLinkId, new ArrayList<List<Link>>());
                ++nbGroups;
            }
            groups.get(firstLinkId).add(path);
        }
        
        fraction /= nbGroups;
        
        Set<Map.Entry<String, List<List<Link>>>> entries = groups.entrySet();
        for(Map.Entry<String, List<List<Link>>> entry : entries) {
            int linkIdInt = domain.getConvertor().getLinkId(entry.getKey());
            loads[linkIdInt] += fraction * tm.get(src, dst);
            calculateLoads(domain, tm, loads, entry.getValue(), fraction, src, dst);
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof SPFLinkLoadStrategy)) return false;
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }
}
