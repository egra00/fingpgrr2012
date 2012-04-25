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

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.FastRerouteSwitchingMethod;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManagerListener;
import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.*;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManagerListener;
import be.ac.ulg.montefiore.run.totem.repository.MultiCommodityFlow.MultiCommodityFlow;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManagerListener;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.*;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.*;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.AbstractGUIModule;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.ClassCellRenderer;
import be.ac.ulg.montefiore.run.totem.netController.facade.NetworkControllerManager;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import be.ac.ucl.ingi.totem.repository.CBGP;
import be.ac.ucl.ingi.totem.repository.model.CBGPSimulator;
import be.ac.ucl.ingi.totem.repository.guiComponents.RoutingTable;
import be.ac.ucl.ingi.totem.repository.guiComponents.BGPInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.IOException;

import org.apache.log4j.Logger;


/*
 * Changes:
 * --------
 * 8-Dec.-2005 : lots of changes ... now can compute fullmesh and add an lsp with selected algo... (GMO)
 * 13-Dec.-2005: remove the Traffic Matrix Manager panel of this module (now in MainWindow). (GMO)
 * 23-Dec.-2005: the combo box now contains a LSPPrimaryRouting instance instead of a String which improves the quality
    of the code (GMO)
 * 12-Jan.-2006: remove useless repaint() calls, fix bug in lspId display, change structure, now listen to events from
 *   RepositoryManager, InterDomainManager and TrafficMatrixManager (for fullMeshComputer) and adapt dialog content in
 *   response to those. (GMO)
 * 03-Feb-2006: change default size of "add lsp" panel (GMO)
 * 04-Apr-2006: Add establishment order when computing fullmesh. (GMO)
 * 04-Apr-2006: Add choice for removing LSPs before establishing fullmesh (GMO).
 * 04-Apr-2006: Add cancellation to fullmesh computing (GMO).
 * 19-May-2006: Add detour backup LSP creation, change layouts, add Lspid field to addLsp (GMO)
 * 16-Aug-2006: Add bw units in addLsp panel (GMO)
 * 18-Aug-2006: Cleanup commented code, Add Lsp id parameter for detour LSPs (GMO)
 * 22-Aug-2006: Add "Default" radio button for protectionType for detour LSPs (GMO)
 * 23-Oct-2006: Add diffserv expandable panel to addLSP dialog. Small change to the layout to enable varaible size (GMO)
 * 24-Oct-2006: not singleton anymore, add a public constructor to accomodate new module loader (GMO)
 * 05-Dec-2006: Add diffserv parameters, inline applyFullMesh(.), change dialog appearance (GMO)
 * 12-Dec-2006: Bugfix in fullmesh computer (GMO)
 * 10-Jan-2007: Bugfix when no domain is loaded (GMO)
 * 19-Jan-2007: Add optDivideTM (GMO)
 * 06-Mar-2007: Fix bug when switching domain and change scrollpane height, in "Lsp" and "fullmesh" dialogs (GMO)
 * 13-Mar-2007: Add "add network controller" and "Remove all network controllers" menu items (GMO)
 * 01-Jun-2007: change keyboard shortcuts (GMO)
 * 14-Jun-2007: add MCF (GMO)
 * 18-Jun-2007: add "list shortest paths", remove "add network Controller" and "remove all network controller" from menu (GMO)
 * 21-Jun-2007: add "ECMP analysus" (GMO)
 * 25-Sep-2007: add "Enable Traffic switching" item, remove reroute methods related items (GMO)
 * 26-Oct-2007: start algo dialog now opens centered to parent (GMO)
 * 29-Nov-2007: add "Compute bypass" menuItem, adapt to new LSPDetourRouting interface (GMO)
 * 17-Dec-2007: change menu item order, create submenus. Add protect all resources with bypasses (GMO)
 * 28-Feb-2008: add class of service panel for primary lsp, fullmesh and bypass lsp. Add Cos MPLS routing menu item (GMO) 
 */

