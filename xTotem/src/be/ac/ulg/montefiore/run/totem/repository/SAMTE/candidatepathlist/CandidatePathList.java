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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Description
 * <p/>
 * Creation date : 05-Jan-2005 15:59:20
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class CandidatePathList {

    protected List<? extends CandidatePath> candidatePaths[][] = null;
    int nbPath;

    public CandidatePathList(int nbNodes) {
        candidatePaths = new ArrayList[nbNodes][nbNodes];
        for (int i = 0; i < candidatePaths.length; i++) {
            for (int j = 0; j < candidatePaths.length; j++) {
                candidatePaths[i][j] = null;
            }
        }
        nbPath = 0;
    }

    public int getNbNodes() {
        return candidatePaths.length;
    }

    public int size() {
        return nbPath;
    }

    public List<? extends CandidatePath> getPath(int src,int dst) {
        if ((src >= 0) && (src < candidatePaths.length)
                && (dst >= 0) && (candidatePaths[0]!=null) && (dst < candidatePaths[0].length)) {
            return candidatePaths[src][dst];
        } else {
            throw new IllegalArgumentException("Src " + src + " or dst " + dst + " node invalid");
        }
    }

    public void setPath(int src, int dst, List<? extends CandidatePath> cp) {
        if ((src >= 0) && (src < candidatePaths.length)
                && (dst >= 0) && (candidatePaths[0]!=null) && (dst < candidatePaths[0].length)) {
            candidatePaths[src][dst] = cp;
            nbPath += cp.size();
        } else {
            throw new IllegalArgumentException("Src " + src + " or dst " + dst + " node invalid");
        }
    }

    public void display() {
        for (int i = 0; i < candidatePaths.length; i++) {
            for (int j = 0; j < candidatePaths.length; j++) {
                if (i!=j) {
                    System.out.println("Paths between (" + i+ "," + j + ")");
                    Iterator<? extends CandidatePath> it = candidatePaths[i][j].iterator();
                    while (it.hasNext()) {
                        it.next().display();
                    }
                }
            }
        }
    }

    public void analyse() {
        analyse("null");
    }

    public void analyse(String threadId) {
        //nbPath = 0;
        int minNbPath = 0, minSrc = 0, minDst = 0;
        minNbPath = Integer.MAX_VALUE;
        for (int i = 0; i < candidatePaths.length; i++) {
            for (int j = 0; j < candidatePaths.length; j++) {
                if (i!=j) {
                    if (candidatePaths[i][j] == null) {
                        System.out.println("T " + threadId + " - No path for demand ("+i+","+j+")");
                    } else {
                        int currentSize = candidatePaths[i][j].size();
                        //nbPath += currentSize;
                        if (currentSize < minNbPath) {
                            minNbPath = currentSize;
                            minSrc = i;
                            minDst = j;
                        }
                    }
                }
            }
        }
        float meanNbPath = (float) nbPath / (float) ((candidatePaths.length * candidatePaths.length-1));
        System.out.println("T " + threadId + " - Candidate Path List of " + nbPath + " paths, mean path " + meanNbPath + " and at least " + minNbPath + " paths for demand (" + minSrc + "," + minDst + ")");
    }

}
