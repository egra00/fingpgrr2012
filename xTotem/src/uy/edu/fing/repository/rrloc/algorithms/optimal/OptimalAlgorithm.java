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
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung2.visualization.BasicVisualizationServer;
import uy.edu.fing.repository.rrloc.iAlgorithm.RRLocAlgorithm;

public class OptimalAlgorithm implements RRLocAlgorithm{

	@Override
	public void run(Object inParams, Object outResutl) {
		
		//Obtain parameters
		Graph<Node, Link> IGPGraph = (Graph<Node, Link>)((List<Object>)inParams).get(0);
		List<BgpRouter> BGPRouters = (List<BgpRouter>)((List<Object>)inParams).get(1);
//		List<Node> BGPRouters = (List<Node>)((List<Object>)inParams).get(1);
		List<Node> nextHops = (List<Node>)((List<Object>)inParams).get(2);
		Domain domain = (Domain)((List<Object>)inParams).get(3);
		
		int BGPRoutersSize = BGPRouters.size();
		int nextHopsSize = nextHops.size();
		int n = IGPGraph.getVertexCount();	
		
		//Build the model
		CPModel m = new CPModel();
				
		//Structure declaration
		IntegerVariable[][] UP = new IntegerVariable[n][n];
		IntegerVariable[][] DOWN = new IntegerVariable[n][n];
		
		
		// For each variable, we define its name and the boundaries of its domain
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				UP[i][j] = makeIntVar("up_" + i + "_" + j, 0, 1);
				m.addVariable(UP[i][j]); // Associate the variable to the model
			}
		}
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				DOWN[i][j] = makeIntVar("down_" + i + "_" + j, 0, 1);
				m.addVariable(DOWN[i][j]);
			}
		}
		
		//Adding the constraint for the function to minimise
		
		//Calculating the length of the shortest IGP path from i to j
		int MINHOPS[][] = new int[n][n];
 
		Object[] nodes = IGPGraph.getVertices().toArray();
		
		for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
            	if(i!=j){
            		MINHOPS[i][j]= hops(IGPGraph,((Node)nodes[i]).getRid(),((Node)nodes[j]).getRid());
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
		
        IntegerVariable R_aux = makeIntVar("R_aux", 0, 10000000);
        IntegerVariable[] z_array = new IntegerVariable[n*n];
        
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                z_array[i*n+j] = makeIntVar("z_" + i +"_" + j, 0, 10000);
                m.addConstraint(eq(mult(MINHOPS[i][j],sum(UP[i][j],DOWN[i][j])), z_array[i*n+j]));
                
            }
        }
        m.addConstraint(eq(sum(z_array), R_aux));
		
		//Adding domain constraints to the model
		
		// ∀u, v ∈ R, up(u, v) + down(u, v) ≤ 1  ---> up(u, v) ≤ 1 - down(u, v)
		for (int i = 0; i < n; i++) {
		for (int j = 0; j < n; j++) {
			if(i!=j){
				Constraint c = (leq(UP[i][j], plus(1,neg(DOWN[i][j]))));
				m.addConstraint(c);
			}
		}
		}
		
		// ∀u, v ∈ R, up(u, v) = down(v, u)
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if(j>i){
					Constraint c = (eq(UP[i][j] , DOWN[j][i]));
					m.addConstraint(c);
				}
			}
		}
		
		//printGraph(IGPGraph,"Grafo IGP");
		
		//Building satellite graphs

		List<Graph<Node, Link>> satellites = new ArrayList<Graph<Node, Link>>();
		
		System.out.println("BGPRouters.size() = "+BGPRoutersSize);
		System.out.println("nextHops.size() = "+nextHopsSize);
		
		for (int i = 0; i < BGPRoutersSize; i++) {
			for (int j = 0; j < nextHopsSize; j++) {
				
				BgpRouter br = BGPRouters.get(i);
				Node nod = nextHops.get(j);
				
				if(br.getRid()!=nod.getRid()){
					
					Graph<Node, Link> sat = new UndirectedSparseMultigraph<Node, Link>();
					
					Node no = nextHops.get(j);
					//BgpRouter r = BGPRouters.get(i);
					
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
							sat.addVertex(findColRId(IGPGraph.getVertices(),w.getRid())); 
						}
					}
					
					//Adding edges
					Collection<Node> vertices = sat.getVertices();
					Iterator<Node> i1 = vertices.iterator();
					Iterator<Node> i2 = vertices.iterator();
					
					while(i1.hasNext()){
						Node node = (Node)i1.next();
						
						if(collectionNcontains(vertices,node)){
							
							while(i2.hasNext()){
								Node node2 = (Node)i2.next();
								
								if(node2.getId()!=node.getId() && collectionNcontains(vertices,node2)){
									Link l = new LinkImpl(domain,node.getId()+"_"+node2.getId(),node.getId(),node2.getId(),0);
									sat.addEdge(l, node, node2);
								}
							}
						}
					}
					
					//printGraph(sat,"Grafo Satelite ("+no.getId()+","+r.getId()+")");
					
					/*Collection<Link> edges = IGPGraph.getEdges();
					Iterator<Link> ite = edges.iterator();
					Collection<Node> vertices = sat.getVertices();
					
					while(ite.hasNext()){
						Link l = (Link)ite.next();
						try {
							if(collectionNcontains(vertices,l.getSrcNode()) && collectionNcontains(vertices,l.getDstNode())){
								sat.addEdge(l, l.getSrcNode(), l.getDstNode());
							}
						}
						catch(NodeNotFoundException e){
							System.out.println("Error while building satellite graphs.");
							e.printStackTrace();
						}
					}*/
					
					satellites.add(sat);
				}
			}
				
		}

		//Adding max-flow min-cut constraints to the model
		
		IntegerVariable[][] arrayRestrictions = new IntegerVariable[satellites.size()][];
		
		for(int y = 0; y < satellites.size(); y++){
			arrayRestrictions[y] = new IntegerVariable[satellites.get(y).getEdgeCount()*2+satellites.get(y).getVertexCount()];	
			for(int z = 0; z < (satellites.get(y).getEdgeCount()*2+satellites.get(y).getVertexCount()); z++){
				arrayRestrictions[y][z] = makeIntVar("arrayRestrictions_" + y + "_" + z, 0, 1);
				m.addVariable(arrayRestrictions[y][z]);
			}
		}
		
		/*List<IntegerVariable[]> arrayRestrictions = new ArrayList<IntegerVariable[]>();
		
		for(int y = 0; y < satellites.size(); y++){
			IntegerVariable[] iv = new IntegerVariable[satellites.get(y).getEdgeCount()];	
			for (int l = 0; l < satellites.get(y).getEdgeCount(); l++) {
				iv[l] = makeIntVar("arrayRestrictions_" + y + "_" + l, 0, 1);
				m.addVariable(iv[l]); // Associate the variable to the model
			}
			arrayRestrictions.add(iv);
		}*/
		
		// For each variable, we define its name and the boundaries of its domain
		/*for (int k = 0; k < satellites.size(); k++) {
			for (int l = 0; l < satellites.get(k).getEdgeCount(); l++) {
				arrayRestrictions[k][l] = makeIntVar("arrayRestrictions_" + k + "_" + l, 0, 1);
				m.addVariable(arrayRestrictions[k][l]); // Associate the variable to the model
			}
		}*/
		
		//Solving the MIP without max-flow min-cut constraints
		
		CPSolver s = new CPSolver();
		
		//Constraint c2 = (eq(UP[5][5] , 1));
		//m.addConstraint(c2);
		
		//CPModel mBK = copyModel(m);
		s.read(m);
		s.solve();
		
		/*Constraint c3 = eq(DOWN[5][5],1);
		SConstraint sc3 = s.makeSConstraint(c3);
		s.post(sc3);*/
		
		System.out.println("------------------------------------------------------------------");
		System.out.println(s.summaryToString());
		System.out.println("------------------------------------------------------------------");
		
		
		/*boolean[][] newContraintsUP = new boolean[n][n];
		boolean[][] newContraintsDOWN = new boolean[n][n];
		
		for (int k = 0; k < n; k++) {
			for (int l = 0; l < n; l++) {
				newContraintsUP[k][l] = false;
				newContraintsDOWN[k][l] = false;
			}
		}*/
		
		int[][] UP_Aux = new int[n][n];
		int[][] DOWN_Aux = new int[n][n];
		
		for (int k = 0; k < n; k++) {
			for (int l = 0; l < n; l++) {
				UP_Aux[k][l] = 0;
				DOWN_Aux[k][l] = 0;
			}
		}
		
		int satIndex = 0;
		
		//List<Constraint> lstConstraints = new ArrayList<Constraint>();
		
		for(int i = 0; i< BGPRoutersSize; i++){
			for(int j = 0; j< nextHopsSize; j++){
				
				BgpRouter br = BGPRouters.get(i);
				Node nod = nextHops.get(j);
				
				//if(i!=j){
				if(br.getRid()!=nod.getRid()){
					
					//Adding the new constraints to the model
					/*for (int k = 0; k < n; k++) {
						for (int l = 0; l < n; l++) {
							if(newContraintsUP[k][l]==true){
								m.addConstraint(eq(UP[k][l],1));
							}
							if(newContraintsDOWN[k][l]==true){
								m.addConstraint(eq(DOWN[k][l],1));
							}
						}
					}
					
					for (int k = 0; k < n; k++) {
						for (int l = 0; l < n; l++) {
							newContraintsUP[k][l] = false;
							newContraintsDOWN[k][l] = false;
						}
					}*/
					
					/*System.out.println("Satelite numero: " + satIndex);
					System.out.println("UP:");
					
					for (int k = 0; k < n; k++) {
						for (int l = 0; l < n; l++) {
							System.out.print(s.getVar(UP[k][l]).getVal() + " ");
						}
						System.out.println();
					}
					
					System.out.println("DOWN:");
					
					for (int k = 0; k < n; k++) {
						for (int l = 0; l < n; l++) {
							System.out.print(s.getVar(DOWN[k][l]).getVal() + " ");
						}
						System.out.println();
					}*/
					
					/*if(primero){
						System.out.println("UP primero:");
						for (int k = 0; k < n; k++) {
							for (int l = 0; l < n; l++) {
								System.out.print(s.getVar(UP[k][l]).getVal() + " ");
							}
							System.out.println();
						}
						System.out.println("DOWN primero:");
						for (int k = 0; k < n; k++) {
							for (int l = 0; l < n; l++) {
								System.out.print(s.getVar(DOWN[k][l]).getVal() + " ");
							}
							System.out.println();
						}
					}*/
					
					for (int k = 0; k < n; k++) {
						for (int l = 0; l < n; l++) {
							if(s.getVar(UP[k][l]).getVal()==1)
								UP_Aux[k][l] = 1;
							else
								UP_Aux[k][l] = 0;
							
							if(s.getVar(DOWN[k][l]).getVal()==1)
								DOWN_Aux[k][l] = 1;
							else
								DOWN_Aux[k][l] = 0;
						}
					}
					
					for (int k = 0; k < n; k++) {
						for (int l = 0; l < n; l++) {
							if(UP_Aux[k][l]==1){
								//m.addConstraint(eq(UP[k][l],1));
								s.addConstraint(eq(UP[k][l],1));
								//s.post(s.makeSConstraint(eq(UP[k][l],1)));
								//s.eq(s.getVar(UP[k][l]),1);
							}
							if(DOWN_Aux[k][l]==1){
								//m.addConstraint(eq(DOWN[k][l],1));
								s.addConstraint(eq(DOWN[k][l],1));
								//s.eq(s.getVar(DOWN[k][l]),1);
								//s.post(s.makeSConstraint(eq(DOWN[k][l],1)));
							}
						}
					}
					
					s.solve();
					
					Graph<Node,Link> gsat = satellites.get(satIndex);
					//System.out.println("Satelite numero "+ satIndex);
					
					//Extending satellite graph
					Graph<MetaNode,ExtendedLink> eg = CreateExtendedGraph(IGPGraph, gsat, UP, DOWN, s);
					
					//printGraph2(eg,"Grafo satelite de " + satIndex);
					
					if(!existsPath(eg,BGPRouters.get(i).getId(),nextHops.get(j).getId())){

						MetaNode src = findColId(eg.getVertices(),BGPRouters.get(i).getId());
						MetaNode dst = findColId(eg.getVertices(),nextHops.get(j).getId());
						
						if(src.getId()!=BGPRouters.get(i).getId()){
							System.out.println("ERROR AL BUSCAR METANODO SRC");
						}
						
						if(dst.getId()!=nextHops.get(j).getId()){
							System.out.println("ERROR AL BUSCAR METANODO DST");
						}
						
						//ExtendedLinkFactory factory = new ExtendedLinkFactory();
						//Calculating max flow min cut edges        
						//EdmondsKarpMaxFlow<MetaNode,ExtendedLink> ek = new EdmondsKarpMaxFlow((DirectedGraph) eg, src, dst, new TransformerExtendedLinkCapacity(), new HashMap<ExtendedLink,Integer>(), factory);
	
						//ek.evaluate(); // This computes the max flow
						
						//Adding restrictions
						//Set<ExtendedLink> minCutEdges = ek.getMinCutEdges(); 
						//Collection<ExtendedLink> minCutEdges = eg.getEdges();
						//System.out.println("COMIENZO DE RESTRICCION");
						Set<ExtendedLink> minCutEdges = getRestrictionEdges(eg,src,dst);
						//System.out.println("Cantidad de links para la restriccion: " + minCutEdges.size());
						//System.out.println("FIN DE RESTRICCION");
						Iterator<ExtendedLink> it = minCutEdges.iterator();
						
						it = minCutEdges.iterator();
						
						//Adding the restriction

						//IntegerVariable[] arrayRestrictions = new IntegerVariable[minCutEdges.size()];
						
						// For each variable, we define its name and the boundaries of its domain
						/*for (int k = 0; k < minCutEdges.size(); k++) {
							arrayRestrictions[k] = makeIntVar("arrayRestrictions_" + i + "_" + j + "_" + k, 0, 1);
							m.addVariable(arrayRestrictions[k]); // Associate the variable to the model
						}*/
						
						int k = 0;
						
						while(it.hasNext()){
							ExtendedLink el = (ExtendedLink)it.next();
	
							int indexI = IndexOf(gsat,el.getSrc().getId());
							int indexJ = IndexOf(gsat,el.getDst().getId());
							
							if(el.getSrc().getType()==MetaNodeType.SRC){
								//m.addConstraint(eq(arrayRestrictions[satIndex][k],UP[indexI][indexJ]));
								s.addConstraint(eq(arrayRestrictions[satIndex][k],UP[indexI][indexJ]));
								//s.eq(s.getVar(arrayRestrictions[satIndex][k]),s.getVar(UP[indexI][indexJ]));
								//s.post(s.makeSConstraint(eq(arrayRestrictions[satIndex][k],UP[indexI][indexJ])));
								//lstConstraints.add(eq(arrayRestrictions[satIndex][k],UP[indexI][indexJ]));
							}
							else{
								//m.addConstraint(eq(arrayRestrictions[satIndex][k],DOWN[indexI][indexJ]));
								s.addConstraint(eq(arrayRestrictions[satIndex][k],DOWN[indexI][indexJ]));
								//s.eq(s.getVar(arrayRestrictions[satIndex][k]),s.getVar(DOWN[indexI][indexJ]));
								//s.post(s.makeSConstraint(eq(arrayRestrictions[satIndex][k],DOWN[indexI][indexJ])));
								//lstConstraints.add(eq(arrayRestrictions[satIndex][k],DOWN[indexI][indexJ]));
							}
							k++;		
						}
						
						for(int p = k; p < (gsat.getEdgeCount()*2+gsat.getVertexCount()); p++){
							//m.addConstraint(eq(arrayRestrictions[satIndex][p],0));
							//s.addConstraint(eq(arrayRestrictions.get(satIndex)[p],0));
							s.addConstraint(eq(arrayRestrictions[satIndex][p],0));	
							//s.eq(s.getVar(arrayRestrictions[satIndex][p]),0);	
							//s.post(s.makeSConstraint(eq(arrayRestrictions[satIndex][p],0)));
							//lstConstraints.add(eq(arrayRestrictions[satIndex][p],0));
						}
						
						//m.addConstraint(geq(sum(arrayRestrictions[satIndex]), 1));
						
						//s.addConstraint(geq(sum(arrayRestrictions.get(satIndex)), 1));
						s.addConstraint(geq(sum(arrayRestrictions[satIndex]), 1));
						//SConstraint scc = s.makeSConstraint(geq(sum(arrayRestrictions[satIndex]), 1));
						//s.post(scc);
						//s.geq(s.sum(s.getVar(arrayRestrictions[satIndex])),1);
						//lstConstraints.add(geq(sum(arrayRestrictions[satIndex]), 1));
						
						//Solving the MIP with the new constraints
						//s.clear();
						
						//s = new CPSolver();
						//s.read(m);
						
						/*Iterator<Constraint> itc = lstConstraints.iterator();
						
						while(itc.hasNext()){
							Constraint c = (Constraint)itc.next();
							s.addConstraint(c);
						}*/
						
						System.out.println("------------------------------------------------------------------");
						System.out.println(s.summaryToString());
						System.out.println("------------------------------------------------------------------");

					}
					
					satIndex++;
				}
			
			}
		}
		
		// Print the solution
		List<String> reflectors = new ArrayList<String>();
		int sessions = 0;
		
		System.out.println("UP Matrix:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int value =  s.getVar(UP[i][j]).getVal();
				String rid = ((Node)IGPGraph.getVertices().toArray()[j]).getId();
				System.out.print(MessageFormat.format("{0} ", value));
				if(value==1) sessions++;
				if(value==1 && !reflectors.contains(rid)){
					reflectors.add(rid);
				}
			}
		System.out.println();
		}
		
		System.out.println("DOWN Matrix:");
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int value =  s.getVar(DOWN[i][j]).getVal();
				if(value==1) sessions++;
				System.out.print(MessageFormat.format("{0} ", value));
			}
		System.out.println();
		}
		
		System.out.println("Route reflectors IDs("+reflectors.size()+"):");
		Iterator<String> it = reflectors.iterator();
		while(it.hasNext()){
			System.out.println((String)it.next());
		}
		
		System.out.println("Number of sessions: "+sessions);
		
	}
	
	/*private static CPModel copyModel(CPModel m){
		
		CPModel model = new CPModel();
		
		//Copying variables
	
		Iterator<Variable> it = m.getConstVarIterator();
		
		while(it.hasNext()){
			model.addVariable((Variable)it.next());
		}
		
		Iterator<IntegerExpressionVariable> it2 = m.getExprVarIterator();
		
		while(it2.hasNext()){
			model.addVariable((IntegerExpressionVariable)it2.next());
		}
		
		Iterator<IntegerVariable> it3 = m.getIntVarIterator();
		
		while(it3.hasNext()){
			model.addVariable((IntegerVariable)it3.next());
		}
		
		Iterator<MultipleVariables> it4 = m.getMultipleVarIterator();
		
		while(it4.hasNext()){
			model.addVariable((MultipleVariables)it4.next());
		}
		
		Iterator<RealVariable> it5 = m.getRealVarIterator();
		
		while(it5.hasNext()){
			model.addVariable((RealVariable)it5.next());
		}
		
		Iterator<SetVariable> it6 = m.getSetVarIterator();
		
		while(it6.hasNext()){
			model.addVariable((SetVariable)it6.next());
		}
		
		//Copying constraints
		
		Iterator<Constraint> itc = m.getConstraintIterator();
		
		while(itc.hasNext()){
			model.addConstraint(itc.next());
		}
		
		return model;
	}*/
	
	private boolean existsPath(Graph<MetaNode,ExtendedLink> eg, String Idi, String Idj) {
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
		
		Transformer<ExtendedLink,Integer> tr = new TransformerExtendedLink();
		DijkstraShortestPath<MetaNode, ExtendedLink> s = new DijkstraShortestPath<MetaNode, ExtendedLink>(egAux,tr);
		List<ExtendedLink> edges = s.getPath(findColId(egAux.getVertices(),Idi),findColId(egAux.getVertices(),Idj));
		
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
	
	private static boolean collectionNcontains(Collection<Node> c, Node n){
		Iterator<Node> it = c.iterator();
		
		boolean found = false;
		
		while(it.hasNext() && !found){
			Node n2 = (Node)it.next();
			if(n2.getId() == n.getId()){
				found = true;
			}
		}
		
		return found;
	}
	
	private static Set<ExtendedLink> getRestrictionEdges(Graph<MetaNode,ExtendedLink> eg, MetaNode src, MetaNode dst){
		
		Set<ExtendedLink> links = new HashSet<ExtendedLink>();
		
		Collection<ExtendedLink> lst = eg.getEdges(); 
		
		List<MetaNode> reachableMetaNodes = new ArrayList<MetaNode>();
		reachableMetaNodes.add(src);
		Iterator<ExtendedLink> it = lst.iterator();
		
		while(it.hasNext()){
			
			ExtendedLink el = (ExtendedLink)it.next();
			
			if(el.getCapacity()==1){
				if(!listMNcontains(reachableMetaNodes,el.getDst())){
					reachableMetaNodes.add(el.getDst());
				}
			}
			
		}
		
		//System.out.println("Alcanzables: " + reachableMetaNodes.size());
		
		it = lst.iterator();
		
		while(it.hasNext()){
			
			ExtendedLink el = (ExtendedLink)it.next();
			
			if(listMNcontains(reachableMetaNodes,el.getSrc()) && el.getDst().getId()==dst.getId() && el.getCapacity()==0){
				//System.out.println("---------------------------------------------------------------------------Agregue");
				links.add(el);
			}
			
		}
		
		return links;
	}

	private static float dist(Graph<Node, Link> IGPGraph,String ridw,String ridn){ 
		Transformer<Link,Float> tr = new TransformerLink();
		DijkstraShortestPath<Node, Link> s = new DijkstraShortestPath<Node, Link>(IGPGraph,tr);
		List<Link> edges = s.getPath(findColRId(IGPGraph.getVertices(),ridw),findColRId(IGPGraph.getVertices(),ridn));
		Iterator<Link> it = edges.iterator();
		float sum = 0;
		while(it.hasNext()){
			Link l = (Link)it.next();
			sum+=l.getMetric();
		}
		return sum;
	}
	
	private static int hops(Graph<Node, Link> IGPGraph,String ridw,String ridn){ 
		Transformer<Link,Float> tr = new TransformerLink();
		DijkstraShortestPath<Node, Link> s = new DijkstraShortestPath<Node, Link>(IGPGraph,tr);
		List<Link> edges = s.getPath(findColRId(IGPGraph.getVertices(),ridw),findColRId(IGPGraph.getVertices(),ridn));
		return edges.size();
	}
	
	private static Node findColRId(Collection<Node> c, String rid){
		Iterator<Node> it = c.iterator();
		boolean found = false;
		Node n = null;
		while(!found && it.hasNext()){
			n = it.next();
			if(n.getRid().equals(rid))
				found = true;
		}
		if(found)
			return n;
		else
			return null;
	} 
	
	private static MetaNode findColId(Collection<MetaNode> c, String id){
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
	
	private static Graph<MetaNode,ExtendedLink> CreateExtendedGraph(Graph<Node, Link> IGPGraph, Graph<Node,Link> g, IntegerVariable[][] UP,IntegerVariable[][] DOWN, CPSolver s){
		
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
		
//		while(itl.hasNext()){
//			Link l = (Link)itl.next();
//				try{	
////					if(s.getVar(UP[IndexOf(IGPGraph,l.getSrcNode().getId())][IndexOf(IGPGraph,l.getDstNode().getId())]).getVal()==1){ //Exists an UP session between the nodes
//						ExtendedLink elUP = new ExtendedLink(0);
//						eg.addEdge(elUP,getMetaNode(MetaNodeType.SRC,eg,l.getSrcNode().getId()),getMetaNode(MetaNodeType.SRC,eg,l.getDstNode().getId()));
////					}
////					else if(s.getVar(DOWN[IndexOf(IGPGraph,l.getSrcNode().getId())][IndexOf(IGPGraph,l.getDstNode().getId())]).getVal()==1){ //Exists an DOWN session between the nodes
//						ExtendedLink elDOWN = new ExtendedLink(0);
//						eg.addEdge(elDOWN,getMetaNode(MetaNodeType.DST,eg,l.getSrcNode().getId()),getMetaNode(MetaNodeType.DST,eg,l.getDstNode().getId()));
////					}	
//				}
//				catch(NodeNotFoundException e){
//					System.out.println("Error while creating the extended graphs.");
//				}
//		}
		
		while(itl.hasNext()){
			Link l = (Link)itl.next();
				try{	
					MetaNode m1SRC = getMetaNode(MetaNodeType.SRC,eg,l.getSrcNode().getId());
					MetaNode m2SRC = getMetaNode(MetaNodeType.SRC,eg,l.getDstNode().getId());
					MetaNode m1DST = getMetaNode(MetaNodeType.DST,eg,l.getSrcNode().getId());
					MetaNode m2DST = getMetaNode(MetaNodeType.DST,eg,l.getDstNode().getId());
					
					if(s.getVar(UP[IndexOf(IGPGraph,l.getSrcNode().getId())][IndexOf(IGPGraph,l.getDstNode().getId())]).getVal()==1) //Exists an UP session between the nodes
						eg.addEdge(new ExtendedLink(1,m1SRC,m2SRC),m1SRC,m2SRC);
					else
						eg.addEdge(new ExtendedLink(0,m1SRC,m2SRC),m1SRC,m2SRC);
					
					if(s.getVar(DOWN[IndexOf(IGPGraph,l.getSrcNode().getId())][IndexOf(IGPGraph,l.getDstNode().getId())]).getVal()==1) //Exists an DOWN session between the nodes
						eg.addEdge(new ExtendedLink(1,m1DST,m2DST),m1DST,m2DST);	
					else
						eg.addEdge(new ExtendedLink(0,m1DST,m2DST),m1DST,m2DST);
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
	
	private static int IndexOf(Graph<Node,Link> IGP, String id){
		int p = 0;
		boolean found = false;
		Iterator<Node> it = IGP.getVertices().iterator();
		
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

}
