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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import be.ac.ulg.montefiore.run.totem.chart.model.plotters.ChartPlotter;
import be.ac.ulg.montefiore.run.totem.chart.model.ChartData;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

/*
* Changes:
* --------
*
*
*/

/**
* This class is used to plot a line graph where all values in a series are sorted.
* The plotted point with the highest Y coordinate has the smallest X coordinate, so that the chart represent a decreasing line.
* A typical use is to represent the links of a domain on the X axis and the load for each link on the Y axis.
*
* <p>Creation date: 20 déc. 2005
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class DecreasingLineChartPlotter implements ChartPlotter {

   /**
    * Build the chart and return the JFreeChart representation.
    *
    * @param data Data to use to compute the chart
    * @param title Title to display on the chart
    * @param xAxisTitle
    * @param yAxisTitle
    * @param params no parameters used
    * @return
    */
    public JFreeChart plot(ChartData data, String title, String xAxisTitle, String yAxisTitle, HashMap<String, String> params) {

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < data.getRowCount(); i++) {
            double[] value = data.getRow(i);
            Arrays.sort(value);
            String rowTitle = data.getRowTitle(i);
            for (int k = value.length-1, l=0; k >= 0; k--, l++) {
                dataset.addValue((Number)value[k], rowTitle, l);
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(title, //title
                    xAxisTitle, //X axis label
                    yAxisTitle, //Y axis label
                    dataset,
                    PlotOrientation.VERTICAL,
                    true, //legend ?
                    true, //tooltip ?
                    false  //URL ?
        );

        return chart;
    }

    public List<ParameterDescriptor> getParameters() {
        return new ArrayList<ParameterDescriptor>();
    }

    public String getDefaultXAxisTitle() {
        return "Links (from most loaded to less loaded)";
    }

    public String getDefaultYAxisTitle() {
        return "Utilization";
    }
}
