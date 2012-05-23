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

import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.ModuleLoader;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.LegendEditor;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.GraphManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.scenario.ScenarioExecutor;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.domainTables.*;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.chart.AddSeriesPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.chart.NewChartPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.chart.PlotterPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.stats.NetworkStatTabbedPane;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.domainTables.LoadTables;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManagerListener;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.DomainAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.chart.model.exception.NoSuchChartException;
import be.ac.ulg.montefiore.run.totem.chart.facade.ChartManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManagerListener;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.scenario.model.Scenario;
import be.ac.ulg.montefiore.run.totem.scenario.persistence.ScenarioFactory;
import be.ac.ulg.montefiore.run.totem.socketInterface.Server;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import uy.edu.fing.repository.tools.CBGPDump.CBGPDumpAlgorithm;

/*
 * Changes:
 * --------
 *  8-Dec-2005 : add application icon, Domain Manager, Algo Manager, Start Algo Panel. (GMO)
 *  8-Dec-2005 : correct multiple instance window bug by adding synchronized. (GMO)
 *  8-Dec-2005 : add infoMessage() to display a message. (GMO)
 * 13-Dec-2005 : add the Save as Image capability, reorganize the menus, add the traffic matrix manager which was
 *                 in the routing module (GMO)
 * 13-Dec-2005 : upgrade Domains Menu. (GMO)
 * 23-Dec-2005 : add the Charts Menu. (GMO)
 * 12-Jan-2006 : add a Console that display standard and error output in a textArea, add the scenario menu to execute
 *   scenario, change the (node, link, lsp, tm) tables, remove useless repaint() calls, now the windw react to change
 *   in the InterDomainManager.
 * 25-Jan-2006 : change the Charts menu to manage charts in a flexible manner. (GMO)
 * 03-Feb-2006 : add menu to the tabbed pane so we can close matrices from there. (GMO)
 * 20-Mar-2006 : add OneTouchExpedable button to splitPanes (GMO).
 * 20-Mar-2006 : now use LegendEditor to choose colors (GMO).
 * 23-Oct-2006 : add start and stop server in scenario menu (GMO)
 * 23-Oct-2006 : add Load topo dialog (GMO)
 * 24-Oct-2006 : can change link colors if no domain is loaded (GMO)
 * 22-Nov-2006 : application can be started even if icon is not found. (GMO)
 * 10-Jan-2007 : add "Load Tables" element under View menu (GMO)
 * 10-Jan-2007 : change scenario executor dialog title (GMO)
 * 06-Mar-2007 : fix bug when showing start algo dialog (GMO)
 * 13-Mar-2007 : replace addDialog with showDialog method (fix display bug) (GMO)
 * 23-Apr-2007 : add create traffic matrix capability (GMO)
 * 25-Apr-2007 : add a showDialog(.) method (GMO)
 * 03-May-2007 : destroy load tables on dialog close (GMO)
 * 01-Jun-2007 : change keyboard shortcuts (GMO)
 * 01-Jun-2007 : add set as default to the matrix menu (GMO)
 * 21-Jun-2007 : Move HostPortDialog inner class to public class of package visualtopo.guiComponents (GMO)
 * 28-Jun-2007 : Add errorMessage(Exception) (GMO)
 * 30-Oct-2007 : Add traffic matrix generation capability using TrafficGeneratorInterface (GMO)
 * 15-Jan-2007 : Add Link Info dialog (GMO)
 */

/**
 * A window that contains all graphic elements of the application.
 * <p/>
 * <p>Creation date: 15-Feb-2005
 *
 * @author Olivier Materne (O.Materne@student.ulg.ac.be)
 */
public class MainWindow extends JFrame implements InterDomainManagerListener {

    static final String ACTION_LOAD = "Load Topology";
    static final String ACTION_DISTANT_LOAD = "Load Topology from network";
    static final String ACTION_SAVE = "Save Topology as ...";
    static final String ACTION_SAVE_VISUAL = "Save Visualization as Image ...";
    static final String ACTION_EXPORT_CBGP = "Export to C-BGP script";
    static final String ACTION_CLOSE = "Close Topology";
    static final String ACTION_EXIT = "Exit";
    static final String ACTION_LIST_NODES = "Nodes List";
    static final String ACTION_LIST_LINKS = "Links List";
    static final String ACTION_LIST_LSP = "LSP List";
    static final String ACTION_NET_STATS = "Network Stats";
    static final String ACTION_VIEW_LOADTABLES = "Load Tables";
    static final String ACTION_VIEW_LINKINFO = "Link Info";

    public static final String CIRCLELAYOUT = "CircleLayout";
    public static final String FRLAYOUT = "FRLayout";
    public static final String ISOMLAYOUT = "ISOMLayout";
    public static final String KKLAYOUT = "KKLayout";
    public static final String SPRINGLAYOUT = "SpringLayout";
    public static final String LOCALLAYOUT = "LocalLayout";
    static final String ACTION_CHOOSE_COLORS = "Choose Link Colors";

