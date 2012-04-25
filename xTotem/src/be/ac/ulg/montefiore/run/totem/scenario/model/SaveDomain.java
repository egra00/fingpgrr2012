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
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.SaveDomainImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.facade.ScenarioExecutionContext;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;

/*
 * Changes:
 * --------
 * - 09-Jan-2007: use scenario context for file name (GMO)
 */

/**
 * This class implements a save domain event.
 *
 * <p>Creation date: 03-f�vr.-2005
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class SaveDomain extends SaveDomainImpl implements Event {

    private static final Logger logger = Logger.getLogger(SaveDomain.class);
    
    public SaveDomain() {}
    
    public SaveDomain(String fileName) {
        setFile(fileName);
    }
    
    public SaveDomain(String fileName, int asId) {
        this(fileName);
        setASID(asId);
    }
    
    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a save domain event - File: "+_File+" - ASID: "+_ASID);

        int asId;
        if(!isSetASID()) {
            asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
        }
        else {
            asId = _ASID;
        }
        
        try {
            String ff = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), _File);
            InterDomainManager.getInstance().saveDomain(asId, ff);
        } catch (InvalidDomainException e) {
            logger.error("Unknown domain "+asId);
            throw new EventExecutionException(e);
        }
        logger.info("Domain saved.");
        return new EventResult();
    }

}
