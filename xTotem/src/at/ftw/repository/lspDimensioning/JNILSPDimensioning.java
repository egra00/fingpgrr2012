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

package at.ftw.repository.lspDimensioning;

import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 * - 5-Mar-2007: remove library loading (GMO)
 */

/**
 * JNI interface of the LSPDimensioning algorithm.
 * 
 * <p>Creation date : 20 juin 2005 11:10:27
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class JNILSPDimensioning {
    private static Logger logger = Logger.getLogger(JNILSPDimensioning.class);

    /**
     * Initializes LSPDimensioning
     *
     * @param slot_time
     * @param MS_nu
     * @param w
     * @param PS_type
     * @param Delay
     * @param epsilon
     */
    public native static void jniinitLSPDimensioning(float slot_time, int MS_nu, float w, int PS_type, float Delay, float epsilon);

    /**
     * Kills LSPDimensioning
     * Unload LSPDimensioning from memory
     */
    public native static void jnikillLSPDimensioning();

    /**
     * Computes the BW assignment for next window.
     *
     * @param samples : measurements for previous window
     * @return the BW assignment for next window  
     */
    public native static float jnicomputeBWAssign(float[] samples);

}
