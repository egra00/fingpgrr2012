package uy.edu.fing.repository.rrloc.tools.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
	private int[][] offsprings;
	
	final double PENALIZACION = 0.001; // Usada en caso de que el grafo elegido no sea un grafo separador
	final int SIZECAKE = 10000;
	private int[] cake;
	
	// Conservo la mejor solucion global
	private int[] _best_sol_global;
	private double _fitness_best_sol_global;
	private boolean _isSeparator_best_sol_global;
	
	// Conservo la mejor solucion en cada generacion
	private int[] _best_sol_iter; // Inicializada luego de hacer Evaluate()
	private double _fitness_best_sol_iter; // Inicializada luego de hacer Evaluate()
	private boolean _isSeparator_best_sol_iter; // Inicializada luego de hacer Evaluate()
	
	private double _FitTotal; // Inicializada luego de hacer Evaluate()

	
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
		offsprings = new int[_sizeoffs][];
		
		cake = new int[SIZECAKE];
	}
	
	public void Initialization()
	{
		_isSeparator_best_sol_global = false;
		_fitness_best_sol_global = PENALIZACION; // Cualquier solucion tiene valor de fitness mayor estricto a PENALIZACION
		
		for(int i =0; i<_sizepopu; i++)
		{			
			population[i] = randomIndi(Math.random());
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
		this.Crossover();
		this.Mutation();
	}
	
	
	public void Selection()
	{
		// Roulette Wheel //
		int _size_cake = 0;
		int portions;
		int j;
		
		for(int i=0; (i < _sizepopu) && (_size_cake < SIZECAKE); i++)
		{
			portions = (int)Math.floor((fit_population[i]/_FitTotal)*10000);
			for(j=_size_cake; (j < SIZECAKE) && (j < _size_cake + portions); j++)
			{
				cake[j] = i;
			}
			_size_cake += portions;

		}
		
		if (_size_cake > SIZECAKE) _size_cake = SIZECAKE; // corrijo por las dudas, puede fallar.....
		
		Random ram = new Random();
		int index;
		int cpy[];
		for(int i=0; i<_sizeoffs; i++)
		{
			index = cake[ram.nextInt(_size_cake)];
			cpy = new int[_tam_individuo];
			
			System.arraycopy(population[index], 0, cpy, 0, _tam_individuo);
			offsprings[i] = cpy;
		}
	}
	
	
	public void Evaluate()
	{
		_fitness_best_sol_iter = PENALIZACION;
		_isSeparator_best_sol_iter = false;
		
		
		fit_population[0] = fitness_function(population[0]);
		_FitTotal = fit_population[0];
		if (PENALIZACION < fit_population[0]) // Cualquier solucion tiene valor de fitness mayor estricto a PENALIZACION
		{
			_best_sol_iter = population[0];
			_fitness_best_sol_iter = fit_population[0];
			_isSeparator_best_sol_iter = true;
		}
		
		for(int i=1; i<_sizepopu; i++)
		{
			fit_population[i] = fitness_function(population[i]);
			_FitTotal += fit_population[i];
			
			if ((PENALIZACION < fit_population[i]) && _fitness_best_sol_iter <= fit_population[i])
			{
				_best_sol_iter = population[i];
				_fitness_best_sol_iter = fit_population[i];
				_isSeparator_best_sol_iter = true;
			}
		}
		
		this.SetGlobalBestSolution();
	}
	
	public void SetGlobalBestSolution()
	{
		if (_isSeparator_best_sol_iter && _fitness_best_sol_global <= _fitness_best_sol_iter)
		{
			_best_sol_global = _best_sol_iter;
			_fitness_best_sol_global = _fitness_best_sol_iter;
			_isSeparator_best_sol_global = true;
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
				tamIndi++;
			}
			i++;
		}
		
		// Components
		Graph<Node, Link> aux = Operations.copyUndirectedSparseGraph(_G);
		Operations.removeAllVertices(aux, set);
		int cantCompConex = 0;
		double media = 0;
		
		for (Set<Node> cc : Components.getAllConnectedComponent(aux)) {
			media += cc.size();
			cantCompConex++;
		}
		
		if (cantCompConex <= 1) // No es un grafo separador, penalizo 
		{
			return PENALIZACION;
		}

		media = media/cantCompConex;

		Iterator<Set<Node>> ii = Components.getAllConnectedComponent(aux).iterator();
		double sum = 0;
		int x;
		while(ii.hasNext())
		{
			x = ii.next().size();
			sum += (x-media)*(x-media); 
		}
	
		
		double desviacion = Math.sqrt(sum/(cantCompConex-1));
		
		return ((double)(_tam_individuo - tamIndi)/_tam_individuo) + ((double)cantCompConex/(_tam_individuo-1)) + (1/Math.exp(desviacion)) + PENALIZACION;
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
		if (!_isSeparator_best_sol_global)
		{
			return this.run();
		}
			 
		
		int[] indi = _best_sol_global;
		
		//Creo separador de grafo
		GraphSeparator SG = new GraphSeparator();
		
		// Set separator
		Set<Node> set =  new HashSet<Node>();
		int i = 0;
		
		for(Node n : _G.getVertices())
		{
			if (indi[i]==1) 
				set.add(n);
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
		this.Initialization();
		this.Evaluate();
		
		for(int i=0; i<_nbgen; i++)
		{
			this.Selection();
			this.Recombine();
			this.Remplace();
			this.Evaluate();
		}

		return this.GetBestSolution();	
	}
}
