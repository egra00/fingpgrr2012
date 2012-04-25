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

/*
 * Changes:
 * --------
 *
 */

/**
 * This class implements a lognormal distribution. A random variable <i>X</i> has
 * a lognormal distribution if the random variable <i>ln X</i> has a normal
 * distribution.  
 *
 * <p>Creation date: 14-juil.-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class LogNormalDistribution implements Distribution {

    private NormalDistribution dis;
    
    /**
     * Initialises a newly created lognormal distribution.
     * @param mu The scale parameter of the lognormal distribution.
     * @param sigma The shape parameter of the lognormal distribution.
     * @throws IllegalArgumentException If <code>sigma</code> is <= 0.
     */
    public LogNormalDistribution(double mu, double sigma) {
        dis = new NormalDistribution(mu, sigma);
    }

    /**
     * Returns a <code>double</code> value according to the lognormal
     * distribution.
     */
    public double generate() {
        return Math.exp(dis.generate());
    }

}
