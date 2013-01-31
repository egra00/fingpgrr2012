package uy.edu.fing.repository.tools.CBGPDump.model;


//BGP4|<time>|A|<router>|<as>|<prefix>|<path>|<origin>|<next-hop>|<pref>|<med>|
//BGP4|<time>|W|<router>|<as>|<prefix>

public class MsjMRT {
	
	private String msj;
	private String time;
	private String type;
	private String router;
	private String as;
	private String prefix;
	private String path;
	private String origin;
	private String next_hop;
	private String pref;
	private String med;
	
	public void setAttribute(int pos, String value) {
		
		if(pos==0) {
			msj = value;
		}
		else if(pos==1) {
			time = value;
		}
		else if(pos==2) {
			type = value;
		}
		else if(pos==3) {
			router = value;
		}
		else if(pos==4) {
			as = value;
		}
		else if(pos==5) {
			prefix = value;
		}
		else if(pos==6) {
			path = value;
		}
		else if(pos==7) {
			origin = value;
		}
		else if(pos==8) {
			next_hop = value;
		}
		else if(pos==9) {
			pref = value;
		}
		else if(pos==10) {
			med = value;
		}

	}
	
	public String toString() {
		return msj +"|"+ time +"|"+ type +"|"+ router +"|"+ as +"|"+ prefix +"|"+ path +"|"+ origin +"|"+ next_hop +"|"+ pref +"|"+ med;
	}
	
	public String getMsj() {
		return msj;
	}
	public void setMsj(String msj) {
		this.msj = msj;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String getRouter() {
		return router;
	}
	public void setRouter(String router) {
		this.router = router;
	}
	public String getAs() {
		return as;
	}
	public void setAs(String as) {
		this.as = as;
	}
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getNext_hop() {
		return next_hop;
	}
	public void setNext_hop(String next_hop) {
		this.next_hop = next_hop;
	}
	public String getPref() {
		return pref;
	}
	public void setPref(String pref) {
		this.pref = pref;
	}
	public String getMed() {
		return med;
	}
	public void setMed(String med) {
		this.med = med;
		
	}
}
