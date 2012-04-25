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
package be.ac.ulg.montefiore.run.totem.domain.facade;

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.persistence.DomainFactory;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomainBuilder;
import be.ac.ulg.montefiore.run.totem.domain.exception.DomainAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.SimplifiedDomainException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;

import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

/*
 * Changes:
 * --------
 *
 * 16-Feb.-2005: useBWSharing (SB).
 * 16-Feb.-2005: update javadoc of loadDomain(String, boolean, boolean, boolean) (JL).
 * 29-Apr.-2005: add the removeAllDomains method (JL).
 * 7-Dec.-2005: add getNbDomains method (GMO).
 * 8-Dec.-2005: add the getAllDomains method (GMO).
 * 12-Jan.-2006: add an observer and notification of changes (GMO).
 * 20-Jan.-2006: when loading a domain that already exists and want it to be the default one,
 *   change the default domain to the existent one before throwing the exception (GMO).
 * 03-Apr.-2006: loadDomain now throw an excption if the domain is invalid (GMO).
 * 24-Apr-2006 : loadDomain now returns the loaded domain (GMO)
 * 10-Oct-2006 : add possiblily to load domain from a network (loadDomain(String host, int port, ...)) (GMO)
 * 31-May-2007 : remove domainChangeListeners when the domain is remove from the manager (GMO)
 */