    static final String ACTION_START_ALGO = "Start...";
    static final String ACTION_VIEW_DOMAINS = "Domain Manager";
    static final String ACTION_VIEW_ALGOS = "Algorithm Manager";
    static final String ACTION_VIEW_MATRIX = "Traffic Matrix Manager";
    static final String ACTION_LOAD_DEMANDMATRIX = "Load Traffic Matrix";
    static final String ACTION_GENERATE_TM = "Generate...";
    static final String ACTION_CREATE_MATRIX = "Create Traffic Matrix";
    static final String ACTION_EXECUTE_SCENARIO = "Execute Scenario";
    static final String ACTION_START_SERVER = "Start Server";
    static final String ACTION_STOP_SERVER = "Stop Server";

    static private final Image APP_ICON;

    static {
        Image tmp = null;
        try {
            tmp = new ImageIcon(MainWindow.class.getResource("/resources/img/icon.gif")).getImage();
        } catch (NullPointerException e) {
        }
        APP_ICON = tmp;
    }

    static private MainWindow mainWindow = null;
    static private GUIManager manager = null;
    static private ModuleLoader moduleLoader = null;
    static private java.util.List<JMenuItem> buttonList = null;

    static private GraphManager topoPanel = null;
    static private OptionsPanel optionsPanel = null;
    static private THistory history = null;
    static private DomainTabbedPane tab = null;

    static private JMenu domainsMenu = null;
    static private JMenu chartsMenu = null;

