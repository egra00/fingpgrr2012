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
package be.ac.ulg.montefiore.run.totem.trafficMatrix.model;

import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;

/*
* Changes:
* --------
*
*/

/**
 * Represent a view of the computed mpls load for links in a domain (traffic that is routed on LSPs).
 * <p/>
 * <p>Creation date: 28/01/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public interface MPLSLoadData extends LoadData {
    /**
     * Same as {@link #getLoad(be.ac.ulg.montefiore.run.totem.domain.model.Link)} but returns the only the mpls load
     * (traffic that is routed on LSPs).
     * @param link
     * @return
     */
    public double getMPLSLoad(Link link);


    /**
     * Same as {@link #getLoad()} but returns the only the mpls load
     * (traffic that is routed on LSPs).
     * @return
     */
    public double[] getMPLSLoad();

    /**
     * Returns an newly created array that represent load on the lsp. For each lsp <code>lsp</code> in the domain,
     * the corresponding load is located at the index domain.getConvertor().getLspId(lsp.getId()) of the returned array.
     * The load corresponds to the traffic routed on the working path
     * ({@link be.ac.ulg.montefiore.run.totem.domain.model.Lsp#getWorkingPath()}) of the lsp at the moment of
     * creation of the data. It is to the user to ensure that the working path hasn't changed since.
     *
     * @return links load for all links in the domain.
     *
     * @param lsp
     * @return
     */
    public double getMPLSLoad(Lsp lsp);


}
