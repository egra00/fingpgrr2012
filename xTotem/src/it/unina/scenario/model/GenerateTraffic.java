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

import it.unina.scenario.model.jaxb.impl.GenerateTrafficImpl;
import it.unina.traffic.TrafficGenerator;
import be.ac.ulg.montefiore.run.totem.scenario.model.Event;
import be.ac.ulg.montefiore.run.totem.scenario.model.EventResult;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Param;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;

import java.util.HashMap;

/*
* Changes:
* --------
*
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 5 oct. 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class GenerateTraffic extends GenerateTrafficImpl implements Event {
    /**
     * This method must be implemented by each event. This method contains what must be done to
     * process the event.
     */
    public EventResult action() throws EventExecutionException {

        HashMap<String, String> params = new HashMap<String, String>();
        for(Object o : getParam()) {
            Param param = (Param) o;
            params.put(param.getName(), param.getValue());
        }

        Domain domain;
        TrafficMatrix tm;

        try {
            if (isSetASID()) {
                domain = InterDomainManager.getInstance().getDomain(getASID());
            } else {
                domain = InterDomainManager.getInstance().getDefaultDomain();
            }

            if (isSetTMID()) {
                tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(), getTMID());
            } else {
                tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(domain.getASID());
            }
        } catch (InvalidDomainException e) {
            throw new EventExecutionException(e);
        } catch (InvalidTrafficMatrixException e) {
            throw new EventExecutionException(e);
        }

        (new TrafficGenerator()).sendTraffic(domain, tm, params);

        return new EventResult(null, "Traffic sent to hosts");
    }
}
