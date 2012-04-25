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

import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.StopAlgoImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class implements a stop algorithm event.
 *
 * <p>Creation date: 22-mars-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class StopAlgo extends StopAlgoImpl implements Event {

    private static final Logger logger = Logger.getLogger(StopAlgo.class);
    
    public StopAlgo() {}
    
    public StopAlgo(String algoName) {
        setName(algoName);
    }
    
    public StopAlgo(String algoName, int asId) {
        this(algoName);
        setASID(asId);
    }
    
    public StopAlgo(String algoName, int asId, int tmId) {
        this(algoName, asId);
        setTMID(tmId);
    }

    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a stop algorithm event. Name: "+_Name);
        
        logger.warn("Please consider using the observer pattern instead of this ugly hack...");
        
        try {
            if(this.isSetASID() && this.isSetTMID()) {
                RepositoryManager.getInstance().stopAlgorithm(this.getName(), this.getASID(), this.getTMID());
            }
            else if(this.isSetASID()) {
                RepositoryManager.getInstance().stopAlgorithm(this.getName(), this.getASID());
            }
            else if(this.isSetTMID()) {
                int asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
                RepositoryManager.getInstance().stopAlgorithm(this.getName(), asId, this.getTMID());
            }
            else {
                RepositoryManager.getInstance().stopAlgorithm(this.getName());
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error while trying to stop the algorithm "+this.getName()+". Message: "+e.getMessage());
            throw new EventExecutionException(e);
        } catch(NullPointerException e) {
            logger.error("No default domain!");
            throw new EventExecutionException("No default domain!", e);
        }
        logger.info("Algorithm " + _Name + " stoppped.");
        return new EventResult();
    }

}
