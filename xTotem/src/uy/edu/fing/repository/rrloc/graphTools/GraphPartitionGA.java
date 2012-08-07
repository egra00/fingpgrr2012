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
	private int[][] offsprings;
	private int[] _best_sol_global;
	private boolean _isSeparator_best_sol_global;
	private int _size_best_sol_global;
	
	private double[] intervals;
	
	
	private int _size_indi; // Inicializada luego de hacer fitness_function()
	private boolean _isSeparator_indi; // Inicializada luego de hacer fitness_function()
	
	private double _FitTotal; // Inicializada luego de hacer Evaluate()
	private int[] _best_sol_iter; // Inicializada luego de hacer Evaluate()
	private int _size_best_sol_iter; // Inicializada luego de hacer Evaluate()
	private boolean _isSeparator_best_sol_iter; // Inicializada luego de hacer Evaluate()
	
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
		_best_sol_iter = new int[_tam_individuo];
	}
	
	public void Initialization()
	{
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
		/*int esperado;
		double ceil;
		double floor;
		
		for(int i = 0, aux = 0; i< _sizepopu && aux < _sizepopu; i++)
		{
			//Valor esperado
			ceil = Math.ceil((fit_population[i]/_FitTotal)*_sizepopu);
			floor = Math.floor((fit_population[i]/_FitTotal)*_sizepopu);
			
			if (((fit_population[i]/_FitTotal)*_sizepopu) - floor > 0.5)
				esperado = (int)ceil;
			else
				esperado = (int)floor;
					
			//System.out.println(esperado);
			for(int j = 0; (j < esperado && aux<_sizepopu)|| (i+1 == _sizepopu && aux<_sizepopu); j++, aux++)
				intervals[aux] = i;
		}
		
		Random ram = new Random();
		int index;
		int cpy[];
		for(int i=0; i<_sizeoffs; i++)
		{
			index = intervals[ram.nextInt(_sizepopu)];
			cpy = new int[_tam_individuo];
			
			System.arraycopy(population[index], 0, cpy, 0, _tam_individuo);
			offsprings[i] = cpy;
		}*/
		
		
		intervals[0] = 0;
		for(int i=1; i<=_sizepopu; i++)
		{
			intervals[i] = intervals[i-1] + fit_population[i-1]/_FitTotal;
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
		fit_population[0] = fitness_function(population[0]);
		_FitTotal = fit_population[0];
		if (_isSeparator_indi)
		{
			_best_sol_iter = population[0];
			_size_best_sol_iter = _size_indi;
		}
		_isSeparator_best_sol_iter = _isSeparator_indi;
		
		
		for(int i=1; i<_sizepopu; i++)
		{
			fit_population[i] = fitness_function(population[i]);
			_FitTotal += fit_population[i];
			
			if (_isSeparator_indi && _size_best_sol_iter > _size_indi)
			{
				_best_sol_iter = population[i];
				_size_best_sol_iter = _size_indi;
				_isSeparator_best_sol_iter = true;
			}
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
		
		
		//if (tamIndi < 1) // No es un grafo separador, penalizo
			//return 1.0;
		
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
			_isSeparator_indi = false;
			return 1.0;
		}
		
		
		_size_indi = tamIndi;
		_isSeparator_indi = true;
		
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
		
		return (_tam_individuo - tamIndi) + cantCompConex + (max_desviacion - desviacion) + 1.0;
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
	
	public void SetBestSolution()
	{
		if (_isSeparator_best_sol_iter && _size_best_sol_iter < _size_best_sol_global)
		{
			_best_sol_global = _best_sol_iter;
			_size_best_sol_global = _size_best_sol_iter;
			_isSeparator_best_sol_global = true;
		}
	}
	
	public GraphSeparator GetBestSolution()
	{	
		if (!_isSeparator_best_sol_global)
		{
			//System.out.println("//////////////////ERRORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR");
			return this.run();
		}
			 
		
		int[] indi = _best_sol_global;
		
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
		_isSeparator_best_sol_global = false;
		_size_best_sol_global = _tam_individuo;
		
		this.Initialization();
		this.Evaluate();
		
		for(int i=0; i<_nbgen; i++)
		{
			this.Selection();
			this.Recombine();
			this.Remplace();
			this.Evaluate();
			this.SetBestSolution();
		}

		return this.GetBestSolution();
		
	}
}
