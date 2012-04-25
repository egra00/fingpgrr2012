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

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidPathException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 24-Jan-2007: use only primary lsps to calculate load (GMO)
* - 14-May-2007: use activated LSPs to calculate load (GMO)
* - 25-Sep-2007: use lsp working path (GMO)
* - 28-Feb-2008: adapt to new interface (GMO)
*/

/**
 * <p>
* Implement the overlay strategy.
* <p>
* With this method, the traffic will be routed on a MPLS tunnel if the [source, destination] pair matches
* the [ingress, egress] pair of the tunnel. If several tunnels matches, the routed path is chosen arbitrarily among them.
* <p>
* If no tunnel matches, it is routed via SPF (eventually using ECMP).
*
* <p>Creation date: 28 ao�t 2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class OverlayStrategy extends AbstractLinkLoadStrategy {
    private static final Logger logger = Logger.getLogger(OverlayStrategy.class);

    public OverlayStrategy(Domain domain, TrafficMatrix tm) {
        super(domain, tm, null);
        changeListener = new MplsLinkLoadStrategyInvalidator(this);
    }

    /**
     * Compute the link load of the specified traffic matrix on the domain
     *
     */
    public void recompute() {
        if (data == null) {
            data = new SettableHybridLoadData(domain);
        }
        data.clear();

        DomainConvertor convertor = domain.getConvertor();

        double load[] = new double[domain.getNbLinks()];
        Arrays.fill(load, 0);

        for (Node srcNode : domain.getAllNodes()) {
            for (Node dstNode : domain.getAllNodes()) {
                if (srcNode.equals(dstNode)) continue;

                double traffic = 0;
                try {
                    traffic = tm.get(srcNode.getId(),dstNode.getId());
                    if (traffic != 0) {
                        List<Lsp> lspList = domain.getPrimaryLsps(srcNode, dstNode);
                        Path routingPath = null;
                        Lsp foundLsp = null;
                        if (lspList.size() > 0) {
                            for (Lsp lsp : lspList) {
                                try {
                                    routingPath = lsp.getWorkingPath();
                                    foundLsp = lsp;
                                    break;
                                } catch (InvalidPathException e) {
                                }
                            }
                        }
                        if (routingPath != null) {
                            logger.debug("Traffic from " + srcNode + " to " + dstNode + " is routed via Path " + routingPath.toNodesString() + ".");
                            data.addMPLSTraffic(foundLsp, traffic);
                            continue;
                        }

                        logger.debug("Traffic from " + srcNode + " to " + dstNode + " is routed via SPF (no LSP).");

                        List<Path> paths;
                        if (spf.getClass().getName().equals("be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF")) {
                            paths = domain.getSPFCache().getPath(srcNode,dstNode, ECMP, false);
                        } else {
                            paths = spf.computeSPF(domain, srcNode.getId(), dstNode.getId(), ECMP);
                        }

                        // Expand paths into link paths
                        List<List<Link>> links = new ArrayList<List<Link>>(paths.size());
                        for(Path path : paths) {
                            links.add(path.getLinkPath());
                        }
                        paths = null; // let GC do his job
                        SPFLinkLoadStrategy.calculateLoads(domain, tm, load, links, 1.0f, convertor.getNodeId(srcNode.getId()), convertor.getNodeId(dstNode.getId()));
                    }
                } catch (RoutingException e) {
                    logger.error("RoutingException for the path between " + srcNode + " and " + dstNode);
                    logger.error(e);
                } catch (NoRouteToHostException e) {
                    logger.error("NoRouteToHostException for the path between " + srcNode + " and " + dstNode);
                    logger.error(e);
                    data.addDroppedTraffic(srcNode, dstNode, traffic);
                } catch (Exception e) {
                    logger.error("Exception during path computation between " + srcNode + " and " + dstNode);
                    logger.error(e);
                }
            }
        }
        for (Link link : domain.getAllLinks()) {
            try {
                int id = domain.getConvertor().getLinkId(link.getId());
                if (load[id] != 0)
                    data.addIPTraffic(link, load[id]);
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }
        dataChanged();
    }

    public boolean equals(Object o) {
        if (!(o instanceof OverlayStrategy)) return false;
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }
}
