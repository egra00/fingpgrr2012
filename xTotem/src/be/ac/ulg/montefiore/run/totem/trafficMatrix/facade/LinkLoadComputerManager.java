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
package be.ac.ulg.montefiore.run.totem.trafficMatrix.facade;

import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.*;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManagerListener;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import java.util.*;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
* - 14-Jun-2007: use TmLoadComputer instead of LinkLoadStrategy interface (GMO)
* - 27-Feb-2008: rewrite interface and implementation (GMO)
*/

/**
 * Maintains a set of {@link be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer} corresponding to
 * existing domains in {@link be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager} and existing traffic
 * matrices in {@link TrafficMatrixManager}.
 *<p>
 * It contains a default computer per domain which is the selected one. Each LinkLoadComputer is identified by a
 * String id which is unique per domain.
 * <p>
 * The role of the LinkLoadComputerManager is three-fold:
 * <ul>
 * <li>Maintains an association between an id and a LinkLoadComputer object</li>
 * <li>Destroys all LinkLoadComputer objects associated with a domain/traffic matrix when the domain/traffic matrix
 * is removed from its manager</li>
 * <li>Assure uniqueness of the LinkLoadComputers objects in the manager. LinkLoadComputer objects should override the
 * {@link Object#equals(Object)} methods.</li>
 * </ul>
 *
 * <p>Creation date: 10 mars 2006
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class LinkLoadComputerManager extends LinkLoadComputerManagerObserver implements TrafficMatrixManagerListener, InterDomainManagerListener {

    private static Logger logger = Logger.getLogger(LinkLoadComputerManager.class);

    private static int currentId = 0;

    /* associates domain ASID with default computer */
    private HashMap<Integer, String> defaultComputers = null;
    /* associates domain ASID with the LinkLoadComputer id with instance */
    private HashMap<Integer, HashMap<String, LinkLoadComputer>> computersByDomain = null;

    private static LinkLoadComputerManager instance = null;

    private LinkLoadComputerManager() {
        defaultComputers = new HashMap<Integer, String>();
        computersByDomain = new HashMap<Integer, HashMap<String, LinkLoadComputer>>();
        TrafficMatrixManager.getInstance().addListener(this);
        InterDomainManager.getInstance().addListener(this);
    }

    /**
     * Returns the unique instance, create it if necessary.
     * @return
     */
    public static LinkLoadComputerManager getInstance() {
        if (instance == null)
            instance = new LinkLoadComputerManager();
        return instance;
    }

    /**
     * Generates an unique string id for the specified domain.
     * @return
     */
    public String generateId(Domain domain) {
        String name;
        do {
            name = "Load " + currentId++;
        } while (computersByDomain.get(domain.getASID()) != null && computersByDomain.get(domain.getASID()).get(name) != null);
        return name;
    }

    /**
     * Generates an unused id for the specified domain, starting with the given prefix.
     * @param domain
     * @param prefix
     * @return
     */
    public String generateId(Domain domain, String prefix) {
        int num = 0;
        String name;
        do {
            name = prefix + " " + num++;
        } while (computersByDomain.get(domain.getASID()) != null && computersByDomain.get(domain.getASID()).get(name) != null);
        return name;
    }

    /**
     * remove listeners
     */
    public void destroy() {
        TrafficMatrixManager.getInstance().removeListener(this);
        InterDomainManager.getInstance().removeListener(this);
    }

    /**
     * Adds a LinkLoadComputer to the manager and make it start listening to events by calling
     * {@link be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer#startListening()}.
     * @param llc the LinkLoadComputer
     * @param addAsDefault Tells if it should be added as default for the LinkLoadComputer domain
     * @param id Id to associate to the LinkLoadComputer
     * @throws LinkLoadComputerIdException If a LinkLoadComputer with the same id already exists in the manager for the
     * same domain.
     * @throws LinkLoadComputerAlreadyExistsException If a LinkLoadComputer that is equal to the given one already exists
     * in the manager.
     */
    public void addLinkLoadComputer(LinkLoadComputer llc, boolean addAsDefault, String id) throws LinkLoadComputerIdException, LinkLoadComputerAlreadyExistsException {
        Domain domain = llc.getDomain();

        HashMap<String, LinkLoadComputer> map = computersByDomain.get(domain.getASID());

        if (map == null) {
            map = new HashMap<String, LinkLoadComputer>();
            computersByDomain.put(domain.getASID(), map);
        }

        if (map.get(id) != null)
            throw new LinkLoadComputerIdException("There is already a LinkLoadComputer with id " + id);

        if (map.containsValue(llc)) {
            throw new LinkLoadComputerAlreadyExistsException("A similar LinkLoadComputer already exists");
        }

        map.put(id, llc);

        llc.startListening();

        notifyAddLinkLoadComputer(llc);

        // Add as default also if there is no default for that domain.
        if (addAsDefault || defaultComputers.get(domain.getASID()) == null) {
            defaultComputers.put(llc.getDomain().getASID(), id);
            notifyChangeDefaultLinkLoadComputer(llc.getDomain().getASID(), llc);
        }
        return;
    }

    /**
     * Add the given LinkLoadComputer to the manager. It will be added as default with a generated id. Make it start
     * listening to events by calling {@link be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer#startListening()}.
     * @param llc
     * @return the id to which the LinkLoadComputer is associated.
     * @throws LinkLoadComputerAlreadyExistsException  If a LinkLoadComputer that is equal to the given one already exists
     */
    public String addLinkLoadComputer(LinkLoadComputer llc) throws LinkLoadComputerAlreadyExistsException {
        try {
            String id = generateId(llc.getDomain());
            addLinkLoadComputer(llc, true, id);
            return id;
        } catch (LinkLoadComputerIdException e) {
            e.printStackTrace();
            logger.fatal(e);
        }
        return null;
    }

    /**
     * Returns a set of LinkLoadComputer for the given domain.
     * @param domain
     * @return
     */
    public Set<LinkLoadComputer> getLinkLoadComputers(Domain domain) {
        if (domain == null) throw new IllegalArgumentException("Domain is null.");
        Set<LinkLoadComputer> lst = new HashSet<LinkLoadComputer>();
        HashMap<String, LinkLoadComputer> map = computersByDomain.get(domain.getASID());
        if (map != null)
            lst.addAll(map.values());
        return lst;
    }

    /**
     * Returns a set of all LinkLoadComputers of the given domain and their id.
     * @param domain
     * @return
     */
    public Set<Pair<String, LinkLoadComputer>> getLinkLoadComputersWithId(Domain domain) {
        if (domain == null) throw new IllegalArgumentException("Domain is null.");
        HashMap<String, LinkLoadComputer> map = computersByDomain.get(domain.getASID());
        if (map != null) {
            Set<Pair<String, LinkLoadComputer>> set = new HashSet<Pair<String, LinkLoadComputer>>(map.size());
            for (Map.Entry<String, LinkLoadComputer> entry : map.entrySet()) {
                set.add(new Pair<String, LinkLoadComputer>(entry.getKey(), entry.getValue()));
            }
            return set;
        }
        return new HashSet<Pair<String, LinkLoadComputer>>(0);
    }

    /**
     * Returns a set of all ids associated with the given domain
     * @param domain
     * @return
     */
    public Set<String> getLinkLoadComputerIds(Domain domain) {
        if (domain == null) throw new IllegalArgumentException("Domain is null.");
        Set<String> lst = new HashSet<String>();
        HashMap<String, LinkLoadComputer> map = computersByDomain.get(domain.getASID());
        if (map != null)
            lst.addAll(map.keySet());
        return lst;
    }

    /**
     * Return the id associated with a LinkLoadComputer that is equal to the given one.
     * @param llc
     * @return
     * @throws InvalidLinkLoadComputerException If no LinkLoadComputer equals to the given one can be found.
     */
    public String getLinkLoadComputerId(LinkLoadComputer llc) throws InvalidLinkLoadComputerException {
        Domain domain = llc.getDomain();

        HashMap<String, LinkLoadComputer> map = computersByDomain.get(domain.getASID());

        if (map == null) {
            throw new InvalidLinkLoadComputerException("Link load computer not found (no llc for domain).");
        }

        String id;

        for (Map.Entry<String, LinkLoadComputer> entry : map.entrySet()) {
            LinkLoadComputer myLLC = entry.getValue();
            if (myLLC.equals(llc)) {
                id = entry.getKey();
                return id;
            }
        }

        throw new InvalidLinkLoadComputerException("Link load computer not found.");
    }

    /**
     * Returns a pair associating a LinkLoadComputer that is equal to the given one with its id in the manager.
     * @param llc
     * @return
     * @throws InvalidLinkLoadComputerException If no LinkLoadComputer equals to the given one can be found.
     */
    public Pair<String, LinkLoadComputer> getLinkLoadComputer(LinkLoadComputer llc) throws InvalidLinkLoadComputerException {
        Domain domain = llc.getDomain();

        HashMap<String, LinkLoadComputer> map = computersByDomain.get(domain.getASID());

        if (map == null) {
            throw new InvalidLinkLoadComputerException("Link load computer not found (no llc for domain).");
        }

        for (Map.Entry<String, LinkLoadComputer> entry : map.entrySet()) {
            LinkLoadComputer myLLC = entry.getValue();
            if (myLLC.equals(llc)) {
                return new Pair<String, LinkLoadComputer>(entry.getKey(), myLLC);
            }
        }

        throw new InvalidLinkLoadComputerException("Link load computer not found.");
    }

    /**
     * Returns the default LinkLoadComputer for the specified domain.
     * @param domain
     * @return
     * @throws InvalidLinkLoadComputerException If there is no default LinkLoadComputer for the given domain.
     */
    public LinkLoadComputer getDefaultLinkLoadComputer(Domain domain) throws InvalidLinkLoadComputerException {
        String id = getDefaultLinkLoadComputerId(domain);
        return computersByDomain.get(domain.getASID()).get(id);
    }

    /**
     * Returns the id of the default LinkLoadComputer of the given domain.
     * @param domain
     * @return
     * @throws InvalidLinkLoadComputerException If there is no default LinkLoadComputer for the given domain.
     */
    public String getDefaultLinkLoadComputerId(Domain domain) throws InvalidLinkLoadComputerException {
        if (domain == null) throw new IllegalArgumentException("Domain is null");
        if (defaultComputers.get(domain.getASID()) == null) {
            throw new InvalidLinkLoadComputerException("LinkLoadComputer not found.");
        }
        String id = defaultComputers.get(domain.getASID());
        return id;
    }

    /**
     * Returns the LinkLoadComputer corresponding to the given id.
     * @param id
     * @param domain
     * @return
     * @throws InvalidLinkLoadComputerException if no LinkLoadComputers exists with this id.
     */
    public LinkLoadComputer getLinkLoadComputer(Domain domain, String id) throws InvalidLinkLoadComputerException {
        if (domain == null) throw new IllegalArgumentException("Domain is null");
        HashMap<String, LinkLoadComputer> map = computersByDomain.get(domain.getASID());
        if (map == null) throw new InvalidLinkLoadComputerException("LinkLoadComputer not found (no llc for the specified domain).");
        LinkLoadComputer llc = map.get(id);
        if (llc == null) throw new InvalidLinkLoadComputerException("LinkLoadComputer not found (no llc corresponding to id: " + id + ").");
        return llc;
    }

    /**
    * Sets a LinkLoadComputer as the default one. A linkLoadComputer that is equal to the given one should be in manager.
    * @param llc
    * @throws InvalidLinkLoadComputerException if the linkLoadComputer cannot be found in the manager.
    */
    public void setDefaultLinkLoadComputer(LinkLoadComputer llc) throws InvalidLinkLoadComputerException {
        if (llc == null) throw new IllegalArgumentException("LinkLoadComputer is null.");;

        try {
            int asId = llc.getDomain().getASID();
            HashMap<String, LinkLoadComputer> map = computersByDomain.get(asId);
            for (Map.Entry<String, LinkLoadComputer> entry : map.entrySet()) {
                LinkLoadComputer curLlc = entry.getValue();
                if (curLlc.equals(llc)) {
                    defaultComputers.put(asId, entry.getKey());
                    notifyChangeDefaultLinkLoadComputer(asId, curLlc);
                    break;
                }
            }
        } catch (NullPointerException ex) {
            logger.warn("Trying to set as default inexistent LinkLoadComputer");
            throw new InvalidLinkLoadComputerException("LinkLoadComputer not found.");
        }
    }

    /**
     * Sets the LinkLoadComputer identified by <code>id</code> as the default one.
     * @param id
     * @throws InvalidLinkLoadComputerException If the LinkLoadComputer with id <code>id</code> cannot be found in the manager.
     */
    public void setDefaultLinkLoadComputer(Domain domain, String id) throws InvalidLinkLoadComputerException {
        LinkLoadComputer llc = computersByDomain.get(domain.getASID()).get(id);
        if (llc == null) throw new InvalidLinkLoadComputerException("Link load computer with id: " + id + "not found.");
        setDefaultLinkLoadComputer(llc);
    }

    /**
     * Remove a LinkLoadComputer that is equal to the given one from the manager and destroys it, after signaling the
     * event {@link #notifyRemoveLinkLoadComputer(be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer)}.
     * @param llc
     */
    public void removeLinkLoadComputer(LinkLoadComputer llc) {
        if (llc == null) throw new IllegalArgumentException("Link load computer is null");

        int asId = llc.getDomain().getASID();

        HashMap<String, LinkLoadComputer> map = computersByDomain.get(asId);
        if (map == null) {
            logger.warn("Trying to remove inexistent LinkLoadComputer");
            return;
        }

        LinkLoadComputer foundLLC = null;
        String id = null;
        for (Map.Entry<String, LinkLoadComputer> entry : map.entrySet()) {
            if (entry.getValue().equals(llc)) {
                map.remove(entry.getKey());
                foundLLC = entry.getValue();
                id = entry.getKey();
                break;
            }
        }
        if (foundLLC == null) {
            logger.warn("Trying to remove inexistent LinkLoadComputer");
            return;
        }

        notifyRemoveLinkLoadComputer(foundLLC);

        if (defaultComputers.get(asId).equals(id)) {
            defaultComputers.remove(asId);
            notifyChangeDefaultLinkLoadComputer(asId, null);
        }

        if (foundLLC != null)
            foundLLC.destroy();
    }

    /**
     * Remove a LinkLoadComputer corresponding to the given id from the manager and destroys it, after signaling the
     * event {@link #notifyRemoveLinkLoadComputer(be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer)}.
     *
     * @param domain
     * @param id
     */
    public void removeLinkLoadComputer(Domain domain, String id) {
        int asId = domain.getASID();

        HashMap<String, LinkLoadComputer> map = computersByDomain.get(asId);
        if (map == null) {
            logger.warn("Trying to remove inexistent LinkLoadComputer");
            return;
        }

        LinkLoadComputer foundLLC = map.remove(id);

        if (foundLLC == null) {
            logger.warn("Trying to remove inexistent LinkLoadComputer");
            return;
        }

        notifyRemoveLinkLoadComputer(foundLLC);

        if (defaultComputers.get(asId).equals(id)) {
            defaultComputers.remove(asId);
            notifyChangeDefaultLinkLoadComputer(asId, null);
        }

        if (foundLLC != null)
            foundLLC.destroy();
    }

    /**
     * Destroys all the LinkLoadComputer associated with given domain, i.e. remove them from manager and call
     * {@link be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer#destroy()} for each of them. Then
     * signal {@link #notifyRemoveMultipleLinkLoadComputer()} .
     * @param domain
     */
    private void destroyLinkLoadComputers(Domain domain) {
        if (domain == null) throw new IllegalArgumentException("domain is null");

        HashMap<String, LinkLoadComputer> map = computersByDomain.get(domain.getASID());
        if (map == null) return;

        for (LinkLoadComputer llc : map.values()) {
            llc.destroy();
        }
        map.clear();
        defaultComputers.remove(domain.getASID());
        notifyRemoveMultipleLinkLoadComputer();
    }

    /**
     * Destroys all the linkLoadComputer associated with a given domain and a traffic matrix, i.e. remove them from
     * manager and call {@link LinkLoadComputerManager#destroy()} for each of them, then signal
     * {@link #notifyRemoveMultipleLinkLoadComputer()} .
     *
     * @param asId The ASID of the domain.
     * @param trafficMatrix The traffic Matrix.
     */
    private void destroyLinkLoadComputers(int asId, TrafficMatrix trafficMatrix) {
        HashMap<String, LinkLoadComputer> map = computersByDomain.get(asId);
        if (map == null) return;

        // some llc were removed
        boolean removed = false;

        List<String> removeList = new ArrayList<String>();
        for (Map.Entry<String, LinkLoadComputer> entry : map.entrySet()) {
            LinkLoadComputer llc = entry.getValue();
            boolean found = false;
            for (TrafficMatrix tm : llc.getTrafficMatrices()) {
                if (tm == trafficMatrix) {
                    found = true;
                    break;
                }
            }
            if (found) {
                removed = true;
                // destroy the llc
                llc.destroy();
                // add the key to the list llc to be removed
                removeList.add(entry.getKey());
            }
        }

        if (removed) {
            // remove the destroyed llcs from the manager 
            for (String s : removeList) {
                map.remove(s);
            }

            String id = defaultComputers.get(asId);
            LinkLoadComputer llc = computersByDomain.get(asId).get(id);
            if (llc != null) {
                boolean found = false;
                for (TrafficMatrix tm : llc.getTrafficMatrices()) {
                    if (tm == trafficMatrix) {
                        found = true;
                        break;
                    }
                }
                if (found) defaultComputers.remove(asId);
            }
            notifyRemoveMultipleLinkLoadComputer();
        }
    }

    public void addTrafficMatrixEvent(TrafficMatrix tm, int tmId) {
    }

    public void removeTrafficMatrixEvent(TrafficMatrix tm, int tmId) {
        int asId = tm.getASID();
        destroyLinkLoadComputers(asId, tm);
    }

    public void changeDefaultTrafficMatrixEvent(int asId, TrafficMatrix tm) {
    }

    public void addDomainEvent(Domain domain) {
    }

    public void removeDomainEvent(Domain domain) {
        destroyLinkLoadComputers(domain);
    }

    public void changeDefaultDomainEvent(Domain domain) {
    }
}
