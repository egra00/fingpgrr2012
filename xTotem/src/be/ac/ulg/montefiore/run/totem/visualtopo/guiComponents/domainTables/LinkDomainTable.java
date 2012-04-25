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

import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeInterfaceNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.GraphManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.PopupMenuFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

/*
* Changes:
* --------
* - 03-Feb-2006 : implements getColumnClass in the model (GMO)
* - 20-Mar-2006 : Suppress load column (GMO)
* - 09-Jan-2007 : add link highlightment (GMO)
* - 18-Jan-2007 : add more columns to the model (GMO)
* - 06-Mar-2007 : add type column (GMO)
* - 04-May-2007 : LinkTableModel now extends DomainElementTableModel (GMO)
* - 04-May-2007 : Allow selection of multiple rows, adapt popup menu (GMO)
* - 05-Nov-2007 : remove "Remove Link" menuitem (GMO
* - 15-Jan-2008: now uses PopupMenuFactory (GMO)
*/

/**
 * Table where each row represent a link of a domain.
 * It uses a LinkTableModel as model.
 *
 * <p>Creation date: 11 janv. 2006
 *
 * @see LinkTableModel
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class LinkDomainTable extends DomainTable {

    public LinkDomainTable(Domain domain) {
        super(new LinkTableModel(domain));
        configure();
    }

    public LinkDomainTable() {
        super(new LinkTableModel(InterDomainManager.getInstance().getDefaultDomain()));
        configure();
    }

    private void configure() {
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        getSelectionModel().addListSelectionListener(new SelectionListener());
    }

    public Link getLinkAt(int row) {
        return getModel().getLinkAt(row);
    }

    public LinkTableModel getModel() {
        return (LinkTableModel)super.getModel();
    }

    private class SelectionListener implements ListSelectionListener {

        /**
         * Called whenever the value of the selection changes.
         *
         * @param e the event that characterizes the change.
         */
        public void valueChanged(ListSelectionEvent e) {
            Domain domain = getDomain();
            if (e.getValueIsAdjusting())
                return;
            else {
                int row = getSelectedRow();
                if (row != -1 && getSelectedRowCount() == 1 && domain == GUIManager.getInstance().getCurrentDomain()) {
                    GraphManager.getInstance().highlight(getLinkAt(row));
                } else {
                    GraphManager.getInstance().unHighlight();
                }
            }
        }
    }

    protected JPopupMenu getMenu(MouseEvent evt) {
        int row = rowAtPoint(evt.getPoint());
        Link link = getLinkAt(row);

        JPopupMenu menu = PopupMenuFactory.createLinkPopupMenu(link);

        for (JMenuItem item : getBaseMenuItems()) {
            menu.add(item);
        }

        return menu;
    }
}

/**
 * Model representing the data of the links of a domain.
 * Adapt to the links changes by implementing links events
 * Used by LinkDomainTable.
 *
 * @see LinkDomainTable
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */
class LinkTableModel extends DomainElementTableModel {

    public LinkTableModel(Domain domain) {
        String[] colNames = {"Link Id",                  //0
                             "Source node",
                             "Source interface",
                             "Destination node",
                             "Destination interface",
                             "Bandwidth",                //5
                             "Reserved bw",
                             "Reservable bw",
                             "Metric",
                             "TE-Metric",
                             "Delay",                    //10
                             "Type",
                             "Status",
                             "Link int Id"};             //13
        columnNames = colNames;
        setDomain(domain);
    }

