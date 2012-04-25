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

import be.ac.ulg.montefiore.run.totem.util.FloatingPointArithmetic;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkCapacityExceededException;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import org.apache.log4j.Logger;

import java.util.*;

/*
* Changes:
* --------
* - 05-Dec-2006: bugfix (NullPointerException) (GMO)
* - 16-Jan-2007: add roundReservation variable + bugfix (GMO)
* - 13-Mar-2007: fix array size bug (GMO)
* - 18-Apr-2007: add recomputeRbw(.) method (GMO)
* - 18-Dec-2008: Fix bug with preemption level (setup was used instead of holding) (GMO)
* - 15-Jan-2008: Allow preemption inter classtype by default, but do not preempt lsp that frees no bandwidth (when preempted in another ct) (GMO)
*/

/**
* This class manages link bandwidth. Adding or removing lsps changes the reserved bandwidth on the links.
* <br>
* This implementation uses preemption. it also uses Diffserv model to update link reservable bandwidth.
*
* <p>Creation date: 3/11/2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class DiffServBandwidthManagement implements BandwidthManagement {
    private static final Logger logger = Logger.getLogger(DiffServBandwidthManagement.class);

    protected Domain domain;

    private boolean roundReservation = true;

    /**
     * Create a new DiffServBandwidthManagement object to use with the given domain. 
     * @param domain
     */
    public DiffServBandwidthManagement(Domain domain) {
        this.domain = domain;
    }

    /**
     * Initialise the Bandwidth Management object with the lsps already present in the domain.
     * This must be called prior to use.
     * @throws LinkCapacityExceededException if the calculated bandwidth exceed link capacity
     */
    public void init() throws LinkCapacityExceededException {
        for (Lsp lsp : domain.getAllLsps()) {
            addLsp(lsp);
        }
    }

    /**
     * Return the rbw array associated with the given link.
     * @param link
     * @return
     */
    protected float[] getRbw(Link link) {
        return link.getRbw();
    }

    /**
     * Add some reservation to the given link.
     * @param link
     * @param bw
     * @param prio
     * @throws LinkCapacityExceededException
     */
    protected void addReservation(Link link, float bw, int prio) throws LinkCapacityExceededException {
        link.addReservation(bw, prio);
    }

    /**
     * Remove some reservation to the given link.
     * @param link
     * @param bw
     * @param prio
     * @throws LinkCapacityExceededException
     */
    protected void removeReservation(Link link, float bw, int prio) throws LinkCapacityExceededException {
        link.removeReservation(bw, prio);
    }


    /**
     * Returns the list of lsps to be preempted when a lsp is to be added to the domain.
     * <br>
     * Preemption is done in all classtypes. For each link of the lsp to add, from the least preemption
     * level, it tries to find one lsp to preempt to get enough bandwidth. If it cannot be found, all lsps at this
     * preemption level are preempted until enough bandwidth is freed on the link. It then cycles through all preemption
     * levels lower than the lsp one. If a lsp frees no bandwidth at all when preempted (in another class type), it is
     * not preempted at all.
     * <br>
     *
     * @param lsp the Lsp to be added to the domain
     * @return
     * @throws LinkCapacityExceededException
     */
    public List<Lsp> getPreemptList(Lsp lsp) throws LinkCapacityExceededException {
        DiffServBandwidthManagementSnapshot snapshot = getSnapshot();

        boolean preemptInterCT = true;

        List<Lsp> pList = new ArrayList<Lsp>();

        for (Link link : lsp.getLspPath().getLinkPath()) {
            logger.debug("Computing preemptList for link: " + link.getId());

            float reservation = roundReservation ? FloatingPointArithmetic.round(link.getBandwidth(), lsp.getReservation()) : lsp.getReservation();
            Lsp[] lspsOnLink = new Lsp[0];
            //float[] rbw = snapshot.getRbw(link);
            lspsOnLink = snapshot.getLspsOnLink(link).toArray(lspsOnLink);

            int ct = lsp.getCT();
            int priority = domain.getPriority(lsp.getSetupPreemption(), ct);

            List<Integer> lowerPLs;
            if (preemptInterCT)
                lowerPLs = domain.getLowerPLsAllCTs(priority);
            else
                lowerPLs = domain.getLowerPLs(priority);

            boolean found = false;

            int minPrio = domain.getMinPriority(ct);
            if (!snapshot.lspCanBeEstablished(link, priority, reservation)) {
                while (!found) {
                    int maxPL = -1;
                    int index = -1;
                    //System.out.println("lPLs size: " + lowerPLs.size());
                    for (int i = 0; i < lowerPLs.size(); i++) {
                        if (domain.getPreemptionLevel((lowerPLs.get(i)).intValue()) > maxPL) {
                            maxPL = domain.getPreemptionLevel((lowerPLs.get(i)).intValue());
                            index = i;
                        }
                    }
                    if (maxPL >= 0) {
                        lowerPLs.remove(index);
                    } else
                        throw new LinkCapacityExceededException("Not enough bandwidth on link: " + link.getId());

                    logger.debug("Trying to preempt lsps at PL: " + maxPL);

                    //find lsps corresponding to this least pl priority
                    // first pass, try to find one...
                    for (Lsp pLsp : lspsOnLink) {
                        if (pLsp.getHoldingPreemption() == maxPL) {
                            logger.debug("LSP " + pLsp.getId());
                            logger.debug("MAXPL: " + maxPL + " HPreemption: " + pLsp.getHoldingPreemption() + " Associated Reservation " + pLsp.getReservation());

                            snapshot.removeLsp(pLsp);
                            // also preempt associated backups
                            if (pLsp.getBackups() != null && pLsp.getBackups().size() > 0) {
                                for (Lsp bLsp : pLsp.getBackups()) {
                                    snapshot.removeLsp(bLsp);
                                }
                            }

                            if (snapshot.lspCanBeEstablished(link, priority, reservation)) {
                                // found LSP to preempt
                                pList.add(pLsp);
                                if (pLsp.getBackups() != null) pList.addAll(pLsp.getBackups());
                                found = true;
                                break; // we have done
                            } else {
                                // no preempted lsp, add it again to the domain
                                snapshot.addLsp(pLsp);
                                if (pLsp.getBackups() != null && pLsp.getBackups().size() > 0) {
                                    for (Lsp bLsp : pLsp.getBackups()) {
                                        snapshot.addLsp(bLsp);
                                    }
                                }
                            }
                        }

                    }

                    //if (!found) System.out.println("Impossible to preempt one LSP of preemption level " + maxPL);

                    if (!found) {
                        // kill lsps to do this
                        for (Lsp pLsp : lspsOnLink) {
                            if (pLsp.getHoldingPreemption() == maxPL) {
                                float reservableBw = snapshot.getReservableBandwidth(minPrio, link, null);
                                logger.debug("Choosing LSP " + pLsp.getId() + " to be preempted");
                                pList.add(pLsp);
                                if (pLsp.getBackups() != null) pList.addAll(pLsp.getBackups());
                                snapshot.removeLsp(pLsp);
                                if (pLsp.getBackups() != null && pLsp.getBackups().size() > 0) {
                                    for (Lsp bLsp : pLsp.getBackups()) {
                                        snapshot.removeLsp(bLsp);
                                    }
                                }
                                if (snapshot.lspCanBeEstablished(link, priority, reservation)) {
                                    found = true;
                                    break;
                                } else {
                                    if (snapshot.getReservableBandwidth(minPrio, link, null) == reservableBw) {
                                        // lsp suppression freed no bandwidth at all. We can add it again to he domain.
                                        pList.remove(pLsp);
                                        snapshot.addLsp(pLsp);
                                        if (pLsp.getBackups() != null && pLsp.getBackups().size() > 0) {
                                            for (Lsp bLsp : pLsp.getBackups()) {
                                                snapshot.addLsp(bLsp);
                                                pList.remove(bLsp);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return pList;
    }

    /**
     * Perform admission control for preemption on link <code>link</code> for an lsp to be established at priority
     * <code>priority</code> with <code>reservation</code> bandwidth.<p>
     * There must be enough reservable bandwidth in the lsp classtype at the min priority value
     * and the total reserved bandwidth on the link should not exceed the link max reservable bandwidth.
     * @param link
     * @param priority
     * @param reservation
     * @return
     */
    protected boolean lspCanBeEstablished(Link link, int priority, float reservation) {
        int minPrio = domain.getMinPriority(domain.getClassType(priority));
        float reservableCT = getReservableBandwidth(minPrio, link, null);
        return reservableCT >= reservation && link.getBandwidth() - getReservedBandwidth(link) >= reservation;
    }

    /**
     * Add lsp reservation. It adds some reservation to the links in the path of the lsp.
     * @param lsp
     * @throws LinkCapacityExceededException If not enough bandwidth is available
     */
    public void addLsp(Lsp lsp) throws LinkCapacityExceededException {
        int priority = domain.getMinPriority(lsp.getCT());

        /* Check for available bw */
        List<Link> links = lsp.getLspPath().getLinkPath();
        float[] rounded = new float[links.size()];
        int top = 0;
        for (Link link : links) {
            rounded[top] = roundReservation ? FloatingPointArithmetic.round(link.getBandwidth(), lsp.getReservation()) : lsp.getReservation();
            // don't use priority to check for bw
            if (getReservableBandwidth(priority, link, null) < rounded[top]) {
                logger.error("Not enough available bandwidth on link " + link.getId());
                throw new LinkCapacityExceededException();
            }
            top++;
        }

        int truePrio = domain.getPriority(lsp.getHoldingPreemption(), lsp.getCT());

        top = 0;
        for (Link link : lsp.getLspPath().getLinkPath()) {
            addReservation(link, rounded[top++], truePrio);
        }
    }

    /**
     * Removes lsp reservation. It removes some reservation to the links in the path of the lsp.
     * @param lsp
     * @throws LinkCapacityExceededException
     */
    public void removeLsp(Lsp lsp) throws LinkCapacityExceededException {
        int truePrio = domain.getPriority(lsp.getHoldingPreemption(), lsp.getCT());

        for (Link link : lsp.getLspPath().getLinkPath()) {
            float reservation = roundReservation ? FloatingPointArithmetic.round(link.getBandwidth(), lsp.getReservation()) : lsp.getReservation();
            removeReservation(link, reservation, truePrio);
        }
    }

    /**
     * returns true. This class manages preemption.
     * @return
     */
    public boolean usePreemption() {
        return true;
    }

    /**
     * Returns the maximum reservable bandwidth at priority level <code>priority</code> for a
     * lsp traversing the link <code>link</code>. If <code>protectedLinks</code> is given, the reservable bandwidth for
     * a backup lsp protecting those links is returned, otherwise, a primary lsp is assumed.
     * <br>
     * Obtain the reservable bandwidth from the Diffserv model associated with the link.
     * @param priority
     * @param link
     * @param protectedLinks
     * @return
     */
    public float getReservableBandwidth(int priority, Link link, Collection<Link> protectedLinks) {
        float[] rbw = getRbw(link);
        return rbw[priority];
    }

    /**
     * Returns the total reserved bandwidth among all classtypes.
     * @return
     */
    protected float getReservedBandwidth(Link link) {
        return link.getTotalReservedBandwidth();
    }

    /**
     * Recompute the reservable bandwidth (rbw array) of the link <code>link<code> by using the lsps established in
     * the domain.<br>
     * Warning: all rbw values should be set to 0 before calling this method.
     * @param link
     * @throws LinkCapacityExceededException
     */
    public void recomputeRbw(Link link) throws LinkCapacityExceededException {
        for (Lsp lsp : domain.getLspsOnLink(link)) {

            int priority = domain.getMinPriority(lsp.getCT());

            /* Check for available bw */
            float rounded = roundReservation ? FloatingPointArithmetic.round(link.getBandwidth(), lsp.getReservation()) : lsp.getReservation();
            // don't use priority to check for bw
            if (link.getReservableBandwidth(priority) < rounded) {
                logger.error("Not enough available bandwidth on link " + link.getId());
                throw new LinkCapacityExceededException();
            }

            int truePrio = domain.getPriority(lsp.getHoldingPreemption(), lsp.getCT());

            link.addReservation(rounded, truePrio);
        }
    }

    /**
     * Returns a snapshot of the reservation in the domain. Adding and removing LSPs to the snapshot will not affect
     * the reservation of the links.
     * @return
     */
    public DiffServBandwidthManagementSnapshot getSnapshot() {
        return new DiffServBandwidthManagementSnapshot(domain);
    }   
}
