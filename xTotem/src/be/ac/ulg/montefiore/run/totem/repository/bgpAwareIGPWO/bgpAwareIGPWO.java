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
package be.ac.ulg.montefiore.run.totem.repository.bgpAwareIGPWO;

import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AddDBException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.LibraryInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ucl.poms.repository.model.*;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.DomainConvertor;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * - 05-Mar-2007 : Move library loading in start(.) and throw exception (GMO)
 * - 17-Sep-2007: set DB valid when starting/stopping the algorithm (GMO)
 */

/**
 * This class is a BGP-aware improvement of the integration of IGPWO (ULg / RUN).
 * IGPWO is the optimizer for IGP link weights (UCL / POMS).
 *
 * <p>Creation date: 06-Fev-2007
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */

public class bgpAwareIGPWO implements IGPWeightOptimization, DomainSyncAlgorithm {

	private static Logger logger = Logger.getLogger(bgpAwareIGPWO.class.getName());
	private static Domain domain = null;
	//private static TrafficMatrix trafficMatrix = null;

	/**
	 * Calculates optimal weights for IGP weights given a traffic demand matrix.
	 */
	public native static void jniinitDBGraph(int num_nodes, int num_arcs);
	public native static void jniinitDBTrafficMatrix(int num_nodes, int num_tm);

    // interDomainTE = 1 means interdomain links in the objective function
    // while interDomainTE = 0 means interdomain links not in the objective function
    public native static double[] jnicalculateWeights(int interDomainTE) throws Exception;

    // interDomainTE = 1 means interdomain links in the objective function
    // while interDomainTE = 0 means interdomain links not in the objective function
	public native static double[] jnicalculateWeightsParameters(int num_iteration, int w_max, int random_initial, int seed, double min_samp_rate, double max_samp_rate, double init_samp_rate, double[] initialWeights, int interDomainTE) throws Exception;

