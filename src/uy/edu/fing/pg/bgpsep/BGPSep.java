package uy.edu.fing.pg.bgpsep;

import java.util.ArrayList;
import java.util.List;

import uy.edu.fing.pg.bgpsep.domain.model.Link;
import uy.edu.fing.pg.bgpsep.domain.model.Router;
import uy.edu.fing.pg.bgpsep.domain.model.iBGPSession;
import uy.edu.fing.pg.bgpsep.domain.model.iBGPSessionType;
import uy.edu.fing.pg.bgpsep.io.IO;
import uy.edu.fing.pg.bgpsep.utils.Separator;
import uy.edu.fing.pg.bgpsep.utils.Utils;
import edu.uci.ics.jung.graph.Graph;


public class BGPSep
{
	
	public static void BGPSepAlgorithm(Graph<Router, Link> IGPTopology, List<iBGPSession> i) {
		if (IGPTopology.getVertexCount() < 2) {
			//i = vacio
		}
		else if (IGPTopology.getVertexCount() == 2) {
			Router u = ((Router)IGPTopology.getVertices().toArray()[0]);
			Router v = ((Router)IGPTopology.getVertices().toArray()[1]);
			
			i.add(new iBGPSession(
					u.getIdRouter(), 
					v.getIdRouter(), 
					iBGPSessionType.peer));
		}
		else {
			Separator graphSeparator = Utils.graphSeparator(IGPTopology);
			
			//El conjunto de routes reflectors estará configurado Full Mesh
			for (Router u : graphSeparator.getSeparator()) {
				for (Router v : graphSeparator.getSeparator()) {
					if (u == v)
						continue;
					i.add(new iBGPSession(
							u.getIdRouter(),
							v.getIdRouter(),
							iBGPSessionType.peer));
					
				}
			}
			
			// Cada router en la componente debe ser un cliente de todos
			// los route reflectos
			for (Graph<Router, Link> g_i : graphSeparator.getComponents()) {
				for (Router u : g_i.getVertices()) {
					for (Router v : graphSeparator.getSeparator()) {
						i.add(new iBGPSession(
								u.getIdRouter(), 
								v.getIdRouter(), 
								iBGPSessionType.client));
					}
				}
				BGPSepAlgorithm(g_i, i);
			}
			
		}
	}

	public static void main(String[] args) {
		
		if (args.length < 1) {
			System.out.println("Uso: bgpsep archivo_de_topologia_brite");
			return;
		}
		
		System.out.println("Running...");
		
		String filename = args[0];
		
		Graph<Router,Link> IGPTopology = IO.load(filename);
		
		System.out.println("Routers: " + IGPTopology.getVertexCount());
		System.out.println("Aristas: " + IGPTopology.getEdgeCount());
		
		List<iBGPSession> iBGPTopology = new ArrayList<iBGPSession>();
		BGPSepAlgorithm(IGPTopology, iBGPTopology);
		
		IO.dumpSimpleIBGPFile(iBGPTopology, filename + ".rrl");
		
		System.out.println("Finish");
		
	}

}
