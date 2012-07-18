package uy.edu.fing.repository.rrloc.graphTools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import agape.tools.Components;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseGraph;

public class GraphPartitionGA 
{
	int _nbgen;
	int _sizepopu;
	int _sizeoffs;
	int _tam_individuo;
	double _pmut;
	double _pcross;
	Graph<Node,Link> _G;
	
	private int[][] population;
	private double[] fit_population;
	private double[] intervals;
	private double _Fsum;
	private int[][] offsprings;
	
	public GraphPartitionGA(Graph<Node,Link> G, int nb_gen, int tam_popu, int tam_offs, double p_mut, double p_cross)
	{
		_nbgen = nb_gen;
		_sizepopu = tam_popu;
		_sizeoffs = tam_offs;
		_pmut = p_mut;
		_pcross = p_cross;
		_G = Operations.copyUndirectedSparseGraph(G);
		_tam_individuo = _G.getVertexCount();
		
		population = new int[_sizepopu][];
		fit_population = new double[_sizepopu];
		intervals = new double[_sizepopu + 1];
		offsprings = new int[_sizeoffs][];
	}
	
	public void Initialization()
	{
		for(int i =0; i<_sizepopu; i++)
		{			
			population[i] = randomIndi(Math.random());
			//System.out.println("");
		}
	}
	
	private int[] randomIndi(double seed) 
	{
		int[] indi = new int[_tam_individuo];
		
		for(int i = 0; i<_tam_individuo; i++)
		{	
			if (Math.random() < seed)
				indi[i] = 0;
			else
				indi[i] = 1;
			
			//System.out.print(indi[i]+"-");
		}
		
		return indi;
	}
	
	public void Crossover()
	{
		for(int i =0; i+1<_sizeoffs; i=i+2)
		{
			 	if (Math.random() <= _pcross) cross(offsprings[i],offsprings[i+1]);
		}
	}
	
	private void cross(int[] ind1, int[] ind2)
	{
		Random ram = new Random();
		int pointCross = ram.nextInt(_tam_individuo);
		int aux;
		
		for(int j=0; j<pointCross; j++)
		{
			aux = ind1[j];
			ind1[j] = ind2[j];
			ind2[j] = aux;
		}
	}
	
	public void Mutation()
	{
		for(int i =0; i<_sizeoffs; i++)
		{
			mut(offsprings[i]);
		}
	}
	
	
	private void mut(int[] ind)
	{
		for(int j=0; j<_tam_individuo; j++)
		{
			if(Math.random() <= _pmut)
			{
				ind[j]=(ind[j]+1)%2;
			}
		}
	}
	
	public void Recombine()
	{
		Crossover();
		Mutation();
	}
	
	
	public void Selection()
	{
		// Roulette Wheel //
		intervals[0] = 0;
		for(int i=1; i<=_sizepopu; i++)
		{
			intervals[i] = intervals[i-1] + fit_population[i-1]/_Fsum;
		}
		
		double ram;
		int index;
		for(int i=0; i<_sizeoffs; i++)
		{
			ram = Math.random();
			index = 0;
			while(index <= _sizepopu && intervals[index] <= ram)
				index++;
			
			int ori[] = population[index-1];
			int cpy[] = new int[_tam_individuo];
			for(int k=0; k<_tam_individuo; k++)
			{
				cpy[k] = ori[k];
			}
			
			offsprings[i] = cpy;
		}	
	}
	
	
	public void Evaluate()
	{
		_Fsum = 0;
		for(int i=0; i<_sizepopu; i++)
		{
			fit_population[i] = fitness_function(population[i]);
			_Fsum += fit_population[i];
			//System.out.println(" / "+fitness_evaluation[i]);
		}
	}
	
	private  double fitness_function(int[] indi) 
	{
		// Set separator
		Set<Node> set =  new HashSet<Node>();
		int i = 0;
		int tamIndi = 0;
		
		for(Node n : _G.getVertices())
		{
			if (indi[i]==1) 
			{
				set.add(n);
				//System.out.print(n.getDescription() +" / ");
				tamIndi++;
			}
			i++;
		}
		
		
		// Components
		Graph<Node, Link> aux = Operations.copyUndirectedSparseGraph(_G);
		Operations.removeAllVertices(aux, set);
		int cantCompConex = 0;
		double media = 0;
		List<Graph<Node, Link>> components = new LinkedList<Graph<Node,Link>>();
		
		for (Set<Node> cc : Components.getAllConnectedComponent(aux)) {
			Graph<Node, Link> component = new UndirectedSparseGraph<Node, Link>();
			Operations.subGraph(aux, component, cc);
			components.add(component);
			media += component.getVertexCount();
			cantCompConex++;
		}
		
		if (cantCompConex <= 1) // No es un grafo separador, penalizo
		{
			return 1;
		}
		
		
		media = media/cantCompConex;

		Iterator<Graph<Node, Link>> ii = components.iterator();
		double sum = 0;
		while(ii.hasNext())
		{
			int x = ii.next().getVertexCount();
			sum += (x-media)*(x-media); 
		}
	
		
		double desviacion = Math.sqrt(sum/(cantCompConex-1));
		double max_desviacion = Math.sqrt((_tam_individuo*((_tam_individuo-2)*(_tam_individuo-2)))/(_tam_individuo-1));
		
		//System.out.print("/ Size:" + size + "/ Media:"+ media + "/ max_Desviacion:" + max_desviacion + "/ Desviacion:" + desviacion + "/ cantCompConex:" + cantCompConex);

		
		return (_tam_individuo-tamIndi) + cantCompConex + (max_desviacion - desviacion) + 1;
	}
	
	public void Remplace()
	{
		Random ram = new Random();
		int index;
		
		for(int i=0; i <_sizepopu; i++)
		{
			index = ram.nextInt(_sizeoffs);
			
			population[i] = offsprings[index];
		}
	}
	
	
	public GraphSeparator GetBestSolution()
	{
		int key = 0;
		int fit_total = 0;
		double best = fit_population[0];
		
		for(int i=1; i<_sizepopu; i++)
		{
			double fit = fit_population[i];
			if (best<fit)
			{
				key = i;
				best = fit;
			}
			fit_total += fit;
		}
		
		if (fit_total == _sizepopu)
		{
			return run();
		}
			 
		
		int[] indi = population[key];
		
		//Creo separador de grafo
		GraphSeparator SG = new GraphSeparator();
		
		// Set separator
		Set<Node> set =  new HashSet<Node>();
		int i = 0;
		
		//System.out.println("///////////////"+ _G.getVertexCount());
		
		for(Node n : _G.getVertices())
		{
			if (indi[i]==1) 
			{
				set.add(n);
				//System.out.println(n.getDescription());
			}
			i++;
			
		}
		
		SG.setSeparator(set);
		SG.setComponents(new ArrayList<Graph<Node, Link>>());
		
		// Components
		Graph<Node, Link> aux = Operations.copyUndirectedSparseGraph(_G);
		Operations.removeAllVertices(aux, set);
		for (Set<Node> iter : Components.getAllConnectedComponent(aux)) {
			Graph<Node, Link> component = new UndirectedSparseGraph<Node, Link>();
			Operations.subGraph(aux, component, iter);
			SG.getComponents().add(component);
		}
		
		return SG;
	}
	
	
	public GraphSeparator run()
	{
		Initialization();
		Evaluate();
		
		for(int i=0; i<_nbgen; i++)
		{
			Selection();
			Recombine();
			Remplace();
			Evaluate();
		}

		return GetBestSolution();
		
	}
}
