package uy.edu.fing.repository.rrloc.tools.graph.kmedoids.model;

import java.util.Random;

public class KMedoids 
{
	int _pops;
	int _nbgen;
	int _sizepopu;
	int _sizeoffs;
	int _tam_individuo;
	double _pmut;
	double _pcross;
	Coord[] _coord;
	private int[][] population;
	private int[][] meds_populations;
	private double[] fit_population;
	private int[][] meds_offsprings;
	private double _max_value_manhattan;
	
	private int[][] offsprings;
	
	final int SIZECAKE = 10000;
	private int[] cake;
	
	// Conservo la mejor solucion global
	private int[] _best_sol_global;
	private int[] _best_sol_global_meds;
	private double _fitness_best_sol_global;

	// Conservo la mejor solucion en cada generacion
	private int[] _best_sol_iter; // Inicializada luego de hacer Evaluate()
	private int[] _best_sol_iter_meds; // Inicializada luego de hacer Evaluate()
	private double _fitness_best_sol_iter; // Inicializada luego de hacer Evaluate()
	
	private double _FitTotal; // Inicializada luego de hacer Evaluate()

	
	public KMedoids(Coord[] coord, int pops, int nb_gen, int tam_popu, int tam_offs, double p_mut, double p_cross)
	{
		_pops = pops;
		_nbgen = nb_gen;
		_sizepopu = tam_popu;
		_sizeoffs = tam_offs;
		_pmut = p_mut;
		_pcross = p_cross;
		_coord = coord;
		_tam_individuo = _coord.length;
		
		population = new int[_sizepopu][];
		meds_populations = new int[_sizepopu][];
		offsprings = new int[_sizeoffs][];
		meds_offsprings = new int[_sizeoffs][];
		
		fit_population = new double[_sizepopu];
		
		cake = new int[SIZECAKE];
		
		double max_dist =0;
		double aux;
		for(int i=0; i<_tam_individuo; i++)
		{
			aux = Math.abs(coord[i].get_x()) + Math.abs(coord[i].get_y());
			if (aux > max_dist) max_dist = aux;
		}
		
		_max_value_manhattan = 2*max_dist*(_tam_individuo - _pops);
		
	}
	
	public void Initialization()
	{
		_fitness_best_sol_global = 0;
		Random ram = new Random();
		
		for(int i =0; i<_sizepopu; i++)
			population[i] = randomIndi(i, ram);
		
		
		for(;;);
	}
	
	private int[] randomIndi(int _indi_id, Random ram) 
	{
		int[] indi = new int[_tam_individuo];
		int[] meds = new int[_pops];
		int delta = ram.nextInt(_tam_individuo/_pops)+1;
		int index = ram.nextInt(_tam_individuo);
	
		for(int i=0; i<_pops; i++)
			meds[i] = (delta*i + index)% _tam_individuo;
		
		for(int i=0; i<_tam_individuo; i++)
			indi[i] =  ram.nextInt(_pops); //(int)(_pops*Math.abs(Math.cos(Math.PI/Math.random())));
		
		meds_populations[_indi_id] = meds;
		
		for(int i=0; i<_tam_individuo; i++)
			System.out.print("-"+indi[i]);
		System.out.print("\n");
		
		return indi;
	}
	
	public void Crossover()
	{
		for(int i =0; i+1<_sizeoffs; i=i+2)
		{
			 if (Math.random() <= _pcross) 
				 cross(offsprings[i], meds_offsprings[i], offsprings[i+1], meds_offsprings[i+1]);
		}
	}
	
	private void cross(int[] ind1, int[] meds_ind1, int[] ind2, int[] meds_ind2)
	{
		Random ram = new Random();
		int pointB = ram.nextInt(_pops);
		int pointA = ram.nextInt(pointB + 1);
		int aux;
		
		for(int i=pointA; i<pointB; i++)
		{
			change_pops(meds_ind1, meds_ind2[i], meds_ind1[i]);
			change_pops(meds_ind1, meds_ind1[i], meds_ind2[i]);
			
			aux = meds_ind1[i];
			meds_ind1[i] = meds_ind2[i];
			meds_ind2[i] = aux;
		}
		
		pointB = ram.nextInt(_tam_individuo);
		pointA = ram.nextInt(pointB + 1);
		
		for(int j=pointA; j<pointB; j++)
		{
			aux = ind1[j];
			ind1[j] = ind2[j];
			ind2[j] = aux;
		}
		
		correctness_centroids(ind1, meds_ind1);
		correctness_centroids(ind2, meds_ind2);
	}
	
