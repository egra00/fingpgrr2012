package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepD;

import java.util.Collection;
import java.util.List;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsep.BGPSepAlgorithm;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;

public class BGPSepDAlgorithm implements RRLocAlgorithm {
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(Object _params, Object result) {
		Object[] params = (Object[])_params;
		
		Graph<Node, Link> G = (Graph<Node, Link>) params[0];
		
		Integer MAX_ITER = (Integer) params[1];
		Double ALPHA = (Double) params[2];
		Double BETA = (Double) params[3];
		Double GAMA = (Double) params[4];
		
		List<iBGPSession> I = (List<iBGPSession>) result;
		
		Graph<Node, Link> Gp = removePedantVertexes(G, I);
		RRLocAlgorithm bgpsep = new BGPSepAlgorithm();
		
		Object[] newParams = new Object[5];
		newParams[0] = Gp;
		newParams[1] = MAX_ITER;
		newParams[2] = ALPHA;
		newParams[3] = BETA;
		newParams[4] = GAMA;
		
		bgpsep.run(newParams, I);
	}
	
	
	protected Graph<Node, Link> removePedantVertexes(Graph<Node, Link> _G, List<iBGPSession> _out_I) 
	{
		boolean pending = true;
		Graph<Node, Link> Gaux;
		Graph<Node, Link> Gp = Operations.copyUndirectedSparseGraph(_G);
		
		while(pending)
		{
			Gaux = Operations.copyUndirectedSparseGraph(Gp);
			pending = false;
			
			for (Node n : Gaux.getVertices()) 
			{
				if (Gaux.degree(n) == 1)
				{
					Collection<Node> out_neighbor = Gaux.getNeighbors(n);
					Node neighbor = out_neighbor.iterator().next();
					_out_I.add(new iBGPSession(n.getId(), neighbor.getId(), iBGPSessionType.client));
					Gp.removeVertex(n);
					pending = true;
				}	
			}
		}
		
		return Gp;
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
