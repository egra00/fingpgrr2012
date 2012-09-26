package uy.edu.fing.repository.rrloc.algorithms.cbr;

public class iBGPColor {
	String color;

	iBGPColor(String color) {
		this.color = color;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof iBGPSessionColored) {
			return obj == null ? color == null : color.equals(((iBGPSessionColored)obj).getColor());	
		}
		return obj == null ? color == null : color.equals(obj);
	}
	
	@Override
	public String toString() {
		return color;
	}
	
}
