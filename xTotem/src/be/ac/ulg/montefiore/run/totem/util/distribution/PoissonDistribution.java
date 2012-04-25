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
 * - 14-Jul.-05: fix javadoc (JLE).
 */

/**
 * This class implements a Poisson distribution using an acceptance-rejection
 * technique if the mean is lower than THRESHOLD. If the mean is >= than
 * THRESHOLD, we use an approximation by a Normal distribution (because the
 * acceptance-rejection technique becomes costly).
 *
 * <p>See "<i>Discrete-Event System Simulation</i>", Second Edition, Jerry BANKS,
 * John S. Carson, II, Barry L.Nelson, pp 345-348, 1999, Prentice Hall.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class PoissonDistribution implements Distribution {
    
    private Distribution dis;
    public static final int THRESHOLD = 15;
    
    /**
     * Initialises a newly created <code>PoissonDistribution</code> object.
     * @param mean The mean of the Poisson distribution.
     * @throws IllegalArgumentException If the mean is <= 0.
     */
    public PoissonDistribution(double mean) {
        if(mean <= 0)
            throw new IllegalArgumentException("The mean must be > 0.");
        if(mean < THRESHOLD)
            dis = new AcceptanceRejection(mean);
        else
            dis = new NormalApproximation(mean);
    }
    
    /**
     * Returns a <code>double</code> value according to the Poisson
     * distribution.
     */
    public double generate() {
        return dis.generate();
    }
    
    private class AcceptanceRejection implements Distribution {
        
        private double emean;
        private Random rand;
        
        public AcceptanceRejection(double mean) {
            rand = new Random();
            emean = Math.exp(-mean);
        }
        
        public double generate() {
            long n = 0;
            double p = 1.0d;
            while(true) {
                p *= rand.nextDouble();
                if(p >= emean) {
                    ++n;
                }
                else {
                    return n;
                }
            }
        }
    }
    
    
    private class NormalApproximation implements Distribution {
        
        private NormalDistribution normalDis;
        private double mean, sqrtMean;
        
        public NormalApproximation(double mean) {
            // We want a gaussian distribution to make the approximation.
            normalDis = new NormalDistribution(0, 1);
            this.mean = mean - 0.5; // mean-0.5 is needed but not mean...
            sqrtMean = Math.sqrt(mean);
        }
        
        public double generate() {
            double d = mean + sqrtMean * normalDis.generate();
            if(d < 0)
                return 0;
            else return Math.ceil(d);
        }
    }
}
