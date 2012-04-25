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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.scenario.model.*;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Scenario;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ChartFormatType;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;

/*
 * Changes:
 * --------
 * - 24-Jan-2007: use Domain#getReverseLink(.) method (GMO)
 * - 09-Aug-2007: allow chart creation (GMO)
 * - 09-Aug-2007: use reroute method instead of network controller (GMO)
 * - 25-Sep-2007: remove use of reroute method. The failed lsps are not rerouted, they are simply not used during the failure (GMO)
 */

/**
 * Worst case link failure scenario generator (primary LSPs version (CSPF,
 * DAMOTE, etc.)).
 *
 * <p>Creation date: 25-mars-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class LSPWorstCaseLinkFailure implements GenerateScenario {

    private static final Logger logger = Logger.getLogger(LSPWorstCaseLinkFailure.class);
    
    private Scenario scenario;


    public LSPWorstCaseLinkFailure(String topologyName, String trafficMatrixFileName, int order, String methodName, HashMap startAlgoParams, HashMap algoParams, String chartFilename) throws Exception {
        this(topologyName, trafficMatrixFileName, order, methodName, startAlgoParams, algoParams, true, chartFilename);
    }

    public LSPWorstCaseLinkFailure(String topologyName, String trafficMatrixFileName, int order, String methodName, HashMap startAlgoParams, HashMap algoParams) throws Exception {
        this(topologyName, trafficMatrixFileName, order, methodName, startAlgoParams, algoParams, false, null);
    }

    private LSPWorstCaseLinkFailure(String topologyName, String trafficMatrixFileName, int order, String methodName, HashMap startAlgoParams, HashMap algoParams, boolean createChart, String chartFilename) throws Exception {
        ObjectFactory factory = new ObjectFactory();
        scenario = factory.createScenario();
        List<Event> events = scenario.getEvent();
        
        // Load the domain only once.
        LoadDomain loadDomain = new LoadDomain(topologyName,true, true);
        events.add(loadDomain);

        InterDomainManager.getInstance().loadDomain(topologyName,true,true);
        Domain domain = InterDomainManager.getInstance().getDefaultDomain();

        // Determine a "final" order for the full mesh
        TrafficMatrixManager.getInstance().loadTrafficMatrix(trafficMatrixFileName,0,true);
        TrafficMatrix trafficMatrix = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix();

        List<Node> nodes = domain.getUpNodes();
        ArrayList<LspScenario> lspsList = new ArrayList<LspScenario>();
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
        List<LSPCreation> eventsFullMesh = new ArrayList<LSPCreation>();
        // store the order in eventsFullMesh
        (new ScenarioOrder()).Order(order, eventsFullMesh, lspsList, methodName, algoParams);
        DeleteAllLSP deleteAllLSP = new DeleteAllLSP(domain.getASID());
        
        events.add(new StartAlgo(methodName,startAlgoParams,domain.getASID()));

        if (createChart) {
            HashMap<String, String> chartCreationParams = new HashMap<String, String>();
            chartCreationParams.put("asId", Integer.toString(domain.getASID()));
            chartCreationParams.put("absoluteLoad", "false");
            chartCreationParams.put("statistic", "Max");
            ChartCreation chartCreation = new ChartCreation("WCA", "LinksReservedBWDataCollector", chartCreationParams);
            events.add(chartCreation);
        }

        HashMap<String, String> controllerParams = new HashMap<String, String>();
        controllerParams.put("routingAlgo", methodName);
        controllerParams.put("remove", "true");

        ShowLinkReservation linkInfo = new ShowLinkReservation(domain.getASID(), false);
        events.addAll(eventsFullMesh);
        events.add(linkInfo);

        HashMap<String, String> chartAddSeriesParams = null;
        if (createChart) {
            chartAddSeriesParams = new HashMap<String, String>();
            chartAddSeriesParams.put("routingAlgo", methodName);
            ChartAddSeries chartAddSeries = new ChartAddSeries("WCA", "No Failure", chartAddSeriesParams);
            events.add(chartAddSeries);
        }


        HashMap<String, Link> linksVisited = new HashMap<String, Link>();
        for(Iterator<Link> it = domain.getUpLinks().iterator(); it.hasNext();) {
            Link link1 = it.next();
            if(linksVisited.containsKey(link1.getId())) {
                continue;
            }
            Link link2 = domain.getReverseLink(link1);
            linksVisited.put(link1.getId(), link1);
            events.add(new LinkDown(domain.getASID(), link1.getId()));
            if (link2 != null) {
                linksVisited.put(link2.getId(), link2);
                events.add(new LinkDown(domain.getASID(), link2.getId()));
            }
            events.add(new Echo("Failure of link between "+link1.getSrcNode().getId()+" and "+link1.getDstNode().getId()));
            events.add(linkInfo);
            if (createChart) {
                ChartAddSeries chartAddSeries = new ChartAddSeries("WCA", link1.getSrcNode().getId()+"-"+link1.getDstNode().getId(), chartAddSeriesParams);
                events.add(chartAddSeries);
            }
            events.add(deleteAllLSP);
            events.add(new LinkUp(domain.getASID(), link1.getId()));
            if (link2 != null)
                events.add(new LinkUp(domain.getASID(), link2.getId()));
            events.addAll(eventsFullMesh);
        }

        if(createChart) {
            ChartFormatType chartFormatType;
            if(chartFilename.endsWith(".png")) {
                chartFormatType = ChartFormatType.PNG;
            } else if(chartFilename.endsWith(".jpg")) {
                chartFormatType = ChartFormatType.JPG;
            } else if(chartFilename.endsWith(".eps")) {
                chartFormatType = ChartFormatType.EPS;
            } else {
                throw new IllegalArgumentException(chartFilename+" does not end with \".png\", \".jpg\" or \".eps\".");
            }
            HashMap<String, String> chartSaveParams = new HashMap<String, String>();
            chartSaveParams.put("asId", Integer.toString(domain.getASID()));
            chartSaveParams.put("allLinks", "false");
            ChartSave chartSave = new ChartSave("WCA", chartFilename, chartFormatType, "Worst Case Link Failure ("+methodName+")", "Links' failures", "Max Reserved Bandwidth", 800, 600, "LoadChartPlotter", chartSaveParams);
            events.add(chartSave);

            ChartDeletion chartDeletion = new ChartDeletion("WCA");
            events.add(chartDeletion);
        }

    }

    public Scenario generateScenario() {
        return scenario;
    }
}
