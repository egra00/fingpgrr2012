package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepS;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepD.BGPSepDAlgorithm;
import uy.edu.fing.repository.rrloc.tools.graph.separator.Separator;
import uy.edu.fing.repository.rrloc.tools.graph.separator.model.GraphSeparator;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung2.graph.Graph;

@SuppressWarnings("unchecked")
public class BGPSepSAlgorithm extends BGPSepDAlgorithm {

	private DijkstraShortestPath<Node, Link> dijkstra;

	
	public Node getNode(Link link, Node node) { // Dado un link "link" y un nodo "node" doy el extremo del que no es "node"
		try {
			if (link.getDstNode() != node)
				return link.getDstNode();
			else
				return link.getSrcNode();
		}
		catch (NodeNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public List<Node> toPathNode(Node u, Node v) {
		List<Node> lst = new LinkedList<Node>();
		
		List<Link> path = dijkstra.getPath(u, v);
		
		Node u_i = u;
		lst.add(u_i);
		
		for(Link link : path) {
			Node u_i_mas_1 = getNode(link, u_i);
			u_i = u_i_mas_1;
			lst.add(u_i);
		}
		
		return lst;
	}
	
	public boolean containsClientSession(List<iBGPSession> iBGPTopology, String u, String v) {
		
		for (iBGPSession session: iBGPTopology) {
			if ((session.getSessionType() == iBGPSessionType.client) &&
				session.getIdLink1().equals(u) &&
				session.getIdLink2().equals(v))
			{
				return true;
			}
				
		}
		return false;
	}
	
	
	
	@Override
	public int run(Object _params, Object result) {
		Object[] params = (Object[])_params;
		
		Graph<Node, Link> G = (Graph<Node, Link>) params[0];
		
		String SEPARATOR = (String) params[1];
		Integer MAX_ITER = (Integer) params[2];
		Double ALPHA = (Double) params[3];
		Double BETA = (Double) params[4];
		Double GAMA = (Double) params[5];
		
		Integer NB_RUN = (Integer)params[6];
		Integer N_GEN = (Integer)params[7];
		Integer SIZE_P = (Integer)params[8];
		Integer SIZE_OF = (Integer)params[9];
		Double PMUT = (Double)params[10];
		Double PCROSS = (Double)params[11];
		
		List<iBGPSession> I = (List<iBGPSession>) result;
		
		/* Step 1: removing the pendant vertexes gradually*/
		
		Graph<Node, Link> Gp = removePedantVertexes(G, I);
		
		/* Step 2: Choose a graph separator S in G'.V */
		
		GraphSeparator graphSeparator;
		
		if ("AE".equals(SEPARATOR)) {
			graphSeparator = Separator.GraphPartitionAE(NB_RUN, Gp, N_GEN, SIZE_P, SIZE_OF, PMUT, PCROSS);
		}
		else {
			graphSeparator = Separator.GRASPBisection(Gp, MAX_ITER, ALPHA, BETA, GAMA);
		}
		
		Set<Node> S = graphSeparator.getSeparator();
		List<Graph<Node, Link>> G1m = graphSeparator.getComponents();
		dijkstra = new DijkstraShortestPath<Node, Link>(Gp);
		
		/* Step 3: find a superset S+ of S */
		Set<Node> Splus = new HashSet<Node>(S);
		
		for (Graph<Node, Link> Gi : G1m) { // Foreach connected componet
			for (Node u : Gi.getVertices()) {
				for (Node v : S) {
					if (!Splus.contains(u)) {
						List<Node> P = toPathNode(u, v);
						for (Iterator<Node> ii = P.iterator() ; ii.hasNext() ; ) {
							Node w = ii.next();
							if (w != u && w != v) Splus.add(w);
						}
					}
				}
			}
		}
		
		/* Step 4: Fully mesh the routers in S+ */
		Set<Node> aux_set = new HashSet<Node>(Splus);
		for (Node u : Splus) {
			aux_set.remove(u);
			for (Node v : aux_set) {
				I.add(new iBGPSession(u.getId(), v.getId(), iBGPSessionType.peer));
			}
		}
		
		/* Step 5: Let every router in G'.V - S+ be a route
		reflector client of some routers in S+ */
		
		Set<Node> GpV_Splus = new HashSet<Node>(Gp.getVertices());
		GpV_Splus.removeAll(Splus);
		for (Node u : GpV_Splus) {
			for (Node v : Splus) {
				List<Node> P = toPathNode(u, v);
				Iterator<Node> ii = P.iterator();
				ii.next(); // el nodo u0, estoy en el nodo u1 (el siguiente)
				Node Ri = null;
				while( ii.hasNext() && !Splus.contains(Ri = ii.next())) {
					Ri = ii.next();
				};
				if(!containsClientSession(I, u.getId(), Ri.getId())) I.add(new iBGPSession(u.getId(), Ri.getId(), iBGPSessionType.client));
			}
		}

		/* Step 6: full mesh routers in Gi - S+ */
		
		for (Graph<Node, Link> Gi : G1m) { // Foreach connected componet
			Set<Node> GiV_Splus = new HashSet<Node>(Gi.getVertices());
			GiV_Splus.removeAll(Splus);
			aux_set = new HashSet<Node>(GiV_Splus);
			for (Node u : GiV_Splus) {
				aux_set.remove(u);
				for (Node v : aux_set) {
					I.add(new iBGPSession(u.getId(), v.getId(), iBGPSessionType.peer));
				}
			}
		}
		
		return 0;
	}
}
