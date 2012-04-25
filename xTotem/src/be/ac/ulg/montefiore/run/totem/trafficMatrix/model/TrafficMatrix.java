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

import java.util.Iterator;
import java.util.Calendar;

import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.BandwidthUnit;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;

/*
 * Changes:
 * --------
 *  - 10-Mar-2005: Add stopOnError parameter in the getLinkLoad() methods (FSK).
 *  - 18-May-2005: Add ECMP parameter in the getLinkLoad() methods (JL).
 *  - 20-Mar-2006: Add methods to manage the tmId (GMO).
 *  - 20-Mar-2006: Add a change observer in getObserver() (GMO)
 *  - 20-Mar-2006: Deprecates methods (set/get LinkLoadStrategy(), getLinkUtilisation(), getLinkLoad()) (GMO).
 *  - 11-Jul-2006: Add getUnit() and setUnit() (GMO)
 */

/**
 * This class represents a traffic matrix.
 *
 * <p>Creation date: 27-janv.-2005
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public interface TrafficMatrix extends Iterable {

    /**
     * Get the change observer.
     * @return the observer
     */
    public TrafficMatrixChangeObserver getObserver();

    /**
     * Returns the id associated with the traffic matrix if it has already been set.
     * @return The traffic matrix Id
     * @throws TrafficMatrixIdException if the id has not been set.
     */
    public int getTmId() throws TrafficMatrixIdException;

    /**
     * Associate an id with the matrix if it not already set.
     * This method can be called only once without throwing an exception.
     * Use {@link #unsetTmId()} to reset the tm id.
     * @throws TrafficMatrixIdException if the id has already been set.
     */
    public void setTmId(int tmId) throws TrafficMatrixIdException;

    /**
     * Returns true if the id had been set
     * @return
     */
    public boolean isSetTmId();

    /**
     * Clear the Tm Id associated with the matrix.
     */
    public void unsetTmId();

    /**
     * Returns the AS ID of the traffic matrix.
     * @return The AS ID of the traffic matrix.
     */
    public int getASID();

    /**
     * returns the unit used in the matrix representation
     * @return
     */
    public BandwidthUnit getUnit();

    /**
     * This function sets the unit of the matrix representation.
     * It can be called only if the unit has not already been set or with an argument equals to the unit already set.
     * Otherwise an <code>IllegalAccessError</code> will be thrown.
     * @param unit
     */
    public void setUnit(BandwidthUnit unit);

    /**
     * Get the date of matrix measurement
     *
     * @return the date of matrix measurement
     */
    public Calendar getDate();

    /**
     * Set the date of matrix measurement
     *
     * @param date the date of matrix measurement
     */
    public void setDate(Calendar date);

    /**
     * Get the matrix measurement duration
     *
     * @return the matrix measurement duration
     */
    public double getDuration();

    /**
     * Set the matrix measurement duration
     * @param duration the matrix measurement duration
     */
    public void setDuration(double duration);

    /**
     * Returns an iterator over the elements in this traffic matrix. The
     * elements of the traffic matrix are returned row by row by the iterator.
     * <strong>Note that the returned iterator doesn't support the 
     * <code>remove</code> operation.</strong>
     * @return An iterator over the elements in this traffic matrix.
     */
    public Iterator<TrafficMatrixElem> iterator();

    /**
     * Returns the amount of traffic exchanged by the nodes <code>src</code> and <code>dst</code>.
     * @param src The source node.
     * @param dst The destination node.
     * @return The amount of traffic exchanged by the nodes <code>src</code> and <code>dst</code>.
     * @throws NodeNotFoundException If there is no node <code>src</code> or no node <code>dst</code>.
     */
    public float get(String src, String dst) throws NodeNotFoundException;
    
    /**
     * Returns the amount of traffic exchanged by the nodes <code>src</code> and <code>dst</code>.
     * @param src The source node.
     * @param dst The destination node.
     * @return The amount of traffic exchanged by the nodes <code>src</code> and <code>dst</code>.
     * @throws NodeNotFoundException If there is no node <code>src</code> or no node <code>dst</code>.
     */
    public float get(int src, int dst) throws NodeNotFoundException;
    
    /**
     * Sets the amount of traffic exchanged by the nodes <code>src</code> and <code>dst</code>.
     * @param src The source node.
     * @param dst The destination node.
     * @param value The amount of traffic.
     * @throws NodeNotFoundException If there is no node <code>src</code> or no node <code>dst</code>.
     */
    public void set(String src, String dst, float value) throws NodeNotFoundException;
    
    /**
     * Sets the amount of traffic exchanged by the nodes <code>src</code> and <code>dst</code>.
     * @param src The source node.
     * @param dst The destination node.
     * @param value The amount of traffic.
     * @throws NodeNotFoundException If there is no node <code>src</code> or no node <code>dst</code>.
     */
    public void set(int src, int dst, float value) throws NodeNotFoundException;

    /**
     * Returns an array representing the matrix in one-dimensional form.
     * The value associated with the pair (srcId, dstId) is at index
     * <code>domain.getConvertor.getNodeId(srcId)*domain.getConvertor.getMaxNodeId()+domain.getConvertor.getNodeId(dstId)</code>
     * in the resulting array.
     * @return
     */
    public float[] toOneDimensionalArray();

    /**
     * Returns a float array representing the traffic matrix.
     * The value associated with the pair (srcId, dstId) is at index
     * <code>[domain.getConvertor.getNodeId(srcId)][domain.getConvertor.getNodeId(dstId)]</code>.
     * @return
     */
    public float[][] toTwoDimensionalArray();

    /**
     * Set the strategy of link load computation. This strategy is used to compute link load and
     * link utilisation.
     *
     * @param strategy the link load computation strategy
     * @deprecated Use class {@link LinkLoadComputer} instead.
     */
    @Deprecated
    public void setLinkLoadStrategy(LinkLoadStrategy strategy);

    /**
     * Get the link load computation strategy
     *
     * @return the link load computation strategy
     * @deprecated Use class {@link LinkLoadComputer} instead.
     */
    @Deprecated
    public LinkLoadStrategy getLinkLoadStrategy();

    /**
     * Returns the link loads computed by the LinkLoadStrategy.
     *
     * The link load is defined by the total amound of traffic on the link express in bandwidth unit.
     * The returned array is indexed by the integer indexes of the domain converter. These indexes can be non-consecutive
     * and thus, some holes can appear in the array. For this reason, the number of elements of the array can be higher
     * than the number of links of the network.
     *
     * Let <code>ret</code> be the returned array. <code>ret[i]</code> is the
     * load of the link <code>InterDomainManager.getInstance().getDomain(this.getASID()).getConvertor().getLinkId(i)</code>.
     *
     * @return the link loads computed by the LinkLoadStrategy.
     * @throws NoRouteToHostException If there is no route between a pair of nodes.
     * @throws RoutingException if an error occurred during the routing process.
     * @deprecated Use class {@link LinkLoadComputer} instead.
     */
    @Deprecated
    public double[] getLinkLoad() throws NoRouteToHostException, RoutingException;

    /**
     * Returns the link utilisations computed by the LinkLoadStrategy.
     *
     * The link utilisation is defined by the link load divided by the link capacity express in percentage.
     *
     * Let <code>ret</code> be the returned array. <code>ret[i]</code> is the
     * utilisation of the link <code>InterDomainManager.getInstance().getDomain(this.getASID()).getConvertor().getLinkId(i)</code>.
     *
     *
     * @return the link utilisations if we route the traffic using a SPF algorithm.
     * @throws NoRouteToHostException if there is no route between a pair of nodes.
     * @throws RoutingException if an error occurred during the routing process.
     * @deprecated Use class {@link LinkLoadComputer} instead.
     */
    @Deprecated
    public double[] getLinkUtilisation() throws NoRouteToHostException, RoutingException;


    /**
     * Represents an element of the traffic matrix. 
     *
     * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
     */
    public interface TrafficMatrixElem {
        
        /**
         * Returns the src index.
         * @return The src index.
         */
        public int getSrc();
        
        /**
         * Returns the dst index.
         * @return The dst index.
         */
        public int getDst();
        
        /**
         * Returns the amount of traffic between the two nodes.
         * @return The amount of traffic between the two nodes.
         */
        public float getValue();
    }
}
