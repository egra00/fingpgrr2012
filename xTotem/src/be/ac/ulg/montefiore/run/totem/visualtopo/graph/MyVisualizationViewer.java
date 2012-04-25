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

import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.Renderer;
import edu.uci.ics.jung.visualization.control.*;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.visualization.contrib.CircleLayout;
import edu.uci.ics.jung.visualization.contrib.KKLayout;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.utils.UserData;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.PopupMenuFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import java.util.*;

/*
* Changes:
* --------
* - 30-Jun-2006: add size parameter to constructor. (GMO)
* - 30-Jun-2006: change graph manipulation possibilities using PluggableGraphMouse, implement PopupMenu as a plugin of
                PluggableGraphMouse, change updateLocation method to use realCoordinates when using LocalLayout,
                adapt ToolTipListener class to new version of Jung. (GMO)
* - 22-Dec-2006: remove unused code in hightlight(Lsp), add highlight(Link), made unHighlight() public (GMO)
* - 12-Feb-2007: adapt to jung 1.7.6 API (GMO)
* - 21-Jun-2007: add highlight(Path) (GMO)
* - 15-Jan-2008: now uses PopupMenuFactory (GMO)
*/

/**
 * Extension of the VisualizationViewer that shows the graph representation of a domain.
 * It listens to changes in the domain and adapt the graph in consequence (by using {@link DomainGraph} class).
 * <p/>
 * <p>Creation date: 30 mars 2006
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class MyVisualizationViewer extends VisualizationViewer implements DomainChangeListener {
    private boolean isOutDated = false;
    private DomainGraph g;
    private Domain domain;

    private java.util.List<Edge> highlightList = null;
    public static final Object TKEY = "TKEY";
    public static final Object HIGHLIGHTKEY = "HIGHLIGHTKEY";

    public MyVisualizationViewer(Layout layout, Renderer r, Domain domain, Dimension size) {
        super(layout, r, size);
        this.domain = domain;

        this.addComponentListener(new VisualizationListener(this));

        renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        setPickSupport(new RadiusPickSupport(this, 10));

        PluggableGraphMouse m = new PluggableGraphMouse();
        m.add(new MyPopupGraphMousePlugin());
        m.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON1_MASK | InputEvent.CTRL_MASK));
        m.add(new ScalingGraphMousePlugin(new LayoutScalingControl(), 0));
        m.add(new ScalingGraphMousePlugin(new ViewScalingControl(), InputEvent.CTRL_MASK));
        m.add(new MyPickingGraphMousePlugin());
        setGraphMouse(m);

        setBackground(Color.white);

        setToolTipListener(new GraphToolTipListener());
        g = (DomainGraph)layout.getGraph();
        startListening();

    }

    /**
     * Set up a new graph Layout
     */
    public void changeLayout(String s) {
        Layout layout;
        if (s.equals(MainWindow.CIRCLELAYOUT))
            layout = new CircleLayout(g);
        else if (s.equals(MainWindow.FRLAYOUT))
            layout = new FRLayout(g);
        else if (s.equals(MainWindow.KKLAYOUT))
            layout = new KKLayout(g);
        else if (s.equals(MainWindow.ISOMLAYOUT))
            layout = new ISOMLayout(g);
        else if (s.equals(MainWindow.LOCALLAYOUT))
            layout = new LocalLayout(g);
        else
            layout = new CircleLayout(g);

        //stop();
        setGraphLayout(layout);
    }

    public void highlight(Link link) {
        unHighlight();
        if (link == null) return;
        Edge e = ((DomainGraph)getGraphLayout().getGraph()).getEdge(link);
        if (e != null) {
            e.setUserDatum(HIGHLIGHTKEY, new Boolean(true), UserData.SHARED);
            highlightList.add(e);
        }
        repaint();
    }

    public void highlight(Path p) {
        unHighlight();
        if (p == null) return;
        java.util.List<Link> links = p.getLinkPath();
        for (Iterator iter = links.listIterator(); iter.hasNext();) {
            Link link = (Link) iter.next();
            Edge e = ((DomainGraph)getGraphLayout().getGraph()).getEdge(link);
            if (e != null) {
                e.setUserDatum(HIGHLIGHTKEY, new Boolean(true), UserData.SHARED);
                highlightList.add(e);

            }/* Removed on 22-Dec-2006: useless ?
                else {
                //get opposite Link.
                try {
                    Node source = link.getSrcNode();
                    Node target = link.getDstNode();
                    java.util.List inList = source.getInLink();
                    for (Iterator i = inList.listIterator(); i.hasNext();) {
                        Link ilink = (Link) i.next();
                        if (ilink.getSrcNode().equals(target)) {
                            e = ((DomainGraph)getGraphLayout().getGraph()).getEdge(ilink);
                            if (e != null) {
                                e.setUserDatum(HIGHLIGHTKEY, new Boolean(true), UserData.SHARED);
                                highlightList.add(e);

                            }
                        }
                    }
                } catch (Exception exp) {
                }
            }*/
        }
        this.repaint();
    }

    /**
     * highlight all edge contained in the Lsp given as parameter
     *
     * @param lsp an Lsp
     */
    public void highlight(Lsp lsp) {
        if (lsp == null) {
            unHighlight();
            return;
        }
        highlight(lsp.getLspPath());
    }


    /**
     * Remove highlighting from all previously highlighted links
     */
    public void unHighlight() {
        if (highlightList == null) {
            highlightList = new LinkedList<Edge>();
            return;
        }
        for (Iterator iter = highlightList.listIterator(); iter.hasNext();)
            ((DirectedEdge) iter.next()).setUserDatum(HIGHLIGHTKEY, new Boolean(false), UserData.SHARED);
        highlightList.clear();
        this.repaint();
    }

    /**
     * This method copy the current graph coordinates of the vertices to the internal topology
     * representation
     */
    public void updateLocation() {
        for (Iterator iter = getGraphLayout().getGraph().getVertices().iterator(); iter.hasNext();) {
            Vertex v = (Vertex) iter.next();
            Node node = (Node) v.getUserDatum(MyVisualizationViewer.TKEY);
            double longi, lat;
            if (getGraphLayout() instanceof LocalLayout) {
                LocalLayout l = (LocalLayout)getGraphLayout();
                longi = l.getRealCoordinates(v).getX();
                lat = l.getRealCoordinates(v).getY();
            } else {
                longi = getGraphLayout().getX(v);
                lat = getGraphLayout().getY(v);
            }
            node.setLongitude((float) longi);
            node.setLatitude((float) lat);
        }
    }

    /**
     * returns true if the current visualization may not reflect the domain
     * @return
     */
    public boolean isOutDated() {
        return isOutDated;
    }

    /**
     * Register this panel to listen to Domain events.
     */
    public void startListening() {
        domain.getObserver().addListener(this);
    }

    /**
     * Remove this panel from listening to domain change events.
     */
    public void stopListening() {
        domain.getObserver().removeListener(this);
        isOutDated = true;
    }

    public void destroy() {
        stopListening();
    }

    /*
      Domain events
    */

    public void addNodeEvent(Node node) {
        g.addVertex(node);
        repaint();
    }

    public void removeNodeEvent(Node node) {
        g.removeVertex(node);
        repaint();
    }

    public void nodeStatusChangeEvent(Node node) {
        repaint();
    }

    public void nodeLocationChangeEvent(Node node) {
        repaint();
    }

    public void addLinkEvent(Link link) {
        g.addEdge(link);
        repaint();
    }

    public void removeLinkEvent(Link link) {
        g.removeEdge(link);
        repaint();
    }

    public void linkStatusChangeEvent(Link link) {
        repaint();
    }

    public void linkMetricChangeEvent(Link link) {
        //should be repainted only if metric is shown
        repaint();
    }

    public void linkTeMetricChangeEvent(Link link) {
        //should be repainted only if TE metric is shown
        repaint();
    }

    public void linkBandwidthChangeEvent(Link link) {
        //work??
        repaint();
    }

    public void linkReservedBandwidthChangeEvent(Link link) {
        //should be repainted only if reservation is shown
        repaint();
    }

    public void linkDelayChangeEvent(Link link) {
    }

    public void addLspEvent(Lsp lsp) {
        // no need to repaint because it'll be done for the linkReservedBandwidthChangeEvent
    }

    public void removeLspEvent(Lsp lsp) {
    }

    public void lspReservationChangeEvent(Lsp lsp) {
    }

    public void lspWorkingPathChangeEvent(Lsp lsp) {
    }

    /**
     * Notify a change in the status of a lsp
     *
     * @param lsp
     */
    public void lspStatusChangeEvent(Lsp lsp) {
    }


    /*****************************************************************
     *
     * Under this point, there is a few inner classes that are only
     * useful to modify graphic objects or graph mouse listeners from their default behavior
     *
     ******************************************************************/

    /**
     * The class that manage tooltips. (ie. when to display tooltips; what information is to be displayed)
     *
     * This class implements ToolTipListener and not ToolTipFunction because "inverseTransform()" must be used
     * to get the point ; "inverseViewTranform" is used in ToolTipFunction.
     */
    class GraphToolTipListener implements ToolTipListener {


        /**
         * This class is responsible to check on which element (vertex, edge, or nothing) is the mouse cursor
         * and to generate the message of the tooltip
         *
         * @param event (this event happens when the mouse is on the graph panel, and not moving for a while)
         * @return The text to be displayed in the tooltip
         */
        public String getToolTipText(MouseEvent event) {
            Point2D point = inverseTransform(event.getPoint());
            PickSupport ps = getPickSupport();

            if (ps != null) {
                Vertex v = ps.getVertex(point.getX(), point.getY());
                if (v != null)  //mouse is on a vertex =>  Vertex Tooltip
                    return getToolTipText(v);
                Edge e = ps.getEdge(point.getX(), point.getY());
                if (e != null) {  //mouse is on a Edge => Edge Tooltip
                    return getToolTipText(e);
                }
                return null;
            }
            return null;
        }


        /**
         * Genrate text for a vertex tooltip
         *
         * @param v the vertex
         * @return the text of the tooltip
         */
        public String getToolTipText(Vertex v) {
            Node node = (Node) v.getUserDatum(TKEY);
            return "<html><p>Id: " + node.getId() + "</p>"
                    + "<p>Status: Node " + (node.getNodeStatus() == Node.STATUS_UP ? "UP" : "DOWN") + "</p>"
                    + "<p>Description: " + (node.getDescription() == null ? "" : node.getDescription()) + "</p></html>";
        }

        /**
         * Genrate text for a link tooltip
         *
         * @param e the edge
         * @return the text of the tooltip
         */
        public String getToolTipText(Edge e) {
            Link link = (Link) e.getUserDatum(TKEY);
            try {
                return "<html><p>Id: " + link.getId() + "</p>"
                        + "<p>Source: " + link.getSrcNode().getId() + "</p>"
                        + "<p>Destination: " + link.getDstNode().getId() + "</p>"
                        + "<p>Status: Link " + (link.getLinkStatus() == Link.STATUS_UP ? "UP" : "DOWN") + "</p>"
                        + "<p>Bandwidth: " + link.getBandwidth() + "</p>"
                        + "<p>Delay: " + link.getDelay() + "</p>"
                        + "<p>Metric: " + link.getMetric() + "</p>"
                        + "<p>Reservation: " + link.getReservedBandwidth() + "</p>"
                        + "<p>Reservable: " + link.getReservableBandwidth() + "</p></html>";
            } catch (NodeNotFoundException ex) {
                return link.getId();
            }
        }

    }

    private class MyPopupGraphMousePlugin extends AbstractPopupGraphMousePlugin {

        protected void handlePopup(MouseEvent e) {
            final VisualizationViewer vv = (VisualizationViewer)e.getSource();
            final Point2D ivp = vv.inverseTransform(e.getPoint());

            PickSupport pickSupport = vv.getPickSupport();

            if (pickSupport != null) {
                JPopupMenu menu;
                Vertex vertex = pickSupport.getVertex(ivp.getX(), ivp.getY());
                Edge edge = pickSupport.getEdge(ivp.getX(), ivp.getY());
                if (vertex != null) { //the mouse is on a vertex
                    menu = PopupMenuFactory.createNodePopupMenu((Node) vertex.getUserDatum(TKEY));
                } else if (edge != null) {//the mouse is on an edge
                    menu = PopupMenuFactory.createLinkPopupMenu((Link) edge.getUserDatum(TKEY));
                } else
                    menu = PopupMenuFactory.createDefaultPopupMenu();

                //display menu
                if (menu.getComponentCount() > 0) {
                    menu.show(vv, e.getX(), e.getY());
                }
            }
        }
    }
}
