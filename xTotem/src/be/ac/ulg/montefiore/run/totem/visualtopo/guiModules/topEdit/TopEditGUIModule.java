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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit;
 
import javax.swing.*;

import java.awt.event.*;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Domain;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.AbstractGUIModule;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.DomainFactory;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.guiComponents.TopEditGUI;

/*
 * Changes:
 * --------
 * - 28-Sep-2007: Move inner TopEditDialog class to TopEditDialog.java (GMO)
 * - 01-Oct-2007: Move inner GenerationDialog class to GenerationDialog.java (GMO)
 *
 */

/**
 *
 *  Module for topology edition.
 *  <p/>
 * <p>Creation date: Mar 20, 2007
 *
 * @author Georges Nimubona (nimubonageorges@hotmail.com)
 */
public class TopEditGUIModule extends AbstractGUIModule {
	
	private static Logger logger = Logger.getLogger(TopEditGUIModule.class);

	public TopEditGUIModule() {
	}
	public JMenu getMenu() {
		JMenu menu = new JMenu("TopEdit");
        JMenuItem newMenuItem = new JMenuItem("New...");
        JMenuItem editMenuItem = new JMenuItem("Edit...");
        JMenuItem generateMenuItem = new JMenuItem("TopGen");
        menu.add(newMenuItem);
        menu.add(editMenuItem);
        menu.addSeparator();
        menu.add(generateMenuItem);
        newMenuItem.setActionCommand("New Edition");
        TopEditListener listener = new TopEditListener();
        newMenuItem.addActionListener(listener);
        editMenuItem.setActionCommand("Edition");
        editMenuItem.addActionListener(listener);
        generateMenuItem.setActionCommand("Generation");
        generateMenuItem.addActionListener(listener);

        return menu;
	}

	public String getName() {
		return "TopEdit";
	}
	
	public boolean loadAtStartup() {
        return true;
    }
	
	private class TopEditListener implements ActionListener {
		
		GUIManager manager = GUIManager.getInstance();
        MainWindow mainWindow = MainWindow.getInstance();
		
        public void actionPerformed(ActionEvent e) {
			if(e.getActionCommand().equals("New Edition")) {
				TopEditGUI.getInstance().newDomain();
                TopEditGUI.getInstance().setVisible(true);
			}
			else if(e.getActionCommand().equals("Edition")) {
				if (manager.getCurrentDomain() == null) {
	                mainWindow.errorMessage("A domain must be loaded to perform this action.");
				} else {
                    TopEditGUI.getInstance().newEdition((new DomainFactory()).clone((Domain)manager.getCurrentDomain()));
                    TopEditGUI.getInstance().setVisible(true);
				}
			}
			else if(e.getActionCommand().equals("Generation")) {
				TopEditGUI.getInstance().generate();
                TopEditGUI.getInstance().setVisible(true);
			}
		}
	}

}
