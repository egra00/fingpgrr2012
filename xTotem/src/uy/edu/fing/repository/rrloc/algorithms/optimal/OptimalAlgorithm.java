package uy.edu.fing.repository.rrloc.algorithms.optimal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.awt.Dimension;
import java.text.MessageFormat;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import edu.uci.ics.jung2.algorithms.layout.CircleLayout;
import edu.uci.ics.jung2.algorithms.layout.Layout;
import edu.uci.ics.jung2.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung2.graph.DirectedSparseGraph;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung2.visualization.BasicVisualizationServer;
import uy.edu.fing.repository.rrloc.algorithms.xAlgorithm.RRLocAlgorithm;

public class OptimalAlgorithm implements RRLocAlgorithm{

	@Override
	public void run(Object inParams, Object outResutl) {
		
		//Obtain parameters
		Graph<Node, Link> IGPGraph = (Graph<Node, Link>)((List<Object>)inParams).get(0);
		List<BgpRouter> BGPRouters = (List<BgpRouter>)((List<Object>)inParams).get(1);
		List<Node> nextHops = (List<Node>)((List<Object>)inParams).get(2);
				
		//Build the model
		CPModel m = new CPModel();
				
		//Structure declaration
		int n = IGPGraph.getVertexCount();
		IntegerVariable[][] UP = new IntegerVariable[n][n];
		IntegerVariable[][] DOWN = new IntegerVariable[n][n];
		
		
		// For each variable, we define its name and the boundaries of its domain
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				UP[i][j] = Choco.makeIntVar("up_" + i + "_" + j, 0, 1);
				m.addVariable(UP[i][j]); // Associate the variable to the model
			}
		}
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				DOWN[i][j] = Choco.makeIntVar("down_" + i + "_" + j, 0, 1);
				m.addVariable(DOWN[i][j]);
			}
		}
		
		//Adding domain constraints to the model
		
		// ∀u, v ∈ R, up(u, v) + down(u, v) ≤ 1  ---> up(u, v) ≤ 1 - down(u, v)
		for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
			if(i!=j){
				Constraint c = (Choco.leq(UP[i][j], Choco.plus(1,Choco.neg(DOWN[i][j]))));
				m.addConstraint(c);
			}
		}
		}
		
		// ∀u, v ∈ R, up(u, v) = down(v, u)
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if(j>i){
					Constraint c = (Choco.eq(UP[i][j] , DOWN[j][i]));
					m.addConstraint(c);
				}
			}
		}
		
		printGraph(IGPGraph,"Grafo IGP");
		
		//Adding max-flow min-cut constraints to the model
		
		//Building satellite graphs

		List<Graph<Node, Link>> satellites = new ArrayList<Graph<Node, Link>>();
		
		System.out.println("BGPRouters.size() = "+BGPRouters.size());
		System.out.println("nextHops.size() = "+nextHops.size());
		
		for (int i = 0; i < BGPRouters.size(); i++) {
			for (int j = 0; j < nextHops.size(); j++) {
				
				Graph<Node, Link> sat = new UndirectedSparseMultigraph<Node, Link>();
				
				Node no = nextHops.get(j);
				BgpRouter r = BGPRouters.get(i);
				
				//Build N(n,r)
				List<Node> lstNnr = new ArrayList<Node>();
				Iterator<Node> iter = nextHops.iterator();
				while(iter.hasNext()){
					Node np = (Node)iter.next();
					if(dist(IGPGraph,r.getRid(),np.getRid())>dist(IGPGraph,r.getRid(),no.getRid()))
						lstNnr.add(np);
				}
				
				//N(n, r) = {n′ ∈ N , dist(r, n′ ) > dist(r, n)}.
				//W(n, r) = {w ∈ R|∀n′ ∈ N(n, r), dist(w, n) < dist(w, n′ )}
				
				//Adding vertices
				Iterator<BgpRouter> it = BGPRouters.iterator();
				
				while(it.hasNext()){
					BgpRouter w = it.next();
					boolean addRouter = true;
					
					Iterator<Node> it2 = lstNnr.iterator();
					
					while((it2.hasNext()) && addRouter){
						Node node = it2.next();
						if(dist(IGPGraph,w.getRid(),no.getRid())>=dist(IGPGraph,w.getRid(),node.getRid())){
							addRouter = false;
						}
					}
					
					if(addRouter){
						sat.addVertex(findCol(IGPGraph.getVertices(),w.getRid())); 
					}
				}
				
				//Adding edges
				Collection<Link> edges = IGPGraph.getEdges();
				Iterator<Link> ite = edges.iterator();
				Collection<Node> vertices = sat.getVertices();
				
				while(ite.hasNext()){
					Link l = (Link)ite.next();
					try {
						if(vertices.contains(l.getSrcNode()) && vertices.contains(l.getDstNode())){
							sat.addEdge(l, l.getSrcNode(), l.getDstNode());
						}
					}
					catch(NodeNotFoundException e){
						System.out.println("Error while building satellite graphs.");
						e.printStackTrace();
					}
				}
				
				//Extending satellite graph
				//Graph<MetaNode,ExtendedLink> eg = CreateExtendedGraph(sat);
				
				printGraph(sat,"Grafo Satelite ("+no.getId()+","+r.getId()+")");
				
				if(sat.getVertexCount()>0)//OJO
					satellites.add(sat);
			}
				
		}
		
		//Adding constraints for the satellite problems
		
		/*for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if(i!=j){
					
					
					
				}
			}
		}*/
		
		Constraint c2 = (Choco.eq(UP[0][0] , 1));
		m.addConstraint(c2);
		
		// Build the solver
		CPSolver s = new CPSolver();

		// Read the model
		s.read(m);
		// Solve the model
		s.solve();
		
		// Print the solution
		System.out.println("UP Matrix:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print(MessageFormat.format("{0} ", s.getVar(UP[i][j]).getVal()));
			}
		System.out.println();
		}
		
		System.out.println("DOWN Matrix:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print(MessageFormat.format("{0} ", s.getVar(DOWN[i][j]).getVal()));
			}
		System.out.println();
		}
		
	}
	
	private static int dist(Graph<Node, Link> IGPGraph,String idw,String idn){ 
		Transformer<Link,Float> tr = new TransformerLink();
		DijkstraShortestPath<Node, Link> s = new DijkstraShortestPath<Node, Link>(IGPGraph,tr);
		List<Link> edges = s.getPath(findCol(IGPGraph.getVertices(),idw),findCol(IGPGraph.getVertices(),idn));
		//System.out.println("Size of path is : "+edges.size());
		Iterator<Link> it = edges.iterator();
		int sum = 0;
		while(it.hasNext()){
			Link l = (Link)it.next();
			sum+=l.getDelay();
		}
		return sum;
	}
	
	private static Node findCol(Collection<Node> c, String id){
		Iterator<Node> it = c.iterator();
		boolean found = false;
		Node n = null;
		while(!found && it.hasNext()){
			n = it.next();
			if(n.getRid().equals(id))
				found = true;
		}
		if(found)
			return n;
		else
			return null;
	} 
	
	private static Graph<MetaNode,ExtendedLink> CreateExtendedGraph(Graph<Node,Link> g){
		
		Graph<MetaNode,ExtendedLink> eg = new DirectedSparseGraph<MetaNode,ExtendedLink>();
		
		//Create the metanodes and link them
		
		Collection<Node> lstNodos = g.getVertices();
		Iterator<Node> it = lstNodos.iterator();
		
		while(it.hasNext()){
			Node n = (Node)it.next();
			MetaNode src = new MetaNode(n.getRid(),MetaNodeType.SRC);
			eg.addVertex(src);
			MetaNode dst = new MetaNode(n.getRid(),MetaNodeType.DST);
			eg.addVertex(dst);
			eg.addEdge(new ExtendedLink(Integer.MAX_VALUE), src, dst);
		}
		
		//Create the links between the metanodes of different nodes
		
//		Collection<Link> lstLinks = g.getEdges();
//		Iterator<Link> itl = lstLinks.iterator();
//		
//		while(itl.hasNext()){
//			Link l = (Link)itl.next();
//		}
		
		return eg;
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

}
