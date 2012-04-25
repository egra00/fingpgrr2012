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
package be.ac.ulg.montefiore.run.totem.repository.SAMTE.core;

import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPL;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPLFactory;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPLGenerator;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPLGeneratorParameter;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.model.*;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomainBuilder;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.LspImpl;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidPathException;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.ObjectiveFunction;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SANeighbourhood;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SAParameter;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SASolution;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.facade.SimulatedAnnealing;
import be.ac.ulg.montefiore.run.totem.repository.allDistinctRoutes.AllDistinctRoutesException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.util.FloatArrayAnalyse;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/*
 * Changes:
 * --------
 * 
 */

/**
 * <p>Creation date: 01-Jun-2005 10:12:55
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class SAMTE {

    private Domain domain;

    private ObjectiveFunction scf;
    private SANeighbourhood nbh;
    private RandomInitialSolutionGenerator gen;
    private TrafficMatrix tm;
    private SimplifiedDomain sDomain = null;
    private SinglePathCPL cpl;
    private int NB_SHORTEST_PATH = 5;
    private int MAX_HOP = 7;
    private SAMTESolution bestSolution;

     public SAMTE(String cplName,ObjectiveFunction scf, int tmId) throws InvalidTrafficMatrixException, InvalidDomainException {
        this(cplName,scf,new RandomOneChangeNBH(),InterDomainManager.getInstance().getDefaultDomain().getASID(),tmId);
     }

    public SAMTE(String cplName,ObjectiveFunction scf, SANeighbourhood nbh, int asId, int tmId) throws InvalidTrafficMatrixException, InvalidDomainException {
        domain = InterDomainManager.getInstance().getDomain(asId);
        tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(),tmId);
        sDomain = SimplifiedDomainBuilder.build(domain);
        try {
            cpl = loadCPL(cplName,sDomain);
        } catch (IOException e) {
            try {
                cpl = generateCPL(NB_SHORTEST_PATH,MAX_HOP,true,cplName);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        this.nbh = nbh;
        ((RandomOneChangeNBH) nbh).setCpl(cpl);
        this.scf = scf;
    }

    private SinglePathCPL loadCPL(String fileName, SimplifiedDomain domain) throws IOException {
        long time = System.currentTimeMillis();
        SinglePathCPLFactory factory = new SinglePathCPLFactory();
        SinglePathCPL cpl = factory.loadCPL(fileName,domain);
        time = System.currentTimeMillis() - time;
        cpl.analyse(Thread.currentThread().getName());
        System.out.println("T " + Thread.currentThread().getName() + " - Load " + fileName + " takes " + time + " ms");
        return cpl;
    }

    private SinglePathCPL generateCPL(int NB_SHORTEST_PATH,int MAX_HOP, boolean save, String fileName) throws
            AllDistinctRoutesException, LinkNotFoundException, NodeNotFoundException, IOException {
        long time = System.currentTimeMillis();
        SinglePathCPLGenerator generator = new SinglePathCPLGenerator();
        SinglePathCPL cpl = generator.generate(new SinglePathCPLGeneratorParameter(MAX_HOP,sDomain,NB_SHORTEST_PATH),true);
        time = System.currentTimeMillis() - time;
        cpl.analyse(Thread.currentThread().getName());
        System.out.println("Generate CPL takes " + time + " ms");
        if (save) {
            time = System.currentTimeMillis();
            SinglePathCPLFactory factory = new SinglePathCPLFactory();
            factory.saveCPL(fileName,cpl);
            time = System.currentTimeMillis() - time;
            System.out.println("save CPL in " + fileName + " takes " + time + " ms");
        }
        return cpl;
    }

    public List<Lsp> execute(int nbLsp, int nbSimulation, SAParameter params) throws Exception {
        System.out.println("T " + Thread.currentThread().getName() + " - Real SAMTE.execute()");
        SASolution allSolution[] = new SASolution[nbSimulation];
        SASolution currentSol = null;
        SASolution bestSol = null;
        double meanCost = 0;
        double meanTime = 0;

        gen = new RandomInitialSolutionGenerator(sDomain,tm,nbLsp,cpl);
        SASolution initialSol = gen.generate(scf);


        SimulatedAnnealing sa = new SimulatedAnnealing(nbh,gen,scf,params,null);
        for (int i = 0; i < allSolution.length; i++) {
            scf.resetNbEvaluated();
            nbh.resetNbUsed();
            ((SAMTESolution) initialSol).getLlc().resetTimeToComputePath();
            long time = System.currentTimeMillis();
            currentSol = sa.solve(initialSol);
            time = System.currentTimeMillis() - time;
            double cost = currentSol.evaluate();
            meanTime += time;
            meanCost += cost;
            if ((bestSol == null) || (cost < bestSol.evaluate()))
                bestSol = currentSol;
            System.out.println("Solution " + i + " : " + cost + " (NB evaluated : "
                    + scf.getNbEvaluated() + " in " + scf.getTimeToEvaluate() + " ms)"
                    + " (timeToManageLSPList " + ((SAMTESolution) currentSol).getTimeToManageLSPList() + " ms)"
                    + " (timeToComputePath " + ((SAMTESolution) currentSol).getLlc().getTimeToComputePath() + " ms) "
                    + " takes " + time + " ms");
        }
        meanCost /= allSolution.length;
        meanTime /= allSolution.length;
        System.out.println("Result \t" + meanCost + "\t" + bestSol.evaluate() + "\t " + meanTime + "\t" + initialSol.evaluate());
        float load[] = ((SAMTESolution) bestSol).getLlc().computeLinkLoad(((SAMTESolution) bestSol).getDomain(), tm, ((SAMTESolution) bestSol).getLspList());
        System.out.println("Link Info (max: " + (FloatArrayAnalyse.getMaximum(load) * 100)
                + " %, mean: " + (FloatArrayAnalyse.getMeanValue(load)* 100)
                + " % , std: " + (FloatArrayAnalyse.getStandardDeviation(load) * 100)
                + " %, percentile10: " + (FloatArrayAnalyse.getPercentile10(load) * 100) + " %)");
        //LinkLoadComputation.displayPathChange(sDomain,tm,((SAMTESolution) bestSol).getLspList());
        bestSolution = (SAMTESolution)bestSol;
        return buildLsps((SAMTESolution) bestSol);
    }

    /**
     * Build the list of Lsp from a SAMTESolution.
     *
     * @param solution
     * @return
     * @throws LinkNotFoundException
     * @throws NodeNotFoundException
     */
    private List<Lsp> buildLsps(SAMTESolution solution) throws LinkNotFoundException, NodeNotFoundException, InvalidPathException {
        DomainConvertor convertor = domain.getConvertor();
        List<ExtendedPath> pathList = solution.getLspList();
        List<Lsp> lspList = new ArrayList<Lsp>(pathList.size());
        for (int i = 0; i < pathList.size(); i++) {
           ExtendedPath ePath = pathList.get(i);
           Node src = domain.getNode(convertor.getNodeId(ePath.getIngress()));
           Node dst = domain.getNode(convertor.getNodeId(ePath.getEgress()));
           Lsp lsp = new LspImpl(domain,domain.generateLspId(),tm.get(src.getId(),dst.getId()),ePath.convert(domain));
           lspList.add(lsp);
        }

        return lspList;
    }

    public SAMTESolution getBestSolution() {
        return bestSolution;
    }


}
