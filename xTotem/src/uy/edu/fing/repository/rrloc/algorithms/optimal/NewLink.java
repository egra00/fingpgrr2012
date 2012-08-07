package uy.edu.fing.repository.rrloc.algorithms.optimal;

import be.ac.ulg.montefiore.run.totem.domain.model.Node;

public class NewLink {
	
	private Node src;
	private Node dst;
	
	public NewLink(Node src, Node dst){
		
	}
	
	public Node getSrc() {
		return src;
	}
	
	public void setSrc(Node src) {
		this.src = src;
	}
	
	public Node getDst() {
		return dst;
	}
	
	public void setDst(Node dst) {
		this.dst = dst;
	}
	
}
