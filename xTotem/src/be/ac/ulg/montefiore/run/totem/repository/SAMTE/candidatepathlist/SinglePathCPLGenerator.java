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
package be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist;

import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedPath;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.repository.allDistinctRoutes.AllDistinctRoutes;
import be.ac.ulg.montefiore.run.totem.repository.allDistinctRoutes.AllDistinctRoutesException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
 * <p>Creation date: 25-Feb-2005 10:00:29
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class SinglePathCPLGenerator {

    private static final Logger logger = Logger.getLogger(SinglePathCPLGenerator.class);

    public SinglePathCPL generate(SinglePathCPLGeneratorParameter params, boolean verbose) throws LinkNotFoundException, NodeNotFoundException, AllDistinctRoutesException {
        SimplifiedDomain topo = params.getTopo();
        SinglePathCPL cpl = new SinglePathCPL(topo.getNbNodes());

        long time = System.currentTimeMillis();
        AllDistinctRoutes allRoutes = new AllDistinctRoutes();
        allRoutes.computeAllDistinctRoute(topo,params.getMaxDepth(),params.getNbMaxPath(),verbose);
        long time1 = System.currentTimeMillis() - time;
        logger.debug("Compute all routes takes " + time1);
        if (verbose)
            System.out.println("Compute all routes takes " + time1 + " ms");
        int nbPath = 0;
        int minNbPath = params.getNbMaxPath();
        for (int srcIdx = 0; srcIdx < topo.getNbNodes(); srcIdx++) {
            for (int dstIdx = 0; dstIdx <topo.getNbNodes(); dstIdx++) {
                if (srcIdx != dstIdx) {
                    List<SimplifiedPath> allPath = allRoutes.getAllDistinctRoutes(srcIdx,dstIdx);
                    int allPathSize = allPath.size();

                    ArrayList<SinglePathCP> cpArray = new ArrayList<SinglePathCP>();
                    for (int pathIdx = 0; pathIdx < allPathSize; pathIdx++) {
                        SimplifiedPath primaryPath = allPath.get(pathIdx);
                        SinglePathCP cp = new SinglePathCP(primaryPath);
                        cpArray.add(cp);
                    }
                    SinglePathCP cpAr[] = new SinglePathCP[cpArray.size()];
                    cpAr = cpArray.toArray(cpAr);
                    Arrays.sort(cpAr,new SinglePathCPComparator());

                    List<SinglePathCP> pl = new ArrayList<SinglePathCP>();
                    int maxNbPath = Math.min(params.getNbMaxPath(),cpAr.length);
                    nbPath += maxNbPath;
                    if (maxNbPath < minNbPath)
                        minNbPath = maxNbPath;
                    for (int i=0; i < maxNbPath; i++) {
                        pl.add(cpAr[i]);
                    }

                    // Check that the pl list containts the smallest SinglePathCP
                    for (int i=0; i < cpAr.length; i++) {
                        boolean present = false;
                        SinglePathCP cp1 = cpAr[i];
                        for (int j = 0; j < pl.size(); j++) {
                            SinglePathCP cp2 = pl.get(j);
                            if (cp1 == cp2) {
                                present = true;
                                break;
                            }
                        }
                        if (present == false) {
                            int sizeCP1 = cp1.getPath().getLinkIdPath().length;
                            for (int j = 0; j < pl.size(); j++) {
                                SinglePathCP cp2 = pl.get(j);
                                int sizeCP2 = cp2.getPath().getLinkIdPath().length;
                                if (sizeCP2 > sizeCP1) {
                                    System.out.println("Error CP2 > CP1 : (" + srcIdx + "," + dstIdx + ")");
                                    System.out.println("CP1 = " + cp1.getPath().toString());
                                    System.out.println("CPL : ");
                                    for (int k = 0; k < pl.size(); k++) {
                                        SinglePathCP currentCP = pl.get(k);
                                        System.out.println(currentCP.getPath().toString());
                                    }
                                }
                            }
                        }

                    }

                    cpl.setPath(srcIdx,dstIdx,pl);
                }
            }
        }
        time = System.currentTimeMillis() - time;
        logger.debug("Generate PairDisjointPathCPL of " + nbPath + " paths and at least " + minNbPath + " paths in " + time + " ms");
        return cpl;
    }

    public class SinglePathCPComparator implements Comparator<SinglePathCP> {

        public int compare(SinglePathCP sp1, SinglePathCP sp2) {
            int length = sp1.getPath().getLinkIdPath().length;
            int length1 = sp2.getPath().getLinkIdPath().length;
            if (length < length1) {
                return -1;
            } else if (length > length1) {
                return 1;
            } else {
                return 0;
            }
        }
    }

}
