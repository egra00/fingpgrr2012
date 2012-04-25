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

import javax.swing.table.AbstractTableModel;


/*
* Changes:
* --------
*
*/

/**
 * Abstract model for tables that are specific to a domain.
 * The model listens for change in the domain. Override the DomainChangeListener methods to react to the changes.
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 *
 * @see DomainTable
 */
public abstract class DomainTableModel extends AbstractTableModel implements DomainChangeListener {

    protected String[] columnNames = {};

    protected Domain domain = null;

    public void setDomain(Domain domain) {
        if (this.domain != null) {
            this.domain.getObserver().removeListener(this);
        }
        this.domain = domain;
        if (domain != null) {
            domain.getObserver().addListener(this);
        }
        fillData();
        fireTableDataChanged();
    }

    public abstract void sortByColumn(int column);

    /**
     * Returns true if the column should be visible by default
     * @param column
     * @return
     */
    public boolean isColumnDefaultVisible(int column) {
        return true;
    }

    protected abstract void fillData();

    public Domain getDomain() {
        return domain;
    }

    /**
     * Returns the name of the column which number is given as parameter
     *
     * @param column contain a column number
     * @return returns the name of the column
     */
    public String getColumnName(int column) {
        return columnNames[column];
    }

    /**
     * Returns the number of columns in the model
     *
     * @return Returns the number of columns in the model
     */
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * Avoid column edition
     *
     * @return returns false
     */
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
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
    }

    public void linkReservedBandwidthChangeEvent(Link link) {
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

    public void lspStatusChangeEvent(Lsp lsp) {
    }
}
