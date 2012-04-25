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

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.IGPWOCalculateWeightsType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.IgpwoInitialWeightArrayType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.IGPWOCalculateWeightsImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemActionList;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemAction;
import be.ac.ucl.poms.repository.IGPWO.IGPWO;
import be.ac.ucl.poms.repository.model.UpdateIGPWeightsAction;
import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 *
 * - 17-Feb.-2005: take the parameters maxPossibleWeight and nbIter into account (JL).
 * - 22-Mar.-2005: bugfix - JAXB doesn't seem to take the default values into account (JL).
 * - 07-Apr.-2005: take multiple traffic matrices into account (JL).
 * - 09-May-2005: log the time to excute IGPWO (FSK).
 */

/**
 * Event to run the IGP WO algorithm.
 *
 * <p>Creation date: 03-f�v.-2005
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class IGPWOCalculateWeights extends IGPWOCalculateWeightsImpl implements Event {
    
    private static final Logger logger = Logger.getLogger(IGPWOCalculateWeights.class);
    
    public IGPWOCalculateWeights() {}
    
    public IGPWOCalculateWeights(int asId) {
        setASID(asId);
    }
    
    public IGPWOCalculateWeights(int asId, List<Integer> tmIds) {
        this(asId);
        
        ObjectFactory factory = new ObjectFactory();
        List list = this.getTrafficMatrix();
        for (Iterator<Integer> iter = tmIds.iterator(); iter.hasNext();) {
            int tmId = iter.next();
            try {
                IGPWOCalculateWeightsType.TrafficMatrixType tm = factory.createIGPWOCalculateWeightsTypeTrafficMatrixType();
                tm.setTMID(tmId);
                list.add(tm);
            } catch (JAXBException e) {
                logger.error("JAXBException in constructor of IGPWOCalculateWeights. Message: "+e.getMessage());
                if(logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                return;
            }
        }
    }
    
    public IGPWOCalculateWeights(int asId, List<Integer> tmIds, int nbIter) {
        this(asId, tmIds);
        setNbIter(nbIter);
    }
    
    public IGPWOCalculateWeights(int asId, List<Integer> tmIds, int nbIter, int maxPossibleWeight) {
        this(asId, tmIds, nbIter);
        setMaxPossibleWeight(maxPossibleWeight);
    }
    
    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing an IGPWOCalculateWeights event - ASID: "+_ASID);
        
        try {
            int asId = isSetASID() ? _ASID : InterDomainManager.getInstance().getDefaultDomain().getASID();
            
            int[] tmIds;
            if(this.getTrafficMatrix().size() == 0) {
                tmIds = new int[]{TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID(asId)};
            }
            else {
                List list = this.getTrafficMatrix();
                tmIds = new int[list.size()];
                int i = 0;
                for (Iterator iter = list.iterator(); iter.hasNext(); ++i) {
                    IGPWOCalculateWeightsType.TrafficMatrixType tm = (IGPWOCalculateWeightsType.TrafficMatrixType) iter.next();
                    tmIds[i] = tm.getTMID();
                }
            }
            IGPWO igpwo = (IGPWO) RepositoryManager.getInstance().getAlgo("IGPWO", asId);
            
            int nbIter = isSetNbIter() ? _NbIter : 150;
            int maxPossibleWeight = isSetMaxPossibleWeight() ? _MaxPossibleWeight : 50;
            int seed = isSetSeed() ? _Seed : 0;
            
            boolean randomInitialArray = true;
            if(isSetInitialWeightArray() && (getInitialWeightArray() == IgpwoInitialWeightArrayType.CURRENT)) {
                randomInitialArray = false;
            }
            
            SamplingRateType samplingRate = getSamplingRate();
            float maxSamplingRate, minSamplingRate, initialSamplingRate;
            if(samplingRate == null) {
                maxSamplingRate = 0.4f;
                minSamplingRate = 0.01f;
                initialSamplingRate = 0.2f;
            } else {
                maxSamplingRate = samplingRate.isSetMax() ? samplingRate.getMax() : 0.4f;
                minSamplingRate = samplingRate.isSetMin() ? samplingRate.getMin() : 0.01f;
                initialSamplingRate = samplingRate.isSetInitial() ? samplingRate.getInitial() : 0.2f;
            }

            long time = System.currentTimeMillis();
            TotemActionList actionList = igpwo.calculateWeightsParameters(asId, tmIds, nbIter, maxPossibleWeight, randomInitialArray, seed, minSamplingRate, maxSamplingRate, initialSamplingRate);
            time = System.currentTimeMillis() - time;
            logger.info("IGPWO takes " + time + " ms to compute weights on domain ASID" + _ASID);
            double[] newLinkWeights = null;
            for (Iterator iter = actionList.iterator(); iter.hasNext();) {
                TotemAction action = (TotemAction) iter.next();
                if (action instanceof UpdateIGPWeightsAction) {
                    newLinkWeights = ((UpdateIGPWeightsAction)action).getWeights();
                }
                action.execute();
            }
            return new EventResult(newLinkWeights);
        }
        catch(Exception e) {
            logger.error("An exception occurred. Message: "+e.getMessage());
            throw new EventExecutionException(e);
        }
    }
}
