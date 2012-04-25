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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.AbstractGUIModule;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.impl.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/*
* Changes:
* --------
* - 31-May-2007: Add Change Capacity. (GMO)
* - 28-Feb-2008: Remove change TM (GMO)
*/

/**
 * Module to perform what-if scenarios.
 * <p/>
 * <p>Creation date: 25/04/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class WhatIfModule extends AbstractGUIModule {
    /**
     * The method should return the menu you want to add to the gui
     *
     * @return
     */
    public JMenu getMenu() {
        final JMenu menu = new JMenu("What-If");
        menu.setMnemonic(KeyEvent.VK_W);
        JMenuItem menuItem;

        menuItem = new JMenuItem("Node down");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Domain domain = GUIManager.getInstance().getCurrentDomain();
                if (domain != null) {
                    JDialog dialog = new SingleActionDialog(new NodeDownWIActionPanel(domain));
                    MainWindow.getInstance().showDialog(dialog);
                } else {
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), "A domain must be loaded to perform this action.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Node up");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Domain domain = GUIManager.getInstance().getCurrentDomain();
                if (domain != null) {
                    JDialog dialog = new SingleActionDialog(new NodeUpWIActionPanel(GUIManager.getInstance().getCurrentDomain()));
                    MainWindow.getInstance().showDialog(dialog);
                } else {
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), "A domain must be loaded to perform this action.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Link down");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Domain domain = GUIManager.getInstance().getCurrentDomain();
                if (domain != null) {
                    JDialog dialog = new SingleActionDialog(new LinkDownWIActionPanel(GUIManager.getInstance().getCurrentDomain()));
                    MainWindow.getInstance().showDialog(dialog);
                } else {
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), "A domain must be loaded to perform this action.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(menuItem);

        menuItem = new JMenuItem("Link up");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Domain domain = GUIManager.getInstance().getCurrentDomain();
                if (domain != null) {
                    JDialog dialog = new SingleActionDialog(new LinkUpWIActionPanel(GUIManager.getInstance().getCurrentDomain()));
                    MainWindow.getInstance().showDialog(dialog);
                } else {
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), "A domain must be loaded to perform this action.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(menuItem);

        /*
        menuItem = new JMenuItem("Change TM");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Domain domain = GUIManager.getInstance().getCurrentDomain();
                if (domain != null) {
                    JDialog dialog = new SingleActionDialog(new ChangeTrafficWIActionPanel(GUIManager.getInstance().getCurrentDomain()));
                    MainWindow.getInstance().showDialog(dialog);
                } else {
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), "A domain must be loaded to perform this action.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(menuItem);*/

        menuItem = new JMenuItem("Change Capacity");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Domain domain = GUIManager.getInstance().getCurrentDomain();
                if (domain != null) {
                    JDialog dialog = new SingleActionDialog(new ChangeLinkCapacityWIActionPanel(GUIManager.getInstance().getCurrentDomain()));
                    MainWindow.getInstance().showDialog(dialog);
                } else {
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), "A domain must be loaded to perform this action.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Compose Events");
        menuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Domain domain = GUIManager.getInstance().getCurrentDomain();
                if (domain != null) {
                    JDialog dialog = new ComposeEventsDialog();
                    MainWindow.getInstance().showDialog(dialog);
                } else {
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), "A domain must be loaded to perform this action.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(menuItem);

        return menu;
    }

    /**
     * Returns the GUIModule's name
     *
     * @return the GUIModule's name
     */
    public String getName() {
        return "What If scenarios";
    }

    /**
     * Should the Module be loaded at GUI startup ? yes
     *
     * @return true
     */
    public boolean loadAtStartup() {
        return true;
    }
}
