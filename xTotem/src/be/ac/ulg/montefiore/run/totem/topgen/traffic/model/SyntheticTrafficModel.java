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
package be.ac.ulg.montefiore.run.totem.topgen.traffic.model;

import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.util.distribution.Distribution;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class generates traffic using the probability distributions.
 *
 * <p>Creation date: 2004
 * 
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class SyntheticTrafficModel implements TrafficModel {
    
    private Distribution dis;
    
    /**
     * Initialises a newly created <code>SyntheticTrafficModel</code> object.
     * @param distribution The distribution to be used.
     */
    public SyntheticTrafficModel(Distribution distribution) {
        this.dis = distribution;
    }
    
    /**
     * Generates traffic between the two nodes according to the given
     * distribution. The parameters are not taken into account.
     */
    public double generate(Node origin, Node destination) {
        return dis.generate();
    }
}
