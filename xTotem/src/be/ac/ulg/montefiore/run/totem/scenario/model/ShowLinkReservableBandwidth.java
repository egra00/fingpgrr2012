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
package be.ac.ulg.montefiore.run.totem.scenario.model;

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.ShowLinkReservableBandwidthImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;

/*
 * Changes:
 * --------
 *
 */

/**
 * Show the reservable bandwidth of a link for a given priority
 *
 * @deprecated Use {@link ShowLinkReservation} instead.
 *
 * <p>Creation date: 4-Feb-2005 18:41:34
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 */
public class ShowLinkReservableBandwidth extends ShowLinkReservableBandwidthImpl implements Event  {
    public EventResult action() throws EventExecutionException {
        Domain domain = null;

        if (this.isSetASID()) {
            try {
                domain = InterDomainManager.getInstance().getDomain(_ASID);
            } catch (InvalidDomainException e) {
                e.printStackTrace();
            }
        } else {
            domain = InterDomainManager.getInstance().getDefaultDomain();
        }

        int priority=0;

        if (isSetPriority()==false){
            priority = domain.getMinPriority();
            System.out.println("Using min priority as priority has not been specified");
        } else{
            if (domain.isExistingPriority(getPriority())==false){
                System.out.println("Error: non existing priority level for showLinkReservableBandwidth scenario event");
                throw new EventExecutionException("non existing priority level for showLinkReservableBandwidth");
            }
            priority = getPriority();

        }

        Link link = null;

        try{
            link = domain.getLink(getLinkId());
        }catch(LinkNotFoundException e){
            System.out.println("Error: non existing link for showLinkReservableBandwidth scenario event");
            throw new EventExecutionException(e);
        }

        EventResult er = new EventResult();
        er.setMessage("Reservable bandwidth for link " + getLinkId() + " at priority " + priority + " : " + link.getReservableBandwidth(priority));
        return er;
    }
}