	private double correctness_centroids(int[] indi, int[] meds)
	{
		int my_med;
		double max_dist;
		double dist_aux;
		
		for(int i = 0; i < _tam_individuo; i++)
		{
			max_dist = dist(_coord[meds[0]], _coord[i]);
			my_med = 0;
			for(int j = 1; j<_pops; j++)
			{
				dist_aux =  dist(_coord[meds[j]], _coord[i]);
				
				if (dist_aux < max_dist)
				{
					my_med = j;
					max_dist = dist_aux;
				}
			}
			
			indi[i] = my_med;
		}
		
		return fitness_function(meds, indi);
	}
	
	private void change_pops(int[] _array, int _old, int _new)
	{
		for(int i=0; i<_pops; i++)
			if(_array[i] == _old) _array[i] = _new;
	}
	
	public void Mutation()
	{
		for(int i =0; i<_sizeoffs; i++)
			mut(offsprings[i], meds_offsprings[i]);
	}
	
	
	private void mut(int[] indi, int[] meds)
	{
		Random ram = new Random();
		int _new_med;
		
		for(int i=0; i<_pops; i++)
		{
			if (Math.random() <= _pmut)
			{
				_new_med = ram.nextInt(_tam_individuo);
				change_pops(meds, _new_med, meds[i]);
				
				meds[i] = _new_med;
			}
		}
		
		for(int i=0; i<_tam_individuo; i++)
		{
			if (Math.random() <= _pmut)	
				indi[i] = ram.nextInt(_pops);
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
			
			cpy = new int[_pops];
			System.arraycopy(meds_populations[index], 0, cpy, 0, _pops);
			meds_offsprings[i] = cpy;
		}
	}
	
	
	public void Evaluate()
	{	
		fit_population[0] = fitness_function(meds_populations[0], population[0]);
		_FitTotal = fit_population[0];
		
		_best_sol_iter = population[0];
		_best_sol_iter_meds = meds_populations[0];
		_fitness_best_sol_iter = fit_population[0];

		
		for(int i=1; i<_sizepopu; i++)
		{
			fit_population[i] = fitness_function(meds_populations[i], population[i]);
			_FitTotal += fit_population[i];
			
			if (_fitness_best_sol_iter <= fit_population[i])
			{
				_best_sol_iter = population[i];
				_best_sol_iter_meds = meds_populations[i];
				_fitness_best_sol_iter = fit_population[i];
			}
		}
		
		this.SetGlobalBestSolution();
	}
	
	public void SetGlobalBestSolution()
	{
		_fitness_best_sol_iter = fitness_function(_best_sol_iter_meds, _best_sol_iter);
		if (_fitness_best_sol_global <= _fitness_best_sol_iter)
		{
			_best_sol_global = _best_sol_iter;
			_best_sol_global_meds = _best_sol_iter_meds;
			_fitness_best_sol_global = _fitness_best_sol_iter;
		}
	}
	
	private  double fitness_function(int[] meds, int[] indi) 
	{
		double sum = 0;
		
		for(int i = 0; i < _tam_individuo; i++)
			sum += dist(_coord[meds[indi[i]]], _coord[i]);		
		
		return (_max_value_manhattan - sum);
	}
	
	private double dist(Coord coord1, Coord coord2) // distancia manhattan
	{
		return Math.abs(coord1.get_x() - coord2.get_x()) + Math.abs(coord1.get_y() - coord2.get_y());
	}
	
	public void Remplace()
	{
		Random ram = new Random();
		int index;
		
		for(int i=0; i <_sizepopu; i++)
		{
			index = ram.nextInt(_sizeoffs);
			population[i] = offsprings[index];
			meds_populations[i] = meds_offsprings[index];
		}
	}
	
	
	public int[] GetBestSolution() // retorna celulas
	{	
		int[] cpy = new int[_tam_individuo];
		
		System.arraycopy(_best_sol_global, 0, cpy, 0, _tam_individuo);
		
		// CREO CELULAS
		for(int i=0; i<_tam_individuo; i++)
			cpy[i] = _best_sol_global_meds[cpy[i]];
		// END CREO CELULAS
		
		return cpy;
	}
	
	public double get_fitness_best_sol_global() 
	{
		return _fitness_best_sol_global;
	}
	
	public int[] run()
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
