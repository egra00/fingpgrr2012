package uy.edu.fing.repository.tools.iBGPViewer.model;


public class MyNode 
{
	private double x;
	private double y;
	private String rid;
	private boolean rr;
	
	
	public MyNode(String _rid, double _x, double _y, boolean _rr)
	{
		x = _x;
		y = _y;
		rid = _rid;
		rr = _rr;
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
		return rid; 
	}

	public String getNode() {
		return rid;
	}

	public void setNode(String _rid) {
		rid = _rid;
	}

	public boolean isRr() {
		return rr;
	}

	public void setRr(boolean rr) {
		this.rr = rr;
	}
}
