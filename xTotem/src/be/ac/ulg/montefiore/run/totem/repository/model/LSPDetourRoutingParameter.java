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

import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 * 22-Aug-2006: add ALGORITHM_DEFAULT constant (GMO).
 */

/**
 * This class specifies the information needed to compute a detour backup.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public final class LSPDetourRoutingParameter extends LSPRoutingParameter {

    private static final Logger logger = Logger.getLogger(LSPDetourRoutingParameter.class);
    
    /**
     * Specifies a local method type.
     */
    public static final int LOCAL = 0;
    
    /**
     * Specifies a global method type.
     */
    public static final int GLOBAL = 1;
    
    /**
     * Specifies a node disjoint protection type.
     */
    public static final int NODE_DISJOINT = 0;
    
    /**
     * Specifies a link disjoint protection type.
     */
    public static final int LINK_DISJOINT = 1;

    /**
     * Default method type or protection type.
     */
    public static final int ALGORITHM_DEFAULT = -1;


    private String protectedLSP;
    private int methodType = ALGORITHM_DEFAULT;
    private int protectionType = ALGORITHM_DEFAULT;
    
    /**
     * Initialises a newly created <code>LSPDetourRoutingParameter</code> object.
     * @param lspId The ID of the LSP to create.
     */
    public LSPDetourRoutingParameter(String lspId) {
        super(lspId);
    }
    
    /**
     * @return Returns the methodType.
     */
    public int getMethodType() {
        return methodType;
    }
    
    /**
     * @param methodType The methodType to set. See the <code>LOCAL</code>, <code>GLOBAL</code> and
     * <code>ALGORITHM_DEFAULT</code> constants.
     */
    public void setMethodType(int methodType) {
        switch(methodType) {
        case LOCAL:
        case GLOBAL:
        case ALGORITHM_DEFAULT:
            this.methodType = methodType;
            break;
        default:
            logger.error("Unknown methodType "+methodType);
        }
    }
    
    /**
     * Sets the method type.
     * @param methodType Allowed values are <code>"LOCAL"</code>, <code>"GLOBAL"</code> and <code>"ALGORITHM_DEFAULT"</code>.
     */
    public void setMethodType(String methodType) {
        if(methodType.equals("LOCAL")) {
            this.methodType = LOCAL;
        }
        else if(methodType.equals("GLOBAL")) {
            this.methodType = GLOBAL;
        }
        else if (methodType.equals("ALGORITHM_DEFAULT")) {
            this.methodType = ALGORITHM_DEFAULT;
        }
        else {
            logger.error("Unknown methodType "+methodType);
        }
    }
    
    /**
     * @return Returns the protectedLSP.
     */
    public String getProtectedLSP() {
        return protectedLSP;
    }
    
    /**
     * @param protectedLSP The protectedLSP to set.
     */
    public void setProtectedLSP(String protectedLSP) {
        this.protectedLSP = protectedLSP;
    }
    
    /**
     * @return Returns the protectionType.
     */
    public int getProtectionType() {
        return protectionType;
    }
    
    /**
     * @param protectionType The protectionType to set. See the <code>NODE_DISJOINT</code>,
     * <code>LINK_DISJOINT</code> and <code>ALGORITHM_DEFAULT</code> constants.
     */
    public void setProtectionType(int protectionType) {
        switch(protectionType) {
        case NODE_DISJOINT:
        case LINK_DISJOINT:
        case ALGORITHM_DEFAULT:
            this.protectionType = protectionType;
            break;
        default:
            logger.error("Unknown protectionType "+protectionType);
        }
    }
    
    /**
     * Sets the protection type.
     * @param protectionType Allowed values are <code>"NODE_DISJOINT"</code>, <code>"LINK_DISJOINT"</code> and <code>"ALGORITHM_DEFAULT"</code>.
     */
    public void setProtectionType(String protectionType) {
        if(protectionType.equals("NODE_DISJOINT")) {
            this.protectionType = NODE_DISJOINT;
        }
        else if(protectionType.equals("LINK_DISJOINT")) {
            this.protectionType = LINK_DISJOINT;
        }
        else if (protectionType.equals("ALGORITHM_DEFAULT")) {
            this.protectionType = ALGORITHM_DEFAULT;
        }
        else {
            logger.error("Unknown protectionType "+protectionType);
        }
    }
}
