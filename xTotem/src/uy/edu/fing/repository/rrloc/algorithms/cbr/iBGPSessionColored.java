package uy.edu.fing.repository.rrloc.algorithms.cbr;

import uy.edu.fing.repository.rrloc.algorithms.iBGPSession;
import uy.edu.fing.repository.rrloc.algorithms.iBGPSessionType;

/**
 * 
 * This class represent a iBGP session with a particual
 * color, used to colored based routing alrotihm
 *
 */
public class iBGPSessionColored extends iBGPSession {
	private String color = "";

	/**
	 * Create a iBGP session without a color
	 * 
	 * @param idLink1
	 * @param idLink2
	 * @param sessionType
	 */
	public iBGPSessionColored(String idLink1, String idLink2, iBGPSessionType sessionType) {
		super(idLink1, idLink2, sessionType);
	}
	
	/**
	 * 
	 * Create a iBGP session with a <em>color</em>
	 * 
	 * @param idLink1
	 * @param idLink2
	 * @param sessionType
	 * @param color
	 */
	public iBGPSessionColored(String idLink1, String idLink2, iBGPSessionType sessionType, String color) {
		super(idLink1, idLink2, sessionType);
		this.color = color; 
	}
	
	/**
	 * Set the session color
	 * 
	 * @param color
	 */
	public void setColor(String color) {
		this.color = color;
	}
	
	/**
	 * Get the session color
	 * 
	 * @return the session color
	 */
	public String getColor() {
		return color;
	}
	
	@Override
	public int hashCode() {
		return 0;
	};
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof iBGPSessionColored) {
			iBGPSessionColored s = (iBGPSessionColored)obj;
			try {
				boolean ret = obj != null && (
						// Es una sessión peer
						(
							(sessionType == s.getSessionType() && (sessionType == iBGPSessionType.peer)) && (
								// Coinciden las puntas
								(	
									// Dado a->b y a'->b' a=a' && b=b'	
									(
											// Coincide el link1 con el link1
											((idLink1 == null && s.getIdLink1() == null) || ((idLink1 != null && s.getIdLink1() != null) && (idLink1.equals(s.getIdLink1())))) &&
											// Coincide el link2 con el link2
											((idLink2 == null && s.getIdLink2() == null) || ((idLink2 != null && s.getIdLink2() != null) && (idLink2.equals(s.getIdLink2()))))
									) ||
									// Dado a->b y a'->b' a=b' && b=a'
									(
											// Coincide el link1 con el link2
											((idLink1 == null && s.getIdLink2() == null) || ((idLink1 != null && s.getIdLink2() != null) && (idLink1.equals(s.getIdLink2())))) &&
											// Coincide el link2 con el link1
											((idLink2 == null && s.getIdLink1() == null) || ((idLink2 != null && s.getIdLink1() != null) && (idLink2.equals(s.getIdLink1()))))
									)
								) &&
								// Coincide el color
								color == s.getColor()
							)
						) ||
						// Es una sessión client
						(
							(sessionType == s.getSessionType() && (sessionType == iBGPSessionType.client)) && (
								// Coinciden las puntas, dado a->b y a'->b' a=a' && b=b'	
								(
										// Coincide el link1 con el link1
										((idLink1 == null && s.getIdLink1() == null) || ((idLink1 != null && s.getIdLink1() != null) && (idLink1.equals(s.getIdLink1())))) &&
										// Coincide el link2 con el link2
										((idLink2 == null && s.getIdLink2() == null) || ((idLink2 != null && s.getIdLink2() != null) && (idLink2.equals(s.getIdLink2()))))
								) &&
								// Coincide el color
								color == s.getColor()
							)
						)
					);
				return ret;
			}
			catch (NullPointerException e) {
				return false;
			}
		}
		if (obj instanceof String) {
			return this.getColor() == null ? obj == null : this.getColor().equals(obj);
		}
		return super.equals(obj);
	}

}