/**
 * The InterDomainManager provide the access to all the domains. This class is a
 * singleton and the single instance can be obtain using getInstance() method.
 *
 * <p>Creation date: 12-Jan-2005 18:11:45
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author  Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class InterDomainManager extends InterDomainManagerObserver {

    private static Logger logger = Logger.getLogger(InterDomainManager.class.getName());

    private HashMap<Integer,Domain> domains = null; // all the domain indexed by their ASID
    private Domain defaultDomain = null; // the default domain
    private static InterDomainManager instance = null; // the single instance of the InterDomainManager

    /**
     * Basic private constructor
     */
    private InterDomainManager() {
        domains = new HashMap<Integer,Domain>();
    }

    /**
     * Get the single instance of the InterDomainManager.
     *
     * @return
     */
    public static InterDomainManager getInstance() {
        if (instance == null) {
            instance = new InterDomainManager();
        }
        return instance;
    }

    /**
     * Adds the domain <code>domain</code> to the <code>InterDomainManager</code>.
     * @param domain The <code>Domain</code> to add.
     * @throws DomainAlreadyExistException If there is already a domain with the same AS ID as <code>domain</code>.
     */
    public void addDomain(Domain domain) throws DomainAlreadyExistException {
        if(domains.containsKey(new Integer(domain.getASID()))) {
            throw new DomainAlreadyExistException("There is already a domain with the AS ID "+domain.getASID());
        }
        domains.put(new Integer(domain.getASID()), domain);
        notifyAddDomain(domain);
    }

    /**
     * Return the default domain
     *
     * @return the default domain
     */
    public Domain getDefaultDomain() {
        return defaultDomain;
    }

    /**
     * Set the ASID domain as the default domain
     *
     * @param ASID
     * @throws InvalidDomainException if the ASID domain is not loaded in the InterDomainManager
     */
    public void setDefaultDomain(int ASID) throws InvalidDomainException {
        defaultDomain = getDomain(ASID);
        notifyChangeDefaultDomain(defaultDomain);
    }

    /**
     * Return the domain with ASID as autonomous system ID
     *
     * @param ASID
     * @return the domain
     * @throws InvalidDomainException if the ASID domain is not loaded in the InterDomainManager
     */
    public Domain getDomain(int ASID) throws InvalidDomainException {
        if (domains.get(new Integer(ASID)) == null) {
            throw new InvalidDomainException("Domain " + ASID + " not initialised");
        } else {
            return domains.get(new Integer(ASID));
        }
    }


    /**
     * Load a domain from network. The toolbox will act as a client, connects to a server on specified host and port
     * and waits for the XML text correponding to the topology.
     * @param host
     * @param port
     * @param isDefaultDomain
     * @param removeMultipleLinks
     * @param useBwSharing
     * @return
     * @throws InvalidDomainException
     * @throws IOException
     * @throws DomainAlreadyExistException
     */
    public Domain loadDomain(String host, int port, boolean isDefaultDomain, boolean removeMultipleLinks, boolean useBwSharing) throws InvalidDomainException, IOException, DomainAlreadyExistException {
        long time = System.currentTimeMillis();
        Domain d = DomainFactory.loadDistantDomain(host, port, removeMultipleLinks, useBwSharing);
        time = System.currentTimeMillis() - time;
        logger.info("Load the domain " + d.getASID() + " from host " + host + " on port " + port + " takes " + time + " milliseconds");
        int ASID = d.getASID();
        if(domains.containsKey(ASID)) {
            if ((isDefaultDomain) || (defaultDomain == null)) {
                defaultDomain = domains.get(ASID);
                notifyChangeDefaultDomain(defaultDomain);
            }
            throw new DomainAlreadyExistException("There is already a domain with the ASID "+ASID);
        }
        domains.put(new Integer(ASID),d);
        if ((isDefaultDomain) || (defaultDomain == null))
            defaultDomain = d;
        notifyAddDomain(d);
        if (defaultDomain == d) notifyChangeDefaultDomain(d);
        return d;
    }

    public Domain loadDomain(InputStream is, boolean isDefaultDomain) throws InvalidDomainException, DomainAlreadyExistException {
        long time = System.currentTimeMillis();
        Domain d = DomainFactory.loadDomain(is);
        time = System.currentTimeMillis() - time;
        logger.info("Load the domain " + d.getASID() + " from input stream takes " + time + " milliseconds");
        int ASID = d.getASID();
        if(domains.containsKey(ASID)) {
            if (defaultDomain == null) {
                defaultDomain = domains.get(ASID);
                notifyChangeDefaultDomain(defaultDomain);
            }
            throw new DomainAlreadyExistException("There is already a domain with the ASID "+ASID);
        }
        domains.put(new Integer(ASID),d);
        if  (isDefaultDomain || (defaultDomain == null))
            defaultDomain = d;
        notifyAddDomain(d);
        if (defaultDomain == d) notifyChangeDefaultDomain(d);
        return d;
    }

    /**
     * Load a domain from a file
     *
     * @param fileName the domain file name
     * @param isDefaultDomain true if the domain must be the default domain and false otherwise
     * @param removeMultipleLinks true if you want to remove the multiple links and false otherwise
     * @return the loaded domain
     * @throws DomainAlreadyExistException If there is already a domain with the same ASID.
     * @throws InvalidDomainException If the domain fails to initialize.
     */
    public Domain loadDomain(String fileName, boolean isDefaultDomain, boolean removeMultipleLinks) throws DomainAlreadyExistException, InvalidDomainException {
        return loadDomain(fileName, isDefaultDomain, removeMultipleLinks, false);
    }

    /**
     * Load a domain from a file
     *
     * @param fileName the domain file name
     * @param isDefaultDomain true if the domain must be the default domain and false otherwise
     * @param removeMultipleLinks true if you want to remove the multiple links and false otherwise
     * @param useBwSharing true if you want to use BW sharing and false otherwise.
     * @return the loaded domain
     * @throws DomainAlreadyExistException If there is already a domain with the same ASID.
     * @throws InvalidDomainException If the domain fails to initialize.
     */
    public Domain loadDomain(String fileName, boolean isDefaultDomain, boolean removeMultipleLinks, boolean useBwSharing) throws DomainAlreadyExistException, InvalidDomainException {
        long time = System.currentTimeMillis();
        Domain d = null;
        try {
            d = DomainFactory.loadDomain(fileName,removeMultipleLinks, useBwSharing);
        } catch (FileNotFoundException e) {
            throw new InvalidDomainException("File not found");
        }
        time = System.currentTimeMillis() - time;
        logger.info("Load the domain " + d.getASID() + " from the file " + fileName + " takes " + time + " milliseconds");
        int ASID = d.getASID();
        if(domains.containsKey(ASID)) {
            if ((isDefaultDomain) || (defaultDomain == null)) {
                defaultDomain = domains.get(ASID);
                notifyChangeDefaultDomain(defaultDomain);
            }
            throw new DomainAlreadyExistException("There is already a domain with the ASID "+ASID);
        }
        domains.put(new Integer(ASID),d);
        if ((isDefaultDomain) || (defaultDomain == null))
            defaultDomain = d;
        notifyAddDomain(d);
        if (defaultDomain == d) notifyChangeDefaultDomain(d);
        return d;
    }

    /**
     * Save the domain ASID in the specified file
     *
     * @param ASID of the domain to save
     * @param fileName in which saving the domain
     * @throws InvalidDomainException if the ASID domain is not loaded in the InterDomainManager
     */
    public void saveDomain(int ASID, String fileName) throws InvalidDomainException {
        long time = System.currentTimeMillis();
        DomainFactory.saveDomain(fileName,getDomain(ASID));
        time = System.currentTimeMillis() - time;
        logger.info("Save the domain " + ASID + " in the file " + fileName + " takes " + time + " milliseconds");
    }

    /**
     * Build a simplified topology of the ASID domain
     *
     * @param ASID
     * @return the simplified topology
     * @throws InvalidDomainException if the ASID domain is not loaded in the InterDomainManager
     */
    public SimplifiedDomain buildSimplifiedDomain(int ASID) throws InvalidDomainException {
        return SimplifiedDomainBuilder.build(getDomain(ASID));
    }

    /**
     * Upload a simplified topology to the ASID domain. <TO IMPLEMENT>
     *
     * @param ASID the ASID in which load the simplified topology
     * @param sDomain the simplified topology
     * @throws InvalidDomainException
     * @throws SimplifiedDomainException
     */
    public void uploadSimplifiedDomain(int ASID, SimplifiedDomain sDomain) throws InvalidDomainException, SimplifiedDomainException {
        SimplifiedDomainBuilder.upload(sDomain,getDomain(ASID));
    }

    /**
     * Removes the default domain from the <code>InterDomainManager</code>.
     * @throws InvalidDomainException If there is no default domain.
     */
    public void removeDefaultDomain() throws InvalidDomainException {
        try {
            int asId = defaultDomain.getASID();
            removeDomain(asId);
        }
        catch(NullPointerException e) {
            throw new InvalidDomainException("There is no default domain.");
        }
        notifyRemoveDomain(defaultDomain);
        notifyChangeDefaultDomain(null);
    }

    /**
     * Removes the domain <code>asId</code> from the <code>InterDomainManager</code>.
     * Also remove all change listeners of the domain.
     * @param asId The domain to remove.
     * @throws InvalidDomainException If there is no domain <code>asId</code>.
     */
    public void removeDomain(int asId) throws InvalidDomainException {
        boolean defaultRemoved = false;
        if((defaultDomain != null) && (defaultDomain.getASID() == asId)) {
            defaultDomain = null;
            defaultRemoved = true;
        }
        
        if(!domains.containsKey(asId)) {
            throw new InvalidDomainException("There is no domain "+asId);
        }

        Domain domain = domains.get(asId);
        domains.remove(asId);

        domain.getObserver().removeAllListeners();
        notifyRemoveDomain(domain);
        if (defaultRemoved) notifyChangeDefaultDomain(null);
    }
    
    /**
     * Removes all the domains from the <code>InterDomainManager</code>.
     * Also remove all change listeners of the domain.
     * Warning: this method does not signal the listeners that domain are removed. Use with caution.
     */
    public void removeAllDomains() {
        for (Domain domain : domains.values()) {
            domain.getObserver().removeAllListeners();
        }
        domains.clear();
        defaultDomain = null;
    }
    
    public Domain[] getAllDomains() {
        Domain dd[] = new Domain[getNbDomains()];
    	return domains.values().toArray(dd);
    }

    public int getNbDomains() {
        return domains.size();
    }

    public void changeAsId(int oldASID, int newASID) throws InvalidDomainException, DomainAlreadyExistException {
        Domain domainToChange = getDomain(oldASID);
        domainToChange.setASID(newASID);
        this.addDomain(domainToChange);
        domains.remove(oldASID);
        TrafficMatrixManager.getInstance().changeAsId(oldASID, newASID);
        System.out.println("AS " + oldASID + " is now AS " + newASID);
    }

}
