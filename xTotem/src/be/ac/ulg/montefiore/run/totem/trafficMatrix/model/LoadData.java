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
package be.ac.ulg.montefiore.run.totem.trafficMatrix.model;

import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;

/*
* Changes:
* --------
*
*/

/**
 * Represent a view of the computed load for links in a domain.
 * <p/>
 * <p>Creation date: 29/01/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public interface LoadData {
    /**
     * Returns an newly created array that represent links load. For each link <code>lnk</code>of class Link in the domain,
     * the corresponding load is located at the index domain.getConvertor().getLinkId(lnk.getId()) of the returned array.
     * @return links load for all links in the domain.
     */
    public double[] getLoad();

    /**
     * Returns the load calculated for the given link or 0 if the link is not found.
     * @param link
     * @return Return the load calculated for the given link.
     */
    public double getLoad(Link link);

    /**
     * Returns an newly created array that represent links utilisation (link load / link cpacity).
     * For each link <code>lnk</code>of class Link in the domain,
     * the corresponding utilisation is located at the index domain.getConvertor().getLinkId(lnk.getId()) of the returned array.
     * @return links load for all links in the domain.
     */
    public double[] getUtilization();

    /**
     * Returns the utilisation calculated for the given link or 0 if the link is not found.
     * @param link
     * @return Return the utilisation calculated for the given link.
     */
    public double getUtilization(Link link);

    /**
     * Returns a new traffic matrix whose value for (src, dst) corresponds to the traffic dropped by node
     * <code>node</code> when routing traffic from src to dst.
     *
     * @param node
     * @return
     */
    public TrafficMatrix getDroppedTrafficMatrix(Node node);

    /**
     * Returns a new traffic matrix whose value for (src, dst) corresponds to the total dropped traffic when
     *  routing traffic from src to dst.
     *
     * @return
     */
    public TrafficMatrix getDroppedTrafficMatrix();

    /**
     * Gets total traffic dropped at node <code>node</code>.
     * @param node
     * @return
     */
    public double getDroppedTraffic(Node node);

    /**
     * Gets the total dropped traffic.
     * @return
     */
    public double getDroppedTraffic();

    /**
     * Adds a listener that signals change in the data.
     * @param listener
     */
    public void addListener(LoadDataListener listener);

    /**
     * Remove a listener.
     * @param listener
     */
    public void removeListener(LoadDataListener listener);

    /**
     * Tells all the listeners that data has changed.
     */
    public void notifyDataChange();

}
