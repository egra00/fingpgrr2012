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

import be.ac.ulg.montefiore.run.totem.scenario.model.*;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.MethodType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Scenario;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/*
 * Changes:
 * --------
 * 19-Dec-2006 : LoadTrafficMatrixEvent useless, add method name parameter to constructor (GMO)
 */

/**
 * Generates a full mesh of primary and backup lsps
 *
 * <p>Creation date: 01-Feb.-2005
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 */
public class PrimaryBackupFullMesh implements GenerateScenario{

    private Scenario scenario;

    public Scenario generateScenario() {
        return scenario;
    }

    public PrimaryBackupFullMesh(String topologyName, String trafficMatrixFileName, String methodName, HashMap startAlgoParams, HashMap algoParams, String backupType) throws Exception{


        ObjectFactory factory = new ObjectFactory();

        scenario = factory.createScenario();
        List events = scenario.getEvent();

        LoadDomain loadTopology = new LoadDomain(topologyName,true,false,true);
        events.add(loadTopology);

        //LoadTrafficMatrix loadDemandMatrix = new LoadTrafficMatrix(0,trafficMatrixFileName);
        //events.add(loadDemandMatrix);


        InterDomainManager.getInstance().loadDomain(topologyName,true,true);
        Domain domain = InterDomainManager.getInstance().getDefaultDomain();

        StartAlgo startAlgo = new StartAlgo(methodName,startAlgoParams, domain.getASID());
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

                LspScenario scenarioElem = new LspScenario(origin.getId(),destination.getId(),demandValue);

                lspsList.add(scenarioElem);
            }
        }

        (new ScenarioOrder()).Order(GenerateScenario.DECREASING_ORDER, events, lspsList, methodName, algoParams);

        ShowLinkReservation linkInfo = new ShowLinkReservation(domain.getASID(), false);
        events.add(linkInfo);

        //just creating backup for all the primary lsps
        for (int i=0; i < lspsList.size(); i++){

            //don't want to specify the lspid and the ProtectionType
            LSPDetourCreation lspBackupCreation = new  LSPDetourCreation(Integer.toString(i) , methodName, algoParams);

            if (backupType.equals("LOCAL"))lspBackupCreation.setMethodType(MethodType.LOCAL);
            else if (backupType.equals("GLOBAL"))lspBackupCreation.setMethodType(MethodType.GLOBAL);
            events.add(lspBackupCreation);
        }

        linkInfo = new ShowLinkReservation(domain.getASID(), false);
        events.add(linkInfo);
    }
}
