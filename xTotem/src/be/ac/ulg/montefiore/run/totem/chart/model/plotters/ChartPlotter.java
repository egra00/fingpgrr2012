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

import org.jfree.chart.JFreeChart;

import java.util.HashMap;
import java.util.List;

import be.ac.ulg.montefiore.run.totem.chart.model.ChartData;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;


/*
* Changes:
* --------
* 25-Jan-2006: add the getParameters method (GMO)
* 13-Aug-2007: add the getDefaultXAxisTitle() and getDefaultYAxisTitle() methods (GMO)
*/


/**
* Interface for chart cretaion.
*
* <p>Creation date: 22 d�c. 2005
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public interface ChartPlotter {
    /**
     * Build a chart based on the given data and parameters
     * @param data Data to use to compute the chart
     * @param title Title to display on the chart
     * @param xAxisTitle
     * @param yAxisTitle
     * @param params Parameters of the chart to build (implementation dependant).
     * @return the JFreeChart representation of the chart.
     */
    public JFreeChart plot(ChartData data, String title, String xAxisTitle, String yAxisTitle, HashMap<String, String> params);

    /**
     * Return a list of parameters that can be given to the plot method.
     * @return
     */
    public List<ParameterDescriptor> getParameters();

    /**
     * returns the default value for the X axis
     * @return
     */
    public String getDefaultXAxisTitle();

    /**
     * returns the default value for the Y axis 
     * @return
     */
    public String getDefaultYAxisTitle();
}
