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
package be.ac.ulg.montefiore.run.totem.domain.model.impl;

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.NodeType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.StatusType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.exception.*;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/*
 * Changes:
 * --------
 *
 * - 01-Feb-2006: extends DomainElement (JLE).
 * - 07-Feb-2006: add getNodeInterface(String) method (JLE).
 * - 07-Feb-2006: some corrections in getAllIPs (JLE).
 * - 08-Feb-2006: bugfix in getNodeInterface and add getNodeInterfaces (JLE).
 * - 06-Mar-2006: add getRouterId and setRouterId (JLE).
 * - 28-Mar-2006: setNodeStatus now advertises the domain observer (JLE).
 * - 30-Jun-2006: add notification when location (node latitude or longitude) change (GMO).
 * - 16-Jan-2007: add incoming and outgoing link lists + rewrite access methods + add getAllOutLink, getAllInLink methods (GMO).
 * - 08-Mar-2007: change constants in enum for Node type, setNodeType(.) doesn't throw exception anymore (GMO)
 * - 08-Mar-2007: change getNodeType behaviour, default node type is EDGE node (GMO)
 * - 25-Apr-2007: getInLink, getOutLink, getAllInLink, getAllOutLink don't throw an exception anymore (GMO)
 * - 25-Apr-2007: do not set node status when calling getNodeStatus (GMO)
 * - 25-Apr-2007: Reroute LSPs when status is set to down (GMO)
 * - 11-May-2007: Setting the status of the node will set lsps down rather than removing them (GMO)
 * - 14-Jun-2007: getNodeType() doesn't throw an exception anymore (GMO)
 * - 25-Sep-2007: setting link status now signals to the lsp using appropriate method (GMO)
 * - 18-Oct-2007: add setNodeStatus(String) method (GMO)
 */

