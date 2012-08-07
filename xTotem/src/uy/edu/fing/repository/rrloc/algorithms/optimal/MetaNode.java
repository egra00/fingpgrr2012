package uy.edu.fing.repository.rrloc.algorithms.optimal;

public class MetaNode {
	
	private String id;
	private int p;
	private MetaNodeType type;
	
	public MetaNode(int p, String id, MetaNodeType type) {
		this.p = p;
		this.id = id;
		this.type = type;
	}
	
	public int getP() {
		return p;
	}
	
	public void setP(int p) {
		this.p = p;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public MetaNodeType getType() {
		return type;
	}
	
	public void setType(MetaNodeType type) {
		this.type = type;
	}

}
