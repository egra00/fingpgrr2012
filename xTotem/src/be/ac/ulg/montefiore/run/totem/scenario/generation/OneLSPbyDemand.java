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
package be.ac.ulg.montefiore.run.totem.scenario.generation;

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Scenario;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.StartAlgo;
import be.ac.ulg.montefiore.run.totem.scenario.model.LoadDomain;
import be.ac.ulg.montefiore.run.totem.scenario.model.ShowLinkReservation;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;


import java.util.*;

/*
 * Changes:
 * --------
 *
 * - 22-Mar.-2005: delete an useless call to LoadDomain::setDefaultDomain() (JL).
 * - 22-Mar.-2005: the LoadTrafficMatrix is useless in this scenarios generator (JL).
 *
 */

/**
 * Generates a scenario with one lsp by demand.
 *
 * <p>Creation date: 14-Dec.-2004
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 */
public class OneLSPbyDemand implements GenerateScenario {
    private Scenario scenario;

    public OneLSPbyDemand(String topologyName, String trafficMatrixFileName, int order, String methodName, HashMap startAlgoParams, HashMap algoParams) throws Exception{
        ObjectFactory factory = new ObjectFactory();

        scenario = factory.createScenario();
        List events = scenario.getEvent();

        LoadDomain loadTopology = new LoadDomain(topologyName,true, true);
        events.add(loadTopology);

        InterDomainManager.getInstance().loadDomain(topologyName,true,true);
        Domain domain = InterDomainManager.getInstance().getDefaultDomain();

        StartAlgo startAlgo = new StartAlgo(methodName,startAlgoParams,domain.getASID());
        events.add(startAlgo);

        TrafficMatrixManager.getInstance().loadTrafficMatrix(trafficMatrixFileName,0,true);
        TrafficMatrix trafficMatrix = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix();

        List<Node> nodes = domain.getUpNodes();

        ArrayList lspsList = new ArrayList();

        for(Iterator<Node> it1 = nodes.iterator(); it1.hasNext();) {
            Node origin = it1.next();

            for(Iterator<Node> it2 = nodes.iterator(); it2.hasNext();) {
                Node destination = it2.next();
                if(origin == destination)
                    continue;


                double demandValue = trafficMatrix.get(origin.getId(),destination.getId());
                if (demandValue==0) continue;

                LspScenario scenarioElem = new LspScenario(origin.getId(),destination.getId(),demandValue);

                lspsList.add(scenarioElem);
            }
        }

        (new ScenarioOrder()).Order(order, events, lspsList, methodName, algoParams);

        ShowLinkReservation linkInfo = new ShowLinkReservation(domain.getASID(), false);
        events.add(linkInfo);

    }


    //no methods
    public Scenario generateScenario() {
        return scenario;  //To change body of implemented methods use File | Settings | File Templates.
    }


}


