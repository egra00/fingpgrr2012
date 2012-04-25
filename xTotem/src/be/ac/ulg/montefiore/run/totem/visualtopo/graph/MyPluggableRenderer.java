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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;

/*
* Changes:
* --------
* - 30-Jun-2006 : rewrite drawSimpleEdge method by copy-pasting from base class and changing arrows color. (GMO)
* - 12-Feb-2007 : draw simple edge now compile on Java 6 (GMO)
* - 31-May-2007 : constructor has now a parameter which is the domain (GMO)
* - 18-Oct-2007 : use the new ModifiedPluggableRenderer class (GMO)
*/

/**
 * 
 * A very simple modification of Pluggable renderer to allow arrows to be painted in different colors
 *
 * <p>Creation date: 30 mars 2006
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class MyPluggableRenderer extends ModifiedPluggableRenderer {

    public MyPluggableRenderer(Domain domain) {
        super();
        //define an object that will manage edge color and aspect
        GraphAspectFunctions.EdgeAspectFunction ecsf = new GraphAspectFunctions.EdgeAspectFunction(domain);
        ecsf.setControlOffsetIncrement(3.f);
        setEdgeStrokeFunction(ecsf);
        setEdgePaintFunction(ecsf);
        setEdgeShapeFunction(ecsf);

        //define an objet that handles vertex graphical properties
        GraphAspectFunctions.VertexAspectFunction vertexAspectFunction = new GraphAspectFunctions.VertexAspectFunction();
        setVertexShapeFunction(vertexAspectFunction);
        setVertexPaintFunction(vertexAspectFunction);
        setVertexStrokeFunction(vertexAspectFunction);
    }

}
