package uy.edu.fing.pg.bgpsep.domain.model;

public class Link {
	
	private int idLink;
	private double length;
	private double delay;
	private double bandwidth;
	private LinkConf conf;
	
	public Link(int idLink, double length, double delay, double bandwidth, LinkConf conf) {
		this.idLink = idLink;
		this.length = length;
		this.delay = delay;
		this.bandwidth = bandwidth;
		this.conf = conf;
	}
	
	public LinkConf getConf() {
		return conf;
	}

	public void setConf(LinkConf conf) {
		this.conf = conf;
	}

	public int getIdLink() {
		return idLink;
	}
	
	public void setIdLink(int idLink) {
		this.idLink = idLink;
	}
	
	public double getLength() {
		return length;
	}
	
	public void setLength(double length) {
		this.length = length;
	}
	
	public double getDelay() {
		return delay;
	}
	
	public void setDelay(double delay) {
		this.delay = delay;
	}
	
	public double getBandwidth() {
		return bandwidth;
	}
	
	public void setBandwidth(double bandwidth) {
		this.bandwidth = bandwidth;
	}

}
