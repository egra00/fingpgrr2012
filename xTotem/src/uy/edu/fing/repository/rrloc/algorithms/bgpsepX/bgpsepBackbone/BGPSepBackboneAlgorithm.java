package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepBackbone;

import java.util.LinkedList;
import java.util.List;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.batesX.batesY.BatesYAlgorithm;
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

	public static class Params
	{
		public int rrs;
		public int pops;
		public Graph<Node, Link> graph;
	}
	
	@Override
	public void run(Object in_params, Object out_result) 
	{
		int _rrs = ((Params) in_params).rrs;
		int _pops = ((Params) in_params).pops;
		Graph<Node, Link> igp = ((Params) in_params).graph;
		List<iBGPSession> lst_sessions = (List<iBGPSession>) out_result;
		
		List<List<Node>> lst_cells = KMedoidsGA.kMedoids(15, igp, _pops ,50, 60, 100, 0.01, 0.1);
		List<Node> lst_PoPs_RRs = new LinkedList<Node>();
		
		BatesYAlgorithm bates = new BatesYAlgorithm();
		
		
		for (List<Node> cell : lst_cells)
		{
			lst_PoPs_RRs.addAll(bates.BatesYPoP(igp, cell, _rrs, lst_sessions));
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
