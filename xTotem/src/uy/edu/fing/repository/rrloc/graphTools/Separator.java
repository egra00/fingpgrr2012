package uy.edu.fing.repository.rrloc.graphTools;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import agape.algos.Separators;
import agape.tools.Components;
import agape.tools.Operations;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseGraph;

public class Separator {
	
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
	
}