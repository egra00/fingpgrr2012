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

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.EnableTrafficSwitchingImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.FastRerouteSwitchingMethod;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 25/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class EnableTrafficSwitching extends EnableTrafficSwitchingImpl implements Event {
    private final static Logger logger = Logger.getLogger(EnableTrafficSwitching.class);

    public EnableTrafficSwitching() {
    }

    public EnableTrafficSwitching(int asId) {
        setASID(asId);
    }

    public EventResult action() throws EventExecutionException {
        logger.debug("Processing an EnableTrafficSwitching event. ASID: "+getASID());

        Domain domain;
        if(!isSetASID()) {
            domain = InterDomainManager.getInstance().getDefaultDomain();
            if(domain == null) {
                logger.error("There is no default domain!");
                throw new EventExecutionException("No default domain.");
            }
        } else {
            try {
                domain = InterDomainManager.getInstance().getDomain(getASID());
            } catch (InvalidDomainException e) {
                logger.error("Unknown domain "+getASID());
                throw new EventExecutionException(e);
            }
        }

        domain.setSwitchingMethod(new FastRerouteSwitchingMethod(domain));

        return new EventResult(null, "FastRerouteSwitchingMethod started on domain " + domain.getASID());
    }
}
