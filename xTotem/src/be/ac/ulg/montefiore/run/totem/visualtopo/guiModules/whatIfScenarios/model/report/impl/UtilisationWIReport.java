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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.report.impl;

import be.ac.ulg.montefiore.run.totem.chart.model.Chart;
import be.ac.ulg.montefiore.run.totem.chart.model.ChartData;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.ChartParameterException;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.InvalidChartDataException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.report.LinksWIReport;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/*
* Changes:
* --------
* - 31-May-2007: Implements getShortName() and equals(.) (GMO)
* - 31-May-2007: the LinkLoadStrategy is now a parameter of the constructor. (GMO)
*/

/**
* The data collected by this class is the utilization obtained by routing the default traffic matrix with a standard
* CSPF and a SPFLinkLoadStrategy.
*
* <p>Creation date: 23/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class UtilisationWIReport extends LinksWIReport {
    private static final Logger logger = Logger.getLogger(UtilisationWIReport.class);

    private LinkLoadStrategy strategy;

    public UtilisationWIReport(Domain domain, LinkLoadStrategy lls) {
        super(domain);
        this.strategy = lls;
    }

    public void computeInitialData() {
        try {
            TrafficMatrix tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(domain.getASID());
            strategy.setTm(tm);
            strategy.recompute();

            initialData = strategy.getData().getUtilization();
            for (int i = 0; i < initialData.length; i++) {
                //convret to %
                initialData[i] *= 100;
            }
        } catch (InvalidTrafficMatrixException e) {
            e.printStackTrace();
        }
    }

    public void computeFinalData() {
        try {
            TrafficMatrix tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(domain.getASID());
            strategy.setTm(tm);
            strategy.recompute();

            finalData = strategy.getData().getUtilization();
            for (int i = 0; i < finalData.length; i++) {
                //convret to %
                finalData[i] *= 100;
            }
        } catch (InvalidTrafficMatrixException e) {
            e.printStackTrace();
        }

    }

    public String getName() {
        return "Utilization, " + strategy.toString();
    }

    public String getShortName() {
        return "Utilization";
    }

    protected boolean hasCharts() {
        return true;
    }

    protected void showCharts() {
        Chart newChart1 = null;
        try {
            newChart1 = new Chart(null, null);
        } catch (ChartParameterException e) {
            e.printStackTrace();
        }

        ChartData chartData = new ChartData();
        try {
            chartData.addRow("Utilization Before Scenario", initialData);
            chartData.addRow("Utilization After Scenario", finalData);
        } catch (InvalidChartDataException e) {
            e.printStackTrace();
        }
        newChart1.setData(chartData);

        try {
            newChart1.plot("DecreasingLineChartPlotter", "Load Variation Report - Charts", "Links (from most loaded to less loaded)", "Utilization (%)", null);
        } catch (ChartParameterException e) {
            e.printStackTrace();
            logger.fatal(e.getMessage());
        }

        JPanel panel1 = new JPanel(new GridLayout(2, 1));
        ChartPanel panel2 = new ChartPanel(newChart1.getPlot());
        panel1.add(panel2);

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("maxValue", "100");
        try {
            newChart1.plot("LoadIntervalChartPlotter", "Load Variation : Interval graph", "Load Interval", "Number of Links (%)", params);
        } catch (ChartParameterException e) {
            e.printStackTrace();
            logger.fatal(e.getMessage());
        }

        ChartPanel panel3 = new ChartPanel(newChart1.getPlot());
        panel1.add(panel3);
        MainWindow.getInstance().showDialog(panel1, "Utilization Charts");
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof UtilisationWIReport)) {
            return false;
        } else {

            return strategy.equals(((UtilisationWIReport)o).strategy);
        }
    }
}
