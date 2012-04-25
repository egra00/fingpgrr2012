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
package be.ac.ulg.montefiore.run.totem.domain.model.impl;

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;

import java.util.List;

/*
 * Changes:
 * --------
 *
 */

/**
 * Compute statistcs on a domain
 *
 * <p>Creation date: 19-Jan-2005 18:40:41
 *
 * @author  Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class DomainStatisticsImpl implements DomainStatistics {

    private DomainImpl domainImpl;

    private Domain domain;

    public DomainStatisticsImpl(Domain domain) {
        this.domain = domain;
        domainImpl = (DomainImpl) domain;
    }

    /**
     * Returns the Balance of the network (resource-oriented network performance indicator).
     * The balance is defined by 1 minus the maximum link load in the network.
     * As we do not dispose link load in our domain XML format, we replace the load by the bandwidth reservation.
     */
    public float getBalance() {
        // Retrieve all links of the domain
        List<Link> allLinks = domain.getAllLinks();

        float maxUtilisation = 0;
        float currentUtilisation;

        // For each link of the domain
        for (int i=0; i < allLinks.size(); i++) {
            Link link = allLinks.get(i);

            // retrieve the utilisation of the link
            currentUtilisation = getUtilisation(link);

            // record the max value for the utilisation
            if (currentUtilisation > maxUtilisation) {
                maxUtilisation = currentUtilisation;
            }
        }

        return (1 - maxUtilisation);
    }

    /**
     * Returns the Utilization of the domain (resource-oriented network performance indicator).
     * The utilization is defined by the sum of all link loads.
     * As we do not dispose link load in our domain XML format, we replace the load by the bandwidth reservation.
     */
    public float getUtilization() {
        // Retrieve all links of the domain
        List<Link> allLinks = domain.getAllLinks();

        float totalUtilisation = 0;
        float currentUtilisation;

        // For each link of the domain
        for (int i=0; i < allLinks.size(); i++) {
            Link link = allLinks.get(i);

            // retrieve the utilisation of the link
            currentUtilisation = getUtilisation(link);

            // Sum the utilisation of all links
            totalUtilisation += currentUtilisation;
        }

        return totalUtilisation;
    }

    /**
     * Returns the Standard Deviation over the utilisation of all the links of this domain
     */
    public float getLinkUtilisationStandardDeviation() {
        // Retrieve all links of the domain
        List<Link> allLinks = domain.getAllLinks();

        int nbLinks = allLinks.size();
        float meanUtilisation = getUtilization() / nbLinks;


        float sumOfDeviations = 0;
        float currentUtilisation;

        for (int i=0; i < allLinks.size(); i++) {
            Link link = allLinks.get(i);

            // retrieve the utilisation of the link
            currentUtilisation = getUtilisation(link);

            sumOfDeviations += ((currentUtilisation - meanUtilisation) * (currentUtilisation - meanUtilisation));
        }

        return (float) Math.sqrt(sumOfDeviations / nbLinks);
    }

    /**
     * Returns the Fairness of the network (traffic-oriented network performance indicator).
     * The fairness is defined as the minimum share in the network.
     * (for the share definition, see below)
     */
    public float getFairness() {

        // Retrieve all the LSPs of the domain
        List<Lsp> allLsps = domain.getAllLsps();
        float fairness = Float.MAX_VALUE;

        // for each LSP of the domain
        for (int i=0; i < allLsps.size(); i++) {
            float share = getShare(allLsps.get(i));

            // record the minimum share of the domain
            if (share < fairness) {
                fairness = share;
            }
        }

        return fairness;
    }

    /**
     * Returns the Throughput of the network (traffic-oriented network performance indicator).
     * Throughput is defined as the sum (over all trunks) of the product of share and demand.
     * (for the share definition, see below)
     */
    public float getThroughput() {

        // Retrieve all the LSPs of the domain
        List<Lsp> allLsps = domain.getAllLsps();
        float throughput = 0;

        // for each LSP of the domain
        for (int i=0; i < allLsps.size(); i++) {
            Lsp lsp = allLsps.get(i);
            float share = getShare(lsp);
            float demand = lsp.getReservation();
            if ((share != Float.NaN) && (share != Float.POSITIVE_INFINITY) && (share != Float.NEGATIVE_INFINITY) &&
                    (demand != 0) && (demand != Float.NaN)) {
                throughput += (share * demand);
                //System.out.println("lsp " + lsp.getId() + " throughput = " + throughput + " share = " + share + " demand = " + demand);
            }
        }

        return throughput;
    }

    /**
     * Returns the pathcost of the lsp given as argument.
     * The pathcost is the sum of TE metric of the links on which the LSP pass on.
     */
    public float getPathcost(Lsp lsp) {
        float pathcost = 0;
        Path path = lsp.getLspPath();
        List<Link> links = path.getLinkPath();

        for (int i = 0; i < links.size(); i++) {
            pathcost += links.get(i).getTEMetric();
        }
        return pathcost;
    }

    /**
     * The Share is defined as the ratio between the available bandwidth and the demand.
     * This method returns the share of the lsp given as argument.
     */
    private float getShare(Lsp lsp) {
        float demand = lsp.getReservation();
        float minAvailableBw = Float.MAX_VALUE;

        try {
            Path path = lsp.getLspPath();
            List<Link> links = path.getLinkPath();

            for (int i=0; i < links.size(); i++) {
                Link currentLink = links.get(i);

                float totalResv = currentLink.getReservedBandwidth();
                float availableBandwidth = (demand / totalResv) * domainImpl.getLinkIgp(currentLink.getId()).getStatic().getMrbw();

                if (availableBandwidth < minAvailableBw) {
                    minAvailableBw = availableBandwidth;
                }
            }

        } catch (LinkNotFoundException e) {
            e.printStackTrace();
        }

        float share = ( minAvailableBw / demand );
        return share;
    }

    private float getUtilisation(Link link) {
        return (link.getReservedBandwidth() / link.getBandwidth());
    }
}
