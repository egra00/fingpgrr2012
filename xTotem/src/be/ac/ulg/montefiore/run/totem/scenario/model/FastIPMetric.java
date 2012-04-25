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
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

import be.ac.ucl.poms.repository.FastIPMetricGeneration.FastIPMetricGeneration;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.FastIPMetricImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.facade.ScenarioExecutionContext;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemActionList;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemAction;
import be.ac.ucl.poms.repository.model.*;
import be.ac.ucl.poms.repository.model.UpdateIGPWeightsAction;

/**
 * This class implements an event that generates heuristic weights for IGP routing
 * and prints the result
 * <p>Creation date: 20-March-2008
 *
 * @author  Hakan Umit (hakan.umit@uclouvain.be)
 */
public class FastIPMetric extends FastIPMetricImpl implements Event {

    private static final Logger logger = Logger.getLogger(FastIPMetric.class);
    
    public FastIPMetric() {}
     
    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.info("Processing a compute MCF event.");
        FastIPMetricGeneration mcf; 
        try {
        if ((!this.isSetDataFile()) && (!this.isSetResultFile()))  {
            mcf = new FastIPMetricGeneration();
        } else {
            String dataFile = "mcf1.dat";
            String resultFile = "mcf1.out";
            if (this.isSetDataFile())
                dataFile = this.getDataFile();
            if (this.isSetResultFile())
                resultFile = this.getResultFile();
            String newDataFile = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), dataFile);
            String newResultFile = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), resultFile);
            mcf = new FastIPMetricGeneration(newDataFile,newResultFile);
        }
            Domain domain;
            TrafficMatrix tm;
            if ((this.isSetASID()) && (this.isSetTMID())) {
                domain = InterDomainManager.getInstance().getDomain(this.getASID());
                tm = TrafficMatrixManager.getInstance().getTrafficMatrix(this.getASID(), this.getTMID());
            } else if ((this.isSetASID()) && (!this.isSetTMID())) {
                domain = InterDomainManager.getInstance().getDomain(this.getASID());
                tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(this.getASID());
            } else if ((!this.isSetASID()) && (this.isSetTMID())) {
                domain = InterDomainManager.getInstance().getDefaultDomain();;
                tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(), this.getTMID());
            } else {
                domain = InterDomainManager.getInstance().getDefaultDomain();
                tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix();
            }
            if ((this.isRunGLPSOL()) && (this._RunGLPSOL)) {
		TotemActionList actionList = mcf.computeLP(domain, tm);
		double[] newLinkWeights = null;
		for (Iterator iter = actionList.iterator(); iter.hasNext();) {
		    TotemAction action = (TotemAction) iter.next();
		    if (action instanceof UpdateIGPWeightsAction) {
			newLinkWeights = ((UpdateIGPWeightsAction)action).getWeights();
		    }
		    action.execute();
		}
		return new EventResult(newLinkWeights);
	    } else {
                System.out.println("Only create the data file");
                mcf.createUtilDataFile(domain,tm);
                EventResult er = new EventResult();
                er.setMessage("Data file created.");
                return er;
            }
        } catch (InvalidDomainException e) {
            throw new EventExecutionException(e);
        } catch (InvalidTrafficMatrixException e) {
            throw new EventExecutionException(e);
        } catch (IOException e) {
            throw new EventExecutionException(e);
        } catch (LinkNotFoundException e) {
            throw new EventExecutionException(e);
        } catch (NodeNotFoundException e) {
            throw new EventExecutionException(e);
        } catch(Exception e) {
            logger.error("An exception occurred. Message: "+e.getMessage());
            throw new EventExecutionException(e);
        }
    }

}
