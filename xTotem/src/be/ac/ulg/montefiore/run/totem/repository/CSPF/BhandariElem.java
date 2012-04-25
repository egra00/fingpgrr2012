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

import be.ac.ulg.montefiore.run.totem.util.PriorityQueueObjectInt;

/*
 * Changes:
 * --------
 *
 */

/**
 * Contains the Bhandari information
 *
 * This class is used to populate a priority queue structure and implement the
 * <code>PriorityQueueObject</code>.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class BhandariElem extends Object implements PriorityQueueObjectInt {

    private int nodeId;
    private float cost;
    private int nextHop;
    private float bandwidth;
    private int linkId;

    public BhandariElem(int nodeId, float cost, int nextHop, float bandwidth, int linkId) {
        this.nodeId = nodeId;
        this.cost = cost;
        this.nextHop = nextHop;
        this.bandwidth = bandwidth;
        this.linkId = linkId;
    }

    /**
     * Get the id of the object
     *
     * @return the object's id
     */
    public int getId() {
        return nodeId;
    }

    /**
     * Get the key of the object
     *
     * @return the object's key
     */
    public double getKey() {
        return cost;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public float getCost() {
        return cost;
    }

    public int getNextHop() {
        return nextHop;
    }

    public float getBandwidth() {
        return bandwidth;
    }

    public int getLinkId() {
        return linkId;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("(nodeId:");
        sb.append(nodeId);
        sb.append(",cost:");
        sb.append(cost);
        sb.append(",nextHop:");
        sb.append(nextHop);
        sb.append(",bandwidth:");
        sb.append(bandwidth);
        sb.append(",linkId:");
        sb.append(linkId);
        sb.append(")");
        return sb.toString();
    }

}