    public boolean isColumnDefaultVisible(int column) {
        if (column == 2 || column == 4 || column == 9 || column == 11 || column == 12 || column == 13) return false;
        return true;
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

    public Link getLinkAt(int row) {
        if (row < 0 || row >= getRowCount())
            return null;
        return (Link)data.get(row);
    }

    public Class<?> getColumnClass(int column) {
        if (column < 5 || column == 12 || column == 11) return String.class;
        else if (column == 13) return Integer.class;
        else return Float.class;
    }

    /**
     * Compute the data in a cell, given row and column number
     *
     * @param row the number of the row
     * @param col the number of the column
     * @return returns the Object to store in the cell
     */
    public Object getValueAt(int row, int col) {
        Link link = getLinkAt(row);

         switch (col) {
                //Link Id
                case 0:
                    return link.getId();
                    //Link source
                case 1:
                    try {
                        return link.getSrcNode().getId();
                    } catch (Exception e) {
                        return null;
                    }
                case 2:
                 try {
                     return link.getSrcInterface().getId();
                 } catch (NodeNotFoundException e) {
                     return null;
                 } catch (NodeInterfaceNotFoundException e) {
                     return null;
                 }
                 //Link destination
                case 3:
                    try {
                        return link.getDstNode().getId();
                    } catch (Exception e) {
                        return null;
                    }
                case 4:
                    try {
                      return link.getDstInterface().getId();
                    } catch (NodeNotFoundException e) {
                        return null;
                    } catch (NodeInterfaceNotFoundException e) {
                        return null;
                    }
                    //Link bandwidth
                case 5:
                    try {
                        return link.getBandwidth();
                    } catch (Exception e) {
                        return null;
                    }
                    //Link reserved bw
                case 6:
                    try {
                        return link.getReservedBandwidth();
                    } catch (Exception e) {
                        return null;
                    }
                    //Link reservable bw
                case 7:
                    try {
                        return link.getReservableBandwidth();
                    } catch (Exception e) {
                        return null;
                    }
                    //Link metric
                case 8:
                    try {
                        return link.getMetric();
                    } catch (Exception e) {
                        return null;
                    }
                case 9:
                    try {
                        return link.getTEMetric();
                    } catch (Exception e) {
                        return null;
                    }
                    //Link delay
                case 10:
                    try {
                        return link.getDelay();
                    } catch (Exception e) {
                        return null;
                    }
                 case 11:
                    return link.getLinkType().toString();
                 case 12:
                    switch (link.getLinkStatus()) {
                        case Link.STATUS_DOWN:
                            return "DOWN";
                        case Link.STATUS_UP:
                            return "UP";
                        default:
                            return "BAD STATUS";
                    }
                 case 13:
                    try {
                        return domain.getConvertor().getLinkId(link.getId());
                    } catch (LinkNotFoundException e) {
                        e.printStackTrace();
                        return null;
                    }
             default:
                    return null;
            }
    }

    public void addLinkEvent(Link link) {
        int index = data.size();
        dataMap.put(link, index);
        data.add(index, link);
        fireTableRowsInserted(index, index);
    }

    public void linkBandwidthChangeEvent(Link link) {
        int index = (Integer)dataMap.get(link);
        fireTableRowsUpdated(index, index);
    }

    public void linkDelayChangeEvent(Link link) {
        int index = (Integer)dataMap.get(link);
        fireTableRowsUpdated(index, index);
    }

    public void linkMetricChangeEvent(Link link) {
        int index = (Integer)dataMap.get(link);
        fireTableRowsUpdated(index, index);
    }

    public void linkReservedBandwidthChangeEvent(Link link) {
        int index = (Integer)dataMap.get(link);
        fireTableRowsUpdated(index, index);
    }

    public void linkStatusChangeEvent(Link link) {
        int index = (Integer)dataMap.get(link);
        fireTableRowsUpdated(index, index);
    }

    public void linkTeMetricChangeEvent(Link link) {
        int index = (Integer)dataMap.get(link);
        fireTableRowsUpdated(index, index);
    }

    public void removeLinkEvent(Link link) {
        int index = (Integer)dataMap.get(link);
        dataMap.remove(link);
        data.remove(index);
        for (int i = index; i < data.size(); i++) {
            dataMap.put(data.get(i), i);
        }
        fireTableRowsDeleted(index, index);
    }


}

