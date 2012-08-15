package uy.edu.fing.repository.rrloc.algorithms.fullmesh;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;


@SuppressWarnings("unchecked")
public class FullMeshAlgorithm implements RRLocAlgorithm
{
	
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
		Graph<Node, Link> igp = (Graph<Node, Link>) in_params;
		List<iBGPSession> lst_sessions = (List<iBGPSession>) out_result;
		List<Node> lst = toList(igp);
		
		
		// full-mesh entre los RRs
		for(; !lst.isEmpty(); )
		{
			Node node1 = lst.remove(0);
			for(Iterator<Node> ii2 = lst.iterator(); ii2.hasNext(); )
			{
				Node node2 = ii2.next();
				iBGPSession session = new iBGPSession(node1.getId(), node2.getId(), iBGPSessionType.peer);
				lst_sessions.add(session);
			}
		}	
	}
}
