package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepBackbone;

import java.util.LinkedList;
import java.util.List;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.batesX.batesZ.BatesZAlgorithm;
import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepD.BGPSepDAlgorithm;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import uy.edu.fing.repository.rrloc.tools.graph.kmedoids.KMedoidsGA;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseGraph;

@SuppressWarnings("unchecked")
public class BGPSepBackboneAlgorithm implements RRLocAlgorithm
{

	@Override
	public void run(Object _in_params, Object out_result) {
		
		Object[] in_params = (Object[])_in_params;
		
		Graph<Node, Link> igp = (Graph<Node, Link>)in_params[0];
		int _pops = (Integer)in_params[1];
		
		Integer MAX_ITER = (Integer)in_params[2];
		Double ALPHA = (Double)in_params[3];
		Double BETA = (Double)in_params[4];
		Double GAMA = (Double)in_params[5];
		
		Integer NB_RUN = (Integer)in_params[6];
		Integer N_GEN = (Integer)in_params[7];
		Integer SIZE_P = (Integer)in_params[8];
		Integer SIZE_OF = (Integer)in_params[9];
		Double PMUT = (Double)in_params[10];
		Double PCROSS = (Double)in_params[11];
		
		List<iBGPSession> lst_sessions = (List<iBGPSession>) out_result;
		
		List<List<Node>> lst_cells = KMedoidsGA.kMedoids(NB_RUN, igp, _pops, N_GEN, SIZE_P, SIZE_OF, PMUT, PCROSS);
		List<Node> lst_PoPs_RRs = new LinkedList<Node>();
		
		BatesZAlgorithm bates = new BatesZAlgorithm();
		
		
		for (List<Node> cell : lst_cells)
		{
			lst_PoPs_RRs.addAll(bates.BatesZPoP(igp, cell, lst_sessions));
		}
		
		
		Graph<Node, Link> core =  backbone_net(igp, lst_PoPs_RRs);
		BGPSepDAlgorithm bgpsepD = new BGPSepDAlgorithm();
		
		
		bgpsepD.run(core, lst_sessions);	
	}
	
	
	public Graph<Node, Link> backbone_net(Graph<Node, Link> igp, List<Node> lst_PoPs_RRs)
	{
		
		List<Node> lst_nodes = new LinkedList<Node>(lst_PoPs_RRs);
		
		Graph<Node, Link> core = new UndirectedSparseGraph<Node, Link>();
		
		for(;!lst_nodes.isEmpty();)
		{
			Node n1 = lst_nodes.remove(0);
			for(Node n2 : lst_nodes)
			{
				Link link = igp.findEdge(n1, n2);
				if(link != null) core.addEdge(link, n1, n2);
			}
		}
				
		return  core;
	}
}
