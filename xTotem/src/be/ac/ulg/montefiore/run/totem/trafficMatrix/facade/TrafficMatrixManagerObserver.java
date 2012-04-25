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

import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;

import java.util.List;
import java.util.ArrayList;

/*
* Changes:
* --------
* 03-Feb.-2006 : add tmId parameter to notifyAddTrafficMatrix (GMO).
* 20-Mar.-2006 : add tmId parameter to notifyRemoveTrafficMatrix (GMO).
*/

/**
* This class is used to notify all listeners of changes in the TrafficMatrixManager
* (i.e. trafficMatrix added, traffic matrix removed, change of default traffic matrix)
*
* <p>Creation date: 9 janv. 2006
*
* @see TrafficMatrixManagerListener
* @see TrafficMatrixManager
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class TrafficMatrixManagerObserver {

    private List<TrafficMatrixManagerListener> listeners;

    public TrafficMatrixManagerObserver() {
        listeners = new ArrayList<TrafficMatrixManagerListener>();
    }

    /**
     * Add a listener to the list of listeners. This method does nothing if the listener is already registered.
     * @param l the listener to add
     */
    public void addListener(TrafficMatrixManagerListener l) {
        if (listeners.contains(l)) return;
        listeners.add(l);
    }

    /**
     * Remove a listener from the list of registered listeners. This method has no effect if the listener is not registered.
     * @param l
     */
    public void removeListener(TrafficMatrixManagerListener l) {
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
     * A TrafficMatrix has been loaded
     * @param tm the new loaded traffic matrix
     * @param tmId the new loaded traffic matrix id
     */
    protected void notifyAddTrafficMatrix(TrafficMatrix tm, int tmId) {
        for (TrafficMatrixManagerListener l : listeners) {
            l.addTrafficMatrixEvent(tm, tmId);
        }
    }

    /**
     * A traffic matrix has been removed
     * @param tm a reference to the removed traffic Matrix
     */
    protected void notifyRemoveTrafficMatrix(TrafficMatrix tm, int tmId) {
        for (TrafficMatrixManagerListener l : listeners) {
            l.removeTrafficMatrixEvent(tm, tmId);
        }
    }

    /**
     * The default traffic matrix has changed for the domain given by its asId.
     * @param asId Asid of the domain for which the traffic matrix has changed
     * @param tm The new default traffic matrix for the domain
     */
    protected void notifyChangeDefaultTrafficMatrix(int asId, TrafficMatrix tm) {
        for (TrafficMatrixManagerListener l : listeners) {
            l.changeDefaultTrafficMatrixEvent(asId, tm);
        }
    }

}
