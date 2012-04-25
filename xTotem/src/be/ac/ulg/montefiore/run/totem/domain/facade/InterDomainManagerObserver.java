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
package be.ac.ulg.montefiore.run.totem.domain.facade;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;

import java.util.List;
import java.util.ArrayList;

/*
* Changes:
* --------
*
*
*/

/**
* This class is used to notify all listeners of changes in the InterDomainManager (i.e. domain added, domain removed, default domain changed)
*
* <p>Creation date: 9 janv. 2006
*
* @see InterDomainManager
* @see InterDomainManagerListener
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class InterDomainManagerObserver {

    private List<InterDomainManagerListener> listeners;

    public InterDomainManagerObserver() {
        listeners = new ArrayList<InterDomainManagerListener>();
    }

    /**
     * Add a listener to the list of listeners. This method does nothing if the listener is already registered.
     * @param l the listener to add
     */
    public void addListener(InterDomainManagerListener l) {
        if (listeners.contains(l)) return;
        listeners.add(l);
    }

    /**
     * Remove a listener from the list of registered listeners. This method has no effect if the listener is not registered.
     * @param l
     */
    public void removeListener(InterDomainManagerListener l) {
        listeners.remove(l);
    }

    /**
     * return the number of registered listeners
     * @return
     */
    public int getNbListeners() {
        return listeners.size();
    }

    /**
     * A new domain added.
     * @param domain The new domain.
     */
    protected void notifyAddDomain(Domain domain) {
        for (InterDomainManagerListener l : listeners) {
            l.addDomainEvent(domain);
        }
    }

    /**
     * A domain removed.
     * @param domain A reference to the domain removed.
     */
    protected void notifyRemoveDomain(Domain domain) {
        for (InterDomainManagerListener l : listeners) {
            l.removeDomainEvent(domain);
        }
    }

    /**
     * The default domain has changed for the specified domain.
     * @param domain the new default domain.
     */
    protected void notifyChangeDefaultDomain(Domain domain) {
        for (InterDomainManagerListener l : listeners) {
            l.changeDefaultDomainEvent(domain);
        }
    }

}
