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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.File;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.DomainAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.persistence.DomainFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ParamType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.TopologyGenerationImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.facade.ScenarioExecutionContext;
import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.topgen.topology.WrapperBrite;
import be.ac.ulg.montefiore.run.totem.topgen.topology.TopologyGeneratorException;
import be.ac.ulg.montefiore.run.totem.util.FileFunctions;

/*
 * Changes:
 * --------
 * - 03-Mar-2007: refactor action method to fit with WrapperBrite, the new topology generator (GNI)
 * - 31-Oct-2007: refactor to use new topology generation interface (GMO) 
 */

/**
 * This class implements a topology generation event.
 *
 * <p>Creation date: 10-janv.-2005
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 * @author	Georges Nimubona (nimubonageorges@hotmail.com)
 */
public class TopologyGeneration extends TopologyGenerationImpl implements Event {

    private static final Logger logger = Logger.getLogger(TopologyGeneration.class);
    
    public TopologyGeneration() {}
    
    /**
     * Creates a new <code>TopologyGeneration</code> event. Note that <code>params</code> must
     * <strong>NOT</strong> be null.
     */
    public TopologyGeneration(HashMap params) {
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
            logger.error("JAXBException in a constructor of TopologyGeneration. Reason: "+e.getMessage());
        }
    }
    
    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.info("Processing a topology generation event.");

        if (isSetType() && !getType().equals("BRITE")) {
            throw new EventExecutionException("Generator not found: "+ getType() + ". Should be BRITE.");
        }

        WrapperBrite wrapperBrite = new WrapperBrite();

        // Fetch the user specified parameters
        List parameters = getParam();
        for (Iterator iter = parameters.iterator(); iter.hasNext();) {
            ParamType param = (ParamType) iter.next();
            String paramName = param.getName();
            String paramValue = param.getValue();
            try {
                wrapperBrite.setParam(paramName, paramValue);
            } catch (InvalidParameterException e) {
                throw new EventExecutionException(e);
            }
        }

        String path;
        if (isSetPath())
            path = FileFunctions.getFilenameFromContext(ScenarioExecutionContext.getContext(), getPath());
        else path = ".";
        path += File.separator;

        try {
            List<Domain> domains = wrapperBrite.generate();
            for(Domain d : domains) {
                int nbNodes = d.getNbNodes();
                int nbLinks = d.getNbLinks();
                int i = 0;
                String tmpFileName = path + "topo"+"-"+nbNodes+"N-"+nbLinks+"L";
                String filename;
                File f;
                do {
                    filename = tmpFileName + String.valueOf(i++) + ".xml";
                    f = new File(filename);
                } while (f.exists());
                logger.info("Writing file " + filename);
                DomainFactory.saveDomain(filename, d);
            }
            InterDomainManager.getInstance().addDomain(domains.get(0));
            InterDomainManager.getInstance().setDefaultDomain(domains.get(0).getASID());
        } catch (TopologyGeneratorException e) {
            e.printStackTrace();
            logger.error("An error occurred during the generation of topologies. Reason: "+e.getMessage());
            throw new EventExecutionException(e);
        } catch (InvalidDomainException e) {
            logger.warn("Could not set the generated domain as default");
        } catch (DomainAlreadyExistException e) {
            logger.warn("Could not add the generated domain to the interdomain manager");
        }

        logger.info("Topologies generated in "+path+".");
        return new EventResult();
    }

}
