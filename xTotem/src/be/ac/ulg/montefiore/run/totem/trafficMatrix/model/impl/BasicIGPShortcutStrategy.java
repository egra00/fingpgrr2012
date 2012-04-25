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
import be.ac.ulg.montefiore.run.totem.domain.model.impl.PathImpl;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidPathException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 20-Mar-2006: implements equals and hashCode (GMO)
* - 24-Apr-2006: important bug fix in computePath (GMO)
* - 24-Jan-2007: use only primary lsps to calculate load (GMO)
* - 14-May-2007: use activated LSPs to calculate load (GMO)
* - 13-Aug-2007: bugfix: SPF algorithm was always used, overwrite isECMP() (GMO)
* - 25-Sep-2007: use lsp working path (GMO)
* - 28-Feb-2008: adapt to new interface (GMO)
*/

/**
 * <p>
 * Implement the basic IGP shortcut link load computation strategy.
 * <p>
 * In this model, all the packets arriving at node A with destination B will be forwarded in the LSP
 * from A to B if exists. This model is the most simple and can be easily implement in real networks.
 * A simple lookup in the BGP table gives the next-hop for any prefix. If an LSP exists to this next-hop,
 * it will be used to forward the traffic. The LSP appears like a virtual interface in the forwarding table.
 * <p>
 * Note: this class does not use ECMP.
 *
 * <p>Creation date: 28-Jun-2005 18:04:30
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class BasicIGPShortcutStrategy extends AbstractLinkLoadStrategy {

    private static final Logger logger = Logger.getLogger(BasicIGPShortcutStrategy.class);

    public BasicIGPShortcutStrategy(Domain domain, TrafficMatrix tm) {
        super(domain, tm, null);
        changeListener = new MplsLinkLoadStrategyInvalidator(this);
    }

    public void recompute() {
        if (data == null) {
            data = new SettableHybridLoadData(domain);
        }
        data.clear();
        List<Node> nodeList = domain.getAllNodes();
        for (int i = 0; i < nodeList.size(); i++) {
            Node srcNode = nodeList.get(i);
            for (int j = 0; j < nodeList.size(); j++) {
                if (j!=i) {
                    Node dstNode = nodeList.get(j);
                    double traffic = 0;
                    try {
                        traffic = tm.get(srcNode.getId(),dstNode.getId());
                        if (traffic != 0) {
                            addTraffic(srcNode,dstNode);
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

        List<Path> paths = null;
        if (spf.getClass().getName().equals("be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF")) {
            paths = domain.getSPFCache().getPath(srcNode,dstNode,this.ECMP, false);
        } else {
            paths = spf.computeSPF(domain,srcNode.getId(),dstNode.getId(),this.ECMP);
        }

        Path p = paths.get(0);

        List<Node> nodeList = p.getNodePath();
        List<Link> linkList = p.getLinkPath();
        Node egress = nodeList.get(nodeList.size()-1);
        int nodeIdx = 0;
        Node currentNode = nodeList.get(nodeIdx);
        while (!currentNode.equals(egress)) {
            List<Lsp> lspList = domain.getPrimaryLsps(currentNode,egress);
            Lsp primary = null;
            Path routingPath = null;
            if ((lspList != null) && (lspList.size() > 0)) {
                for (Lsp lsp : lspList) {
                    try {
                        routingPath = lsp.getWorkingPath();
                        primary = lsp;
                        break;
                    } catch (InvalidPathException e) {
                    }
                }
            }

            if (routingPath != null) {
                logger.debug("Pair ("+srcNode.getId() + "," + dstNode.getId() + ") " + p.toString() + " use LSP " + primary.getId());

                // add the traffic for the primary lsp
                data.addMPLSTraffic(primary, traffic);
                currentNode = primary.getLspPath().getDestinationNode();
                for(;nodeIdx < nodeList.size();nodeIdx++) {
                    if (nodeList.get(nodeIdx).equals(currentNode))
                        break;
                }
            } else {
                // ip traffic
                Link l = linkList.get(nodeIdx);
                data.addIPTraffic(l, traffic);
                nodeIdx++;
                currentNode = nodeList.get(nodeIdx);
            }
        }
    }

    /**
     * Compute the path from source to destination. Note that this method is not used by
     * {@link #addTraffic(be.ac.ulg.montefiore.run.totem.domain.model.Node, be.ac.ulg.montefiore.run.totem.domain.model.Node)}.
     * @param srcNode
     * @param dstNode
     * @return
     * @throws NoRouteToHostException
     * @throws RoutingException
     */
    public Path computePath(Node srcNode, Node dstNode) throws NoRouteToHostException, RoutingException {
        List<Path> paths = null;
        if (spf.getClass().getName().equals("be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF")) {
            paths = domain.getSPFCache().getPath(srcNode,dstNode,this.ECMP, false);
        } else {
            paths = spf.computeSPF(domain,srcNode.getId(),dstNode.getId(),this.ECMP);
        }

        Path p = paths.get(0);

        List<Link> linkPath = new ArrayList<Link>();

        List<Node> nodeList = p.getNodePath();
        Node egress = nodeList.get(nodeList.size()-1);
        int nodeIdx = 0;
        Node currentNode = nodeList.get(nodeIdx);
        while (!currentNode.equals(egress)) {
            List<Lsp> lspList = domain.getPrimaryLsps(currentNode,egress);
            Lsp primary = null;
            Path routingPath = null;
            if ((lspList != null) && (lspList.size() > 0)) {
                for (Lsp lsp : lspList) {
                    try {
                        routingPath = lsp.getWorkingPath();
                        primary = lsp;
                        break;
                    } catch (InvalidPathException e) {
                    }
                }
            }

            if (routingPath != null) {
                logger.debug("Pair ("+srcNode.getId() + "," + dstNode.getId() + ") " + p.toString() + " use LSP " + primary.getId());
                for (Link l : routingPath.getLinkPath()) {
                    linkPath.add(l);
                }
                Path foundPath = new PathImpl(domain);
                try {
                    foundPath.createPathFromLink(linkPath);
                } catch (NodeNotFoundException e) {
                    e.printStackTrace();
                } catch (InvalidPathException e) {
                    e.printStackTrace();
                }
                return foundPath;
            } else {
                // ip traffic
                linkPath.add(p.getLinkPath().get(nodeIdx));
                nodeIdx++;
                currentNode = nodeList.get(nodeIdx);
            }
        }
        Path foundPath = new PathImpl(domain);
        try {
            foundPath.createPathFromLink(linkPath);
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        }

        return foundPath;
    }

    public boolean equals(Object o) {
        if (!(o instanceof BasicIGPShortcutStrategy)) return false;
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public boolean isECMP() {
        return false;
    }


}
