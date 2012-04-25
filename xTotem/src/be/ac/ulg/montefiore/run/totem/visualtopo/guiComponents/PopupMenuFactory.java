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

import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.exception.StatusTypeException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkCapacityExceededException;
import be.ac.ulg.montefiore.run.totem.domain.exception.DiffServConfigurationException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.routingGUIModule.RoutingGUIModule;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/*
* Changes:
* --------
*
*/

/**
* Factory to handle creation of popup menus. Popup menus can bve created for node, link, single lsp, multiple lsps or
* default. 
*
* <p>Creation date: 15/01/2008
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class PopupMenuFactory {

    /**
     * Creates a popupmenu for the case the mouse was clicked
     * on a Link.
     *
     * @param link the link on which the mouse was clicked
     */
    public static JPopupMenu createLinkPopupMenu(Link link) {
        JPopupMenu popup = new JPopupMenu();

        //a button to change link status
        JMenuItem menuItem;
        if (link.getLinkStatus() == Link.STATUS_DOWN) {
            menuItem = new JMenuItem("Set Link Up");
        } else {
            menuItem = new JMenuItem("Set Link Down");
        }
        menuItem.addActionListener(new ChangeLinkStatus(link));
        popup.add(menuItem);
        menuItem = new JMenuItem("Change Link Bandwidth");
        menuItem.addActionListener(new ChangeBWListener(link));
        popup.add(menuItem);

        return popup;
    }

    /**
     * Creates a popupmenu for the case the mouse was clicked
     * on a link.
     *
     * @param node the node on which the mouse was clicked
     */
    public static JPopupMenu createNodePopupMenu(Node node) {
        JPopupMenu popup = new JPopupMenu();

        //a button to change node status
        JMenuItem menuItem;
        if (node.getNodeStatus() == Node.STATUS_DOWN) {
            menuItem = new JMenuItem("Set Node Up");
        } else {
            menuItem = new JMenuItem("Set Node Down");
        }
        menuItem.addActionListener(new ChangeNodeStatus(node));
        popup.add(menuItem);

        try {
            RepositoryManager.getInstance().getAlgo("CBGP");
            menuItem = new JMenuItem("View BGP Info");
            menuItem.addActionListener(new ViewBGPInfoListener(node));
            popup.add(menuItem);

            menuItem = new JMenuItem("View Routing Table");
            menuItem.addActionListener(new ViewRTListener(node));
            popup.add(menuItem);
        } catch (NoSuchAlgorithmException e) {
        }

        return popup;
    }

    /**
     * Creates a popup menu for a single lsp
     * @param lsp
     * @return
     */
    public static JPopupMenu createLspPopupMenu(Lsp lsp) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem menuItem = new JMenuItem("Remove this lsp");
        menuItem.addActionListener(new RemoveLspsAction(lsp));
        popup.add(menuItem);

        return popup;
    }

    /**
     * Creates a popup menu for multiple lsps
     * @param lsps
     * @return
     */
    public static JPopupMenu createLspPopupMenu(Lsp[] lsps) {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem menuItem = new JMenuItem("Remove selected lsps");
        menuItem.addActionListener(new RemoveLspsAction(lsps));
        popup.add(menuItem);

        return popup;
    }


     /**
     * This method create a default popupmenu
     */
    public static JPopupMenu createDefaultPopupMenu() {
        JPopupMenu popup = new JPopupMenu();

        JMenuItem menuItem;

        menuItem = new JMenuItem("Add Lsp");
        menuItem.addActionListener(new AddLspListener());
        popup.add(menuItem);

        menuItem = new JMenuItem("Save As Image...");
        menuItem.addActionListener(new SaveImageListener());
        popup.add(menuItem);

         return popup;
    }


    /***************************
    *   Actions for links
    ***************************/

    private static class ChangeLinkStatus implements ActionListener {
        private Link link;

        public ChangeLinkStatus(Link link) {
            this.link = link;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (link.getLinkStatus() == Link.STATUS_UP)
                    link.setLinkStatus(Link.STATUS_DOWN);
                else
                    link.setLinkStatus(Link.STATUS_UP);
            } catch (StatusTypeException ex) {
            }
        }
    }

    /**
     * The ActionListener class responsible for handling Bandwidth modification demands and displaying a pannel where to
     * select the new Bandwidth to be put on the link
     */
    private static class ChangeBWListener implements ActionListener {
        private Link link;

        public ChangeBWListener(Link link) {
            this.link = link;
        }

        public void actionPerformed(ActionEvent e) {
            Object result = JOptionPane.showInputDialog(MainWindow.getInstance(), "Select new bandwidth:", "Change Link BandWidth", JOptionPane.PLAIN_MESSAGE, null, null, String.valueOf(link.getBandwidth()));

            // cancelled
            if (result == null) return;

            try {
                link.setBandwidth(Float.valueOf(result.toString()));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Not a number. Operation cancelled.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (LinkCapacityExceededException e1) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Cannot set the bandwidth of the link " + link.getId() + " to the given value.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (DiffServConfigurationException e1) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Cannot set the bandwidth of the link " + link.getId() + " to the given value.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /***************************
    *   Actions for nodes
    ***************************/


    private static class ChangeNodeStatus implements ActionListener {
        private Node node;

        public ChangeNodeStatus(Node node) {
            this.node = node;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                if (node.getNodeStatus() == Node.STATUS_UP)
                    node.setNodeStatus(Node.STATUS_DOWN);
                else
                    node.setNodeStatus(Node.STATUS_UP);
            } catch (StatusTypeException ex) {
            }
        }
    }


    /**
     * This Lisntener handle the view routing table demands
     */
    private static class ViewRTListener implements ActionListener {
        private Node node;

        public ViewRTListener(Node node) {
            this.node = node;
        }

        public void actionPerformed(ActionEvent e) {
            RoutingGUIModule.getInstance().displayRT(node);
        }
    }

    /**
     * This Lisntener handle the view bgp info demands
     */
    private static class ViewBGPInfoListener implements ActionListener {
        private Node node;

        public ViewBGPInfoListener(Node node) {
            this.node = node;
        }

        public void actionPerformed(ActionEvent e) {
            RoutingGUIModule.getInstance().displayBGPInfo(node);
        }
    }


    /***************************
    *   Actions for lsps
    ***************************/

    private static class RemoveLspsAction implements ActionListener {
        private Lsp[] lsps;

        public RemoveLspsAction(Lsp lsp) {
            this.lsps = new Lsp[1];
            this.lsps[0] = lsp;
        }

        public RemoveLspsAction(Lsp[] lsps) {
            this.lsps = lsps;
        }

        public void actionPerformed(ActionEvent e) {
            for (Lsp lsp : lsps) {
                Domain domain = lsp.getDomain();
                try {
                    domain.removeLsp(lsp);
                } catch (LspNotFoundException e1) {
                    // do noting. It happens when removing multiple lsps: removing a primary can cause to
                    // automatically remove backups, so that some of the ids may not correspond to an established
                    // lsp anymore.
                }
            }
        }
    }


    /***************************
    *   Other Actions
    ***************************/

    /**
     * This Listener is responsible to save the displayed image in a file.
     */
    private static class SaveImageListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            MainWindow.getInstance().saveVisualization();
        }
    }


    /**
     * This Listener handle the Add LSP demands
     */
    private static class AddLspListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            RoutingGUIModule.getInstance().displayPanel();
        }
    }

}
