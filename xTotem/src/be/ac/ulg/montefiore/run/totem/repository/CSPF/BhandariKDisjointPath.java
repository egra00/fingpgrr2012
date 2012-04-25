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

import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.CreatePathException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedPath;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 *
 */

/**
 * Implementation of the Bhandari K Disjoint Shortest Path aglorithm. This algorithm can be used to compute disjoint
 * paths between pair of nodes. This implementation works on the SimplifiedDomain and supports only link disjoint paths
 * although Bhandari supports also node disjoint path. This implementation does not support multiple links between nodes.
 *
 * For more information about the algorithms see R. Bhandari, "Survivable Networks: Algorithms for Diverse Routing",
 * Kluwer Academic.
 *
 * <p>Creation date: 18-May-2005
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class BhandariKDisjointPath {

    private static Logger logger = Logger.getLogger(BhandariKDisjointPath.class.getName());
    private SimplifiedDomain topo;

    /**
     * Init the topology as a SimplifiedDomain
     *
     * @param topo
     */
    public BhandariKDisjointPath(SimplifiedDomain topo) {
        this.topo = topo;
    }

    /**
     *
     * Compute K disjoint shortest path between src to dst. The algorithm first computes a shortest path using the
     * Bhandari shortest path. Then the path's links are removed and the metric of the opposite links are negativate.
     * A second shortest path is computed using the Bhandari algorithm that manages the negative metric. This process
     * is done K times. At the end with the K path, the algorithm removes the interlacing links and reassembles the
     * segments to produces the K disjoint paths.
     *
     * @param src
     * @param dst
     * @param k
     * @return
     * @throws NoRouteToHostException
     * @throws RoutingException
     * @throws CloneNotSupportedException
     * @throws CreatePathException
     * @throws LinkNotFoundException
     * @throws NodeNotFoundException
     * @see Bhandari
     */
    public SimplifiedPath[] computeLinkDisjointPath(int src, int dst, int k) throws NoRouteToHostException, RoutingException, CloneNotSupportedException, CreatePathException, LinkNotFoundException, NodeNotFoundException {
        SimplifiedDomain workingTopo = (SimplifiedDomain) topo.clone();
        Bhandari bhandari = new Bhandari();

        logger.debug("Compute " + k + " disjoint path with Bhandari between " + src + " and " + dst + " on topology " + workingTopo.getName());
        //workingTopo.display();
        SimplifiedPath path[] = new SimplifiedPath[k];
        for (int pathIdx=0; pathIdx < k-1; pathIdx++) {
            workingTopo.display();
            path[pathIdx] = bhandari.computeSPF(workingTopo,src,dst);
            // Modify the topology
            int[] links = path[pathIdx].getLinkIdPath();
            for (int i = 0; i < links.length; i++) {
                int linkId = links[i];
                int linkSrcNode = workingTopo.getLinkSrc(linkId);
                int linkDstNode = workingTopo.getLinkDst(linkId);
                List<SimplifiedDomain.Link> connectivity = workingTopo.getConnectivity(linkDstNode,linkSrcNode);
                for (int j = 0; j < connectivity.size(); j++) {
                    int linkInt = connectivity.get(j).getId();
                    //Change Weight
                    workingTopo.setLinkWeight(linkInt,- workingTopo.getLinkWeight(linkInt));
                    logger.debug(" Set weight of link " + linkInt + " to " + workingTopo.getLinkWeight(linkInt));
                }
                // Remove Link
                workingTopo.removeLink(linkId);
                logger.debug("Remove link " + linkId);
            }
        }
        workingTopo.display();
        path[k-1] = bhandari.computeSPF(workingTopo,src,dst);

        logger.debug("Print computed path with interlacing");
        for (int i = 0; i < path.length; i++) {
            SimplifiedPath linkIdPath = path[i];
            logger.debug("Path " + i + " : " + linkIdPath.toString());
        }


        List interlacingLink[] = new List[k];
        for (int i = 0; i < k; i++) {
            interlacingLink[i] = new ArrayList(path[i].getLinkIdPath().length);
        }
        for (int pathIdx=0; pathIdx < k-1; pathIdx++) {
            int[] links1 = path[pathIdx].getLinkIdPath();
            for (int pathIdx2=pathIdx+1; pathIdx2 < k; pathIdx2++) {
                int[] links2 = path[pathIdx2].getLinkIdPath();
                for (int i = 0; i < links1.length; i++) {
                    int l1 = links1[i];
                    for (int j = 0; j < links2.length; j++) {
                        int l2 = links2[j];
                        if ((topo.getLinkSrc(l1) == topo.getLinkDst(l2)) && (topo.getLinkDst(l1) == topo.getLinkSrc(l2))) {
                            interlacingLink[pathIdx].add(new Integer(l1));
                            interlacingLink[pathIdx2].add(new Integer(l2));
                        }
                    }
                }
            }
        }

        //System.out.println("Number of interlacing links : " + nbInterlacingLink);


        /*
        for (int i = 0; i < interlacingLink.length; i++) {
        List links = interlacingLink[i];
        System.out.print("Link to remove from path " + i + " : ");
        for (int j = 0; j < links.size(); j++) {
        System.out.print(((Integer) links.get(j)).intValue() + " ");
        }
        System.out.println("");
        } */


        List<SimplifiedPath> segments[] = new List[k];
        for (int i = 0; i < k; i++) {
            segments[i] = new ArrayList<SimplifiedPath>();
        }

        SimplifiedPath seg;
        for (int pathIdx = 0; pathIdx < path.length; pathIdx++) {
            int lastCut = 0;
            int[] currentPath = path[pathIdx].getLinkIdPath();
            for (int cutIdx = lastCut; cutIdx < currentPath.length; cutIdx++) {
                for (int i = 0; i < interlacingLink[pathIdx].size(); i++) {
                    int cuttingLink = ((Integer) interlacingLink[pathIdx].get(i)).intValue();
                    if (currentPath[cutIdx] == cuttingLink) {
                        if ((cutIdx - lastCut) != 0) {
                            int linkPath[] = new int[cutIdx - lastCut];
                            for (int l = lastCut; l < cutIdx; l++) {
                                linkPath[l - lastCut] = currentPath[l];
                            }
                            seg = new SimplifiedPath(workingTopo,linkPath);
                            segments[pathIdx].add(seg);
                        }
                        if (cutIdx+1 < currentPath.length) {
                            lastCut = cutIdx+1;
                        } else {
                            System.out.println("ERROR : We cut the last link in the path1");
                        }
                        break;
                    }
                }

            }
            int linkPath[] = new int[currentPath.length - lastCut];
            for (int l = lastCut; l < currentPath.length; l++) {
                linkPath[l - lastCut] = currentPath[l];
            }
            seg = new SimplifiedPath(workingTopo,linkPath);
            segments[pathIdx].add(seg);
        }

        /*
        logger.debug("Display " + segments.length + " list of segments");
        for (int i = 0; i < segments.length; i++) {
            List segment = segments[i];
            logger.debug("Segments " + i + " : ");
            for (int j = 0; j < segments[i].size(); j++) {
                logger.debug(((SimplifiedPath) segments[i].get(j)).toString() + " ");
            }
            System.out.println("");
        } */


        SimplifiedPath allPath[] = constructPathFromSegments(segments,dst);
        return allPath;
    }

    /**
     * This method is used to reassemble the path from the segments.
     *
     * @param segments
     * @param dstNode
     * @return
     * @throws CreatePathException
     * @throws LinkNotFoundException
     */
    private SimplifiedPath[] constructPathFromSegments(List<SimplifiedPath> segments[],int dstNode) throws CreatePathException, LinkNotFoundException {
        SimplifiedPath goodPath[] = new SimplifiedPath[segments.length];
        ArrayList<Integer> allPath[] = new ArrayList[segments.length];
        boolean canNotFindPath;
        int segNodeDst;
        int currentPathOfSeg;
        List currentSegments = null;

        for (int pathIdx=0; pathIdx < segments.length; pathIdx++) {
            //logger.debug("compute path " + pathIdx);
            canNotFindPath = false;
            currentSegments = segments[pathIdx];
            currentPathOfSeg = pathIdx;

            ArrayList<Integer> linkIdPath = new ArrayList<Integer>();
            SimplifiedPath currentSeg = segments[pathIdx].get(0);
            for (int i = 0; i < currentSeg.getLinkIdPath().length; i++) {
                //logger.debug("add link " + currentSeg.getLinkIdPath()[i]);
                linkIdPath.add(new Integer(currentSeg.getLinkIdPath()[i]));
            }
            segNodeDst = topo.getLinkDst(currentSeg.getLinkIdPath()[currentSeg.getLinkIdPath().length - 1]);

            while ((segNodeDst!=dstNode) && (!canNotFindPath)) {
                canNotFindPath = false;
                if (currentPathOfSeg < (segments.length-1))
                    currentPathOfSeg++;
                else
                    currentPathOfSeg = 0;

                if (currentPathOfSeg == pathIdx)
                    throw new CreatePathException("ERROR impossible to find the next segments");
                currentSegments = segments[currentPathOfSeg];
                int i;
                for (i = 0; i < currentSegments.size(); i++) {
                    SimplifiedPath nextSeg = (SimplifiedPath) currentSegments.get(i);
                    if ((topo.getLinkSrc(nextSeg.getLinkIdPath()[0])) == segNodeDst) {
                        currentSeg = nextSeg;
                        segNodeDst = topo.getLinkDst(currentSeg.getLinkIdPath()[currentSeg.getLinkIdPath().length - 1]);
                        for (int j = 0; j < currentSeg.getLinkIdPath().length; j++) {
                            //logger.debug("add link " + currentSeg.getLinkIdPath()[j]);
                            linkIdPath.add(new Integer(currentSeg.getLinkIdPath()[j]));
                        }
                        break;
                    }
                }
            }
            allPath[pathIdx] = linkIdPath;
        }

        for (int i = 0; i < allPath.length; i++) {
            /*
            StringBuffer sb = new StringBuffer("computed path [");
            sb.append(i);
            sb.append(" : ");
            for (int j = 0; j < allPath[i].size(); j++) {
                sb.append(" ");
                sb.append(allPath[i].get(j));
            }
            sb.append(" ]");
            logger.debug(sb.toString());
            */
            goodPath[i] = new SimplifiedPath(topo,allPath[i]);
        }
        return goodPath;
    }

}
