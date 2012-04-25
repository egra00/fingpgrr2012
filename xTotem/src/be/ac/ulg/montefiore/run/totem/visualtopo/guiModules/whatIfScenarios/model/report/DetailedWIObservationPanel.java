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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.report;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.routingGUIModule.IGPRoutingOptionsPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.report.impl.*;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidLinkLoadComputerException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/*
* Changes:
* --------
* - 28-Feb-2008: Replace Load and utilisation panel (GMO) 
*/

/**
* Panel designed to choose the observation reports of a What-If scenario. 
*
* <p>Creation date: 29/05/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class DetailedWIObservationPanel extends JPanel {
    private Domain domain;
    private DefaultListModel listModel;
    private JTabbedPane obsPane;

    public DetailedWIObservationPanel(Domain domain) {
        super(new GridBagLayout());
        this.domain = domain;
        setupUI();
    }

    private void setupUI() {
        setBorder(BorderFactory.createTitledBorder("Observation"));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.5;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;

        obsPane = new JTabbedPane();

        /*LoadObservationPanel panel1 = new LoadObservationPanel();
        obsPane.add(panel1.getObservationName(), panel1);
        UtilizationObservationPanel panel2 = new UtilizationObservationPanel();
        obsPane.add(panel2.getObservationName(), panel2);
        */
        ReservedBwObservationPanel panel3 = new ReservedBwObservationPanel();
        obsPane.add(panel3.getObservationName(), panel3);
        LinkLoadComputerObservationPanel panel4 = new LinkLoadComputerObservationPanel();
        obsPane.add(panel4.getObservationName(), panel4);
        LinkLoadComputerUtilisationObservationPanel panel5 = new LinkLoadComputerUtilisationObservationPanel();
        obsPane.add(panel5.getObservationName(), panel5);

        add(obsPane, c);

        c.weightx = 0.5;
        c.gridx++;

        listModel = new DefaultListModel();
        JList list = new JList(listModel);
        add(new JScrollPane(list), c);

        c.gridy++;
        c.gridx = 0;
        c.weighty = 0;
        c.gridwidth = 2;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton addObsButton = new JButton("Add Observation Report");
        addObsButton.addActionListener(new AddObservationAcionListener());
        buttonPanel.add(addObsButton);
        add(buttonPanel, c);

    }

    public java.util.List<WIReport> getReports() {
        List<WIReport> list = new ArrayList<WIReport>();
        for (Object o : listModel.toArray()) {
            list.add((WIReport)o);
        }
        return list;
    }

    public Dimension getPreferredSize() {
        Dimension dim = super.getPreferredSize();
        return new Dimension(dim.width+100, dim.height);

    }

    private interface ReportBuilder {
        public WIReport getReport();
    }

    private class LoadObservationPanel extends IGPRoutingOptionsPanel implements ReportBuilder {

        public String getObservationName() {
            return "Load";
        }

        public WIReport getReport() {
            return new LoadWIReport(domain, getStrategy(domain));
        }
    }

    private class UtilizationObservationPanel extends IGPRoutingOptionsPanel implements ReportBuilder {

        public String getObservationName() {
            return "Utilization";
        }

        public WIReport getReport() {
            return new UtilisationWIReport(domain, getStrategy(domain));
        }
    }

    private class ReservedBwObservationPanel extends JPanel implements ReportBuilder {
        private JComboBox combo;

        public ReservedBwObservationPanel() {
            super(new GridBagLayout());
            setupUI();
        }

        public String getObservationName() {
            return "Reserved Bandwidth";
        }

        private void setupUI() {

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            add(new JLabel("Select class type:"), c);
            c.gridy++;
            c.anchor = GridBagConstraints.NORTH;

            combo = new JComboBox();

            combo.addItem("All CTs");

            if (domain != null) {
                for (int ct : domain.getAllCTId()) {
                    combo.addItem("CT " + ct);
                }
            }

            add(combo, c);
        }

        public WIReport getReport() {
            String selected = (String)combo.getSelectedItem();
            if (selected.equals("All CTs")) {
                return new ReservedBandwidthWIReport(domain);
            } else {
                int ct = Integer.valueOf(selected.substring(3, selected.length()));
                return new ReservedBandwidthWIReport(domain, ct);
            }
        }
    }

    private class LinkLoadComputerObservationPanel extends JPanel implements ReportBuilder {
        protected final JComboBox initialLLCCombo;
        protected final JComboBox finalLLCCombo;

        public LinkLoadComputerObservationPanel() {
            super();

            initialLLCCombo = new JComboBox();
            initialLLCCombo.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e) {
                    finalLLCCombo.setSelectedItem(initialLLCCombo.getSelectedItem());
                }
            });
            finalLLCCombo = new JComboBox();
            fill();

            setupUI();
        }

        private void setupUI() {
            setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;

            add(new JLabel("Select before scenario LinkLoadComputer:"), c);
            c.gridy++;

            add(initialLLCCombo, c);
            c.gridy++;

            add(new JLabel("Select after scenario LinkLoadComputer:"), c);
            c.gridy++;

            add(finalLLCCombo, c);
            c.gridy++;
        }

        public void fill() {
            for (String llcId : LinkLoadComputerManager.getInstance().getLinkLoadComputerIds(domain)) {
                initialLLCCombo.addItem(llcId);
                finalLLCCombo.addItem(llcId);
            }
        }

        public String getObservationName() {
            return "Load";
        }

        public WIReport getReport() {
            if (initialLLCCombo.getSelectedItem() == null || finalLLCCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(DetailedWIObservationPanel.this, "Link load should be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            LinkLoadComputer initialLLC;
            LinkLoadComputer finalLLC;

            try {
                initialLLC = LinkLoadComputerManager.getInstance().getLinkLoadComputer(domain, (String)initialLLCCombo.getSelectedItem());
            } catch (InvalidLinkLoadComputerException e) {
                JOptionPane.showMessageDialog(DetailedWIObservationPanel.this, "Link load computer not found: " + (String)initialLLCCombo.getSelectedItem(), "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            try {
                finalLLC = LinkLoadComputerManager.getInstance().getLinkLoadComputer(domain, (String)finalLLCCombo.getSelectedItem());
            } catch (InvalidLinkLoadComputerException e) {
                JOptionPane.showMessageDialog(DetailedWIObservationPanel.this, "Link load computer not found: " + (String)finalLLCCombo.getSelectedItem(), "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            return new LinkLoadComputerLoadWIReport(domain, initialLLC, finalLLC);
        }
    }

    private class LinkLoadComputerUtilisationObservationPanel extends LinkLoadComputerObservationPanel {
        public LinkLoadComputerUtilisationObservationPanel() {
            super();
        }

        public String getObservationName() {
            return "Utilisation";
        }

        public WIReport getReport() {
            if (initialLLCCombo.getSelectedItem() == null || finalLLCCombo.getSelectedItem() == null) {
                JOptionPane.showMessageDialog(DetailedWIObservationPanel.this, "Link load should be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            LinkLoadComputer initialLLC;
            LinkLoadComputer finalLLC;

            try {
                initialLLC = LinkLoadComputerManager.getInstance().getLinkLoadComputer(domain, (String)initialLLCCombo.getSelectedItem());
            } catch (InvalidLinkLoadComputerException e) {
                JOptionPane.showMessageDialog(DetailedWIObservationPanel.this, "Link load computer not found: " + (String)initialLLCCombo.getSelectedItem(), "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            try {
                finalLLC = LinkLoadComputerManager.getInstance().getLinkLoadComputer(domain, (String)finalLLCCombo.getSelectedItem());
            } catch (InvalidLinkLoadComputerException e) {
                JOptionPane.showMessageDialog(DetailedWIObservationPanel.this, "Link load computer not found: " + (String)finalLLCCombo.getSelectedItem(), "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            return new LinkLoadComputerUtilisationWIReport(domain, initialLLC, finalLLC);
        }
    }

    private class AddObservationAcionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ReportBuilder builder = (ReportBuilder)obsPane.getSelectedComponent();
            WIReport report = builder.getReport();

            if (report == null) return;

            if (!listModel.contains(report))
                listModel.addElement(report);
        }
    }


}
