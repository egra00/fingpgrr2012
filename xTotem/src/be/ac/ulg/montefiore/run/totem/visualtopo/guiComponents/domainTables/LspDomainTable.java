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
import be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidPathException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.GraphManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.PopupMenuFactory;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/*
* Changes:
* --------
* - 03-Feb-2006: implements getColumnClass in the model, add multiple selection and multiple lsps removal capability (GMO)
* - 03-Apr-2006: remove useless try .. catch blocks (GMO)
* - 23-Oct-2006: add diffserv columns : (classType and preemption level) (GMO)
* - 18-Jan-2007: add more columns to the model (GMO)
* - 04-May-2007: adapt popup menu (GMO)
* - 11-May-2007: add status column (GMO)
* - 25-Sep-2007: add Working path column (GMO)
* - 05-Dec-2007: take care of bypas LSPs (GMO)
* - 15-Jan-2008: now uses PopupMenuFactory (GMO)
* - 28-Feb-2008: add classes of service column (GMO)
*/

/**
 * Table where each row represent a lsp of a domain.
 * It uses a LspTableModel as model.
 *
 * <p>Creation date: 11 janv. 2006
 *
 * @see LspTableModel
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class LspDomainTable extends DomainTable {

    public LspDomainTable(Domain domain) {
        super(new LspTableModel(domain));
        configure();
    }

    public LspDomainTable() {
        super(new LspTableModel(InterDomainManager.getInstance().getDefaultDomain()));
        configure();
    }

    private void configure() {
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        SelectionListener listener = new SelectionListener();
        getSelectionModel().addListSelectionListener(listener);
        //setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    public LspTableModel getModel() {
        return (LspTableModel)super.getModel();
    }

    public Lsp getLspAt(int row) {
        return getModel().getLspAt(row);
    }

    /**
     * A Listener that detect row selection changes in an Lsp table, and highligths the corresponding Lsp
     * in the graph.
     */
    class SelectionListener implements ListSelectionListener {
        /**
         * Get the new selected Lsp and highlight it
         *
         * @param e the fired event
         */
        public void valueChanged(ListSelectionEvent e) {
            Domain domain = getDomain();
            if (e.getValueIsAdjusting())
                return;
            else {
                int row = getSelectedRow();
                if (row != -1 && getSelectedRowCount() == 1 && domain == GUIManager.getInstance().getCurrentDomain()) {
                    GraphManager.getInstance().highlight(getLspAt(row));
                }
                else {
                    GraphManager.getInstance().unHighlight();
                }
            }
        }
    }

    protected JPopupMenu getMenu(MouseEvent evt) {
        int row = rowAtPoint(evt.getPoint());
        if (!isCellSelected(row, 0)) {
            getSelectionModel().setSelectionInterval(row, row);
        }

        Lsp[] lsps = new Lsp[getSelectedRows().length];
        int i = 0;
        for (int k : getSelectedRows()) {
            lsps[i++] = getLspAt(k);
        }

        JPopupMenu menu = PopupMenuFactory.createLspPopupMenu(lsps);
        for (JMenuItem item : getBaseMenuItems()) {
            menu.add(item);
        }

        return menu;
    }
}


/**
 * Model representing the data of the lsps of a domain.
 * Adapt to the lsps changes by implementing lsps events
 * Used by LspDomainTable.
 *
 * @see LspDomainTable
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */
class LspTableModel extends DomainElementTableModel {

    private static Logger logger = Logger.getLogger(LspTableModel.class);

    public LspTableModel(Domain domain) {
        String[] colNames = {"Lsp Id",
                             "Ingress",
                             "Egress",
                             "Reservation",
                             "Class type",
                             "Setup preemption level",
                             "Holding preemption level", //6
                             "Path",
                             "Working Path",           //8
                             "Status",                 //9
                             "Classes of service",     //10
                             "Backups",
                             "Backup Type",
                             "Protected lsp",
                             "Protected links",
                             "Lsp int Id"};
        columnNames = colNames;
        setDomain(domain);
    }

    public Lsp getLspAt(int row) {
        return (Lsp)data.get(row);
    }

    public boolean isColumnDefaultVisible(int column) {
        return column < 6 || column == 7;
    }

