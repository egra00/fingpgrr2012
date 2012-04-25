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
package be.ac.ulg.montefiore.run.totem.repository.DAMOTE;

import be.ac.ulg.montefiore.run.totem.repository.model.exception.AddDBException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;

/*
 * Changes:
 * --------
 * 21-Nov-2005 : Made non static. (Also change the library). (GMO)
 * 22-Aug-2006 : Add jniaddBackupLSP method (Also change the library) (GMO)
 * 05-Mar-2007 : Suppress library loading (GMO)
 */

/**
 * This class implements the JNI interface for DAMOTE (ULg).
 * DAMOTE is a Decentralized Agent for Mpls Online Traffic Engineering.
 *
 * <p>Creation date: 02-Feb.-2005
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 */
public class JNIDAMOTE {

    //specific usage of a global variable which will be accessed from C code to return information
    public int[] lsps = null;

    // instance identifier, set by C code after call to jniinitDamote
    public int sessId = -1;

    // Print the DAMOTE DB in the C code
    public native void jniprintDamoteDB();

    /**
     * Initializes DAMOTE
     * High Level configuration, database filling,...
     * DAMOTE code is more commented (see documentation of DAMOTE itself for details)
     * @param nbOA the number of OAs or CTs(not taken into account in C code (FIX needed)
     * @param nbPL the number of PLs - preemption or priority levels - (not taken into account in C code (FIX needed)
     * @param colorClause link colors
     * @param allowReroute allow rerouting of less prioritary LSPs when computing?
     * @param capacityClause take into account the max capacity of the link
     * @param loadbal loadbalancing score function contribution
     * @param load minhop score function
     * @param sqLoad
     * @param relativeLoad
     * @param sqRelativeLoad traffic minimization score function contribution
     * @param delay
     * @param reroutingScorecoef  score function configuration
     */
    public native void jniinitDamote(int nbOA, int nbPL, boolean colorClause, boolean allowReroute, boolean capacityClause, float loadbal, float load, float sqLoad, float relativeLoad, float sqRelativeLoad, float delay, float fortz, float[][] reroutingScorecoef);

    /**
     * Kills DAMOTE
     * Unload DAMOTE from memory
     */
    public native void jnikillDamote();

    /**
     * Adds Node in DAMOTE database
     * @param nodeId
     */
    public native void jniaddNode(int nodeId) throws AddDBException;

    /**
     * Adds Link in DAMOTE database
     * @param linkId
     * @param color
     * @param src
     * @param dst
     * @param cap capacity of the link per CT or OA
     * @param rbw bandwidth caracteristics of the link
     * @param pbw bandwidth caracteristics of the link
     * @param bbw bandwidth caracteristics of the link
     * @param fbw bandwidth caracteristics of the link
     */
    public native void jniaddLink(int linkId, int color, int src, int dst, float[] cap, float[][] rbw, float[][] pbw, float[][] bbw, float[][] fbw) throws AddDBException; // remains to solve: list lsps on link, need a corresponding table

    /**
     * Adds an already computed primary LSP to DAMOTE database
     * Note that the least priority and the default OA will be chosen.
     * (No preemption support)
     * @param lspid the LSP id of the LSP
     * @param path the path as a list of NODE ids
     * @param PL
     * @param OA
     * @param reservation requested bandwidth
     */
    public native void  jniaddLSP(int lspid, int[] path, int PL, int OA, float reservation) throws AddDBException;

    /**
     * Adds an already computed backup LSP to DAMOTE database
     * @param lspid the LSP id of the LSP
     * @param primaryId the LSP id of the protected LSP
     * @param path the path as a list of NODE ids
     * @param PL
     * @param OA
     * @param reservation requested bandwidth
     * @param type false for global backup, true for local backup
     * @throws AddDBException
     */
    public native void jniaddBackupLSP(int lspid, int primaryId, int[] path, int PL, int OA, float reservation, boolean type) throws AddDBException;

    /**
     * Remove the LSP identified by lspid from DAMOTE database
     * @param lspid
     */
    public native void jniremoveLSP(int lspid) throws AddDBException;

    /**
     * Removes the node identified by nodeId from DAMOTE database
     * @param nodeId
     * @throws AddDBException
     */
    public native void jniremoveNode(int nodeId) throws AddDBException;


    /**
     * Removes the link identified by srcNodeId and dstNodeId from DAMOTE database
     * @param srcNodeId
     * @param dstNodeId
     * @throws AddDBException
     */
    public native void jniremoveLink(int srcNodeId, int dstNodeId) throws AddDBException;


    /**
     * Computes a primary LSP with DAMOTE
     * @param lspId
     * @param src
     * @param dst
     * @param rrid used when computing a rerouting of another LSP
     * @param rrsrc used when computing a rerouting of another LSP
     * @param rrdst used when computing a rerouting of another LSP
     * @param PL
     * @param OA
     * @param reservation requested bandwidth
     * @param colorArray lists of link colors that should not be used
     * @param ADDLSP add the LSP to the database
     * @param PREEMPT allow preemption
     * @return
     */
    public native int[] jnicomputePath(int lspId, int src, int dst, int rrid, int rrsrc, int rrdst, int PL, int OA, float reservation, int[] colorArray, boolean ADDLSP, boolean PREEMPT) throws AddDBException, NoRouteToHostException, RoutingException; // primary path only, only using DAMOTE in DS-TE mode, probably using

    /**
     * Computes a global detour or local detours with DAMOTE
     * @param primarylspId
     * @param backupType
     * @param ADDLSP
     * @param PREEMPT
     * @return
     */
    public native Object[] jnicomputeBackupPath(int primarylspId, int[] backuplspIds, int backupType, boolean ADDLSP, boolean PREEMPT) throws AddDBException, NoRouteToHostException;

}
