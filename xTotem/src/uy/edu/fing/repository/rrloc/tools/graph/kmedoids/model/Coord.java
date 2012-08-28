package uy.edu.fing.repository.rrloc.tools.graph.kmedoids.model;

public class Coord 
{
	private double _x;
	private double _y;
	
	
	public Coord(double x, double y)
	{
		_x = x;
		_y = y;
	}


	public double get_x() {
		return _x;
	}


	public void set_x(double _x) {
		this._x = _x;
	}


	public double get_y() {
		return _y;
	}


	public void set_y(double _y) {
		this._y = _y;
	}
}
