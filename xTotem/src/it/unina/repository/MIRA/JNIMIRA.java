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

package it.unina.repository.MIRA;

import org.apache.log4j.Logger;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AddDBException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;

/*
 * Changes:
 * --------
 * 05-Mar.-2007: Suppress library loading (GMO)
 */

/**
 * This class implements the JNI interface for MIRA (Unina).
 * <p/>
 * <p>Creation date : 6 juin 2005 12:26:54
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class JNIMIRA {
    private static Logger logger = Logger.getLogger(JNIMIRA.class);

    // Print the MIRA DB in the C code
    public native static void jniprintMiraDB();

    /**
     * Initializes MIRA
     * High Level configuration, database filling,...
     * @param miraVersion : specify if we want to use s-MIRA (type = 0) or new-MIRA (type = 1)
     */
    public native static void jniinitMira(int miraVersion);

    /**
     * Kills MIRA
     * Unload MIRA from memory
     */
    public native static void jnikillMira();

    /**
     * Adds Node in MIRA database
     * @param nodeId
     * @param type : specify if the node is an edge node (1) or not (0)
     */
    public native static void jniaddNode(int nodeId, int type) throws AddDBException;

    /**
     * Adds Link in MIRA database
     * @param srcId
     * @param dstId
     * @param cap capacity of the link per CT or OA
     */
    public native static void jniaddLink(int srcId, int dstId, float cap, float reservedBw, float metric) throws AddDBException;

    /**
     * Adds an already computed primary LSP to MIRA database
     * @param path the path as a list of NODE ids
     * @param reservation requested bandwidth
     */
    public native static void  jniaddPath(int[] path, float reservation) throws AddDBException;

    /**
     * Remove an already computed primary LSP to MIRA database
     * @param path the path as a list of NODE ids
     * @param reservation requested bandwidth
     */
    public native static void  jniremovePath(int[] path, float reservation) throws AddDBException;

    /**
     * Removes the node identified by nodeId from MIRA database
     * @param nodeId
     * @throws AddDBException
     */
    public native static void jniremoveNode(int nodeId) throws AddDBException;


    /**
     * Removes the link identified by srcNodeId and dstNodeId from MIRA database
     * @param srcId
     * @param dstId
     * @throws AddDBException
     */
    public native static void jniremoveLink(int srcId, int dstId) throws AddDBException;


    /**
     * Computes a primary LSP with MIRA
     * @param src
     * @param dst
     * @param bandwidth requested bandwidth
     * @param ADDLSP add the LSP to the database
     * @return
     */
    public native static int[] jnicomputePath(int src, int dst, float bandwidth, int ADDLSP) throws AddDBException, NoRouteToHostException, RoutingException;

}
