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

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.VertexLocationFunction;
import edu.uci.ics.jung.visualization.Coordinates;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import be.ac.ulg.montefiore.run.totem.domain.model.Node;

/*
* Changes:
* --------
* - 30-June-2006: Rewrite of the class. Now uses a VertexLocationFunction class in conjoonction with a CoordMapper object. (GMO)
*
*/

/**
 * An extention to the AbstractLayout class that allow to represent graph accordingly to
 * geographic coordinates stored in the .xml file. Note that if no coordinates were present in the file,
 * the vertices will be randomly placed.
 *
 * <p>Creation date: 30 mars 2006
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */
class LocalLayout extends AbstractLayout {
    private CoordMapper mapper;

    /**
     * Constructor
     *
     * @param g
     */
    public LocalLayout(Graph g) {
        super(g);
    }

    public void initialize(Dimension size) {
        initialize(size, new MyVertexLocationFunction(size, getGraph()));
    }

    /**
     * Do nothing : no iteration is needed to compute layout.
     */
    public void advancePositions() {//do not modify positions after first time.
    }

    /**
     * Do nothing.
     *
     * @param v A vertex
     */
    protected void initialize_local_vertex(Vertex v) {
    }

    /**
     * @return returns true
     */
    public boolean incrementsAreDone() {
        return true;
    }


    /**
     * @return returns false
     */
    public boolean isIncremental() {
        return false;
    }

    public Coordinates getRealCoordinates(Vertex v) {
        return mapper.unmap(getX(v), getY(v));
    }


    private class MyVertexLocationFunction implements VertexLocationFunction {
        Map v_locations = new HashMap<Vertex, Point2D>();

        /**
         * Compute the coordinates range and cretae the CoordMapper object.
         * @param d
         * @param g
         */
        public MyVertexLocationFunction(Dimension d, Graph g) {
            float minLong = 180;
            float minLat = 90;
            float maxLong = -180;
            float maxLat = -90;

            //get geographical bounds of network
            for (Iterator iter = g.getVertices().iterator(); iter.hasNext();) {
                Vertex v = (Vertex) iter.next();
                Node node = (Node) v.getUserDatum(MyVisualizationViewer.TKEY);
                float lat = 0, longi = 0;
                try {
                    lat = (new Float(node.getLatitude())).floatValue();
                    longi = (new Float(node.getLongitude())).floatValue();
                } catch (Exception e) {
                }
                if (longi == 0.0 && lat == 0.0) {
                    continue;   //do not modify maximas when there is no info
                }
                if (longi < minLong) minLong = longi;
                if (lat < minLat) minLat = lat;
                if (longi > maxLong) maxLong = longi;
                if (lat > maxLat) maxLat = lat;
            }
            float XRange = maxLong - minLong;
            float YRange = maxLat - minLat;

            if (XRange <= 0) {
                XRange = (float) d.getWidth();
                minLong = 0;
            }
            if (YRange <= 0) {
                YRange = (float) d.getHeight();
                minLat = 0;
            }

            mapper = new CoordMapper(XRange, minLong, YRange, minLat, d.getWidth() * 0.9, 10, d.getHeight() * 0.9, 10);
        }

        /**
         * Return the position of the vertex. The position is taken from the node latitude and longitude and mapped
         * to screen position thanks to the CoordMapper object.
         * @param v
         * @return
         */
        public Point2D getLocation(ArchetypeVertex v) {
            Point2D location = (Point2D) v_locations.get(v);
            if (location == null) {
                Node node = (Node) v.getUserDatum(MyVisualizationViewer.TKEY);
                double lat, longi;
                lat = (new Float(node.getLatitude())).floatValue();
                longi = (new Float(node.getLongitude())).floatValue();
                if (lat == 0 && longi == 0) {
                    lat = Math.random() * mapper.getSrcRangeY() + mapper.getSrcMinY();
                    longi = Math.random() * mapper.getSrcRangeX() + mapper.getSrcMinX();
                }
                location = new Point2D.Double(mapper.map(longi, lat).getX(), mapper.map(longi, lat).getY());
                v_locations.put(v, location);
            }
            return location;
        }

        public Iterator getVertexIterator() {
            return v_locations.keySet().iterator();
        }
    }
}

