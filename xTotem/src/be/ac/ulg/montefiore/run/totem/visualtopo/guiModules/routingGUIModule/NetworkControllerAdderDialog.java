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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.routingGUIModule;

import be.ac.ulg.montefiore.run.totem.netController.facade.NetworkControllerManager;
import be.ac.ulg.montefiore.run.totem.netController.exception.NetworkControllerAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.netController.exception.NetworkControllerInitialisationException;
import be.ac.ulg.montefiore.run.totem.netController.exception.InvalidNetworkControllerException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ParamTable;

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
* <Replace this by a description of the class>
*
* <p>Creation date: 09-mars-2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class NetworkControllerAdderDialog extends JDialog {

    private static int nb = 1;

    private JComboBox controllersCombo;
    private ParamTable params;

    public NetworkControllerAdderDialog() {
        super(MainWindow.getInstance(), "Add a network controller");
        setupUI();
    }

    private void setupUI() {

        JPanel generalPanel = new JPanel(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JButton okBtn = new JButton("Accept");
        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        okBtn.addActionListener(new AcceptActionListener());
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);

        JPanel nPanel = new JPanel();
        nPanel.setLayout(new BoxLayout(nPanel, BoxLayout.PAGE_AXIS));

        nPanel.add(new JLabel("Available Network Controllers"));
        controllersCombo = new JComboBox();
        for (Class cl : NetworkControllerManager.getInstance().getAvailableNetworkControllers()) {
            controllersCombo.addItem(cl.getSimpleName());
        }
        controllersCombo.addActionListener(new ControllersComboListener());
        nPanel.add(controllersCombo);

        nPanel.add(new JLabel("Parameters:"));

        generalPanel.add(nPanel, BorderLayout.NORTH);

        params = new ParamTable(3);
        generalPanel.add(new JScrollPane(params){
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, params.getRowHeight() * 5);
            }
        }, BorderLayout.CENTER);

        generalPanel.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(generalPanel);

        if (controllersCombo.getItemCount() > 0) {
            controllersCombo.setSelectedIndex(0);
        }
    }

    private class ControllersComboListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (controllersCombo.getSelectedItem() != null) {
                try {
                    params.fill(NetworkControllerManager.getInstance().getStartParameters((String)controllersCombo.getSelectedItem()));
                } catch (InvalidNetworkControllerException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    private class AcceptActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String name = "Controller " + nb++;
            try {
                NetworkControllerManager.getInstance().addNetworkController(name, (String)controllersCombo.getSelectedItem(), params.toHashMap());
                dispose();
            } catch (NetworkControllerAlreadyExistException e1) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), e1.getClass().getSimpleName() + " : " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            } catch (NetworkControllerInitialisationException e1) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), e1.getClass().getSimpleName() + " : " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
            }
        }
    }
}