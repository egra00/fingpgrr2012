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

import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidPathException;

import java.util.List;

/*
 * Changes:
 * --------
 *
 * - 19-Sep-2005: comment correction (JLE).
 * - 31-Mar-2006: new exception thrown (InvalidPathException) when creating the path (GMO)
 * - 21-Jun-2007: add toNodesString() and toLinksString() (GMO)
 */

/**
 * Represent a Path in a Domain and is used by a LSP
 *
 * <p>Creation date: 20-Jan-2005 10:45:08
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface Path {

    /**
     * Create a Path from a list of Link
     * @param path
     * @throws NodeNotFoundException
     */
    public void createPathFromLink(List<Link> path) throws NodeNotFoundException, InvalidPathException;

    /**
     * Create a Path from a list of Node
     * @param path
     * @throws NodeNotFoundException
     */
    public void createPathFromNode(List<Node> path) throws NodeNotFoundException, InvalidPathException;

    /**
     * Get the path as a List of Link
     * @return
     */
    public List<Link> getLinkPath();

    /**
     * Get the path as a List of Node
     * @return
     */
    public List<Node> getNodePath();

    /**
     * Get the source node
     * @return
     */
    public Node getSourceNode();

    /**
     * Get the cost of the path
     * @return
     */
    public float getSumLinkMetrics();

    /**
     * Get the destination node
     * @return
     */
    public Node getDestinationNode();

    /**
     * Return true if the path contains the link Link
     *
     * @param link
     * @return
     */
    public boolean containsLink(Link link);

    /**
     * Return true if the path is equal to the given Path
     * @param path
     * @return
     */
    public boolean equals(Path path);

    /**
     * Get a String displaying the path as node ids
     *
     * @return
     */
    public String toString();

    /**
     * Get a String displaying the path as node ids
     * @return
     */
    public String toNodesString();

    /**
     * Get a String displaying the path as link ids
     * @return
     */
    public String toLinksString();
}
