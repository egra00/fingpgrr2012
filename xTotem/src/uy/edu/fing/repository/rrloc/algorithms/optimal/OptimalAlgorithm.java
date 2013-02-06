package uy.edu.fing.repository.rrloc.algorithms.optimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.awt.Dimension;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.LinkImpl;

import edu.uci.ics.jung2.algorithms.layout.CircleLayout;
import edu.uci.ics.jung2.algorithms.layout.Layout;
import edu.uci.ics.jung2.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung2.graph.DirectedSparseGraph;
import edu.uci.ics.jung2.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.visualization.BasicVisualizationServer;
import edu.uci.ics.jung2.visualization.VisualizationViewer;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;

import ilog.concert.*;
import ilog.cplex.*;

public class OptimalAlgorithm implements RRLocAlgorithm{

	@SuppressWarnings("unchecked")
	@Override
	public void run(Object inParams, Object outResutl) {
		
		//Obtain parameters
		Graph<Node, Link> IGPGraph = (Graph<Node, Link>)((List<Object>)inParams).get(0);
		List<Node> BGPRouters = (List<Node>)((List<Object>)inParams).get(1);
		List<Node> nextHops = (List<Node>)((List<Object>)inParams).get(2);
		Domain domain = (Domain)((List<Object>)inParams).get(3);
		
		// List of sessions
		List<iBGPSession> lstSessions = (List<iBGPSession>) outResutl;
		
		/*for(int y = 0; y < BGPRouters.size(); y++){
			System.out.println(y+" - "+BGPRouters.get(y).getId());
		}*/
		
		int BGPRoutersSize = BGPRouters.size();
		int nextHopsSize = nextHops.size();
		int n = IGPGraph.getVertexCount();	
		
		System.out.println("Number of routers BGP = "+BGPRoutersSize);
		System.out.println("Number of next hops = "+nextHopsSize);
		
		if(nextHopsSize==0)
			System.out.println("WARNING: Number of next hops is zero.");
		
		try {
			
	     IloCplex cplex = new IloCplex();
	     
	     cplex.setParam(IloCplex.IntParam.MIPSearch, IloCplex.MIPSearch.Traditional);
				
		//Structure declaration
		
		IloNumVar[][] UP = new IloNumVar[BGPRoutersSize][BGPRoutersSize];
		IloNumVar[][] DOWN = new IloNumVar[BGPRoutersSize][BGPRoutersSize];
		
		// For each variable, we define its type, boundaries of its domain and name
		for (int i = 0; i < BGPRoutersSize; i++) {
			for (int j = 0; j < BGPRoutersSize; j++) {
				UP[i][j] = cplex.numVar(0, 1, IloNumVarType.Int, "UP["+i+"]["+j+"]");
				DOWN[i][j] = cplex.numVar(0, 1, IloNumVarType.Int, "DOWN["+i+"]["+j+"]");
			}
		}
		
		// Each variable must appear in at least one constraint or the objective function (CPLEX)
		for (int i = 0; i < BGPRoutersSize; i++) {
			for (int j = 0; j < BGPRoutersSize; j++) {
				if(i==j){
					cplex.addEq(UP[i][j], 0);
					cplex.addEq(DOWN[i][j], 0);
				}
			}
		}
		
		//Adding the constraint for the function to minimise
		
		//Calculating the length of the shortest IGP path from i to j
		int MINHOPS[][] = new int[n][n];
 
		Object[] nodes = IGPGraph.getVertices().toArray();
		
		for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
            	if(i!=j){
            		MINHOPS[i][j]= hops(IGPGraph,(Node)nodes[i],(Node)nodes[j]);
            	}
            }
		}
		
		IloNumExpr[] ExpAux = new IloNumExpr[BGPRoutersSize*BGPRoutersSize];
		
