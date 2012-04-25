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

import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.predicates.SelfLoopEdgePredicate;

import java.awt.*;
import java.awt.geom.GeneralPath;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.LinkDecorator;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.LinkType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.NodeType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.StatusType;
import org.apache.commons.collections.Predicate;

/*
* Changes:
* --------
*
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 12/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class GraphAspectFunctions {
    public static class EdgeAspectFunction  extends EdgeShape.BentLine implements EdgeStrokeFunction, EdgePaintFunction, EdgeShapeFunction {
        private static int width = 2;

        private final static EdgeShape.Loop loop = new EdgeShape.Loop();
        private final static Predicate is_self_loop = SelfLoopEdgePredicate.getInstance();
        private final static GeneralPath instance = new GeneralPath();
        private final static float[] interPattern = {20f, 5f, 5f, 5f};

        protected static Color color = new Color(0, 255, 0);
        protected static Color downColor = Color.black;

        public Stroke getStroke(Edge e) {
            LinkDecorator link = (LinkDecorator) e.getUserDatum(MyDirectedSparseGraph.KEY);

            if (link.getLink().getType() == LinkType.INTER)
                return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0f, interPattern, 0f);
            return new BasicStroke(width);
        }

        public Shape getShape(Edge e) {
            if (is_self_loop.evaluate(e))
                return loop.getShape(e);
            int index = 1;
            if(parallelEdgeIndexFunction != null) {
                index = parallelEdgeIndexFunction.getIndex(e);
            }

            //float controlY = 3 + 5*index;
            float controlY = width+1 + 6*index;
            instance.reset();
            instance.moveTo(0.0f, controlY);
            instance.lineTo(1.0f, controlY);
            instance.lineTo(1.0f, 1.0f);
            return instance;
        }

        public Paint getDrawPaint(Edge e) {
            LinkDecorator link = (LinkDecorator) e.getUserDatum(MyDirectedSparseGraph.KEY);

            Color c = link.getLink().getStatus() == StatusType.DOWN ? downColor : color;

            if (link.getLink().getType() == LinkType.VIRTUAL) {
                    c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 128);
            }
            return c;
        }

        public Paint getFillPaint(Edge e) {
            return null;
        }
    }

    /**
     * This inner class manage all properties of the vertices representation (shapes, colors, size, ...)
     */
    public static class VertexAspectFunction extends AbstractVertexShapeFunction
            implements VertexPaintFunction, VertexSizeFunction,
            VertexAspectRatioFunction, VertexStrokeFunction, VertexShapeFunction {


        protected static Color bgColor = Color.white;     //vertex interior color
        protected static Color borderColor = Color.blue;
        protected static Color downBorderColor = Color.black;;
        protected int vertexSize; //vertex size
        protected float ratio; //vertex aspect ratio
        protected Stroke stroke = null;

        /**
         * a simple constructor
         */
        public VertexAspectFunction() {
            vertexSize = 15;
            ratio = 1.0f;
            stroke = new BasicStroke(2);
            setSizeFunction(this);
            setAspectRatioFunction(this);
        }


        /**
         * returns the vertex inner color.
         *
         * @param vertex
         * @return the vertex inner color
         */
        public Paint getFillPaint(Vertex vertex) {
            return bgColor;
        }


        /**
         * returns the vertex border color.
         *
         * @param vertex
         * @return the vertex border color
         */
        public Paint getDrawPaint(Vertex vertex) {
            Node node = (Node) vertex.getUserDatum(MyDirectedSparseGraph.KEY);
            Color c = node.getStatus() == StatusType.DOWN ? downBorderColor : borderColor;
            if (node.getType() == NodeType.VIRTUAL) {
                // transparent color for virtual node
                c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 128);
            }
            return c;
        }

        /**
         * return vertex size
         *
         * @param vertex
         * @return the size of the vertex
         */
        public int getSize(Vertex vertex) {
            return vertexSize;
        }

        /**
         * compute width/heigth ratio for a Vertex
         *
         * @param v a Vertex
         * @return an integer : the appropriate width/heigth ratio
         */
        public float getAspectRatio(Vertex v) {
            return ratio;
        }

        /**
         * Compute appropiate shape for the vertex (here a circle)
         *
         * @param v a vertex
         * @return Returns vertex shape
         */
        public Shape getShape(Vertex v) {
            return factory.getEllipse(v);
        }

        /**
         * Returns border effect for the vertex
         *
         * @param v a vertex
         * @return a Stroke
         */
        public Stroke getStroke(Vertex v) {
            return stroke;
        }

    }

}
