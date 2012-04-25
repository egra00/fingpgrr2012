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
package be.ac.ulg.montefiore.run.totem.visualtopo.facade;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.GUIModule;
import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* 8-Dec.-2005 : Add the IGPWO module (GMO)
* 3-Feb.-2006 : Add the SAMTE module (GMO)
* 28-Mar.-2006 : Add the CBGP module (GMO)
* 24-Oct-2006 : Modules are now defined in the preferences.xml file (GMO)
* 24-Oct-2006 : Fix javadoc (GMO)
*
*/

/**
 * We use this class to dynamically load/unload GUIModule to the TOTEM GUI. Use it as follow :<br>
 * <ul>
 * <li>Create your module, implementing the GUIModule Interface. Add a public constructor with no arguments</li>
 * <li>Add your module classname to the preferences.xml file under section AVAILABLE-GUI-MODULES. It will be
 *    automatically added to the module database,<br>
 *    <u>or</u> add your Module to the Module database by using the addModule method implemented in this class</li>
 * <li>Load/Unload your module via the GUI</li>
 * </ul>
 * @author : Olivier Materne ( O.Materne@student.ulg.ac.be)
 *
 */
public class ModuleLoader {

    final static private Logger logger = Logger.getLogger(ModuleLoader.class);

    final private int width = 300;
    final private int heigth = 300;
    private JDialog dialog = null;

    final private int NOTLOADED = 0;
    final private int LOADED = 1;

    static private ModuleLoader moduleLoader = null;
    static private List<ModuleCell> allModules = null;


    /**
     * A private Constructor
     */
    private ModuleLoader() {
        allModules = new LinkedList<ModuleCell>();
    }


    /**
     * Load some basic modules. Used by the contructor. Check which core modules it should manage and
     * starts the "Load at startup" modules.
     */
    public void initBasicModules() {
        String modules = PreferenceManager.getInstance().getPrefs().get("AVAILABLE-GUI-MODULES", "be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.routingGUIModule.RoutingGUIModule");
        for (String moduleClassName : modules.split(":")) {
            try {
                Class clazz = Class.forName(moduleClassName);
                addGUIModule((GUIModule)clazz.newInstance());
            } catch (ClassNotFoundException e) {
                logger.error("Module not found: " + moduleClassName);
            } catch (IllegalAccessException e) {
                logger.error("Illegal access exception for module: " + moduleClassName);
            } catch (InstantiationException e) {
                logger.error("Instantiation exception for module: " + moduleClassName);
            } catch (ClassCastException e) {
                logger.error("Module is not a GUIModule: " + moduleClassName);
            }
        }
        for (Iterator iter = allModules.listIterator(); iter.hasNext();) {
            ModuleCell cell = (ModuleCell) iter.next();
            if (cell.getModule().loadAtStartup() && (cell.getStatus() == NOTLOADED)) {
                loadModule(cell);
                cell.setStatus(LOADED);
            }
        }
    }


    /**
     * Get a simple instance of the ModuleLoader
     *
     * @return returns an instance of ModuleLoader
     */
    public static ModuleLoader getInstance() {
        if (moduleLoader == null)
            moduleLoader = new ModuleLoader();
        return moduleLoader;
    }


    /**
     * Load a module in the GUI
     *
     * @param cell A module cell containing the GUIModule and internal information for the ModuleLoader
     */
    private void loadModule(ModuleCell cell) {
        if (cell.menu != null)
            MainWindow.getInstance().addMenu(cell.menu);
        cell.getModule().initialize();

    }


    /**
     * Load the needed modules and unload unneeded ones. This private method should only be called from the gui
     */
    private void loadGUIModules() {
        for (Iterator iter = allModules.listIterator(); iter.hasNext();) {  //iter through the list to check the
            //modules that should be loaded and unloaded.
            ModuleCell cell = (ModuleCell) iter.next();
            if (cell.getStatus() == NOTLOADED && cell.cbox.isSelected()) {
                loadModule(cell);
                cell.setStatus(LOADED);
            }
            if (cell.getStatus() == LOADED && !(cell.cbox.isSelected())) {
                unloadModule(cell);
                cell.setStatus(NOTLOADED);
            }
        }
    }


