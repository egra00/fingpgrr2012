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
package be.ac.ulg.montefiore.run.totem.netController.facade;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
import be.ac.ulg.montefiore.run.totem.netController.exception.InvalidNetworkControllerException;
import be.ac.ulg.montefiore.run.totem.netController.exception.NetworkControllerAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.netController.exception.NetworkControllerInitialisationException;
import be.ac.ulg.montefiore.run.totem.netController.model.NetworkController;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

/*
 * Changes:
 * --------
 * 24-Apr.-2006 : addNetworkController now returns the new network controller (GMO)
 * 13-Mar.-2007 : maintain an instance of each existing network controller (GMO)
 * 13-Mar.-2007 : add getAvailableNetworkControllers() and getStartParameters(.) (GMO)
 * 10-May.-2007 : stops the network controllers when removed from the manager (GMO)
 */

/**
 * This class is a singleton and provides a global access point to the network
 * controllers.
 *
 * <p>Creation date: 23-mars-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class NetworkControllerManager {
    
    private static final Logger logger = Logger.getLogger(NetworkControllerManager.class);
    private static NetworkControllerManager instance = null;

    // maintain an instance of each algorithm class
    // used to obtain the parameters
    // don't need much memory since real initialisation is done in start()
    private HashMap<String, NetworkController> instances;

    private HashMap<String, NetworkController> netControllers;
    private HashMap<String, String> availableNetControllers;
    private Class<NetworkController> netController;
    
    private NetworkControllerManager() {

        instances = new HashMap<String, NetworkController>();
        netControllers = new HashMap<String, NetworkController>();

        // build availableNetControllers
        availableNetControllers = new HashMap<String, String>();
        String[] listNetControllers = PreferenceManager.getInstance().getPrefs().get("AVAILABLE-NET-CONTROLLERS", "be.ac.ulg.montefiore.run.totem.netController.model.RerouteLSPOnFailureNetworkController").split(":");
        for (int i = 0; i < listNetControllers.length; ++i) {
            String shortName = listNetControllers[i].substring(listNetControllers[i].lastIndexOf('.')+1);
            availableNetControllers.put(shortName, listNetControllers[i]);

            Class clazz = null;
            try {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(listNetControllers[i]);
                instances.put(shortName, (NetworkController)clazz.newInstance());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        }
        
        netController = NetworkController.class;
    }

    /**
     * Returns the unique instance of <code>NetworkControllerManager</code>.
     * @return The unique instance of <code>NetworkControllerManager</code>.
     */
    public static NetworkControllerManager getInstance() {
        if(instance == null) {
            instance = new NetworkControllerManager();
        }
        return instance;
    }

    /**
     * Adds a new network controller.
     * @param name The name of the network controller to add.
     * @param className The name of the class of the network controller to add.
     * The corresponding full qualified class name must be given in the
     * <code>preferences.xml</code> file.
     * @param params The parameters to initialise the network controller.
     * @throws NetworkControllerAlreadyExistException If <code>name</code> is
     * already used.
     * @throws NetworkControllerInitialisationException If the class
     * <code>className</code> can't be loaded or if an error occurs during the
     * initialisation of the network controller.
     */
    public NetworkController addNetworkController(String name, String className, HashMap<String, String> params) throws NetworkControllerAlreadyExistException, NetworkControllerInitialisationException {
        if(netControllers.containsKey(name)) {
            throw new NetworkControllerAlreadyExistException(name+" is already used!");
        }
        
        if(!availableNetControllers.containsKey(className)) {
            throw new NetworkControllerInitialisationException("Unknown class name "+className);
        }
        
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(availableNetControllers.get(className));
            if(!netController.isAssignableFrom(clazz)) {
                throw new NetworkControllerInitialisationException("The class "+className+" doesn't implement NetworkController!");
            }
            NetworkController nc = (NetworkController) clazz.newInstance();
            nc.start(params);
            netControllers.put(name, nc);
            return nc;
        } catch (ClassNotFoundException e) {
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new NetworkControllerInitialisationException("The class "+className+" can't be loaded.");
        } catch (InstantiationException e) {
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new NetworkControllerInitialisationException("The class "+className+" has not a nullary constructor.");
        } catch (IllegalAccessException e) {
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new NetworkControllerInitialisationException("The class "+className+" has not a nullary public constructor.");
        } catch (ExceptionInInitializerError e) {
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new NetworkControllerInitialisationException("There was an error during the initialisation of "+className+".");
        }
    }
    
    /**
     * Removes the network controller <code>name</code>.
     * @param name The name of the network controller to remove.
     * @throws InvalidNetworkControllerException If there is no network
     * controller with the name <code>name</code>.
     */
    public void removeNetworkController(String name) throws InvalidNetworkControllerException {
        if(!netControllers.containsKey(name)) {
            throw new InvalidNetworkControllerException("No network controller with the name "+name);
        }
        NetworkController nc = netControllers.remove(name);
        nc.stop();
    }
    
    /**
     * Removes all the network controllers from the
     * <code>NetworkControllerManager</code>.
     */
    public void removeAllNetworkControllers() {
        for (NetworkController nc : netControllers.values()) {
            nc.stop();
        }
        netControllers.clear();
    }

    public List<Class> getAvailableNetworkControllers() {
        List<Class> lst = new ArrayList<Class>();
        for (String ss : availableNetControllers.values()) {
            try {
                lst.add(Class.forName(ss));
            } catch (ClassNotFoundException e) {
                logger.error("Unable to find class: " + ss);
            }
        }
        return lst;
    }

    public List<ParameterDescriptor> getStartParameters(String name) throws InvalidNetworkControllerException {
        if (instances.get(name) == null) {
            throw new InvalidNetworkControllerException("No nework controller : " + name);
        }

        return instances.get(name).getStartParameters();
    }
}
