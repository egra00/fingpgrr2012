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

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ParamType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.StartAlgoImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.LibraryInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemAlgorithm;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

/*
 * Changes:
 * --------
 *
 * - 22-Mar.-2005: catch NullPointerException in action() (JL).
 * - 06-Mar.-2007: catch LibraryInitialisationException in action() (GMO)
 *
 */

/**
 * This class implements a start algorithm event.
 *
 * <p>Creation date: 08-Déc.-2004
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class StartAlgo extends StartAlgoImpl implements Event {

    private final static Logger logger = Logger.getLogger(StartAlgo.class);
    
    public StartAlgo() {}
    
    /**
     * Creates a new <code>StartAlgo</code> element. Note that <code>algoParams</code> can be null.
     */
    public StartAlgo(String algoName, HashMap algoParams) {
        setName(algoName);
        
        if(algoParams != null) {
            ObjectFactory factory = new ObjectFactory();
            
            try {
                Set set = algoParams.entrySet();
                for (Iterator iter = set.iterator(); iter.hasNext();) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    ParamType param = factory.createParamType();
                    param.setName((String) entry.getKey());
                    param.setValue((String) entry.getValue());
                    getParam().add(param);
                }
            }
            catch(JAXBException e) {
                logger.error("JAXBException in a constructor of StartAlgo. Reason: "+e.getMessage());
            }
        }
    }
    
    public StartAlgo(String algoName, HashMap algoParams, int asId) {
        this(algoName, algoParams);
        setASID(asId);
    }
    
    public StartAlgo(String algoName, HashMap algoParams, int asId, int tmId) {
        this(algoName, algoParams, asId);
        setTMID(tmId);
    }

    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a startAlgo event: "+ this.getName());
        
        HashMap<String, String> params = new HashMap<String, String>();
        for (Iterator it = getParam().iterator(); it.hasNext();) {
           ParamType paramType = (ParamType) it.next();
           params.put(paramType.getName(),paramType.getValue());
        }
        
        /* BQU: need to check that a least a default domain is available before going further !... */
        /* JL: this check is useful only if no AS ID is specified... */
        if ((InterDomainManager.getInstance().getDefaultDomain() == null) && !isSetASID()) {
            /* BQU: How can I report an error here ? Maybe action() should be able to throw Exception's */
            logger.error("No default domain available");
            throw new EventExecutionException("No default domain available");
        }

        TotemAlgorithm algo;
        try {
            if(this.isSetASID() && this.isSetTMID()) {
                RepositoryManager.getInstance().startAlgo(this.getName(), params, this.getASID(), this.getTMID());
                algo = RepositoryManager.getInstance().getAlgo(this.getName(), this.getASID(), this.getTMID());
            }
            else if(this.isSetASID()) {
                RepositoryManager.getInstance().startAlgo(this.getName(), params, this.getASID());
                algo = RepositoryManager.getInstance().getAlgo(this.getName(), this.getASID());
            }
            else if(this.isSetTMID()) {
                int asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
                RepositoryManager.getInstance().startAlgo(this.getName(), params, asId, this.getTMID());
                algo = RepositoryManager.getInstance().getAlgo(this.getName(), asId, this.getTMID());
            }
            else {
                RepositoryManager.getInstance().startAlgo(this.getName(), params);
                algo = RepositoryManager.getInstance().getAlgo(this.getName());
            }
        } catch (LibraryInitialisationException e) {
            logger.error("An error when loading an algorithm library. Message: "+e.getMessage());
            logger.error("Check that the library exists and that it is compiled for this architecture.");
            throw new EventExecutionException(e);
        } catch (AlgorithmInitialisationException e) {
            logger.error("An error occurred when starting an algorithm. Message: "+e.getMessage());
            throw new EventExecutionException(e);
        } catch(NullPointerException e) {
            logger.error("No default domain!");
            throw new EventExecutionException(e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("The algorithm was not added to the manager");
            throw new EventExecutionException(e);
        }
        logger.info("Algorithm " + algo.getClass().getSimpleName() + " started.");
        return new EventResult(algo);
    }
}
