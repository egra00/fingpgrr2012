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

import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.UndirectedEdge;
import edu.uci.ics.jung.utils.Pair;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;

/*
* Changes:
* --------
*
*/

/**
* Pluggable renderer with a red arrow.
*
* <p>Creation date: 12/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ModifiedPluggableRenderer extends PluggableRenderer {
    /**
     * This function is a simple copy-paste of parent (defined in PluggableRenderer) with just a small change to draw
     * the arrows in red.
     * @param g
     * @param e
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    protected void drawSimpleEdge(Graphics2D g, Edge e, int x1, int y1, int x2, int y2) {
        Pair endpoints = e.getEndpoints();
        Vertex v1 = (Vertex)endpoints.getFirst();
        Vertex v2 = (Vertex)endpoints.getSecond();
        boolean isLoop = v1.equals(v2);
        Shape s2 = vertexShapeFunction.getShape(v2);
        Shape edgeShape = edgeShapeFunction.getShape(e);

        boolean edgeHit = true;
        boolean arrowHit = true;
        Rectangle deviceRectangle = null;
        if(screenDevice != null) {
            Dimension d = screenDevice.getSize();
            if(d.width <= 0 || d.height <= 0) {
                d = screenDevice.getPreferredSize();
            }
            deviceRectangle = new Rectangle(0,0,d.width,d.height);
        }

        AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);

        if(isLoop) {
            // this is a self-loop. scale it is larger than the vertex
            // it decorates and translate it so that its nadir is
            // at the center of the vertex.
            Rectangle2D s2Bounds = s2.getBounds2D();
            xform.scale(s2Bounds.getWidth(),s2Bounds.getHeight());
            xform.translate(0, -edgeShape.getBounds2D().getWidth()/2);
        } else {
            // this is a normal edge. Rotate it to the angle between
            // vertex endpoints, then scale it to the distance between
            // the vertices
            float dx = x2-x1;
            float dy = y2-y1;
            float thetaRadians = (float) Math.atan2(dy, dx);
            xform.rotate(thetaRadians);
            float dist = (float) Math.sqrt(dx*dx + dy*dy);
            xform.scale(dist, 1.0);
        }

        edgeShape = xform.createTransformedShape(edgeShape);

        edgeHit = viewTransformer.transform(edgeShape).intersects(deviceRectangle);

        if(edgeHit == true) {

            Paint oldPaint = g.getPaint();

            // get Paints for filling and drawing
            // (filling is done first so that drawing and label use same Paint)
            Paint fill_paint = edgePaintFunction.getFillPaint(e);
            if (fill_paint != null)
            {
                g.setPaint(fill_paint);
                g.fill(edgeShape);
            }
            Paint draw_paint = edgePaintFunction.getDrawPaint(e);
            if (draw_paint != null)
            {
                g.setPaint(draw_paint);
                g.draw(edgeShape);
            }

            float scalex = (float)g.getTransform().getScaleX();
            float scaley = (float)g.getTransform().getScaleY();
            // see if arrows are too small to bother drawing
            if(scalex < .3 || scaley < .3) return;

            if (edgeArrowPredicate.evaluate(e)) {

                Shape destVertexShape =
                    vertexShapeFunction.getShape((Vertex)e.getEndpoints().getSecond());
                AffineTransform xf = AffineTransform.getTranslateInstance(x2, y2);
                destVertexShape = xf.createTransformedShape(destVertexShape);

                arrowHit = viewTransformer.transform(destVertexShape).intersects(deviceRectangle);
                if(arrowHit) {

                    AffineTransform at;
                    if (edgeShape instanceof GeneralPath)
                        at = getArrowTransform((GeneralPath)edgeShape, destVertexShape);
                    else
                        at = getArrowTransform(new GeneralPath(edgeShape), destVertexShape);
                    if(at == null) return;
                    Shape arrow = edgeArrowFunction.getArrow(e);
                    arrow = at.createTransformedShape(arrow);
                    // note that arrows implicitly use the edge's draw paint
                    g.setColor(Color.RED);
                    g.fill(arrow);
                }
                if (e instanceof UndirectedEdge) {
                    Shape vertexShape =
                        vertexShapeFunction.getShape((Vertex)e.getEndpoints().getFirst());
                    xf = AffineTransform.getTranslateInstance(x1, y1);
                    vertexShape = xf.createTransformedShape(vertexShape);

                    arrowHit = viewTransformer.transform(vertexShape).intersects(deviceRectangle);

                    if(arrowHit) {
                        AffineTransform at;
                        if (edgeShape instanceof GeneralPath)
                            at = getReverseArrowTransform((GeneralPath)edgeShape, vertexShape, !isLoop);
                        else
                            at = getReverseArrowTransform(new GeneralPath(edgeShape), vertexShape, !isLoop);
                        if(at == null) return;
                        Shape arrow = edgeArrowFunction.getArrow(e);
                        arrow = at.createTransformedShape(arrow);
                        g.setColor(Color.RED);
                        g.fill(arrow);
                    }
                }
            }
            // use existing paint for text if no draw paint specified
            if (draw_paint == null)
                g.setPaint(oldPaint);
            String label = edgeStringer.getLabel(e);
            if (label != null) {
                labelEdge(g, e, label, x1, x2, y1, y2);
            }


            // restore old paint
            g.setPaint(oldPaint);
        }
    }

}
