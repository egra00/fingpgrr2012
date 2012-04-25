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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.chart;

import be.ac.ulg.montefiore.run.totem.chart.model.Chart;
import be.ac.ulg.montefiore.run.totem.chart.model.plotters.PlotterManager;
import be.ac.ulg.montefiore.run.totem.chart.model.plotters.ChartPlotter;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.NoSuchChartException;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.ChartParameterException;
import be.ac.ulg.montefiore.run.totem.chart.facade.ChartManager;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ParamTable;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.jfree.chart.ChartPanel;

/*
* Changes:
* --------
* - 13-Aug-2007: Add default values for axis and titles (GMO)
*/

/**
* Panel that is used to plot a chart whose name is given in the constructor. It asks for parameters such as chart title,
* xAxisTitle, yAxisTitle, the plotter type and the specific plotter parameters. On acceptance, it retrieve the chart
* from the manager and plot it using its plot(..) method.
*
* <p>Creation date: 24 janv. 2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class PlotterPanel extends JPanel {
    private JTextField title;
    private JTextField xAxisTitle;
    private JTextField yAxisTitle;
    private ParamTable params;
    private JComboBox plotters;
    private String chartName;

    public PlotterPanel(String chartName) {
        this.chartName = chartName;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 5));

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.PAGE_AXIS));

        JLabel label;

        label = new JLabel("Chart title :");
        northPanel.add(label);
        title = new JTextField(chartName);
        northPanel.add(title);

        label = new JLabel("X Axis title :");
        northPanel.add(label);
        xAxisTitle = new JTextField();
        northPanel.add(xAxisTitle);

        label = new JLabel("Y Axis title :");
        northPanel.add(label);
        yAxisTitle = new JTextField();
        northPanel.add(yAxisTitle);

        label = new JLabel("Available plotters :");
        northPanel.add(label);
        plotters = new JComboBox();
        for (String plot : PlotterManager.getInstance().getAvailablePlotters()) {
            plotters.addItem(plot);
        };
        plotters.addActionListener(new SelectPlotterListener());
        northPanel.add(plotters);


        add(northPanel, BorderLayout.NORTH);

        params = new ParamTable(3);
        add(new JScrollPane(params) {
            public Dimension getPreferredSize() {
                int width = super.getPreferredSize().width;
                int height = params.getRowHeight() * 6;
                return new Dimension(width, height);
            }
        }, BorderLayout.CENTER);

        JButton accept = new JButton("Plot Chart");
        accept.addActionListener(new AcceptListener());
        add(accept, BorderLayout.SOUTH);

        plotters.setSelectedIndex(0);
    }

    private class SelectPlotterListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            ChartPlotter plotter = PlotterManager.getInstance().getPlotterInstance((String)plotters.getSelectedItem());
            List<ParameterDescriptor> lst = PlotterManager.getInstance().getPlotterParameters((String)plotters.getSelectedItem());
            params.fill(lst);
            yAxisTitle.setText(plotter.getDefaultYAxisTitle());
            xAxisTitle.setText(plotter.getDefaultXAxisTitle());
        }
    }

    private class AcceptListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // stop cell editing
            if (params.getCellEditor() != null && !params.getCellEditor().stopCellEditing())
                return;

            String cTitle = title.getText().equals("") ? null : title.getText();
            String cXTitle = xAxisTitle.getText().equals("") ? null : xAxisTitle.getText();
            String cYTitle = yAxisTitle.getText().equals("") ? null : yAxisTitle.getText();

            Chart chart = null;
            try {
                chart = ChartManager.getInstance().getChart(chartName);
                chart.plot((String)plotters.getSelectedItem(), cTitle, cXTitle, cYTitle, params.toHashMap());
                //close the dialog window on success
                ((JDialog)getRootPane().getParent()).dispose();
                MainWindow.getInstance().showDialog(new ChartPanel(chart.getPlot()), "Chart: " + chartName);
                MainWindow.getInstance().updateChartMenu();
            } catch (NoSuchChartException e1) {
                e1.printStackTrace();
                return;
            } catch (ChartParameterException e1) {
                e1.printStackTrace();
            }
        }
    }
}
