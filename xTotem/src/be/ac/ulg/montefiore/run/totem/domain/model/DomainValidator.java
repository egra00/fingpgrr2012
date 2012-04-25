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
package be.ac.ulg.montefiore.run.totem.domain.model;

import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;

import java.util.List;

/*
 * Changes:
 * --------
 *  1-Jun-2005 : add isEqualCostMultiPath method (FSK)
 *  14-Nov-2005 : change isNoMultiGraph into isMultiGraph (GMO)
 *  24-Apr-2006 : add getEqualCostMultiPath() (GMO)
 *  21-Jun-2007 : add getEqualCostMultiPath(.) with algo parameter (GMO)
 */

/**
 * A DomainValidator can check and force some characteristics on a Domain
 *
 * <p>Creation date: 12-Jan-2005 17:51:08
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface DomainValidator {

    /**
     * Verify if an IGP information section is present
     * @return true if the section is present and false otherwise
     */
    public boolean isIGPInfo();

    /**
     * Checks if the graph is connected.
     * @return true if the graph is connected and false if not.
     */
    public boolean isConnected() throws NodeNotFoundException;

    /**
     * This method return true if the domain contains NO loop links.
     */
    public boolean isLoopFree() throws NodeNotFoundException;

    /**
     * Checks that all the nodes of the topology have at least an outlink and an inlink.
     * If it's not the case, it will add a link that is the "inverse" of an existing link.
     * This method throws an <code>IllegalArgumentException</code> if the topology isn't connected.
     */
    public boolean isDuplexConnected() throws NodeNotFoundException, LinkNotFoundException;

    /**
     * Check if there exist at least two link coming into and going out of each node.
     *
     * @return true if there exist at least two link coming into and going out of each node and false otherwise
     */
    public boolean isDualConnected() throws NodeNotFoundException;

    /**
     * Check if multiple links exists
     *
     * @return true if multiple links exists and false otherwise
     */
    public boolean isMultiGraph() throws NodeNotFoundException;

    /**
     * Check if there exists equal cost multi path in the topology
     *
     * @return true if there is equal cost multi path and false otherwise
     */
    public boolean isEqualCostMultiPath() throws NoRouteToHostException, RoutingException;

    /**
     * Returns a list containing lists of paths. The inner list contains all equal cost paths from some source
     * to some destination. Routing is done to thanks to the CSPF algorithm.
     * @return
     * @throws NoRouteToHostException
     * @throws RoutingException
     */
    public List<List<Path>> getEqualCostMultiPath() throws NoRouteToHostException, RoutingException;

    /**
     * Returns a list containing lists of paths. The inner list contains all equal cost paths from some source
     * to some destination. Routing is done to thanks to the specified algorithm.
     * @return
     * @throws NoRouteToHostException
     * @throws RoutingException
     * @throws NoSuchAlgorithmException If the given algorithm cannot be found.
     */
    public List<List<Path>> getEqualCostMultiPath(String algo) throws NoRouteToHostException, RoutingException, NoSuchAlgorithmException;

    /**
     * Adds the IGP information if not present.
     */
    public void forceIGPInfo();

    /**
     * Removes nodes and links that are not in the connected set of maximum size so that the graph is connected.
     */
    public void forceConnected() throws NodeNotFoundException, LinkNotFoundException;

    /**
     * This method removes all the loops present in the topology.
     * A loop is a link with the source node that is the same as the destination node.
     */
    public void forceLoopFree() throws NodeNotFoundException, LinkNotFoundException;

    /**
     * Checks that all the nodes of the topology have at least an outlink and an inlink.
     * If it's not the case, it will add a link that is the "inverse" of an existing link.
     * This method throws an <code>IllegalArgumentException</code> if the topology isn't connected.
     */
    public void forceDuplexConnected() throws NodeNotFoundException, LinkNotFoundException, LinkAlreadyExistException;

    /**
     * Remove multiple links. If the metric of the multiple links are equals, we merge the
     * capacity of the two links.
     */
    public void forceNoMultiGraph();

}
