package uy.edu.fing.pg.bgpsep.domain.model;

public class iBGPSession {
	private int idLink1;
	private int idLink2;
	private iBGPSessionType sessionType;
	
	public iBGPSession(int idLink1, int idLink2, iBGPSessionType sessionType) {
		this.idLink1 = idLink1;
		this.idLink2 = idLink2;
		this.sessionType = sessionType;
	}
	
	public int getIdLink1() {
		return idLink1;
	}
	public void setIdLink1(int idLink1) {
		this.idLink1 = idLink1;
	}
	public int getIdLink2() {
		return idLink2;
	}
	public void setIdLink2(int idLink2) {
		this.idLink2 = idLink2;
	}
	public iBGPSessionType getSessionType() {
		return sessionType;
	}
	public void setSessionType(iBGPSessionType sessionType) {
		this.sessionType = sessionType;
	}

}
