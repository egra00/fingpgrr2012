package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepS;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepD.BGPSepDAlgorithm;
import uy.edu.fing.repository.rrloc.graphTools.GraphSeparator;
import uy.edu.fing.repository.rrloc.graphTools.Separator;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung2.graph.Graph;

@SuppressWarnings("unchecked")
public class BGPSepSAlgorithm extends BGPSepDAlgorithm {

	private DijkstraShortestPath<Node, Link> dijkstra;

	
	public boolean contiene(List<iBGPSession> lst, String n1, String n2)
	{
		for(iBGPSession session : lst)
		{
			if (session.getIdLink1().equals(n1) && session.getIdLink2().equals(n2) ||
				session.getIdLink1().equals(n2) && session.getIdLink2().equals(n1))
			{
				return true;
			}
		}
		return false;
	}
	
	
	public Node getNode(Link link, Node node) // Dado un link "link" y un nodo "node" doy el extremo del que no es "node"
	{
		try 
		{
			if (link.getDstNode() != node)
				return link.getDstNode();
			else
				return link.getSrcNode();
		}
		catch (NodeNotFoundException e) 
		{
			e.printStackTrace();
			return null;
		}
	}
	
	
	public List<Node> toPathNode(Node u, Node v)
	{
		List<Node> lst = new LinkedList<Node>();
		List<Link> path = dijkstra.getPath(u, v);
		
		Node u_i = u;
		lst.add(u_i);
		
		for(Link link : path)
		{
			Node u_i_mas_1 = getNode(link, u_i);
			u_i = u_i_mas_1;
			lst.add(u_i);
		}
		
		return lst;
	}
	
	
	
	@Override
	public void run(Object param, Object result) {
		Graph<Node, Link> G = (Graph<Node, Link>) param;
		List<iBGPSession> I = (List<iBGPSession>) result;
		
		/* Step 1: removing the pendant vertexes gradually*/
		
		Graph<Node, Link> Gp = removePedantVertexes(G, I);
		
		/* Step 2: Choose a graph separator S in G'.V */
		
		GraphSeparator graphSeparator = Separator.GraphPartitionAE(15, G ,50, 60, 100, 0.01, 0.1);
		Set<Node> S = graphSeparator.getSeparator();
		List<Graph<Node, Link>> G1m = graphSeparator.getComponents();
		dijkstra = new DijkstraShortestPath<Node, Link>(Gp);
		
		/* Step 3: find a superset S+ of S */
		Set<Node> Splus = new HashSet<Node>(S);
		
		for (Graph<Node, Link> Gi : G1m)  // Foreach connected componet
		{
			for (Node u : Gi.getVertices()) 
			{
				for (Node v : S) 
				{
					if (!Splus.contains(u)) 
					{
						List<Node> P = toPathNode(u, v);
						
						for (Iterator<Node> ii = P.iterator() ; ii.hasNext() ; )
						{
							Node w = ii.next();
							if (w != u && w != v) Splus.add(w);
						}
					}
				}
			}
		}
		
		/* Step 4: Fully mesh the routers in S+ */
		for (Node u : Splus) {
			for (Node v : Splus) {
				if (u != v && !contiene (I, u.getId(), v.getId())) {
					I.add(new iBGPSession(u.getId(), v.getId(), iBGPSessionType.peer));
				}
			}
		}
		
		/* Step 5: Let every router in G'.V - S+ be a route
		reflector client of some routers in S+ */
		
		Set<Node> GpV_Splus = new HashSet<Node>(Gp.getVertices());
		GpV_Splus.removeAll(Splus);
		for (Node u : GpV_Splus) 
		{
			for (Node v : Splus) 
			{
				List<Node> P = toPathNode(u, v);
				Iterator<Node> ii = P.iterator();
				Node Ri = ii.next(); // como el nodo u0, estoy en u1 (el siguiente)
				for ( ; ii.hasNext() && Splus.contains(Ri = ii.next()) ; );
				I.add(new iBGPSession(u.getId(), Ri.getId(), iBGPSessionType.client));
			}
		}

		/* Step 6: full mesh routers in Gi - S+ */
		
		for (Graph<Node, Link> Gi : G1m) { // Foreach connected componet
			Set<Node> GiV_Splus = new HashSet<Node>(Gi.getVertices());
			GiV_Splus.removeAll(Splus);
			for (Node u : GiV_Splus) {
				for (Node v : GiV_Splus) {
					if (u != v && !contiene (I, u.getId(), v.getId())) {
						I.add(new iBGPSession(u.getId(), v.getId(), iBGPSessionType.peer));
					}
				}
			}
		}
	}
}
