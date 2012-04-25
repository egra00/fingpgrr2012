/* TOTEM-v3.2 June 18 2008*/

/*
 * ===========================================================
 * TOTEM : A TOolbox for Traffic Engineering Methods
 * ===========================================================
 *
 * (C) Copyright 2004-2006, by Research Unit in Networking RUN, University of Liege. All Rights Reserved.
 *
 * Project Info:  http://totem.run.montefiore.ulg.ac.be
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License version 2.0 as published by the Free Software Foundation;
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
*/
package be.ac.ulg.montefiore.run.totem.topgen.util;

import org.apache.log4j.Logger;

import java.util.*;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.TrafficMatrixImpl;
/*
 * Changes:
 * --------
 *
 */

/**
*
*
* <p>Creation date: 8 janv. 07
*
* @author Georges Nimubona (nimubonageorges@hotmail.com)
*/
public class PlacementObject {
	
	private HashMap<String,String> parameters;
	
	
	public PlacementObject() {
		parameters = new HashMap<String,String>();
	}

	public String getParam(String name) throws InvalidParameterException {
		if(parameters.get(name) == null)
			throw new InvalidParameterException("Invalid parameter name :" + name);
		else return parameters.get(name);
	}

	public void setParam(String name, String value)
			throws InvalidParameterException {
		parameters.put(name, value);
	}
	
	public TrafficMatrix buildTM(double[][] elements) throws TMBuildingException {
		int length = elements.length;
		
		try {
			TrafficMatrix tm = new TrafficMatrixImpl(Integer.parseInt(parameters.get("ASID")));
			for(int i = 0; i < length; i++)
				for(int j = 0; j < length; j++)
					if(i == j)
						tm.set(i,j,0f);
					else {
						tm.set(i,j, (float) elements[i][j]);
						//System.out.println(elements[i][j]);
					}
			return tm;
		} 
		catch (NumberFormatException e) {
			throw new TMBuildingException(e.getMessage());
		} 
		catch (InvalidDomainException e) {
			throw new TMBuildingException(e.getMessage());
		}
		catch (NodeNotFoundException e) {
			throw new TMBuildingException(e.getMessage());
		}
	}
}
