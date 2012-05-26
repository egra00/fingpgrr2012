package uy.edu.fing.repository.rrloc.algorithms.bgpsepD;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import uy.edu.fing.repository.rrloc.algorithms.bgpsep.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.bgpsep.iBGPSessionType;
import uy.edu.fing.repository.rrloc.algorithms.iAlgorithm.RRLocAlgorithm;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;

public class BGPSepDAlgorithm implements RRLocAlgorithm {
	
	private Logger my_logger;
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(Object param, Object result) 
	{
		my_logger = Logger.getLogger(BGPSepDAlgorithm.class);
		Graph<Node, Link> G = (Graph<Node, Link>) param;
		List<iBGPSession> I = (List<iBGPSession>) result;
		
		myrun(G, I);
	}
	
	
	public void myrun(Graph<Node, Link> G, List<iBGPSession> I) 
	{
		boolean pending = true;
		Graph<Node, Link> Gp = copy(G);
		
		while(pending)
		{
			G = copy(Gp);
			pending = false;
			
			for (Node n : G.getVertices()) 
			{
				if ((G.inDegree(n) == G.outDegree(n)) && G.outDegree(n) == 1)
				{
					Collection<Node> out_neighbor = (List<Node>) G.getNeighbors(n);
					Node neighbor = out_neighbor.iterator().next();
					I.add(new iBGPSession(n.getId(), neighbor.getId(), iBGPSessionType.client));
					Gp.removeVertex(n);
					pending = true;
				}
				else if (G.inDegree(n) != G.outDegree(n))
				{
					my_logger.info("[WARNING]: In node "+ n.getId() + " in-degree != out-degree (used out-degree)");
				}	
			}
			
			RRLocAlgorithm bgpsep = new BGPSepDAlgorithm();
			
			bgpsep.run(Gp, I);
		}
	}
	
	public Graph<Node, Link> copy(Graph<Node, Link> original) 
	{
		Graph<Node, Link> target = new UndirectedSparseMultigraph<Node, Link>();
		
		//Copy nodes
		for (Node n : original.getVertices()) 
		{
			target.addVertex(n);
		}
		
		//Copy links
		for (Link l : original.getEdges()) 
		{
			try 
			{
				target.addEdge(l, l.getSrcNode(), l.getDstNode());
			} 
			catch (NodeNotFoundException e) 
			{
				e.printStackTrace();
			}
		}
		return target;
	}
	
	/*
	 * 
	 * Used for independent execution
	 * 
	 */
	public static void main(String args[]) 
	{
		
	}
	
}