    /**
     * Unload a module  ie.  remove the menu, terminate properly the module execution, ...
     *
     * @param cell
     */
    private void unloadModule(ModuleCell cell) {
        GUIModule module = cell.getModule();
        if (module.isUnloadable())
            return;
        module.terminate();
        //remove Menu from menubar
        if (cell.menu != null) {
            JMenuBar bar = MainWindow.getInstance().getJMenuBar();
            bar.remove(cell.menu);
            bar.updateUI();
        }
    }


    /**
     * Add module to the "Managed Module Database" so that the Module Loader can take care of it
     *
     * @param module the module to be added
     */
    public void addGUIModule(GUIModule module) {
        allModules.add(new ModuleCell(module, NOTLOADED));
    }


    /**
     * Show a panel in the gui that will display managed Modules and allows the user to load/unload modules
     */
    public void showModuleLoaderDialog() {
        final JPanel generalPanel = new JPanel(new BorderLayout());
        final JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.black));

        final JButton button2 = new JButton();
        button2.setText("Accept");
        buttonPanel.add(button2);
        final JButton button1 = new JButton();
        button1.setText("Cancel");
        buttonPanel.add(button1);

        final JPanel modulePanel = new JPanel(new GridLayout(0, 1, 10, 10));

        for (Iterator iter = allModules.listIterator(); iter.hasNext();) {
            ModuleCell moduleCell = (ModuleCell) iter.next();
            JCheckBox checkBox = new JCheckBox(moduleCell.getModule().getName());
            modulePanel.add(checkBox);
            checkBox.setSelected(moduleCell.getStatus() == LOADED);
            checkBox.setEnabled(!((moduleCell.getModule().isUnloadable()) && (moduleCell.getStatus() == LOADED)));
            moduleCell.cbox = checkBox;
        }

        generalPanel.add(buttonPanel, BorderLayout.SOUTH);
        generalPanel.add(modulePanel, BorderLayout.CENTER);

        button1.addActionListener(new LoaderListener());
        button2.addActionListener(new LoaderListener());

        //add the Panel into a JDialog and show it
        dialog = MainWindow.getInstance().showDialog(generalPanel, "Module Loader");
        dialog.setAlwaysOnTop(true);
    }


    /**
     * A Simple class that contain a reference to a GUIModule and some management information
     */
    class ModuleCell {
        private JMenu menu;
        private int status = NOTLOADED;
        private GUIModule module = null;
        private JCheckBox cbox = null;


        /**
         * create a ModuleCell with given GUIModule and status
         *
         * @param module
         * @param status
         */
        ModuleCell(GUIModule module, int status) {
            this.module = module;
            this.status = status;
            this.menu = module.getMenu();
        }


        /**
         * Return Module Status  (Loaded, not loaded, ...)
         *
         * @return the module status
         */
        int getStatus() {
            return status;
        }


        /**
         * return the GUIModule set in this cell
         *
         * @return the GUIModule
         */
        GUIModule getModule() {
            return module;
        }


        /**
         * sets a GUIModule into this cell
         *
         * @param module
         */
        void setModule(GUIModule module) {
            this.module = module;
        }


        /**
         * change the status of the GUIModule to the given parameter
         *
         * @param status
         */
        void setStatus(int status) {
            if (module == null)
                this.status = NOTLOADED;
            else
                this.status = status;
        }
    }


    /**
     * A Listen for the user accepting changes in the GUIModule Loaded/unloaded state
     */
    class LoaderListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            dialog.dispose();
            if (((JButton) e.getSource()).getText().equals("Accept")) {
                loadGUIModules();
            }
        }
    }
}
