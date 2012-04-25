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
package be.ac.ulg.montefiore.run.totem.domain.diffserv;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;

import java.util.Arrays;

/*
* Changes:
* --------
*
*/

/**
* Implements the Russian Dolls Bandwidth Constraints Model (See RFC 4127)
*
* <p>Creation date: 8/01/2008
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class RDMModel implements DiffServModel {

    private static RDMModel instance = null;

    private RDMModel() {}

    public static RDMModel getInstance() {
        if (instance == null)
            instance = new RDMModel();
        return instance;
    }

    /**
     * Check that
     * <ul>
     * <li>classtypes are all present from 0 to nbCT.</li>
     * <li>BC(x) <= BC(x-1), for all x: 1 <= x <= maxCT</li>
     * <li>BC0 = mrbw</li>
     * </ul>
     * @param domain
     * @param bc
     * @param mrbw
     * @return
     */
    public boolean checkConstraints(Domain domain, float bc[], float mrbw) {
        int[] cts = domain.getAllCTId();

        //if (cts.length != bc.length) return false;
        Arrays.sort(cts);

        for (int i = cts.length-1; i > 0; i--) {
            // all classtypes present
            if (cts[i] != i) return false;
            // bc(x) <= bc(x-1)
            if (bc[i] > bc[i-1]) return false;
        }

        if (bc[0] != mrbw) return false;

        return true;
    }

    /**
     *
     *  Computes the reservable bandwidth array in the following way:
     * <pre>
     * "Unreserved TE-Class [i]" =
     *   MIN  [
     *     [ BCc - SUM ( Reserved(CTb,q) ) ] for q <= p and c <= b <= 7,
     *     [ BC(c-1) - SUM ( Reserved(CTb,q) ) ] for q <= p and (c-1)<= b <= 7,
     *      . . .
     *     [ BC0 - SUM ( Reserved(CTb,q) ) ] for q <= p and 0 <= b <= 7,
     *   ]
     *
     * where:
     *
     *   TE-Class [i] <--> < CTc , preemption p>
     *   in the configured TE-Class mapping.
     * </pre>
     *
     * @param domain
     * @param bc
     * @param reservedBwArray
     * @param mrbw
     * @return
     */
    public float[] getReservableBandwidth(Domain domain, float bc[], float reservedBwArray[], float mrbw) {
        float reservableBandwidth[] = new float[DiffServConstant.MAX_NB_PRIORITY];
        Arrays.fill(reservableBandwidth, -1);

        for (int prio : domain.getPriorities()) {
            reservableBandwidth[prio] = getReservableBandwidth(domain, prio, bc, reservedBwArray, mrbw);
        }

        return reservableBandwidth;
    }

    /**
     *  Computes the reservable bandwidth in the following way:
     * <pre>
     * "Unreserved TE-Class [i]" =
     *   MIN  [
     *     [ BCc - SUM ( Reserved(CTb,q) ) ] for q <= p and c <= b <= 7,
     *     [ BC(c-1) - SUM ( Reserved(CTb,q) ) ] for q <= p and (c-1)<= b <= 7,
     *      . . .
     *     [ BC0 - SUM ( Reserved(CTb,q) ) ] for q <= p and 0 <= b <= 7,
     *   ]
     *
     * where:
     *
     *   TE-Class [i] <--> < CTc , preemption p>
     *   in the configured TE-Class mapping.
     * </pre>
     *
     * @param domain
     * @param priority
     * @param bc
     * @param reservedBwArray
     * @param mrbw
     * @return
     */
    public float getReservableBandwidth(Domain domain, int priority, float bc[], float reservedBwArray[], float mrbw) {
        int c = domain.getClassType(priority);
        int p = domain.getPreemptionLevel(priority);

        float sum = 0;
        float min = Float.MAX_VALUE;
        for (int b = c+1; b <= domain.getMaxCTvalue(); b++) {
            sum += getReservedBandwidthCT(domain, b, p, reservedBwArray);
        }
        for (int b = c; b >= 0 ; b--) {
            sum += getReservedBandwidthCT(domain, b, p, reservedBwArray);
            if (bc[b] - sum < min) min = bc[b] - sum;
        }

        return min;
    }

    private float getReservedBandwidthCT(Domain domain, int c, int p, float reservedBwArray[]) {
        float reservedBw = 0;
        for (int q : domain.getPreemptionLevels(c)) {
            if (q <= p) {
                int prio = domain.getPriority(q, c);
                reservedBw += reservedBwArray[prio];
            }
        }
        return reservedBw;
    }

    public String toString() {
        return "RDM";
    }
}
