package uy.edu.fing.repository.rrloc.tools.graph.separator.bisection;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Bisection_GRASP 
{
	public Bisector run(int[][] G, int Size, int MaxIter, double alfa, double beta)
	{
		Bisector best_solution = new Bisector();
		
		Random ran0 = new Random();
		Random ran1 = new Random();
		Random ran2 = new Random();
		Random ran3 = new Random();
		
		for(int i=0; i<MaxIter; i++)
		{
			//System.out.println("ITERACION "+ i+"..");
			Bisector current_solution = Construct_Greedy_Randomized_Solution(G, Size, ran0, ran1);
			current_solution = Local_Search(G, Size, ran2, ran3, current_solution);
			if (current_solution.isBetter(alfa, beta, best_solution))
				best_solution = current_solution;
		}
		
		return best_solution;
	}


	private Bisector Construct_Greedy_Randomized_Solution(int[][] G, int Size, Random ran0, Random ran1) 
	{
		List<List<Integer>> _l1 = new LinkedList<List<Integer>>();
		List<Integer> _l2 = new LinkedList<Integer>();
		List<Integer> _l3 = new LinkedList<Integer>();
		
		for(int i=0; i<Size; i++)
		{
			_l3 = new LinkedList<Integer>();
			for(int j=0; j<Size; j++)
				if(G[i][j]==0 || G[j][i]==0) _l3.add(j);
			
			if(_l3.size()>0)
			{
				_l2.add(i);
				_l1.add(_l3);
			}
		}
			
		int index = ran0.nextInt(_l2.size());
		int u = _l2.get(index);
		_l3 = _l1.get(index);
		int v = _l3.get(ran1.nextInt(_l3.size()));

		Bisector solution = new Bisector();
		
		for(int i=0; i<Size; i++)
			if(i!=u && i!=v) solution.getS().add(i);
		
		solution.getC1().add(u);
		solution.getC2().add(v);

		return solution;
	}

	
	private boolean Exist_Edge_Between(int u, Set<Integer> C, int[][] G) 
	{
		for(Integer v: C)
			if(G[u][v]==1 || G[v][u]==1) return true;
		
		return false;
	}
	

	private Bisector Local_Search(int[][] G, int Size, Random ran0, Random ran1, Bisector solution)
	{
		List<Integer> _l;
		boolean follow = true;

		
		while (follow)
		{
			follow = false;

			if(solution.getC1().size()<=solution.getC2().size() && !(_l=Nodes_Of_Border_Between_S_C(solution.getS(), solution.getC1(), solution.getC2(), G)).isEmpty())
			{	
				follow = true;
				
				do
				{	
					int node = _l.remove(ran0.nextInt(_l.size()));
					
					solution.getS().remove(node);
					solution.getC1().add(node);
				}
				while(solution.getC1().size()<=solution.getC2().size() && !_l.isEmpty());
			}
			
			if((solution.getC2().size()<=solution.getC1().size() && !(_l=Nodes_Of_Border_Between_S_C(solution.getS(), solution.getC2(), solution.getC1(), G)).isEmpty()))
			{
				follow = true;
				
				do
				{	
					int node = _l.remove(ran1.nextInt(_l.size()));
					
					solution.getS().remove(node);
					solution.getC2().add(node);
				}
				while(solution.getC2().size()<=solution.getC1().size() && !_l.isEmpty());
			}
		}
		
		if (solution.getC1().size()<=0 || solution.getC2().size()<=0 || solution.getS().size()<=0) 
			System.out.println("ANDA MAAAAALLLLLLLLLLLLLLLLLLLL");
		
		return solution;
	}
	

	private List<Integer> Nodes_Of_Border_Between_S_C(Set<Integer> S, Set<Integer> C, Set<Integer> X, int[][] G) 
	{
		List<Integer> l_node = new LinkedList<Integer>();
		
		for(Integer u : S)
			if ((Exist_Edge_Between(u, C, G) && !Exist_Edge_Between(u, X, G)) || (!Exist_Edge_Between(u, C, G) && !Exist_Edge_Between(u, X, G)))
				l_node.add(u);
				
		return l_node;
	}
}
