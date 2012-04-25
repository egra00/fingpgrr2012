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

import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidPathException;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/*
 * Changes:
 * --------
 *
 * - 19-Sep-2005: comment correction (JLE).
 * - 14-Oct-2005: add equals(Object) method (GMO)
 * - 20-Mar-2006: add hashCode() method (GMO).
 * - 31-Mar-2006: check path continuousness eventually throwing an InvalidPathException when creating the path (GMO)
 * - 19-May-2006: more explicit messages in thrown exception (GMO)
 */

/**
 * Represent a Path in a Domain and is used by a LSP
 *
 * <p>Creation date: 20-Jan-2005 10:45:08
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class PathImpl implements Path {

    private ArrayList<Link> linkPath;
    private ArrayList<Node> nodePath;
    private DomainImpl domain;

    public PathImpl(Domain domain) {
        linkPath = new ArrayList<Link>();
        nodePath = new ArrayList<Node>();
        this.domain = (DomainImpl) domain;
    }

    /**
     * Create a Path from a list of Link
     * @param path
     * @throws NodeNotFoundException
     * @throws InvalidPathException When the path is not continuous
     */
    public void createPathFromLink(List<Link> path) throws NodeNotFoundException, InvalidPathException {

        // check path continuity
        Node end = path.get(0).getDstNode();
        for (int i = 1; i < path.size(); i++) {
            if (!path.get(i).getSrcNode().equals(end)) {
                throw new InvalidPathException("Path not continuous.");
            }
            end = path.get(i).getDstNode();
        }

        this.linkPath = new ArrayList<Link>();
        this.linkPath.addAll(path);
        nodePath = new ArrayList<Node>(path.size()+1);
        for (int i = 0; i < path.size(); i++) {
            Link link = path.get(i);
            nodePath.add(link.getSrcNode());
            if (i == (path.size()-1))
                nodePath.add(link.getDstNode());
        }
    }

    /**
     * Create a Path from a list of Node
     * @param path
     * @throws NodeNotFoundException
     * @throws InvalidPathException When the path is not continuous
     */
    public void createPathFromNode(List<Node> path) throws NodeNotFoundException, InvalidPathException {
        this.nodePath = new ArrayList<Node>();
        nodePath.addAll(path);
        linkPath = new ArrayList<Link>(path.size()-1);
        for (int i = 0; i < path.size()-1; i++) {
            Node src = path.get(i);
            Node dst = path.get(i+1);
            List<Link> links = null;
            links = domain.getLinksBetweenNodes(src,dst);
            if (links.size() >= 1)
                linkPath.add(links.get(0));
            else throw new InvalidPathException("Path not continuous.");
        }
    }

    /**
     * Get the path as a List of Link
     * @return
     */
    public List<Link> getLinkPath() {
        return (List<Link>) linkPath.clone();
    }

    /**
     * Get the path as a List of Node
     * @return
     */
    public List<Node> getNodePath() {
        return (List<Node>) nodePath.clone();
    }

    /**
     * Get the source node
     * @return
     */
    public Node getSourceNode() {
        if ((nodePath != null) && (nodePath.size() >= 1))
            return nodePath.get(0);
        return null;
    }

    /**
     * Get the cost of the path
     * @return
     */
    public float getSumLinkMetrics() {
        float cost = 0;
        for (Link l : getLinkPath()) {
            cost += l.getMetric();
        }
        return cost;
    }

    /**
     * Get the destination node
     * @return
     */
    public Node getDestinationNode() {
        if ((nodePath != null) && (nodePath.size() >= 1))
            return nodePath.get(nodePath.size()-1);
        return null;
    }

    /**
     * Return true if the path contains the link Link
     *
     * @param link
     * @return
     */
    public boolean containsLink(Link link) {
        Iterator<Link> it = linkPath.iterator();
        while (it.hasNext()) {
            Link l = it.next();
            if (l.getId().equals(link.getId()))
                return true;
        }
        return false;
    }

    /**
     * Return true if the path is equal to the given Path
     * @param path
     * @return
     */ 
    public boolean equals(Path path) {
        List<Link> p2 = path.getLinkPath();
        if (p2.size() != linkPath.size())
            return false;
        for (int i = 0; i < p2.size(); i++) {
            if (!p2.get(i).getId().equals(linkPath.get(i).getId()))
                return false;
        }
        return true;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Path)) return false;
        else return equals((Path) o);
    }

    public int hashCode() {
        int result = linkPath.size();
        for (Link lnk : linkPath) {
            result = 29 * result + lnk.getId().hashCode();
        }
        return result;
    }

    /**
     * Get a String displaying the path as node id
     *
     * @return
     */
    public String toString() {
        return toNodesString();
    }

    public String toNodesString() {
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < nodePath.size(); i++) {
            String nodeId = null;
            nodeId = nodePath.get(i).getId();
            //if (nodeId.indexOf('.') > 0)  {
            //    sb.append(" " + nodeId.substring(0,nodeId.indexOf('.')));
            //} else {
                sb.append(" " + nodeId);
            //}
        }
        sb.append(" ]");
        return sb.toString();
    }

    public String toLinksString() {
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < linkPath.size(); i++) {
            String linkId = null;
            linkId = linkPath.get(i).getId();
            sb.append(" " + linkId);
        }
        sb.append(" ]");
        return sb.toString();
    }

}
