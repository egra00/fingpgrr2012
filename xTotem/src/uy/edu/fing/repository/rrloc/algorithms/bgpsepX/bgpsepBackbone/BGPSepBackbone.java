package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepBackbone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.iAlgorithm.BindAlgorithm;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;

public class BGPSepBackbone extends BindAlgorithm {
	private int pops;
	
	public BGPSepBackbone() {
		logger = Logger.getLogger(BGPSepBackbone.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new BGPSepBackboneAlgorithm();
		name = "BGPSepBackbone";
		thread = new Thread(this, name);
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
			params.add(new ParameterDescriptor("Amount PoPs", "Amount of PoPs (by default is one).", Integer.class, null));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAlgorithmParams(HashMap params) 
	{
		String asId = (String) params.get("ASID");
        String popsS = (String) params.get("Amount PoPs");
        pops = 5;
        
        if(asId == null || asId.isEmpty()) {
        	domain = InterDomainManager.getInstance().getDefaultDomain();
        	if(domain == null){
	        	logger.error("There is no default domain");
	            return null;
        	}
        } else {
            try {
                domain = InterDomainManager.getInstance().getDomain(Integer.parseInt(asId));
            } catch(InvalidDomainException e) {
                logger.error("Cannot load domain " + asId);
                return null;
            }
        }
		
        if(popsS != null && !popsS.isEmpty() && Integer.parseInt(popsS) <= domain.getNbNodes()) pops = Integer.parseInt(popsS);
        
		// Topologia IGP representada en un grafo jung 
		Graph<Node, Link> jIGPTopology = new UndirectedSparseMultigraph<Node, Link>();
		
		// Cargo un nodo Jung con los datos necesarios para realizar el algoritmo
		Operations.addAllVertices(jIGPTopology, (Set<Node>)(new HashSet<Node>(domain.getAllNodes())));
		for (Link link : domain.getAllLinks()) {
			try {
				// Elimino la direccionalidad del grafo
				if (jIGPTopology.findEdge(link.getDstNode(), link.getSrcNode()) == null) {
					jIGPTopology.addEdge(link, link.getSrcNode(), link.getDstNode());
				}
			} catch (NodeNotFoundException e) {
				logger.error("Parsing Totem domain to Jung graph");
				e.printStackTrace();
			}
		}
		
		BGPSepBackboneAlgorithm.Params param = new BGPSepBackboneAlgorithm.Params();
		param.graph = jIGPTopology;
		param.pops = pops;
		
		return param;
	}
	
	@Override
	public Object initAlgorithmResult() {
		return new ArrayList<iBGPSession>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void log(Object algorithmResult) {
		List<iBGPSession> iBGPTopology = (List<iBGPSession>)algorithmResult;
		
		logger.debug("iBGP sessions ("+iBGPTopology.size()+")");
		for (iBGPSession session: iBGPTopology) {
			logger.debug(session.getIdLink1() + " - " + session.getIdLink2() + " -> " + session.getSessionType());
		}
		
	}
}
