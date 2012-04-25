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

//import from Brite_modified.jar
//NOTE: Constants from ModelConstants are not final
//so we cannot use the switch-case instruction.
import Model.ModelConstants;


/*
 * Changes:
 * --------
 *
 *
 */

/**
 *
 *
 * <p>Creation date: Apr 25, 2007
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 * @author Georges Nimubona (nimubonageorges@hotmail.com)
 */
public final class WrapperConstants {
	
    /* Topology types */
    public static final int TYPE_AS_ONLY = 0;
    public static final int TYPE_ROUTER_ONLY = 1;
    public static final int TYPE_TOP_DOWN = 2;
    public static final int TYPE_BOTTOM_UP = 3;
    
    /* Models */
    public static final int MODEL_WAXMAN = 0;
    public static final int MODEL_BA1 = 1;
    public static final int MODEL_BA2 = 2;
    public static final int MODEL_GLP = 3;
    
    /* GUI Constants */
    public static final int GUI_EDGE_CONNECTION_MODEL = 0;
    public static final int GUI_GROUPING_MODEL = 1;
    public static final int GUI_AS_ASSIGNMENT = 2;
    public static final int GUI_INTER_BW = 3;
    public static final int GUI_TOP_ALPHA = 4;
    public static final int GUI_TOP_BETA = 5;
    public static final int GUI_BOTTOM_ALPHA = 6;
    public static final int GUI_BOTTOM_BETA = 7;
    public static final int GUI_SYNTH_DISTR = 8;
    public static final int GUI_GRAVITY_FRICTION = 9;
    public static final int GUI_GRAVITY_DISTRIBUTION = 10;
    public static final int GUI_DISTR = 11;
    public static final int GUI_DISTR_BIMODAL = 12;
    public static final int GUI_DISTR_CONSTANT = 13;
    public static final int GUI_DISTR_NORMAL = 14;
    public static final int GUI_DISTR_POISSON = 15;
    public static final int GUI_DISTR_UNIFORM_FLOAT = 16;
    public static final int GUI_DISTR_UNIFORM_INT = 17;
    
    /* Metric Constants */
    public static final int METRIC_HOP_COUNT = 0;
    public static final int METRIC_INV_BW = 1;
    
    /* Traffic models */
    public static final int TRAFFIC_MODEL_SYNTH = 0;
    public static final int TRAFFIC_MODEL_GRAVITY = 1;
    
    /* Probability distributions */
    public static final int DISTRIBUTION_BIMODAL = 0;
    public static final int DISTRIBUTION_CONSTANT = 1;
    public static final int DISTRIBUTION_NORMAL = 2;
    public static final int DISTRIBUTION_POISSON = 3;
    public static final int DISTRIBUTION_UNIFORM_FLOAT = 4;
    public static final int DISTRIBUTION_UNIFORM_INT = 5;
    public static final int DISTRIBUTION_INV_NORMAL = 6;
    public static final int DISTRIBUTION_LOGISTIC = 7;
    public static final int DISTRIBUTION_LOGLOGISTIC = 8;
    public static final int DISTRIBUTION_LOGNORMAL = 9;
    
    /* Gravity model friction factors */
    public static final int GRAVITY_FRICTION_DISTANCE = 0;
    public static final int GRAVITY_FRICTION_DISTRIBUTION = 1;
    
    private WrapperConstants() { }
/* Get Methods */
    
    /**
     Returns the number of models according to <code>topologyType</code>.
     @param topologyType A type of topology.
     @return The number of models.
     */
    public static int getNumberOfModels(int topologyType) {
        switch(topologyType) {
        case TYPE_AS_ONLY:
            return 1;
        case TYPE_ROUTER_ONLY:
            return 1;
        case TYPE_TOP_DOWN:
            return 2;
        case TYPE_BOTTOM_UP:
            return 1;
        default:
            throw new IllegalArgumentException("Type "
                    +topologyType+" not found !");
        }
    }
    
    /* There are two strModel methods to permit the addition of models
     * that are not convenient for both the top-level and the bottom-
     * level. There are two strBWDist methods for the same reason. */
    
    public static String getStrASAssignment(int ASAssignment) {
        if(ASAssignment == ModelConstants.BU_ASSIGN_CONST)
            return "Constant";
        else if(ASAssignment == ModelConstants.BU_ASSIGN_UNIFORM)
            return "Uniform";
        else if(ASAssignment == ModelConstants.BU_ASSIGN_EXP)
            return "Exponential";
        else if(ASAssignment == ModelConstants.BU_ASSIGN_HT)
            return "Heavy Tailed";
        throw new IllegalArgumentException("AS Assignment "
                +ASAssignment+" not found !");
    }
    
