package uy.edu.fing.repository.rrloc.tools.graph.kmedoids;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import uy.edu.fing.repository.rrloc.tools.graph.kmedoids.model.Coord;
import uy.edu.fing.repository.rrloc.tools.graph.kmedoids.model.KMedoids;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;

public class KMedoidsGA 
{
	
	public static List<Graph<Node, Link>> kMedoids(int nb_run, Graph<Node,Link> G, int pops, int nGen, int sizeP, int sizeOf, double pmut, double pcross)
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
		
		HashMap<Integer, Set<Node>> hash = new HashMap<Integer, Set<Node>>();
		List<Graph<Node, Link>> lst_pops = new 	LinkedList<Graph<Node, Link>>();
		int i=0;
		
		/*int mmm =0;
		for(Node node : G.getVertices())
		{
			System.out.print("    /   "+ mmm+": "+node.getId());
			mmm++;
		}
		System.out.println("");*/
		
		
		//System.out.println("////////////////////   "+ _fitness_best_solution +"   ////SUM    "+ Math.pow(((1-_fitness_best_solution)/_fitness_best_solution), 2));
		
		/*System.out.println("////////////////////   "+ _fitness_best_solution );
		for(int k = 0; k<G.getVertexCount(); k++)
		{	
			System.out.print("-"+_best_solution[k]);
		}
		System.out.println("");*/
		
		for(Node node : G.getVertices())
		{
			int med = _best_solution[i];
			if(!hash.containsKey(med))
			{
				//System.out.println("/////MED: "+ med);
				Set<Node> set = new HashSet<Node>();
				hash.put(med, set);
			}
			
			hash.get(med).add(node);
			i++;
		}
		
		Graph<Node, Link> subG;
		Graph<Node, Link> aux = Operations.copyUndirectedSparseGraph(G);
		
	//	System.out.println("/////TAM: "+ hash.values().size());
		
		for(Set<Node> set : hash.values())
		{
			//System.out.println("MED TAM  /  "+set.size());
			subG = Operations.copyUndirectedSparseGraph(aux);
			Operations.removeAllVertices(aux, set);
			Operations.removeAllVertices(subG, new HashSet<Node>(aux.getVertices()));
			lst_pops.add(subG);
		}
				
		return lst_pops;
	}
}
