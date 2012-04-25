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

import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManagerListener;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManagerListener;
import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.TotemActionExecutionException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.*;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.ClassCellRenderer;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
* Changes:
* --------
*
*/

/**
 * Dialog to compute multiple bypass lsps at once. Can only compute LSPs protecting all resources.
 * <p/>
 * <p>Creation date: 11/12/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class MultipleBypassDialog extends JDialog implements RepositoryManagerListener, InterDomainManagerListener {
    private final static Logger logger = Logger.getLogger(MultipleBypassDialog.class);

    private Domain domain;

    private final JRadioButton linkRadioBtn;
    private final JRadioButton nodeRadioBtn;
    private final ButtonGroup btnGroup;

    private final JTextField bwField;
    private final JLabel unitLabel;
    private final JComboBox bwTypeCombo;


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

    private static enum Order {
        DECREASING_BW_ORDER, INCREASING_BW_ORDER, SHUFFLE_ORDER;

        public String toString() {
            switch (this) {
                case DECREASING_BW_ORDER:
                    return "Decreasing Bandwidth Order";
                case INCREASING_BW_ORDER:
                    return "Increasing Bandwidth Order";
                case SHUFFLE_ORDER:
                    return "Shuffle Order";
                default:
                    return super.toString();
            }
        }
    }

    private final JComboBox orderCombo;

    private final JComboBox algoCombo;
    private final ParamTable params;

    private final ExpandablePanel diffServPanel;
    private final JButton okBtn;
    private final JButton cancelBtn;

    public MultipleBypassDialog() {
        super(MainWindow.getInstance(), "Protect all resources using Bypass LSPs", false);

        domain = InterDomainManager.getInstance().getDefaultDomain();

        btnGroup = new ButtonGroup();
        linkRadioBtn = new JRadioButton("Links (NHOP)");
        nodeRadioBtn = new JRadioButton("Nodes (NNHOP)");
        btnGroup.add(linkRadioBtn);
        btnGroup.add(nodeRadioBtn);
        linkRadioBtn.setSelected(true);

        bwField = new JTextField();

        bwTypeCombo = new JComboBox(BwType.values());
        bwTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setUnit();
            }
        });
        unitLabel = new JLabel("Unit");

        orderCombo = new JComboBox(Order.values());

        params = new ParamTable(6);

        algoCombo = new JComboBox();
        algoCombo.setRenderer(new ClassCellRenderer());
        algoCombo.addActionListener(new AlgoComboListener(params));

        diffServPanel = new ExpandablePanel(this, "DiffServ", new DiffservPanel(domain));

        okBtn = new JButton("Accept");
        okBtn.addActionListener(new AcceptActionListener());
        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setUnit();
        fillAlgoCombo();

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
        c.gridwidth = 2;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;

        JPanel resourcePanel = new JPanel(new GridLayout(1, 2));
        resourcePanel.setBorder(BorderFactory.createTitledBorder("Protected resources"));
        resourcePanel.add(linkRadioBtn);
        resourcePanel.add(nodeRadioBtn);

        JPanel mainPanel = new JPanel(new GridBagLayout());

        mainPanel.add(resourcePanel, c);

        c.gridy++;
        mainPanel.add(new JLabel("Bandwidth"), c);
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

        mainPanel.add(new JLabel("Establishment order"), c);
        c.gridy++;
        mainPanel.add(orderCombo, c);

        c.gridy++;
        diffServPanel.setToolTipText("If the panel is retracted, diffServ configuration will be ignored.");
        mainPanel.add(diffServPanel, c);

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


        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);

        add(mainPanel, BorderLayout.CENTER);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public void dispose() {
        RepositoryManager.getInstance().removeListener(this);
        InterDomainManager.getInstance().removeListener(this);
        super.dispose();
    }

    private void fillAlgoCombo() {
        algoCombo.removeAllItems();
        if (domain == null) return;

        for (TotemAlgorithm algo : RepositoryManager.getInstance().getAllStartedAlgos(domain.getASID(), LSPBypassRouting.class))
            algoCombo.addItem(algo);
    }

    private void setUnit() {
        String unit;

        if (bwTypeCombo.getSelectedItem() == null)
            unit = "?";
        else if (bwTypeCombo.getSelectedItem() == BwType.FIXED) {
            if (domain == null)
                unit = "?";
            else
                unit = domain.getBandwidthUnit().toString();
        } else
            unit = "%";

        unitLabel.setText(unit);
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
        fillAlgoCombo();
        setUnit();
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
                MainWindow.getInstance().errorMessage("Bandwidth must be a float.");
                return;
            }

            if (algoCombo.getSelectedItem() == null) {
                MainWindow.getInstance().errorMessage("Please select an algorithm");
                return;
            }
            LSPBypassRouting routing = (LSPBypassRouting) algoCombo.getSelectedItem();
            if (routing == null) {
                MainWindow.getInstance().errorMessage("Null algorithm.");
                return;
            }

            //create a progress bar
            ProgressBarPanel progressBar = new ProgressBarPanel(0, 100, 400);
            progressBar.setCancelable(false);
            progressBar.getProgressBar().setIndeterminate(true);
            JDialog pBarDialog = MainWindow.getInstance().showDialog(progressBar, "Progress Bar");
            progressBar.setMessage("Computing parameters...");

            try {

                List<LSPBypassRoutingParameter> paramList = new ArrayList<LSPBypassRoutingParameter>();
                if (linkRadioBtn.isSelected()) {
                    for (Link l : domain.getAllLinks()) {
                        LSPBypassRoutingParameter rParam = new LSPBypassRoutingParameter(domain.generateBypassLspId(l.getId()), l.getDstNode().getId());
                        rParam.addProtectedLink(l.getId());

                        BwType bwType = (BwType) bwTypeCombo.getSelectedItem();
                        switch (bwType) {
                            case FIXED:
                                rParam.setBandwidth(bw);
                                break;
                            case CAPACITY:
                                rParam.setBandwidth(l.getBandwidth() * bw / 100);
                                break;
                            case RESV_BW:
                                if (diffServPanel.isExpanded()) {
                                    int ct = ((DiffservPanel) diffServPanel.getPanel()).getClassType();
                                    rParam.setBandwidth(l.getReservedBandwidthCT(ct) * bw / 100);
                                } else
                                    rParam.setBandwidth(l.getReservedBandwidth() * bw / 100);
                        }

                        if (diffServPanel.isExpanded()) {
                            DiffservPanel diffserv = (DiffservPanel) diffServPanel.getPanel();
                            rParam.setClassType(diffserv.getClassType());
                            rParam.setSetup(diffserv.getSetupLevel());
                            rParam.setHolding(diffserv.getHoldingLevel());
                        }
                        rParam.putAllRoutingAlgorithmParameter(params.toHashMap());

                        paramList.add(rParam);
                    }
                } else { //node protection
                    for (Node n : domain.getAllNodes()) {
                        for (Link first : n.getAllInLink()) {
                            for (Link second : n.getAllOutLink()) {
                                if (first.getSrcNode() == second.getDstNode())
                                    continue;
                                LSPBypassRoutingParameter rParam = new LSPBypassRoutingParameter(domain.generateBypassLspId(n.getId()), second.getDstNode().getId());
                                rParam.addProtectedLink(first.getId());

                                BwType bwType = (BwType) bwTypeCombo.getSelectedItem();
                                switch (bwType) {
                                    case FIXED:
                                        rParam.setBandwidth(bw);
                                        break;
                                    case CAPACITY:
                                        float cap = Math.min(first.getBandwidth(), second.getBandwidth());
                                        rParam.setBandwidth(cap * bw / 100);
                                        break;
                                    case RESV_BW:
                                        if (diffServPanel.isExpanded()) {
                                            int ct = ((DiffservPanel) diffServPanel.getPanel()).getClassType();
                                            float resv = Math.min(first.getReservedBandwidthCT(ct), second.getReservedBandwidthCT(ct));
                                            rParam.setBandwidth(resv * bw / 100);
                                        } else {
                                            float resv = Math.min(first.getReservedBandwidth(), second.getReservedBandwidth());
                                            rParam.setBandwidth(resv * bw / 100);
                                        }
                                }

                                if (diffServPanel.isExpanded()) {
                                    DiffservPanel diffserv = (DiffservPanel) diffServPanel.getPanel();
                                    rParam.setClassType(diffserv.getClassType());
                                    rParam.setSetup(diffserv.getSetupLevel());
                                    rParam.setHolding(diffserv.getHoldingLevel());
                                }
                                rParam.putAllRoutingAlgorithmParameter(params.toHashMap());

                                paramList.add(rParam);
                            }
                        }
                    }
                }

                progressBar.setMessage("Sorting LSPs");

                if (orderCombo.getSelectedItem() == Order.DECREASING_BW_ORDER) {
                    Collections.sort(paramList, new Comparator<LSPRoutingParameter>() {
                        public int compare(LSPRoutingParameter o, LSPRoutingParameter o1) {
                            return new Float(o1.getBandwidth()).compareTo(new Float(o.getBandwidth()));
                        }
                    });
                } else if (orderCombo.getSelectedItem() == Order.INCREASING_BW_ORDER) {
                    Collections.sort(paramList, new Comparator<LSPRoutingParameter>() {
                        public int compare(LSPRoutingParameter o, LSPRoutingParameter o1) {
                            return new Float(o.getBandwidth()).compareTo(new Float(o1.getBandwidth()));
                        }
                    });
                } else if (orderCombo.getSelectedItem() == Order.SHUFFLE_ORDER) {
                    Collections.shuffle(paramList);
                } else {
                    logger.error("Sorting scheme unknown.");
                    MainWindow.getInstance().errorMessage("Sorting scheme unknown.");
                    return;
                }

                progressBar.setMessage("Establishing LSP");
                int progress = 0;
                progressBar.setMaximum(paramList.size());

                boolean error = false;
                logger.info("Routing " + paramList.size() + " bypass LSPs");
                for (LSPBypassRoutingParameter param : paramList) {
                    progressBar.setMessage("Routing LSP " + progress++ + " of " + paramList.size());
                    try {
                        TotemActionList list = routing.routeBypass(domain, param);
                        for (Object o : list) {
                            try {
                                ((TotemAction)o).execute();
                            } catch (TotemActionExecutionException e1) {
                                //e1.printStackTrace();
                                logger.error("Error while trying to execute the action for bypass " + param.getProtectedLink().get(0) + " to destination " + param.getDstNode());
                            }
                        }
                    } catch (RoutingException e1) {
                        //e1.printStackTrace();
                        logger.error("Error while routing bypass protecting " + param.getProtectedLink().get(0) + " to destination " + param.getDstNode());
                        error = true;
                    } catch (NoRouteToHostException e1) {
                        //e1.printStackTrace();
                        logger.error("No route to host while routing bypass protecting " + param.getProtectedLink().get(0) + " to destination " + param.getDstNode());
                        error = true;
                    }
                }

                if (error) {
                    MainWindow.getInstance().errorMessage("Some errors occurs. See output for details.");
                } else {
                    dispose();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                pBarDialog.dispose();
            }
        }
    }

    /**
     * Listener that display the parameters in the given ParamTable, corresponding to the chosen algorithm in the comboBox
     */
    private class AlgoComboListener implements ActionListener {
        ParamTable table = null;

        /**
         * @param update The Table in wich to display the algorithm parameter
         */
        public AlgoComboListener(ParamTable update) {
            this.table = update;
        }

        public void actionPerformed(ActionEvent e) {
            JComboBox combo = (JComboBox) e.getSource();

            if (combo.getSelectedItem() == null) {
                table.empty();
                return;
            }

            LSPBypassRouting routingAlgo = (LSPBypassRouting) combo.getSelectedItem();

            table.fill(routingAlgo.getBypassRoutingParameters());
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
