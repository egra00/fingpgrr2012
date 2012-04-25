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
package be.ac.ulg.montefiore.run.totem.topgen.traffic;

import org.apache.log4j.Logger;

import java.util.*;

import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

/*
 * Changes:
 * --------
 *
 *
 */

/**
 * Interface of a traffic generator class	
 *
 * <p>Creation date: 26 avr. 07
 *
 * @author Georges Nimubona (nimubonageorges@hotmail.com)
 */

public interface TrafficGeneratorInterface {
	
	/**
     * set user parameters.
     *
     * @param name name of the parameter.
     * @param value value of the parameter.
     * @throws InvalidParameterException If the parameter is invalid.
     */
	public void setParam(String name, String value) throws InvalidParameterException;
	
	/**
     * get the value of the specified parameter.
     *
     * @param name name of the parameter.
     * @throws InvalidParameterException If the parameter is invalid.
     * @return the value of the parameter. 
     */
	public String getParam(String name) throws InvalidParameterException;
	
	/**
     * Generate a traffic matrix.
     *
     * @return a list of the domains in the topology. 
     * @throws TrafficGenerationException If an error occurs.
     */
	public List<TrafficMatrix> generate() throws TrafficGenerationException;

    /**
     * Returns a list of parameters that can be used with the generator.
     * @return
     */
    public List<ParameterDescriptor> getAvailableParameters();
}
 