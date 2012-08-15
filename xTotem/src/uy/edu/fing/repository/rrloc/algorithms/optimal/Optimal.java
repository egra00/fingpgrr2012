package uy.edu.fing.repository.rrloc.algorithms.optimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.BindAlgorithm;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
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
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.NodeType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.NodeImpl;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;

public class Optimal extends BindAlgorithm {

	private Domain domain;
	
	public Optimal() {
		logger = Logger.getLogger(Optimal.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new OptimalAlgorithm();
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
			//params.add(new ParameterDescriptor("FILEPATH", "Relative file path.", String.class, "conf.cnf"));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dumpResultInDomain(Object algorithmResult) throws Exception {
		List<iBGPSession> iBGPTopology = (List<iBGPSession>)algorithmResult;
		ObjectFactory factory = new ObjectFactory();
		
		
        int n = JOptionPane.YES_OPTION;
        if (((DomainImpl)domain).getBgp() != null) 
        {
            n = JOptionPane.showConfirmDialog(MainWindow.getInstance(), "<html>BGP information already exists for that domain.<br>" +
                    " This action will remove all prior existing information. Would you like to continue ?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        }
        if (n == JOptionPane.YES_OPTION) 
        {
		
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
		
	}

	@Override
	public Object getAlgorithmParams(HashMap params) {
		List<Object> lstParams = new ArrayList<Object>();
		
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
				
		lstParams.add(jIGPTopology);
		
		//Add BGP routers set
		List<be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter> lstBGPRouters = domain.getAllBgpRouters();
		lstParams.add(lstBGPRouters);
//		List<be.ac.ulg.montefiore.run.totem.domain.model.Node> lstBGPRouters = domain.getAllNodes();
//		lstParams.add(lstBGPRouters);
		
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

	@Override
	public void log(Object algorithmResult) {
		List<iBGPSession> iBGPTopology = (List<iBGPSession>)algorithmResult;
		
		logger.debug("iBGP sessions ("+iBGPTopology.size()+")");
		for (iBGPSession session: iBGPTopology) {
			logger.debug(session.getIdLink1() + " - " + session.getIdLink2() + " -> " + session.getSessionType());
		}
	}
	
}
