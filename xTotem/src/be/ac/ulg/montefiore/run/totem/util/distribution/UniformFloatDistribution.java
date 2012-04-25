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
 * This class implements a uniform float distribution.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class UniformFloatDistribution implements Distribution {
    
    private double offset, range;
    private Random rand;
    
    /**
     Initialises a newly created <code>UniformFloatDistribution</code>
     object.
     @param lower The lowest value this object must return (exclusive).
     @param upper The highest value this object must return (exclusive).
     @throws IllegalArgumentException If <code>lower</code> is >= than
     <code>upper</code>.
     */
    public UniformFloatDistribution(double lower, double upper) {
        rand = new Random();
        if(lower >= upper)
            throw new IllegalArgumentException("lower must be < than upper.");
        range = upper - lower;
        offset = lower;
    }
    
    /**
     Returns a <code>double</code> value according to the uniform float
     distribution.
     */
    public double generate() {
        double d;
        // We check that d is not equal to 0.
        // We do that to exclude lower (to have a symmetric interval).
        do {
            d = rand.nextDouble();
        } while(d == 0);
        return offset + d * range;
    }
}
