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

import be.ac.ulg.montefiore.run.totem.domain.exception.DomainChangeObserverException;

/*
 * Changes:
 * --------
 *
 * - 23-Mar-2005: add priorities and removeListener (JL)
 * - 08-Nov-2005: add getNbListeners (GMO)
 * - 30-Jun-2006: add notifyNodeLocationChange (GMO)
 * - 11-May-2007: add notifyLspStatusChange (GMO)
 * - 31-May-2007: add removeAllListeners() method (GMO)
 * - 25-Sep-2007: change notifyLspStatusChange in notifyLspWorkingPathChange (GMO)
 * - 29-Nov-2007: remove notifyRerouteLsp(.) (GMO)
 * - 26-Feb-2008: Add notifyLspStatusChange(.) (GMO) 
 */

/**
 * A DomainChangeObserver is associated to a Domain and notify to all
 * listeners the Domain changes. The priorities of the listeners define the
 * order in which they are notified.
 *
 * <p>Creation date: 12-Jan-2005 18:12:00
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public interface DomainChangeObserver {

    public static final int LOWEST_PRIORITY = 0;
    public static final int HIGHEST_PRIORITY = 9;
    
    /**
     * Adds a listener with the lowest priority. This method does nothing
     * if <code>l</code> is already registered.
     * @param l The listener to add.
     */
    public void addListener(DomainChangeListener l);

    /**
     * Adds a listener with the specified priority. This method does nothing
     * if <code>l</code> is already registered.
     * @param l The listener to add.
     * @param priority The priority of the listener. This must be greater or
     * equal than <code>LOWEST_PRIORITY</code> and lower or equal than
     * <code>HIGHEST_PRIORITY</code>.
     * @throws DomainChangeObserverException If <code>priority</code> is
     * invalid.
     */
    public void addListener(DomainChangeListener l, int priority) throws DomainChangeObserverException;
    
    /**
     * Removes the listener <code>l</code>. This method does nothing if
     * <code>l</code> is not registered.
     * @param l The listener to remove.
     */
    public void removeListener(DomainChangeListener l);

    /**
     * Removes all listeners.
     */
    public void removeAllListeners();

    /**
     * return the number of listeners for all priorites inferior or equal to <code>priority</code>
     * @param priority
     * @return
     */
    public int getNbListeners(int priority);
    /**
     * return the total number of listeners
     * @return
     */
    public int getNbListeners();

    /*************************************************
     * Methods dealing with nodes
     ************************************************/
    /**
     * Notify node add
     *
     * @param node
     */
    public void notifyAddNode(Node node);

    /**
     * Notify node remove
     *
     * @param node
     */
    public void notifyRemoveNode(Node node);

    /**
     * Notify node status change
     *
     * @param node
     */
    public void notifyNodeStatusChange(Node node);

    /**
     * Notify a node location change
     *
     * @param node
     */
    public void notifyNodeLocationChange(Node node);

    /*************************************************
     * Methods dealing with links
     ************************************************/
    /**
     * Notify link add
     *
     * @param link
     */
    public void notifyAddLink(Link link);

    /**
     * Notify link remove
     *
     * @param link
     */
    public void notifyRemoveLink(Link link);

    /**
     * Notify link status change
     *
     * @param link
     */
    public void notifyLinkStatusChange(Link link);

    /**
     * Notify link metric change
     *
     * @param link
     */
    public void notifyLinkMetricChange(Link link);

    /**
     * Notify link TE metric change
     *
     * @param link
     */
    public void notifyLinkTeMetricChange(Link link);

    /**
     * Notify link bandwidth change
     *
     * @param link
     */
    public void notifyLinkBandwidthChange(Link link);

    /**
     * Notify link reserved bandwidth change
     *
     * @param link
     */
    public void notifyLinkReservedBandwidthChange(Link link);

    /**
     * Notify link delay change
     *
     * @param link
     */
    public void notifyLinkDelayChange(Link link);

    /*************************************************
     * Methods dealing with LSPs
     ************************************************/
    /**
     * Notify LSP add
     *
     * @param lsp
     */
    public void notifyAddLsp(Lsp lsp);

    /**
     * Notify LSP remove
     *
     * @param lsp
     */
    public void notifyRemoveLsp(Lsp lsp);

    /**
     * Notify LSP reservation change
     *
     * @param lsp
     */
    public void notifyLspReservationChange(Lsp lsp);

    /**
     * Notify a change in the status of the lsp.
     * @param lsp
     */
    public void notifyLspStatusChange(Lsp lsp);

    /**
     * Notify a change in the working path of the lsp
     * @param lsp
     */
    public void notifyLspWorkingPathChange(Lsp lsp);

}
