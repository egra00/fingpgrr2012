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

import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.TrafficMatrixImpl;
import be.ac.ulg.montefiore.run.totem.topgen.util.TMBuildingException;
import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;

import java.util.List;
import java.util.ArrayList;

/*
* Changes:
* --------
*
*/

/**
* Generate traffic matrices for which each pair src-dst has the same traffic value.
*
* <p>Creation date: 29/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ConstantTrafficGenerator extends AbstractTrafficGenerator {
    private static final ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();
    static {
        try {
            params.add(new ParameterDescriptor("value", "Constant to use in the TM.", Double.class, 0.0));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    private double value;

    protected TrafficMatrix buildTM() throws TMBuildingException {
        TrafficMatrix tm = new TrafficMatrixImpl(domain);
        for (Node src : nodes) {
            for (Node dst : nodes) {
                try {
                    tm.set(src.getId(), dst.getId(), (float)value);
                } catch (NodeNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return tm;
    }

    protected void initialize_generation() throws InvalidParameterException {
        if (parameters.get("value") == null) {
            throw new InvalidParameterException("value parameter should be set.");
        }
        value = Double.parseDouble(parameters.get("value"));
    }

    public List<ParameterDescriptor> getAvailableParameters() {
        List<ParameterDescriptor> list = super.getAvailableParameters();
        list.addAll(params);
        return list;
    }
}
