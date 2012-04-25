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

import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.*;
import be.ac.ulg.montefiore.run.totem.repository.model.SPF;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFHopCount;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFInvCap;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFTEMetric;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;

import javax.swing.*;
import java.awt.*;

/*
* Changes:
* --------
*
*/

/**
 * Panel to choose the IGP routing options. This creates a LinkLoadStrategy object from the chosen options.
 * The object is build and returned by {@link #getStrategy(be.ac.ulg.montefiore.run.totem.domain.model.Domain)}} .
 * <p/>
 * <p>Creation date: 29/05/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class IGPRoutingOptionsPanel extends JPanel {
    private StrategyPanel strategyPanel = null;
    private MetricPanel metricPanel = null;
    private RoutingOptionsPanel optionsPanel = null;

    public IGPRoutingOptionsPanel() {
        super(new GridBagLayout());
        setupUI();
    }

    private void setupUI() {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 0;

        metricPanel = new MetricPanel();
        add(metricPanel, c);
        c.gridx++;

        strategyPanel = new StrategyPanel();
        add(strategyPanel, c);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;

        optionsPanel = new RoutingOptionsPanel();
        add(optionsPanel, c);
    }

    /**
     * Create the link load strategy corresponding to the chosen options. The strategy has a null traffic matrix.
     * @return the newly created LinkLoadStrategy object.
     */
    public LinkLoadStrategy getStrategy(Domain domain) {
        LinkLoadStrategy lls = strategyPanel.getStrategy(domain);
        lls.setSPFAlgo(metricPanel.getSPF());
        lls.setECMP(optionsPanel.getECMP());

        return lls;
    }

    /**
     * Options: ecmp enabled or not.
     */
    static class RoutingOptionsPanel extends JPanel {
        private JCheckBox ECMPChk;

        public RoutingOptionsPanel() {
            super(new GridLayout(0, 1, 10, 10));
            setupUI();
        }

        private void setupUI() {
            setBorder(BorderFactory.createTitledBorder("Options"));
            ECMPChk = new JCheckBox("ECMP (Equal Cost Multi Path)", true);
            add(ECMPChk);
        }

        public boolean getECMP() {
            return ECMPChk.isSelected();
        }

    }

    /**
     * Panel to choose the strategy to use.
     */
    static class StrategyPanel extends JPanel {
        private ButtonGroup strategyGroup = null;

        public StrategyPanel() {
            super(new GridLayout(0, 1, 10, 10));
            setupUI();
        }

        private void setupUI() {
            setBorder(BorderFactory.createTitledBorder("Select Strategy"));

            strategyGroup = new ButtonGroup();
            JRadioButton strategy;
            strategy = new JRadioButton("Full IP routing");
            strategy.setSelected(true);
            strategy.setActionCommand("Full IP routing");
            strategyGroup.add(strategy);
            add(strategy);
            strategy = new JRadioButton("Basic IGP shortcut");
            strategy.setActionCommand("Basic IGP shortcut");
            strategyGroup.add(strategy);
            add(strategy);
            strategy = new JRadioButton("IGP shortcut");
            strategy.setActionCommand("IS");
            strategyGroup.add(strategy);
            add(strategy);
            strategy = new JRadioButton("Overlay");
            strategy.setActionCommand("Overlay");
            strategyGroup.add(strategy);
            add(strategy);
        }


        public LinkLoadStrategy getStrategy(Domain domain) {
            String command = strategyGroup.getSelection().getActionCommand();
            LinkLoadStrategy lls;
            if (command.equals("Full IP routing")) {
                lls = new SPFLinkLoadStrategy(domain, null);
            } else if (command.equals("Basic IGP shortcut")) {
                lls = new BasicIGPShortcutStrategy(domain, null);
            } else if (command.equals("IS")) {
                lls = new IGPShortcutStrategy(domain, null);
            } else if (command.equals("Overlay")) {
                lls = new OverlayStrategy(domain, null);
            } else {
                JOptionPane.showMessageDialog(this, "Command not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            return lls;
        }
    }

    /**
     * Panel to choose the metric to use.
     */
    static class MetricPanel extends JPanel {
        private ButtonGroup metricGroup = null;

        public MetricPanel() {
            super(new GridLayout(0, 1, 10, 10));
            setupUI();
        }

        private void setupUI() {
            setBorder(BorderFactory.createTitledBorder("Select Metric"));
            metricGroup = new ButtonGroup();
            JRadioButton metric = new JRadioButton("Metric");
            metric.setActionCommand("Metric");
            metric.setSelected(true);
            metricGroup.add(metric);
            add(metric);
            metric = new JRadioButton("TE Metric");
            metric.setActionCommand("TE Metric");
            metricGroup.add(metric);
            add(metric);
            metric = new JRadioButton("Inv. Capacity");
            metric.setActionCommand("Inv. Capacity");
            metricGroup.add(metric);
            add(metric);
            metric = new JRadioButton("Hop Count");
            metric.setActionCommand("Hop Count");
            metricGroup.add(metric);
            add(metric);
        }

        public SPF getSPF() {
            String command = metricGroup.getSelection().getActionCommand();

            SPF spf = null;
            if (command.equals("Metric")) {
                spf = new CSPF();
            } else if (command.equals("TE Metric")) {
                spf = new CSPFTEMetric();
            } else if (command.equals("Inv. Capacity")) {
                spf = new CSPFInvCap();
            } else if (command.equals("Hop Count")) {
                spf = new CSPFHopCount();
            } else {
                JOptionPane.showMessageDialog(this, "Command not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            return spf;
        }
    }


}