    public static int getASAssignment(String ASAssignment) {
        if(ASAssignment.equals("Constant")) {
            return (int) ModelConstants.BU_ASSIGN_CONST;
        }
        else if(ASAssignment.equals("Uniform")) {
            return (int) ModelConstants.BU_ASSIGN_UNIFORM;
        }
        else if(ASAssignment.equals("Exponential")) {
            return (int) ModelConstants.BU_ASSIGN_EXP;
        }
        else if(ASAssignment.equals("Heavy Tailed")) {
            return (int) ModelConstants.BU_ASSIGN_HT;
        }
        throw new IllegalArgumentException("AS Assignment "+ASAssignment+" not found !");
    }
    
    public static String getStrDistribution(int distribution) {
        switch(distribution) {
        case DISTRIBUTION_BIMODAL :
            return "Bimodal";
        case DISTRIBUTION_CONSTANT :
            return "Constant";
        case DISTRIBUTION_NORMAL :
            return "Normal";
        case DISTRIBUTION_POISSON :
            return "Poisson";
        case DISTRIBUTION_UNIFORM_FLOAT :
            return "Uniform (float)";
        case DISTRIBUTION_UNIFORM_INT :
            return "Uniform (integer)";
        case DISTRIBUTION_INV_NORMAL:
            return "Inverse normal";
        case DISTRIBUTION_LOGISTIC:
            return "Logistic";
        case DISTRIBUTION_LOGLOGISTIC:
            return "LogLogistic";
        case DISTRIBUTION_LOGNORMAL:
            return "LogNormal";
        default:
            throw new IllegalArgumentException("Distribution "+distribution
                    +" not found !");
        }
    }
    
    public static int getDistribution(String distribution) {
        if(distribution.equals("Bimodal")) {
            return DISTRIBUTION_BIMODAL;
        }
        else if(distribution.equals("Constant")) {
            return DISTRIBUTION_CONSTANT;
        }
        else if(distribution.equals("Normal")) {
            return DISTRIBUTION_NORMAL;
        }
        else if(distribution.equals("Poisson")) {
            return DISTRIBUTION_POISSON;
        }
        else if(distribution.equals("Uniform (float)")) {
            return DISTRIBUTION_UNIFORM_FLOAT;
        }
        else if(distribution.equals("Uniform (integer)")) {
            return DISTRIBUTION_UNIFORM_INT;
        }
        else if(distribution.equals("Inverse normal")) {
            return DISTRIBUTION_INV_NORMAL;
        }
        else if(distribution.equals("Logistic")) {
            return DISTRIBUTION_LOGISTIC;
        }
        else if(distribution.equals("LogLogistic")) {
            return DISTRIBUTION_LOGLOGISTIC;
        }
        else if(distribution.equals("LogNormal")) {
            return DISTRIBUTION_LOGNORMAL;
        }
        throw new IllegalArgumentException("Distribution "+distribution+" not found !");
    }
    
    public static String getStrEdgeConnectionModel(int edgeConn) {
        if(edgeConn == ModelConstants.TD_RANDOM)
            return "Random";
        else if(edgeConn == ModelConstants.TD_SMALLEST)
            return "Smallest-Degree";
        else if(edgeConn == ModelConstants.TD_SMALLEST_NONLEAF)
            return "Smallest-Degree NonLeaf";
        else if(edgeConn == ModelConstants.TD_KDEGREE)
            return "Smallest k-Degree";
        throw new IllegalArgumentException("Edge Connection Model "
                +edgeConn+" not found !");
    }
    
    public static int getEdgeConnectionModel(String edgeConn) {
        if(edgeConn.equals("Random")) {
            return (int) ModelConstants.TD_RANDOM;
        }
        else if(edgeConn.equals("Smallest-Degree")) {
            return (int) ModelConstants.TD_SMALLEST;
        }
        else if(edgeConn.equals("Smallest-Degree NonLeaf")) {
            return (int) ModelConstants.TD_SMALLEST_NONLEAF;
        }
        else if(edgeConn.equals("Smallest k-Degree")) {
            return (int) ModelConstants.TD_KDEGREE;
        }
        throw new IllegalArgumentException("Edge Connection model "+edgeConn+" not found !");
    }
    
    public static String getStrFrictionFactor(int frictionFactor) {
        switch(frictionFactor) {
        case GRAVITY_FRICTION_DISTANCE:
            return "Distance";
        case GRAVITY_FRICTION_DISTRIBUTION:
            return "Probability Distribution";
        default:
            throw new IllegalArgumentException("Friction factor "
                    +frictionFactor+" not found !");
        }
    }
    
    public static int getFrictionFactor(String frictionFactor) {
        if(frictionFactor.equals("Distance")) {
            return GRAVITY_FRICTION_DISTANCE;
        }
        else if(frictionFactor.equals("Probability Distribution")) {
            return GRAVITY_FRICTION_DISTRIBUTION;
        }
        throw new IllegalArgumentException("Friction factor "+frictionFactor+" not found !");
    }
    
