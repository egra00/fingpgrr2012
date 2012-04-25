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

import be.ac.ulg.montefiore.run.totem.domain.model.*;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/*
* Changes:
* --------
*
*/

/**
 * Abstract model for tables that are specific to a domain.
 * It adds a way to represent the data and a way to sort the columns.
 * The model listens for change in the domain. Override the DomainChangeListener methods to react to the changes.
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 *
 * @see DomainTable
 */
public abstract class DomainElementTableModel extends DomainTableModel implements DomainChangeListener {

    private static Logger logger = Logger.getLogger(DomainElementTableModel.class);

    protected HashMap dataMap = null;
    protected ArrayList data = null;

    public void sortByColumn(int column) {
        Collections.sort(data, new ColumnSorter(column));
        for (int i = 0; i < data.size(); i++) {
            dataMap.put(data.get(i), new Integer(i));
        }
        fireTableDataChanged();
    }

    protected abstract void fillData();

    /**
     * Returns the number of rows of data
     *
     * @return returns the number of rows
     */
    public int getRowCount() {
        return data.size();
    }

    protected class ColumnSorter implements Comparator {
        int column = -1;

        public ColumnSorter(int column) {
            this.column = column;
        }

        public int compare(Object o, Object o1) {
            int row1 = (Integer)dataMap.get(o);
            int row2 = (Integer)dataMap.get(o1);

            try {
                Comparable value1 = (Comparable)getValueAt(row1, column);
                Comparable value2 = (Comparable)getValueAt(row2, column);
                if (value1 == null) return (value2 == null) ? 0 : -1;
                if (value2 == null) return 1;
                return value1.compareTo(value2);
            } catch (ClassCastException c) {
                logger.debug("Elements not comparable");
            }
            return 0;
        }
    }

}
