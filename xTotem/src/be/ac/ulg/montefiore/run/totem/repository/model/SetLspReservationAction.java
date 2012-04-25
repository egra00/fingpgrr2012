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

import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkCapacityExceededException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.TotemActionExecutionException;

/*
 * Changes:
 * --------
 *
 */

/**
 * This action can be used to set the reservation of an LSP.
 *
 * <p>Creation date: 18-mai-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class SetLspReservationAction implements TotemAction {

    private Lsp lsp;
    private float reservation;

    /**
     * Creates a new <code>SetLspReservationAction</code> action.
     * @param lsp The LSP whose you want to set the reservation.
     * @param reservation The new reservation of the LSP.
     */
    public SetLspReservationAction(Lsp lsp, float reservation) {
        this.lsp = lsp;
        this.reservation = reservation;
    }

    /**
     * Sets the reservation of the LSP.
     */
    public void execute() throws TotemActionExecutionException {
        try {
            lsp.setReservation(reservation);
        } catch (LinkCapacityExceededException e) {
            e.printStackTrace();
            throw new TotemActionExecutionException(e);
        }
    }

    /**
     * Returns the LSP whose you want to set the reservation.
     * @return The LSP whose you want to set the reservation.
     */
    public Lsp getLsp() {
        return lsp;
    }
    
    /**
     * Returns the new reservation.
     * @return The new reservation.
     */
    public float getReservation() {
        return reservation;
    }
}
