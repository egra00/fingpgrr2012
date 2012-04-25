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

import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Node;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.graph.MyDirectedSparseGraph;

/*
* Changes:
* --------
*/

/**
 * Simple class that display the id of the node as label.
 * <p/>
 * <p>Creation date: 15 Oct 2007
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class NodeLabeller implements VertexStringer {
    private static NodeLabeller instance = null;

    private NodeLabeller() {}

    public static NodeLabeller getInstance() {
        if (instance == null) {
            instance = new NodeLabeller();
        }
        return instance;
    }

    public String getLabel(ArchetypeVertex v) {
        try {
            return String.valueOf(((Node) v.getUserDatum(MyDirectedSparseGraph.KEY)).getId());
        } catch (Exception ex) {
            //if an exception is catch do not label the link
            return null;
        }
    }
}
