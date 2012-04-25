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

import java.io.IOException;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.repository.MultiCommodityFlow.MultiCommodityFlow;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.ComputeMCFImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.facade.ScenarioExecutionContext;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.LinkLoadComputerIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.LinkLoadComputerAlreadyExistsException;

/*
 * Changes:
 * --------
 * 09-Jan-2007: use scenario context for file name (GMO)
 */

/**
 * This class implements an event that starts the MCF algorithm. The result can be printed by using the 
 * {@link ShowLinkLoad} event.
 *
 * <p>Creation date: 03-déc.-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author  Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class ComputeMCF extends ComputeMCFImpl implements Event {

    private static final Logger logger = Logger.getLogger(ComputeMCF.class);
    
    public ComputeMCF() {}

    public ComputeMCF(int asId, String llcId) {
        setASID(asId);
        setLlcId(llcId);
    }

    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a compute MCF event.");

        Domain domain;
        TrafficMatrix tm;

        if(isSetASID()) {
            try {
                domain = InterDomainManager.getInstance().getDomain(getASID());
            } catch (InvalidDomainException e) {
                logger.error("Unknown domain "+getASID());
                throw new EventExecutionException(e);
            }
        } else {
            domain = InterDomainManager.getInstance().getDefaultDomain();
            if(domain == null) {
                logger.error("There is no default domain!");
                throw new EventExecutionException("No default domain.");
            }
        }

        if (this.isSetTMID()) {
            try {
                tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(), getTMID());
            } catch (InvalidTrafficMatrixException e) {
                e.printStackTrace();
                throw new EventExecutionException(e);
            }
        } else {
            try {
                tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(domain.getASID());
            } catch (InvalidTrafficMatrixException e) {
                e.printStackTrace();
                throw new EventExecutionException(e);
            }
        }

        try {
            MultiCommodityFlow mcf;

            if ((!this.isSetDataFile()) && (!this.isSetResultFile()))  {
                mcf = new MultiCommodityFlow(domain, tm);
            } else {
                String dataFile = "mcf1.dat";
                String resultFile = "mcf1.out";
                if (this.isSetDataFile())
                    dataFile = this.getDataFile();
                if (this.isSetResultFile())
                    resultFile = this.getResultFile();
                String newDataFile = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), dataFile);
                String newResultFile = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), resultFile);
                mcf = new MultiCommodityFlow(domain, tm, newDataFile,newResultFile);
            }

            if (!this.isSetRunGLPSOL() || isRunGLPSOL()) {
                try {
                    if (isSetLlcId())
                        LinkLoadComputerManager.getInstance().addLinkLoadComputer(mcf, true, getLlcId());
                    else
                        LinkLoadComputerManager.getInstance().addLinkLoadComputer(mcf);
                } catch (LinkLoadComputerIdException e) {
                    throw new EventExecutionException(e);
                } catch (LinkLoadComputerAlreadyExistsException e) {
                    throw new EventExecutionException(e);
                }

                return new EventResult(null, "MCF added.");
            } else {
                System.out.println("Only create the data file");
                String file = mcf.createMCFMinMaxUtilDataFile(domain,tm);
                EventResult er = new EventResult();

                er.setMessage("Data file created in " + file);
                return er;
            }
        } catch (IOException e) {
            throw new EventExecutionException(e);
        } catch (LinkNotFoundException e) {
            throw new EventExecutionException(e);
        } catch (NodeNotFoundException e) {
            throw new EventExecutionException(e);
        }
    }

}
