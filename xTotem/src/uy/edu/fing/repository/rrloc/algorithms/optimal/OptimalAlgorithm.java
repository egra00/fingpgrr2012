package uy.edu.fing.repository.rrloc.algorithms.optimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.awt.Dimension;
import java.text.MessageFormat;

import javax.swing.JFrame;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections15.Factory;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantFactory;
import org.apache.commons.collections15.functors.InstantiateFactory;

import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.LinkImpl;

import static choco.Choco.*;
import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.MultipleVariables;
import choco.kernel.model.variables.Variable;
import choco.kernel.model.variables.integer.IntegerExpressionVariable;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.model.variables.real.RealVariable;
import choco.kernel.model.variables.set.SetVariable;
import choco.kernel.solver.constraints.SConstraint;
import choco.kernel.solver.constraints.SConstraintType;
import choco.kernel.solver.variables.integer.IntDomainVar;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.EdgeShape.CubicCurve;
import edu.uci.ics.jung.graph.decorators.StringLabeller;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung2.algorithms.flows.EdmondsKarpMaxFlow;
import edu.uci.ics.jung2.algorithms.layout.CircleLayout;
import edu.uci.ics.jung2.algorithms.layout.Layout;
import edu.uci.ics.jung2.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung2.graph.DirectedGraph;
import edu.uci.ics.jung2.graph.DirectedSparseGraph;
import edu.uci.ics.jung2.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseGraph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung2.visualization.BasicVisualizationServer;
import edu.uci.ics.jung2.visualization.VisualizationViewer;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;

import ilog.concert.*;
import ilog.cplex.*;
import ilog.cplex.IloCplex.UnknownObjectException;

public class OptimalAlgorithm implements RRLocAlgorithm{

