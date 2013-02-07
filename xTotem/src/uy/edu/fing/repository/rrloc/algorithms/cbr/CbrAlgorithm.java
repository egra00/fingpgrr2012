package uy.edu.fing.repository.rrloc.algorithms.cbr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung2.graph.Graph;

public class CbrAlgorithm implements RRLocAlgorithm {

	@SuppressWarnings("unchecked")
	@Override
	public int run(Object in_params, Object out_result) {
		Graph<Node, Link> igp = (Graph<Node, Link>)((Object[]) in_params)[0];
		List<Node> nextHops = (List<Node>)((Object[]) in_params)[1];
		Object[] ret = (Object[]) out_result;
		
		Set<iBGPSessionColored> lstSessions = new HashSet<iBGPSessionColored>();
		
		HashMap<String, ArrayList<iBGPColor>> colorsPerRouter = new HashMap<String, ArrayList<iBGPColor>>();
		
		HashMap<String, HashMap<String, ArrayList<iBGPSessionColored>>> coloredTopology = 
				new HashMap<String, HashMap<String,ArrayList<iBGPSessionColored>>>();
		
		DijkstraShortestPath<Node, Link> dijkstra = new DijkstraShortestPath<Node, Link>(igp);;
		
		for (Node n : nextHops) {
			for (Node r : igp.getVertices()) {
				for (Link link : dijkstra.getPath(n, r)) {
					try {
						
						iBGPSessionColored s = new iBGPSessionColored(
								link.getDstNode().getId(), 
								link.getSrcNode().getId(), 
								iBGPSessionType.peer,
								n.getId());
						
						// Es un set y está override el equals, no agrega repetidos
						lstSessions.add(s);
						
						// Creo la topología colorada a partir de las sesiones 
						
						if (!coloredTopology.containsKey(s.getIdLink1())) {
							coloredTopology.put(s.getIdLink1(), new HashMap<String, ArrayList<iBGPSessionColored>>());
						}
						if (!coloredTopology.get(s.getIdLink1()).containsKey(s.getIdLink2())) {
							coloredTopology.get(s.getIdLink1()).put(s.getIdLink2(), new ArrayList<iBGPSessionColored>());
						}
						coloredTopology.get(s.getIdLink1()).get(s.getIdLink2()).add(s);
						
						if (!coloredTopology.containsKey(s.getIdLink2())) {
							coloredTopology.put(s.getIdLink2(), new HashMap<String, ArrayList<iBGPSessionColored>>());
						}
						if (!coloredTopology.get(s.getIdLink2()).containsKey(s.getIdLink1())) {
							coloredTopology.get(s.getIdLink2()).put(s.getIdLink1(), new ArrayList<iBGPSessionColored>());
						}
						coloredTopology.get(s.getIdLink2()).get(s.getIdLink1()).add(s);
						
						// Para cada router, necesito los colors que conoce.
						
						if (!colorsPerRouter.containsKey(s.getIdLink1())) {
							colorsPerRouter.put(s.getIdLink1(), new ArrayList<iBGPColor>());
						}
						colorsPerRouter.get(s.getIdLink1()).add(new iBGPColor(s.getColor()));
						
						if (!colorsPerRouter.containsKey(s.getIdLink2())) {
							colorsPerRouter.put(s.getIdLink2(), new ArrayList<iBGPColor>());
						}
						colorsPerRouter.get(s.getIdLink2()).add(new iBGPColor(s.getColor()));
						
						
					} catch (NodeNotFoundException e) {
						e.printStackTrace();
					}
				};
			}
		}
		
		ret[0] = lstSessions;
		ret[1] = coloredTopology;
		ret[2] = colorsPerRouter;
		ret[3] = nextHops;
		
		return 0;
		
	}

}
