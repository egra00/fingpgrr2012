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

import javax.swing.*;
import java.awt.*;
import java.io.File;

/*
 * Changes:
 * --------
 *
 * - 03-May-2005: Fix javadoc (JL).
 * - 23-Oct-2006: Add LoadTopo method with a parameter indicating current directory to display (GMO)
 */

/**
 * <p>Creation date: 15-Feb-2005
 *
 * @author Olivier Materne (O.Materne@student.ulg.ac.be)
 */
public class TopoChooser extends JFileChooser {

    static private File topoFile = null;

    /**
     * Create and display in container c a JFileChooser specially
     * designed to load .xml files. Return the chosen file.
     */
    public File loadTopo(Container c) {
        if (topoFile == null)
            topoFile = new File(".");
        return loadTopo(c, topoFile);
    }

    /**
     * Create and display in container c a JFileChooser specially
     * designed to load .xml files. Return the chosen file.
     * @param c
     * @param from Indication of the current directory
     * @return
     */
    public File loadTopo(Container c, File from) {
        if (topoFile == null)
            topoFile = new File(".");

        this.setCurrentDirectory(from);
        this.addChoosableFileFilter(new XmlFilter());
        int choice = this.showOpenDialog((Component) c);
        if (choice == JFileChooser.APPROVE_OPTION) {
            topoFile = this.getSelectedFile();
            return topoFile;
        }
        return null;
    }


    /**
     * Diplay a File chooser to load a new Matrix from a file
     *
     * @param c
     * @return the file to be loaded
     */
    public File loadMatrix(Container c) {

        if (topoFile == null)
            topoFile = new File(".");

        this.setCurrentDirectory(topoFile);
        this.addChoosableFileFilter(new XmlFilter());
        int choice = this.showOpenDialog((Component) c);
        if (choice == JFileChooser.APPROVE_OPTION) {
            topoFile = this.getSelectedFile();
            return topoFile;
        }
        return null;
    }


    /**
     * Select a File where to save a Domain
     *
     * @param c
     * @return the selected file
     */
    public File saveTopo(Container c) {

        this.setCurrentDirectory(topoFile);
        this.addChoosableFileFilter(new XmlFilter());
        int choice = this.showSaveDialog((Component) c);
        if (choice == JFileChooser.APPROVE_OPTION) {
            topoFile = this.getSelectedFile();
            return topoFile;
        }
        return null;
    }


    /**
     * This class define a filter that accept only directories and .xml files
     * <p/>
     * *
     */

    class XmlFilter extends javax.swing.filechooser.FileFilter {

        /**
         * return true if the file has a XML extension
         *
         * @param f the file to be tested
         * @return true if the extension is .XML; else false
         */
        public boolean accept(File f) {
            if (f.isDirectory())
                return true;

            String fileName = f.getName();
            int i = fileName.lastIndexOf('.');
            if (i < fileName.length() - 1 && i > 0) {
                if (fileName.substring(i + 1).toLowerCase().equals("xml"))
                    return true;
            }
            return false;
        }

        public String getDescription() {
            return ".xml";
        }
    }

}
