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
package be.ac.ulg.montefiore.run.totem.domain.model.impl;

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 13-Feb-2008: Classes of service are now taken into account. (GMO)
*/

/**
* Reacts to change in node or link status. Activates and deactivates the appropriate bypass and local detour LSPs. This
* also activates global detour if the failure is detected by the ingress router (the point of local recovery is the ingress).
* Bypass LSPs are only selected if they belong to the same classtype as the primary.
*
* <p>Creation date: 4/12/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class FastRerouteSwitchingMethod extends SwitchingMethod {
    private final static Logger logger = Logger.getLogger(FastRerouteSwitchingMethod.class);

    /**
     * Tell if detour lsps should be preferred to bypass ones, or the opposite
     */
    private boolean preferDetourOverBypass = true;

    /**
     * Tell wether a node protection tunnel should be preferred to a link protection if a link failure is detected
     */
    private boolean preferNodeProtectionOverLinkProtection = true;

    private Domain domain;

    public FastRerouteSwitchingMethod(Domain domain) {
        this.domain = domain;
    }

    public void start() {
        for (Lsp lsp : domain.getAllPrimaryLsps()) {
            activateBackupsLsps(lsp);
        }
        domain.getObserver().addListener(this);
    }

    public void stop() {
        domain.getObserver().removeListener(this);
    }

    public void nodeStatusChangeEvent(Node node) {
        Set<Lsp> lsps = new HashSet<Lsp>();

        for (Link link : node.getAllOutLink()) {
            modifiedLsps(link, lsps);
        }
        for (Link link : node.getAllInLink()) {
            modifiedLsps(link, lsps);
        }

        for (Lsp lsp : lsps) {
            activateBackupsLsps(lsp);
        }
    }

    public void linkStatusChangeEvent(Link link) {
        Set<Lsp> lsps = new HashSet<Lsp>();
        modifiedLsps(link, lsps);

        for (Lsp lsp : lsps) {
            activateBackupsLsps(lsp);
        }
    }

    private void modifiedLsps(Link link, Set<Lsp> lsps) {
        for (Lsp lsp : domain.getLspsOnLink(link)) {
            if (!lsp.isBackupLsp())
                lsps.add(lsp);
            else if (lsp.isDetourLsp())
                try {
                    lsps.add(lsp.getProtectedLsp());
                } catch (LspNotFoundException e) {
                    e.printStackTrace();
                }
            else {
                // bypass
                try {
                    for (Link l : lsp.getProtectedLinks()) {
                        for (Lsp myLsp : domain.getLspsOnLink(l)) {
                            if (!myLsp.isBackupLsp()) {
                                lsps.add(myLsp);
                            }
                        }
                    }
                } catch (LinkNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void activateBackupsLsps(Lsp lsp) {
        logger.debug("Activating backups for lsp " + lsp.getId());

        for (Lsp bLsp : lsp.getActivatedBackups()) {
            try {
                lsp.deactivateBackup(bLsp);
            } catch (LspNotFoundException e) {
                e.printStackTrace();
            }
        }

        if (lsp.getLspPath().getSourceNode().getNodeStatus() == Node.STATUS_DOWN ||
                lsp.getLspPath().getDestinationNode().getNodeStatus() == Node.STATUS_DOWN) {
            logger.info("Lsp " + lsp.getId() + " has no path because ingress or egress node is down.");
            return;
        }

        List<Link> linkPath = lsp.getLspPath().getLinkPath();
        List<Node> nodePath = lsp.getLspPath().getNodePath();
        int i = 0;
        while (i < linkPath.size()) {
            Link l = linkPath.get(i);
            if (l.getLinkStatus() == Link.STATUS_DOWN) {
                Lsp backupLsp = findBackupLsp(lsp, i);
                try {
                    if (backupLsp != null) {
                        lsp.activateBackup(backupLsp);
                        i = nodePath.indexOf(backupLsp.getLspPath().getDestinationNode());
                    } else {
                        break;
                    }
                } catch (LspNotFoundException e) {
                    //should not happen since findBackupLsp should find a good backup
                    e.printStackTrace();
                    break;
                }
            } else {
                i++;
            }
        }

        if (i < linkPath.size()) {
            logger.info("Lsp " + lsp.getId() + " has no working path.");
        }
    }

    private Lsp findBackupLsp(Lsp lsp, int linkIndex) {
        boolean nodeProtectionOnly;

        List<Link> linkPath = lsp.getLspPath().getLinkPath();
        List<Node> nodePath = lsp.getLspPath().getNodePath();

        Link l = linkPath.get(linkIndex);
        Node n = nodePath.get(linkIndex+1);

        nodeProtectionOnly = n.getNodeStatus() == Node.STATUS_DOWN;

        Lsp backupLsp = null;
        if (preferDetourOverBypass) {
            backupLsp = findDetourLsp(lsp, l, nodeProtectionOnly);
            if (backupLsp == null) {
                backupLsp = findBypassLsp(lsp, l, nodeProtectionOnly);
            }
        } else {
            backupLsp = findBypassLsp(lsp, l, nodeProtectionOnly);
            if (backupLsp == null) {
                backupLsp = findDetourLsp(lsp, l, nodeProtectionOnly);
            }
        }

        return backupLsp;
    }

    private Lsp findBypassLsp(Lsp lsp, Link l, boolean nodeProtectionOnly) {
        Set<Lsp> set = new HashSet<Lsp>();

        List<Node> nodePath = lsp.getLspPath().getNodePath();
        // find a facility backup
        try {
            for (Lsp bLsp : domain.getLspStartingAtIngress(l.getSrcNode())) {
                if (bLsp.isBypassLsp() && bLsp.getLspStatus() == Lsp.STATUS_UP && lsp.getCT() == bLsp.getCT() && nodePath.contains(bLsp.getLspPath().getDestinationNode())) {
                    if (bLsp.getAcceptedClassesOfService().containsAll(lsp.getAcceptedClassesOfService())) {
                        boolean nodeProtection = l.getDstNode() != bLsp.getLspPath().getDestinationNode();
                        if (nodeProtection || !nodeProtectionOnly) {
                            // the lsp might be useful if it protects node or it protect a link and we do not request for a node protection only
                            set.add(bLsp);
                            if (preferNodeProtectionOverLinkProtection == nodeProtection) {
                                return bLsp;
                            }
                        }
                    }
                }
            }
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
        // either we requested node protection and found none. either we requested linkProtection and found none. So return any.
        return set.size() > 0 ? (Lsp)set.toArray()[0] : null;
    }

    private Lsp findDetourLsp(Lsp lsp, Link l, boolean nodeProtectionOnly) {
        Set<Lsp> set = new HashSet<Lsp>();

        // find the local detour backup
        for (Lsp bLsp : lsp.getBackups()) {
            try {
                if (bLsp.getLspStatus() == Lsp.STATUS_UP && l.getSrcNode() == bLsp.getLspPath().getSourceNode()) {
                    if (bLsp.getAcceptedClassesOfService().containsAll(lsp.getAcceptedClassesOfService())) {
                        boolean nodeProtection = l.getDstNode() != bLsp.getLspPath().getDestinationNode();
                        if (nodeProtection || !nodeProtectionOnly) {
                            // the lsp might be useful if it protects node or it protect a link and we do not request for a node protection only
                            set.add(bLsp);
                            if (preferNodeProtectionOverLinkProtection == nodeProtection) {
                                return bLsp;
                            }
                        }
                    }
                }
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
        }
        // either we request node protection and found none. either we request linkProtection and found none. So return any.
        return set.size() > 0 ? (Lsp)set.toArray()[0] : null;
    }
}
