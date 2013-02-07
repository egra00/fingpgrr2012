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
	
	private int _MAX_ITER = 25000;
	private double _ALPHA = 0.035;
	private double _BETA = 0.014;
	private double _GAMA = 0.15;
	
	private final int _N_GEN = 50;
	private final int _SIZE_P = 60;
	private final int _SIZE_OF = 100;
	private final double _PMUT = 0.01;
	private final double _PCROSS = 0.1;
	private final int _NB_RUN = 15;
	
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
	public Object getAlgorithmParams(HashMap params) {
        Integer ASID;
        String SEPARATOR;
    	Integer MAX_ITER;
    	Double ALPHA;
    	Double BETA;
    	Double GAMA; 
    	
    	Integer NB_RUN;
    	Integer N_GEN;
    	Integer SIZE_P;
    	Integer SIZE_OF;
    	Double PMUT;
    	Double PCROSS;
    	
    	SEPARATOR = (String)params.get("separator");
        
        try {
        	ASID = Integer.parseInt((String)params.get("ASID"));
        }
        catch (Exception e) {
        	ASID = null;
        }
        
        try {
        	MAX_ITER = Integer.parseInt((String)params.get("max_iter"));
        }
        catch (Exception e) {
        	MAX_ITER = _MAX_ITER;
        }
        
        try {
        	ALPHA = Double.parseDouble((String)params.get("alpha")); 
        }
        catch (Exception e) {
        	ALPHA = _ALPHA;
        }
        
        try {
        	BETA = Double.parseDouble((String)params.get("beta")); 
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
        
        try {
        	NB_RUN = Integer.parseInt((String)params.get("nb_run"));
        }
        catch (Exception e) {
        	NB_RUN = _NB_RUN;
        }
        
        try {
        	N_GEN = Integer.parseInt((String)params.get("n_gen"));
        }
        catch (Exception e) {
        	N_GEN = _N_GEN;
        }
        
        try {
        	SIZE_P = Integer.parseInt((String)params.get("sizeP"));
        }
        catch (Exception e) {
        	SIZE_P = _SIZE_P;
        }
        
        try {
        	SIZE_OF = Integer.parseInt((String)params.get("sizeOf"));
        }
        catch (Exception e) {
        	SIZE_OF = _SIZE_OF;
        }
        
        try {
        	PMUT = Double.parseDouble((String)params.get("pmut")); 
        }
        catch (Exception e) {
        	PMUT = _PMUT;
        }
        
        try {
        	PCROSS = Double.parseDouble((String)params.get("pcross")); 
        }
        catch (Exception e) {
        	PCROSS = _PCROSS;
        }
        
        System.out.println("ASID: " + ASID);
        System.out.println("MAX_ITER: " + MAX_ITER);
        System.out.println("ALPHA: " + ALPHA);
        System.out.println("BETA: " + BETA);
        System.out.println("GAMA: " + GAMA);
        System.out.println("NB_RUN: " + NB_RUN);
        System.out.println("N_GEN: " + N_GEN);
        System.out.println("SIZE_P: " + SIZE_P);
        System.out.println("SIZE_OF: " + SIZE_OF);
        System.out.println("PMUT: " + PMUT);
        System.out.println("PCROSS: " + PCROSS);
      
        if(ASID == null) {
        	domain = InterDomainManager.getInstance().getDefaultDomain();
        	if(domain == null){
	        	logger.error("There is no default domain");
	            return null;
        	}
        } else {
            try {
                domain = InterDomainManager.getInstance().getDomain(ASID);
            } catch(InvalidDomainException e) {
                logger.error("Cannot load domain " + ASID);
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
		
		Object[] returnedParams = new Object[12];
		returnedParams[0] = jIGPTopology;
		returnedParams[1] = SEPARATOR;
		returnedParams[2] = MAX_ITER;
		returnedParams[3] = ALPHA;
		returnedParams[4] = BETA;
		returnedParams[5] = GAMA;
		returnedParams[6] = NB_RUN;
		returnedParams[7] = N_GEN;
		returnedParams[8] = SIZE_P;
		returnedParams[9] = SIZE_OF;
		returnedParams[10] = PMUT;
		returnedParams[11] = PCROSS;

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
