package uy.edu.fing.pg.bgpsep.utils;

import java.util.List;
import java.util.Set;

import uy.edu.fing.pg.bgpsep.domain.model.Link;
import uy.edu.fing.pg.bgpsep.domain.model.Router;
import edu.uci.ics.jung.graph.Graph;

public class Separator {
	private Set<Router> separator;
	private List<Graph<Router, Link>> components;
	
	public Separator() {
	}
	
	public Set<Router> getSeparator() {
		return separator;
	}
	public void setSeparator(Set<Router> separator) {
		this.separator = separator;
	}
	public List<Graph<Router, Link>> getComponents() {
		return components;
	}
	public void setComponents(List<Graph<Router, Link>> components) {
		this.components = components;
	} 

}
