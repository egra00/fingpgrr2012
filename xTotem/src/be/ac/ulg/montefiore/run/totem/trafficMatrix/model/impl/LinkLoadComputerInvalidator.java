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

import be.ac.ulg.montefiore.run.totem.domain.model.DomainChangeListener;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrixChangeListener;

/*
* Changes:
* --------
*
*/

/**
 * Invalidate LinkLoadComputer data when a signal of a change in the topology occurs and when a traffic matrix
 * element change is signaled.
 *
 * @see AbstractLinkLoadComputer#AbstractLinkLoadComputer(be.ac.ulg.montefiore.run.totem.domain.model.Domain, LinkLoadComputerInvalidator)
 *
 * <p>Creation date: 22/02/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class LinkLoadComputerInvalidator implements DomainChangeListener, TrafficMatrixChangeListener {
    protected final LinkLoadComputer llc;

    public LinkLoadComputerInvalidator(LinkLoadComputer llc) {
        this.llc = llc;
    }

    public void addNodeEvent(Node node) {
        llc.invalidate();
    }

    public void removeNodeEvent(Node node) {
        llc.invalidate();
    }

    public void nodeStatusChangeEvent(Node node) {
        llc.invalidate();
    }

    public void nodeLocationChangeEvent(Node node) {
    }

    public void addLinkEvent(Link link) {
        llc.invalidate();
    }

    public void removeLinkEvent(Link link) {
        llc.invalidate();
    }

    public void linkStatusChangeEvent(Link link) {
        llc.invalidate();
    }

    public void linkMetricChangeEvent(Link link) {
    }

    public void linkTeMetricChangeEvent(Link link) {
    }

    public void linkBandwidthChangeEvent(Link link) {
    }

    public void linkReservedBandwidthChangeEvent(Link link) {
    }

    public void linkDelayChangeEvent(Link link) {
    }

    public void addLspEvent(Lsp lsp) {
    }

    public void removeLspEvent(Lsp lsp) {
    }

    public void lspReservationChangeEvent(Lsp lsp) {
    }

    public void lspWorkingPathChangeEvent(Lsp lsp) {
    }

    public void lspStatusChangeEvent(Lsp lsp) {
    }

    public void elementChangeEvent(String src, String dst) {
        llc.invalidate();
    }
}
