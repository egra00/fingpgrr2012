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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.LocalDatabaseException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

import java.util.List;

/*
* Changes:
* --------
*
*/

/**
 * This interface must be implemented by all algorithms capable of routing detour LSPs.
 * <p/>
 * <p>Creation date: 28/11/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public interface LSPDetourRouting extends LSPBackupRouting {
    /**
     * Computes a detour backup for a LSP
     *
     * @param domain
     * @param param specifies the parameters for the backup routing algorithm
     * @return the detour computed
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException
     */
    public TotemActionList routeDetour(Domain domain, LSPDetourRoutingParameter param) throws RoutingException, NoRouteToHostException, LocalDatabaseException;

    /**
     * Returns a list of available algorithm specfic parameters that can be passed to the routeDetour method.
     * @return
     */
    public List<ParameterDescriptor> getDetourRoutingParameters();


}
