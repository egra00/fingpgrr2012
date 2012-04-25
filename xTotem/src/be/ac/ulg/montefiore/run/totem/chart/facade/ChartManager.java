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
package be.ac.ulg.montefiore.run.totem.chart.facade;

import be.ac.ulg.montefiore.run.totem.chart.model.Chart;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.NoSuchChartException;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.ChartParameterException;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/*
* Changes:
* --------
* 25-Jan-2006: add getAllChartsName() method, method addChart now throw an exception
*
*/

/**
* Singleton class. The instance can be obtained by the getInstance static method.
* Global access point to the currently used charts objects.
* Charts are identified by their name which is of type String.
*
* <p>Creation date: 16 d�c. 2005
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ChartManager {

    private static ChartManager manager = null;

    private HashMap<String, Chart> charts = null;

    /**
     * Default constructor
     */
    private ChartManager() {
        charts = new HashMap<String, Chart>();
    }

    /**
     * return the instance of the singleton class ChartManager
     * @return
     */
    public static ChartManager getInstance() {
        if (manager == null) {
            manager = new ChartManager();
        }
        return manager;
    }

    /**
     * Add a chart to the manager
     * @param name Name of the chart.
     * @param chart
     */
    public void addChart(String name, Chart chart) throws ChartParameterException {
        if (charts.containsKey(name)) throw new ChartParameterException("Chart with same name already exists.");
        charts.put(name, chart);
    }

    /**
     * return a chart given its name
     * @param name
     * @return
     * @throws NoSuchChartException if the chart does not exists
     */
    public Chart getChart(String name) throws NoSuchChartException {
        Chart ret = charts.get(name);
        if (ret == null) {
            throw new NoSuchChartException();
        }
        return ret;
    }

    /*
    public List<Pair<String, Chart>> getAllCharts() {
        ArrayList<Pair<String, Chart>> lst = new ArrayList<Pair<String, Chart>>();
        for (java.util.Map.Entry entry : charts.entrySet()) {
            lst.add(new Pair(entry.getKey(), entry.getValue()));
        }
        return lst;
    }
    */

    public String[] getAllChartsName() {
        String[] ret = new String[charts.size()];
        int i = 0;
        for (String str : charts.keySet()) {
            ret[i] = str;
            i++;
        }
        return ret;
    }


    /**
     * remove a chart from manager.
     * @param name
     * @throws NoSuchChartException if the chart did not exists in the manager.
     */
    public void removeChart(String name) throws NoSuchChartException {
        if (charts.remove(name) == null) {
            throw new NoSuchChartException();
        }
    }

}
