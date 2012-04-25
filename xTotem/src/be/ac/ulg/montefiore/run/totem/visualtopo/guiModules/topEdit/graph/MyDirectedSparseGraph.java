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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.graph;

import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.utils.UserData;

import java.util.Collection;
import java.util.Set;
import java.util.HashMap;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.NodeFactory;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.LinkFactory;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.*;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.guiComponents.LinkPropertiesDialog;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.guiComponents.NodePropertiesDialog;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.LinkIgp;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Link;

import javax.swing.*;

/*
* Changes:
* --------
* - 23-Nov-2007: fix bug when renaming nodes (GMO)
*/

/**
* Directed Sparse graph that allow parallel links. Contains methods to create nodes and links and to edit them thanks
* to a dialog.
*
* <p>Creation date: 3/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class MyDirectedSparseGraph extends SparseGraph implements DirectedGraph {
    /**
     * Main key: used in vertex to refer to the corresponding node instance.
     * Used in edges to refer to the corresponding linkDecorator instance
     */
    final public static Object KEY = "KEY";

    private DomainDecorator domainDecorator;

    private NodeFactory nodeFactory;
    private LinkFactory linkFactory;

    private MyStaticLayout layout;

	/**
	 * Creates an instance of a sparse directed graph.
	 */
	public MyDirectedSparseGraph(DomainDecorator domainDecorator, NodeFactory nodeFactory, LinkFactory linkFactory) {
		super();
        Collection edge_predicates =
            getEdgeConstraints();
        edge_predicates.add(DIRECTED_EDGE);

        this.domainDecorator = domainDecorator;
        this.nodeFactory = nodeFactory;
        this.linkFactory = linkFactory;

        initVertices();
        initEdges();
	}

    public LinkFactory getLinkFactory() {
        return linkFactory;
    }

    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    /**
     * Sets the layout to update when a node location is set in the dialog.
     * @param layout
     */
    public void setLayout(MyStaticLayout layout) {
        this.layout = layout;
    }

    /**
     * Add all vertices to the graph
     */
    private void initVertices() {
        Domain domain = domainDecorator.getDomain();

        for (Object o : domain.getTopology().getNodes().getNode()) {
            Node n = (Node) o;
            DirectedSparseVertex v = new DirectedSparseVertex();
            v.addUserDatum(KEY, n, UserData.SHARED);
            
            super.addVertex(v);
        }
    }

    /**
     * Add all edges to the graph
     */
    private void initEdges() {
        Domain domain = domainDecorator.getDomain();
        if (domain.isSetTopology() && domain.getTopology().isSetLinks()) {
            for (Object o : domain.getTopology().getLinks().getLink()) {
                Link l = (Link) o;
                LinkDecorator linkDecorator = domainDecorator.getLink(l.getId());
                String src = l.getFrom().getNode();
                String dst = l.getTo().getNode();
                Edge newEdge = new DirectedSparseEdge(getVertex(src), getVertex(dst));
                newEdge.addUserDatum(KEY, linkDecorator, UserData.SHARED);
                super.addEdge(newEdge);
            }
        }
    }

    public DomainDecorator getDomainDecorator() {
        return domainDecorator;
    }

    private Vertex getVertex(String id) {
        for (Vertex v : (Set<Vertex>)getVertices()) {
            Node n = (Node)v.getUserDatum(KEY);
            if (n.getId().equals(id)) {
                return v;
            }
        }
        return null;
    }

    public Edge addEdge(final Edge e) {
        final LinkDecorator l = (LinkDecorator)e.getUserDatum(KEY);
        domainDecorator.addLink(l);
        super.addEdge(e);
        return e;
    }

    public Vertex addVertex(final Vertex v) {
        final Node n = (Node)v.getUserDatum(KEY);
        domainDecorator.addNode(n);
        super.addVertex(v);
        return v;
    }

    public Edge createEdge(Vertex src, Vertex dst) {
        String srcStr = ((Node) src.getUserDatum(KEY)).getId();
        String dstStr = ((Node) dst.getUserDatum(KEY)).getId();

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("src", srcStr);
        params.put("dst", dstStr);
        final LinkDecorator l = linkFactory.createDefaultObject(params);

        final Edge e = new DirectedSparseEdge(src, dst);
        e.addUserDatum(KEY, l, UserData.SHARED);

        return e;
    }

    public Vertex createVertex() {
        final Node n = nodeFactory.createDefaultObject(null);
        final DirectedSparseVertex v = new DirectedSparseVertex();
        v.addUserDatum(KEY, n, UserData.SHARED);

        return v;
    }

    public Edge editEdge(Edge e) {
        final LinkDecorator l = (LinkDecorator)e.getUserDatum(KEY);
        final LinkIgp linkIgp = l.getLinkIgp();

        JDialog dialog = new LinkPropertiesDialog(domainDecorator, l) {
            protected void postProcessingOnSuccess() {
                if (linkIgp == null) {
                    // the link hadn't an igp part
                    domainDecorator.addLinkIgp(l.getLinkIgp());
                } else if (l.getLinkIgp() == null) {
                    // the link had an igp but was removed
                    domainDecorator.removeLinkIgp(linkIgp);
                }
            }
        };
        dialog.pack();
        dialog.setLocationRelativeTo(dialog.getParent());
        dialog.setVisible(true);

        return e;
    }

    public Vertex editVertex(final Vertex v) {
        final Node n = (Node)v.getUserDatum(KEY);
        final String oldId = n.getId();

        JDialog dialog = new NodePropertiesDialog(n, domainDecorator) {
            protected void postProcessingOnSuccess() {
                domainDecorator.renameNode(oldId, n.getId());
                if (n.isSetLocation()) {
                    layout.moveToLocation(v, n.getLocation().getLongitude(), n.getLocation().getLatitude());
                }
            }
        };
        dialog.pack();
        dialog.setLocationRelativeTo(dialog.getParent());
        dialog.setVisible(true);
        return v;
    }

    public void removeEdge(Edge e) {
        LinkDecorator l = (LinkDecorator)e.removeUserDatum(KEY);
        super.removeEdge(e);
        domainDecorator.removeLink(l);
    }

    public void removeVertex(Vertex v) {
        Node n = (Node)v.removeUserDatum(KEY);
        super.removeVertex(v);
        domainDecorator.removeNode(n);
    }

}
