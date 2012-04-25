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
package at.ftw.scenario.model;

import java.io.IOException;

import org.apache.log4j.Logger;

import at.ftw.repository.reopt.LSPGeneration;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkCapacityExceededException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.model.SetLspReservationAction;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemActionList;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.CreatePathException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.TotemActionExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.model.Event;
import be.ac.ulg.montefiore.run.totem.scenario.model.EventResult;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import at.ftw.scenario.model.jaxb.impl.FTWReoptImpl;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class calls the Reopt (FTW, Vienna) algorithm.
 *
 * <p>Creation date: 09-mai-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class FTWReopt extends FTWReoptImpl implements Event {

    private static final Logger logger = Logger.getLogger(FTWReopt.class);
    
    public FTWReopt() {}
    
    public FTWReopt(int asId) {
        setASID(asId);
    }
    
    public FTWReopt(int asId, int tmId) {
        this(asId);
        setTMID(tmId);
    }

    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a FTWReopt event.");
        
        Domain domain;
        if(isSetASID()) {
            try {
                domain = InterDomainManager.getInstance().getDomain(getASID());
            } catch (InvalidDomainException e) {
                logger.error("Domain "+getASID()+" unknown!");
                throw new EventExecutionException(e);
            }
        } else {
            domain = InterDomainManager.getInstance().getDefaultDomain();
            if(domain == null) {
                logger.error("No default domain!");
                throw new EventExecutionException("No default domain!");
            }
        }
        
        TrafficMatrix tm;
        if(isSetTMID()) {
            try {
                tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(), getTMID());
            } catch(InvalidTrafficMatrixException e) {
                logger.error("Traffic matrix "+getTMID()+" unknown!");
                throw new EventExecutionException(e);
            }
        } else {
            try {
                tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(domain.getASID());
            } catch (InvalidTrafficMatrixException e) {
                logger.error("No default traffic matrix for the domain "+getASID()+"!");
                throw new EventExecutionException(e);
            }
        }
        
        int profit = isSetProfit() ? getProfit() : 1;
        int changeCost = isSetChangeCost() ? getChangeCost() : 10;
        boolean runSolver = isSetRunSolver() ? isRunSolver() : true;
        boolean applyChanges = isSetApplyChanges() ? isApplyChanges() : true;
        
        at.ftw.repository.reopt.FTWReopt ftwReopt = new at.ftw.repository.reopt.FTWReopt();
        try {
            StringBuffer sb = new StringBuffer();
            if(isSetLSPGeneration()) {
                LSPGeneration lspGen = new LSPGeneration();
                lspGen.generateLSP(domain.getASID(), getLSPGeneration().getNbParallelPaths());
                if(runSolver) {
                    TotemActionList<SetLspReservationAction> actionList = ftwReopt.calculateInitialSolution(domain, tm, profit, changeCost);
                    for (SetLspReservationAction action : actionList) {
                        sb.append("Initial reservation for LSP "+action.getLsp().getId()+": "+action.getReservation());
                        sb.append("\n");
                        if (applyChanges) action.execute();
                    }
                } else {
                    ftwReopt.createDataFile(domain, tm, profit, changeCost, true);
                    sb.append("Data file created.\n");
                }
            } else {
                if(runSolver) {
                    TotemActionList<SetLspReservationAction> actionList = ftwReopt.reopt(domain, tm, profit, changeCost);
                    for (SetLspReservationAction action : actionList) {
                        sb.append("New reservation for LSP "+action.getLsp().getId()+": "+action.getReservation());
                        sb.append("\n");
                        if (applyChanges) action.execute();
                    }
                } else {
                    ftwReopt.createDataFile(domain, tm, profit, changeCost, false);
                    sb.append("Data file created.\n");
                }
            }
            logger.info(sb.toString());
            return new EventResult(null, sb.toString());
        } catch (InvalidDomainException e) {
            logger.error("Weird InvalidDomainException! Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (CloneNotSupportedException e) {
            logger.error("Weird CloneNotSupportedException! Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (CreatePathException e) {
            logger.error("CreatePathException. Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (LinkNotFoundException e) {
            logger.error("Weird LinkNotFoundException! Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (NodeNotFoundException e) {
            logger.error("Weird NodeNotFoundException! Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (RoutingException e) {
            logger.error("RoutingException! Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (LspAlreadyExistException e) {
            logger.error("Weird LspAlreadyExistException! Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (LinkCapacityExceededException e) {
            logger.error("LinkCapacityExceededException! Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (IOException e) {
            logger.error("IOException! Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (TotemActionExecutionException e) {
            logger.error("TotemActionExecutionException! Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        }
    }
}
