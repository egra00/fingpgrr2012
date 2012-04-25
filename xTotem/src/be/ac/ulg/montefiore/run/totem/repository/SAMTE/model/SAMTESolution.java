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
package be.ac.ulg.montefiore.run.totem.repository.SAMTE.model;

import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedPath;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.ObjectiveFunction;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SASolution;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;

import java.util.ArrayList;
import java.util.List;

/*
* Changes:
* --------
*
*/

/**
 * <p>Creation date: 25-Feb-2005 12:20:23
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class SAMTESolution  implements SASolution {

    private ObjectiveFunction of;
    private List<ExtendedPath> lspList;
    private SimplifiedDomain sDomain;
    private TrafficMatrix tm;
    private double cost;
    private boolean isInitialized;
    private boolean isEvaluated;
    private double[] linkLoad;
    private long timeToManageLSPList;

    private SimplifiedPath RP[][];  // RP[s][d] : get the real path between s and d
    private List<Integer> PUN[][];  // PUN[i][d] : get the list of source node that cross the
                                    // ingress node i for reaching destination node d
    private LinkLoadComputation llc;

    public SAMTESolution(ObjectiveFunction of, TrafficMatrix tm, SimplifiedDomain sDomain) {
        this.tm = tm;
        this.sDomain = sDomain;
        this.of = of;
        lspList = new ArrayList<ExtendedPath>();
        isEvaluated = false;
        cost = Double.MAX_VALUE;
        RP = new SimplifiedPath[sDomain.getNbNodes()][sDomain.getNbNodes()];
        linkLoad = new double[sDomain.getNbLinks()];
        timeToManageLSPList = 0;
        PUN = new List[sDomain.getNbNodes()][sDomain.getNbNodes()];
        for (int ingress = 0; ingress < PUN.length; ingress++) {
            for (int dst = 0; dst < PUN.length; dst++) {
                PUN[ingress][dst] = new ArrayList<Integer>();
            }
        }
        isInitialized = false;
        llc = new LinkLoadComputation();
    }

    public LinkLoadComputation getLlc() {
        return llc;
    }

    public SimplifiedDomain getDomain() {
        return sDomain;
    }

    public TrafficMatrix getTm() {
        return tm;
    }

    public long getTimeToManageLSPList() {
        return timeToManageLSPList;
    }

    public void resetTime() {
        timeToManageLSPList = 0;
    }

    public boolean initStructure() throws Exception {
        lspList.clear();
        resetLinkLoad();
        resetPUN();
        // init RP, update PUN and LinkLoad
        for(int srcNode = 0; srcNode < sDomain.getNbNodes(); srcNode++) {
            for(int dstNode = 0; dstNode < sDomain.getNbNodes(); dstNode++) {
                if (dstNode != srcNode) {
                    SimplifiedPath path = llc.computePath(sDomain,lspList,srcNode,dstNode);
                    if (path == null) {
                        System.out.println("Find Loop for path between " + srcNode + " and " + dstNode + " when init RP");
                        return false;
                    } else {
                        float bw = tm.get(srcNode,dstNode);
                        // Add new path to PUN
                        List<Integer> srcNodeList;
                        int linkIdPath[] = path.getLinkIdPath();
                        for (int i = 0; i < linkIdPath.length; i++) {
                            srcNodeList = PUN[sDomain.getLinkSrc(linkIdPath[i])][dstNode];
                            srcNodeList.add(new Integer(srcNode));
                        }
                        // Add path to LinkLoad
                        linkIdPath = path.getLinkIdPath();
                        for (int i = 0; i < linkIdPath.length; i++) {
                            linkLoad[linkIdPath[i]] += bw;
                        }
                        RP[srcNode][dstNode] = path;
                    }
                }
            }
        }
        isInitialized = true;
        return true;
    }

    private void resetPUN() {
        for (int ingress = 0; ingress < PUN.length; ingress++) {
            for (int dst = 0; dst < PUN.length; dst++) {
                PUN[ingress][dst].clear();
            }
        }
    }

    private void resetLinkLoad() {
        for (int i = 0; i < linkLoad.length; i++) {
            linkLoad[i] = 0;
        }
    }

    public List<ExtendedPath> getLspList() {
        return lspList;
    }

    public boolean removeLsp(int number) throws Exception {
        long time = System.currentTimeMillis();
        ExtendedPath ePath = lspList.get(number);
        if (ePath == null) {
            throw new LspNotFoundException("LSP " + number + " not found in LSPList");
        }
        lspList.remove(number);
        int dstNode = ((IntDstNodeFEC) ePath.getFec()).getDstNode();
        List<Integer> changingDemand = PUN[ePath.getIngress()][dstNode];
        int affectedDemand[] = new int[changingDemand.size()];
        for (int i = 0; i < affectedDemand.length; i++) {
            affectedDemand[i] = changingDemand.get(i).intValue();
        }
        for (int i = 0; i < affectedDemand.length; i++) {
            int srcNode = affectedDemand[i];
            SimplifiedPath path = llc.computePath(sDomain,lspList,srcNode,dstNode);
            if (path == null) {
                lspList.add(ePath);
                System.out.println("Find Loop for path between " + srcNode + " and " + dstNode + " when removing LSP " + ePath.toString());
                return false;
            } else {
                float bw = tm.get(srcNode,dstNode);
                updatePUN(srcNode,dstNode,RP[srcNode][dstNode],path);
                updateLinkLoad(bw,RP[srcNode][dstNode],path);
                RP[srcNode][dstNode] = path;
            }
        }
        /*
        resetLinkLoad();
        for(int srcNode = 0; srcNode < sDomain.getNbNodes(); srcNode++) {
            for(int dstNodeIdx = 0; dstNodeIdx < sDomain.getNbNodes(); dstNodeIdx++) {
                if (dstNodeIdx != srcNode) {
                    SimplifiedPath path = LinkLoadComputation.computePath(sDomain,lspList,srcNode,dstNodeIdx);
                    if (path == null) {
                        lspList.remove(ePath);
                        System.out.println("Find Loop for path between " + srcNode + " and " + dstNodeIdx);
                        return false;
                    }
                    float bw = tm.get(srcNode,dstNodeIdx);
                    int linkIdPath[] = path.getLinkIdPath();
                    for (int i = 0; i < linkIdPath.length; i++) {
                        linkLoad[linkIdPath[i]] += bw;
                    }
                }
            }
        }
        */
        isEvaluated = false;
        timeToManageLSPList += System.currentTimeMillis() - time;
        return true;
    }

    public boolean removeLsp(ExtendedPath path) throws Exception{
        for (int i = 0; i < lspList.size(); i++) {
            if (lspList.get(i).equals(path)) {
                return removeLsp(i);
            }
        }
        return false;
    }

    public double[] getLinkLoad() {
        return (double[]) linkLoad;
    }

    public double[] getRelativeLinkLoad() {
        double use[] = new double[linkLoad.length];
        try {
            for (int i = 0; i < use.length; i++) {
                use[i] = (double) linkLoad[i] / (double) sDomain.getLinkCapacity(i);
            }
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
        }
        return use;
    }

    public SimplifiedPath getPath(int srcNode, int dstNode) {
        return RP[srcNode][dstNode];
    }

    private void updatePUN(int src, int dst, SimplifiedPath oldPath, SimplifiedPath newPath) throws LinkNotFoundException {
        List<Integer> srcNodeList;
        //Remove old path from PUN
        int linkIdPath[] = oldPath.getLinkIdPath();
        for (int i = 0; i < linkIdPath.length; i++) {
            srcNodeList = PUN[sDomain.getLinkSrc(linkIdPath[i])][dst];
            srcNodeList.remove(new Integer(src));
        }
        // Add new path to PUN
        linkIdPath = newPath.getLinkIdPath();
        for (int i = 0; i < linkIdPath.length; i++) {
            srcNodeList = PUN[sDomain.getLinkSrc(linkIdPath[i])][dst];
            srcNodeList.add(new Integer(src));
        }
    }

    private void updateLinkLoad(float bw, SimplifiedPath oldPath, SimplifiedPath newPath) {
        //Remove old path from LinkLoad
        int linkIdPath[] = oldPath.getLinkIdPath();
        for (int i = 0; i < linkIdPath.length; i++) {
            linkLoad[linkIdPath[i]] -= bw;
        }

        // Add new path to LinkLoad
        linkIdPath = newPath.getLinkIdPath();
        for (int i = 0; i < linkIdPath.length; i++) {
            linkLoad[linkIdPath[i]] += bw;
        }

    }

    public boolean addLsp(ExtendedPath ePath) throws Exception {
        //checkPUNConsistency();
        long time = System.currentTimeMillis();
        if (!isInitialized) {
            //System.out.println("Add LSP - initilized structure");
            if (!initStructure())
                return false;
            isInitialized = true;
        }
        lspList.add(ePath);
        int dstNode = ((IntDstNodeFEC) ePath.getFec()).getDstNode();
        List<Integer> changingDemand = PUN[ePath.getIngress()][dstNode];
        int affectedDemand[] = new int[changingDemand.size()];
        for (int i = 0; i < affectedDemand.length; i++) {
            affectedDemand[i] = changingDemand.get(i).intValue();
        }
        /*System.out.println("Add LSP at ingress " + ePath.getIngress() + " : " + ePath.toString());
        System.out.print("PUN["+ePath.getIngress()+"]["+dstNode + "] :");
        for (int i = 0; i < affectedDemand.length; i++) {
            System.out.print(" " + affectedDemand[i]);
        }
        System.out.println("");
        */
        for (int idx = 0; idx < affectedDemand.length; idx++) {
            int srcNodeIdx = affectedDemand[idx];
            //System.out.println("Update path using ingress " + ePath.getIngress() + " for demand ("+ srcNodeIdx +"," +dstNode+")");
            SimplifiedPath path = llc.computePath(sDomain,lspList,srcNodeIdx,dstNode);
            if (path == null) {
                lspList.remove(ePath);
                System.out.println("Find Loop for path between " + srcNodeIdx + " and " + dstNode + " when adding LSP " + ePath.toString());
                return false;
            } else {
                float bw = tm.get(srcNodeIdx,dstNode);
                updatePUN(srcNodeIdx,dstNode,RP[srcNodeIdx][dstNode],path);
                updateLinkLoad(bw,RP[srcNodeIdx][dstNode],path);
                RP[srcNodeIdx][dstNode] = path;
            }
        }
        //checkPUNConsistency();
        //checkConsistency();

        isEvaluated = false;
        time = System.currentTimeMillis() - time;
        timeToManageLSPList += time;
        //System.out.println("Add LSP in a list of " + lspList.size() + " LSP takes " + time);
        return true;
    }

    /**
     * Evaluate the solution using the objective function
     *
     * @return the score of the solution
     */
    public double evaluate() {
        if (!isEvaluated)
            cost = of.evaluate(this);
        isEvaluated = true;
        return cost;
    }

    /**
     * Display a solution. Use for debug
     */
    public void display() {
        System.out.println("Solution : ");
        for (int i = 0; i < lspList.size(); i++) {
            try {
                System.out.println("\tLSP " + i + " (" + lspList.get(i).getIngress() +","+ lspList.get(i).getEgress() + ") - FEC : " + lspList.get(i).getFec().toString() + " - " + lspList.get(i));
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void displayFull() throws Exception {
        System.out.println("All Path");
        for(int srcNode = 0; srcNode < sDomain.getNbNodes(); srcNode++) {
            for(int dstNodeIdx = 0; dstNodeIdx < sDomain.getNbNodes(); dstNodeIdx++) {
                if (dstNodeIdx != srcNode) {
                    SimplifiedPath path = llc.computePath(sDomain,lspList,srcNode,dstNodeIdx);
                    if (path == null) {
                        System.out.println("Find Loop for path between " + srcNode + " and " + dstNodeIdx);
                    }
                    System.out.println("Path ("+ srcNode + "," + dstNodeIdx + ") : " + path.toString());
                }
            }
        }

        System.out.println("PUN :");
        for(int ingressNode = 0; ingressNode < sDomain.getNbNodes(); ingressNode++) {
            for(int dstNodeIdx = 0; dstNodeIdx < sDomain.getNbNodes(); dstNodeIdx++) {
                if (dstNodeIdx != ingressNode) {
                    List<Integer> l = PUN[ingressNode][dstNodeIdx];
                    System.out.print("PUN["+ingressNode+"]["+dstNodeIdx+"] :");
                    for (int i = 0; i < l.size(); i++) {
                        System.out.print(" " + l.get(i));
                    }
                    System.out.println("");
                }
            }
        }
    }

    /**
     * Get the objective function
     *
     * @return
     */
    public ObjectiveFunction getObjectiveFunction() {
        return of;
    }

    /**
     * Clone deeply a solution
     *
     * @return
     */
    public Object clone() {
        SAMTESolution newSol = new SAMTESolution(of,tm,sDomain);
        List<ExtendedPath> newLspList = new ArrayList<ExtendedPath>();
        for (int i = 0; i < lspList.size(); i++) {
            newLspList.add((ExtendedPath) lspList.get(i).clone());
        }
        try {
            newSol.lspList = newLspList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Init PUN
        for (int ingress = 0; ingress < PUN.length; ingress++) {
            for (int dst = 0; dst < PUN.length; dst++) {
                List<Integer> PUNList = PUN[ingress][dst];
                for (int i = 0; i < PUNList.size(); i++) {
                    newSol.PUN[ingress][dst].add(new Integer(PUNList.get(i).intValue()));
                }
            }
        }

        // Init LinkLoad
        for (int i = 0; i < linkLoad.length; i++) {
            newSol.linkLoad[i] = linkLoad[i];
        }

        // Init RP
        for(int srcNode = 0; srcNode < sDomain.getNbNodes(); srcNode++) {
            for(int dstNode = 0; dstNode < sDomain.getNbNodes(); dstNode++) {
                if (dstNode != srcNode) {
                    int[] linkIdPath = RP[srcNode][dstNode].getLinkIdPath();
                    int[] newLinkId = new int[linkIdPath.length];
                    for (int i = 0; i < newLinkId.length; i++) {
                        newLinkId[i] = linkIdPath[i];
                    }
                    SimplifiedPath newPath = new SimplifiedPath(sDomain,newLinkId);
                    newSol.RP[srcNode][dstNode] = newPath;
                }
            }
        }
        newSol.isInitialized = true;
        newSol.llc = llc;
        return newSol;
    }
    /*
    public boolean checkPUNConsistency() throws Exception {
        for(int srcNode = 0; srcNode < sDomain.getNbNodes(); srcNode++) {
            for(int dstNodeIdx = 0; dstNodeIdx < sDomain.getNbNodes(); dstNodeIdx++) {
                if (dstNodeIdx != srcNode) {
                    SimplifiedPath path = LinkLoadComputation.computePath(sDomain,lspList,srcNode,dstNodeIdx);
                    if (path == null) {
                        System.out.println("Find Loop for path between " + srcNode + " and " + dstNodeIdx);
                        return false;
                    }
                    int linkIdPath[] = path.getLinkIdPath();
                    for (int i = 0; i < linkIdPath.length; i++) {
                        int ingressNode = sDomain.getLinkSrc(linkIdPath[i]);
                        List<Integer> l = PUN[ingressNode][dstNodeIdx];
                        boolean present = false;
                        for (int j = 0; j < l.size(); j++) {
                            if (l.get(j).intValue() == srcNode)
                                present =true;
                        }
                        if (!present) {
                            System.out.println("Pair ("+srcNode+","+dstNodeIdx+") " + path.toString()
                                    + " not in PUN for ingress" + ingressNode + " (link " + linkIdPath[i] + ")");
                        }
                    }
                }
            }
        }
        return true;
    }

    public boolean checkConsistency() throws Exception {
        boolean result = true;
        resetLinkLoad();
        for(int srcNode = 0; srcNode < sDomain.getNbNodes(); srcNode++) {
            for(int dstNodeIdx = 0; dstNodeIdx < sDomain.getNbNodes(); dstNodeIdx++) {
                if (dstNodeIdx != srcNode) {
                    SimplifiedPath path = LinkLoadComputation.computePath(sDomain,lspList,srcNode,dstNodeIdx);
                    if (path == null) {
                        System.out.println("Find Loop for path between " + srcNode + " and " + dstNodeIdx);
                        return false;
                    }
                    if (!path.equals(RP[srcNode][dstNodeIdx])) {
                        System.out.println("Path different for ("+srcNode+","+dstNodeIdx+") : "
                                + path.toString() + " - " + RP[srcNode][dstNodeIdx]);
                        result = false;
                    }
                    float bw = tm.get(srcNode,dstNodeIdx);
                    int linkIdPath[] = path.getLinkIdPath();
                    for (int i = 0; i < linkIdPath.length; i++) {
                        linkLoad[linkIdPath[i]] += bw;
                    }
                }
            }
        }

        return result;
    }
    */
    private static class Pair {

        int src;
        int dst;

        public Pair(int src, int dst) {
            this.src = src;
            this.dst = dst;
        }

    }
}
