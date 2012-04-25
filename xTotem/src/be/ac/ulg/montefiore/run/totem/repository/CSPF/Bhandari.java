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

import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.util.PriorityQueue;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.*;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidPathException;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 *
 * - 21-Sep-2005: add an empty implementation for the
 *   computeSPF(Domain, boolean, String) and
 *   computeSPF(Domain, boolean, String, boolean) methods (JLE).
 * - 20-Mar-2006: add equals and hashCode methods (GMO).
 */

/**
 * Implementation of the Bhandari modified Dijkstra aglorithm.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class Bhandari implements SPF {

    private static Logger logger = Logger.getLogger(Bhandari.class.getName());

    private BhandariElem path[];
    private PriorityQueue tent;
    private float distance[];
    private boolean presentInPath[];
    private boolean presentInTent[];

    /**
     * Compute the SPF from a source node to a destination node on the given domain
     *
     * @param domain
     * @param src
     * @param dst
     * @return
     * @throws RoutingException
     * @throws NoRouteToHostException
     */
    public Path computeSPF(Domain domain, String src, String dst) throws RoutingException, NoRouteToHostException {
        if(dst == null) {
            throw new RoutingException("Bhandari : try to compute SPF with dst null");
        }
        //logger.info("Bhandari : compute SPF from " + src + " to " + dst);
        SimplifiedDomain sDomain = SimplifiedDomainBuilder.build(domain);
        try {
            int srcId = domain.getConvertor().getNodeId(src);
            int dstId = domain.getConvertor().getNodeId(dst);
            BhandariElem[] allPath = bhandari(sDomain,srcId);
            SimplifiedPath sPath = extractPath(sDomain,allPath,srcId,dstId);
            Path path = sPath.convert(domain);
            /*
            if (path != null) {
                StringBuffer sb = new StringBuffer("Path computed : [");
                for (int i = 0; i < path.getNodePath().size(); i++) {
                    sb.append(" " + path.getNodePath().get(i).getId());
                }
                sb.append(" ]");
                logger.info(sb.toString());
            } */
            return path;
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Computes the shortest path between two nodes.
     *
     * @param domain
     * @param src    the source node
     * @param dst    the destination node
     * @param ECMP   true if multipath activated
     * @return a list of equal cost multi path
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException
     *
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException
     *
     */
    public List<Path> computeSPF(Domain domain, String src, String dst, boolean ECMP) throws RoutingException, NoRouteToHostException {
        logger.warn("computeSPF with ECMP not implemented");
        return null;
    }

    /**
     * Compute the SPF from a source node to all destination nodes on the given domain
     *
     * @param domain
     * @param src
     * @return
     * @throws RoutingException
     * @throws NoRouteToHostException
     */
    public List<Path> computeSPF(Domain domain, String src) throws RoutingException, NoRouteToHostException {
        //logger.info("Bhandari : compute SPF from " + src + " to all destinations");
        SimplifiedDomain sDomain = SimplifiedDomainBuilder.build(domain);
        try {
            int srcId = domain.getConvertor().getNodeId(src);
            BhandariElem[] allPath = bhandari(sDomain,srcId);
            List<SimplifiedPath> pathList = extractPath(sDomain,allPath,srcId);
            List<Path> goodPath = new ArrayList<Path>(pathList.size());
            for (int i = 0; i < pathList.size(); i++) {
                goodPath.add(pathList.get(i).convert(domain));
            }
            return goodPath;
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Path> computeSPF(Domain domain, String src, boolean ECMP) throws RoutingException, NoRouteToHostException {
        logger.warn("computeSPF with ECMP not implemented");
        return null;
    }
    
    /**
     * Compute the SPF from all source nodes to all destination nodes on the given domain
     *
     * @param domain
     * @return
     * @throws RoutingException
     * @throws NoRouteToHostException
     */
    public List<Path> computeFullMeshSPF(Domain domain) throws RoutingException, NoRouteToHostException {
        List<Path> pathList = new ArrayList<Path>();
        List<Node> nodes = domain.getUpNodes();
        for (int srcNodeIdx = 0; srcNodeIdx < nodes.size(); srcNodeIdx++) {
            pathList.addAll(computeSPF(domain,nodes.get(srcNodeIdx).getId()));
        }
        return pathList;
    }

    public List<Path> computeFullMeshSPF(Domain domain, boolean ECMP) throws RoutingException, NoRouteToHostException {
        logger.warn("computeFullMeshSPF with ECMP not implemented");
        return null;
    }
    
    /**
     * Compute the SPF from a source node to a destination node on the SimplifiedDomain
     *
     * @param src
     * @param dst
     * @return
     * @throws LinkNotFoundException
     * @throws NodeNotFoundException
     * @throws RoutingException
     * @throws NoRouteToHostException
     */
    public SimplifiedPath computeSPF(SimplifiedDomain domain, int src, int dst) throws LinkNotFoundException, NodeNotFoundException, RoutingException, NoRouteToHostException {
        //logger.info("Bhandari : compute path from " + src + " to " + dst + " on domain \"" + domain.getName()+"\"");
        BhandariElem[] allPath = bhandari(domain,src);
        //displayAllPath(allPath);
        SimplifiedPath path = extractPath(domain,allPath,src,dst);
        /*if (path != null) {
            StringBuffer sb = new StringBuffer("Path computed : [");
            for (int i = 0; i < path.getLinkIdPath().length; i++) {
                sb.append(" " + path.getLinkIdPath()[i]);
            }
            sb.append(" ]");
            logger.info(sb.toString());
        } */
        return path;
    }

    /**
     * Compute the SPF from all source nodes to all destination nodes on the SimplifiedDomain
     *
     * @param src
     * @return
     * @throws LinkNotFoundException
     * @throws NodeNotFoundException
     * @throws RoutingException
     * @throws NoRouteToHostException
     */
    public List<SimplifiedPath> computeSPF(SimplifiedDomain domain, int src) throws LinkNotFoundException, NodeNotFoundException, RoutingException, NoRouteToHostException {
        //logger.info("Bhandari : compute path from " + src + " to all destination on domain \"" + domain.getName() + "\"");
        BhandariElem[] allPath = bhandari(domain,src);
        List<SimplifiedPath> pathList = extractPath(domain,allPath,src);
        /*
        for (int i = 0; i < pathList.size(); i++) {
            SimplifiedPath path = pathList.get(i);
            if (path != null) {
                StringBuffer sb = new StringBuffer("Path computed : [");
                for (int j = 0; j < path.getLinkIdPath().length; j++) {
                    sb.append(" " + path.getLinkIdPath()[j]);
                }
                sb.append(" ]");
                logger.info(sb.toString());
            }
        }  */
        return pathList;
    }

    public List<Path> computeSPF(Domain domain, boolean isSource, String node) {
        logger.warn("This method is not implemented in Bhandari!");
        return null;
    }
    
    public List<Path> computeSPF(Domain domain, boolean isSource, String node, boolean ECMP) {
        logger.warn("This method is not implemented in Bhandari!");
        return null;
    }
    
    /**
     * Compute the shortest path with the Bhandari modified Dijkstra algorithm. This method supposes that
     * the nodeId are consecutive in the SimplifiedTopology.
     *
     * @param srcNode
     */
    private BhandariElem[] bhandari(SimplifiedDomain domain, int srcNode) throws RoutingException, NodeNotFoundException, LinkNotFoundException {
        int nbNodes = domain.getNbNodes(); // number of nodes in the topology
        path = new BhandariElem[nbNodes]; // current computed path
        tent = new PriorityQueue(nbNodes);
        presentInPath = new boolean[nbNodes];
        presentInTent = new boolean[nbNodes];
        distance = new float[nbNodes];

        int currentNode;
        int outLinks[];

        // Initialisation
        for(int nodeIdx = 0; nodeIdx < nbNodes; nodeIdx++) {
            if (domain.isNode(nodeIdx)) {
                tent.add(new BhandariElem(nodeIdx,Integer.MAX_VALUE,-1,-1,-1));
                distance[nodeIdx] = Integer.MAX_VALUE;
                presentInTent[nodeIdx] = true;
                presentInPath[nodeIdx] = false;
                path[nodeIdx] = null;
            }
        }
        BhandariElem pathNode = new BhandariElem(srcNode,0,srcNode,-1,-1);
        currentNode = pathNode.getId();
        tent.update(pathNode);

        BhandariElem testElem = (BhandariElem) tent.removeNext();
        if (testElem.getId() != srcNode)
            throw new RoutingException("Problem during the initialisation of the tent list");

        // Add the source node to the PATH list
        path[srcNode] = pathNode;
        presentInPath[srcNode] = true;
        presentInTent[srcNode] = false;
        distance[srcNode] = 0;

        // relax the source node
        outLinks = domain.getOutLinks(currentNode);
        for (int i = 0; i < outLinks.length; i++) {
            relax(domain,currentNode,outLinks[i]);
        }

        while (tent.size() != 0) {
            // get the next node with the shortest distance
            pathNode = (BhandariElem) tent.removeNext();
            currentNode = pathNode.getId();
            // add this node to the PATH list
            path[currentNode] = pathNode;
            presentInPath[currentNode] = true;
            presentInTent[currentNode] = false;
            // relax the current node
            outLinks = domain.getOutLinks(currentNode);
            for (int i = 0; i < outLinks.length; i++) {
                relax(domain,currentNode,outLinks[i]);
            }
        }
        return path;
    }

    /**
     * Relax the node at the end of the link
     *
     * @param currentNode current node
     * @param linkId link to relax
     */
    private boolean relax(SimplifiedDomain domain, int currentNode, int linkId) throws LinkNotFoundException {
        boolean relax = false;
        int nextNodeId = domain.getLinkDst(linkId);
        float distToNextNode = distance[currentNode] + domain.getLinkWeight(linkId);
        if (distance[nextNodeId] > distToNextNode) {
            distance[nextNodeId] = distToNextNode;
            BhandariElem elem = new BhandariElem(nextNodeId, distToNextNode, currentNode, domain.getLinkCapacity(linkId),linkId);
            if (presentInTent[nextNodeId]) {
                tent.update(elem);
            } else {
                tent.add(elem);
            }
            if (presentInPath[nextNodeId]) {
                path[nextNodeId] = null;
                presentInPath[nextNodeId] = false;
                presentInTent[nextNodeId] = true;
            }
            relax = true;
        }
        return relax;
    }

    /**
     * Extract the path from a source to a destination from the path tree computed by bhandari
     *
     * @param allPath
     * @param srcNode
     * @param dstNode
     * @return
     * @throws NoRouteToHostException
     * @throws RoutingException
     */
    private SimplifiedPath extractPath(SimplifiedDomain domain, BhandariElem allPath[], int srcNode, int dstNode)
            throws NoRouteToHostException, RoutingException {

        int reversePath[] = new int[allPath.length];
        int currentNode = dstNode;
        int pathLength = 0;
        while ((currentNode != srcNode) && (pathLength < allPath.length)){
            if (allPath[currentNode].getNextHop() == -1)
                throw new NoRouteToHostException();
            pathLength++;
            reversePath[allPath.length - pathLength] = allPath[currentNode].getLinkId();
            currentNode = allPath[currentNode].getNextHop();
        }
        if (pathLength == allPath.length)
            throw new RoutingException("ExtractPath : Impossible find a path for demand between ("+srcNode +"," +dstNode+")");

        int[] linkIdPath = new int[pathLength];
        int offset = allPath.length - pathLength;
        for(int i=0; i < pathLength;i++) {
            linkIdPath[i] = reversePath[offset + i];
        }

        SimplifiedPath path = new SimplifiedPath(domain,linkIdPath);
        return path;
    }

    /**
     * Extract a list of path from a source to all destination from the path tree computed by bhandari
     *
     * @param allPath
     * @param srcNode
     * @return
     * @throws RoutingException
     * @throws NoRouteToHostException
     */
    private List<SimplifiedPath> extractPath(SimplifiedDomain domain, BhandariElem allPath[], int srcNode) throws RoutingException, NoRouteToHostException {
        List<SimplifiedPath> pathList = new ArrayList<SimplifiedPath>(domain.getNbNodes());
        for (int dstNode = 0; dstNode < domain.getNbNodes(); dstNode++) {
            if ((domain.isNode(dstNode)) && (dstNode != srcNode)) {
                pathList.add(extractPath(domain,allPath,srcNode,dstNode));
            }
        }
        return pathList;
    }

    // DEBUG
    private void displayAllPath(BhandariElem allPath[]) {
        for (int i = 0; i < allPath.length; i++) {
            BhandariElem bhandariElem = allPath[i];
            System.out.println("Elem " + i + " : " + bhandariElem.toString());
        }
    }

    public boolean equals(Object o) {
        if (!(o instanceof Bhandari))
            return false;
        return true;
    }

    public int hashCode() {
        return Bhandari.class.getName().hashCode();
    }

}
