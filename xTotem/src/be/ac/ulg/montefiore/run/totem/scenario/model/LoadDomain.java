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

import be.ac.ulg.montefiore.run.totem.domain.exception.DomainAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.LoadDomainImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.facade.ScenarioExecutionContext;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;

/*
 * Changes:
 * --------
 *
 * - 16-Feb.-2005: Add the useBWSharing attribute (JL).
 * - 09-Jan-2007: use scenario context for file name (GMO) 
 *
 */

/**
 * This class implements an event which loads a given domain.
 *
 * <p>Creation date: 03-d�c.-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class LoadDomain extends LoadDomainImpl implements Event {

    private final static Logger logger = Logger.getLogger(LoadDomain.class);
    
    public LoadDomain() {}
    
    public LoadDomain(String fileName) {
        setFile(fileName);
    }
    
    public LoadDomain(String fileName, boolean removeMultipleLinks) {
        this(fileName);
        setRemoveMultipleLinks(removeMultipleLinks);        
    }
        
    public LoadDomain(String fileName, boolean removeMultipleLinks, boolean isDefaultDomain) {
        this(fileName, removeMultipleLinks);
        setDefaultDomain(isDefaultDomain);
    }
    
    public LoadDomain(String fileName, boolean removeMultipleLinks, boolean isDefaultDomain, boolean useBWSharing) {
        this(fileName, removeMultipleLinks, isDefaultDomain);
        setUseBWSharing(useBWSharing);
    }
    
    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.info("Processing a load topology event - File: "+_File);
        Domain domain;
        try {
            String filePath = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), _File);

            if(isSetDefaultDomain()) {
                if(isSetUseBWSharing()) {
                    domain = InterDomainManager.getInstance().loadDomain(filePath, _DefaultDomain, _RemoveMultipleLinks, _UseBWSharing);
                }
                else {
                    domain = InterDomainManager.getInstance().loadDomain(filePath, _DefaultDomain, _RemoveMultipleLinks, false);
                }
            }
            else {
                if(isSetUseBWSharing()) {
                    domain = InterDomainManager.getInstance().loadDomain(filePath, false, _RemoveMultipleLinks, _UseBWSharing);
                }
                else {
                    domain = InterDomainManager.getInstance().loadDomain(filePath, false, _RemoveMultipleLinks, false);
                }
            }
        }
        catch(DomainAlreadyExistException e) {
            logger.error("There is already a domain with the same AS ID.");
            throw new EventExecutionException(e);
        } catch (InvalidDomainException e) {
            logger.error("The domain is invalid.");
            throw new EventExecutionException(e);
        }
        logger.info("Domain " + domain.getASID() + " loaded.");
        return new EventResult(domain);
    }
}
