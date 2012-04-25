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
package be.ac.ulg.montefiore.run.totem.domain.model;

/*
 * Changes:
 * --------
 *
 */

/**
 * Compute statistcs on a domain
 *
 * <p>Creation date: 12-Jan-2005 18:56:21
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface DomainStatistics {

    /**
     * Returns the Balance of the network (resource-oriented network performance indicator).
     * The balance is defined by 1 minus the maximum link load in the network.
     * As we do not dispose link load in our domain XML format, we replace the load by the bandwidth reservation.
     */
    public float getBalance();

    /**
     * Returns the Utilization of the domain (resource-oriented network performance indicator).
     * The utilization is defined by the sum of all link loads.
     * As we do not dispose link load in our domain XML format, we replace the load by the bandwidth reservation.
     */
    public float getUtilization();

    /**
     * Returns the Standard Deviation over the utilisation of all the links of this domain
     */
    public float getLinkUtilisationStandardDeviation();

     /**
     * Returns the Fairness of the network (traffic-oriented network performance indicator).
     * The fairness is defined as the minimum share in the network.
     * (for the share definition, see below)
     */
    public float getFairness();

    /**
     * Returns the Throughput of the network (traffic-oriented network performance indicator).
     * Throughput is defined as the sum (over all trunks) of the product of share and demand.
     * (for the share definition, see below)
     */
    public float getThroughput();

    /**
     * Returns the pathcost of the lsp given as argument.
     * The pathcost is the sum of TE metric of the links on which the LSP pass on.
     */
    public float getPathcost(Lsp lsp);

}
