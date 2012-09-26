package uy.edu.fing.repository.rrloc.algorithms;

public class iBGPSession {
	protected String idLink1;
	protected String idLink2;
	protected iBGPSessionType sessionType;
	
	public iBGPSession(String idLink1, String idLink2, iBGPSessionType sessionType) {
		this.idLink1 = idLink1;
		this.idLink2 = idLink2;
		this.sessionType = sessionType;
	}
	
	public String getIdLink1() {
		return idLink1;
	}
	public void setIdLink1(String idLink1) {
		this.idLink1 = idLink1;
	}
	public String getIdLink2() {
		return idLink2;
	}
	public void setIdLink2(String idLink2) {
		this.idLink2 = idLink2;
	}
	public iBGPSessionType getSessionType() {
		return sessionType;
	}
	public void setSessionType(iBGPSessionType sessionType) {
		this.sessionType = sessionType;
	}

}