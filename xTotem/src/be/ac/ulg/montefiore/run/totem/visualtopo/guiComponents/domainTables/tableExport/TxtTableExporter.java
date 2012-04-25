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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.domainTables.tableExport;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

/*
* Changes:
* --------
*
*/

/**
* Exports a Jtable as a text file. Intended to work with tables that use row selections. If some rows are selected,
* only those ones are exported. In none is selected, the whole table is exported.
* <p>
* The first record will contain the column names beginning with a {@link #LINE_COMMENT} and separated by a
* {@link #COL_DELIMITER}. Then a {@link #ROW_DELIMITER} is written. Each row of the table is represented
* by their cell values separated by a {@link #COL_DELIMITER} and the rows are separated by a {@link #ROW_DELIMITER}.
*
* <p>Creation date: 27/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class TxtTableExporter implements TableExporter {
    public final static String ROW_DELIMITER = "\n";
    public final static String COL_DELIMITER = " ";
    public final static String LINE_COMMENT = "#";

    /**
     * Exports the given table to the given text file.
     * @param fileName Name of the file
     * @param table Table to export
     * @throws IOException Thrown if an error occurs while writing to the file.
     */
    public void export(String fileName, JTable table) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));

        // Export selected rows
        int[] rows = table.getSelectedRows();

        // Export entire table if there is no selection
        if (rows == null || rows.length < 1) {
            rows = new int[table.getRowCount()];
            for (int i = 0; i < rows.length; i++) {
                rows[i] = i;
            }
        }

        // Write headers
        for (int col = 0; col < table.getColumnCount(); col++) {
            bw.write(LINE_COMMENT);
            bw.write(table.getColumnName(col));
            if (col != table.getColumnCount())
                bw.write(COL_DELIMITER);
        }
        bw.write(ROW_DELIMITER);

        // Write table content
        for (int row : rows) {
            for (int col = 0; col < table.getColumnCount(); col++) {
                Object o = table.getValueAt(row, col);
                if (o != null) bw.write(o.toString());
                if (col != table.getColumnCount())
                    bw.write(COL_DELIMITER);
            }
            bw.write(ROW_DELIMITER);
        }

        bw.close();

    }

    /**
     * Exports the given table to the given text file.
     * @param file a file
     * @param table Table to export
     * @throws IOException Thrown if an error occurs while writing to the file.
     */
    public void export(File file, JTable table) throws IOException {
        export(file.getAbsolutePath(), table);
    }
}
