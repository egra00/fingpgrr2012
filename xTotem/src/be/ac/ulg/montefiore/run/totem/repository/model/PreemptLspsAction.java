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
import java.util.List;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.TotemActionExecutionException;

/*
 * Changes:
 * --------
 * 31-Mar-2006 : use List<String> instead of List (GMO).
 */

/**
 * Action to manage LSPs preempted by computation algorithm
 *
 * <p>Creation date: 20-Apr.-2004
 *
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 */
public class PreemptLspsAction implements TotemAction{

    private static Logger logger = Logger.getLogger(PreemptLspsAction.class.getName());

    private List<String> lsps;
    private Domain domain;

    public PreemptLspsAction(Domain domain, List<String> lsps) {
        this.lsps = lsps;
        this.domain = domain;
    }

    public void execute() throws TotemActionExecutionException {
        LspNotFoundException ex = null;
        if (lsps.size() != 0){
            for (String id : lsps) {
                logger.info("Lsp " + id + " chosen to be preempted will be removed ");
                try {
                    domain.removeLsp(domain.getLsp(id));
                } catch (LspNotFoundException e) {
                    logger.error("Lsp not found: " + id);
                    ex = e;
                }
            }
        }
        if (ex != null) throw new TotemActionExecutionException(ex);
    }

    public List<String> getLsps() {
        return lsps;
    }

}
