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

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.diffserv.DiffServConstant;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.DomainElementListCellRenderer;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.domainTables.LinkLoadInfoPanel;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.util.Arrays;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.List;
import java.util.Collections;

/*
* Changes:
* --------
* - 28-Feb-2008: Add Load info (GMO)
*/

/**
 *  Displays link information. The link can be chosen on the panel. Displays diffsrev constraints, reservation and load.
 *
 * <p>Creation date: 14/01/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class LinkInfoPanel extends JPanel implements DomainChangeListener {

    private Domain domain;
    private Link link;

    private JLabel[] bcText;
    private JLabel[] bcValues;

    final private JLabel mrbwValue;
    final private JLabel modelType;

    final private JComboBox linksCombo;

    final private JTable reservationTable;
    final private LinkLoadInfoPanel loadPanel;


    private final static NumberFormat format = new DecimalFormat();
    static {
        format.setMaximumFractionDigits(4);
    }

    public LinkInfoPanel(Domain domain) {
        this.domain = domain;
        modelType = new JLabel();
        mrbwValue = new JLabel();
        linksCombo = new JComboBox();
        linksCombo.setRenderer(new DomainElementListCellRenderer());
        linksCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setLink((Link)linksCombo.getSelectedItem());
            }
        });

        reservationTable = new JTable(new ReservationTableModel());
        loadPanel = new LinkLoadInfoPanel(domain);

        setLayout(new BorderLayout());

        setDomain(domain);
        setLink((Link)linksCombo.getSelectedItem());

        setupUI();
    }

    public void setDomain(Domain domain) {
        if (this.domain == null) {
            this.domain.getObserver().removeListener(this);
        }
        this.domain = domain;
        linksCombo.removeAllItems();
        if (domain == null) {
            bcValues = null;
            bcText = null;
        } else {
            domain.getObserver().addListener(this);
            int cts[] = domain.getAllCTId();
            Arrays.sort(cts);
            bcValues = new JLabel[cts.length];
            bcText = new JLabel[cts.length];
            for (int i = 0; i < cts.length; i++) {
                bcText[i] = new JLabel("BC" + cts[i] + ":");
                bcValues[i] = new JLabel();
            }
            for (Link l : domain.getAllLinks()) {
                linksCombo.addItem(l);
            }
        }
        loadPanel.setDomain(domain);
    }

    public void setLink(Link l) {
        this.link = l;
        if (l == null) {
            //setDomain(null);
            modelType.setText("");
            mrbwValue.setText("");
            if (bcValues != null) {
                for (JLabel label : bcValues) {
                    label.setText("");
                }
            }
        } else {
            if (l.getDomain() != domain) {
                setDomain(domain);
            }
            modelType.setText(l.getDiffServModel().toString());
            mrbwValue.setText(format.format(l.getBandwidth()));

            int cts[] = domain.getAllCTId();
            Arrays.sort(cts);
            float[] bcs = l.getBCs();
            for (int i = 0; i < cts.length; i++) {
                bcValues[i].setText(format.format(bcs[cts[i]]));
            }
        }
        ((ReservationTableModel)reservationTable.getModel()).fill();
        loadPanel.setLink(link);
    }

    public void destroy() {
        if (domain != null) {
            domain.getObserver().removeListener(this);
        }
    }

    private void setupUI() {

        JPanel linkChooserPanel = new JPanel(new BorderLayout());
        linkChooserPanel.setBorder(BorderFactory.createTitledBorder("Selected link"));
        linkChooserPanel.add(linksCombo);

        add(linkChooserPanel, BorderLayout.NORTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        add(mainPanel, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5);

        JPanel constraintsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        constraintsPanel.setBorder(BorderFactory.createTitledBorder("Model & Constraints"));
        constraintsPanel.add(new JLabel("Model:"));
        constraintsPanel.add(modelType);

        if (bcValues != null) {
            for (int i = 0; i < bcValues.length; i++) {
                constraintsPanel.add(bcText[i]);
                constraintsPanel.add(bcValues[i]);
            }
        }

        constraintsPanel.add(new JLabel("Mrbw:"));
        constraintsPanel.add(mrbwValue);

        mainPanel.add(constraintsPanel, c);

        JPanel reservationPanel = new JPanel(new BorderLayout());
        reservationPanel.setBorder(BorderFactory.createTitledBorder("Current reservation"));

        reservationPanel.add(new JScrollPane(reservationTable) {
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, reservationTable.getRowHeight() * 10);
            }
        }, BorderLayout.CENTER);

        c.gridy++;
        mainPanel.add(reservationPanel, c);
        c.gridy++;
        mainPanel.add(loadPanel, c);
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
        if (this.link == link) {
            setLink(link);
        }
    }

    public void linkReservedBandwidthChangeEvent(Link link) {
        if (this.link == link) {
            ((ReservationTableModel)reservationTable.getModel()).fill();
        }
    }

    public void linkDelayChangeEvent(Link link) {
    }

    public void addLspEvent(Lsp lsp) {
    }

    public void removeLspEvent(Lsp lsp) {
    }

    public void lspReservationChangeEvent(Lsp lsp) {
    }

    public void lspWorkingPathChangeEvent(Lsp lsp) {
    }

    /**
     * Notify a change in the status of a lsp
     *
     * @param lsp
     */
    public void lspStatusChangeEvent(Lsp lsp) {
    }

    private class ReservationTableModel extends AbstractTableModel {

        private final String[] columnNames = {"Priority", "Reserved Bandwidth", "Reservable Bandwidth"};

        private float[] reservedBw = new float[DiffServConstant.MAX_NB_PRIORITY];
        private float[] reservableBw = new float[DiffServConstant.MAX_NB_PRIORITY];

        private int[] priorities;

        public void fill() {
            priorities = null;
            if (domain != null) {
                List<Integer> prios = domain.getPriorities();
                Collections.sort(prios);
                priorities = new int[prios.size()];
                if (link != null) {
                    for (int i = 0; i < prios.size(); i++) {
                        int priority = prios.get(i);
                        reservedBw[priority] = link.getReservedBandwidth(priority);
                        reservableBw[priority] = link.getReservableBandwidth(priority);
                        priorities[i] = priority;
                    }
                }
            }
            fireTableDataChanged();
        }

        public int getRowCount() {
            return DiffServConstant.MAX_NB_PRIORITY;
        }

        public int getColumnCount() {
            return 3;
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            if (domain == null || link == null || priorities == null || rowIndex >= priorities.length) {
                return null;
            }
            int prio = priorities[rowIndex];
            switch (columnIndex) {
                case 0:
                    return prio + " (ct: "+ domain.getClassType(prio) + ", pl: " + domain.getPreemptionLevel(prio) +")";
                case 1:
                    return format.format(reservedBw[prio]);
                case 2:
                    return format.format(reservableBw[prio]);
            }
            return null;
        }
    }

}
