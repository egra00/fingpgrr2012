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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LoadDataListener;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LoadData;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

/*
* Changes:
* --------
* - 18-Jan-2007: bugfix in AllResvTableModel (GMO)
* - 05-Feb-2007: add utilisation column, display reserved bw instead of reservable bw (GMO)
* - 06-Apr-2007: React to change in the domain (GMO)
* - 04-May-2007: listeners can be removed by calling destroy() (GMO)
* - 15-Jan-2008: add column for CT reservation (GMO)
* - 28-Feb-2008: adapt to the new LinkLoadComputer interface (GMO)
*/

/**
 * Panel that displays a tabbed pane of tables representing the current reservation and calculated load of a network.
 * Each table has one column for link name, one for link capacity and one for a load value (Reservation or calculated load).
 * <p/>
 * <p>Creation date: 21/12/2006
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class LoadTables extends JPanel {

    private ArrayList<LoadTableModel> managedListeners;

    public LoadTables() {
        super(new BorderLayout());
        managedListeners = new ArrayList<LoadTableModel>();
        setupUI();
    }

    private void setupUI() {
        JTabbedPane panes = new JTabbedPane();
        Domain domain = GUIManager.getInstance().getCurrentDomain();

        ValueTable vt = new ValueTable(new AllResvTableModel());
        vt.setDomain(domain);
        panes.add("Reservation", new JScrollPane(vt));

        for (int ct : domain.getAllCTId()) {
            vt = new ValueTable(new ResvTableModel(ct));
            vt.setDomain(domain);
            panes.add("Reservation CT " + ct, new JScrollPane(vt));
        }

        for (Pair<String, LinkLoadComputer> pair : LinkLoadComputerManager.getInstance().getLinkLoadComputersWithId(domain)) {
            String id = pair.getFirst();
            LinkLoadComputer llc = pair.getSecond();
            LoadTableModel ltm = new LoadTableModel(llc.getData());

            managedListeners.add(ltm);

            vt = new ValueTable(ltm);
            vt.setDomain(domain);
            panes.add(id, new JScrollPane(vt));
        }

        add(panes, BorderLayout.CENTER);
    }

    // destroy the panel: mainly to remove listeners
    public void destroy() {
        for (LoadTableModel ltm : managedListeners) {
            ltm.destroy();
        }
    }

    public class ValueTable extends DomainTable {
        public ValueTable(DomainTableModel model) {
            super(model);
        }
    }

    private class ValueTableModel extends DomainElementTableModel {

        public ValueTableModel() {
            String[] cols = {"Link Id", "Capacity", "Value", "Utilisation (%)"};
            columnNames = cols;
        }

        protected void fillData() {
            this.dataMap = new HashMap<Link, Integer>();
            data = new ArrayList<Link>();
            if (domain != null) {
                int i = 0;
                for (Link link : domain.getAllLinks()) {
                    data.add(i, link);
                    dataMap.put(link, new Integer(i++));
                }
            }
        }

        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return String.class;
                case 1:
                case 2:
                case 3:
                    return Float.class;
                default:
                    return String.class;
            }
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Link link = (Link)data.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return link.getId();
                case 1:
                    return link.getBandwidth();
                case 2:
                case 3:
                    return null;
            }
            throw new IllegalArgumentException("Column does not exist");
        }

        public void addLinkEvent(Link link) {
            super.addLinkEvent(link);
            int index = data.size();
            dataMap.put(link, new Integer(index));
            data.add(index, link);
            fireTableDataChanged();
        }

        public void linkBandwidthChangeEvent(Link link) {
            super.linkBandwidthChangeEvent(link);
            int index = (Integer)dataMap.get(link);
            fireTableRowsUpdated(index, index);
        }

        public void removeLinkEvent(Link link) {
            super.removeLinkEvent(link);
            fillData();
            fireTableDataChanged();
        }
    }

    private class LoadTableModel extends ValueTableModel implements LoadDataListener {
        private LoadData ldv;

        public LoadTableModel(LoadData ldv) {
            this.ldv = ldv;
            //ldv.addListener(this);
            columnNames[2] = "Load Value";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Link link = (Link)data.get(rowIndex);

            switch (columnIndex) {
                case 2:
                    return ldv.getLoad(link);
                 case 3:
                    return ldv.getLoad(link) / link.getBandwidth() * 100;
                default:
                    return super.getValueAt(rowIndex, columnIndex);
            }
        }

        public void destroy() {
            ldv.removeListener(this);
        }

        public void loadChangeEvent() {
            fireTableDataChanged();
        }
    }

    private class ResvTableModel extends ValueTableModel {
        private int ct;

        public ResvTableModel(int ct) {
            String[] cols = {"Link Id", "Capacity", "Reserved Value", "Reservable Value", "Utilisation (%)"};
            columnNames = cols;
            this.ct = ct;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Link link = (Link)data.get(rowIndex);

            switch (columnIndex) {
                case 0:
                    return link.getId();
                case 1:
                    return link.getBCs()[ct];
                case 2:
                    return link.getReservedBandwidthCT(ct);
                case 3:
                    return link.getReservableBandwidthCT(ct);
                case 4:
                    return (1- link.getReservableBandwidthCT(ct) / link.getBCs()[ct]) * 100;
                default:
                    return super.getValueAt(rowIndex, columnIndex);
            }
        }

        public Class<?> getColumnClass(int column) {
            if (column == 4) return Float.class;
            else return super.getColumnClass(column);
        }

        public void linkReservedBandwidthChangeEvent(Link link) {
            super.linkReservedBandwidthChangeEvent(link);
            int index = (Integer)dataMap.get(link);
            fireTableRowsUpdated(index, index);
        }
    }

    private class AllResvTableModel extends ValueTableModel {

        public AllResvTableModel() {
            columnNames[2] = "Reserved Value";
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Link link = (Link)data.get(rowIndex);

            switch (columnIndex) {
                case 2:
                    return link.getTotalReservedBandwidth();
                case 3:
                    return link.getTotalReservedBandwidth() / link.getBandwidth() * 100;
                default:
                    return super.getValueAt(rowIndex, columnIndex);
            }
        }

        public void linkReservedBandwidthChangeEvent(Link link) {
            super.linkReservedBandwidthChangeEvent(link);
            int index = (Integer)dataMap.get(link);
            fireTableRowsUpdated(index, index);
        }
    }

}
