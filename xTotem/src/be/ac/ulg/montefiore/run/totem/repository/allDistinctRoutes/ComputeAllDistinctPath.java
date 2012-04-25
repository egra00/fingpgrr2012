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
package be.ac.ulg.montefiore.run.totem.repository.allDistinctRoutes;

import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedPath;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;

import java.util.List;
import java.util.ArrayList;

/*
 * Changes:
 * --------
 *
 */

/**
 * Compute and store all distinct route on a SimplifiedDomain. We use
 * a deep first search algorithm to explore all the distinct path. The maxDepth
 * parameter is used to limit the maximum length of a path. All the computed path
 * contains no loop.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class ComputeAllDistinctPath {

    private final int NO_NEXT_LINK = -1;
    private SimplifiedDomain topology;
    private int currentNode;
    private int currentLink;
    private List<Integer> currentLinkRoute;
    private ArrayList<Integer> currentNodeRoute;
    private boolean markedLinks[];
    private int currentDepth;

    /**
     * Initialise the SimplifiedDomain
     * @param domain
     */
    public ComputeAllDistinctPath(SimplifiedDomain domain) {
        this.topology = domain;
        markedLinks = new boolean[topology.getNbLinks()];
        for (int i = 0; i < markedLinks.length; i++) {
            markedLinks[i] = false;
        }
        currentDepth = 0;
    }

    /**
     * Compute all distinct routes from a source to a destination with maxDepth maximum route length.
     * @param srcNode
     * @param dstNode
     * @param maxDepth
     * @return
     * @throws NodeNotFoundException
     * @throws LinkNotFoundException
     */
    public List<SimplifiedPath> compute(int srcNode, int dstNode, int maxDepth) throws NodeNotFoundException, LinkNotFoundException {
        List<SimplifiedPath> routes = new ArrayList<SimplifiedPath>(); // List of all the routes
        currentLinkRoute = new ArrayList<Integer>();
        currentNodeRoute = new ArrayList<Integer>();
        boolean loop = false;
        boolean foundRoute = false;
        boolean hopLimit = false;

        for (int i = 0; i < markedLinks.length; i++) {
            markedLinks[i] = false;
        }
        currentDepth = 0;
        currentNode = srcNode;
        currentLink = -1;
        do {
            loop = foundRoute = hopLimit = false;
            boolean doneAtThisNode = stepAhead();
            if (!doneAtThisNode) {
                if (currentNodeRoute.contains(new Integer(currentNode))) {
                    loop = true;
                }
                if (currentNode == dstNode) {
                    foundRoute = true;
                }
                if (currentDepth > maxDepth)  {
                    hopLimit = true;
                }
            }
            if (doneAtThisNode || loop || hopLimit) {
                backup(loop);
            } else if (foundRoute) {
                // Create route as int array
                int[] routeAr = new int[currentLinkRoute.size()];
                for (int i = 0; i < routeAr.length; i++) {
                    routeAr[i] = currentLinkRoute.get(i);
                }
                routes.add(new SimplifiedPath(topology,routeAr));
                backup(false);
            }
        } while ((currentNode != srcNode) || (getNextLink(srcNode) != NO_NEXT_LINK));

        return routes;
    }

    /**
     * Explore next path
     *
     * @return
     * @throws LinkNotFoundException
     * @throws NodeNotFoundException
     */
    private boolean stepAhead() throws LinkNotFoundException, NodeNotFoundException {
        int linkId = getNextLink(currentNode);
        if (linkId == NO_NEXT_LINK) {
            return true;
        }
        currentLink = linkId;
        currentLinkRoute.add(new Integer(currentLink));
        currentNodeRoute.add(new Integer(currentNode));
        currentNode = topology.getLinkDst(currentLink);
        setLinkMark(currentLink);
        currentDepth++;
        return false;
    }

    /**
     * Get back to explore other path
     * @param bool
     * @throws LinkNotFoundException
     * @throws NodeNotFoundException
     */
    private void backup(boolean bool) throws LinkNotFoundException, NodeNotFoundException {
        int parentNode = topology.getLinkSrc(currentLink);
        int[] outLinks = topology.getOutLinks(currentNode);
        if (!bool) {
            for (int i = 0; i < outLinks.length; i++) {
                int idx = currentLinkRoute.indexOf(outLinks[i]);
                if ((idx < 0) || (outLinks[i] == currentLink)) {
                    unsetLinkMark(outLinks[i]);
                }
            }
        }
        currentLinkRoute.remove(currentLinkRoute.size()-1);
        currentNodeRoute.remove(currentNodeRoute.size()-1);
        currentDepth--;
        currentNode = parentNode;
        if (currentLinkRoute.size() != 0) {
            currentLink =  currentLinkRoute.get(currentLinkRoute.size()-1).intValue();
        } else {
            currentLink = -1;
        }

    }

    /**
     * Get the next link to explore
     *
     * @param nodeId
     * @return
     * @throws NodeNotFoundException
     */
    private int getNextLink(int nodeId) throws NodeNotFoundException {
        int outLinks[] = topology.getOutLinks(nodeId);
        for(int i=0;i < outLinks.length;i++) {
            if ((outLinks[i] < markedLinks.length) && (isMarkedLink(outLinks[i]) == false)) {
                return outLinks[i];
            }
        }
        return NO_NEXT_LINK;
    }

    /**
     * True if the link is already marked
     *
     * @param linkId
     * @return
     */
    private boolean isMarkedLink(int linkId) {
        return (markedLinks[linkId] == true);
    }

    /**
     * Mark the link
     *
     * @param linkId
     */
    private void setLinkMark(int linkId) {
        markedLinks[linkId] = true;
    }

    /**
     * Unmark the link
     *
     * @param linkId
     */
    private void unsetLinkMark(int linkId) {
        markedLinks[linkId] = false;
    }

}
