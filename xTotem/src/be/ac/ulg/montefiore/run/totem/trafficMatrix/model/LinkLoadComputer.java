
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
package be.ac.ulg.montefiore.run.totem.trafficMatrix.model;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;

import java.util.List;

/*
* Changes:
* --------
* - 22-Mar-2006: Add Id attributes with get, set, unset and isset accessors (GMO).
* - 06-Apr-2007: add addListener(.) and removeListener(.) (GMO)
* - 27-Feb-2008: rewrite interface (GMO)
*/

/**
 * The classes that implements LinkLoadComputer calculates the links load and utilization for a given domain, given some
 * traffic matrices. The calculated data are in the form of a {@link LoadData} object.
 * The LinkLoadComputer can listen to events (from the domain or from the traffic matrices) to maintain the correct state
 * (up-to-date, outdated) of its data. The data can be updated by calling {@link #update()} or {@link #recompute()}.
 *
 * Some listeners can also be attached to the LinkLoadComputer, signaling a change in the validity of the data
 * (returned by {@link #getData()}.
 *
 * <p>Creation date: 10 mars 2006
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public interface LinkLoadComputer {

    /**
     * Add a listener. Once added, the listener will be notified of changes in the validity of the LinkLoadComputer.
     * @param listener
     */
    public void addListener(LinkLoadComputerListener listener);

    /**
     * Remove a listener.
     * @param listener
     */
    public void removeListener(LinkLoadComputerListener listener);

    /**
     * Returns true if the internal data are up-to-date
     * @return
     */
    public boolean isUpToDate();

    /**
     * Recomputes the load data.
     */
    public void recompute();

    /**
     * Updates the data if the data is outdated.
     */
    public void update();

    /**
     * Call this method to signal the llc that its data are no more up-to-date
     */
    public void invalidate();

    /**
     * Returns the domain associated.
     * @return
     */
    public Domain getDomain();

    /**
     * TODO: maybe change this to a set
     * Returns the traffic matrix.
     * @return
     */
    public List<TrafficMatrix> getTrafficMatrices();

    /**
     * Free resources associated with the object and stop listening to events.
     */
    public void destroy();

    /**
     * Start listening to changes to keep data up-to-date
     */
    public void startListening();

    /**
     * Stop listening to changes in the data
     */
    public void stopListening();

    /**
     * Return a string representing the parameters in short.
     * @return
     */
    public String getShortName();

    /**
     * Returns a view of the data. The returned data are still used internally.
     * @return
     */
    public LoadData getData();

    /**
     * Returns the data used by the link load computer and do not use it internally any more. After the data has been
     * detached, the load has to be computed again by a call to {@link #recompute()}.
     * @return
     */
    public LoadData detachData();

    /**
     * returns true if the LinkLoadComputer uses the same parameters as the object o. (the computed data at a given
     * moment are equivalent)
     * @param o
     * @return
     */
    public boolean equals(Object o);

    public int hashCode();
}
