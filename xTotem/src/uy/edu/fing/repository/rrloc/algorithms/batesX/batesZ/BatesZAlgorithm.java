package uy.edu.fing.repository.rrloc.algorithms.batesX.batesZ;

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
public class BatesZAlgorithm implements RRLocAlgorithm
{

	public static class Params
	{
		public int rrs;
		public int pops;
		public Graph<Node, Link> graph;
	}
	
	public BatesZAlgorithm()
	{
	}
	
	
	@Override
	public void run(Object in_params, Object out_result) 
	{
		int _rrs = ((Params) in_params).rrs;
		int _pops = ((Params) in_params).pops;
		Graph<Node, Link> igp = ((Params) in_params).graph;
		List<iBGPSession> lst_sessions = (List<iBGPSession>) out_result;
		
		List<List<Node>> lst_cells = KMedoidsGA.kMedoids(15, igp, _pops ,50, 60, 100, 0.01, 0.1);
		List<Node> lst_PoPs_RRs = new LinkedList<Node>();
		
		for (List<Node> cell : lst_cells)
		{
			lst_PoPs_RRs.addAll(BatesZPoP(igp, cell, _rrs, lst_sessions));
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
	
	
	public List<Node> BatesZPoP(Graph<Node, Link> igp, List<Node> cell, int rrs, List<iBGPSession> lst_sessions)
	{
		List<Node> lst_rrs_pop = new LinkedList<Node>();
		List<Node> lst_node = lst_nodes_order_by_priority(igp, cell);
		
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
		
		return lst_rrs_pop;
	}
	
	
	
	public List<Node> toList(Graph<Node, Link> igp)
	{
		List<Node> lst = new LinkedList<Node>();
		
		for(Iterator<Node> i = igp.getVertices().iterator(); i.hasNext();)
			lst.add(i.next());
		return lst;
	}
	
	
	public List<Node> lst_nodes_order_by_priority(Graph<Node, Link> igp, List<Node> cell)
	{
		List<Node> lst_nodes = new LinkedList<Node>();
		List<Node> lst_nodes_aux = new LinkedList<Node>();
		
		//Ordeno por grado los PoPs de la cell
		for(Node n1 : cell)
		{
			for(Node n2 : igp.getVertices())
			{
				if (!cell.contains(n2) && igp.isNeighbor(n1, n2) && !lst_nodes.contains(n1)) 
					lst_nodes = insert_order_by_degree(igp, lst_nodes, n1); // inserta ordenado segun el grado del nodo
			}
		}
		
		
		//Ordeno los nodos por grado el resto de los nodos en la cell
		for(Node node : cell) 
		{
			if(!lst_nodes.contains(node))
				lst_nodes_aux = insert_order_by_degree(igp, lst_nodes_aux, node); // inserta ordenado segun el grado del nodo
		}
		
		lst_nodes.addAll(lst_nodes_aux);
		return lst_nodes;
	}
	
	
	public List<Node> insert_order_by_degree(Graph<Node, Link> igp, List<Node> lst, Node node)
	{
		
		int index = 0;
		int degree_node = igp.degree(node);
		
		for(Iterator<Node> ii = lst.iterator(); ii.hasNext() && igp.degree(ii.next()) > degree_node ;) 
			index++;		
		
		lst.add(index, node);
		
		return lst;
	}
}
