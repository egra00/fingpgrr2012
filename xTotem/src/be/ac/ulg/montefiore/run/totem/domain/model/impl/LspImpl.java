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

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.LspBackupType;
import be.ac.ulg.montefiore.run.totem.domain.exception.*;
import be.ac.ulg.montefiore.run.totem.repository.model.LSPRoutingParameter;
import be.ac.ulg.montefiore.run.totem.repository.model.LSPPrimaryRoutingParameter;
import be.ac.ulg.montefiore.run.totem.repository.model.LSPDetourRoutingParameter;
import be.ac.ulg.montefiore.run.totem.repository.model.LSPBypassRoutingParameter;

import javax.xml.bind.JAXBException;
import java.util.*;

import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 * - 14-feb-2005: Added a new constructor for backup lsps.
 * - 20-Oct-2005: Added priority level check in constructor with diffserv parameters (now throw an exception if priority level doesn't exist) (GMO)
 * - 08-Nov-2005:  Added notification of lsp bw changes (GMO).
 * - 31-Mar-2006: getLspPath does not throw exceptions anymore. The default constructor now throws exceptions (GMO)
 * - 03-Apr-2006: add init method (GMO).
 * - 23-Oct-2006: change getSetupPreemption(), getHoldingPreemption() and getClassType() behaviour for backup lsps. Now,
 *                inherits the properties from the corresponding primary lsp.
 * - 22-Nov-2006: add addBackupLsp(.), removeBackupLsp(.), getBackups(). (GMO)
 * - 22-Nov-2006: add check in init() method to see if preemption levels exists (GMO)
 * - 09-Jan-2007: fix setReservation(.) method, now throws LinkCapacityExceeded (GMO)
 * - 11-May-2007: add status, getLspStatus(), setLspStatus(.), makePrimary(), setLspId(). Backup LSPs are down by default (GMO)
 * - 25-Sep-2007: add methods to deal with activated backups, and to signal change in node status (GMO)
 * - 25-Oct-2007: add status initialisation in setLspPath(), status is now a variable of this class and not from jaxb LspImpl anymore, (de)activateBackup now throws exception  (GMO)
 * - 29-Nov-2007: add isDetourLsp() method (GMO)
 * - 29-Nov-2007: add constructor for bypass LSPs (GMO)
 * - 05-Dec-2007: add isBypassLsp() method, use isDetourLsp() instead of isBackupLsp(). Can ativate a bypass lsp (GMO)
 * - 15-Jan-2008: setReservation now works if the lsp is not established (GMO)
 * - 26-Feb-2008: add class of service methods (GMO)
 * - 26-Feb-2008: rename isActivated() into isDetourActivated() (GMO)
 * - 26-Feb-2008: add setInitParameters(.) method (GMO)
 * - 26-Feb-2008: signals change in working path and in status on backup (de)activation and on link/node up/down events (GMO)
 */

/**
 * A Label Switched Path in a Domain
 *
 * A LspImpl is in UP state when all its links are UP. It is in DOWN state when one of its link is DOWN.
 * Change in link status are signalled through the "nodeUpEvent"-like methods
 *
 * <p>Creation date: 19-Jan-2005 15:47:36
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author Olivier Delcourt(delcourt@run.montefiore.ulg.ac.be)
 */
public class LspImpl extends be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.LspImpl implements Lsp {
    private static final Logger logger = Logger.getLogger(LspImpl.class);

    private DomainImpl domain;
    private Path path = null;

    /* backup lsps associated with this primary lsp */
    private Set<Lsp> backups;

    private int status = Lsp.STATUS_UP;

    public LspImpl() {
    }

    /**
     * Basic constructor to create a LSP with a bandwidth and a path
     *
     * @param domain
     * @param reservation
     * @param path
     */
    public LspImpl(Domain domain, String id, float reservation, Path path) {
        this.setDomain(domain);
        this.setId(id);
        this.setBw(reservation);
        this.setLspPath(path);
    }

    /**
     * More advanced constructor to create a primary lsp with all diffserv parameters.
     * @param domain
     * @param id
     * @param reservation
     * @param path
     * @param classType
     * @param holdingPreemption
     * @param setupPreemption
     */
    public LspImpl(Domain domain, String id, float reservation, Path path, int classType, int holdingPreemption, int setupPreemption) throws DiffServConfigurationException {
        this.setDomain(domain);
        this.setId(id);
        this.setBw(reservation);
        this.setLspPath(path);
        this.setDiffServParameters(classType, setupPreemption, holdingPreemption);
    }

    /**
     * Advanced constructor to create a detour backup lsp.
     * @param domain
     * @param protectedLspId
     * @param backupId
     * @param lspBackupType should be {@link LspBackupType#DETOUR_LOCAL} or {@link LspBackupType#DETOUR_E_2_E}
     * @param protectedLinks
     */
    public LspImpl(Domain domain, String protectedLspId, String backupId, Path path, LspBackupType lspBackupType, Collection<Link> protectedLinks) {
        if (lspBackupType == LspBackupType.BYPASS) throw new IllegalArgumentException("Backup type should be detour");

        this.setDomain(domain);
        this.setId(backupId);
        this.setLspPath(path);
        try {
            this.setBw(domain.getLsp(protectedLspId).getReservation());
        }catch (LspNotFoundException e){
            e.printStackTrace();
        }

        ObjectFactory factory = new ObjectFactory();


        try{
            BackupType backupType = factory.createLspBackupType();
            backupType.setProtectedLsp(protectedLspId);
            backupType.setType(lspBackupType);
            BackupType.ProtectedLinksType protectedLinksType = factory.createLspBackupTypeProtectedLinksType();
            for (Link l : protectedLinks) {
                protectedLinksType.getProtectedLink().add(l.getId());
            }
            backupType.setProtectedLinks(protectedLinksType);
            this.setBackup(backupType);
        }catch(JAXBException e){
            e.printStackTrace();
        }
    }

    /**
     * Advanced constructor to create a backup bypass lsp.
     * @param domain
     * @param backupId
     * @param protectedLinks
     */
    public LspImpl(Domain domain, String backupId, float bw, Path path, Collection<Link> protectedLinks) {
        this.setDomain(domain);
        this.setId(backupId);
        this.setLspPath(path);
        this.setBw(bw);

        ObjectFactory factory = new ObjectFactory();


        try{
            BackupType backupType = factory.createLspBackupType();
            backupType.setType(LspBackupType.BYPASS);
            BackupType.ProtectedLinksType protectedLinksType = factory.createLspBackupTypeProtectedLinksType();
            for (Link l : protectedLinks) {
                protectedLinksType.getProtectedLink().add(l.getId());
            }
            backupType.setProtectedLinks(protectedLinksType);
            this.setBackup(backupType);
        }catch(JAXBException e){
            e.printStackTrace();
        }
    }

    /**
     * Advanced constructor to create a backup bypass lsp, specifying the classtype
     * @param domain
     * @param backupId
     * @param protectedLinks
     */
    public LspImpl(Domain domain, String backupId, float bw, Path path, Collection<Link> protectedLinks, int classType) throws DiffServConfigurationException {
        this(domain, backupId, bw, path, protectedLinks);
        int setupPreemption = domain.getMinPriority(classType);
        int holdingPreemption = setupPreemption;
        setDiffServParameters(classType, setupPreemption, holdingPreemption);
    }

    /**
     * Advanced constructor to create a backup bypass lsp, specifying diffserv parameters
     * @param domain
     * @param backupId
     * @param protectedLinks
     */
    public LspImpl(Domain domain, String backupId, float bw, Path path, Collection<Link> protectedLinks, int classType, int holdingPreemption, int setupPreemption) throws DiffServConfigurationException {
        this(domain, backupId, bw, path, protectedLinks);
        setDiffServParameters(classType, setupPreemption, holdingPreemption);
    }


    /**
     * Initialise the LSP : set a reference to the domain and create the LSP path.
     * This method must be called prior to usage of the instance.
     * Also checks if the priority is defined in the domain.
     * @param domain The domain to which the LSP belongs
     * @throws InvalidPathException if the path is invalid (not continuous or if a link or node cannot be found)
     * @throws DiffServConfigurationException if the setup or holding priority is not defined in the domain.
     */
    public void init(Domain domain) throws InvalidPathException, DiffServConfigurationException {
        setDomain(domain);
        if (!domain.isExistingPriority(getSetupPreemption(), getCT())) {
            throw new DiffServConfigurationException("Non existing priority level.");
        }
        if (!domain.isExistingPriority(getHoldingPreemption(), getCT())) {
            throw new DiffServConfigurationException("Non existing priority level.");
        }

        try {
            List<Link> linkList = new ArrayList<Link>(this.getPath().getLink().size());
            for (int i = 0; i < this.getPath().getLink().size(); i++) {
                linkList.add(domain.getLink((String) getPath().getLink().get(i)));
            }
            path = new PathImpl(domain);
            path.createPathFromLink(linkList);
        } catch (LinkNotFoundException e) {
            throw new InvalidPathException("Link not found.");
        } catch (NodeNotFoundException e) {
            throw new InvalidPathException("Node not found.");
        } catch (InvalidPathException e) {
            throw e;
        }
    }

    /**
     * Initilise the lsp with the given parameters
     * @param param
     */
    public void setInitParameters(LSPRoutingParameter param) throws IllegalArgumentException {
        if (param.isSetBandwidth()) setBw(param.getBandwidth());
        //if (param.getLspId() != null) super.setId(param.getLspId());
        if (param.getAcceptedCos() != null) {
            for (String s : param.getAcceptedCos()) {
                try {
                    addAcceptedClassOfService(s);
                } catch (ClassOfServiceNotFoundException e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("Class of service exception");
                } catch (ClassOfServiceAlreadyExistException e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("Class of service exception");
                }
            }
        }

        if (param instanceof LSPPrimaryRoutingParameter) {
            LSPPrimaryRoutingParameter p = (LSPPrimaryRoutingParameter)param;
            if (p.isSetSetup() && p.isSetHolding() && p.isSetClassType()) {
                try {
                    setDiffServParameters(p.getClassType(), p.getSetup(), p.getHolding());
                } catch (DiffServConfigurationException e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("Diffserv configuration exception");
                }
            } else {
                logger.debug("No diffserv parameters given");
            }
            if (p.isSetMetric()) setMetric(p.getMetric());
            if (p.isSetMaxRate()) setMaxRate(p.getMaxRate());
        } else if (param instanceof LSPDetourRoutingParameter) {
            LSPDetourRoutingParameter p = (LSPDetourRoutingParameter)param;

            ObjectFactory factory = new ObjectFactory();

            try{
                BackupType backupType;
                if (isSetBackup())
                    backupType = getBackup();
                else
                    backupType = factory.createLspBackupType();
                backupType.setProtectedLsp(p.getProtectedLSP());
                switch (p.getProtectionType()) {
                    case LSPDetourRoutingParameter.GLOBAL:
                        backupType.setType(LspBackupType.DETOUR_E_2_E);
                        break;
                    case LSPDetourRoutingParameter.LOCAL:
                        backupType.setType(LspBackupType.DETOUR_LOCAL);
                        break;
                    default:
                        logger.warn("Backup type unknown");
                }
                this.setBackup(backupType);
            }catch(JAXBException e){
                e.printStackTrace();
            }
        } else if (param instanceof LSPBypassRoutingParameter) {
            LSPBypassRoutingParameter p = (LSPBypassRoutingParameter)param;
            if (p.isSetSetup() && p.isSetHolding() && p.isSetClassType()) {
                try {
                    setDiffServParameters(p.getClassType(), p.getSetup(), p.getHolding());
                } catch (DiffServConfigurationException e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("Diffserv configuration exception");
                }
            } else {
                logger.debug("No diffserv parameters given");
            }
        }
    }

    public void setElementId(String id) throws IdException {
        try {
            domain.getLsp(getId());
            throw new IdException("Cannot change lsp id when lsp is in the domain.");
        } catch (LspNotFoundException e) {
            final String oldId = getId();
            super.setId(id);
            if (getBackups() != null) {
                try {
                    for (Lsp bLsp : getBackups()) {
                        bLsp.setProtectedLsp(id);
                    }
                } catch (BadLspTypeException ex) {
                    /* revert changes */
                    logger.error(ex);
                    for (Lsp bLsp : getBackups()) {
                        try {
                            bLsp.setProtectedLsp(oldId);
                        } catch (BadLspTypeException exx) {
                        }
                    }
                    super.setId(oldId);
                    throw new IdException("Cannot set Id of backups.");
                }
            }
        }
    }

    public Domain getDomain() {
        return domain;
    }

    /**
     * Change the reservation of the lsp.
     * @param bw
     * @throws LinkCapacityExceededException
     */
    public void setReservation(float bw) throws LinkCapacityExceededException {

        try {
            domain.getLsp(getId());
        } catch (LspNotFoundException e) {
            // the lsp is not established in the domain, so we can change the reservation without a problem.
            setBw(bw);
            return;
        }

        float oldBw = this.getBw();
        try {
            domain.getBandwidthManagement().removeLsp(this);
            this.setBw(bw);
            domain.getBandwidthManagement().addLsp(this);
            domain.getObserver().notifyLspReservationChange(this);
        } catch (DiffServConfigurationException e) {
            //should not happen
            this.setBw(oldBw);
            e.printStackTrace();
        } catch (LinkCapacityExceededException e) {
            this.setBw(oldBw);
            throw e;
        } catch (LspNotFoundException e) {
            //should not happen
            this.setBw(oldBw);
            e.printStackTrace();
        }
    }
    
    /**
     * Get the reservation of a LSP
     * 
     * @return
     */
    public float getReservation() {
        return this.getBw();
    }

    /**
     * Get the path of the LSP
     *
     * @return the path
     */
    public Path getLspPath() {
        return path;
    }

    /**
     * If nocheck is true, returns the current working path even if some links are down.
     * Else, returns the working path. <br>
     *
     * The working path is the current path used for routing on this LSP. It is different from the normal path since the
     * lsp can be routed temporarily on backups.
     *
     * @param nocheck
     * @throws InvalidPathException
     * @return
     */
    public Path getWorkingPath(boolean nocheck) throws InvalidPathException {

        Path routingPath = new PathImpl(domain);

        if (!isBackupLsp()) {
            List<Lsp> activatedLsps = getActivatedBackups();

            List<Link> linkPath = new ArrayList<Link>();
            // position in activatedLsps
            int k = 0;
            int i = 0;
            while (i < getLspPath().getLinkPath().size()) {
                Node currentNode = getLspPath().getNodePath().get(i);
                if (k < activatedLsps.size() && currentNode == activatedLsps.get(k).getLspPath().getSourceNode()) {
                    for (Link l : activatedLsps.get(k).getLspPath().getLinkPath()) {
                        linkPath.add(l);
                        if (l.getLinkStatus() == Link.STATUS_DOWN) {
                            logger.debug("Working path not UP for primary: " + getId());
                            if (!nocheck)
                                throw new InvalidPathException("Link " + l.getId() + " is DOWN.");
                        }
                    }
                    i = getLspPath().getNodePath().indexOf(activatedLsps.get(k).getLspPath().getDestinationNode());
                    k++;
                } else {
                    Link l = getLspPath().getLinkPath().get(i);
                    if (l.getLinkStatus() == Link.STATUS_DOWN) {
                        //logger.debug("Working path not UP for primary: " + getId());
                        if (!nocheck)
                            throw new InvalidPathException("Link " + l.getId() + " is DOWN.");
                    }
                    linkPath.add(l);
                    i++;
                }
            }
            if (k != activatedLsps.size()) {
                logger.fatal("Too many activated lsps for primary: " + getId());
                String msg = "Activated LSPs: ";
                for (Lsp lsp : activatedLsps) {
                    msg += lsp.getId() + " ";
                }
                logger.debug(msg);
            }

            try {
                routingPath.createPathFromLink(linkPath);
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }

            return routingPath;
        } else { // backup
            //logger.debug("Working path of backup asked for lsp " + getId());
            throw new InvalidPathException("Working path of backup asked");
        }

    }

    /**
     *
     * Returns a path for the LSP that is routable, i.e. a path where all links are up.<br>
     *
     * @return
     * @throws InvalidPathException If no path can be found
     */
    public Path getWorkingPath() throws InvalidPathException {
          return getWorkingPath(false);
    }

    // sorted list
    private ArrayList<Lsp> activatedBackups = new ArrayList<Lsp>();

    /**
     * Returns a list of activated backup sorted by ingress
     *
     * @return
     */
    public List<Lsp> getActivatedBackups() {
        return (List<Lsp>)(activatedBackups.clone());
    }

    public boolean isDetourActivated() {
        try {
            LspImpl pLsp = (LspImpl)getProtectedLsp();
            return pLsp.activatedBackups.contains(this);
        } catch (LspNotFoundException e) {
            // primary lsp
            return true;
        }
    }


    public void activateBackup(Lsp backupLsp) throws LspNotFoundException {
        final class IngressComp implements Comparator<Lsp> {
            private List<Node> nodeList;

            public IngressComp() {
                this.nodeList = getLspPath().getNodePath();
            }

            public int compare(Lsp lsp, Lsp lsp1) {
                return nodeList.indexOf(lsp.getLspPath().getSourceNode()) - nodeList.indexOf(lsp1.getLspPath().getSourceNode());
            }
        }

        if (backupLsp.getLspStatus() != Lsp.STATUS_UP) {
            logger.error("Backup to activate is down: " + backupLsp.getId());
            throw new LspNotFoundException("Backup to activate is down: " + backupLsp.getId());
        }

        if (backupLsp.isDetourLsp()) {
            if (getBackups() != null && getBackups().contains(backupLsp)) {
                if (activatedBackups.contains(backupLsp)) {
                    logger.warn("Activating a backup that is already active: " + backupLsp.getId());
                } else {
                    activatedBackups.add(backupLsp);
                    Collections.sort(activatedBackups, new IngressComp());
                    logger.debug("Lsp " + backupLsp.getId() + " activated.");
                    domain.getObserver().notifyLspWorkingPathChange(this);
                    // Status may have changed.
                    try {
                        getWorkingPath();
                        setLspStatus(Lsp.STATUS_UP);
                    } catch (InvalidPathException e) {
                        setLspStatus(Lsp.STATUS_DOWN);
                    }
                }
            } else {
                logger.error("Backup to activate not found: " + backupLsp.getId());
                throw new LspNotFoundException("Backup to activate not found: " + backupLsp.getId());
            }
        } else if (backupLsp.isBypassLsp()) {
            if (getLspPath().getNodePath().contains(backupLsp.getLspPath().getSourceNode()) && getLspPath().getNodePath().contains(backupLsp.getLspPath().getDestinationNode())) {
                if (activatedBackups.contains(backupLsp)) {
                    logger.warn("Activating a backup that is already active: " + backupLsp.getId());
                } else {
                    activatedBackups.add(backupLsp);
                    Collections.sort(activatedBackups, new IngressComp());
                    logger.debug("Lsp " + backupLsp.getId() + " activated.");
                    domain.getObserver().notifyLspWorkingPathChange(this);
                    // Status may have changed.
                    try {
                        getWorkingPath();
                        setLspStatus(Lsp.STATUS_UP);
                    } catch (InvalidPathException e) {
                        setLspStatus(Lsp.STATUS_DOWN);
                    }
                }
            } else {
                logger.error("Backup to activate is not a bypass of this primary: " + backupLsp.getId());
                throw new LspNotFoundException("Backup to activate is not a bypass of this primary: " + backupLsp.getId());
            }
        } else {
            logger.error("Backup to activate is not a bypass and not a detour: " + backupLsp.getId());
            throw new LspNotFoundException("Backup to activate is not a bypass and not a detour: " + backupLsp.getId());
        }
    }

    public void deactivateBackup(Lsp backupLsp) throws LspNotFoundException {
        if (activatedBackups.remove(backupLsp)) {
            logger.debug("Lsp " + backupLsp.getId() + " deactivated.");
            domain.getObserver().notifyLspWorkingPathChange(this);
            // Status may have changed.
            try {
                getWorkingPath();
                setLspStatus(Lsp.STATUS_UP);
            } catch (InvalidPathException e) {
                setLspStatus(Lsp.STATUS_DOWN);
            }
        } else {
            logger.error("Backup to deactivate not found: " + backupLsp.getId());
            throw new LspNotFoundException("Backup to deactivate not found: " + backupLsp.getId());
        }
    }

    /**
     * Maintain the status of the lsp.
     * @param link
     */
    public void linkDownEvent(Link link) {
        if (isBackupLsp()) {
            if (getLspStatus() != Lsp.STATUS_DOWN) {
                setLspStatus(Lsp.STATUS_DOWN);
                if (isDetourLsp()) {
                    // if the detour was activated and became down, the primary also become down
                    if (isDetourActivated()) {
                        try {
                            ((LspImpl)getProtectedLsp()).setLspStatus(Lsp.STATUS_DOWN);
                        } catch (LspNotFoundException e) {
                            //should not happen
                            e.printStackTrace();
                        }
                    }
                } else {
                    // if the bypass became down, all primary lsps using that bypass also become down
                    try {
                        for (Link myLink : getProtectedLinks()) {
                            List<Lsp> lsps = domain.getLspsOnLink(myLink);
                            for (Lsp lsp : lsps) {
                                LspImpl lspI = (LspImpl)lsp;
                                if (!lsp.isBackupLsp() && lspI.activatedBackups.contains(this)) {
                                    lspI.setLspStatus(Lsp.STATUS_DOWN);
                                }
                            }
                        }
                    } catch (LinkNotFoundException e) {
                        //should not happen
                        e.printStackTrace();
                    }
                }
            }
        } else { // primary lsp
            try {
                if (getWorkingPath(true).containsLink(link)) {
                    setLspStatus(Lsp.STATUS_DOWN);
                }
            } catch (InvalidPathException e) {
                e.printStackTrace();
            }
        }
    }

    public void nodeDownEvent(Node node) {
        if (isBackupLsp()) {
            if (getLspStatus() != Lsp.STATUS_DOWN) {
                setLspStatus(Lsp.STATUS_DOWN);
                if (isDetourLsp()) {
                    // if the detour was activated and became down, the primary also become down
                    if (isDetourActivated()) {
                        try {
                            ((LspImpl)getProtectedLsp()).setLspStatus(Lsp.STATUS_DOWN);
                        } catch (LspNotFoundException e) {
                            //should not happen
                            e.printStackTrace();
                        }
                    }
                } else {
                    // if the bypass became down, all primary lsps using that bypass also become down
                    try {
                        for (Link myLink : getProtectedLinks()) {
                            List<Lsp> lsps = domain.getLspsOnLink(myLink);
                            for (Lsp lsp : lsps) {
                                LspImpl lspI = (LspImpl)lsp;
                                if (!lsp.isBackupLsp() && lspI.activatedBackups.contains(this)) {
                                    lspI.setLspStatus(Lsp.STATUS_DOWN);
                                }
                            }
                        }
                    } catch (LinkNotFoundException e) {
                        //should not happen
                        e.printStackTrace();
                    }
                }
            }
        } else { // primary lsp
            try {
                if (getWorkingPath(true).getNodePath().contains(node)) {
                    setLspStatus(Lsp.STATUS_DOWN);
                }
            } catch (InvalidPathException e) {
                e.printStackTrace();
            }
        }
    }

    public void linkUpEvent(Link link) {
        if (isBackupLsp()) {
            if (getLspStatus() != Lsp.STATUS_UP) {
                boolean isUp = true;
                for (Link l : getLspPath().getLinkPath()) {
                    if (l.getLinkStatus() == Link.STATUS_DOWN) {
                        isUp = false;
                        break;
                    }
                }
                if (isUp) {
                    setLspStatus(Lsp.STATUS_UP);
                    if (isDetourLsp()) {
                        // if the detour was activated and became up, the primary may also become up
                        if (isDetourActivated()) {
                            try {
                                getProtectedLsp().getWorkingPath();
                                ((LspImpl)getProtectedLsp()).setLspStatus(Lsp.STATUS_UP);
                            } catch (InvalidPathException e) {
                                // the lsp is still down
                            } catch (LspNotFoundException e) {
                                //should not happen
                                e.printStackTrace();
                            }
                        }
                    } else {
                        // if the bypass became up, all primary lsps using that bypass may also become up
                        try {
                            for (Link myLink : getProtectedLinks()) {
                                List<Lsp> lsps = domain.getLspsOnLink(myLink);
                                for (Lsp lsp : lsps) {
                                    LspImpl lspI = (LspImpl)lsp;
                                    if (!lsp.isBackupLsp() && lspI.activatedBackups.contains(this)) {
                                        try {
                                            lspI.getWorkingPath();
                                            lspI.setLspStatus(Lsp.STATUS_UP);
                                        } catch (InvalidPathException e) {
                                            // the lsp is still down
                                        }
                                    }
                                }
                            }
                        } catch (LinkNotFoundException e) {
                            //should not happen
                            e.printStackTrace();
                        }
                    }
                }
            }
        } else { // primary lsp
            try {
                getWorkingPath();
                setLspStatus(Lsp.STATUS_UP);
            } catch (InvalidPathException e) {
                e.printStackTrace();
            }
        }
    }

    public void nodeUpEvent(Node node) {
        boolean up = true;
        for (Link l : getLspPath().getLinkPath()) {
            if (l.getLinkStatus() == Link.STATUS_DOWN) {
                up = false;
                break;
            }
        }
        if (up) {
            setLspStatus(Lsp.STATUS_UP);
        }
    }

    /**
     * Set a new path for a LSP. also sets the status according to the node/links status
     *
     * @param path
     */
    public void setLspPath(Path path) {
        this.path = path;
        /*System.out.println("SetLspPath of " + path.getLinkPath().size() + " links");
        for (int i = 0; i < path.getLinkPath().size(); i++) {
        System.out.println("Link Id : " +  path.getLinkPath().get(i).getId());
        }
        */

        PathType pathType = this.getPath();
        if (pathType == null) {
            ObjectFactory of = new ObjectFactory();
            //of.c
            try {
                pathType = of.createLspPathType();
                this.setPath(pathType);
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        pathType.getLink().clear();
        List<Link> linkList = path.getLinkPath();
        boolean isUp = true;
        for (int i = 0; i < linkList.size(); i++) {
            pathType.getLink().add(i,linkList.get(i).getId());
            if (linkList.get(i).getLinkStatus() == Link.STATUS_DOWN)
                isUp = false;
        }
        this.setLspStatus(isUp ? Lsp.STATUS_UP : Lsp.STATUS_DOWN);
        //this.setPath(pathType);
    }

    /**
     * Set the domain reference
     *
     * @param domain
     */
    public void setDomain(Domain domain) {
        this.domain = (DomainImpl) domain;
    }

    /**
     * Gets the DiffServ class type of the LSP, the diffserv class of the primary lsp if it is a backup, or the class
     * type of the minimum priority if it is not defined.
     *
     * @return
     */
    public int getCT()  {
        if (this.getDiffServ() != null) {
            return this.getDiffServ().getCt();
        }
        if (isDetourLsp()) {
            try {
                return getProtectedLsp().getCT();
            } catch (LspNotFoundException e) {
                //should not happen
                e.printStackTrace();
            }
        }
        //return domain.getMinCT();
        return domain.getClassType(domain.getMinPriority());
    }

    private void setDiffServParameters(int classType, int setupPreemption, int holdingPreemption) throws DiffServConfigurationException {
        if (!domain.isExistingPriority(setupPreemption, classType) || !domain.isExistingPriority(holdingPreemption, classType)) {
            throw new DiffServConfigurationException("Unknown Priority");
        }
        try {
            ObjectFactory factory = null;
            if (!this.isSetDiffServ()) {
                factory = new ObjectFactory();
                DiffServType diffServType = factory.createLspDiffServType();
                this.setDiffServ(diffServType);
            }
            this.getDiffServ().setCt(classType);
            if (!this.getDiffServ().isSetPreemption()) {
                if (factory == null) factory = new ObjectFactory();
                DiffServType.PreemptionType preemptionType = factory.createLspDiffServTypePreemptionType();
                this.getDiffServ().setPreemption(preemptionType);
            }
            this.getDiffServ().getPreemption().setHolding(holdingPreemption);
            this.getDiffServ().getPreemption().setSetup(setupPreemption);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets the holding preemption level of the LSP, the holding preemption level of the primary lsp if it is a backup,
     * or the holding preemption level of the minimum priority if it is not defined.
     *
     * @return
     */
    public int getHoldingPreemption() {
        if (this.getDiffServ() != null && this.getDiffServ().getPreemption() != null) {
            return this.getDiffServ().getPreemption().getHolding();
        }
        if (isDetourLsp()) {
            try {
                return getProtectedLsp().getHoldingPreemption();
            } catch (LspNotFoundException e) {
                e.printStackTrace();
            }
        }
        //return domain.getMinPreemptionLevel();
        return domain.getPreemptionLevel(domain.getMinPriority());
    }

    /**
     * Gets the setup preemption level of the LSP, the setup preemption level of the primary lsp if it is a backup,
     * or the setup preemption level of the minimum priority if it is not defined.
     *
     * @return
     */
    public int getSetupPreemption() {
        if (this.getDiffServ() != null && this.getDiffServ().getPreemption() != null) {
            return this.getDiffServ().getPreemption().getSetup();
        }
        if (isDetourLsp()) {
            try {
                return getProtectedLsp().getSetupPreemption();
            } catch (LspNotFoundException e) {
                e.printStackTrace();
            }
        }
        return domain.getPreemptionLevel(domain.getMinPriority());
    }

    /**
     * True if the LSP is a backup LSP and false otherwise
     *
     * @return True if the LSP is a backup LSP and false otherwise
     */
    public boolean isBackupLsp() {
        return this.getBackup() != null;
    }

    public boolean isDetourLsp() {
        return isBackupLsp() && this.getBackupType() != Lsp.BYPASS_BACKUP_TYPE;
    }

    public boolean isBypassLsp() {
        return isBackupLsp() && this.getBackupType() == Lsp.BYPASS_BACKUP_TYPE;
    }

    /**
     * If the LSP is a backup LSP, get the type of Backup :
     * <ul>
     *  <li> {@link Lsp.DETOUR_E2E_BACKUP_TYPE} if end to end detour backup</li>
     *  <li> {@link Lsp.DETOUR_LOCAL_BACKUP_TYPE} if local detour backup</li>
     *  <li> {@link Lsp.BYPASS_BACKUP_TYPE} if bypass backup</li>
     *  <li> -1 otherwise</li>
     * </ul>
     *
     * @return the type of backup LSP
     */
    public int getBackupType() {
        if (isBackupLsp()) {
            if (this.getBackup().getType().equals(LspBackupType.DETOUR_E_2_E))
                return Lsp.DETOUR_E2E_BACKUP_TYPE;
            else if (this.getBackup().getType().equals(LspBackupType.DETOUR_LOCAL))
                return Lsp.DETOUR_LOCAL_BACKUP_TYPE;
            else if (this.getBackup().getType().equals(LspBackupType.BYPASS))
                return Lsp.BYPASS_BACKUP_TYPE;
        }
        return -1;
    }

    /**
     * If the LSP is a backup LSP and protect a particular LSP, this method return this protected LSP
     * and null otherwise.
     *
     * @return
     */
    public Lsp getProtectedLsp() throws LspNotFoundException {
        if (isDetourLsp())
            return domain.getLsp(this.getBackup().getProtectedLsp());
        return null;
    }

    public void setProtectedLsp(String lspId) throws BadLspTypeException {
        if (isDetourLsp()) {
            getBackup().setProtectedLsp(lspId);
        } else {
            throw new BadLspTypeException("Lsp " + getId() + " not a detour backup lsp.");
        }
    }

    /**
     * If the LSP is a backup LSP and protect one or more links, this method return the list of protected links
     * and null otherwise.
     *
     * @return
     */
    public List<Link> getProtectedLinks() throws LinkNotFoundException {
        if (isBackupLsp() && this.getBackup().getProtectedLinks() != null) {
            List protectedLinks = this.getBackup().getProtectedLinks().getProtectedLink();
            List<Link> linkList = new ArrayList<Link>(protectedLinks.size());
            for (int i = 0; i < protectedLinks.size(); i++) {
                linkList.add(domain.getLink((String) protectedLinks.get(i)));
            }
            return linkList;
        }
        return null;
    }

    /**
     * Two LSPs are equal if they belong to the same domain and have the same
     * id.
     */
    public boolean equals(Object o) {
    	if(!(o instanceof LspImpl)) {
    		return false;
    	}
    	
    	LspImpl lsp = (LspImpl) o;
    	return (lsp.domain.getASID() == this.domain.getASID()) && (lsp.getId().equals(this.getId()));
    }
    
    public int hashCode() {
    	return domain.getASID() + this.getId().hashCode();
    }

    /**
     * Add a lsp in the list of backup lsps of this primary lsp.
     * This method should not be called directly.
     * @param lsp Backup LSP to add to this primary
     * @throws IllegalArgumentException if the given lsp does not correspond to a backup lsp of this one
     */
    public void addBackupLsp(Lsp lsp) {
        try {
            if (!lsp.isDetourLsp() || !lsp.getProtectedLsp().equals(this))
                throw new IllegalArgumentException("Lsp not a detour backup or not a backup of this lsp.");
        } catch (LspNotFoundException e) {
            throw new IllegalArgumentException("Primary LSP cannot be found.");
        }
        if (backups == null)
            backups = new HashSet<Lsp>();
        backups.add(lsp);
    }

    /**
     * Removes a LSP for the list of backups
     * @param lsp
     * @throws LspNotFoundException
     */
    public void removeBackupLsp(Lsp lsp) throws LspNotFoundException {
        if (backups != null && backups.contains(lsp)) {
            backups.remove(lsp);
        } else {
            throw new LspNotFoundException("Lsp " + lsp.getId() + " not found in the backups list.");
        }
    }

    /**
     * returns a set of the backups lsps
     * @return
     */
    public Set<Lsp> getBackups() {
        if (backups == null) return new HashSet<Lsp>();
        return backups;
    }

    public void makePrimary() {
        if (!isBackupLsp()) {
            logger.warn("Making a primary lsp out of an already primary lsp!");
            return;
        } else {
            try {
                setDiffServParameters(getCT(), getSetupPreemption(), getHoldingPreemption());
            } catch (DiffServConfigurationException e) {
                e.printStackTrace();
            }
        }
        setBackup(null);
        backups = null;
    }

    public int getLspStatus() {
        return status;
    }

    public void addAcceptedClassOfService(String name) throws ClassOfServiceNotFoundException, ClassOfServiceAlreadyExistException {
        if (!domain.isExistingClassOfService(name)) {
            throw new ClassOfServiceNotFoundException("Class of service " + name + " not defined in the domain.");
        }

        if (!isSetAcceptedCos()) {
            ObjectFactory factory = new ObjectFactory();
            try {
                setAcceptedCos(factory.createLspAcceptedCosType());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        if (getAcceptedCos().getCos().contains(name)) {
            throw new ClassOfServiceAlreadyExistException("Class of service " + name + " already accepted by LSP " + getId());
        }

        getAcceptedCos().getCos().add(name);

    }

    public void removeAcceptedClassOfService(String name) throws ClassOfServiceNotFoundException {
        if (isSetAcceptedCos() && getAcceptedCos().getCos().contains(name)) {
            getAcceptedCos().getCos().remove(name);
        } else {
            throw new ClassOfServiceNotFoundException("Class of service " + name + " already not accepted by LSP " + getId());
        }

        if (isSetAcceptedCos() && getAcceptedCos().getCos().size() <= 0) {
            getAcceptedCos().unsetCos();
        }
    }

    public List<String> getAcceptedClassesOfService() {
        if (isSetAcceptedCos()) {
            List<String> list = new ArrayList<String>();
            for (Object o : getAcceptedCos().getCos()) {
                list.add((String)o);
            }
            return list;
        } else if (isDetourLsp()) {
            try {
                return getProtectedLsp().getAcceptedClassesOfService();
            } catch (LspNotFoundException e) {
                logger.warn("Protected lsp not found");
                return domain.getClassesOfService();
            }
        } else {
            return domain.getClassesOfService();
        }
    }

    public boolean acceptClassOfService(String name) {
        if (!isSetAcceptedCos())
            return true;

        if (getAcceptedCos().getCos().contains(name))
            return true;
        
        return false;
    }

    private void setLspStatus(int status) {
        boolean changed = this.status != status;
        switch (status) {
            case STATUS_UP:
                this.status = STATUS_UP;
                break;
            case STATUS_DOWN:
                this.status = STATUS_DOWN;
                break;
            default:
                logger.error("Bad Lsp Status");
        }
        if (changed) domain.getObserver().notifyLspStatusChange(this);
    }
}
