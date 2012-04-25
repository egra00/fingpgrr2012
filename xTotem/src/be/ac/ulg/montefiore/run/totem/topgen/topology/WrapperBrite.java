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

import static be.ac.ulg.montefiore.run.totem.topgen.topology.WrapperConstants.*;

import Graph.Edge;
import Graph.Node;
import Model.*;
import Topology.Topology;
import Util.RandomGenManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.topgen.util.Converter;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;


/*
 * Changes:
 * --------
 * - 31-Oct-2007: Rewrite. Use abstractTopologyGenerator. (GMO)
 * - 16-Nov-2007: Wrong parameters used for bottom model of a top-down topology (GMO)
 */

/**
 * A wrapper for <code>Brite</code>
 * <p/>
 * <p>Creation date: Apr 25, 2007
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 * @author Georges Nimubona (nimubonageorges@hotmail.com)
 * @author Gael Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class WrapperBrite extends AbstractTopologyGenerator {
    private static Logger logger = Logger.getLogger(WrapperBrite.class);

    private static ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();

    static {
        try {
            params.add(new ParameterDescriptor("maxTrials", "Max trials before forcing connected or dual connected", Integer.class, 3));
            final String[] models = {"Waxman", "Barabasi-Albert 1", "Barabasi-Albert 2", "GLP"};
            params.add(new ParameterDescriptor("topologyType", "Type of toplogy", String.class, "2 Level: Bottom-Up", new String[]{"1 Level: AS Only", "1 Level: Router (IP) Only", "2 Level: Top-Down", "2 Level: Bottom-Up"}));
            params.add(new ParameterDescriptor("topLevelModel", "Model for the top level", String.class, "GLP", models));
            params.add(new ParameterDescriptor("bottomLevelModel", "Model for the bottom level", String.class, "GLP", models));
            params.add(new ParameterDescriptor("edgeConnectionModel", "", String.class, "Random", new String[]{"Random", "Smallest-Degree", "Smallest-Degree NonLeaf", "Smallest k-Degree"}));
            params.add(new ParameterDescriptor("k", "", Integer.class, 0));
            params.add(new ParameterDescriptor("groupingModel", "", String.class, "Random Pick", new String[]{"Random Walk", "Random Pick"}));
            final String[] dis1 = {"Constant", "Uniform", "Exponential", "Heavy Tailed"};
            params.add(new ParameterDescriptor("asAssignment", "", String.class, "Uniform", dis1));
            params.add(new ParameterDescriptor("interBWDist", "", String.class, "Uniform", dis1));
            params.add(new ParameterDescriptor("intraBWDist", "", String.class, "Uniform", dis1));
            params.add(new ParameterDescriptor("numAS", "", Integer.class, 9));
            params.add(new ParameterDescriptor("interBWMax", "", Double.class, 20000d));
            params.add(new ParameterDescriptor("interBWMin", "", Double.class, 10000d));
            params.add(new ParameterDescriptor("intraBWMax", "", Double.class, 1000d));
            params.add(new ParameterDescriptor("intraBWMin", "", Double.class, 500d));
            params.add(new ParameterDescriptor("topHS", "", Integer.class, 10));
            params.add(new ParameterDescriptor("topLS", "", Integer.class, 1));
            params.add(new ParameterDescriptor("topN", "", Integer.class, 15));
            params.add(new ParameterDescriptor("topNodePlacement", "", String.class, "Random", new String[]{"Random", "Heavy Tailed"}));
            params.add(new ParameterDescriptor("topGrowthType", "", String.class, "All", new String[]{"All", "Incremental"}));
            // not used
            params.add(new ParameterDescriptor("topPreferentialConnectivity", "", String.class, "None", new String[]{"None", "On"}));
            params.add(new ParameterDescriptor("topAlpha", "", Double.class, 0.42));
            params.add(new ParameterDescriptor("topBeta", "", Double.class, 0.65));
            params.add(new ParameterDescriptor("topM", "", Integer.class, 2));
            params.add(new ParameterDescriptor("bottomHS", "", Integer.class, 10));
            params.add(new ParameterDescriptor("bottomLS", "", Integer.class, 1));
            params.add(new ParameterDescriptor("bottomN", "", Integer.class, 15));
            params.add(new ParameterDescriptor("bottomNodePlacement", "", String.class, "Random", new String[]{"Random", "Heavy Tailed"}));
            params.add(new ParameterDescriptor("bottomGrowthType", "", String.class, "All", new String[]{"All", "Incremental"}));
            //not used
            params.add(new ParameterDescriptor("bottomPreferentialConnectivity", "", String.class, "None", new String[]{"None", "On"}));
            params.add(new ParameterDescriptor("bottomAlpha", "", Double.class, 0.42));
            params.add(new ParameterDescriptor("bottomBeta", "", Double.class, 0.65));
            params.add(new ParameterDescriptor("bottomM", "", Integer.class, 2));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    /* The current step of the generation. */
    private byte step;


    /**
     * Initializes a newly created <code>WrapperBrite</code> object.
     */
    public WrapperBrite() {
        super();
        step = 0;
    }



    /* (non-Javadoc)
	 * @see be.ac.ulg.montefiore.run.totem.topgen.topology.TopologyGenerator#generate()
	 */
    public List<Domain> generate() throws TopologyGeneratorException {

        /* briteModel is the "main" model. topLevelModel and bottomLevelModel
        are for Top-Down and Bottom-Up topology types. */
        Model briteModel, topLevelModel, bottomLevelModel;
        List<Domain> domains = new ArrayList<Domain>();

        try {
            int topologyType = getTopologyType(getStringParameter("topologyType"));
            int topLevelModelName = getModel0(getStringParameter("topLevelModel"));
            int bottomLevelModelName = getModel1(getStringParameter("bottomLevelModel"));
            if (topologyType == TYPE_AS_ONLY) {
                if (topLevelModelName == MODEL_WAXMAN) {
                    briteModel = new ASWaxman(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getDoubleParameter("topAlpha"),
                            getDoubleParameter("topBeta"),
                            getGrowthType(getStringParameter("topGrowthType")),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"));

                } else if (topLevelModelName == MODEL_BA1) {
                    briteModel = new ASBarabasiAlbert(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"));
                } else if (topLevelModelName == MODEL_BA2) {
                    briteModel = new ASBarabasiAlbert2(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"),
                            getDoubleParameter("topAlpha"),
                            getDoubleParameter("topBeta"));
                } else if (topLevelModelName == MODEL_GLP) {
                    briteModel = new ASGLP(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"),
                            getDoubleParameter("topAlpha"),
                            getDoubleParameter("topBeta"));
                } else {
                    throw new InvalidParameterException("Model "
                            + topLevelModelName
                            + " unknown !");
                }
            } else if (topologyType == TYPE_ROUTER_ONLY) {
                if (topLevelModelName == MODEL_WAXMAN) {
                    briteModel = new RouterWaxman(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getDoubleParameter("topAlpha"),
                            getDoubleParameter("topBeta"),
                            getGrowthType(getStringParameter("topGrowthType")),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"));
                } else if (topLevelModelName == MODEL_BA1) {
                    briteModel = new RouterBarabasiAlbert(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"));
                } else if (topLevelModelName == MODEL_BA2) {
                    briteModel = new RouterBarabasiAlbert2(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"),
                            getDoubleParameter("topAlpha"),
                            getDoubleParameter("topBeta"));
                } else if (topLevelModelName == MODEL_GLP) {
                    briteModel = new RouterGLP(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"),
                            getDoubleParameter("topAlpha"),
                            getDoubleParameter("topBeta"));
                } else {
                    throw new InvalidParameterException("Model "
                            + topLevelModelName
                            + " unknown !");
                }
            } else if (topologyType == TYPE_TOP_DOWN) {
                if (topLevelModelName == MODEL_WAXMAN) {
                    topLevelModel = new ASWaxman(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getDoubleParameter("topAlpha"),
                            getDoubleParameter("topBeta"),
                            getGrowthType(getStringParameter("topGrowthType")),
                            getInterBWDist(getStringParameter("interBWDist")),
                            getDoubleParameter("interBWMin"),
                            getDoubleParameter("interBWMax"));

                } else if (topLevelModelName == MODEL_BA1) {
                    topLevelModel = new ASBarabasiAlbert(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getInterBWDist(getStringParameter("interBWDist")),
                            getDoubleParameter("interBWMin"),
                            getDoubleParameter("interBWMax"));
                } else if (topLevelModelName == MODEL_BA2) {
                    topLevelModel = new ASBarabasiAlbert2(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getInterBWDist(getStringParameter("interBWDist")),
                            getDoubleParameter("interBWMin"),
                            getDoubleParameter("interBWMax"),
                            getDoubleParameter("topAlpha"),
                            getDoubleParameter("topBeta"));
                } else if (topLevelModelName == MODEL_GLP) {
                    topLevelModel = new ASGLP(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getInterBWDist(getStringParameter("interBWDist")),
                            getDoubleParameter("interBWMin"),
                            getDoubleParameter("interBWMax"),
                            getDoubleParameter("topAlpha"),
                            getDoubleParameter("topBeta"));
                } else {
                    throw new InvalidParameterException("Model "
                            + topLevelModelName
                            + " unknown !");
                }
                if (bottomLevelModelName == MODEL_WAXMAN) {
                    bottomLevelModel = new RouterWaxman(getIntegerParameter("bottomN"),
                            getIntegerParameter("bottomHS"),
                            getIntegerParameter("bottomLS"),
                            getNodePlacement(getStringParameter("bottomNodePlacement")),
                            getIntegerParameter("bottomM"),
                            getDoubleParameter("bottomAlpha"),
                            getDoubleParameter("bottomBeta"),
                            getGrowthType(getStringParameter("bottomGrowthType")),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"));
                } else if (bottomLevelModelName == MODEL_BA1) {
                    bottomLevelModel = new RouterBarabasiAlbert(getIntegerParameter("bottomN"),
                            getIntegerParameter("bottomHS"),
                            getIntegerParameter("bottomLS"),
                            getNodePlacement(getStringParameter("bottomNodePlacement")),
                            getIntegerParameter("bottomM"),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"));
                } else if (bottomLevelModelName == MODEL_BA2) {
                    bottomLevelModel = new RouterBarabasiAlbert2(getIntegerParameter("bottomN"),
                            getIntegerParameter("bottomHS"),
                            getIntegerParameter("bottomLS"),
                            getNodePlacement(getStringParameter("bottomNodePlacement")),
                            getIntegerParameter("bottomM"),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"),
                            getDoubleParameter("bottomAlpha"),
                            getDoubleParameter("bottomBeta"));
                } else if (bottomLevelModelName == MODEL_GLP) {
                    bottomLevelModel = new RouterGLP(getIntegerParameter("bottomN"),
                            getIntegerParameter("bottomHS"),
                            getIntegerParameter("bottomLS"),
                            getNodePlacement(getStringParameter("bottomNodePlacement")),
                            getIntegerParameter("bottomM"),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"),
                            getDoubleParameter("bottomAlpha"),
                            getDoubleParameter("bottomBeta"));
                } else {
                    throw new InvalidParameterException("Model "
                            + bottomLevelModelName
                            + " unknown !");
                }
                ArrayList models = new ArrayList(2);
                models.add(topLevelModel);
                models.add(bottomLevelModel);
                briteModel = new TopDownHierModel(models,
                        getEdgeConnectionModel(getStringParameter("edgeConnectionModel")),
                        getIntegerParameter("k"),
                        getInterBWDist(getStringParameter("interBWDist")),
                        getDoubleParameter("interBWMin"),
                        getDoubleParameter("interBWMax"),
                        getInterBWDist(getStringParameter("intraBWDist")),
                        getDoubleParameter("intraBWMin"),
                        getDoubleParameter("intraBWMax"));

            } else if (topologyType == TYPE_BOTTOM_UP) {
                if (topLevelModelName == MODEL_WAXMAN) {
                    topLevelModel = new RouterWaxman(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getDoubleParameter("topAlpha"),
                            getDoubleParameter("topBeta"),
                            getGrowthType(getStringParameter("topGrowthType")),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"));
                } else if (topLevelModelName == MODEL_BA1) {
                    topLevelModel = new RouterBarabasiAlbert(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"));
                } else if (topLevelModelName == MODEL_BA2) {
                    topLevelModel = new RouterBarabasiAlbert2(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"),
                            getDoubleParameter("topAlpha"),
                            getDoubleParameter("topBeta"));
                } else if (topLevelModelName == MODEL_GLP) {
                    topLevelModel = new RouterGLP(getIntegerParameter("topN"),
                            getIntegerParameter("topHS"),
                            getIntegerParameter("topLS"),
                            getNodePlacement(getStringParameter("topNodePlacement")),
                            getIntegerParameter("topM"),
                            getIntraBWDist(getStringParameter("intraBWDist")),
                            getDoubleParameter("intraBWMin"),
                            getDoubleParameter("intraBWMax"),
                            getDoubleParameter("topAlpha"),
                            getDoubleParameter("topBeta"));
                } else {
                    throw new InvalidParameterException("Model "
                            + topLevelModelName
                            + " unknown !");
                }

                briteModel = new BottomUpHierModel(topLevelModel,
                        getIntegerParameter("numAS"),
                        getGroupingModel(getStringParameter("groupingModel")),
                        getASAssignment(getStringParameter("asAssignment")),
                        getInterBWDist(getStringParameter("interBWDist")),
                        getDoubleParameter("interBWMin"),
                        getDoubleParameter("interBWMax"));
            } else {
                throw new InvalidParameterException("Toplogy type unknown: " + topologyType);
            }

            RandomGenManager rgm = new RandomGenManager();
            briteModel.setRandomGenManager(rgm);

            int metric = getMetric(getStringParameter("metric"));
            int nbTopo = getIntegerParameter("numTopologies");
            int maxTrials = getIntegerParameter("maxTrials");

            Topology topology;
            Domain domain;
            //printParams();

            for (int i = 0; i < nbTopo; ++i) {
                int nbTrials = 0;
                System.out.println("Generating topo " + i);
                while (true) {
                    topology = new Topology(briteModel);
                    /* Reset BRITE classes */
                    Model.reset();
                    Edge.reset();
                    Node.reset();

                    domain = Converter.briteTopologyToDomain(topology, true, metric);
                    nbTrials++;

                    if (nbTrials == maxTrials)
                        break;
                    if ((!getBooleanParameter("mustBeConnected") || topology.getGraph().isConnected()) &&
                            !getBooleanParameter("mustBeDualConnected") || domain.getValidator().isDualConnected())
                        break;
                }

                if (nbTrials == maxTrials) {
                    if (getBooleanParameter("mustBeConnected")) {
                        logger.info("Forcing topology to be connected.");
                        domain.getValidator().forceConnected();
                    }
                    if (getBooleanParameter("mustBeDualConnected")) {
                        logger.info("Forcing topology to be dual connected.");
                        domain.getValidator().forceDuplexConnected();
                    }
                }

                topology = null;
                domains.add(domain);
            }

        } catch (InvalidParameterException e) {
            e.printStackTrace();
            throw new TopologyGeneratorException("Invalid parameter: " + e.getMessage());
        } catch (NodeNotFoundException e) {
            throw new TopologyGeneratorException(e.getMessage());
        } catch (NodeAlreadyExistException e) {
            throw new TopologyGeneratorException(e.getMessage());
        } catch (LinkAlreadyExistException e) {
            throw new TopologyGeneratorException(e.getMessage());
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
            throw new TopologyGeneratorException(e.getMessage());
        }


        return domains;
    }

    public List<ParameterDescriptor> getAvailableParameters() {
        List<ParameterDescriptor> list = super.getAvailableParameters();
        list.addAll(params);
        return list;
    }

    /**
     * Debug
     */
    public void printParams() {
        for (String param : parameters.keySet()) {
            System.out.println(param + ": " + parameters.get(param));
        }
    }

    /**
     * Gives the current step of the generation.
     * First Step is 0.
     *
     * @return The current step of the generation.
     */
    public byte getStep() {
        return step;
    }

    /**
     * Resets an existing <code>WrapperBrite</code> object.
     * This function exists to permit several generations
     * within the same session.
     */
    public void reset() {
        step = 0;
        parameters = null;
    }

    /**
     * Sets the current step of the generation.
     */
    public void setStep(byte step) {
        this.step = step;
    }
}

