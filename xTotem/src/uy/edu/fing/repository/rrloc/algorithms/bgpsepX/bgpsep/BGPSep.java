package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsep;

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

@SuppressWarnings("unchecked")
public class BGPSep extends BindAlgorithm {
	
	private Integer _MAX_ITER = 25000;
	private Double _ALPHA = 0.035;
	private Double _BETA = 0.014;
	private Double _GAMA = 0.15;
	
	private Integer MAX_ITER;
	private Double ALPHA;
	private Double BETA;
	private Double GAMA;
	
	public BGPSep() {
		logger = Logger.getLogger(BGPSep.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new BGPSepAlgorithm();
		name = "BGPSep";
		thread = new Thread(this, name);
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
			params.add(new ParameterDescriptor("separator", "Methauristic utiliced for graph separator", String.class, "GRASP", new String[]{"GRASP", "EA"}));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public Object getAlgorithmParams(HashMap params) 
	{
        Integer asId;
        
        try {
        	asId = Integer.valueOf((String)params.get("ASID"));
        }
        catch (Exception e) {
        	asId = null;
        }
        
        try {
        	MAX_ITER = Integer.valueOf((String)params.get("max_iter"));
        }
        catch (Exception e) {
        	MAX_ITER = _MAX_ITER;
        }
        
        try {
        	ALPHA = Double.valueOf((String)params.get("alpha")); 
        }
        catch (Exception e) {
        	ALPHA = _ALPHA;
        }
        
        try {
        	BETA = Double.valueOf((String)params.get("beta")); 
        }
        catch (Exception e) {
        	BETA = _BETA;
        }
        
        try {
        	GAMA = Double.parseDouble((String)params.get("gama")); 
        }
        catch (Exception e) {
        	GAMA = _GAMA;
        }
        
        System.out.println("ASID: " + asId);
        System.out.println("MAX_ITER: " + MAX_ITER);
        System.out.println("ALPHA: " + ALPHA);
        System.out.println("BETA: " + BETA);
        System.out.println("GAMA: " + GAMA);
      
        if(asId == null) {
        	domain = InterDomainManager.getInstance().getDefaultDomain();
        	if(domain == null){
	        	logger.error("There is no default domain");
	            return null;
        	}
        } else {
            try {
                domain = InterDomainManager.getInstance().getDomain(asId);
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
		
		Object[] returnedParams = new Object[5];
		returnedParams[0] = jIGPTopology;
		returnedParams[1] = MAX_ITER;
		returnedParams[2] = ALPHA;
		returnedParams[3] = BETA;
		returnedParams[4] = GAMA;

		return returnedParams;
	}
	
	@Override
	public Object initAlgorithmResult() {
		return new ArrayList<iBGPSession>();
	}

	@Override
	public void log(Object algorithmResult) {
		List<iBGPSession> iBGPTopology = (List<iBGPSession>)algorithmResult;
		
		logger.debug("iBGP sessions ("+iBGPTopology.size()+")");
		for (iBGPSession session: iBGPTopology) {
			logger.debug(session.getIdLink1() + " - " + session.getIdLink2() + " -> " + session.getSessionType());
		}
		
	}

}
