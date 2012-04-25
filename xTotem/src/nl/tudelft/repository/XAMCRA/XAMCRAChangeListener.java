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

package nl.tudelft.repository.XAMCRA;

import org.apache.log4j.Logger;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;

/*
 * Changes:
 * --------
 * 
 */

/**
 *
 * <p/>
 * <p>Creation date : 28 nov. 2005 13:49:47
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class XAMCRAChangeListener implements DomainChangeListener {
    private static Logger logger = Logger.getLogger(XAMCRAChangeListener.class);
    private Domain domain;
    private XAMCRA instance;

    public XAMCRAChangeListener(Domain domain, XAMCRA instance) {
        this.domain = domain;
        this.instance = instance;
    }

    public void addNodeEvent(Node node) {
        instance.invalidateDB();
    }

    public void removeNodeEvent(Node node) {
        instance.invalidateDB();
    }

    public void nodeStatusChangeEvent(Node node) {
        instance.invalidateDB();
    }

    public void nodeLocationChangeEvent(Node node) {
    }

    public void addLinkEvent(Link link) {
        instance.invalidateDB();
    }

    public void removeLinkEvent(Link link) {
        instance.invalidateDB();
    }

    public void linkStatusChangeEvent(Link link) {
        instance.invalidateDB();
    }

    public void linkMetricChangeEvent(Link link) {
        instance.invalidateDB();
    }

    public void linkTeMetricChangeEvent(Link link) {
        instance.invalidateDB();
    }

    public void linkBandwidthChangeEvent(Link link) {
        instance.invalidateDB();
    }

    public void linkReservedBandwidthChangeEvent(Link link) {
        //instance.invalidateDB();
    }

    public void linkDelayChangeEvent(Link link) {
        instance.invalidateDB();
    }

    public void addLspEvent(Lsp lsp) {
        try {
            instance.addLSP(lsp);
        }
        catch(RoutingException e) {
            logger.error("Error while trying to add the LSP "+lsp.getId()+" to the XAMCRA database!");
            logger.error("Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    public void removeLspEvent(Lsp lsp) {
        instance.invalidateDB();
    }

    public void lspReservationChangeEvent(Lsp lsp) {
        instance.invalidateDB();
    }

    public void lspWorkingPathChangeEvent(Lsp lsp) {
    }

    public void lspStatusChangeEvent(Lsp lsp) {
        instance.invalidateDB();
    }

}
