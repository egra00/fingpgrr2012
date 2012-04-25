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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents;

import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.LibraryInitialisationException;

import java.util.List;
import java.util.HashMap;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/*
* Changes:
* --------
* - 12-Jan.-2006: Notify change not necessary due to the new Repository Observer (GMO)
* - 05-Mar.-2007: catch LibraryInitialisationException on start (GMO)
* - 06-Mar.-2007: Change scrollPane default height (GMO)
*/

/**
* Panel to choose an algorithm to start. Algorithms that can be chosen can be filtered by their class.
*
* <p>Creation date: 15 nov. 2005
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class StartAlgoPanel extends JPanel {

    static private RepositoryManager manager = RepositoryManager.getInstance();

    JComboBox algos = null;
    //JTable params = null;
    ParamTable params = null;
    Class filter = null;

    public StartAlgoPanel() {
        initComponents();
    }

    public StartAlgoPanel(Class filter) {
        this.filter = filter;
        initComponents();

    }

    private void initComponents() {
        setLayout(new BorderLayout(8, 5));


        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.PAGE_AXIS));

        JLabel l1 = new JLabel("Available algorithms :");
        l1.setLabelFor(algos);
        northPanel.add(l1);

        //algos = new JComboBox(manager.getAllAlgos());

        algos = new JComboBox();
        for (Class a : manager.getAllTotemAlgos(filter)) {
                algos.addItem(a.getSimpleName());
        }

        algos.addActionListener(new AlgoComboListener());
        northPanel.add(algos);

        JLabel l2 = new JLabel("Parameters :");
        l2.setLabelFor(params);
        northPanel.add(l2);

        add(northPanel, BorderLayout.NORTH);

        params = new ParamTable(6);
        add(new JScrollPane(params){
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, params.getRowHeight() * 8);
            }
        }, BorderLayout.CENTER);

        JButton accept = new JButton("Start Algorithm");
        accept.addActionListener(new AcceptListener());
        add(accept, BorderLayout.SOUTH);
        
        algos.setSelectedIndex(0);
    }

    /**
     * Listener that start the chosen algorithm with the parameters from the table content
     */
    private class AcceptListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            // TODO?? validate parameters or do it in Jtable cell editors

            // not tested
            // stop cell editing
            if (params.getCellEditor() != null && !params.getCellEditor().stopCellEditing())
                return;

            //Construct hashmap for parameters
            HashMap<String, String> startAlgoParams = params.toHashMap();

            //start the algo
            try {
                manager.startAlgo(algos.getSelectedItem().toString(), startAlgoParams);
                //close the dialog window on success
                ((JDialog)getRootPane().getParent()).dispose();
            } catch (LibraryInitialisationException e1) {
                MainWindow.getInstance().errorMessage("Impossible to load the library for the specified algorithm.\n Please check that the library exists in the lib directory and that it is compiled for this architecture.");
            } catch (AlgorithmInitialisationException e1) {
                //e1.printStackTrace();
                MainWindow.getInstance().errorMessage("Impossible to start the specified algorithm (" + e1.getMessage() + ").");
            }
        }
    }

    /**
     * Action is triggered when the selected algo changes, it fills the table with the default value
     *  of the parameters of the newly selected algo. 
     */
    private class AlgoComboListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            List<ParameterDescriptor> algoParams = null;

            try {
                algoParams = manager.getAlgoParameters(algos.getSelectedItem().toString());
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            }

            params.fill(algoParams);
        }
    }
}
