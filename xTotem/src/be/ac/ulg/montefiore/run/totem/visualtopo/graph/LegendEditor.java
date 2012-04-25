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
package be.ac.ulg.montefiore.run.totem.visualtopo.graph;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

/*
* Changes:
* --------
*
*/

/**
* This class is used to change the colors of the legend associated with the class {@link GraphAspectFunctions}.
* It also update the LegendPanel to reflect the new colors.
*
* <p>Creation date: 16 mars 2006
*
* @author GaÃ¯Â¿Ål Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LegendEditor {
    private AButton aButton = null;
    private JColorChooser colorChooser = null;
    private ColorLegend legend = null;

    /**
     * Display a panel that allows to change the colors associated with the different ranges.
     */
    public void chooseColors() {
        legend = GraphAspectFunctions.getColorLegend();
        JPanel panel = new JPanel();

        JPanel panel2 = new JPanel();

        //create buttons
        panel2.setLayout(new GridLayout(0, 1, 0, 3));
        for (int i = 0; i < legend.getNbColors(); i++) {
            AButton button = new AButton(legend.getName(i), i);
            button.setIcon(new ColorSquare(legend.getColor(i)));
            panel2.add(button);
            button.addActionListener(new SelectIntervalListener());
        }

        //create color chooser

        panel.setLayout(new BorderLayout());
        panel.add(panel2, BorderLayout.WEST);
        colorChooser = new JColorChooser();
        colorChooser.getSelectionModel().addChangeListener(new ColorChangeListener());
        panel.add(colorChooser, BorderLayout.CENTER);

        JDialog dialog = MainWindow.getInstance().showDialog(panel, "Choose links colors by link load");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new CloseColorChooserListener());

    }

    /**
     * The ActionListener class responsible for selecting a range in rder to change its color.
     */
    class SelectIntervalListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            aButton = (AButton) e.getSource();
        }
    }

    /**
     * Listener that handle color changes
     */
    class ColorChangeListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            Color color = colorChooser.getColor();
            if (aButton != null) {

                legend.setColor(aButton.getIndex(), color);
                aButton.setIcon(new ColorSquare(color));
                aButton.updateUI();
            }
        }
    }

    /**
     * A JButton with an index
     */
    class AButton extends JButton {
        private int index;

        AButton(String name, int i) {
            super(name);
            index = i;
        }

        int getIndex() {
            return index;
        }
    }

    /**
     * Handle changes to the window that allow to choose colors. The only implemented method is windownClosing.
     * The goal is to take into account the changes if the user close the window.
     * When the window is closed, the legend will be installed in the LegendPanel and the graph will be repainted with
     * the new colors.
     */
    class CloseColorChooserListener implements WindowListener {
        public void windowOpened(WindowEvent e) {

        }

        public void windowClosing(WindowEvent e) {
            ((JDialog) e.getSource()).dispose();
            aButton = null;
            MainWindow.getInstance().getOptionsPanel().getLegendPanel().setLegend(legend);
            GraphManager.getInstance().repaint();
        }

        public void windowClosed(WindowEvent e) {

        }

        public void windowIconified(WindowEvent e) {

        }

        public void windowDeiconified(WindowEvent e) {

        }

        public void windowActivated(WindowEvent e) {

        }

        public void windowDeactivated(WindowEvent e) {

        }
    }
}
