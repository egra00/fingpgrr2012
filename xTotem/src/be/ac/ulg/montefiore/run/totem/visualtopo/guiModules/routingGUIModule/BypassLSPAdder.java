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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.DomainElement;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManagerListener;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ExpandablePanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ParamTable;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.StartAlgoPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.ClassCellRenderer;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.DomainElementListCellRenderer;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManagerListener;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.TotemActionExecutionException;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/*
* Changes:
* --------
* - 17-Dec-2007: Add different bandwidth calculation for bypass LSPs. Fix bug with protected links. (GMO)
* - 28-Feb-2008: Add classes of service panel (GMO)
*/

/**
* Dialog whose purpose is to compute bypass LSPs. It adapts to the current default domain.
*
* <p>Creation date: 26/11/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class BypassLSPAdder extends JDialog implements RepositoryManagerListener, InterDomainManagerListener {

    private static enum BwType {
        FIXED, RESV_BW, CAPACITY;

        public String toString() {
            switch (this) {
                case FIXED:
                    return "Fixed";
                case RESV_BW:
                    return "Current Reserved Bandwidth";
                case CAPACITY:
                    return "Link Capacity";
                default:
                    return super.toString();
            }
        }
    }

    private Domain domain;

    private final JTextField idField;
    private final JTextField bwField;
    private final JComboBox bwTypeCombo;
    private final JLabel unitLabel;
    private final JComboBox protectedResourceCombo;
    private final JRadioButton linkRadioBtn;
    private final JRadioButton nodeRadioBtn;
    private final ButtonGroup btnGroup;

    private final JComboBox nodeProtectionCombo;

    private final JComboBox algoCombo;
    private final ParamTable params;

    private final ExpandablePanel diffServPanel;
    private final ExpandablePanel cosPanel;
    private final JButton okBtn;
    private final JButton cancelBtn;

    public BypassLSPAdder() {
        super(MainWindow.getInstance(), "Add a Bypass LSP", false);

        domain = InterDomainManager.getInstance().getDefaultDomain();

        idField = new JTextField();
        bwField = new JTextField();
        bwTypeCombo = new JComboBox(BwType.values());
        bwTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setUnit();
            }
        });
        unitLabel = new JLabel("Unit");

        protectedResourceCombo = new JComboBox();
        protectedResourceCombo.setRenderer(new DomainElementListCellRenderer());
        protectedResourceCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fillNodeCombo();
                setUnit();
            }
        });
        linkRadioBtn = new JRadioButton("Link (NHOP)");
        linkRadioBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fillCombo();
                setUnit();
            }
        });
        nodeRadioBtn = new JRadioButton("Node (NNHOP)");
        nodeRadioBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fillCombo();
                setUnit();
            }
        });
        btnGroup = new ButtonGroup();
        btnGroup.add(linkRadioBtn);
        btnGroup.add(nodeRadioBtn);
        linkRadioBtn.setSelected(true);

        nodeProtectionCombo = new JComboBox();
        nodeProtectionCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setUnit();
            }
        });
        nodeProtectionCombo.setRenderer(new PairLinkRenderer());
        nodeProtectionCombo.setEnabled(false);

        params = new ParamTable(6);

        algoCombo = new JComboBox();
        algoCombo.setRenderer(new ClassCellRenderer());
        algoCombo.addActionListener(new AlgoComboListener(params));

        diffServPanel = new ExpandablePanel(this, "DiffServ", new DiffservPanel(domain));
        cosPanel = new ExpandablePanel(this, "Classes of Service", new ClassesOfServicePanel(domain));


        okBtn = new JButton("Accept");
        okBtn.addActionListener(new AcceptActionListener());
        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        fillCombo();
        fillAlgoCombo();
        setUnit();

        setupUI();

        RepositoryManager.getInstance().addListener(this);
        InterDomainManager.getInstance().addListener(this);
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 5, 5, 5);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;

        /* build resource panel */
        JPanel resourcePanel = new JPanel(new GridBagLayout());
        resourcePanel.setBorder(BorderFactory.createTitledBorder("Protected resource"));
        resourcePanel.add(linkRadioBtn, c);
        c.gridx++;
        resourcePanel.add(nodeRadioBtn, c);
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy++;
        resourcePanel.add(protectedResourceCombo, c);
        c.gridy++;
        c.gridx++;
        resourcePanel.add(nodeProtectionCombo, c);
        c.gridwidth = 2;

        c.gridx = 0;
        c.gridy = 0;

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.add(new JLabel("Lsp id (leave blank to generate):"), c);
        c.gridy++;
        mainPanel.add(idField, c);
        c.gridy++;
        mainPanel.add(new JLabel("Bandwidth:"), c);
        c.gridy++;
        mainPanel.add(bwTypeCombo, c);
        c.gridy++;
        c.gridwidth = 1;
        mainPanel.add(bwField, c);
        c.gridx++;
        c.weightx = 0.0;
        mainPanel.add(unitLabel, c);
        c.weightx = 1.0;
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy++;
        mainPanel.add(resourcePanel, c);
        c.gridy++;

        diffServPanel.setToolTipText("If the panel is retracted, diffServ configuration will be ignored.");
        mainPanel.add(diffServPanel, c);
        c.gridy++;

        cosPanel.setToolTipText("If the panel is retracted, all classes of service will be accepted by the LSP.");
        mainPanel.add(cosPanel, c);
        c.gridy++;

        mainPanel.add(new JLabel("Algorithm"), c);
        c.gridy++;
        mainPanel.add(algoCombo, c);

        JButton startAlgo = new JButton("Start another algorithm...");
        startAlgo.addActionListener(new StartAlgoListener(this, LSPBypassRouting.class));
        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        mainPanel.add(startAlgo, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy++;
        mainPanel.add(new JLabel("Additional routing parameters"), c);

        c.gridy++;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(new JScrollPane(params) {
            public Dimension getPreferredSize() {
                int width = super.getPreferredSize().width;
                //int height = params.getPreferredSize().height + 50;
                int height = params.getRowHeight() * 8;
                return new Dimension(width, height);
            }
        }, c);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.add(okBtn);
        buttonPanel.add(cancelBtn);

        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setUnit() {
        String unit;

        if (domain == null) {
            unit = "?";
            return;
        }
        String domainUnit = domain.getBandwidthUnit().toString();

        if (bwTypeCombo.getSelectedItem() == null)
            unit = "?";
        else if (bwTypeCombo.getSelectedItem() == BwType.FIXED) {
            unit = domainUnit;
        } else if (bwTypeCombo.getSelectedItem() == BwType.CAPACITY) {
            unit = "%";
            if (linkRadioBtn.isSelected()) {
                if (protectedResourceCombo.getSelectedItem() != null) {
                    unit += " of " + ((Link)protectedResourceCombo.getSelectedItem()).getBandwidth() + " " + domainUnit;
                }
            } else {
                if (nodeProtectionCombo.getSelectedItem() != null) {
                    Pair<Link, Link> p = (Pair<Link, Link>)nodeProtectionCombo.getSelectedItem();
                    unit += " of " + Math.min(p.getFirst().getBandwidth(), p.getSecond().getBandwidth()) + " " + domainUnit;
                }
            }
        } else if (bwTypeCombo.getSelectedItem() == BwType.RESV_BW) {
            int ct = -1;
            if (diffServPanel.isExpanded()) {
                ct = ((DiffservPanel)diffServPanel.getPanel()).getClassType();
            }
            unit = "%";
            if (linkRadioBtn.isSelected()) {
                if (protectedResourceCombo.getSelectedItem() != null) {
                    if (ct < 0)
                        unit += " of " + ((Link)protectedResourceCombo.getSelectedItem()).getReservedBandwidth() + " " + domainUnit;
                    else unit += " of " + ((Link)protectedResourceCombo.getSelectedItem()).getReservedBandwidth(ct) + " " + domainUnit;
                }
            } else {
                if (nodeProtectionCombo.getSelectedItem() != null) {
                    Pair<Link, Link> p = (Pair<Link, Link>)nodeProtectionCombo.getSelectedItem();
                    if (ct < 0)
                        unit += " of " + Math.min(p.getFirst().getReservedBandwidth(), p.getSecond().getReservedBandwidth()) + " " + domainUnit;
                    else unit += " of " + Math.min(p.getFirst().getReservedBandwidth(ct), p.getSecond().getReservedBandwidth(ct)) + " " + domainUnit;
                }
            }
        } else {
            unit = "?";
        }

        unitLabel.setText(unit);
    }

    private void fillCombo() {
        protectedResourceCombo.removeAllItems();
        nodeProtectionCombo.setEnabled(nodeRadioBtn.isSelected());
        if (domain == null) return;
        if (!nodeRadioBtn.isSelected()) {
            for (Link l : domain.getAllLinks()) {
                protectedResourceCombo.addItem(l);
            }
        } else {
            for (Node n : domain.getAllNodes()) {
                protectedResourceCombo.addItem(n);
            }
            fillNodeCombo();
        }
    }

    private void fillNodeCombo() {
        if (nodeRadioBtn.isSelected()) {
            nodeProtectionCombo.removeAllItems();
            Node n = (Node)protectedResourceCombo.getSelectedItem();
            if (n == null) return;
            //nodeProtectionCombo.addItem("All");
            for (Link src : n.getAllInLink()) {
                for (Link dst : n.getAllOutLink()) {
                    try {
                        if (src.getSrcNode() != dst.getDstNode()) {
                            Pair<Link, Link> pair = new Pair<Link, Link>(src, dst);
                            nodeProtectionCombo.addItem(pair);
                        }
                    } catch (NodeNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void fillAlgoCombo() {
        algoCombo.removeAllItems();
        if (domain == null) return;

        for (TotemAlgorithm algo : RepositoryManager.getInstance().getAllStartedAlgos(domain.getASID(), LSPBypassRouting.class))
            algoCombo.addItem(algo);
    }

    public void dispose() {
        RepositoryManager.getInstance().removeListener(this);
        InterDomainManager.getInstance().removeListener(this);
        super.dispose();
    }

    public void startAlgoEvent(TotemAlgorithm algo) {
        fillAlgoCombo();
    }

    public void stopAlgoEvent(TotemAlgorithm algo) {
        fillAlgoCombo();
    }

    public void addDomainEvent(Domain domain) {
    }

    public void removeDomainEvent(Domain domain) {
    }

    public void changeDefaultDomainEvent(Domain domain) {
        this.domain = domain;
        ((DiffservPanel)diffServPanel.getPanel()).setDomain(domain);
        ((ClassesOfServicePanel)cosPanel.getPanel()).setDomain(domain);
        fillCombo();
        fillAlgoCombo();
    }


    private class AcceptActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (domain == null) {
                MainWindow.getInstance().errorMessage("A domain must be loaded.");
                return;
            }

            // stop cell editing
            if (params.getCellEditor() != null && !params.getCellEditor().stopCellEditing())
                return;

            float bw;

            if (bwField.getText().equals("")) {
                MainWindow.getInstance().errorMessage("Please specify the bandwidth field.");
                return;
            }
            try {
                bw = Float.parseFloat(bwField.getText());
            } catch (NumberFormatException e1) {
                MainWindow.getInstance().errorMessage("Bandwidth field must be a float value.");
                return;
            }

            if (algoCombo.getSelectedItem() == null) {
                MainWindow.getInstance().errorMessage("Please select an algorithm");
                return;
            }

            String protectedResource = ((DomainElement)protectedResourceCombo.getSelectedItem()).getId();
            String lspId = idField.getText().length() > 0 ? idField.getText() : domain.generateBypassLspId(protectedResource);

            LSPBypassRoutingParameter routingParams;
            try {
                if (linkRadioBtn.isSelected()) {
                    String dstNode = ((Link)protectedResourceCombo.getSelectedItem()).getDstNode().getId();
                    routingParams = new LSPBypassRoutingParameter(lspId, dstNode);
                    routingParams.addProtectedLink(protectedResource);
                } else {
                    Pair<Link, Link> protectedLinks = (Pair<Link, Link>)nodeProtectionCombo.getSelectedItem();
                    if (protectedLinks == null) {
                        MainWindow.getInstance().errorMessage("Please select a path to be protected.");
                        return;
                    }
                    String dstNode = protectedLinks.getSecond().getDstNode().getId();
                    routingParams = new LSPBypassRoutingParameter(lspId, dstNode);
                    routingParams.addProtectedLink(protectedLinks.getFirst().getId());
                }
            } catch (NodeNotFoundException e1) {
                MainWindow.getInstance().errorMessage("Unexpected error: Node not found.");
                return;
            }

            routingParams.setBandwidth(bw);

            if (diffServPanel.isExpanded()) {
                DiffservPanel diffserv = (DiffservPanel) diffServPanel.getPanel();
                routingParams.setClassType(diffserv.getClassType());
                routingParams.setSetup(diffserv.getSetupLevel());
                routingParams.setHolding(diffserv.getHoldingLevel());
            }
            if (cosPanel.isExpanded()) {
                routingParams.setAcceptedCos(((ClassesOfServicePanel)cosPanel.getPanel()).getClassesOfService());
            }
            
            routingParams.putAllRoutingAlgorithmParameter(params.toHashMap());

            LSPBypassRouting routing = (LSPBypassRouting) algoCombo.getSelectedItem();
            if (routing == null) {
                MainWindow.getInstance().errorMessage("Null algorithm.");
                return;
            }

            try {
                TotemActionList actions = routing.routeBypass(domain, routingParams);
                for (Object o : actions) {
                    ((TotemAction)o).execute();
                }
            } catch (RoutingException e1) {
                e1.printStackTrace();
                MainWindow.getInstance().errorMessage(e1);
                return;
            } catch (NoRouteToHostException e1) {
                e1.printStackTrace();
                MainWindow.getInstance().errorMessage(e1);
                return;
            } catch (TotemActionExecutionException e1) {
                e1.printStackTrace();
                MainWindow.getInstance().errorMessage(e1);
                return;
            }

            //close dialog on success
            // commented. Do not close the dialog as we maybe want to establish more than one bypass
            // (example: multiple NNHOP protecting the same node)
            //dispose();
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

            LSPBypassRouting routingAlgo = (LSPBypassRouting) combo.getSelectedItem();

            table.fill(routingAlgo.getBypassRoutingParameters());
        }
    }

    public class PairLinkRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value == null) return this;
            if (value instanceof Pair) {
                try {
                    if (value instanceof Pair) {
                        Pair<Link, Link> pair = (Pair<Link, Link>)value;
                        setText(pair.getFirst().getId() + " >> " + pair.getSecond().getId());
                    } else {
                        setText(value.toString());
                    }
                } catch (ClassCastException e) {
                }
            }
            return this;
        }
    }


}
