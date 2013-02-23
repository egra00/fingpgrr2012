package uy.edu.fing.repository.rrloc.algorithms.optimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import be.ac.ulg.montefiore.run.totem.domain.model.impl.LinkImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.NodeType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.NodeImpl;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import edu.uci.ics.jung2.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung2.graph.Graph;

public class Optimal extends BindAlgorithm {

	public Optimal() {
		logger = Logger.getLogger(Optimal.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new OptimalAlgorithm();
		name = "Optimal";
		thread = new Thread(this, name);
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
			//params.add(new ParameterDescriptor("FILEPATH", "Relative file path.", String.class, "conf.cnf"));
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
		
		List<Object> lstParams = new ArrayList<Object>();
		// Topología IGP representada en un grafo jung 
		Graph<Node, Link> jIGPTopology = new DirectedSparseMultigraph<Node, Link>();
		
		// Cargo un nodo Jung con los datos necesarios para realizar el algoritmo
		Operations.addAllVertices(jIGPTopology, (Set<Node>)(new HashSet<Node>(domain.getAllNodes())));
		for (Link link : domain.getAllLinks()) {
			try {
				if (jIGPTopology.findEdge(link.getSrcNode(), link.getDstNode()) == null) {
					jIGPTopology.addEdge(link, link.getSrcNode(), link.getDstNode());
				}
				// Elimino la direccionalidad del grafo
				if (jIGPTopology.findEdge(link.getDstNode(), link.getSrcNode()) == null) {
					Link l = new LinkImpl(domain,link.getDstNode().getId()+"_"+link.getSrcNode().getId(),link.getDstNode().getId(),link.getSrcNode().getId(),link.getBandwidth());
					((LinkImpl)l).getIgpLink().getStatic().setMetric(link.getMetric());
					jIGPTopology.addEdge(l, l.getSrcNode(), l.getDstNode());
				}
			} catch (NodeNotFoundException e) {
				logger.error("Parsing Totem domain to Jung graph");
				e.printStackTrace();
			}
		}
				
		lstParams.add(jIGPTopology);
		
		//Add BGP routers set
//		List<be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter> lstBGPRouters = domain.getAllBgpRouters();
//		lstParams.add(lstBGPRouters);
		List<be.ac.ulg.montefiore.run.totem.domain.model.Node> lstBGPRouters = domain.getAllNodes();
		lstParams.add(lstBGPRouters);
		
		//Add Next-hop set
		List<Node> lstNextHops = new ArrayList<Node>();
		Iterator<Node> it = domain.getAllNodes().iterator();
		while(it.hasNext()){
			Node n = it.next();
			if(((NodeImpl)n).getType()==NodeType.EDGE){
				lstNextHops.add(n);
			}
		}
		lstParams.add(lstNextHops);
		lstParams.add(domain);
		
		return lstParams;
	}

	@Override
	public Object initAlgorithmResult() {
		return new ArrayList<iBGPSession>();
	}
	
}
