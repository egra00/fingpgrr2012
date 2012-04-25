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
 * This class implements an inverse normal distribution. The pdf of the
 * distribution is
 * <tt>sqrt(lambda/(2*pi*x^3)) * exp(-lambda/(2*mu^2*x) * (x-mu)^2)</tt>.
 *
 * <p>This distribution is also known as the inverse gaussian distribution or the
 * Wald distribution.
 * 
 * <p>The implementation was taken from
 * <a href="http://www.cqs.washington.edu/papers/zabel/chp4.doc7.html#488046">Zabel's dissertation</a>.
 * In this text, the minus sign in the square root of eq. (4.53) must be replaced
 * by a plus sign.
 *
 * <p>Creation date: 14-juil.-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class InverseNormalDistribution implements Distribution {

    private NormalDistribution dis;
    private Random rand;
    private double mu, lambda;
    
    /**
     * Initialises a newly created inverse normal distribution.
     * @param mu The location parameter of the inverse normal distribution.
     * @param lambda The scale parameter of the inverse normal distribution.
     */
    public InverseNormalDistribution(double mu, double lambda) {
        dis = new NormalDistribution();
        rand = new Random();
        this.mu = mu;
        this.lambda = lambda;
    }

    /**
     * Returns a <code>double</code> value according to the inverse normal
     * distribution.
     */
    public double generate() {
        double n = dis.generate();
        double v = n*n;
        double w = mu*v;
        double c = mu/(2.0*lambda);

        double x = mu + c*(w - Math.sqrt(w*(4.0*lambda + w)));
        double p = mu/(mu + x);

        if (p > rand.nextDouble()) {
            return x;
        } else {
            return(mu*mu/x);
        }
    }

}
