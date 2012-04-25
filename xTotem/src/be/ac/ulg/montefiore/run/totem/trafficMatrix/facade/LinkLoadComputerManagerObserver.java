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
package be.ac.ulg.montefiore.run.totem.trafficMatrix.facade;

import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;

import java.util.List;
import java.util.ArrayList;

/*
* Changes:
* --------
*/

/**
* This class is used to notify all listeners of changes in the LinkLoadComputerManager
* (i.e. LinkLoadComputer added, LinkLoadComputer removed, change of default LinkLoadComputer)
*
* <p>Creation date: 9 janv. 2006
*
* @see LinkLoadComputerManagerListener
* @see LinkLoadComputerManager
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LinkLoadComputerManagerObserver {

    private List<LinkLoadComputerManagerListener> listeners;

    public LinkLoadComputerManagerObserver() {
        listeners = new ArrayList<LinkLoadComputerManagerListener>();
    }

    /**
     * Add a listener to the list of listeners. This method does nothing if the listener is already registered.
     * @param l the listener to add
     */
    public void addListener(LinkLoadComputerManagerListener l) {
        if (listeners.contains(l)) return;
        listeners.add(l);
    }

    /**
     * Remove a listener from the list of registered listeners. This method has no effect if the listener is not registered.
     * @param l
     */
    public void removeListener(LinkLoadComputerManagerListener l) {
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
     * A LinkLoadComputer has been added to the manager
     * @param llc
     */
    protected void notifyAddLinkLoadComputer(LinkLoadComputer llc) {
        for (LinkLoadComputerManagerListener l : listeners) {
            l.addLinkLoadComputerEvent(llc);
        }
    }

    /**
     * A LinkLoadComputer has been removed from the manager
     * @param llc a reference to the removed LinkLoadComputer
     */
    protected void notifyRemoveLinkLoadComputer(LinkLoadComputer llc) {
        for (LinkLoadComputerManagerListener l : listeners) {
            l.removeLinkLoadComputerEvent(llc);
        }
    }

    /**
     * One or more LinkLoadComputer has been removed from the manager
     */
    protected void notifyRemoveMultipleLinkLoadComputer() {
        for (LinkLoadComputerManagerListener l : listeners) {
            l.removeMultipleLinkLoadComputerEvent();
        }
    }

    /**
     * The default LinkLoadComputer has changed for the domain given by its asId.
     * @param asId Asid of the domain for which the LinkLoadComputer has changed
     * @param llc The new default LinkLoadComputer for the domain
     */
    protected void notifyChangeDefaultLinkLoadComputer(int asId, LinkLoadComputer llc) {
        for (LinkLoadComputerManagerListener l : listeners) {
            l.changeDefaultLinkLoadComputerEvent(asId, llc);
        }
    }

}
