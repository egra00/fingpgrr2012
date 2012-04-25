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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.guiComponents;

import java.util.List;
import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.*;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Node;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.DomainAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.XMLFactory;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.exception.AlreadyExistException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.exception.NotFoundException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecoratorImpl;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.LinkDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.constraint.DomainConstraint;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.constraint.InterfacesDomainConstraint;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.constraint.CheckBCsDomainConstraint;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.constraint.HasBandwidthDomainConstraint;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.DomainFactory;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.TopoChooser;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;

/*
 * Changes:
 * --------
 * - 28-Sep-2007: add "Edit properties..." menu item (GMO)
 * - 17-Dec-2007: add "rename all links..." and "set node locations" functionalities (GMO)
 *
 */

/**
 *	Graphical user interface of the topology editor
 *
 * <p>Creation date: Mar 20, 2007
 *
 * @author Georges Nimubona (nimubonageorges@hotmail.com)
 */
public class TopEditGUI extends JFrame {

    final static private Logger logger = Logger.getLogger(TopEditGUI.class);

    static private Image img;
    static {
        try {
            img = new ImageIcon(TopEditGUI.class.getResource("/resources/img/icon.gif")).getImage();
        } catch (NullPointerException e) {
            img = null;
        }
    }

	static private TopEditGUI topEditGUI = null;

	private JTabbedPane tabbedPane = null;

    // tell if the dialog of domain properties should be shown automatically
    private boolean showDialog = true;
    // defualt value to false
    private boolean batchMode = false;
    // default value to editing
    private ModalGraphMouse.Mode selectedMode = ModalGraphMouse.Mode.EDITING;

    private DomainFactory domainFactory;

	private TopEditGUI() {
		super("Topology Editor @RUN");
        setIconImage(img);
        domainFactory = new DomainFactory();
		setupUI();
	}
	
	public static TopEditGUI getInstance() {
		if (topEditGUI == null) {
            topEditGUI = new TopEditGUI();
        }
        return topEditGUI;
	}
	
	private void setupUI() {
		
		setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowCloser());
		setLayout(new BorderLayout());
		
		//Setup the menu bar
		setJMenuBar(createMenuBar());

		// Setup the tools panel
		JToolBar tools = createToolBar();

		// Setup the tabbed pane
		
		tabbedPane = new JTabbedPane();

		// Adding to the container
		add(tools, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);

