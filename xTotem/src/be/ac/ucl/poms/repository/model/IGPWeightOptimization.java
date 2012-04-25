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
package be.ac.ucl.poms.repository.model;

/*
 * Changes:
 * --------
 *
 */

/**
 * Totem class.
 * You can modify this file to run your own test file (toolbox execution scenario).
 *
 * <p>Creation date: 1-Jan-2005
 *
 * @author  Selin Cerav-Erbas (cerav@poms.ucl.ac.be)
 * @author  Hakan Umit (umit@poms.ucl.ac.be)
 */
import be.ac.ulg.montefiore.run.totem.repository.model.*;
/**
 * This interface specifies the methods required by an algorithm that computes IGP Weights.
 *
 */
public interface IGPWeightOptimization extends TotemAlgorithm{
    public TotemActionList calculateWeights(int ASID, int[] TMID) throws Exception;
}
