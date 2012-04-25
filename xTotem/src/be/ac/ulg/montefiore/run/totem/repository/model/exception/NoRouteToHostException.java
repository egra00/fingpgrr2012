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
package be.ac.ulg.montefiore.run.totem.repository.model.exception;

import be.ac.ulg.montefiore.run.totem.domain.model.Node;

/*
 * Changes:
 * --------
 * 
 * - 13-May-2005: add the src and dst (JL).
 */

/**
 * Thrown when a routing algorithm can't find a route to a destination.
 *
 * <p>Creation date: 25-Jan.-2005
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class NoRouteToHostException extends Exception {

    private Node src, dst;
    
    /**
     * Creates a new instance without detail message.
     */
    public NoRouteToHostException() {}

    /**
     * Creates a new instance of the exception and specifies the nodes between
     * which there is no route.
     */
    public NoRouteToHostException(Node src, Node dst) {
        this.src = src;
        this.dst = dst;
    }

    /**
     * Constructs an instance with the specified detail message.
     * @param msg the detail message.
     */
    public NoRouteToHostException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance with the specified detail message and specifies
     * the nodes between which there is no route.
     */
    public NoRouteToHostException(String msg, Node src, Node dst) {
        super(msg);
        this.src = src;
        this.dst = dst;
    }

    /**
     * @return Returns the dst.
     */
    public Node getDst() {
        return dst;
    }

    /**
     * @return Returns the src.
     */
    public Node getSrc() {
        return src;
    }
}
