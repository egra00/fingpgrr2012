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
package be.ac.ulg.montefiore.run.totem.domain.model;

import be.ac.ulg.montefiore.run.totem.domain.exception.LinkCapacityExceededException;
import be.ac.ulg.montefiore.run.totem.domain.exception.DiffServConfigurationException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException;

import java.util.List;
import java.util.Collection;

/*
* Changes:
* --------
* - 18-Apr-2007: add recomputeRbw(.) method (GMO)
* - 17-Dec-2007: getReservableBandwidth now use a Collection object instead of a Set (GMO)
* - 17-Dec-2007: remove methods for temporary use (addLspTemp, removeLspTemp, cancelChanges) and a getSnapshot() method (GMO)
*/

/**
 * This class manages link bandwidth.
 * <p/>
 * <p>Creation date: 30/10/2006
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public interface BandwidthManagement {
    /**
     * Returns the list of lsps to be preempted when a lsp is to be added to the domain.
     *
     * @param lsp the Lsp to be added to the domain
     * @return
     * @throws LinkCapacityExceededException if not enough bandwidth can be freed to accept the lsp
     */
    List<Lsp> getPreemptList(Lsp lsp) throws LinkCapacityExceededException;

    /**
     * Initialise the Bandwidth Management object with the lsps already present in the domain.
     * This must be called prior to use.
     * @throws LinkCapacityExceededException if the calculated bandwidth exceed link capacity
     */
    void init() throws LinkCapacityExceededException;

    /**
     * Add lsp reservation. It adds some reservation to the links in the path of the lsp.
     * @param lsp
     * @throws LinkCapacityExceededException
     * @throws DiffServConfigurationException
     * @throws LspNotFoundException if the argument is a backup lsp and the primary lsp cannot be found in the domain
     */
    void addLsp(Lsp lsp) throws LinkCapacityExceededException, DiffServConfigurationException, LspNotFoundException;

    /**
     * Removes lsp reservation. It removes some reservation to the links in the path of the lsp.
     * @param lsp
     * @throws DiffServConfigurationException
     * @throws LspNotFoundException
     * @throws LinkCapacityExceededException
     */
    void removeLsp(Lsp lsp) throws DiffServConfigurationException, LspNotFoundException, LinkCapacityExceededException;

    /**
     * Returns the maximum reservable bandwidth at priority level <code>priority</code> for a
     * lsp traversing the link <code>link</code>. If <code>protectedLinks</code> is given, the reservable bandwidth for
     * a backup lsp protecting those links is returned, otherwise, a primary lsp is assumed.
     * <br>
     * This method takes the temporarily added and removed lsps into account.
     * @param priority
     * @param link
     * @param protectedLinks
     * @return
     */
    public float getReservableBandwidth(int priority, Link link, Collection<Link> protectedLinks);

    /**
     * returns true if preemption is supported by the class.
     * @return
     */
    public boolean usePreemption();

    /**
     * Recompute the reservable bandwidth (rbw array) of the link <code>link<code>.<br>
     * Warning: all rbw values should be set to 0 before calling this method.
     * @param link
     * @throws LinkCapacityExceededException
     */
    public void recomputeRbw(Link link) throws LinkCapacityExceededException;


    /**
     * Returns a bandwidth management instance that can be used temporarily. Adding or removing lsps to the returned
     * instance should not affects link reservation at all on the domain.
     * @return
     */
    public BandwidthManagement getSnapshot();
}
