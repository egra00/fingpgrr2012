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

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.IPAddressFormatException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NotInitialisedException;
import be.ac.ulg.montefiore.run.totem.domain.exception.StatusTypeException;
import be.ac.ulg.montefiore.run.totem.domain.exception.IdException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.NodeInterface;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.StatusType;
import be.ac.ulg.montefiore.run.totem.util.DataValidationTools;

/*
 * Changes:
 * --------
 *
 * - 08-Feb-2006: rename getInterfaceStatus into getNodeInterfaceStatus (JLE)
 * - 08-Feb-2006: rename setInterfaceStatus into setNodeInterfaceStatus (JLE)
 * - 08-Feb-2006: add equals(Object) and hashCode() methods (JLE)
 * - 06-Mar-2006: add NodeInterfaceImpl(String) constructor (JLE)
 * - 04-May-2006: add setDomain(Domain) and setNode(Node) methods (JLE)
 * - 27-Oct-2006: add setIP(String) and setIPMask(String, int) (JLE)
 * - 18-Oct-2007: use DatavalidationTools to check ip addresses (GMO)
 */

/**
 * Represents an interface of a node.
 *
 * <p>Creation date: 07-f�vr.-2006
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class NodeInterfaceImpl extends be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.NodeInterfaceImpl implements NodeInterface {
    
    private static Logger logger = Logger.getLogger(NodeInterfaceImpl.class);
        
    private Domain domain;
    private Node node;
    
    public NodeInterfaceImpl() {}
    
    public NodeInterfaceImpl(Domain domain, Node node, String nodeInterfaceId) {
        super();
        this.domain = domain;
        this.node = node;
        this.setId(nodeInterfaceId);
    }

    public void setDomain(Domain domain) {
    	this.domain = domain;
    }
    
    public void setNode(Node node) {
    	this.node = node;
    }
    
    public int getNodeInterfaceStatus() {
        if(this.getStatus() == null) {
            this.setStatus(StatusType.UP);
        }
        if(this.getStatus().equals(StatusType.UP)) {
            return STATUS_UP;
        }
        return STATUS_DOWN;
    }

    public void setNodeInterfaceStatus(int status) throws StatusTypeException {
        logger.info("Interface "+this.getId()+" set status "+ ((status == STATUS_DOWN) ? "DOWN" : "UP"));
        if(status == STATUS_DOWN) {
            this.setStatus(StatusType.DOWN);
        } else if(status == STATUS_UP) {
            this.setStatus(StatusType.UP);
        } else {
            throw new StatusTypeException("Status "+status+" not allowed!");
        }
    }

    public String getIP() throws NotInitialisedException {
        if(this.getIp() == null) {
            throw new NotInitialisedException("IP address of interface "+getId()+" not initialised!");
        }
        return this.getIp().getValue();
    }

    public String getIPMask() throws NotInitialisedException {
        if(this.getIp() == null) {
            throw new NotInitialisedException("IP address of interface "+getId()+" not initialised!");
        }
        return this.getIp().getMask();
    }

    public void setIP(String address) throws IPAddressFormatException {
        if(!DataValidationTools.isIPAddress(address)) {
            throw new IPAddressFormatException(address+" is not a valid IPv4 address!");
        }
        IpType ip = this.getIp();
        if(ip == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                ip = factory.createNodeInterfaceIpType();
                this.setIp(ip);
            } catch(JAXBException e) {
                logger.error("JAXBException in setIP. Message: "+e.getMessage());
                if(logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
            }
        }
        ip.setValue(address);
    }
    
    public void setIPMask(String address, int maskLength) throws IPAddressFormatException {
        if(!DataValidationTools.isIPAddress(address)) {
            throw new IPAddressFormatException(address+" is not a valid IPv4 address!");
        }
        if((maskLength < 0) || (maskLength > 32)) {
            throw new IPAddressFormatException(maskLength+" is smaller than 0 or higher than 32!");
        }
        IpType ip = this.getIp();
        if(ip == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                ip = factory.createNodeInterfaceIpType();
                this.setIp(ip);
            } catch(JAXBException e) {
                logger.error("JAXBException in setIP. Message: "+e.getMessage());
                if(logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
            }
        }
        ip.setMask(address+"/"+maskLength);
    }
    
    public boolean equals(Object o) {
        if(!(o instanceof NodeInterfaceImpl)) {
            return false;
        }
        NodeInterfaceImpl nodeInterface = (NodeInterfaceImpl) o;

        //we still want to be able to use equals and hashcode methods if the class is not initialized
        if (domain == null) return super.equals(o);

        return (nodeInterface.domain.getASID() == this.domain.getASID()) &&
               (nodeInterface.node.getId().equals(this.node.getId())) &&
               (nodeInterface.getId().equals(this.getId()));
    }
    
    public int hashCode() {
        if (domain == null) return super.hashCode();
        return domain.getASID() + node.getId().hashCode() + this.getId().hashCode();
    }

    public void setElementId(String id) throws IdException {
        setId(id);
    }

    public Domain getDomain() {
        return domain;
    }
}
