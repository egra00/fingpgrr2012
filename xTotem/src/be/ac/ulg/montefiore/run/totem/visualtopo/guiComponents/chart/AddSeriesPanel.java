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

import be.ac.ulg.montefiore.run.totem.chart.facade.ChartManager;
import be.ac.ulg.montefiore.run.totem.chart.model.Chart;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.NoSuchChartException;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.ChartParameterException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ParamTable;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/*
* Changes:
* --------
* - 13-Aug-2007: add default value for series name (GMO)
*/

/**
* Panel that is used to add series to an existing chart. The chart identifier is passed in the constructor.
*
* <p>Creation date: 24 janv. 2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class AddSeriesPanel extends JPanel {

    private String chartName;
    private JTextField seriesName;
    private ParamTable params;
    private Chart chart = null;

    public AddSeriesPanel(String chartName) {
        this.chartName = chartName;
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 5));

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.PAGE_AXIS));
        northPanel.setBorder(BorderFactory.createTitledBorder("Information"));
        JLabel label;
        label = new JLabel("Chart name: " + chartName);
        northPanel.add(label);

        try {
            chart = ChartManager.getInstance().getChart(chartName);
        } catch (NoSuchChartException e) {
            e.printStackTrace();
            return;
        }

        label = new JLabel("Data collector type: " + chart.getCollector().getClass().getSimpleName());
        northPanel.add(label);

        add(northPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());

        JPanel centerNorth = new JPanel();
        centerNorth.setLayout(new BoxLayout(centerNorth, BoxLayout.PAGE_AXIS));
        label = new JLabel("Series Name :");
        centerNorth.add(label);
        seriesName = new JTextField();
        centerNorth.add(seriesName);
        seriesName.setText(chart.getCollector().getDefaultSeriesName());

        centerPanel.add(centerNorth, BorderLayout.NORTH);

        params = new ParamTable(3);
        centerPanel.add(new JScrollPane(params) {
            public Dimension getPreferredSize() {
                int width = super.getPreferredSize().width;
                int height = params.getRowHeight() * 7;
                return new Dimension(width, height);
            }
        }, BorderLayout.CENTER);
        params.fill(chart.getCollector().getDataParameters());

        add(centerPanel, BorderLayout.CENTER);

        JButton accept = new JButton("Add Series");
        accept.addActionListener(new AcceptListener());
        add(accept, BorderLayout.SOUTH);

    }

    private class AcceptListener implements ActionListener {

        /**
         * Add series to the chart
         * @param e
         */
        public void actionPerformed(ActionEvent e) {

            if (seriesName.getText().equals("")) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Please provide a name to the series", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                chart.addSeries(seriesName.getText(), params.toHashMap());
                //close the dialog window on success
                ((JDialog)getRootPane().getParent()).dispose();
            } catch (ChartParameterException e1) {
                //e1.printStackTrace();
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Error: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