/**
 * Represent a Node in a Domain
 *
 * <p>Creation date: 19-Jan-2005 15:46:33
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class NodeImpl extends be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.NodeImpl implements Node {

    private static final Logger logger = Logger.getLogger(NodeImpl.class);
    
    private Domain domain;

    private ArrayList<Link> inLinks;
    private ArrayList<Link> outLinks;

    public NodeImpl() { }

    /**
     * Create a node
     *
     * @param domain
     */
    public NodeImpl(Domain domain,String nodeId) {
        this.domain = domain;
        this.setId(nodeId);
        inLinks = new ArrayList<Link>();
        outLinks = new ArrayList<Link>();
    }

    public void init(Domain domain) {
        setDomain(domain);
        inLinks = new ArrayList<Link>();
        outLinks = new ArrayList<Link>();
    }

    /**
     * Set the domain
     * 
     * @param domain
     */
    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    /**
     * Initialisation method: Add a link to the list of incoming links.
     * @param l
     */
    public void addInLink(Link l) {
        if (!inLinks.contains(l)) {
            inLinks.add(l);
        } else {
            logger.debug("Trying to add \"IN\" link that is already present on node " + getId() + ".");
        }
    }

    /**
     * Initialisation method: Add a link to the list of outcoming links.
     * @param l
     */
    public void addOutLink(Link l) {
        if (!outLinks.contains(l)) {
            outLinks.add(l);
        } else {
            logger.debug("Trying to add \"OUT\" link that is already present on node " + getId() + ".");
        }
    }

    /**
     * Initialisation method: Remove a link from the list of incoming links.
     * @param l
     */
    public void delInLink(Link l) {
        if (!inLinks.remove(l)) {
            logger.debug("Trying to delete \"IN\" link that is not present on node " + getId() + ".");
        }
    }

    /**
     * Initialisation method: Remove a link from the list of outcoming links.
     * @param l
     */
    public void delOutLink(Link l) {
        if (!outLinks.remove(l)) {
            logger.debug("Trying to delete \"OUT\" link that is not present on node " + getId() + ".");
        }
    }

    /**
     * Gets the type of the node.
     * @return A member of the enum <code>Node.Type</code>. <code>Type.EDGE</code> if not set.
     */
    public Type getNodeType() {
        if(this.getType() == null) {
            return Type.EDGE;
        }
        if(this.getType().equals(NodeType.CORE)) {
            return Type.CORE;
        }
        if(this.getType().equals(NodeType.EDGE)) {
            return Type.EDGE;
        }
        if(this.getType().equals(NodeType.NEIGH)) {
            return Type.NEIGH;
        }
        if(this.getType().equals(NodeType.VIRTUAL)) {
            return Type.VIRTUAL;
        }
        logger.error("Node type unknown. Assuming EDGE.");
        return Type.EDGE;
    }

    /**
     * Sets the type of the node.
     * @param type
     */
    public void setNodeType(Type type) {
        switch (type) {
            case CORE :
                this.setType(NodeType.CORE);
                break;
            case EDGE:
                this.setType(NodeType.EDGE);
                break;
            case NEIGH:
                this.setType(NodeType.NEIGH);
                break;
            case VIRTUAL:
                this.setType(NodeType.VIRTUAL);
                break;
        }
    }

    /**
     * Get the status of the node
     *
     * @return Node.STATUS_UP if the node is UP and Node.STATUS_DOWN otherwise
     */
    public int getNodeStatus() {
        if (this.getStatus() != null && this.getStatus().getValue().equals("DOWN")) {
            return Node.STATUS_DOWN;
        }
        return Node.STATUS_UP;
    }

    /**
     * Set the status of the node
     *
     * @param status Node.STATUS_DOWN or Node.STATUS_UP
     * @throws StatusTypeException if status is neither Node.STATUS_DOWN nor Node.STATUS_UP
     */
    public void setNodeStatus(int status) throws StatusTypeException {
        logger.info("Node " + getId() + " set status " + ((status == STATUS_DOWN) ? "DOWN" : "UP"));
        int oldStatus = this.getNodeStatus();
        if (status == Node.STATUS_DOWN) {
            this.setStatus(StatusType.DOWN);
            if (oldStatus != status) {

                Set<Lsp> lsps = new HashSet<Lsp>();
                for (Link l : getAllInLink())
                    lsps.addAll(domain.getLspsOnLink(l));
                for (Link l : getAllOutLink())
                    lsps.addAll(domain.getLspsOnLink(l));
                for (Lsp lsp : lsps) {
                    lsp.nodeDownEvent(this);
                }

                domain.getObserver().notifyNodeStatusChange(this);
            }
        }
        else if (status == Node.STATUS_UP) {
            this.setStatus(StatusType.UP);
            if(oldStatus != status) {

                Set<Lsp> lsps = new HashSet<Lsp>();
                for (Link l : getAllInLink())
                    lsps.addAll(domain.getLspsOnLink(l));
                for (Link l : getAllOutLink())
                    lsps.addAll(domain.getLspsOnLink(l));
                for (Lsp lsp : lsps) {
                    lsp.nodeUpEvent(this);
                }

                domain.getObserver().notifyNodeStatusChange(this);
            }
        } else {
            throw new StatusTypeException(new StringBuffer().append("Status ").append(status).append(" not allowed").toString());
        }
    }

    public void setNodeStatus(String status) throws StatusTypeException {
        if (status.equalsIgnoreCase("up"))
            setNodeStatus(Node.STATUS_UP);
        else if (status.equalsIgnoreCase("down"))
            setNodeStatus(Node.STATUS_DOWN);
        else throw new StatusTypeException("Status: " + status + " unknown.");
    }

    /**
     * Return the list of the links that begins at this node and for which status is UP.
     *
     * @return the list of the links that arrives at this node
     */
    public List<Link> getOutLink() {
        ArrayList<Link> upOutLinks = new ArrayList<Link>(outLinks.size());
        for (Link l : outLinks) {
            if (l.getLinkStatus() == Link.STATUS_UP) {
                upOutLinks.add(l);
            }
        }
        return upOutLinks;
    }

    /**
     * Return the list of the links that ends at this node and for which status is UP.
     *
     * @return the list of the links that arrives at this node
     */
    public List<Link> getInLink() {
        ArrayList<Link> upInLinks = new ArrayList<Link>(inLinks.size());
        for (Link l : inLinks) {
            if (l.getLinkStatus() == Link.STATUS_UP) {
                upInLinks.add(l);
            }
        }
        return upInLinks;
    }

    /**
     * Return the list of the links that begins at this node.
     *
     * @return the list of the links that arrives at this node
     */
    public List<Link> getAllOutLink() {
        return (ArrayList<Link>)outLinks.clone();
    }

    /**
     * Return the list of the links that ends at this node.
     *
     * @return the list of the links that arrives at this node
     */
    public List<Link> getAllInLink() {
        return (ArrayList<Link>)inLinks.clone();
    }

    /**
     * Get the longitude of a node and 0 if not defined
     *
     * @return
     */
    public float getLongitude() {
        if (this.getLocation() != null) {
            return this.getLocation().getLongitude();
        }
        return 0;
    }

    /**
     * Get the latitude of a node and 0 if not defined
     *
     * @return
     */
    public float getLatitude() {
        if (this.getLocation() != null) {
            return this.getLocation().getLatitude();
        }
        return 0;
    }

    /**
     * Set the longitude of a node
     *
     * @param longitude
     */
    public void setLongitude(float longitude) {
        if (this.getLocation() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                this.setLocation(factory.createNodeLocationType());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        this.getLocation().setLongitude(longitude);
        domain.getObserver().notifyNodeLocationChange(this);
    }

    /**
     * Set the latitude of a node
     *
     * @param latitude
     */
    public void setLatitude(float latitude) {
        if (this.getLocation() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                this.setLocation(factory.createNodeLocationType());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        this.getLocation().setLatitude(latitude);
        domain.getObserver().notifyNodeLocationChange(this);
    }

    public boolean equals(Object o) {
        if(!(o instanceof NodeImpl)) {
        	return false;
        }
        
        NodeImpl node = (NodeImpl) o;
        return (node.domain.getASID() == this.domain.getASID()) && (this.getId().equals(node.getId()));
    }

    public int hashCode() {
        return domain.getASID() + this.getId().hashCode();
    }
    
    /**
     * Returns the BGP router associated with this node. If the node
     * does not support BGP, returns null.
     */
    public BgpRouter getBgpRouter()
    {
        return domain.getBgpRouter(this.getId());
    }

    public List<String> getallIPs() {
        @SuppressWarnings("unchecked")
        List<NodeInterface> interfaces = this.getInterfaces().getInterface();
        List<String> ipList = new ArrayList<String>();
        for(NodeInterface nodeInterface : interfaces) {
            try {
                ipList.add(nodeInterface.getIP());
            } catch(NotInitialisedException e) {
                // nothing to do, just skip this interface
            }
        }
        return ipList;
    }
    
    public NodeInterface getNodeInterface(String nodeInterfaceId) throws NodeInterfaceNotFoundException {
        if(this.getInterfaces() == null) {
            throw new NodeInterfaceNotFoundException("There is no defined interface.");
        }
        @SuppressWarnings("unchecked")
        List<NodeInterface> interfaces = this.getInterfaces().getInterface();
        for(NodeInterface nodeInterface : interfaces) {
            if(nodeInterface.getId().equals(nodeInterfaceId)) {
                return nodeInterface;
            }
        }
        throw new NodeInterfaceNotFoundException("The node "+getId()+" does not have an interface "+nodeInterfaceId+".");
    }
    
    public NodeInterface getNodeInterfaceByIP(String nodeInterfaceIP) throws NodeInterfaceNotFoundException {
        if(this.getInterfaces() == null) {
            throw new NodeInterfaceNotFoundException("There is no defined interface.");
        }
        @SuppressWarnings("unchecked")
        List<NodeInterface> interfaces = this.getInterfaces().getInterface();
        for(NodeInterface nodeInterface : interfaces) {
            try {
                if(nodeInterface.getIP().equals(nodeInterfaceIP)) {
                    return nodeInterface;
                }
            } catch(NotInitialisedException e) {
                // We do NOT care about this exception...
            }
        }
        throw new NodeInterfaceNotFoundException("The node "+getId()+" does not have an interface with IP address "+nodeInterfaceIP+".");
    }
    
    public List<NodeInterface> getNodeInterfaces() {
        if(this.getInterfaces() == null) {
            return new ArrayList<NodeInterface>();
        }
        @SuppressWarnings("unchecked")
        List<NodeInterface> interfaces = (List<NodeInterface>) this.getInterfaces().getInterface();
        return interfaces;
    }
    
    public void addNodeInterface(NodeInterface nodeInterface) throws NodeInterfaceAlreadyExistException {
        if(this.getInterfaces() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                this.setInterfaces(factory.createNodeInterfacesType());
            } catch(JAXBException e) {
                logger.error("JAXBException in addNodeInterface. Message: "+e.getMessage());
                if(logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
            }
        } else {
            @SuppressWarnings("unchecked")
            List<NodeInterface> interfaces = (List<NodeInterface>) this.getInterfaces().getInterface();
            for(NodeInterface interf : interfaces) {
                if(interf.equals(nodeInterface)) {
                    throw new NodeInterfaceAlreadyExistException("Interface "+nodeInterface.getId()+" already exists in node "+this.getId());
                }
            }
        }

        this.getInterfaces().getInterface().add(nodeInterface);
    }

    /**
     * Sets the node id to the given id.
     * @param id
     * @throws IdException If the node is in the domain
     */
    public void setElementId(String id) throws IdException {
        try {
            domain.getNode(super.getId());
            throw new IdException("Cannot set Id when element is in the domain.");
        } catch (NodeNotFoundException e) {
            super.setId(id);

            for (Link l : inLinks) {
                ((LinkImpl) l).getTo().setNode(id);
            }

            for (Link l : outLinks) {
                ((LinkImpl) l).getFrom().setNode(id);
            }

        }
    }

    public Domain getDomain() {
        return domain;
    }
}
