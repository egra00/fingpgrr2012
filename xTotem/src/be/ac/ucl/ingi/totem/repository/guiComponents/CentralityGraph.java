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


import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TreeMap;

/**
 * 
 * @author Thomas Vanstals
 */

public class CentralityGraph extends JFrame
{
    CategoryDataset nodeDataset;
    CategoryDataset linkDataset;
    JFreeChart nodeChart;
    JFreeChart linkChart;
    JCheckBox normalize;
    Hashtable resultHt;
    int intraNormalisationFactor;
    int extraNormalisationFactor;
    TreeMap nodeTreeMap;
    TreeMap linkTreeMap;
    
    static final String INTRA_NORMALIZATION_FACTOR = "intraNormalisationFactor";
    static final String EXTRA_NORMALIZATION_FACTOR = "extraNormalisationFactor";
    static final String NODE = "node";
    static final String LINK = "link";
    static final String NORMALIZE = "normalize";
    static final String NONE = "none";
    
    
    public CentralityGraph(Hashtable ht, String title)
    {
        super(title);
        resultHt = ht;
        intraNormalisationFactor = ((Integer) resultHt.get(INTRA_NORMALIZATION_FACTOR)).intValue();
        extraNormalisationFactor = ((Integer) resultHt.get(EXTRA_NORMALIZATION_FACTOR)).intValue();
        
        JPanel jp = new JPanel();
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
        Container contents = getContentPane();
        
        // ordering the dataset
        nodeTreeMap = orderData((Hashtable) resultHt.get(NODE));
        linkTreeMap = orderData((Hashtable) resultHt.get(LINK));
        
        
        // creating dataset
        nodeDataset = new DefaultCategoryDataset();
        linkDataset = new DefaultCategoryDataset();
        updateDataset((DefaultCategoryDataset) nodeDataset, NODE, NORMALIZE, (TreeMap) nodeTreeMap.clone());
        updateDataset((DefaultCategoryDataset) linkDataset, LINK, NORMALIZE, (TreeMap) linkTreeMap.clone());
        
        // creating charts
        nodeChart = createChart(nodeDataset, NODE);
        linkChart = createChart(linkDataset, LINK);
        JTabbedPane jtp;
        // filling JTabbedPane
        jtp = new JTabbedPane();
        jtp.addTab(NODE, new ChartPanel(nodeChart, false));
        jtp.addTab(LINK, new ChartPanel(linkChart, false));
        
        normalize = new JCheckBox("Normalize centrality");
        normalize.setSelected(true);
        normalize.addActionListener(new normalizeListener());
        
        jp.add(jtp);
        JPanel jpb = new JPanel();
        jpb.add(normalize);
        //jpb.add(reset);
        jp.add(jpb);
        contents.add(jp);
    }
    
    /**
     * ordone les données
     * @param dataHt la hashtable qui contient les données à ordonner
     * @return les données ordonnées dans un TreeMap
     */
    private TreeMap orderData(Hashtable dataHt){
        // tri du dataset en fonction de la centralité intra
        TreeMap treeMap = new TreeMap();
        Enumeration<String> e = dataHt.keys();
        while(e.hasMoreElements()){
            String nodeId = e.nextElement();
            float IntraCentrality=((float) ((int [])dataHt.get(nodeId))[0])/intraNormalisationFactor;
            treeMap.put(IntraCentrality+" - "+nodeId, nodeId); 
            // il faut ajouter le nodeId pour éviter que les clés nulles ne soient écrasées
        }
        return treeMap;
    }
    
    /**
     * met a jour le graphe lorsque l'utilisateur clique sur la JCheckBox
     * @author Thomas Vanstals
     *
     */
    private class normalizeListener implements ActionListener {
        
        public void actionPerformed(ActionEvent ae) {
            if(normalize.isSelected()){ // normalize
                updateDataset((DefaultCategoryDataset) nodeDataset, NODE, NORMALIZE, (TreeMap) nodeTreeMap.clone());
                updateDataset((DefaultCategoryDataset) linkDataset, LINK, NORMALIZE, (TreeMap) linkTreeMap.clone());
            }
            else {// reset
                updateDataset((DefaultCategoryDataset) nodeDataset, NODE, NONE, (TreeMap) nodeTreeMap.clone());
                updateDataset((DefaultCategoryDataset) linkDataset, LINK, NONE, (TreeMap) linkTreeMap.clone());                
            }
            // mise a jour des graphes
            nodeChart.fireChartChanged();
            linkChart.fireChartChanged();
        }
    }
    
    /**
     * met a jour le dataset contenant les valeurs des centralités affichées dans le graphe
     * @param dataset le dataset a mettre a jour
     * @param type le type de centralité a mettre a jour : node ou link
     * @param action précise si il faut normaliser ou pas
     * @param treeMap les id des noeuds/liens ordonnées en fonction de la centralité intra
     */
    private void updateDataset(DefaultCategoryDataset dataset, String type, String action, TreeMap treeMap) {
        dataset.clear(); // reset the data set
        Hashtable dataHt = (Hashtable) resultHt.get(type); // on prend la hashtable correspondante au type demandé
        
        // row keys...
        String series1 = type+" inta-centrality";
        String series2 = type+" extra-centrality";
        
        // computing normalisation factors
        float localIntraNormalisationFactor = 1; // pas de normalisation
        float localExtraNormalisationFactor = 1; // pas de normalisation
        if (action.equals("normalize")){ // the data have to be normalised
            localIntraNormalisationFactor = intraNormalisationFactor;
            localExtraNormalisationFactor = extraNormalisationFactor;
        }
        
        // filling the dataset...        
        while(!treeMap.isEmpty()){
            String nodeId = (String) treeMap.get(treeMap.lastKey()); // l'id du noeud
            treeMap.remove(treeMap.lastKey());
            // calcul des centralités 
            float IntraCentrality=((float) ((int [])dataHt.get(nodeId))[0])/localIntraNormalisationFactor;
            float ExtraCentrality=((float) ((int [])dataHt.get(nodeId))[1])/localExtraNormalisationFactor;
            // ajout des centralités dans le dataset
            dataset.addValue(IntraCentrality, series1, nodeId);
            dataset.addValue(ExtraCentrality, series2, nodeId);
        }
    }    
    
    /**
     * crée le graphe
     * @param dataset le dataset à ploter
     * @param type "node" ou "link"
     */
    private static JFreeChart createChart(CategoryDataset dataset, String type) {
        
        final CategoryAxis categoryAxis = new CategoryAxis(type);
        final NumberAxis valueAxis = new NumberAxis("Value");
        final CategoryPlot plot = new CategoryPlot(dataset,
                categoryAxis,
                valueAxis,
                new LayeredBarRenderer());
        plot.setOrientation(PlotOrientation.VERTICAL);
        final JFreeChart chart = new JFreeChart(null,
                JFreeChart.DEFAULT_TITLE_FONT, plot, true);
        
        final LayeredBarRenderer renderer = (LayeredBarRenderer) plot.getRenderer();
        // size of the bar
        renderer.setSeriesBarWidth(0, 1.0);
        renderer.setSeriesBarWidth(1, 0.3);
        final CategoryAxis domainAxis = plot.getDomainAxis();
        // rotate the xAxis content
        domainAxis.setCategoryLabelPositions(
                CategoryLabelPositions.UP_90
                // CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 6.0)
        );
        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        // display the yAxis content as integer
        return chart;
    }
} 