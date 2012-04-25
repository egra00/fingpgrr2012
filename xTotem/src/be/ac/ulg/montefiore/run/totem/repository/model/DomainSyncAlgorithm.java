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
package be.ac.ulg.montefiore.run.totem.repository.model;

/*
 * Changes:
 * --------
 * - 26-Jan-2006: add isBValid, invalidateDB and restart methods. (GMO)
 */

/**
 * This interface must be implemented <strong>only</strong> by the
 * algorithms that have a local database related to a domain.
 *
 * <p>Creation date: 11-mars-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public interface DomainSyncAlgorithm extends TotemAlgorithm {
    /**
     * return true if the local DB is up-to-date
     * @return
     */
    public boolean isDBValid();

    /**
     * Tells the algo that the database is not up-to-date
     */
    public void invalidateDB();

    /**
     * restarts the algorithm with the same parameters
     */
    public void restart();

}
