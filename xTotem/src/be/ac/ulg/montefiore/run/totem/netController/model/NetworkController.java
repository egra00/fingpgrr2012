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
package be.ac.ulg.montefiore.run.totem.netController.model;

import java.util.HashMap;
import java.util.List;

import be.ac.ulg.montefiore.run.totem.domain.model.DomainChangeListener;
import be.ac.ulg.montefiore.run.totem.netController.exception.NetworkControllerInitialisationException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

/*
 * Changes:
 * --------
 * - 13-Mar-2007: add getStartParameters() method (GMO)
 * - 11-May-2007: add stop() method (GMO)
 */

/**
 * This interface must be implemented by all the network controllers. A
 * network controller is a component which listens to the events occurring on a
 * network and which reacts in some way to these events.
 *
 * <p>Creation date: 23-mars-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public interface NetworkController extends DomainChangeListener {
    
    /**
     * Starts the network controller.
     * @param params The parameters to initialise the network controller. See
     * the specific documentation of each network controller to have a list of
     * the supported parameters.
     * @throws NetworkControllerInitialisationException If an error occurs
     * during the initialisation of the controller.
     */
    public void start(HashMap<String, String> params) throws NetworkControllerInitialisationException;

    /**
     * Stop the network controller. Should remove listeners.
     */
    public void stop();

    /**
     *  Returns the optional parameters that can be given when starting the algorithm
     * @return the list of algorithm parameters
     */
    public List<ParameterDescriptor> getStartParameters();


}
