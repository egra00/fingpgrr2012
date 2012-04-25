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

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ChartFormatType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Scenario;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.*;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.DomainConvertor;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.util.NotYetImplementedException;

import java.util.HashMap;
import java.util.List;

/*
 * Changes:
 * --------
 *
 * - 25-Mar-2005: fix javadoc (JLE).
 * - 14-Feb-2006: add chart generation capability (JLE).
 * - 14-Feb-2006: fix javadoc (JLE).
 * - 24-Jan-2007: use Domain#getReverseLink(.) method (GMO)
 */

/**
 * Worst case link failure scenario generator (SPF or MCF version).
 * 
 * <p>Creation date: 17-Feb-2005 10:24:46
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class WorstCaseLinkFailure implements GenerateScenario {

    private Scenario scenario;
   
    public WorstCaseLinkFailure(String topologyName, String trafficMatrixFileName, String methodName, HashMap startAlgoParams, boolean createChart, String chartFileName) throws Exception {
        if(createChart && methodName.equals("MCF")) {
            throw new NotYetImplementedException("The chart generation capability is not yet implemented for MCF.");
        }
        
        ObjectFactory factory = new ObjectFactory();

        scenario = factory.createScenario();
        @SuppressWarnings("unchecked")
        List<Event> events = scenario.getEvent();

        LoadDomain loadTopology = new LoadDomain(topologyName,true,true);
        events.add(loadTopology);

        LoadTrafficMatrix loadTrafficMatrix = new LoadTrafficMatrix(trafficMatrixFileName);
        events.add(loadTrafficMatrix);

        Domain domain = InterDomainManager.getInstance().loadDomain(topologyName,true,true);

        if(!methodName.equals("MCF")) {
            StartAlgo startAlgo = new StartAlgo(methodName,startAlgoParams,domain.getASID());
            events.add(startAlgo);
            
            if(createChart) {
                HashMap<String, String> chartCreationParams = new HashMap<String, String>();
                chartCreationParams.put("asId", Integer.toString(domain.getASID()));
                chartCreationParams.put("absoluteLoad", "false");
                chartCreationParams.put("statistic", "Max");
                ChartCreation chartCreation = new ChartCreation("WCA", "LinksLoadDataCollector", chartCreationParams);
                events.add(chartCreation);
            }
        }

        TrafficMatrixManager.getInstance().loadTrafficMatrix(trafficMatrixFileName);
        //TrafficMatrix trafficMatrix = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix();

        LinkDown linkDown;
        LinkUp linkUp;

        List<Node> nodes = domain.getUpNodes();
        String llcId = "Load 0";

        if (methodName.equals("MCF")) {
            ComputeMCF computeMCF = new ComputeMCF(domain.getASID(), llcId);
            computeMCF.setRunGLPSOL(true);
            events.add(computeMCF);
        } else {
            IGPRouting routing = new IGPRouting(domain.getASID(), llcId);
            routing.setSPFtype(methodName);
            events.add(routing);
        }

        HashMap<String, String> chartAddSeriesParams = new HashMap<String, String>();
        chartAddSeriesParams.put("linkLoadComputerId", llcId);

        if(createChart) {
            ChartAddSeries chartAddSeries = new ChartAddSeries("WCA", "No failure", chartAddSeriesParams);
            events.add(chartAddSeries);
        }

        ShowLinkLoad linkLoad = new ShowLinkLoad(domain.getASID(), llcId, false);
        events.add(linkLoad);

        /*
        int lspID = 0;
        for(Iterator<Node> it1 = nodes.iterator(); it1.hasNext();) {
        Node source = it1.next();
        for(Iterator<Node> it2 = nodes.iterator(); it2.hasNext();) {
        Node destination = it2.next();
        if(source == destination)
        continue;
        float demandValue = trafficMatrix.get(source.getId(),destination.getId());
        LSPCreation lspCreation = new LSPCreation(source.getId(), destination.getId(), Integer.toString(lspID++), demandValue, methodName, algoParams);
        events.add(lspCreation);
        }
        }
        ShowLinkInfo linkInfo = new ShowLinkInfo(InfoLinkType.RESERVATION,false,domain.getASID());
        events.add(linkInfo);

        for (int j =0; j < lspID; j++) {
        LSPDeletion lspDeletion = new LSPDeletion(Integer.toString(j));
        events.add(lspDeletion);
        } */

        boolean linkVisited[] = new boolean[domain.getNbLinks()];
        for (int i = 0; i < linkVisited.length; i++) {
            linkVisited[i] = false;
        }
        DomainConvertor convertor = domain.getConvertor();

        for(Node linkFailureNode : nodes) {
            List<Link> linkList = linkFailureNode.getOutLink();
            for (int i = 0; i < linkList.size(); i++) {
                Link failedLink1 = linkList.get(i);
                if(linkVisited[convertor.getLinkId(failedLink1.getId())]) {
                    continue;
                }

                Link failedLink2 = domain.getReverseLink(failedLink1);

                if (failedLink2!= null)
                    linkVisited[convertor.getLinkId(failedLink2.getId())] = true;

                linkDown = new LinkDown(failedLink1.getId());
                events.add(linkDown);
                if (failedLink2 != null) {
                    linkDown = new LinkDown(failedLink2.getId());
                    events.add(linkDown);
                }

                Echo echo = new Echo("Failure of link between " + failedLink1.getSrcNode().getId() + " and "
                        + failedLink1.getDstNode().getId());
                events.add(echo);
                /*
                lspID = 0;
                for(Iterator<Node> it1 = nodes.iterator(); it1.hasNext();) {
                Node source = it1.next();
                for(Iterator<Node> it2 = nodes.iterator(); it2.hasNext();) {
                Node destination = it2.next();
                if(source == destination)
                continue;
                float demandValue = trafficMatrix.get(source.getId(),destination.getId());
                LSPCreation lspCreation = new LSPCreation(source.getId(), destination.getId(), Integer.toString(lspID++), demandValue, methodName, algoParams);
                events.add(lspCreation);
                }
                }
                linkInfo = new ShowLinkInfo(InfoLinkType.RESERVATION,false,domain.getASID());
                events.add(linkInfo);

                for (int j =0; j < lspID; j++) {
                LSPDeletion lspDeletion = new LSPDeletion(Integer.toString(j));
                events.add(lspDeletion);
                }       */

                linkLoad = new ShowLinkLoad(domain.getASID(), llcId, false);
                events.add(linkLoad);

                if(createChart) {
                    ChartAddSeries chartAddSeries = new ChartAddSeries("WCA", failedLink1.getSrcNode().getId()+"-"+failedLink1.getDstNode().getId(), chartAddSeriesParams);
                    events.add(chartAddSeries);
                }

                linkUp = new LinkUp(failedLink1.getId());
                events.add(linkUp);
                if (failedLink2 != null) {
                    linkUp = new LinkUp(failedLink2.getId());
                    events.add(linkUp);
                }

            }
        }
 
        if(createChart) {
            ChartFormatType chartFormatType;
            if(chartFileName.endsWith(".png")) {
                chartFormatType = ChartFormatType.PNG;
            } else if(chartFileName.endsWith(".jpg")) {
                chartFormatType = ChartFormatType.JPG;
            } else if(chartFileName.endsWith(".eps")) {
                chartFormatType = ChartFormatType.EPS;
            } else {
                throw new IllegalArgumentException(chartFileName+" does not end with \".png\", \".jpg\" or \".eps\".");
            }
            HashMap<String, String> chartSaveParams = new HashMap<String, String>();
            chartSaveParams.put("asId", Integer.toString(domain.getASID()));
            chartSaveParams.put("allLinks", "false");
            ChartSave chartSave = new ChartSave("WCA", chartFileName, chartFormatType, "Worst Case Link Failure ("+methodName+")", "Links' failures", "Maximum link load", 800, 600, "LoadChartPlotter", chartSaveParams);
            events.add(chartSave);
            
            ChartDeletion chartDeletion = new ChartDeletion("WCA");
            events.add(chartDeletion);
        }
    }

    public Scenario generateScenario() {
        return scenario;
    }
}
