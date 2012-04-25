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
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LoadData;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.report.LinksWIReport;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;


/*
* Changes:
* --------
* - 31-May-2007: Implement showCharts(), getShortName() and equals(.) (GMO)
* - 31-May-2007: the LinkLoadStrategy is now a parameter of the constructor. (GMO)
*/

/**
* The data collected by this class is the load obtained by routing the default traffic matrix with a standard CSPF and
*  a SPFLinkLoadStrategy.
* This class does not have charts.
*
* <p>Creation date: 23/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LoadWIReport extends LinksWIReport {
    private final static Logger logger = Logger.getLogger(LoadWIReport.class);

    private LinkLoadStrategy strategy;
    
    // to design a more complex report
    protected LoadData initialLoadData;
    protected LoadData finalLoadData;

    public LoadWIReport(Domain domain, LinkLoadStrategy lls) {
        super(domain);
        this.strategy = lls;
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
            chartData.addRow("Load Before Scenario", initialData);
            chartData.addRow("Load After Scenario", finalData);
        } catch (InvalidChartDataException e) {
            e.printStackTrace();
        }
        newChart1.setData(chartData);

        try {
            newChart1.plot("DecreasingLineChartPlotter", "Load Variation Report - Charts", "Links (from most loaded to less loaded)", "Load", null);
        } catch (ChartParameterException e) {
            e.printStackTrace();
            logger.fatal(e.getMessage());
        }

        ChartPanel panel = new ChartPanel(newChart1.getPlot());
        MainWindow.getInstance().showDialog(panel, "Load Chart");

    }

    public void computeInitialData() {
        try {
            TrafficMatrix tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(domain.getASID());
            strategy.setTm(tm);
            strategy.recompute();

            initialLoadData = strategy.detachData();
            initialData = initialLoadData.getLoad();
        } catch (InvalidTrafficMatrixException e) {
            e.printStackTrace();
        }
    }

    public void computeFinalData() {
        try {
            TrafficMatrix tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(domain.getASID());
            strategy.setTm(tm);
            strategy.recompute();

            finalLoadData = strategy.detachData();
            finalData = finalLoadData.getLoad();
        } catch (InvalidTrafficMatrixException e) {
            e.printStackTrace();
        }

    }

    public String getName() {
        return "Load, " + strategy.toString();
    }

    public String getShortName() {
        return "Load";
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof LoadWIReport)) {
            return false;
        } else {
            return strategy.equals(((LoadWIReport)o).strategy);
        }
    }
}
