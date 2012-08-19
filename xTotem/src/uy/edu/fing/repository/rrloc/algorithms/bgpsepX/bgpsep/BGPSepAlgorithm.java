package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsep;

import java.util.List;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import uy.edu.fing.repository.rrloc.tools.graph.GraphSeparator;
import uy.edu.fing.repository.rrloc.tools.graph.Separator;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;

@SuppressWarnings("unchecked")
public class BGPSepAlgorithm implements RRLocAlgorithm {
	
	
	public boolean contiene(List<iBGPSession> lst, String n1, String n2)
	{
		for(iBGPSession session : lst)
		{
			if (session.getIdLink1().equals(n1) && session.getIdLink2().equals(n2) ||
				session.getIdLink1().equals(n2) && session.getIdLink2().equals(n1))
			{
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public void run(Object param, Object result) {
		Graph<Node, Link> IGPTopology = (Graph<Node, Link>) param;
		List<iBGPSession> i = (List<iBGPSession>) result;
		
		if (IGPTopology.getVertexCount() < 2) {
			//i = vacio
		}
		else if (IGPTopology.getVertexCount() == 2) {
			Node u = ((Node)IGPTopology.getVertices().toArray()[0]);
			Node v = ((Node)IGPTopology.getVertices().toArray()[1]);
			
			i.add(new iBGPSession(
					u.getId(), 
					v.getId(), 
					iBGPSessionType.peer));
		}
		else {
			GraphSeparator graphSeparator = Separator.GraphPartitionAE(15, IGPTopology ,50, 60, 100, 0.01, 0.1);
			
			//El conjunto de routes reflectors estara configurado Full Mesh
			for (Node u : graphSeparator.getSeparator()) {
				for (Node v : graphSeparator.getSeparator()) {
					if (u != v && !contiene(i, u.getId(), v.getId())) 
					{
						i.add(new iBGPSession(u.getId(), v.getId(), iBGPSessionType.peer));
					}
				}
			}
			
			// Cada router en la componente debe ser un cliente de todos
			// los route reflectos
			for (Graph<Node, Link> g_i : graphSeparator.getComponents()) {
				for (Node u : g_i.getVertices()) {
					for (Node v : graphSeparator.getSeparator()) {
						i.add(new iBGPSession(u.getId(), v.getId(), iBGPSessionType.client));
					}
				}
				run(g_i, i);
			}
			
		}
	}
	
	/*
	 * 
	 * Used for independent execution
	 * 
	 */
	public static void main(String args[]) {
		
	}
	
}