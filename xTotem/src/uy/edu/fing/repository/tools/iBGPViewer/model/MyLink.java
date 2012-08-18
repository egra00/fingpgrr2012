package uy.edu.fing.repository.tools.iBGPViewer.model;


public class MyLink 
{
	
	private String r1;
	private String r2;
	private TypeMylink type;
	
	public MyLink(String _r1, String _r2, TypeMylink _type)
	{
		r1 = _r1;
		r2 = _r2;
		type = _type;
	}

	public String getR1() {
		return r1;
	}

	public void setR1(String r1) {
		this.r1 = r1;
	}

	public String getR2() {
		return r2;
	}

	public void setR2(String r2) {
		this.r2 = r2;
	}

	public TypeMylink getType() {
		return type;
	}

	public void setType(TypeMylink type) {
		this.type = type;
	}





}
