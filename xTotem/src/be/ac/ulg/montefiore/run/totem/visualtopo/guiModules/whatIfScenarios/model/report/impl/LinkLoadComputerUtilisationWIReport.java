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

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.report.LinksWIReport;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.chart.model.Chart;
import be.ac.ulg.montefiore.run.totem.chart.model.ChartData;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.ChartParameterException;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.InvalidChartDataException;
import org.jfree.chart.ChartPanel;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

/*
* Changes:
* --------
*/

/**
 * Reports to compare the utilisation calculated by LinkLoadComputer. It uses two LinkLoadComputer objects
 * (that can be the same). one is used to compute the initial data and the other for the final data. A dropped
 * traffic panel is added to the LinksWIReport.
 *
 * <p>Creation date: 25/02/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LinkLoadComputerUtilisationWIReport extends LinksWIReport {
    private static final Logger logger = Logger.getLogger(LinkLoadComputerUtilisationWIReport.class);

    private final LinkLoadComputer initialLLC;
    private final LinkLoadComputer finalLLC;

    private double initialDroppedTraffic;
    private double finalDroppedTraffic;

    public LinkLoadComputerUtilisationWIReport(Domain domain, LinkLoadComputer initialLLC, LinkLoadComputer finalLLC) {
        super(domain);
        this.initialLLC = initialLLC;
        this.finalLLC = finalLLC;
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

    public void computeInitialData() {
        initialLLC.update();
        initialData = initialLLC.getData().getUtilization();
        for (int i = 0; i < initialData.length; i++) {
            initialData[i] *= 100;
        }
        initialDroppedTraffic = initialLLC.getData().getDroppedTraffic();
    }

    public void computeFinalData() {
        finalLLC.update();
        finalData = finalLLC.getData().getUtilization();
        for (int i = 0; i < finalData.length; i++) {
            finalData[i] *= 100;
        }
        finalDroppedTraffic = finalLLC.getData().getDroppedTraffic();
    }

    public String getName() {
        return "Utilisation, " + initialLLC.getClass().getSimpleName() + " --> " + finalLLC.getClass().getSimpleName();
    }

    public String getShortName() {
        return "Utilisation";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof LinkLoadComputerUtilisationWIReport)) {
            return false;
        } else {
            LinkLoadComputerUtilisationWIReport r = (LinkLoadComputerUtilisationWIReport)o;
            return initialLLC.equals(r.initialLLC) && finalLLC.equals(r.finalLLC);
        }
    }

    public JPanel getPanel() {

        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.gridy = 0;
        c1.insets = new Insets(0, 0, 5, 10);
        c1.anchor = GridBagConstraints.LINE_START;
        c1.weightx = 0;

        final JPanel myPanel = new JPanel(new GridBagLayout());
        myPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Dropped traffic"));

        final JLabel label16 = new JLabel("Dropped traffic Before scenario");
        myPanel.add(label16, c1);
        c1.gridx++;
        c1.weightx = 1.0;
        final JLabel label13 = new JLabel(formatter.format(initialDroppedTraffic));
        myPanel.add(label13, c1);
        c1.weightx = 0;
        c1.gridx--;
        c1.gridy++;
        final JLabel label15 = new JLabel("Dropped traffic After scenario");
        myPanel.add(label15, c1);
        c1.gridx++;
        c1.weightx = 1.0;
        final JLabel label14 = new JLabel(formatter.format(finalDroppedTraffic));
        myPanel.add(label14, c1);

        addStatPanel(myPanel);

        return super.getPanel();
    }
}
