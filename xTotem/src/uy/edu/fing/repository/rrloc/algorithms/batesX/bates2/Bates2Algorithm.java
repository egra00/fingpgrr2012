package uy.edu.fing.repository.rrloc.algorithms.batesX.bates2;

import java.util.Iterator;
import java.util.List;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;

@SuppressWarnings("unchecked")
public class Bates2Algorithm implements RRLocAlgorithm
{	
	@Override
	public void run(Object in_params, Object out_resutl) 
	{
		
		Graph<Node, Link> igp = (Graph<Node, Link>) in_params;
		List<iBGPSession> lst_sessions = (List<iBGPSession>) out_resutl;

		
		Node node = Operations.getMaxDegVertex(igp);
		
		Node node1 = null;
		int deg_node1 = Integer.MIN_VALUE;
		
		// Escojo los 2 nodos mas conectado
		for(Iterator<Node> ii=igp.getVertices().iterator() ; ii.hasNext(); )
		{
			Node node2 = ii.next();
			int max = igp.degree(node2);
			
			if(node != node2)
			{
				if(deg_node1 < max)
				{
					deg_node1 = max;
					node1 = node2;
				}
			}
			
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
		
		// Hago clientes al resto de los routers con el RR
		for(Iterator<Node> ii2 = igp.getVertices().iterator(); ii2.hasNext(); )
		{
			Node node2 = ii2.next();
			if (node1 != node2)
			{
				iBGPSession session = new iBGPSession(node2.getId(), node1.getId(), iBGPSessionType.client);
				lst_sessions.add(session);
			}
		}
		
	}
}