    // Link Type = 0 for intra, 1 for inter and 2 for virtual
    public native static void jniaddArc(int index, int new_id, int new_headnode, int new_tailnode, double new_capacity, int type) throws AddDBException;
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
		    System.loadLibrary("bgpawareigpwo");
        } catch (UnsatisfiedLinkError e) {
            throw new LibraryInitialisationException("Cannot load library bgpawareigpwo.");
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
		/*
			 try{
			 if(params.containsKey("TMID")) {
			 trafficMatrix = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(),Integer.parseInt(((String) params.get("TMID"))));
			 }
			 else {
			 trafficMatrix = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(domain.getASID());
			 }
			 }catch(InvalidTrafficMatrixException e){
			 e.printStackTrace();
			 }
			 */
		DomainConvertor convertor = domain.getConvertor();

		int nbLinks = domain.getUpLinks().size();
		int nbNodes = domain.getUpNodes().size();

		logger.info("number of nodes " + nbNodes + " number of links " + nbLinks);

		jniinitDBGraph(nbNodes, nbLinks);

		// Adding nodes
		int index=0;
		for (int i=0;i<nbNodes;i++){
			String nodeId = domain.getUpNodes().get(i).getId();

			try{
				int intnodeId = convertor.getNodeId(nodeId);
				logger.info("Valeur de l'index : " + index + "Valeur de l'ID du lien " + intnodeId);
				jniaddNode(index, intnodeId);
			}
			catch(AddDBException e){
				e.printStackTrace();
			}
			catch(NodeNotFoundException e){
				e.printStackTrace();
			}
			index++;
		}

		// Adding links
		int index2=0;
		for (int i=0;i<nbLinks;i++){

			Link link = domain.getUpLinks().get(i);

			try{
				int intlinkId = convertor.getLinkId(link.getId());

				String headnodeId= link.getDstNode().getId();
				int intheadnodeId= convertor.getNodeId(headnodeId);
				String tailnodeId = link.getSrcNode().getId();
				int inttailnodeId= convertor.getNodeId(tailnodeId);

				float linkCapacity = link.getBandwidth();

				logger.info("Valeur de l'index (link) : " + index2 + " Valeur de l'ID du lien " + intlinkId);
				logger.info("Tail: " + inttailnodeId + " Head " + intheadnodeId);

                int type = 0;
                if (link.getLinkType() == Link.Type.INTER) {
                    type = 1;
                } else if (link.getLinkType() == Link.Type.VIRTUAL) {
                    type = 2;
                }
				jniaddArc(index2, intlinkId, intheadnodeId, inttailnodeId, linkCapacity, type);//load needs to be initialized
			}

			catch(AddDBException e){
				e.printStackTrace();
			}
			catch(LinkNotFoundException e){
				e.printStackTrace();
			}
			catch(NodeNotFoundException e){
				e.printStackTrace();
			}
			index2++;
		}
        DBValid = true;
	}

	public TotemActionList calculateWeights(int ASID, int[] TMID) throws Exception{

		if (domain.getASID() != ASID){
			throw new Exception("ERROR: Not the good Topology loaded into IGPWO database");
		}

		int nbNodes = domain.getUpNodes().size();
		
		DomainConvertor convertor = domain.getConvertor();
		
		jniinitDBTrafficMatrix(nbNodes, TMID.length);
	
		TrafficMatrix trafficMatrix = null; 
		
		for (int i=0; i<TMID.length; i++) {
			try { 

				trafficMatrix = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(),TMID[i]);
			}
			catch(InvalidTrafficMatrixException e) {
				e.printStackTrace();
			}
			for (int j=0;j<nbNodes;j++){

				Node node = domain.getUpNodes().get(j);

				for (int k=0;k<nbNodes;k++){
					if (j!=k){

						Node node2 = domain.getUpNodes().get(k);

						try{

							double demandValue = trafficMatrix.get(node.getId(),node2.getId());

							jniaddCommodity(i,convertor.getNodeId(node2.getId()), convertor.getNodeId(node.getId()), demandValue);

						}

						catch(Exception e){
							e.printStackTrace();
						}

					}
				}

			}

		}
		TotemActionList actionList = new TotemActionList();
		
		try{

			double[] linkWeights = jnicalculateWeights(0);
		
			TotemAction updateIGPWeights = new UpdateIGPWeightsAction(domain,linkWeights);

			actionList.add(updateIGPWeights);

		}
		catch(Exception e){
			e.printStackTrace();
		}

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
        return this.calculateWeightsParameters(ASID, TMID, num_iters, w_max, true, 0, 0.01, 0.4, 0.2, true);
    }

	public TotemActionList calculateWeightsParameters(int ASID, int[] TMID, int num_iters, int w_max, boolean random, int seed, double min_samp_rate, double max_samp_rate, double init_samp_rate, boolean interDomainTE) throws Exception{

        //System.out.println("IGPWO: domainASID:" + domain.getASID() + ", given ASID:" + ASID);
        
		if (domain.getASID() != ASID){
			throw new Exception("ERROR: Not the good Topology loaded into IGPWO database");
		}

		int nbNodes = domain.getUpNodes().size();

		DomainConvertor convertor = domain.getConvertor();

		jniinitDBTrafficMatrix(nbNodes, TMID.length);

		TrafficMatrix trafficMatrix = null; 

		for (int i=0; i<TMID.length; i++) {
			try { 

				trafficMatrix = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(),TMID[i]);
			}
			catch(InvalidTrafficMatrixException e){
				e.printStackTrace();
			}

			for (int j=0;j<nbNodes;j++){

				Node node = domain.getUpNodes().get(j);

				for (int k=0;k<nbNodes;k++){
					if (j!=k){

						Node node2 = domain.getUpNodes().get(k);

						try{

							double demandValue = trafficMatrix.get(node.getId(),node2.getId());

							jniaddCommodity(i,convertor.getNodeId(node2.getId()), convertor.getNodeId(node.getId()), demandValue);

						}

						catch(Exception e){
							e.printStackTrace();
						}

					}
				}

			}

		}

		int random_initial = 1;

		int nbLinks = domain.getUpLinks().size();

		double[] initialWeights = new double[nbLinks];

		if (random == false) {

			random_initial = 0;

			for (Link link : domain.getUpLinks()) {

				double linkWeight = link.getTEMetric();
				initialWeights[convertor.getLinkId(link.getId())] = linkWeight;

			}

		}

		TotemActionList actionList = new TotemActionList();

		try{
            int intvalueInterdomainTE;
            if (interDomainTE) {
                intvalueInterdomainTE = 1;
            } else {
                intvalueInterdomainTE = 0;
            }

			double[] linkWeights = jnicalculateWeightsParameters(num_iters, w_max, random_initial, seed, min_samp_rate, max_samp_rate, init_samp_rate, initialWeights, intvalueInterdomainTE);
			TotemAction updateIGPWeights = new UpdateIGPWeightsAction(domain,linkWeights);

			actionList.add(updateIGPWeights);

		}
		catch(Exception e){
			e.printStackTrace();
		}

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
}

