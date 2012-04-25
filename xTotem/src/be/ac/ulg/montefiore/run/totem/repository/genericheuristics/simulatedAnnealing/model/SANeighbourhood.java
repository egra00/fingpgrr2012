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
package be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model;

/*
 * Changes:
 * --------
 *
 */

/**
 * Define a neighbourhood of a solution
 *
 * <p>Creation date: 16-Nov-2004 14:42:40
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface SANeighbourhood {

    /**
     * The name of the neighbourhood
     *
     * @return the name
     */

    public String toString();

    /**
     * Generate a neighbour of the solution given in argument and modify the given solution.
     *
     * @param solution
     */
    public void computeNeighbour(SASolution solution);

    /**
     * Return to the previous solution.
     * Be carreful to call this method with the same solution as the past call of the computeNeighbour method
     *
     * @param solution
     */
    public void returnToPreviousSolution(SASolution solution);

    /**
     * Return the size of the neighbourhood
     *
     * @return
     */
    public int getNbNeighbour();

    public int getNbUsed();

    public void resetNbUsed();
    
}
