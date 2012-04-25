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
package be.ac.ulg.montefiore.run.totem.trafficMatrix.model;

import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.persistence.TrafficMatrixFactory;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;

/**
 * <p>Creation date: 20 avr. 2007
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */

public class ListOfTrafficMatrices {
    private static final Logger logger = Logger.getLogger(ListOfTrafficMatrices.class);

    Pattern filePattern = Pattern.compile(".*-(\\d+)-(\\d+)-(\\d+)-(\\d\\d)(\\d\\d)\\.xml");
//    Pattern filePattern = Pattern.compile(".*-(2004)-(03)-(01)-(00)(\\d\\d)\\.xml");

    TreeMap<Calendar,File> sortedListOfTMs;

    /**
     * This constructor loads all the traffic matrices in the directory and
     * builds the corresponding Traffic Matrix List
     * @param directoryToLoad : directory in which all the traffic matrix to load are loacted
     */
    public ListOfTrafficMatrices(String directoryToLoad) throws IOException {
        sortedListOfTMs = new TreeMap<Calendar,File>();

        File rootTrafficMatrixDirectory = new File(directoryToLoad);
        if (!rootTrafficMatrixDirectory.canRead()) {
            throw new IOException();
        }

        this.recursiveListDirectory(rootTrafficMatrixDirectory);

        System.out.println("All the " + sortedListOfTMs.size() + " Traffic Matrices are loaded in the Toolbox.");


    }

    public boolean hasNextTM() {
        return !sortedListOfTMs.isEmpty();
    }

    public File getNextTM() {
        return sortedListOfTMs.get(sortedListOfTMs.firstKey());
    }

    public Calendar getNextDate() {
        return sortedListOfTMs.firstKey();
    }

    public void removeNextTm() {
        sortedListOfTMs.remove(sortedListOfTMs.firstKey());
    }

    private void recursiveListDirectory(File directory) throws IOException {
        File trafficMatrixDirectory = directory;
        if (!trafficMatrixDirectory.canRead()) {
            throw new IOException();
        }

        String[] listedDirectoriesAndFiles = trafficMatrixDirectory.list();
        for (int i = 0; i < listedDirectoriesAndFiles.length; i++) {
            //System.out.println(i + " : " + listedDirectoriesAndFiles[i]);
            String newFile = directory + File.separator + listedDirectoriesAndFiles[i];
            File currentFile = new File(newFile);
            if (currentFile.isDirectory()) {
                this.recursiveListDirectory(currentFile);
            } else {
                this.addTrafficMatrix(currentFile);
            }
        }
    }

    private void addTrafficMatrix(File trafficMatrixFile) {
        //System.out.println("Loading traffic matrix " + trafficMatrixFile.getAbsolutePath());

        Calendar thisTMDate = Calendar.getInstance();

        //System.out.println(trafficMatrixFile.getName());
        Matcher result = filePattern.matcher(trafficMatrixFile.getName());
        if (result.matches()) {
            // System.out.println("date is finded");
            //System.out.println(result.group(1) + " " + result.group(2) + " " + result.group(3) + " " + result.group(4) + " " + result.group(5));

            String year = result.group(1);
            String month = result.group(2);
            String day = result.group(3);
            String hour = result.group(4);
            String min = result.group(5);

            thisTMDate.set(Integer.parseInt(year), Integer.parseInt(month) - 1, Integer.parseInt(day), Integer.parseInt(hour), Integer.parseInt(min));
            sortedListOfTMs.put(thisTMDate, trafficMatrixFile);
        } else {
            logger.error("date is not finded for " + trafficMatrixFile.getName() + " : please update the patern for the traffic matrix in the ListOfTrafficMatrices class");
            //System.out.println("date is not finded for " + trafficMatrixFile.getName() + " : please update the patern for the traffic matrix in the ListOfTrafficMatrices class");
        }

    }

}
