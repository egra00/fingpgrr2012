package uy.edu.fing.repository.rrloc.algorithms.zhang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.BindAlgorithm;
import uy.edu.fing.repository.rrloc.iAlgorithm.ManagerRRLocAlgorithm;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpNeighborImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpRouterImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.DomainImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory;
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

	@Override
	public void dumpResultInDomain(Object algorithmResult) throws Exception {
		List<iBGPSession> iBGPTopology = (List<iBGPSession>)algorithmResult;
		
		ManagerRRLocAlgorithm.getInstance().lock(domain.getASID());
		
    	ObjectFactory factory = new ObjectFactory();
    	
		// Se elimina toda posible configuracion previa
		((DomainImpl)domain).removeBgpRouters();
		
		// Todos los routers tendrán sesiones bgp
		for (Node router : domain.getAllNodes()) {
			BgpRouter bgpRouter = factory.createBgpRouter();
	        bgpRouter.setId(router.getId());
	        bgpRouter.setRid(router.getRid());
	        domain.addBgpRouter((BgpRouterImpl)bgpRouter);
		}
		
		// Creo las sesiones
		for (iBGPSession session : iBGPTopology) {
			
			BgpRouterImpl router1 = (BgpRouterImpl)domain.getBgpRouter(session.getIdLink1());
			BgpRouterImpl router2 = (BgpRouterImpl)domain.getBgpRouter(session.getIdLink2());
			
			// El router2, el destino, sera reflector en caso que router1 sea su cliente
			router2.setReflector(
					router2.isReflector() ||
					session.getSessionType().equals(iBGPSessionType.client));
			
			BgpNeighbor bgpNeighbor = factory.createBgpNeighbor();
			bgpNeighbor.setIp(router2.getRid());
			bgpNeighbor.setAs(domain.getASID());
			if (router1.getNeighbors() == null) {
				router1.setNeighbors(factory.createBgpRouterNeighborsType());
			}
			router1.getNeighbors().getNeighbor().add((be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor)bgpNeighbor);
			
			// El router1, el origen, sera cliente en caso de tener una session de tipo client.
			((BgpNeighborImpl)bgpNeighbor).setReflectorClient(
					((BgpNeighborImpl)bgpNeighbor).isReflectorClient() ||
					session.getSessionType().equals(iBGPSessionType.client));
			
			bgpNeighbor = factory.createBgpNeighbor();
			bgpNeighbor.setIp(router1.getRid());
			bgpNeighbor.setAs(domain.getASID());
			if (router2.getNeighbors() == null) {
				router2.setNeighbors(factory.createBgpRouterNeighborsType());
			}
			router2.getNeighbors().getNeighbor().add((be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor)bgpNeighbor);
		}
        
        ManagerRRLocAlgorithm.getInstance().unlock(domain.getASID());
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
