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
package be.ac.ulg.montefiore.run.totem.domain.model;

import be.ac.ulg.montefiore.run.totem.domain.exception.*;
import be.ac.ulg.montefiore.run.totem.domain.diffserv.DiffServModel;

/*
 * Changes:
 * --------
 *  - 21-Oct-2005: add getPreemptList (GMO).
 *  - 01-Feb-2006: extends DomainElement (JLE).
 *  - 08-Feb-2006: add getSrcInterface & getDstInterface (JLE).
 *  - 08-Feb-2006: comments fixes.
 *  - 06-Mar-2006: add setSrcInterface and setDstInterface (JLE).
 *  - 24-Oct-2006: add getReservedBandwidthCT(.), getReservableBandwidthCT(.) (GMO)
 *  - 22-Nov-2006: add getTotalReservedBandwidth(), getRbw(), getDiffServModel(). Remove getPreemptList(.) (GMO)
 *  - 22-Nov-2006: removeReservation(.) now throws exception (GMO)
 *  - 18-Jan-2007: add getTotalReservableBandwidth() (GMO)
 *  - 22-Jan-2007: add getDescription() and setDescription(String) (JLE)
 *  - 22-Jan-2007: remove unused import (JLE)
 *  - 06-Mar-2007: change constants in enum for Link type, add setLinkType(.) (GMO)
 *  - 18-Apr-2007: add setBC(.), setRbw(.), addPriority(.) and removePriority(.) methods (GMO)
 *  - 24-Apr-2007: add setMaximumBandwidth(.) and getMaximumBandwidth() (GMO)
 *  - 24-Apr-2007: setBandwidth now throws LinkCapacityExceededException (GMO)
 *  - 15-Jan-2008: setBandwidth now throws DiffServConfigurationException (GMO)
 *  - 15-Jan-2008: add getReservations method (GMO)
 */

/**
 * Represent a Link in a Domain
 *
 * <p>Creation date: 12-Jan-2005 17:27:03
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface Link extends DomainElement {

    public final int STATUS_UP = 0;
    public final int STATUS_DOWN = 1;
    public enum Type {
        INTRA, INTER, ACCESS, PEERING, VIRTUAL;
    }

    public Node getSrcNode() throws NodeNotFoundException;
    public Node getDstNode() throws NodeNotFoundException;
    
    public NodeInterface getSrcInterface() throws NodeNotFoundException, NodeInterfaceNotFoundException;
    public NodeInterface getDstInterface() throws NodeNotFoundException, NodeInterfaceNotFoundException;
    public void setSrcInterface(String srcInterfaceId) throws NodeNotFoundException, NodeInterfaceNotFoundException;
    public void setDstInterface(String dstInterfaceId) throws NodeNotFoundException, NodeInterfaceNotFoundException;

    public float getDelay();
    public void setDelay(float delay);
    
    public float getBandwidth();
    public void setBandwidth(float bw) throws LinkCapacityExceededException, DiffServConfigurationException;

    /**
     * Sets the bandwidth of the link. This sets the true link capacity. It corresponds to mbw (Maximum Bandwidth)
     * in IGP and bw in normal link.
     * <br>It does not change the reservable bandwidth of the link (mrbw in igp).
     * @param value
     */
    public void setMaximumBandwidth(float value);
    /**
     * Return the maximum bandwidth of the link. It is the true link capacity.
     * @return Returns the Mbw value of igp static section if exists. Otherwise returns the link bw.
     */
    public float getMaximumBandwidth();

    /**
     * Get the status of the link. Returns down if the source or destination node is down or link is down.
     *
     * @return Link.STATUS_UP if the link is UP and Link.STATUS_DOWN otherwise
     */
    public int getLinkStatus();
    public void setLinkStatus(int status) throws StatusTypeException;

    public Type getLinkType();
    public void setLinkType(Link.Type type);

    public float getMetric();
    public void setMetric(float metric);
    
    public float getTEMetric();
    public void setTEMetric(float metric);

    public float getReservableBandwidth();
    public float getReservedBandwidth();

    /**
     * Returns a copy of the reservation array.
     * @return a new array indexed by priority level representing the current reservations.
     */
    public float[] getReservations();

    public float getTotalReservedBandwidth();
    public float getTotalReservableBandwidth();

    public float getReservedBandwidthCT(int ct);
    public float getReservableBandwidthCT(int ct);

    public float getReservableBandwidth(int priority);
    public float getReservedBandwidth(int priority);

    public void addReservation(float bw) throws LinkCapacityExceededException;
    public void addReservation(float bw, int priority) throws LinkCapacityExceededException;

    public int getDiffServBCM();
    public DiffServModel getDiffServModel();

    public void removeReservation(float bw) throws LinkCapacityExceededException;
    public void removeReservation(float bw, int priority) throws LinkCapacityExceededException;

    public float[] getBCs();

    /**
     * Set the BC to the given value.
     * @param ct class type
     * @param value
     * @throws DiffServConfigurationException when the classType cannot be found in the domain.
     * @throws LinkCapacityExceededException If the new BC value do not leave enough bandwidth for the current established LSPs.
     */
    public void setBC(int ct, float value) throws DiffServConfigurationException, LinkCapacityExceededException;

    public float[] getRbw();
    public void setRbw(float[] rbw);

    /**
     * This function should be called after a priority is added to the domain. The new priority is added to
     * the rbw array and the rbw array is refreshed (thanks to the BandwidthManagement).
     * If the priority corresponds to a new class type, a correponding BC is added (with 0 bandwidth).<br>
     * Warning: this method is not intended to be called directly.
     * @param priority the priority that was added.
     */
    public void addPriority(int priority);

    /**
     * This function should be called after a priority is removed from the domain. The priority is removed from
     * the rbw array and the rbw array is refreshed (thanks to the BandwidthManagement).
     * @param priority
     */
    public void removePriority(int priority);


    public String getDescription();
    public void setDescription(String description);
}
