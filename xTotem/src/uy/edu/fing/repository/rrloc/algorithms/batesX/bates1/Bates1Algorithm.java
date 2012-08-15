package uy.edu.fing.repository.rrloc.algorithms.batesX.bates1;

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
public class Bates1Algorithm implements RRLocAlgorithm
{
	@Override
	public void run(Object in_params, Object out_result) 
	{
		Graph<Node, Link> igp = (Graph<Node, Link>) in_params;
		List<iBGPSession> lst_sessions = (List<iBGPSession>) out_result;

		// Escojo el mas conectado, solo 1
		Node node = Operations.getMaxDegVertex(igp);
		
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
		
	}
}
