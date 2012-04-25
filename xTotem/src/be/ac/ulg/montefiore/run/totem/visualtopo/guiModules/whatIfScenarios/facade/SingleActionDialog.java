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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.facade;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.ActionExecutionException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.BadParametersException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIActionPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.report.WIReport;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.report.DetailedWIObservationPanel;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/*
* Changes:
* --------
* - 31-May-2007: Use DetailedWIObservationPanel (GMO)
*/

/**
* Dialog to execute a single WhatIf action. Displays the panel corresponding to the action and an instance of
*  {@link DetailedWIObservationPanel} to choose the value to compare before and after the action execution.
*
* <p>Creation date: 23/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class SingleActionDialog extends JDialog {

    private WIActionPanel actionPanel;
    private DetailedWIObservationPanel observationPanel;
    private Domain domain;

    public SingleActionDialog(WIActionPanel panel) {
        super(MainWindow.getInstance(), panel.getWIActionName());
        actionPanel = panel;
        domain = InterDomainManager.getInstance().getDefaultDomain();
        observationPanel = new DetailedWIObservationPanel(domain);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout(5, 5));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));

        JButton okBtn = new JButton("Execute");
        okBtn.addActionListener(new ExecuteActionListener());

        buttonPanel.add(okBtn);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(cancelButton);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;

        mainPanel.add(actionPanel, c);
        c.gridy++;
        mainPanel.add(observationPanel, c);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public void dispose() {
        actionPanel.destroy();
        super.dispose();
    }

    private class ExecuteActionListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            try {

                List<WIReport> list = observationPanel.getReports();

                /* get initial state for observations */
                for (WIReport report : list) {
                    report.computeInitialData();
                }

                /* execute the action */
                actionPanel.createWIAction().execute();

                /* get final state for observations */
                for (WIReport report : list) {
                    report.computeFinalData();
                }

                JTabbedPane tabbedPane = new JTabbedPane();
                for (WIReport report : list) {
                    tabbedPane.add(report.getName(), report.getPanel());
                }

                MainWindow.getInstance().showDialog(tabbedPane, "What-If scenario report", false);

            } catch (ActionExecutionException e1) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Error while executing the action.", "Error", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            } catch (BadParametersException e1) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Error while executing the action: bad parameters.", "Error", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        }
    }

}
