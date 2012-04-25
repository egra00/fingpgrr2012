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
package be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SimulatedAnnealingConcepts.NeighborFunction;

import be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SimulatedAnnealingConcepts.LinksWeightSolution;
import be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SaBgpAwareLwo;

/**
 * <p>Creation date: 11 sept. 2007
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */

public class OneRandomWeightChange extends BGPAwareNeighborFunction {
    public String getName() {
        return "OneRandomWeightChange";
    }

    public void proposeMove(LinksWeightSolution currentLinkWeights, LinksWeightSolution currentLinkWeightsModified) {
        if (nbUnidirectionnalLinks != 0) {
            // OK the init method has been called.
            for (String linkId : listOfLinks) {
                currentLinkWeightsModified.setMetric(linkId, currentLinkWeights.getMetric(linkId));
            }

            int choosedLinkInt = (int) Math.round(nbUnidirectionnalLinks * Math.random() - 0.5);
            String choosedLinkId = listOfLinks.get(choosedLinkInt);

            float newLinkWeight = (float) Math.round(SaBgpAwareLwo.maxLinkWeight * Math.random() - 0.5);

//            System.out.println("################################################");
//            System.out.println("Modifying link " + choosedLinkId + ", new metric is " + newLinkWeight);
//            System.out.println("################################################");

            currentLinkWeightsModified.setMetric(choosedLinkId, newLinkWeight);
            currentLinkWeightsModified.setMetric(correspondingLink.get(choosedLinkId), newLinkWeight);
        }
    }
}
