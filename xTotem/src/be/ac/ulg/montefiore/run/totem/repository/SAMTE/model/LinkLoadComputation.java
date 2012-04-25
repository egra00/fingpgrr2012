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

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedPath;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.Bhandari;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/*
* Changes:
* --------
*
*/

/**
 * <p>Creation date: 24-Feb-2005 10:27:12
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class LinkLoadComputation {

    private Logger logger = Logger.getLogger(LinkLoadComputation.class.getName());

    private SimplifiedPath shortestPath[][] = null;
    private List<Pair> useByDemand[] = null;

    private long timeToComputePath = 0;

    public long getTimeToComputePath() {
        return timeToComputePath;
    }

    public void resetTimeToComputePath() {
        timeToComputePath = 0;
    }

    public void init(SimplifiedDomain domain) throws Exception {
        shortestPath = new SimplifiedPath[domain.getNbNodes()][domain.getNbNodes()];
        Bhandari bhandari = new Bhandari();
        for(int srcNode = 0; srcNode < domain.getNbNodes(); srcNode++) {
            List<SimplifiedPath> allPathFromSource = bhandari.computeSPF(domain,srcNode);
            for (int i = 0; i < allPathFromSource.size(); i++) {
                SimplifiedPath simplifiedPath = allPathFromSource.get(i);
                int dstNode = domain.getLinkDst(simplifiedPath.getLinkIdPath()[simplifiedPath.getLinkIdPath().length-1]);
                shortestPath[srcNode][dstNode] = simplifiedPath;
            }
        }
        useByDemand = new ArrayList[domain.getNbNodes()];
        for (int i = 0; i < useByDemand.length; i++) {
            useByDemand[i] = new ArrayList<Pair>();

        }
        for(int srcNode = 0; srcNode < shortestPath.length; srcNode++) {
            for (int dstNode = 0; dstNode < shortestPath.length; dstNode++) {
                if(srcNode != dstNode) {
                    int linkPath[] = shortestPath[srcNode][dstNode].getLinkIdPath();
                    for (int i = 0; i < linkPath.length; i++) {
                        useByDemand[domain.getLinkSrc(linkPath[i])].add(new Pair(srcNode,dstNode));
                    }
                }
            }
        }
    }

    public float[] computeLinkLoad(Domain domain, TrafficMatrix tm, List<ExtendedLsp> lspList) throws Exception {
        float[] linkLoad = new float[domain.getUpLinks().size()];
        for (int i = 0; i < linkLoad.length; i++) {
            linkLoad[i] = 0;
        }
        HashMap<Node,List<ExtendedLsp>> ingressNodeLsps = new HashMap<Node,List<ExtendedLsp>>();
        Iterator<ExtendedLsp> itLsp = lspList.iterator();
        while (itLsp.hasNext()) {
            ExtendedLsp lsp = itLsp.next();
            List<ExtendedLsp> nodeLspList = ingressNodeLsps.get(lsp.getIngress());
            if (nodeLspList == null) {
                nodeLspList = new ArrayList<ExtendedLsp>();
                ingressNodeLsps.put(lsp.getIngress(),nodeLspList);
            }
            nodeLspList.add(lsp);
        }

        DomainConvertor convertor = domain.getConvertor();

        List<Node> nodeList = domain.getUpNodes();
        CSPF cspf = new CSPF();
        for(int srcNodeIdx=0;srcNodeIdx < nodeList.size();srcNodeIdx++) {
            for(int dstNodeIdx=0;dstNodeIdx < nodeList.size();dstNodeIdx++) {
                if (srcNodeIdx != dstNodeIdx) {
                    float bw = tm.get(srcNodeIdx,dstNodeIdx);
                    Node dstNode = nodeList.get(dstNodeIdx);
                    Path path = cspf.computeSPF(domain,nodeList.get(srcNodeIdx).getId(),dstNode.getId());
                    List<Node> nodePath = path.getNodePath();
                    int currentNodeIdx = 0;
                    Node currentNode = nodePath.get(currentNodeIdx);
                    while (!currentNode.equals(dstNode) && (currentNodeIdx < nodePath.size())) {
                        List<ExtendedLsp> nodeLspList = ingressNodeLsps.get(currentNode);
                        Node skipToNode = null;
                        if (nodeLspList != null) {
                            for (int i = 0; i < nodeLspList.size(); i++) {
                                if (nodeLspList.get(i).match(new TrafficDescriptor(dstNode))) {
                                    //logger.warn("Lsp " + nodeLspList.get(i).getId() + " match destination " + dstNode.getId() + " for demand from " + nodeList.get(srcNodeIdx).getId() + " to " + dstNode.getId());
                                    Iterator<Link> linkPath = nodeLspList.get(i).getLspPath().getLinkPath().iterator();
                                    while (linkPath.hasNext()) {
                                        Link l =  linkPath.next();
                                        linkLoad[convertor.getLinkId(l.getId())] += bw;
                                    }
                                    skipToNode = nodeLspList.get(i).getEgress();
                                    break;
                                }
                            }
                        }
                        if (skipToNode != null) {
                            currentNode = skipToNode;
                            currentNodeIdx = 0;
                            path = cspf.computeSPF(domain,currentNode.getId(),dstNode.getId());
                            nodePath = path.getNodePath();
                            //logger.warn("Compute new Path from " + currentNode.getId() + " to " + dstNode.getId() + " :" + path.toString());
                        } else {
                            currentNodeIdx++;
                            currentNode = nodePath.get(currentNodeIdx);
                            linkLoad[convertor.getLinkId(path.getLinkPath().get(currentNodeIdx-1).getId())] += bw;
                        }
                    }
                }
            }
        }
        return linkLoad;
    }

    public SimplifiedPath computePath(SimplifiedDomain domain,List<ExtendedPath> lspList,int srcNode,int dstNode) throws Exception {
        long time = System.currentTimeMillis();
        if (shortestPath == null) {
            init(domain);
        }
        int maxHop = 30;
        ArrayList<Integer> computedLinkIdPath = new ArrayList<Integer>();

        List<ExtendedPath> ingressNodeLsps[] = new List[domain.getNbNodes()];
        for (int i = 0; i < ingressNodeLsps.length; i++) {
            ingressNodeLsps[i] = null;
        }
        Iterator<ExtendedPath> itLsp = lspList.iterator();
        while (itLsp.hasNext()) {
            ExtendedPath lsp = itLsp.next();
            if (ingressNodeLsps[lsp.getIngress()] == null) {
                ingressNodeLsps[lsp.getIngress()] = new ArrayList<ExtendedPath>();
            }
            ingressNodeLsps[lsp.getIngress()].add(lsp);
        }

        SimplifiedPath simplifiedPath = shortestPath[srcNode][dstNode];
        if (simplifiedPath == null) {
            System.out.println("No path between " + srcNode + " and " + dstNode);
            System.exit(0);
        }


        int currentNodeIdx = 0;
        int currentNode = domain.getLinkSrc(simplifiedPath.getLinkIdPath()[currentNodeIdx]);
        int nbHop = 0;
        boolean findLoop = false;
        while ((!findLoop) && (currentNode != dstNode) && (currentNodeIdx <= simplifiedPath.getLinkIdPath().length) && (nbHop < maxHop)) {
            nbHop++;
            List<ExtendedPath> nodeLspList = ingressNodeLsps[currentNode];
            int skipToNode = -1;
            if (nodeLspList != null) {
                for (int j = 0; j < nodeLspList.size(); j++) {
                    if (nodeLspList.get(j).match(new TrafficDescriptor(dstNode))) {
                        int[] linkPath = nodeLspList.get(j).getLinkIdPath();
                        for (int k = 0; k < linkPath.length; k++) {
                            computedLinkIdPath.add(new Integer(linkPath[k]));
                        }
                        skipToNode = nodeLspList.get(j).getEgress();
                        break;
                    }
                }
            }
            if (skipToNode != -1) {
                currentNode = skipToNode;
                currentNodeIdx = 0;
                if (currentNode != dstNode) {
                    simplifiedPath = shortestPath[currentNode][dstNode];
                    if (simplifiedPath == null) {
                        System.out.println("After LSP No path between " + srcNode + " and " + dstNode);
                        System.exit(0);
                    }
                }
            } else {
                // Detect loop
                int nextNode = domain.getLinkDst(simplifiedPath.getLinkIdPath()[currentNodeIdx]);
                if ((computedLinkIdPath.size() > 0) && (domain.getLinkSrc(computedLinkIdPath.get(0).intValue()) == nextNode)) {
                    System.out.println("Find loop on link " + computedLinkIdPath.get(0).intValue());
                    findLoop = true;
                }
                for (int i = 0; i < computedLinkIdPath.size(); i++) {
                    if (domain.getLinkDst(computedLinkIdPath.get(i).intValue()) == nextNode) {
                        System.out.println("Find loop on link " + computedLinkIdPath.get(i).intValue());
                        findLoop = true;
                    }
                }

                if (!findLoop) {
                    computedLinkIdPath.add(new Integer(simplifiedPath.getLinkIdPath()[currentNodeIdx]));
                    currentNode = domain.getLinkDst(simplifiedPath.getLinkIdPath()[currentNodeIdx]);
                    currentNodeIdx++;
                }
            }
        }

        if (findLoop) {
            System.out.println("Lsp loop for traffic (" + srcNode + "," + dstNode + ") on node " + currentNode);
            return null;
        }

        if (nbHop == maxHop) {
            System.out.println("Lsp loop for traffic (" + srcNode + "," + dstNode + ") on node " + currentNode);
            System.exit(0);
        }
        timeToComputePath += System.currentTimeMillis() - time;
        return new SimplifiedPath(domain,computedLinkIdPath);
    }

    public float[] computeLinkLoad(SimplifiedDomain domain,TrafficMatrix tm, List<ExtendedPath> lspList) throws Exception {
        if (shortestPath == null) {
            init(domain);
        }

        float[] linkLoad = new float[domain.getNbLinks()];
        for (int i = 0; i < linkLoad.length; i++) {
            linkLoad[i] = 0;
        }
        for(int srcNode = 0; srcNode < domain.getNbNodes(); srcNode++) {
            for(int dstNode = 0; dstNode < domain.getNbNodes(); dstNode++) {
                if (dstNode != srcNode) {
                    float bw = tm.get(srcNode,dstNode);
                    SimplifiedPath path = computePath(domain,lspList,srcNode,dstNode);
                    if (path != null) {
                        int linkIdPath[] = path.getLinkIdPath();
                        for (int i = 0; i < linkIdPath.length; i++) {
                            linkLoad[linkIdPath[i]] += bw;
                        }
                    } else {
                        System.out.println("Find Loop for path between " + srcNode + " and " + dstNode);
                    }
                }
            }
        }
        for (int i = 0; i < linkLoad.length; i++) {
            linkLoad[i] = linkLoad[i] / domain.getLinkCapacity(i);
        }
        return linkLoad;
    }

    public void displayPathChange(SimplifiedDomain domain,TrafficMatrix tm, List<ExtendedPath> lspList) throws Exception {
        if (shortestPath == null) {
            init(domain);
        }

        for(int srcNode = 0; srcNode < domain.getNbNodes(); srcNode++) {
            for(int dstNode = 0; dstNode < domain.getNbNodes(); dstNode++) {
                if (dstNode != srcNode) {
                    SimplifiedPath path = computePath(domain,lspList,srcNode,dstNode);
                    if ((srcNode == 0) && (dstNode == 22)) {
                        System.out.println("Path for demand (" + srcNode + "," + dstNode + ") from " +
                                shortestPath[srcNode][dstNode].toString() + " to " +
                                path.toString());
                    }

                    if (!shortestPath[srcNode][dstNode].equals(path))
                        System.out.println("Path change for demand (" + srcNode + "," + dstNode + ") from " +
                                shortestPath[srcNode][dstNode].toString() + " to " +
                                path.toString());
                }
            }
        }
    }

    public void displayPath(SimplifiedDomain domain,TrafficMatrix tm, List<ExtendedPath> lspList) throws Exception {
        if (shortestPath == null) {
            init(domain);
        }

        for(int srcNode = 0; srcNode < domain.getNbNodes(); srcNode++) {
            for(int dstNode = 0; dstNode < domain.getNbNodes(); dstNode++) {
                if (dstNode != srcNode) {
                    SimplifiedPath path = computePath(domain,lspList,srcNode,dstNode);
                    System.out.println("Path for demand (" + srcNode + "," + dstNode + ") : " +
                                path.toString());
                }
            }
        }
    }

    public boolean isCreatingLoops(SimplifiedDomain domain,List<ExtendedPath> lspList,ExtendedPath newPath) throws Exception {
        for(int srcNode = 0; srcNode < domain.getNbNodes(); srcNode++) {
            for(int dstNode = 0; dstNode < domain.getNbNodes(); dstNode++) {
                if (dstNode != srcNode) {
                    SimplifiedPath path = computePath(domain,lspList,srcNode,dstNode);
                    if (path == null) {
                        System.out.println("Find Loop for path between " + srcNode + " and " + dstNode);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private class Pair {

        int src;
        int dst;

        public Pair(int src, int dst) {
            this.src = src;
            this.dst = dst;
        }

    }

}
