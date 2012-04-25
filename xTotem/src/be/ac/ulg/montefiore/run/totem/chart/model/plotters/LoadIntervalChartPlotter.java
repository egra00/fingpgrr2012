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
import org.apache.log4j.Logger;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import be.ac.ulg.montefiore.run.totem.chart.model.plotters.ChartPlotter;
import be.ac.ulg.montefiore.run.totem.chart.model.ChartData;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

/*
* Changes:
* --------
* - 25-Apr-2007: add an extra category for the value superior to maxValue. (GMO)
*
*/

/**
* This class is used to build an interval graph.
* The interval 0-maxValue is divided in a number of disjoint sets (correponding to the <code>nbInterval</code> parameter.
* For each of these sets, a bar is represented. The height of the bar corresponds to the frequency of the data for the
* considerated set. If some values are superior to maxValue, an extra category is added.
* <p>
* A typical use of this chart is to represent the relative load of the links of a domain.
* X Axis represent the load categories (ex: 0%-33%, 33%-66%, 66%-100%). Y axis is the relative number of links that
* match the load category.
*
* <p>Creation date: 19 déc. 2005
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LoadIntervalChartPlotter implements ChartPlotter {
    private final static Logger logger = Logger.getLogger(LoadIntervalChartPlotter.class);

    private static final ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();

    static {
        try {
        	params.add(new ParameterDescriptor("nbInterval", "Number of categories to represent.", Integer.class, new Integer(10)));
            params.add(new ParameterDescriptor("maxValue", "Value to use for 100%", Double.class, new Double(1)));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
    }



    /**
     * Build the chart and return the JFreeChart representation.
     * The parameters are :<br>
     *     - nbInterval (default value 10) : number of categories to represent.<br>
     *     - maxValue (default 1) : value used for 100%
     * @param data Data to use to compute the chart
     * @param title Title to display on the chart
     * @param xAxisTitle
     * @param yAxisTitle
     * @param params (nbInterval, maxValue)
     * @return
     */
    public JFreeChart plot(ChartData data, String title, String xAxisTitle, String yAxisTitle, HashMap<String, String> params) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int nbInterval = 10;
        double maxValue = 1;
        if (params != null) {
            String nbIntervalStr = params.get("nbInterval");
            if (nbIntervalStr != null) {
                nbInterval = Integer.parseInt(nbIntervalStr);
            }
            // value of 100%
            String maxValueStr = params.get("maxValue");
            if (maxValueStr != null) {
                maxValue = Double.parseDouble(maxValueStr);
            }
        }

        for (int j = 0; j < data.getRowCount(); j++) {
            double[] values = data.getRow(j);
            int[] valuesCount = new int[nbInterval+1];
            int size = values.length;

            for (int i = 0; i < values.length; i++) {
                if (values[i] > maxValue) {
                    valuesCount[nbInterval]++;
                    //System.out.println("Value >=1 : " + values[i]);
                }
                else {
                    valuesCount[(int) (values[i] * nbInterval / maxValue)]++;
                    //System.out.println("Value : " + values[i] + " on category n°" + (int) (values[i] * nbInterval / maxValue));
                }
            }

            NumberFormat formatter = new DecimalFormat();
            formatter.setMaximumFractionDigits(2);

            for (int i = 0; i < nbInterval; i++) {
                double from = (double) i / nbInterval * 100;
                double to = (double) (i + 1) / nbInterval * 100;
                String legend = new String(formatter.format(from) + "-" + formatter.format(to) + " %");
                String rowTitle = data.getRowTitle(j);
                double value = (double)valuesCount[i] / (double)size * 100;
                dataset.addValue(value, rowTitle, legend);
            }

            /* Add an extra category if some links are loaded to more than 100% */
            if (valuesCount[nbInterval] != 0) {
                String legend = new String("> 100 %");
                String rowTitle = data.getRowTitle(j);
                double value = (double)valuesCount[nbInterval] / (double)size * 100;
                dataset.addValue(value, rowTitle, legend);
            }

        }

        JFreeChart chart = ChartFactory.createBarChart(title,
                    xAxisTitle,
                    yAxisTitle,
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    false);


        return chart;

    }

    public List<ParameterDescriptor> getParameters() {
        return (List<ParameterDescriptor>)params.clone();
    }

    public String getDefaultXAxisTitle() {
        return "Link utilization interval";
    }

    public String getDefaultYAxisTitle() {
        return "Percentage of links in the interval";
    }
}