	@Override
	public void run(Object inParams, Object outResutl) {
		
		//Obtain parameters
		Graph<Node, Link> IGPGraph = (Graph<Node, Link>)((List<Object>)inParams).get(0);
//		List<BgpRouter> BGPRouters = (List<BgpRouter>)((List<Object>)inParams).get(1);
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
		
		System.out.println("BGP Routers = "+BGPRoutersSize);
		System.out.println("next Hops = "+nextHopsSize);
		
		try {
			
	     IloCplex cplex = new IloCplex();
	     
	     cplex.setParam(IloCplex.IntParam.MIPSearch, IloCplex.MIPSearch.Traditional);
				
		//Structure declaration
		
		IloNumVar[][] UP = new IloNumVar[BGPRoutersSize][nextHopsSize];
		IloNumVar[][] DOWN = new IloNumVar[BGPRoutersSize][nextHopsSize];
		
		// For each variable, we define its type, boundaries of its domain and name
		for (int i = 0; i < BGPRoutersSize; i++) {
			for (int j = 0; j < nextHopsSize; j++) {
				UP[i][j] = cplex.numVar(0, 1, IloNumVarType.Int, "UP["+i+"]["+j+"]");
				DOWN[i][j] = cplex.numVar(0, 1, IloNumVarType.Int, "DOWN["+i+"]["+j+"]");
			}
		}
		
		// Each variable must appear in at least one constraint or the objective function (CPLEX)
		for (int i = 0; i < BGPRoutersSize; i++) {
			for (int j = 0; j < nextHopsSize; j++) {
				if(BGPRouters.get(i).getId()==nextHops.get(j).getId()){
					cplex.addEq(UP[i][j], 0);
					cplex.addEq(DOWN[i][j], 0);
				}
			}
		}
		
		//Adding the constraint for the function to minimise
		
		//Calculating the length of the shortest IGP path from i to j
		int MINHOPS[][] = new int[BGPRoutersSize][nextHopsSize];
 
		Object[] nodes = IGPGraph.getVertices().toArray();
		
		for(int i = 0; i < BGPRoutersSize; i++) {
            for(int j = 0; j < nextHopsSize; j++) {
            	if(BGPRouters.get(i).getId()!=nextHops.get(j).getId()){
            		MINHOPS[i][j]= hops(IGPGraph,((Node)nodes[i]).getId(),((Node)nodes[j]).getId());
            	}
            }
		}
		
		/*System.out.println("MINHOPS:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print(MINHOPS[i][j]+" ");
			}
		System.out.println();
		}*/
		
		IloNumExpr[] ExpAux = new IloNumExpr[BGPRoutersSize*nextHopsSize];
		
		for(int i = 0; i < BGPRoutersSize; i++) {
			for(int j = 0; j < nextHopsSize; j++) {
				if(BGPRouters.get(i).getId()!=nextHops.get(j).getId())
					ExpAux[i*BGPRoutersSize+j] = cplex.prod(MINHOPS[i][j],cplex.sum(UP[i][j],DOWN[i][j]));
				else
					ExpAux[i*BGPRoutersSize+j] = UP[i][i];
			}
		}
		
		cplex.addMinimize(cplex.sum(ExpAux));
		
		//Adding domain constraints to the model
		
		// ∀u, v ∈ R, up(u, v) + down(u, v) ≤ 1 
		for (int i = 0; i < BGPRoutersSize; i++) {
			for (int j = 0; j < nextHopsSize; j++) {
				if(BGPRouters.get(i).getId()!=nextHops.get(j).getId())
					cplex.addRange(0,cplex.sum(UP[i][j], DOWN[i][j]),1);
			}
		}
		
		// ∀u, v ∈ R, up(u, v) = down(v, u)
		for (int i = 0; i < BGPRoutersSize; i++) {
			for (int j = 0; j < nextHopsSize; j++) {
				if(BGPRouters.get(i).getId()!=nextHops.get(j).getId())
					cplex.addEq(UP[i][j], DOWN[j][i]);
			}
		}
		
		//printGraph_(IGPGraph,"Grafo IGP");
		
		//Building satellite graphs

		List<Graph<Node, Link>> satellites = new ArrayList<Graph<Node, Link>>();
		
		String[] satNames = new String[BGPRoutersSize*nextHopsSize];
		
		for (int i = 0; i < BGPRoutersSize; i++) {
			for (int j = 0; j < nextHopsSize; j++) {
				
				//BgpRouter br = BGPRouters.get(i);
				Node br = BGPRouters.get(i);
				Node nod = nextHops.get(j);
				
				if(br.getRid()!=nod.getRid()){
					
					Graph<Node, Link> sat = new UndirectedSparseMultigraph<Node, Link>();
					
					Node no = nextHops.get(j);
					//BgpRouter no = BGPRouters.get(i);
					
					//BgpRouter r = BGPRouters.get(i);
					Node r = BGPRouters.get(i);
					
					//Build N(n,r)
					List<Node> lstNnr = new ArrayList<Node>();
					Iterator<Node> iter = nextHops.iterator();
					while(iter.hasNext()){
						Node np = (Node)iter.next();
						if(dist(IGPGraph,r.getId(),np.getId())>dist(IGPGraph,r.getId(),no.getId()))
							lstNnr.add(np);
					}
					
					//N(n, r) = {n′ ∈ N , dist(r, n′ ) > dist(r, n)}.
					//W(n, r) = {w ∈ R|∀n′ ∈ N(n, r), dist(w, n) < dist(w, n′ )}
					
					//Adding vertices
					//Iterator<BgpRouter> it = BGPRouters.iterator();
					Iterator<Node> it = BGPRouters.iterator();
					
					while(it.hasNext()){
						//BgpRouter w = it.next();
						Node w = it.next();
						boolean addRouter = true;
						
						Iterator<Node> it2 = lstNnr.iterator();
						
						while((it2.hasNext()) && addRouter){
							Node node = it2.next();
							if(dist(IGPGraph,w.getId(),no.getId())>=dist(IGPGraph,w.getId(),node.getId())){
								addRouter = false;
							}
						}
						
						if(addRouter){
							sat.addVertex(findColNodeId(IGPGraph.getVertices(),w.getId())); 
						}
					}
					
					//Adding edges
					Collection<Node> vertices = sat.getVertices();
					Iterator<Node> i1 = vertices.iterator();
					
					while(i1.hasNext()){
						Node node = (Node)i1.next();
						
						Iterator<Node> i2 = vertices.iterator();
							
						while(i2.hasNext()){
							Node node2 = (Node)i2.next();
	
							if(node2.getId()!=node.getId()){ 
								Link l = new LinkImpl(domain,node.getId()+"_"+node2.getId(),node.getId(),node2.getId(),0);
								sat.addEdge(l, node, node2);
							}
						}
					}
					
					//printGraph_(sat,"Grafo Satelite ("+no.getId()+","+r.getId()+")");
						
					satNames[satellites.size()]="Grafo Satelite ("+no.getId()+","+r.getId()+")";
					
					satellites.add(sat);
				}
			}
				
		}

		//Adding max-flow min-cut constraints to the model
		
		//Solving the MIP without max-flow min-cut constraints
		
		System.out.println("####Solucion 0##############################################################################");
		
		//System.out.println(cplex.toString());
		
		boolean res = cplex.solve();
		
		lstSessions = printSolution(IGPGraph,cplex,UP,DOWN,BGPRouters,nextHops,BGPRoutersSize, nextHopsSize);
		
		System.out.println("####Fin Solucion 0##############################################################################");
		
		int satIndex = 0;
		
		for(int i = 0; i< BGPRoutersSize; i++){
			for(int j = 0; j< nextHopsSize; j++){
				
				//BgpRouter br = BGPRouters.get(i);
				Node br = BGPRouters.get(i);
				Node nod = nextHops.get(j);
				
				if(br.getRid()!=nod.getRid()){
					
					//Adding the new constraints to the model
					
					Graph<Node,Link> gsat = satellites.get(satIndex);
					
					//Extending satellite graph
					Graph<MetaNode,ExtendedLink> eg = CreateExtendedGraph(IGPGraph, gsat, UP, DOWN, cplex, BGPRouters, nextHops);
					
					//printGraph2_(eg,satNames[satIndex]);
					
					if(!existsPath(eg,BGPRouters.get(i).getId(),nextHops.get(j).getId())){

						MetaNode src = findColMetaNodeId(eg.getVertices(),BGPRouters.get(i).getId());
						MetaNode dst = findColMetaNodeId(eg.getVertices(),nextHops.get(j).getId());
						
						//System.out.println("METANODO SRC = "+src.getId());
						//System.out.println("METANODO DST = "+dst.getId());
						
						//printGraph2(eg,src.getId()+" - "+dst.getId());
						
						if(src.getId()!=BGPRouters.get(i).getId()){
							System.out.println("ERROR SEARCHING SRC METANODE");
						}
						
						if(dst.getId()!=nextHops.get(j).getId()){
							System.out.println("ERROR SEARCHING DST METANODE");
						}
						
						//Calculating max flow min cut edges        
						
						//Adding restrictions
						
						Set<ExtendedLink> minCutEdges = getRestrictionEdges(eg,src,dst);
						
						int minCutEdgesSize = minCutEdges.size();
						
						IloNumVar[] restrictionEdges = new IloNumVar[minCutEdgesSize];
						
						String[] restrictionEdgesNames = new String[minCutEdgesSize];
						
						for(int k=0; k<minCutEdgesSize; k++){
							restrictionEdgesNames[k] = "restrictionEdge_"+satIndex+"_"+i+"_"+j+"_"+k;
						}
						
						restrictionEdges = cplex.numVarArray(minCutEdgesSize, 0, 1, IloNumVarType.Int, restrictionEdgesNames);
						
						//System.out.println("Cantidad de links para la restriccion: " + minCutEdgesSize);
						
						Iterator<ExtendedLink> it = minCutEdges.iterator();
						
						it = minCutEdges.iterator();
						
						//Adding the restriction
						
						int k = 0;
						
						while(it.hasNext()){
							ExtendedLink el = (ExtendedLink)it.next();
							
							int indexI = IndexOf(BGPRouters,el.getSrc().getId());
							int indexJ = IndexOf(nextHops,el.getDst().getId());
							
							if(el.getSrc().getType()==MetaNodeType.SRC){
								cplex.addEq(restrictionEdges[k],UP[indexI][indexJ]);
							}
							else{
								cplex.addEq(restrictionEdges[k],DOWN[indexI][indexJ]);
							}
							k++;		
						}
						
						cplex.addRange(1, cplex.sum(restrictionEdges), minCutEdgesSize);
						
						System.out.println("####Solucion "+(satIndex+1)+"##############################################################################");
						
						//System.out.println(cplex.toString());
						
						//Solving the MIP with the new constraints
						res = cplex.solve();
						
						lstSessions = printSolution(IGPGraph,cplex,UP,DOWN,BGPRouters,nextHops,BGPRoutersSize,nextHopsSize);
						
						System.out.println("####Fin Solucion "+(satIndex+1)+"##############################################################################");
					}
					
					satIndex++;
				}
			}
		}
		
		// Print the solution
		if(res){
			lstSessions = printSolution(IGPGraph,cplex,UP,DOWN,BGPRouters,nextHops,BGPRoutersSize, nextHopsSize);
			
			//System.out.println("Largo lista sesiones = "+ lstSessions.size());
	     }
	     else {
	        System.out.println(" No solution found ");
	     }
		
		cplex.end();
		
		 }
	      catch (IloException e) {
	         System.err.println("Concert exception caught: " + e);
	      }
		
	}
	
