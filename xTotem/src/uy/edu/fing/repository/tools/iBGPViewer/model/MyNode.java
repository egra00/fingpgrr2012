package uy.edu.fing.repository.tools.iBGPViewer.model;

import be.ac.ulg.montefiore.run.totem.domain.model.Node;

public class MyNode 
{
	private double x;
	private double y;
	private Node node;
	
	
	public MyNode(Node n, double xx, double yy)
	{
		x = xx;
		y = yy;
		node = n;
	}
	
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	
	public String toString() 
	{
		return node.getRid(); 
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}
}
