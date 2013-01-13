package uy.edu.fing.repository.rrloc.tools.graph.separator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.ejml.alg.dense.decomposition.SingularMatrixException;
import org.ejml.simple.SimpleMatrix;

import uy.edu.fing.repository.rrloc.tools.graph.separator.ApproachGA.GraphPartitionGA;
import uy.edu.fing.repository.rrloc.tools.graph.separator.ApproachGRASP.Bisection_GRASP;
import uy.edu.fing.repository.rrloc.tools.graph.separator.ApproachGRASP.Bisector;
import uy.edu.fing.repository.rrloc.tools.graph.separator.model.GraphSeparator;
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
	//////////////////////////////////////////////////// Approach GRASP (GA) //////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static public GraphSeparator GRASPBisection(Graph<Node, Link> G, int max_iter, double alfa, double beta) {
		GraphSeparator best_solution;

		if (G.getEdgeCount() < (G.getVertexCount() * (G.getVertexCount() - 1)) / 2) 
		{
			int _G[][] = new int[G.getVertexCount()][G.getVertexCount()];
			
			int m=0;
			int j=0;
			
			for(Node u : G.getVertices())
			{
				j=0;
				for(Node v : G.getVertices())
				{
					if (G.findEdge(u, v)!=null) 
						_G[m][j] = 1;
					else
						_G[m][j] = 0;
					j++;
				}
				m++;
			}
				
			Bisection_GRASP bisection = new Bisection_GRASP();
			Bisector bisector = bisection.run(_G, G.getVertexCount(), max_iter, alfa , beta);
			
			//Creo separador de grafo
			best_solution = new GraphSeparator();
			
			// Set separator
			Set<Node> set =  new HashSet<Node>();
			int i = 0;
			
			for(Node n : G.getVertices())
			{
				if (bisector.getS().contains(i)) set.add(n);
				i++;
			}
			
			best_solution.setSeparator(set);
			best_solution.setComponents(new ArrayList<Graph<Node, Link>>());
			
			// Components
			Graph<Node, Link> aux = Operations.copyUndirectedSparseGraph(G);
			Operations.removeAllVertices(aux, set);
			for (Set<Node> iter : Components.getAllConnectedComponent(aux)) {
				Graph<Node, Link> component = new UndirectedSparseGraph<Node, Link>();
				Operations.subGraph(aux, component, iter);
				best_solution.getComponents().add(component);
			}			
		} 
		else // El grafo fisico esta en full mesh ... no existe un grafo separador ... invento una solucion.
		{
			// Creo separador de grafo
			best_solution = new GraphSeparator();

			// Set separator
			Set<Node> set = new HashSet<Node>();

			for (Node n : G.getVertices()) {
				set.add(n);
			}

			best_solution.setSeparator(set);
			best_solution.setComponents(new ArrayList<Graph<Node, Link>>());
		}
		
		return best_solution;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////////////////FIN: Approach GRASP (GA) /////////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	
	
	
	
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////// Approach Evolutive (GA) //////////////////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	static public GraphSeparator GraphPartitionAE(int nb_run, Graph<Node,Link> G, int NGen, int sizeP, int sizeOf, double pmut, double pcross)
	{
		GraphSeparator best_solution;
		double fit_best_solution;
		GraphSeparator current_solution;
		double fit_current_solution;
		
		GraphPartitionGA separador;
		
		if ( G.getEdgeCount() < (G.getVertexCount()*(G.getVertexCount()-1))/2 ) 
		{
			separador = new GraphPartitionGA(G, NGen, sizeP, sizeOf, pmut, pcross);
			best_solution = separador.run();
			fit_best_solution = separador.get_fitness_best_sol_global();
			
			for(int i=1; i< nb_run; i++)
			{
				current_solution = separador.run();
				fit_current_solution = separador.get_fitness_best_sol_global();
				if (fit_best_solution <= fit_current_solution)
				{
					best_solution = current_solution;
					fit_best_solution = fit_current_solution;
				}
					
			}
		}
		else // El grafo fisico esta en full mesh ... no existe un grafo separador ... invento una solucion.
		{
			//Creo separador de grafo
			best_solution = new GraphSeparator();
			
			// Set separator
			Set<Node> set =  new HashSet<Node>();
			
			for(Node n : G.getVertices())
			{
				set.add(n);
			}
			
			best_solution.setSeparator(set);
			best_solution.setComponents(new ArrayList<Graph<Node, Link>>());
		}
		return best_solution;
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

