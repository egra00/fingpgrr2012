package be.ac.ulg.montefiore.run.totem.repository.rrloc.algorithms.bgpsep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpNeighborImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpRouterImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.DomainImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.graphTools.GraphSeparator;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.graphTools.Separator;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;

@SuppressWarnings("unchecked")
public class BGPSepAlgorithm implements RRLocAlgorithm {
	
	private Domain domain;
	private Graph<Node, Link> jIGPTopology;
	private List<iBGPSession> iBGPTopology;
	private static Logger logger = Logger.getLogger(BGPSepAlgorithm.class);
	
	
	public void run(Graph<Node, Link> param, List<iBGPSession> result) {
		Graph<Node, Link> IGPTopology = param;
		List<iBGPSession> i = result;
		
		if (IGPTopology.getVertexCount() < 2) {
			//i = vacio
		}
		else if (IGPTopology.getVertexCount() == 2) {
			Node u = ((Node)IGPTopology.getVertices().toArray()[0]);
			Node v = ((Node)IGPTopology.getVertices().toArray()[1]);
			
			i.add(new iBGPSession(
					u.getId(), 
					v.getId(), 
					iBGPSessionType.peer));
		}
		else {
			GraphSeparator graphSeparator = Separator.graphSeparator(IGPTopology);
			
			//El conjunto de routes reflectors estará configurado Full Mesh
			for (Node u : graphSeparator.getSeparator()) {
				for (Node v : graphSeparator.getSeparator()) {
					if (u == v)
						continue;
					i.add(new iBGPSession(
							u.getId(),
							v.getId(),
							iBGPSessionType.peer));
					
				}
			}
			
			// Cada router en la componente debe ser un cliente de todos
			// los route reflectos
			for (Graph<Node, Link> g_i : graphSeparator.getComponents()) {
				for (Node u : g_i.getVertices()) {
					for (Node v : graphSeparator.getSeparator()) {
						i.add(new iBGPSession(
								u.getId(), 
								v.getId(), 
								iBGPSessionType.client));
					}
				}
				run(g_i, i);
			}
			
		}
	}
	
	
	@Override
	public void init() {
		iBGPTopology = new ArrayList<iBGPSession>();
		jIGPTopology = new UndirectedSparseMultigraph<Node, Link>();
		
	}


	@Override
	public void stop() {
		iBGPTopology = null;
		jIGPTopology = null;
		
	}


	@Override
	public void dump() {
		
		try {
			
			ObjectFactory factory = new ObjectFactory();
			
			// Se elimina toda posible configuración previ   Domain domain;a
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
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}


	@Override
	public void log() {
		
		logger.debug("iBGP sessions ("+iBGPTopology.size()+")");
		for (iBGPSession session: iBGPTopology) {
			logger.debug(session.getIdLink1() + " - " + session.getIdLink2() + " -> " + session.getSessionType());
		}
		
	}


	@Override
	public void run() {
		
		logger.debug("Starting BGPSep");
		run(jIGPTopology, iBGPTopology);
		logger.debug("End BGPSep");
		
	}


	@Override
	public void setParameters(HashMap params) {
		
        String asId = (String) params.get("ASID");
        if(asId == null) {
        	domain = InterDomainManager.getInstance().getDefaultDomain();
        	if(domain == null){
	        	logger.error("There is no default domain");
	            return;
        	}
        } else {
            try {
                domain = InterDomainManager.getInstance().getDomain(Integer.parseInt(asId));
            } catch(InvalidDomainException e) {
                logger.error("Cannot load domain " + asId);
                return;
            }
        }
        
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
		
	}	
}
