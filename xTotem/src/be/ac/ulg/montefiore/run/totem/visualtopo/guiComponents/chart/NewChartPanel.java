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
import be.ac.ulg.montefiore.run.totem.chart.model.collectors.DataCollectorManager;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.ChartParameterException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ParamTable;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/*
* Changes:
* --------
*
*/

/**
* Panel that asks for new chart parameters. When the parameters are accepted, a new Chart object is created and added
* to the ChartManager. The Chart menu of the Main Window is also updated.
*
* <p>Creation date: 24 janv. 2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class NewChartPanel extends JPanel {
    private JComboBox collectors;
    private JTextField chartName;
    private ParamTable params;
    static private MainWindow mainWindow = MainWindow.getInstance();
    static private ChartManager chartManager = ChartManager.getInstance();


    public NewChartPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 5));

        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.PAGE_AXIS));

        JLabel label;

        label = new JLabel("Chart identifier :");
        northPanel.add(label);
        chartName = new JTextField();
        northPanel.add(chartName);

        label = new JLabel("Available data collectors :");
        northPanel.add(label);
        collectors = new JComboBox();
        for (String col : DataCollectorManager.getInstance().getAvailableDataCollectors()) {
            collectors.addItem(col);
        };
        collectors.addActionListener(new SelectCollectorListener());

        northPanel.add(collectors);

        add(northPanel, BorderLayout.NORTH);

        params = new ParamTable(3);
        add(new JScrollPane(params) {
            public Dimension getPreferredSize() {
                int width = super.getPreferredSize().width;
                int height = params.getRowHeight() * 6;
                return new Dimension(width, height);
            }
        }, BorderLayout.CENTER);

        JButton accept = new JButton("Create Chart");
        accept.addActionListener(new AcceptListener());
        add(accept, BorderLayout.SOUTH);

        collectors.setSelectedIndex(0);
    }

    private class SelectCollectorListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            List<ParameterDescriptor> lst = DataCollectorManager.getInstance().getCollectorParameters((String)collectors.getSelectedItem());
            params.fill(lst);
        }
    }

    private class AcceptListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // stop cell editing
            if (params.getCellEditor() != null && !params.getCellEditor().stopCellEditing())
                return;

            if (chartName.getText().equals("")) {
                JOptionPane.showMessageDialog(mainWindow, "Please provide a name to the chart", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Chart chart = null;
            try {
                chart = new Chart((String) collectors.getSelectedItem(), params.toHashMap());
            } catch (ChartParameterException ex) {
                mainWindow.errorMessage("Exception in parameter: " + ex.getMessage());
                return;
            }

            try {
                chartManager.addChart(chartName.getText(), chart);
                //close the dialog window on success
                ((JDialog)getRootPane().getParent()).dispose();
                mainWindow.updateChartMenu();
            } catch (ChartParameterException e1) {
                mainWindow.errorMessage("A chart with the same name already exists.");
            }
        }
    }
}
