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

import edu.uci.ics.jung.visualization.SettableVertexLocationFunction;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.ArchetypeVertex;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.awt.geom.Point2D;

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Node;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.CoordMapper;

/*
* Changes:
* --------
*
*/

/**
* 
*
* <p>Creation date: 11/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class MySettableVertexLocationFunction implements SettableVertexLocationFunction {
    private Map v_locations = new HashMap<Vertex, Point2D>();

    private CoordMapper mapper;

    /**
     *
     * @param mapper
     */
    public MySettableVertexLocationFunction(CoordMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Return the position of the vertex. The position is taken from the node latitude and longitude and mapped
     * to screen position thanks to the CoordMapper object. If the node location is not defined, return a random number.
     * @param v
     * @return
     */
    public Point2D getLocation(ArchetypeVertex v) {
        Point2D location = (Point2D) v_locations.get(v);
        if (location == null) {
            Node node = (Node) v.getUserDatum(MyDirectedSparseGraph.KEY);
            double lat, longi;
            if (node.isSetLocation()) {
                lat = node.getLocation().getLatitude();
                longi = node.getLocation().getLongitude();
            } else {
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

    /**
     * Set the vertex location in the hashmap and update the node location properties if they were set.
     * @param v
     * @param location
     */
    public void setLocation(ArchetypeVertex v, Point2D location) {
        Node n = (Node)v.getUserDatum(MyDirectedSparseGraph.KEY);
        // update location if it was set before
        if (n.isSetLocation()) {
            Coordinates c = mapper.unmap(location.getX(), location.getY());
            n.getLocation().setLatitude((float)c.getY());
            n.getLocation().setLongitude((float)c.getX());
        }
        v_locations.put(v, location);
    }
}
