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
package be.ac.ulg.montefiore.run.totem.chart.model.collectors;

import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
* Maintains a list of all available collectors, can create instance of specific collectors, can get parameters of
* specific collectors
*
* <p>Creation date: 25 janv. 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class DataCollectorManager {
    private static final Logger logger = Logger.getLogger(DataCollectorManager.class);
    private static DataCollectorManager manager = null;

    private HashMap<String, String> availableDataCollectors = null;
    private HashMap<String, ChartDataCollector> collectorsInstances = null;


    private DataCollectorManager() {
        availableDataCollectors = new HashMap<String, String>();
        collectorsInstances = new HashMap<String, ChartDataCollector>();
        String[] collectors = PreferenceManager.getInstance().getPrefs().get("AVAILABLE-DATA-COLLECTORS", "be.ac.ulg.montefiore.run.totem.chart.model.collectors.LinksLoadDataCollector:be.ac.ulg.montefiore.run.totem.chart.model.collectors.LinksReservedBWDataCollector").split(":");
        for (String collector : collectors) {
            String shortName = collector.substring(collector.lastIndexOf('.')+1);
            availableDataCollectors.put(shortName, collector);
            ChartDataCollector dc = null;
            try {
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(collector);
                dc = (ChartDataCollector) clazz.newInstance();
                collectorsInstances.put(shortName, dc);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
                logger.error("Could not instantiate specified class: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                logger.error("ChartDataCollector unknown : " + collector);
            }

        }
    }

    /**
     * return the unique instance of the manager
     * @return
     */
    static public DataCollectorManager getInstance() {
        if (manager == null) {
            manager = new DataCollectorManager();
        }
        return manager;
    }

    /**
     * return a table containing the availables collectors names
     * @return
     */
    public String[] getAvailableDataCollectors() {
        return availableDataCollectors.keySet().toArray(new String[0]);
    }

    /**
     * returns the parameters that can be passed to the collector given by its name
     * @param shortName
     * @return a list of parameters
     */
    public List<ParameterDescriptor> getCollectorParameters(String shortName) {
        return collectorsInstances.get(shortName).getParameters();
    }

    /**
     * return a new instance of the collector whose name is given as parameter
     * @param name The name of the collector to instantiate.
     * @return
     */
    public ChartDataCollector getCollectorInstance(String name) {
        ChartDataCollector chartCollector = null;
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(availableDataCollectors.get(name));
            chartCollector = (ChartDataCollector)clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            logger.error("Could not instantiate specified class: " + e.getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error("ChartDataCollector unknown : " + name);
        }
        return chartCollector;
    }

}
