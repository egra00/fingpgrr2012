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

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.SPF;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.StrategyType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.IGPRoutingImpl;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.LinkLoadComputerIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.LinkLoadComputerAlreadyExistsException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.*;
import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
 * This class implements an event that starts a computation with a {@link AbstractLinkLoadStrategy}. The resulting load
 * can be displayed using {@link ShowLinkLoad} event.
 * <p/>
 * <p>Creation date: 21/02/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class IGPRouting extends IGPRoutingImpl implements Event {
    private static final Logger logger = Logger.getLogger(IGPRouting.class);

    public IGPRouting() {
    }

    public IGPRouting(int asId, String llcId) {
        setASID(asId);
        setLlcId(llcId);
    }

    public EventResult action() throws EventExecutionException {
        logger.debug("Processing an IGPRouting event");

        Domain domain;
        if (isSetASID()) {
            try {
                domain = InterDomainManager.getInstance().getDomain(getASID());
            } catch (InvalidDomainException e) {
                logger.error("Unknown domain " + getASID());
                throw new EventExecutionException(e);
            }
        } else {
            domain = InterDomainManager.getInstance().getDefaultDomain();
            if (domain == null) {
                logger.error("There is no default domain!");
                throw new EventExecutionException("No default domain.");
            }
        }

        TrafficMatrix trafficMatrix = null;
        if (this.isSetTMID()) {
            try {
                trafficMatrix = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(), _TMID);
            } catch (InvalidTrafficMatrixException e) {
                e.printStackTrace();
                throw new EventExecutionException(e);
            }
        } else {
            try {
                trafficMatrix = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(domain.getASID());
            } catch (InvalidTrafficMatrixException e) {
                e.printStackTrace();
                throw new EventExecutionException(e);
            }
        }

        LinkLoadStrategy lls = null;
        if (!isSetStrategy() || getStrategy().equals(StrategyType.IP)) {
            lls = new SPFLinkLoadStrategy(domain, trafficMatrix);
        } else if (getStrategy().equals(StrategyType.BIS)) {
            lls = new BasicIGPShortcutStrategy(domain, trafficMatrix);
        } else if (getStrategy().equals(StrategyType.OVERLAY)) {
            lls = new OverlayStrategy(domain, trafficMatrix);
        } else if (getStrategy().equals(StrategyType.IS)) {
            lls = new IGPShortcutStrategy(domain, trafficMatrix);
        }

        boolean ECMP = isSetECMP() ? isECMP() : false;
        lls.setECMP(ECMP);
        if (ECMP) {
            logger.debug("ECMP Used");
        }

        if (this.isSetSPFtype()) {
            SPF spf = null;
            try {
                spf = (SPF) RepositoryManager.getInstance().getAlgo(this.getSPFtype());
                lls.setSPFAlgo(spf);
            } catch (NoSuchAlgorithmException e) {
                logger.error("Algorithm specified in SPFType for IGPRouting event not found!");
                logger.error("Using default SPF instead");
            } catch (ClassCastException e) {
                logger.error("The specified algorithm isn't a SPF algorithm!");
                logger.error("Using default SPF instead");
            }
        }

        String id;
        try {
            if (isSetLlcId()) {
                LinkLoadComputerManager.getInstance().addLinkLoadComputer(lls, true, getLlcId());
                id = getLlcId();
            } else {
                id = LinkLoadComputerManager.getInstance().addLinkLoadComputer(lls);
            }
        } catch (LinkLoadComputerIdException e) {
            throw new EventExecutionException(e);
        } catch (LinkLoadComputerAlreadyExistsException e) {
            throw new EventExecutionException(e);
        }

        return new EventResult(null, "LinkLoadComputer added. Use ShowLinkLoad event with LLCId " + id);
    }
}
