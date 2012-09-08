package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsep;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import uy.edu.fing.repository.rrloc.tools.graph.separator.Separator;
import uy.edu.fing.repository.rrloc.tools.graph.separator.model.GraphSeparator;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;

@SuppressWarnings("unchecked")
public class BGPSepAlgorithm implements RRLocAlgorithm {
	
	
	@Override
	public void run(Object param, Object result) {
		Graph<Node, Link> IGPTopology = (Graph<Node, Link>) param;
		List<iBGPSession> i = (List<iBGPSession>) result;
		
		if (IGPTopology.getVertexCount() == 2) {
			Node u = ((Node)IGPTopology.getVertices().toArray()[0]);
			Node v = ((Node)IGPTopology.getVertices().toArray()[1]);
			
			i.add(new iBGPSession(
					u.getId(), 
					v.getId(), 
					iBGPSessionType.peer));

		}
		else if (IGPTopology.getVertexCount() > 2) {
			GraphSeparator graphSeparator = Separator.GraphPartitionAE(20, IGPTopology ,50, 80, 140, 0.01, 0.1);
			
			//El conjunto de routes reflectors estara configurado Full Mesh
			Set<Node> aux_set = new HashSet<Node>(graphSeparator.getSeparator());
			for (Node u : graphSeparator.getSeparator()) {
				aux_set.remove(u);
				for (Node v : aux_set) {
					i.add(new iBGPSession(u.getId(), v.getId(), iBGPSessionType.peer));
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