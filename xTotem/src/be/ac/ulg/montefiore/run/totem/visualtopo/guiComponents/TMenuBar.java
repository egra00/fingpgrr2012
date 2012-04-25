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

import javax.swing.*;
import java.awt.event.ActionListener;

/*
 * Changes:
 * --------
 * - 06-Mar.-2007 : add constructor calling super() (GMO)
 * - 26-Avr.-2007 : setting all methods to be public (GNI)
 * 
 */

/**
 * This class extends JMenuBar and is used to make the creation of simple
 * JMenuBars more easy.
 * <p/>
 * <p>Creation date: 15-Feb-2005
 *
 * @author Olivier Materne (O.Materne@student.ulg.ac.be)
 */
public class TMenuBar extends JMenuBar {
    private JMenu currentMenu;

    public TMenuBar() {
        super();
    }

    /**
     * This method create a new Menu and sets it as currently edited menu so
     * that every new menuItem that will be added to the menubar using the method
     * addMenuItem will be added to this menu.
     *
     * @param s        a String : the menu name
     * @param keyEvent the accelerator key
     */
    public void addMenu(String s, int keyEvent) {
        JMenu menu = new JMenu(s);
        menu.setMnemonic(keyEvent);
        currentMenu = menu;
        this.add(menu);
    }


    /**
     * Add a menuItem at the end of the currently active menu.
     *
     * @param menuItem the menu item to add
     * @param ks       the accelerator key
     * @param l        an associated action listener
     */
    public void addMenuItem(JMenuItem menuItem, KeyStroke ks, ActionListener l) {
        currentMenu.add(menuItem);
        menuItem.setAccelerator(ks);
        menuItem.addActionListener(l);
    }


    /**
     * This method returns the menu that is currently edited.
     *
     * @return returns the currently edited menu
     */
    public JMenu getCurrentMenu() {
        return currentMenu;
    }

}
