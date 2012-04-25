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
package be.ac.ulg.montefiore.run.totem.domain.model.impl;

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Igp;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.LinkIgp;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.repository.model.SPF;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;

import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;

import org._3pq.jgrapht.UndirectedGraph;
import org._3pq.jgrapht.graph.Multigraph;
import org._3pq.jgrapht.alg.ConnectivityInspector;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;

/*
 * Changes:
 * --------
 *  3-Feb-05: Merge forceIGPInfo and forceDiffServ (FS)
 *  1-Jun-2005: add isEqualCostMultiPath method (FSK)
 *  14-Nov-2005: change isNoMultiGraph into isMultiGraph (GMO)
 *  08-May-2006: bugfix in forceNoMultiGraph (GMO)
 *  27-Apr-2007: bugfix: replace Node.getInLink() by Node.getAllInLink() (GMO)
 *  13-Jun-2007: Use isSet..() methods in forceIGPInfo() (GMO)
 *  21-Jun-2007: add getEqualCostMultiPath(.) with algo parameter (GMO)
 *  02-Oct-2007: fix bug in forceIGPInfo when there is no link in the domain (GMO)
 *  31-Oct-2007: fix bug in forceDuplexConnected() (GMO)
 */

/**
 * A DomainValidator can check and force some characteristics on a Domain
 *
 * <p>Creation date: 19-Jan-2005 18:40:51
 *
 * @author  Simon Balon (balon@run.montefiore.ulg.ac.be)
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class DomainValidatorImpl implements DomainValidator {
    private static Logger logger = Logger.getLogger(DomainValidator.class.getName());

    private DomainImpl domain;
    private UndirectedGraph domainGraph;

    public DomainValidatorImpl(Domain domain) {
        this.domain = (DomainImpl) domain;
        domainGraph = null;
    }

    /**
     * Verify if an IGP information section is present
     * @return true if the section is present and false otherwise
     */
    public boolean isIGPInfo() {
        Igp igp = domain.getIgp();
        return (igp != null);
    }

    /**
     * Checks if the graph is connected.
     * @return true if the graph is connected and false if not.
     */
    public boolean isConnected() throws NodeNotFoundException {
        UndirectedGraph topoGraph = buildGraphFromDomain();
        ConnectivityInspector cinsp = new ConnectivityInspector(topoGraph);
        // cinsp.

        return cinsp.isGraphConnected();
    }

    /**
     * This method return true if the domain contains NO loop links.
     */
    public boolean isLoopFree() throws NodeNotFoundException {
        List<Link> allLinks = domain.getAllLinks();

        for (int i=0; i < allLinks.size(); i++) {
            Link link = allLinks.get(i);
            if (link.getSrcNode().getId().equals(link.getDstNode().getId())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks that all the nodes of the topology have at least an outlink and an inlink.
     * If it's not the case, it will add a link that is the "inverse" of an existing link.
     * This method throws an <code>IllegalArgumentException</code> if the topology isn't connected.
     */
    public boolean isDuplexConnected() throws NodeNotFoundException, LinkNotFoundException {
        Link[][] tab = new Link[domain.getNbNodes()][2];
        List<Link> allLinks = domain.getAllLinks();

        DomainConvertor convertor = domain.getConvertor();

        for (int i=0; i < allLinks.size(); i++) {
            Link link = allLinks.get(i);
            tab[convertor.getLinkId(link.getSrcNode().getId())][0] = link;
            tab[convertor.getLinkId(link.getDstNode().getId())][1] = link;
        }

        for (int i=0; i < tab.length; i++) {
            if (tab[i][0] == null) {
                if (tab[i][1] == null)
                    throw new IllegalArgumentException("Topology not connected !");
                return false;
            }
            else if (tab[i][1] == null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Check if there exist at least two link coming into and going out of each node.
     *
     * @return true if there exist at least two link coming into and going out of each node and false otherwise
     */
    public boolean isDualConnected() throws NodeNotFoundException {
        List<Node> allNodes = domain.getAllNodes();

        for (int i=0; i < allNodes.size(); i++) {
            Node currentNode = allNodes.get(i);
            if ((currentNode.getAllInLink().size() < 2) || (currentNode.getAllOutLink().size() < 2))
                return false;
        }
        return true;
    }

    /**
     * Check if multiple links exists
     *
     * @return true if multiple links exists and false otherwise
     */
    public boolean isMultiGraph() throws NodeNotFoundException {
        List<Link> allLinks = domain.getAllLinks();

        for (int i=0; i < allLinks.size(); i++) {
            Link link1 = allLinks.get(i);
            for (int j=0; j < allLinks.size(); j++) {
                Link link2 = allLinks.get(j);
                if ((i != j) && (link1.getSrcNode().getId().equals(link2.getSrcNode().getId())) &&
                        (link1.getDstNode().getId().equals(link2.getDstNode().getId()))) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if there exists equal cost multi path in the topology
     *
     * @return true if there is equal cost multi path and false otherwise
     */
    public boolean isEqualCostMultiPath() throws NoRouteToHostException, RoutingException {
        List<Node> nodeList = domain.getAllNodes();
        SPF spf = new CSPF();
        boolean result = false;

        for (int i = 0; i < nodeList.size(); i++) {
            Node srcNode = (Node) nodeList.get(i);
            for (int j = 0;j < nodeList.size(); j++) {
                Node dstNode = (Node) nodeList.get(j);
                if (srcNode != dstNode) {
                    List<Path> paths = spf.computeSPF(domain,srcNode.getId(),dstNode.getId(),true);
                    if (paths.size() > 1) {
                        System.out.println("Multiple path between " + srcNode.getId() + " and " + dstNode.getId());
                        for (int k = 0; k < paths.size(); k++) {
                            System.out.println(paths.get(k).toString());
                        }
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    public List<List<Path>> getEqualCostMultiPath() throws NoRouteToHostException, RoutingException {
        try {
            return getEqualCostMultiPath("CSPF");
        } catch (NoSuchAlgorithmException e) {
            logger.fatal("CSPF not found.");
        }
        return null;
    }

    public List<List<Path>> getEqualCostMultiPath(String algo) throws NoRouteToHostException, RoutingException, NoSuchAlgorithmException {
        SPF spf;
        try {
            RepositoryManager.getInstance().startAlgo(algo, null, domain.getASID());
        } catch (AlgorithmInitialisationException e) {
        }
        spf = (SPF)RepositoryManager.getInstance().getAlgo(algo, domain.getASID());

        List<Node> nodeList = domain.getAllNodes();

        List<List<Path>> ret = new ArrayList<List<Path>>();
        for (int i = 0; i < nodeList.size(); i++) {
            Node srcNode = (Node) nodeList.get(i);
            for (int j = 0;j < nodeList.size(); j++) {
                Node dstNode = (Node) nodeList.get(j);
                if (srcNode != dstNode) {
                    List<Path> paths = spf.computeSPF(domain,srcNode.getId(),dstNode.getId(),true);
                    if (paths.size() > 1) {
                        List<Path> l  = new ArrayList<Path>();
                        for (int k = 0; k < paths.size(); k++) {
                            l.add(paths.get(k));
                        }
                        ret.add(l);
                    }
                }
            }
        }
        return ret;

    }


    /**
     * Adds the IGP information if not present.
     *
     * Create the IGP link if not present
     * Create the static section if not present.
     * If not define, the maximum bandwidth (mbw) and maximum reservable
     * bandwidth (mrbw) are set to the link capacity and 155000 otherwise (VERY DANGEROUS).
     * If not define, the metric (and TE metric) is the inverse of the capacity if present and 1 otherwise.
     * Create the dynamic section if not present and set the value of the rbw for each priority to the
     * maximum reservable bandwidth
     *
     */
    public void forceIGPInfo() {
        float bwValue = 155000;
        ObjectFactory factory = new ObjectFactory();

        if (domain.getNbLinks() != 0) {
            if (domain.getIgp() == null) {
                try {
                    domain.setIgp(factory.createIgp());
                    logger.debug("Create IGP section");
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }

            if (domain.getIgp().getLinks() == null) {
                try {
                    domain.getIgp().setLinks(factory.createIgpIgpLinksType());
                    logger.debug("Create IGP links section");
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }

            List<Link> allLinks = domain.getAllLinks();
            for (int i = 0; i < allLinks.size(); i++) {
                LinkImpl link = (LinkImpl) allLinks.get(i);
                LinkIgp linkIgp = null;
                Iterator<LinkIgp> linkIgpIt = domain.getIgp().getLinks().getLink().iterator();
                while (linkIgpIt.hasNext()) {
                    LinkIgp linkIgpTmp = linkIgpIt.next();
                    if (linkIgpTmp.getId().equals(link.getId())) {
                        linkIgp = linkIgpTmp;
                        break;
                    }
                }
                if (linkIgp == null) {
                    try {
                        linkIgp = factory.createLinkIgp();
                    } catch (JAXBException e) {
                        e.printStackTrace();
                    }
                    domain.getIgp().getLinks().getLink().add(linkIgp);
                    linkIgp.setId(link.getId());
                    logger.debug("Create IGP Link for link " + link.getId());
                }
                // Create static section if not present
                if (linkIgp.getStatic() == null) {
                    LinkIgp.StaticType staticType = null;
                    try {
                        staticType = factory.createLinkIgpStaticType();
                    } catch (JAXBException e) {
                        e.printStackTrace();
                    }
                    linkIgp.setStatic(staticType);
                    logger.debug("Create static section for link " + link.getId());
                }
                float bw = (!link.isSetBw()) ? bwValue : link.getBw();
                if (!linkIgp.getStatic().isSetMbw())
                    linkIgp.getStatic().setMbw(bw);
                if (!linkIgp.getStatic().isSetMrbw())
                    linkIgp.getStatic().setMrbw(bw);
                if (!linkIgp.getStatic().isSetMetric()) {
                    float metric = bw == 0 ? 100000000f / 0.01f : 100000000f / bw;
                    linkIgp.getStatic().setMetric(metric);
                }
                if (!linkIgp.getStatic().isSetTeMetric()) {
                    linkIgp.getStatic().setTeMetric(1);
                }
            }
        }
    }

    /**
     * Removes nodes and links that are not in the connected set of maximum size so that the graph is connected.
     */
    public void forceConnected() throws NodeNotFoundException, LinkNotFoundException {
        UndirectedGraph topoGraph = buildGraphFromDomain();  // some redundancy
        ConnectivityInspector cinsp = new ConnectivityInspector(topoGraph);

        List connectedsets = cinsp.connectedSets();

        int max = 0;
        int cs = 0;
        for (int i=0;i<connectedsets.size();i++){
            Set connectedSet = (Set) connectedsets.get(0);
            if (connectedSet.size() > cs){
                max = i;
                cs = connectedSet.size();
            }
        }

        // we've got the max connected set of maximum size
        // remove the other nodes (still to deal with the links...)
        Set connectedSet = (Set) connectedsets.get(max);

        Set verticesSet = topoGraph.vertexSet();
        Object[] verticesArray = verticesSet.toArray();

        List<Link> allLinks = domain.getAllLinks();
        for (int i=0;i<verticesArray.length;i++){
            if (connectedSet.contains(verticesArray[i])==false){

                // Remove the node if it is not in the max connected set
                Node node = domain.getNode((String)verticesArray[i]);
                domain.removeNode(node);

                // Also remove all the incoming or outcoming links
                for (int j=0; j < allLinks.size(); j++) {
                    Link currentLink = allLinks.get(j);
                    if ((currentLink.getSrcNode().getId().equals(node.getId())) || (currentLink.getDstNode().getId().equals(node.getId()))) {
                        domain.removeLink(currentLink);
                    }
                }
            }
        }

    }

    /**
     * This method removes all the loops present in the topology.
     * A loop is a link with the source node that is the same as the destination node.
     */
    public void forceLoopFree() throws NodeNotFoundException, LinkNotFoundException {
        List<Link> allLinks = domain.getAllLinks();

        for (int i=0; i < allLinks.size(); i++) {
            Link link = allLinks.get(i);
            if (link.getSrcNode().getId().equals(link.getDstNode().getId())) {
                domain.removeLink(link);
            }
        }
    }

    /**
     * Checks that all the nodes of the topology have at least an outlink and an inlink.
     * If it's not the case, it will add a link that is the "inverse" of an existing link.
     * This method throws an <code>IllegalArgumentException</code> if the topology isn't connected.
     */
    public void forceDuplexConnected() throws NodeNotFoundException, LinkNotFoundException, LinkAlreadyExistException {
        DomainConvertor convertor = domain.getConvertor();

        Link[][] tab = new Link[convertor.getMaxNodeId()][2];
        List<Link> allLinks = domain.getAllLinks();

        for (int i=0; i < allLinks.size(); i++) {
            Link link = allLinks.get(i);
            tab[convertor.getNodeId(link.getSrcNode().getId())][0] = link;
            tab[convertor.getNodeId(link.getDstNode().getId())][1] = link;
        }

        for (int i=0; i < tab.length; i++) {
            if (tab[i] == null) continue;
            if (tab[i][0] == null) {
                if (tab[i][1] == null)
                    throw new IllegalArgumentException("Topology not connected !");

                Node source = tab[i][1].getDstNode();
                Node destination = tab[i][1].getSrcNode();
                float bandwidth = tab[i][1].getBandwidth();

                // Create a link from the node source to the destination node of bandwidth banwdidth
                Link link = new LinkImpl(this.domain, source.getId() + "->" + destination.getId(),source.getId(), destination.getId(), bandwidth);
                domain.addLink(link);

            }
            else if (tab[i][1] == null) {
                Node source = tab[i][0].getDstNode();
                Node destination = tab[i][0].getSrcNode();
                float bandwidth = tab[i][0].getBandwidth();

                // Create a link from the node source to the destination node of bandwidth banwdidth
                Link link = new LinkImpl(this.domain, source.getId() + "->" + destination.getId(),source.getId(), destination.getId(), bandwidth);
                domain.addLink(link);
            }
        }
    }

    /**
     * Remove multiple links. If the metric of the multiple links are equals, we merge the
     * capacity of the two links.
     * If not, we remove the link with the largest metric.
     */
    public void forceNoMultiGraph() {
        List<Link> allLinks = domain.getAllLinks();

        for (int i=0; i < allLinks.size(); i++) {
            Link link1 = allLinks.get(i);
            for (int j=0; j < allLinks.size(); j++) {
                Link link2 = allLinks.get(j);
                try {
                    if ((i != j) && (link1.getSrcNode().getId().equals(link2.getSrcNode().getId())) &&
                            (link1.getDstNode().getId().equals(link2.getDstNode().getId()))) {
                        float w1 = link1.getMetric();
                        float w2 = link2.getMetric();

                        if (w1 == w2) {
                            float newBw = link1.getBandwidth() + link2.getBandwidth();

                            LinkImpl linkImpl1 = (LinkImpl) link1;
                            linkImpl1.setBw(newBw);

                            LinkIgp linkIgp1 = domain.getLinkIgp(link1.getId());

                            linkIgp1.getStatic().setMbw(newBw);
                            linkIgp1.getStatic().setMrbw(newBw);

                            // setting the bcs
                            // just dividing the new bandwidth between available CTs
                            List<LinkIgp.StaticType.DiffServType.BcType> bc = linkIgp1.getStatic().getDiffServ().getBc();
                            float rbw = newBw / (float) domain.getNbCT();
                            for (int k=0; k<bc.size(); k++){
                                bc.get(k).setValue(rbw);
                            }

                            // also setting dynamic part accordingly
                            List<LinkIgp.DynamicType.RbwType.PriorityType> priorityList = linkIgp1.getDynamic().getRbw().getPriority();

                            for (int k=0; k<priorityList.size(); k++){
                                priorityList.get(k).setValue(rbw);
                            }
                            logger.info("Merge link " + link1.getId() + " with link " + link2.getId() + " with capacity " + newBw);
                        }
                        if (w1 > w2) domain.removeLink(link1);
                        else domain.removeLink(link2);
                    }
                }
                catch(NodeNotFoundException e) {
                    logger.error("NodeNotFoundException in forceNoMultiGraph. Message: "+e.getMessage());
                }
                catch(LinkNotFoundException e) {
                    logger.error("LinkNotFoundException in forceNoMultiGraph. Message: "+e.getMessage());
                }
            }
        }
    }


    /**
     * Builds one graph that represents the domain.
     * @return the constructed graph.
     */
    private UndirectedGraph buildGraphFromDomain() throws NodeNotFoundException {
        if (domainGraph == null) {
            List<Link> allLinks = domain.getAllLinks();
            List<Node> allNodes = domain.getAllNodes();

            domainGraph = new Multigraph(); // non-simple (multiple edges between same vertex pair) undirected graph ( and with no loops)
            for (int i=0; i < allNodes.size(); i++) {
                String nodeId = allNodes.get(i).getId();
                domainGraph.addVertex(nodeId);
            }

            for (int i=0; i < allLinks.size(); i++) {
                Link link = allLinks.get(i);
                domainGraph.addEdge(link.getSrcNode().getId(), link.getDstNode().getId());
            }
        }
        return domainGraph;
    }

}
