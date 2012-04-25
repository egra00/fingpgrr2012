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

import java.util.HashMap;
import java.util.List;

/*
 * Changes:
 * --------
 * - 08-Nov-2005: Add putAllRoutingAlgorithmParameter method
 * - 25-Sep-2007: getRoutingAlgorithmParameter() now returns a string (GMO)
 * - 26-Feb-2008: add isSetBandwidth(), getAccepedCos() and setAcceptedCos(.) (GMO)
 */

/**
 * This abstract class is the base class for the routing parameters classes.
 *
 * <p>See the classes implementing the routing algorithms to know the specific required parameters.
 * 
 * <p>Creation date: 01-D�c.-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public abstract class LSPRoutingParameter {

    protected float bandwidth;
    protected boolean isSetBandwidth = false;
    protected String lspId;
    protected HashMap<String, String> routingAlgorithmParams;
    protected List<String> acceptedCos = null;

    /**
     * Initialises a newly created <code>LSPRoutingParameter</code> object.
     */
    public LSPRoutingParameter(String lspId) {
        routingAlgorithmParams = new HashMap<String, String>();
        this.lspId = lspId;
    }
    
    /**
     * Sets the value of the parameter <code>name</code> to <code>value</code>.
     */
    public void putRoutingAlgorithmParameter(String name, String value) {
        routingAlgorithmParams.put(name, value);
    }

    /**
     * Sets all the parameters in <code>params</code>, overwriting existing ones.
     */
    public void putAllRoutingAlgorithmParameter(HashMap<String, String> params) {
        routingAlgorithmParams.putAll(params);
    };

    /**
     * Returns the value of the parameter <code>name</code>. If there is no parameter <code>name</code>,
     * this method returns <code>null</code>.
     */
    public String getRoutingAlgorithmParameter(String name) {
        return routingAlgorithmParams.get(name);
    }
    
    /**
     * @return Returns the bandwidth.
     */
    public float getBandwidth() {
        return bandwidth;
    }
    
    /**
     * @param bandwidth The bandwidth to set.
     */
    public void setBandwidth(float bandwidth) {
        this.bandwidth = bandwidth;
        isSetBandwidth = true;
    }

    public boolean isSetBandwidth() {
        return isSetBandwidth;
    }

    /**
     * @return Returns the lspId.
     */
    public String getLspId() {
        return lspId;
    }
    
    /**
     * @param lspId The lspId to set.
     */
    public void setLspId(String lspId) {
        this.lspId = lspId;
    }

    /**
     * Returns a list of accepted classes of services for the LSP to route
     * @return
     */
    public List<String> getAcceptedCos() {
        return acceptedCos;
    }

    /**
     * Sets the accepted classes of services for the LSP to route
     * @param acceptedCos
     */
    public void setAcceptedCos(List<String> acceptedCos) {
        this.acceptedCos = acceptedCos;
    }
}
