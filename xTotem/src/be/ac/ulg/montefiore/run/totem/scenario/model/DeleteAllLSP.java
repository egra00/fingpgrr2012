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
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.DeleteAllLSPImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

/*
 * Changes:
 * --------
 *
 */

/**
 * This event deletes all the LSPs from a domain.
 *
 * <p>Creation date: 29-avr.-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class DeleteAllLSP extends DeleteAllLSPImpl implements Event {

    private static final Logger logger = Logger.getLogger(DeleteAllLSP.class);
    
    public DeleteAllLSP() {}
    
    public DeleteAllLSP(int asId) {
        setASID(asId);
    }

    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a deleteAllLSP event.");
        
        if(isSetASID()) {
            try {
                InterDomainManager.getInstance().getDomain(_ASID).removeAllLsps();
            }
            catch(InvalidDomainException e) {
                logger.error("Unknown domain "+_ASID);
                throw new EventExecutionException(e);
            }
        }
        else {
            try {
                InterDomainManager.getInstance().getDefaultDomain().removeAllLsps();
            }
            catch(NullPointerException e) {
                logger.error("There is no default domain!");
                throw new EventExecutionException(e);
            }
        }
        return new EventResult();
    }
}
