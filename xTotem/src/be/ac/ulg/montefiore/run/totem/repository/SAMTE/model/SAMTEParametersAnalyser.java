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

package be.ac.ulg.montefiore.run.totem.repository.SAMTE.model;


import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPL;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPLGenerator;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPLGeneratorParameter;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomainBuilder;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.ObjectiveFunction;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.facade.SimulatedAnnealing;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SAInitialSolutionGenerator;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SANeighbourhood;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SAParameter;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SASolution;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Description
 * <p/>
 * Creation date : 07-Dec-2004 09:53:55
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class SAMTEParametersAnalyser {

    private int NB_ITER = 5;

    private float T0Max;
    private float T0Min;
    private int nbT0;

    private int LMax;
    private int LMin;
    private int nbL;

    private float alphaMax;
    private float alphaMin;
    private int nbAlpha;

    private float epsilonMax;
    private float epsilonMin;
    private int nbEpsilon;

    private int K2Max;
    private int K2Min;
    private int nbK2;

    private ArrayList neighbourhoodList;
    private ArrayList solutionGeneratorList;
    private Domain domain = null;

    private ObjectiveFunction scf;

    public SAMTEParametersAnalyser(ObjectiveFunction scf) {
        this.scf = scf;
    }

    private void init() throws Exception {
        domain = InterDomainManager.getInstance().getDefaultDomain();
        TrafficMatrix tm = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix();

        SimplifiedDomain sDomain = SimplifiedDomainBuilder.build(domain);
        sDomain.getLinks();

        SinglePathCPLGenerator generator = new SinglePathCPLGenerator();
        SinglePathCPL cpl = generator.generate(new SinglePathCPLGeneratorParameter(7,sDomain,5),true);
        cpl.analyse();

        T0Max = 0.03f;
        T0Min = 0.01f;
        nbT0 = 4;

        LMax = 5 * cpl.size();
        LMin = cpl.size() / 2;
        nbL = 4;

        alphaMax = 0.975f;
        alphaMin = 0.80f;
        nbAlpha = 4;

        epsilonMax = 11;
        epsilonMin = 2;
        nbEpsilon = 3;

        K2Max = 6;
        K2Min = 2;
        nbK2 = 3;

        int nbLspPerSol = 3;
        RandomOneChangeNBH nbh = new RandomOneChangeNBH();
        nbh.setCpl(cpl);
        neighbourhoodList = new ArrayList();
        neighbourhoodList.add(nbh);
        solutionGeneratorList = new ArrayList();
        solutionGeneratorList.add(new RandomInitialSolutionGenerator(sDomain,tm,nbLspPerSol,cpl));
    }

    public void analyse(String reportDir, double minParetoBound, double maxParetoBound, double paretoDelta) throws Exception {

        String outputFileName = reportDir + File.separatorChar + "params-report.txt";
        String dataFileName = reportDir + File.separatorChar + "params-report.dat";
        String gnuplotFileName = reportDir + File.separatorChar + "params-report.gp";
        String reportFileName = reportDir + File.separatorChar + "params-report.tex";

        init();
        AnalyseReport report = new AnalyseReport(minParetoBound,maxParetoBound,paretoDelta);

        float currentT0 = T0Min;
        int currentL = LMin;
        float currentAlpha = alphaMin;
        float currentEpsilon = epsilonMin;
        int currentK2 = K2Min;
        SASolution currentSolution;

        SAParameter bestParam = null;
        double bestCost = Double.MAX_VALUE;
        long bestTime = Long.MAX_VALUE;

        FileWriter fw1 = new FileWriter(dataFileName);
        BufferedWriter gnuplotFile = new BufferedWriter(fw1);

        FileWriter fw = new FileWriter(outputFileName);
        BufferedWriter br = new BufferedWriter(fw);
        StringBuffer sb = new StringBuffer();
        br.write("SAParameters Analyser on file " + domain.getName() + " with " + domain.getUpNodes().size() + " nodes and " + domain.getUpLinks().size() +  " links\n");
        System.out.println("SAParameters Analyser on file " + domain.getName() + " with " + domain.getUpNodes().size() + " nodes and " + domain.getUpLinks().size() +  " links\n");
        int nbAnalyse = solutionGeneratorList.size() * neighbourhoodList.size() * nbT0 * nbL * nbEpsilon * nbAlpha * nbK2;
        br.write("Analysis of " + nbAnalyse + " differents set of parameters\n\n");
        System.out.println("Analysis of " + nbAnalyse + " differents set of parameters");

        long allTime = System.currentTimeMillis();
        for (int i = 0; i < solutionGeneratorList.size(); i++) {
            SAInitialSolutionGenerator gen  = (SAInitialSolutionGenerator) solutionGeneratorList.get(i);
            for (int j = 0; j < neighbourhoodList.size(); j++) {
                SANeighbourhood nbh = (SANeighbourhood) neighbourhoodList.get(j);
                br.write("Neigbourhood : " + nbh.toString() + " Solution generator " + gen.toString() + "\n");
                for(int idxT0=0;idxT0 < nbT0;idxT0++) {
                    currentT0 = T0Min + idxT0 * ((T0Max - T0Min) / (nbT0 - 1));
                    for(int idxL=0;idxL < nbL;idxL++) {
                        currentL = LMin + idxL * ((LMax - LMin) / (nbL - 1));
                        for(int idxAlpha=0;idxAlpha < nbAlpha;idxAlpha++) {
                            currentAlpha = alphaMin + idxAlpha * ((alphaMax - alphaMin) / (nbAlpha - 1));
                            for(int idxEpsilon=0;idxEpsilon < nbEpsilon;idxEpsilon++) {
                                currentEpsilon = epsilonMin + idxEpsilon * ((epsilonMax - epsilonMin) / (nbEpsilon - 1));
                                for(int idxK2=0;idxK2 < nbK2;idxK2++) {
                                    currentK2 = K2Min + idxK2 * ((K2Max - K2Min) / (nbK2 - 1));
                                    try {
                                        SAParameter params = new SAParameter(currentT0,currentL,currentAlpha,currentEpsilon,currentK2,true);
                                        SimulatedAnnealing sa = new SimulatedAnnealing(nbh,gen,scf,params);
                                        long meanTime = 0;
                                        double meanCost = 0;
                                        double topCost = Double.MAX_VALUE;
                                        int nbTopCost = 0;
                                        SASolution allSol[] = new SASolution[NB_ITER];
                                        for (int k = 0; k < NB_ITER; k++) {
                                            System.gc();
                                            long time = System.currentTimeMillis();
                                            currentSolution = sa.solve();
                                            allSol[k] = currentSolution;
                                            time = System.currentTimeMillis() - time;
                                            meanTime += time;
                                            double cost = currentSolution.evaluate();
                                            meanCost += cost;
                                            if (cost < topCost) {
                                                topCost = cost;
                                                nbTopCost = 1;
                                            } else if (cost == topCost) {
                                                nbTopCost++;
                                            }
                                        }
                                        meanTime = (long) (meanTime / NB_ITER);
                                        meanCost  = (meanCost / (float) NB_ITER);
                                        if ((meanCost < bestCost) || ((meanCost == bestCost) && (meanTime < bestTime))) {
                                            bestCost = meanCost;
                                            bestTime = meanTime;
                                            bestParam = params;
                                        }
                                        for (int k = 0; k < allSol.length; k++) {
                                            report.addSolution(allSol[k],params,meanCost,allSol[k].evaluate(),meanTime);
                                        }

                                        sb.delete(0,sb.length());
                                        sb.append("\tParams ");
                                        sb.append(params.toString());
                                        sb.append(" - mean cost : ");
                                        sb.append(meanCost);
                                        sb.append(" - best cost : ");
                                        sb.append(topCost);
                                        sb.append(" in ");
                                        sb.append(((float) nbTopCost / (float) NB_ITER) * (float) 100);
                                        sb.append(" % (");
                                        sb.append(meanTime);
                                        sb.append(" ms)\n");
                                        br.write(sb.toString());
                                        System.out.println(sb.toString());
                                        br.flush();
                                        gnuplotFile.write(meanTime + "\t" + meanCost + "\n");
                                        gnuplotFile.flush();
                                    } catch (IllegalArgumentException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        allTime = System.currentTimeMillis() - allTime;
        br.write("The best set of parameters found is : " + bestParam.toString() + " : " + bestCost  + " in " + bestTime + " ms\n");
        br.write("The simulation takes " + allTime + " ms");
        br.close();
        fw.close();
        gnuplotFile.close();
        fw1.close();
        report.printLatexReport(reportFileName);
        createReport(reportDir,gnuplotFileName,dataFileName);
    }

    public void createReport(String reportDir,String gnuplotFileName,String dataFileName) throws IOException {
        FileWriter fw = new FileWriter(gnuplotFileName);
        BufferedWriter br = new BufferedWriter(fw);
        br.write("set title \"Simulated Annealing parameter analyse\"\n");
        br.write("set xlabel \"Time (ms)\"\n");
        br.write("set ylabel \"Mean Utility\"\n");
        br.write("set terminal postscript eps color solid \"Times-Roman\" 18\n");
        br.write("set output \"params-report.eps\"\n");
        br.write("plot \"params-report.dat\" using 1:2\n");
        br.close();
        fw.close();
        fw = new FileWriter(reportDir + File.separator + "Makefile");
        br = new BufferedWriter(fw);
        br.write("LATEX_FILE=params-report\n");
        br.write("OUTPUT_FILE=params-report\n\n");
        br.write("all:\n");
        br.write("\tgnuplot params-report.gp\n");
        br.write("\tlatex $(LATEX_FILE)\n");
        br.write("\tdvips -t landscape $(LATEX_FILE) -o $(OUTPUT_FILE).ps\n");
        br.close();
        fw.close();
    }


    public class AnalyseReport {

        private Solution minMeanCost;
        private Solution maxMeanCost;
        private Solution minMinCost;
        private Solution maxMinCost;
        private Solution minMeanTime;
        private Solution maxMeanTime;
        private Solution paretoSolutions[];
        private Solution worstSolutions[];
        private double paretoMinBound;
        private double paretoMaxBound;
        private double delta;

        public AnalyseReport(double paretoMinBound, double paretoMaxBound, double delta) {
            int nbSolution = (int) Math.floor((paretoMaxBound - paretoMinBound) / delta);
            paretoSolutions = new Solution[nbSolution];
            worstSolutions = new Solution[nbSolution];
            this.paretoMinBound = paretoMinBound;
            this.paretoMaxBound = paretoMinBound + delta * (paretoSolutions.length - 1.0);
            this.delta = delta;
        }

        public int findSolutionIdx(double cost) {
            if (cost >= this.paretoMaxBound)
                return this.paretoSolutions.length-1;
            int idx = (int) Math.floor((cost - paretoMinBound) / delta);
            return idx;
        }

        public void addSolution(SASolution sol, SAParameter param, double meanCost, double minCost, long meanTime) {
            System.out.println("Add sol (mean cost " + meanCost + ", minCost " + minCost + ", meanTime " + meanTime +")");
            Solution newSolution = null;
            if ((minMeanCost == null) || (meanCost < minMeanCost.getMeanCost())) {
                if (newSolution == null)
                    newSolution = new Solution(sol,param,meanCost,minCost,meanTime);
                minMeanCost = newSolution;
            }
            if ((maxMeanCost == null) || (meanCost > maxMeanCost.getMeanCost())) {
                if (newSolution == null)
                    newSolution = new Solution(sol,param,meanCost,minCost,meanTime);
                maxMeanCost = newSolution;
            }
            if ((minMinCost == null) || (minCost < minMinCost.getMeanCost())) {
                if (newSolution == null)
                    newSolution = new Solution(sol,param,meanCost,minCost,meanTime);
                minMinCost = newSolution;
            }
            if ((maxMinCost == null) || (minCost > maxMinCost.getMeanCost())) {
                if (newSolution == null)
                    newSolution = new Solution(sol,param,meanCost,minCost,meanTime);
                maxMinCost = newSolution;
            }
            if ((minMeanTime == null) || (meanTime < minMeanTime.getMeanCost())) {
                if (newSolution == null)
                    newSolution = new Solution(sol,param,meanCost,minCost,meanTime);
                minMeanTime = newSolution;
            }
            if ((maxMeanTime == null) || (meanTime > maxMeanTime.getMeanCost())) {
                if (newSolution == null)
                    newSolution = new Solution(sol,param,meanCost,minCost,meanTime);
                maxMeanTime = newSolution;
            }
            int solIdx = findSolutionIdx(meanCost);
            if ((paretoSolutions[solIdx] == null) || (paretoSolutions[solIdx].getMeanTime() > meanTime)) {
                if (newSolution == null)
                    newSolution = new Solution(sol,param,meanCost,minCost,meanTime);
                paretoSolutions[solIdx] = newSolution;
            }
            if ((worstSolutions[solIdx] == null) || (worstSolutions[solIdx].getMeanTime() < meanTime)) {
                if (newSolution == null)
                    newSolution = new Solution(sol,param,meanCost,minCost,meanTime);
                worstSolutions[solIdx] = newSolution;
            }
        }

        public void printLatexReport(String fileName) throws IOException {
            FileWriter fw = new FileWriter(fileName);
            BufferedWriter br = new BufferedWriter(fw);

            br.write("\\documentclass[11pt,a4paper]{article}\n");
            br.write("\\usepackage[T1]{fontenc}\n");
            br.write("\\usepackage[dvips]{graphicx}\n\n");
            br.write("\\begin{document}\n");

            br.write("\\begin{figure}[!h]\n");
            br.write("\\centering\n");
            br.write("\\includegraphics[height=7cm]{params-report.eps}\n");
            br.write("\\caption{\\label{params} \\textit{Parameter Analysis}}\n");
            br.write("\\end{figure}\n\n");
            br.write("\\begin{table}[!h]\n" + "\\begin{center}\n" + "\\begin{tabular}{|c|c|c|c|c|c|c|c|c|c|c|c|}\n");
            br.write("\\hline T0 & L & Alpha & E & K2 & mean cost & min cost & time & max & mean & std & per10 \\\\ \n");
            br.write(printLatexSolution(minMeanCost));
            br.write(printLatexSolution(maxMeanCost));
            br.write(printLatexSolution(minMinCost));
            br.write(printLatexSolution(maxMinCost));
            br.write(printLatexSolution(minMeanTime));
            br.write(printLatexSolution(maxMeanTime));
            br.write("\\hline \n" +
                    "\\end{tabular}\n" +
                    "\\caption{\\label{solution} Solutions}\n" +
                    "\\end{center}\n" +
                    "\\end{table}\n\n");

            br.write("\\begin{table}[!h]\n" + "\\begin{center}\n" + "\\begin{tabular}{|c|c|c|c|c|c|c|c|c|c|c|c|}\n");
            br.write("\\hline T0 & L & Alpha & E & K2 & mean cost & min cost & time & max & mean & std & per10 \\\\ \n");
            for (int i = 0; i < paretoSolutions.length; i++) {
                Solution paretoSolution = paretoSolutions[i];
                if (paretoSolution != null)
                    br.write(printLatexSolution(paretoSolution));
            }
            br.write("\\hline \n" +
                    "\\end{tabular}\n" +
                    "\\caption{\\label{pareto-sol} Pareto optimal solutions}\n" +
                    "\\end{center}\n" +
                    "\\end{table}\n\n");

            br.write("\\begin{table}[!h]\n" + "\\begin{center}\n" + "\\begin{tabular}{|c|c|c|c|c|c|c|c|c|c|c|c|}\n");
            br.write("\\hline T0 & L & Alpha & E & K2 & mean cost & min cost & time & max & mean & std & per10 \\\\ \n");
            for (int i = 0; i < worstSolutions.length; i++) {
                Solution paretoSolution = worstSolutions[i];
                if (paretoSolution != null)
                    br.write(printLatexSolution(paretoSolution));
            }
            br.write("\\hline \n" +
                    "\\end{tabular}\n" +
                    "\\caption{\\label{worste-sol} Worste solutions}\n" +
                    "\\end{center}\n" +
                    "\\end{table}\n\n");
            br.write("\\end{document}\n");
            br.close();
            fw.close();
        }

        public String printLatexSolution(Solution sol) {
            StringBuffer sb = new StringBuffer("\\hline ");
            sb.append(sol.getParam().getT0());
            sb.append(" & ");
            sb.append(sol.getParam().getL());
            sb.append(" & ");
            sb.append(sol.getParam().getAlpha());
            sb.append(" & ");
            sb.append(sol.getParam().getEpsilon2());
            sb.append(" & ");
            sb.append(sol.getParam().getK2());
            sb.append(" & ");
            sb.append((float) Math.round(sol.getMeanCost()* 10000) / 100.0);
            sb.append(" & ");
            sb.append((float) Math.round(sol.getMinCost()* 10000) / 100.0);
            sb.append(" & ");
            sb.append(sol.getMeanTime());
            double load[] = ((SAMTESolution) sol.getSol()).getRelativeLinkLoad();
            sb.append(" & ");
            sb.append((float) Math.round(DoubleArrayAnalyse.getMaximum(load)* 10000) / 100.0);
            sb.append(" & ");
            sb.append((float) Math.round(DoubleArrayAnalyse.getMeanValue(load)* 10000) / 100.0);
            sb.append(" & ");
            sb.append((float) Math.round(DoubleArrayAnalyse.getStandardDeviation(load)* 10000) / 100.0);
            sb.append(" & ");
            sb.append((float) Math.round(DoubleArrayAnalyse.getPercentile10(load)* 10000) / 100.0);
            sb.append("\\\\ \n");
            return sb.toString();
        }
    }

    public class Solution {

        private SASolution sol;
        private SAParameter param;
        private double meanCost;
        private double minCost;
        private long meanTime;

        public Solution(SASolution sol, SAParameter param, double meanCost, double minCost, long meanTime) {
            this.sol = sol;
            this.param = param;
            this.meanCost = meanCost;
            this.minCost = minCost;
            this.meanTime = meanTime;
        }

        public SASolution getSol() {
            return sol;
        }

        public void setSol(SASolution sol) {
            this.sol = sol;
        }

        public SAParameter getParam() {
            return param;
        }

        public void setParam(SAParameter param) {
            this.param = param;
        }

        public double getMeanCost() {
            return meanCost;
        }

        public void setMeanCost(double meanCost) {
            this.meanCost = meanCost;
        }

        public double getMinCost() {
            return minCost;
        }

        public void setMinCost(double minCost) {
            this.minCost = minCost;
        }

        public long getMeanTime() {
            return meanTime;
        }

        public void setMeanTime(long meanTime) {
            this.meanTime = meanTime;
        }
    }


}
