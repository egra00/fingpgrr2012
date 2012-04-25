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
 * - 21-Sep-2007: routeDetour throw LocalDatabase exception (GMO)
 * - 29-Nov-2007: move detour methods and bypass methods in specific interfaces. (GMO)
 */

/**
 * This interface must be implemented by algorithms that compute backup LSP.
 *
 * <p>Creation date: 25-May-2004
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public interface LSPBackupRouting extends TotemAlgorithm {

}
