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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkCapacityExceededException;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import java.util.List;
import java.util.HashMap;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* 15-Jan-2008: Temporary data are now list of lsps and reserved bandwidth array instead of reservable array (GMO) 
*/

/**
* This class implements a snapshot of the manages DiffServBandwidthManagement. Adding or removing lsps do not change
* change the reserved bandwidth on the links. It also maintain a list of lsp traversing every link of the domain.
* <br>
* This implementation uses preemption. it also uses Diffserv model to update link reservable bandwidth.
*
* <p>Creation date: 14/12/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class DiffServBandwidthManagementSnapshot extends DiffServBandwidthManagement {
    private final static Logger logger = Logger.getLogger(DiffServBandwidthManagementSnapshot.class);

    private HashMap<String, Pair<List<Lsp>, float[]>> tempData;

    /**
     * Create a new DiffServBandwidthManagement object to use with the given domain.
     *
     * @param domain
     */
    public DiffServBandwidthManagementSnapshot(Domain domain) {
        super(domain);
        tempData = new HashMap<String, Pair<List<Lsp>, float[]>>();
    }

    /**
     * Return the rbw array associated with the given link. It returns the temporary data or the real rbw array if
     * no temporary data exists.
     * @param link
     * @return
     */
    protected float[] getRbw(Link link) {
        Pair<List<Lsp>, float[]> elem = tempData.get(link.getId());
        if (elem == null) {
            /*List<Lsp> lspsOnLink = super.getLspsOnLink(link);
            float[] rbw = super.getRbw(link);
            elem = new Pair<List<Lsp>, float[]>(lspsOnLink, rbw);
            tempData.put(link.getId(), elem);
            */
            return super.getRbw(link);
        }
        return link.getDiffServModel().getReservableBandwidth(domain, link.getBCs(), elem.getSecond(), link.getBandwidth());
    }

    /**
     * Returns the list of LSPs traversing the given link. This method takes into account previous calls to
     * {@link #addLsp(be.ac.ulg.montefiore.run.totem.domain.model.Lsp)} and
     * {@link #removeLsp(be.ac.ulg.montefiore.run.totem.domain.model.Lsp)}.
     * @param link
     * @return
     */
    protected List<Lsp> getLspsOnLink(Link link) {
        Pair<List<Lsp>, float[]> elem = tempData.get(link.getId());
        if (elem == null) {
            return domain.getLspsOnLink(link);
        }
        return elem.getFirst();
    }

    /**
     * returns the total resserved badnwidth among all classtypes.
     * @return
     */
    protected float getReservedBandwidth(Link link) {
        Pair<List<Lsp>, float[]> elem = tempData.get(link.getId());
        if (elem == null) {
            return super.getReservedBandwidth(link);
        }
        float reservedBw = 0;
        for (int i = 0; i < elem.getSecond().length; i++) {
            if (elem.getSecond()[i] != -1) {
                reservedBw += elem.getSecond()[i];
            }
        }
        return reservedBw;
    }

    /**
     * Add the reservation to the data maintained by this class. It does not affect the link reservation.
     * @param link
     * @param bw
     * @param prio
     * @throws LinkCapacityExceededException
     */
    protected void addReservation(Link link, float bw, int prio) throws LinkCapacityExceededException {
        Pair<List<Lsp>, float[]> elem = tempData.get(link.getId());

        float[] reservedBw;
        if (elem == null) {
            reservedBw = link.getReservations();
        } else {
            reservedBw = elem.getSecond();
        }

        float mrbw = link.getBandwidth();
        float[] rbw = link.getDiffServModel().getReservableBandwidth(domain, link.getBCs(), reservedBw, mrbw);

        if (rbw[prio] < bw) {
            throw new LinkCapacityExceededException();
        }
        reservedBw[prio] += bw;

        if (elem == null) {
            List<Lsp> list = getLspsOnLink(link);
            tempData.put(link.getId(), new Pair<List<Lsp>, float[]>(list, reservedBw));
        } else {
            elem.setSecond(reservedBw);
        }
    }

    /**
     * Remove the reservation from the data maintained by this class.  It does not affect the link reservation.
     * @param link
     * @param bw
     * @param prio
     * @throws LinkCapacityExceededException
     */
    protected void removeReservation(Link link, float bw, int prio) throws LinkCapacityExceededException {
        Pair<List<Lsp>, float[]> elem = tempData.get(link.getId());

        float[] reservedBw;
        if (elem == null) {
            reservedBw = link.getReservations();
        } else {
            reservedBw = elem.getSecond();
        }

        if (reservedBw[prio] < bw) {
            throw new LinkCapacityExceededException();
        }
        reservedBw[prio] -= bw;

        if (elem == null) {
            List<Lsp> list = getLspsOnLink(link);
            tempData.put(link.getId(), new Pair<List<Lsp>, float[]>(list, reservedBw));
        } else {
            elem.setSecond(reservedBw);
        }
    }

    /**
     * Add the reservation of this LSPs to the temporary data maintained by this class. Also updates the list of lsps
     * traversing link on the lsp path.
     * @param lsp
     * @throws LinkCapacityExceededException
     */
    public void addLsp(Lsp lsp) throws LinkCapacityExceededException {
        super.addLsp(lsp);
        for (Link link : lsp.getLspPath().getLinkPath()) {
            Pair<List<Lsp>, float[]> elem = tempData.get(link.getId());
            if (elem == null) {
                List<Lsp> lspsOnLink = domain.getLspsOnLink(link);
                if (lspsOnLink.contains(lsp)) {
                    logger.error("LSP " + lsp.getId() + " already on link " + link.getId());
                } else
                    lspsOnLink.add(lsp);

                float[] reservedBw = link.getReservations();
                elem = new Pair<List<Lsp>, float[]>(lspsOnLink, reservedBw);
                tempData.put(link.getId(), elem);
            } else {
                List<Lsp> lspsOnLink = elem.getFirst();
                if (lspsOnLink.contains(lsp)) {
                    logger.error("LSP " + lsp.getId() + " already on link " + link.getId());
                } else
                    lspsOnLink.add(lsp);
            }

        }
    }

    /**
     * Removes the reservation associated with the LSP in the temporary data maintained by this class.
     * Also updates the list of lsps traversing link on the lsp path.
     * @param lsp
     * @throws LinkCapacityExceededException
     */
    public void removeLsp(Lsp lsp) throws LinkCapacityExceededException {
        super.removeLsp(lsp);

        for (Link link : lsp.getLspPath().getLinkPath()) {
            Pair<List<Lsp>, float[]> elem = tempData.get(link.getId());
            if (elem == null) {
                List<Lsp> lspsOnLink = domain.getLspsOnLink(link);
                if (!lspsOnLink.remove(lsp))
                    logger.error("LSP " + lsp.getId() + " not present on link " + link.getId());
                float[] reservedBw = link.getReservations();
                elem = new Pair<List<Lsp>, float[]>(lspsOnLink, reservedBw);
                tempData.put(lsp.getId(), elem);
            } else {
                List<Lsp> lspsOnLink = elem.getFirst();
                if (!lspsOnLink.remove(lsp))
                    logger.error("LSP " + lsp.getId() + " not present on link " + link.getId());
            }
        }
    }

}