    /**
     * Constructor
     */
    private MainWindow() {

        super(GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice().getDefaultConfiguration());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                initUI();
            }
        });
    }


    /**
     * Returns a single instance of the MainWindow
     *
     * @return returns a single instance of the MainWindow
     */
    public static synchronized MainWindow getInstance() {
        if (mainWindow == null) {
            mainWindow = new MainWindow();
        }
        return mainWindow;
    }


    /**
     * A simple method that is called by the constructor to initialise the different
     * components of the MainWindow such as menus, panels, ...
     */
    private void initUI() {

        //set up main window
        manager = GUIManager.getInstance();

        moduleLoader = ModuleLoader.getInstance();
        setTitle("ToTem Project");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowCloser());
        setJMenuBar(this.generateMenu());
        setIconImage(APP_ICON);

        //initialise main layout
        final JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerSize(10);
        splitPane1.setOneTouchExpandable(true);
        splitPane1.setOrientation(0);
        splitPane1.setResizeWeight(0.82);
        final JSplitPane splitPane2 = new JSplitPane();
        splitPane2.setDividerSize(10);
        splitPane2.setOneTouchExpandable(true);
        splitPane2.setResizeWeight(0.18);
        splitPane1.setLeftComponent(splitPane2);
        getContentPane().add(splitPane1);

        //initialise the main panel (which will contains topology graph)
        topoPanel = GraphManager.getInstance();
        splitPane2.setRightComponent(topoPanel);

        //initialise right option panel
        Dimension zeroDimension = new Dimension(0, 0);
        optionsPanel = new OptionsPanel();
        optionsPanel.setMinimumSize(zeroDimension);
        splitPane2.setLeftComponent(optionsPanel);

        //initialise history panel
        history = new THistory("Welcome !!\n\n");
        JScrollPane histPanel = new JScrollPane(history);

        //initialise bottom panel (lsp + links + ...)
        tab = new DomainTabbedPane();
        tab.setPreferredSize(new Dimension(tab.getPreferredSize().width, 150));
        splitPane1.setRightComponent(tab);

        //show the application
        history.append("ToTem GUI started.");
        pack();
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setVisible(true);
        moduleLoader.initBasicModules();

        InterDomainManager.getInstance().addListener(this);
    }

    GraphManager getTopoPanel() {
        return topoPanel;
    }

    public NodeDomainTable getNodeTable() {
        return tab.getNodeTable();
    }

    public LinkDomainTable getLinkTable() {
        return tab.getLinkTable();
    }

    public LspDomainTable getLspTable() {
        return tab.getLspTable();
    }

    public OptionsPanel getOptionsPanel() {
        return optionsPanel;
    }

    public void saveVisualization() {
        ImageChooser imgChooser = new ImageChooser();
        File outFile = null;
        BufferedImage img = null;

        // pop up the file chooser
        outFile = imgChooser.saveImage(this);

        if (outFile == null) {
           //errorMessage("Impossible to save the file.");
           return;
        }

        // find the file format from selected filter, default to png
        String filter = imgChooser.getFileFilter().getDescription();
        String format = null;
        if (filter.startsWith("*."))
            format = filter.substring(2);
        else {
            //logger.warn("No file format specified, saving as png");
            format = "png";
        }

        // append the extension to the file name, if doesn't exist
        String fileName = outFile.getName();
        int index = fileName.lastIndexOf('.') + 1;
        if (index < 0 || !fileName.substring(index).toLowerCase().equals(format.toLowerCase())) {
            outFile = new File(outFile.getAbsolutePath() + "." + format);
        }


        // check for file existence
        if (outFile.exists()) {
            String message = "The file " + outFile.getAbsolutePath() + " Already exists.\n" +
                    "Do you want to overwrite it?";
            int choice = JOptionPane.showOptionDialog(this, message, "File exists", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
            if (choice != JOptionPane.YES_OPTION) return;
        }

        //get image from graph manager
        img = GraphManager.getInstance().getImage();

        // write image to file
        try {
            ImageIO.write(img, format, outFile);
        } catch (Exception e) {
            errorMessage("Impossible to save the file: " + e.getMessage());
            return;
        }

        // check result
        if (outFile.exists())
            infoMessage("File saved as: " + outFile.getAbsolutePath());
        else errorMessage("Undefined error. File not saved");
    }


    public void setLinkLabel(int label) {
        optionsPanel.labellerChooser.setSelectedIndex(label);
        //LinkLabeller.getInstance().select(label);
    }

    /**
     * A method that generate the menubar for this application
     *
     * @return returns the menubar for the main window
     */
    private JMenuBar generateMenu() {
        JMenuItem menuItem;
        TMenuBar mbar = new TMenuBar();
        KeyStroke ks;
        buttonList = new LinkedList<JMenuItem>();

        //Create the menu "File"
        mbar.addMenu("File", KeyEvent.VK_F);

        ks = KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK);
        mbar.addMenuItem(new JMenuItem(ACTION_LOAD), ks, new LoadTopologyListener());

        menuItem = new JMenuItem(ACTION_DISTANT_LOAD);
        mbar.addMenuItem(menuItem, null, new LoadTopoFromNetworkListener());

        menuItem = new JMenuItem(ACTION_SAVE);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK);
        mbar.addMenuItem(menuItem, ks, new SaveTopologyListener());
        menuItem.setEnabled(false);
        buttonList.add(menuItem);

        menuItem = new JMenuItem(ACTION_SAVE_VISUAL);
        mbar.addMenuItem(menuItem, null, new SaveVisualListener());
        menuItem.setEnabled(false);
        buttonList.add(menuItem);
        
        menuItem = new JMenuItem(ACTION_EXPORT_CBGP);
        mbar.addMenuItem(menuItem, null, new ExportCbgpListener());
        menuItem.setEnabled(false);
        buttonList.add(menuItem);

        menuItem = new JMenuItem(ACTION_CLOSE);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK);
        mbar.addMenuItem(menuItem, ks, new CloseTopologyListener());
        menuItem.setEnabled(false);
        buttonList.add(menuItem);

        mbar.getCurrentMenu().addSeparator();
        menuItem = new JMenuItem("Load Module");
        mbar.addMenuItem(menuItem, null, new AddModuleListener());

        mbar.getCurrentMenu().addSeparator();
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK);
        mbar.addMenuItem(new JMenuItem(ACTION_EXIT), ks, new ExitListener());


        //Create the menu "View"
        mbar.addMenu("View", KeyEvent.VK_V);

        menuItem = new JMenuItem(ACTION_LIST_NODES);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        mbar.addMenuItem(menuItem, ks, new ShowNodesListListener());
        menuItem.setEnabled(false);
        buttonList.add(menuItem);

        menuItem = new JMenuItem(ACTION_LIST_LINKS);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0);
        mbar.addMenuItem(menuItem, ks, new ShowLinksListListener());
        menuItem.setEnabled(false);
        buttonList.add(menuItem);

        menuItem = new JMenuItem(ACTION_LIST_LSP);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0);
        mbar.addMenuItem(menuItem, ks, new ShowLspListListener());
        menuItem.setEnabled(false);
        buttonList.add(menuItem);

        mbar.getCurrentMenu().addSeparator();

        menuItem = new JMenuItem(ACTION_NET_STATS);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
        mbar.addMenuItem(menuItem, ks, new ShowStatsListListener());
        menuItem.setEnabled(false);
        buttonList.add(menuItem);

        menuItem = new JMenuItem(ACTION_VIEW_LOADTABLES);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0);
        mbar.addMenuItem(menuItem, ks, new ShowLoadTable());
        menuItem.setEnabled(false);
        buttonList.add(menuItem);

        menuItem = new JMenuItem(ACTION_VIEW_LINKINFO);
        mbar.addMenuItem(menuItem, null, new ViewLinkInfo());
        menuItem.setEnabled(false);
        buttonList.add(menuItem);

        mbar.getCurrentMenu().addSeparator();

        JMenu subMenu = new JMenu("Layout");
        subMenu.setEnabled(false);
        ButtonGroup group = new ButtonGroup();
        buttonList.add(subMenu);
        ActionListener layoutListener = new LayoutSelectionListener();

        menuItem = new JMenuItem(LOCALLAYOUT);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F1, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        menuItem.setAccelerator(ks);
        menuItem.addActionListener(layoutListener);
        group.add(menuItem);
        subMenu.add(menuItem);

        menuItem = new JMenuItem(CIRCLELAYOUT);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F2, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        menuItem.setAccelerator(ks);
        menuItem.addActionListener(layoutListener);
        group.add(menuItem);
        subMenu.add(menuItem);

        menuItem = new JMenuItem(FRLAYOUT);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F3, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        menuItem.setAccelerator(ks);
        menuItem.addActionListener(layoutListener);
        group.add(menuItem);
        subMenu.add(menuItem);

        menuItem = new JMenuItem(KKLAYOUT);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        menuItem.setAccelerator(ks);
        menuItem.addActionListener(layoutListener);
        group.add(menuItem);
        subMenu.add(menuItem);

        menuItem = new JMenuItem(ISOMLAYOUT);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F5, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        menuItem.setAccelerator(ks);
        menuItem.addActionListener(layoutListener);
        group.add(menuItem);
        subMenu.add(menuItem);

        menuItem = new JMenuItem(ACTION_CHOOSE_COLORS);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK);
        mbar.addMenuItem(menuItem, ks, new ChooseColorsListener());

        mbar.getCurrentMenu().add(subMenu);

        mbar.getCurrentMenu().addSeparator();
        menuItem = new JMenuItem("Console");
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0);
        mbar.addMenuItem(menuItem, ks, new ConsoleActionListener());

        mbar.addMenu("Domains", KeyEvent.VK_D);

        domainsMenu = mbar.getCurrentMenu();
        buildDomainsMenu();

        mbar.addMenu("Algorithms", KeyEvent.VK_A);
        menuItem = new JMenuItem(ACTION_START_ALGO);
        mbar.addMenuItem(menuItem, null, new StartAlgoListener());
        //menuItem.setEnabled(false);
        //buttonList.add(menuItem);

        mbar.getCurrentMenu().addSeparator();
        menuItem = new JMenuItem(ACTION_VIEW_ALGOS);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0);
        mbar.addMenuItem(menuItem, ks, new ViewAlgosListener());

        mbar.addMenu("TrafficMatrix", KeyEvent.VK_T);

        menuItem = new JMenuItem(ACTION_CREATE_MATRIX);
        mbar.addMenuItem(menuItem, null, new CreateTMListener());
        menuItem.setEnabled(false);
        buttonList.add(menuItem);

        menuItem = new JMenuItem(ACTION_LOAD_DEMANDMATRIX);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK);
        mbar.addMenuItem(menuItem, ks, new LoadTrafficMatrixListener());
        menuItem.setEnabled(false);
        buttonList.add(menuItem);

        menuItem = new JMenuItem(ACTION_GENERATE_TM);
        menuItem.setEnabled(false);
        buttonList.add(menuItem);
        mbar.addMenuItem(menuItem, null, new GenerateTMActionListener());

        mbar.getCurrentMenu().addSeparator();

        menuItem = new JMenuItem(ACTION_VIEW_MATRIX);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
        mbar.addMenuItem(menuItem, ks, new SelectTMListener());

        mbar.addMenu("Scenario", KeyEvent.VK_C);

        menuItem = new JMenuItem(ACTION_EXECUTE_SCENARIO);
        ks = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
        mbar.addMenuItem(menuItem, ks, new ScenarioActionListener());

        mbar.getCurrentMenu().addSeparator();
        menuItem = new JMenuItem(ACTION_START_SERVER);
        mbar.addMenuItem(menuItem, null, new StartServerActionListener());
        menuItem = new JMenuItem(ACTION_STOP_SERVER);
        //menuItem.setEnabled(false);
        mbar.addMenuItem(menuItem, null, new StopServerActionListener());

        // Charts Menu
        mbar.addMenu("Charts", KeyEvent.VK_H);
        chartsMenu = mbar.getCurrentMenu();
        updateChartMenu();

        return (JMenuBar) mbar;
    }

    public void addDomainEvent(Domain domain) {
        buildDomainsMenu();
    }

    public void removeDomainEvent(Domain domain) {
        buildDomainsMenu();
    }

    public void changeDefaultDomainEvent(Domain domain) {
        if (domain == null) {
            setButtonsEnabled(false);
        }
        else {
            setButtonsEnabled(true);
        }
        buildDomainsMenu();
    }

    /**
     * Rebuild the Domains menu contained in domainsMenu,
     * if domainsMenu is null, do nothing.
     * Domains Menu is dynamic and contains all the loaded domains.
     * Use this function to rebuild the menu after a change in the domains (domain loaded, domain unloaded, change default domain)
     */
    private void buildDomainsMenu() {
        if (domainsMenu == null) {
            return;
        }
        domainsMenu.removeAll();
        JRadioButtonMenuItem checkItem;
        for (Domain domain : InterDomainManager.getInstance().getAllDomains()) {
            checkItem = new JRadioButtonMenuItem(domain.getURI().toString());
            checkItem.addActionListener(new SelectDomainListener(domain.getASID()));
            if (domain == InterDomainManager.getInstance().getDefaultDomain())
                checkItem.setSelected(true);
            domainsMenu.add(checkItem);
        }

        JMenuItem menuItem;
        domainsMenu.addSeparator();
        menuItem = new JMenuItem(ACTION_VIEW_DOMAINS);
        KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0);
        menuItem.setAccelerator(ks);
        menuItem.addActionListener(new ViewDomainsListener());
        domainsMenu.add(menuItem);

    }

    public void updateChartMenu() {
        if (chartsMenu == null) {
            return;
        }
        chartsMenu.removeAll();
        JMenuItem menuItem = new JMenuItem("New chart ...");
        menuItem.addActionListener(new NewChartActionListener());
        chartsMenu.add(menuItem);
        chartsMenu.addSeparator();


        JMenuItem subMenuItem;
        for (String chart : ChartManager.getInstance().getAllChartsName()) {
            JMenu subMenu = new JMenu(chart);
            subMenuItem = new JMenuItem("Add series...");
            subMenuItem.addActionListener(new AddSeriesListener(chart));
            subMenu.add(subMenuItem);
            subMenuItem = new JMenuItem("Plot...");
            subMenuItem.addActionListener(new PlotChartListener(chart));
            subMenu.add(subMenuItem);

            subMenu.addSeparator();

            subMenuItem = new JMenuItem("View last plot");
            subMenu.add(subMenuItem);
            subMenuItem.addActionListener(new ViewChartListener(chart));
            try {
                if (ChartManager.getInstance().getChart(chart).getPlot() == null) {
                    subMenuItem.setEnabled(false);
                }
            } catch (NoSuchChartException e) {
                e.printStackTrace();
            }
            subMenuItem = new JMenuItem("Remove chart");
            subMenuItem.addActionListener(new RemoveChartListener(chart));
            subMenu.add(subMenuItem);

            //menuItem.add(subMenu);
            chartsMenu.add(subMenu);
        }

    }

    /**
     * Add a menu to the JMenuBar for this MainWindow
     *
     * @param menu
     */
    public void addMenu(JMenu menu) {
        if (mainWindow.getJMenuBar() != null) {
            mainWindow.getJMenuBar().add(menu);
            mainWindow.getJMenuBar().updateUI();
        }
    }

    /**
     * A method to enable the disabled buttons of the menubar.
     */
    private void setButtonsEnabled(boolean enabled) {
        if (buttonList != null)
            for (Iterator iter = buttonList.listIterator(); iter.hasNext();)
                ((JMenuItem) iter.next()).setEnabled(enabled);
    }

    public JDialog showDialog(JComponent content, String title) {
        return showDialog(content, title, false);
    }

    /**
     * Show a new dialog, centered relative to this window.
     * @param content the content of the dialog
     * @param title the window title
     * @param modal
     * @return the newly create dialog
     */
    public JDialog showDialog(JComponent content, String title, boolean modal) {
        JDialog dialog = new JDialog(this, title, modal);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return dialog;
    }

    public void showDialog(JDialog dialog) {
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * This method show a Error Message JDialog on screen with the specified Error Message.
     *
     * @param msg the message to be shown
     */
    public void errorMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        //repaint();
    }

    public void errorMessage(Exception e) {
        String msg = e.getClass().getSimpleName();

        if (e.getMessage() != null) {
            msg += ": " + e.getMessage();
        }

        if (e.getCause() != null) {
            msg += "\nCaused by: " + e.getCause().getMessage();
        }

        errorMessage(msg);
    }

    public void infoMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Info", JOptionPane.INFORMATION_MESSAGE);
        //repaint();
    }


    /**
     * This method pop up a Confirm JDialog on screen and exit the application if requested
     * by the user.
     */
    void exit() {
        int n = JOptionPane.showConfirmDialog(this, "Do you really want to quit ?", "Exit",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (n == JOptionPane.YES_OPTION)
            System.exit(1);
    }



    /**
     * A simple class that listen for Window events. It the same behaviour as the
     * WindowAdapter class, exept for the windowClosing method that has been rewrited.
     *
     * @author Olivier Materne
     */
    class WindowCloser extends WindowAdapter {


        /**
         * A rewriting of the windowClosing method so that a confiramtion is asked when
         * this method is called from an instance of MainWindow.
         * windowClosing is the method that is called when the cross at the top of a window
         * is clicked.
         *
         * @param e the fired event
         */
        public void windowClosing(WindowEvent e) {
            /*if (e.getWindow() instanceof MainWindow)
                ((MainWindow) e.getWindow()).exit();
            else*/
                System.exit(1);
        }
    }


    /**
     * An extention of JText Area used to keep a mention of past events
     */
    private class THistory extends JTextArea {

        THistory(String s) {
            super(s);
            configure();
        }

        private void configure() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setEditable(false);
        }


        /**
         * Create a new line in the THistory with the time and the specified String s.
         *
         * @param s the new entry to add
         */
        public void append(String s) {
            super.append("[" + Calendar.getInstance().getTime().toString() + "] ");//
            super.append(s + "\n");
        }

    }

    private class DomainTabbedPane extends JTabbedPane implements InterDomainManagerListener, TrafficMatrixManagerListener {
        NodeDomainTable nodes;
        LinkDomainTable links;
        LspDomainTable lsps;
        List<TrafficMatrixTable> matrices = null;

        DomainTabbedPane() {
            super();
            add("Nodes", new JScrollPane((nodes = new NodeDomainTable())));
            add("Links", new JScrollPane((links = new LinkDomainTable())));
            add("Lsps", new JScrollPane((lsps = new LspDomainTable())));
            InterDomainManager.getInstance().addListener(this);
            TrafficMatrixManager.getInstance().addListener(this);
            addTrafficMatrices();
            addMouseListener(new MatrixMenuListener());
        }

        public NodeDomainTable getNodeTable() {
            return nodes;
        }

        public LinkDomainTable getLinkTable() {
            return links;
        }

        public LspDomainTable getLspTable() {
            return lsps;
        }

        public void addDomainEvent(Domain domain) {
        }

        public void removeDomainEvent(Domain domain) {
        }

        public void changeDefaultDomainEvent(Domain domain) {
            nodes.setDomain(domain);
            links.setDomain(domain);
            lsps.setDomain(domain);
            for (int i=0; i < matrices.size(); i++) {
                //remove the first traffic matrix tab (4th tab)
                remove(3);
            }
            addTrafficMatrices();
        }

        private void addTrafficMatrices() {
            matrices = new ArrayList<TrafficMatrixTable>();
            if (GUIManager.getInstance().getCurrentDomain() == null) return;
            int asId = GUIManager.getInstance().getCurrentDomain().getASID();
            for (int tmId : GUIManager.getInstance().getManagedMatrices()) {
                String def = "";
                try {
                    TrafficMatrix tm = TrafficMatrixManager.getInstance().getTrafficMatrix(asId, tmId);
                    TrafficMatrixTable tmt = new TrafficMatrixTable(tm, tmId);
                    if (TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID(asId) == tmId) {
                        def = "*";
                    }
                    add("TrafficMatrix" + def, new JScrollPane(tmt));
                    matrices.add(tmt);
                } catch (InvalidTrafficMatrixException e) {
                    //e.printStackTrace();
                }
            }
        }

        public void addTrafficMatrixEvent(TrafficMatrix tm, int tmId) {
            if (GUIManager.getInstance().getCurrentDomain() != null && tm.getASID() == GUIManager.getInstance().getCurrentDomain().getASID()) {
                TrafficMatrixTable tmt = new TrafficMatrixTable(tm, tmId);
                add("TrafficMatrix", new JScrollPane(tmt));
                matrices.add(tmt);
            }
        }

        public void removeTrafficMatrixEvent(TrafficMatrix tm, int tmId) {
            int i = 0;
            for (TrafficMatrixTable tmt : matrices) {
                if (tmt.getTM() == tm) {
                    tmt.destroy();
                    remove(i+3);
                    matrices.remove(tmt);
                    break;
                }
                i++;
            }
        }

        public void changeDefaultTrafficMatrixEvent(int asId, TrafficMatrix tm) {
            if (GUIManager.getInstance().getCurrentDomain() == null || GUIManager.getInstance().getCurrentDomain().getASID() != asId) {
                return;
            }
           int i = 0;
           for (TrafficMatrixTable tmt : matrices) {
               if (tmt.getTM() == tm) {
                   setTitleAt(i+3 ,"TrafficMatrix*");
               }
               else setTitleAt(i+3, "TrafficMatrix");
               i++;
           }
        }

        private class MatrixMenuListener extends MouseAdapter {
            private final JPopupMenu menu;
            private int matrixIndex = 0;

            public MatrixMenuListener() {
                JMenuItem menuItem;

                menu = new JPopupMenu();

                menuItem = new JMenuItem("Close matrix");
                menuItem.addActionListener(new CloseMatrixAction());
                menu.add(menuItem);

                menuItem = new JMenuItem("Set as default");
                menuItem.addActionListener(new SetAsDefaultMatrixAction());
                menu.add(menuItem);

            }

            public void mousePressed(MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    int index = indexAtLocation(evt.getX(), evt.getY());
                    if (index >= 3) {
                        matrixIndex = index-3;
                        menu.show(evt.getComponent(), evt.getX(), evt.getY());
                    }
                }
            }

            private class CloseMatrixAction implements ActionListener {
                public void actionPerformed(ActionEvent e) {
                    int asId = matrices.get(matrixIndex).getTM().getASID();
                    int tmId = matrices.get(matrixIndex).getTMId();
                    try {
                        TrafficMatrixManager.getInstance().removeTrafficMatrix(asId, tmId);
                    } catch (InvalidTrafficMatrixException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            private class SetAsDefaultMatrixAction implements ActionListener {
                public void actionPerformed(ActionEvent e) {
                    int asId = matrices.get(matrixIndex).getTM().getASID();
                    int tmId = matrices.get(matrixIndex).getTMId();
                    try {
                        TrafficMatrixManager.getInstance().setDefaultTrafficMatrix(asId, tmId);
                    } catch (InvalidTrafficMatrixException e1) {
                        e1.printStackTrace();
                    }
                }
            }

        }
    }

    /********************************************************************************************************
     *
     *       ACTIONLISTENERS
     *
     ********************************************************************************************************/


    /**
     * The Listener called when the user decide to load a new topology
     */
    class LoadTopologyListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JDialog dialog = new LoadTopoDialog();
            dialog.setLocationRelativeTo(getInstance());
            dialog.setVisible(true);
        }
    }

    private class LoadTopoFromNetworkListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            HostPortDialog dialog = new HostPortDialog();
            dialog.setActionListener(new LoadDomainConnectActionListener(dialog));
            showDialog(dialog);
        }
    }

    private class LoadDomainConnectActionListener implements ActionListener {

        private HostPortDialog dialog;

        public LoadDomainConnectActionListener(HostPortDialog dialog) {
            this.dialog = dialog;
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            String host = String.valueOf(dialog.getHost());

            int port;
            try {
                port = dialog.getPort();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(getInstance(), "Port must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                InterDomainManager.getInstance().loadDomain(host, port, true, false, false);
                dialog.dispose();
            } catch (InvalidDomainException e1) {
                JOptionPane.showMessageDialog(getInstance(), "Domain cannot be loaded : Invalid Domain.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(getInstance(), "Cannot connect to server, an IOException occurred:\nError: " + e1.getClass().getSimpleName() + "\nMsg: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (DomainAlreadyExistException e1) {
                JOptionPane.showMessageDialog(getInstance(), "Domain cannot be loaded : Domain is already loaded.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    /**
     * The Listener called when the user decide to save the current topology
     */
    class SaveTopologyListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            manager.saveTopo();
            //repaint();
        }
    }

    class SaveVisualListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            saveVisualization();
        }
    }

    
    class ExportCbgpListener implements ActionListener 
    {

        public void actionPerformed(ActionEvent e) 
        {
        	JFileChooser fc = new JFileChooser();
            int returnVal = fc.showSaveDialog(MainWindow.getInstance());

            if (returnVal == JFileChooser.APPROVE_OPTION) 
            {
                File file = fc.getSelectedFile();
                String filename = file.getAbsolutePath();
                if (!filename.toLowerCase().endsWith(".cli")) 
                {
                    filename = filename.concat(".cli");
                }
                
                System.out.println(filename);
                
                CBGPDumpAlgorithm alg = new CBGPDumpAlgorithm();
                alg.run(filename);
            }
        }
    }
    
    class CloseTopologyListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null)
                manager.closeDomain(manager.getCurrentDomain().getASID());
        }
    }


    /**
     * The Listener called when Exit Button is pressed
     */
    class ExitListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            mainWindow.exit();
        }
    }


    /**
     * Called when the user wants to change the module configuration.
     */
    class AddModuleListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            moduleLoader.showModuleLoaderDialog();
        }
    }


    /**
     * The listener used when a user wants to load a TrafficMatrix
     */
    class LoadTrafficMatrixListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            File mf = (new TopoChooser()).loadMatrix((Container) mainWindow);
            if (mf == null) //cancel button has been pressed
                return;
            manager.addDemandMatrix(mf);
            //repaint();
        }
    }

    class GenerateTMActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null) {
                JDialog dialog = new GenerateTMDialog();
                showDialog(dialog);
            } else {
                JOptionPane.showMessageDialog(mainWindow, "A domain must be loaded to perform this action.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    /**
     * The listener called when a user wants to see a Node Table
     */
    class ShowNodesListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JScrollPane nodes = new JScrollPane(new NodeDomainTable());
            nodes.setPreferredSize(new Dimension(600, 200));
            mainWindow.showDialog(nodes, "Nodes Table");
        }
    }


    /**
     * The listener called when a user wants to see a Links Table
     */
    class ShowLinksListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JScrollPane links = new JScrollPane(new LinkDomainTable());
            links.setPreferredSize(new Dimension(600, 200));
            mainWindow.showDialog(links, "Links Table");
        }
    }


    /**
     * The listener called when a user wants to see a Lsp Table
     */
    class ShowLspListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JScrollPane lsps = new JScrollPane(new LspDomainTable());
            lsps.setPreferredSize(new Dimension(600, 200));
            mainWindow.showDialog(lsps, "LSPs Table");
        }
    }


    /**
     * The listener called when a user wants to see a NetworkStats Table
     */
    class ShowStatsListListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            NetworkStatTabbedPane pane = new NetworkStatTabbedPane(manager.getCurrentDomain());
            pane.setPreferredSize(new Dimension(600, 200));
            mainWindow.showDialog(pane, "Stats Table");
        }
    }

    class ChooseColorsListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            new LegendEditor().chooseColors();
        }
    }


    /**
     * The Listener called when the user wants to change the domain layout
     */
    class LayoutSelectionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            final ActionEvent ev = e;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    topoPanel.changeLayout(ev.getActionCommand());
                    //mainWindow.repaint();
                }
            });
        }
    }

    class StartAlgoListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            showDialog(new StartAlgoPanel(), "Start Algo ...");
        }
    }
    
    class ViewDomainsListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		DomainHandler.getInstance().show();
    	}
    }

    class SelectDomainListener implements ActionListener {
        private int asId;

        public SelectDomainListener(int asId) {
            this.asId = asId;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                InterDomainManager.getInstance().setDefaultDomain(asId);
            } catch (InvalidDomainException e1) {
                e1.printStackTrace();
            }
        }
    }

    class ViewAlgosListener implements ActionListener {
    	public void actionPerformed(ActionEvent e) {
    		AlgorithmHandler.getInstance().show();
    	}
    }

    /**
     * A Listener to display the Traffic matrix management panel
     */
    public class SelectTMListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            RMguiMatrixHandler.getInstance().show();
        }
    }

    public class CreateTMListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (manager.getCurrentDomain() != null) {
                try {
                    TrafficMatrix tm = TrafficMatrixManager.getInstance().createEmptyTrafficMatrix(manager.getCurrentDomain().getASID());
                    RMguiMatrixHandler.getInstance().editTrafficMatrix(tm);
                    return;
                } catch (InvalidDomainException e1) {
                }
            }
            JOptionPane.showMessageDialog(mainWindow, "A domain must be loaded to perform this action.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Display a dialog that contains a textarea displaying standard output and error output
     */
    private class ConsoleActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Console console = new Console();
            console.setSize(400, 220);
            console.setVisible(true);
        }
    }

    private class ScenarioActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            File mf = (new TopoChooser()).loadMatrix((Container) mainWindow);
            if (mf == null) //cancel button has been pressed
                return;

            try {
                Scenario scenario = (Scenario) ScenarioFactory.loadScenario(mf.getAbsolutePath());
                ScenarioExecutor se = new ScenarioExecutor(scenario);
                String title = "Scenario (paths relative to " + scenario.getScenarioPath() + " )";
                JPanel p = se.getPanel();
                p.setPreferredSize(new Dimension(450, 550));
                showDialog(p, title);
            } catch (Throwable ex) {
                //System.out.println(ex.getMessage());
                ex.printStackTrace();
                errorMessage("Bad Scenario File.");
            }
        }
    }

    private class StartServerActionListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            try {
                Server.getInstance().start();
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(getInstance(), "Error starting the server: " + e1.getClass().getSimpleName() + " " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class StopServerActionListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            Server.getInstance().stop();
        }
    }


    /**
     * Display a dialog to creta a new chart
     */
    private class NewChartActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            showDialog(new NewChartPanel(), "Create Chart");
        }
    }

    /**
     * Display a dialog for chart plotting
     */
    private class PlotChartListener implements ActionListener {
        String name = null;

        public PlotChartListener(String name) {
            this.name = name;
        }

        public void actionPerformed(ActionEvent e) {
            showDialog(new PlotterPanel(name), "Plot a chart");
        }
    }

    /**
     * Remove the chart from the manager
     */
    private class RemoveChartListener implements ActionListener {
        String name = null;

        public RemoveChartListener(String name) {
            this.name = name;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                ChartManager.getInstance().removeChart(name);
            } catch (NoSuchChartException e1) {
                //e1.printStackTrace();
            }
            updateChartMenu();
        }
    }

    /**
     * View the last generated chart
     */
    private class ViewChartListener implements ActionListener {
        String name = null;

        public ViewChartListener(String name) {
            this.name = name;
        }

        public void actionPerformed(ActionEvent e) {
            JFreeChart chart = null;
            try {
                chart = ChartManager.getInstance().getChart(name).getPlot();
            } catch (NoSuchChartException e1) {
                //e1.printStackTrace();
                JOptionPane.showMessageDialog(mainWindow, "No Such Chart: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                updateChartMenu();
                return;
            }

            showDialog(new ChartPanel(chart), "Chart: " + name);
        }
    }

    /**
     * Add data series to a chart
     */
    private class AddSeriesListener implements ActionListener {
        String name = null;

        public AddSeriesListener(String name) {
            this.name = name;
        }


        public void actionPerformed(ActionEvent e) {
            showDialog(new AddSeriesPanel(name), "Add Data Series");
        }
    }


    private class ShowLoadTable implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            final LoadTables tables = new LoadTables();
            JDialog d = new JDialog(MainWindow.getInstance(), "Load Tables") {
                public void dispose() {
                    tables.destroy();
                    super.dispose();
                }
            };
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(tables);
            //d.setSize(400, 400);
            showDialog(d);
        }
    }

    private class ViewLinkInfo implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Domain domain = manager.getCurrentDomain();
            final LinkInfoPanel panel = new LinkInfoPanel(domain);
            JDialog d = new JDialog(MainWindow.getInstance(), "Link information") {
                public void dispose() {
                    panel.destroy();
                    super.dispose();
                }
            };
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setContentPane(panel);
            showDialog(d);            
        }
    }
}
