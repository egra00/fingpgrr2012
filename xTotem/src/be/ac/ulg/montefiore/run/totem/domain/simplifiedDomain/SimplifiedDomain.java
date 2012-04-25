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
package be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain;

import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;

import java.util.List;
import java.util.ArrayList;

/*
* Changes:
* --------
*
*/

/**
 * A simple view of a domain topology. A SimplifiedDomain can be used by algorithms
 * that needs a very simple view of the topology (nodes and links). The SimplifiedDomain
 * is more optimised that Domain and so can be more efficient for complex computing algorithm.
 *
 * <p>Creation date: 13-Jan-2005 10:37:16
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author  Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class SimplifiedDomain {

    private String name;
    private int nbNodes;
    private int nbLinks;
    private List<Link> connectivity[][];
    private Link links[];

    public SimplifiedDomain() {
        name = "";
    }

    /**
     * Simple constructor
     *
     * @param name
     * @param nbNodes number of nodes
     * @param maxLinks number of links
     */
    public SimplifiedDomain(String name, int nbNodes, int maxLinks) {
        this.name = name;
        this.nbNodes = nbNodes;
        connectivity = new ArrayList[nbNodes][nbNodes];
        for (int i=0;i < nbNodes; i++) {
            for (int j=0;j < nbNodes; j++) {
                connectivity[i][j] = null;
            }
        }
        this.nbLinks = 0;
        links = new Link[maxLinks];
        for (int i = 0; i < links.length; i++) {
            links[i] = null;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Link[] getLinks() {
        return links;
    }

    public List<Link> getConnectivity(int srcNode, int dstNode) {
        return connectivity[srcNode][dstNode];
    }

    /**
     * Add a link
     *
     * @param srcNode source node
     * @param dstNode destination node
     * @param capacity capacity of the link
     * @param weight weight of the link
     * @param delay delay of the link
     */
    public void addLink(int linkId, int srcNode, int dstNode, float capacity, float weight, float delay) {
        Link link = new Link(linkId,srcNode,dstNode,capacity,weight,delay);
        if (connectivity[srcNode][dstNode] == null) {
            connectivity[srcNode][dstNode] = new ArrayList<Link>();
        }
        connectivity[srcNode][dstNode].add(link);
        links[linkId] = link;
        ++nbLinks;
    }

    public void removeInOutLinks(int nodeId) throws LinkNotFoundException {
        for (int i=0; i < links.length; i++) {
            if ((links[i].getSrcNode() == nodeId) | (links[i].getDstNode() == nodeId)) {
                removeLink(i);
            }
        }
    }

    public void removeLink(int linkId) throws LinkNotFoundException {
        Link link = links[linkId];
        if (link == null) {
            throw new LinkNotFoundException("Link " + linkId + " not found");
        }
        List<Link> linkBetweenNode = connectivity[link.srcNode][link.dstNode];
        int i;
        boolean find = false;
        for (i=0; i < linkBetweenNode.size(); i++) {
            if (linkBetweenNode.get(i).getId() == linkId) {
                linkBetweenNode.remove(i);
                find = true;
                break;
            }
        }
        if (!find) {
            System.out.println("Error in removeLink : the link does not exist");
        } else {
            if (linkBetweenNode.size() == 0)
                connectivity[link.srcNode][link.dstNode] = null;
        }
        links[linkId] = null;
        nbLinks--;
    }

    /**
     * Retun the number of nodes
     * @return the number of nodes
     */
    public int getNbNodes() {
        return nbNodes;
    }

    /**
     * Retun the number of links
     * @return the number of links
     */
    public int getNbLinks() {
        return nbLinks;
    }

    /**
     * Get the source node of the link
     * @param linkId ID of the link
     * @return node ID of the source
     */
    public int getLinkSrc(int linkId) throws LinkNotFoundException {
        if (links[linkId] == null) {
            throw new LinkNotFoundException("Link " + linkId + " not found");
        }
        return links[linkId].srcNode;
    }

    /**
     * Get the destination node of the link
     * @param linkId ID of the link
     * @return node ID of the destination
     */
    public int getLinkDst(int linkId) throws LinkNotFoundException {
        if (links[linkId] == null) {
            throw new LinkNotFoundException("Link " + linkId + " not found");
        }
        return links[linkId].dstNode;
    }

    /**
     * Get the capacity of the link
     * @param linkId ID of the link
     * @return link capacity
     */
    public float getLinkCapacity(int linkId) throws LinkNotFoundException {
        if (links[linkId] == null) {
            throw new LinkNotFoundException("Link " + linkId + " not found");
        }
        return links[linkId].capacity;
    }

    /**
     * Get the weight of the link
     * @param linkId ID of the link
     * @return link weight
     */
    public float getLinkWeight(int linkId) throws LinkNotFoundException {
        if (links[linkId] == null) {
            throw new LinkNotFoundException("Link " + linkId + " not found");
        }
        return links[linkId].weight;
    }
    /**
     * Set the link weight
     *
     * @param linkId
     * @param weight
     * @return
     */
    public float setLinkWeight(int linkId,float weight) throws LinkNotFoundException {
        if (links[linkId] == null) {
            throw new LinkNotFoundException("Link " + linkId + " not found");
        }
        return links[linkId].weight = weight;
    }

    /**
     * Get the weight of the link
     * @param linkId ID of the link
     * @return link weight
     */
    public float getLinkDelay(int linkId) throws LinkNotFoundException {
        if (links[linkId] == null) {
            throw new LinkNotFoundException("Link " + linkId + " not found");
        }
        return links[linkId].delay;
    }

    /**
     * As the nodeId are not necessarely consecutive, this method checks if the nodeId is a node
     * by cheking that the node has out links.
     *
     * @param nodeId
     * @return false is the node has no outLinks and true otherwise
     */
    public boolean isNode(int nodeId) {
        if ((nodeId < 0) || (nodeId >= nbNodes))
            return false;

        // find the number of out links
        int nbOutLinks = 0;
        for (int i = 0; i < connectivity[nodeId].length; i++) {
            if (connectivity[nodeId][i] != null) {
                nbOutLinks += connectivity[nodeId][i].size();
            }
        }
        return ((nbOutLinks == 0) ? false : true);
    }

    public boolean isLink(int linkId) {
        if ((linkId < 0) || (linkId >= nbLinks))
            return false;
        if (links[linkId] == null)
            return false;
        return true;
    }


    /**
     * Get all the output links of a node
     *
     * @param nodeId ID of the node
     * @return a array of the output link ID
     */
    public int[] getOutLinks(int nodeId) throws NodeNotFoundException {

        if (nodeId >= nbNodes)
            throw new NodeNotFoundException();

        // find the number of out links
        int nbOutLinks = 0;
        for (int i = 0; i < connectivity[nodeId].length; i++) {
            if (connectivity[nodeId][i] != null) {
                nbOutLinks += connectivity[nodeId][i].size();
            }
        }

        // copy the list of out links
        int outLinks[] = new int[nbOutLinks];
        int idxOutLinks = 0;
        for (int i = 0; i < connectivity[nodeId].length; i++) {
            if (connectivity[nodeId][i] != null) {
                for (int j = 0; j < connectivity[nodeId][i].size(); j++) {
                    outLinks[idxOutLinks] = connectivity[nodeId][i].get(j).getId();
                    idxOutLinks++;
                }
            }
        }
        return outLinks;
    }

    /**
     * Get all the input links of a node
     *
     * @param nodeId ID of the node
     * @return a array of the input link ID
     */
    public int[] getInLinks(int nodeId) throws NodeNotFoundException {
        // find the number of in links
        if (nodeId >= nbNodes)
            throw new NodeNotFoundException();

        int nbInLinks = 0;
        for (int i = 0; i < nbNodes; i++) {
            if (connectivity[i][nodeId] != null) {
                nbInLinks += connectivity[i][nodeId].size();
            }
        }

        // copy the list of out links
        int inLinks[] = new int[nbInLinks];
        int idxInLinks = 0;
        for (int i = 0; i < nbNodes; i++) {
            if (connectivity[i][nodeId] != null) {
                for (int j = 0; j < connectivity[i][nodeId].size(); j++) {
                    inLinks[idxInLinks] = connectivity[i][nodeId].get(j).getId();
                    idxInLinks++;
                }
            }
        }
        return inLinks;
    }

    public Object clone() throws CloneNotSupportedException {
        if (name == null) {
            name = "";
        }
        SimplifiedDomain resultTopo  = new SimplifiedDomain(new String(name),nbNodes,links.length);
        for (int linkId = 0; linkId < links.length; linkId++) {
            if (links[linkId] != null)
                resultTopo.addLink(linkId,links[linkId].srcNode,links[linkId].dstNode,links[linkId].capacity,links[linkId].weight,links[linkId].delay);
        }
        return resultTopo;
    }

    // DEBUG
    public void display() {
        System.out.println("The topology consists of " + nbNodes + " nodes and " + nbLinks + " links");
        System.out.println("Id\tSrc\tdst\tcapa\tweight");
        for (int i = 0; i < links.length; i++) {
            Link link = links[i];
            if (link != null)
                System.out.println(i + "\t" + link.srcNode + "\t" + link.dstNode + "\t" + link.capacity + "\t" + link.weight);
        }
    }

    public int[] getNodesPath(int linksPath[]) throws LinkNotFoundException {
        int nodesPath[] = new int[linksPath.length + 1];
        if (links[linksPath[0]] == null) {
            throw new LinkNotFoundException("Link " + linksPath[0] + " not found");
        }
        nodesPath[0] = links[linksPath[0]].getSrcNode();
        for (int i=0; i < linksPath.length; i++) {
            if (links[linksPath[i]] == null) {
                throw new LinkNotFoundException("Link " + linksPath[i] + " not found");
            }
            nodesPath[i + 1] = links[linksPath[i]].getDstNode();
        }
        return nodesPath;
    }

    /**
     * Contains the link information
     */
    public class Link {
        private int id;
        private int srcNode;
        private int dstNode;
        private float capacity;
        private float weight;
        private float delay;

        public Link(int id, int srcNode, int dstNode, float capacity, float weight,float delay) {
            this.srcNode = srcNode;
            this.dstNode = dstNode;
            this.capacity = capacity;
            this.weight = weight;
            this.delay = delay;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        public int getSrcNode() {
            return srcNode;
        }

        public int getDstNode() {
            return dstNode;
        }

        public float getCapacity() {
            return capacity;
        }

        public float getMetric() {
            return weight;
        }

        public void setMetric(float weight) {
            this.weight = weight;
        }

        public float getDelay() {
            return delay;
        }

        public boolean equals(Link link) {
            if ((link.id != id) || (link.srcNode != srcNode) || (link.dstNode != dstNode) || (link.capacity != capacity)
                    || (link.weight != weight) || (link.delay != delay))
                return false;
            return true;
        }
    }
}