    public static String getStrGroupingModel(int groupingModel) {
        if(groupingModel == ModelConstants.BU_RANDOMWALK)
            return "Random Walk";
        else if(groupingModel == ModelConstants.BU_RANDOMPICK)
            return "Random Pick";
        throw new IllegalArgumentException("Grouping Model "
                +groupingModel+" not found !");
    }
    
    public static int getGroupingModel(String groupingModel) {
        if(groupingModel.equals("Random Walk")) {
            return (int) ModelConstants.BU_RANDOMWALK;
        }
        else if(groupingModel.equals("Random Pick")) {
            return (int) ModelConstants.BU_RANDOMPICK;
        }
        throw new IllegalArgumentException("Grouping Model "+groupingModel+" not found !");
    }
    
    public static String getStrGrowthType(int growthType) {
        if(growthType == ModelConstants.GT_ALL)
            return "All";
        else if(growthType == ModelConstants.GT_INCREMENTAL)
            return "Incremental";
        throw new IllegalArgumentException("Growth Type "
                +growthType+" not found !");
    }
    
    public static int getGrowthType(String growthType) {
        if(growthType.equals("All")) {
            return (int) ModelConstants.GT_ALL;
        }
        else if(growthType.equals("Incremental")) {
            return (int) ModelConstants.GT_INCREMENTAL;
        }
        throw new IllegalArgumentException("Growth Type "+growthType+" not found !");
    }
    
    public static String getStrInterBWDist(int interBWDist) {
        if(interBWDist == ModelConstants.BW_CONSTANT)
            return "Constant";
        else if(interBWDist == ModelConstants.BW_UNIFORM)
            return "Uniform";
        else if(interBWDist == ModelConstants.BW_EXPONENTIAL)
            return "Exponential";
        else if(interBWDist == ModelConstants.BW_HEAVYTAILED)
            return "Heavy Tailed";
        throw new IllegalArgumentException("Inter BW Dist "
                +interBWDist+" not found !");
    }
    
    public static int getInterBWDist(String dist) {
        if(dist.equals("Constant")) {
            return (int) ModelConstants.BW_CONSTANT;
        }
        else if(dist.equals("Uniform")) {
            return (int) ModelConstants.BW_UNIFORM;
        }
        else if(dist.equals("Exponential")) {
            return (int) ModelConstants.BW_EXPONENTIAL;
        }
        else if(dist.equals("Heavy Tailed")) {
            return (int) ModelConstants.BW_HEAVYTAILED;
        }
        throw new IllegalArgumentException("Inter BW Dist "+dist+" not found !");
    }
    
    public static String getStrIntraBWDist(int intraBWDist) {
        if(intraBWDist == ModelConstants.BW_CONSTANT)
            return "Constant";
        else if(intraBWDist == ModelConstants.BW_UNIFORM)
            return "Uniform";
        else if(intraBWDist == ModelConstants.BW_EXPONENTIAL)
            return "Exponential";
        else if(intraBWDist == ModelConstants.BW_HEAVYTAILED)
            return "Heavy Tailed";
        throw new IllegalArgumentException("Intra BW Dist "
                +intraBWDist+" not found !");
    }
    
    public static int getIntraBWDist(String dist) {
        if(dist.equals("Constant")) {
            return (int) ModelConstants.BW_CONSTANT;
        }
        else if(dist.equals("Uniform")) {
            return (int) ModelConstants.BW_UNIFORM;
        }
        else if(dist.equals("Exponential")) {
            return (int) ModelConstants.BW_EXPONENTIAL;
        }
        else if(dist.equals("Heavy Tailed")) {
            return (int) ModelConstants.BW_HEAVYTAILED;
        }
        throw new IllegalArgumentException("Inter BW Dist "+dist+" not found !");        
    }
    
    public static String getStrMetric(int metric) {
        switch(metric) {
        case METRIC_HOP_COUNT:
            return "Hop count";
        case METRIC_INV_BW:
            return "Inverse of BW";
        default:
            throw new IllegalArgumentException("Metric "
                    +metric+" not found !");
        }
    }
    
    public static int getMetric(String metric) {
        if(metric.equals("Hop count")) {
            return METRIC_HOP_COUNT;
        }
        else if(metric.equals("Inverse of BW")) {
            return METRIC_INV_BW;
        }
        throw new IllegalArgumentException("Metric "+metric+" not found !");
    }
    
    public static String getStrModel0(int model0) {
        switch(model0) {
        case MODEL_WAXMAN:
            return "Waxman";
        case MODEL_BA1:
            return "Barabasi-Albert 1";
        case MODEL_BA2:
            return "Barabasi-Albert 2";
        case MODEL_GLP:
            return "GLP";
        default:
            throw new IllegalArgumentException("Top-level model "
                    +model0+" not found !");
        }
    }
    