	private static List<iBGPSession> printSolution(Graph<Node, Link> IGPGraph, IloCplex cplex, IloNumVar[][] UP, IloNumVar[][] DOWN, List<Node> BGPRouters, List<Node> nextHops,int BGPRoutersSize, int nextHopsSize){
		try{
			System.out.println("--------------------------------------------");
	        System.out.println();
	        System.out.println("Solution found:");
	        System.out.println(" Objective value = " + cplex.getObjValue());
	        System.out.println();
	        
			List<String> reflectors = new ArrayList<String>();
			List<iBGPSession> lstSessions = new ArrayList<iBGPSession>();
			int sessions = 0;
			
			System.out.println("UP Matrix:");
			for (int i = 0; i < BGPRoutersSize; i++) {
				for (int j = 0; j < nextHopsSize; j++) {
					long value = Math.round(cplex.getValue(UP[i][j]));
					String rid = BGPRouters.get(i).getId();
					String rid2 = nextHops.get(j).getId();
					System.out.print(value+" ");
					if(value==1.0){
						lstSessions.add(new iBGPSession(rid,rid2, iBGPSessionType.peer));
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
				for (int j = 0; j < nextHopsSize; j++) {
					long value = Math.round(cplex.getValue(DOWN[i][j]));
					String rid = BGPRouters.get(i).getId();
					String rid2 = nextHops.get(j).getId();
					System.out.print(value+" ");
					if(value==1.0){
						lstSessions.add(new iBGPSession(rid2,rid, iBGPSessionType.peer));
						sessions++;
					}
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
	        
	        return lstSessions;
		}
		catch (IloException e) {
		   System.err.println("Concert exception caught: " + e);
		   return null;
		}
	}
	
	private boolean existsPath(Graph<MetaNode,ExtendedLink> eg, String Idi, String Idj) {
		Graph<MetaNode,ExtendedLink> egAux = new UndirectedSparseGraph<MetaNode,ExtendedLink>();
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
		
		//printGraph2_(egAux,Idi+" - "+Idj);
		
		Transformer<ExtendedLink,Integer> tr = new TransformerExtendedLink();
		DijkstraShortestPath<MetaNode, ExtendedLink> s = new DijkstraShortestPath<MetaNode, ExtendedLink>(egAux,tr);
		List<ExtendedLink> edges = s.getPath(findColMetaNodeId(egAux.getVertices(),Idi),findColMetaNodeId(egAux.getVertices(),Idj));
		
		if(edges.size()>0)
			return true;
		else
			return false;
	}
	
	private static boolean listMNcontains(List<MetaNode> lst, MetaNode m){
		Iterator<MetaNode> it = lst.iterator();
		
		boolean found = false;
		
		while(it.hasNext() && !found){
			MetaNode mn = (MetaNode)it.next();
			if(mn.getId() == m.getId()){
				found = true;
			}
		}
		
		return found;
	}
	
	/*private static boolean collectionNcontains(Collection<Node> c, Node n){
		Iterator<Node> it = c.iterator();
		
		boolean found = false;
		
		while(it.hasNext() && !found){
			Node n2 = (Node)it.next();
			if(n2.getId() == n.getId()){
				found = true;
			}
		}
		
		return found;
	}*/
	
	private static Set<ExtendedLink> getRestrictionEdges(Graph<MetaNode,ExtendedLink> eg, MetaNode src, MetaNode dst){
		
		Set<ExtendedLink> links = new HashSet<ExtendedLink>();
		
		Collection<ExtendedLink> lst = eg.getEdges(); 
		
		List<MetaNode> reachableMetaNodes = new ArrayList<MetaNode>();
		reachableMetaNodes.add(src);
		Iterator<ExtendedLink> it = lst.iterator();
		
		while(it.hasNext()){
			
			ExtendedLink el = (ExtendedLink)it.next();
			
			if(el.getCapacity()!=0){
				if(!listMNcontains(reachableMetaNodes,el.getDst())){
					reachableMetaNodes.add(el.getDst());
				}
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

	private static float dist(Graph<Node, Link> IGPGraph,String idw,String idn){ 
		Transformer<Link,Float> tr = new TransformerLink();
		DijkstraShortestPath<Node, Link> s = new DijkstraShortestPath<Node, Link>(IGPGraph,tr);
		List<Link> edges = s.getPath(findColNodeId(IGPGraph.getVertices(),idw),findColNodeId(IGPGraph.getVertices(),idn));
		Iterator<Link> it = edges.iterator();
		float sum = 0;
		while(it.hasNext()){
			Link l = (Link)it.next();
			sum+=l.getMetric();
		}
		return sum;
	}
	
	private static int hops(Graph<Node, Link> IGPGraph,String idw,String idn){ 
		Transformer<Link,Float> tr = new TransformerLink();
		DijkstraShortestPath<Node, Link> s = new DijkstraShortestPath<Node, Link>(IGPGraph,tr);
		List<Link> edges = s.getPath(findColNodeId(IGPGraph.getVertices(),idw),findColNodeId(IGPGraph.getVertices(),idn));
		return edges.size();
	}
	
	private static Node findColNodeId(Collection<Node> c, String id){
		Iterator<Node> it = c.iterator();
		boolean found = false;
		Node n = null;
		while(!found && it.hasNext()){
			n = it.next();
			if(n.getId().equals(id))
				found = true;
		}
		if(found)
			return n;
		else
			return null;
	} 
	
	private static MetaNode findColMetaNodeId(Collection<MetaNode> c, String id){
		Iterator<MetaNode> it = c.iterator();
		boolean found = false;
		MetaNode n = null;
		while(!found && it.hasNext()){
			n = it.next();
			if(n.getId().equals(id))
				found = true;
		}
		if(found)
			return n;
		else
			return null;
	} 
	
	private static Graph<MetaNode,ExtendedLink> CreateExtendedGraph(Graph<Node, Link> IGPGraph, Graph<Node,Link> g, IloNumVar[][] UP, IloNumVar[][] DOWN, IloCplex cplex, List<Node> BGPRouters, List<Node> nextHops){
		
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
						
						if(cplex.getValue(UP[IndexOf(BGPRouters,l.getSrcNode().getId())][IndexOf(nextHops,l.getDstNode().getId())])==1.0) //Exists an UP session between the nodes
							eg.addEdge(new ExtendedLink(1,m1SRC,m2SRC),m1SRC,m2SRC);
						else
							eg.addEdge(new ExtendedLink(0,m1SRC,m2SRC),m1SRC,m2SRC);
						
						if(cplex.getValue(DOWN[IndexOf(BGPRouters,l.getSrcNode().getId())][IndexOf(nextHops,l.getDstNode().getId())])==1.0) //Exists an DOWN session between the nodes
							eg.addEdge(new ExtendedLink(1,m1DST,m2DST),m1DST,m2DST);	
						else
							eg.addEdge(new ExtendedLink(0,m1DST,m2DST),m1DST,m2DST);
						
					} catch (IloException e) {
						e.printStackTrace();
					}
					
				}
				catch(NodeNotFoundException e){
					System.out.println("Error while creating the extended graphs.");
				}
		}
		
		return eg;
	} 
	
	private static MetaNode getMetaNode(MetaNodeType type, Graph<MetaNode,ExtendedLink> eg, String id){

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
	
	private static void printGraph(Graph<Node,Link> g,String title){
		
		Layout<Node,Link> layout = new CircleLayout(g);
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
	
	private static void printGraph2(Graph<MetaNode,ExtendedLink> g,String title){
		
		Layout<MetaNode,ExtendedLink> layout = new CircleLayout(g);
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
	
	public static void printGraph_(Graph<Node,Link> g,String title){
		 
		Layout<Node,Link> layout = new CircleLayout(g);
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
		 
		private static void printGraph2_(Graph<MetaNode,ExtendedLink> g,String title){
		 
		Layout<MetaNode,ExtendedLink> layout = new CircleLayout(g);
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
