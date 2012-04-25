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

import java.util.Collection;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.impl.SparseGraph;

/**
 * An implementation of <code>Graph</code> that consists of a
 * <code>Vertex</code> set and a <code>DirectedEdge</code> set.
 * This implementation DOES ALLOW parallel edges on the contrary of
 * edu.uci.ics.jung.graph.impl.DirectedSparseGraph
 *
 * <p>Edge constraints imposed by this class: DIRECTED_EDGE
 *
 * <p>For additional system and user constraints defined for
 * this class, see the superclasses of this class.</p>
 *
 * @see edu.uci.ics.jung.graph.impl.DirectedSparseVertex
 * @see edu.uci.ics.jung.graph.impl.DirectedSparseEdge
 *
 * @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class DirectedSparseGraph extends SparseGraph
	implements DirectedGraph {

	/**
	 * Creates an instance of a sparse directed graph.
	 */
	public DirectedSparseGraph() {
		super();
//        system_edge_requirements.add(DIRECTED_EDGE);
//        user_edge_requirements.add(NOT_PARALLEL_EDGE);
        Collection edge_predicates =
            getEdgeConstraints();
        edge_predicates.add(DIRECTED_EDGE);
	}
}

