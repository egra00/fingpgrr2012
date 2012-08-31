package uy.edu.fing.repository.rrloc.algorithms.zhang;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import uy.edu.fing.repository.rrloc.tools.graph.kmedoids.KMedoidsGA;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.algorithms.shortestpath.DijkstraDistance;
import edu.uci.ics.jung2.graph.Graph;


@SuppressWarnings("unchecked")
public class ZhangAlgorithm implements RRLocAlgorithm
{
	
	private HashMap<String, Integer> degree_nodes;
	private DijkstraDistance<Node, Link> dst_nodes;
	
	ZhangAlgorithm()
	{
		degree_nodes = new HashMap<String, Integer>();	
	}
	
	public static class Params
	{
		public int nbr_level1;
		public int nbr_level2;
		public int pops;
		public Graph<Node, Link> graph;
	}
	
	@Override
	public void run(Object in_params, Object out_result) 
	{
		Graph<Node, Link> igp = ((Params) in_params).graph;
		int level_one = ((Params) in_params).nbr_level1;
		int level_two = ((Params) in_params).nbr_level2;
		int pops = ((Params) in_params).pops;
		dst_nodes = new  DijkstraDistance<Node, Link>(igp); 
		List<iBGPSession> lst_sessions = (List<iBGPSession>) out_result;
		
		List<Graph<Node, Link>> lst_pops = KMedoidsGA.kMedoids(15, igp, pops ,50, 60, 100, 0.01, 0.1);
		List<Node> lst_PoPs_RRs = new LinkedList<Node>();
		
		for (Graph<Node, Link> g : lst_pops)
		{
			lst_PoPs_RRs.addAll(ZhangPoP(level_one, level_two, g, lst_sessions));
		}
		
		for(;!lst_PoPs_RRs.isEmpty();)
		{
			Node n1 = lst_PoPs_RRs.remove(0);
			for(Node n2 : lst_PoPs_RRs)
			{
				iBGPSession session = new iBGPSession(n2.getId(), n1.getId(), iBGPSessionType.peer);
				lst_sessions.add(session);
			}
		}	
	}
	
	
	public List<Node> ZhangPoP(int level_one, int level_two, Graph<Node, Link> igp, List<iBGPSession> lst_sessions)
	{
		List<Node> lst_level1 = new LinkedList<Node>();
		List<Node> lst_level2 = new LinkedList<Node>();
		
		List<Node> lst_node = lst_nodes_order_degree(igp);
		
		// Escojo los routers en el primer nivel
		for(int cant = 0 ; !lst_node.isEmpty() && cant < level_one; cant++) 
		{
			lst_level1.add(lst_node.remove(0));
		}

		
		// Escojo los routers en el segundo nivel
		for(int cant = 0 ; !lst_node.isEmpty() && cant < level_two; cant++) 
		{
			lst_level2.add(lst_node.remove(0));
		}
		
		
		
		// mesh desde el level 2 al level 1
		for(Iterator<Node> ii = lst_level2.iterator(); ii.hasNext(); ) 
		{
			
			Node node1 = ii.next();
			List<Node> lst = twoRRcloset(node1, lst_level1);

			for(Iterator<Node> ii2 = lst.iterator(); ii2.hasNext(); )
			{
				Node node2 = ii2.next();
				iBGPSession session = new iBGPSession(node1.getId(), node2.getId(), iBGPSessionType.client);
				lst_sessions.add(session);
			}
			
		}
		
		
		// mesh desde el low level al level 2
		for(Iterator<Node> ii = lst_node.iterator(); ii.hasNext(); ) 
		{
			Node node1 = ii.next();
			List<Node> lst = twoRRcloset(node1, lst_level2);
			
			for(Iterator<Node> ii2 = lst.iterator(); ii2.hasNext(); )
			{
				Node node2 = ii2.next();
				iBGPSession session = new iBGPSession(node1.getId(), node2.getId(), iBGPSessionType.client);
				lst_sessions.add(session);
			}
			
		}
		return lst_level1;
	}
	
	
	public List<Node> twoRRcloset(Node node, List<Node> lst)
	{
		List<Node> l = new LinkedList<Node>();
		
		Node node1 = null;
		int dst_node_node1 = Integer.MAX_VALUE;
		
		for(Iterator<Node> ii = lst.iterator(); ii.hasNext();) 
		{
			Node n = ii.next();
			int dst_node_n = dst_nodes.getDistance(node, n).intValue() ;
			if(dst_node_node1 > dst_node_n)
			{
				node1 = n;
				dst_node_node1 = dst_node_n;
			}
		}
		
		
		Node node2 = null;
		int dst_node_node2 = Integer.MAX_VALUE;
		
		for(Iterator<Node> ii = lst.iterator(); ii.hasNext();) 
		{
			Node n = ii.next();
			int dst_node_n = dst_nodes.getDistance(node, n).intValue();
			if (node1 != n)
			{
				if(dst_node_node2 > dst_node_n)
				{
					node2 = n;
					dst_node_node2 = dst_node_n;
				}
			}
		}
		
		if (node1 != null) l.add(node1);
		if (node2 != null) l.add(node2);
		
		
		return l;
	}
	
	public List<Node> lst_nodes_order_degree(Graph<Node, Link> igp)
	{
		List<Node> lst_nodes = new LinkedList<Node>();
	
		//Ordeno los nodos por grado
		for(Iterator<Node> ii = igp.getVertices().iterator(); ii.hasNext();) 
		{
			Node node = ii.next();
			degree_nodes.put(node.getId(), igp.degree(node));
			lst_nodes = insert_order(lst_nodes, node); // inserta ordenado segun el grado del nodo
		}
		
		return lst_nodes;
	}
	
	public List<Node> insert_order(List<Node> lst, Node node)
	{
		int index = 0;
		int degree_node = degree_nodes.get(node.getId());
		
		for(Iterator<Node> ii = lst.iterator(); ii.hasNext() && degree_nodes.get(ii.next().getId()) > degree_node ;) 
			index++;		
		
		lst.add(index, node);
		
		return lst;
	}
}
