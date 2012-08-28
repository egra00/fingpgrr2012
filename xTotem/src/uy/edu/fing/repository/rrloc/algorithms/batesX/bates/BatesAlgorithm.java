package uy.edu.fing.repository.rrloc.algorithms.batesX.bates;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;

@SuppressWarnings("unchecked")
public class BatesAlgorithm implements RRLocAlgorithm{

	
	public static class Params
	{
		public int cant_rr;
		public Graph<Node, Link> graph;
	}
	
	public List<Node> toList(Graph<Node, Link> igp)
	{
		List<Node> lst = new LinkedList<Node>();
		
		for(Iterator<Node> i = igp.getVertices().iterator(); i.hasNext();)
			lst.add(i.next());
		return lst;
	}
	
	@Override
	public void run(Object in_params, Object out_result) 
	{
		Graph<Node, Link> igp = ((Params) in_params).graph;
		int cant_rr = ((Params) in_params).cant_rr;
		List<iBGPSession> lst_sessions = (List<iBGPSession>) out_result;
		List<Node> my_lst_node = new LinkedList<Node>();
		List<Node> lst = toList(igp);
		
		
		// Escojo al azar los RRs
		for(int i=0; i<cant_rr; i++)
		{
			Random ram = new Random();
			int index = ram.nextInt(lst.size());
			Node node = lst.remove(index);
			my_lst_node.add(node);	
		}
		
		// full-mesh entre los RRs
		for(; !my_lst_node.isEmpty(); )
		{
			Node node1 = my_lst_node.remove(0);
			for(Iterator<Node> ii2 = my_lst_node.iterator(); ii2.hasNext(); )
			{
				Node node2 = ii2.next();
				iBGPSession session = new iBGPSession(node1.getId(), node2.getId(), iBGPSessionType.peer);
				lst_sessions.add(session);
			}
			
			// Hago clientes al resto de los routers con el RR node
			for(Iterator<Node> ii2 = lst.iterator(); ii2.hasNext(); )
			{
				Node node2 = ii2.next();
				iBGPSession session = new iBGPSession(node2.getId(), node1.getId(), iBGPSessionType.client);
				lst_sessions.add(session);
			}
		}
		
		for( ; !lst.isEmpty() ; )
		{
			Node n1 = lst.remove(0);
			for(Node n2 : lst)
			{
				iBGPSession session = new iBGPSession(n2.getId(), n1.getId(), iBGPSessionType.peer);
				lst_sessions.add(session);
			}
		}
	}
}