    protected void fillData() {
        this.dataMap = new HashMap<Lsp, Integer>();
        data = new ArrayList<Lsp>();
        if (domain != null) {
            int i = 0;
            for (Lsp lsp : domain.getAllLsps()) {
                data.add(i, lsp);
                dataMap.put(lsp, new Integer(i++));
            }
        }
    }

    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
            case 1:
            case 2:
                return String.class;
            case 3:
                return Float.class;
            case 4:
            case 5:
            case 6:
                return Integer.class;
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
                return String.class;
            case 15:
                return Integer.class;
            default:
                return String.class;

        }
    }


    /**
     * Compute the data in a cell, given row and column number
     *
     * @param row the number of the row
     * @param col the number of the column
     * @return returns the Object to store in the cell
     */
    public Object getValueAt(int row, int col) {
        Lsp lsp = getLspAt(row);

        /*
        if (InterDomainManager.getInstance().getDefaultDomain() == null) {
            return null;
        }
        Lsp lsp = InterDomainManager.getInstance().getDefaultDomain().getAllLsps().get(row);
        */

        switch (col) {
            //Lsp Id
            case 0:
                return lsp.getId();
                //Lsp ingress
            case 1:
                return lsp.getLspPath().getSourceNode().getId();
                //Lsp egress
            case 2:
                return lsp.getLspPath().getDestinationNode().getId();
                //Lsp Reservation
            case 3:
                //return String.valueOf(lsp.getReservation());
                return lsp.getReservation();
            case 4:
                return lsp.getCT();
            case 5:
                return lsp.getSetupPreemption();
                //Lsp path
            case 6:
                return lsp.getHoldingPreemption();
            case 7:
                java.util.List lspNodeList = null;
                lspNodeList = lsp.getLspPath().getNodePath();
                String pathStr = "";
                for (Iterator iter = lspNodeList.iterator(); iter.hasNext();) {
                    //remove domain name if present
                    String fullname = new String(((Node) iter.next()).getId());
                    int j = fullname.lastIndexOf('.', fullname.lastIndexOf('.') - 1);
                    if (j == -1)
                        pathStr = pathStr + " > " + fullname;
                    else {
                        pathStr = pathStr + " > " + fullname.substring(0, j);
                    }
                }
                return pathStr;
            case 8:
                try {
                    if (lsp.isBackupLsp()) {
                        if (lsp.isDetourLsp()) {
                            return lsp.isDetourActivated() ? "ACTIVATED" : "DISABLED";
                        } else {
                            return "";
                        }
                    }
                    return lsp.getWorkingPath().toNodesString();
                } catch (InvalidPathException e) {
                    return "NOT ROUTABLE";
                }
            case 9:
                switch (lsp.getLspStatus()) {
                    case Lsp.STATUS_UP:
                        return "UP";
                    case Lsp.STATUS_DOWN:
                        return "DOWN";
                    default:
                        return "UNKNOWN";
                }
            case 10:
                List<String> cos = lsp.getAcceptedClassesOfService();
                if (cos == null || cos.size() <= 0) return "<ALL>";
                String ret = "";
                for (String s : cos) {
                    ret = ret.concat(s).concat(", ");
                }
                ret = ret.substring(0, ret.length()-2);
                return ret;
            case 11:
                ret = "";
                if (lsp.getBackups() != null && lsp.getBackups().size() > 0) {
                    for (Lsp l : lsp.getBackups()) {
                        ret += l.getId();
                        ret += ", ";
                    }
                    ret = ret.substring(0, ret.length()-2);
                }
                return ret;
            case 12:
                String bType = "";
                if (lsp.isBackupLsp()) {
                    switch (lsp.getBackupType()) {
                        case Lsp.DETOUR_E2E_BACKUP_TYPE:
                            bType = "Global Detour";
                            break;
                        case Lsp.DETOUR_LOCAL_BACKUP_TYPE:
                            bType = "Local Detour";
                            break;
                        case Lsp.BYPASS_BACKUP_TYPE:
                            bType = "Bypass";
                    }
                }
                return bType;
            case 13:
                String pLsp = "";
                if (lsp.isDetourLsp()) {
                    try {
                        pLsp = lsp.getProtectedLsp().getId();
                    } catch (LspNotFoundException e) {
                        pLsp = "!lsp not found";
                    }
                }
                return pLsp;
            case 14:
                String pLinks = "";
                if (lsp.isBackupLsp()) {
                    try {
                        for (Link l : lsp.getProtectedLinks()) {
                            pLinks += l.getId();
                            pLinks += ", ";
                        }
                        pLinks = pLinks.substring(0, pLinks.length()-2);
                    } catch (LinkNotFoundException e) {
                        pLinks = "!Link not found";
                    }
                }
                return pLinks;
            case 15:
                try {
                    return domain.getConvertor().getLspId(lsp.getId());
                } catch (LspNotFoundException e) {
                    return -1;
                }
            default:
                return null;
        }
    }

    public void addLspEvent(Lsp lsp) {
        int index = data.size();
        dataMap.put(lsp, index);
        data.add(index, lsp);
        fireTableRowsInserted(index, index);
    }

    public void removeLspEvent(Lsp lsp) {
        int index = (Integer)dataMap.get(lsp);
        dataMap.remove(lsp);
        data.remove(index);
        for (int i = index; i < data.size(); i++) {
            dataMap.put(data.get(i), i);
        }
        fireTableRowsDeleted(index, index);
    }

    public void lspReservationChangeEvent(Lsp lsp) {
        int index = (Integer)dataMap.get(lsp);
        fireTableRowsUpdated(index, index);
    }

    public void lspWorkingPathChangeEvent(Lsp lsp) {
        int index = (Integer)dataMap.get(lsp);
        fireTableRowsUpdated(index, index);
        for (Lsp bLsp : lsp.getBackups()) {
            index = (Integer)dataMap.get(bLsp);
            fireTableRowsUpdated(index, index);
        }
    }

    public void lspStatusChangeEvent(Lsp lsp) {
        int index = (Integer)dataMap.get(lsp);
        fireTableRowsUpdated(index, index);
    }
}


