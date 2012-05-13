package be.ac.ulg.montefiore.run.totem.repository.rrloc.algorithms.bgpsep;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpNeighborImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpRouterImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.DomainImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.algorithms.xAlgorithm.BindAlgorithm;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;

@SuppressWarnings("unchecked")
public class BGPSep extends BindAlgorithm {
	private Logger my_logger;
	
	
	public BGPSep() {
		my_logger = Logger.getLogger(BGPSep.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new BGPSepAlgorithm();
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public Object getAlgorithmParams(Domain domain) {
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
				my_logger.error("Parsing Totem domain to Jung graph");
				e.printStackTrace();
			}
		}
				
		return jIGPTopology;
	}
	
	@Override
	public Object initAlgorithmResult() {
		return new ArrayList<iBGPSession>();
	}

	@Override
	public void dumpResultInDomain(Domain domain, Object algorithmResult) throws Exception {
		List<iBGPSession> iBGPTopology = (List<iBGPSession>)algorithmResult;
		ObjectFactory factory = new ObjectFactory();
		
		// Se elimina toda posible configuración previa
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
			
			// El router2, el destino, será reflector en caso que router1 sea su cliente
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
			
			// El router1, el origen, será cliente en caso de tener una session de tipo client.
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
	}

	@Override
	public void log(Object algorithmResult) {
		List<iBGPSession> iBGPTopology = (List<iBGPSession>)algorithmResult;
		
		my_logger.debug("iBGP sessions ("+iBGPTopology.size()+")");
		for (iBGPSession session: iBGPTopology) {
			my_logger.debug(session.getIdLink1() + " - " + session.getIdLink2() + " -> " + session.getSessionType());
		}
		
	}

}
