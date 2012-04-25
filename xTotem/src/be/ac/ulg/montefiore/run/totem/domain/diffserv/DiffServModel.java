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

/*
 * Changes:
 * --------
 * 10-Nov-2006: complete rewrite (GMO)
 * 15-Jan-2008: complete rewrite (GMO)
 */

/**
 * Generic diffserv model interface
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 * @author Gael Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public interface DiffServModel {

    /**
     * Check that the given constraints are set correctly for the given domain (right number of constraints, ...)
     * @param domain
     * @param bc Array of bandwidth constraints, indexed by the constraint number
     * @param mrbw Maximum reservable bandwidth
     * @return true if the constraints are satisfied, false otherwise
     */
    public boolean checkConstraints(Domain domain, float bc[], float mrbw);

    /**
     * Returns an array representing the reservable bandwidth at each of the priority level.
     * (The array to be distributed by the IGP protocol)
     * @param domain
     * @param bc Array of bandwidth constraints, indexed by the constraint number
     * @param reservedBwArray Array representing the reserved bandwidth at each of the priority level
     * @param mrbw Maximum reservable bandwidth
     * @return
     */
    public float[] getReservableBandwidth(Domain domain, float bc[], float reservedBwArray[], float mrbw);

    /**
     * Returns the reservable bandwidth at a given priority level given the constraints and the reserved bandwidth.
     * @param domain
     * @param priority
     * @param bc Array of bandwidth constraints, indexed by the constraint number
     * @param reservedBwArray Array representing the reserved bandwidth at each of the priority level
     * @param mrbw Maximum reservable bandwidth
     * @return
     */
    public float getReservableBandwidth(Domain domain, int priority, float bc[], float reservedBwArray[], float mrbw);
}
