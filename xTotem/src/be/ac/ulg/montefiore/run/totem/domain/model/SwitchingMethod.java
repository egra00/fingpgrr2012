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
*
*/

/**
 * Implements to react to change in node or link status. Must activate and deactivate the appropriate backups LSPs (using
 * {@link Lsp#activateBackup(Lsp)} and {@link Lsp#deactivateBackup(Lsp)}) in response to a change of link or node status.
 *
 * <p/>
 * <p>Creation date: 11/09/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public abstract class SwitchingMethod implements DomainChangeListener {

    public abstract void start();
    public abstract void stop(); 

    final public void addNodeEvent(Node node) {}

    final public void removeNodeEvent(Node node) {
    }

    final public void nodeLocationChangeEvent(Node node) {
    }

    final public void addLinkEvent(Link link) {
    }

    final public void removeLinkEvent(Link link) {
    }

    final public void linkMetricChangeEvent(Link link) {
    }

    final public void linkTeMetricChangeEvent(Link link) {
    }

    final public void linkBandwidthChangeEvent(Link link) {
    }

    final public void linkReservedBandwidthChangeEvent(Link link) {
    }

    final public void linkDelayChangeEvent(Link link) {
    }

    final public void addLspEvent(Lsp lsp) {
    }

    final public void removeLspEvent(Lsp lsp) {
    }

    final public void lspReservationChangeEvent(Lsp lsp) {
    }

    final public void lspWorkingPathChangeEvent(Lsp lsp) {
    }

    final public void lspStatusChangeEvent(Lsp lsp) {
    }
}
