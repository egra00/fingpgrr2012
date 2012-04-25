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

import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.SPFCacheImpl;

import java.util.List;

/*
 * Changes:
 * --------
 * 20-Mar.-2006: Added stopOnError parameter to getPath. (GMO)
 */

/**
 * The SPFCache is designed to improve the performance of SPF path computation.
 *
 * It's based on the assumption that path does not change except when a link metric
 * change, a link status change, a link addition or a link remove. So it can
 * improve the performance if multiple call to getPath are done without metric, status or links changes.
 *
 * <p>Creation date: 07-Jul-2005 15:03:45
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface SPFCache {

    /**
     * Get the domain listener of the cache
     *
     * @return the domain listener of the cache 
     */
    public SPFCacheListener getListener();

    /**
     * Get the SPF path between a source node and a destination node
     *
     * @param src the source node
     * @param dst the destination node
     * @return the SPF path
     * @throws NoRouteToHostException
     * @throws RoutingException
     */
    public Path getPath(Node src, Node dst) throws NoRouteToHostException, RoutingException;

    /**
     * Get all the SPF path between a source node and a destination node if the ECMP is true
     * and a single SPF otherwise
     *
     * @param src the source node
     * @param dst the destination node
     * @param ECMP true if equal cost multiple path is activated and false otherwise
     * @return a list of equal cost SPF
     * @throws NoRouteToHostException
     * @throws RoutingException
     */
    public List<Path> getPath(Node src, Node dst, boolean ECMP) throws NoRouteToHostException, RoutingException;

    /**
     * Get all the SPF path between a source node and a destination node if the ECMP is true
     * and a single SPF otherwise
     *
     * @param src the source node
     * @param dst the destination node
     * @param ECMP true if equal cost multiple path is activated and false otherwise
     * @param stopOnError
     * @return a list of equal cost SPF
     * @throws NoRouteToHostException
     * @throws RoutingException
     */
    public List<Path> getPath(Node src, Node dst, boolean ECMP, boolean stopOnError) throws NoRouteToHostException, RoutingException;
    /**
     * Remove the SPF path computed between source node an destination node from the cache
     *
     * This method must be used if the path have potentially changed.
     *
     * @param src the source node
     * @param dst the destination node
     */
    public void clearPath(Node src,Node dst);

    /**
     * Remove all the SPF path in the cache
     */
    public void clear();

    /**
     * The listener that updates the SPF cache
     */
    public interface SPFCacheListener extends DomainChangeListener { }

}
