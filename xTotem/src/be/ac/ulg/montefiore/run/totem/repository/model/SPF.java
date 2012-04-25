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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;

import java.util.List;

/*
 * Changes:
 * --------
 *
 */

/**
 * This interface describes the Shortest Path First service.
 *
 * <p>Creation date: 25-May-2004
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface SPF {

    /**
     * Computes a unique shortest path between two nodes
     *
     * @param domain
     * @param src the source node
     * @param dst the destination node
     * @return a Path
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException
     */
    public Path computeSPF(Domain domain, String src, String dst)
        throws RoutingException, NoRouteToHostException;

    /**
     * Computes the shortest path between two nodes.
     *
     * @param domain
     * @param src the source node
     * @param dst the destination node
     * @param ECMP true if multipath activated
     * @return a list of equal cost multi path
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException
     */
    public List<Path> computeSPF(Domain domain, String src, String dst, boolean ECMP)
        throws RoutingException, NoRouteToHostException;

    /**
     * Computes a unique shortest path from a source node to all destination node
     *
     * @param domain
     * @param src the source node
     * @return a list of N-1 (N = number of nodes) Path
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException
     */
    public List<Path> computeSPF(Domain domain, String src)
        throws RoutingException, NoRouteToHostException;

    /**
     * Computes a unique shortest path from (resp. to) a node to (resp. from) all
     * the other nodes depending on the value of <code>isSource</code>. If
     * <code>isSource</code> is <code>true</code>, <code>node</code> is considered
     * as the source node and the method computes a shortest path from this node
     * to all the other nodes.
     * @param domain The domain on which the paths have to be computed.
     * @param node The source or destination node.
     * @param isSource <code>true</code> if <code>node</code> is the source and <code>false</code> otherwise.
     * @return The shortest paths from or to <code>node</code>.
     * @throws RoutingException
     * @throws NoRouteToHostException
     */
    public List<Path> computeSPF(Domain domain, boolean isSource, String node)
        throws RoutingException, NoRouteToHostException;
    
    /**
     * Computes the shortest paths from a source node to all destination node
     *
     * @param domain
     * @param src the source node
     * @param ECMP <code>true</code> if multipath is activated
     * @return a list of N-1 (N = number of nodes) Path
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException
     */
    public List<Path> computeSPF(Domain domain, String src, boolean ECMP)
        throws RoutingException, NoRouteToHostException;
    
    /**
     * Computes the shortest paths from (resp. to) a node to (resp. from) all
     * the other nodes depending on the value of <code>isSource</code>. If
     * <code>isSource</code> is <code>true</code>, <code>node</code> is considered
     * as the source node and the method computes a shortest path from this node
     * to all the other nodes.
     * @param domain The domain on which the paths have to be computed.
     * @param node The source or destination node.
     * @param isSource <code>true</code> if <code>node</code> is the source and <code>false</code> otherwise.
     * @param ECMP <code>true</code> if multipath is activated
     * @return The shortest paths from or to <code>node</code>.
     * @throws RoutingException
     * @throws NoRouteToHostException
     */
    public List<Path> computeSPF(Domain domain, boolean isSource, String node, boolean ECMP)
        throws RoutingException, NoRouteToHostException;
    
    /**
     * Computes a unique shortest path between all nodes in the topology
     *
     * @return a list of N * N-1 Path (N = number of nodes)
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException
     */
    public List<Path> computeFullMeshSPF(Domain domain)
        throws RoutingException, NoRouteToHostException;

    /**
     * Computes the shortest paths between all nodes in the topology
     *
     * @param ECMP <code>true</code> if multipath is activated
     * @return a list of N * N-1 Path (N = number of nodes)
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException
     */
    public List<Path> computeFullMeshSPF(Domain domain, boolean ECMP)
        throws RoutingException, NoRouteToHostException;

    public boolean equals(Object o);
    public int hashCode();
}
