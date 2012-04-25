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

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.ECMPAnalysisImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

import java.util.List;

/*
 * Changes:
 * --------
 *
 * - 19-Sep-2005: add a check to see if there is a default domain (JLE).
 * - 21-Jun-2007: Use algo parameter if present (GMO)
 */

/**
 * This class implements an ECMP analysis event.
 *
 * <p>Creation date: 15-sept.-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class ECMPAnalysis extends ECMPAnalysisImpl implements Event {

    private static final Logger logger = Logger.getLogger(ECMPAnalysis.class);
    
    public ECMPAnalysis() {}
    
    public ECMPAnalysis(int asId) {
        setASID(asId);
    }

    public EventResult action() throws EventExecutionException {
        logger.debug("Processing an ECMP analysis event. ASID: "+getASID());

        Domain domain;
        if(!isSetASID()) {
            domain = InterDomainManager.getInstance().getDefaultDomain();
            if(domain == null) {
                logger.error("There is no default domain!");
                throw new EventExecutionException("No default domain.");
            }
        } else {
            try {
                domain = InterDomainManager.getInstance().getDomain(getASID());
            } catch (InvalidDomainException e) {
                logger.error("Unknown domain "+getASID());
                throw new EventExecutionException(e);
            }
        }
        
        System.out.println("Running ECMP Analysis on ASID "+domain.getASID());

        List<List<Path>> paths = null;
        try {
            if (isSetAlgo()) {
                paths = domain.getValidator().getEqualCostMultiPath(getAlgo());
            } else {
                paths = domain.getValidator().getEqualCostMultiPath();
            }
        } catch (NoRouteToHostException e) {
            logger.error("NoRouteToHostException in ECMPAnalysis. Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (RoutingException e) {
            logger.error("RoutingException in ECMPAnalysis. Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (NoSuchAlgorithmException e) {
            logger.error("NoSuchAlgorithmException in ECMPAnalysis. Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        }

        StringBuilder sb = new StringBuilder();
        for (List<Path> l : paths) {
            for (Path p : l) {
                sb.append(p.toString());
                sb.append("\n");
            }
            sb.append("\n");
        }
        return new EventResult(paths, sb.toString());
    }

}
