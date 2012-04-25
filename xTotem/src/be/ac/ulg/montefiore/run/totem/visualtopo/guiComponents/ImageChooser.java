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
import javax.swing.filechooser.FileFilter;
import javax.imageio.ImageIO;
import java.io.File;
import java.awt.*;


/**
* File Chooser used to save an image in one of the writing format specified in ImageIO
*
* <p>Creation date: 13 d�c. 2005
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ImageChooser extends JFileChooser {
    static private File oldFile = null;
    static private final String DEFAULT_EXTENSION = "png";


    /**
     * Display a dialog to choose a file, the filters works by extension
     * @param c
     * @return the chosen file
     */
    public File saveImage(Container c) {

        FileFilter defaultFilter = null;

        if (oldFile != null)
            this.setCurrentDirectory(oldFile.getParentFile());

        for (String s : ImageIO.getWriterFormatNames()) {
            FileFilter filter = new ExtensionFilter(s);
            this.addChoosableFileFilter(filter);
            if (s.equals(DEFAULT_EXTENSION)) {
                defaultFilter = filter;
            }
        }
        this.setFileFilter(defaultFilter);

        int choice = this.showSaveDialog((Component) c);
        if (choice == JFileChooser.APPROVE_OPTION) {
            oldFile = this.getSelectedFile();
            return oldFile;
        }
        return null;
    }

    /**
     * Filter that accept all files matching the given extension (in the constructor)
     */
    class ExtensionFilter extends FileFilter {
        private String extension = null;

        /**
         * cretae a filter that will match all files with extension <code>extension</code>
         * @param extension
         */
        public ExtensionFilter(String extension) {
            this.extension = extension;
        }

        public boolean accept(File f) {
            if (f.isDirectory())
                return true;

            String fileName = f.getName();
            int i = fileName.lastIndexOf('.');
            if (i < fileName.length() - 1 && i > 0) {
                if (fileName.substring(i + 1).toLowerCase().equals(extension.toLowerCase()))
                    return true;
            }
            return false;
        }

        public String getDescription() {
            return "*." + extension;
        }

    }

}
