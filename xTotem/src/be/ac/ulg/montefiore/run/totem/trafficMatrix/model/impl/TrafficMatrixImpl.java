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

package be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Calendar;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.DomainConvertor;
import be.ac.ulg.montefiore.run.totem.domain.model.BandwidthUnit;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrixChangeObserver;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;

/*
* Changes:
* --------
*  - 10-Mar-2005: Add stopOnError parameter in the getLinkLoad() methods (FSK).
*  - 18-May-2005: Add ECMP parameter in the getLinkLoad() methods (JL).
*  - 2-Jun-2005: init matrix to zero in the constructor (FSK)
*  - 20-Mar-2006: Add methods to manage the tmId (GMO).
*  - 20-Mar-2006: Add notification of change via observer (GMO)
*  - 20-Mar-2006: Deprecates methods (set/get LinkLoadStrategy(), getLinkUtilisation(), getLinkLoad()) (GMO).
*  - 11-Jul-2006: Add getUnit(), setUnit() and convertUnit() methods (GMO)
*  - 11-Jul-2006: Add toOneDimensionalArray() and toTwoDimensionalArray() (GMO)
*  - 14-Jun-2007: Add constructor with a domain as parameter (GMO)
*/

/**
 * Implementation of the {@link be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix} interface.
 *
 * <p>Creation date: 28-janv.-2005
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class TrafficMatrixImpl implements TrafficMatrix {

    private TrafficMatrixChangeObserver observer;
    private Calendar date;
    private double duration;
    private Domain domain;
    private float[][] matrix;
    private int tmId;
    private boolean isSetTmId = false;
    private BandwidthUnit unit = null;
    private LinkLoadStrategy lls = null;

    public TrafficMatrixImpl(Domain domain) {
        this.domain = domain;
        int maxNodeId = domain.getConvertor().getMaxNodeId();
        matrix = new float[maxNodeId][maxNodeId];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = 0;
            }
        }
        lls = new SPFLinkLoadStrategy(domain, this);
        observer = new TrafficMatrixChangeObserverImpl();
    }

    /**
     * Creates a new <code>TrafficMatrixImpl</code> object.
     * @param asId The AS ID of the traffic matrix.
     * @throws InvalidDomainException If there is no domain with the AS ID <code>asId</code>.
     */
    public TrafficMatrixImpl(int asId) throws InvalidDomainException {
        domain = InterDomainManager.getInstance().getDomain(asId);
        int maxNodeId = domain.getConvertor().getMaxNodeId();
        matrix = new float[maxNodeId][maxNodeId];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = 0;              
            }
        }
        lls = new SPFLinkLoadStrategy(domain, this);
        observer = new TrafficMatrixChangeObserverImpl();
    }

    public TrafficMatrixChangeObserver getObserver() {
        return observer;
    }

    public int getTmId() throws TrafficMatrixIdException {
        if (!isSetTmId) throw new TrafficMatrixIdException("TMid is not set.");
        return tmId;
    }

    public void setTmId(int tmId) throws TrafficMatrixIdException {
        if (isSetTmId) throw new TrafficMatrixIdException("TMid is already set.");
        isSetTmId = true;
        this.tmId = tmId;
    }

    public boolean isSetTmId() {
        return isSetTmId;
    }

    public void unsetTmId() {
        isSetTmId = false;
    }

    /**
     * @see be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix#getASID()
     */
    public int getASID() {
        return domain.getASID();
    }

    /**
     * Converts the values used in the matrix representation to the <code>convertTo</code> unit.
     *
     * if unit has not already been set, it simply sets the unit and return.
     *
     * @param convertTo
     */
    private void convertUnit(BandwidthUnit convertTo) {
        if (convertTo == null) throw new IllegalArgumentException("Null argument");

        //nothing to convert
        if (unit == convertTo) return;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                matrix[i][j] = convertTo.convert(unit, matrix[i][j]);
            }
        }
        unit = convertTo;
    }

    /**
     * returns the unit used in the matrix representation
     *
     * @return
     */
    public BandwidthUnit getUnit() {
        return unit;
    }

    /**
     * A call to this function indicates the unit used in the matrix representation. Values are converted to correspond
     * to the domain units.
     * If <code>unit</code> is <code>BandwidthUnit.DEFAULT_UNIT. The unit of the domain will be used (no conversion).
     * This function can be called only if the unit has not already been set or with an argument equals to the unit already used.
     * Otherwise an <code>IllegalAccessError</code> will be thrown.
     * @param unit
     */
    public void setUnit(BandwidthUnit unit) {
        if (this.unit == unit) return;
        if (this.unit != null) throw new IllegalAccessError("Unit is already set.");

        if (unit == BandwidthUnit.DEFAULT_UNIT) {
            this.unit = domain.getBandwidthUnit();
        } else {
            this.unit = unit;
        }
        convertUnit(domain.getBandwidthUnit());
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    /**
     * @see be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix#iterator()
     */
    public Iterator<TrafficMatrixElem> iterator() {
        return new Itr();
    }

    /**
     * @see be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix#get(java.lang.String, java.lang.String)
     */
    public float get(String src, String dst) throws NodeNotFoundException {
        DomainConvertor convertor = domain.getConvertor();
        int srcId = convertor.getNodeId(src);
        int dstId = convertor.getNodeId(dst);
        return matrix[srcId][dstId];
    }

    /**
     * @see be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix#get(int, int)
     */
    public float get(int src, int dst) throws NodeNotFoundException {
        DomainConvertor convertor = domain.getConvertor();
        // We check if src and dst are correct
        convertor.getNodeId(src);
        convertor.getNodeId(dst);
        return matrix[src][dst];
    }

    /**
     * @see be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix#set(java.lang.String, java.lang.String, float)
     */
    public void set(String src, String dst, float value) throws NodeNotFoundException {
        DomainConvertor convertor = domain.getConvertor();
        int srcId = convertor.getNodeId(src);
        int dstId = convertor.getNodeId(dst);
        matrix[srcId][dstId] = value;
        observer.notifyElementChange(src, dst);
    }

    /**
     * @see be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix#set(int, int, float)
     */
    public void set(int src, int dst, float value) throws NodeNotFoundException {
        DomainConvertor convertor = domain.getConvertor();
        // We check if src and dst are correct
        String srcStr = convertor.getNodeId(src);
        String dstStr = convertor.getNodeId(dst);
        matrix[src][dst] = value;
        observer.notifyElementChange(srcStr, dstStr);
    }

    /**
     * Returns an array representing the matrix in one-dimensional form.
     * The value associated with the pair (srcId, dstId) is at index
     * <code>domain.getConvertor.getNodeId(srcId)*domain.getConvertor.getMaxNodeId()+domain.getConvertor.getNodeId(dstId)</code>
     * in the resulting array.
     * @return
     */
    public float[] toOneDimensionalArray() {
        int nbNodes = domain.getConvertor().getMaxNodeId();
        float[] result = new float[nbNodes*nbNodes];
        for (int i = 0; i < nbNodes; i++) {
            for (int j = 0; j < nbNodes; j++) {
                result[i*nbNodes+j] = matrix[i][j];
            }
        }
        return result;
    }

    /**
     * Returns a float array representing the traffic matrix.
     * The value associated with the pair (srcId, dstId) is at index
     * <code>[domain.getConvertor.getNodeId(srcId)][domain.getConvertor.getNodeId(dstId)]</code>.
     * @return
     */
    public float[][] toTwoDimensionalArray() {
        return matrix;
    }

    /**
     * Set the strategy of link load computation. This strategy is used to compute link load and
     * link utilisation.
     *
     * @param strategy the link load computation strategy
     * @deprecated Use class {@link be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer} instead.
     */
    public void setLinkLoadStrategy(LinkLoadStrategy strategy) {
        this.lls = strategy;
    }

    /**
     * Get the link load computation strategy
     *
     * @return the link load computation strategy
     * @deprecated Use class {@link be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer} instead.
     */
    public LinkLoadStrategy getLinkLoadStrategy() {
        return lls;
    }

    /**
     * Returns the link loads computed by the LinkLoadStrategy.
     * <p/>
     * The link load is defined by the total amound of traffic on the link express in bandwidth unit.
     * <p/>
     * Let <code>ret</code> be the returned array. <code>ret[i]</code> is the
     * load of the link <code>InterDomainManager.getInstance().getDomain(this.getASID()).getConvertor().getLinkId(i)</code>.
     *
     * @return the link loads computed by the LinkLoadStrategy.
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException
     *          If there is no route between a pair of nodes.
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException
     *          if an error occurred during the routing process.
     * @deprecated Use class {@link be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer} instead.
     */
    public double[] getLinkLoad() throws NoRouteToHostException, RoutingException {
        lls.recompute();
        return lls.getData().getLoad();
    }

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
     * @deprecated Use class {@link be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer} instead.
     */
    public double[] getLinkUtilisation() throws NoRouteToHostException, RoutingException {
        lls.recompute();
        return lls.getData().getUtilization();
    }

    private class Itr implements Iterator<TrafficMatrixElem> {
        private int i,j; // current position in the matrix
        private boolean hasBeenReturned; // true if the element on the current position has been returned

        public Itr() {
            i = 0;
            j = -1;
            hasBeenReturned = true;
        }

        public TrafficMatrixElem next() {
            if(!hasBeenReturned) {
                hasBeenReturned = true;
                return new TrafficMatrixElemImpl(i, j, matrix[i][j]);
            }
            else {
                if(!hasNext()) {
                    throw new NoSuchElementException("No more elements in the matrix.");
                }
                hasBeenReturned = true;
                return new TrafficMatrixElemImpl(i, j, matrix[i][j]);
            }
        }

        public boolean hasNext() {
            if(!hasBeenReturned) {
                return true;
            }

            boolean hasNext = false;
            ++j;
            // We look for the next element to return.
            outerloop:
            for(; i < matrix.length; ++i) {
                for(; j < matrix.length; ++j) {
                    try {
                        // We check if the current position is acceptable
                        domain.getConvertor().getNodeId(i);
                        domain.getConvertor().getNodeId(j);
                        hasNext = true;
                        break outerloop;
                    }
                    catch(NodeNotFoundException e) {
                        // current position not acceptable. Nothing to do, just iterate.
                    }
                }
                j = 0;
            }

            if(hasNext) {
                hasBeenReturned = false;
                return true;
            }
            else {
                return false;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException("The remove operation is not supported by this iterator.");
        }
    }

    private class TrafficMatrixElemImpl implements TrafficMatrixElem {
        private int src, dst;
        private float value;

        public TrafficMatrixElemImpl(int src, int dst, float value) {
            this.src = src;
            this.dst = dst;
            this.value = value;
        }

        public int getSrc() {
            return src;
        }

        public int getDst() {
            return dst;
        }

        public float getValue() {
            return value;
        }
    }
}
