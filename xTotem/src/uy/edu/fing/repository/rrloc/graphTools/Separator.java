package uy.edu.fing.repository.rrloc.graphTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.ejml.alg.dense.decomposition.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;

import agape.algos.Separators;
import agape.tools.Components;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseGraph;

public class Separator {
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////////// Minimal AB-separator //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	static public GraphSeparator graphSeparator(Graph<Node,Link> g) {
		
		Separators<Node, Link> s = new Separators<Node, Link>();
		Set<Set<Node>> gs = s.getAllMinimalSeparators(g);
		
		GraphSeparator ret = new GraphSeparator();
		if (!gs.isEmpty()) {
			ret.setSeparator((Set<Node>)gs.toArray()[0]);
		}
		else {
			ret.setSeparator(new HashSet<Node>());
		}
		ret.setComponents(new ArrayList<Graph<Node, Link>>());
		
		Graph<Node, Link> aux = Operations.copyUndirectedSparseGraph(g);
		Operations.removeAllVertices(aux, ret.getSeparator());
		for (Set<Node> iter : Components.getAllConnectedComponent(aux)) {
			Graph<Node, Link> component = new UndirectedSparseGraph<Node, Link>();
			Operations.subGraph(aux, component, iter);
			ret.getComponents().add(component);
		}
		
		return ret;
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////FIN: Minimal AB-separator //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////// Approach Evolutive (GA) //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static public GraphSeparator GraphPartitionAE(Graph<Node,Link> G, int NGen, int sizeP, double pmut, double pcross)
	{
		int sizeI = G.getVertexCount();
		int[][] Population = Initialization(sizeP, sizeI);
		// System.out.println("Initialization(sizeP, sizeI);");
		int[][] tempPopu;
		double[] fitness;
		
		for(int i=0; i<NGen; i++)
		{
			fitness = Fitness(G, Population, sizeP, sizeI);
		//	System.out.println("Fitness(G, Population, sizeP, sizeI);");
			tempPopu = Selection(Population, fitness, sizeP, sizeI);
		//	System.out.println("Selection(Population, fitness,sizeP, sizeI);");
			tempPopu = Crossover(tempPopu, sizeP, sizeI, pcross);
		//	System.out.println("Crossover(tempPopu, sizeP, sizeI, pcross);");
			Population = Mutation(tempPopu, sizeP, sizeI, pmut);
		//	System.out.println("Mutation(tempPopu, sizeP, sizeI, pmut);");
		}
		
		//System.out.println("IN: BestSolution(G, Population, sizeP, sizeI);");
		GraphSeparator SG = BestSolution(G, Population, sizeP, sizeI);
		while(null==SG)
			SG=GraphPartitionAE(G, NGen, sizeP, pmut, pcross);
		
		return SG;
		
	}
	

	private static GraphSeparator BestSolution(Graph<Node, Link> G, int[][] population, int sizeP, int sizeI) 
	{
		double[] fitness = Fitness(G, population, sizeP, sizeI);
		double best = fitness[0];
		int key = 0;
		int fit_total = 0;
		
		for(int i=1; i<sizeP; i++)
		{
			double fit = fitness[i];
			if (best<fit)
			{
				key = i;
				best = fit;
			}
			fit_total += fit;
		}
		
		if (fit_total == 0)
		{
			
			return null;
		}
			 
		
		int[] indi = population[key];
		
		//Creo separador de grafo
		GraphSeparator SG = new GraphSeparator();
		
		// Set separator
		Set<Node> set =  new HashSet<Node>();
		int i = 0;
		
		//System.out.println("///////////////"+G.getVertexCount());
		
		for(Node n : G.getVertices())
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
		Graph<Node, Link> aux = Operations.copyUndirectedSparseGraph(G);
		Operations.removeAllVertices(aux, set);
		for (Set<Node> iter : Components.getAllConnectedComponent(aux)) {
			Graph<Node, Link> component = new UndirectedSparseGraph<Node, Link>();
			Operations.subGraph(aux, component, iter);
			SG.getComponents().add(component);
		}
		
		return SG;
	}



