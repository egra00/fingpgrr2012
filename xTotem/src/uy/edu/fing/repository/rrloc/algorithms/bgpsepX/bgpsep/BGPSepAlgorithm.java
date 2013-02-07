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
	public int run(Object _param, Object result) {
		List<iBGPSession> i = (List<iBGPSession>) result;
		
		Object[] param = (Object[])_param;
		
		Graph<Node, Link> IGPTopology = (Graph<Node, Link>) param[0];

		String SEPARATOR = (String)param[1];
		Integer MAX_ITER = (Integer)param[2];
		Double ALPHA = (Double)param[3];
		Double BETA = (Double)param[4];
		Double GAMA = (Double)param[5];
		
		Integer NB_RUN = (Integer)param[6];
		Integer N_GEN = (Integer)param[7];
		Integer SIZE_P = (Integer)param[8];
		Integer SIZE_OF = (Integer)param[9];
		Double PMUT = (Double)param[10];
		Double PCROSS = (Double)param[11];
		
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
		
			GraphSeparator graphSeparator;
			
			if ("GRASP".equals(SEPARATOR)) {
				graphSeparator = Separator.GRASPBisection(IGPTopology, MAX_ITER, ALPHA, BETA, GAMA);
			}
			else {
				graphSeparator = Separator.GraphPartitionAE(NB_RUN, IGPTopology, N_GEN, SIZE_P, SIZE_OF, PMUT, PCROSS);
			}
			
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
				
				Object[] newParams = new Object[12];
				
				newParams[0] = g_i;
				newParams[1] = SEPARATOR;
				newParams[2] = MAX_ITER;
				newParams[3] = ALPHA;
				newParams[4] = BETA;
				newParams[5] = GAMA;
				newParams[6] = NB_RUN;
				newParams[7] = N_GEN;
				newParams[8] = SIZE_P;
				newParams[9] = SIZE_OF;
				newParams[10] = PMUT;
				newParams[11] = PCROSS;
				
				run(newParams, i);
			}
		}
		
		return 0;
	}
	
	/*
	 * 
	 * Used for independent execution
	 * 
	 */
	public static void main(String args[]) {
		
	}
	
}