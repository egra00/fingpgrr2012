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

/*
 * Changes:
 * --------
 * - 23-Oct-2006: add isSetClassType(), isSetSetup() and isSetHolding() (GMO)
 * - 26-Feb-2008: add isSetMaxRate() and isSetMetric() (GMO) 
 */

/**
 * This class specifies the information needed to compute a primary LSP.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public final class LSPPrimaryRoutingParameter extends LSPRoutingParameter {

    private String srcNode, dstNode;
    private float metric, maxRate;
    private boolean isSetMetric = false, isSetMaxRate = false;
    private int setup, holding, classType;
    private boolean isSetSetup = false, isSetHolding = false, isSetClassType = false;

    /**
     * Creates a new instance of <code>LSPPrimaryRoutingParameter</code>.
     * @param srcNode The source ID of the LSP to create.
     * @param dstNode The destination ID of the LSP to create.
     */
    public LSPPrimaryRoutingParameter(String srcNode, String dstNode, String lspId) {
        super(lspId);
        this.srcNode = srcNode;
        this.dstNode = dstNode;
    }
    
    /**
     * @return Returns the classType if set, 0 otherwise.
     */
    public int getClassType() {
        return classType;
    }
    
    /**
     * @param classType The classType to set.
     */
    public void setClassType(int classType) {
        this.classType = classType;
        isSetClassType = true;
    }

    /**
     *
     * @return true if the classType is set, false otherwise.
     */
    public boolean isSetClassType() {
        return isSetClassType;
    }

    /**
     * @return Returns the dstNode.
     */
    public String getDstNode() {
        return dstNode;
    }
    
    /**
     * @param dstNode The dstNode to set.
     */
    public void setDstNode(String dstNode) {
        this.dstNode = dstNode;
    }
    
    /**
     * @return Returns the holding preemption level if sef, 0 otherwise.
     */
    public int getHolding() {
        return holding;
    }
    
    /**
     * @param holding The holding to set.
     */
    public void setHolding(int holding) {
        this.holding = holding;
        isSetHolding = true;
    }

    /**
     *
     * @return True if the holding preemption level is set, false otherwise.
     */
    public boolean isSetHolding() {
        return isSetHolding;
    }

    /**
     * @return Returns the maxRate.
     */
    public float getMaxRate() {
        return maxRate;
    }
    
    /**
     * @param maxRate The maxRate to set.
     */
    public void setMaxRate(float maxRate) {
        this.maxRate = maxRate;
        isSetMaxRate = true;
    }

    public boolean isSetMaxRate() {
        return isSetMaxRate;
    }

    /**
     * @return Returns the metric.
     */
    public float getMetric() {
        return metric;
    }
    
    /**
     * @param metric The metric to set.
     */
    public void setMetric(float metric) {
        this.metric = metric;
        isSetMetric = true;
    }

    public boolean isSetMetric() {
        return isSetMetric;
    }

    /**
     * @return Returns the setup preemption level if set, 0 otherwise.
     */
    public int getSetup() {
        return setup;
    }
    
    /**
     * @param setup The setup to set.
     */
    public void setSetup(int setup) {
        this.setup = setup;
        isSetSetup = true;
    }

    /**
     * @return true if the setup is set
     */
    public boolean isSetSetup() {
        return isSetSetup;
    }

    /**
     * @return Returns the srcNode.
     */
    public String getSrcNode() {
        return srcNode;
    }
    
    /**
     * @param srcNode The srcNode to set.
     */
    public void setSrcNode(String srcNode) {
        this.srcNode = srcNode;
    }
}
