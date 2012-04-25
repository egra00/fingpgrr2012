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

import java.util.*;

import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.topgen.util.TMBuildingException;
import be.ac.ulg.montefiore.run.totem.topgen.util.PlacementObject;
import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.topgen.traffic.model.SyntheticTrafficModel;
import be.ac.ulg.montefiore.run.totem.topgen.traffic.model.TrafficModel;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;

/*
 * Changes:
 * --------
 * - 30-Oct-2007: Refacto to use the new interface (GMO) 
 *
 */

/**
 * Implemention of a traffic generator.
 * It generates traffic matrix elements randomly according to a <code>Distribution</code>.
 *
 * <p>Creation date: 8 janv. 07
 *
 * @author Georges Nimubona (nimubonageorges@hotmail.com)
 */
public class RandomTrafficGenerator extends AbstractDistributionTrafficGenerator {
    private TrafficModel model;
    private PlacementObject po;

    /**
     * Initializes a newly created <code>RandomTrafficGenerator</code> object.
     */
    public RandomTrafficGenerator() {
        parameters = new HashMap<String,String>();
    }

    public RandomTrafficGenerator(HashMap<String, String> parameters) throws InvalidParameterException {
		for (String param : parameters.keySet()) {
            setParam(param, parameters.get(param));
        }
	}

    protected TrafficMatrix buildTM() throws TMBuildingException {
        TrafficMatrix tm;

        double[][] elements = new double[domain.getConvertor().getMaxNodeId()][domain.getConvertor().getMaxNodeId()];
        for (double[] el : elements) {
            Arrays.fill(el, 0);
        }

        for (Node src : nodes) {
            for (Node dst : nodes) {
                try {
                    elements[domain.getConvertor().getNodeId(src.getId())][domain.getConvertor().getNodeId(dst.getId())] = model.generate(src, dst);
                } catch (NodeNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        tm = po.buildTM(elements);

        return tm;
    }

    protected void initialize_generation() throws InvalidParameterException {
        super.initialize_generation();

        po = new PlacementObject();
        try {
            po.setParam("ASID", String.valueOf(domain.getASID()));
        } catch (InvalidParameterException e) {
            e.printStackTrace();
        }
        model = new SyntheticTrafficModel(dis);
    }
}
 