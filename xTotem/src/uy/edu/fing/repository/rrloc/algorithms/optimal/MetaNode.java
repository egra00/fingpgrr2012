package uy.edu.fing.repository.rrloc.algorithms.optimal;

public class MetaNode {
	
	private String rid; //Router-ID
	private int p;
	private MetaNodeType type;
	
	public MetaNode(int p, String rid, MetaNodeType type) {
		this.p = p;
		this.rid = rid;
		this.type = type;
	}
	
	public int getP() {
		return p;
	}
	
	public void setP(int p) {
		this.p = p;
	}
	
	public String getRid() {
		return rid;
	}
	
	public void setRid(String rid) {
		this.rid = rid;
	}
	
	public MetaNodeType getType() {
		return type;
	}
	
	public void setType(MetaNodeType type) {
		this.type = type;
	}

}
