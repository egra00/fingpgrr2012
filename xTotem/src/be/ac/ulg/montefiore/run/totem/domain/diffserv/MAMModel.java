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
* Implements the Maximum Allocation Bandwidth Constraints Model (MAM). See RFC 4125.
*
* <p>Creation date: 7/01/2008
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class MAMModel implements DiffServModel {

    private static MAMModel instance = null;

    private MAMModel() {}

    public static MAMModel getInstance() {
        if (instance == null)
            instance = new MAMModel();
        return instance;
    }

    /**
     * Every bandwidth constraint should be inferior or equal to the maximum reservable bandwidth
     * @param domain
     * @param bc
     * @param mrbw
     * @return
     */
    public boolean checkConstraints(Domain domain, float bc[], float mrbw) {
        for (int ct : domain.getAllCTId()) {
            if (ct >= bc.length || bc[ct] > mrbw)
                return false;
        }
        return true;
    }

    /**
     *
     *  Computes the reservable bandwidth array in the following way:
     * <pre>
     * "Unreserved TE-Class [i]" =
     *    MIN  [
     *      [ BCc - SUM ( Reserved(CTc,q) ) ] for q <= p  ,
     *      [ Max-Res-Bw - SUM (Reserved(CTb,q)) ] for q <= p and 0 <= b <= 7,
     *    ]
     *
     * where:
     *     TE-Class [i] <--> < CTc , preemption p>
     *     in the configured TE-Class mapping.
     * </pre>
     * @param domain
     * @param bc
     * @param reservedBwArray
     * @param mrbw
     * @return
     */
    public float[] getReservableBandwidth(Domain domain, float bc[], float reservedBwArray[], float mrbw) {
        float[] reservableBandwidthArray = new float[reservedBwArray.length];
        Arrays.fill(reservableBandwidthArray, -1);
        for (int prio : domain.getPriorities()) {
            int classtype = domain.getClassType(prio);
            int preemptionLevel = domain.getPreemptionLevel(prio);

            reservableBandwidthArray[prio] = Math.min(
                    bc[classtype] - getReservedBandwidthCT(domain, classtype, preemptionLevel, reservedBwArray),
                    mrbw - getTotalReservedBandwidth(domain, preemptionLevel, reservedBwArray));

        }
        return reservableBandwidthArray;
    }

    /**
     * Returns the reservable bandwidth at a given priority level given the constraints and the reserved bandwidth.
     * The reservable bandwidth in computed the following way:
     * <pre>
     * "Unreserved TE-Class [i]" =
     *    MIN  [
     *      [ BCc - SUM ( Reserved(CTc,q) ) ] for q <= p  ,
     *      [ Max-Res-Bw - SUM (Reserved(CTb,q)) ] for q <= p and 0 <= b <= 7,
     *    ]
     *
     * where:
     *     TE-Class [i] <--> < CTc , preemption p>
     *     in the configured TE-Class mapping.
     * </pre>
     *
     * @param domain
     * @param priority <
     * @param bc Array of bandwidth constraints, indexed by the constraint number
     * @param reservedBwArray Array representing the reserved bandwidth at each of the priority level
     * @param mrbw Maximum reservable bandwidth
     * @return
     */
    public float getReservableBandwidth(Domain domain, int priority, float bc[], float reservedBwArray[], float mrbw) {
        int classtype = domain.getClassType(priority);
        int preemptionLevel = domain.getPreemptionLevel(priority);

        return Math.min(bc[classtype] - getReservedBandwidthCT(domain, classtype, preemptionLevel, reservedBwArray),
                    mrbw - getTotalReservedBandwidth(domain, preemptionLevel, reservedBwArray));
    }

    /**
     * Returns the reserved bandwidth in a given classype for all preemption level values inferior or equal to the given one
     * @param domain
     * @param c
     * @param p
     * @param reservedBwArray
     * @return
     */
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

    /**
     * Returns the total reservable bandwidth in all classtypes for all preemption level values inferior or equal to the given one
     * @param domain
     * @param p
     * @param reservedBwArray
     * @return
     */
    private float getTotalReservedBandwidth(Domain domain, int p, float reservedBwArray[]) {
        float reservedBw = 0;
        for (int b : domain.getAllCTId()) {
            reservedBw += getReservedBandwidthCT(domain, b, p, reservedBwArray);
        }
        return reservedBw;
    }


    public String toString() {
        return "MAM";
    }
}
