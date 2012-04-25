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
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.LoadTrafficMatrixImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.facade.ScenarioExecutionContext;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;

/*
 * Changes:
 * --------
 * - 09-Jan-2007: use scenario context for file name (GMO)
 *
 */

/**
 * This class implements an event which loads a given traffic matrix.
 *
 * <p>Creation date: 03-d�c.-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class LoadTrafficMatrix extends LoadTrafficMatrixImpl implements Event {

    private static final Logger logger = Logger.getLogger(LoadTrafficMatrix.class);
    
    public LoadTrafficMatrix() {}
    
    public LoadTrafficMatrix(String fileName) {
        setFile(fileName);
    }
    
    public LoadTrafficMatrix(int tmId, String fileName) {
        this(fileName);
        setTMID(tmId);
    }
    
    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a load demand matrix event - TMID: "+_TMID+" - File: "+_File);
        TrafficMatrix tm;
        String ff = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), _File);
        try {
            if(isSetTMID()) {
                tm = TrafficMatrixManager.getInstance().loadTrafficMatrix(ff, _TMID, false);
            }
            else {
                tm = TrafficMatrixManager.getInstance().loadTrafficMatrix(ff);
            }
        } catch (TrafficMatrixAlreadyExistException e) {
            logger.error("There is already a traffic matrix with the same TM ID for this AS ID");
            throw new EventExecutionException(e);
        } catch (InvalidDomainException e) {
            logger.error("The specified domain was not found.");
            throw new EventExecutionException(e);
        } catch (NodeNotFoundException e) {
            logger.error("Unknown node in the traffic matrix. Exception message: "+e.getMessage());
            throw new EventExecutionException(e);
        } catch (InvalidTrafficMatrixException e) {
            logger.error("Unable to load Traffic Matrix: " + ff);
            throw new EventExecutionException(e);
        }
        logger.info("Traffic Matrix loaded.");
        return new EventResult(tm);
    }
}
