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

import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCP;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPL;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.ObjectiveFunction;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SAInitialSolutionGenerator;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SASolution;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;

import java.util.Random;

/*
* Changes:
* --------
*
*/

/**
 * <p>Creation date: 25-Feb-2005 13:52:07
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class RandomInitialSolutionGenerator implements SAInitialSolutionGenerator {

    private SimplifiedDomain domain;
    private TrafficMatrix tm;
    private int maxLsp;
    private SinglePathCPL cpl;
    private Random generator;

    public RandomInitialSolutionGenerator(SimplifiedDomain domain, TrafficMatrix tm, int maxLsp,SinglePathCPL cpl) {
        this.domain = domain;
        this.tm = tm;
        this.maxLsp = maxLsp;
        this.cpl = cpl;
        generator = new Random(System.currentTimeMillis());
    }

    /**
     * generate an initial solution
     *
     * @param fct
     * @return
     * @throws Exception
     */
    public SASolution generate(ObjectiveFunction fct) throws Exception {
        SAMTESolution sol = new SAMTESolution(fct,tm,domain);
        int maxTry = 10;

        for(int i=0; i < maxLsp; i++) {
            int nbTry = 0;
            ExtendedPath ePath = null;
             int ingress, egress,destination;
            do {
                ingress = generator.nextInt(domain.getNbNodes());
                egress = generator.nextInt(domain.getNbNodes());
                while (egress == ingress) {
                    egress = generator.nextInt(domain.getNbNodes());
                }
                destination = egress;
                int random = generator.nextInt(this.cpl.getPath(ingress,egress).size());
                int path[] = ((SinglePathCP) this.cpl.getPath(ingress,egress).get(random)).getPath().getLinkIdPath();
                nbTry++;
                ePath = new ExtendedPath(domain,path,new IntDstNodeFEC(destination));
            } while ((nbTry < maxTry) && (sol.addLsp(ePath) == false));
        }
        return sol;
    }

    public String toString() {
        return "RandomInitialSolution";
    }
}
