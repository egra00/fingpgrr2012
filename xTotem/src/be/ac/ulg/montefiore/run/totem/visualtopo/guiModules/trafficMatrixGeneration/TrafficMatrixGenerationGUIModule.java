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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.trafficMatrixGeneration;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.AbstractGUIModule;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.TopoChooser;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ProgressBarPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.SwingWorker;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.DomainImpl;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManagerListener;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.bgp.BgpFieldsCreation;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.generation.POPPOPTrafficMatrixGeneration;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.generation.InterDomainTrafficMatrixGeneration;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.persistence.TrafficMatrixFactory;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.HashMap;

/*
* Changes:
* --------
* - Refactor the whole class, use TrafficMatrixGenerationData class, add possiblity to add BGP sessions (GMO)
* - 18-Oct-2007 : remove all bgp informations before adding iBGP sessions (GMO)
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 26/06/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class TrafficMatrixGenerationGUIModule extends AbstractGUIModule implements InterDomainManagerListener {

    private HashMap<Integer, TrafficMatrixGenerationData> bgpDirInfos;

    public TrafficMatrixGenerationGUIModule() {
        bgpDirInfos = new HashMap<Integer, TrafficMatrixGenerationData>(4);
    }

    public JMenu getMenu() {
        JMenu menu = new JMenu(getName());

        JMenuItem menuItem;

        menuItem = new JMenuItem("Select BGP directories...");
        menuItem.addActionListener(new SelectBGPDirActionListener());
        menu.add(menuItem);

        menuItem = new JMenuItem("Read cluster file...");
        menuItem.addActionListener(new ReadClusterActionListener());
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Add iBGP fullmesh to the domain");
        menuItem.addActionListener(new AddiBGPFullmeshActionListener());
        menu.add(menuItem);

        menuItem = new JMenuItem("Add eBGP information (from BGP dumps)");
        menuItem.addActionListener(new AddeBGPSessionsActionListener());
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Generate Inter Domain TM (NetFlow)...");
        menuItem.addActionListener(new GenerateInterTMActionListener());
        menu.add(menuItem);

        menuItem = new JMenuItem("Load Inter Domain TM");
        menuItem.addActionListener(new LoadInterTMActionListener());
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("Generate Intra TM");
        menuItem.addActionListener(new GenerateIntraTMActionListener());
        menu.add(menuItem);
        return menu;
    }

    public boolean isUnloadable() {
        return false;
    }

    public boolean loadAtStartup() {
        return true;
    }

    public String getName() {
        return "TMGeneration";
    }

    public void initialize() {
        InterDomainManager.getInstance().addListener(this);
    }

    public void terminate() {
        InterDomainManager.getInstance().removeListener(this);
    }

    public void addDomainEvent(Domain domain) {
    }

    public void removeDomainEvent(Domain domain) {
        bgpDirInfos.remove(domain.getASID());
    }

    public void changeDefaultDomainEvent(Domain domain) {
    }

    /**
     * Action Listeners
     */
    private class SelectBGPDirActionListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            Domain domain = GUIManager.getInstance().getCurrentDomain();
            if (domain == null) {
                MainWindow.getInstance().errorMessage("A domain must be loaded.");
                return;
            }

            TrafficMatrixGenerationData info;
            if ((info = bgpDirInfos.get(domain.getASID())) == null) {
                info = new TrafficMatrixGenerationData();
                bgpDirInfos.put(domain.getASID(), info);
            }

            JDialog d = new SelectBGPDirectoriesDialog(domain, info);
            MainWindow.getInstance().showDialog(d);
        }
    }

    private class AddiBGPFullmeshActionListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            Domain domain = GUIManager.getInstance().getCurrentDomain();
            if (domain == null) {
                MainWindow.getInstance().errorMessage("A domain must be loaded.");
                return;
            }
            DomainImpl domainImpl = (DomainImpl)domain;

            int n = JOptionPane.YES_OPTION;
            if (domainImpl.getBgp() != null) {
                n = JOptionPane.showConfirmDialog(MainWindow.getInstance(), "<html>BGP information already exists for that domain.<br>" +
                        " This action will remove all prior existing information. Would you like to continue ?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            }
            if (n == JOptionPane.YES_OPTION) {
                ((DomainImpl)domain).removeBgpRouters();
                (new BgpFieldsCreation()).addiBGPFullMesh((DomainImpl)domain);
            }
        }
    }

    private class AddeBGPSessionsActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            final Domain domain = GUIManager.getInstance().getCurrentDomain();
            if (domain == null) {
                MainWindow.getInstance().errorMessage("A domain must be loaded.");
                return;
            }

            final TrafficMatrixGenerationData info = bgpDirInfos.get(domain.getASID());
            if (info == null || !info.isSetBGPRibDir() || !info.isSetBGPRibFileSuffix()) {
                MainWindow.getInstance().errorMessage("Please select BGP directories first.");
                return;
            }

            ProgressBarPanel progressBar = new ProgressBarPanel(0, 100);
            progressBar.setMessage("It can take several minutes for big domains");
            progressBar.setCancelable(false);
            progressBar.getProgressBar().setIndeterminate(true);
            progressBar.getProgressBar().setSize(500, 60);

            final JDialog pDialog = MainWindow.getInstance().showDialog(progressBar, "Generating intra domain TM ...");
            pDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            SwingWorker sw = new SwingWorker() {
                Exception error = null;
                TrafficMatrix tm = null;

                public Object construct() {

                    try {
                        (new BgpFieldsCreation()).addeBGPSessions((DomainImpl)domain, info.getBGPRibDir(), info.getBGPRibFileSuffix());
                        return null;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        error = e1;
                        return null;
                    }
                }

                public void finished() {
                    pDialog.dispose();
                    if (error != null) {
                        MainWindow.getInstance().errorMessage(error);
                        return;
                    }
                }
            };

            sw.start();
        }
    }

    private class GenerateInterTMActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Domain domain = GUIManager.getInstance().getCurrentDomain();
            if (domain == null) {
                MainWindow.getInstance().errorMessage("A domain must be loaded.");
                return;
            }

            TrafficMatrixGenerationData info = bgpDirInfos.get(domain.getASID());
            if (info == null) {
                info = new TrafficMatrixGenerationData();
                bgpDirInfos.put(domain.getASID(), info);
            }
            JDialog dialog = new GenerateInterTMDialog(new InterDomainTrafficMatrixGeneration(domain), info);
            MainWindow.getInstance().showDialog(dialog);

        }
    }

    private class LoadInterTMActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Domain domain = GUIManager.getInstance().getCurrentDomain();
            if (domain == null) {
                MainWindow.getInstance().errorMessage("A domain must be loaded.");
                return;
            }

            File matrix = new TopoChooser().loadMatrix(MainWindow.getInstance());

            if (matrix != null) {
                try {
                    TrafficMatrixGenerationData info = bgpDirInfos.get(domain.getASID());
                    if (info == null) {
                        info = new TrafficMatrixGenerationData();
                        bgpDirInfos.put(domain.getASID(), info);
                    }
                    info.setInterTm(TrafficMatrixFactory.loadInterDomainMatrix(matrix.getAbsolutePath()));
                } catch (Exception e1) {
                    e1.printStackTrace();
                    MainWindow.getInstance().errorMessage("Unable to load the traffic matrix: " + e1.getMessage());
                    return;
                }
            }
        }
    }

    private class ReadClusterActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Domain domain = GUIManager.getInstance().getCurrentDomain();
            if (domain == null) {
                MainWindow.getInstance().errorMessage("A domain must be loaded.");
                return;
            }

            final TrafficMatrixGenerationData info = bgpDirInfos.get(domain.getASID());
            if (info == null || !info.isSetBGPRibDir() || !info.isSetBGPRibFileSuffix()) {
                MainWindow.getInstance().errorMessage("Please select BGP directories first.");
                return;
            }

            MainWindow.getInstance().showDialog(new ReadClusterDialog(domain, info));
        }
    }

    private class GenerateIntraTMActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            final Domain domain = GUIManager.getInstance().getCurrentDomain();
            if (domain == null) {
                MainWindow.getInstance().errorMessage("A domain must be loaded.");
                return;
            }
            final int ASID = domain.getASID();
            final TrafficMatrixGenerationData info = bgpDirInfos.get(ASID);

            if (info == null || !info.isSetInterTm()) {
                MainWindow.getInstance().errorMessage("Please load a inter domain traffic matrix first.");
                return;
            }
            if (!info.isSetPrefixesMap()) {
                MainWindow.getInstance().errorMessage("Please load BGP cluster file first.");
                return;
            }

            ProgressBarPanel progressBar = new ProgressBarPanel(0, 100);
            progressBar.setMessage("It can take several minutes for big domains");
            progressBar.setCancelable(false);
            progressBar.getProgressBar().setIndeterminate(true);
            progressBar.getProgressBar().setSize(500, 60);

            final JDialog pDialog = MainWindow.getInstance().showDialog(progressBar, "Generating intra domain TM ...");
            pDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            SwingWorker sw = new SwingWorker() {
                Exception error = null;
                TrafficMatrix tm = null;

                public Object construct() {

                    try {
                        POPPOPTrafficMatrixGeneration tmGen = new POPPOPTrafficMatrixGeneration(domain);
                        tm = tmGen.generateTrafficMatrix(null, info.getPrefixesMap(), info.getInterTm());
                        return tm;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        error = e1;
                        return null;
                    }
                }

                public void finished() {
                    pDialog.dispose();
                    if (error != null) {
                        MainWindow.getInstance().errorMessage("Impossible to generate IntraTM: " + error.getMessage());
                        return;
                    }
                    try {
                        int tmId = TrafficMatrixManager.getInstance().generateTMID(ASID);
                        TrafficMatrixManager.getInstance().addTrafficMatrix(tm, tmId);
                        MainWindow.getInstance().infoMessage("Traffic Matrix added with id: " + tmId);
                    } catch (InvalidDomainException e1) {
                        e1.printStackTrace();
                        MainWindow.getInstance().errorMessage(e1);
                    } catch (TrafficMatrixAlreadyExistException e1) {
                        e1.printStackTrace();
                        MainWindow.getInstance().errorMessage(e1);
                    } catch (TrafficMatrixIdException e1) {
                        e1.printStackTrace();
                        MainWindow.getInstance().errorMessage(e1);
                    }
                }
            };

            sw.start();
        }
    }


}
