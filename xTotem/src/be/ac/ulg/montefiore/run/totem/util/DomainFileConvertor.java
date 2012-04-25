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

import org.apache.log4j.Logger;

import java.io.*;

/*
* Changes:
* --------
*
*/

/**
*
* Converts domains from various schema version
*
* <p>Creation date: 12 juil. 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class DomainFileConvertor {
    private static final Logger logger = Logger.getLogger(DomainFileConvertor.class);


    /**
     * Upgrade the Schema instance number.
     * Add the units part in information part. Default units are mbps for bandwidth and ms for delay.
     * It also tries to respect the indentation when adding the units part.
     * Info element must be alone on a line.
     * This function only adds bandwidth and delay units if units part is not present.
     * @param fileName
     * @param targetFileName
     * @throws IOException
     */
    public static void updateDomain10To11(String fileName, String targetFileName) throws IOException {
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
                if (!line.contains("v1_0")) {
                    logger.error("Not in 1.0 format");
                    br.close();
                    bw.flush();
                    bw.close();
                    file.delete();
                    return;
                }
                line = line.replaceFirst("v1_0", "v1_1");
                bw.write(line);
                bw.newLine();
                line = br.readLine();
                break;
            }
            bw.write(line);
            bw.newLine();
            line = br.readLine();
        }

        String indentString = null;
        boolean inInfo = false;
        while (line != null) {
            if (line.contains("<info>")) {
                inInfo = true;
                indentString = line.substring(0, line.indexOf("<info>"));
                for (char c : indentString.toCharArray()) {
                    if (!Character.isWhitespace(c)) {
                        logger.error("Invalid character sequence. XML element <info> must be the only element on its line.");
                        logger.info("Using default indent string");
                        indentString = "    ";
                    }
                }
            } else if (line.contains("</info>") || line.contains("<diff-serv>")) {
                logger.info("Units not found.");
                inInfo = false;

                bw.write(indentString);
                bw.write(indentString);
                bw.write("<units>");
                bw.newLine();
                bw.write(indentString);
                bw.write(indentString);
                bw.write(indentString);
                bw.write("<unit type=\"bandwidth\" value=\"mbps\"/>");
                bw.newLine();
                bw.write(indentString);
                bw.write(indentString);
                bw.write(indentString);
                bw.write("<unit type=\"delay\" value=\"ms\"/>");
                bw.newLine();
                bw.write(indentString);
                bw.write(indentString);
                bw.write("</units>");
                bw.newLine();

                bw.write(line);
                bw.newLine();
                break;
            } else if (inInfo && line.contains("<units>")) {
                logger.info("Units found.");
                bw.write(line);
                bw.newLine();
                break;
            }
            bw.write(line);
            bw.newLine();
            line = br.readLine();
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
     * Converts domains with schema located in http://totem.info.ucl.ac.be/Schema/Domain.xsd to v1.1
     * @param fileName
     * @param targetFileName
     * @throws IOException
     */
    public static void domainUcl2Domain10(String fileName, String targetFileName) throws IOException {

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
                if (line.contains("http://totem.info.ucl.ac.be/Schema/Domain.xsd")) {
                    logger.info("Schema UCL found.");
                }
                line = line.replaceFirst("http://totem.info.ucl.ac.be/Schema/Domain.xsd", "http://totem.run.montefiore.ulg.ac.be/Schema/Domain-v1_0.xsd");
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
            logger.info("reversing changes");
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
            bw.write(line);
            bw.newLine();
            line = br.readLine();
        }

        logger.info("Applying changes");
        br.close();
        bw.flush();
        bw.close();
        if(targetFileName.equals(fileName)) {
            if(!file.renameTo(new File(targetFileName))) {
                logger.error("The renaming operation didn't succeed! The file is named "+file.getName()+" instead of "+targetFileName);
            }
        }
    }

    public static void addLocation(String fileName, String targetFileName) throws IOException {
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
        String pLine= null;
        if (line != null && line.contains("<xml")) {
            pLine = line;
            line = br.readLine();
            if (line.contains("<domain")) {
                bw.write(pLine);
                bw.write("");
                bw.write(line);
            }
        }

        line = br.readLine();
        while (line != null) {
            bw.write(line);
            line = br.readLine();
        }

    }

}
