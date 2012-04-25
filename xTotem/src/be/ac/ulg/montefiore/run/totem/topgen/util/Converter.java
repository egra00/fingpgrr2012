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

package be.ac.ulg.montefiore.run.totem.topgen.util;

//import from Brite_modified.jar
import Topology.Topology;
import Graph.Edge;
import Graph.GraphConstants;
import Graph.NodeConf;
import Graph.RouterNodeConf;

import be.ac.ulg.montefiore.run.totem.domain.exception.*;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.DomainImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.LinkImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.NodeImpl;

/*
 * Changes:
 * --------
 *
 */

/**
 * This class contains some methods to import topologies in different formats
 * into our schema-derived data structures (see domain package).
 *
 * <p>Creation date: 2004
 *
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class Converter {
    
    /**
     * The metric to use is the hop count.
     */
    public static final int METRIC_HOP_COUNT = 0;
    
    /**
     * The metric to use is the inverse of the bandwith.
     */
    public static final int METRIC_INV_BW = 1;
    
    /**
     * This method parses a <code>Topology</code> object that is in BRITE
     * format and returns a <code>Domain</code> object that contains the same
     * information.
     * @param topology The <code>Topology</code> object to be converted.
     * @param linksDirected True if you want that all the links of the topology
     * are directed. If a link is undirected, we create a new link so that 
     * there are two links with one in each direction.
     * @param metric Indicates which metric to use. See METRIC_* constants in
     * this class for more information.
     * @throws JAXBException If an error occurred during the conversion.
     * @throws IllegalArgumentException If <code>metric</code> is not valid.
     * @return A <code>Domain</code> object.
     */
    public static Domain briteTopologyToDomain(Topology topology, boolean linksDirected, int metric) throws NodeAlreadyExistException, LinkAlreadyExistException, NodeNotFoundException {
        Domain domain = new DomainImpl(0);
        
        domain.setDescription("This is a topology generated with BRITE."
                +" Model(s) information:"
                +topology.getModel().toString()
                +"See BRITE documentation for more"
                +" information.");
        
        Graph.Node[] nodes = topology.getGraph().getNodesArray();
        for(int i = 0; i < nodes.length; ++i) {
            domain.addNode(convertBriteNode(nodes[i], domain));
        }

        Edge[] links = topology.getGraph().getEdgesArray();
        /* firstFreeID designates the first free link ID (for link creation) */
        int firstFreeID = links.length;
        for(int i = 0; i < links.length; ++i) {
            Edge link = links[i];
            
            Link linkType = convertBriteLink(link, firstFreeID, false, false, domain);
            domain.addLink(linkType);
            addMetric(linkType, metric);

            boolean directed = (link.getDirection()==GraphConstants.DIRECTED ? true : false);
            if(linksDirected && !directed) {
                /* We have to add a link... */
                linkType = convertBriteLink(link, firstFreeID++, true, true, domain);
                domain.addLink(linkType);
                addMetric(linkType, metric);
            }
        }
        
        return domain;
    }
    
    /**
     * This method converts a Node from BRITE to our Node type.
     */
    private static Node convertBriteNode(Graph.Node node, Domain domain) {
        NodeConf nodeConf = node.getNodeConf();
        int corrAS;
        Node nodeType = new NodeImpl(domain, Integer.toString(node.getID()));
        /* Node type is always EDGE because BRITE doesn't generate real node types... */
        nodeType.setNodeType(Node.Type.EDGE);
        nodeType.setLongitude(nodeConf.getX());
        nodeType.setLatitude(nodeConf.getY());
        if((nodeConf instanceof RouterNodeConf) &&
                ((corrAS = ((RouterNodeConf) nodeConf).getCorrAS()) != -1)) {
            nodeType.setDescription("ASID = "+corrAS);
        }
        return nodeType;
    }
    
    /**
     * This method converts a Link from BRITE to our Link type.
     */
    private static Link convertBriteLink(Edge link, int firstFreeID, boolean reversed, boolean isNewLink, Domain domain) {
        Link linkType;
        if(isNewLink) {
            if(reversed) {
                linkType = new LinkImpl(domain, Integer.toString(firstFreeID), Integer.toString(link.getDst().getID()), Integer.toString(link.getSrc().getID()), (float)link.getBW());
            }
            else {
                linkType = new LinkImpl(domain, Integer.toString(firstFreeID), Integer.toString(link.getSrc().getID()), Integer.toString(link.getDst().getID()), (float)link.getBW());
            }
        }
        else {
            if(!reversed) {
                linkType = new LinkImpl(domain, Integer.toString(link.getID()), Integer.toString(link.getSrc().getID()), Integer.toString(link.getDst().getID()), (float)link.getBW());
            }
            else {
                linkType = new LinkImpl(domain, Integer.toString(link.getID()), Integer.toString(link.getDst().getID()), Integer.toString(link.getSrc().getID()), (float)link.getBW());
            }
        }
        linkType.setDelay((float) link.getDelay());
        try {
            linkType.setBandwidth((float) link.getBW());
        } catch (LinkCapacityExceededException e) {
            e.printStackTrace();
        } catch (DiffServConfigurationException e) {
            e.printStackTrace();
        }
        return linkType;
    }
    
    private static void addMetric(Link link, int metric) {
        switch(metric) {
        case METRIC_HOP_COUNT:
            link.setMetric(1.0f);
            link.setTEMetric(1.0f);
            break;
        case METRIC_INV_BW:
            link.setMetric(1/link.getBandwidth());
            link.setTEMetric(1/link.getBandwidth());
            break;
        default:
            throw new IllegalArgumentException("Metric "+metric+" not found !");
        }
    }
}
