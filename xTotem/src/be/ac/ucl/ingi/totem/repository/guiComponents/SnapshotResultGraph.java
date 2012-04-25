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

package be.ac.ucl.ingi.totem.repository.guiComponents;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeMap;

/**
 * 
 * @author Thomas Vanstals
 *
 */
public class SnapshotResultGraph extends JFrame
{
    static final String TOTAL_CHANGES = "totalChanges";
    static final String AS_TOTAL_CHANGES = "asTotalChanges";
    // les différents types de changements possibles
    static final String SERIES1 = "prefix down";
    static final String SERIES2 = "prefix up";
    static final String SERIES3 = "peer change";
    static final String SERIES4 = "egress change";
    static final String SERIES5 = "intra cost change";
    static final String SERIES6 = "intra path change";
    static final String CHANGES_COUNT = "changes count";
    
    static TreeMap tm;
    
    /**
     * affiche le résultat de la comparaison entre deux snapshot
     * @param ht contient les résutats a afficher
     * @param title le titre de la fenetre
     */
    public SnapshotResultGraph(Hashtable ht, String title)
    {
        super(title);
        this.setSize(700,400);
        Enumeration e = null;
        Hashtable resultHt = null;
        for (e = ht.elements(); e.hasMoreElements();) {
            resultHt = (Hashtable) e.nextElement();
            // on récupère la première hashtable qui représente un AS
            break;
        }
        tm = orderData(resultHt);
        CategoryDataset dataset = createDataset(resultHt);
        JFreeChart chart = createChart(dataset);
        ChartPanel chartPanel = new ChartPanel(chart, false);
        chartPanel.setPreferredSize(new Dimension(700,400));
        this.setContentPane(chartPanel);
    }
    
    /**
     * ordonne les données
     */
    private TreeMap orderData(Hashtable dataHt){
        // tri du dataset en fonction du nombre de changements
        TreeMap treeMap = new TreeMap();
        Hashtable changeCountHt = (Hashtable) dataHt.get(CHANGES_COUNT);
        float asTotalChanges = ((Integer) changeCountHt.get(AS_TOTAL_CHANGES)).floatValue();
        changeCountHt.remove(AS_TOTAL_CHANGES); // plus besoin, il ne reste que les id des nodes
        Enumeration<String> e = changeCountHt.keys();
        while(e.hasMoreElements()){
            String nodeId = e.nextElement();
            float totalChanges = ((Integer) changeCountHt.get(nodeId)).floatValue();
            float normalisedTotalChanges = totalChanges/asTotalChanges; // pour pouvoir les comparer sous forme de string 
            treeMap.put(normalisedTotalChanges+nodeId, nodeId);
        }
        return treeMap;
    }
    
    /**
     * create the dataset
     * @param resultHt containt the data needed to build the dataset
     * @return un dataset utilisé pour construire la graphe
     * @todo supporter plusieurs AS
     */
    private static CategoryDataset createDataset(Hashtable resultHt) {

        // create the dataset...
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // ht2.size() contient le nombre de noeuds sur lequel on a des informations
        String[] xAxisLabels = new String[tm.size()]; // xAxisLabels contient le nom des neouds
        
        int i = 0;
        while(!tm.isEmpty()){
            String nodeId = (String) tm.get(tm.lastKey()); // l'id du noeud
            tm.remove(tm.lastKey());
            xAxisLabels[i] = nodeId;
            i++;
        }
        // building data
        i = 0;
        Hashtable temp1;
        Hashtable temp2;
        while (i < xAxisLabels.length){            
            temp1 = (Hashtable) resultHt.get(xAxisLabels[i]);
            temp2 = (Hashtable) temp1.get(SERIES1);
            dataset.addValue(temp2.size(), SERIES1, xAxisLabels[i]);
            // temp2.size() est le nombre de prefixs down pour le noeud xAxisLabels[i]
            temp2 = (Hashtable) temp1.get(SERIES2);
            dataset.addValue(temp2.size(), SERIES2, xAxisLabels[i]);
            // idem ... pour prefix up
            temp2 = (Hashtable) temp1.get(SERIES3);
            dataset.addValue(temp2.size(), SERIES3, xAxisLabels[i]);
            // idem ...pour les peer change
            temp2 = (Hashtable) temp1.get(SERIES4);
            dataset.addValue(temp2.size(), SERIES4, xAxisLabels[i]);
            // ... 
            temp2 = (Hashtable) temp1.get(SERIES5);
            dataset.addValue(temp2.size(), SERIES5, xAxisLabels[i]);
            temp2 = (Hashtable) temp1.get(SERIES6);
            dataset.addValue(temp2.size(), SERIES6, xAxisLabels[i]);
            i++;
        }
        return dataset;
        
    }
    
    /**
     * create the chart
     * @param dataset 
     * @return un JFreeChart
     */
    private static JFreeChart createChart(CategoryDataset dataset) {
        // create the chart...
        JFreeChart chart = ChartFactory.createStackedBarChart(
                "BGP impacts of the changes",         // chart title
                "Node name",               // domain axis label
                "Changes",                  // range axis label
                dataset,                  // data
                PlotOrientation.VERTICAL, // orientation
                true,                     // include legend
                true,                     // tooltips?
                false                     // URLs?
        );
        
        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        
        // set the background color for the chart...
        chart.setBackgroundPaint(Color.white);
        
        // get a reference to the plot for further customisation...
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.white);
        
        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        
        // disable bar outlines...
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(
                CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
        );
        // OPTIONAL CUSTOMISATION COMPLETED.
        return chart;
    }
} 