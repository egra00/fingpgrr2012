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

import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.CandidatePathList;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCP;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPL;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SANeighbourhood;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SASolution;

import java.util.Random;

/*
* Changes:
* --------
* 03-Feb.-2006 : implements getNbNeighbour (GMO).
*/

/**
 * <p>Creation date: 28-Feb-2005 15:45:03
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class RandomOneChangeNBH implements SANeighbourhood {

    private static final int MAX_TRY = 100;
    private int nbUsed;
    private SinglePathCPL cpl;
    private ExtendedPath oldLsp;
    private ExtendedPath newLsp;
    private int oldLspIdx;
    private Random generator;

    public RandomOneChangeNBH() {
        nbUsed = 0;
        cpl = null;
        generator = new Random(System.currentTimeMillis());
    }

    public CandidatePathList getCpl() {
        return cpl;
    }

    public void setCpl(SinglePathCPL cpl) {
        this.cpl = cpl;
    }

    public int getNbUsed() {
        return nbUsed;
    }

    public void resetNbUsed() {
        nbUsed = 0;
    }

    public String toString() {
        return "Random One change";
    }

    /**
     * Generate a neighbour of the solution given in argument and modify the given solution.
     *
     * @param solution
     */
    public void computeNeighbour(SASolution solution) {
        long time = System.currentTimeMillis();
        long timeAdd = 0;
        SAMTESolution sol = (SAMTESolution) solution;
        SimplifiedDomain domain = sol.getDomain();
        int nbLsp = sol.getLspList().size();
        oldLspIdx = generator.nextInt(nbLsp);
        oldLsp = sol.getLspList().get(oldLspIdx);
        //System.out.println("Random number between [0,"+nbLsp+"] : " + oldLspIdx);
        try {
            if (!sol.removeLsp(oldLspIdx)) {
                System.out.println("Loop when remove LSP " + oldLspIdx);
            } else {
                int nbIter = 0;
                boolean mustContinue;
                timeAdd = System.currentTimeMillis();
                do {
                    mustContinue = false;
                    int ingress = generator.nextInt(domain.getNbNodes());
                    int egress = generator.nextInt(domain.getNbNodes());
                    while (egress == ingress) {
                        egress = generator.nextInt(domain.getNbNodes());
                    }
                    int destination = egress;
                    int random = generator.nextInt(this.cpl.getPath(ingress,egress).size());
                    int path[] = ((SinglePathCP) this.cpl.getPath(ingress,egress).get(random)).getPath().getLinkIdPath();
                    newLsp = new ExtendedPath(domain,path,new IntDstNodeFEC(destination));
                    //System.out.println("Add new LSP " + ePath.toString());
                    nbIter++;
                    if (sol.addLsp(newLsp) == false) {
                        mustContinue = true;
                    }
                } while ((nbIter < MAX_TRY) && (mustContinue));
                timeAdd = System.currentTimeMillis() - timeAdd;
                if (nbIter >= MAX_TRY) {
                    System.out.println("More than " + MAX_TRY + " try create loops");
                    sol.addLsp(oldLsp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        time = System.currentTimeMillis() - time;
        //System.out.println("Compute neighbour " + nbUsed  + " takes " + time + " (timeToAdd " + timeAdd + ")");
        nbUsed++;
    }

    /**
     * Return to the previous solution.
     * Be carreful to call this method with the same solution as the past call of the computeNeighbour method
     *
     * @param solution
     */
    public void returnToPreviousSolution(SASolution solution) {
        SAMTESolution sol = (SAMTESolution) solution;
        //sol.getLspList().set(oldLspIdx,oldLsp);
        try {
            sol.removeLsp(newLsp);
            sol.addLsp(oldLsp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Return the size of the neighbourhood
     *
     * @return
     */
    public int getNbNeighbour() {
        return (cpl == null) ? 0 : cpl.size();
    }

}
