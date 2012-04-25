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
package be.ac.ulg.montefiore.run.totem.visualtopo.graph;

import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.DirectedEdge;
import edu.uci.ics.jung.utils.UserData;
import edu.uci.ics.jung.utils.PredicateUtils;
import edu.uci.ics.jung.exceptions.ConstraintViolationException;

import java.util.Map;
import java.util.HashMap;

import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;

/*
* Changes:
* --------
* - 08-May-2006 : Use the new DirectedSparseGraph (with parallel edges enabled) and display information about constraints violation (GMO)
* - 18-Oct-2007 : Add domain instance and getDomain() method (GMO)
*/

/**
* Graph used by MyVisualizationViewer class.
* This class adds to the DirectedSparseGraph the possibility to be manipulated given the domain elements (Node, Link).
*
* <p>Creation date: 30 mars 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class DomainGraph extends DirectedSparseGraph {
    private Map<String, Vertex> vertices;
    private Map<String, Edge> edges;

    private final Domain domain;

    public DomainGraph(Domain domain) {
        super();
        this.domain = domain;
        vertices = new HashMap<String, Vertex>();
        edges = new HashMap<String, Edge>();
    }

    public Domain getDomain() {
        return domain;
    }

    public void addVertex(Node node) {
        DirectedSparseVertex vertex = new DirectedSparseVertex();
        vertex.setUserDatum(MyVisualizationViewer.TKEY, node, UserData.SHARED);  //link node and vertex
        vertices.put(node.getId(), vertex);
        addVertex(vertex);
    }

    public void addEdge(Link link) {
        DirectedEdge e = null;
        try {
            Vertex src = vertices.get(link.getSrcNode().getId());
            Vertex dst = vertices.get(link.getDstNode().getId());
            e = new DirectedSparseEdge(src, dst);
            e.setUserDatum(MyVisualizationViewer.TKEY, link, UserData.SHARED);  //link edge and topology link
            e.setUserDatum(MyVisualizationViewer.HIGHLIGHTKEY, new Boolean(false), UserData.SHARED);
            edges.put(link.getId(), e);
            addEdge(e);
        } catch (NodeNotFoundException ex) {
            ex.printStackTrace();
        } catch (ConstraintViolationException ex) {
            /* Display information about violated predicates */
            Map m = PredicateUtils.evaluateNestedPredicates(ex.getViolatedConstraint(), e);
            for (Object o : m.keySet()) {
                System.out.println(o + " : " + m.get(o));
            }
            throw ex;
        }
    }

    public void removeVertex(Node node) {
        //System.out.println("removing node: " + node.getId());
        Vertex v = vertices.remove(node.getId());
        removeVertex(v);
    }

    public void removeEdge(Link link) {
        //System.out.println("removing link: " + link.getId());
        Edge e = edges.remove(link.getId());
        removeEdge(e);
    }

    public void removeEdge(Edge e) {
        Link link = ((Link)e.getUserDatum(MyVisualizationViewer.TKEY));
        //System.out.println("removing edge, link:" + link.getId());
        edges.remove(link.getId());
        super.removeEdge(e);
    }

    public void removeVertex(Vertex v) {
        Node node = ((Node)v.getUserDatum(MyVisualizationViewer.TKEY));
        //System.out.println("removing vertex, node:" + node.getId());
        vertices.remove(node.getId());
        super.removeVertex(v);
    }

    public Edge getEdge(Link link) {
        return edges.get(link.getId());
    }

    public Vertex getVertex(Node node) {
        return vertices.get(node.getId());
    }
}