		for(int i = 0; i < BGPRoutersSize; i++) {
			for(int j = 0; j < BGPRoutersSize; j++) {
				if(i!=j)
					ExpAux[i*BGPRoutersSize+j] = cplex.prod(MINHOPS[i][j],cplex.sum(UP[i][j],DOWN[i][j]));
				else
					ExpAux[i*BGPRoutersSize+j] = UP[i][j];
			}
		}
		
		cplex.addMinimize(cplex.sum(ExpAux));
		
		//Adding domain constraints to the model
		
		// ∀u, v ∈ R, up(u, v) + down(u, v) ≤ 1 
		for (int i = 0; i < BGPRoutersSize; i++) {
			for (int j = 0; j < BGPRoutersSize; j++) {
				if(i!=j)
					cplex.addRange(0,cplex.sum(UP[i][j], DOWN[i][j]),1);
			}
		}
		
		// ∀u, v ∈ R, up(u, v) = down(v, u)
		for (int i = 0; i < BGPRoutersSize; i++) {
			for (int j = 0; j < BGPRoutersSize; j++) {
				if(i!=j)
					cplex.addEq(UP[i][j], DOWN[j][i]);
			}
		}
		
		//printGraph_(IGPGraph,"Grafo IGP");
		
		//Building satellite graphs

		List<Graph<Node, Link>> satellites = new ArrayList<Graph<Node, Link>>();
		
		String[] satNames = new String[BGPRoutersSize*nextHopsSize];
		
		for (int i = 0; i < BGPRoutersSize; i++) {
			for (int j = 0; j < nextHopsSize; j++) {
				
				Node br = BGPRouters.get(i);
				Node nod = nextHops.get(j);
				
				if(br.getId()!=nod.getId()){
					
					Graph<Node, Link> sat = new DirectedSparseMultigraph<Node, Link>();
					
					//Building N(n,r) = {n′ ∈ N , dist(r, n′ ) > dist(r, n)}.
					List<Node> lstNnr = new ArrayList<Node>();
					Iterator<Node> iter = nextHops.iterator();
					while(iter.hasNext()){
						Node np = (Node)iter.next();
						if(dist(IGPGraph,br,np)>dist(IGPGraph,br,nod))
							lstNnr.add(np);
					}
					
					//Building W(n, r) = {w ∈ R|∀n′ ∈ N(n, r), dist(w, n) < dist(w, n′ )}
					//Adding vertices
					Iterator<Node> it = BGPRouters.iterator();
					
					while(it.hasNext()){
						Node w = it.next();
						boolean addRouter = true;
						
						Iterator<Node> it2 = lstNnr.iterator();
						
						while((it2.hasNext()) && addRouter){
							Node node = it2.next();
							if(dist(IGPGraph,w,nod)>=dist(IGPGraph,w,node)){
								addRouter = false;
							}
						}
						
						if(addRouter){
							sat.addVertex(w);
						}
					}
					
					//Adding edges
					Collection<Node> vertices = sat.getVertices();
					Iterator<Node> i1 = vertices.iterator();
					
					while(i1.hasNext()){
						Node u = (Node)i1.next();
						
						Iterator<Node> i2 = vertices.iterator();
							
						while(i2.hasNext()){
							Node v = (Node)i2.next();
							
							//We only consider sessions (u, v) that a BGP message crossing it increases its distance 
							//to n and decreases its distance to r
							//dist(n, u) ≤ dist(n, v) and dist(v, r) ≤ dist(u, r) 
							if(v.getId()!=u.getId() &&
							   dist(IGPGraph,nod,u) <= dist(IGPGraph,nod,v) &&
							   dist(IGPGraph,v,br) <= dist(IGPGraph,u,br)){ 
								Link l = new LinkImpl(domain,u.getId()+"_"+v.getId(),u.getId(),v.getId(),0);
								sat.addEdge(l, u, v);
							}
						}
					}
					
					//printGraph_(sat,"Grafo Satelite ("+br.getId()+","+nod.getId()+")");
						
					satNames[satellites.size()]="Satellite Graph ("+br.getId()+","+nod.getId()+")";
					
					satellites.add(sat);
				}
			}
				
		}

