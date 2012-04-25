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

import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.*;
import be.ac.ulg.montefiore.run.totem.domain.model.*;

import java.util.List;
import java.util.ArrayList;

/*
* Changes:
* --------
*
*/

/**
* Provide default implementation for some functionnalities of the {@link LinkLoadComputer}.
*
* <p>Creation date: 28/01/2008
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public abstract class AbstractLinkLoadComputer implements LinkLoadComputer {
    private List<LinkLoadComputerListener> listeners;
    private boolean upToDate = false;

    /**
     * The associated domain
     */
    protected Domain domain;

    /**
     * The object that listens to events and invalidate the LinkLoadComputer
     */
    protected LinkLoadComputerInvalidator changeListener;

    /**
     * Creates a new LinkLoadComputer with the default change listener ({@link LinkLoadComputerInvalidator}.
     * @param domain
     */
    protected AbstractLinkLoadComputer(Domain domain) {
        this.domain = domain;
        listeners = new ArrayList<LinkLoadComputerListener>();
        this.changeListener = new LinkLoadComputerInvalidator(this);
    }

    /**
     * Creates a new LinkLoadComputer with the given change listener.
     * @param domain
     * @param changeListener
     */
    protected AbstractLinkLoadComputer(Domain domain, LinkLoadComputerInvalidator changeListener) {
        this.domain = domain;
        listeners = new ArrayList<LinkLoadComputerListener>();
        this.changeListener = changeListener;
    }

    /**
     * Add a listener. Once added, the listener will be notified of changes in the validity of the LinkLoadComputer.
     * @param listener
     */
    public void addListener(LinkLoadComputerListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    /**
     * Remove a listener.
     * @param listener
     */
    public void removeListener(LinkLoadComputerListener listener) {
        listeners.remove(listener);
    }

    /**
     * Destroy the linkLoadComputer: remove all listeners, invalidate the data and stop listening to events.
     */
    public void destroy() {
        upToDate = false;
        listeners.clear();
        stopListening();
    }

    /**
     * add the change listener to the domain, and to the currently used traffic matrices
     */
    public void startListening() {
        domain.getObserver().addListener(changeListener);
        for (TrafficMatrix tm : getTrafficMatrices()) {
            tm.getObserver().addListener(changeListener);
        }
    }

    /**
     * Remove the listener from the domain and from the currently used traffic matrices
     */
    public void stopListening() {
        domain.getObserver().removeListener(changeListener);
        for (TrafficMatrix tm : getTrafficMatrices()) {
            tm.getObserver().removeListener(changeListener);
        }
    }

    /**
     * Call {@link #recompute()} if the data is not up-to-date, else do nothing.
     */
    public void update() {
        if (!isUpToDate())
            recompute();
    }

    /**
     * Call this method when data has been recomputed, signals a change in the data.
     */
    protected void dataChanged() {
        setUptoDate(true);
        getData().notifyDataChange();
    }

    public Domain getDomain() {
        return domain;
    }

    public boolean isUpToDate() {
        return upToDate;
    }

    /**
     * If the data validity has changed, also signals the listeners.
     * @param upToDate
     */
    protected void setUptoDate(boolean upToDate) {
        if (this.upToDate != upToDate) {
            this.upToDate = upToDate;
            for (LinkLoadComputerListener listener : listeners) {
                listener.validityChangeEvent(this);
            }
        }
    }

    /**
     * Mark the data as outdated and signals the listeners if the validity has changed.
     */
    public void invalidate() {
        setUptoDate(false);
    }
}
