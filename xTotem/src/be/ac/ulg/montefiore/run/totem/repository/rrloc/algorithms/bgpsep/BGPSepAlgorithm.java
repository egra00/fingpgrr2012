package be.ac.ulg.montefiore.run.totem.repository.rrloc.algorithms.bgpsep;

import java.util.List;

import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.graphTools.GraphSeparator;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.graphTools.Separator;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import edu.uci.ics.jung2.graph.Graph;

@SuppressWarnings("unchecked")
public class BGPSepAlgorithm implements RRLocAlgorithm {
	
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
			GraphSeparator graphSeparator = Separator.graphSeparator(IGPTopology);
			
			//El conjunto de routes reflectors estará configurado Full Mesh
			for (Node u : graphSeparator.getSeparator()) {
				for (Node v : graphSeparator.getSeparator()) {
					if (u == v)
						continue;
					i.add(new iBGPSession(
							u.getId(),
							v.getId(),
							iBGPSessionType.peer));
					
				}
			}
			
			// Cada router en la componente debe ser un cliente de todos
			// los route reflectos
			for (Graph<Node, Link> g_i : graphSeparator.getComponents()) {
				for (Node u : g_i.getVertices()) {
					for (Node v : graphSeparator.getSeparator()) {
						i.add(new iBGPSession(
								u.getId(), 
								v.getId(), 
								iBGPSessionType.client));
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
	public static void main(String args[]) 
	{
		
	}
	
}
