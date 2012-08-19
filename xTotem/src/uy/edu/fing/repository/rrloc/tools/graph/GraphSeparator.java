package uy.edu.fing.repository.rrloc.tools.graph;

import java.util.List;
import java.util.Set;

import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import edu.uci.ics.jung2.graph.Graph;

public class GraphSeparator {
	private Set<Node> separator;
	private List<Graph<Node, Link>> components;
	
	public GraphSeparator() {
	}
	
	public Set<Node> getSeparator() {
		return separator;
	}
	public void setSeparator(Set<Node> separator) {
		this.separator = separator;
	}
	public List<Graph<Node, Link>> getComponents() {
		return components;
	}
	public void setComponents(List<Graph<Node, Link>> components) {
		this.components = components;
	} 

}
