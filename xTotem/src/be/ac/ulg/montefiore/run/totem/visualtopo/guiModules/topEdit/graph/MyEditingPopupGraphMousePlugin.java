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

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.*;
import java.util.Iterator;
import java.util.Set;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.LinkDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.guiComponents.LinkPropertiesDialog;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.guiComponents.NodePropertiesDialog;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Node;

/*
* Changes:
* --------
*
*/

/**
 * Can only be used with a MyDirectedSparseGraph instance. Popups to create and edit edges.
 * Can operate in batch mode or not. 
 * <p/>
 * <p>Creation date: 3/10/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class MyEditingPopupGraphMousePlugin extends AbstractPopupGraphMousePlugin implements BatchModeCapable {
    SettableVertexLocationFunction vertexLocations;

    private boolean batchMode = false;

    public MyEditingPopupGraphMousePlugin(SettableVertexLocationFunction vertexLocations) {
        this.vertexLocations = vertexLocations;
    }

    public void setBatchMode(boolean batchMode) {
        this.batchMode = batchMode;
    }

    public boolean getBatchMode() {
        return batchMode;
    }

    protected void handlePopup(MouseEvent e) {
        final VisualizationViewer vv =
                (VisualizationViewer) e.getSource();
        final Layout layout = vv.getGraphLayout();
        final MyDirectedSparseGraph graph = (MyDirectedSparseGraph) layout.getGraph();
        final Point p = e.getPoint();
        final Point2D ivp = vv.inverseViewTransform(e.getPoint());
        PickSupport pickSupport = vv.getPickSupport();
        if (pickSupport != null) {

            final Vertex vertex = pickSupport.getVertex(ivp.getX(), ivp.getY());
            final Edge edge = pickSupport.getEdge(ivp.getX(), ivp.getY());
            final PickedState pickedState = vv.getPickedState();
            JPopupMenu popup = new JPopupMenu();

            if (vertex != null) {
                Set picked = pickedState.getPickedVertices();
                if (picked.size() > 0) {
                    JMenu directedMenu = new JMenu("Create Directed Edge");
                    popup.add(directedMenu);
                    String vertexId = ((Node)vertex.getUserDatum(MyDirectedSparseGraph.KEY)).getId();
                    for (Iterator iterator = picked.iterator(); iterator.hasNext();) {
                        final Vertex other = (Vertex) iterator.next();
                        String otherId = ((Node)other.getUserDatum(MyDirectedSparseGraph.KEY)).getId();
                        directedMenu.add(new AbstractAction("[" + otherId + "," + vertexId + "]") {
                            public void actionPerformed(ActionEvent e) {
                                final Edge newEdge = graph.createEdge(other, vertex);
                                if (!batchMode) {
                                    DomainDecorator domainDecorator = graph.getDomainDecorator();
                                    final LinkDecorator l = (LinkDecorator)newEdge.getUserDatum(MyDirectedSparseGraph.KEY);
                                    // add link on success
                                    JDialog dialog = new LinkPropertiesDialog(domainDecorator, l) {
                                        protected void postProcessingOnSuccess() {
                                            graph.addEdge(newEdge);
                                            vv.repaint();
                                        }
                                    };
                                    dialog.pack();
                                    dialog.setLocationRelativeTo(dialog.getParent());
                                    dialog.setVisible(true);
                                } else {
                                    graph.addEdge(newEdge);
                                    vv.repaint();
                                }
                            }
                        });
                    }
                }
                popup.add(new AbstractAction("Delete Vertex") {
                    public void actionPerformed(ActionEvent e) {
                        pickedState.pick(vertex, false);
                        graph.removeVertex(vertex);
                        vv.repaint();
                    }
                });
                popup.add(new AbstractAction("Edit Vertex") {
                    public void actionPerformed(ActionEvent e) {
                        pickedState.pick(vertex, false);
                        graph.editVertex(vertex);
                        vv.repaint();
                    }
                });
            } else if (edge != null) {
                popup.add(new AbstractAction("Delete Edge") {
                    public void actionPerformed(ActionEvent e) {
                        pickedState.pick(edge, false);
                        graph.removeEdge(edge);
                        vv.repaint();
                    }
                });
                popup.add(new AbstractAction("Edit Edge") {
                    public void actionPerformed(ActionEvent e) {
                        pickedState.pick(edge, false);
                        graph.editEdge(edge);
                        vv.repaint();
                    }
                });
            } else {
                popup.add(new AbstractAction("Create Vertex") {
                    public void actionPerformed(ActionEvent e) {
                        final Vertex newVertex = graph.createVertex();
                        vertexLocations.setLocation(newVertex, vv.inverseTransform(p));
                        final MyStaticLayout layout = (MyStaticLayout)vv.getGraphLayout();

                        for (Iterator iterator = graph.getVertices().iterator(); iterator.hasNext();) {
                            layout.lockVertex((Vertex) iterator.next());
                        }

                        final Node n = (Node)newVertex.getUserDatum(MyDirectedSparseGraph.KEY);

                        if (!batchMode) {
                            DomainDecorator domainDecorator = graph.getDomainDecorator();
                            JDialog dialog = new NodePropertiesDialog(n, domainDecorator) {
                                protected void postProcessingOnSuccess() {
                                    graph.addVertex(newVertex);
                                    vv.getModel().restart();
                                    if (n.isSetLocation()) {
                                        layout.moveToLocation(newVertex, n.getLocation().getLongitude(), n.getLocation().getLatitude());
                                    }
                                }

                                public void dispose() {
                                    for (Iterator iterator = graph.getVertices().iterator(); iterator.hasNext();) {
                                        layout.unlockVertex((Vertex) iterator.next());
                                    }
                                    vv.repaint();
                                    super.dispose();
                                }
                            };
                            dialog.pack();
                            dialog.setLocationRelativeTo(dialog.getParent());
                            dialog.setVisible(true);
                        } else {
                            graph.addVertex(newVertex);
                            vv.getModel().restart();
                            for (Iterator iterator = graph.getVertices().iterator(); iterator.hasNext();) {
                                layout.unlockVertex((Vertex) iterator.next());
                            }
                            vv.repaint();
                        }
                    }
                });
            }
            if (popup.getComponentCount() > 0) {
                popup.show(vv, e.getX(), e.getY());
            }
        }

    }
}
