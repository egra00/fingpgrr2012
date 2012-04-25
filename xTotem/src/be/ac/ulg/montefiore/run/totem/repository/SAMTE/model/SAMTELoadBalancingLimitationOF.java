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

import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.HeuristicSolution;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.ObjectiveFunction;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;

/*
* Changes:
* --------
*
*/

/**
 * <p>Creation date: 25-Feb-2005 12:23:46
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class SAMTELoadBalancingLimitationOF implements ObjectiveFunction {

    private String name = "SAMTELoadBalancingLimitationOF";
    private static SAMTELoadBalancingLimitationOF instance = null;
    private int nbEvaluated ;
    private long timeToEvaluate;
    private double alpha;

    public SAMTELoadBalancingLimitationOF() {
        nbEvaluated = 0;
        timeToEvaluate = 0;
        alpha = 0.5;
    }

    public SAMTELoadBalancingLimitationOF(double alpha) {
        nbEvaluated = 0;
        timeToEvaluate = 0;
        this.alpha = alpha;
    }

    /**
     * Get the name of the objective function
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    public double getAlpha() {
        return alpha;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    /**
     * Evaluate an heuristic solution
     *
     * @param solution
     * @return the value of the solution
     */
    public double evaluate(HeuristicSolution solution) {
        return evaluate((SAMTESolution) solution);
    }

    /**
     * Evaluate an heuristic solution
     *
     * @param solution
     * @return the value of the solution
     */
    public double evaluate(SAMTESolution solution) {
        /*
        nbEvaluated++;
        long time = System.currentTimeMillis();
        double maxUtil = Double.MAX_VALUE;
        try {
        double[] linkLoad = solution.getRelativeLinkLoad();
        maxUtil = DoubleArrayAnalyse.getMaximum(linkLoad);
        } catch (Exception e) {
        e.printStackTrace();
        }
        timeToEvaluate += System.currentTimeMillis() - time;
        return maxUtil;
        */
        long time = System.currentTimeMillis();
        double[] linkLoad = solution.getRelativeLinkLoad();
        double meanUtil = DoubleArrayAnalyse.getMeanValue(linkLoad);
        double deviationTerm = 0;
        double trafficTerm = 0;
        for (int i = 0; i < linkLoad.length; i++) {
            deviationTerm += Math.pow(linkLoad[i] - meanUtil,2);
            trafficTerm += Math.pow(linkLoad[i],2);
        }
        double score = deviationTerm + alpha * trafficTerm;
        timeToEvaluate += System.currentTimeMillis() - time;
        nbEvaluated++;
        return score;

    }

    public int getNbEvaluated() {
        return nbEvaluated;
    }

    public void resetNbEvaluated() {
        nbEvaluated = 0;
        timeToEvaluate = 0;
    }

    public long getTimeToEvaluate() {
        return timeToEvaluate;
    }
}