	private static int[][] Mutation(int[][] population, int sizeP, int sizeI, double pmut) 
	{
		int[][] popuMut = new int[sizeP][];
		
		for(int i=0; i<sizeP; i++)
		{
			int[] indiMut = new int[sizeI];
			int[] indi = population[i];
			

			for(int j=0; j<sizeI; j++)
			{
				if(Math.random()<pmut)
				{
					indiMut[j]=(indi[j]+1)%2;
				}
				else
				{
					indiMut[j]=indi[j];
				}
			}		
			popuMut[i] = indi;
		}
		return popuMut;
	}



	private static int[][] Crossover(int[][] population, int sizeP, int sizeI, double pcross) 
	{
		int[][] popuCross = new int[sizeP][];
		List<Integer> choice_list= new LinkedList<Integer>();
		Random ram = new Random();
		Random ram1 = new Random();
		int key1;
		int key2;
		
		for(int i=0; i<sizeP; i+=2)
		{
			//Selecciono primer individuo
			while(choice_list.contains(key1=ram.nextInt(sizeP)));
			choice_list.add(key1);
			
			//Selecciono segundo individuo
			while(choice_list.contains(key2=ram.nextInt(sizeP)));
			choice_list.add(key2);
			
			
			int[] child1 = new int[sizeI];
			int[] child2 = new int[sizeI];
			
			int[] indi1 = population[key1];
			int[] indi2 = population[key2];
			
			if(Math.random()<pcross)
			{
				int pointCross = ram1.nextInt(sizeI);
				
				//Creo primer individuo
				for(int j=0; j<pointCross; j++)
					child1[j] = indi1[j];
				 
				for(int j=pointCross; j<sizeI; j++)
					child1[j] = indi2[j];
				
				
				//Creo segundo individuo
				for(int j=0; j<pointCross; j++)
					child2[j] = indi2[j];
				 
				for(int j=pointCross; j<sizeI; j++)
					child2[j]=indi1[j];
			}
			else
			{
				for(int j=0; j<sizeI; j++)
					child1[j]=indi1[j];
				
				for(int j=0; j<sizeI; j++)
					child2[j]=indi2[j];
			}
			
			popuCross[i] = child1;
			popuCross[i+1] = child2;
		}
		return popuCross;
	}



	private static int[][] Selection(int[][] population, double[] fitness, int sizeP, int sizeI) 
	{
		int[][] popuSelec = new int[sizeP][];
		double Npointer = Math.random()/sizeP;
		List<Integer> visited_list = new LinkedList<Integer>();
		double d = 1/sizeP;
		double begin[] = new double[sizeP];
		int order[] = new int[sizeP];
		double P[] = new double[sizeP];
		int j;
		double fit_total = 0;
		

		for(int i=0; i<sizeP; i++)
		{
			fit_total += fitness[i];
			
			//Busco el maximo de los no visitados
			double max_fit = fitness[0];
			int key = 0;
			for(int k = 1; k<sizeP; k++)
			{
				if(!visited_list.contains(k) && (fitness[k]> max_fit))
				{
					key = k;
					max_fit = fitness[k];
				}
			}
			visited_list.add(key);
			
			order[i] = key;
		}
		
		P[0] = fitness[order[0]]/fit_total;
		begin[0] = 0.0;
		
		for(int i=1; i<sizeP; i++)
		{
			P[i] = fitness[order[i]]/fit_total;
			begin[i] = begin[i-1] + P[i-1];
		}
		
		for(int i=0; i<sizeP; i++)
		{
			double pointer= i*d+Npointer;
			j = 0;
			while((j<sizeP) && (pointer>begin[j++]));
			
			int[] cpyIndi = new int[sizeI];
			int[] indi = population[order[j]];
			for(int k=0; k<sizeI; k++)
			{
				cpyIndi[k] = indi[k];
			}
			
			popuSelec[i] = cpyIndi;
		}
		return popuSelec;
	}



	private static double[] Fitness(Graph<Node, Link> G, int[][] population, int sizeP, int sizeI) 
	{
		double[] fitness_evaluation = new double[sizeP];
		
		for(int i=0; i<sizeP; i++)
		{
			fitness_evaluation[i] = fitness_function(G, population[i], sizeI);
			//System.out.println(" / "+fitness_evaluation[i]);
		}
		return fitness_evaluation;
	}


