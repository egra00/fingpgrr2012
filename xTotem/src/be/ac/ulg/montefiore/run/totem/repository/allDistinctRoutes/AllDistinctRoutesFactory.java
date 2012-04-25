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

import java.util.List;
import java.util.ArrayList;
import java.io.*;
import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 *
 */

/**
 * This factory can save and load and AllDistinctRoutes object in a text file.
 * This experimental code is used to increase the simulation time of algorithms.
 *
 * <p>Creation date: 1-Jan-2004
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class AllDistinctRoutesFactory {

    private static Logger logger = Logger.getLogger(AllDistinctRoutesFactory.class.getName());

    /**
     * Load all the distinct routes from a file
     *
     * @param routes
     * @param fileName
     * @throws IOException
     * @throws AllDistinctRoutesException
     */
    public static void loadAllDistinctRoutes(AllDistinctRoutes routes, String fileName) throws IOException, AllDistinctRoutesException {
        long time = System.currentTimeMillis();
        FileReader fr = new FileReader(fileName);
        BufferedReader br = new BufferedReader(fr);
        String line = null;
        int lineIdx = 0;
        int pos = 0;
        int nbTotalRoute = 0;
        while ((line = br.readLine()) != null) {
            lineIdx++;
            if ((pos = line.lastIndexOf("MAX_DEPTH")) != -1) {
                String result[] = line.split("\\s");
                for (int i = 0; i < result.length; i++) {
                    String s = result[i];
                    if (s.equals(":")) {
                        routes.maxDepth = (Integer.decode(result[i+1])).intValue();
                    }
                }
            }

            if ((pos = line.lastIndexOf("#")) == -1) {
                String result[] = line.split("\\s");
                if (result.length < 3)
                    throw new AllDistinctRoutesException("Syntax error in the all distinct routes files " + fileName + " in line " + lineIdx);
                int srcNode, dstNode;
                srcNode = (Integer.decode(result[0])).intValue();
                dstNode = (Integer.decode(result[1])).intValue();
                int[] p = new int[result.length - 2];
                for (int i = 2; i < result.length; i++) {
                    p[i-2] = (Integer.decode(result[i])).intValue();
                }
                SimplifiedPath path = new SimplifiedPath(routes.domain,p);
                if ((srcNode < 0) && (srcNode >= routes.domain.getNbNodes()))
                    throw new AllDistinctRoutesException("Source node in line " + lineIdx + " not present in the topology");
                if ((dstNode < 0) && (dstNode >= routes.domain.getNbNodes()))
                    throw new AllDistinctRoutesException("Destination node in line " + lineIdx + " not present in the topology");
                if (routes.routes[srcNode][dstNode] == null)
                    routes.routes[srcNode][dstNode] = new ArrayList<SimplifiedPath>();
                routes.routes[srcNode][dstNode].add(path);
                nbTotalRoute++;
            }
        }
        logger.info("Load " + nbTotalRoute + " routes from the file " + fileName + " in " + (System.currentTimeMillis() - time)  + " ms");
    }

    /**
     * Save all the distinct routes in a file
     *
     * @param routes
     * @param fileName
     * @throws IOException
     * @throws AllDistinctRoutesException
     */
    public static void saveAllDistinctRoutes(AllDistinctRoutes routes, String fileName) throws IOException, AllDistinctRoutesException {
        long time = System.currentTimeMillis();
        FileWriter fw = new FileWriter(fileName);
        BufferedWriter br = new BufferedWriter(fw);
        StringBuffer title = new StringBuffer("# All distinct routes file\n");
        title.append("# Topology : ");
        title.append(routes.domain.getName());
        title.append("\n");
        title.append("# NB_NODES : ");
        title.append(routes.domain.getNbNodes());
        title.append("\n");
        title.append("# MAX_DEPTH : ");
        title.append(routes.maxDepth);
        title.append("\n");
        title.append("# SrcNode DstNode     list of links Id\n");
        br.write(title.toString());
        StringBuffer routeString = new StringBuffer();
        int nbTotalRoute = 0;
        for(int srcNode=0;srcNode < routes.domain.getNbNodes();srcNode++) {
            for(int dstNode=0;dstNode < routes.domain.getNbNodes();dstNode++) {
                if (dstNode != srcNode) {
                    List<SimplifiedPath> pairRoutes = routes.getAllDistinctRoutes(srcNode,dstNode);
                    if (pairRoutes != null) {
                        for (int routeIdx=0;routeIdx < pairRoutes.size();routeIdx++) {
                            SimplifiedPath route = pairRoutes.get(routeIdx);
                            routeString.delete(0,routeString.length());
                            routeString.append(srcNode);
                            routeString.append(" ");
                            routeString.append(dstNode);
                            routeString.append("\t");
                            for(int linkIdx=0;linkIdx < route.getLinkIdPath().length; linkIdx++) {
                                int linkId = route.getLinkIdPath()[linkIdx];
                                routeString.append(linkId);
                                routeString.append(" ");
                            }
                            routeString.append("\n");
                            br.write(routeString.toString());
                            br.flush();
                            nbTotalRoute++;
                        }
                    } else {
                        System.out.println("No route between node " + srcNode + " and " + dstNode);
                    }

                }
            }
        }
        logger.info("Save " + nbTotalRoute + " routes in the file " + fileName + " in " + (System.currentTimeMillis() - time) + " ms");
        br.close();
        fw.close();
    }
}
