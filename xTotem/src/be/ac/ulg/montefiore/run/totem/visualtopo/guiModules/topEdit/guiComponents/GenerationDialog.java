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

import be.ac.ulg.montefiore.run.totem.topgen.topology.TopologyGenerator;
import be.ac.ulg.montefiore.run.totem.topgen.topology.WrapperBrite;
import be.ac.ulg.montefiore.run.totem.topgen.topology.TopologyGeneratorException;
import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.DomainImpl;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 31-Oct-2007: Rewrite part to use new generation interface. (GMO)
* - 16-Nov-2007: Redesign dialog (GMO)
* - 23-Nov-2007: Remove unused parameters (GMO)
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 1/10/2007
*
* @author Georges Nimubona (nimubonageorges@hotmail.com)
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class GenerationDialog extends JDialog {
    private final static Logger logger = Logger.getLogger(GenerationDialog.class);

    /* The selected type of topology. */
    private final JComboBox topologyTypeCombo;

    /* The selected model. */
    private final JComboBox model0Combo;
    private final JComboBox model1Combo;

    /* Topology type specific parameters. */
    private final JComboBox edgeConnectionModelCombo;
    private final JComboBox interBWDistCombo;
    private final JComboBox groupingModelCombo;
    private final JComboBox ASAssignmentCombo;
    private final JTextField k;
    private final JTextField numberOfAS;
    private final JTextField maxInterBW;
    private final JTextField minInterBW;
    private final JComboBox intraBWDistCombo;
    private final JTextField maxIntraBW;
    private final JTextField minIntraBW;

    /* Parameters for the model. */
    private final JTextField topHS;
    private final JTextField bottomHS;
    private final JTextField topLS;
    private final JTextField bottomLS;
    private final JTextField topN;
    private final JTextField bottomN;
    private final JComboBox topNodePlacementCombo;
    private final JComboBox bottomNodePlacementCombo;
    private final JComboBox topGrowthTypeCombo;
    private final JComboBox bottomGrowthTypeCombo;
    private final JComboBox topPrefConnCombo;
    private final JComboBox bottomPrefConnCombo;
    private final JTextField topAlpha;
    private final JTextField bottomAlpha;
    private final JTextField topBeta;
    private final JTextField bottomBeta;
    private final JTextField topM;
    private final JTextField bottomM;


    /* General Parameters */
    private final JCheckBox mustBeConnected, mustBeDualConnected;
    private final JComboBox metricCombo;
    private final JTextField numberOfTopo;

    /* Buttons */
    private JButton generate;
    private JButton cancel;

    private TopologyGenerator wb;
    private HashMap<String, ParameterDescriptor> params;

    private final static String[] topologyType = {"1 Level: AS Only", "1 Level: Router (IP) Only", "2 Level: Top-Down", "2 Level: Bottom-Up"};
    private final static String[] model = {"Waxman", "Barabasi-Albert 1", "Barabasi-Albert 2", "GLP"};

    private final JComponent[] commonTopComponents;
    private final JComponent[] commonBottomComponents;
    private final JComponent[] intraBwComponents;
    private final JComponent[] interBwComponents;

    public GenerationDialog() {
        super(TopEditGUI.getInstance(), "Topology generator");
        wb = new WrapperBrite();
        params = new HashMap<String, ParameterDescriptor>();

        for (ParameterDescriptor param : wb.getAvailableParameters()) {
            params.put(param.getName(), param);
        }

        ActionListener typeActionListener = new TypeComboActionListener();
        topologyTypeCombo = new JComboBox(topologyType);
        topologyTypeCombo.addActionListener(typeActionListener);
        model0Combo = new JComboBox(model);
        model0Combo.addActionListener(typeActionListener);
        model1Combo = new JComboBox(model);
        model1Combo.addActionListener(typeActionListener);

        String[] edgeConnectionModel = {"Random", "Smallest-Degree", "Smallest-Degree NonLeaf", "Smallest k-Degree"};
        String[] groupingModel = {"Random Walk", "Random Pick"};
        String[] distribution = {"Constant", "Uniform", "Exponential", "Heavy Tailed"};
        String[] placement = {"Random", "Heavy Tailed"};
        String[] growthType = {"All", "Incremental"};
        String[] prefConn = {"None", "On"};
        String[] metric = {"Hop Count", "Inverse of BW"};

        edgeConnectionModelCombo = new JComboBox(edgeConnectionModel);
        groupingModelCombo = new JComboBox(groupingModel);
        interBWDistCombo = new JComboBox(distribution);
        intraBWDistCombo = new JComboBox(distribution);
        topNodePlacementCombo = new JComboBox(placement);
        topGrowthTypeCombo = new JComboBox(growthType);
        bottomNodePlacementCombo = new JComboBox(placement);
        bottomGrowthTypeCombo = new JComboBox(growthType);
        topPrefConnCombo = new JComboBox(prefConn);
        ASAssignmentCombo = new JComboBox(distribution);
        bottomPrefConnCombo = new JComboBox(prefConn);
        metricCombo = new JComboBox(metric);

        numberOfAS = new JTextField();
        maxInterBW = new JTextField();
        minInterBW = new JTextField();
        maxIntraBW = new JTextField();
        minIntraBW = new JTextField();
        topHS = new JTextField();
        bottomHS = new JTextField();
        topLS = new JTextField();
        bottomLS = new JTextField();
        topN = new JTextField();
        bottomN = new JTextField();
        topAlpha = new JTextField();
        bottomAlpha = new JTextField();
        topBeta = new JTextField();
        bottomBeta = new JTextField();
        topM = new JTextField();
        bottomM = new JTextField();
        numberOfTopo = new JTextField();
        k = new JTextField();

        mustBeConnected = new JCheckBox("Must be connected");
        mustBeDualConnected = new JCheckBox("Must be dual connected");

        setupUI();

        commonTopComponents = new JComponent[] {topN, topHS, topLS, topNodePlacementCombo, topM};
        commonBottomComponents = new JComponent[] {bottomN, bottomHS, bottomLS, bottomNodePlacementCombo, bottomM};
        intraBwComponents = new JComponent[] {intraBWDistCombo, minIntraBW, maxIntraBW};
        interBwComponents = new JComponent[] {interBWDistCombo, minInterBW, maxInterBW};

        // Fill default params
        setDefaultParam();
        pack();
        setVisible(true);
    }

    private class TypeComboActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            //these 3 are only for bottom up model.
            groupingModelCombo.setEnabled(false);
            ASAssignmentCombo.setEnabled(false);
            numberOfAS.setEnabled(false);

            //these are only for top down model
            edgeConnectionModelCombo.setEnabled(false);
            k.setEnabled(false);

            if (topologyTypeCombo.getSelectedItem().equals(topologyType[0]) || topologyTypeCombo.getSelectedItem().equals(topologyType[1])) {
                // only 1 model used
                model1Combo.setEnabled(false);

                for (JComponent c : commonTopComponents) {
                    c.setEnabled(true);
                }
                for (JComponent c : commonBottomComponents) {
                    c.setEnabled(false);
                }
                for (JComponent c : intraBwComponents) {
                    c.setEnabled(true);
                }
                for (JComponent c : interBwComponents) {
                    c.setEnabled(false);
                }

                bottomAlpha.setEnabled(false);
                bottomBeta.setEnabled(false);
                bottomGrowthTypeCombo.setEnabled(false);

                if (model0Combo.getSelectedItem().equals(model[0]) || model0Combo.getSelectedItem().equals(model[2]) || model0Combo.getSelectedItem().equals(model[3])) { //waxman - BA2 - GLP
                    topAlpha.setEnabled(true);
                    topBeta.setEnabled(true);
                    if (model0Combo.getSelectedItem().equals(model[0])) { //waxman
                        topGrowthTypeCombo.setEnabled(true);
                    } else topGrowthTypeCombo.setEnabled(false);
                } else {
                    topAlpha.setEnabled(false);
                    topBeta.setEnabled(false);
                    topGrowthTypeCombo.setEnabled(false);
                }
            } else if (topologyTypeCombo.getSelectedItem().equals(topologyType[2])) { // top Down
                edgeConnectionModelCombo.setEnabled(true);
                k.setEnabled(true);

                model1Combo.setEnabled(true);

                for (JComponent c : commonTopComponents) {
                    c.setEnabled(true);
                }
                for (JComponent c : commonBottomComponents) {
                    c.setEnabled(true);
                }
                for (JComponent c : intraBwComponents) {
                    c.setEnabled(true);
                }
                for (JComponent c : interBwComponents) {
                    c.setEnabled(true);
                }

                if (model0Combo.getSelectedItem().equals(model[0]) || model0Combo.getSelectedItem().equals(model[2]) || model0Combo.getSelectedItem().equals(model[3])) { //waxman - BA2 - GLP
                    topAlpha.setEnabled(true);
                    topBeta.setEnabled(true);
                    if (model0Combo.getSelectedItem().equals(model[0])) { //waxman
                        topGrowthTypeCombo.setEnabled(true);
                    } else topGrowthTypeCombo.setEnabled(false);
                } else {
                    topAlpha.setEnabled(false);
                    topBeta.setEnabled(false);
                    topGrowthTypeCombo.setEnabled(false);
                }

                if (model1Combo.getSelectedItem().equals(model[0]) || model1Combo.getSelectedItem().equals(model[2]) || model1Combo.getSelectedItem().equals(model[3])) { //waxman - BA2 - GLP
                    bottomAlpha.setEnabled(true);
                    bottomBeta.setEnabled(true);
                    if (model1Combo.getSelectedItem().equals(model[0])) { //waxman
                        bottomGrowthTypeCombo.setEnabled(true);
                    } else bottomGrowthTypeCombo.setEnabled(false);
                } else {
                    bottomAlpha.setEnabled(false);
                    bottomBeta.setEnabled(false);
                    bottomGrowthTypeCombo.setEnabled(false);
                }
            } else if (topologyTypeCombo.getSelectedItem().equals(topologyType[3])) { // bottom up
                groupingModelCombo.setEnabled(true);
                ASAssignmentCombo.setEnabled(true);
                numberOfAS.setEnabled(true);

                model1Combo.setEnabled(false);

                for (JComponent c : commonTopComponents) {
                    c.setEnabled(true);
                }
                for (JComponent c : commonBottomComponents) {
                    c.setEnabled(false);
                }
                for (JComponent c : intraBwComponents) {
                    c.setEnabled(true);
                }
                for (JComponent c : interBwComponents) {
                    c.setEnabled(true);
                }

                bottomAlpha.setEnabled(false);
                bottomBeta.setEnabled(false);
                bottomGrowthTypeCombo.setEnabled(false);

                if (model0Combo.getSelectedItem().equals(model[0]) || model0Combo.getSelectedItem().equals(model[2]) || model0Combo.getSelectedItem().equals(model[3])) { //waxman - BA2 - GLP
                    topAlpha.setEnabled(true);
                    topBeta.setEnabled(true);
                    if (model0Combo.getSelectedItem().equals(model[0])) { //waxman
                        topGrowthTypeCombo.setEnabled(true);
                    } else topGrowthTypeCombo.setEnabled(false);
                } else {
                    topAlpha.setEnabled(false);
                    topBeta.setEnabled(false);
                    topGrowthTypeCombo.setEnabled(false);
                }

            }
        }
    }

    private void setupUI() {

        JPanel buttonPanel = new JPanel();
        JPanel paramPanel = new JPanel();

        JPanel typePanel = new JPanel();
        JPanel topModelPanel = new JPanel();
        JPanel bottomModelPanel = new JPanel();
        JPanel bwPanel = new JPanel();
        JPanel generalParamPanel = new JPanel();

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        //c.ipadx = 100;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 10, 2, 10);

        typePanel.setLayout(new GridBagLayout());
        typePanel.setBorder(BorderFactory.createTitledBorder("Model type"));
        typePanel.add(new JLabel("Topology type"), c);
        c.gridy++;
        typePanel.add(topologyTypeCombo, c);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 1;

        typePanel.add(new JLabel("Edge connection model"), c);
        c.gridx++;
        typePanel.add(new JLabel("Grouping model"), c);
        c.gridx = 0;
        c.gridy++;
        typePanel.add(edgeConnectionModelCombo, c);
        c.gridx++;
        typePanel.add(groupingModelCombo, c);
        c.gridx = 0;
        c.gridy++;
        typePanel.add(new JLabel("K"), c);
        c.gridx++;
        typePanel.add(new JLabel("AS assignment model"), c);
        c.gridx = 0;
        c.gridy++;
        typePanel.add(k, c);
        c.gridx++;
        typePanel.add(ASAssignmentCombo, c);
        c.gridy++;
        typePanel.add(new JLabel("Number of AS"), c);
        c.gridy++;
        typePanel.add(numberOfAS, c);

        c.gridx = 0;
        c.gridy = 0;
        topModelPanel.setLayout(new GridBagLayout());
        topModelPanel.setBorder(BorderFactory.createTitledBorder("Model 0"));
        topModelPanel.add(new JLabel("Type"), c);
        c.gridy++;
        topModelPanel.add(model0Combo, c);
        c.gridy++;
        topModelPanel.add(new JLabel("Top N"), c);
        c.gridy++;
        topModelPanel.add(topN, c);
        c.gridy++;
        topModelPanel.add(new JLabel("Top HS"), c);
        c.gridy++;
        topModelPanel.add(topHS, c);
        c.gridy++;
        topModelPanel.add(new JLabel("Top LS"), c);
        c.gridy++;
        topModelPanel.add(topLS, c);
        c.gridy++;
        topModelPanel.add(new JLabel("Top node placement"), c);
        c.gridy++;
        topModelPanel.add(topNodePlacementCombo, c);
        c.gridy++;
        topModelPanel.add(new JLabel("Top M"), c);
        c.gridy++;
        topModelPanel.add(topM, c);
        c.gridy++;
        topModelPanel.add(new JLabel("Top alpha"), c);
        c.gridy++;
        topModelPanel.add(topAlpha, c);
        c.gridy++;
        topModelPanel.add(new JLabel("Top beta"), c);
        c.gridy++;
        topModelPanel.add(topBeta, c);
        c.gridy++;
        topModelPanel.add(new JLabel("Top growth type"), c);
        c.gridy++;
        topModelPanel.add(topGrowthTypeCombo, c);
        /* not used in WrapperBrite
        c.gridy++;
        topModelPanel.add(new JLabel("Top pref conn"), c);
        c.gridy++;
        topModelPanel.add(topPrefConnCombo, c);
        */
        c.gridy = 0;
        bottomModelPanel.setLayout(new GridBagLayout());
        bottomModelPanel.setBorder(BorderFactory.createTitledBorder("Model 1"));
        bottomModelPanel.add(new JLabel("Type"), c);
        c.gridy++;
        bottomModelPanel.add(model1Combo, c);
        c.gridy++;
        bottomModelPanel.add(new JLabel("Bottom N"), c);
        c.gridy++;
        bottomModelPanel.add(bottomN, c);
        c.gridy++;
        bottomModelPanel.add(new JLabel("Bottom HS"), c);
        c.gridy++;
        bottomModelPanel.add(bottomHS, c);
        c.gridy++;
        bottomModelPanel.add(new JLabel("Bottom LS"), c);
        c.gridy++;
        bottomModelPanel.add(bottomLS, c);
        c.gridy++;
        bottomModelPanel.add(new JLabel("Bottom node placement"), c);
        c.gridy++;
        bottomModelPanel.add(bottomNodePlacementCombo, c);
        c.gridy++;
        bottomModelPanel.add(new JLabel("Bottom M"), c);
        c.gridy++;
        bottomModelPanel.add(bottomM, c);
        c.gridy++;
        bottomModelPanel.add(new JLabel("Bottom alpha"), c);
        c.gridy++;
        bottomModelPanel.add(bottomAlpha, c);
        c.gridy++;
        bottomModelPanel.add(new JLabel("Bottom beta"), c);
        c.gridy++;
        bottomModelPanel.add(bottomBeta, c);
        c.gridy++;
        bottomModelPanel.add(new JLabel("Bottom growth type"), c);
        c.gridy++;
        bottomModelPanel.add(bottomGrowthTypeCombo, c);
        /* not used in WrapperBrite
        c.gridy++;
        bottomModelPanel.add(new JLabel("Bottom pref conn"), c);
        c.gridy++;
        bottomModelPanel.add(bottomPrefConnCombo, c);
        */
        
        c.gridx = 0;
        c.gridy = 0;

        bwPanel.setLayout(new GridBagLayout());
        bwPanel.setBorder(BorderFactory.createTitledBorder("Bandwidth parameters"));
        bwPanel.add(new JLabel("Intra BW distribution"), c);
        c.gridx++;
        bwPanel.add(new JLabel("Inter BW distribution"), c);
        c.gridx = 0;
        c.gridy++;
        bwPanel.add(intraBWDistCombo, c);
        c.gridx++;
        bwPanel.add(interBWDistCombo, c);
        c.gridx = 0;
        c.gridy++;
        bwPanel.add(new JLabel("Max intra BW"), c);
        c.gridx++;
        bwPanel.add(new JLabel("Max inter BW"), c);
        c.gridx = 0;
        c.gridy++;
        bwPanel.add(maxIntraBW, c);
        c.gridx++;
        bwPanel.add(maxInterBW, c);
        c.gridx = 0;
        c.gridy++;
        bwPanel.add(new JLabel("Min intra BW"), c);
        c.gridx++;
        bwPanel.add(new JLabel("Min inter BW"), c);
        c.gridx = 0;
        c.gridy++;
        bwPanel.add(minIntraBW, c);
        c.gridx++;
        bwPanel.add(minInterBW, c);

        generalParamPanel.setLayout(new GridBagLayout());
        generalParamPanel.setBorder(BorderFactory.createTitledBorder("General parameters"));
        c.gridx = 0;
        c.gridy = 0;
        generalParamPanel.add(new JLabel("Number of Topologies"), c);
        c.gridx++;
        generalParamPanel.add(new JLabel("Metric"), c);
        c.gridx = 0;
        c.gridy++;
        generalParamPanel.add(numberOfTopo, c);
        c.gridx++;
        generalParamPanel.add(metricCombo, c);
        c.gridx = 0;
        c.gridy++;
        generalParamPanel.add(mustBeConnected, c);
        c.gridx++;
        generalParamPanel.add(mustBeDualConnected, c);


        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(generate = new JButton("Generate"));
        buttonPanel.add(cancel = new JButton("Cancel"));
        generate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                generate();
            }
        });
        cancel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;

        paramPanel.setLayout(new GridBagLayout());
        paramPanel.add(typePanel, c);
        c.gridwidth = 1;
        c.gridy++;
        paramPanel.add(topModelPanel, c);
        c.gridx++;
        paramPanel.add(bottomModelPanel, c);
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy++;
        paramPanel.add(bwPanel, c);
        c.gridy++;
        paramPanel.add(generalParamPanel, c);

        setLayout(new BorderLayout());
        add(paramPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

    }

    private void generate() {
        // Fetch the user specified parameters
        try {
            wb.setParam("topologyType", (String)topologyTypeCombo.getSelectedItem());
            wb.setParam("topLevelModel", (String)model0Combo.getSelectedItem());
            wb.setParam("bottomLevelModel", (String)model1Combo.getSelectedItem());
            wb.setParam("edgeConnectionModel", (String)edgeConnectionModelCombo.getSelectedItem());
            wb.setParam("groupingModel", (String)groupingModelCombo.getSelectedItem());
            wb.setParam("interBWDist", (String)interBWDistCombo.getSelectedItem());
            wb.setParam("intraBWDist", (String)intraBWDistCombo.getSelectedItem());
            wb.setParam("asAssignment", (String)ASAssignmentCombo.getSelectedItem());
            wb.setParam("topNodePlacement", (String)topNodePlacementCombo.getSelectedItem());
            wb.setParam("bottomNodePlacement", (String)bottomNodePlacementCombo.getSelectedItem());
            wb.setParam("topGrowthType", (String)topGrowthTypeCombo.getSelectedItem());
            wb.setParam("bottomGrowthType", (String)bottomGrowthTypeCombo.getSelectedItem());
            wb.setParam("topPreferentialConnectivity", (String)topPrefConnCombo.getSelectedItem());
            wb.setParam("bottomPreferentialConnectivity", (String)bottomPrefConnCombo.getSelectedItem());
            wb.setParam("metric", (String)metricCombo.getSelectedItem());
            wb.setParam("interBWMax", maxInterBW.getText());
            wb.setParam("intraBWMax", maxIntraBW.getText());
            wb.setParam("interBWMin", minInterBW.getText());
            wb.setParam("intraBWMin", minIntraBW.getText());
            wb.setParam("topHS", topHS.getText());
            wb.setParam("topLS", topLS.getText());
            wb.setParam("topN", topN.getText());
            wb.setParam("topM", topM.getText());
            wb.setParam("topAlpha", topAlpha.getText());
            wb.setParam("topBeta", topBeta.getText());
            wb.setParam("bottomHS", bottomHS.getText());
            wb.setParam("bottomLS", bottomLS.getText());
            wb.setParam("bottomN", bottomN.getText());
            wb.setParam("bottomM", bottomM.getText());
            wb.setParam("bottomAlpha", bottomAlpha.getText());
            wb.setParam("bottomBeta", bottomBeta.getText());
            wb.setParam("numTopologies", numberOfTopo.getText());
            wb.setParam("k", k.getText());
            wb.setParam("numAS", numberOfAS.getText());
            wb.setParam("mustBeConnected", Boolean.toString(mustBeConnected.isSelected()));
            wb.setParam("mustBeDualConnected", Boolean.toString(mustBeDualConnected.isSelected()));
        }
        catch (InvalidParameterException e) {
            e.printStackTrace();
            logger.warn(e.getMessage());
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            java.util.List<Domain> domains = wb.generate();
            for(Domain d : domains) {
                /* commented by GMO. Seems useless
                java.util.List<Node> nodes = d.getAllNodes();
                for(Node n : nodes) {
                    n.setLatitude(0);
                    n.setLongitude(0);
                }*/
                d.setURI(new URI("./"));
                // assign a private asid
                int asId = (int)Math.round(64512 + Math.random() * 1022);
                d.setASID(asId);
                TopEditGUI.getInstance().newEdition((DomainImpl)d);
            }
        }
        catch (URISyntaxException e) {
            logger.warn(e.getMessage());
        }
        catch (TopologyGeneratorException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        dispose();
    }

    private void setDefaultParam() {

        topologyTypeCombo.setSelectedItem((String)params.get("topologyType").getDefaultValue());
        model0Combo.setSelectedItem((String)params.get("topLevelModel").getDefaultValue());
        model1Combo.setSelectedItem((String)params.get("bottomLevelModel").getDefaultValue());
        edgeConnectionModelCombo.setSelectedItem((String)params.get("edgeConnectionModel").getDefaultValue());
        groupingModelCombo.setSelectedItem((String)params.get("groupingModel").getDefaultValue());
        interBWDistCombo.setSelectedItem((String)params.get("interBWDist").getDefaultValue());
        intraBWDistCombo.setSelectedItem((String)params.get("intraBWDist").getDefaultValue());
        ASAssignmentCombo.setSelectedItem((String)params.get("asAssignment").getDefaultValue());
        topNodePlacementCombo.setSelectedItem((String)params.get("topNodePlacement").getDefaultValue());
        bottomNodePlacementCombo.setSelectedItem((String)params.get("bottomNodePlacement").getDefaultValue());
        topGrowthTypeCombo.setSelectedItem((String)params.get("topGrowthType").getDefaultValue());
        bottomGrowthTypeCombo.setSelectedItem((String)params.get("bottomGrowthType").getDefaultValue());
        topPrefConnCombo.setSelectedItem((String)params.get("topPreferentialConnectivity").getDefaultValue());
        bottomPrefConnCombo.setSelectedItem((String)params.get("bottomPreferentialConnectivity").getDefaultValue());
        metricCombo.setSelectedItem((String)params.get("metric").getDefaultValue());

        maxInterBW.setText(String.valueOf(params.get("interBWMax").getDefaultValue()));
        minInterBW.setText(String.valueOf(params.get("interBWMin").getDefaultValue()));
        maxIntraBW.setText(String.valueOf(params.get("intraBWMax").getDefaultValue()));
        minIntraBW.setText(String.valueOf(params.get("intraBWMin").getDefaultValue()));

        topHS.setText(String.valueOf(params.get("topHS").getDefaultValue()));
        topLS.setText(String.valueOf(params.get("topLS").getDefaultValue()));
        topM.setText(String.valueOf(params.get("topM").getDefaultValue()));
        topN.setText(String.valueOf(params.get("topN").getDefaultValue()));
        topAlpha.setText(String.valueOf(params.get("topAlpha").getDefaultValue()));
        topBeta.setText(String.valueOf(params.get("topBeta").getDefaultValue()));

        bottomHS.setText(String.valueOf(params.get("bottomHS").getDefaultValue()));
        bottomLS.setText(String.valueOf(params.get("bottomLS").getDefaultValue()));
        bottomM.setText(String.valueOf(params.get("bottomM").getDefaultValue()));
        bottomN.setText(String.valueOf(params.get("bottomN").getDefaultValue()));
        bottomAlpha.setText(String.valueOf(params.get("bottomAlpha").getDefaultValue()));
        bottomBeta.setText(String.valueOf(params.get("bottomBeta").getDefaultValue()));

        numberOfTopo.setText(String.valueOf(params.get("numTopologies").getDefaultValue()));
        k.setText(String.valueOf(params.get("k").getDefaultValue()));
        numberOfAS.setText(String.valueOf(params.get("numAS").getDefaultValue()));

        mustBeConnected.setSelected((Boolean)params.get("mustBeConnected").getDefaultValue());
        mustBeDualConnected.setSelected((Boolean)params.get("mustBeConnected").getDefaultValue());
    }
}
