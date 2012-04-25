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
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkCapacityExceededException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.TotemActionExecutionException;
import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 * -23-Aug-2006 : Add logger (GMO)
 */

/**
 * This action describes an LSP and can be used to add the LSP on the domain
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class AddLspAction implements TotemAction {
    private static final Logger logger = Logger.getLogger(AddLspAction.class);

    protected Lsp lsp;
    protected Domain domain;

    /**
     * Creates an AddLspAction
     *
     * @param lsp the LSP
     */
    public AddLspAction(Domain domain, Lsp lsp) {
        this.lsp = lsp;
        this.domain = domain;
    }

    /**
     * Adds the LSP to the domain
     */
    public void execute() throws TotemActionExecutionException {
        try {
            domain.addLsp(lsp);
        } catch (LinkCapacityExceededException e) {
            logger.error(e.getClass().getSimpleName() + " : not enough bandwidth to accomodate LSP with " + lsp.getReservation() + " bw requirement.");            
            throw new TotemActionExecutionException("Impossible to add the LSP to the domain (" + e.getClass().getSimpleName() + ")");
        } catch (Exception e) {
            logger.error(e.getClass().getSimpleName() + " : " + e.getMessage());
            throw new TotemActionExecutionException("Impossible to add the LSP to the domain (" + e.getClass().getSimpleName() + ")");
        }
    }

    /**
     * Gets the LSP
     *
     * @return the LSP
     */
    public Lsp getLsp() {
        return lsp;
    }

}
