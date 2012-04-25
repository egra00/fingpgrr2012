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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import be.ac.ulg.montefiore.run.totem.chart.model.ChartData;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

/*
 * Changes:
 * --------
 *
 * - 14-Feb-2006: customize bar margins (JLE).
 * - 14-Feb-2006: customize labels orientation (JLE).
 */

/**
 * This plotter creates bar charts using links loads. The x-axis represents
 * the links and the y-axis represents the (absolute or relative) load.
 * 
 * <p>This class has been designed to allow the creation of two different bar
 * charts:
 * <ol>
 * <li>Charts where the load of all links is displayed.</li>
 * <li>Charts where only a statistic about the load of links is displayed.</li>
 * </ol>
 * 
 * The distinction of the two types of charts is made thanks to the
 * <code>allLinks</code> parameter. If it is <code>true</code>, the first type
 * of charts is created. Otherwise, the second type of charts is created.
 * 
 * <p>There is also a distinction in the meaning of the series names. In the
 * first case, they give the name of the algorithm used to compute loads. In the
 * second case, they give the links IDs.
 *
 * <p>Finally, for the first type of plots, a legend will be displayed. This is
 * not the case for the second type of plots.
 *
 * <p>Creation date: 13-f�vr.-2006
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class LoadChartPlotter implements ChartPlotter {

    private static final Logger logger = Logger.getLogger(LoadChartPlotter.class);

    private static final ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();
    static {
        try {
            params.add(new ParameterDescriptor("allLinks", "The data set contains series of all links loads or a statistic about load (max, mean...)", Boolean.class, Boolean.FALSE));
            params.add(new ParameterDescriptor("asId", "Domain (leave blank for default).", Integer.class, null));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Creates and returns a bar chart using <code>data</code>.
     * 
     * <p>The following parameters are accepted:
     * <ol>
     * <li>allLinks (boolean, default is <code>true</code>): see the
     * documentation above.</li>
     * <li>asId (integer): the domain to consider. The default behavior is to
     * use the default domain.
     * </ol>
     */
    public JFreeChart plot(ChartData data, String title, String xAxisTitle,
            String yAxisTitle, HashMap<String, String> params) {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        
        boolean isAllLinks;
        if(!params.containsKey("allLinks")) {
            isAllLinks = true;
        } else {
            String allLinks = params.get("allLinks");
            if(allLinks.equals("true")) {
                isAllLinks = true;
            } else if(allLinks.equals("false")) {
                isAllLinks = false;
            } else {
                logger.error("allLinks must be equal to \"true\" or \"false\".");
                logger.error(allLinks+" is an unknown value. Revert to default (true).");
                isAllLinks = true;
            }
        }
        
        Domain domain;
        if(params == null || params.get("asId") == null) {
            domain = InterDomainManager.getInstance().getDefaultDomain();
        } else {
            try {
                domain = InterDomainManager.getInstance().getDomain(Integer.parseInt(params.get("asId")));
            } catch(NumberFormatException e) {
                logger.error("asId must be an integer. Revert to default domain.");
                domain = InterDomainManager.getInstance().getDefaultDomain();
            } catch(InvalidDomainException e) {
                logger.error("There is no domain "+params.get("asId")+". Revert to default domain.");
                domain = InterDomainManager.getInstance().getDefaultDomain();                
            }
        }
        
        for(int i = 0; i < data.getRowCount(); ++i) {
            double[] row = data.getRow(i);
            for(int j = 0; j < row.length; ++j) {
                if(!isAllLinks) {
                    dataSet.addValue(row[j], "", data.getRowTitle(i));
                } else {
                    try {
                        dataSet.addValue(row[j], data.getRowTitle(i), domain.getConvertor().getLinkId(j));
                    } catch(LinkNotFoundException e) {
                        logger.error("There is no link corresponding to integer "+j);
                    }
                }
            }
        }
        
        JFreeChart chart = ChartFactory.createBarChart(title, xAxisTitle, yAxisTitle, dataSet, PlotOrientation.VERTICAL, isAllLinks, true, false); 
        
        CategoryAxis axis = chart.getCategoryPlot().getDomainAxis();
        axis.setLowerMargin(0);
        axis.setUpperMargin(0);
        axis.setCategoryMargin(0.2);
        axis.setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        
        return chart;
    }

    @SuppressWarnings("unchecked")
    public List<ParameterDescriptor> getParameters() {
        return (List<ParameterDescriptor>) params.clone();
    }

    public String getDefaultXAxisTitle() {
        return "";
    }

    public String getDefaultYAxisTitle() {
        return "Utilization";
    }
}
