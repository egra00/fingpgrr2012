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
package be.ac.ucl.poms.repository.IGPWO;

import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.*;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ucl.poms.repository.model.*;

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.DomainImpl;
import be.ac.ulg.montefiore.run.totem.domain.exception.*;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.TrafficMatrixImpl;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

import org.apache.log4j.Logger;

import java.util.*;

/*
 * Changes:
 * --------
 *  - 13-May-2005: some parameters are made optional (SCE)
 *  - 29-Nov-2005: Added the possibility to obtain the algorithm parameters (getStartAlgoParameters()). (GMO)
 *  - 08-Dec-2005: Implement new getRunningParameters() from the TotemAlgorithm interface. (GMO)
 *  - 05-Mar-2007: Move library loading in start(.) and throw exception (GMO)
 *  - 14-Jun-2007: Add ability to work with neighbours and virtual links (GMO)
 *  - 17-Sep-2007: set DB valid when starting/stopping the algorithm (GMO)
 */

/**
 * This class implements the integration of IGPWO (UCL/IAG/POMS).
 * IGPWO is the optimizer for IGP link weights.
 *
 * <p>Creation date: 10-Dec-2004
 *
 * @author Selin Cerav-Erbas (cerav@poms.ucl.ac.be)
 * @author Hakan Umit (umit@poms.ucl.ac.be)
 */

public class IGPWO implements IGPWeightOptimization, DomainSyncAlgorithm {

	private static Logger logger = Logger.getLogger(IGPWO.class);
	private static Domain domain = null;
    private static Domain intraDomain = null;


	/**
	 * Calculates optimal weights for IGP weights given a traffic demand matrix.
	 */
	public native static void jniinitDBGraph(int num_nodes, int num_arcs);
	public native static void jniinitDBTrafficMatrix(int num_nodes, int num_tm);
	public native static double[] jnicalculateWeights() throws Exception;
	public native static double[] jnicalculateWeightsParameters(int num_iteration, int w_max, int random_initial, int seed, double min_samp_rate, double max_samp_rate, double init_samp_rate, double[] initialWeights) throws Exception;
	public native static void jniaddArc(int index, int new_id, int new_headnode, int new_tailnode, double new_capacity) throws AddDBException;
	public native static void jniaddNode(int index, int new_id) throws AddDBException;
	public native static void jniaddCommodity(int matrix_index, int new_dstnode, int new_srcnode, double demandValue) throws AddDBException;
	public native static void jnikillIGPWO() throws Exception;

	//public native static void jnideleteArc(int new_id);
	//public native static void jnideleteNode(int new_id);
	//public native static void jnideleteCommodity(int new_dstnode, int new_srcnode,double com_matrix);

    private static HashMap runningParams = null;

    private static final ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();

    private static boolean DBValid = true;

    static {
    	try {
    		params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
    	} catch (AlgorithmParameterException e) {
    		e.printStackTrace();
    	}
    }
    
