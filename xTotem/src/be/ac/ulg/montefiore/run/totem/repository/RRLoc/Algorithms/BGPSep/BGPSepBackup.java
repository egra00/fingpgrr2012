package be.ac.ulg.montefiore.run.totem.repository.RRLoc.Algorithms.BGPSep;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.repository.RRLoc.GraphTools.GraphSeparator;
import be.ac.ulg.montefiore.run.totem.repository.RRLoc.GraphTools.Separator;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;

public class BGPSepBackup {
	private static Logger logger = Logger.getLogger(BGPSepBackup.class);
	
	private static void BGPSepAlgorithm(Graph<Node, Link> IGPTopology, List<iBGPSession> i) {
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
				BGPSepAlgorithm(g_i, i);
			}
			
		}
	}
	
	public static void BGPSepAlgorithm(Domain domain) {
		// Topología IGP representada en un grafo jung 
		Graph<Node, Link> jIGPTopology = new UndirectedSparseMultigraph<Node, Link>();
		// Lista que contendrá todas las sesiones iBGP
		List<iBGPSession> iBGPTopology = new ArrayList<iBGPSession>();
		
		// Cargo un nodo Jung con los datos necesarios para realizar el algoritmo
		Operations.addAllVertices(jIGPTopology, (Set<Node>)(new HashSet<Node>(domain.getAllNodes())));
		for (Link link : domain.getAllLinks()) {
			try {
				jIGPTopology.addEdge(link, link.getSrcNode(), link.getDstNode());
			} catch (NodeNotFoundException e) {
				logger.error("Parsing Totem domain to Jung graph");
				e.printStackTrace();
			}
		}
		
		BGPSepAlgorithm(jIGPTopology, iBGPTopology);
		
		logger.info("iBGP sessions ("+iBGPTopology.size()+")");
		for (iBGPSession session: iBGPTopology) {
			logger.info(session.getIdLink1() + " - " + session.getIdLink2() + " -> " + session.getSessionType());
		}
		
	}
}
