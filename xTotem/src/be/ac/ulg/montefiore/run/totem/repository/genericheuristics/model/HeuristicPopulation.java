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
package be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model;

import java.util.ArrayList;

/*
 * Changes:
 * --------
 *
 */

/**
 * An heuristic population is a list of solution
 *
 * <p>Creation date: 24-Nov-2004 16:01:21
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class HeuristicPopulation extends ArrayList<HeuristicSolution> {

    /**
     * Create a list of solution
     */
    public HeuristicPopulation() {
        super();
    }

    /**
     * Create a list of size solution
     *
     * @param size
     */
    public HeuristicPopulation(int size) {
        super(size);
    }

    /**
     * Get the solution with the maxmimum evaluation
     *
     * @return
     */
    public HeuristicSolution getMax() {
        double bestCost = 0;
        HeuristicSolution bestSol = null;

        for (int i = 0; i < this.size(); i++) {
            HeuristicSolution sol = this.get(i);
            if (sol.evaluate() > bestCost) {
                bestCost = sol.evaluate();
                bestSol = sol;
            }
        }
        return bestSol;
    }

    /**
     * Get the solution with the minimum evaluation
     *
     * @return
     */
    public HeuristicSolution getMin() {
        double bestCost = this.get(0).evaluate();
        HeuristicSolution bestSol = this.get(0);

        for (int i = 0; i < this.size(); i++) {
            HeuristicSolution sol = this.get(i);
            if (sol.evaluate() < bestCost) {
                bestCost = sol.evaluate();
                bestSol = sol;
            }
        }
        return bestSol;
    }

    /**
     * Display the solution. Use for debug
     */
    public void display() {
        for (int i = 0; i < this.size(); i++) {
            System.out.println("Solution " + i  + " : ");
            this.get(i).display();
        }
    }

}
