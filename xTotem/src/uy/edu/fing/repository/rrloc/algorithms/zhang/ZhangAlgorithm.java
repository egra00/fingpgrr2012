package uy.edu.fing.repository.rrloc.algorithms.zhang;

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
public class ZhangAlgorithm implements RRLocAlgorithm
{
	public static class Params
	{
		public int cnb;
		public Graph<Node, Link> graph;
	}
	
	public List<Node> toList(Graph<Node, Link> igp)
	{
		List<Node> lst = new LinkedList<Node>();
		
		for(Iterator<Node> i = igp.getVertices().iterator(); i.hasNext();)
			lst.add(i.next());
		return lst;
	}
	
	
	public boolean contiene(List<iBGPSession> lst, iBGPSession se2)
	{
		
		for(Iterator<iBGPSession> ii = lst.iterator(); ii.hasNext();)
		{
			iBGPSession se = ii.next();
			if(se.getIdLink1().equals(se2.getIdLink1()) && se.getIdLink2().equals(se2.getIdLink2()) ||
					se.getIdLink1().equals(se2.getIdLink2()) && se.getIdLink2().equals(se2.getIdLink1()))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void run(Object in_params, Object out_resutl) 
	{
		Graph<Node, Link> igp = ((Params) in_params).graph;
		int cant_rr = ((Params) in_params).cnb;
		List<iBGPSession> lst_sessions = (List<iBGPSession>) out_resutl;
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
		for(Iterator<Node> ii = my_lst_node.iterator(); ii.hasNext(); )
		{
			Node node1 = ii.next();
			for(Iterator<Node> ii2 = my_lst_node.iterator(); ii2.hasNext(); )
			{
				Node node2 = ii2.next();
				if (node1 != node2)
				{
					iBGPSession session = new iBGPSession(node1.getId(), node2.getId(), iBGPSessionType.peer);
					if (!contiene(lst_sessions, session))
					{
						lst_sessions.add(session);
					}
				}
			}
		}
		
		
		// Hago clientes al resto de los routers con todos los RRs
		for(Iterator<Node> ii = my_lst_node.iterator(); ii.hasNext(); )
		{
			Node node1 = ii.next();
			for(Iterator<Node> ii2 = igp.getVertices().iterator(); ii2.hasNext(); )
			{
				Node node2 = ii2.next();
				if(!my_lst_node.contains(node2))
				{
					iBGPSession session = new iBGPSession(node2.getId(), node1.getId(), iBGPSessionType.client);
					lst_sessions.add(session);
				}
			}
		}
		
	}
}
