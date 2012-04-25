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
package be.ac.ulg.montefiore.run.totem.util.distribution;

import java.util.Random;

/*
 * Changes:
 * --------
 *
 * - 14-Jul.-05: check stdDeviation before creating rand (JLE).
 */

/**
 * This class implements a Normal distribution.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class NormalDistribution implements Distribution {
    
    private double mean, stdDeviation;
    private Random rand;
    
    /**
     * Initialises a newly created normal distribution with mean equals to 0 and
     * standard deviation equals to 1.
     */
    public NormalDistribution() {
        this(0,1);
    }
    
    /**
     Initialises a newly created <code>NormalDistribution</code> object.
     @param mean The mean of the distribution.
     @param stdDeviation The standard deviation of the distribution.
     @throws IllegalArgumentException If <code>stdDeviation</code> is <= 0.
     */
    public NormalDistribution(double mean, double stdDeviation) {
        if(stdDeviation <= 0)
            throw new IllegalArgumentException("stdDeviation must be > 0.");
        rand = new Random();
        this.mean = mean;
        this.stdDeviation = stdDeviation;
    }
    
    /**
     Returns a <code>double</code> value according to the Normal
     distribution.
     */
    public double generate() {
        return rand.nextGaussian()*stdDeviation + mean;
    }
}
