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

package be.ac.ulg.montefiore.run.totem.topgen.topology;

import java.util.List;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

/*
 * Changes:
 * --------
 * - 31-Oct-2007: Add getAvailableParameters() method. (GMO)
 *
 */

/**
 * Interface of a topology generator
 *
 * <p>Creation date: Apr 25, 2007
 *
 * @author Georges Nimubona (nimubonageorges@hotmail.com)
 */

public interface TopologyGenerator {

	/**
	 * set user parameters.
	 *
	 * @param name name of the parameter.
	 * @param value value of the parameter.
	 * @throws InvalidParameterException If the parameter is invalid.
	 */
	public void setParam(String name, String value)
			throws InvalidParameterException;

	/**
	 * get the value of the specified parameter.
	 *
	 * @param name name of the parameter.
	 * @throws InvalidParameterException If the parameter is invalid.
	 * @return the value of the parameter. 
	 */
	public String getParam(String name)
			throws InvalidParameterException;

	/**
	 * This method generates topology with the setted parameters.
	 */
	public List<Domain> generate() throws TopologyGeneratorException;

    /**
     * Returns a list of parameters that can be used with the generator
     * @return
     */
    public List<ParameterDescriptor> getAvailableParameters();
}