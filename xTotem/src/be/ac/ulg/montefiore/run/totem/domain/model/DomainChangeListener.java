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

/*
 * Changes:
 * --------
 * - 30-Jun-2006 : Add nodeLocationChangeEvent (GMO)
 * - 11-May-2007: Add lspStatusChangeEvent (GMO)
 * - 25-Sep-2007: Change lspStatusChangeEvent in lspWorkingPathChangeEvent (GMO)
 * - 29-Nov-2007: Remove rerouteLspEvent (GMO)
 * - 26-Feb-2008: Add lspStatusChangeEvent (GMO) 
 */

/**
 * Define all the events that a listener will receive. The DomainChangeAdapter implements
 * the DomainChangeListener with empty methods. It can be used to reduce the number of method
 * to implements by subclassing the DomainChangeAdapter and overiding only usefull methods.
 *
 * @see DomainChangeAdapter
 *
 * <p>Creation date: 12-Jan-2005 18:23:51
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface DomainChangeListener {

    /*************************************************
     * Methods dealing with nodes
     ************************************************/
    /**
     * Notify a add node event
     *
     * @param node
     */
    public void addNodeEvent(Node node);

    /**
     * Notify a remove node event
     *
     * @param node
     */
    public void removeNodeEvent(Node node);

    /**
     * Notify a node status change event
     *
     * @param node
     */
    public void nodeStatusChangeEvent(Node node);

    /**
     * Notify a node location change event
     *
     * @param node
     */
    public void nodeLocationChangeEvent(Node node);

    /*************************************************
     * Methods dealing with links
     ************************************************/

    /**
     * Notify a add link event
     *
     * @param link
     */
    public void addLinkEvent(Link link);

    /**
     * Notify a remove link event
     *
     * @param link
     */
    public void removeLinkEvent(Link link);

    /**
     * Notify a link status change event
     *
     * @param link
     */
    public void linkStatusChangeEvent(Link link);

    /**
     * Notify a link metric change event
     *
     * @param link
     */
    public void linkMetricChangeEvent(Link link);

    /**
     * Notify a link TE metric change event
     *
     * @param link
     */
    public void linkTeMetricChangeEvent(Link link);

    /**
     * Notify a link bandwidth change event
     *
     * @param link
     */
    public void linkBandwidthChangeEvent(Link link);

    /**
     * Notify a link reserved bandwidth change event
     *
     * @param link
     */
    public void linkReservedBandwidthChangeEvent(Link link);

    /**
     * Notify a link delay change event
     *
     * @param link
     */
    public void linkDelayChangeEvent(Link link);

    /*************************************************
     * Methods dealing with LSP
     ************************************************/
    /**
     * Notify a add LSP event
     *
     * @param lsp
     */
    public void addLspEvent(Lsp lsp);

    /**
     * Notify a remove LSP event
     *
     * @param lsp
     */
    public void removeLspEvent(Lsp lsp);

    /**
     * Notify a LSP reservation change event
     *
     * @param lsp
     */
    public void lspReservationChangeEvent(Lsp lsp);

    /**
     * Notify a change in the working path of the lsp. The working path of a lsp is the path used for routing.
     * @param lsp
     */
    public void lspWorkingPathChangeEvent(Lsp lsp);

    /**
     * Notify a change in the status of a lsp
     * @param lsp
     */
    public void lspStatusChangeEvent(Lsp lsp);
}
