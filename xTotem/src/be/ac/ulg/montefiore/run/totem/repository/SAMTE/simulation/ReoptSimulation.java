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

import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.ObjectiveFunction;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SAParameter;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.*;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.BasicIGPShortcutStrategy;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.core.SAMTE;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.model.SAMTEMaxLoadOF;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.text.DateFormat;

/*
* Changes:
* --------
* - 20-Mar-2006: use LinkLoadComputer to calculate load (GMO)
* - 29-Feb-2008: fix bug with link ids (only use up links) (GMO)
*/

/**
 * <p>Creation date: 08-Jul-2005 11:09:05
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class ReoptSimulation {

    private Domain domain;
    private String cplName;
    private String tmDirName;
    private String tmMaxName;
    private String reportName;
    private String outputFile;
    private SAParameter params;
    private ObjectiveFunction fct;
    private int nbLsp;
    private int nbRun;
    private List<Lsp> tmMaxSol;
    private LinkUtilInfo linkUtilInfo[];
    private LinkUtilInfo optLinkUtilInfo[];
    private LinkUtilInfo maxLinkUtilInfo[];
    private float linkCapacities[];
    private double perPairChanged[];
    private double perVolChanged[];


    public ReoptSimulation(int ASID,String cplName, String tmDirName, String tmMaxName, String reportName, String outputFile) {
        try {
            domain = InterDomainManager.getInstance().getDomain(ASID);
        } catch (InvalidDomainException e) {
            e.printStackTrace();
        }
        this.cplName = cplName;
        this.tmDirName = tmDirName;
        this.tmMaxName = tmMaxName;
        this.reportName = reportName;
        this.outputFile = outputFile;
        float T0 = 0.05f;
        int L = 5000;
        float alpha = 0.80f;
        float epsilon = 12f;
        int K = 4;
        params = new SAParameter(T0,L,alpha,epsilon,K,true);
        fct = new SAMTEMaxLoadOF();
        nbLsp = 5;
        nbRun = 2;
    }

    private List<Lsp> executeSAMTE(int tmId, boolean addSol2DB) throws Exception {
        System.out.println("Execute SAMTE on TM " + tmId + " with NB_LSP " + nbLsp + ", NB_RUN " + nbRun + ", params " + params.toString());
        SAMTE samte = new SAMTE(cplName,fct,tmId);
        List<Lsp> lspList = samte.execute(nbLsp,nbRun,params);

        if (addSol2DB) {
            Domain domain = InterDomainManager.getInstance().getDefaultDomain();
            for (int i = 0; i < lspList.size(); i++) {
                Lsp lsp = lspList.get(i);
                //System.out.println("LSP "+ i + " : " + lsp.getLspPath().toString());
                domain.addLsp(lsp);
            }
        }
        return lspList;
    }

    private void updateDomain(List<Lsp> lsp) throws LspAlreadyExistException, LinkCapacityExceededException, LspNotFoundException, DiffServConfigurationException {
        domain.removeAllLsps();
        for (int i = 0; i < lsp.size(); i++) {
            domain.addLsp(lsp.get(i));
        }
    }

    private void processTM(int tmIdx, BufferedWriter writer) throws Exception {
        TrafficMatrix tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(),tmIdx);
        BasicIGPShortcutStrategy llc = new BasicIGPShortcutStrategy(domain, tm);

        Path oldPath[][] = new Path[domain.getNbNodes()][domain.getNbNodes()];
        double totalTraffic = 0;
        for (int i = 0; i < oldPath.length; i++) {
            Node srcNode = domain.getNode(domain.getConvertor().getNodeId(i));
            for (int j = 0; j < oldPath[i].length; j++) {
                if (i != j) {
                    Node dstNode = domain.getNode(domain.getConvertor().getNodeId(j));
                    oldPath[i][j] = llc.computePath(srcNode,dstNode);
                    totalTraffic += tm.get(i,j);
                }

            }
        }

        updateDomain(tmMaxSol);
        double linkUtil[] = llc.getData().getUtilization();

        List<Lsp> currentSol = executeSAMTE(tmIdx,false);
        updateDomain(currentSol);
        double linkUtilOpt[] = llc.getData().getUtilization();
        Path newPath[][] = new Path[domain.getNbNodes()][domain.getNbNodes()];
        for (int i = 0; i < newPath.length; i++) {
            Node srcNode = domain.getNode(domain.getConvertor().getNodeId(i));
            for (int j = 0; j < newPath[i].length; j++) {
                if (i != j) {
                    Node dstNode = domain.getNode(domain.getConvertor().getNodeId(j));
                    newPath[i][j] = llc.computePath(srcNode,dstNode);
                }
            }
        }

        llc.destroy();

        int nbPairChanged = 0;
        perVolChanged[tmIdx] = 0;

        for (int i = 0; i < newPath.length; i++) {
            for (int j = 0; j < newPath[i].length; j++) {
                if ((i != j) && (!(oldPath[i][j].equals(newPath[i][j])))) {
                    nbPairChanged++;
                    perVolChanged[tmIdx] += tm.get(i,j);
                }
            }
        }
        perPairChanged[tmIdx] = (double) nbPairChanged / (double) (domain.getNbNodes() * (domain.getNbNodes()-1));
        perVolChanged[tmIdx] /= totalTraffic;

        List<Link> upLinks = domain.getUpLinks();
        double[] upLinkUtil = new double[upLinks.size()];
        double[] upLinkUtilOpt = new double[upLinks.size()];
        int i = 0;
        for (Link l : upLinks) {
            int idx = domain.getConvertor().getLinkId(l.getId());
            upLinkUtil[i] = linkUtil[idx];
            upLinkUtilOpt[i] = linkUtilOpt[idx];
            i++;
        }

        double varMax = (DoubleArrayAnalyse.getMaximum(upLinkUtil) - DoubleArrayAnalyse.getMaximum(upLinkUtilOpt)) / DoubleArrayAnalyse.getMaximum(upLinkUtilOpt);
        double varMean = (DoubleArrayAnalyse.getMeanValue(upLinkUtil) - DoubleArrayAnalyse.getMeanValue(upLinkUtilOpt)) / DoubleArrayAnalyse.getMeanValue(upLinkUtilOpt);
        double varStd = (DoubleArrayAnalyse.getStandardDeviation(upLinkUtil) - DoubleArrayAnalyse.getStandardDeviation(upLinkUtilOpt)) / DoubleArrayAnalyse.getStandardDeviation(upLinkUtilOpt);
        double varPer10 = (DoubleArrayAnalyse.getPercentile10(upLinkUtil) - DoubleArrayAnalyse.getPercentile10(upLinkUtilOpt)) / DoubleArrayAnalyse.getPercentile10(upLinkUtilOpt);
        double varFortz = (DoubleArrayAnalyse.getIGPWOObjectiveFunctionValue(upLinkUtil,linkCapacities) - DoubleArrayAnalyse.getIGPWOObjectiveFunctionValue(upLinkUtilOpt,linkCapacities)) / DoubleArrayAnalyse.getIGPWOObjectiveFunctionValue(upLinkUtilOpt,linkCapacities);
        maxLinkUtilInfo[tmIdx] = new LinkUtilInfo(DoubleArrayAnalyse.getMaximum(upLinkUtil),DoubleArrayAnalyse.getMeanValue(upLinkUtil),DoubleArrayAnalyse.getStandardDeviation(upLinkUtil),DoubleArrayAnalyse.getPercentile10(upLinkUtil),DoubleArrayAnalyse.getIGPWOObjectiveFunctionValue(upLinkUtil,linkCapacities));
        optLinkUtilInfo[tmIdx] = new LinkUtilInfo(DoubleArrayAnalyse.getMaximum(upLinkUtilOpt),DoubleArrayAnalyse.getMeanValue(upLinkUtilOpt),DoubleArrayAnalyse.getStandardDeviation(upLinkUtilOpt),DoubleArrayAnalyse.getPercentile10(upLinkUtilOpt),DoubleArrayAnalyse.getIGPWOObjectiveFunctionValue(upLinkUtilOpt,linkCapacities));

        linkUtilInfo[tmIdx] = new LinkUtilInfo(varMax,varMean,varStd,varPer10,varFortz);

        DateFormat df1 = DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT);
        StringBuffer line = new StringBuffer(df1.format(tm.getDate().getTime()));
        line.append("\t");
        /*
        line.append(optLinkUtilInfo[tmIdx].max);
        line.append("\t");
        line.append(optLinkUtilInfo[tmIdx].mean);
        line.append("\t");
        line.append(optLinkUtilInfo[tmIdx].std);
        line.append("\t");
        line.append(optLinkUtilInfo[tmIdx].per10);
        line.append("\t");
        line.append(optLinkUtilInfo[tmIdx].fortz);
        */
        line.append(varMax*100);
        line.append("\t");
        line.append(varMean*100);
        line.append("\t");
        line.append(varStd*100);
        line.append("\t");
        line.append(varPer10*100);
        line.append("\t");
        line.append(varFortz);
        line.append("\t");
        line.append(perPairChanged[tmIdx]*100);
        line.append("\t");
        line.append(perVolChanged[tmIdx]*100);
        line.append("\n");
        writer.write(line.toString());
        writer.flush();

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

        List<Link> upLinks = domain.getUpLinks();
        linkCapacities = new float[upLinks.size()];
        int i = 0;
        for (Iterator<Link> it = upLinks.iterator(); it.hasNext(); ++i) {
            Link link = it.next();
            linkCapacities[i] = link.getBandwidth();
        }

        FileWriter outputFileWriter = new FileWriter(outputFile);
        BufferedWriter outputFile = new BufferedWriter(outputFileWriter);
        outputFile.write("# SAMTE simulation on domain " + ((domain.getName() != null) ? domain.getName() : Integer.toString(domain.getASID())) + "\n");
        outputFile.write("# Traffic matrix dir : " + tmDirName + "\n");
        outputFile.write("# NB LSP : " + nbLsp + "\n");
        outputFile.write("# Objective funtion : " + fct.getName() + "\n");
        outputFile.write("# SA params : " + params.toString() + "\n");
        outputFile.write("# Date \t Max \t Mean \t Std \t Per10 \t Fortz \t Nb Pair changed \t Traffic changed (%)\n");


        // Load TM max
        int tmMaxId = tmId;
        TrafficMatrixManager.getInstance().loadTrafficMatrix(tmMaxName,tmMaxId,false);
        int nbTM = tmId;

        linkUtilInfo = new LinkUtilInfo[nbTM];
        optLinkUtilInfo = new LinkUtilInfo[nbTM];
        maxLinkUtilInfo = new LinkUtilInfo[nbTM];
        perPairChanged = new double[nbTM];
        perVolChanged = new double[nbTM];

        // Compute Solution for TM Max
        tmMaxSol = this.executeSAMTE(tmMaxId,false);

        // Process all TM one by one
        for (int tmIdx = 0; tmIdx < nbTM; tmIdx++) {
            TrafficMatrixManager.getInstance().loadTrafficMatrix(tmToLoad[tmIdx],tmIdx,false);
            System.out.println("Load TM " + tmToLoad[tmIdx].substring(tmToLoad[tmIdx].lastIndexOf('/'),tmToLoad[tmIdx].length()));
            this.processTM(tmIdx,outputFile);
            TrafficMatrixManager.getInstance().removeTrafficMatrix(domain.getASID(),tmIdx);
        }

        outputFile.close();
        outputFileWriter.close();
        writeReport(reportName,tmToLoad);
    }


    private void writeReport(String fileName, String tmLoadedName[]) throws Exception {
        FileWriter latexFW = new FileWriter(fileName);
        BufferedWriter latexReport = new BufferedWriter(latexFW);
        latexReport.write("\\documentclass[a4paper,10pt]{article}\n");
        latexReport.write("\\usepackage[T1]{fontenc}\n");
        latexReport.write("\\usepackage{graphicx}\n");

        latexReport.write("\\setlength{\\oddsidemargin}{0cm}\n");
        latexReport.write("\\setlength{\\evensidemargin}{0cm}\n");
        latexReport.write("\\setlength{\\headheight}{0cm}\n");
        latexReport.write("\\setlength{\\headsep}{0cm}\n");
        latexReport.write("\\setlength{\\footskip}{1cm}\n");
        latexReport.write("\\setlength{\\hoffset}{0cm}\n");
        latexReport.write("\\setlength{\\voffset}{0cm}\n");
        latexReport.write("\\setlength{\\textwidth}{16cm}\n");
        latexReport.write("\\setlength{\\textheight}{24cm}\n");
        latexReport.write("\\setlength{\\topmargin}{0cm}\n");

        latexReport.write("\\title{Traffic Matrix analyse}\n\\author{Fabian Skiv�e}\n");
        latexReport.write("\\begin{document}\n\\maketitle\n");

        int maxValueTM = 0;
        double maxValue = Double.MIN_VALUE;
        int meanValueTM = 0;
        double meanValue = Double.MIN_VALUE;
        int stdValueTM = 0;
        double stdValue = Double.MIN_VALUE;
        int per10ValueTM = 0;
        double per10Value = Double.MIN_VALUE;

        double meanMax = 0;
        double meanMean = 0;
        double meanStd = 0;
        double meanPer10 = 0;

        for (int tmIdx = 0; tmIdx < linkUtilInfo.length; tmIdx++) {
            meanMax += linkUtilInfo[tmIdx].max;
            meanMean += linkUtilInfo[tmIdx].mean;
            meanStd += linkUtilInfo[tmIdx].std;
            meanPer10 += linkUtilInfo[tmIdx].per10;

            if (linkUtilInfo[tmIdx].max > maxValue) {
                maxValueTM = tmIdx;
                maxValue = linkUtilInfo[tmIdx].max;
            }
            if (linkUtilInfo[tmIdx].mean > meanValue) {
                meanValueTM = tmIdx;
                meanValue = linkUtilInfo[tmIdx].mean;
            }
            if (linkUtilInfo[tmIdx].std > stdValue) {
                stdValueTM = tmIdx;
                stdValue = linkUtilInfo[tmIdx].std;
            }
            if (linkUtilInfo[tmIdx].per10 > per10Value) {
                per10ValueTM = tmIdx;
                per10Value = linkUtilInfo[tmIdx].per10;
            }
        }

        meanMax /= linkUtilInfo.length;
        meanMean /= linkUtilInfo.length;
        meanStd /= linkUtilInfo.length;
        meanPer10 /= linkUtilInfo.length;

        latexReport.write("\\begin{tabular}{|c|c|c|c|c|c|}\n");
        latexReport.write("\\hline\n");
        latexReport.write("Type & TM & Max & Mean & Std & Per10\\\\\n");
        latexReport.write("\\hline\n");
        StringBuffer line = new StringBuffer("max MAX & ");
        line.append(tmLoadedName[maxValueTM].substring(tmLoadedName[maxValueTM].lastIndexOf('/')+1,tmLoadedName[maxValueTM].length()));
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[maxValueTM].max * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[maxValueTM].mean * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[maxValueTM].std * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[maxValueTM].per10 * 1000f) / 10f);
        line.append("\\\\\n");
        latexReport.write(line.toString());
        latexReport.write("\\hline\n");
        line = new StringBuffer("max MEAN & ");
        line.append(tmLoadedName[meanValueTM].substring(tmLoadedName[meanValueTM].lastIndexOf('/')+1,tmLoadedName[meanValueTM].length()));
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[meanValueTM].max * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[meanValueTM].mean * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[meanValueTM].std * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[meanValueTM].per10 * 1000f) / 10f);
        line.append("\\\\\n");
        latexReport.write(line.toString());
        latexReport.write("\\hline\n");
        line = new StringBuffer("max STD & ");
        line.append(tmLoadedName[stdValueTM].substring(tmLoadedName[stdValueTM].lastIndexOf('/')+1,tmLoadedName[stdValueTM].length()));
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[stdValueTM].max * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[stdValueTM].mean * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[stdValueTM].std * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[stdValueTM].per10 * 1000f) / 10f);
        line.append("\\\\\n");
        latexReport.write(line.toString());
        latexReport.write("\\hline\n");
        line = new StringBuffer("max PER10 & ");
        line.append(tmLoadedName[per10ValueTM].substring(tmLoadedName[per10ValueTM].lastIndexOf('/')+1,tmLoadedName[per10ValueTM].length()));
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[per10ValueTM].max * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[per10ValueTM].mean * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[per10ValueTM].std * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(linkUtilInfo[per10ValueTM].per10 * 1000f) / 10f);
        line.append("\\\\\n");
        latexReport.write(line.toString());
        latexReport.write("\\hline\n");
        line = new StringBuffer("mean & & ");
        line.append(Math.round(meanMax * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(meanMean * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(meanStd * 1000f) / 10f);
        line.append(" & ");
        line.append(Math.round(meanPer10 * 1000f) / 10f);
        line.append("\\\\\n");
        latexReport.write(line.toString());
        latexReport.write("\\hline\n");
        latexReport.write("\\end{tabular}\n \\vspace{0.5cm}\n \n");

        double meanPairChanged = 0;
        double meanVolChanged = 0;
        for (int i = 0; i < perPairChanged.length; i++) {
            meanPairChanged += perPairChanged[i];
            meanVolChanged += perVolChanged[i];
        }

        meanPairChanged /= perPairChanged.length;
        meanVolChanged /= perVolChanged.length;

        latexReport.write("\nMean percentage of pair whose path changed " + meanPairChanged + "\\\\ \n");
        latexReport.write("\nMean percentage of volume whose path changed " + meanVolChanged + "\\\\ \n");

        latexReport.write("\n\\section{Relative comparaison}\n\n");
        latexReport.write("In this section, we present the relative data i.e. (tmMax-opt) / tmMax.\\\\ \n" +
                "\\vspace{0.5cm}");

        int nbElemByArray = 35;
        for (int tmIdx = 0; tmIdx < linkUtilInfo.length; tmIdx++) {
            if ((tmIdx % 25) == 0) {
                if (tmIdx !=0) {
                    latexReport.write("\\hline\n");
                    latexReport.write("\\end{tabular}\n \\newpage \n\n");
                }
                latexReport.write("\\begin{tabular}{|c|c|c|c|c|}\n");
                latexReport.write("\\hline\n");
                latexReport.write("TM & Max & Mean & Std & Per10\\\\\n");
            }
            line = new StringBuffer();
            //System.out.println("TMLoadedName " + tmIdx + " : " + tmLoadedName[tmIdx]);
            line.append(tmLoadedName[tmIdx].substring(tmLoadedName[tmIdx].lastIndexOf('/')+1,tmLoadedName[tmIdx].length()));
            line.append(" & ");
            line.append(Math.round(linkUtilInfo[tmIdx].max * 1000f) / 10f);
            line.append(" & ");
            line.append(Math.round(linkUtilInfo[tmIdx].mean * 1000f) / 10f);
            line.append(" & ");
            line.append(Math.round(linkUtilInfo[tmIdx].std * 1000f) / 10f);
            line.append(" & ");
            line.append(Math.round(linkUtilInfo[tmIdx].per10 * 1000f) / 10f);
            line.append("\\\\ \n");
            latexReport.write("\\hline\n");
            latexReport.write(line.toString());
        }
        latexReport.write("\\hline\n");
        latexReport.write("\\end{tabular}\n");

        latexReport.write("\n\\section{Optimisation on the maximum TM}\n\n");
        latexReport.write("In this section, we present the result obtained by using samte on max TM\\\\ \n" +
                "\\vspace{0.5cm}");

        for (int tmIdx = 0; tmIdx < linkUtilInfo.length; tmIdx++) {
            if ((tmIdx % 25) == 0) {
                if (tmIdx !=0) {
                    latexReport.write("\\hline\n");
                    latexReport.write("\\end{tabular}\n \\newpage \n\n");
                }
                latexReport.write("\\begin{tabular}{|c|c|c|c|c|}\n");
                latexReport.write("\\hline\n");
                latexReport.write("TM & Max & Mean & Std & Per10\\\\\n");
            }
            line = new StringBuffer();
            //System.out.println("TMLoadedName " + tmIdx + " : " + tmLoadedName[tmIdx]);
            line.append(tmLoadedName[tmIdx].substring(tmLoadedName[tmIdx].lastIndexOf('/')+1,tmLoadedName[tmIdx].length()));
            line.append(" & ");
            line.append(Math.round(maxLinkUtilInfo[tmIdx].max * 1000f) / 10f);
            line.append(" & ");
            line.append(Math.round(maxLinkUtilInfo[tmIdx].mean * 1000f) / 10f);
            line.append(" & ");
            line.append(Math.round(maxLinkUtilInfo[tmIdx].std * 1000f) / 10f);
            line.append(" & ");
            line.append(Math.round(maxLinkUtilInfo[tmIdx].per10 * 1000f) / 10f);
            line.append("\\\\ \n");
            latexReport.write("\\hline\n");
            latexReport.write(line.toString());
        }
        latexReport.write("\\hline\n");
        latexReport.write("\\end{tabular}\n");

        latexReport.write("\n\\section{Optimisation on the each TM}\n\n");
        latexReport.write("In this section, we present the result obtained by using samte applied on each TM\\\\ \n\\vspace{0.5cm}\n");

        for (int tmIdx = 0; tmIdx < linkUtilInfo.length; tmIdx++) {
            if ((tmIdx % 25) == 0) {
                if (tmIdx !=0) {
                    latexReport.write("\\hline\n");
                    latexReport.write("\\end{tabular}\n \\newpage \n\n");
                }
                latexReport.write("\\begin{tabular}{|c|c|c|c|c|}\n");
                latexReport.write("\\hline\n");
                latexReport.write("TM & Max & Mean & Std & Per10\\\\\n");
            }
            line = new StringBuffer();
            //System.out.println("TMLoadedName " + tmIdx + " : " + tmLoadedName[tmIdx]);
            line.append(tmLoadedName[tmIdx].substring(tmLoadedName[tmIdx].lastIndexOf('/')+1,tmLoadedName[tmIdx].length()));
            line.append(" & ");
            line.append(Math.round(optLinkUtilInfo[tmIdx].max * 1000f) / 10f);
            line.append(" & ");
            line.append(Math.round(optLinkUtilInfo[tmIdx].mean * 1000f) / 10f);
            line.append(" & ");
            line.append(Math.round(optLinkUtilInfo[tmIdx].std * 1000f) / 10f);
            line.append(" & ");
            line.append(Math.round(optLinkUtilInfo[tmIdx].per10 * 1000f) / 10f);
            line.append("\\\\ \n");
            latexReport.write("\\hline\n");
            latexReport.write(line.toString());
        }
        latexReport.write("\\hline\n");
        latexReport.write("\\end{tabular}\n");

        latexReport.write("\\end{document}\n");
        latexReport.close();
        latexFW.close();
    }

    public int getNbRun() {
        return nbRun;
    }

    public void setNbRun(int nbRun) {
        this.nbRun = nbRun;
    }

    public int getNbLsp() {
        return nbLsp;
    }

    public void setNbLsp(int nbLsp) {
        this.nbLsp = nbLsp;
    }

    public ObjectiveFunction getFct() {
        return fct;
    }

    public void setFct(ObjectiveFunction fct) {
        this.fct = fct;
    }

    public SAParameter getParams() {
        return params;
    }

    public void setParams(SAParameter params) {
        this.params = params;
    }


}
