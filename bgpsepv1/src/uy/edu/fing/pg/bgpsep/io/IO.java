package uy.edu.fing.pg.bgpsep.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import uy.edu.fing.pg.bgpsep.domain.model.Link;
import uy.edu.fing.pg.bgpsep.domain.model.LinkConf;
import uy.edu.fing.pg.bgpsep.domain.model.Router;
import uy.edu.fing.pg.bgpsep.domain.model.RouterConf;
import uy.edu.fing.pg.bgpsep.domain.model.RouterType;
import uy.edu.fing.pg.bgpsep.domain.model.iBGPSession;
import Graph.Edge;
import Graph.Graph;
import Graph.Node;
import Import.BriteImport;
import Model.ModelConstants;
import edu.uci.ics.jung.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung.io.MatrixFile;

public class IO {
	
	@SuppressWarnings("unchecked")
	public static edu.uci.ics.jung.graph.Graph<Router, Link> load(String filename) {
		File f = new File(filename);
		
		if(!f.exists()){
			System.out.println("Wrong filename.");
			return null;
		}
		else{
			BriteImport bi = new BriteImport(f,ModelConstants.RT_FILE);
			
		    Graph g = bi.parse();
		
		    UndirectedSparseMultigraph<Router, Link> ret = new UndirectedSparseMultigraph<Router, Link>();
		    
			HashMap<Integer,Node> m = g.getNodes();
		    
		    Iterator<Integer> it = m.keySet().iterator();
		    
		    HashMap<Integer,Router> routers = new HashMap<Integer,Router>(); 
		    
		    while(it.hasNext()){
		    	int id = it.next();
		    	//Node n = m.get(id);
		    	RouterConf conf = new RouterConf();
		    	Router r = new Router(id,RouterType.CLASSIC,conf);
		    	routers.put(id, r);
		    	ret.addVertex(r);
		    }
		    
		    HashMap<Integer,Edge> ma = g.getEdges();
		    
		    it = ma.keySet().iterator();
		    
		    while(it.hasNext()){
		    	int id = it.next();
		    	Edge e = ma.get(id);
		    	LinkConf conf = new LinkConf();
		    	Link l = new Link(id,e.getEuclideanDist(),e.getDelay(),e.getBW(),conf);
		    	ret.addEdge(l, routers.get(e.getSrc().getID()), routers.get(e.getDst().getID()));
		    }
		    
		    return ret;
		}
	}
	
	public static void dump(edu.uci.ics.jung.graph.Graph<Router, Link> graph, String filename) {
		
		MatrixFile<Router,Link> gf = new MatrixFile<Router,Link>(null,null,null,null);
		gf.save(graph,filename);
		
		File f = new File(filename);
		
		BufferedWriter bw;
		
		try {
			 bw = new BufferedWriter(new FileWriter(f, true));
			 Collection<Router> col = graph.getVertices();
			 Iterator<Router> it = col.iterator();
			
			 bw.write("\nNodes("+ graph.getVertexCount() +")" + "\n");
			 while(it.hasNext())
			 {
				 Router r= it.next();
				 bw.write(r.getIdRouter() + " - " + r.getTag().toString() + "\n");
			 }
			 
			 bw.close();
			 System.out.println("[MESSAGE]: Output format file");
			 
		}
		catch (IOException e) {
		    System.out.println("Error reading from file.");
		    System.exit(0);
		}
		
	}
	
	public static void dumpSimpleIBGPFile(List<iBGPSession> iBGPTopology, String filename) {
		File f = new File(filename);
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter(f, true));
			bw.write("\niBGP sessions ("+iBGPTopology.size()+")\n");
			for (iBGPSession iter: iBGPTopology) {
				bw.write(iter.getIdLink1() + " - " + iter.getIdLink2() + " -> " + iter.getSessionType() + "\n");
			}
			bw.close();
			System.out.println("[MESSAGE]: Output format file");
		}
		catch (IOException e) {
			System.out.println("Error writing in a file");
			System.exit(0);
		}
	}
	
}
