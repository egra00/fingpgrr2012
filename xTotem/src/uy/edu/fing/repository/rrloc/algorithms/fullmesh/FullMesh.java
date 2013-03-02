package uy.edu.fing.repository.rrloc.algorithms.fullmesh;

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

public class FullMesh extends BindAlgorithm {
	
	public FullMesh() {
		logger = Logger.getLogger(FullMesh.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new FullMeshAlgorithm();
		name = "FullMesh";
		thread = new Thread(this, name);
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAlgorithmParams(HashMap params) 
	{
        String asId = (String) params.get("ASID");
      
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
		
		
		// Topología IGP representada en un grafo jung 
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
				
		return jIGPTopology;
	}
	
	@Override
	public Object initAlgorithmResult() {
		return new ArrayList<iBGPSession>();
	}
}
