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
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.domainTables.tableExport.TableExporter;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.domainTables.tableExport.TxtTableExporter;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;

/*
* Changes:
* --------
* - 03-Feb-2006: add sort capability (only ascending) (GMO)
* - 22-Nov-2006: sort only with mouse button 1 (GMO)
* - 18-Jan-2007: add column model to the table and a popup menu to show/hide columns. add isColumnDefaultVisible(.) method to the model. (GMO)
* - 18-Jan-2007: add tooltip (GMO)
* - 03-May-2007: add export feature (GMO)
* - 03-May-2007: move DomainTableModel into another class (GMO)
* - 15-Jan-2008: add getBaseMenuItems() (GMO)
*/

/**
* Represent a JTable based an a domain. Use a DomainTableModel as model.
*
* <p>Creation date: 11 janv. 2006
*
* @see DomainTableModel
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class DomainTable extends JTable {

    final XTableColumnModel columnModel = new XTableColumnModel();
    final JPopupMenu popup = new JPopupMenu();

    private final ExportActionListener exportAction = new ExportActionListener(); 

    public DomainTable(DomainTableModel dm) {
        super(dm);
        setColumnModel(columnModel);
        createDefaultColumnsFromModel();

        for (int i = 0; i < getModel().getColumnCount(); i++) {
            final int columnIndex = i;
            String s = getModel().getColumnName(i);
            final TableColumn tc = columnModel.getColumnByModelIndex(columnIndex);

            final JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(s);
            boolean visible = getModel().isColumnDefaultVisible(columnIndex);
            menuItem.setState(visible);
            columnModel.setColumnVisible(tc, visible);

            menuItem.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    boolean visible = columnModel.isColumnVisible(tc);
                    columnModel.setColumnVisible(tc, !visible);
                    menuItem.setState(!visible);
                }
            });
            popup.add(menuItem);
        }

        getTableHeader().addMouseListener(new SortListener());
        getTableHeader().addMouseListener(new ShowHideColumn());


        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                showMenu(e);
            }

            public void mouseReleased(MouseEvent e) {
                showMenu(e);
            }
            private void showMenu(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    getMenu(e).show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

    }

    final protected JMenuItem[] getBaseMenuItems() {
        JMenuItem[] items = new JMenuItem[1];
        items[0] = new JMenuItem("Export as txt");
        items[0].addActionListener(exportAction);
        return items;
    }

    protected JPopupMenu getMenu(MouseEvent evt) {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem[] items = getBaseMenuItems();
        for (int i = 0; i < items.length; i++) {
            popup.add(items[i]);
        }
        return popup;
    }

    public void setDomain(Domain domain) {
        getModel().setDomain(domain);
    }

    public Domain getDomain() {
        return getModel().getDomain();
    }

    public DomainTableModel getModel() {
        return (DomainTableModel)super.getModel();
    }

    private DomainTable getThis() {
        return this;
    }

    public Component prepareRenderer(TableCellRenderer renderer,
                                         int rowIndex, int vColIndex) {
            Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
            if (c instanceof JComponent) {
                JComponent jc = (JComponent)c;
                jc.setToolTipText(String.valueOf(getValueAt(rowIndex, vColIndex)));
            }
            return c;
    }

    protected class ExportActionListener implements ActionListener {
        private TableExporter tbe;
        private File lastFile;

        public ExportActionListener() {
            tbe = new TxtTableExporter();
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            JFileChooser fch;
            if (lastFile != null) {
                fch = new JFileChooser(lastFile.getParentFile());
            } else fch =  new JFileChooser();

            int choice = fch.showSaveDialog(getThis());
            if (choice == JFileChooser.APPROVE_OPTION) {
                File f = fch.getSelectedFile();
                try {
                    tbe.export(f, getThis());
                    lastFile = f;
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), "File saved as " + f.getAbsolutePath(), "File saved", JOptionPane.INFORMATION_MESSAGE);
                } catch (IOException e1) {
                    e1.printStackTrace();
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), "IOException: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                return;
            }

        }
    }

    protected class SortListener extends MouseAdapter {

        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                TableColumnModel tcm = getTableHeader().getColumnModel();
                int colIndex = tcm.getColumnIndexAtX(e.getX());
                int col = tcm.getColumn(colIndex).getModelIndex();
                if (col < 0) return;
                getModel().sortByColumn(col);
            }
        }
    }

    protected class ShowHideColumn extends MouseAdapter  {
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }
        private void showPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

}

