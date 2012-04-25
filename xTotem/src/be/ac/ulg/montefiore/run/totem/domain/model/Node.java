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
package be.ac.ulg.montefiore.run.totem.domain.model;

import be.ac.ulg.montefiore.run.totem.domain.exception.NodeInterfaceAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeInterfaceNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.StatusTypeException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeTypeException;

import java.util.List;

/*
 * Changes:
 * --------
 *
 * - 01-Feb-2006: extends DomainElement (JLE).
 * - 07-Feb-2006: add getNodeInterface(String) method (JLE).
 * - 07-Feb-2006: getAllIPs now returns a list of String (JLE).
 * - 08-Feb-2006: add getNodeInterfaces (JLE).
 * - 06-Mar-2006: add getRouterId and setRouterId (JLE).
 * - 16-Jan-2007: add getAllInLink & getAllOutLink methods + update javadoc (GMO).
 * - 08-Mar-2007: change constants in enum for Node type, setNodeType(.) doesn't throw exception anymore (GMO)
 * - 25-Apr-2007: getInLink, getOutLink, getAllInLink, getAllOutLink don't throw an exception anymore (GMO)
 * - 14-Jun-2007: getNodeType() doesn't throw an exception anymore (GMO)
 * - 18-Oct-2007: add setNodeStatus(String) method (GMO)
 */

/**
 * Represents a Node in a Domain.
 * 
 * <p>Creation date : 19-Jan-2005 15:46:33
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author Bruno Quoitin (bqu@info.ucl.ac.be)
 */
public interface Node extends DomainElement {

    public final int STATUS_UP = 0;
    public final int STATUS_DOWN = 1;

    public enum Type {
        EDGE, CORE, NEIGH, VIRTUAL;
    }

    public String getRid();
    public void setRid(String rid);

    public List<String> getallIPs();

    public int getNodeStatus();
    public void setNodeStatus(int status) throws StatusTypeException;
    public void setNodeStatus(String status) throws StatusTypeException;

    /**
     * Return the list of the links that begins at this node.
     *
     * @return the list of the links that arrives at this node
     */
    public List<Link> getAllOutLink();

    /**
     * Return the list of the links that ends at this node.
     *
     * @return the list of the links that arrives at this node
     */
    public List<Link> getAllInLink();

    /**
     * Return the list of the links that begins at this node and for which status is UP.
     *
     * @return the list of the links that arrives at this node
     */
    public List<Link> getOutLink();

    /**
     * Return the list of the links that ends at this node and for which status is UP.
     *
     * @return the list of the links that arrives at this node
     */
    public List<Link> getInLink();

    public float getLongitude();
    public float getLatitude();

    public void setLongitude(float longitude);
    public void setLatitude(float latitude);

    public boolean equals(Object o);
    // The hashCode method has to be overridden so as to maintain the general
    // contract for the hashCode method, which states that equal objects must
    // have equal hash codes.
    public int hashCode();

    public Type getNodeType();
    public void setNodeType(Type type);
    
    public String getDescription();
    public void setDescription(String description);
    
    public BgpRouter getBgpRouter();
    
    public NodeInterface getNodeInterface(String nodeInterfaceId) throws NodeInterfaceNotFoundException;
    public NodeInterface getNodeInterfaceByIP(String nodeInterfaceIP) throws NodeInterfaceNotFoundException;
    public List<NodeInterface> getNodeInterfaces();
    public void addNodeInterface(NodeInterface nodeInterface) throws NodeInterfaceAlreadyExistException;
}