/**
 * @author Olivier Materne (O.Materne@student.ulg.ac.be)
 * @author Gael Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class RoutingGUIModule extends AbstractGUIModule {
    private static Logger logger = Logger.getLogger(RoutingGUIModule.class);

    private static MainWindow mainWindow = null;
    private static GUIManager manager = null;
    private static TrafficMatrixManager matrixManager = null;

    private static RoutingGUIModule lastInstance = null;

    /**
     * a public constructor with no parameter
     */
    public RoutingGUIModule() {
        manager = GUIManager.getInstance();
        mainWindow = MainWindow.getInstance();
        matrixManager = TrafficMatrixManager.getInstance();
        lastInstance = this;
    }

    public static RoutingGUIModule getInstance() {
        return lastInstance;
    }

    /**
     * @return the menu for this GUIModule
     */
    public JMenu getMenu() {
        JMenu menu = new JMenu("Routing");
        menu.setMnemonic(KeyEvent.VK_R);

        JMenuItem menuItem;
        KeyStroke ks;

        JMenu subMenu = new JMenu("Primary computation");

        menuItem = new JMenuItem("Add Primary Lsp");
        menuItem.addActionListener(new AddLspListener());
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK);
        menuItem.setAccelerator(ks);
        subMenu.add(menuItem);

        menuItem = new JMenuItem("Apply Full Mesh");
        menuItem.addActionListener(new FullMeshActionListener());
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK);
        menuItem.setAccelerator(ks);
        subMenu.add(menuItem);

        menu.add(subMenu);

        subMenu = new JMenu("Detour computation (one-to-one backup)");

        menuItem = new JMenuItem("Protect single primary Lsp");
        menuItem.addActionListener(new ComputeDetourLspListener());
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK);
        menuItem.setAccelerator(ks);
        subMenu.add(menuItem);

        menuItem = new JMenuItem("Protect all primary lsps");
        menuItem.setEnabled(false);
        menuItem.setToolTipText("Not implemented.");
        subMenu.add(menuItem);

        menu.add(subMenu);

        subMenu = new JMenu("Bypass computation (facility backup)");

        menuItem = new JMenuItem("Protect single resource");
        menuItem.addActionListener(new ComputeBypassLspListener());
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.CTRL_MASK);
        menuItem.setAccelerator(ks);
        subMenu.add(menuItem);

        /*
        menuItem = new JMenuItem("Protect all resources");
        menuItem.addActionListener(new ComputeMultipleBypassLspListener());
        subMenu.add(menuItem);
        */
        
        menu.add(subMenu);

        menu.addSeparator();

        menuItem = new JMenuItem("IGP routing...");
        menuItem.addActionListener(new IGPRoutingListener());
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK);
        menuItem.setAccelerator(ks);
        menu.add(menuItem);

        menuItem = new JMenuItem("Cos MPLS routing...");
        menuItem.addActionListener(new MPLSRoutingListener());
        menu.add(menuItem);

        menuItem = new JMenuItem("OptDivideTM...");
        menuItem.addActionListener(new OptDivideTMListener());
        menu.add(menuItem);

        menuItem = new JMenuItem("Multi Commodity Flow");
        menuItem.addActionListener(new MCFListener());
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = new JMenuItem("List shortests paths");
        menuItem.addActionListener(new ListShortestPathsListener());
        menu.add(menuItem);

        menuItem = new JMenuItem("ECMP Analysis");
        menuItem.addActionListener(new ECMPAnalysisListener());
        menu.add(menuItem);

        /*
        menu.addSeparator();

        menuItem = new JMenuItem("Add network Controller");
        menuItem.addActionListener(new AddNetControllerActionListener());
        menu.add(menuItem);

        menuItem = new JMenuItem("Remove All network Controllers");
        menuItem.addActionListener(new RemoveControllersActionListener());
        menu.add(menuItem);
        */


        menu.addSeparator();
        menuItem = new JMenuItem("Enable Traffic Switching");
        menuItem.addActionListener(new TrafficSwitchingActionListener());
        menu.add(menuItem);

        return menu;
    }


    /**
     * Return the name of the module ("Routing")
     *
     * @return the name of this module
     */
    public String getName() {
        return "Routing";
    }


    /**
     * This module should be loaded at startup
     *
     * @return true
     */
    public boolean loadAtStartup() {
        return true;
    }


    /**
     * A lot of method uses this module, thus it was made impossible to remove
     *
     * @return true
     */
    public boolean isUnloadable() {
        return true;
    }

    public void displayPanel() {
        new LSPAdder();
    }
    
    /**
     * Display the routing table of a node
     */
    public void displayRT(Node node) {
        // CBGP simulator available ?
        CBGPSimulator cbgp;
        try {   
            cbgp = (CBGP) RepositoryManager.getInstance().getAlgo("CBGP");
        } catch (NoSuchAlgorithmException e) {
            mainWindow.errorMessage("Please start the CBGP algorithm before using it!");
            return;
        }
        Vector routes;
        try {
            routes = cbgp.netNodeGetRT(node.getRid(), null);
        } catch (RoutingException e) {
            routes = null;
            return;
        }
        if (routes != null) {
            // mainWindow.addDialog(new JScrollPane(new RoutingTable()), 600, 200, "R Table");
            new RoutingTable(routes, node);
        } else {
            // imprimer message d'erreur
            mainWindow.errorMessage("error");
        }
    }
    
    /**
     * Display the BGP info of a node
     */
    public void displayBGPInfo(Node node) {
        // CBGP simulator available ?
        CBGPSimulator cbgp;
        try {   
            cbgp = (CBGP) RepositoryManager.getInstance().getAlgo("CBGP");
        } catch (NoSuchAlgorithmException e) {
            mainWindow.errorMessage("Please start the CBGP algorithm before using it!");
            return;
        }
        new BGPInfo(node);
    }

    public List<String> addDetourLsp(String lspId, String protectedLspId, String methodType, String protectionType, LSPDetourRouting routing, HashMap<String, String> params) throws NoRouteToHostException, RoutingException, TotemActionExecutionException, LocalDatabaseException {
        Domain domain = manager.getCurrentDomain();
        LSPDetourRoutingParameter param = new LSPDetourRoutingParameter(lspId);
        param.setMethodType(methodType);
        param.setProtectionType(protectionType);
        param.setProtectedLSP(protectedLspId);

        param.putAllRoutingAlgorithmParameter(params);

        if (routing == null) {
            mainWindow.errorMessage("Null algorithm.");
            return null;
        }

        TotemActionList acList = routing.routeDetour(domain, param);
        ArrayList<String> lspIds = new ArrayList<String>(acList.size());
        for (int i = 0; i < acList.size(); i++) {
            if (acList.get(i) instanceof AddLspAction) {
                lspIds.add(((AddLspAction)acList.get(i)).getLsp().getId());
            }
            ((TotemAction)acList.get(i)).execute();
        }
        return lspIds;

    }

    /**
     * A listener to trigger a AddLspAction
     */
    private class AddLspListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null)
                new LSPAdder();
            else mainWindow.errorMessage("A domain must be loaded to perform this action.");
        }
    }

    private class ComputeDetourLspListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null)
                new BackupLSPAdder();
            else mainWindow.errorMessage("A domain must be loaded to perform this action.");
        }
    }

    private class ComputeBypassLspListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null) {
                JDialog bypassDialog = new BypassLSPAdder();
                bypassDialog.pack();
                bypassDialog.setLocationRelativeTo(MainWindow.getInstance());
                bypassDialog.setVisible(true);
            }
            else mainWindow.errorMessage("A domain must be loaded to perform this action.");
        }
    }

    private class ComputeMultipleBypassLspListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null) {
                JDialog bypassDialog = new MultipleBypassDialog();
                bypassDialog.pack();
                bypassDialog.setLocationRelativeTo(MainWindow.getInstance());
                bypassDialog.setVisible(true);
            }
            else mainWindow.errorMessage("A domain must be loaded to perform this action.");
        }
    }

    /**
     * A Listener to trigger a Full Mesh
     */
    private class FullMeshActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null && manager.getManagedMatrices().size() > 0)
                new FullMeshComputer();
            else if (manager.getCurrentDomain() == null)
                mainWindow.errorMessage("A domain must be loaded to perform this action.");
            else mainWindow.errorMessage("A Traffic Matrix must be loaded to perform this action.");
        }
    }

    private class IGPRoutingListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null && manager.getManagedMatrices().size() > 0)
                new IGPRouting();
            else if (manager.getCurrentDomain() == null)
                mainWindow.errorMessage("A domain must be loaded to perform this action.");
            else mainWindow.errorMessage("A Traffic Matrix must be loaded to perform this action.");
        }
    }

    private class MPLSRoutingListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null && manager.getManagedMatrices().size() > 0)
                mainWindow.showDialog(new MPLSRoutingDialog());
            else if (manager.getCurrentDomain() == null)
                mainWindow.errorMessage("A domain must be loaded to perform this action.");
            else mainWindow.errorMessage("A Traffic Matrix must be loaded to perform this action.");
        }
     }

    private class OptDivideTMListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null && manager.getManagedMatrices().size() > 0)
                mainWindow.showDialog(new OptDivideTMDialog());
            else if (manager.getCurrentDomain() == null)
                mainWindow.errorMessage("A domain must be loaded to perform this action.");
            else mainWindow.errorMessage("A Traffic Matrix must be loaded to perform this action.");
        }
    }

    private class MCFListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null && manager.getManagedMatrices().size() > 0) {
                try {
                    Domain domain = manager.getCurrentDomain();
                    TrafficMatrix tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(domain.getASID());
                    MultiCommodityFlow mcf = new MultiCommodityFlow(domain, tm);

                    try {
                        String id = LinkLoadComputerManager.getInstance().generateId(domain, "MCF");
                        LinkLoadComputerManager.getInstance().addLinkLoadComputer(mcf, true, id);
                    } catch (LinkLoadComputerAlreadyExistsException e1) {
                        try {
                            Pair<String, LinkLoadComputer> pair = LinkLoadComputerManager.getInstance().getLinkLoadComputer(mcf);
                            mcf = (MultiCommodityFlow)pair.getSecond();
                            LinkLoadComputerManager.getInstance().setDefaultLinkLoadComputer(domain, pair.getFirst());
                        } catch (InvalidLinkLoadComputerException e2) {
                            mainWindow.errorMessage(e2.getMessage());
                            e2.printStackTrace();
                            return;
                        }
                    } catch (LinkLoadComputerIdException e1) {
                        mainWindow.errorMessage(e1.getMessage());
                        e1.printStackTrace();
                        return;
                    }

                    mcf.recompute();
                } catch (InvalidTrafficMatrixException e1) {
                    mainWindow.errorMessage(e1);
                } catch (IOException e1) {
                    mainWindow.errorMessage(e1);
                }
            }
            else if (manager.getCurrentDomain() == null)
                mainWindow.errorMessage("A domain must be loaded to perform this action.");
            else mainWindow.errorMessage("A Traffic Matrix must be loaded to perform this action.");
        }
    }

    private class ListShortestPathsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() == null)
                mainWindow.errorMessage("A domain must be loaded to perform this action.");
            else
                mainWindow.showDialog(new ListShortestPathsDialog(manager.getCurrentDomain()));
        }
    }

    private class ECMPAnalysisListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() == null)
                mainWindow.errorMessage("A domain must be loaded to perform this action.");
            else
                mainWindow.showDialog(new ECMPAnalysisDialog(manager.getCurrentDomain()));
        }
    }


    private class AddNetControllerActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null) {
                JDialog dialog = new NetworkControllerAdderDialog();
                mainWindow.showDialog(dialog);
            } else {                                                                     
                mainWindow.errorMessage("A domain must be loaded to perform this action.");
            }
        }
    }

    private class RemoveControllersActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            NetworkControllerManager.getInstance().removeAllNetworkControllers();
            JOptionPane.showMessageDialog(MainWindow.getInstance(), "Network controllers removed.", "Operation done.", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class TrafficSwitchingActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null) {
                Domain domain = manager.getCurrentDomain();
                FastRerouteSwitchingMethod sm = new FastRerouteSwitchingMethod(domain);
                domain.setSwitchingMethod(sm);
            } else {
                mainWindow.errorMessage("A domain must be loaded to perform this action.");
            }
        }
    }

    /**
     * A class to compute a fullmesh of lsps. This class shows a window that will ask for
     * the traffic matrix to use, the computation algorithm and its parameters
     */
    private class FullMeshComputer implements ActionListener, RepositoryManagerListener, TrafficMatrixManagerListener, InterDomainManagerListener {
        private JComboBox algo = null;
        private JComboBox matrix = null;
        private JComboBox order = null;
        private ParamTable params = null;
        private JDialog dialog = null;
        private ExpandablePanel diffServPanel = null;
        private ExpandablePanel cosPanel = null;

        public static final String DECREASING_BW_ORDER = "Decreasing Bandwidth Order";
        public static final String INCREASING_BW_ORDER = "Increasing Bandwidth Order";
        public static final String SHUFFLE_ORDER = "Shuffle Order";

        FullMeshComputer() {
            dialog = new JDialog(MainWindow.getInstance(), "Compute fullmesh");
            dialog.setContentPane(setupUI());
            dialog.pack();
            dialog.setLocationRelativeTo(MainWindow.getInstance());
            dialog.setVisible(true);

            RepositoryManager.getInstance().addListener(this);
            TrafficMatrixManager.getInstance().addListener(this);
            InterDomainManager.getInstance().addListener(this);

            dialog.addWindowListener(new WindowListener() {
                public void windowOpened(WindowEvent e) {}
                public void windowClosing(WindowEvent e) {
                    hide();
                }
                public void windowClosed(WindowEvent e) {}
                public void windowIconified(WindowEvent e) {}
                public void windowDeiconified(WindowEvent e) {}
                public void windowActivated(WindowEvent e) {}
                public void windowDeactivated(WindowEvent e) {}
            });

            if (algo.getItemCount() > 0)
                algo.setSelectedIndex(0);
        }

        private JPanel setupUI() {
            JPanel jp = new JPanel();

            //jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
            jp.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 5, 5, 5);


            jp.add(new JLabel("Choose a Traffic Matrix:"), c);
            c.gridy++;

            matrix = new JComboBox();
            fillMatrixCombo();
            jp.add(matrix, c);
            c.gridy++;

            jp.add(new JLabel("Select establishment order:"), c);
            c.gridy++;
            order = new JComboBox();
            order.addItem(DECREASING_BW_ORDER);
            order.setSelectedItem(DECREASING_BW_ORDER);
            order.addItem(INCREASING_BW_ORDER);
            order.addItem(SHUFFLE_ORDER);
            jp.add(order, c);
            c.gridy++;

            diffServPanel = new ExpandablePanel(dialog, "DiffServ", new DiffservPanel(manager.getCurrentDomain()));
            diffServPanel.setToolTipText("If the panel is retracted, diffServ configuration will be ignored.");
            jp.add(diffServPanel, c);
            c.gridy++;

            cosPanel = new ExpandablePanel(dialog, "Classes of Service", new ClassesOfServicePanel(manager.getCurrentDomain()));
            cosPanel.setToolTipText("If the panel is retracted, all classes of service will be accepted by the LSP.");
            jp.add(cosPanel, c);
            c.gridy++;

            jp.add(new JLabel("Choose algorithm:"), c);
            c.gridy++;

            algo = new JComboBox();
            algo.setRenderer(new ClassCellRenderer());
            fillAlgoCombo();
            jp.add(algo, c);
            c.gridy++;
            c.fill = GridBagConstraints.NONE;

            JButton startAlgo = new JButton("Start another algorithm...");
            startAlgo.addActionListener(new StartAlgoListener(dialog, LSPPrimaryRouting.class));
            jp.add(startAlgo, c);
            c.gridy++;
            c.fill = GridBagConstraints.HORIZONTAL;

            jp.add(new JLabel("Additional routing parameters"), c);
            c.gridy++;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.BOTH;
            params = new ParamTable(7);
            algo.addActionListener(new AlgoComboListener(params));
            jp.add(new JScrollPane(params) {
                public Dimension getPreferredSize() {
                    int width = super.getPreferredSize().width;
                    int height = params.getPreferredSize().height + 50;
                    return new Dimension(width, height);
                }
            }, c);


            c.fill = GridBagConstraints.HORIZONTAL;
            c.weighty = 0.0;
            c.gridy++;
            JButton accept = new JButton("Compute");
            accept.addActionListener(this);
            jp.add(accept, c);

            dialog.getRootPane().setDefaultButton(accept);
            return jp;
        }

        private void hide() {
            dialog = null;
            RepositoryManager.getInstance().removeListener(this);
            TrafficMatrixManager.getInstance().removeListener(this);
            InterDomainManager.getInstance().removeListener(this);
        }

        private void fillAlgoCombo() {
            algo.removeAllItems();
            for (TotemAlgorithm a : RepositoryManager.getInstance().getAllStartedAlgos(manager.getCurrentDomain().getASID(), LSPPrimaryRouting.class)) {
                algo.addItem(a);
            }
        }

        private void fillMatrixCombo() {
            matrix.removeAllItems();
            for (Integer i : manager.getManagedMatrices()) {
                matrix.addItem(i);
            }
        }


        public void actionPerformed(ActionEvent e) {
            computeFullMesh();
        }

        /**
         * Compute the fullmesh of lsps with the current content of the window fields
         */
        void computeFullMesh() {
            new Thread(new Runnable() {
                public void run() {
                    if (matrix.getSelectedItem() != null) {
                        // stop cell editing
                        if (params.getCellEditor() != null && !params.getCellEditor().stopCellEditing())
                            return;

                        HashMap<String, String> routingParams = params.toHashMap();

                        LSPPrimaryRouting routing = (LSPPrimaryRouting) algo.getSelectedItem();
                        int tmId = (Integer) matrix.getSelectedItem();

                        Domain domain = manager.getCurrentDomain();
                        String orderStr = (String)order.getSelectedItem();

                        TrafficMatrix tm = null;
                        boolean errors = false;
                        Exception lastError = null;

                        if (domain == null) {
                            mainWindow.errorMessage("A domain must be loaded to perform this action.");
                            return;
                        }

                        try {
                            tm = matrixManager.getTrafficMatrix(domain.getASID(), tmId);
                        } catch (InvalidTrafficMatrixException e) {
                            mainWindow.errorMessage("A Traffic Matrix must be loaded to perform this action.");
                            return;
                        }

                        if (algo.getSelectedItem() == null) {
                            mainWindow.errorMessage("Please select an algorithm");
                            return;
                        }

                        if (domain.getNbLsps() > 0) {
                            int n = JOptionPane.showConfirmDialog(mainWindow, "Do you want to remove all lsps established in the current domain prior to applying the fullmesh ?", "Confirm suppress LSPs",
                                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                            if (n == JOptionPane.YES_OPTION)
                                domain.removeAllLsps();
                        }

                        int numberOfNodes = domain.getNbNodes();
                        ArrayList<LSPPrimaryRoutingParameter> paramList = new ArrayList<LSPPrimaryRoutingParameter>();

                        //create a progress bar
                        ProgressBarPanel progressBar = new ProgressBarPanel(0, numberOfNodes * numberOfNodes, 400);
                        JDialog dialog = mainWindow.showDialog(progressBar, "Progress Bar");

                        progressBar.setMessage("Searching Traffic Matrix for LSPs to add");

                        //compute params list from matrix

                        for (int i = 0; i < numberOfNodes; i++) {
                            String source;
                            try {
                                source = domain.getConvertor().getNodeId(i);
                            } catch (Exception e) {
                                errors = true;
                                continue;
                            }
                            for (int j = 0; j < numberOfNodes; j++) {
                                if (i == j) continue;
                                float bw;  //the required bandwidth
                                String target;
                                LSPPrimaryRoutingParameter param = null;
                                try {
                                    bw = tm.get(i, j);
                                    //if (bw == 0)
                                    //  continue;
                                    target = domain.getConvertor().getNodeId(j);
                                    param = new LSPPrimaryRoutingParameter(source, target, domain.generateLspId());
                                    param.setBandwidth(bw);
                                    if (diffServPanel.isExpanded()) {
                                        DiffservPanel panel = (DiffservPanel)diffServPanel.getPanel();
                                        param.setClassType(panel.getClassType());
                                        param.setSetup(panel.getSetupLevel());
                                        param.setHolding(panel.getHoldingLevel());
                                    }
                                    if (cosPanel.isExpanded()) {
                                        param.setAcceptedCos(((ClassesOfServicePanel)cosPanel.getPanel()).getClassesOfService());
                                    }
                                } catch (Exception e) {
                                    errors = true;
                                    lastError = e;
                                    continue;
                                }
                                paramList.add(param);
                            }
                        }

                        progressBar.setMessage("Sorting LSPs");

                        if (orderStr.equals(FullMeshComputer.DECREASING_BW_ORDER)) {
                            Collections.sort(paramList, new Comparator<LSPRoutingParameter>() {
                                public int compare(LSPRoutingParameter o, LSPRoutingParameter o1) {
                                    return new Float(o1.getBandwidth()).compareTo(new Float(o.getBandwidth()));
                                }
                            });
                        } else if (orderStr.equals(FullMeshComputer.INCREASING_BW_ORDER)) {
                            Collections.sort(paramList, new Comparator<LSPRoutingParameter>() {
                                public int compare(LSPRoutingParameter o, LSPRoutingParameter o1) {
                                    return new Float(o.getBandwidth()).compareTo(new Float(o1.getBandwidth()));
                                }
                            });
                        } else if (orderStr.equals(FullMeshComputer.SHUFFLE_ORDER)) {
                            Collections.shuffle(paramList);
                        } else {
                            logger.error("Sorting scheme unknown: " + order);
                            mainWindow.errorMessage("Sorting scheme unknown: " + order);
                            return;
                        }

                        progressBar.setMessage("");


                        int progress = 0;
                        progressBar.setMaximum(paramList.size());
                        progressBar.setCancelable(true);

                        for (java.util.ListIterator iter = paramList.listIterator(); iter.hasNext();) {
                            LSPPrimaryRoutingParameter param = (LSPPrimaryRoutingParameter) iter.next();
                            param.putAllRoutingAlgorithmParameter(routingParams);
                            if (progressBar.isCancelled())
                                break;
                            TotemActionList acList = null;
                            try {
                                acList = routing.routeLSP(domain, param);
                                for (int i = 0; i < acList.size(); i++) {
                                    TotemAction action = (TotemAction) acList.get(i);
                                    /* debug
                                    if (action instanceof PreemptLspsAction) {
                                        logger.debug("Executing preempt action: ");
                                        for (String s : ((PreemptLspsAction)action).getLsps()) {
                                            logger.debug()
                                        }
                                    }*/
                                    action.execute();
                                }
                            } catch (Exception e) {
                                logger.error("Exception occurred: " + e.getClass().getSimpleName() + " Msg: " + e.getMessage());
                                errors = true;
                                lastError = e;
                            }
                            progressBar.setValue(++progress); // update the bar progress
                            progressBar.setMessage("ROUTING : " + progress + " LSPs of " + paramList.size() + " added.");
                        }
                        dialog.dispose();
                        if (errors)
                            mainWindow.errorMessage("Some errors occured during computation. (Last error : " + lastError.getClass().getSimpleName() + " " + lastError.getMessage() + ")");
                        if (progressBar.isCancelled())
                            mainWindow.errorMessage("Operation not completed, cancelled by user.");
                        return;

                    } else {
                        mainWindow.errorMessage("Please select a Traffic Matrix.");
                    }
                }
            }).start();
        }

        public void startAlgoEvent(TotemAlgorithm algo) {
            fillAlgoCombo();
        }

        public void stopAlgoEvent(TotemAlgorithm algo) {
            fillAlgoCombo();
        }

        public void addTrafficMatrixEvent(TrafficMatrix tm, int tmId) {
            fillMatrixCombo();
        }

        public void removeTrafficMatrixEvent(TrafficMatrix tm, int tmId) {
            fillMatrixCombo();
        }

        public void changeDefaultTrafficMatrixEvent(int asId, TrafficMatrix tm) {
        }

        public void addDomainEvent(Domain domain) {}

        public void removeDomainEvent(Domain domain) {}

        public void changeDefaultDomainEvent(Domain domain) {
            dialog.setContentPane(setupUI());
            dialog.validate();
        }
    }



    /**
     * A class to add an Lsp to the domain. This class shows a window that will ask for
     * the new lsp 's ingress, egress, required bandwidth, computation algorithm and parameters
     */
    private class LSPAdder implements ActionListener, RepositoryManagerListener, InterDomainManagerListener {
        private JComboBox source = null;
        private JComboBox dest = null;
        private JTextField bwField = null;
        private JTextField lspId = null;
        private JComboBox algo = null;
        private ParamTable params = null;
        private JDialog dialog = null;
        private Domain currentDomain = null;
        private ExpandablePanel diffServPanel = null;
        private ExpandablePanel cosPanel = null;
        private JButton accept = null;

        /**
         * Constructor
         */
        LSPAdder() {
            dialog = new JDialog(MainWindow.getInstance(), "Add LSP");
            dialog.setContentPane(setupUI());
            dialog.pack();
            dialog.setLocationRelativeTo(MainWindow.getInstance());
            dialog.setVisible(true);

            RepositoryManager.getInstance().addListener(this);
            InterDomainManager.getInstance().addListener(this);

            dialog.addWindowListener(new WindowListener() {
                public void windowOpened(WindowEvent e) {}
                public void windowClosing(WindowEvent e) {
                    hide();
                }
                public void windowClosed(WindowEvent e) {}
                public void windowIconified(WindowEvent e) {}
                public void windowDeiconified(WindowEvent e) {}
                public void windowActivated(WindowEvent e) {}
                public void windowDeactivated(WindowEvent e) {}
            });

            if (algo.getItemCount() > 0)
                algo.setSelectedIndex(0);
        }

        private JPanel setupUI() {
            currentDomain = manager.getCurrentDomain();

            JPanel jp = new JPanel();

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 5, 5, 5);

            jp.setLayout(new GridBagLayout());

            jp.add(new JLabel("Lsp Id (leave blank to generate)."), c);
            lspId = new JTextField();
            c.gridy++;
            jp.add(lspId, c);
            String nodes[] = manager.getNodesList();
            if (nodes != null) {
                source = new JComboBox(nodes);
                dest = new JComboBox(nodes);
            } else {
                source = new JComboBox();
                dest = new JComboBox();
            }
            c.gridy++;
            jp.add(new JLabel("Ingress"), c);
            c.gridy++;
            jp.add(source, c);
            c.gridy++;
            jp.add(new JLabel("Egress"), c);
            c.gridy++;
            jp.add(dest, c);
            c.gridy++;
            jp.add(new JLabel("Bandwidth"), c);

            c.gridwidth = 1;
            c.gridy++;

            bwField = new JTextField("BW here", 10);
            jp.add(bwField, c);

            c.weightx = 0.0;
            c.gridx++;
            String unit = currentDomain == null ? "BPS" : currentDomain.getBandwidthUnit().toString();
            jp.add(new JLabel(unit), c);

            c.gridx = 0;
            c.gridwidth = 2;
            c.weightx = 1.0;
            c.gridy++;

            diffServPanel = new ExpandablePanel(dialog, "DiffServ", new DiffservPanel(manager.getCurrentDomain()));
            diffServPanel.setToolTipText("If the panel is retracted, diffServ configuration will be ignored.");
            jp.add(diffServPanel, c);

            c.gridy++;
            cosPanel = new ExpandablePanel(dialog, "Classes of Service", new ClassesOfServicePanel(manager.getCurrentDomain()));
            cosPanel.setToolTipText("If the panel is retracted, all classes of service will be accepted by the LSP.");
            jp.add(cosPanel, c);

            c.gridy++;
            jp.add(new JLabel("Algorithm"), c);
            algo = new JComboBox();
            algo.setRenderer(new ClassCellRenderer());

            c.gridy++;
            jp.add(algo, c);

            JButton startAlgo = new JButton("Start another algorithm...");
            startAlgo.addActionListener(new StartAlgoListener(dialog, LSPPrimaryRouting.class));
            c.gridy++;
            c.fill = GridBagConstraints.NONE;
            jp.add(startAlgo, c);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridy++;
            jp.add(new JLabel("Additional routing parameters"), c);

            params = new ParamTable(6);
            algo.addActionListener(new AlgoComboListener(params));
            fillCombo();
            c.gridy++;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.BOTH;
            jp.add(new JScrollPane(params) {
                public Dimension getPreferredSize() {
                    int width = super.getPreferredSize().width;
                    //int height = params.getPreferredSize().height + 50;
                    int height = params.getRowHeight() * 8;
                    return new Dimension(width, height);
                }
            }, c);

            accept = new JButton("Accept Parameters");
            accept.addActionListener(this);
            dialog.getRootPane().setDefaultButton(accept);
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weighty = 0.0;
            c.gridy++;
            jp.add(accept, c);
            return jp;
        }

        private void hide() {
            dialog = null;
            RepositoryManager.getInstance().removeListener(this);
            InterDomainManager.getInstance().removeListener(this);
        }

        private void fillCombo() {
            algo.removeAllItems();
            if (manager.getCurrentDomain() != null) {
                for (TotemAlgorithm a : RepositoryManager.getInstance().getAllStartedAlgos(manager.getCurrentDomain().getASID(), LSPPrimaryRouting.class)) {
                    algo.addItem(a);
                }
            }
        }

        /**
         * add the Lsp with current content of the window fields
         */
        void addLSP() {
            Float bw = null;
            try {
                bw = new Float(bwField.getText());
            } catch (Exception e) {
                mainWindow.errorMessage("Bandwidth Field must be a number");
                return;
            }

            if (algo.getSelectedItem() == null) {
                mainWindow.errorMessage("Please select an algorithm");
                return;
            }

            // stop cell editing
            if (params.getCellEditor() != null && !params.getCellEditor().stopCellEditing())
                return;

            HashMap<String, String> routingParams = params.toHashMap();

            String newLspId = lspId.getText().length() > 0 ? lspId.getText() : manager.getCurrentDomain().generateLspId();
            try {
                LSPPrimaryRoutingParameter param = new LSPPrimaryRoutingParameter((String) source.getSelectedItem(), (String) dest.getSelectedItem(), newLspId);
                param.setBandwidth(bw);

                if (diffServPanel.isExpanded()) {
                    DiffservPanel diffserv = (DiffservPanel)diffServPanel.getPanel();
                    param.setClassType(diffserv.getClassType());
                    param.setSetup(diffserv.getSetupLevel());
                    param.setHolding(diffserv.getHoldingLevel());
                }

                if (cosPanel.isExpanded()) {
                    param.setAcceptedCos(((ClassesOfServicePanel)cosPanel.getPanel()).getClassesOfService());
                }

                param.putAllRoutingAlgorithmParameter(routingParams);

                LSPPrimaryRouting routing = (LSPPrimaryRouting)algo.getSelectedItem();
                if (routing == null) {
                    mainWindow.errorMessage("Null algorithm.");
                    return;
                }

                TotemActionList acList = routing.routeLSP(manager.getCurrentDomain(), param);
                List<String> pList = null;
                for (Object o : acList) {
                    ((TotemAction)o).execute();
                    if (o instanceof PreemptLspsAction) {
                        pList = ((PreemptLspsAction)o).getLsps();
                    }
                }
                mainWindow.infoMessage("LSP \"" + newLspId + "\" added to current domain");
                if (pList != null && pList.size() > 0) {
                    String msg = "<html>Preempted LSPs:<br>";
                    for (String s : pList) {
                        msg += s + "<br>";
                    }
                    mainWindow.infoMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
                mainWindow.errorMessage(e);
                return;
            }
        }


        /**
         * Lisp for a click on the button
         *
         * @param e fired action
         */
        public void actionPerformed(ActionEvent e) {
            addLSP();
        }

        public void startAlgoEvent(TotemAlgorithm algo) {
            fillCombo();
        }

        public void stopAlgoEvent(TotemAlgorithm algo) {
            fillCombo();
        }

        public void addDomainEvent(Domain domain) {
        }

        public void removeDomainEvent(Domain domain) {
        }

        public void changeDefaultDomainEvent(Domain domain) {
            dialog.setContentPane(setupUI());
            dialog.pack();
        }
    }

    private class BackupLSPAdder implements ActionListener, RepositoryManagerListener, InterDomainManagerListener, DomainChangeListener {
        private JComboBox algo = null;
        private ParamTable params = null;
        private JComboBox lspsBox = null;
        private ButtonGroup methodTypeGroup = null;
        private ButtonGroup protectionTypeGroup = null;
        private JDialog dialog = null;
        private Domain currentDomain = null;
        private JTextField lspId = null;

        /**
         * Constructor
         */
        BackupLSPAdder() {
            currentDomain = manager.getCurrentDomain();
            if (currentDomain != null) currentDomain.getObserver().addListener(this);

            dialog = new JDialog(MainWindow.getInstance(), "Compute Detour LSP");
            dialog.setContentPane(setupUI());
            dialog.pack();
            dialog.setLocationRelativeTo(MainWindow.getInstance());
            dialog.setVisible(true);

            RepositoryManager.getInstance().addListener(this);
            InterDomainManager.getInstance().addListener(this);

            dialog.addWindowListener(new WindowListener() {
                public void windowOpened(WindowEvent e) {}
                public void windowClosing(WindowEvent e) {
                    hide();
                }
                public void windowClosed(WindowEvent e) {}
                public void windowIconified(WindowEvent e) {}
                public void windowDeiconified(WindowEvent e) {}
                public void windowActivated(WindowEvent e) {}
                public void windowDeactivated(WindowEvent e) {}
            });

            if (algo.getItemCount() > 0)
                algo.setSelectedIndex(0);
        }

        private JPanel setupUI() {
            JPanel jp = new JPanel();

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 2;
            c.gridheight = 1;
            c.weightx = 1.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 5, 5, 5);

            jp.setLayout(new GridBagLayout());

            jp.add(new JLabel("Lsp Id (leave blank to generate)."), c);
            c.gridy++;
            lspId = new JTextField();
            jp.add(lspId, c);
            c.gridy++;

            jp.add(new JLabel("Protected LSP"), c);
            lspsBox = new JComboBox();
            fillLspsCombo();
            c.gridy++;
            jp.add(lspsBox, c);

            JRadioButton btn;
            JPanel optPanel = new JPanel(new GridLayout(2, 1, 5, 5));
            optPanel.setBorder(BorderFactory.createTitledBorder("Detour type"));
            methodTypeGroup = new ButtonGroup();
            btn = new JRadioButton("Local");
            btn.setActionCommand("LOCAL");
            btn.setSelected(true);
            methodTypeGroup.add(btn);
            optPanel.add(btn);
            btn = new JRadioButton("Global");
            btn.setActionCommand("GLOBAL");
            methodTypeGroup.add(btn);
            optPanel.add(btn);
            c.gridy++;
            c.gridwidth = 1;
            c.fill = GridBagConstraints.BOTH;
            jp.add(optPanel, c);

            optPanel = new JPanel(new GridLayout(3, 1, 5, 5));
            optPanel.setBorder(BorderFactory.createTitledBorder("Protection type"));
            protectionTypeGroup = new ButtonGroup();
            btn = new JRadioButton("Node disjoint");
            btn.setActionCommand("NODE_DISJOINT");
            protectionTypeGroup.add(btn);
            optPanel.add(btn);
            btn = new JRadioButton("Link disjoint");
            btn.setActionCommand("LINK_DISJOINT");
            protectionTypeGroup.add(btn);
            optPanel.add(btn);
            btn = new JRadioButton("Default");
            btn.setActionCommand("ALGORITHM_DEFAULT");
            btn.setSelected(true);
            protectionTypeGroup.add(btn);
            optPanel.add(btn);
            c.gridx++;
            jp.add(optPanel, c);

            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = 2;
            c.gridx = 0;
            c.gridy++;
            jp.add(new JLabel("Algorithm"), c);
            algo = new JComboBox();
            algo.setRenderer(new ClassCellRenderer());
            c.gridy++;
            jp.add(algo, c);

            JButton startAlgo = new JButton("Start another algorithm...");
            startAlgo.addActionListener(new StartAlgoListener(dialog, LSPDetourRouting.class));
            c.gridy++;
            c.fill = GridBagConstraints.NONE;
            jp.add(startAlgo, c);

            c.gridy++;
            c.fill = GridBagConstraints.HORIZONTAL;
            jp.add(new JLabel("Additional routing parameters"), c);

            params = new ParamTable(6);
            algo.addActionListener(new DetourAlgoComboListener(params));
            fillCombo();
            c.gridy++;
            c.weighty = 1.0;
            c.fill = GridBagConstraints.BOTH;
            jp.add(new JScrollPane(params) {
                public Dimension getPreferredSize() {
                    int width = super.getPreferredSize().width;
                    //int height = params.getPreferredSize().height + 50;
                    int height = params.getRowHeight() * 8;
                    return new Dimension(width, height);
                }
            }, c);

            c.gridy++;
            c.weighty = 0.0;
            c.fill = GridBagConstraints.HORIZONTAL;
            JButton accept = new JButton("Accept Parameters");
            accept.addActionListener(this);
            jp.add(accept, c);
            dialog.getRootPane().setDefaultButton(accept);
            return jp;
        }

        private void addLsp() {
            if (lspsBox.getSelectedItem()  == null) {
                mainWindow.errorMessage("Please select the lsp to protect.");
                return;
            }

            if (algo.getSelectedItem() == null) {
                mainWindow.errorMessage("Please select an algorithm");
                return;
            }

            // stop cell editing
            if (params.getCellEditor() != null && !params.getCellEditor().stopCellEditing())
                return;

            HashMap<String, String> routingParams = params.toHashMap();

            String id = lspId.getText().length() > 0 ? lspId.getText() : null;

            List<String> lspIds = null;
            try {
                lspIds = addDetourLsp(id, (String)lspsBox.getSelectedItem(), methodTypeGroup.getSelection().getActionCommand(), protectionTypeGroup.getSelection().getActionCommand(), (LSPDetourRouting)algo.getSelectedItem(), routingParams);
            } catch (Exception e) {
                e.printStackTrace();
                mainWindow.errorMessage(e.getClass().getSimpleName() + ": " + e.getMessage());
                return;
            }
            String infoMessage = "Following LSPs will be added to current domain:";
            for (String i : lspIds) {
                infoMessage = infoMessage.concat("\n        ");
                infoMessage = infoMessage.concat(i);
            }
            mainWindow.infoMessage(infoMessage);
        }

        private void hide() {
            dialog = null;
            RepositoryManager.getInstance().removeListener(this);
            InterDomainManager.getInstance().removeListener(this);
        }

        private void fillCombo() {
            algo.removeAllItems();
            if (currentDomain != null) {
                for (TotemAlgorithm a : RepositoryManager.getInstance().getAllStartedAlgos(currentDomain.getASID(), LSPDetourRouting.class)) {
                    algo.addItem(a);
                }
            }
        }

        private void fillLspsCombo() {
            lspsBox.removeAllItems();
            if (currentDomain != null) {
                for (Lsp lsp : currentDomain.getAllLsps()) {
                    lspsBox.addItem(lsp.getId());
                }
            }
        }

        public void actionPerformed(ActionEvent e) {
            addLsp();
        }

        public void startAlgoEvent(TotemAlgorithm algo) {
            fillCombo();
        }

        public void stopAlgoEvent(TotemAlgorithm algo) {
            fillCombo();
        }

        public void addDomainEvent(Domain domain) {
        }

        public void removeDomainEvent(Domain domain) {
        }

        public void changeDefaultDomainEvent(Domain domain) {
            if (currentDomain != null)
                currentDomain.getObserver().removeListener(this);
            currentDomain = domain;
            if (domain != null)
                domain.getObserver().addListener(this);
            dialog.setContentPane(setupUI());
            dialog.validate();
        }

        public void addNodeEvent(Node node) {
        }

        public void removeNodeEvent(Node node) {
        }

        public void nodeStatusChangeEvent(Node node) {
        }

        public void nodeLocationChangeEvent(Node node) {
        }

        public void addLinkEvent(Link link) {
        }

        public void removeLinkEvent(Link link) {
        }

        public void linkStatusChangeEvent(Link link) {
        }

        public void linkMetricChangeEvent(Link link) {
        }

        public void linkTeMetricChangeEvent(Link link) {
        }

        public void linkBandwidthChangeEvent(Link link) {
        }

        public void linkReservedBandwidthChangeEvent(Link link) {
        }

        public void linkDelayChangeEvent(Link link) {
        }

        public void addLspEvent(Lsp lsp) {
            fillLspsCombo();
        }

        public void removeLspEvent(Lsp lsp) {
            fillLspsCombo();
        }

        public void lspReservationChangeEvent(Lsp lsp) {
        }

        public void lspWorkingPathChangeEvent(Lsp lsp) {
        }

        public void lspStatusChangeEvent(Lsp lsp) {
        }
    }

    /**
     * Listener that display the parameters in the given ParamTable, corresponding to the chosen algorithm in the comboBox
     */
    private class AlgoComboListener implements ActionListener {
        ParamTable table = null;

        /**
         *
         * @param update The Table in wich to display the algorithm parameter
         */
        public AlgoComboListener(ParamTable update) {
            this.table = update;
        }

        public void actionPerformed(ActionEvent e) {
            JComboBox combo = (JComboBox)e.getSource();

            if (combo.getSelectedItem() == null) {
                table.empty();
                return;
            }

            LSPPrimaryRouting routingAlgo = (LSPPrimaryRouting) combo.getSelectedItem();

            table.fill(routingAlgo.getPrimaryRoutingParameters());
        }
    }

    /**
     * Listener that display the parameters in the given ParamTable, corresponding to the chosen algorithm in the comboBox
     */
    private class DetourAlgoComboListener implements ActionListener {
        ParamTable table = null;

        /**
         *
         * @param update The Table in wich to display the algorithm parameter
         */
        public DetourAlgoComboListener(ParamTable update) {
            this.table = update;
        }

        public void actionPerformed(ActionEvent e) {
            JComboBox combo = (JComboBox)e.getSource();

            if (combo.getSelectedItem() == null) {
                table.empty();
                return;
            }

            LSPDetourRouting routingAlgo = (LSPDetourRouting) combo.getSelectedItem();

            table.fill(routingAlgo.getDetourRoutingParameters());
        }
    }


    /**
     * When the parent dialog is closed, refresh the combo with the started routing algorithms of the current domain.
     */
    private class StartAlgoListener implements ActionListener {
        JDialog parent = null;
        Class filter = null;

        public StartAlgoListener(JDialog parent, Class filter) {
            this.parent = parent;
            this.filter = filter;
        }

        public void actionPerformed(ActionEvent e) {
            JDialog startAlgoDlg = new JDialog(parent, "Start Routing Algo ...", true);
            startAlgoDlg.setContentPane(new StartAlgoPanel(filter));
            startAlgoDlg.pack();
            startAlgoDlg.setLocationRelativeTo(parent);
            startAlgoDlg.setVisible(true);
        }
    }
}
