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

import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ProgressBarPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.ActionExecutionException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.BadParametersException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIAction;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIActionPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.impl.*;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.report.WIReport;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.report.DetailedWIObservationPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/*
* Changes:
* --------
* - 31-May-2007: Use an observation panel with options (DetailedWIObservationPanel). (GMO)
* - 31-May-2007: Add change link capacity event. (GMO)
* - 28-Feb-2008: remove change TM option, fix bug in list display (GMO)
*/

/**
* Dialog to permit the composition of multiple WI events. New events must be added inside the setupUI() method. 
*
* <p>Creation date: 24/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ComposeEventsDialog extends JDialog {
    private JList actionList;
    private DefaultListModel listModel;
    private JTabbedPane pane;
    private DetailedWIObservationPanel obsvPanel;

    private Domain domain;

    public ComposeEventsDialog() {
        super(MainWindow.getInstance(), "Compose Events", false);

        domain = InterDomainManager.getInstance().getDefaultDomain();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        JPanel listBtnsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btn = new JButton("Move up");
        btn.addActionListener(new MoveUpActionListener());
        listBtnsPanel.add(btn);
        btn = new JButton("Move Down");
        btn.addActionListener(new MoveDownActionListener());
        listBtnsPanel.add(btn);
        btn = new JButton("Delete");
        btn.addActionListener(new DeleteActionListener());
        listBtnsPanel.add(btn);

        listModel = new DefaultListModel();
        actionList = new JList(listModel);
        actionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        actionList.setCellRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof WIAction) {
                    setText(((WIAction)value).getName());
                }
                return this;
            }
        });

        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.setBorder(BorderFactory.createTitledBorder("Events list"));
        westPanel.add(new JScrollPane(actionList), BorderLayout.CENTER);
        westPanel.add(listBtnsPanel, BorderLayout.SOUTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));


        btn = new JButton("Execute Event List");
        btn.addActionListener(new ExecuteEventsActionListener());
        btnPanel.add(btn);

        btn = new JButton("Cancel");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        btnPanel.add(btn);

        pane = new JTabbedPane(JTabbedPane.NORTH);

        WIActionPanel acPanel;
        acPanel = new LinkDownWIActionPanel(domain);
        pane.add(acPanel.getWIActionName(), acPanel);
        acPanel = new LinkUpWIActionPanel(domain);
        pane.add(acPanel.getWIActionName(), acPanel);
        acPanel = new NodeDownWIActionPanel(domain);
        pane.add(acPanel.getWIActionName(), acPanel);
        acPanel = new NodeUpWIActionPanel(domain);
        pane.add(acPanel.getWIActionName(), acPanel);
        /*acPanel = new ChangeTrafficWIActionPanel(domain);
        pane.add(acPanel.getWIActionName(), acPanel);*/
        acPanel = new ChangeLinkCapacityWIActionPanel(domain);
        pane.add(acPanel.getWIActionName(), acPanel);

        JPanel generalPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;

        JPanel eventsPanel = new JPanel(new BorderLayout());
        eventsPanel.setBorder(BorderFactory.createTitledBorder("Events"));
        eventsPanel.add(pane, BorderLayout.CENTER);

        btn = new JButton("Add Event");
        btn.addActionListener(new AddEventActionListener());
        JPanel addEventBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addEventBtnPanel.add(btn);
        eventsPanel.add(addEventBtnPanel, BorderLayout.SOUTH);

        generalPanel.add(eventsPanel, c);
        c.gridy++;

        obsvPanel = new DetailedWIObservationPanel(domain);
        generalPanel.add(obsvPanel, c);

        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.add(generalPanel, BorderLayout.CENTER);
        eastPanel.add(btnPanel, BorderLayout.SOUTH);

        add(westPanel, BorderLayout.WEST);
        add(eastPanel, BorderLayout.CENTER);

    }

    public void dispose() {
        for (int i = 0; i < pane.getTabCount(); i++) {
                ((WIActionPanel)pane.getComponentAt(i)).destroy();
        }
        super.dispose();
    }

    private ComposeEventsDialog getThis() {
        return this;
    }

    private class MoveUpActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int index = actionList.getSelectedIndex();

            if (index < 1) return;

            Object ac1 = listModel.getElementAt(index);
            Object ac2 = listModel.getElementAt(index-1);

            listModel.setElementAt(ac2, index);
            listModel.setElementAt(ac1, index-1);

            actionList.setSelectedIndex(index-1);
        }
    }

    private class MoveDownActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int index = actionList.getSelectedIndex();

            if (index < 0) return;
            if (index+1 >= listModel.size()) return;

            Object ac1 = listModel.getElementAt(index);
            Object ac2 = listModel.getElementAt(index+1);

            listModel.setElementAt(ac2, index);
            listModel.setElementAt(ac1, index+1);

            actionList.setSelectedIndex(index+1);
        }
    }

    private class DeleteActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            int index = actionList.getSelectedIndex();
            if (index < 0) return;

            listModel.remove(index);
        }
    }

    private class AddEventActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            WIAction action;
            try {
                action = ((WIActionPanel)pane.getSelectedComponent()).createWIAction();
                listModel.addElement(action);
            } catch (BadParametersException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(getThis(), "Bad Parameters " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class ExecuteEventsActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {
                public void run() {
                    List<WIReport> list = obsvPanel.getReports();
                    if (list.size() < 1) {
                        JOptionPane.showMessageDialog(MainWindow.getInstance(), "Please select at least one report.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    ProgressBarPanel progressBar = new ProgressBarPanel(0, 100);
                    progressBar.setMessage("It can take several minutes for big Domains");
                    progressBar.setCancelable(false);
                    progressBar.getProgressBar().setIndeterminate(true);
                    progressBar.getProgressBar().setSize(500, 60);

                    JDialog pDialog = MainWindow.getInstance().showDialog(progressBar, "Generating Report ...");
                    pDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

                    dispose();

                    progressBar.setMessage("Getting initial data ...");
                    for (WIReport report : list) {
                        report.computeInitialData();
                    }

                    progressBar.setMessage("Executing required actions ...");
                    for (int i = 0; i < listModel.size(); i++) {
                        try {
                            ((WIAction) listModel.getElementAt(i)).execute();
                        } catch (ActionExecutionException e1) {
                            JOptionPane.showMessageDialog(getThis(), "Impossible to execute action: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }

                    progressBar.setMessage("Getting final data ...");
                    for (WIReport report : list) {
                        report.computeFinalData();
                    }

                    JDialog dialog = new JDialog(MainWindow.getInstance(), "What-If scenario report", false);
                    JTabbedPane tabbedPane = new JTabbedPane();
                    for (WIReport report : list) {
                        tabbedPane.add(report.getName(), report.getPanel());
                    }
                    dialog.add(tabbedPane);
                    dialog.setLocationRelativeTo(MainWindow.getInstance());
                    dialog.setVisible(true);
                    dialog.pack();

                    pDialog.dispose();
                }
            }).start();
        }
    }

}
