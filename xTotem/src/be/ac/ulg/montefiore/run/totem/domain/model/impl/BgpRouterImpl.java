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
package be.ac.ulg.montefiore.run.totem.domain.model.impl;

import be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpNetwork;

import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.ArrayList;

/**
 * Implementation of the representation of a BGP router.
 *
 * @author : Bruno Quoitin (bqu@info.ucl.ac.be)
 * Contributor(s) :
 *
 * Creation date : 08-Feb-2005
 *
 * Changes:
 * --------
 *
 */
public class BgpRouterImpl extends be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.BgpRouterImpl implements BgpRouter {

    /**
     * Returns the list of networks that are advertised by this router.
     *
     * @return list of networks advertised by this router.
     */
    public List<BgpNetwork> getAllNetworks() {
	if ((getNetworks() != null) && (getNetworks().getNetwork() != null))
	    return getNetworks().getNetwork();
	return new ArrayList<BgpNetwork>();
    }

    /**
     * Returns the list of this router's neighbors.
     *
     * @return list of neighbors of this router.
     */
    public List<BgpNeighbor> getAllNeighbors() {
	if ((getNeighbors() != null) && (getNeighbors().getNeighbor() != null))
	    return getNeighbors().getNeighbor();
	return new ArrayList<BgpNeighbor>();
    }

}

