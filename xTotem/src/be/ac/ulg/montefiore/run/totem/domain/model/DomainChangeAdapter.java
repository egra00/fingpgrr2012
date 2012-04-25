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
 */

/**
 * DomainChangeAdapter can be used to reduce the number of method to implements
 * if you need to define a DomainChangeListener by subclassing the
 * DomainChangeAdapter and overiding only usefull methods.
 *
 * <p>Creation date: 12-Jan-2005 18:24:10
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class DomainChangeAdapter implements DomainChangeListener {

    /**
     * Notify a add node event
     *
     * @param node
     */
    public void addNodeEvent(Node node) {}

    /**
     * Notify a remove node event
     *
     * @param node
     */
    public void removeNodeEvent(Node node) {}

    /**
     * Notify a node status change event
     *
     * @param node
     */
    public void nodeStatusChangeEvent(Node node) {}

    /**
     * Notify a node location change event
     *
     * @param node
     */
    public void nodeLocationChangeEvent(Node node) {}

    /**
     * Notify a add link event
     *
     * @param link
     */
    public void addLinkEvent(Link link) {}

    /**
     * Notify a remove link event
     *
     * @param link
     */
    public void removeLinkEvent(Link link) {}

    /**
     * Notify a link status change event
     *
     * @param link
     */
    public void linkStatusChangeEvent(Link link) {}

    /**
     * Notify a link metric change event
     *
     * @param link
     */
    public void linkMetricChangeEvent(Link link) {}

    /**
     * Notify a link TE metric change event
     *
     * @param link
     */
    public void linkTeMetricChangeEvent(Link link) {}

    /**
     * Notify a link bandwidth change event
     *
     * @param link
     */
    public void linkBandwidthChangeEvent(Link link) {}

    /**
     * Notify a link reserved bandwidth change event
     *
     * @param link
     */
    public void linkReservedBandwidthChangeEvent(Link link) {}

    /**
     * Notify a link delay change event
     *
     * @param link
     */
    public void linkDelayChangeEvent(Link link) {}

    /**
     * Notify a add LSP event
     *
     * @param lsp
     */
    public void addLspEvent(Lsp lsp) {}

    /**
     * Notify a remove LSP event
     *
     * @param lsp
     */
    public void removeLspEvent(Lsp lsp) {}

    /**
     * Notify a LSP reservation change event
     *
     * @param lsp
     */
    public void lspReservationChangeEvent(Lsp lsp) {}

    /**
     * Notify a change in the working path of the lsp
     *
     * @param lsp
     */
    public void lspWorkingPathChangeEvent(Lsp lsp) {}

    /**
     * Notify a change in the status of a lsp
     *
     * @param lsp
     */
    public void lspStatusChangeEvent(Lsp lsp) {}

}
