package uy.edu.fing.pg.bgpsep.utils;

import java.util.ArrayList;
import java.util.Set;

import uy.edu.fing.pg.bgpsep.domain.model.Link;
import uy.edu.fing.pg.bgpsep.domain.model.Router;
import agape.algos.Separators;
import agape.tools.Components;
import agape.tools.Operations;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class Utils {
	
	@SuppressWarnings("unchecked")
	static public Separator graphSeparator(Graph<Router,Link> g) {
		
		Separators<Router, Link> s = new Separators<Router, Link>();
		Set<Set<Router>> gs = s.getAllMinimalSeparators(g);
		
		Separator ret = new Separator();
		ret.setSeparator((Set<Router>)gs.toArray()[0]);
		ret.setComponents(new ArrayList<Graph<Router, Link>>());
		
		Graph<Router, Link> aux = Operations.copyUndirectedSparseGraph(g);
		Operations.removeAllVertices(aux, ret.getSeparator());
		for (Set<Router> iter : Components.getAllConnectedComponent(aux)) {
			Graph<Router, Link> component = new UndirectedSparseGraph<Router, Link>();
			Operations.subGraph(aux, component, iter);
			ret.getComponents().add(component);
		}
		
		return ret;
	}

}
