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

import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.HybridLoadData;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LoadData;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;
import be.ac.ulg.montefiore.run.totem.repository.model.SPF;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;

import java.util.List;
import java.util.ArrayList;

/*
 * Changes:
 * --------
 * - 20-Mar-2006 : implements equals and hashCode (GMO).
 * - 19-Jan-2007 : add toString() method (GMO)
 * - 13-Aug-2007 : use isECMP() method instead of ECMP variable (GMO)
 */

/**
 *
 * This adapter can be used by link load strategy implementations to avoid
 * property methods redefinition. It uses {@link HybridLoadData}.
 *
 * <p>Creation date: 28-Jun-2005 18:00:25
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public abstract class AbstractLinkLoadStrategy extends AbstractLinkLoadComputer implements LinkLoadStrategy {
    protected boolean ECMP;
    protected SPF spf;
    protected TrafficMatrix tm;

    protected SettableHybridLoadData data;

    /**
     * Creates a linkLoadStrategy object to compute load on the given domain from the given traffic matrix.
     * Sets the default property value : ECMP = false, stopOnError = true, spf = CSPF.
     * Use the default {@link LinkLoadComputerInvalidator} ({@link LinkLoadStrategyInvalidator}).
     *
     * @param domain
     * @param tm
     */
    protected AbstractLinkLoadStrategy(Domain domain, TrafficMatrix tm) {
        super(domain, null);
        data = new SettableHybridLoadData(domain);
        this.tm = tm;
        ECMP = false;
        spf = new CSPF();
        changeListener = new LinkLoadStrategyInvalidator(this);
    }

    /**
     * Creates a linkLoadStrategy object to compute load on the given domain from the given traffic matrix.
     * Sets the default property value : ECMP = false, stopOnError = true, spf = CSPF.
     * Use the given {@link LinkLoadComputerInvalidator} as invalidator.
     *
     * @param domain
     * @param tm
     * @param changeListener
     */
    protected AbstractLinkLoadStrategy(Domain domain, TrafficMatrix tm, LinkLoadComputerInvalidator changeListener) {
        super(domain, null);
        data = new SettableHybridLoadData(domain);
        this.tm = tm;
        ECMP = false;
        spf = new CSPF();
        this.changeListener = changeListener;
    }

    /**
     * Sets a new traffic matrix to calculate load. Also invalidate the data.
     * @param tm
     */
    public void setTm(TrafficMatrix tm) {
        if (this.tm != null) this.tm.getObserver().removeListener(changeListener);
        invalidate();
        this.tm = tm;
    }

    public LoadData detachData() {
        SettableHybridLoadData oldData = data;
        data = null;
        return oldData;
    }

    /**
     * Returns hybrid data
     * @return
     */
    public HybridLoadData getData() {
        return data;
    }

    /**
     * Get the ECMP (Equal-Cost Multi-Path) property.
     * <p/>
     * By default : false
     *
     * @return true if equal-cost multi-path is activated and false otherwise
     */
    public boolean isECMP() {
        return ECMP;
    }

    /**
     * Set the ECMP (Equal-Cost Multi-Path) property.
     *
     * @param ECMP true if equal-cost multi-path must be activated and false otherwise
     */
    public void setECMP(boolean ECMP) {
        this.ECMP = ECMP;
    }

    /**
     * Get the SPF (Shortest Path First algorithm) property. The SPF is the routing algorithm used to compute the link load.
     * <p/>
     * By default : dijkstra SPF implemented by the class CSPF.
     *
     * @return the SPF
     */
    public SPF getSPFAlgo() {
        return spf;
    }

    /**
     * Set the SPF (Shortest Path First algorithm) property. The SPF is the routing algorithm used to compute the link load.
     *
     * @param spf the SPF to use in the link load computation
     */
    public void setSPFAlgo(SPF spf) {
        this.spf = spf;
        invalidate();
    }
    public String getShortName() {
        return toString();
    }

    public List<TrafficMatrix> getTrafficMatrices() {
        List<TrafficMatrix> list = new ArrayList<TrafficMatrix>(1);
        list.add(tm);
        return list;
    }

    public String toString() {
        String tmId = "INVALID";
        try {
            tmId = String.valueOf(tm.getTmId());
        } catch (TrafficMatrixIdException e) {
        }
        return getClass().getSimpleName() + " (TM: " + tmId + ", Algo: " + getSPFAlgo().getClass().getSimpleName() + ", ECMP: " + (isECMP() ? "YES" : "NO") + ")";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractLinkLoadStrategy)) return false;

        final AbstractLinkLoadStrategy lls = (AbstractLinkLoadStrategy) o;

        if ((domain == null) != (lls.domain == null)) return false;
        if (domain != null && (domain.getASID() != lls.domain.getASID())) return false;
        if ((tm == null) != (lls.tm == null)) return false;
        if (tm != null) {
            if (tm.isSetTmId() != lls.tm.isSetTmId()) return false;
            try {
                if (tm.getTmId() != lls.tm.getTmId()) return false;
            } catch (TrafficMatrixIdException e) {
                //Both get throw exception
            }
        }
        if (isECMP() != lls.isECMP()) return false;
        return spf.equals(lls.spf);
    }

    public int hashCode() {
        int result;
        result = (domain != null ? domain.getASID() : 0);
        try {
            result = 29 * result + (tm != null && tm.isSetTmId() ? tm.getTmId() : 0);
        } catch (TrafficMatrixIdException e) {
            //never happen
        }
        result = 29 * result + (spf != null ? spf.hashCode() : 0);
        return result;
    }

}
