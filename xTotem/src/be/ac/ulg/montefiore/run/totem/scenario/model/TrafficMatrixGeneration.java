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

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ParamType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.TrafficMatrixGenerationImpl;
import be.ac.ulg.montefiore.run.totem.scenario.facade.ScenarioExecutionContext;
import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.topgen.traffic.TrafficGenerationException;
import be.ac.ulg.montefiore.run.totem.topgen.traffic.TrafficGeneratorFactory;
import be.ac.ulg.montefiore.run.totem.topgen.traffic.TrafficGeneratorInterface;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.util.*;
import java.io.File;

/*
 * Changes:
 * --------
 * - 07-Jul-05: add generateOnlyEdgeTraffic parameter (FSK)
 * - 08-Jan-07: refactor the action method in order to support the new traffic generation architecture (GNI).
 * - 30-Oct-07: use the new interface (GMO).  
 */

/**
 * This class implements a traffic matrix generation event.
 *
 * <p>Creation date: 10-janv.-2005
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class TrafficMatrixGeneration extends TrafficMatrixGenerationImpl
        implements Event {

    private static final Logger logger = Logger.getLogger(TrafficMatrixGeneration.class);

    public TrafficMatrixGeneration() {}
    
    public TrafficMatrixGeneration(HashMap params) {
        ObjectFactory factory = new ObjectFactory();
        try {
            Set set = params.entrySet();
            for (Iterator iter = set.iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                ParamType param = factory.createParamType();
                param.setName((String) entry.getKey());
                param.setValue((String) entry.getValue());
                getParam().add(param);
            }
        }
        catch(JAXBException e) {
            logger.error("JAXBException in a constructor of DemandMatrixGeneration. Reason: "+e.getMessage());
        }        
    }
    
    public TrafficMatrixGeneration(HashMap params, int asId) {
        this(params);
        setASID(asId);
    }
    
    public TrafficMatrixGeneration(int tmId, HashMap params) {
        this(params);
        setTMID(tmId);
    }
    
    public TrafficMatrixGeneration(HashMap params, int asId, int tmId) {
        this(params, asId);
        setTMID(tmId);
    }

    public TrafficMatrixGeneration(String generator, HashMap params, int asId) {
        this(params);
        setASID(asId);
        setType(generator);
    }

    public TrafficMatrixGeneration(String generator, int tmId, HashMap params) {
        this(params);
        setTMID(tmId);
        setType(generator);
    }

    public TrafficMatrixGeneration(String generator, HashMap params, int asId, int tmId) {
        this(params, asId);
        setTMID(tmId);
        setType(generator);
    }

    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    /* (non-Javadoc)
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.info("Processing a traffic matrix generation event.");

        int asId;
        if(isSetASID()) {
            asId = _ASID;
        }
        else {
            asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
        }

        TrafficGeneratorInterface generator;
        if (isSetType()) {
            try {
                generator = TrafficGeneratorFactory.createGenerator(getType());
            } catch (InvalidParameterException e) {
                e.printStackTrace();
                throw new EventExecutionException(e);
            }
        } else {
            generator = TrafficGeneratorFactory.createDefaultGenerator();
        }

        for (ParamType p : (List<ParamType>)getParam()) {
            try {
                generator.setParam(p.getName(), p.getValue());
            } catch (InvalidParameterException e) {
                e.printStackTrace();
                throw new EventExecutionException(e);
            }
        }

        String path;
        if (isSetPath())
            path = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), getPath());
        else path = ".";
        path += File.separator;

        try {
            List<TrafficMatrix> tms = generator.generate();
            int nbNodes = InterDomainManager.getInstance().getDomain(asId).getNbNodes();
            int nbLinks = InterDomainManager.getInstance().getDomain(asId).getNbLinks();
            int i = 0;
            for (TrafficMatrix tm : tms) {
                int tmId;
                if (tms.size() == 1 && isSetTMID()) {
                    tmId = getTMID();
                } else {
                    if (isSetTMID()) {
                        logger.warn("TM Id set but multiple matrices were generated. Using generated Ids.");
                    }
                    tmId = TrafficMatrixManager.getInstance().generateTMID(asId);
                }
                try {
                    TrafficMatrixManager.getInstance().addTrafficMatrix(tm, tmId);
                } catch (TrafficMatrixIdException e) {
                    logger.error("Could not load the traffic matrix " + tmId);
                }
                String filename = path+"TM"+i+"-topo-"+nbNodes+"N-"+nbLinks+"L"+".xml";
                TrafficMatrixManager.getInstance().saveTrafficMatrix(asId, tmId++, filename);
                i++;
                logger.info("Traffic matrix saved as " + filename);
            }
            return new EventResult(tms);
        } catch (TrafficGenerationException e) {
            e.printStackTrace();
            throw new EventExecutionException(e);
        } catch (InvalidDomainException e) {
            e.printStackTrace();
            throw new EventExecutionException(e);
        } catch (TrafficMatrixAlreadyExistException e) {
            e.printStackTrace();
            throw new EventExecutionException(e);
        } catch (InvalidTrafficMatrixException e) {
            e.printStackTrace();
            throw new EventExecutionException(e);
        }
    }
}
