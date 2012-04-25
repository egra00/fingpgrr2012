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
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrixChangeListener;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 03-Feb-2006 : model now retains tmId (GMO)
* - 23-Apr-2007 : add editing capabilities, listens to changes (GMO)
* - 20-Sep-2007 : fix bug in setValueAt(.) when a null value is given (GMO)
*
*/

/**
 * Table representing a traffic matrix.
 * It uses a TMTableModel as model which listens to changes in the traffic matrix.
 * It has editing capability. To enable editing call {@link #setEditable()}.
 *
 * <p>Creation date: 11 janv. 2006
 *
 * @see TMTableModel
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class TrafficMatrixTable extends JTable {

    private final static Logger logger = Logger.getLogger(TrafficMatrixTable.class);


    private static enum Operation {
        SET, DIVIDE, MULTIPLY, ADD, SUBSTRACT;
    }

    public TrafficMatrixTable(TrafficMatrix tm, int tmId) {
        super(new TMTableModel(tm, tmId));
    }

    /**
     * Remove listener
     */
    public void destroy() {
        logger.debug("Destroying TM table.");
        getModel().destroy();
    }

    public TMTableModel getModel() {
        return (TMTableModel)super.getModel();
    }

    public TrafficMatrix getTM() {
        return getModel().getTM();
    }

    public int getTMId() {
        return getModel().getTMId();
    }

    public void setTM(TrafficMatrix tm, int tmId)  {
        getModel().setTM(tm, tmId);
    }

    /**
     * Enables editing capabilities
     */
    public void setEditable() {
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setCellSelectionEnabled(true);
        getModel().setEditable(true);
        addMouseListener(new ContextMenuListener(this));
    }


    class ContextMenuListener extends MouseAdapter {
        private JPopupMenu menu;
        private TrafficMatrixTable table;


        public ContextMenuListener(TrafficMatrixTable table) {
            menu = new JPopupMenu();

            this.table = table;
            JMenuItem item;

            item = new JMenuItem("Set");
            item.addActionListener(new OperationListener(Operation.SET));
            menu.add(item);

            item = new JMenuItem("Multiply");
            item.addActionListener(new OperationListener(Operation.MULTIPLY));
            menu.add(item);

            item = new JMenuItem("Divide");
            item.addActionListener(new OperationListener(Operation.DIVIDE));
            menu.add(item);

            item = new JMenuItem("Add");
            item.addActionListener(new OperationListener(Operation.ADD));
            menu.add(item);

            item = new JMenuItem("Substract");
            item.addActionListener(new OperationListener(Operation.SUBSTRACT));
            menu.add(item);
        }

        public void mousePressed(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                menu.show(table, evt.getX(), evt.getY());
            }
        }

        class OperationListener implements ActionListener {

            private Operation op;
            private TrafficMatrix tm;

            public OperationListener(Operation op) {
                this.op = op;
                tm = table.getTM();
            }

            /**
             * Invoked when an action occurs.
             */
            public void actionPerformed(ActionEvent e) {

                /* show dialog */
                float inputValue = 0.0f;
                boolean ok = false;
                do {
                    String inputString = JOptionPane.showInputDialog(table, "Enter the value:", 0.0);
                    if (inputString == null) return;
                    try {
                        inputValue = Float.valueOf(inputString);
                        ok = true;
                    } catch (NumberFormatException ex) {
                        ok = false;
                    }
                } while (!ok);


                // Get the min and max ranges of selected cells
                int rowIndexStart = table.getSelectedRow();
                int rowIndexEnd = table.getSelectionModel().getMaxSelectionIndex();
                int colIndexStart = table.getSelectedColumn();
                int colIndexEnd = table.getColumnModel().getSelectionModel().getMaxSelectionIndex();

                // Check each cell in the range
                for (int r = rowIndexStart; r <= rowIndexEnd; r++) {
                    for (int c = colIndexStart; c <= colIndexEnd; c++) {
                        if (c != 0 && table.isCellSelected(r, c)) {
                            // cell is selected
                            float newValue = 0.0f;
                            float oldValue = 0.0f;
                            try {
                                int src = getModel().getSrcNodeId(r);
                                int dst = getModel().getDstNodeId(convertColumnIndexToModel(c));
                                oldValue = tm.get(src, dst);

                                switch (op) {
                                    case SET:
                                        newValue = inputValue;
                                        break;
                                    case MULTIPLY:
                                        newValue = oldValue * inputValue;
                                        break;
                                    case DIVIDE:
                                        newValue = oldValue / inputValue;
                                        break;
                                    case ADD:
                                        newValue = oldValue + inputValue;
                                        break;
                                    case SUBSTRACT:
                                        newValue = (oldValue - inputValue) < 0 ? 0 : (oldValue - inputValue);
                                        break;
                                }

                                tm.set(src, dst, newValue);
                            } catch (NodeNotFoundException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * This class contains the data model for a JTable containing a Traffic Matrix.
 * 
 * <p>Creation date: 11 janv. 2006
 *
 * @see TrafficMatrixTable
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
class TMTableModel extends AbstractTableModel implements TrafficMatrixChangeListener {
    private Domain domain = null;
    private TrafficMatrix tm = null;
    private int tmId = -1;

    private int[] id2Ordinal;
    private int[] ordinal2Id;

    private boolean editable = false;

    public TMTableModel(TrafficMatrix tm, int tmId) {
        try {
            domain = InterDomainManager.getInstance().getDomain(tm.getASID());
            this.tm = tm;
            this.tmId = tmId;

            id2Ordinal = new int[domain.getConvertor().getMaxNodeId()+1];
            ordinal2Id = new int[domain.getNbNodes()];

            for (int i = 0; i < domain.getNbNodes(); i++) {
                Node n = domain.getAllNodes().get(i);
                int id = domain.getConvertor().getNodeId(n.getId());
                id2Ordinal[id] = i;
                ordinal2Id[i] = id;
            }

            tm.getObserver().addListener(this);
        } catch (InvalidDomainException e) {
            //e.printStackTrace();
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int getDstNodeId(int columnIndex) {
        return ordinal2Id[columnIndex-1];
    }

    public int getSrcNodeId(int rowIndex) {
        return ordinal2Id[rowIndex];
    }

    private int getColumnIndex(int nodeId) {
        return id2Ordinal[nodeId]+1;
    }

    private int getRowIndex(int nodeId) {
        return id2Ordinal[nodeId];
    }

    /**
     * remove listeners
     */
    public void destroy() {
        if (tm != null)
            tm.getObserver().removeListener(this);
    }

    public void setTM(TrafficMatrix tm, int tmId) {
        destroy();
        this.tm = tm;
        this.tmId = tmId;
        fireTableStructureChanged();
    }

    public TrafficMatrix getTM() {
        return tm;
    }

    public int getTMId() {
        return tmId;
    }

    /**
     * Returns the name of the column which number is given as parameter
     *
     * @param column contain a column number
     * @return returns the name of the column
     */
    public String getColumnName(int column) {
        if (column == 0 || domain == null)
            return null;
        try {
            return domain.getConvertor().getNodeId(getDstNodeId(column));
        } catch (NodeNotFoundException e) {
            return null;
        }
    }

    /**
     * Returns <code>Object.class</code> regardless of <code>columnIndex</code>.
     *
     * @param columnIndex the column being queried
     * @return the Object.class
     */
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) return String.class;
        else return Float.class;
    }

    /**
     * Returns the number of rows of data
     *
     * @return returns the number of rows
     */
    public int getRowCount() {
        return (domain == null) ? 0 : domain.getNbNodes();
    }


    /**
     * Returns the number of columns in the model
     *
     * @return Returns the number of columns in the model
     */
    public int getColumnCount() {
        return (domain == null) ? 1 : domain.getNbNodes() + 1;
    }


    /**
     * Avoid column edition
     *
     * @return returns false
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 0 ? false : editable;
    }


    /**
     * Compute the data in a cell, given row and column number ie. the Traffic Matrix value.
     *
     * @param row the number of the row
     * @param col the number of the column
     * @return returns the Object to store in the cell
     */
    public Object getValueAt(int row, int col) {
        if (col == 0)
            try {
                return domain.getConvertor().getNodeId(getSrcNodeId(row));
            } catch (NodeNotFoundException e) {
                return null;
            }
        try {
            return tm.get(getSrcNodeId(row), getDstNodeId(col));
        } catch (Exception e) {
            return null;
        }
    }


    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        try {
            float value = aValue == null ? 0.0f : (Float)aValue;
            tm.set(getSrcNodeId(rowIndex), getDstNodeId(columnIndex), value);
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isEditable() {
        return editable;
    }

    /**
     * Notify a change in the value of the source-destination pair from node <code>src</code> to node <code>dst</code>.
     */
    public void elementChangeEvent(String src, String dst) {
        try {
            int row = getRowIndex(domain.getConvertor().getNodeId(src));
            int col = getColumnIndex(domain.getConvertor().getNodeId(dst));
            fireTableCellUpdated(row, col);
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }

    }
}