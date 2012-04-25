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
package be.ac.ulg.montefiore.run.totem.util;

/*
 * Changes:
 * --------
 *
 */

/**
 * Interface that describes the operations on a priority queue.
 *
 * A priority queue is a structure that allows access to the element with the lowest key
 * in a O(1) time and the add or delete operation in O(log(N)).
 *
 * <p>Creation date: 05-Jan.-2004
 * 
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface PriorityQueueIF {

    /**
	 * Gets the object with the minimum key in the queue
	 *
	 * @return the object with the minimum key
	 */
    public PriorityQueueObject next();

    /**
	 * Adds an object to the queue.
	 *
	 * @param elem the object to add
	 */
    public void add(PriorityQueueObject elem);

    /**
	 * Removes and returns the next object from the queue
	 *
	 * @return The object removed from the queue
	 */
    public PriorityQueueObject removeNext();

    /**
	 * Updates the Object with the same id in the queue
	 *
	 * @param elem The object to update in the queue
	 */
    public void update(PriorityQueueObject elem);

    /**
	 * Gets the size of the queue
	 *
	 * @return size of the queue
	 */
    public int size();
}
