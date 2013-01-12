package uy.edu.fing.repository.rrloc.utils;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.xml.DOMConfigurator;

import uy.edu.fing.repository.rrloc.tools.graph.separator.Separator;
import uy.edu.fing.repository.rrloc.tools.graph.separator.model.GraphSeparator;

import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
import be.ac.ulg.montefiore.run.totem.core.Totem;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.persistence.DomainFactory;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;


	
public class TestCalibrationBisectionGRASP 
{
	static public int MAX_TEST = 3;
	
	static public int MAX_ITER = 25000;
	static public int MIN_ITER = 25000;
	static public int STEPS_ITER = 25000;

	static public double MAX_BETA = 0.02;
	static public double MIN_BETA = 0.02;
	static public int  STEPS_BETA = 1;
	
	static public double MAX_ALFA = 0.05;
	static public double MIN_ALFA = 0.05;
	static public int  STEPS_ALFA = 1;
	
	
    public static void init(){
        String log4jFile = "log4j.xml";
        File file = new File(log4jFile);
        if (!file.exists()) {
            String defaultLog4jFile = "/resources/log4j.xml";
            URL url = Totem.class.getResource(defaultLog4jFile);
            if (url == null) {
                System.out.println("Cannot find default log config file in JAR : " + defaultLog4jFile);
                System.exit(0);
            }
            System.out.println("Init Logging from JAR with config file : /resources/log4j.xml");
            DOMConfigurator.configure(url);
        } else {
            System.out.println("Init Logging with config file : log4j.xml");
            DOMConfigurator.configure(log4jFile);
        }
        PreferenceManager.getInstance().getPrefs();

        Locale.setDefault(Locale.ENGLISH);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                RepositoryManager.getInstance().stopAlgorithms();
            }
        });
    }
	
	
	public static void main(String args[]) 
	{
		init();
		for(int i=0; i<MAX_TEST; i++)
		{
	        try 
	        {
	        	Domain domain = DomainFactory.loadDomain("examples/TestCalibration/test"+i+".xml", false, false); 
	    		Graph<Node, Link> G = new UndirectedSparseMultigraph<Node, Link>();
	    		
	    		Operations.addAllVertices(G, (Set<Node>)(new HashSet<Node>(domain.getAllNodes())));
	    		for (Link link : domain.getAllLinks()) {
	    			try 
	    			{
	    				if (G.findEdge(link.getDstNode(), link.getSrcNode()) == null) 
	    				{
	    					G.addEdge(link, link.getSrcNode(), link.getDstNode());
	    				}
	    			} 
	    			catch (NodeNotFoundException e) 
	    			{
	    				e.printStackTrace();
	    			}
	    		}
	    		
	    		GraphSeparator gs_best = null;
	    		int can_iter_best = 0;
	    		double alfa_best = 0;
	    		double beta_best = 0;
	    		
	    		for(int can_iter=MIN_ITER; can_iter<=MAX_ITER; can_iter+=STEPS_ITER)
	    		{
	    			for(int j=0; j<STEPS_ALFA; j++)
	    			{
	    				double alfa= MIN_ALFA + ((MAX_ALFA - MIN_ALFA)/STEPS_ALFA)*j;
	    				for(int m=0; m<STEPS_BETA; m++)
	    				{
	    					double beta= MIN_BETA + ((MAX_BETA - MIN_BETA)/STEPS_BETA)*m;
	    					GraphSeparator gs = Separator.GRASPBisection(G, can_iter, alfa, beta);
	    					if(isBetter(gs, gs_best, 0.05, 0.02))
	    					{
	    						gs_best = gs;
	    						can_iter_best = can_iter;
	    						alfa_best = alfa;
	    						beta_best = beta;
	    					}
	    					
	    				}
	    			}
	    		}
	    		
	    		System.out.println("##################################################### TEST "+i+ " #####################################################" );
				System.out.println("Parameters: "+can_iter_best+"\t"+alfa_best+"\t"+beta_best+"\t");
				System.out.println("Graph: "+G.getVertexCount()+"\t"+G.getEdgeCount()+"\t"+(((double)G.getEdgeCount())/(G.getVertexCount() * (G.getVertexCount() - 1)) / 2));
				
				System.out.println("Separator: "+gs_best.getSeparator().size()+"\t"+ ((double)(gs_best.getSeparator().size()))/G.getVertexCount()+"\t"+gs_best.getComponents().size());
				int can=0;
				double media=0;
				System.out.println("Detail  of components:");
				for(Graph<Node, Link> comp: gs_best.getComponents())
				{
					System.out.println("\t\t C"+can+" :"+comp.getVertexCount());
					media+= comp.getVertexCount();
					can++;
				}
				media = media/gs_best.getComponents().size();
				System.out.println("\tMedia: "+ media);
				double balanced =0;
				for(Graph<Node, Link> comp : gs_best.getComponents())
					balanced += Math.abs(comp.getVertexCount() - media);
				System.out.println("\tBalanced: "+ balanced);
				
				System.out.println("##################################################### FIN TEST "+i+ " #####################################################\n" );
	    	
	        	
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
			
	}


	private static boolean isBetter(GraphSeparator gs, GraphSeparator gs1, double alfa, double beta)
	{
		if (gs1== null) return true;
		
		double gs_size = gs.getSeparator().size();
		
		double gs_average=0;
		for(Graph<Node, Link> comp: gs.getComponents())
			gs_average+= comp.getVertexCount();
		
		gs_average = gs_average/gs.getComponents().size();
		
		
		double gs_balanced = 0;
		for(Graph<Node, Link> comp : gs.getComponents())
			gs_balanced += Math.abs(comp.getVertexCount() - gs_average);

		
		
		double gs1_size = gs1.getSeparator().size();
		
		double gs1_average=0;
		for(Graph<Node, Link> comp: gs1.getComponents())
			gs1_average+= comp.getVertexCount();
		
		gs1_average = gs1_average/gs1.getComponents().size();
		
		
		double gs1_balanced = 0;
		for(Graph<Node, Link> comp : gs1.getComponents())
			gs1_balanced += Math.abs(comp.getVertexCount() - gs1_average);
		
		
		if(gs_size < gs1_size)
		{
			if(gs_balanced < gs1_balanced)
			{
				return true;
			}
			else if (gs_balanced <= (1+alfa)*gs1_balanced)
			{
				return true;
			}
		}
		else if(gs_size <= (1+beta)*gs1_size)
		{
			if(gs_balanced < gs1_balanced)
			{
				return true;
			}
		}
		
		return false;
	}

}
