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

import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

import java.util.List;
import java.util.Vector;
import java.util.HashMap;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/*
* Changes:
* --------
*
*/

/*
* Table designed to enter algorithm parameters.
* Contains 3 colums: Parameter (nale of the parameter), Value and Description.
* Only Value column is editable.
*
* <p>Creation date: 16 nov. 2005
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ParamTable extends JTable {

	/* Cell editors for column 1 */
	private Vector<TableCellEditor> editors = null;
    /* Cell renderers for column 1 */
	private Vector<TableCellRenderer> renderers = null;
	
    public ParamTable(int nbRows) {
        super(new DefaultTableModel(new String[] {"Parameter", "Value", "Description"}, nbRows) {
           public boolean isCellEditable(int row, int column) {
                return (column == 1);
           }
        });

        editors = new Vector<TableCellEditor>(nbRows);
        editors.setSize(nbRows);
        renderers = new Vector<TableCellRenderer>(nbRows);
        renderers.setSize(nbRows);
        
        getColumn("Description").setCellRenderer(new DefaultTableCellRenderer() {
               public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                                boolean hasFocus, int row, int column)
                {
                   Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                   cell.setForeground(Color.gray);
                   return cell;
               }});
    }

    /**
     *  Return the content of the cell on the column "Description", otherwise return default tooltip.
     * @param e
     * @return
     */
    public String getToolTipText(MouseEvent e) {
 	   String tip = null;
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        int colIndex = columnAtPoint(p);
        int realColumnIndex = convertColumnIndexToModel(colIndex);
        
        if (realColumnIndex == 2) {
        	tip = (getValueAt(rowIndex, realColumnIndex) != null) ? getValueAt(rowIndex, realColumnIndex).toString() : null;
        }
        else tip = super.getToolTipText(e);
        if (tip != null && tip.equals("")) tip = null;
        return tip;
    }

    /**
     * Return specific renderer if column "Value", else return default renderer
     * @param row
     * @param column
     * @return
     */
    public TableCellRenderer getCellRenderer(int row, int column) {
        int modelColumn = convertColumnIndexToModel( column );
        
        if (modelColumn == 1 && renderers.get(row) != null) {
        	return renderers.get(row);
        }
        return super.getCellRenderer(row, column);
    }
    
    /**
     * Set specific cell renderer if column == 1
     * @param row
     * @param column
     * @param tcr
     */
    public void setCellRenderer(int row, int column, TableCellRenderer tcr) {
        int modelColumn = convertColumnIndexToModel( column );
    	if (modelColumn == 1) renderers.set(row, tcr);    	
    }


    // With that method, cells are not editable anymore in column 1
    /**
     * return the specific cell editor
     * @param row
     * @param column
     * @return
     */
    public TableCellEditor getCellEditor(int row, int column)
    {
        int modelColumn = convertColumnIndexToModel( column );

        if (modelColumn == 1 && editors.get(row) != null) {
            //System.out.println(editors.get(row));
        	return editors.get(row);
        }
        return super.getCellEditor(row, column);
    }
    
   /**
    * Set a specific cell editor if column == 1
    * @param row
    * @param column
    * @param tce
    */
    public void setCellEditor(int row, int column, TableCellEditor tce) {
        int modelColumn = convertColumnIndexToModel( column );
    	if (modelColumn == 1) editors.set(row, tce);
    }

    /**
     * remove specific cell editor
     * @param row
     * @param column
     */
    public void removeCellEditor(int row, int column) {
        int modelColumn = convertColumnIndexToModel( column );
    	if (modelColumn == 1 && row < editors.size())
    		editors.set(row, null);
    }

    /**
     * Remove all specifics cell editors
     */
    public void removeAllCellEditors() {
        for (int i = 0; i < editors.size(); i++)
        	editors.set(i, null);
    }

    /**
     * Fill the table with List of parameter given.
     * TODO: Set a specific cell editor and renderer for the data type in the column value
     * set a specific cell editor with a comboBox when values are enum
     * @param params
    */
    public void fill(List<ParameterDescriptor> params) {
        if (getCellEditor() != null) getCellEditor().cancelCellEditing();

        int i = 0;
        for (ParameterDescriptor al : params) {
            if (i >= getRowCount()) {
                ((DefaultTableModel)this.getModel()).insertRow(i, (Vector)null);
                editors.add(null);
                renderers.add(null);
            }
            if (al.getPossibleValues() != null) {
                setCellEditor(i, 1, new DefaultCellEditor(new JComboBox(al.getPossibleValues())));
            } else {
                removeCellEditor(i, 1);
                //setCellEditor(i, 1, this.getDefaultEditor(al.getType()));
                //setCellRenderer(i, 1, this.getDefaultRenderer(al.getType()));
            }
            
           	setValueAt(al.getName(), i, 0);
           	setValueAt(al.getDefaultValue(), i, 1);
           	setValueAt(al.getDescription(), i, 2);

            i++;
        }
        
        while (i < getRowCount()) {
        	((DefaultTableModel)this.getModel()).removeRow(i);
        }
    }

    /**
     * empty table (delete all rows)
     */
    public void empty() {
        while (getRowCount() > 0) ((DefaultTableModel)this.getModel()).removeRow(0); 
    }


    /**
     * Convert the table values and parameters name into a HashMap of (parameters names, values) that can be passed to the algorithm
     * @return the newly created HashMap
     */
    public HashMap<String, String> toHashMap() {
        HashMap<String, String> routingParams = new HashMap<String, String>();
        for (int i = 0; i < getRowCount(); i++) {
            if (getValueAt(i, 0) != null && getValueAt(i, 0).toString() != "" && getValueAt(i, 1) != null && getValueAt(i, 1).toString() != "") {
                routingParams.put((String) getValueAt(i, 0), getValueAt(i, 1).toString());

                ///*debug*/ System.out.println((String) getValueAt(i, 0) + " = " + getValueAt(i, 1).toString());
            }
        }
        return routingParams;
    }
}
