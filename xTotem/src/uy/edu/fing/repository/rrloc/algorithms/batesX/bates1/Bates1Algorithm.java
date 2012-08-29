package uy.edu.fing.repository.rrloc.algorithms.batesX.bates1;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import uy.edu.fing.repository.rrloc.tools.graph.kmedoids.KMedoidsGA;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;


@SuppressWarnings("unchecked")
public class Bates1Algorithm implements RRLocAlgorithm
{
	
	public static class Params
	{
		public int pops;
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
		int _pops = ((Params) in_params).pops;
		Graph<Node, Link> igp = ((Params) in_params).graph;
		List<iBGPSession> lst_sessions = (List<iBGPSession>) out_result;
		
		List<Graph<Node, Link>> lst_pops = KMedoidsGA.kMedoids(15, igp, _pops ,50, 60, 100, 0.01, 0.1);
		List<Node> lst_PoPs_RRs = new LinkedList<Node>();
		
		for (Graph<Node, Link> g : lst_pops)
		{
			//System.out.println("////Tamaño grafo     "+g.getVertexCount());
			lst_PoPs_RRs.addAll(PoPs(g, lst_sessions));
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
	
	
	public List<Node> PoPs(Graph<Node, Link> igp, List<iBGPSession> lst_sessions)
	{
		List<Node> lst = new LinkedList<Node>();
		Node node;
		
		if(igp.getEdgeCount()>0)
		{
			// Escojo el mas conectado, solo 1
			node = Operations.getMaxDegVertex(igp);
		}
		else
		{
			// Escojo uno al azar
			node = toList(igp).get((new Random()).nextInt(igp.getVertexCount()));
		}

		
		// Hago clientes al resto de los routers con el RR
		for(Iterator<Node> ii2 = igp.getVertices().iterator(); ii2.hasNext(); )
		{
			Node node2 = ii2.next();
			if (node != node2)
			{
				iBGPSession session = new iBGPSession(node2.getId(), node.getId(), iBGPSessionType.client);
				lst_sessions.add(session);
			}
		}
		
		List<Node> aux = toList(igp);
		aux.remove(node);
		for(;!aux.isEmpty();)
		{
			Node n1 = aux.remove(0);
			for(Node n2 : aux)
			{
				iBGPSession session = new iBGPSession(n2.getId(), n1.getId(), iBGPSessionType.peer);
				lst_sessions.add(session);
			}
		}
		
		lst.add(node);
		
		
		return lst;
	}
}
