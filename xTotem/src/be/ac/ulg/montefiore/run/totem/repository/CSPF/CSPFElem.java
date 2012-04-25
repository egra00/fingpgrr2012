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
package be.ac.ulg.montefiore.run.totem.repository.CSPF;

import be.ac.ulg.montefiore.run.totem.util.PriorityQueueObject;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;

import java.util.List;
import java.util.ArrayList;

/*
 * Changes:
 * --------
 *
 */

/**
 * Contains the CSPF information
 *
 * This class is used to populate a priority queue structure and implement the
 * <code>PriorityQueueObject</code>.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class CSPFElem extends Object implements PriorityQueueObject {

    private Node node;
    private float cost;
    private List<Node> nextHops;
    private float bandwidth;
    private List<Link> links;

    public CSPFElem(Node node, float cost, Node nextHop, float bandwidth, Link link) {
        this.node = node;
        this.cost = cost;
        
        this.nextHops = new ArrayList<Node>();
        this.nextHops.add(nextHop);
        
        this.bandwidth = bandwidth;
        
        this.links = new ArrayList<Link>();
        this.links.add(link);
    }

    /**
     * Get the id of the object
     *
     * @return the object's id
     */
    public String getId() {
        return node.getId();
    }

    /**
     * Get the key of the object
     *
     * @return the object's key
     */
    public float getKey() {
        return cost;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public Node getNextHop() {
        return nextHops.get(0);
    }

    public void setNextHop(Node nextHop) {
        this.nextHops.set(0,nextHop);
    }

    public List<Node> getNextHops() {
        return nextHops;
    }

    public void addNextHop(Node nextHop) {
        this.nextHops.add(nextHop);
    }

    public float getBandwidth() {
        return bandwidth;
    }

    public void setBandwidth(float bandwidth) {
        this.bandwidth = bandwidth;
    }

    public void addLink(Link link) {
        this.links.add(link);
    }
    
    public List<Link> getLinks() {
        return links;
    }
    
    public Link getLink() {
        return links.get(0);
    }
    
    public void setLink(Link link) {
        this.links.set(0, link);
    }
}
