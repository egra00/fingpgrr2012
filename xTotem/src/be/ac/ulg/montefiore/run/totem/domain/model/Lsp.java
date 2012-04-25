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

import be.ac.ulg.montefiore.run.totem.domain.exception.*;

import java.util.List;
import java.util.Set;

/*
 * Changes:
 * --------
 *
 * - 01-Feb-2006: extends DomainElement (JLE)
 * - 31-Mar-2006: getLspPath does not throw exceptions anymore (GMO)
 * - 03-Apr-2006: add init method (GMO).
 * - 22-Nov-2006: add addBackupLsp(.), removeBackupLsp(.), getBackups(). (GMO)
 * - 09-Jan-2007: setReservation now throws LinkCapacityExceeded (GMO)
 * - 11-May-2007: add status, getLspStatus(), setLspStatus(.), makePrimary(), setLspId() (GMO)
 * - 25-Sep-2007: add methods to deal with activated backups, and to signal change in node status (GMO)
 * - 25-Oct-2007: remove setLspStatusMethod, (de)activateBackup now throws exception (GMO)
 * - 29-Nov-2007: add isDetourLsp() method (GMO)
 * - 05-Dec-2007: add isBypassLsp() method (GMO)
 * - 26-Feb-2008: add class of service methods (GMO)
 * - 26-Feb-2008: rename isActivated() into isDetourActivated() (GMO) 
 */

/**
 * A Label Switched Path in a Domain
 *
 * <p>Creation date: 19-Jan-2005 15:47:36
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface Lsp extends DomainElement {

    public final int DETOUR_E2E_BACKUP_TYPE = 0;
    public final int DETOUR_LOCAL_BACKUP_TYPE = 1;
    public final int BYPASS_BACKUP_TYPE = 2;

    public static final int STATUS_UP = 0;
    public static final int STATUS_DOWN = 1;

    /**
     *
     * @param domain
     * @throws InvalidPathException If the LSP has invalid path.
     */
    public void init(Domain domain) throws InvalidPathException, DiffServConfigurationException;

    public float getMetric();
    
    public float getMaxRate();

    public float getReservation();
    
    public void setReservation(float bw) throws LinkCapacityExceededException;

    public Path getLspPath();

    /**
     * The working path is the current path used for routing on this LSP. It is different from the normal path since the
     * lsp can be routed temporarily on backups.
     *
     * @return
     * @throws InvalidPathException when called on a backup lsp or when the working path is down
     */
    public Path getWorkingPath() throws InvalidPathException;

    /**
     * Returns a list of activated backups (the list is ordered by ingress)
     * @return
     */
    public List<Lsp> getActivatedBackups();

    /**
     * Returns true if the backup is a detour and that it is activated for the corresponding primary
     * @return
     */
    public boolean isDetourActivated();

    /**
     * Activate the specified backup
     * @param backupLsp
     * @throws LspNotFoundException If the backup cannot be found, if it is not a backup or if the backup is a detour of
     * another lsp.
     */
    public void activateBackup(Lsp backupLsp) throws LspNotFoundException;

    /**
     * Deactivate the specified backup
     * @param backupLsp
     * @throws LspNotFoundException
     */
    public void deactivateBackup(Lsp backupLsp) throws LspNotFoundException;

    /**
     * A link came down on the lsp path. Sets the lsp status accordingly.
     * @param link
     */
    public void linkDownEvent(Link link);

    /**
     * A link came up on the lsp path. Sets the lsp status accordingly.
     * @param link
     */
    public void linkUpEvent(Link link);

    /**
     * A node came up on the lsp path. Sets the lsp status accordingly.
     * @param node
     */
    public void nodeUpEvent(Node node);

    /**
     * A node came up on the lsp path. Sets the lsp status accordingly.
     * @param node
     */
    public void nodeDownEvent(Node node);

    public void setLspPath(Path path);

    public int getCT();

    public int getHoldingPreemption();

    public int getSetupPreemption();

    //public void setDiffServParameters(int classType, int setupPreemption, int holdingPreemption) throws DiffServConfigurationException;

    public boolean isBackupLsp();
    public boolean isDetourLsp();
    public boolean isBypassLsp();

    public int getBackupType();

    public Lsp getProtectedLsp() throws LspNotFoundException;

    /**
     * Sets the protected lsp of a backup lsp.
     * @param lspId
     * @throws BadLspTypeException If the lsp is not a backup lsp
     */
    public void setProtectedLsp(String lspId) throws BadLspTypeException;

    public List<Link> getProtectedLinks() throws LinkNotFoundException;

    /**
     * Add a lsp in the list of backup lsps of this primary lsp.
     */
    public void addBackupLsp(Lsp lsp);
    
    /**
     * Removes a LSP for the list of backups
     * @param lsp
     * @throws LspNotFoundException
     */
    public void removeBackupLsp(Lsp lsp) throws LspNotFoundException;

    /**
     * returns a set of the backups lsps
     * @return
     */
    public Set<Lsp> getBackups();

    /**
     * Make a primary lsp out of the lsp object. Do nothing if the lsp is already a primary lsp.
     */
    public void makePrimary();

    /**
     * Returns the status of the LSP. Can be {@link #STATUS_DOWN} or {@link #STATUS_UP}. The status of the lsp
     * is changed via the linkUpEvent, linkDownEvent, nodeUpEvent, nodeDownEvent methods.
     * @return
     */
    public int getLspStatus();


    //Classes of Service

    /**
     * Add a class of service to the list of accepted classes of service.
     * @param name
     * @throws ClassOfServiceNotFoundException If the class is not defined in the domain
     * @throws ClassOfServiceAlreadyExistException If the class is already present in the accepted list
     */
    public void addAcceptedClassOfService(String name) throws ClassOfServiceNotFoundException, ClassOfServiceAlreadyExistException;

    /**
     * Remove a class of service to the list of accepted classes of service.
     * @param name
     * @throws ClassOfServiceNotFoundException if the class is not defined in the list
     */
    public void removeAcceptedClassOfService(String name) throws ClassOfServiceNotFoundException;

    /**
     * Returns the list of class of service names for which traffic can be routed on this LSP.
     * @return
     */
    public List<String> getAcceptedClassesOfService();

    /**
     * Tell if the lsp can accept traffic of the specified class.
     * @param name
     * @return true if the class is present in the accepted classes of service list, or if the list is empty, false otherwise.
     */
    public boolean acceptClassOfService(String name);
}
