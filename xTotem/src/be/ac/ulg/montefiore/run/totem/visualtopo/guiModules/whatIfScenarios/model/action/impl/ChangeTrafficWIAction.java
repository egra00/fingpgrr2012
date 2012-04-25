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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.impl;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.ActionExecutionException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIAction;

/*
* Changes:
* --------
*
*/

/**
* Change the default traffic matrix for the given domain.
*
* <p>Creation date: 24/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ChangeTrafficWIAction implements WIAction {
    private Domain domain;
    private TrafficMatrix tm;

    public ChangeTrafficWIAction(Domain domain, TrafficMatrix newTm) {
        this.domain = domain;
        this.tm = newTm;
    }

    public void execute() throws ActionExecutionException {
        try {
            TrafficMatrixManager.getInstance().setDefaultTrafficMatrix(domain.getASID(), tm.getTmId());
        } catch (InvalidTrafficMatrixException e) {
            e.printStackTrace();
            throw new ActionExecutionException(e);
        } catch (TrafficMatrixIdException e) {
            e.printStackTrace();
            throw new ActionExecutionException(e);
        }

    }

    public String getName() {
        try {
            return "Change traffic matrix: " + tm.getTmId();
        } catch (TrafficMatrixIdException e) {
            return "Change traffic matrix: Invalid ID";
        }
    }
}
