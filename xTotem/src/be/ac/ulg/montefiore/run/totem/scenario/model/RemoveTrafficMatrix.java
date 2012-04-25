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

import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.RemoveTrafficMatrixImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class implements a remove traffic matrix event.
 *
 * <p>Creation date: 20-f�vr.-2006
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class RemoveTrafficMatrix extends RemoveTrafficMatrixImpl implements
        Event {
    
    private static final Logger logger = Logger.getLogger(RemoveTrafficMatrix.class);

    public RemoveTrafficMatrix() {}
    
    public RemoveTrafficMatrix(int tmId) {
        setTMID(tmId);
    }
    
    public RemoveTrafficMatrix(int asId, int tmId) {
        this(tmId);
        setASID(asId);
    }

    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a remove traffic event");
        int asId;
        if(!isSetASID()) {
            try {
                asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
            } catch(NullPointerException e) {
                logger.error("No ASID specified and no default domain!");
                throw new EventExecutionException(e);
            }
        } else {
            asId = getASID();
        }
        
        if(isSetTMID()) {
            try {
                TrafficMatrixManager.getInstance().removeTrafficMatrix(asId, getTMID());
            } catch(TrafficMatrixException e) {
                logger.error("No traffic matrix "+getTMID()+" for domain "+asId);
                throw new EventExecutionException(e);
            }
        } else {
            try {
                TrafficMatrixManager.getInstance().removeDefaultTrafficMatrix(asId);
            } catch(TrafficMatrixException e) {
                logger.error("No default traffic matrix for domain "+asId);
                throw new EventExecutionException(e);
            }
        }
        logger.info("Traffic matrix removed.");
        return new EventResult();
    }
}
