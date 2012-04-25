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
package be.ac.ulg.montefiore.run.totem.chart.model.plotters;

import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
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

public class PlotterManager {
    private static final Logger logger = Logger.getLogger(PlotterManager.class);
    private static PlotterManager manager = null;

    private HashMap<String, String> availableChartsPlotters = null;
    private HashMap<String, ChartPlotter> plottersInstances = null;


    private PlotterManager() {

        availableChartsPlotters = new HashMap<String, String>();
        plottersInstances = new HashMap<String, ChartPlotter>();
        String[] plotters = PreferenceManager.getInstance().getPrefs().get("AVAILABLE-CHART-PLOTTERS", "be.ac.ulg.montefiore.run.totem.chart.model.plotters.DecreasingLineChartPlotter:be.ac.ulg.montefiore.run.totem.chart.model.plotters.LoadIntervalChartPlotter").split(":");
        for (String plotter : plotters) {
            String shortName = plotter.substring(plotter.lastIndexOf('.')+1);
            availableChartsPlotters.put(shortName, plotter);
            ChartPlotter cp = null;
            try {
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(plotter);
                cp = (ChartPlotter) clazz.newInstance();
                plottersInstances.put(shortName, cp);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
                logger.error("Could not instantiate specified class: " + e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                logger.error("ChartDataCollector unknown : " + plotter);
            }
        }
    }

    /**
     * return the unique instance of the manager
     * @return
     */
    static public PlotterManager getInstance() {
        if (manager == null) {
            manager = new PlotterManager();
        }
        return manager;
    }

    /**
     * return a table containing the availables plotters names
     * @return
     */
    public String[] getAvailablePlotters() {
        return availableChartsPlotters.keySet().toArray(new String[0]);
    }

    /**
     * returns the parameters that can be passed to the plotter given by its name
     * @param shortName
     * @return a list of parameters
     */
    public List<ParameterDescriptor> getPlotterParameters(String shortName) {
        return plottersInstances.get(shortName).getParameters();
    }

    /**
     * return a new instance of the plotter whose name is given as parameter
     * @param name The name of the collector to instantiate.
     * @return
     */
    public ChartPlotter getPlotterInstance(String name) {

        Class clazz = null;
        ChartPlotter plotter = null;
        try {
            clazz = Thread.currentThread().getContextClassLoader().loadClass(availableChartsPlotters.get(name));
            plotter = (ChartPlotter)clazz.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            logger.error("ChartPlotter unknown : " + name);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
            logger.error("Could not instantiate specified class: " + e.getMessage());
        }
        return plotter;
    }

}
