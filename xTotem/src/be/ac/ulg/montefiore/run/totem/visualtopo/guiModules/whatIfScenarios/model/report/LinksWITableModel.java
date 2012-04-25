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

import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.domainTables.DomainTableModel;

import java.util.Arrays;
import java.util.Comparator;

/*
* Changes:
* --------
* - 04-May-2007: it now extends DomainTableModel (GMO)
*/

/**
 *
 * <p/>
 * <p>Creation date: 23/04/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class LinksWITableModel extends DomainTableModel {
    private class Elem {
        int idI;
        String idString;
        double initialData;
        double finalData;
        double difference;
    }

    private Elem[] data;

    public LinksWITableModel(Domain domain, double[] initialData, double[] finalData) {
        data = new Elem[domain.getNbLinks()];
        int i = 0;
        for (Link l : domain.getAllLinks()) {
            try {
                Elem elem = new Elem();
                elem.idString = l.getId();
                elem.idI = domain.getConvertor().getLinkId(l.getId());
                elem.finalData = finalData[elem.idI];
                elem.initialData = initialData[elem.idI];
                elem.difference = elem.finalData - elem.initialData;
                data[i] = elem;
                i++;
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }

        this.domain = domain;

        String[] colNames = {"Link Id",
                             "Initial Value",
                             "Final Value",
                             "Difference"};

        columnNames = colNames;
    }

    /**
     * Returns the number of rows in the model. A
     * <code>JTable</code> uses this method to determine how many rows it
     * should display.  This method should be quick, as it
     * is called frequently during rendering.
     *
     * @return the number of rows in the model
     * @see #getColumnCount
     */
    public int getRowCount() {
        return data.length;
    }

    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
            case 2:
            case 3:
                return Float.class;
            default:
                return Object.class;
        }
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return data[rowIndex].idString;
            case 1:
                return data[rowIndex].initialData;
            case 2:
                return data[rowIndex].finalData;
            case 3:
                return data[rowIndex].difference;

        }
        return null;
    }

    public void sortByColumn(int column) {
        Arrays.sort(data, new ElemSorter(column));
        fireTableDataChanged();
    }

    protected void fillData() {
        // nothing to do
        // (done in constructor)
    }

    private class ElemSorter implements Comparator<Elem> {
        private int col;

        public ElemSorter(int col) {
            this.col = col;
        }

        public int compare(Elem elem1, Elem elem2) {
            if (elem1 == null && elem2 == null) return 0;
            if (elem1 == null && elem2 != null) return -1;
            if (elem1 != null && elem2 == null) return 1;

            switch (col) {
                case 0:
                    return elem1.idString.compareTo(elem2.idString);
                case 1:
                    return Double.valueOf(elem1.initialData).compareTo(elem2.initialData);
                case 2:
                    return Double.valueOf(elem1.finalData).compareTo(elem2.finalData);
                case 3:
                    return Double.valueOf(elem1.difference).compareTo(elem2.difference);
            }
            return 0;
        }
    }
}
