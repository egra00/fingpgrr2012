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
package be.ac.ulg.montefiore.run.totem.repository.SAMTE.scenario;

import be.ac.ulg.montefiore.run.totem.scenario.model.Event;
import be.ac.ulg.montefiore.run.totem.scenario.model.EventResult;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.ObjectiveFunction;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SAParameter;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SANeighbourhood;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.scenario.jaxb.impl.SAMTEImpl;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.scenario.jaxb.ObjectiveType;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.model.SAMTEMaxLoadOF;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.model.SAMTESolution;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.model.RandomOneChangeNBH;

import java.util.List;

/*
* Changes:
* --------
*
*/

/**
 * <p>Creation date: 23-Jun-2005 17:14:55
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class SAMTE extends SAMTEImpl implements Event{

    /**
     * This method must be implemented by each event. This method contains what must be done to
     * process the event.
     */
    public EventResult action() throws EventExecutionException {
        try {
            int asId = isSetASID() ? _ASID : InterDomainManager.getInstance().getDefaultDomain().getASID();
            int tmId = this.isSetTMID() ? _TMID : TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID();
            int nbLsp = this.isSetNbLSP() ? _NbLSP : 5;
            int nbRun = this.isSetNbRun() ? _NbRun : 5;
            String cplName = this.isSetCplName() ? this._CplName : "cpl.txt";

            ObjectiveType objective = ObjectiveType.MAX_LOAD;
            if ((this.isSetSimulatedAnnealing()) && this.getSimulatedAnnealing().isSetObjectiveFunction()) {
                objective = this.getSimulatedAnnealing().getObjectiveFunction().getName();
            }

            float T0 = 0.023333333f;
            int L = 5000;
            float alpha = 0.80f;
            float epsilon = 12f;
            int K = 4;
            if (this.isSetSimulatedAnnealing()) {
                T0 = this.getSimulatedAnnealing().isSetT0() ? this.getSimulatedAnnealing().getT0() : 0.023333333f;
                L = this.getSimulatedAnnealing().isSetL() ? this.getSimulatedAnnealing().getL() : 5000;
                alpha = this.getSimulatedAnnealing().isSetAlpha() ? this.getSimulatedAnnealing().getAlpha() : 0.80f;
                epsilon = this.getSimulatedAnnealing().isSetE() ? this.getSimulatedAnnealing().getE() : 12f;
                K = this.getSimulatedAnnealing().isSetK() ? this.getSimulatedAnnealing().getK() : 4;
            }

            SAMTEThread threads[] = new SAMTEThread[nbRun];
            for (int i = 0; i < threads.length; i++) {
                ObjectiveFunction of = null;
                if (objective.equals(ObjectiveType.MAX_LOAD)) {
                    of = new SAMTEMaxLoadOF();
                } /*else if (objective.equals(ObjectiveType.LOAD_BAL)) {
                    of = SAMTELoadBalancingLimitationOF.getInstance();
                    List<be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Param> params = this.getSimulatedAnnealing().getObjectiveFunction().getParam();
                    if (params != null) {
                        for (int j = 0; j < params.size(); j++) {
                            Param param = params.get(j);
                            if (param.getName().equals("alpha")) {
                                SAMTELoadBalancingLimitationOF.getInstance().setAlpha(Float.parseFloat(param.getValue()));
                            }
                        }
                    }
                }   */

                System.out.println("Create SAMTE Thread " + i);
                threads[i] = new SAMTEThread(i,cplName,of,asId,tmId,nbLsp,new SAParameter(T0,L,alpha,epsilon,K,true));
                threads[i].start();
            }

            // Wait all thread
            try {
                for (int i = 0; i < threads.length; i++) {
                    threads[i].join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Compute the best solution
            List<Lsp> lspList = threads[0].getSolution();
            double bestScore = threads[0].getScore();
            int bestThread = 0;
            for (int i = 1; i < threads.length; i++) {
                if (bestScore > threads[i].getScore()) {
                    bestScore = threads[i].getScore();
                    lspList = threads[i].getSolution();
                    bestThread = i;
                }
            }
            String msg = "Best solution " + bestScore + " from thread " + bestThread;
            System.out.println(msg);
            /*
            be.ac.ulg.montefiore.run.totem.repository.SAMTE.core.SAMTE samte = new be.ac.ulg.montefiore.run.totem.repository.SAMTE.core.SAMTE(cplName,of,asId,tmId);
            List<Lsp> lspList = samte.execute(nbLsp,nbRun,params);
            */

            // Add the solution to the domain
            Domain domain = InterDomainManager.getInstance().getDomain(asId);
            for (int i = 0; i < lspList.size(); i++) {
                Lsp lsp = lspList.get(i);
                System.out.println("LSP "+ i + " : " + lsp.getLspPath().toString());
                domain.addLsp(lsp);
            }
            return new EventResult(lspList, msg);
        } catch (Exception e) {
            e.printStackTrace();
            throw new EventExecutionException(e);
        }
    }

    private class SAMTEThread extends Thread {

        private int threadId;
        private String cplName;
        private ObjectiveFunction of;
        private int asId;
        private int tmId;
        private int nbLsp;
        private SAParameter params;

        private List<Lsp> solution;
        private SAMTESolution samteSolution;

        /**
         * Allocates a new <code>Thread</code> object. This constructor has
         * the same effect as <code>Thread(null, null,</code>
         * <i>gname</i><code>)</code>, where <b><i>gname</i></b> is
         * a newly generated name. Automatically generated names are of the
         * form <code>"Thread-"+</code><i>n</i>, where <i>n</i> is an integer.
         *
         * @see Thread#Thread(ThreadGroup,
                *      Runnable, String)
         */
        public SAMTEThread(int threadId, String cplName, ObjectiveFunction of, int asId, int tmId, int nbLsp, SAParameter params) {
            super((new Integer(threadId).toString()));
            this.threadId = threadId;
            this.cplName = cplName;
            this.of = of;
            this.asId = asId;
            this.tmId = tmId;
            this.nbLsp = nbLsp;
            this.params = params;
        }

        public void run() {
            try {
                System.out.println("T " + threadId + " - Start SAMTE on domain " + asId + " and tm " + tmId + " with " + nbLsp + " LSPs");
                System.out.println("T " + threadId + " - Objective " + of.getName());
                System.out.println("T " + threadId + " - Params " + params.toString());
                SANeighbourhood nbh = new RandomOneChangeNBH();
                be.ac.ulg.montefiore.run.totem.repository.SAMTE.core.SAMTE samte = new be.ac.ulg.montefiore.run.totem.repository.SAMTE.core.SAMTE(cplName,of,nbh,asId,tmId);
                solution = samte.execute(nbLsp,1,params);
                samteSolution = samte.getBestSolution();
                System.out.println("T " + threadId + " - Solution found " + samteSolution.evaluate() + " with " + samteSolution.getLspList().size() + " LSPs");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public List<Lsp> getSolution() {
            return solution;
        }

        public double getScore() {
            return of.evaluate(samteSolution);
        }

    }

}
