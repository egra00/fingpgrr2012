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
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.StatusTypeException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Topology;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.NodeDownImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

import javax.xml.bind.JAXBException;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class implements a node down event.
 *
 * <p>Creation date: 01-d�c.-2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class NodeDown extends NodeDownImpl implements Event {

    private final static Logger logger = Logger.getLogger(NodeDown.class);
    
    public NodeDown() {}
    
    public NodeDown(String nodeId) {
        setNodeId(nodeId);
    }
    
    public NodeDown(int asId, String nodeId) {
        this(nodeId);
        setASID(asId);
    }
    
    public NodeDown(int asId, String nodeId, String cause) {
        this(asId, nodeId);
        setCause(cause);
    }
    
    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a node down event - ASID: "+_ASID+" - Node ID: "+_NodeId+" - Cause: "+_Cause);
        
        try {
            Domain domain;
            if(isSetASID()) {
                domain = InterDomainManager.getInstance().getDomain(_ASID);
            }
            else {
                domain = InterDomainManager.getInstance().getDefaultDomain();
            }
            Node node = domain.getNode(_NodeId);
            node.setNodeStatus(Node.STATUS_DOWN);

            logger.info("Node " + node.getId() + " status set to down.");
            return new EventResult(node);
        }
        catch(StatusTypeException e) {
            logger.error("Weird StatusTypeException !");
            throw new EventExecutionException(e);
        }
        catch(NodeNotFoundException e) {
            logger.error("Unknown node "+_NodeId);
            throw new EventExecutionException(e);
        }
        catch(InvalidDomainException e) {
            logger.error("Unknown domain "+_ASID);
            throw new EventExecutionException(e);
        }
    }

}
