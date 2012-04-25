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
 */

/**
 * This class implements a Bimodal distribution by means of a mixture of two
 * Normal distributions.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class BimodalDistribution implements Distribution {
    
    private double coinFlip;
    private NormalDistribution dis1, dis2;
    private Random rand;
    
    /**
     Initialises a newly created <code>BimodalDistribution</code> object.
     @param mean1 The mean of the first Normal distribution.
     @param stdDeviation1 The standard deviation of the first Normal
     distribution.
     @param mean2 The mean of the second Normal distribution.
     @param stdDeviation2 The standard deviation of the second Normal
     distribution.
     @param coinFlip The probability of using the first Normal distribution.
     (1 - <code>coinFlip</code>) is the probability of using
     the second distribution.
     @throws IllegalArgumentException If stdDeviation1 is <= 0, stdDeviation2
     is <= 0 or coinFlip is not between 0
     and 1 (inclusive).
     */
    public BimodalDistribution(double mean1, double stdDeviation1,
            double mean2, double stdDeviation2,
            double coinFlip) {
        rand = new Random();
        if(stdDeviation1 <= 0 || stdDeviation2 <= 0)
            throw new IllegalArgumentException("stdDeviation1 and "+
            "stdDeviation2 must be > 0.");
        if(coinFlip < 0 || coinFlip > 1)
            throw new IllegalArgumentException("coinFlip must be >= 0 and "+
            "<= 1.");
        dis1 = new NormalDistribution(mean1, stdDeviation1);
        dis2 = new NormalDistribution(mean2, stdDeviation2);
        this.coinFlip = coinFlip;
    }
    
    /**
     Returns a <code>double</code> value according to the Bimodal
     distribution.
     */
    public double generate() {
        double d = rand.nextDouble();
        if(d <= coinFlip)
            return dis1.generate();
        else
            return dis2.generate();
    }
}
