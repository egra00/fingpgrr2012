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

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.LoadDistantDomainImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.persistence.DomainFactory;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.DomainAlreadyExistException;

import java.io.IOException;

/*
* Changes:
* --------
*
*/

/**
* Load a domain from network. The toolbox will act as a client, connects to a server on specified host
* and waits for the XML text correponding to the topology. The port on which to connect can be specified. If not, the
* default port defined in the {@link DomainFactory#DEFAULT_PORT} constant will be used.
*
* <p>Creation date: 3 oct. 2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LoadDistantDomain extends LoadDistantDomainImpl implements Event {

    public LoadDistantDomain() {}

    public LoadDistantDomain(String host, int port) {
        setHost(host);
        setPort(port);
    }

    public LoadDistantDomain(String host) {
        setHost(host);
    }


    /**
     * This method must be implemented by each event. This method contains what must be done to
     * process the event.
     */
    public EventResult action() throws EventExecutionException {

        Domain domain;

        try {
            if (isSetPort()) {
                domain = InterDomainManager.getInstance().loadDomain(getHost(), getPort(), true, true, true);
            } else {
                domain = InterDomainManager.getInstance().loadDomain(getHost(), DomainFactory.DEFAULT_PORT, true, true, true);
            }
        } catch (InvalidDomainException e) {
            //e.printStackTrace();
            throw new EventExecutionException(e);
        } catch (IOException e) {
            //e.printStackTrace();
            throw new EventExecutionException(e);
        } catch (DomainAlreadyExistException e) {
            //e.printStackTrace();
            throw new EventExecutionException(e);
        }

        return new EventResult(domain, "Domain loaded from host: " + getHost());
    }
}
