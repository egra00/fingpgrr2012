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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;

import java.util.List;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 29-Nov-2007: executes now do nothing (GMO)
*/

/**
* Class used to trigger rerouting. It will signals listeners that a rerouting should take place.
*
* <p>Creation date: 09-mars-2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class TriggerRerouteLspsAction implements TotemAction {

    private static final Logger logger = Logger.getLogger(TriggerRerouteLspsAction.class);

    private List<Lsp> lsps;
    private Domain domain;

    public TriggerRerouteLspsAction(Domain domain, List<Lsp> lsps) {
        this.lsps = lsps;
        this.domain = domain;
    }

    /**
     * Trigger a rerouting event
     */
    public void execute() {
        //TODO: call the rerouting functionnality to recompute the path of the LSP then add it back to the topology.
        logger.warn("Rerouting not implemented.");
    }
}
