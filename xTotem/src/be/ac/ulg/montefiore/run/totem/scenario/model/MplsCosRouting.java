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
package be.ac.ulg.montefiore.run.totem.scenario.model;

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.MplsCosRoutingImpl;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.MplsCosRoutingType;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.PureMPLSCosLinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.LinkLoadComputerIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.LinkLoadComputerAlreadyExistsException;
import org.apache.log4j.Logger;

import java.util.List;

/*
* Changes:
* --------
*
*/

/**
* Starts the {@link PureMPLSCosLinkLoadComputer} link load computer. The resulting load
* can be displayed using {@link ShowLinkLoad} event.
*
* <p>Creation date: 20/02/2008
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class MplsCosRouting extends MplsCosRoutingImpl implements Event {
    private static final Logger logger = Logger.getLogger(MplsCosRouting.class);

    private Domain domain;

    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a MplsCosRouting event.");

        if(isSetASID()) {
            try {
                domain = InterDomainManager.getInstance().getDomain(getASID());
            } catch (InvalidDomainException e) {
                logger.error("Unknown domain "+getASID());
                throw new EventExecutionException(e);
            }
        } else {
            domain = InterDomainManager.getInstance().getDefaultDomain();
            if(domain == null) {
                logger.error("There is no default domain!");
                throw new EventExecutionException("No default domain.");
            }
        }

        PureMPLSCosLinkLoadComputer llc = new PureMPLSCosLinkLoadComputer(domain);

        for (MplsCosRoutingType.CosType cosType : (List<MplsCosRoutingType.CosType>)getCos()) {
            if (!domain.isExistingClassOfService(cosType.getName())) {
                throw new EventExecutionException("Class of service not found: " + cosType.getName());
            }

            try {
                TrafficMatrix tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(), cosType.getValue());
                llc.addTrafficMatrix(tm, cosType.getName());
            } catch (InvalidTrafficMatrixException e) {
                throw new EventExecutionException("Traffic matrix not found with id: " + cosType.getValue());
            }
        }

        try {
            if (isSetLlcId()) {
                LinkLoadComputerManager.getInstance().addLinkLoadComputer(llc, true, getLlcId());
            } else {
                LinkLoadComputerManager.getInstance().addLinkLoadComputer(llc);
            }
        } catch (LinkLoadComputerIdException e) {
            throw new EventExecutionException(e);
        } catch (LinkLoadComputerAlreadyExistsException e) {
            throw new EventExecutionException(e);
        }

        return new EventResult(null, "LinkLoadComputer added.");
    }
}
