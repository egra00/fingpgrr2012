package uy.edu.fing.repository.rrloc.algorithms.optimal;

public class ExtendedLink {
	
	private int capacity;
	private MetaNode src;
	private MetaNode dst;

	public ExtendedLink(int capacity, MetaNode src, MetaNode dst) {
		this.capacity = capacity;
		this.src = src;
		this.dst = dst;
	}

	public MetaNode getSrc() {
		return src;
	}

	public void setSrc(MetaNode src) {
		this.src = src;
	}

	public MetaNode getDst() {
		return dst;
	}

	public void setDst(MetaNode dst) {
		this.dst = dst;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

}
