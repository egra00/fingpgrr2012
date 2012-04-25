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

import edu.uci.ics.jung.visualization.StaticLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.visualization.SettableVertexLocationFunction;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;

import java.awt.geom.Point2D;

import be.ac.ulg.montefiore.run.totem.visualtopo.graph.CoordMapper;

/*
* Changes:
* --------
*
*/

/**
* Static layout.
*
* <p>Creation date: 15/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class MyStaticLayout extends StaticLayout {
    final private CoordMapper mapper;

    public MyStaticLayout(Graph g, CoordMapper mapper) {
        super(g);
        this.mapper = mapper;
    }

    /**
     * Move a vertex to a specific location using the CoordMapper.
     * @param picked
     * @param longitude
     * @param latitude
     */
    public void moveToLocation(Vertex picked, double longitude, double latitude) {
        Coordinates c = mapper.map(longitude, latitude);
        double x = c.getX();
        double y = c.getY();
        forceMove(picked, x, y);
    }

    /**
     * Also sets the location in the {@link SettableVertexLocationFunction}.
     * @param picked
     * @param x
     * @param y
     */
    public void forceMove(Vertex picked, double x, double y) {
        super.forceMove(picked, x, y);
        ((SettableVertexLocationFunction)vertex_locations).setLocation(picked, new Point2D.Double(x, y));
    }

}
