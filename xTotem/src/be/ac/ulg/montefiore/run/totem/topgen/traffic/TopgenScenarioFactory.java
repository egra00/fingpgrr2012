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
package be.ac.ulg.montefiore.run.totem.topgen.traffic;

import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.scenario.model.*;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.Scenario;
import be.ac.ulg.montefiore.run.totem.topgen.util.RoutingMatrix;

import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.Iterator;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class is a scenario factory for Topgen.
 *
 * <p>Creation date: 2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class TopgenScenarioFactory {

    /**
     * Builds and returns a simple scenario.
     * @param trafficGenerator The traffic generator to be used.
     * @throws IllegalStateException If <code>generate()</code> has not been called in
     *                               <code>trafficGenerator</code>.
     * @return A Scenario XML file.
     */
    public static Scenario makeSimpleScenario(TrafficGenerator trafficGenerator) throws JAXBException, IllegalStateException, NodeNotFoundException {
        double[] traffic = trafficGenerator.getTraffic();
        ObjectFactory factory = new ObjectFactory();
        Scenario scenario = factory.createScenario();

        RoutingMatrix matrix = trafficGenerator.getMatrix();
        List events = scenario.getEvent();
        List<Node> nodes = InterDomainManager.getInstance().getDefaultDomain().getUpNodes();
        int lspID = 0;
        for(Iterator<Node> it1 = nodes.iterator(); it1.hasNext();) {
            Node origin = it1.next();
            String src = origin.getId();
            int srcInt = InterDomainManager.getInstance().getDefaultDomain().getConvertor().getNodeId(src);
            for(Iterator<Node> it2 = nodes.iterator(); it2.hasNext();) {
                Node destination = it2.next();
                if(origin == destination)
                    continue;
                String dst = destination.getId();
                int dstInt = InterDomainManager.getInstance().getDefaultDomain().getConvertor().getNodeId(dst);
                int index = matrix.getKey(srcInt, dstInt);
                if(traffic[index] != 0) {
                    LSPCreation lspCreation = new LSPCreation(src, dst, Integer.toString(lspID++), ((float) traffic[index]), "CSPF", null);
                    events.add(lspCreation);
                }
            }
        }

        return scenario;
    }
}
