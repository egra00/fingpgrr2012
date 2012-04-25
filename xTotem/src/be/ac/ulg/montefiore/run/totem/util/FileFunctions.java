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
package be.ac.ulg.montefiore.run.totem.util;

import java.net.URL;
import java.io.*;

/*
 * Changes:
 * --------
 * - 10-Jan-2007: rename class (FileCopy --> FileFunctions), add getFilenameFromContext(.) method (GMO)
 */

/**
 * Some functions that manipulate files.
 *
 *
 * <p>Creation date: 01-Feb-2005 11:58:05
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class FileFunctions {

    /**
     * Copy a file frow an URL to the outFile
     * @param inFile
     * @param outFile
     * @throws IOException
     */
    public static void copy(URL inFile, String outFile) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(inFile.openStream()));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line;
        while ((line = br.readLine()) != null) {
            bw.write(line);
            bw.write("\n");
        }
        br.close();
        bw.close();
    }

    /**
     * Returns the canonical filename of a file interpreted from the given context.<br>
     * <ul>
     * <li>If the filename correspond to an absolute file, the canonical path corresponding to this file is returned.</li>
     * <li>If the filename is relative, it is interpreted relative to the <code>context</code> directory if it is a
     * directory, or from the directory parent to the <code>context</code>, if it is a regular file.
     * </ul>
     *
     * @param context
     * @param fileName
     * @return the canonical path of the file <code>fileName</code> interpreted in the context <code>context</code>  
     */
    public static String getFilenameFromContext(String context, String fileName) {
        File f = new File(fileName);
        if (f.isAbsolute()) return fileName;

        File c = new File(context);
        if (!c.isDirectory()) c = c.getParentFile();

        String pathName;
        try {
            pathName = new File(c, f.getPath()).getCanonicalPath();
        } catch (IOException e) {
            pathName = new File(c, f.getPath()).getAbsolutePath();
        }

        return pathName; 
    }

}
