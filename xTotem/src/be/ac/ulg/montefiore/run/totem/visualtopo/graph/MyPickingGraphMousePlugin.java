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

import edu.uci.ics.jung.visualization.control.PickingGraphMousePlugin;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.PickSupport;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.Layout;

import java.awt.geom.Point2D;
import java.awt.event.MouseEvent;

/*
* Changes:
* --------
*
*/

/**
* This class extends the PickingGraphMousePlugin. The mousePressed() method is a simple copy-paste from base class.
* The only difference is that it uses inverseTranform() VisualizationViewer method instead of inverseViewTransform.
* A bug in Jung ???
*
* <p>Creation date: 30 juin 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class MyPickingGraphMousePlugin extends PickingGraphMousePlugin {

    public void mousePressed(MouseEvent e) {
        down = e.getPoint();
        VisualizationViewer vv = (VisualizationViewer)e.getSource();
        PickSupport pickSupport = vv.getPickSupport();
        PickedState pickedState = vv.getPickedState();
        if(pickSupport != null && pickedState != null) {
            Layout layout = vv.getGraphLayout();
            if(e.getModifiers() == modifiers) {
                vv.addPostRenderPaintable(lensPaintable);
                rect.setFrameFromDiagonal(down,down);
                // p is the screen point for the mouse event
                Point2D p = e.getPoint();
                // take away the view transform
                //--- There is the difference from the base class - GMO 
                //Point2D ip = vv.inverseViewTransform(p);
                Point2D ip = vv.inverseTransform(p);

                vertex = pickSupport.getVertex(ip.getX(), ip.getY());
                if(vertex != null) {
                    if(pickedState.isPicked(vertex) == false) {
                        pickedState.clearPickedVertices();
                        pickedState.pick(vertex, true);
                    }
                    // layout.getLocation applies the layout transformer so
                    // q is transformed by the layout transformer only
                    Point2D q = layout.getLocation(vertex);
                    // transform the mouse point to graph coordinate system
                    Point2D gp = vv.inverseLayoutTransform(ip);

                    offsetx = (float) (gp.getX()-q.getX());
                    offsety = (float) (gp.getY()-q.getY());
                } else if((edge = pickSupport.getEdge(ip.getX(), ip.getY())) != null) {
                    pickedState.clearPickedEdges();
                    pickedState.pick(edge, true);
                } else {
                    pickedState.clearPickedEdges();
                    pickedState.clearPickedVertices();
                }

            } else if(e.getModifiers() == addToSelectionModifiers) {
                vv.addPostRenderPaintable(lensPaintable);
                rect.setFrameFromDiagonal(down,down);
                Point2D p = e.getPoint();
                // remove view transform
                Point2D ip = vv.inverseViewTransform(p);
                vertex = pickSupport.getVertex(ip.getX(), ip.getY());
                if(vertex != null) {
                    boolean wasThere = pickedState.pick(vertex, !pickedState.isPicked(vertex));
                    if(wasThere) {
                        vertex = null;
                    } else {

                        // layout.getLocation applies the layout transformer so
                        // q is transformed by the layout transformer only
                        Point2D q = layout.getLocation(vertex);
                        // translate mouse point to graph coord system
                        Point2D gp = vv.inverseLayoutTransform(ip);

                        offsetx = (float) (gp.getX()-q.getX());
                        offsety = (float) (gp.getY()-q.getY());
                    }
                } else if((edge = pickSupport.getEdge(ip.getX(), ip.getY())) != null) {
                    pickedState.pick(edge, !pickedState.isPicked(edge));
                }
            }
        }
        if(vertex != null) e.consume();
    }
}