		//Adding max-flow min-cut constraints to the model
		
		//Solving the MIP without max-flow min-cut constraints
		
		//System.out.println("####Solution 0##############################################################################");
		
		//System.out.println(cplex.toString());
		
		boolean res = cplex.solve();
		
		if(!res){
			System.out.println("ERROR: MIP SOLVER COULDN'T FOUND A SOLUTION");
			cplex.end();
		}
		
		//printSolution(IGPGraph,cplex,UP,DOWN,BGPRouters,BGPRoutersSize);
		
		//System.out.println("####End solution 0##############################################################################");
		
		int satIndex = 0;
		
		for(int i = 0; i< BGPRoutersSize; i++){
			for(int j = 0; j< nextHopsSize; j++){
				
				Node br = BGPRouters.get(i);
				Node nod = nextHops.get(j);
				
				if(br.getId()!=nod.getId()){
					
					//Adding the new constraints to the model
					
					Graph<Node,Link> gsat = satellites.get(satIndex);
					
					//Extending satellite graph
					Graph<MetaNode,ExtendedLink> eg = CreateExtendedGraph(IGPGraph, gsat, UP, DOWN, cplex, BGPRouters, nextHops);
					
					//printGraph2_(eg,satNames[satIndex]);
					
					if(!existsPath(eg,nod.getId(),br.getId())){

						MetaNode src = getMetaNode(MetaNodeType.SRC,eg,nod.getId());
						MetaNode dst = getMetaNode(MetaNodeType.DST,eg,br.getId());
						
						//printGraph2(eg,src.getId()+" - "+dst.getId());
						
						if(src.getId()!=nod.getId()){
							System.out.println("ERROR: SEARCHING SRC METANODE");
						}
						
						if(dst.getId()!=br.getId()){
							System.out.println("ERROR: SEARCHING DST METANODE");
						}
						
						//Calculating max flow min cut edges        
						
						//Adding restrictions
						
						Set<ExtendedLink> minCutEdges = getRestrictionEdges(eg,src,dst);
						
						int minCutEdgesSize = minCutEdges.size();
						
						if(minCutEdgesSize==0){
							System.out.println("ERROR: MINCUTEDGES SIZE IS 0");
							cplex.end();
						}
						
						IloNumVar[] restrictionEdges = new IloNumVar[minCutEdgesSize];
						
						String[] restrictionEdgesNames = new String[minCutEdgesSize];
						
						for(int k=0; k<minCutEdgesSize; k++){
							restrictionEdgesNames[k] = "restrictionEdge_"+satIndex+"_"+i+"_"+j+"_"+k;
						}
						
						restrictionEdges = cplex.numVarArray(minCutEdgesSize, 0, 1, IloNumVarType.Int, restrictionEdgesNames);
						
						Iterator<ExtendedLink> it = minCutEdges.iterator();
						
						it = minCutEdges.iterator();
						
						//Adding the restriction
						
						int k = 0;
						
						while(it.hasNext()){
							ExtendedLink el = (ExtendedLink)it.next();
							
							int indexI = IndexOf(BGPRouters,el.getSrc().getId());
							int indexJ = IndexOf(BGPRouters,el.getDst().getId());
							
							if(el.getSrc().getType()==MetaNodeType.SRC){
								cplex.addEq(restrictionEdges[k],UP[indexI][indexJ]);
								k++;
							}
						}
						
						it = minCutEdges.iterator();
						
						while(it.hasNext()){
							ExtendedLink el = (ExtendedLink)it.next();
							
							int indexI = IndexOf(BGPRouters,el.getSrc().getId());
							int indexJ = IndexOf(BGPRouters,el.getDst().getId());
							
							if(el.getSrc().getType()==MetaNodeType.DST){
								cplex.addEq(restrictionEdges[k],DOWN[indexI][indexJ]);
								k++;
							}
						}
						
						cplex.addRange(1, cplex.sum(restrictionEdges), minCutEdgesSize);
						
						//System.out.println("####Solution "+(satIndex+1)+" - "+satNames[satIndex]+"##############################################################################");
						
						//System.out.println(cplex.toString());
						
						//Solving the MIP with the new constraints
						res = cplex.solve();
						
						if(!res){
							System.out.println("ERROR: MIP SOLVER COULDN'T FOUND A SOLUTION");
							cplex.end();
						}
						
						//printSolution(IGPGraph,cplex,UP,DOWN,BGPRouters,BGPRoutersSize);
						
						//System.out.println("####End solution "+(satIndex+1)+" - "+satNames[satIndex]+"##############################################################################");
					}
					
					satIndex++;
				}
			}
		}
		
