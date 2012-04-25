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

/*
* Changes:
* --------
*
*/

/**
 * Define the events that a listener will receive.
 *
 * <p>Creation date: 9 janv. 2006
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 *
 * @see LinkLoadComputerManagerObserver
 * @see LinkLoadComputerManager
 */
public interface LinkLoadComputerManagerListener {

    /**
     * A LinkLoadComputer has been added to the manager
     * @param llc
     */
    void addLinkLoadComputerEvent(LinkLoadComputer llc);

    /**
     * A LinkLoadComputer has been removed from the manager
     * @param llc a reference to the removed LinkLoadComputer
     */
    void removeLinkLoadComputerEvent(LinkLoadComputer llc);
    
    /**
     * One or more LinkLoadComputer has been removed from the manager
     */
    void removeMultipleLinkLoadComputerEvent();

    /**
     * The default LinkLoadComputer has changed for the domain given by its asId.
     * @param asId Asid of the domain for which the LinkLoadComputer has changed
     * @param llc The new default LinkLoadComputer for the domain
     */
    void changeDefaultLinkLoadComputerEvent(int asId, LinkLoadComputer llc);

}
