package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsep;

import java.util.HashSet;
import java.util.Iterator;
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
	public void run(Object _param, Object result) {
		List<iBGPSession> i = (List<iBGPSession>) result;
		
		Object[] param = (Object[])_param;
		
		Graph<Node, Link> IGPTopology = (Graph<Node, Link>) param[0];

		Integer MAX_ITER = (Integer)param[1];
		Double ALPHA = (Double)param[2];
		Double BETA = (Double)param[3];
		Double GAMA = (Double)param[4];
		
		if (IGPTopology.getVertexCount() == 2) {
			Iterator<Node> ii = IGPTopology.getVertices().iterator();
			Node u = ii.next();
			Node v = ii.next();
			
			i.add(new iBGPSession(
					u.getId(), 
					v.getId(), 
					iBGPSessionType.peer));

		}
		else if (IGPTopology.getVertexCount() > 2) {
			
			
			/*long time_in = System.currentTimeMillis();
			GraphSeparator graphSeparator = Separator.GRASPBisection(IGPTopology, 25000, 0.035, 0.014, 0.15);
			//GraphSeparator graphSeparator = Separator.GraphPartitionAE(15, IGPTopology ,50, 500, 800, 0.01, 0.2);
			long time_out = System.currentTimeMillis();
			
			System.out.println("Graph: "+IGPTopology.getVertexCount()+"\t"+IGPTopology.getEdgeCount()+"\t"+(((double)IGPTopology.getEdgeCount())/(IGPTopology.getVertexCount() * (IGPTopology.getVertexCount() - 1)) / 2));
			
			System.out.println("Separator: "+graphSeparator.getSeparator().size()+"\t"+ ((double)(graphSeparator.getSeparator().size()))/IGPTopology.getVertexCount()+"\t"+graphSeparator.getComponents().size());
			int can=0;
			double media=0;
			System.out.println("Detail  of components:");
			for(Graph<Node, Link> comp: graphSeparator.getComponents())
			{
				System.out.println("\t\t C"+can+": "+comp.getVertexCount());
				media+= comp.getVertexCount();
				can++;
			}
			media = media/graphSeparator.getComponents().size();
			System.out.println("\tMedia: "+ media);
			double balanced =0;
			for(Graph<Node, Link> comp : graphSeparator.getComponents())
				balanced += Math.abs(comp.getVertexCount() - media);
			System.out.println("\tBalanced: "+ balanced);
			System.out.println("Time (ms): "+ (time_out - time_in));
			
			for (Node u : graphSeparator.getSeparator()) {
				for (Node v : graphSeparator.getSeparator()) {
					i.add(new iBGPSession(u.getId(), v.getId(), iBGPSessionType.client));
				}
			}*/ 
			
			GraphSeparator graphSeparator = Separator.GRASPBisection(IGPTopology, MAX_ITER, ALPHA, BETA, GAMA);
			
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
				
				Object[] newParams = new Object[5];
				newParams[0] = g_i;
				newParams[1] = MAX_ITER;
				newParams[2] = ALPHA;
				newParams[3] = BETA;
				newParams[4] = GAMA;
				
				run(newParams, i);
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