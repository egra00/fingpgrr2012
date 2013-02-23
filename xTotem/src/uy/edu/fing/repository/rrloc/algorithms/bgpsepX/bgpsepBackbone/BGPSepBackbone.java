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

	private final int _POPS = 5;
	
	private final int _MAX_ITER = 25000;
	private final double _ALPHA = 0.035;
	private final double _BETA = 0.014;
	private final double _GAMA = 0.15;
	
	private final int _KM_N_GEN = 50;
	private final int _KM_SIZE_P = 60;
	private final int _KM_SIZE_OF = 100;
	private final double _KM_PMUT = 0.01;
	private final double _KM_PCROSS = 0.1;
	private final int _KM_NB_RUN = 15;
	
	private final int _N_GEN = 50;
	private final int _SIZE_P = 60;
	private final int _SIZE_OF = 100;
	private final double _PMUT = 0.01;
	private final double _PCROSS = 0.1;
	private final int _NB_RUN = 15;
	
	public BGPSepBackbone() {
		logger = Logger.getLogger(BGPSepBackbone.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new BGPSepBackboneAlgorithm();
		name = "BGPSepBackbone";
		thread = new Thread(this, name);
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
			params.add(new ParameterDescriptor("PoPs", "Amount of PoPs (by default is one).", Integer.class, null));
			params.add(new ParameterDescriptor("separator", "Methauristic utiliced for graph separator", String.class, "GRASP", new String[]{"GRASP", "EA"}));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAlgorithmParams(HashMap params)  {
        int POPS;
        
        Integer ASID;
        String SEPARATOR;
        
        Integer MAX_ITER;
    	Double ALPHA;
    	Double BETA;
    	Double GAMA;
    	
    	Integer KM_NB_RUN;
    	Integer KM_N_GEN;
    	Integer KM_SIZE_P;
    	Integer KM_SIZE_OF;
    	Double KM_PMUT;
    	Double KM_PCROSS;
    	
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
        	POPS = Integer.parseInt((String)params.get("PoPs"));
        }
        catch (Exception e) {
        	POPS = _POPS;
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
        	KM_NB_RUN = Integer.parseInt((String)params.get("km_nb_run"));
        }
        catch (Exception e) {
        	KM_NB_RUN = _KM_NB_RUN;
        }
        
        try {
        	KM_N_GEN = Integer.parseInt((String)params.get("km_n_gen"));
        }
        catch (Exception e) {
        	KM_N_GEN = _KM_N_GEN;
        }
        
        try {
        	KM_SIZE_P = Integer.parseInt((String)params.get("km_sizeP"));
        }
        catch (Exception e) {
        	KM_SIZE_P = _KM_SIZE_P;
        }
        
        try {
        	KM_SIZE_OF = Integer.parseInt((String)params.get("km_sizeOf"));
        }
        catch (Exception e) {
        	KM_SIZE_OF = _KM_SIZE_OF;
        }
        
        try {
        	KM_PMUT = Double.parseDouble((String)params.get("km_pmut")); 
        }
        catch (Exception e) {
        	KM_PMUT = _KM_PMUT;
        }
        
        try {
        	KM_PCROSS = Double.parseDouble((String)params.get("km_pcross")); 
        }
        catch (Exception e) {
        	KM_PCROSS = _KM_PCROSS;
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
        
      /*  System.out.println("ASID: " + ASID);
        System.out.println("MAX_ITER: " + MAX_ITER);
        System.out.println("ALPHA: " + ALPHA);
        System.out.println("BETA: " + BETA);
        System.out.println("GAMA: " + GAMA);
        System.out.println("KM_NB_RUN: " + KM_NB_RUN);
        System.out.println("KM_N_GEN: " + KM_N_GEN);
        System.out.println("KM_SIZE_P: " + KM_SIZE_P);
        System.out.println("KM_SIZE_OF: " + KM_SIZE_OF);
        System.out.println("KM_PMUT: " + KM_PMUT);
        System.out.println("KM_PCROSS: " + KM_PCROSS);
        System.out.println("NB_RUN: " + NB_RUN);
        System.out.println("N_GEN: " + N_GEN);
        System.out.println("SIZE_P: " + SIZE_P);
        System.out.println("SIZE_OF: " + SIZE_OF);
        System.out.println("PMUT: " + PMUT);
        System.out.println("PCROSS: " + PCROSS);*/
        
        if (ASID == null) {
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
		
		Object[] returnedParams = new Object[19];
		returnedParams[0] = jIGPTopology;
		returnedParams[1] = SEPARATOR;
		returnedParams[2] = POPS;
		returnedParams[3] = MAX_ITER;
		returnedParams[4] = ALPHA;
		returnedParams[5] = BETA;
		returnedParams[6] = GAMA;
		returnedParams[7] = KM_NB_RUN;
		returnedParams[8] = KM_N_GEN;
		returnedParams[9] = KM_SIZE_P;
		returnedParams[10] = KM_SIZE_OF;
		returnedParams[11] = KM_PMUT;
		returnedParams[12] = KM_PCROSS;
		returnedParams[13] = NB_RUN;
		returnedParams[14] = N_GEN;
		returnedParams[15] = SIZE_P;
		returnedParams[16] = SIZE_OF;
		returnedParams[17] = PMUT;
		returnedParams[18] = PCROSS;

		return returnedParams;
	}
	
	@Override
	public Object initAlgorithmResult() {
		return new ArrayList<iBGPSession>();
	}
}
