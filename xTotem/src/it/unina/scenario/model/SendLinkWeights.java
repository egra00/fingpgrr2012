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
package it.unina.scenario.model;

import it.unina.scenario.model.jaxb.impl.SendLinkWeightsImpl;
import it.unina.traffic.MultipleUDPLinkWeightsSender;
import it.unina.traffic.LinkWeightsSender;
import be.ac.ulg.montefiore.run.totem.scenario.model.Event;
import be.ac.ulg.montefiore.run.totem.scenario.model.EventResult;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;


/*
* Changes:
* --------
*
*/

/**
* Scenario event that send link weigths to hosts in the current network.
*
* <p>Creation date: 24/10/2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class SendLinkWeights extends SendLinkWeightsImpl implements Event {
    /**
     * This method must be implemented by each event. This method contains what must be done to
     * process the event.
     */
    public EventResult action() throws EventExecutionException {

        Domain domain;
        try {
            if (isSetASID()) {
                domain = InterDomainManager.getInstance().getDomain(getASID());
            } else {
                domain = InterDomainManager.getInstance().getDefaultDomain();
            }
        } catch (InvalidDomainException e) {
            throw new EventExecutionException(e);
        }

        LinkWeightsSender lws = new MultipleUDPLinkWeightsSender(domain);
        lws.sendLinkWeights();

        return new EventResult(null, "Link weights sent. Check log for possible errors.");
    }
}
