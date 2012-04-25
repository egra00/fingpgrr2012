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
 * This class implements a logistic distribution. The pdf of the distribution is
 * <tt>1/sigma * exp((x-mu)/sigma) / (1+exp((x-mu)/sigma))^2</tt>.
 *
 * <p><b>Implementation:</b> inversion technique.
 *
 * <p>Creation date: 14-juil.-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class LogisticDistribution implements Distribution {

    private double mu, sigma;
    private Random rand;
    
    /**
     * Initialises a newly created logistic distribution.
     * @param mu The location parameter of the logistic distribution.
     * @param sigma The scale parameter of the logistic distribution.
     */
    public LogisticDistribution(double mu, double sigma) {
        rand = new Random();
        this.mu = mu;
        this.sigma = sigma;
    }

    /**
     * Returns a <code>double</code> value according to the logistic distribution.
     */
    public double generate() {
        return mu - sigma*Math.log(1/rand.nextDouble()-1);
    }

}
