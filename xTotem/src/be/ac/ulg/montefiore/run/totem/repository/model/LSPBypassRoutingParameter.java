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

import java.util.ArrayList;
import java.util.List;

/*
 * Changes:
 * --------
 * - 29-Nov-2007: add class type setup and holding preemption levels. (GMO)
 * - 17-Nov-2007: add dstNode variable (GMO)
 */

/**
 * This class specifies the information needed to compute a bypass backup.
 * It basically contains general LSP information (from {@link LSPRoutingParameter}), diffserv information
 * (setup and holding levels and classtype), a list of protected linksand a destination node id.
 * For fast re-routable LSPs, only one protected link should be set. For NHOP lsps, the dstNode is optional and should
 * be the protected link destination node. For NNHOP, it is mandatory and should be the next next hop. 
 *
 * <p>Creation date: 25-May-2005
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public final class LSPBypassRoutingParameter extends LSPRoutingParameter {

    private int setup, holding, classType;
    private boolean isSetSetup = false, isSetHolding = false, isSetClassType = false;

    private String dstNode = null;

    private ArrayList<String> links;
    
    /**
     * Initialises a newly created <code>LSPBypassRoutingParameter</code> object.
     */
    public LSPBypassRoutingParameter(String lspId, String dstNode) {
        super(lspId);
        this.dstNode = dstNode;
        links = new ArrayList<String>();
    }

    /**
     * Assume that the destination of the LSP is the destination node of the last protected link.
     * @param lspId
     */
    public LSPBypassRoutingParameter(String lspId) {
        super(lspId);
        links = new ArrayList<String>();
    }

    /**
     * Adds the link <code>link</code> to the list of the links protected by the LSP to create.
     * @param link The ID of the link to add.
     */
    public void addProtectedLink(String link) {
        links.add(link);
    }
    
    /**
     * Returns the list of the links to protect.
     */
    public List<String> getProtectedLink() {
        return (List<String>) links.clone();
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

    public String getDstNode() {
        return dstNode;
    }

    public void setDstNode(String dstNode) {
        this.dstNode = dstNode;
    }

    public boolean isSetDstNode() {
        return this.dstNode != null;
    }
}
