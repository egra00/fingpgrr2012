package uy.edu.fing.repository.rrloc.algorithms.zhang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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

public class Zhang  extends BindAlgorithm {
	private int level_one;
	private int level_two;
	private int pops;
	
	public Zhang() {
		logger = Logger.getLogger(Zhang.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new ZhangAlgorithm();
		name = "Zhang";
		thread = new Thread(this, name);
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
			params.add(new ParameterDescriptor("RRs Level1", "Number of routers reflector in Level 1 (by default is two).", Integer.class, null));
			params.add(new ParameterDescriptor("RRs Level2", "Number of routers reflector in Level 2 (by default is two).", Integer.class, null));
			params.add(new ParameterDescriptor("Amount PoPs", "Amount of PoPs (by default is one).", Integer.class, null));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
		
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAlgorithmParams(HashMap params) 
	{
        String asId = (String) params.get("ASID");
        String level_1 = (String) params.get("RRs Level1");
        String level_2 = (String) params.get("RRs Level2");
        String popsS = (String) params.get("Amount PoPs");
        
        level_one = 2;
        level_two = 2;
        pops = 1; 
        
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
        if(level_1 != null && !level_1.isEmpty() && (Integer.parseInt(level_1)) < domain.getNbNodes()) level_one = Integer.parseInt(level_1);        
        if(level_2 != null && !level_2.isEmpty() && (Integer.parseInt(level_2)) < domain.getNbNodes()) level_two = Integer.parseInt(level_2);
        
        if(level_one + level_two > domain.getNbNodes())
        {
        	level_one = 2;
        	level_two = 2;
        }
		
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
	
		ZhangAlgorithm.Params par = new ZhangAlgorithm.Params();
		par.nbr_level1 = level_one;
		par.nbr_level2 = level_two;
		par.pops = pops;
		par.graph = jIGPTopology;
		
		return par;
	}
	
	@Override
	public Object initAlgorithmResult() {
		return new ArrayList<iBGPSession>();
	}

}
