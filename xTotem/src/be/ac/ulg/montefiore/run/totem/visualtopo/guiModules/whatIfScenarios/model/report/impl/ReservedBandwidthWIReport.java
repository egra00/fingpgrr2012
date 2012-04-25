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
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
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
* - 31-May-2007: Add class type parameter. (GMO)
* - 31-May-2007: Implements equals(.), getName(), getShortName(). (GMO)
*/

/**
* The data collected by this report is the current reserved bandwidth on all links. 
*
* <p>Creation date: 23/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class ReservedBandwidthWIReport extends LinksWIReport {
    private static final Logger logger = Logger.getLogger(ReservedBandwidthWIReport.class);

    int ct = -1;

    public ReservedBandwidthWIReport(Domain domain) {
        super(domain);
    }

    public ReservedBandwidthWIReport(Domain domain, int ct) {
        super(domain);
        this.ct = ct;
    }

    public void computeInitialData() {
        initialData = new double[domain.getConvertor().getMaxLinkId()];
        if (ct < 0) {
            for (Link l : domain.getAllLinks()) {
                try {
                    int id = domain.getConvertor().getLinkId(l.getId());
                    initialData[id] = (l.getReservedBandwidth() / l.getBandwidth()) * 100;
                } catch (LinkNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else {
            int priority = domain.getMinPriority(ct);
            for (Link l : domain.getAllLinks()) {
                try {
                    int id = domain.getConvertor().getLinkId(l.getId());
                    initialData[id] = (l.getReservedBandwidth(priority) / l.getBCs()[ct]) * 100;
                } catch (LinkNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void computeFinalData() {
        finalData = new double[domain.getConvertor().getMaxLinkId()];
        if (ct < 0) {
            for (Link l : domain.getAllLinks()) {
                try {
                    int id = domain.getConvertor().getLinkId(l.getId());
                    finalData[id] = (l.getReservedBandwidth() / l.getBandwidth()) * 100;
                } catch (LinkNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } else {
            int priority = domain.getMinPriority(ct);
            for (Link l : domain.getAllLinks()) {
                try {
                    int id = domain.getConvertor().getLinkId(l.getId());
                    initialData[id] = (l.getReservedBandwidth(priority) / l.getBCs()[ct]) * 100;
                } catch (LinkNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String getName() {
        if (ct < 0)
            return "Reserved Bw, %, All CTs ";
        else return "Reserved Bw, %, CT " + ct;
    }

    public String getShortName() {
        return "Reserved Bw";
    }

    /**
     * return true
     * @return
     */
    protected boolean hasCharts() {
        return true;
    }

    /**
     * Show two charts in a dialog: A decreasing line chart and a interval
     */
    protected void showCharts() {
        Chart newChart1 = null;
        try {
            newChart1 = new Chart(null, null);
        } catch (ChartParameterException e) {
            e.printStackTrace();
        }

        ChartData chartData = new ChartData();
        try {
            chartData.addRow("Reserved Bandwidth Before Scenario", initialData);
            chartData.addRow("Reserved Bandwidth After Scenario", finalData);
        } catch (InvalidChartDataException e) {
            e.printStackTrace();
        }
        newChart1.setData(chartData);

        try {
            newChart1.plot("DecreasingLineChartPlotter", "Load Variation Report - Charts", "Links (from most loaded to less loaded)", "Reserved Bandwidth (%)", null);
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
            newChart1.plot("LoadIntervalChartPlotter", "Load Variation : Interval graph", "Reserved bandwidth (%)", "Number of Links (%)", params);
        } catch (ChartParameterException e) {
            e.printStackTrace();
            logger.fatal(e.getMessage());
        }

        ChartPanel panel3 = new ChartPanel(newChart1.getPlot());
        panel1.add(panel3);
        MainWindow.getInstance().showDialog(panel1, "Utilization Charts");

    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof ReservedBandwidthWIReport)) {
            return false;
        } else {
            return ct == ((ReservedBandwidthWIReport)o).ct;
        }
    }

}
