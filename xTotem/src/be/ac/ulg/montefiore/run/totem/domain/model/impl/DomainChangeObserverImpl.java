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

import be.ac.ulg.montefiore.run.totem.domain.exception.DomainChangeObserverException;
import be.ac.ulg.montefiore.run.totem.domain.model.*;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 *
 * - 23-March-2005: modify impl according to the new interface (JL).
 * - 8-Nov-2005 : modify impl according to the new interface (GMO).
 * - 30-Jun-2006 : modify impl according to the new interface (GMO).
 * - 11-May-2007 : add notifyLspStatusChange (GMO).
 * - 31-May-2007 : add removeAllListeners() method (GMO).
 */

/**
 * The DomainChangeObserverImpl is associated to a Domain and notify to all listeners the Domain changes.
 *
 * <p>Creation date: 12-Jan-2005 18:12:00
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class DomainChangeObserverImpl implements DomainChangeObserver {

    private static final Logger logger = Logger.getLogger(DomainChangeObserverImpl.class);
    
    private Domain domain;
    private List<DomainChangeListener>[] listeners;

    public DomainChangeObserverImpl(Domain domain){
        this.domain = domain;
        // Type unsafe but there is no mean to create generic arrays in Java.
        listeners = new ArrayList[HIGHEST_PRIORITY-LOWEST_PRIORITY+1];
    }

    public void addListener(DomainChangeListener l) {
        try {
            this.addListener(l, LOWEST_PRIORITY);
        }
        catch(DomainChangeObserverException e) {
            // this exception should never be thrown...
            logger.error("Weird exception in addListener. Message: "+e.getMessage());
        }
    }
    
    public void addListener(DomainChangeListener l, int priority) throws DomainChangeObserverException {
        if((priority < LOWEST_PRIORITY) || (priority > HIGHEST_PRIORITY)) {
            throw new DomainChangeObserverException("Bad priority value: "+priority+"!");
        }
        
        // First check that l is not already registered...
        for (int i = 0; i < listeners.length; ++i) {
            if(listeners[i] == null) {
                continue;
            }
            if(listeners[i].contains(l)) {
                return;
            }
        }
        
        // Add the listener
        if(listeners[priority] == null) {
            listeners[priority] = new ArrayList<DomainChangeListener>();
        }
        listeners[priority].add(l);
    }

    public void removeListener(DomainChangeListener l) {
        for(int i = 0; i < listeners.length; ++i) {
            if(listeners[i] == null) {
                continue;
            }
            if(listeners[i].remove(l)) {
                // The addListener methods ensure that l isn't added more than
                // one time, so we can return.
                return;
            }
        }
    }

    public void removeAllListeners() {
        for (int i = 0; i < listeners.length; ++i) {
            if (listeners[i] != null) {
                listeners[i].clear();
            }
        }
    }


    public int getNbListeners(int priority) {
        if((priority < LOWEST_PRIORITY) || (priority > HIGHEST_PRIORITY)) {
            return 0;
        }
        int nb = 0;
        for (int i=0; i <= priority; i++) {
            if (listeners[i] != null) {
                nb += listeners[i].size();
            }
        }
        return nb;
    }

    public int getNbListeners() {
       int nb = 0;
        for(int i = 0; i < listeners.length; ++i) {
            if(listeners[i] != null) {
                nb += listeners[i].size();
            }
        }
        return nb;
    }


    /*************************************************
     * Methods dealing with nodes
     ************************************************/
    /**
     * Notify node add
     *
     * @param node
     */
    public void notifyAddNode(Node node) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.addNodeEvent(node);
            }
        }
    }

    /**
     * Notify node remove
     *
     * @param node
     */
    public void notifyRemoveNode(Node node) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.removeNodeEvent(node);
            }
        }
    }

    /**
     * Notify node status change
     *
     * @param node
     */
    public void notifyNodeStatusChange(Node node) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.nodeStatusChangeEvent(node);
            }
        }
    }

    public void notifyNodeLocationChange(Node node) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.nodeLocationChangeEvent(node);
            }
        }
    }

    /*************************************************
     * Methods dealing with links
     ************************************************/
    /**
     * Notify link add
     *
     * @param link
     */
    public void notifyAddLink(Link link) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.addLinkEvent(link);
            }
        }
    }
    /**
     * Notify link remove
     *
     * @param link
     */
    public void notifyRemoveLink(Link link){
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.removeLinkEvent(link);
            }
        }
    }

    /**
     * Notify link status change
     *
     * @param link
     */
    public void notifyLinkStatusChange(Link link) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.linkStatusChangeEvent(link);
            }
        }
    }

    /**
     * Notify link metric change
     *
     * @param link
     */
    public void notifyLinkMetricChange(Link link) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.linkMetricChangeEvent(link);
            }
        }
    }

    /**
     * Notify link TE metric change
     *
     * @param link
     */
    public void notifyLinkTeMetricChange(Link link) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.linkTeMetricChangeEvent(link);
            }
        }
    }

    /**
     * Notify link bandwidth change
     *
     * @param link
     */
    public void notifyLinkBandwidthChange(Link link) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.linkBandwidthChangeEvent(link);
            }
        }
    }

    /**
     * Notify link reserved bandwidth change
     *
     * @param link
     */
    public void notifyLinkReservedBandwidthChange(Link link) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.linkReservedBandwidthChangeEvent(link);
            }
        }
    }

    /**
     * Notify link delay change
     *
     * @param link
     */
    public void notifyLinkDelayChange(Link link) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.linkDelayChangeEvent(link);
            }
        }
    }

    /*************************************************
     * Methods dealing with LSPs
     ************************************************/
    /**
     * Notify LSP add
     *
     * @param lsp
     */
    public void notifyAddLsp(Lsp lsp) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.addLspEvent(lsp);
            }
        }
    }

    /**
     * Notify LSP remove
     *
     * @param lsp
     */
    public void notifyRemoveLsp(Lsp lsp) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.removeLspEvent(lsp);
            }
        }
    }

    /**
     * Notify LSP reservation change
     *
     * @param lsp
     */
    public void notifyLspReservationChange(Lsp lsp) {
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.lspReservationChangeEvent(lsp);
            }
        }
    }

    public void notifyLspStatusChange(Lsp lsp) {
        logger.debug("Lsp Status change for lsp " + lsp.getId());
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            logger.debug("Notifying a change in lsp status for lsp: " + lsp.getId());
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.lspStatusChangeEvent(lsp);
            }
        }
    }

    /**
     * Notify a change in the working path
     * @param lsp
     */
    public void notifyLspWorkingPathChange(Lsp lsp) {
        logger.debug("Lsp Working path change for lsp " + lsp.getId());
        for(int j = listeners.length-1; j >= 0; --j) {
            if(listeners[j] == null) {
                continue;
            }
            logger.debug("Notifying a change in lsp routing path for lsp: " + lsp.getId());
            for (Iterator<DomainChangeListener> iter = listeners[j].iterator(); iter.hasNext();) {
                DomainChangeListener element = iter.next();
                element.lspWorkingPathChangeEvent(lsp);
            }
        }
    }

}
