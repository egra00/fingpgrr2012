package uy.edu.fing.repository.rrloc.utils;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.log4j.xml.DOMConfigurator;

import uy.edu.fing.repository.rrloc.tools.graph.separator.Separator;
import uy.edu.fing.repository.rrloc.tools.graph.separator.model.GraphSeparator;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
import be.ac.ulg.montefiore.run.totem.core.Totem;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.persistence.DomainFactory;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;


	
public class TestCalibrationGraphPartitionGA 
{
	// (int nb_run, Graph<Node,Link> G, int NGen, int sizeP, int sizeOf, double pmut, double pcross)
	// (20, IGPTopology ,50, 200, 350, 0.01, 0.1)
	
	static public int MAX_TEST = 30;
	
	static public int MIN_SIZEP = 100;
	static public int MAX_SIZEP = 200;
	static public int STEPS_SIZEP = 100;
	
	static public int STEPS_PMUT = 5;
	static public double MIN_PMUT = 0.01;
	static public double MAX_PMUT = 0.04;
	
	static public int STEPS_PCROSS = 5;
	static public double MIN_CROSS = 0.1;
	static public double MAX_CROSS = 0.4;
	
	static public int STEPS_NGEN = 25;
	static public int MAX_NGEN = 25;
	static public int MIN_NGEN = 25;
	
	static public int MIN_NB_RUN = 10;
	static public int MAX_NB_RUN = 10;
	static public int STEPS_NB_RUN = 10;

	
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
	        	Domain domain = DomainFactory.loadDomain("topologies/TestCalibration/test"+i+".xml", false, false); 
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
	    		int nb_run_best =0;
	    		int NGen_best = 0;
	    		int sizeP_best =0; 
	    		int sizeOf_best =0;
	    		double pmut_best = 0;
	    		double pcross_best = 0;
	    		long time_best = 0;

	    		for(int nb_run= MIN_NB_RUN; nb_run<=MAX_NB_RUN; nb_run+= STEPS_NB_RUN)
	    			for(int NGen= MIN_NGEN; NGen<=MAX_NGEN; NGen+= STEPS_NGEN)
	    				for(int sizeP= MIN_SIZEP; sizeP<=MAX_SIZEP; sizeP+= STEPS_SIZEP)
	    				{
	    					int sizeOf = (int)((1 + 0.6)*sizeP);
	    	    			for(int j=0; j<=STEPS_PMUT; j++)
	    	    			{
	    	    				double pmut= MIN_PMUT + ((MAX_PMUT - MIN_PMUT)/STEPS_PMUT)*j;
	    	    				for(int m=0; m<=STEPS_PCROSS; m++)
	    	    				{
	    	    					double pcross= MIN_CROSS + ((MAX_CROSS - MIN_CROSS)/STEPS_PCROSS)*m;
	    	    					
	    	    					long time_in = System.currentTimeMillis();
	    	    					GraphSeparator gs = Separator.GraphPartitionAE(nb_run, G, NGen, sizeP, sizeOf, pmut, pcross);
	    	    					long time_out = System.currentTimeMillis();
	    	    					
	    	    					if(isBetter(gs, gs_best))
	    	    					{
	    	    						gs_best = gs;
	    	    			    		nb_run_best = nb_run;
	    	    			    		NGen_best = NGen;
	    	    			    		sizeP_best = sizeP; 
	    	    			    		sizeOf_best = sizeOf;
	    	    			    		pmut_best = pmut;
	    	    			    		pcross_best = pcross;
	    	    			    		time_best = time_out - time_in;
	    	    					}
	    	    					
	    	    				}
	    	    			}
	    				}

	    		
	    		System.out.println("##################################################### TEST "+i+ " #####################################################" );
				System.out.println("Parameters: "+nb_run_best+"\t"+NGen_best+"\t"+sizeP_best+"\t"+sizeOf_best+"\t"+pmut_best+"\t"+pcross_best);
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
				
				System.out.println("Time (ms): "+ time_best);
				System.out.println("##################################################### FIN TEST "+i+ " #####################################################\n" );
	    	
	        	
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		}
			
	}


	private static boolean isBetter(GraphSeparator gs, GraphSeparator gs1)
	{
		if (gs1== null) return true;
		
		if(fitness_function(gs)<= fitness_function(gs1)) return true;
				
		return false;
	}
	
	
	public static  double fitness_function(GraphSeparator gs) 
	{	
		int tam_individuo = gs.getSeparator().size();
		int cantCompConex = 0;
		double media = 0;
		for (Graph<Node, Link> cc : gs.getComponents()){
			media += cc.getVertexCount();
			tam_individuo += cc.getVertexCount();
			cantCompConex++;
		}
			
		
		media = media/cantCompConex;

		double balanced = 0;
		for (Graph<Node, Link> cc : gs.getComponents())
			balanced += Math.abs(cc.getVertexCount() - media);

		return  evaluate_guy(tam_individuo, gs.getSeparator().size(), balanced, cantCompConex);	
	}
	
	private static double evaluate_guy(int _tam_individuo, double _tam_separator, double _balanced_separator, double _cant_component){		
		if(_cant_component > 1)
			return ((((2*_tam_individuo) + 1)/(_balanced_separator + 1)) * (_tam_individuo/_tam_separator)) + 1;
		
		return 1;
	}

}

