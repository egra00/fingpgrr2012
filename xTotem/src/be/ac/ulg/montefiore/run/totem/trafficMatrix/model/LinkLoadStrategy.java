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
package be.ac.ulg.montefiore.run.totem.trafficMatrix.model;

import be.ac.ulg.montefiore.run.totem.repository.model.SPF;

/*
 * Changes:
 * --------
 * - 14-Jun-2007: now extends TmLoadComputer (GMO)
 * - 27-Feb-2008: now extends LinkLoadComputer (GMO)
 */

/**
 * Define the behavior of a link load computation stategy. Different strategies are shortest path first,
 * basic IGP shortcut, IGP shortcut, etc. Theses strategies of link load computation are implemented using a
 * strategy pattern.
 * <br>
 * Three properties can be used to control the computation :
 * <ul>
 *	<li>ECMP : true if equal-cost multi-path is activated and false otherwise. By default : false. </li>
 *  <li>SPF : specifies the shortest path first algorithm to use. By default dijkstra SPF implemented by the
 * class CSPF.</li>
 *  <li>tm : The traffic matrix to use for the computation</li>
 * </ul>
 *
 * <p>Creation date: 28-Jun-2005 17:28:54
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface LinkLoadStrategy extends LinkLoadComputer {

    /**
     * Sets the traffic matrix to use with computation
     * @param tm
     */
    public void setTm(TrafficMatrix tm);

    /**
     * Get the ECMP (Equal-Cost Multi-Path) property.
     *
     * By default : false
     *
     * @return true if equal-cost multi-path is activated and false otherwise
     */
    public boolean isECMP();

    /**
     * Set the ECMP (Equal-Cost Multi-Path) property.
     *
     * @param ECMP true if equal-cost multi-path must be activated and false otherwise
     */
    public void setECMP(boolean ECMP);

    /**
     * Get the SPF (Shortest Path First algorithm) property. The SPF is the routing algorithm used to compute the link load.
     *
     * By default : dijkstra SPF implemented by the class CSPF.
     *
     * @return the SPF
     */
    public SPF getSPFAlgo();

    /**
     * Set the SPF (Shortest Path First algorithm) property. The SPF is the routing algorithm used to compute the link load.
     *
     * @param spf the SPF to use in the link load computation
     */
    public void setSPFAlgo(SPF spf);

    public boolean equals(Object o);

    public int hashCode();

    public String toString();

}
