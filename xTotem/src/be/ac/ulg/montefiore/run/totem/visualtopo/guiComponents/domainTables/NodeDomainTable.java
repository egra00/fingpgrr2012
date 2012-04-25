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

import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.PopupMenuFactory;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

/*
* Changes:
* --------
* - 03-Feb-2006: implements getColumnClass in the model (GMO)
* - 30-Jun-2006: add getRowForNode method (not used) (GMO)
* - 30-Jun-2006: add nodeLocationChangeEvent implementation (GMO)
* - 10-Jan-2007: add "router ID" column (GMO)
* - 18-Jan-2007: add more columns to the model (GMO)
* - 04-May-2007: adapt popup menu (GMO)
* - 05-Nov-2007: remove "Remove Node" menuitem (GMO)
* - 15-Jan-2008: now uses PopupMenuFactory (GMO)
*/

/**
 * Table where each row represent a node of a domain.
 * It uses a NodeTableModel as model.
 *
 * <p>Creation date: 11 janv. 2006
 *
 * @see NodeTableModel
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class NodeDomainTable extends DomainTable {

    public NodeDomainTable(Domain domain) {
        super(new NodeTableModel(domain));
        configure();
    }

    public NodeDomainTable() {
        super(new NodeTableModel(InterDomainManager.getInstance().getDefaultDomain()));
        configure();
    }

    private void configure() {
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    public Node getNodeAt(int row) {
        return getModel().getNodeAt(row);
    }

    public NodeTableModel getModel() {
        return (NodeTableModel)super.getModel();
    }

    protected JPopupMenu getMenu(MouseEvent evt) {
        int row = rowAtPoint(evt.getPoint());
        Node node = getNodeAt(row);

        JPopupMenu menu = PopupMenuFactory.createNodePopupMenu(node);

        for (JMenuItem item : getBaseMenuItems()) {
            menu.add(item);
        }

        return menu;
    }

}

/**
 * Model representing the data of the nodes of a domain.
 * Adapt to the nodes changes by implementing nodes events
 * Used by NodeDomainTable.
 *
 * @see NodeDomainTable
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */
class NodeTableModel extends DomainElementTableModel {

    public NodeTableModel(Domain domain) {
        String[] colNames = {"Node Id",
                             "Router Id",
                             "Node description",
                             "Node longitude",
                             "Node latitude",
                             "Node type",
                             "Node status",
                             "Node int Id"};
        columnNames = colNames;
        setDomain(domain);
    }

    public boolean isColumnDefaultVisible(int column) {
        if (column > 4) return false;
        return true;
    }

    protected void fillData() {
        this.dataMap = new HashMap<Node, Integer>();
        data = new ArrayList<Node>();
        if (domain != null) {
            int i = 0;
            for (Node node : domain.getAllNodes()) {
                data.add(i, node);
                dataMap.put(node, new Integer(i++));
            }
        }
    }

    public Class<?> getColumnClass(int column) {
        if (column == 3 || column == 4) return Float.class;
        else if (column == 7) return Integer.class;
        else return String.class;
    }

    public Node getNodeAt(int row) {
        if (row < 0 || row >= getRowCount()) {
            return null;
        }
        else return (Node)data.get(row);
    }

    /**
     * Compute the data in a cell, given row and column number
     *
     * @param row the number of the row
     * @param col the number of the column
     * @return returns the Object to store in the cell
     */
    public Object getValueAt(int row, int col) {
        Node node = (Node)data.get(row);
           switch (col) {
                //Node Id
                case 0:
                    return node.getId();
                // router ID
                case 1:
                   return node.getRid();
                    //Node Description
                case 2:
                    try {
                        return node.getDescription();
                    } catch (Exception e) {
                        return null;
                    }
                    //Node Longitude
                case 3:
                    try {
                        return node.getLongitude();
                    } catch (Exception e) {
                        return null;
                    }
                    //Node Latitude
                case 4:
                    try {
                        return node.getLatitude();
                    } catch (Exception e) {
                        return null;
                    }
                case 5:
                   return node.getNodeType().toString();
               case 6:
                   switch (node.getNodeStatus()) {
                       case Node.STATUS_UP:
                           return "UP";
                       case Node.STATUS_DOWN:
                           return "DOWN";
                       default:
                           return "BAD STATUS";
                   }
               case 7:
                   try {
                       return domain.getConvertor().getNodeId(node.getId());
                   } catch (NodeNotFoundException e) {
                       return null;
                   }
               default:
                    return null;
            }
    }

    public int getRowForNode(Node node) {
        Integer i;
        if ((i = (Integer)dataMap.get(node)) == null) {
            return -1;
        }
        return i.intValue();
    }

    public void addNodeEvent(Node node) {
        int index = data.size();
        dataMap.put(node, index);
        data.add(index, node);
        fireTableRowsInserted(index, index);
    }

    public void nodeLocationChangeEvent(Node node) {
        int index = (Integer)dataMap.get(node);
        fireTableRowsUpdated(index, index);
    }

    public void nodeStatusChangeEvent(Node node) {
        int index = (Integer)dataMap.get(node);
        fireTableRowsUpdated(index, index);
    }

    public void removeNodeEvent(Node node) {
        int index = (Integer)dataMap.get(node);
        dataMap.remove(node);
        data.remove(index);
        for (int i = index; i < data.size(); i++) {
            dataMap.put(data.get(i), i);
        }
        fireTableRowsDeleted(index, index);
    }
}