	public void start(HashMap params) throws AlgorithmInitialisationException {

        try {
            System.loadLibrary("igpwo");
        } catch (UnsatisfiedLinkError e) {
            throw new LibraryInitialisationException("Cannot load library igpwo.");
        }

        runningParams = params;
		try{
			if(params.get("ASID") != null) {
				domain = InterDomainManager.getInstance().getDomain(Integer.parseInt(((String) params.get("ASID"))));
			}
			else {
				domain = InterDomainManager.getInstance().getDefaultDomain();
			}
		}catch(InvalidDomainException e){
			e.printStackTrace();
		}

        intraDomain = buildIntraDomain(domain);

        int nbNodes = intraDomain.getUpNodes().size();
        int nbLinks = intraDomain.getUpLinks().size();

		logger.info("number of nodes " + nbNodes + ", number of links " + nbLinks);

		jniinitDBGraph(nbNodes, nbLinks);

        DomainConvertor intraConvertor = intraDomain.getConvertor();

		// Adding nodes
        int index = 0;
		for (Node n : intraDomain.getUpNodes()) {
            try {
		        int intnodeId = intraConvertor.getNodeId(n.getId());
				logger.info("Node index: " + index + " Node ID: " + intnodeId);
				jniaddNode(index, intnodeId);
			}
			catch(AddDBException e){
			    e.printStackTrace();
			} catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
            index++;
		}

		// Adding links
        int index2 = 0;
        for (Link link : intraDomain.getUpLinks()) {
            try {
                int intlinkId = intraConvertor.getLinkId(link.getId());

                String headnodeId = link.getDstNode().getId();
                int intheadnodeId = intraConvertor.getNodeId(headnodeId);
                String tailnodeId = link.getSrcNode().getId();
                int inttailnodeId = intraConvertor.getNodeId(tailnodeId);

                float linkCapacity = link.getBandwidth();

                logger.info("Link index: " + index2 + " Link ID: " + intlinkId);
                logger.info("Tail: " + inttailnodeId + " Head " + intheadnodeId);
                jniaddArc(index2, intlinkId, intheadnodeId, inttailnodeId, linkCapacity);//load needs to be initialized
            } catch (AddDBException e) {
                e.printStackTrace();
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
            index2++;
        }

        DBValid = true;
	}

	public TotemActionList calculateWeights(int ASID, int[] TMID) throws Exception{
        logger.debug("IGPWO: domainASID:" + domain.getASID() + ", given ASID:" + ASID);

        if (domain.getASID() != ASID) {
            throw new Exception("ERROR: Not the good Topology loaded into IGPWO database");
        }

        DomainConvertor interConvertor = domain.getConvertor();
        DomainConvertor intraConvertor = intraDomain.getConvertor();
        TotemActionList actionList = new TotemActionList();

        int nbNodes = intraDomain.getUpNodes().size();

        jniinitDBTrafficMatrix(nbNodes, TMID.length);

        for (int i = 0; i < TMID.length; i++) {
            TrafficMatrix trafficMatrix = null;
            TrafficMatrix intraMatrix = null;
            try {
                trafficMatrix = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(), TMID[i]);
            } catch (InvalidTrafficMatrixException e) {
                e.printStackTrace();
            }

            if (domain != intraDomain) {
                intraMatrix = buildIntraMatrix(trafficMatrix);
            } else {
                intraMatrix = trafficMatrix;
            }

            for (Node node : intraDomain.getUpNodes()) {

                for (Node node2 : intraDomain.getUpNodes()) {
                    if (!node.getId().equals(node2.getId())) {
                        try {
                            double demandValue = intraMatrix.get(node.getId(), node2.getId());
                            jniaddCommodity(i, intraConvertor.getNodeId(node2.getId()), intraConvertor.getNodeId(node.getId()), demandValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        double[] intraLinkWeights;
        try {
            intraLinkWeights = jnicalculateWeights();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        double[] linkWeights = new double[domain.getNbLinks()];


        /*
        for (int i = 0; i < linkWeights.length; i++) {
            try {
                String linkId = convertor.getLinkId(i);
                try {
                    // link is in the intra topology
                    int intraIntId = intraConvertor.getLinkId(linkId);
                    linkWeights[i] = intraLinkWeights[intraIntId];
                } catch (LinkNotFoundException e) {
                    // link is not in the intra topology
                    linkWeights[i] = domain.getLink(linkId).getTEMetric();
                }
            } catch (LinkNotFoundException e) {
                // int ids are not consecutive. i does not correspond to a link
                linkWeights[i] = -1;
            }
        }*/

        Arrays.fill(linkWeights, 0);
        for (Link link : intraDomain.getAllLinks()) {
            linkWeights[interConvertor.getLinkId(link.getId())] = intraLinkWeights[intraConvertor.getLinkId(link.getId())];
        }

        TotemAction updateIGPWeights = new UpdateIGPWeightsAction(domain, linkWeights);
        actionList.add(updateIGPWeights);

        return actionList;
	}


	public void stop() {
        runningParams = null;
        DBValid = false;
		try{
			jnikillIGPWO();
		}
		catch(Exception e){
			e.printStackTrace();
			logger.warn("IGPWO failed to stop");

		}
		logger.info("IGPWO algorithm has finished its task");


	}

    public TotemActionList calculateWeightsParameters(int ASID, int[] TMID, int num_iters, int w_max) throws Exception {
        return this.calculateWeightsParameters(ASID, TMID, num_iters, w_max, true, 0, 0.01, 0.4, 0.2);
    }

    public TotemActionList calculateWeightsParameters(int ASID, int[] TMID, int num_iters, int w_max, boolean random, int seed, double min_samp_rate, double max_samp_rate, double init_samp_rate) throws Exception {
        logger.debug("IGPWO: domainASID:" + domain.getASID() + ", given ASID:" + ASID);

        if (domain.getASID() != ASID) {
            throw new Exception("ERROR: Not the good Topology loaded into IGPWO database");
        }

        DomainConvertor interConvertor = domain.getConvertor();
        DomainConvertor intraConvertor = intraDomain.getConvertor();
        TotemActionList actionList = new TotemActionList();

        int nbNodes = intraDomain.getUpNodes().size();
        int nbLinks = intraDomain.getUpLinks().size();

        jniinitDBTrafficMatrix(nbNodes, TMID.length);

        for (int i = 0; i < TMID.length; i++) {
            TrafficMatrix trafficMatrix = null;
            TrafficMatrix intraMatrix = null;
            try {
                trafficMatrix = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(), TMID[i]);
            } catch (InvalidTrafficMatrixException e) {
                e.printStackTrace();
            }

            if (domain != intraDomain) {
                intraMatrix = buildIntraMatrix(trafficMatrix);
            } else {
                intraMatrix = trafficMatrix;
            }

            for (Node node : intraDomain.getUpNodes()) {

                for (Node node2 : intraDomain.getUpNodes()) {
                    if (!node.getId().equals(node2.getId())) {
                        try {
                            double demandValue = intraMatrix.get(node.getId(), node2.getId());
                            jniaddCommodity(i, intraConvertor.getNodeId(node2.getId()), intraConvertor.getNodeId(node.getId()), demandValue);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        int random_initial = 1;

        double[] initialWeights = new double[nbLinks];


        if (random == false) {

            random_initial = 0;

            for (Link link : intraDomain.getUpLinks()) {
                double linkWeight = link.getTEMetric();
                initialWeights[intraConvertor.getLinkId(link.getId())] = linkWeight;
            }
        }

        double[] intraLinkWeights;
        try {
            intraLinkWeights = jnicalculateWeightsParameters(num_iters, w_max, random_initial, seed, min_samp_rate, max_samp_rate, init_samp_rate, initialWeights);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        double[] linkWeights = new double[domain.getNbLinks()];


        /*
        for (int i = 0; i < linkWeights.length; i++) {
            try {
                String linkId = convertor.getLinkId(i);
                try {
                    // link is in the intra topology
                    int intraIntId = intraConvertor.getLinkId(linkId);
                    linkWeights[i] = intraLinkWeights[intraIntId];
                } catch (LinkNotFoundException e) {
                    // link is not in the intra topology
                    linkWeights[i] = domain.getLink(linkId).getTEMetric();
                }
            } catch (LinkNotFoundException e) {
                // int ids are not consecutive. i does not correspond to a link
                linkWeights[i] = -1;
            }
        }*/

        Arrays.fill(linkWeights, 0);
        for (Link link : intraDomain.getAllLinks()) {
            linkWeights[interConvertor.getLinkId(link.getId())] = intraLinkWeights[intraConvertor.getLinkId(link.getId())];
        }

        TotemAction updateIGPWeights = new UpdateIGPWeightsAction(domain, linkWeights);
        actionList.add(updateIGPWeights);

        return actionList;
    }

	public List<ParameterDescriptor> getStartAlgoParameters() {
		return (List<ParameterDescriptor>) params.clone();
	}

    public HashMap getRunningParameters() {
        return (runningParams == null) ? null : (HashMap)runningParams.clone();
    }


    public boolean isDBValid() {
        return DBValid;
    }

    public void invalidateDB() {
        DBValid = false;
    }

    public void restart() {
        HashMap params = getRunningParameters();
        stop();
        try {
            start(params);
        } catch (AlgorithmInitialisationException e) {
            e.printStackTrace();
        }
    }

    private TrafficMatrix buildIntraMatrix(TrafficMatrix completeMatrix) throws Exception {
        logger.info("Building intra traffic matrix.");
        TrafficMatrix newMatrix;
        TrafficMatrix newMatrixInter;

        // Generate a new TM full of 0 values...
        newMatrix = new TrafficMatrixImpl(intraDomain);
        newMatrixInter = new TrafficMatrixImpl(domain);

        SPF algo;
        try {
            logger.info("CSPF not started.");
            algo = (SPF) RepositoryManager.getInstance().getAlgo("CSPF", domain.getASID());
        } catch (NoSuchAlgorithmException e) {
            logger.info("Starting CSPF.");
            RepositoryManager.getInstance().startAlgo("CSPF", null, domain.getASID());
        }

        // Create the intraTM
        for (Node node : domain.getUpNodes()){
            if ((node.getNodeType() != Node.Type.NEIGH) && (node.getNodeType() != Node.Type.VIRTUAL)) {
                for (Node node2 : domain.getUpNodes()){
                    if (!node.getId().equals(node2.getId())){
                            if (node2.getNodeType() == Node.Type.NEIGH) {
                                List<Link> inLinks = node2.getInLink();
                                if (inLinks.size() != 1) {
                                    throw new Exception("Nodes NEIGH must have one and only one incoming link !");
                                }
                                Node egress = inLinks.get(0).getSrcNode();
                                newMatrix.set(node.getId(), egress.getId(), newMatrix.get(node.getId(), egress.getId()) + completeMatrix.get(node.getId(),node2.getId()));
                                newMatrixInter.set(node.getId(), egress.getId(), newMatrixInter.get(node.getId(), egress.getId()) + completeMatrix.get(node.getId(),node2.getId()));
                            } else if (node2.getNodeType() == Node.Type.VIRTUAL) {
                                List<Link> inLinks = node2.getInLink();
                                float minIgpCost = Float.MAX_VALUE;
                                Node egress = inLinks.get(0).getSrcNode();
                                int nbMinIgpCostNH = 0;
                                List<Node> egresses = new ArrayList<Node>();
                                for (int l=0; l < inLinks.size(); l++) {
                                    List<Link> newInLinks = inLinks.get(l).getSrcNode().getInLink();
                                    if (newInLinks.size() != 1) {
                                        throw new Exception("Nodes NEIGH must have one and only one incoming link !");
                                    }
                                    Node possibleEgress = newInLinks.get(0).getSrcNode();
                                    Node possibleNextHop = newInLinks.get(0).getDstNode();
                                    float igpcost = 0;
                                    if (node.getId().compareTo(possibleEgress.getId()) != 0) {
                                        List<Path> paths = domain.getSPFCache().getPath(node, possibleEgress, false);
                                        List<Link> linkPath = paths.get(0).getLinkPath();
                                        for (int m=0; m < linkPath.size(); m++) {
                                            igpcost += linkPath.get(m).getMetric();
                                        }
                                    }
                                    if (igpcost < minIgpCost) {
                                        egress = possibleNextHop;
                                        minIgpCost = igpcost;
                                        nbMinIgpCostNH = 1;
                                        egresses.clear();
                                        egresses.add(possibleNextHop);
                                    } else if (igpcost == minIgpCost) {
                                        nbMinIgpCostNH++;
                                        egresses.add(possibleNextHop);
                                    }
                                }
                                if (nbMinIgpCostNH == 1) {
                                    newMatrixInter.set(node.getId(), egress.getId(), newMatrixInter.get(node.getId(), egress.getId()) + completeMatrix.get(node.getId(),node2.getId()));
                                    egress = egress.getInLink().get(0).getSrcNode();
                                    newMatrix.set(node.getId(), egress.getId(), newMatrix.get(node.getId(), egress.getId()) + completeMatrix.get(node.getId(),node2.getId()));
                                } else {
                                    for (int l = 0; l < egresses.size(); l++) {
                                        egress = egresses.get(l);
                                        newMatrixInter.set(node.getId(), egress.getId(), newMatrixInter.get(node.getId(), egress.getId()) + (completeMatrix.get(node.getId(),node2.getId()) / nbMinIgpCostNH));
                                        egress = egress.getInLink().get(0).getSrcNode();
                                        newMatrix.set(node.getId(), egress.getId(), newMatrix.get(node.getId(), egress.getId()) + (completeMatrix.get(node.getId(),node2.getId()) / nbMinIgpCostNH));
                                    }
                                }
                            } else {
                                newMatrix.set(node.getId(), node2.getId(), newMatrix.get(node.getId(), node2.getId()) + completeMatrix.get(node.getId(),node2.getId()));
                                newMatrixInter.set(node.getId(), node2.getId(), newMatrixInter.get(node.getId(), node2.getId()) + completeMatrix.get(node.getId(),node2.getId()));
                            }
                        }
                    }
                }
            }

        int tmId = TrafficMatrixManager.getInstance().generateTMID(domain.getASID());
        try {
            TrafficMatrixManager.getInstance().addTrafficMatrix(newMatrixInter, tmId);
            logger.info("A new traffic matrix used for IGP optimization was created as matrix " + tmId);
        } catch (TrafficMatrixAlreadyExistException e) {
            e.printStackTrace();
        } catch (InvalidDomainException e) {
            e.printStackTrace();
        } catch (TrafficMatrixIdException e) {
            e.printStackTrace();
        }

        return newMatrix;
    }

    private Domain buildIntraDomain(Domain completeDomain) {

        boolean containVirtualNodes = false;
        for (Node n : completeDomain.getAllNodes()) {
            if (n.getNodeType() == Node.Type.VIRTUAL) {
                containVirtualNodes = true;
                break;
            }
        }
        if (!containVirtualNodes) {
            return completeDomain;
        }

        logger.info("Building intra topology.");
        Domain newDomain = new DomainImpl(completeDomain.getASID());

        for (Node n : completeDomain.getUpNodes()) {
            if (n.getNodeType() != Node.Type.NEIGH && n.getNodeType() != Node.Type.VIRTUAL) {
                try {
                    newDomain.addNode(n);
                } catch (NodeAlreadyExistException e) {
                    e.printStackTrace();
                }
            }
        }

        for (Link l : completeDomain.getUpLinks()) {
            if (l.getLinkType() == Link.Type.INTRA) {
                try {
                    newDomain.addLink(l);
                } catch (LinkAlreadyExistException e) {
                    e.printStackTrace();
                } catch (NodeNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return newDomain;
    }
}

