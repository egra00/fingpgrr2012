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

import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedPath;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;

import java.util.*;
import java.io.IOException;

import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 * - 27-Apr-2007: prevent NullPointerException when calling computeAllDistinctRoute with maxDepth <= 2 (GMO)
 */

/**
 * AllDistinctsRoutes can be used to compute all distincts route, load or save them
 * to a file and give easy acces to computed routes. This class use ComputeAllDistinctPath
 * to compute all distinct route on a SimplifiedDomain. We use a deep first search algorithm
 * to explore all the distinct path. The maxDepth parameter is used to limit the maximum
 * length of a path. All the computed path contains no loop. This algorithm can be used to
 * design more intelligent TE methods.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class AllDistinctRoutes {

    private static Logger logger = Logger.getLogger(AllDistinctRoutes.class.getName());
    protected SimplifiedDomain domain;
    protected List<SimplifiedPath> routes[][] = null;
    protected int maxDepth = 0 ;

    /**
     * Load all distinct routes from a file
     *
     * @param domain
     * @param fileName
     * @throws IOException
     * @throws AllDistinctRoutesException
     */
    public void loadAllDistinctRoute(SimplifiedDomain domain, String fileName) throws IOException, AllDistinctRoutesException {
        this.domain = domain;
        routes = new List[domain.getNbNodes()][domain.getNbNodes()];
        AllDistinctRoutesFactory.loadAllDistinctRoutes(this,fileName);
    }

    /**
     * Save all distinct routes to a file
     *
     * @param fileName
     * @throws IOException
     * @throws AllDistinctRoutesException
     */
    public void saveAllDistinctRoute(String fileName) throws IOException, AllDistinctRoutesException {
        AllDistinctRoutesFactory.saveAllDistinctRoutes(this,fileName);
    }

    /**
     * Compute all the distinct routes on a given SimplifiedDomain.
     * The maxDepth parameter specifies the maximum length of the generated paths
     *
     * @param domain
     * @param maxDepth
     * @throws LinkNotFoundException
     * @throws NodeNotFoundException
     */
    public void computeAllDistinctRoute(SimplifiedDomain domain, int maxDepth) throws LinkNotFoundException, NodeNotFoundException {
        long time = System.currentTimeMillis();
        ComputeAllDistinctPath cadp = new ComputeAllDistinctPath(domain);
        int nbNodes = domain.getNbNodes();
        routes = new List[nbNodes][nbNodes];
        int nbRoutes = 0;
        for(int srcNode=0;srcNode < nbNodes;srcNode++) {
            for(int dstNode=0;dstNode < nbNodes;dstNode++) {
                if (dstNode != srcNode) {
                    routes[srcNode][dstNode] = cadp.compute(srcNode,dstNode,maxDepth);
                    nbRoutes += routes[srcNode][dstNode].size();

                }
            }
        }
        logger.info("Compute " + nbRoutes + " routes with MAX_DEPTH=" + maxDepth + " in " + (System.currentTimeMillis()-time) + " ms");
        this.maxDepth = maxDepth;
        this.domain = domain;
    }

    public void computeAllDistinctRoute(SimplifiedDomain domain, int maxDepth, int nbPath,boolean verbose) throws LinkNotFoundException, NodeNotFoundException {
            long time = System.currentTimeMillis();
            ComputeAllDistinctPath cadp = new ComputeAllDistinctPath(domain);
            int nbNodes = domain.getNbNodes();
            routes = new List[nbNodes][nbNodes];
            int nbRoutes = 0;
            for(int srcNode=0;srcNode < nbNodes;srcNode++) {
                for(int dstNode=0;dstNode < nbNodes;dstNode++) {
                    if (dstNode != srcNode) {
                        long time1 = System.currentTimeMillis();
                        List<SimplifiedPath> pathList = null;
                        for (int depth=2;depth < maxDepth;depth++) {
                            pathList = cadp.compute(srcNode,dstNode,depth);
                            if (pathList.size() >= nbPath) 
                                break;
                        }
                        routes[srcNode][dstNode] = pathList == null ? new ArrayList<SimplifiedPath>(0) : pathList;
                        nbRoutes += routes[srcNode][dstNode].size();
                        time1 = System.currentTimeMillis() - time1;
                        if (verbose) {
                            System.out.println("Compute " + routes[srcNode][dstNode].size() + " path for pair (" + srcNode + "," + dstNode + ") in " + time1 + " ms");
                        }
                    }
                }
            }
            logger.info("Compute " + nbRoutes + " routes with MAX_DEPTH=" + maxDepth + " in " + (System.currentTimeMillis()-time) + " ms");
            this.maxDepth = maxDepth;
            this.domain = domain;
        }


    /**
     * Get the maximum path length
     * @return
     */
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Get all the disctinct routes from a source to a destination
     *
     * @param srcNodeId
     * @param dstNodeId
     * @return a list of SimplifiedPath
     * @throws AllDistinctRoutesException
     */
    public List<SimplifiedPath> getAllDistinctRoutes(int srcNodeId, int dstNodeId) throws AllDistinctRoutesException {
       if (srcNodeId < 0 || srcNodeId >= domain.getNbNodes())
            throw new AllDistinctRoutesException("The node with id " + srcNodeId + " didn't exist in the domain.");
       if (dstNodeId < 0 || dstNodeId >= domain.getNbNodes())
            throw new AllDistinctRoutesException("The node with id " + srcNodeId + " didn't exist in the domain.");
       return routes[srcNodeId][dstNodeId];
    }
}
