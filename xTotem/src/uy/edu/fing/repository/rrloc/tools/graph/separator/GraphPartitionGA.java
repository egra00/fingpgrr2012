package uy.edu.fing.repository.rrloc.tools.graph.separator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import uy.edu.fing.repository.rrloc.tools.graph.separator.model.GraphSeparator;
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
	
	final int SIZECAKE = 100000;
	private int[] cake;
	
	// Conservo la mejor solucion global
	private int[] _best_sol_global;
	private double _fitness_best_sol_global;
	
	// Conservo la mejor solucion en cada generacion
	private int[] _best_sol_iter; // Inicializada luego de hacer Evaluate()
	private double _fitness_best_sol_iter; // Inicializada luego de hacer Evaluate()
	
	private double _FitTotal; // Inicializada luego de hacer Evaluate()
	
	private double _more_latitude; // Inicializada luego de hacer get_frame();
	private double _less_latitude; // Inicializada luego de hacer get_frame();
	
	private double _more_longitude; // Inicializada luego de hacer get_frame();
	private double _less_longitude; // Inicializada luego de hacer get_frame();

	
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
		
		_more_latitude = Double.MIN_VALUE;
		_less_latitude = Double.MAX_VALUE;
		
		_more_longitude = Double.MIN_VALUE;
		_less_longitude = Double.MAX_VALUE;
		
		this.get_frame();
	}
	
	
	private void get_frame()
	{
		_more_latitude = Double.MIN_VALUE;
		_less_latitude = Double.MAX_VALUE;
		
		_more_longitude = Double.MIN_VALUE;
		_less_longitude = Double.MAX_VALUE;
		
		double lon;
		double lat;
		
		for(Node node: _G.getVertices())
		{
			lat = node.getLatitude();
			lon = node.getLongitude();
			
			if(_more_latitude < lat) _more_latitude = lat;
			if(_more_longitude < lon) _more_longitude = lon;
			
			if(_less_latitude > lat) _less_latitude = lat;
			if(_less_longitude > lon) _less_longitude = lon;		
		}
		
		_more_latitude++;
		_less_latitude--;
		
		_more_longitude++;
		_less_longitude--;
	}
	
	
	public void Initialization()
	{
		_fitness_best_sol_global = 0;
		Random ram0 = new Random();
		Random ram1 = new Random();
		Random ram2 = new Random();
		
		for(int i =0; i<_sizepopu; i++)
		{			
			population[i] = randomIndi(ram0, ram1, ram2);
		}
	}
	
	private int[] randomIndi(Random ram0, Random ram1, Random ram2) 
	{
		int[] indi = new int[_tam_individuo];
		int sel = ram0.nextInt(3);
		
		if (sel == 0)
			longuitudinal_cut(indi, ram1, ram2);
		else if (sel == 1)
			latitudinal_cut(indi, ram1, ram2);
		else
			diagonal_cut(indi, ram1, ram2);
			
		return indi;
	}
	
	private void longuitudinal_cut(int[] indi, Random ram1, Random ram2)
	{
		double pointB = ram1.nextInt((int)_more_longitude - (int)_less_longitude) + Math.random();
		double pointA = ram2.nextInt(((int)pointB) + 1) + Math.random();
		
		pointB += _less_longitude;
		pointA += _less_longitude;
		
		int pos=0;
		for(Node node : _G.getVertices())
		{
			if(pointA <= node.getLongitude() && node.getLongitude() <= pointB)
				indi[pos] = 1;
			else
				indi[pos] = 0;
			
			pos++;
		}
	}
	
	private void latitudinal_cut(int[] indi, Random ram1, Random ram2)
	{
		double pointB = ram1.nextInt((int)_more_latitude - (int)_less_latitude) + Math.random();
		double pointA = ram2.nextInt(((int)pointB) + 1) + Math.random();
		
		pointB += _less_latitude;
		pointA += _less_latitude;
		
		int pos=0;
		for(Node node : _G.getVertices())
		{
			if(pointA <= node.getLatitude() && node.getLatitude() <= pointB)
				indi[pos] = 1;
			else
				indi[pos] = 0;
			
			pos++;
		}
	}
	
	
	private void diagonal_cut(int[] indi, Random ram1, Random ram2)
	{
		double point1_X = _less_longitude + ram1.nextInt((int)_more_longitude - (int)_less_longitude) + Math.random();
		double point1_Y = _less_latitude + ram2.nextInt((int)_more_latitude - (int)_less_latitude) + Math.random();
	
		double point2_X = _less_longitude + ram1.nextInt((int)_more_longitude - (int)_less_longitude) + Math.random();
		double point2_Y = _less_latitude + ram2.nextInt((int)_more_latitude - (int)_less_latitude) + Math.random();
		
		double _a = (point1_X - point2_X)/(point1_Y - point2_Y);
		double _b = point2_Y - _a*point2_X;
		double _bp = _b - ram2.nextInt(1+(int)Math.min(point1_Y, point2_Y) - (int)_less_latitude) + Math.random();
		
		
		int pos=0;
		double lat;
		double lon;
		for(Node node : _G.getVertices())
		{
			lat = node.getLatitude();
			lon = node.getLongitude();
			if(lat <= evaluate_recta(_a, lon, _bp) && lat <= evaluate_recta(_a, lon, _b))
				indi[pos] = 1;
			else
				indi[pos] = 0;
			
			pos++;
		}
		
	}
	
	private double evaluate_recta(double a, double x, double b)
	{
		return a*x+b;
	}
	
	public void Crossover()
	{
		Random ram = new Random();
		for(int i =0; i+1<_sizeoffs; i=i+2)
		{
			 if (Math.random() <= _pcross) cross(offsprings[i], offsprings[i+1], ram);
		}
	}
	
	private void cross(int[] ind1, int[] ind2, Random ram)
	{
		int pointB = ram.nextInt(_tam_individuo);
		int pointA = ram.nextInt(pointB + 1);
		int aux;
		
		for(int j=pointA; j<pointB; j++)
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
			mut(offsprings[i], Math.random());
		}
	}
	
	
	private void mut(int[] indi, double seed)
	{
		for(int j=0; j<_tam_individuo; j++)
		{
			if(Math.random() <= _pmut)
			{
				if (Math.random() <= seed)
					indi[j] = 0;
				else
					indi[j] = 1;
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
			portions = (int)Math.floor((fit_population[i]/_FitTotal)*SIZECAKE);
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
		fit_population[0] = fitness_function(population[0]);
		_FitTotal = fit_population[0];
		
		_best_sol_iter = population[0];
		_fitness_best_sol_iter = fit_population[0];
	
		for(int i=1; i<_sizepopu; i++)
		{
			fit_population[i] = fitness_function(population[i]);
			_FitTotal += fit_population[i];
			
			if (_fitness_best_sol_iter <= fit_population[i])
			{
				_best_sol_iter = population[i];
				_fitness_best_sol_iter = fit_population[i];
			}
		}
		
		this.SetGlobalBestSolution();
	}
	
	public void SetGlobalBestSolution()
	{
		if (_fitness_best_sol_global <= _fitness_best_sol_iter)
		{
			_best_sol_global = _best_sol_iter;
			_fitness_best_sol_global = _fitness_best_sol_iter;
		}
	}
	
	public  double fitness_function(int[] indi) 
	{
		// Set separator
		Set<Node> set =  new HashSet<Node>();
		int i = 0;
		int tamIndi = 0;
		
		for(Node n : _G.getVertices())
		{
			if (indi[i]>0) 
			{
				set.add(n);
				tamIndi++;
			}
			i++;
		}
		
		// Components
		Graph<Node, Link> aux = Operations.copyUndirectedSparseGraph(_G);
		Operations.removeAllVertices(aux, set);
		List<Set<Node>> lst_ccs = Components.getAllConnectedComponent(aux);
		
		int cantCompConex = 0;
		double media = 0;
		for (Set<Node> cc : lst_ccs){
			media += cc.size();
			cantCompConex++;
		}
				
		media = media/cantCompConex;

		double balanced = 0;
		for(Set<Node> cc : lst_ccs)
			balanced += Math.abs(cc.size() - media);

		return  evaluate_guy(tamIndi, balanced, cantCompConex);	
	}
	
	private double evaluate_guy(double _tam_separator, double _balanced_separator, double _cant_component){		
		if(_cant_component > 1)
			return ((((2*_tam_individuo) + 1)/(_balanced_separator + 1)) * (_tam_individuo/_tam_separator)) + 1;
		
		return 1;
	}
	
	public void Replace()
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
		if (!is_separator(_best_sol_global))
			return this.run();
			 		
		//Creo separador de grafo
		GraphSeparator SG = new GraphSeparator();
		
		// Set separator
		Set<Node> set =  new HashSet<Node>();
		int i = 0;
		
		for(Node n : _G.getVertices())
		{
			if (_best_sol_global[i] > 0) 
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
	
	
	public double get_fitness_best_sol_global() 
	{
		return _fitness_best_sol_global;
	}
	
	private boolean is_separator(int[] indi) 
	{
		// Set separator
		Set<Node> set =  new HashSet<Node>();
		int i = 0;
		for(Node n : _G.getVertices())
		{
			if (indi[i]>0) set.add(n);
			i++;
		}
		
		// Components
		Graph<Node, Link> aux = Operations.copyUndirectedSparseGraph(_G);
		Operations.removeAllVertices(aux, set);
		List<Set<Node>> lst_ccs = Components.getAllConnectedComponent(aux);
				
		return (lst_ccs.size() > 1);
	}

	public GraphSeparator run()
	{		
		this.Initialization();
		this.Evaluate();
		
		for(int i=0; i<_nbgen; i++)
		{
			this.Selection();
			this.Recombine();
			this.Replace();
			this.Evaluate();
		}

		return this.GetBestSolution();	
	}
}