    public static int getModel0(String model) {
        if(model.equals("Waxman")) {
            return MODEL_WAXMAN;
        }
        else if(model.equals("Barabasi-Albert 1")) {
            return MODEL_BA1;
        }
        else if(model.equals("Barabasi-Albert 2")) {
            return MODEL_BA2;
        }
        else if(model.equals("GLP")) {
            return MODEL_GLP;
        }
        throw new IllegalArgumentException("Unknown model "+model);
    }
    
    public static String getStrModel1(int model1) {
        switch(model1) {
        case MODEL_WAXMAN:
            return "Waxman";
        case MODEL_BA1:
            return "Barabasi-Albert 1";
        case MODEL_BA2:
            return "Barabasi-Albert 2";
        case MODEL_GLP:
            return "GLP";
        default:
            throw new IllegalArgumentException("Bottom-level model "
                    +model1+" not found !");
        }
    }
    
    public static int getModel1(String model) {
        if(model.equals("Waxman")) {
            return MODEL_WAXMAN;
        }
        else if(model.equals("Barabasi-Albert 1")) {
            return MODEL_BA1;
        }
        else if(model.equals("Barabasi-Albert 2")) {
            return MODEL_BA2;
        }
        else if(model.equals("GLP")) {
            return MODEL_GLP;
        }
        throw new IllegalArgumentException("Unknown model "+model);
    }
    
    public static String getStrNodePlacement(int nodePlacement) {
        if(nodePlacement == ModelConstants.NP_RANDOM)
            return "Random";
        else if(nodePlacement == ModelConstants.NP_HEAVYTAILED)
            return "Heavy Tailed";
        throw new IllegalArgumentException("Node Placement "
                +nodePlacement+" not found !");
    }
    
    public static int getNodePlacement(String nodePlacement) {
        if(nodePlacement.equals("Random")) {
            return (int) ModelConstants.NP_RANDOM;
        }
        else if(nodePlacement.equals("Heavy Tailed")) {
            return (int) ModelConstants.NP_HEAVYTAILED;
        }
        throw new IllegalArgumentException("Node Placement "+nodePlacement+" not found !");
    }
    
    public static String getStrPrefConn(int prefConn) {
        if(prefConn == ModelConstants.PC_NONE)
            return "None";
        else if(prefConn == ModelConstants.PC_BARABASI)
            return "On";
        throw new IllegalArgumentException("Preferential connectivity "
                +prefConn+" not found !");
    }
    
    public static int getPrefConn(String prefConn) {
        if(prefConn.equals("None")) {
            return (int) ModelConstants.PC_NONE;
        }
        else if(prefConn.equals("On")) {
            return (int) ModelConstants.PC_BARABASI;
        }
        throw new IllegalArgumentException("Preferential connectivity "+prefConn+" not found !");
    }
    
    public static String getStrTopologyType(int topologyType) {
        switch(topologyType) {
        case TYPE_AS_ONLY:
            return "1 Level: AS Only";
        case TYPE_ROUTER_ONLY:
            return "1 Level: Router (IP) Only";
        case TYPE_TOP_DOWN:
            return "2 Level: Top-Down";
        case TYPE_BOTTOM_UP:
            return "2 Level: Bottom-Up";
        default:
            throw new IllegalArgumentException("Topology type "
                    +topologyType+" not found !");
        }
    }
    
    public static int getTopologyType(String topologyType) {
        if(topologyType.equals("1 Level: AS Only")) {
            return TYPE_AS_ONLY;
        }
        else if(topologyType.equals("1 Level: Router (IP) Only")) {
            return TYPE_ROUTER_ONLY;
        }
        else if(topologyType.equals("2 Level: Top-Down")) {
            return TYPE_TOP_DOWN;
        }
        else if(topologyType.equals("2 Level: Bottom-Up")) {
            return TYPE_BOTTOM_UP;
        }
        throw new IllegalArgumentException("Topology type "+topologyType+" not found !");
    }
    
    public static String getStrTrafficModel(int trafficModel) {
        switch(trafficModel) {
        case TRAFFIC_MODEL_SYNTH:
            return "Synthetic traffic";
        case TRAFFIC_MODEL_GRAVITY:
            return "Gravity model";
        default:
            throw new IllegalArgumentException("Traffic model "+trafficModel
                    +" not found !");
        }
    }
    
    public static int getTrafficModel(String trafficModel) {
        if(trafficModel.equals("Synthetic traffic")) {
            return TRAFFIC_MODEL_SYNTH;
        }
        else if(trafficModel.equals("Gravity model")) {
            return TRAFFIC_MODEL_GRAVITY;
        }
        throw new IllegalArgumentException("Traffic model "+trafficModel+" not found !");
    }
}

    

