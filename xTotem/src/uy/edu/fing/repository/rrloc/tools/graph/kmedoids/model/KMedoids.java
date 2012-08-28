package uy.edu.fing.repository.rrloc.tools.graph.kmedoids.model;

import java.util.LinkedList;
import java.util.List;
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
	private double[] fit_population;
	private int[][] offsprings;
	
	final int SIZECAKE = 10000;
	private int[] cake;
	
	// Conservo la mejor solucion global
	private int[] _best_sol_global;
	private double _fitness_best_sol_global;

	// Conservo la mejor solucion en cada generacion
	private int[] _best_sol_iter; // Inicializada luego de hacer Evaluate()
	private double _fitness_best_sol_iter; // Inicializada luego de hacer Evaluate()
	
	private double _FitTotal; // Inicializada luego de hacer Evaluate()

	private int[] _mut_cpy;
	
	public KMedoids(Coord[] coord, int pop, int nb_gen, int tam_popu, int tam_offs, double p_mut, double p_cross)
	{
		_pops = pop;
		_nbgen = nb_gen;
		_sizepopu = tam_popu;
		_sizeoffs = tam_offs;
		_pmut = p_mut;
		_pcross = p_cross;
		_coord = coord;
		_tam_individuo = _coord.length;
		
		population = new int[_sizepopu][];
		fit_population = new double[_sizepopu];
		offsprings = new int[_sizeoffs][];
		
		cake = new int[SIZECAKE];
		_mut_cpy = new int[_tam_individuo];
	}
	
	public void Initialization()
	{
		_fitness_best_sol_global = 0;
		Random ram = new Random();
		
		for(int i =0; i<_sizepopu; i++)
			population[i] = randomIndi(ram);
	}
	
	private int[] randomIndi(Random ram) 
	{
		int[] indi = new int[_tam_individuo];
		
		List<Integer> lst = new LinkedList<Integer>();
		for(int i=0; i<_tam_individuo; i++)
			lst.add(i);
		
		List<Integer> medoids = new LinkedList<Integer>();
		for(int i=0; i<_pops; i++)
		{
			int index = ram.nextInt(lst.size());
			int med = lst.remove(index);
			
			medoids.add(indi[med] = med);
		}
			
		Random ram1 = new Random();
		for(int i = 0; i<_tam_individuo; i++)
		{	
			int index = ram1.nextInt(_pops);
			if(!medoids.contains(i)) indi[i] = medoids.get(index);
		}
		
		/*for(int i = 0; i<_tam_individuo; i++)
		{	
			System.out.print("-"+indi[i]);
		}
		System.out.println("");*/
		
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
		int med1 = 0;
		int med2 = 0;
		int index;
		List<Integer> meds_ind1 = new LinkedList<Integer>();
		List<Integer> meds_ind2 = new LinkedList<Integer>();
		boolean exito_ind1 = false;
		boolean exito_ind2 = false;
		
		for(int j=0; j<_tam_individuo && (meds_ind1.size()<_pops || meds_ind2.size()<_pops); j++)
		{
			if (ind1[j] == j) meds_ind1.add(j);
			if (ind2[j] == j) meds_ind2.add(j);
		}
		
		for(int j=0; j<3 && !exito_ind1; j++)
		{
			index = ram.nextInt(_pops);
			med1 = meds_ind1.get(index);
			if(!meds_ind2.contains(med1)) exito_ind1 = true;
			
		}
		
		for(int j=0; j<3 && !exito_ind2; j++)
		{
			index = ram.nextInt(_pops);
			med2 = meds_ind2.get(index);
			if(!meds_ind1.contains(med2)) exito_ind2 = true;
			
		}
		
		if (exito_ind1 && exito_ind2)
		{
			for(int j=0; j<_tam_individuo; j++)
			{
				if (ind1[j] == med1) ind1[j] = med2;
				if (ind2[j] == med2) ind2[j] = med1;
			}
			
			ind1[med2] = med2;
			ind2[med1] = med1;
		}
	}
	
	public void Mutation()
	{
		Random ram = new Random();
		for(int i =0; i<_sizeoffs; i++)
		{
			if(Math.random() <= _pmut) mut(offsprings[i], ram);
		}
	}
	
	
	private void mut(int[] ind, Random ram)
	{
		int index = ram.nextInt(_tam_individuo);
		int pos = index;
		int j=0;
		
		System.arraycopy(ind, 0, _mut_cpy, 0, _tam_individuo);
	
	/*	for(int k = 0; k<_tam_individuo; k++)
		{	
			System.out.print("-"+_mut_cpy[k]);
		}*/
		
		
		for( ; j<_tam_individuo; )
		{
			while (_mut_cpy[pos] == pos) 
				pos = (++pos) % _tam_individuo;
			
			while (j<_tam_individuo && _mut_cpy[j] == j) j++;
			
			
			if (j<_tam_individuo)
			{
				ind[pos] = _mut_cpy[j];
				
				pos = ((++pos)%_tam_individuo);
				j++;
			}
		}
		
	/*	System.out.print("   /  " +index+ "  /  ");
		
		for(int k = 0; k<_tam_individuo; k++)
		{	
			System.out.print("-"+ind[k]);
		}
		
		System.out.println("\n");*/
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
			//System.out.println(portions);
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
	
	private  double fitness_function(int[] indi) 
	{
		double sum = 0;
		
		for(int i = 0; i < _tam_individuo; i++)
		{
			sum += Math.abs(_coord[indi[i]].get_x() - _coord[i].get_x()) + Math.abs(_coord[indi[i]].get_y() - _coord[i].get_y());
		}
		
		//System.out.println(sum +"  ///   "+ (1+Math.sqrt(sum)) +"  /// "+ 1/(1+Math.sqrt(sum)));
		return  1/(1+Math.sqrt(sum));
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
	
	
	public int[] GetBestSolution()
	{	
		int[] cpy = new int[_tam_individuo];
		
		System.arraycopy(_best_sol_global, 0, cpy, 0, _tam_individuo);
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
