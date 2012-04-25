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
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * <p>Creation date: 14 sept. 2007
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */

public abstract class BGPAwareNeighborFunction {
    int nbUnidirectionnalLinks = 0;
    Map<String,String> correspondingLink = new HashMap<String,String>();
    ArrayList<String> listOfLinks = new ArrayList<String>();

    public void init(Domain domain) throws Exception {
        for (Link l : domain.getUpLinks()) {
            if (l.getLinkType() == Link.Type.INTRA) {
                nbUnidirectionnalLinks++;
                listOfLinks.add(l.getId());
                if (correspondingLink.get(l.getId()) == null) {
                    for (Link l2 : domain.getUpLinks()) {
                        if (l2.getLinkType() == Link.Type.INTRA) {
                            if ((l.getSrcNode().getId().compareTo(l2.getDstNode().getId()) == 0) && (l.getDstNode().getId().compareTo(l2.getSrcNode().getId()) == 0)) {
                                // l and l2 are opposite direction links
                                correspondingLink.put(l.getId(), l2.getId());
                                correspondingLink.put(l2.getId(), l.getId());
                            }
                        }
                    }
                }
            }
        }
    }

    public abstract String getName();

    public abstract void proposeMove(LinksWeightSolution currentLinkWeights, LinksWeightSolution currentLinkWeightsModified);
}
