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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.scenario;

import be.ac.ulg.montefiore.run.totem.scenario.model.Scenario;
import be.ac.ulg.montefiore.run.totem.scenario.facade.ScenarioExecutionContext;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.TextAreaOutputStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

/*
* Changes:
* --------
* - 19-May-2006 : add "stop on error" checkbox (GMO)
* - 10-Jan-2007 : use ScenarioExecutionContext class, rename stopOnError checkbox (GMO)
* - 09-Aug-2007 : change default resize weight parameters (GMO)
* - 09-Aug-2007 : Add "expand all" and "collapse all" buttons (GMO)
*/

/**
* Graphical representation of a scenario in a panel.
* A ScenarioJTree is used to represent the scenario.
*
* <p>Creation date: 9 janv. 2006
*
* @see ScenarioJTree
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ScenarioExecutor {

    private static final Icon expandAllIcon;
    private static final Icon collapseAllIcon;

    static {
        URL url = ScenarioExecutor.class.getResource("/resources/img/expandall.gif");
        if (url != null)
            expandAllIcon = new ImageIcon(url);
        else expandAllIcon = null;
        url = ScenarioExecutor.class.getResource("/resources/img/collapseall.gif");
        if (url != null)
            collapseAllIcon = new ImageIcon(url);
        else collapseAllIcon = null;
    }

    private ScenarioJTree tree = null;
    private JButton stepButton = null;
    private JButton endButton = null;
    private JButton advanceButton = null;
    private JCheckBox stopOnError = null;
    private TextAreaOutputStream output = null;
    private JPanel panel = null;
    private Scenario scenario;

    /**
     * Create a new tree representing the given scenario
     * @param scenario
     */
    public ScenarioExecutor(Scenario scenario) {
        JTextArea area = new JTextArea();
        area.setLineWrap(true);
        area.setEditable(false);
        this.scenario = scenario;
        ScenarioExecutionContext.setContext(scenario.getScenarioPath());
        output = new TextAreaOutputStream(area);
        tree = new ScenarioJTree(scenario, output, false);
    }

    /**
     * build a panel containg the representation of the scenario in addition to control buttons
     *  (step, finish execution, execute to selection).
     * @return the built panel
     */
    public JPanel getPanel() {
        panel = new JPanel(new BorderLayout());

        JPanel additionalBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        JLabel expLabel;
        if (expandAllIcon != null)
             expLabel = new JLabel(expandAllIcon);
        else expLabel = new JLabel("Expand all");
        expLabel.setToolTipText("Expand all");
        expLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                tree.expandAll();
            }
        });

        JLabel colLabel;
        if (collapseAllIcon != null)
            colLabel = new JLabel(collapseAllIcon);
        else colLabel = new JLabel("Collapse all");
        colLabel.setToolTipText("Collapse all");
        colLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                tree.collapseAll();
            }
        });
        additionalBtnPanel.add(expLabel);
        additionalBtnPanel.add(colLabel);
        panel.add(additionalBtnPanel, BorderLayout.NORTH);

        JSplitPane jsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        jsp.setOneTouchExpandable(true);
        jsp.setTopComponent(new JScrollPane(tree.getJTree()));
        jsp.setBottomComponent(new JScrollPane(output.getTextArea()));
        jsp.setResizeWeight(0.7);
        panel.add(jsp, BorderLayout.CENTER);

        JPanel commandPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        stepButton = new JButton("Step");
        stepButton.addActionListener(new StepActionListener());
        buttonPanel.add(stepButton);

        advanceButton = new JButton("Advance to selection");
        advanceButton.addActionListener(new AdvanceToSelectionActionListener());
        buttonPanel.add(advanceButton);

        endButton = new JButton("Finish Execution");
        endButton.addActionListener(new FinishExecutionActionListener());
        buttonPanel.add(endButton);

        commandPanel.add(buttonPanel, BorderLayout.CENTER);
        JPanel optionsPanel = new JPanel(new FlowLayout());

        stopOnError = new JCheckBox("Stop on error");
        stopOnError.addActionListener(new StopOnErrorActionListener());
        stopOnError.setSelected(true);
        tree.setStopOnError(true);
        optionsPanel.add(stopOnError);

        commandPanel.add(optionsPanel, BorderLayout.SOUTH);
        panel.add(commandPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void executed() {
        if (tree.isExecutionFinished()) {
            stepButton.setEnabled(false);
            endButton.setEnabled(false);
            advanceButton.setEnabled(false);
            stopOnError.setEnabled(false);
        }
        //MainWindow.getInstance().refresh();
    }

    private class StepActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            ScenarioExecutionContext.setContext(scenario.getScenarioPath());
            tree.step();
            executed();
        }
    }

    private class AdvanceToSelectionActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            ScenarioExecutionContext.setContext(scenario.getScenarioPath());
            int count = tree.executeToSelection();
            executed();
            if (count == 0) {
                JOptionPane.showMessageDialog(panel, "Events already executed.", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private class FinishExecutionActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            ScenarioExecutionContext.setContext(scenario.getScenarioPath());
            tree.finishExecution();
            executed();
        }
    }

    private class StopOnErrorActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            tree.setStopOnError(stopOnError.isSelected());
        }
    }

}
