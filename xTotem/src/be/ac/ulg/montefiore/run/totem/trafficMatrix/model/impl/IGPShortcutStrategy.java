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
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidPathException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;

import java.util.List;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 28-Aug-2006 : spf type was not taken into account (CSPF was always used). (GMO)
* - 24-Jan-2007: use only primary lsps to calculate load (GMO)
* - 14-May-2007: use activated LSPs to calculate load (GMO)
* - 13-Aug-2007: overwrite isECMP() (GMO)
* - 25-Sep-2007: use lsp working path (GMO)
* - 28-Feb-2008: adapt to new interface (GMO)
*/

/**
 * <p>
 * Implement the IGP shortcut link load computation strategy (IETF RFC-3906).
 * <p>
 * "Traffic to nodes that are the tail-end of TE-tunnels, will flow over those TE-tunnels.
 * Traffic to nodes that are downstream of the tail-end nodes will also flow over those TE-tunnels.
 * If there are multiple TE-tunnels to different intermediate nodes on the path to destination node X, traffic will
 * flow over the TE-tunnel whose tail-end node is closest to node X". (RFC-3906)
 *<p>
 * Notes:
 * <ul>
 * <li>this strategy does not use ECMP.</li>
 * <li>the implementation works as follow: The normal IP shortest path is calculated and traffic is forwarded using all
 * TE-tunnels on that path (no matter the metric of the path).
 * </ul>
 *
 * <p>Creation date: 23 juin 2006
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class IGPShortcutStrategy extends AbstractLinkLoadStrategy {

    private static final Logger logger = Logger.getLogger(IGPShortcutStrategy.class);

    public IGPShortcutStrategy(Domain domain, TrafficMatrix tm) {
        super(domain, tm, null);
        changeListener = new MplsLinkLoadStrategyInvalidator(this);
    }

    public void recompute() {
        if (data == null) {
            data = new SettableHybridLoadData(domain);
        }
        data.clear();
        for (Node srcNode : domain.getAllNodes()) {
            for (Node dstNode : domain.getAllNodes()) {
                if (srcNode.equals(dstNode)) continue;
                double traffic = 0;
                try {
                    traffic = tm.get(srcNode.getId(),dstNode.getId());
                    if (traffic != 0) {
                        this.addTraffic(srcNode, dstNode);
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
                    logger.error(e);
                    e.printStackTrace();
                }
            }
        }
        dataChanged();
    }

    private void addTraffic(Node srcNode, Node dstNode) throws NoRouteToHostException, RoutingException {
        double traffic = 0;
        try {
            traffic = tm.get(srcNode.getId(), dstNode.getId());
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
        if (traffic == 0) return;

        if (srcNode.getNodeStatus() == Node.STATUS_DOWN || dstNode.getNodeStatus() == Node.STATUS_DOWN) {
            data.addDroppedTraffic(srcNode, dstNode, traffic);
            return;
        }

        Path p;
        if (spf.getClass().getName().equals("be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF")) {
            p = domain.getSPFCache().getPath(srcNode,dstNode);
        } else {
            p = spf.computeSPF(domain, srcNode.getId(), dstNode.getId());
        }

        List<Node> nodeList = p.getNodePath();
        List<Link> linkList = p.getLinkPath();
        Node currentNode = nodeList.get(0);
        int currentNodeIdx = 0;
        Node egress = nodeList.get(nodeList.size()-1);
        boolean found;

        while (!currentNode.equals(egress)) {
            int lspEgressIdx = nodeList.size()-1;
            found = false;
            while (lspEgressIdx != currentNodeIdx) {
                List<Lsp> lspList = null;
                Node egressNode = nodeList.get(lspEgressIdx);
                lspList = domain.getPrimaryLsps(currentNode, egressNode);
                Path routingPath = null;
                Lsp foundLsp = null;
                if (lspList != null && lspList.size() > 0) {
                    for (Lsp lsp : lspList) {
                        try {
                            routingPath = lsp.getWorkingPath();
                            foundLsp = lsp;
                            break;
                        } catch (InvalidPathException e1) {
                        }
                    }
                }

                if (routingPath != null) {
                    logger.debug("Using LSP between " + currentNode.getId() + " and " + egressNode.getId());
                    data.addMPLSTraffic(foundLsp, traffic);
                    currentNode = egressNode;
                    currentNodeIdx = lspEgressIdx;
                    found = true;
                } else {
                    lspEgressIdx--;
                }
            }
            if (!found) {
                logger.debug("No LSP in shortest path starting at node: " + currentNode.getId());
                Link l = linkList.get(currentNodeIdx);
                data.addIPTraffic(l, traffic);
                currentNode = nodeList.get(++currentNodeIdx);
            }
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof IGPShortcutStrategy)) return false;
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean isECMP() {
        return false;
    }
}
