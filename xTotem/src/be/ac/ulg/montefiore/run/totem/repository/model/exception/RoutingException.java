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

/*
 * Changes:
 * --------
 * 
 * - 20-Sep-2005: add a new constructor and comment correction (JLE).
 */

/**
 * Thrown when a routing algorithm makes a error.
 *
 * <p>Creation date: 25-Jan.-2005
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class RoutingException extends Exception {

    /**
     * Creates a new instance of <code>RoutingException</code> without detail message.
     */
    public RoutingException() {}

    /**
     * Constructs an instance of <code>RoutingException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public RoutingException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>RoutingException</code> from the specified throwable object.
     * @param t The throwable object from which the new exception has to be initialized.
     */
    public RoutingException(Throwable t) {
        super(t);
    }
}
