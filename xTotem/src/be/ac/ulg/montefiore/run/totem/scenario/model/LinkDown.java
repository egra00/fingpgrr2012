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

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.StatusTypeException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Topology;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.LinkDownImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

import javax.xml.bind.JAXBException;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class implements a link down event.
 *
 * <p>Creation date: 01-d�c.-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */

public class LinkDown extends LinkDownImpl implements Event {

    private final static Logger logger = Logger.getLogger(LinkDown.class);
    
    public LinkDown() {}
    
    public LinkDown(String linkId) {
        setLinkId(linkId);
    }
    
    public LinkDown(int asId, String linkId) {
        this(linkId);
        setASID(asId);
    }
    
    public LinkDown(int asId, String linkId, String cause) {
        this(asId, linkId);
        setCause(cause);
    }
    
    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a link down event - ASID: "+_ASID+" - Link ID: "+_LinkId+" - Cause: "+_Cause);
        
        try {
            Domain domain;
            if(isSetASID()) {
                domain = InterDomainManager.getInstance().getDomain(_ASID);
            }
            else {
                domain = InterDomainManager.getInstance().getDefaultDomain();
            }
            Link link = domain.getLink(_LinkId);
            link.setLinkStatus(Link.STATUS_DOWN);

            logger.info("Link " + link.getId() + " status set to down.");
            return new EventResult(link);
        }
        catch(LinkNotFoundException e) {
            logger.error("Unknown link "+_LinkId);
            throw new EventExecutionException(e);
        }
        catch(InvalidDomainException e) {
            logger.error("Unknown domain "+_ASID);
            throw new EventExecutionException(e);
        }
        catch(StatusTypeException e) {
            logger.error("Weird StatusTypeException !");
            throw new EventExecutionException(e);
        }
    }
}
