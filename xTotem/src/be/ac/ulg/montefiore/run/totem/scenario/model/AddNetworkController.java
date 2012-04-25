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
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.netController.exception.NetworkControllerAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.netController.exception.NetworkControllerInitialisationException;
import be.ac.ulg.montefiore.run.totem.netController.facade.NetworkControllerManager;
import be.ac.ulg.montefiore.run.totem.netController.model.NetworkController;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ParamType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.AddNetworkControllerImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;

/*
 * Changes:
 * --------
 *
 */

/**
 * This event creates and adds a new network controller to the network
 * controller manager.
 *
 * <p>Creation date: 24-mars-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class AddNetworkController extends AddNetworkControllerImpl implements
        Event {

    private static final Logger logger = Logger.getLogger(AddNetworkController.class);

    public AddNetworkController() {}
    
    public AddNetworkController(String name, String className) {
        setName(name);
        setClassName(className);
    }
    
    public AddNetworkController(String name, String className, int asId) {
        this(name, className);
        setASID(asId);
    }
    
    public AddNetworkController(String name, String className, int asId, int tmId) {
        this(name, className, asId);
        setTMID(tmId);
    }

    public AddNetworkController(String name, String className, HashMap<String, String> params) {
        this(name, className);
        ObjectFactory factory = new ObjectFactory();
        try {
            Set<Map.Entry<String, String>> set = params.entrySet();
            for (Iterator<Map.Entry<String, String>> iter = set.iterator(); iter.hasNext();) {
                Map.Entry<String, String> entry = iter.next();
                ParamType param = factory.createParamType();
                param.setName(entry.getKey());
                param.setValue(entry.getValue());
                getParam().add(param);
            }
        }
        catch(JAXBException e) {
            logger.error("JAXBException in a constructor of AddNetworkController. Reason: "+e.getMessage());
        }
    }
    
    public AddNetworkController(String name, String className, HashMap<String, String> params, int asId) {
        this(name, className, params);
        setASID(asId);
    }
    
    public AddNetworkController(String name, String className, HashMap<String, String> params, int asId, int tmId) {
        this(name, className, params, asId);
        setTMID(tmId);
    }
    
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing an AddNetworkController event - Name: "+this.getName()+" - ClassName: "+this.getClassName()+".");
        
        HashMap<String, String> params = new HashMap<String, String>();
        try {
            params.put("ASID", this.isSetASID() ? Integer.toString(this.getASID()) : Integer.toString(InterDomainManager.getInstance().getDefaultDomain().getASID()));
        } catch (NullPointerException e1) {
            // Probably, there is no default domain. Maybe this is not necessary for this kind of network controller so just skip.
        }
        try {
            params.put("TMID", this.isSetTMID() ? Integer.toString(this.getTMID()) : Integer.toString(TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID()));
        } catch (InvalidTrafficMatrixException e1) {
            // There is no default traffic matrix. Maybe this is not necessary for this kind of network controller so just skip.
        }
        for (Iterator<ParamType> iter = this.getParam().iterator(); iter.hasNext();) {
            ParamType param = iter.next();
            params.put(param.getName(), param.getValue());
        }

        NetworkController nc;
        try {
            nc = NetworkControllerManager.getInstance().addNetworkController(this.getName(), this.getClassName(), params);
        } catch (NetworkControllerAlreadyExistException e) {
            logger.error("Name "+getName()+" already used!");
            throw new EventExecutionException(e);
        } catch (NetworkControllerInitialisationException e) {
            logger.error("Error during the initialisation of the network controller.");
            throw new EventExecutionException(e);
        }
        logger.info("Network controller added.");
        return new EventResult(nc);
    }
}
