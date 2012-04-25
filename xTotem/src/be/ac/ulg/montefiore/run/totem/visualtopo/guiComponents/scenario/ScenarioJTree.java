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
import be.ac.ulg.montefiore.run.totem.scenario.model.Event;
import be.ac.ulg.montefiore.run.totem.scenario.model.EventResult;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.EventType;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.List;
import java.util.Iterator;
import java.awt.*;
import java.io.OutputStream;
import java.io.IOException;

/*
* Changes:
* --------
* 24-Apr-2006: add colors from execution result. The scenario messages will be printed on a given outputStream. (GMO)
* 19-May-2006: stopOnError bugfix (GMO)
* 09-Aug-2007: ensure that current executing event is visible (GMO)
* 09-Aug-2007: add methods to expand and collapse the whole tree (GMO)
*/

/**
* Represent a scenario by a JTree object.
* Get the events name and parameters through reflection from jaxb.
* It permit to choose to display either all possible variables or only the variables that were set in the XML file.
* The variables that were not set are displayed in grey.      
*
* <p>Creation date: 5 janv. 2006
*
* @see EventTypeNode
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ScenarioJTree {

    private int currentIndex = 0;
    private JTree tree = null;
    private boolean stopOnError = true;
    private OutputStream output;

    /**
     * Build the internal JTree representation, displaying only the variables that are set.
     * @param scenario the scenario to represent in the tree.
     * @param output OutputStream used to display scenario execution result.
     */
    public ScenarioJTree(Scenario scenario, OutputStream output) {
        this(scenario, output, false);
    }

    /**
     * Build the internal JTree representation
     * @param scenario the scenario to represent in the tree.
     * @param output OutputStream used to display scenario execution result.
     * @param displayNotSet set to display the variable that were not set in the scenario XML file.
     */
    public ScenarioJTree(Scenario scenario, OutputStream output, boolean displayNotSet) {
        this.output = output;
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Scenario");

        Iterator iterator = scenario.getEvent().iterator();
        for (; iterator.hasNext();) {
            EventType event = (EventType) iterator.next();

            EventTypeNode n = new RootEventTypeNode(event, displayNotSet);

            addNodes(n, root);
        }

        tree = new JTree(root);
        tree.setCellRenderer(new ScenarioTreeCellRenderer());
    }

    /**
     * returns a reference to the tree
     * @return
     */
    public JTree getJTree() {
        return tree;
    }

    private void addNodes(EventTypeNode o, DefaultMutableTreeNode parent) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(o);
        parent.add(newNode);
        List<EventTypeNode> lst = o.getChildren();
        for (EventTypeNode node : lst) {
            addNodes(node, newNode);
        }
    }


    public boolean isStopOnError() {
        return stopOnError;
    }

    public void setStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

    /* Execution methods */

    /**
     * return true if no exception occurs, false otherwise.
     * @return
     */
    private boolean oneStep() {
        boolean error = false;
        if (currentIndex < tree.getModel().getChildCount(tree.getModel().getRoot())) {
            RootEventTypeNode node = (RootEventTypeNode)((DefaultMutableTreeNode)tree.getModel().getChild(tree.getModel().getRoot(), currentIndex)).getUserObject();
            Event e = (Event)(node.getValue());
            try {
                EventResult er = e.action();
                if (output != null && er.getMessage() != null)
                    try {
                        output.write(er.getMessage().getBytes());
                        output.write('\n');
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                node.setStatus(RootEventTypeNode.EXECUTED_NO_ERROR);
            } catch (EventExecutionException e1) {
                error = true;
                node.setStatus(RootEventTypeNode.EXECUTED_ERROR);
                String errorMsg;
                if (e1.getCause() != null) {
                    e1.getCause().printStackTrace();
                    errorMsg = e1.getCause().getClass().getSimpleName();
                    errorMsg += " : ";
                    errorMsg += e1.getCause().getMessage();
                }
                else {
                    e1.printStackTrace();
                    errorMsg = "Error : ";
                    errorMsg += e1.getMessage();
                }
                if (stopOnError) JOptionPane.showMessageDialog(tree, errorMsg, "Execution error", JOptionPane.ERROR_MESSAGE);
                if (output != null) {
                    try {
                        output.write(errorMsg.getBytes());
                        output.write('\n');
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
            currentIndex++;
        }
        return !error;
    }

    public void step() {
        oneStep();
        ensureCurrentEventVisible();
    }

    private void ensureCurrentEventVisible() {
        TreeModel model = tree.getModel();
        int index = currentIndex;
        if (isExecutionFinished()) index--;
        DefaultMutableTreeNode node = (DefaultMutableTreeNode)model.getChild(model.getRoot(), index);
        TreePath tp = new TreePath(node.getPath());
        tree.scrollPathToVisible(tp);
        tree.repaint();
    }

    public int executeToSelection() {
        int count = 0;
        if (tree.getSelectionCount() <= 0) return 0;
        int index = tree.getModel().getIndexOfChild(tree.getModel().getRoot(), tree.getSelectionPath().getPathComponent(1));
        while (currentIndex <= index) {
            boolean ret = oneStep();
            count++;
            if (stopOnError && !ret) break;
        }
        ensureCurrentEventVisible();
        return count;
    }

    public int finishExecution() {
        int count = 0;
        while (currentIndex < tree.getModel().getChildCount(tree.getModel().getRoot())) {
            boolean ret = oneStep();
            count++;
            if (stopOnError && !ret) break;
        }
        ensureCurrentEventVisible();
        return count;
    }

    public boolean isExecutionFinished() {
        return currentIndex >= tree.getModel().getChildCount(tree.getModel().getRoot());
    }

    public void expandAll() {
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        ensureCurrentEventVisible();
    }

    public void collapseAll() {
        for (int i = 1; i < tree.getRowCount(); i++) {
            tree.collapseRow(i);
        }
        ensureCurrentEventVisible();
    }

    /* Renderer */

    private class ScenarioTreeCellRenderer extends DefaultTreeCellRenderer {

        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            Component c = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Object o = ((DefaultMutableTreeNode) value).getUserObject();
            if (o instanceof EventTypeNode) {
                if (o instanceof RootEventTypeNode) {
                    RootEventTypeNode r = (RootEventTypeNode) o;
                    switch (r.getStatus()) {
                        case RootEventTypeNode.NOT_EXECUTED:
                            break;
                        case RootEventTypeNode.EXECUTED_NO_ERROR:
                            c.setForeground(Color.green);
                            break;
                        case RootEventTypeNode.EXECUTED_ERROR:
                            c.setForeground(Color.red);
                    }
                }
                EventTypeNode e = (EventTypeNode)o;
                setText(e.displayString());
                if (!e.isset()) c.setForeground(Color.GRAY);
            }
            return c;
        }
    }

}

