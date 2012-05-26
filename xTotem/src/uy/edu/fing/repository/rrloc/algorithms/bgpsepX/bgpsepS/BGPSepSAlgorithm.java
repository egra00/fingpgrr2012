package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.iBGPSessionType;
import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepD.BGPSepDAlgorithm;
import uy.edu.fing.repository.rrloc.graphTools.GraphSeparator;
import uy.edu.fing.repository.rrloc.graphTools.Separator;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung2.graph.Graph;

@SuppressWarnings("unchecked")
public class BGPSepSAlgorithm extends BGPSepDAlgorithm {

	@Override
	public void run(Object param, Object result) {
		Graph<Node, Link> G = (Graph<Node, Link>) param;
		List<iBGPSession> I = (List<iBGPSession>) result;
		
		/* Step 1: removing the pendant vertexes gradually*/
		
		Graph<Node, Link> Gp = Operations.copyUndirectedSparseGraph(G);
		removePedantVertexes(G, I, Gp);
		
		/* Step 2: Choose a graph separator S ⊆ G'.V */
		
		GraphSeparator graphSeparator = Separator.graphSeparator(Gp);
		Set<Node> S = graphSeparator.getSeparator();
		List<Graph<Node, Link>> G1m = graphSeparator.getComponents();
		
		/* Step 3: find a superset S+ of S */
		
		Set<Node> Splus = new HashSet<Node>(S);
		for (Graph<Node, Link> Gi : G1m) { // Foreach connected componet
			for (Node u : Gi.getVertices()) {
				DijkstraShortestPath<Node, Link> dijkstra = new DijkstraShortestPath<Node, Link>(G); 
				for (Node v : S) {
					if (!Splus.contains(u)) {
						List<Link> P = dijkstra.getPath(u, v);
						for (Link link : P) {
							try {
								Node w = link.getDstNode();
								if (w.getId() != u.getId()) {
									Splus.add(w);
								}
							} catch (NodeNotFoundException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
		/* Step 4: Fully mesh the routers in S+ */
		
		for (Node u : Splus) {
			for (Node v : Splus) {
				if (u != v) {
					I.add(new iBGPSession(u.getId(), v.getId(), iBGPSessionType.peer));
				}
			}
		}
		
		/* Step 5: Let every router in G'.V − S+ be a route
		reflector client of some routers in S+ */
		
		Set<Node> GpV_Splus = new HashSet<Node>(Gp.getVertices());
		GpV_Splus.removeAll(Splus);
		for (Node u : GpV_Splus) {
			DijkstraShortestPath<Node, Link> dijkstra = new DijkstraShortestPath<Node, Link>(G);
			for (Node v : Splus) {
				List<Link> P = dijkstra.getPath(u, v);
				Node Ri = null;
				for (Link link : P) {
					try {
						Ri = link.getDstNode();
						if (Splus.contains(Ri)) {
							break;
						}
					} catch (NodeNotFoundException e) {
						e.printStackTrace();
					}
				}
				I.add(new iBGPSession(u.getId(), Ri.getId(), iBGPSessionType.client));
			}
		}

		/* Step 6: full mesh routers in Gi − S+ */
		
		for (Graph<Node, Link> Gi : G1m) { // Foreach connected componet
			Set<Node> GiV_Splus = new HashSet<Node>(Gi.getVertices());
			GiV_Splus.removeAll(Splus);
			for (Node u : GiV_Splus) {
				for (Node v : GiV_Splus) {
					if (u != v) {
						I.add(new iBGPSession(u.getId(), v.getId(), iBGPSessionType.peer));
					}
				}
			}
		}
	}
}
