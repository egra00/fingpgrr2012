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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules;

import javax.swing.*;

/*
 * Changes:
 * --------
 * 
 *
 */

/**
 * Implement this interface will allow you to add your own modules to the gui.
 * You will then be able to add new menus to the gui, insert you're own panels,
 * JDialog, etc.
 * <p/>
 * <p>A more simple way to do this is by extending the abstract class:
 * AbstractModuleLoader.
 * <p/>
 * <p>Once your module created, you can add it to the managed module list by
 * using the addModule method of ModuleLoader.
 * <p/>
 * <p>
 * The full class name of the module can be added to the preferences.xml file under the section "AVAILABLE-GUI-MODULES".
 * The ModuleLoader will load them automatically. This can be possible only if the GUI module have a public constructor
 * with no parameter. 
 * <p/>
 * <p>Creation date: 21-Mar-2005
 *
 * @author Olivier Materne (O.Materne@student.ulg.ac.be)
 */
public interface GUIModule {


    /**
     * The method should return the menu you want to add to the gui
     *
     * @return
     */
    public JMenu getMenu();


    /**
     * Once loaded, can your GUIModule be unloaded ??
     *
     * @return true if the module can be unloaded, else false
     */
    public boolean isUnloadable();


    /**
     * Should the module be loaded at startup ? (For the Module to be properly loaded at startup, it should be added
     * to the database before the GUI is launched)
     *
     * @return true if the GUIModule must be loaded at startup, else false
     */
    public boolean loadAtStartup();


    /**
     * Any action that must be done at the module initialization
     */
    public void initialize();


    /**
     * Any action that must be done when the Module is unloaded
     */
    public void terminate();


    /**
     * Returns the GUIModule's name
     *
     * @return the GUIModule's name
     */
    public String getName();
}
