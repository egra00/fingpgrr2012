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

/*
 * Changes:
 * --------
 *
 */

/**
 * This class generates traffic using the gravity model.
 *
 * <p>Creation date: 2004
 * 
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class GravityTrafficModel implements TrafficModel {
    
    private AttractionFactor attraction;
    private RepulsionFactor repulsion;
    private FrictionFactor friction;
    private double scalingConstant;
    
    /**
     * Initialises a newly created <code>GravityTrafficModel</code> object.
     * @param attraction The attraction factor.
     * @param repulsion The repulsion factor.
     * @param friction The friction factor.
     * @param scalingConstant The scaling constant.
     */
    public GravityTrafficModel(AttractionFactor attraction,
            RepulsionFactor repulsion,
            FrictionFactor friction,
            double scalingConstant) {
        this.attraction = attraction;
        this.repulsion = repulsion;
        this.friction = friction;
        this.scalingConstant = scalingConstant;
    }
    
    /**
     * Generates traffic using the gravity model.
     * All the parameters must be different from <code>null</code>.
     */
    public double generate(Node origin, Node destination) {
        if(origin == null || destination == null)
            throw new NullPointerException("origin, destination and/or network"
                    +" are equal to null !");
        return ((attraction.generate(destination)
                * repulsion.generate(origin))
                / friction.generate(origin, destination))
                * scalingConstant;
    }
}
