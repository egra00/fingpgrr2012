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

import be.ac.ulg.montefiore.run.totem.domain.exception.DomainAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.persistence.TrafficMatrixFactory;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.TopoChooser;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.GraphManager;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/*
 * Changes:
 * --------
 * 5-Dec.-2005 : change managed matrices behaviour to enable multiple domains. (GMO)
 * 8-Dec.-2005 : lots of changes, mainly to permit multiple domain management (GMO)
 * 13-Dec.-2005 : Add Listeners to update GUI on change in domains, TMs and algos (GMO)
 * 12-Jan.-2006 : Now react to the change in the InterDomainManager (GMO)
 * 20-Mar.-2006 : minor changes to be compatible with LinkLoadComputer (GMO)
 * 03-Apr.-2006 : does not react to interDomain events anymore, remove displayDomain() method (GMO)
 * 08-May-2006 : allow loading of multiGraph (GMO)
 * 11-Jul-2006 : better exception handling in addDemandMatrix (GMO)
 * 16-Aug-2006 : add xml extension in saveTopo() (GMO)
 * 22-Aug-2006 : domain now loads with bandwidth sharing enabled (GMO)
 * 23-Oct-2006 : disable bandwidth sharing by default, add saveTrafficMatrix(.) (GMO)
 * 13-Mar-2007 : remove networkStats() meethod (GMO)
 */

