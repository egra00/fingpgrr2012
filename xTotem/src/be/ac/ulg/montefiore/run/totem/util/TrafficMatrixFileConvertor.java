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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class contains several methods to convert traffic matrix files from
 * one version of the schema to another.
 *
 * <p>Creation date: 10-mars-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class TrafficMatrixFileConvertor {

    private static final Logger logger = Logger.getLogger(TrafficMatrixFileConvertor.class);

    /**
     * This method updates the traffic matrix file <code>fileName</code> to a
     * traffic matrix file 1.1 valid. The name of the new file is given by
     * <code>targetFileName</code>. If the update fails, the created file is
     * deleted and error messages are printed using logging facilities.
     * @param fileName The file to update.
     * @param targetFileName The target file name. It can be equal to <code>fileName</code>.
     * @throws FileNotFoundException If the file <code>fileName</code> doesn't exist or can't be opened for reading.
     * @throws IOException If an I/O error occurs when reading the file <code>fileName</code> or creating the file <code>targetFileName</code>.
     */
    public static void updateTrafficMatrix1To11(String fileName, String targetFileName) throws FileNotFoundException, IOException {
        logger.info("Updating "+fileName+"...");
        
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        
        BufferedWriter bw;
        File file;
        int i = 0;
        if(targetFileName.equals(fileName)) {
            do {
                file = new File(targetFileName+i);
                ++i;
            } while(file.exists());
            bw = new BufferedWriter(new FileWriter(file));
        }
        else {
            file = new File(targetFileName);
            bw = new BufferedWriter(new FileWriter(file));
        }

        String line = br.readLine();
        while(line != null) {
            if(line.contains("noNamespaceSchemaLocation")) {
                line = line.replaceFirst("v1_0", "v1_1");
                bw.write(line);
                bw.newLine();
                break;
            }
            bw.write(line);
            bw.newLine();
            line = br.readLine();
        }
        
        if(line == null) {
            logger.error("Attribute noNamespaceSchemaLocation not found!");
            // Delete the file
            br.close();
            bw.flush();
            bw.close();
            file.delete();
            // End of file reached so we can return...
            return;
        }
        
        line = br.readLine();
        while(line != null) {
            if((line.contains("TrafficMatrix")) && (!line.contains("TrafficMatrixFile"))) {
                line = line.replaceFirst("TrafficMatrix", "IntraTM");
                bw.write(line);
                bw.newLine();
                if(line.contains("</")) {
                    break;
                }
                else {
                    line = br.readLine();
                    continue;
                }
            }
            bw.write(line);
            bw.newLine();
            line = br.readLine();
        }
        
        if(line == null) {
            logger.error("Element TrafficMatrix not found!");
            // Delete the file
            br.close();
            bw.flush();
            bw.close();
            file.delete();
            return;
        }
        
        line = br.readLine();
        while(line != null) {
            bw.write(line);
            bw.newLine();
            line = br.readLine();
        }
        
        br.close();
        bw.flush();
        bw.close();
        if(targetFileName.equals(fileName)) {
            if(!file.renameTo(new File(targetFileName))) {
                logger.error("The renaming operation didn't succeed! The file is named "+file.getName()+" instead of "+targetFileName);
            }
        }
    }


    /**
     * convert Traffic Matrix file from 1.1 to 1.2 : change <date> format (date -> dateTime), update version number.
     * @param fileName
     * @param targetFileName
     * @throws IOException
     */
    public static void updateTrafficMatrix11To12(String fileName, String targetFileName) throws IOException {
        logger.info("Updating "+fileName+"...");

        BufferedReader br = new BufferedReader(new FileReader(fileName));

        BufferedWriter bw;
        File file;
        int i = 0;
        if(targetFileName.equals(fileName)) {
            do {
                file = new File(targetFileName+i);
                ++i;
            } while(file.exists());
            bw = new BufferedWriter(new FileWriter(file));
        }
        else {
            file = new File(targetFileName);
            bw = new BufferedWriter(new FileWriter(file));
        }

        boolean error = false;
        String line = br.readLine();
        while(line != null && !error) {
            if(line.contains("noNamespaceSchemaLocation")) {
                if (!line.contains("v1_1")) {
                    logger.error("Not at 1.1 format.");
                    error = true;
                } else {
                    line = line.replaceFirst("v1_1", "v1_2");
                    bw.write(line);
                    bw.newLine();
                }
                break;
            }
            bw.write(line);
            bw.newLine();
            line = br.readLine();
        }

        if(line == null) {
            logger.error("Attribute noNamespaceSchemaLocation not found!");
            error = true;
        }
        if (error) {
            // Delete the file
            br.close();
            bw.flush();
            bw.close();
            file.delete();
            // End of file reached so we can return...
            return;
        }

        line = br.readLine();

        int mode = 0;
        boolean next = true;
        while (line != null) {
            switch (mode) {
                case 0:
                     if (line.contains("<info")) {
                         logger.info("<info> found.");
                         mode = 1;
                         next = false;
                     }
                     else next = true;
                 break;
                case 1:
                     if (line.contains("<date")) {
                         logger.info("<date> found.");
                         mode = 2;
                         next = false;
                     }
                    else next = true;
                 break;
                case 2:
                    Pattern p = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\d");
                    Matcher m = p.matcher(line);
                    if (m.find()) {
                        logger.info("found date. appending time.");
                        bw.write(line.substring(0, m.end()));
                        bw.write("T00:00:00");
                        bw.write(line.substring(m.end()));
                        mode = 3;
                        bw.newLine();
                        line = br.readLine();
                        next = false;
                    }
                    else if (line.contains("</")) {
                        logger.warn("Date not found");
                        mode = 3;
                    }
                    else next = true;
                    break;
                 case 3:
                    next = true;
                 break;
            }
            if (next) {
                bw.write(line);
                bw.newLine();
                line = br.readLine();
            }
        }

        switch (mode) {
            case 0:
                logger.warn("<info> not found.");
                break;
            case 1:
                logger.warn("<date> not found.");
                break;
            case 2:
                logger.error("Wrong date format.");
                break;
            case 3:
                break;
            default:
                logger.error("undefined error");
                break;
        }

        /* cancel changes */
        if (mode == 2 || mode > 3) {
            logger.info("reversing changes");
            // Delete the file
            br.close();
            bw.flush();
            bw.close();
            file.delete();
            // End of file reached so we can return...
            return;
        }

        logger.info("Applying changes");
        /* apply changes */
        br.close();
        bw.flush();
        bw.close();
        if(targetFileName.equals(fileName)) {
            if(!file.renameTo(new File(targetFileName))) {
                logger.error("The renaming operation didn't succeed! The file is named "+file.getName()+" instead of "+targetFileName);
            }
        }
    }
}