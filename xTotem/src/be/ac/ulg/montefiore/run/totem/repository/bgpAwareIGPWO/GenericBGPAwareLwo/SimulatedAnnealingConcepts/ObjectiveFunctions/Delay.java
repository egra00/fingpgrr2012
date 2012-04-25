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
package be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SimulatedAnnealingConcepts.ObjectiveFunctions;

import be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SimulatedAnnealingConcepts.LinkLoads;
import be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SaBgpAwareLwo;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;

import java.util.Collection;
import java.util.Map;

/**
 * <p>Creation date: 11 sept. 2007
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */

public class Delay extends BGPAwareObjectiveFunction {

    public String getName() {
        return "Delay";
    }

    public double getValue(LinkLoads linkLoads, Domain domain, boolean includeInterDomainLinks, boolean avoidHPReroutings) {
        double delayValue = 0;
        Map<String, Double> allLoads = linkLoads.getAllLoads();
        for (Link currentLink : domain.getAllLinks()) {
            if (currentLink.getLinkType() != Link.Type.VIRTUAL) {
                if ((currentLink.getLinkType() != Link.Type.INTER) || includeInterDomainLinks) {
                    double currentLoad = allLoads.get(currentLink.getId());
                    double currentBw = currentLink.getBandwidth();
                    delayValue += this.linkCost(currentLoad, currentBw);
                }
            }
        }
        return delayValue * 1000000;
    }

    private double linkCost(double load, double capa) {
        double cost = 1.f / (capa - load);
        return cost;
    }
}
