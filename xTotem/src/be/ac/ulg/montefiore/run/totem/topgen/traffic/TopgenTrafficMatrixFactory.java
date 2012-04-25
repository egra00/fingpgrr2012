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

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.topgen.util.RoutingMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.TrafficMatrixImpl;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class implements a traffic matrix factory for Topgen.
 *
 * <p>Creation date: 15-d�c.-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class TopgenTrafficMatrixFactory {
    
    private static final Logger logger = Logger.getLogger(TopgenTrafficMatrixFactory.class);
    
    /**
     * Sets the default traffic matrix with the information contained in <code>trafficGenerator</code>.
     * @param trafficGenerator The traffic generator to use.
     * @throws IllegalStateException If <code>generate()</code> has not been called in
     *                               <code>trafficGenerator</code>.
     */
    public static void makeSimpleTrafficMatrix(TrafficGenerator trafficGenerator) throws IllegalStateException {
        double[] traffic = trafficGenerator.getTraffic();
        RoutingMatrix matrix = trafficGenerator.getMatrix();
        
        Domain domain = InterDomainManager.getInstance().getDefaultDomain();
        List<Node> nodes = domain.getUpNodes();
        
        try {
            TrafficMatrix tm = new TrafficMatrixImpl(domain.getASID());
            int tmId = TrafficMatrixManager.getInstance().generateTMID(domain.getASID());
            TrafficMatrixManager.getInstance().addTrafficMatrix(tm, tmId);
            TrafficMatrixManager.getInstance().setDefaultTrafficMatrix(tmId);
            for(Iterator<Node> it1 = nodes.iterator(); it1.hasNext();) {
                Node origin = it1.next();
                String src = origin.getId();
                int srcInt = domain.getConvertor().getNodeId(src);
                for(Iterator<Node> it2 = nodes.iterator(); it2.hasNext();) {
                    Node destination = it2.next();
                    if(origin == destination)
                        continue;
                    String dst = destination.getId();
                    int dstInt = domain.getConvertor().getNodeId(dst);
                    int index = matrix.getKey(srcInt, dstInt);
                    if(traffic[index] != 0) {
                        tm.set(srcInt, dstInt, (float) traffic[index]);
                    }
                }
            }
        }
        catch(NodeNotFoundException e) {
            logger.error("NodeNotFoundException in TopgenTrafficMatrixFactory.");
            return;
        }
        catch(InvalidDomainException e) {
            logger.error("InvalidDomainException in TopgenTrafficMatrixFactory.");
            return;
        }
        catch(TrafficMatrixAlreadyExistException e) {
            logger.error("TrafficMatrixAlreadyExistException in TopgenTrafficMatrixFactory.");
            return;
        }
        catch(InvalidTrafficMatrixException e) {
            logger.error("InvalidTrafficMatrixException in TopgenTrafficMatrixFactory.");
            return;
        } catch (TrafficMatrixIdException e) {
            e.printStackTrace();
            return;
        }
    }
}
