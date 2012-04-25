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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.domainTables;

import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.*;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.text.DecimalFormat;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/*
* Changes:
* --------
*/

/**
 * Displays information about link load. The LinkLoadComputer can be chosen on the panel. It displays ip, mpls load and
 * dropped traffic separetely.
 *
 * <p>Creation date: 6/02/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class LinkLoadInfoPanel extends JPanel {
    private static final DecimalFormat formatter = new DecimalFormat();

    static {
        formatter.setMaximumFractionDigits(3);
    }

    private Domain domain;
    private Link link = null;

    private final JComboBox llcCombo;
    private final JTabbedPane pane;

    private final TotalLoadPanel totalPanel;
    private final IPLoadPanel ipPanel;
    private final MPLSLoadPanel mplsPanel;
    private final DroppedLoadPanel droppedPanel;

    private final GridBagConstraints baseConstraint;


    public LinkLoadInfoPanel(Domain domain) {
        super();
        this.domain = domain;

        setLayout(new GridBagLayout());
        baseConstraint = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5);

        totalPanel = new TotalLoadPanel();
        ipPanel = new IPLoadPanel();
        mplsPanel = new MPLSLoadPanel();
        droppedPanel = new DroppedLoadPanel();
        pane = new JTabbedPane();
        llcCombo = new JComboBox();
        llcCombo.addActionListener(new ComboActionListener());
        fillLLC();

        baseConstraint.weighty = 0.0;
        add(llcCombo, baseConstraint);
        baseConstraint.weighty = 1.0;
        baseConstraint.gridy++;
        add(pane, baseConstraint);
    }

    private void fillLLC() {
        llcCombo.removeAllItems();
        for (LinkLoadComputer llc : LinkLoadComputerManager.getInstance().getLinkLoadComputers(domain)) {
            llcCombo.addItem(llc);
        }
    }

    /**
     * Change the link to be displayed
     * @param link
     */
    public void setLink(Link link) {
        this.link = link;
        totalPanel.dataChanged();
        ipPanel.dataChanged();
        mplsPanel.dataChanged();
        droppedPanel.dataChanged();
    }

    /**
     * Change the domain
     * @param domain
     */
    public void setDomain(Domain domain) {
        this.domain = domain;
        this.link = null;
        fillLLC();
    }

    private void setData(LoadData loadData) {
        removePanels();
        if (loadData != null) {
            totalPanel.setData(loadData);
            pane.add("Total load", totalPanel);
            if (loadData instanceof IPLoadData) {
                ipPanel.setData((IPLoadData)loadData);
                pane.add("IP Load", ipPanel);
            }
            if (loadData instanceof MPLSLoadData) {
                mplsPanel.setData((MPLSLoadData)loadData);
                pane.add("MPLS Load", mplsPanel);
            }
            droppedPanel.setData(loadData);
            pane.add("Dropped Traffic", droppedPanel);
        }

        //validate();
    }

    private void removePanels() {
        pane.remove(ipPanel);
        pane.remove(mplsPanel);
        pane.remove(droppedPanel);
    }

    private class ComboActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            LinkLoadComputer llc = (LinkLoadComputer)llcCombo.getSelectedItem();
            if (llc == null) setData(null);
            else setData(llc.getData());
        }
    }

    private class DroppedLoadPanel extends AbstractLoadPanel {
        private final DroppedTrafficTableModel model;

        public DroppedLoadPanel() {
            super();
            model = new DroppedTrafficTableModel();
            setupUI();
        }

        public void setData(LoadData data) {
            model.setData(data);
            dataChanged();
        }

        protected void dataChanged() {
            model.fireTableDataChanged();
        }

        private void setupUI() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5);

            final JTable ipTable = new JTable(model);
            add(new JScrollPane(ipTable) {
                public Dimension getPreferredSize() {
                    return new Dimension(super.getPreferredSize().width, ipTable.getRowHeight() * (model.getRowCount()+2));
                }}, c);
        }

    }

    private class IPLoadPanel extends AbstractLoadPanel {
        private final IPLoadTableModel model;

        public IPLoadPanel() {
            super();
            model = new IPLoadTableModel();
            setupUI();
        }

        public void setData(IPLoadData ipData) {
            model.setData(ipData);
            dataChanged();
        }

        protected void dataChanged() {
            model.fireTableDataChanged();
        }

        private void setupUI() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5);

            final JTable ipTable = new JTable(model);
            add(new JScrollPane(ipTable) {
                public Dimension getPreferredSize() {
                    return new Dimension(super.getPreferredSize().width, ipTable.getRowHeight() * (model.getRowCount()+2));
                }}, c);
        }

    }

    private class TotalLoadPanel extends AbstractLoadPanel {
        private final TotalLoadTableModel model;

        public TotalLoadPanel() {
            super();
            model = new TotalLoadTableModel();
            setupUI();
        }

        public void setData(LoadData data) {
            model.setData(data);
            dataChanged();
        }

        protected void dataChanged() {
            model.fireTableDataChanged();
        }

        private void setupUI() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5);

            final JTable ipTable = new JTable(model);
            add(new JScrollPane(ipTable) {
                public Dimension getPreferredSize() {
                    return new Dimension(super.getPreferredSize().width, ipTable.getRowHeight() * (model.getRowCount()+2));
                }}, c);
        }

    }

    private class MPLSLoadPanel extends AbstractLoadPanel {
        private final MPLSLoadTableModel model;

        public MPLSLoadPanel() {
            super();
            model = new MPLSLoadTableModel();
            setupUI();
        }

        public void setData(MPLSLoadData mplsData) {
            model.setData(mplsData);
        }

        public void dataChanged() {
            model.fireTableDataChanged();
        }

        private void setupUI() {
            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5);

            final JTable ipTable = new JTable(model);
            add(new JScrollPane(ipTable) {
                public Dimension getPreferredSize() {
                    return new Dimension(super.getPreferredSize().width, ipTable.getRowHeight() * (model.getRowCount()+2));
                }}, c);
        }

    }

    private abstract class AbstractLoadPanel extends JPanel {
        protected abstract void dataChanged();
    }

    private class DroppedTrafficTableModel extends  AbstractLoadTableModel {

        public DroppedTrafficTableModel() {
            super();
            columnNames[1] = "Traffic";
        }

        public int getColumnCount() {
            return 2;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Node node;
            try {
                node = link.getSrcNode();
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
                return null;
            }
            if (rowIndex < getRowCount()-1 && data instanceof CosLoadData) {
                CosLoadData cosData = (CosLoadData)data;
                String cos = domain.getClassesOfService().get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return cos;
                    case 1:
                        return formatter.format(cosData.getDroppedTraffic(cos, node));
                    default:
                        return null;
                }
            } else {
                switch (columnIndex) {
                    case 0:
                        return "Total";
                    case 1:
                        return formatter.format(data.getDroppedTraffic(node));
                    default:
                        return null;
                }
            }
        }
    }

    private class TotalLoadTableModel extends AbstractLoadTableModel {
        public void setData(LoadData data) {
            super.setData(data);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < getRowCount()-1 && data instanceof CosLoadData) {
                CosLoadData cosData = (CosLoadData)data;
                String cos = domain.getClassesOfService().get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return cos;
                    case 1:
                        return formatter.format(cosData.getLoad(cos, link));
                    case 2:
                        return formatter.format(cosData.getLoad(cos, link)/link.getBandwidth()*100);
                    default:
                        return null;
                }
            } else {
                switch (columnIndex) {
                    case 0:
                        return "Total";
                    case 1:
                        return formatter.format(data.getLoad(link));
                    case 2:
                        return formatter.format(data.getLoad(link)/link.getBandwidth()*100);
                    default:
                        return null;
                }
            }
        }
    }

    private class IPLoadTableModel extends AbstractLoadTableModel {
        public void setData(LoadData data) {
            if (!(data instanceof IPLoadData))
                throw new IllegalArgumentException("data must be of type IP");
            super.setData(data);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < getRowCount()-1 && data instanceof CosIPLoadData) {
                CosIPLoadData cosData = (CosIPLoadData)data;
                String cos = domain.getClassesOfService().get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return cos;
                    case 1:
                        return formatter.format(cosData.getIPLoad(cos, link));
                    case 2:
                        return formatter.format(cosData.getIPLoad(cos, link)/link.getBandwidth()*100);
                    default:
                        return null;
                }
            } else {
                switch (columnIndex) {
                    case 0:
                        return "Total";
                    case 1:
                        return formatter.format(((IPLoadData)data).getIPLoad(link));
                    case 2:
                        return formatter.format(((IPLoadData)data).getIPLoad(link)/link.getBandwidth()*100);
                    default:
                        return null;
                }
            }
        }
    }

    private class MPLSLoadTableModel extends AbstractLoadTableModel {
        public MPLSLoadTableModel() {
            super();
            columnNames[2] = "% Reserved bandwidth";
        }

        public void setData(LoadData data) {
            if (!(data instanceof MPLSLoadData))
                throw new IllegalArgumentException("data must be of type MPLS");
            super.setData(data);
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < getRowCount()-1 && data instanceof CosMPLSLoadData) {
                CosMPLSLoadData cosData = (CosMPLSLoadData)data;
                String cos = domain.getClassesOfService().get(rowIndex);
                switch (columnIndex) {
                    case 0:
                        return cos;
                    case 1:
                        return formatter.format(cosData.getMPLSLoad(cos, link));
                    case 2:
                        return formatter.format(cosData.getMPLSLoad(cos, link)/link.getTotalReservedBandwidth()*100);
                    default:
                        return null;
                }
            } else {
                switch (columnIndex) {
                    case 0:
                        return "Total";
                    case 1:
                        return formatter.format(((MPLSLoadData)data).getMPLSLoad(link));
                    case 2:
                        return formatter.format(((MPLSLoadData)data).getMPLSLoad(link)/link.getTotalReservedBandwidth()*100);
                    default:
                        return null;
                }
            }
        }
    }

    private abstract class AbstractLoadTableModel extends AbstractTableModel {
        protected String[] columnNames = {"Class of service", "Load", "% Capacity"};
        protected LoadData data = null;

        public void setData(LoadData data) {
            this.data = data;
            fireTableDataChanged();
        }

        public int getRowCount() {
            if (data == null) return 0;

            if (data instanceof CosLoadData)
                return domain.getClassesOfService().size()+1;
            return 1;
        }

        public int getColumnCount() {
            return 3;
        }

        public String getColumnName(int column) {
            if (column < columnNames.length)
                return columnNames[column];
            return super.getColumnName(column);
        }
    }
}
