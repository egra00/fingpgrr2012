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
package be.ac.ulg.montefiore.run.totem.scenario.model;

import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.TemplateImpl;
import be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO.GenericBGPAwareLwo.SaBgpAwareLwo;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;

/*
 * Changes:
 * --------
 *
 *
 */

/**
 * Template event.
 *
 * <p>Creation date: 11-dec.-06
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class Template extends TemplateImpl implements Event {
    
    public EventResult action() throws EventExecutionException {
        // TODO Auto-generated method stub
        System.out.println("Template code here !");

        test();

        EventResult er = new EventResult();
        return er;
    }

    private void test() {
        try {
            System.out.println("Test code executed.");
            SaBgpAwareLwo algo = new SaBgpAwareLwo();
            int ASID = InterDomainManager.getInstance().getDefaultDomain().getASID();
            int[] TMIDs =  new int[1];
            TMIDs[0] = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(ASID).getTmId();
            algo.calculateWeights(ASID, TMIDs);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
