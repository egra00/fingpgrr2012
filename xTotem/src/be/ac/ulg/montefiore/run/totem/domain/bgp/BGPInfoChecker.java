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
package be.ac.ulg.montefiore.run.totem.domain.bgp;

import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidFileException;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.io.*;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
* Some helper functions to check BGP dumps directory and filename information.
*
* <p>Creation date: 17/09/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public final class BGPInfoChecker {

    public final static Pattern MRTFormatPattern = Pattern.compile("\\|[A-Za-z]+\\|((\\d{1,3}\\.?){4,4})\\|\\d+\\|[\\d\\./]*\\|(\\d+)(\\s\\d+)*\\|[A-Za-z]+\\|((\\d{1,3}\\.?){4,4})\\|\\d+\\|");
    private final static Logger logger = Logger.getLogger(BGPInfoChecker.class);

    public static final boolean isIdNaming(Domain domain, String dir) throws InvalidFileException {
        List<Node> nodes = domain.getAllNodes();

        // Additional checks on the format...
        String id = nodes.get(0).getId();
        File tempFile = new File(dir + File.separator + id);
        if (tempFile.exists() && tempFile.isDirectory()) {
            return true; // directories named according id
        } else {
            id = nodes.get(0).getRid();
            tempFile = new File(dir + File.separator + id);
            if (tempFile.exists() && tempFile.isDirectory()) {
                return false; // directories named according rid
            } else {
                throw new InvalidFileException("Directories have to be named according routers id or rid!, aborting.");
            }
        }
    }


    public static final boolean isMRTFile(String filename) {
        File file = new File(filename);

        boolean gzipped = filename.endsWith(".gz");

        // Little test with regular expressions (to enhance) to see if format is MRT ASCII READABLE FORMAT
        BufferedReader in = null;
        try {
            if (gzipped) {
                FileInputStream fis = new FileInputStream(file);
                GZIPInputStream gis = new GZIPInputStream(fis);
                InputStreamReader isr = new InputStreamReader(gis);
                in = new BufferedReader(isr);
            } else {
                FileReader fr = new FileReader(file);
                in = new BufferedReader(fr);
            }

            String line;
            if ((line = in.readLine()) != null) {

                Matcher matcher = MRTFormatPattern.matcher(line);
                if (matcher.find()) {
                    if (matcher.group(1) == null || matcher.group(3) == null || matcher.group(5) == null) {
                        logger.warn("File seems to be in MRT ASCII MACHINE READABLE FORMAT but some fields seem missing");
                        return false;
                    }
                } else {
                    logger.warn("File not in MRT ASCII MACHINE READABLE FORMAT! Use route_btoa utily to convert it!");
                    return false;
                }
            } else {
                logger.warn("File is empty!");
                return false;
            }
        } catch (FileNotFoundException e) {
            logger.warn("File " + filename + " does not exists.");
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("IOException");
            return false;
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }


}