	private static double fitness_function(Graph<Node,Link> G, int[] indi, int sizeI) 
	{
		// Set separator
		Set<Node> set =  new HashSet<Node>();
		int i = 0;
		int tamIndi = 0;
		for(Node n : G.getVertices())
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
		Graph<Node, Link> aux = Operations.copyUndirectedSparseGraph(G);
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
			return 0;
		}
		
		
		media = media/cantCompConex;

		Iterator<Graph<Node, Link>> ii = components.iterator();
		double sum = 0;
		while(ii.hasNext())
		{
			int x = ii.next().getVertexCount();
			sum += (x-media)*(x-media); 
		}
		
		double size = G.getVertexCount();
		
		double desviacion = Math.sqrt(sum/(cantCompConex-1));
		double max_desviacion = Math.sqrt((size*((size-2)*(size-2)))/(size-1));
		
		//System.out.print("/ Size:" + size + "/ Media:"+ media + "/ max_Desviacion:" + max_desviacion + "/ Desviacion:" + desviacion + "/ cantCompConex:" + cantCompConex);

		
		return (sizeI-tamIndi) + cantCompConex + (max_desviacion - desviacion);
	}



	private static int[][] Initialization(int sizeP, int sizeI) 
	{
		int[][] population = new int[sizeP][];
	
		for(int i =0; i<sizeP; i++)
		{			
			population[i] = randomIndi(sizeI, Math.random());
			//System.out.println("");
		}
		return population;
	}

	private static int[] randomIndi(int sizeI, double seed) 
	{
		int[] indi = new int[sizeI];
		
		for(int i = 0; i<sizeI; i++)
		{	
			if (Math.random() < seed)
				indi[i] = 0;
			else
				indi[i] = 1;
			
			//System.out.print(indi[i]+"-");
		}
		
		return indi;
	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////FIN: Approach Evolutive (GA) /////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////// Spectral Bisection //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static public GraphSeparator SpectralBisection(Graph<Node,Link> G) 
	{ 
		GraphSeparator SG = new GraphSeparator();
		Graph<Node, Link> V1 = new UndirectedSparseGraph<Node, Link>();
		Graph<Node, Link> V2 = new UndirectedSparseGraph<Node, Link>();
		HashMap<Node, Double> hmap = new HashMap<Node, Double>();
		
		double fv[] = Fiedler_Vector(G, 100);
		
		double median = 0;
		for(int i = 0; i<fv.length ; i++)
			median += fv[i];
		median = median/fv.length;
		
		int i=0;
		for (Node v : G.getVertices()) 
		{
			if (fv[i]<= median)
			{
				V1.addVertex(v);
				hmap.put(v, fv[i]);
			}
			else
			{
				V2.addVertex(v);
				hmap.put(v, fv[i]);
			}
			i++;
		}
		
		if (V1.getVertexCount() - V2.getVertexCount() > 1)
		{
			int dif = V1.getVertexCount() - V2.getVertexCount();
			Collection<Node> col_nodes = V1.getVertices();
			List<Node> l_aux = new LinkedList<Node>();
			
			for (Node v : col_nodes) 
			{
				i=0;
				Iterator<Node> ii = l_aux.iterator();
				while( ii.hasNext() && (Math.abs(median - hmap.get(v)) >= Math.abs(median - hmap.get(ii.next()))))
					i++;
				l_aux.add(i, v);
			}
			
			Iterator<Node> ii = l_aux.iterator();
			for (int j = 0; j<(dif/2); j++)
			{
				Node n = ii.next();
				V2.addVertex(n);
				V1.removeVertex(n);
			}
				
		}
		else if (V2.getVertexCount() - V1.getVertexCount() > 1)
		{
			int dif = V2.getVertexCount() - V1.getVertexCount();
			Collection<Node> col_nodes = V2.getVertices();
			List<Node> l_aux = new LinkedList<Node>();
			
			for (Node v : col_nodes) 
			{
				i=0;
				Iterator<Node> ii = l_aux.iterator();
				while( ii.hasNext() && (Math.abs(median - hmap.get(v)) >= Math.abs(median - hmap.get(ii.next()))))
					i++;
				l_aux.add(i, v);
			}
			
			Iterator<Node> ii = l_aux.iterator();
			for (int j = 0; j<(dif/2); j++)
			{
				Node n = ii.next();
				V1.addVertex(n);
				V2.removeVertex(n);
			}
		}
			
		Graph<Node, Link> V1p = new UndirectedSparseGraph<Node, Link>();
		Graph<Node, Link> V2p = new UndirectedSparseGraph<Node, Link>();
		for(Node n : V1.getVertices())
		{
			Collection<Node> col_ne = G.getNeighbors(n);
			for(Node v : col_ne)
			{
				if (V2.containsVertex(v)) 
				{	
					if (!V2p.containsVertex(v)) V2p.addVertex(v);
					V1p.addVertex(n);
				}
			}
			
		}
		
		Graph<Node, Link> aux1;
		Graph<Node, Link> aux2;
		
		if (V1p.getVertexCount() <= V2p.getVertexCount())
		{
			aux1 = V1p;
			aux2 = V1;
		}
		else
		{
			aux1 = V2p;
			aux2 = V2;
		}
		
		for(Node n : aux1.getVertices())
		{
			aux2.removeVertex(n);
		}
		
		// Set separator
		Set<Node> set =  new HashSet<Node>();
		for(Node n : V1p.getVertices())
		{
			set.add(n);
			System.out.println(n.getDescription());
		}
		SG.setSeparator(set);
		
		
		// Components
		List<Graph<Node, Link>> components = new LinkedList<Graph<Node, Link>>();
		components.add(V1);
		components.add(V2);
		SG.setComponents(components);
		
		
		return SG;
	}
	
	static private double[] Fiedler_Vector(Graph<Node,Link> G, int k)
	{
		int max_degree = Integer.MIN_VALUE;
		double L[][] = new double[G.getVertexCount()][G.getVertexCount()];
		double fv[] = new double[G.getVertexCount()];
		int i = 0;
		int j = 0;
		for(Node u : G.getVertices())
		{
			j = 0;
			for(Node v : G.getVertices())
			{
				if(i==j)
				{
					L[i][j] = G.outDegree(u); 
					if(G.outDegree(u) >= max_degree) max_degree = G.outDegree(u);
				}
				else if (i!=j && G.isNeighbor(u, v))
				{
					L[i][j] = -1;
				}
				else
				{
					L[i][j] = 0;
				}
				j++;
			}
			i++;
		}
		
		double FiedlerNumber = 8*max_degree/(double)G.getVertexCount();
		double x[] = new double[G.getVertexCount()];
		
		for(i = 0; i<G.getVertexCount(); i++)
		{
			L[i][i] -= FiedlerNumber;
		}
		
		
		
		for(i=0; i < k; i++)
		{
			//Norma
			double norma = 0;
			for(int h=0; h<G.getVertexCount(); h++)
			{
				x[h] = Math.random();
				norma += x[h]*x[h];
			}
			norma=Math.sqrt(norma);
			
			
			for(int g = 0; g<G.getVertexCount(); g++)
				fv[g]= (1/norma)*x[g];
			
			x=Solve_Ecuation_System(L, fv);
		}
		
		return fv;
	}
	
	static private double[] Solve_Ecuation_System(double[][] A, double[] b )
	{
		SimpleMatrix AA = new SimpleMatrix(b.length, b.length);
		SimpleMatrix bb = new SimpleMatrix(b.length, 1);
		
		for(int i = 0; i<b.length; i++)
		{
			for(int j = 0; j<b.length; j++)
			{
				AA.set(i, j, A[i][j]);
			}
		}
		
		for(int i = 0; i<b.length; i++)
		{
			bb.set(i, b[i]);
		}
		
		try
		{
			SimpleMatrix xx = AA.solve(bb);
			
			double x[] = new double[b.length];
			
			for(int i = 0; i<b.length; i++)
			{
				x[i]=xx.get(i);
			}
			
			return x;
		}
		catch (SingularMatrixException e)
		{
			e.printStackTrace();
		}
		
		return null;

	}
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////// Spectral Bisection //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
}

