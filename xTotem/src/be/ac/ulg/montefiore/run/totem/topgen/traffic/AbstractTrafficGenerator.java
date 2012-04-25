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

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.topgen.util.TMBuildingException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.SPFLinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
* Changes:
* --------
*
*/

/**
* Traffic generator that can generate a specified number of matrices. Each matrix is routable thanks to SPF.
* A matrix that is not routable is computed again with a maximum number of times given by the maxTrials parameter.
*
* <p>Creation date: 29/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public abstract class AbstractTrafficGenerator implements TrafficGeneratorInterface {

    private static final Logger logger = Logger.getLogger(AbstractTrafficGenerator.class);

    protected static final ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();
    static {
        try {
            params.add(new ParameterDescriptor("ASID", "Domain AS id.", Integer.class, null));
            params.add(new ParameterDescriptor("numTrafficMatrices", "Number of matrices to generate", Integer.class, 1));
            params.add(new ParameterDescriptor("shouldBeRoutable", "Tell if the matrix should be routable with SPF", Boolean.class, true,  new Boolean[] {true, false}));
            params.add(new ParameterDescriptor("maxTrials", "Number of trials before giving up finding a routable matrix.", Integer.class, 3));
            params.add(new ParameterDescriptor("generateOnlyEdgeTraffic", "If set to true, only traffic to/from non-CORE nodes will be generated.", Boolean.class, true, new Boolean[] {true, false}));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    /* The choices made by the user. */
    protected HashMap<String,String> parameters;
    protected Domain domain;
    protected List<Node> nodes;

    protected ParameterDescriptor findParam(String name) {
        for (ParameterDescriptor param : getAvailableParameters()) {
            if (param.getName().equals(name)) return param;
        }
        return null;
    }

    public void setParam(String name, String value) throws InvalidParameterException {
        ParameterDescriptor param = findParam(name);
        if (param == null) throw new InvalidParameterException("Unknown parameter: " + name);

        Object o;
        try {
            if (param.getType() == Integer.class) {
                o = Integer.parseInt(value);
            } else if (param.getType() == Double.class) {
                o = Double.parseDouble(value);
            } else if (param.getType() == Float.class) {
                o = Float.parseFloat(value);
            } else if (param.getType() == String.class) {
                o = value;
            } else if (param.getType() == Boolean.class) {
                o = Boolean.parseBoolean(value);
            } else {
                o = value;
            }
            if (param.getPossibleValues() != null) {
                boolean found = false;
                for (Object pValue : param.getPossibleValues()) {
                    if (pValue.equals(o)) {
                        found = true;
                        break;
                    }
                }
                if (!found) throw new InvalidParameterException("Value " + value + " for parameter " + name + " is not in the list of possible values.");
            }
        } catch (NumberFormatException e) {
            throw new InvalidParameterException("Wrong type for parameter " + name + " Expecting type: " + param.getType().getSimpleName());
        }

        if (parameters == null) parameters = new HashMap<String, String>();
        parameters.put(name, value);
    }

    public String getParam(String name) throws InvalidParameterException {
        if(parameters.get(name) == null)
            throw new InvalidParameterException("Invalid parameter name :" + name);
        else return parameters.get(name);
    }

    /**
     * Generate a traffic matric based on the paremeters. The method {@link #initialize_generation()} is called at first,
     * Then method {@link #buildTM()} is called for each matrix generation.
     * @return
     * @throws TrafficGenerationException
     */
    public List<TrafficMatrix> generate() throws TrafficGenerationException {
        long time = System.currentTimeMillis();
        int asId;
        List<TrafficMatrix> tms;

        List<Node> tmpNodes;
        try {
            if (parameters.get("ASID") == null) {
                domain = InterDomainManager.getInstance().getDefaultDomain();
                asId = domain.getASID();
            } else {
                asId = Integer.parseInt(parameters.get("ASID"));
                domain = InterDomainManager.getInstance().getDomain(asId);
            }
            tmpNodes = domain.getUpNodes();

        } catch (InvalidDomainException e) {
            e.printStackTrace();
            throw new TrafficGenerationException("Domain not found.");
        }

        nodes = new ArrayList<Node>(tmpNodes.size());
        // if generateOnlyEdgeTraffic is set, we remove all the core nodes
        boolean edgeTraffic;
        if (parameters.get("generateOnlyEdgeTraffic") == null) {
            edgeTraffic = (Boolean)findParam("generateOnlyEdgeTraffic").getDefaultValue();
        } else {
            edgeTraffic = Boolean.parseBoolean(parameters.get("generateOnlyEdgeTraffic"));
        }
        if (edgeTraffic) {
            for (Node n : tmpNodes) {
                if (n.getNodeType() != Node.Type.CORE) {
                    nodes.add(n);
                }
            }
        } else {
            nodes.addAll(tmpNodes);
        }

        try {
            initialize_generation();
        } catch (InvalidParameterException e) {
            e.printStackTrace();
            throw new TrafficGenerationException("Invalid Parameter: " + e.getMessage());
        }

        //TODO implement traffic generation for a fraction of the nodes only

        try {
			LinkLoadComputer llc;

			int maxTrials;
            if (parameters.get("maxTrials") == null) {
                maxTrials = (Integer)findParam("maxTrials").getDefaultValue();
            } else {
                maxTrials = Integer.parseInt(parameters.get("maxTrials"));
            }
            int nbMat;
            if (parameters.get("numTrafficMatrices") == null) {
                nbMat = (Integer)findParam("numTrafficMatrices").getDefaultValue();
            } else {
                nbMat = Integer.parseInt(parameters.get("numTrafficMatrices"));
            }
            boolean routable;
            if (parameters.get("shouldBeRoutable") == null) {
                routable = (Boolean)(findParam("shouldBeRoutable").getDefaultValue());
            } else {
                routable = Boolean.parseBoolean(parameters.get("shouldBeRoutable"));
            }

			tms = new ArrayList<TrafficMatrix>();
			for(int i = 0; i < nbMat; i++) {
                TrafficMatrix tm;
                int nbTrials = 0;
                double max = 0;
				do {
                    logger.info("Try " + nbTrials + " to generate TM");
					tm = buildTM();
                    if (routable) {
					    llc = new SPFLinkLoadStrategy(domain, tm);
                        llc.recompute();
					    max = DoubleArrayAnalyse.getMaximum(llc.getData().getUtilization());
                    } else max = 0;
					nbTrials++;
				} while ((max >= 1) && (nbTrials < maxTrials));
				if(max < 1)
					tms.add(tm);
			}
			time = System.currentTimeMillis() - time;
			logger.info("Traffic matrices generated in " + time + " ms");
		}
		catch (NumberFormatException e) {
			throw new TrafficGenerationException(e.getMessage());
		}
		catch (TMBuildingException e) {
			throw new TrafficGenerationException(e.getMessage());
		}
    	return tms;
    }

    /**
     * Build a TrafficMatrix.
     * @return
     * @throws TMBuildingException
     */
    protected abstract TrafficMatrix buildTM() throws TMBuildingException;


    /**
     * Initialize the generation of traffic matrices. Should fetch class specific parameters and prepare the data
     * structures.
     * @throws InvalidParameterException
     */
    protected abstract void initialize_generation() throws InvalidParameterException;

    public List<ParameterDescriptor> getAvailableParameters() {
        return (ArrayList<ParameterDescriptor>)params.clone();
    }
}
