package uy.edu.fing.repository.rrloc.algorithms.batesX.batesY;

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
import edu.uci.ics.jung2.graph.Graph;


@SuppressWarnings("unchecked")
public class BatesYAlgorithm implements RRLocAlgorithm
{
	
	private HashMap<String, Integer> degree_nodes;
	
	public static class Params
	{
		public int rrs;
		public int pops;
		public Graph<Node, Link> graph;
	}
	
	public BatesYAlgorithm()
	{
		degree_nodes = new HashMap<String, Integer>();
	}
	
	
	@Override
	public void run(Object in_params, Object out_result) 
	{
		int _rrs = ((Params) in_params).rrs;
		int _pops = ((Params) in_params).pops;
		Graph<Node, Link> igp = ((Params) in_params).graph;
		List<iBGPSession> lst_sessions = (List<iBGPSession>) out_result;
		
		List<Graph<Node, Link>> lst_pops = KMedoidsGA.kMedoids(15, igp, _pops ,50, 60, 100, 0.01, 0.1);
		List<Node> lst_PoPs_RRs = new LinkedList<Node>();
		
		for (Graph<Node, Link> g : lst_pops)
		{
			lst_PoPs_RRs.addAll(BatesYPoP(_rrs, g, lst_sessions));
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
	
	
	public List<Node> BatesYPoP(int rrs, Graph<Node, Link> igp, List<iBGPSession> lst_sessions)
	{
		List<Node> lst_rrs_pop = new LinkedList<Node>();
		List<Node> lst_node = lst_nodes_order_degree(igp);
		
		// Escojo los routers reflectors del PoP
		for(int cant = 0 ; !lst_node.isEmpty() && cant < rrs; cant++) 
		{
			lst_rrs_pop.add(lst_node.remove(0));
		}
		
		// full-mesh entre los RRs
		for(Node node1 : lst_rrs_pop)
		{
			
			// Hago clientes al resto de los routers con el RR node
			for(Iterator<Node> ii2 = lst_node.iterator(); ii2.hasNext(); )
			{
				Node node2 = ii2.next();
				iBGPSession session = new iBGPSession(node2.getId(), node1.getId(), iBGPSessionType.client);
				lst_sessions.add(session);
			}
		}
		
		// Hago el full-mesh entre los clientes
		for( ; !lst_node.isEmpty() ; )
		{
			Node n1 = lst_node.remove(0);
			for(Node n2 : lst_node)
			{
				iBGPSession session = new iBGPSession(n2.getId(), n1.getId(), iBGPSessionType.peer);
				lst_sessions.add(session);
			}
		}
		
		return lst_rrs_pop;
	}
	
	
	public List<Node> toList(Graph<Node, Link> igp)
	{
		List<Node> lst = new LinkedList<Node>();
		
		for(Iterator<Node> i = igp.getVertices().iterator(); i.hasNext();)
			lst.add(i.next());
		return lst;
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
