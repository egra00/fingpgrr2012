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
import java.util.HashMap;
import java.util.Hashtable;

/**
 * <p>Creation date: 11 sept. 2007
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */

public class Fortz extends BGPAwareObjectiveFunction {

    double constantFactor = 1000;

    private boolean firstGetValuesCall = true;
    private Hashtable<String, Double> firstLoadValues = new Hashtable<String, Double>();

    public String getName() {
        return "Fortz";
    }

    public double getValue(LinkLoads linkLoads, Domain domain, boolean includeInterDomainLinks, boolean avoidHPReroutings) {
        double fortzFunctionValue = 0;
        Map<String, Double> allLoads = linkLoads.getAllLoads();
        for (Link currentLink : domain.getAllLinks()) {
            if (currentLink.getLinkType() != Link.Type.VIRTUAL) {
                double currentLoad = allLoads.get(currentLink.getId());
                if (firstGetValuesCall)
                    firstLoadValues.put(currentLink.getId(), new Double(currentLoad));
                if ((currentLink.getLinkType() != Link.Type.INTER) || (includeInterDomainLinks && !avoidHPReroutings)) {
                    currentLoad = allLoads.get(currentLink.getId());
                    double currentBw = currentLink.getBandwidth();
                    fortzFunctionValue += this.linkCost(currentLoad, currentBw);
                } else if (currentLink.getLinkType() != Link.Type.INTER) {
                    if (includeInterDomainLinks && avoidHPReroutings) {
                       System.out.println("You should not activate interdomain links in the score function and avoid hot potato reroutings !");
                    } else {
                        if (avoidHPReroutings) {
                            currentLoad = allLoads.get(currentLink.getId());
                            double loadDifference;
                            if (firstGetValuesCall)
                                loadDifference = 0;
                            else
                                loadDifference = currentLoad - firstLoadValues.get(currentLink.getId());
                            if (loadDifference < 0)
                                loadDifference = -loadDifference;
                            fortzFunctionValue += constantFactor * loadDifference;
                        }
                    }
                }
            }
        }
        if (firstGetValuesCall)
            firstGetValuesCall = false;
        return fortzFunctionValue;
    }

    private double linkCost(double load, double capa) {
        double cost;
        double utilization = load / capa;
        if (utilization < 1.f/3) {
            cost = load;
        } else if (utilization < 2.f/3) {
            cost = 3 * load - 2.f/3 * capa;
        } else if (utilization < 0.9f) {
            cost = 10 * load - 16.f/3 * capa;
        } else if (utilization < 1) {
            cost = 70 * load - 178.f/3 * capa;
        } else if (utilization < 1.1f) {
            cost = 500 * load - 1468.f/3 * capa;
        } else {
            cost = 5000 * load - 16318.f/3 * capa;
        }
        return cost;
    }
}
