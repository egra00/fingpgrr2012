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
package be.ac.ulg.montefiore.run.totem.topgen.topology;

import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 16-Nov-2007: Add information message when using default values (GMO)
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 31/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public abstract class AbstractTopologyGenerator implements TopologyGenerator {
    private static final Logger logger = Logger.getLogger(AbstractTopologyGenerator.class);

    private static ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();

    static {
        try {
            params.add(new ParameterDescriptor("mustBeConnected", "Tell if the resulting topology should be connected.", Boolean.class, true, new Boolean[] {true, false}));
            params.add(new ParameterDescriptor("mustBeDualConnected", "Tell if the resulting topology should be connected by duplex links.", Boolean.class, true, new Boolean[] {true, false}));
            params.add(new ParameterDescriptor("numTopologies", "Number of topologies to generate", Integer.class, 1));
            params.add(new ParameterDescriptor("topologyPrefix", "The prefix of the files", String.class, "topo"));
            params.add(new ParameterDescriptor("metric", "The metric to use", String.class, "Inverse of BW", new String[] {"Inverse of BW", "Hop Count"}));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    /* The choices made by the user. */
    protected HashMap<String, String> parameters;

    protected AbstractTopologyGenerator() {
        parameters = new HashMap<String, String>();
    }


    /* (non-Javadoc)
	 * @see be.ac.ulg.montefiore.run.totem.topgen.topology.TopologyGenerator#setParam(java.lang.String, java.lang.String)
	 */
    public void setParam(String name, String value) throws InvalidParameterException {
        ParameterDescriptor param = findParam(name);
        if (param == null) throw new InvalidParameterException("Unknown parameter: " + name);
        if (!param.validate(value)) throw new InvalidParameterException("Value " + value + " not assignable to parameter " + name);
        parameters.put(name, value);
    }

    /* (non-Javadoc)
	 * @see be.ac.ulg.montefiore.run.totem.topgen.topology.TopologyGenerator#getParam(java.lang.String)
	 */
    public String getParam(String name) throws InvalidParameterException {
        if (parameters.get(name) == null)
            throw new InvalidParameterException("Invalid parameter: " + name);
        else
            return parameters.get(name);
    }

    protected ParameterDescriptor findParam(String name) {
        for (ParameterDescriptor param : getAvailableParameters()) {
            if (param.getName().equals(name)) return param;
        }
        return null;
    }

    protected int getIntegerParameter(String name) throws InvalidParameterException {
        ParameterDescriptor param = findParam(name);
        if (param == null) throw new InvalidParameterException("Parameter " + name + " unknown");
        String value = parameters.get(name);
        try {
            if (value == null) {
                logger.info("Getting default value for param: " + name);
                return (Integer) param.getDefaultValue();
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new InvalidParameterException("Parameter " + name + " not integer type");
        }
    }

    protected double getDoubleParameter(String name) throws InvalidParameterException {
        ParameterDescriptor param = findParam(name);
        if (param == null) throw new InvalidParameterException("Parameter " + name + " unknown");
        String value = parameters.get(name);
        try {
            if (value == null) {
                logger.info("Getting default value for param: " + name);
                return (Double) param.getDefaultValue();
            } else {
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new InvalidParameterException("Parameter " + name + " not double type");
        }
    }

    protected String getStringParameter(String name) throws InvalidParameterException {
        ParameterDescriptor param = findParam(name);
        if (param == null) throw new InvalidParameterException("Parameter " + name + " unknown");
        String value = parameters.get(name);
        try {
            if (value == null) {
                logger.info("Getting default value for param: " + name);
                return (String) param.getDefaultValue();
            } else {
                return value;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new InvalidParameterException("Parameter " + name + " not string type");
        }
    }

    protected float getFloatParameter(String name) throws InvalidParameterException {
        ParameterDescriptor param = findParam(name);
        if (param == null) throw new InvalidParameterException("Parameter " + name + " unknown");
        String value = parameters.get(name);
        try {
            if (value == null) {
                logger.info("Getting default value for param: " + name);
                return (Float) param.getDefaultValue();
            } else {
                return Float.parseFloat(value);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new InvalidParameterException("Parameter " + name + " not string type");
        }
    }

    protected boolean getBooleanParameter(String name) throws InvalidParameterException {
        ParameterDescriptor param = findParam(name);
        if (param == null) throw new InvalidParameterException("Parameter " + name + " unknown");
        String value = parameters.get(name);
        try {
            if (value == null) {
                logger.info("Getting default value for param: " + name);
                return (Boolean) param.getDefaultValue();
            } else {
                return Boolean.parseBoolean(value);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new InvalidParameterException("Parameter " + name + " not string type");
        }
    }

    public List<ParameterDescriptor> getAvailableParameters() {
        return (List<ParameterDescriptor>)params.clone();
    }

}
