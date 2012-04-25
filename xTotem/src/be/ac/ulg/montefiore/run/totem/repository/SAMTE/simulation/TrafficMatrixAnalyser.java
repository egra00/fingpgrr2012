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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.DomainConvertor;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;

import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.text.DateFormat;

/*
* Changes:
* --------
*
*/

/**
 * <p>Creation date: 19-Jul-2005 13:42:53
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class TrafficMatrixAnalyser {

    private static final int MAX_NB_INTER = 1000;

    private Domain domain;
    private String tmDirName;
    private String reportName;
    private double volumeByInter[][];
    private double pairsByInter[][];
    private int nbInter = 0;
    private int tmIdxMaxVol = 0;
    private double tmMaxVol = 0;
    private double interSize = 40000;
    private int nbTM = 0;

    public TrafficMatrixAnalyser(Domain domain, String tmDirName, String reportName) {
        this.domain = domain;
        this.tmDirName = tmDirName;
        this.reportName = reportName;
    }

    /*
    public void generateDataPlotFile(float tmArray[],String fileName) throws IOException {
    FileWriter fw = new FileWriter(fileName);
    BufferedWriter bw = new BufferedWriter(fw);
    bw.write("# Idx Value");
    for (int i = 0; i < tmArray.length; i++) {
    bw.write(i + "\t" + tmArray[i] + "\n");
    }
    bw.close();
    fw.close();
    }

    public void generateHistogramDataFile(int nbIntervals, float tmArray[],String fileName) throws IOException {
    float min = Float.MAX_VALUE;
    float max = 0;
    for (int i = 0; i < tmArray.length; i++) {
    if (tmArray[i] < min)
    min = tmArray[i];
    if (tmArray[i] > max)
    max = tmArray[i];
    }
    max = max + (float) 0.001;

    int intervalArray[] = new int[nbIntervals];
    for (int i = 0; i < intervalArray.length; i++) {
    intervalArray[i] = 0;
    }

    float intervalLength = (max - min) / nbIntervals;
    for (int i = 0; i < tmArray.length; i++) {
    int intervalIdx = Math.round((tmArray[i] - min) / intervalLength);
    if (intervalIdx >= intervalArray.length)
    intervalIdx = intervalArray.length-1;
    intervalArray[intervalIdx]++;
    }

    // Generate data file
    FileWriter fw = new FileWriter(fileName + ".dat");
    BufferedWriter bw = new BufferedWriter(fw);
    bw.write("# Idx Value\n");
    for (int i = 0; i < intervalArray.length; i++) {
    bw.write(i + "\t" + (intervalArray[i] / (float) tmArray.length) + "\n");
    }
    bw.close();
    fw.close();

    // Generate gnuplot file
    fw = new FileWriter(fileName + ".gp");
    bw = new BufferedWriter(fw);
    bw.write("set encoding iso_8859_1\n");
    bw.write("set title \"Distribution of the traffic matrix using " + nbIntervals + " intervals of " + intervalLength + "\"\n");
    bw.write("set xlabel \"Bandwidth interval\"\n");
    bw.write("set ylabel \"%\"\n");
    bw.write("set terminal postscript eps color\n");
    bw.write("set output \"figs/tm-"+ nbIntervals + "int.eps\n");
    bw.write("plot \"data/tm-"+ nbIntervals + "int.dat\" with boxes\n");
    bw.close();
    fw.close();
    }
    */

    private void processTM(int tmIdx,double interSize) throws Exception{
        TrafficMatrix tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(),tmIdx);
        DomainConvertor convertor = domain.getConvertor();
        List<Node> nodes = domain.getAllNodes();
        double nbPairs = 0;
        double totalTraffic = 0;
        int intIdx = 0;
        for (int i = 0; i < nodes.size(); i++) {
            Node srcNode = nodes.get(i);
            for (int j = 0; j < nodes.size(); j++) {
                if (i != j) {
                    Node dstNode = nodes.get(j);
                    float traffic = tm.get(convertor.getNodeId(srcNode.getId()),convertor.getNodeId(dstNode.getId()));

                    nbPairs++;
                    totalTraffic += traffic;
                    intIdx = (int) Math.floor((double) traffic / (double) interSize);
                    if (intIdx >= nbInter) {
                        nbInter = intIdx+1;
                    }
                    if (intIdx >= MAX_NB_INTER) {
                        System.out.println("MAX_NB_INTER (" + MAX_NB_INTER + ") must be upgrade to " + nbInter);
                    } else {
                        pairsByInter[tmIdx][intIdx] += 1d;
                        volumeByInter[tmIdx][intIdx] += traffic;
                    }
                }
            }
        }
        if (totalTraffic > tmMaxVol) {
            tmIdxMaxVol = tmIdx;
            tmMaxVol = totalTraffic;
        }

        for (int i = 0; i < nbInter; i++) {
            volumeByInter[tmIdx][i] /= totalTraffic;
            pairsByInter[tmIdx][i] /= nbPairs;
        }

        System.out.println("NbPairs " + nbPairs + " total Traffic " + totalTraffic  + " nbInter " + nbInter);
    }

    private void writeReport() throws Exception {
        DateFormat df1 = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
        TrafficMatrix tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(),tmIdxMaxVol);
        int tmIdx = tmIdxMaxVol;
        FileWriter outputFileWriter = new FileWriter(reportName);
        BufferedWriter outputFile = new BufferedWriter(outputFileWriter);
        outputFile.write("# Traffic matrix analysis on domain " + ((domain.getName() != null) ? domain.getName() : Integer.toString(domain.getASID())) + "\n");
        outputFile.write("# Traffic matrix : " + df1.format(tm.getDate().getTime()) + "\n");
        outputFile.write("# Interval \t Pairs \t Volume\n");
        for (int i = 0; i < nbInter; i++) {
            StringBuffer line = new StringBuffer();
            line.append(i * (interSize/1000d));
            line.append("\t");
            line.append(pairsByInter[tmIdx][i]);
            line.append("\t");
            line.append(volumeByInter[tmIdx][i]);
            line.append("\n");
            outputFile.write(line.toString());
        }
        outputFile.close();
        outputFileWriter.close();
    }

    private void writeMeanReport() throws Exception {
        double meanVolumeByInter[] = new double[nbInter];
        double meanPairsByInter[] = new double[nbInter];

        for (int tmIdx = 0; tmIdx < pairsByInter.length; tmIdx++) {
            for (int intIdx = 0; intIdx < nbInter; intIdx++) {
                meanPairsByInter[intIdx] = pairsByInter[tmIdx][intIdx];
                meanVolumeByInter[intIdx] = volumeByInter[tmIdx][intIdx];
            }
        }

        double normPairs = 0;
        double normVolume = 0;
        for (int i = 0; i < nbInter; i++) {
            meanPairsByInter[i] = meanPairsByInter[i] / nbTM;
            normPairs += meanPairsByInter[i];
            meanVolumeByInter[i] = meanVolumeByInter[i] / nbTM;
            normVolume += meanVolumeByInter[i];
        }

        for (int i = 0; i < nbInter; i++) {
            meanPairsByInter[i] = meanPairsByInter[i] / normPairs;
            meanVolumeByInter[i] = meanVolumeByInter[i] / normVolume;
        }

        FileWriter outputFileWriter = new FileWriter(reportName);
        BufferedWriter outputFile = new BufferedWriter(outputFileWriter);
        outputFile.write("# Traffic matrix analysis on domain " + ((domain.getName() != null) ? domain.getName() : Integer.toString(domain.getASID())) + "\n");
        outputFile.write("# NB Traffic matrix : " + nbTM + "\n");
        outputFile.write("# Interval \t Pairs \t Volume\n");
        for (int i = 0; i < nbInter; i++) {
            StringBuffer line = new StringBuffer();
            line.append(i * (interSize/1000d));
            line.append("\t");
            line.append(meanPairsByInter[i]);
            line.append("\t");
            line.append(meanVolumeByInter[i]);
            line.append("\n");
            outputFile.write(line.toString());
        }
        outputFile.close();
        outputFileWriter.close();
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
        nbTM = tmId;

        volumeByInter = new double[nbTM][MAX_NB_INTER];
        pairsByInter = new double[nbTM][MAX_NB_INTER];
        for (int i = 0; i < volumeByInter.length; i++) {
            for (int j = 0; j < volumeByInter[i].length; j++) {
                volumeByInter[i][j] = 0;
                pairsByInter[i][j] = 0;
            }
        }

        // Process all TM one by one
        for (int tmIdx = 0; tmIdx < nbTM; tmIdx++) {
            TrafficMatrixManager.getInstance().loadTrafficMatrix(tmToLoad[tmIdx],tmIdx,false);
            System.out.println("Load TM " + tmToLoad[tmIdx].substring(tmToLoad[tmIdx].lastIndexOf('/'),tmToLoad[tmIdx].length()));
            this.processTM(tmIdx,interSize);
            if (tmIdx != tmIdxMaxVol) {
                TrafficMatrixManager.getInstance().removeTrafficMatrix(domain.getASID(),tmIdx);
            }
        }

        DateFormat df1 = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
        TrafficMatrix tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(),tmIdxMaxVol);

        System.out.println("Max volume TM " + df1.format(tm.getDate().getTime()) + " : " + tmMaxVol);
        System.out.println("Interval Size " + interSize + " : " + nbInter);
        //writeReport();
        writeMeanReport();

    }

}
