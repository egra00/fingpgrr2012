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

package be.ac.ucl.poms.repository.model;

import be.ac.ulg.montefiore.run.totem.repository.model.TotemAction;
import be.ac.ulg.montefiore.run.totem.domain.model.DomainConvertor;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;

import java.util.List;

/*
 * Changes:
 * --------
 * 24-Apr.-2006 : Add getWeights method (GMO).
 */

/**
 * Action returned by IGP WO, updates all IGP weights accordingly
 * Implements the actions for IGP-WO
 * 
 * <p>Creation date: 1-Jan-2005
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 * 
 *
*/

public class UpdateIGPWeightsAction implements TotemAction {

    protected double[] IGPWeights;
    Domain domain = null;

    public UpdateIGPWeightsAction(Domain domain, double[] IGPWeights) {

        this.domain = domain;
        this.IGPWeights = new double[IGPWeights.length];
        for (int i=0; i<IGPWeights.length; i++){
            this.IGPWeights[i]=IGPWeights[i];

        }
    }

    public double[] getWeights() {
        return IGPWeights;
    }

    /**
     *
     */
    public void execute() {

        List<Link> linksList = domain.getUpLinks();
        DomainConvertor convertor = domain.getConvertor();

        for (int i=0; i<linksList.size(); i++){
            String linkId = linksList.get(i).getId();
            int intlinkId = 0;
            try{
                intlinkId = convertor.getLinkId(linkId);
            }
            catch(LinkNotFoundException e){
                e.printStackTrace();
            }
            double weightvalue = IGPWeights[intlinkId];
     
            linksList.get(i).setTEMetric((float)weightvalue);

        }
    }

}