		// Print the solution and create sessions list
		if(res){
			//printSolution(IGPGraph,cplex,UP,DOWN,BGPRouters,BGPRoutersSize);
			createSessionList(lstSessions,cplex,UP,DOWN,BGPRouters,BGPRoutersSize);
	     }
	     else {
			System.out.println("ERROR: MIP SOLVER COULDN'T FOUND A SOLUTION");
			cplex.end();
	     }
		
		cplex.end();
		
		 }
	      catch (IloException e) {
	         System.err.println("Concert exception caught: " + e);
	      }
		
	}
	
	private static float dist(Graph<Node, Link> IGPGraph,Node src,Node dst){ 
		//Returns the sum of the IGP metric in the links contained in the shortest path from node 'src' to node 'dst'
		
		Transformer<Link,Float> tr = new TransformerLink();
		DijkstraShortestPath<Node, Link> s = new DijkstraShortestPath<Node, Link>(IGPGraph,tr);
		List<Link> edges = s.getPath(src,dst);
		Iterator<Link> it = edges.iterator();
		float sum = 0;
		while(it.hasNext()){
			Link l = (Link)it.next();
			sum+=l.getMetric();
		}
		return sum;
	}
	
	private static int hops(Graph<Node, Link> IGPGraph,Node n1,Node n2){ 
		//Returns the number of routers in the shortest path from node 'n1' to node 'n2'
		
		Transformer<Link,Float> tr = new TransformerLink();
		DijkstraShortestPath<Node, Link> s = new DijkstraShortestPath<Node, Link>(IGPGraph,tr);
		List<Link> edges = s.getPath(n1,n2);
		return edges.size();
	}
	
	private static Graph<MetaNode,ExtendedLink> CreateExtendedGraph(Graph<Node, Link> IGPGraph, Graph<Node,Link> g, IloNumVar[][] UP, IloNumVar[][] DOWN, IloCplex cplex, List<Node> BGPRouters, List<Node> nextHops){
		//Creates the extended graph from the graph 'g'
		
		Graph<MetaNode,ExtendedLink> eg = new DirectedSparseGraph<MetaNode,ExtendedLink>();
		
		//Create the metanodes and link them
		
		Collection<Node> lstNodos = g.getVertices();
		Iterator<Node> it = lstNodos.iterator();
		int p = 0;
		
		while(it.hasNext()){
			Node n = (Node)it.next();
			MetaNode src = new MetaNode(p,n.getId(),MetaNodeType.SRC);
			eg.addVertex(src);
			MetaNode dst = new MetaNode(p,n.getId(),MetaNodeType.DST);
			eg.addVertex(dst);
			eg.addEdge(new ExtendedLink(Integer.MAX_VALUE, src, dst), src, dst);
			p++;
		}
		
		//Create the links between the metanodes of different nodes
		
		Collection<Link> lstLinks = g.getEdges();
		Iterator<Link> itl = lstLinks.iterator();
		
		while(itl.hasNext()){
			Link l = (Link)itl.next();
				try{	
					MetaNode m1SRC = getMetaNode(MetaNodeType.SRC,eg,l.getSrcNode().getId());
					MetaNode m1DST = getMetaNode(MetaNodeType.DST,eg,l.getSrcNode().getId());
					
					MetaNode m2SRC = getMetaNode(MetaNodeType.SRC,eg,l.getDstNode().getId());
					MetaNode m2DST = getMetaNode(MetaNodeType.DST,eg,l.getDstNode().getId());
					
					try {
						
						if(cplex.getValue(UP[IndexOf(BGPRouters,l.getSrcNode().getId())][IndexOf(BGPRouters,l.getDstNode().getId())])==1.0) //Exists an UP session between the nodes
							eg.addEdge(new ExtendedLink(1,m1SRC,m2SRC),m1SRC,m2SRC);
						else
							eg.addEdge(new ExtendedLink(0,m1SRC,m2SRC),m1SRC,m2SRC);
						
						if(cplex.getValue(DOWN[IndexOf(BGPRouters,l.getSrcNode().getId())][IndexOf(BGPRouters,l.getDstNode().getId())])==1.0) //Exists an DOWN session between the nodes
							eg.addEdge(new ExtendedLink(1,m1DST,m2DST),m1DST,m2DST);	
						else
							eg.addEdge(new ExtendedLink(0,m1DST,m2DST),m1DST,m2DST);
						
					} catch (IloException e) {
						e.printStackTrace();
					}
					
				}
				catch(NodeNotFoundException e){
					System.out.println("ERROR: COLUDN'T CREATE THE EXTENDED GRAPHS");
				}
		}
		
		return eg;
	} 
	
	private static boolean existsPath(Graph<MetaNode,ExtendedLink> eg, String idn, String idr) {
		//Returns true if exists a path from the node with id 'idn' to the node with id 'idr' in the graph 'eg', false otherwise
		
		Graph<MetaNode,ExtendedLink> egAux = new DirectedSparseGraph<MetaNode,ExtendedLink>();
		Iterator<MetaNode> it = eg.getVertices().iterator();
		
		while(it.hasNext()){
			MetaNode mn = (MetaNode)it.next();
			egAux.addVertex(mn);
		}

		Iterator<ExtendedLink> it2 = eg.getEdges().iterator();
		
		while(it2.hasNext()){
			ExtendedLink el = (ExtendedLink)it2.next();
			if(el.getCapacity()!=0)
				egAux.addEdge(el,el.getSrc(),el.getDst());
		}
		
		//printGraph2_(egAux,"EXIST PATH? "+idn+" - "+idr);
		
		Transformer<ExtendedLink,Integer> tr = new TransformerExtendedLink();
		DijkstraShortestPath<MetaNode, ExtendedLink> s = new DijkstraShortestPath<MetaNode, ExtendedLink>(egAux,tr);
		List<ExtendedLink> edges = s.getPath(getMetaNode(MetaNodeType.SRC,egAux,idn),getMetaNode(MetaNodeType.DST,egAux,idr));
		
		if(edges.size()>0){
			return true;
		}
		else
			return false;
	}
	
	private static Set<ExtendedLink> getRestrictionEdges(Graph<MetaNode,ExtendedLink> eg, MetaNode src, MetaNode dst){
		//Computes the set of links from node 'src' to node 'dst' to create the restriction 
		
		Set<ExtendedLink> links = new HashSet<ExtendedLink>();
		
		Collection<ExtendedLink> lst = eg.getEdges(); 
		
		List<MetaNode> reachableMetaNodes = new ArrayList<MetaNode>();
		reachableMetaNodes.add(src);
		Iterator<ExtendedLink> it = lst.iterator();
		
		while(it.hasNext()){
			
			ExtendedLink el = (ExtendedLink)it.next();
			
			if(!listMNcontains(reachableMetaNodes,el.getDst()) && el.getCapacity()!=0 && existsPath(eg,src.getId(),el.getDst().getId())){
				reachableMetaNodes.add(el.getDst());
			}
			
		}
		
		it = lst.iterator();
		
		while(it.hasNext()){
			
			ExtendedLink el = (ExtendedLink)it.next();
			
			if(listMNcontains(reachableMetaNodes,el.getSrc()) && el.getDst().getId()==dst.getId() && el.getCapacity()==0){
				links.add(el);
			}
			
		}
		
		return links;
	}
	
	@SuppressWarnings("unused")
	private static void printSolution(Graph<Node, Link> IGPGraph, IloCplex cplex, IloNumVar[][] UP, IloNumVar[][] DOWN, List<Node> BGPRouters,int BGPRoutersSize){
		//Prints in the standard output information about the solution found by the solver 
		
		try{
			System.out.println("--------------------------------------------");
	        System.out.println();
	        System.out.println("Solution found:");
	        System.out.println(" Objective value = " + cplex.getObjValue());
	        System.out.println();
	        
			List<String> reflectors = new ArrayList<String>();
			int sessions = 0;
			
			System.out.println("UP Matrix:");
			for (int i = 0; i < BGPRoutersSize; i++) {
				for (int j = 0; j < BGPRoutersSize; j++) {
					long value = Math.round(cplex.getValue(UP[i][j]));
					String rid2 = BGPRouters.get(j).getId();
					System.out.print(value+" ");
					if(value==1.0){
						sessions++;
					}
					if(value==1.0 && !reflectors.contains(rid2)){
						reflectors.add(rid2);
					}
				}
			System.out.println();
			}
			
			System.out.println("DOWN Matrix:");
			for (int i = 0; i < BGPRoutersSize; i++) {
				for (int j = 0; j < BGPRoutersSize; j++) {
					long value = Math.round(cplex.getValue(DOWN[i][j]));
					String rid = BGPRouters.get(i).getId();
					System.out.print(value+" ");
					if(value==1.0 && !reflectors.contains(rid)){
						reflectors.add(rid);
					}
				}
			System.out.println();
			}
			
			System.out.println("Route reflectors IDs("+reflectors.size()+"):");
			Iterator<String> it = reflectors.iterator();
			while(it.hasNext()){
				System.out.println((String)it.next());
			}
			
			System.out.println("Number of sessions: "+sessions);
	        
	        System.out.println("--------------------------------------------");
	        
		}
		catch (IloException e) {
		   System.err.println("Concert exception caught: " + e);
		}
	}
	
	private static void createSessionList(List<iBGPSession> lstSessions, IloCplex cplex, IloNumVar[][] UP, IloNumVar[][] DOWN, List<Node> BGPRouters,int BGPRoutersSize){
		//Creates the list of sessions to dump them in the Totem domain
		
		try{
	     
			for (int i = 0; i < BGPRoutersSize; i++) {
				for (int j = 0; j < BGPRoutersSize; j++) {
					long value = Math.round(cplex.getValue(UP[i][j]));
					if(value==1.0)
						lstSessions.add(new iBGPSession(BGPRouters.get(i).getId(),BGPRouters.get(j).getId(), iBGPSessionType.client));
				}
			}
			
			for (int i = 0; i < BGPRoutersSize; i++) {
				for (int j = 0; j < BGPRoutersSize; j++) {
					long value = Math.round(cplex.getValue(DOWN[i][j]));
					if(value==1.0)
						lstSessions.add(new iBGPSession(BGPRouters.get(j).getId(),BGPRouters.get(i).getId(), iBGPSessionType.client));
				}
			}
		}
		catch (IloException e) {
		   System.err.println("Concert exception caught: " + e);
		}
	}

	private static boolean listMNcontains(List<MetaNode> lst, MetaNode m){
		//Returns true if the metanode 'm' is contained in the list 'lst', false otherwise
		
		Iterator<MetaNode> it = lst.iterator();
		
		boolean found = false;
		
		while(it.hasNext() && !found){
			MetaNode mn = (MetaNode)it.next();
			if(mn.getId() == m.getId() && mn.getType()==m.getType()){
				found = true;
			}
		}
		
		return found;
	}
	
	private static MetaNode getMetaNode(MetaNodeType type, Graph<MetaNode,ExtendedLink> eg, String id){
		//Returns the metanode in the graph 'eg' with id 'id' and type 'type', null if not found
		
		boolean found = false;
		Iterator<MetaNode> it = eg.getVertices().iterator();
		MetaNode mn = null;
		
		while(it.hasNext() && !found){
			mn = (MetaNode)it.next();
			if((mn.getType()==type) && (mn.getId()==id)){
				found = true;
			}
		}
		
		if(found)
			return mn;
		else
			return null;
	}
	
	private static int IndexOf(List<Node> lst, String id){
		//Returns the index in the list 'lst' of the node with id 'id'
		
		int p = 0;
		boolean found = false;
		Iterator<Node> it = lst.iterator();
		
		while(it.hasNext() && !found){
			Node n = (Node)it.next();
			if(n.getId()==id){
				found = true;
			}
			else
				p++;
		}
		
		if(found)
			return p;
		else
			return -1;
	}
	
	@SuppressWarnings("unused")
	private static void printGraph(Graph<Node,Link> g,String title){
		//Draws in a new window the grap 'g' and it's information 
		
		Layout<Node,Link> layout = new CircleLayout<Node,Link>(g);
		layout.setSize(new Dimension(300,300));
		BasicVisualizationServer<Node,Link> vv =
				new BasicVisualizationServer<Node,Link>(layout);
				vv.setPreferredSize(new Dimension(350,350)); //Sets the viewing area size
			
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
		
	}
	
	@SuppressWarnings("unused")
	private static void printGraph2(Graph<MetaNode,ExtendedLink> g,String title){
		//Draws in a new window the grap 'g' and it's information 
		
		Layout<MetaNode,ExtendedLink> layout = new CircleLayout<MetaNode,ExtendedLink>(g);
		layout.setSize(new Dimension(300,300));
		BasicVisualizationServer<MetaNode,ExtendedLink> vv =
				new BasicVisualizationServer<MetaNode,ExtendedLink>(layout);
				vv.setPreferredSize(new Dimension(350,350)); //Sets the viewing area size
			
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
		
	}
	@SuppressWarnings("unused")
	private static void printGraph_(Graph<Node,Link> g,String title){
		//Draws in a new window the grap 'g' and it's information 
		
		Layout<Node,Link> layout = new CircleLayout<Node,Link>(g);
		layout.setSize(new Dimension(300,300));
		VisualizationViewer<Node,Link> vv = new VisualizationViewer<Node, Link>(layout);
		vv.setPreferredSize(new Dimension(350,350));
		Transformer<Node, String> vertexPaint = new Transformer<Node, String>() {
		@Override
		public String transform(Node node) 
		{
		return node.getId();
		}
		};
		 
		vv.getRenderContext().setVertexLabelTransformer(vertexPaint);
		 
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
		 
		}
	
	@SuppressWarnings("unused")
	private static void printGraph2_(Graph<MetaNode,ExtendedLink> g,String title){
		//Draws in a new window the grap 'g' and it's information 
		
		Layout<MetaNode,ExtendedLink> layout = new CircleLayout<MetaNode,ExtendedLink>(g);
		layout.setSize(new Dimension(300,300));
		VisualizationViewer<MetaNode,ExtendedLink> vv = new VisualizationViewer<MetaNode,ExtendedLink>(layout);
		vv.setPreferredSize(new Dimension(350,350)); //Sets the viewing area size
		 
		Transformer<MetaNode, String> vertexString = new Transformer<MetaNode, String>() {
		@Override
		public String transform(MetaNode node) 
		{
		return node.getId();
		}
		};
		 
		Transformer<ExtendedLink, String> edgeString = new Transformer<ExtendedLink, String>() {
		@Override
		public String transform(ExtendedLink link) 
		{
		return String.valueOf(link.getCapacity());
		}
		};
		 
		vv.getRenderContext().setVertexLabelTransformer(vertexString);
		vv.getRenderContext().setEdgeLabelTransformer(edgeString);
		 
		JFrame frame = new JFrame(title);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
		 
		}

}
