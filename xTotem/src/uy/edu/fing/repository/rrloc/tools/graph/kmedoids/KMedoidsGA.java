package uy.edu.fing.repository.rrloc.tools.graph.kmedoids;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import uy.edu.fing.repository.rrloc.tools.graph.kmedoids.model.Coord;
import uy.edu.fing.repository.rrloc.tools.graph.kmedoids.model.KMedoids;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;

public class KMedoidsGA 
{
	
	public static List<List<Node>> kMedoids(int nb_run, Graph<Node,Link> G, int pops, int nGen, int sizeP, int sizeOf, double pmut, double pcross)
	{
		Coord[] coord = new Coord[G.getVertexCount()];
		int cant = 0;
		int[] _best_solution;
		int[] _current_solution;
		double _fitness_best_solution;
		
		for(Node node: G.getVertices())
		{
			coord[cant] = new Coord(node.getLongitude(), node.getLatitude());
			cant++;
		}
		
		
		KMedoids km = new KMedoids(coord, pops, nGen, sizeP, sizeOf, pmut, pcross);
		_best_solution = km.run();
		_fitness_best_solution = km.get_fitness_best_sol_global();
		
		for(int i=1; i< nb_run; i++)
		{
			_current_solution = km.run();
			if (_fitness_best_solution <= km.get_fitness_best_sol_global())
			{
				_best_solution = _current_solution;
				_fitness_best_solution = km.get_fitness_best_sol_global();
			}
		}
		
		HashMap<Integer, List<Node>> hash = new HashMap<Integer, List<Node>>();
		List<List<Node>> lst_pops = new LinkedList<List<Node>>();
		int i=0;
		
		for(Node node : G.getVertices())
		{
			int med = _best_solution[i];
			if(!hash.containsKey(med))
			{

				List<Node> set = new LinkedList<Node>();
				hash.put(med, set);
			}
			
			hash.get(med).add(node);
			i++;
		}
		
		
		for(List<Node> set : hash.values())
			lst_pops.add(set);
				
		return lst_pops;
	}
}
