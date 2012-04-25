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
 * This class implements a uniform integer distribution.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class UniformIntDistribution implements Distribution {
    
    private Random rand;
    private long offset, range;
    
    /**
     Initialises a newly created <code>UniformIntDistribution</code> object.
     @param lower The lowest value this object must return.
     @param upper The highest value this object must return.
     @throws IllegalArgumentException If <code>lower</code> is >= than
     <code>upper</code>.
     */
    public UniformIntDistribution(long lower, long upper) {
        rand = new Random();
        if(lower >= upper)
            throw new IllegalArgumentException("lower must be < than upper.");
        range = upper - lower + 1;
        offset = lower;
    }
    
    /**
     Returns a <code>double</code> value according to the uniform integer
     distribution.
     */
    public double generate() {
        return offset + (long) (rand.nextDouble() * range);
    }
}
