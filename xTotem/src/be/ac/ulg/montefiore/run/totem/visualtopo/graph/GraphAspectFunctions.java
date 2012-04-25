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

import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.*;
import edu.uci.ics.jung.graph.predicates.SelfLoopEdgePredicate;
import org.apache.commons.collections.Predicate;

import java.awt.*;
import java.awt.geom.GeneralPath;

/*
 * Changes:
 * --------
 * - 03-Feb.-2006 : fix link down color bug (GMO)
 * - 20-Mar.-2006 : Now uses ColorLegend and LinkColorShower, add getter and setter.
 *                   (Legend is not implemented in this class anymore) (GMO)
 * - 30-Jun-2006 : rewrite EdgeColorStrokeFunction.getShape() to adapt to new version of Jung library. (GMO)
 * - 30-Jun-2006 : remove unused ArrowAspectFunction class. (GMO)
 * - 30-Jun-2006 : Change vertex size (13 -> 15), add EdgeHelperFunctions class. (GMO)
 * - 22-Nov-2006 : TotalReservedBandwidth is shown by default (GMO)
 * - 31-May-2007 : Use WidthCalculator to compute link width (GMO)
 * - 18-Oct-2007 : rename EdgeColorStrokeFunction in EdgeAspectFunction (GMO)
 */

/**
 * A collection of classes that manage the graph aspect.
 * <p/>
 * <p>Creation date: 21-Mar-2005
 *
 * @author Olivier Materne (O.Materne@student.ulg.ac.be)
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class GraphAspectFunctions {

    private static ColorLegend legend = new DefaultLinkLoadLegend();
    private static LinkColorShower linkColorShower = new TotalReservedBandwidthColorShower();
    private static NodeColorShower nColorShower = new UpDownNodeColorShower();
    private static ColorLegend nLegend = new UpDownNodeLegend();

    public static ColorLegend getColorLegend() {
        return legend;
    }

    public static void setColorLegend(ColorLegend legend) {
        GraphAspectFunctions.legend = legend;
        GraphManager.getInstance().repaint();
    }

    public static LinkColorShower getColorShower() {
        return linkColorShower;
    }

    public static void setColorShower(LinkColorShower linkColorShower) {
        GraphAspectFunctions.linkColorShower = linkColorShower;
        GraphManager.getInstance().repaint();
    }

    /**
     * This inner class handles the aspect of the edges
     *
     * @author Olivier Materne
     */
    public final static class EdgeAspectFunction extends EdgeShape.BentLine
            implements EdgePaintFunction, EdgeStrokeFunction {

        protected static EdgeShape.Loop loop = new EdgeShape.Loop();
        protected static Predicate is_self_loop = SelfLoopEdgePredicate.getInstance();

        private final static float[] highlightPattern = {5f, 5f};
        private final static float[] interPattern = {20f, 5f, 5f, 5f};

        private static GeneralPath instance = new GeneralPath();

        private MyWidthCalculator widthCalc;

        public EdgeAspectFunction(Domain domain) {
            widthCalc = new MyWidthCalculator(domain);
        }

        /**
         * Return the shape of the Edge. With this method, if there is 2 edges between 2 nodes, they will be seen as
         * 2 parallel lines.
         *
         * @param e
         * @return
         */
        public Shape getShape(Edge e) {
            if (is_self_loop.evaluate(e))
                return loop.getShape(e);
            int index = 1;
            if(parallelEdgeIndexFunction != null) {
                index = parallelEdgeIndexFunction.getIndex(e);
            }

            Link ilink = (Link) e.getUserDatum(MyVisualizationViewer.TKEY);  //get IgpLink from edge
            int width = widthCalc.getWidth(ilink.getBandwidth());

            //float controlY = 3 + 5*index;
            float controlY = width+1 + 6*index;
            instance.reset();
            instance.moveTo(0.0f, controlY);
            instance.lineTo(1.0f, controlY);
            instance.lineTo(1.0f, 1.0f);
            return instance;
        }


        /**
         * Handle egde shape
         *
         * @param e an Edge
         * @return the adequate Stroke for that Edge
         */
        public Stroke getStroke(Edge e) {
            Link ilink = (Link) e.getUserDatum(MyVisualizationViewer.TKEY);  //get IgpLink from edge
            int width = widthCalc.getWidth(ilink.getBandwidth());

            switch (ilink.getLinkType()) {
                case VIRTUAL:
                    width = 3;
                    break;
            }

            if (((Boolean) e.getUserDatum(MyVisualizationViewer.HIGHLIGHTKEY)).booleanValue())
                return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0f, highlightPattern, 0f);
            else if (ilink.getLinkType() == Link.Type.INTER)
                return new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 0f, interPattern, 0f);
            return new BasicStroke(width);
        }


        public Paint getFillPaint(Edge edge) {
            return null; //we do not use this (but must be there to implement the interface)
        }

        /**
         * Draw the edge with the color choosen accordingly to link load
         *
         * @param e
         * @return
         */
        public Paint getDrawPaint(Edge e) {
            Link link = (Link) e.getUserDatum(MyVisualizationViewer.TKEY);  //get IgpLink from edge

            //Compute color accordingly to load
            float div = linkColorShower.getColorValue(link);

            Color c = legend.getColor(div);


            switch (link.getLinkType()) {
                case VIRTUAL:
                    c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 128);
                    break;

            }


            return c;
        }
    }


    /**
     * This inner class manage all properties of the vertices representation (shapes, colors, size, ...)
     */
    public final static class VertexAspectFunction extends AbstractVertexShapeFunction
            implements VertexPaintFunction, VertexSizeFunction,
            VertexAspectRatioFunction, VertexStrokeFunction {
        Color bgColor = null;     //vertex interior color
        int vertexSize; //vertex size
        float ratio; //vertex aspect ratio
        Stroke stroke = null;

        /**
         * a simple constructor
         */
        public VertexAspectFunction() {
            bgColor = Color.white;
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

            Node node = (Node) vertex.getUserDatum(MyVisualizationViewer.TKEY);

            //Compute color accordingly to load
            float div = nColorShower.getColorValue(node);

            Color c = nLegend.getColor(div);


            switch (node.getNodeType()) {
                case VIRTUAL:
                    c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 128);
                    break;
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
