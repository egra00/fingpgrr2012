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

import be.ac.ulg.montefiore.run.totem.repository.allDistinctRoutes.AllDistinctRoutesException;
import be.ac.ulg.montefiore.run.totem.repository.allDistinctRoutes.AllDistinctRoutes;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedPath;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;

import java.util.*;

/**
 * Description
 * <p/>
 * Creation date : 05-Jan-2005 16:29:17
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class PairDisjointPathCPLGenerator {

    public static CandidatePathList generate(PairDisjointPathCPLGeneratorParameter params) throws AllDistinctRoutesException, LinkNotFoundException, NodeNotFoundException {
        SimplifiedDomain topo = params.getTopo();
        PairDisjointPathCPL cpl = new PairDisjointPathCPL(topo.getNbNodes());

        long time = System.currentTimeMillis();
        AllDistinctRoutes allRoutes = new AllDistinctRoutes();
        allRoutes.computeAllDistinctRoute(topo,params.getMaxDepth());
        int nbPath = 0;
        int minNbPath = params.getNbMaxPath();
        for (int srcIdx = 0; srcIdx < topo.getNbNodes(); srcIdx++) {
            for (int dstIdx = 0; dstIdx <topo.getNbNodes(); dstIdx++) {
                if (srcIdx != dstIdx) {
                    List<SimplifiedPath> allPath = allRoutes.getAllDistinctRoutes(srcIdx,dstIdx);
                    int allPathSize = allPath.size();

                    ArrayList<PairDisjointPathCP> cpArray = new ArrayList<PairDisjointPathCP>();
                    for (int pathIdx = 0; pathIdx < allPathSize; pathIdx++) {
                        SimplifiedPath primaryPath = allPath.get(pathIdx);
                        for (int backupIdx = pathIdx+1; backupIdx < allPathSize; backupIdx++) {
                            if (pathIdx != backupIdx) {
                                SimplifiedPath backupPath = allPath.get(backupIdx);
                                if (primaryPath.isDisjoint(backupPath)) {
                                    PairDisjointPathCP cp = new PairDisjointPathCP(primaryPath,backupPath);
                                    cpArray.add(cp);
                                }
                            }
                        }
                    }
                    PairDisjointPathCP cpAr[] = new PairDisjointPathCP[cpArray.size()];
                    cpAr = cpArray.toArray(cpAr);
                    Arrays.sort(cpAr,new PDPCPComparator());

                    List<PairDisjointPathCP> pl = new ArrayList<PairDisjointPathCP>();
                    int maxNbPath = Math.min(params.getNbMaxPath(),cpAr.length);
                    nbPath += maxNbPath;
                    if (maxNbPath < minNbPath)
                        minNbPath = maxNbPath;
                    for (int i=0; i < maxNbPath; i++) {
                        pl.add(cpAr[i]);
                    }

                    // Check that the pl list containts the smallest PairDisjointPathCP
                    for (int i=0; i < cpAr.length; i++) {
                        boolean present = false;
                        PairDisjointPathCP cp1 = cpAr[i];
                        for (int j = 0; j < pl.size(); j++) {
                            PairDisjointPathCP cp2 = (PairDisjointPathCP) pl.get(j);
                            if (cp1 == cp2) {
                                present = true;
                                break;
                            }
                        }
                        if (present == false) {
                            int sizeCP1 = cp1.getPrimary().getLinkIdPath().length + cp1.getBackup().getLinkIdPath().length;
                            for (int j = 0; j < pl.size(); j++) {
                                PairDisjointPathCP cp2 = (PairDisjointPathCP) pl.get(j);
                                int sizeCP2 = cp2.getPrimary().getLinkIdPath().length + cp2.getBackup().getLinkIdPath().length;
                                if (sizeCP2 > sizeCP1) {
                                    System.out.println("Error CP2 > CP1 : (" + srcIdx + "," + dstIdx + ")");
                                    System.out.println("CP1 = " + cp1.getPrimary().toString() + " " + cp1.getBackup().toString());
                                    System.out.println("CPL : ");
                                    for (int k = 0; k < pl.size(); k++) {
                                        PairDisjointPathCP currentCP = (PairDisjointPathCP) pl.get(k);
                                        System.out.println(currentCP.getPrimary().toString() + " " +  currentCP.getBackup().toString());
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
        //System.out.println("Generate PairDisjointPathCPL of " + nbPath + " paths and at least " + minNbPath + " paths in " + time + " ms");
        return cpl;
    }

}
