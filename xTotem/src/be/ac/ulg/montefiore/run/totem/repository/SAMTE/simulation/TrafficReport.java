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
package be.ac.ulg.montefiore.run.totem.repository.SAMTE.simulation;

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SAParameter;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.ObjectiveFunction;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Iterator;
import java.text.DateFormat;

/*
 * Changes:
 * --------
 * 
 */

/**
 * <p>Creation date: 18-Jul-2005 15:59:22
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class TrafficReport {

    private Domain domain;
    private String tmDirName;
    private String reportName;

    public TrafficReport(Domain domain, String tmDirName, String reportName) {
        this.domain = domain;
        this.tmDirName = tmDirName;
        this.reportName = reportName;
    }

    private void processTM(int tmIdx,BufferedWriter writer) throws Exception {
        TrafficMatrix tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(),tmIdx);
        DomainConvertor convertor = domain.getConvertor();
        List<Node> nodes = domain.getAllNodes();
        double total = 0;
        for (int i = 0; i < nodes.size(); i++) {
            Node srcNode = nodes.get(i);
            for (int j = 0; j < nodes.size(); j++) {
                Node dstNode = nodes.get(j);
                total += tm.get(convertor.getNodeId(srcNode.getId()),convertor.getNodeId(dstNode.getId()));
            }
        }
        DateFormat df1 = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
        StringBuffer line = new StringBuffer(df1.format(tm.getDate().getTime()));
        line.append("\t");
        line.append(total);
        line.append("\n");
        writer.write(line.toString());
     }

    public void start() throws Exception {
        String tmToLoad[] = null;
        File f = new File(tmDirName);
        int tmId = 0;
        if (!f.isDirectory()) {
            System.out.println(tmDirName + " not a directory !!!");
        } else {
            File listOfFiles[] = f.listFiles();
            Arrays.sort(listOfFiles); //,new FileNameComparator());
            tmToLoad = new String[listOfFiles.length];
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].getAbsolutePath().contains(".xml")) {
                    tmToLoad[tmId] = listOfFiles[i].getAbsolutePath();
                    tmId++;
                }
            }
        }

        FileWriter outputFileWriter = new FileWriter(reportName);
        BufferedWriter outputFile = new BufferedWriter(outputFileWriter);
        outputFile.write("# SAMTE simulation on domain " + ((domain.getName() != null) ? domain.getName() : Integer.toString(domain.getASID())) + "\n");
        outputFile.write("# Traffic matrix dir : " + tmDirName + "\n");
        outputFile.write("# Date \t Volume\n");

        int nbTM = tmId;

        // Process all TM one by one
        for (int tmIdx = 0; tmIdx < nbTM; tmIdx++) {
            TrafficMatrixManager.getInstance().loadTrafficMatrix(tmToLoad[tmIdx],tmIdx,false);
            System.out.println("Load TM " + tmToLoad[tmIdx].substring(tmToLoad[tmIdx].lastIndexOf('/'),tmToLoad[tmIdx].length()));
            this.processTM(tmIdx,outputFile);
            TrafficMatrixManager.getInstance().removeTrafficMatrix(domain.getASID(),tmIdx);
        }

        outputFile.close();
        outputFileWriter.close();
    }

}
