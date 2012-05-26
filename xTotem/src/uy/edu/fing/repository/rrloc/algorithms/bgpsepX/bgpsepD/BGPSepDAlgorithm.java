package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepD;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.iBGPSessionType;
import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsep.BGPSepAlgorithm;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;

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
		Graph<Node, Link> Gp = Operations.copyUndirectedSparseGraph(G);
		
		while(pending)
		{
			G = Operations.copyUndirectedSparseGraph(Gp);
			pending = false;
			
			for (Node n : G.getVertices()) 
			{
				if ((G.inDegree(n) == G.outDegree(n)) && G.outDegree(n) == 1)
				{
					Collection<Node> out_neighbor = G.getNeighbors(n);
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
		}
		
		RRLocAlgorithm bgpsep = new BGPSepAlgorithm();
		bgpsep.run(Gp, I);
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
