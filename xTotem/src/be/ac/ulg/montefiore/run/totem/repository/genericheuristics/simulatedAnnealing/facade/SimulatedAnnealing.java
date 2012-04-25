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
package be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.facade;

import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.*;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.ObjectiveFunction;

/*
 * Changes:
 * --------
 *
 */

/**
 * Implementation of the Simulated Algorithm
 *
 * <p>Creation date: 18 nov. 2004 11:56:14
 *
 * @author  Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class SimulatedAnnealing {

    protected SANeighbourhood neighbourhood;
    SAInitialSolutionGenerator solutionGenerator;
    protected ObjectiveFunction scf;
    protected SAParameter param;
    protected SAReportGenerator reportGenerator;
    public boolean minimise;

    /**
     * This constructor build a simulated annealing instance with the specified parameters.
     *
     * @param neighbourhood : the neighbourhood function
     * @param solutionGenerator : the solution generator
     * @param param : the parameters of the Simulated Annealing (T0, alpha, etc)
     */
    public SimulatedAnnealing(SANeighbourhood neighbourhood, SAInitialSolutionGenerator solutionGenerator, ObjectiveFunction scf, SAParameter param, SAReportGenerator reportGenerator) {
        this.neighbourhood = neighbourhood;
        this.solutionGenerator = solutionGenerator;
        this.scf = scf;
        this.param = param;
        minimise = param.getMinimise();
        this.reportGenerator = reportGenerator;
    }

    public SimulatedAnnealing(SANeighbourhood neighbourhood, SAParameter param, SAReportGenerator reportGenerator) {
        this.neighbourhood = neighbourhood;
        this.solutionGenerator = null;
        this.param = param;
        minimise = param.getMinimise();
        this.reportGenerator = reportGenerator;
    }


    /**
     * This constructor build a simulated annealing instance with the specified parameters.
     *
     * @param neighbourhood : the neighbourhood function
     * @param solutionGenerator : the solution generator
     * @param param : the parameters of the Simulated Annealing (T0, alpha, etc)
     */
    public SimulatedAnnealing(SANeighbourhood neighbourhood, SAInitialSolutionGenerator solutionGenerator, ObjectiveFunction scf, SAParameter param) {
        this.neighbourhood = neighbourhood;
        this.solutionGenerator = solutionGenerator;
        this.param = param;
        this.scf = scf;
        minimise = param.getMinimise();
        this.reportGenerator = null;
    }

    /**
     * This constructor build a simulated annealing instance with the specified parameters.
     *
     * @param neighbourhood : the neighbourhood function
     * @param solutionGenerator : the solution generator
     */
    public SimulatedAnnealing(SANeighbourhood neighbourhood, SAInitialSolutionGenerator solutionGenerator, ObjectiveFunction scf) {
        this.neighbourhood = neighbourhood;
        this.solutionGenerator = solutionGenerator;
        this.scf = scf;
        this.reportGenerator = null;
    }

    /**
     * This constructor build a simulated annealing instance without solution generator.
     * If you use this constructor, you cannot call after the solve() method, you have to provide
     * yourself the initial solution and call the solve(SASolution initialSolution) instead.
     *
     * @param neighbourhood : the neighbourhood function
     * @param param : the parameters of the Simulated Annealing (T0, alpha, etc)
     */
    public SimulatedAnnealing(SANeighbourhood neighbourhood, SAParameter param) {
        this.neighbourhood = neighbourhood;
        this.solutionGenerator = null;
        this.param = param;
        minimise = param.getMinimise();
        this.reportGenerator = null;
    }


    /**
     * This method will return the solution of the simulated Annealing MetaHeuristic.
     * The initial solution is generated with the solutionGenerator.
     *
     * @return the solution of the algorithm (type : SASolution)
     */
    public SASolution solve() throws Exception {
        if (solutionGenerator == null) {
            throw new IllegalArgumentException("You cannot call the solve() method without initial solution generator...\n");
        } else if (scf == null) {
            throw new IllegalArgumentException("You cannot call the solve() method without objective function...\n");
        }

        if (param == null) {
            throw new IllegalArgumentException("You cannot call the solve() method without params ...\n");
        }

        // Generate the initial solution
        SASolution initialSolution = solutionGenerator.generate(scf);
        //System.out.println("Use solve() - init sol : " + initialSolution.evaluate());
        // Solve the problem starting from the initial solution
        SASolution finalSolution = solve(initialSolution);

        // Return the solution
        return finalSolution;
    }

    public SASolution solve(SAParameter param) throws Exception {
         if (solutionGenerator == null) {
            throw new IllegalArgumentException("You cannot call the solve() method without initial solution generator...\n");
        } else if (scf == null) {
            throw new IllegalArgumentException("You cannot call the solve() method without objective function...\n");
        }

        this.param = param;
        minimise = param.getMinimise();

        // Generate the initial solution
        SASolution initialSolution = solutionGenerator.generate(scf);
        //System.out.println("Use solve(param) - init sol : " + initialSolution.evaluate());
        // Solve the problem starting from the initial solution
        SASolution finalSolution = solve(initialSolution);

        // Return the solution
        return finalSolution;
    }

    public float proposeT0(int plateauSize) throws IllegalArgumentException, Exception {
        return proposeT0(0.1f,plateauSize);
    }

    public float proposeT0(float T0, int plateauSize) throws IllegalArgumentException, Exception {
        if (solutionGenerator == null) {
            throw new IllegalArgumentException("You cannot call the proposeT0() method without initial solution generator...\n");
        } else if (scf == null) {
            throw new IllegalArgumentException("You cannot call the proposeT0() method without objective function...\n");
        }


        float T = T0;
        float acceptedMoves = 0f;

        while ( (acceptedMoves < 0.5) | (acceptedMoves > 0.9) ) {

            if (acceptedMoves < 0.5) {
                T *= 2f;
            }
            else {
                T *= (9f/10);
            }

            // Generate the initial solution
            SASolution presentSolution = solutionGenerator.generate(scf);
            double presentF = presentSolution.evaluate();

            double newF;

            int nbAcceptedMoves = 0;

            for (int i=0; i < plateauSize; i++) {
                // Choose a solution randomly in the neighbourhood of present solution (propose a move)
                neighbourhood.computeNeighbour(presentSolution);
                newF = presentSolution.evaluate();

                if (compare (newF, presentF) ) {
                    // Accept the move
                    presentF = newF;
                    if (newF != presentF)
                        nbAcceptedMoves++;
                }
                else {
                    // Toss

                    // Compute the probability
                    double deltaF = newF - presentF;
                    deltaF = (deltaF > 0) ? deltaF : -deltaF;
                    double probability = Math.exp(- (deltaF / T));
                    //System.out.println("delta = " + deltaF + " T = " + T + " proba = " + probability);

                    // draw a number randomly and uniformly distributed in [0,1]
                    double u = Math.random();

                    if (u <= probability) {
                        // Accept the move
                        presentF = newF;
                        nbAcceptedMoves++;
                    } else {
                        neighbourhood.returnToPreviousSolution(presentSolution);
                    }
                }
            }

            acceptedMoves = (((float) nbAcceptedMoves) / plateauSize);
            System.out.println("accepted moves: " + acceptedMoves + " T: " + T);
        }


        return T;
    }

    public int proposeL() {
        return neighbourhood.getNbNeighbour();
    }

    /**
     * This method will return the solution of the simulated Annealing MetaHeuristic.
     * The initial solution is given as argument.
     *
     * @return the solution of the algorithm (type : SASolution)
     */
    public SASolution solve(SASolution initialSolution) throws Exception {
        //System.out.println("Eval fct : " + initialSolution.getObjectiveFunction().getName());
        long time = System.currentTimeMillis();
        SASolution presentSolution = (SASolution) initialSolution.clone();
        //System.out.println("Initial Solution : ");
        //((GAMCOSolution) presentSolution).displayLinkLoad();
        double presentF = presentSolution.evaluate();

        // Initialize the variables.
        SASolution bestSolution = (SASolution) presentSolution.clone();
        //System.out.println("Best solution : " + bestSolution.evaluate());
        double bestF = presentF;
        int k = 1;
        float T = param.getT0();
        int L = param.getL();
        float alpha = param.getAlpha();
        int K2 = param.getK2();
        float epsilon2 = param.getEpsilon2();
        int nbIteration = 0;
        int nbAcceptedMoves = 0;

        //System.out.println("T0: " + T + " L: " + L + " alpha: " + alpha + " epsilon: " + epsilon2 + " K: " + K2);

        neighbourhood.resetNbUsed();
        bestSolution.getObjectiveFunction().resetNbEvaluated();

        // This variable will be used in the main loop.
        double newF;

        // This round robin database will contain the values of the nbAcceptedMoves
        // during the last K2 plateaus.
        int RRD[] = new int[K2];
        int RRDid = 0;
        int RRDTotal = 0;
        boolean RRDFirstFill = true;

        // If there is a report generator, we give to him the initial solution.
        if (reportGenerator != null) {
            reportGenerator.addSolution(presentSolution.evaluate(), bestSolution.evaluate(), T);
        }

        boolean terminate = false;
        while (terminate == false) {

            // Lines added for the Graphical User Interface to be able to close the corresponding thread
            try{
                if (Thread.interrupted()){
                    throw new InterruptedException();
                }
            }
            catch(InterruptedException e){
                return null;
            }

            // Choose a solution randomly in the neighbourhood of present solution (propose a move)
            //System.out.println("Current Solution");
            //((GAMCOSolution) presentSolution).displayLinkLoad();
            neighbourhood.computeNeighbour(presentSolution);
            //((GAMCOSolution) presentSolution).displayLinkLoad();

            newF = presentSolution.evaluate();
            //System.out.println("New solution : " + newF);
            nbIteration++;

            if (compare (newF, presentF) ) {
                // Accept the move
                //System.out.println("Cost of the old solution : " + presentF + " and cost of the new solution " + newF + " (move accepted)");
                presentF = newF;
                nbAcceptedMoves++;

                if ( compare (newF, bestF) ) {
                    // Store the value of the best solution
                    //System.out.println("This new solution is the best known solution.");
                    bestSolution = (SASolution) presentSolution.clone();
                    bestF = newF;
                    //System.out.println("Best solution : " + bestF);                           
                }
            }
            else {
                // Toss

                //System.out.println("Cost of the old solution : " + presentF + " and cost of the new solution " + newF + " (toss)");
                // Compute the probability
                double deltaF = newF - presentF;
                deltaF = (deltaF > 0) ? deltaF : -deltaF;
                //double probability = Math.exp(- (deltaF / T));
                double probability = 1 - (deltaF / T);
                //System.out.println("delta = " + deltaF + " T = " + T + " proba = " + probability);

                // draw a number randomly and uniformly distributed in [0,1]
                double u = Math.random();
                //System.out.println("The random value is : " + u);

                if (u <= probability) {
                    // Accept the move
                    presentF = newF;
                    if (deltaF != 0) {
                        nbAcceptedMoves++;
                    }
                } else {
                    //System.out.println("Reject the solution and come to the previous one");
                    neighbourhood.returnToPreviousSolution(presentSolution);
                }
            }

            // If there is a report generator, we give to him the present solution.
            if (reportGenerator != null) {
                reportGenerator.addSolution(presentF, bestF, T);
            }

            // Evaluate the stopping conditions
            if (nbIteration < L) {
                k++;
            }
            else {
                RRD[RRDid] = nbAcceptedMoves;

                RRDid++;
                if (RRDid == K2) {
                    //System.out.println("Number of accepted moves during the first plateau : " + RRD[0]);
                    RRDid = 0;
                    RRDFirstFill = false;
                }

                // Compute the number of accepted moves during the last K2 temperature plateaus
                RRDTotal = 0;
                for (int i = 0; i < K2; i++) {
                    RRDTotal += RRD[i];
                }
                float reference = 100 * (((float) RRDTotal) / (K2 * L));

                
                // If the percentage of accepted moves during the last K2 plateaus is less than epsilon2
                // AND that we have made at least K2 temperature plateaus.
                if ((reference < epsilon2) & (RRDFirstFill == false) ) {
                    // We terminate the algorithm
                    terminate = true;
                }
                else {
                    // We go on for another plateau
                    T *= alpha;
                    k++;

                    nbIteration = 0;
                    nbAcceptedMoves = 0;
                }
            }

        }
        time = System.currentTimeMillis() - time;
        //System.out.println("SA " + bestSolution.evaluate() + " " + param.toString() + " explores " + neighbourhood.getNbUsed() + " solutions (evaluation takes " + bestSolution.getObjectiveFunction().getTimeToEvaluate() + " ms) in " + time + " ms");
        return bestSolution;
    }


    /**
     * If minimise == true, returns true if a < b and false otherwise.
     * If minimise == false (maximisation problem), returns true if a > b and false otherwise.
     */
    protected boolean compare(double a, double b) {
        boolean c;

        if (minimise) {
            if (a < b) {
                c = true;
            }
            else {
                c = false;
            }
        }
        else {
            if (a > b) {
                c = true;
            }
            else {
                c = false;
            }
        }

        return c;
    }
}