/**
 * This class is used to manage most interractions between TOTEM and the GUI.
 * <p/>
 * <p/>
 * <p>Creation date: 10-Jan-2005
 *
 * @author Olivier Materne (O.Materne@student.ulg.ac.be)
 * @author Gael Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class GUIManager {

    private static Logger logger = Logger.getLogger(GUIManager.class);

    static final int TOPO_CHECKED = 1;
    static final int TOPO_NOT_CHECKED = 2;
    public static final int TOPO_NOT_LOADED = 3;

    private static InterDomainManager idm = null;
    private static TrafficMatrixManager matrixManager = null;
    private static GUIManager guiManager = null;
    private static GraphManager graphManager = null;
    private static RepositoryManager algoManager = null;

    private GUIManager() {
        idm = InterDomainManager.getInstance();
        graphManager = GraphManager.getInstance();
        matrixManager = TrafficMatrixManager.getInstance();
        algoManager = RepositoryManager.getInstance();
    }


    /**
     * Use this method to get an instance of GUIManager since the constructor
     * is private
     *
     * @return returns an instance of GUIManager
     */
    public static GUIManager getInstance() {
        if (guiManager == null) {
            guiManager = new GUIManager();
        }
        return guiManager;
    }


    /**
     * Returns the Domain currently loaded in the GUI
     *
     * @return the Domain currently loaded in the GUI
     */
    public Domain getCurrentDomain() {
        return idm.getDefaultDomain();
    }


    public int loadTopology(File tFile) throws InvalidDomainException, DomainAlreadyExistException  {
        idm.loadDomain(tFile.getAbsolutePath(), true, false, false);
        return TOPO_CHECKED;
    }

    /**
     * Load given domain in the InterDomainManager and Display it in the GUI
     * If <code>domain</code> is null, do nothing.
     *
     * @param domain
     * @throws DomainAlreadyExistException if a domain with same ASID is already in the InterDomainManager
     */
    public void openDomain(Domain domain) throws DomainAlreadyExistException {
        if (domain != null) {
            idm.addDomain(domain);
            try {
                idm.setDefaultDomain(domain.getASID());
            } catch (InvalidDomainException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load given domain in the InterDomainManager and Display it in the GUI.
     * If the domain is already in InterDomainManager, it'll be replaced by the given one.
     * If <code>domain</code> is null, do nothing.
     *
     * @param domain
     */
    public void openDomainReplace(Domain domain) {
        if (domain != null) {
            try {
                idm.removeDomain(domain.getASID());
            } catch (InvalidDomainException e) {}

            try {
                idm.addDomain(domain);
            } catch (DomainAlreadyExistException e) {
                e.printStackTrace();
            }

            try {
                idm.setDefaultDomain(domain.getASID());
            } catch (InvalidDomainException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove the Domain from InterDomainManager and display another one (if exists).
     * @param ASID
     */
    public void closeDomain(int ASID) {

        algoManager.stopAlgorithms(ASID);

        // remove all loaded Traffic Matrix on the domain
        for (int tmId : getManagedMatrices()) {
            try {
                matrixManager.removeTrafficMatrix(ASID, tmId);
            } catch (InvalidTrafficMatrixException e) {
                e.printStackTrace();
            }
        }

        try {
            idm.removeDomain(ASID);
        } catch (InvalidDomainException e) {
            logger.warn("Trying to remove inexistent domain.");
            //e.printStackTrace();
        }
        if (idm.getDefaultDomain() == null && idm.getNbDomains() > 0) {
            try {
                idm.setDefaultDomain(idm.getAllDomains()[0].getASID());
            } catch (InvalidDomainException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Load DemandMatrix from file and add it to the TrafficMatrixManager
     * TrafficMatrix file must match one of the loaded domain.
     *
     * @param mFile the File from which the matrix will be loaded
     */
    public void addDemandMatrix(File mFile) {
        MainWindow mainWindow = MainWindow.getInstance();
        int tmId;
        TrafficMatrix tm = null;
        try {
            tm = TrafficMatrixFactory.loadTrafficMatrix(mFile.getAbsolutePath());
            tmId = matrixManager.generateTMID(tm.getASID());
            matrixManager.addTrafficMatrix(tm, tmId);
            matrixManager.setDefaultTrafficMatrix(tm.getASID(), tmId);
        } catch (InvalidDomainException e) {
            mainWindow.errorMessage("The file contains an Invalid Traffic Matrix or a Traffic Matrix that cannot be used with the loaded Domain.");
            return;
        } catch (TrafficMatrixAlreadyExistException e) {
            mainWindow.errorMessage("A Traffic Matrix with the same ID is already loaded");
            return;
        } catch (InvalidTrafficMatrixException e) {
            mainWindow.errorMessage("The file contains an Invalid Traffic Matrix or a Traffic Matrix that cannot be used with the loaded Domain.");
            return;
        } catch (TrafficMatrixIdException e) {
            mainWindow.errorMessage("The file contains an Invalid Traffic Matrix or a Traffic Matrix that cannot be used with the loaded Domain.");
            return;
        } catch (NodeNotFoundException e) {
            mainWindow.errorMessage("The file contains an Invalid Traffic Matrix or a Traffic Matrix that cannot be used with the loaded Domain.");
            return;
        }
        mainWindow.infoMessage("Traffic Matrix loaded for domain with ASID: " + tm.getASID());
    }


    /**
     * Remove all matrices currently managed (all matrices for the domain displayed)
     */
    public void removeTrafficMatrices() {
        for (Integer tmId : matrixManager.getTrafficMatrices(getCurrentDomain().getASID())) {
            try {
                matrixManager.removeTrafficMatrix(getCurrentDomain().getASID(), tmId.intValue());
            } catch (InvalidTrafficMatrixException e) {
                continue;
            }
        }
    }


    /**
     * Remove Traffic Matrix with the specified tmid from both Gui and TrafficMatrixManager
     *
     * @param tmid the matrix to be removed
     */
    public void removeTrafficMatrix(int tmid) {
        try {
            matrixManager.removeTrafficMatrix(getCurrentDomain().getASID(), tmid);
            if (!matrixManager.getTrafficMatrices(getCurrentDomain().getASID()).isEmpty())
                matrixManager.setDefaultTrafficMatrix(matrixManager.getTrafficMatrices(getCurrentDomain().getASID()).get(0).intValue());
        } catch (InvalidTrafficMatrixException e) {
            e.printStackTrace();
        }
    }

    public void saveTrafficMatrix(int tmid) {
        TopoChooser saver = new TopoChooser();
        File file = saver.saveTopo(MainWindow.getInstance());
        if (file != null) {
            try {
                String filename = file.getAbsolutePath();
                if (!filename.toLowerCase().endsWith(".xml")) {
                    filename = filename.concat(".xml");
                }
                matrixManager.saveTrafficMatrix(getCurrentDomain().getASID(), tmid, filename);
            } catch (Exception e) {
                MainWindow.getInstance().errorMessage("The traffic matrix could not be saved");
            }
        }
    }


    /**
     * Called by the actionlistener when the save Button in the menubar is pressed,
     * this method call a chooser and then save the topology in the selected file.
     */
    public void saveTopo() {
        TopoChooser saver = new TopoChooser();
        File file = saver.saveTopo(MainWindow.getInstance());
        if (file != null) {
            graphManager.updateLocation();
            try {
                String filename = file.getAbsolutePath();
                if (!filename.toLowerCase().endsWith(".xml")) {
                    filename = filename.concat(".xml");
                }
                idm.saveDomain(getCurrentDomain().getASID(), filename);
            } catch (Exception e) {
                MainWindow.getInstance().errorMessage("The domain could not be saved");
            }
        }
    }


    /**
     * This methods return the current topology name (the domain URI)
     *
     * @return current domain name
     */
    public String getName() {
        return getCurrentDomain().getURI().toString();
    }


    /**
     * A method that returns all topology nodes in a vector
     *
     * @return returns vector containing containing IDs of the node of the current domain
     */
    public String[] getNodesList() {
        if (getCurrentDomain() == null) return null;
        int numberOfNodes = getCurrentDomain().getNbNodes();
        String nodes[] = new String[numberOfNodes];
        for (int i = 0; i < numberOfNodes; i++) {
            try {
                nodes[i] = getCurrentDomain().getConvertor().getNodeId(i);
            } catch (Exception e) {
            }
        }
        return nodes;
    }

    /**
     * Return a list of matrices TMID. All these matrices are compatible with the currently selected Domain and were
     * previously loaded from the gui.
     *
     * @return a list of matrices TMID
     */
    public List<Integer> getManagedMatrices() {
        return (getCurrentDomain() != null) ? matrixManager.getTrafficMatrices(getCurrentDomain().getASID()) : new ArrayList<Integer>();
    }
}
