package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepBackbone;

import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.batesX.batesZ.BatesZAlgorithm;
import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepD.BGPSepDAlgorithm;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import uy.edu.fing.repository.rrloc.tools.graph.kmedoids.KMedoidsGA;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseGraph;

@SuppressWarnings("unchecked")
public class BGPSepBackboneAlgorithm implements RRLocAlgorithm
{

	@Override
	public int run(Object _in_params, Object out_result) {
		
		Object[] in_params = (Object[])_in_params;
		
		Graph<Node, Link> igp = (Graph<Node, Link>)in_params[0];
		String SEPARATOR = (String)in_params[1];
		Integer POPS = (Integer)in_params[2];
		
		Integer MAX_ITER = (Integer)in_params[3];
		Double ALPHA = (Double)in_params[4];
		Double BETA = (Double)in_params[5];
		Double GAMA = (Double)in_params[6];
		
		Integer KM_NB_RUN = (Integer)in_params[7];
		Integer KM_N_GEN = (Integer)in_params[8];
		Integer KM_SIZE_P = (Integer)in_params[9];
		Integer KM_SIZE_OF = (Integer)in_params[10];
		Double KM_PMUT = (Double)in_params[11];
		Double KM_PCROSS = (Double)in_params[12];
		
		Integer NB_RUN = (Integer)in_params[13];
		Integer N_GEN = (Integer)in_params[14];
		Integer SIZE_P = (Integer)in_params[15];
		Integer SIZE_OF = (Integer)in_params[16];
		Double PMUT = (Double)in_params[17];
		Double PCROSS = (Double)in_params[18];
		
		List<iBGPSession> lst_sessions = (List<iBGPSession>) out_result;
		
		List<List<Node>> lst_cells = null;
		
		try {
			lst_cells = KMedoidsGA.kMedoids(KM_NB_RUN, igp, POPS, KM_N_GEN, KM_SIZE_P, KM_SIZE_OF, KM_PMUT, KM_PCROSS);
		}
		catch (Exception e) {
			if (MainWindow.cliMode()) {
				System.err.println("Invalid POPS size");
			}
			else {
				JOptionPane.showMessageDialog(MainWindow.getInstance(), "Invalid POPS size");	
			}
			return -1;
		};
		
		List<Node> lst_PoPs_RRs = new LinkedList<Node>();
		
		BatesZAlgorithm bates = new BatesZAlgorithm();
		
		for (List<Node> cell : lst_cells) {
			lst_PoPs_RRs.addAll(bates.BatesZPoP(igp, cell, lst_sessions));
		}
		
		Graph<Node, Link> core =  backbone_net(igp, lst_PoPs_RRs);
		BGPSepDAlgorithm bgpsepD = new BGPSepDAlgorithm();
		
		Object[] newParams = new Object[12];
		newParams[0] = core;
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
		
		return bgpsepD.run(newParams, lst_sessions);	
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
