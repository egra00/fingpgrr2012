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
package be.ac.ulg.montefiore.run.totem.repository.model;

import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;

import java.util.HashMap;
import java.util.List;

/*
 * Changes:
 * --------
 *  29-Nov-2005 : Added the possibility to obtain the algorithm parameters (getStartAlgoParameters()). (GMO)
 *  8-Dec.-2005 : Added getRunningParameters() method. (GMO)
 *  6-Mar.-2007 : start(.) now throws an exception (GMO)
 */

/**
 * Each algorithm must implemented this generic interface
 *
 * <p>Creation date: 23-Mar.-2004
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface TotemAlgorithm {

    /**
     * Used to start and initialize the algorithm
     */
    public void start(HashMap params) throws AlgorithmInitialisationException;

    /**
     * Used to stop the algorithm
     */ 
    public void stop();

    /**
     *  Returns the optional parameters that can be given when starting the algorithm
     * @return the list of algorithm parameters
     */
    public List<ParameterDescriptor> getStartAlgoParameters();

    /**
     * Returns the parameters given when the algorithm was started
     * @return
     */
    public HashMap getRunningParameters();

}