        //default size
        setSize(800, 600);
	}

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;

        menu = new JMenu("File");
        menuBar.add(menu);

        menuItem = new JMenuItem(new NewTopologyAction());
        menu.add(menuItem);

        menuItem = new JMenuItem(new LoadTopologyAction());
        menu.add(menuItem);

        menuItem = new JMenuItem(new EditPropertiesAction());
        menu.add(menuItem);

        menuItem = new JMenuItem(new SaveTopologyAction());
        menu.add(menuItem);

        menuItem = new JMenuItem(new CloseTopologyAction());
        menu.add(menuItem);

        menu = new JMenu("Actions");
        menuBar.add(menu);

        menuItem = new JMenuItem(new UploadTopologyAction());
        menu.add(menuItem);

        menuItem = new JMenuItem(new ViewXMLAction());
        menu.add(menuItem);

        menuItem = new JMenuItem(new ValidateXMLAction());
        menu.add(menuItem);

        menuItem = new JMenuItem(new GenerateAction());
        menu.add(menuItem);

        menuItem = new JMenuItem(new RenameLinksAction());
        menu.add(menuItem);

        menuItem = new JMenuItem(new SetAllLocationAction());
        menu.add(menuItem);

        menuBar.add(createModelsMenu());

        menuBar.add(createModeMenu());

        menuBar.add(createConstraintsMenu());

        return menuBar;
    }
    private JMenu createConstraintsMenu() {
        JMenu menu = new JMenu("Constraints");
        final JCheckBoxMenuItem ifMenuItem = new JCheckBoxMenuItem("Interfaces");
        ifMenuItem.setSelected(true);
        menu.add(ifMenuItem);

        final JCheckBoxMenuItem bcMenuItem = new JCheckBoxMenuItem("Diffserv BCs");
        bcMenuItem.setSelected(true);
        menu.add(bcMenuItem);

        final JCheckBoxMenuItem bwMenuItem = new JCheckBoxMenuItem("Bandwidth is set");
        bwMenuItem.setSelected(true);
        menu.add(bwMenuItem);

        menu.addSeparator();
        JMenuItem item = new JMenuItem("Check Selected Constraints");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DomainDecorator domain = getCurrentPanel().getDomainDecorator();
                DomainConstraint c;
                if (domain == null) return;
                if (ifMenuItem.isSelected()) {
                    c = new InterfacesDomainConstraint();
                    if (!c.validate(domain)) {
                        JOptionPane.showMessageDialog(TopEditGUI.this, c.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                if (bcMenuItem.isSelected()) {
                    c = new CheckBCsDomainConstraint();
                    if (!c.validate(domain)) {
                        JOptionPane.showMessageDialog(TopEditGUI.this, c.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                if (bwMenuItem.isSelected()) {
                    c = new HasBandwidthDomainConstraint();
                    if (!c.validate(domain)) {
                        JOptionPane.showMessageDialog(TopEditGUI.this, c.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                JOptionPane.showMessageDialog(TopEditGUI.this, "Constraints validated.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        menu.add(item);

        return menu;
    }

    private JMenu createModeMenu() {
        JMenu menu = new JMenu("Mode");
        ButtonGroup bg = new ButtonGroup();
        for (final ModalGraphMouse.Mode mode : (List<ModalGraphMouse.Mode>)ModalGraphMouse.Mode.getValues()) {
            Action a = new ChangeModeAction(mode);
            JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(a);
            bg.add(menuItem);
            if (selectedMode == mode) menuItem.setSelected(true);
            menu.add(menuItem);
        }
        return menu;
    }

    private JMenu createModelsMenu() {
        JMenu menu = new JMenu("Models");

        JMenu subMenu = new JMenu("Select");
        JMenu subsubMenu = createRadioModelMenu();
        subsubMenu.setText("Nodes");

        for (int i = 0; i < subsubMenu.getItemCount(); i++) {
            subsubMenu.getItem(i).addActionListener(new SelectNodeModelActionListener(subsubMenu.getItem(i).getText()));
        }

        subMenu.add(subsubMenu);
        subsubMenu = createRadioModelMenu();
        subsubMenu.setText("Links");
        subMenu.add(subsubMenu);

        for (int i = 0; i < subsubMenu.getItemCount(); i++) {
            subsubMenu.getItem(i).addActionListener(new SelectLinkModelActionListener(subsubMenu.getItem(i).getText()));
        }

        menu.add(subMenu);

        subMenu = new JMenu("Edit");
        subsubMenu = createModelMenu();
        subsubMenu.setText("Nodes");

        for (int i = 0; i < subsubMenu.getItemCount(); i++) {
            subsubMenu.getItem(i).addActionListener(new EditNodeModelActionListener(subsubMenu.getItem(i).getText()));
        }

        subMenu.add(subsubMenu);
        subsubMenu = createModelMenu();
        subsubMenu.setText("Links");
        subMenu.add(subsubMenu);

        for (int i = 0; i < subsubMenu.getItemCount(); i++) {
            subsubMenu.getItem(i).addActionListener(new EditLinkModelActionListener(subsubMenu.getItem(i).getText()));
        }

        menu.add(subMenu);

        JCheckBoxMenuItem item = new JCheckBoxMenuItem(new BatchModeAction());
        item.setSelected(batchMode);
        menu.add(item);

        return menu;
    }

    private JMenu createModelMenu() {
        JMenuItem menuItem;
        JMenu subsubMenu = new JMenu("Nodes");

        ButtonGroup bg = new ButtonGroup();
        menuItem = new JMenuItem("Default Model 1");
        bg.add(menuItem);
        subsubMenu.add(menuItem);
        menuItem = new JMenuItem("Default Model 2");
        bg.add(menuItem);
        subsubMenu.add(menuItem);
        menuItem = new JMenuItem("Default Model 3");
        bg.add(menuItem);
        subsubMenu.add(menuItem);
        menuItem = new JMenuItem("Default Model 4");
        bg.add(menuItem);
        subsubMenu.add(menuItem);

        return subsubMenu;
    }

    private JMenu createRadioModelMenu() {
        JMenuItem menuItem;
        JMenu subsubMenu = new JMenu("Nodes");

        ButtonGroup bg = new ButtonGroup();
        menuItem = new JRadioButtonMenuItem("Default Model 1");
        bg.add(menuItem);
        subsubMenu.add(menuItem);
        menuItem = new JRadioButtonMenuItem("Default Model 2");
        bg.add(menuItem);
        subsubMenu.add(menuItem);
        menuItem = new JRadioButtonMenuItem("Default Model 3");
        bg.add(menuItem);
        subsubMenu.add(menuItem);
        menuItem = new JRadioButtonMenuItem("Default Model 4");
        bg.add(menuItem);
        subsubMenu.add(menuItem);

        return subsubMenu;
    }

    private JToolBar createToolBar() {
        JToolBar toolbar = new JToolBar("Tools Panel");

        return toolbar;
    }

    public GraphEditorPanel getCurrentPanel() {
        return (GraphEditorPanel)tabbedPane.getSelectedComponent();
    }

    private void updateCurrentTabTitle() {
        int index = tabbedPane.getSelectedIndex();
        String newTitle = getTabTitle(getCurrentPanel().getDomainDecorator());
        tabbedPane.setTitleAt(index, newTitle);
    }

    private String getTabTitle(DomainDecorator domainDecorator) {
        Domain domain = domainDecorator.getDomain();
        if (!domain.isSetInfo() || !domain.getInfo().isSetTitle() || domain.getInfo().getTitle().equals("")) {
            return "AS: " + String.valueOf(domain.getASID());
        } else {
            return domain.getInfo().getTitle() + " (AS:" + domain.getASID() + ")";
        }
    }

    /**
     * Create a new tabbed pane corresponding to the domain.
     * @param domain
     */
    public void newEdition(Domain domain) {
        DomainDecorator domainDecorator = new DomainDecoratorImpl(domain);
        final GraphEditorPanel panel = new GraphEditorPanel(domainDecorator);
        tabbedPane.addTab(getTabTitle(domainDecorator), panel);
        tabbedPane.setSelectedComponent(panel);


        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                panel.init();
                panel.setMode(selectedMode);
                panel.setBatchMode(batchMode);
            }
        });

    }

    public void newDomain() {
            Domain domain = domainFactory.createDefaultObject(null);
            newEdition(domain);
            if (showDialog) {
                new DomainPropertiesDialog(domain) {
                    protected void postProcessingOnSuccess() {
                        updateCurrentTabTitle();
                    }
                };
            }
    }


    /*===================================*/
    /*              ACTIONS              */
    /*===================================*/

    private class NewTopologyAction extends AbstractAction {
        public NewTopologyAction() {
            super("New Topology...", null);
        }

        public void actionPerformed(ActionEvent e) {
            newDomain();
        }
    }

    private class CloseTopologyAction extends AbstractAction {
        public CloseTopologyAction() {
            super("Close Topology", null);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getSelectedComponent() != null)
                tabbedPane.remove(tabbedPane.getSelectedComponent());
        }
    }

    private class UploadTopologyAction extends AbstractAction {
        public UploadTopologyAction() {
            super("Upload in TOTEM", null);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getSelectedComponent() == null) {
                JOptionPane.showMessageDialog(TopEditGUI.this, "No domain", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            InputStream is = XMLFactory.getInputStream(getCurrentPanel().getDomainDecorator());
            try {
                InterDomainManager.getInstance().loadDomain(is, true);
                tabbedPane.remove(tabbedPane.getSelectedComponent());
            } catch (InvalidDomainException e1) {
                JOptionPane.showMessageDialog(TopEditGUI.this, "Invalid Domain", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (DomainAlreadyExistException e1) {
                JOptionPane.showMessageDialog(TopEditGUI.this, "A domain with the same id already exists", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class ViewXMLAction extends AbstractAction {
        public ViewXMLAction() {
            super("View XML", null);
        }

        public void actionPerformed(ActionEvent e) {
            // create a dialog
            JTextArea area = new JTextArea();
            JDialog dialog = new JDialog(TopEditGUI.this, "XML View", false);
            dialog.setSize(450, 650);
            if (getCurrentPanel() != null)
                area.setText(XMLFactory.getXML(getCurrentPanel().getDomainDecorator()));
            JScrollPane scrollPane = new JScrollPane(area);
            dialog.setContentPane(scrollPane);

            //dialog.pack();
            dialog.setVisible(true);
        }
    }

    private class ValidateXMLAction extends AbstractAction {

        public ValidateXMLAction() {
            super("Validate", null);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getSelectedComponent() == null) {
                JOptionPane.showMessageDialog(TopEditGUI.this, "No domain", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (XMLFactory.validate(getCurrentPanel().getDomainDecorator())) {
                    JOptionPane.showMessageDialog(TopEditGUI.this, "Domain validated", "Validation", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(TopEditGUI.this, "Domain does not validate", "Validation", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(TopEditGUI.this, "Domain does not validate. " + ex.getClass().getSimpleName() + ": " + ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class SetAllLocationAction extends AbstractAction {
        public SetAllLocationAction() {
            super("Set node locations", null);
        }

        public void actionPerformed(ActionEvent e) {
            getCurrentPanel().setNodeLocations();
            JOptionPane.showMessageDialog(TopEditGUI.this, "Location set for all nodes", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private class RenameLinksAction extends AbstractAction {
        public RenameLinksAction() {
            super("Rename links...", null);
        }

        public void actionPerformed(ActionEvent e) {
            Dialog d = new RenameLinksDialog();
            d.pack();
            d.setLocationRelativeTo(TopEditGUI.this);
            d.setVisible(true);
        }
    }

    private class GenerateAction extends AbstractAction {
        public GenerateAction() {
            super("Generate topology...", null);
        }

        public void actionPerformed(ActionEvent e) {
            generate();
        }
    }

    private class LoadTopologyAction extends AbstractAction {
        public LoadTopologyAction() {
            super("Load Topology...", null);
        }

        public void actionPerformed(ActionEvent e) {
            Dialog d = new LoadTopoDialog();
            d.pack();
            d.setLocationRelativeTo(TopEditGUI.this);
            d.setVisible(true);
        }
    }

    private class SaveTopologyAction extends AbstractAction {
        public SaveTopologyAction() {
            super("Save topology...", null);
        }

        public void actionPerformed(ActionEvent e) {
            File f = (new TopoChooser()).saveTopo(TopEditGUI.this);
            if (f != null) {
                String filename = f.getAbsolutePath();
                if (!filename.toLowerCase().endsWith(".xml")) {
                    filename = filename.concat(".xml");
                }

                try {
                    XMLFactory.saveDomain(getCurrentPanel().getDomainDecorator().getDomain(), f.getAbsolutePath());
                } catch (JAXBException e1) {
                    e1.printStackTrace();
                    String msg;
                    if (e1.getLinkedException() != null) {
                        msg = e1.getLinkedException().getMessage();
                    } else msg = e1.getMessage();

                    JOptionPane.showMessageDialog(TopEditGUI.getInstance(), "Unable to save domain: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(TopEditGUI.getInstance(), "File not found : " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private class EditPropertiesAction extends AbstractAction {
        public EditPropertiesAction() {
            super("Properties...", null);
        }

        public void actionPerformed(ActionEvent e) {
            if (tabbedPane.getSelectedComponent() == null) {
                JOptionPane.showMessageDialog(TopEditGUI.this, "No domain", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Domain domain = getCurrentPanel().getDomainDecorator().getDomain();
            new DomainPropertiesDialog(domain) {
                protected void postProcessingOnSuccess() {
                    updateCurrentTabTitle();
                }
            };
        }
    }

    private class ChangeModeAction extends AbstractAction {
        ModalGraphMouse.Mode mode;

        public ChangeModeAction(ModalGraphMouse.Mode mode) {
            super(mode.getMode(), null);
            this.mode = mode;
        }

        public void actionPerformed(ActionEvent e) {
            TopEditGUI.this.selectedMode = mode;
            for (Component c : tabbedPane.getComponents()) {
                ((GraphEditorPanel)c).setMode(mode);
            }
        }
    }

    private class BatchModeAction extends AbstractAction {
        public BatchModeAction() {
            super("Use batch mode", null);
        }

        public void actionPerformed(ActionEvent e) {
            batchMode = !batchMode;
            for (Component c : tabbedPane.getComponents()) {
                ((GraphEditorPanel)c).setBatchMode(batchMode);
            }
        }
    }

    private class EditNodeModelActionListener implements ActionListener {
        final String modelName;

        public EditNodeModelActionListener(String modelName) {
            this.modelName = modelName;
        }

        public void actionPerformed(ActionEvent e) {
           Node node = getCurrentPanel().getNodeFactory().getModel(modelName);

            final Node finalNode;
            if (node == null) {
                node = finalNode = getCurrentPanel().getNodeFactory().createDefaultObject(null);
            } else finalNode = null;

            JDialog prop = new NodePropertiesDialog(node, getCurrentPanel().getDomainDecorator()){
                protected void postProcessingOnSuccess() {
                    if (finalNode != null) {
                        try {
                            getCurrentPanel().getNodeFactory().addInstance(modelName, finalNode);
                        } catch (AlreadyExistException e1) {
                            JOptionPane.showMessageDialog(TopEditGUI.this, "Model already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
            };
            prop.pack();
            prop.setLocationRelativeTo(TopEditGUI.this);
            prop.setVisible(true);
        }
    }

    private class EditLinkModelActionListener implements ActionListener {
        final String modelName;

        public EditLinkModelActionListener(String modelName) {
            this.modelName = modelName;
        }

        public void actionPerformed(ActionEvent e) {
           LinkDecorator link = getCurrentPanel().getLinkFactory().getModel(modelName);

            final LinkDecorator finalLink;
            if (link == null) {
                link = finalLink = getCurrentPanel().getLinkFactory().createDefaultObject(null);
            } else finalLink = null;

            JDialog prop = new LinkPropertiesDialog(getCurrentPanel().getDomainDecorator(), link){
                protected void postProcessingOnSuccess() {
                    if (finalLink != null) {
                        try {
                            getCurrentPanel().getLinkFactory().addInstance(modelName, finalLink);
                        } catch (AlreadyExistException e1) {
                            JOptionPane.showMessageDialog(TopEditGUI.this, "Model already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    }
                }
            };
            prop.pack();
            prop.setLocationRelativeTo(TopEditGUI.this);
            prop.setVisible(true);
        }
    }

    private class SelectNodeModelActionListener implements ActionListener {
        final String modelName;

        public SelectNodeModelActionListener(String modelName) {
            this.modelName = modelName;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                getCurrentPanel().getNodeFactory().setDefaultModel(modelName);
            } catch (NotFoundException e1) {
                JOptionPane.showMessageDialog(TopEditGUI.this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    private class SelectLinkModelActionListener implements ActionListener {
        final String modelName;

        public SelectLinkModelActionListener(String modelName) {
            this.modelName = modelName;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                getCurrentPanel().getLinkFactory().setDefaultModel(modelName);
            } catch (NotFoundException e1) {
                JOptionPane.showMessageDialog(TopEditGUI.this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
    }

    /**
     * Reset this singleton instance
     */
    public static void reset() {
    	topEditGUI = null;
    }
    

    /**
     *  This method triggers a generation dialog
     */
    public void generate() {
    	new GenerationDialog();
    }

	/**
     * This method pop up a Confirm JDialog on screen and exit the application if requested
     * by the user.
     */
    void exit() {
        int n = JOptionPane.showConfirmDialog(this, "Do you really want to quit ?", "Exit",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (n == JOptionPane.YES_OPTION) {
        	reset();
        	dispose();
        }
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
         * this method is called from an instance of TopEditGUI.
         * windowClosing is the method that is called when the cross at the top of a window
         * is clicked.
         *
         * @param e the fired event
         */
        public void windowClosing(WindowEvent e) {
            if (e.getWindow() instanceof TopEditGUI)
                exit();
            else
                System.exit(1);
        }
    }

}


