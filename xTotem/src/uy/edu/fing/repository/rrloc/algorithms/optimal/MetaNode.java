package uy.edu.fing.repository.rrloc.algorithms.optimal;

public class MetaNode {
	
	private String rid; //Router-ID
	private MetaNodeType type;
	
	public MetaNode(String rid, MetaNodeType type) {
		this.rid = rid;
		this.type = type;
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
