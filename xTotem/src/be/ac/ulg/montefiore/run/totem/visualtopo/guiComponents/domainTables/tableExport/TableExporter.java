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
import java.io.IOException;
import java.io.File;

/*
* Changes:
* --------
*
*/

/**
 * Interface designed to export the content of a JTable in a file.
 *
 * <p/>
 * <p>Creation date: 27/04/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public interface TableExporter {
    /**
     * Exports the given table to the given file.
     * @param fileName Name of the file
     * @param table Table to export
     * @throws IOException Thrown if an error occurs while writing to the file.
     */
    public void export(String fileName, JTable table) throws IOException;

    /**
     * Exports the given table to the given file.
     * @param file a file
     * @param table Table to export
     * @throws IOException Thrown if an error occurs while writing to the file.
     */
    public void export(File file, JTable table) throws IOException;
}
