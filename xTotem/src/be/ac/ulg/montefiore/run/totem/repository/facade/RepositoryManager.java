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
package be.ac.ulg.montefiore.run.totem.repository.facade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

/*
 * Changes:
 * --------
 *
 * - 10-March-2005: Completely new implementation of this class (JL).
 * - 22-March-2005: add the three stopAlgorithm methods (JL).
 * - 07-June-2005: added MIRA algorithm
 * - 08-Dec.-2005: add an instance of each algorithm (GMO).
 * - 09-Dec.-2005: Added XAMCRA algorithm (GMO).
 * - 12-Jan.-2006: Added the observer capability and notification of changes (GMO).
 * - 27-Jan.-2006: Added stopAlgorithms(asid) to stop all algorithms started on a specific domain (GMO).
 * - 20-Mar.-2006: implements hashCode for algorithm entries (GMO).
 * - 05-Mar.-2007: check operating system for library compatibility in constructor (GMO)
 * - 14-Jun.-2007: fiw bug with empty list (GMO)
 */

/**
 * This class is a singleton that provides a global access point to the
 * algorithms. Note that this class uses the <code>preferences.xml</code>
 * file to list the available algorithms.
 *
 * <p>Creation date: 10-mars-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class RepositoryManager extends RepositoryManagerObserver {
    
    private static final Logger logger = Logger.getLogger(RepositoryManager.class);
    private static RepositoryManager manager = null;

    // maintain an instance of each algorithm class
    // used to obtain the algorithm parameters
    // don't need much memory since real initialisation is done in start()
    private HashMap<String, TotemAlgorithm> algoInstances;

    private HashMap<String, String> availableAlgos;
    private HashMap<String, List<Entry>> algos;
    private Class<TotemAlgorithm> totemAlgo;
    private Class<DomainSyncAlgorithm> domainSyncAlgo;
    private Class<DomainTMSyncAlgorithm> domainTMSyncAlgo;
    
    private RepositoryManager() {
        algos = new HashMap<String, List<Entry>>();
        availableAlgos = new HashMap<String, String>();
        algoInstances = new HashMap<String, TotemAlgorithm>();

        // use preferences.xml to list available algorithms
        String[] algos;
        if (System.getProperty("os.name").contains("Linux") && System.getProperty("os.arch").contains("i386")) {
           // algos = PreferenceManager.getInstance().getPrefs().get("AVAILABLE-ALGORITHMS", "nl.tudelft.repository.XAMCRA.XAMCRA:be.ac.ulg.montefiore.run.totem.repository.DAMOTE.DAMOTE:be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF:be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFInvFreeBw:be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFInvCap:be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFHopCount:be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFTEMetric:be.ac.ucl.poms.repository.IGPWO.IGPWO:be.ac.ucl.ingi.totem.repository.CBGP:it.unina.repository.MIRA.MIRA").split(":");
        	algos = PreferenceManager.getInstance().getPrefs().get("AVAILABLE-ALGORITHMS", "").split(":");
        } else {
            logger.warn("Most algorithms are designed to run on Linux platform with a 32 bits JVM. Your configuration doesn't seem to match this crierion. Some algorithms won't be available.");
            //algos = PreferenceManager.getInstance().getPrefs().get("COMPATIBLE-ALGORITHMS", "be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF:be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFInvFreeBw:be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFInvCap:be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFHopCount:be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPFTEMetric").split(":");
            algos = PreferenceManager.getInstance().getPrefs().get("COMPATIBLE-ALGORITHMS", "").split(":");
        }

        for (int i = 0; i < algos.length; ++i) {
            String shortName = algos[i].substring(algos[i].lastIndexOf('.')+1);
            availableAlgos.put(shortName, algos[i]);
            try {
                Class clazz = Thread.currentThread().getContextClassLoader().loadClass(algos[i]);
                algoInstances.put(shortName, (TotemAlgorithm)clazz.newInstance());
            } catch (ClassNotFoundException e) {
                logger.error("Cannot be found algorithm: " + algos[i]);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (UnsatisfiedLinkError e) {
                logger.error("The library for algo " + shortName + " cannot be found : " + e.getMessage());
                logger.error("Check that the library is present and compiled for this architecture.");
            }
        }
 
        totemAlgo = TotemAlgorithm.class;
        domainSyncAlgo = DomainSyncAlgorithm.class;
        domainTMSyncAlgo = DomainTMSyncAlgorithm.class;
    }
    
    /**
     * Returns the single instance of the RepositoryManager.
     */
    public static RepositoryManager getInstance() {
        if(manager == null) {
            manager = new RepositoryManager();
        }
        return manager;
    }
    
    /**
     * Adds the entry <code>e</code> in the map <code>algos</code>.
     * @param e The entry to add.
     * @throws AlgorithmInitialisationException If there is already an entry
     * equals to <code>e</code>.
     */
    private void addEntry(Entry e) throws AlgorithmInitialisationException {
        String name = e.getName();
        List<Entry> list = algos.get(name);
        if(list == null) {
            list = new ArrayList<Entry>();
            list.add(e);
            algos.put(name, list);
        }
        else {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                Entry entry = iter.next();
                if(entry.equals(e)) {
                    throw new AlgorithmInitialisationException("Algorithm already started.");
                }
            }
            list.add(e);
        }
    }
    
    /**
     * Starts the algorithm <code>name</code>. This algorithm must have been
     * specified in the <code>preferences.xml</code> file. If it implements
     * <code>DomainSyncAlgorithm</code> or
     * <code>DomainTMSyncAlgorithm</code>, we use default
     * values for the AS ID and the TM ID.
     * @param name The name of the algorithm to start.
     * @param params The parameters to give to the algorithm.
     * It can be <code>null</code>.
     * @throws AlgorithmInitialisationException If the algorithm was not
     * specified in the preferences file, if the specified class was not
     * found, if the algorithm doesn't implement <code>TotemAlgorithm</code>,
     * if there is an error during the instantiation of the class, if the
     * algorithm was already started, or if the algorithm requires an AS ID
     * and/or a TM ID and that there are no default values.
     */
    public void startAlgo(String name, HashMap<String, String> params) throws AlgorithmInitialisationException {
        if(!availableAlgos.containsKey(name)) {
            throw new AlgorithmInitialisationException("The algorithm "+name+" was not specified in the preferences file.");
        }
        
        String qualifiedName = availableAlgos.get(name);
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(qualifiedName);
            boolean isTotemAlgoImplemented = totemAlgo.isAssignableFrom(clazz);
            boolean isDSImplemented = domainSyncAlgo.isAssignableFrom(clazz);
            boolean isDTMSImplemented = domainTMSyncAlgo.isAssignableFrom(clazz);

            if(!isTotemAlgoImplemented) {
                throw new AlgorithmInitialisationException("The algorithm doesn't implement TotemAlgorithm.");
            }
            if(isDSImplemented && isDTMSImplemented) {
                throw new AlgorithmInitialisationException("The algorithm cannot implement both DomainSyncAlgorithm and DomainTMSyncAlgorithm.");
            }
            
            TotemAlgorithm algo = null;
            Entry entry = null;
            if(!isDSImplemented && !isDTMSImplemented) {
                algo = (TotemAlgorithm) clazz.newInstance();
                entry = new AlgorithmEntry(name, algo);
            }
            else if(isDSImplemented) {
                algo = (DomainSyncAlgorithm) clazz.newInstance();
                try { 
                    entry = new DomainAlgorithmEntry(name, InterDomainManager.getInstance().getDefaultDomain().getASID(), (DomainSyncAlgorithm) algo);
                }
                catch(NullPointerException e) {
                    throw new AlgorithmInitialisationException("There is no default domain!");
                }
                if(params == null) {
                    params = new HashMap<String, String>();
                }
                params.put("ASID", Integer.toString(((DomainAlgorithmEntry) entry).getASID()));
            }
            else {
                algo = (DomainTMSyncAlgorithm) clazz.newInstance();
                try {
                    int asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
                    int tmId = TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID();
                    entry = new DomainTrafficAlgorithmEntry(name, asId, tmId, (DomainTMSyncAlgorithm) algo);
                }
                catch(InvalidTrafficMatrixException e) {
                    throw new AlgorithmInitialisationException("There is no default traffic matrix!");
                }
                catch(NullPointerException e) {
                    throw new AlgorithmInitialisationException("There is no default domain!");
                }
                if(params == null) {
                    params = new HashMap<String, String>();
                }
                params.put("ASID", Integer.toString(((DomainTrafficAlgorithmEntry) entry).getASID()));
                params.put("TMID", Integer.toString(((DomainTrafficAlgorithmEntry) entry).getTMID()));
            }

            algo.start(params);

            addEntry(entry);
            notifyStartAlgo(algo);
        }
        catch(ClassNotFoundException e) {
            throw new AlgorithmInitialisationException("The class "+qualifiedName+" was not found.");
        }
        catch(IllegalAccessException e) {
            throw new AlgorithmInitialisationException("The class "+qualifiedName+" has not a nullary public constructor.");
        }
        catch(InstantiationException e) {
            throw new AlgorithmInitialisationException("The class "+qualifiedName+" has not a nullary constructor.");
        }
        catch(ExceptionInInitializerError e) {
            throw new AlgorithmInitialisationException("There was an error during the initialisation of "+qualifiedName+".");
        }
    }
    
    /**
     * Starts the algorithm <code>name</code>. This algorithm must have been
     * specified in the <code>preferences.xml</code> file and must implement
     * <code>DomainSyncAlgorithm</code> or
     * <code>DomainTMSyncAlgorithm</code> (we use default
     * value for the TM ID in this latter case).
     * @param name The name of the algorithm to start.
     * @param params The parameters to give to the algorithm. It can be
     * <code>null</code>.
     * @param asId The domain on which the algorithm will operate.
     * @throws AlgorithmInitialisationException If the algorithm was not
     * specified in the preferences file, if the specified class was not
     * found, if there is an error during the instantiation of the class, if
     * the algorithm was already started, if the algorithm doesn't implement
     * <code>TotemAlgorithm</code>, or if the algorithm requires a TM ID and
     * that there is no default value.
     */
    public void startAlgo(String name, HashMap<String, String> params, int asId) throws AlgorithmInitialisationException {
        if(!availableAlgos.containsKey(name)) {
            throw new AlgorithmInitialisationException("The algorithm "+name+" was not specified in the preferences file.");
        }

        String qualifiedName = availableAlgos.get(name);
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(qualifiedName);
            boolean isTotemAlgoImplemented = totemAlgo.isAssignableFrom(clazz);
            boolean isDSImplemented = domainSyncAlgo.isAssignableFrom(clazz);
            boolean isDTMSImplemented = domainTMSyncAlgo.isAssignableFrom(clazz);

            if(!isTotemAlgoImplemented) {
                throw new AlgorithmInitialisationException("The algorithm doesn't implement TotemAlgorithm.");
            }       
            
            if(isDSImplemented && isDTMSImplemented) {
                throw new AlgorithmInitialisationException("The algorithm cannot implement both DomainSyncAlgorithm and DomainTMSyncAlgorithm.");
            }
            
            TotemAlgorithm algo = null;
            Entry entry = null;
            if(isDSImplemented) {
                algo = (DomainSyncAlgorithm) clazz.newInstance();
                entry = new DomainAlgorithmEntry(name, asId, (DomainSyncAlgorithm) algo);
                if(params == null) {
                    params = new HashMap<String, String>();
                }
                params.put("ASID", Integer.toString(asId));
            }
            else if(isDTMSImplemented) {
                algo = (DomainTMSyncAlgorithm) clazz.newInstance();
                try {
                    int tmId = TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID(asId);
                    entry = new DomainTrafficAlgorithmEntry(name, asId, tmId, (DomainTMSyncAlgorithm) algo);
                }
                catch(InvalidTrafficMatrixException e) {
                    throw new AlgorithmInitialisationException("There is no default traffic matrix!");
                }
                if(params == null) {
                    params = new HashMap<String, String>();
                }
                params.put("ASID", Integer.toString(asId));
                params.put("TMID", Integer.toString(((DomainTrafficAlgorithmEntry) entry).getTMID()));
            }
            else {
                algo = (TotemAlgorithm) clazz.newInstance();
                entry = new AlgorithmEntry(name, algo);
            }
            addEntry(entry);
            algo.start(params);
            notifyStartAlgo(algo);
        }
        catch(ClassNotFoundException e) {
            throw new AlgorithmInitialisationException("The class "+qualifiedName+" was not found.");
        }
        catch(IllegalAccessException e) {
            throw new AlgorithmInitialisationException("The class "+qualifiedName+" has not a nullary public constructor.");
        }
        catch(InstantiationException e) {
            throw new AlgorithmInitialisationException("The class "+qualifiedName+" has not a nullary constructor.");
        }
        catch(ExceptionInInitializerError e) {
            throw new AlgorithmInitialisationException("There was an error during the initialisation of "+qualifiedName+".");
        }
    }
    
    /**
     * Starts the algorithm <code>name</code>. This algorithm must have been
     * specified in the <code>preferences.xml</code> file and must implement
     * <code>DomainTMSyncAlgorithm</code>.
     * @param name The name of the algorithm to start.
     * @param params The parameters to give to the algorithm. It can be
     * <code>null</code>.
     * @param asId The domain on which the algorithm will operate.
     * @param tmId The traffic matrix on which the algorithm will operate.
     * @throws AlgorithmInitialisationException If the algorithm was not
     * specified in the preferences file, if the specified class was not
     * found, if the algorithm doesn't implement
     * <code>DomainTMSyncAlgorithm</code>, if there is an
     * error during the instantiation of the class, or if the algorithm was
     * already started.
     */
    public void startAlgo(String name, HashMap<String, String> params, int asId, int tmId) throws AlgorithmInitialisationException {
        if(!availableAlgos.containsKey(name)) {
            throw new AlgorithmInitialisationException("The algorithm "+name+" was not specified in the preferences file.");
        }
        
        String qualifiedName = availableAlgos.get(name);
        try {
            Class clazz = Thread.currentThread().getContextClassLoader().loadClass(qualifiedName);
            boolean isTotemAlgoImplemented = totemAlgo.isAssignableFrom(clazz);
            boolean isDSImplemented = domainSyncAlgo.isAssignableFrom(clazz);
            boolean isDTMSImplemented = domainTMSyncAlgo.isAssignableFrom(clazz);

            if(!isTotemAlgoImplemented) {
                throw new AlgorithmInitialisationException("The algorithm doesn't implement TotemAlgorithm.");
            }
            if(isDSImplemented && isDTMSImplemented) {
                throw new AlgorithmInitialisationException("The algorithm cannot implement both DomainSyncAlgorithm and DomainTMSyncAlgorithm.");
            }
            
            TotemAlgorithm algo = null;
            Entry entry = null;
            if(isDTMSImplemented) {            
                algo = (DomainTMSyncAlgorithm) clazz.newInstance();
                entry = new DomainTrafficAlgorithmEntry(name, asId, tmId, (DomainTMSyncAlgorithm) algo);
                if(params == null) {
                    params = new HashMap<String, String>();
                }
                params.put("ASID", Integer.toString(asId));
                params.put("TMID", Integer.toString(tmId));
            }
            else if(isDSImplemented) {
                algo = (DomainSyncAlgorithm) clazz.newInstance();
                entry = new DomainAlgorithmEntry(name, asId, (DomainSyncAlgorithm) algo);
                if(params == null) {
                    params = new HashMap<String, String>();
                }
                params.put("ASID", Integer.toString(asId));
            }
            else {
                algo = (TotemAlgorithm) clazz.newInstance();
                entry = new AlgorithmEntry(name, algo);
            }
            addEntry(entry);
            algo.start(params);
            notifyStartAlgo(algo);
        }
        catch(ClassNotFoundException e) {
            throw new AlgorithmInitialisationException("The class "+qualifiedName+" was not found.");
        }
        catch(IllegalAccessException e) {
            throw new AlgorithmInitialisationException("The class "+qualifiedName+" has not a nullary public constructor.");
        }
        catch(InstantiationException e) {
            throw new AlgorithmInitialisationException("The class "+qualifiedName+" has not a nullary constructor.");
        }
        catch(ExceptionInInitializerError e) {
            throw new AlgorithmInitialisationException("There was an error during the initialisation of "+qualifiedName+".");
        }
    }
    
    /**
     * Returns the instance of the algorithm <code>name</code>. We use default
     * values for the AS ID and TM ID if necessary.
     * @param name The name of the algorithm.
     * @return The instance of the algorithm <code>name</code>.
     * @throws NoSuchAlgorithmException If the algorithm doesn't exist or if
     * it was not started, or if default values are required but not available.
     */
    public TotemAlgorithm getAlgo(String name) throws NoSuchAlgorithmException {
        List<Entry> list = algos.get(name);
        if(list == null) {
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
        
        if(list.get(0) instanceof DomainAlgorithmEntry) {
            try {
                int asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
                for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                    DomainAlgorithmEntry entry = (DomainAlgorithmEntry) iter.next();
                    if((entry.getName().equals(name)) && (entry.getASID() == asId)) {
                        return entry.getAlgo();
                    }
                }
                throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
            }
            catch(NullPointerException e) {
                throw new NoSuchAlgorithmException("No default domain available!");
            }
        }
        else if(list.get(0) instanceof DomainTrafficAlgorithmEntry) {
            try {
                int asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
                int tmId = TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID(asId);
                for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                    DomainTrafficAlgorithmEntry entry = (DomainTrafficAlgorithmEntry) iter.next();
                    if((entry.getName().equals(name)) && (entry.getASID() == asId) && (entry.getTMID() == tmId)) {
                        return entry.getAlgo();
                    }
                }
                throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
            }
            catch(TrafficMatrixException e) {
                throw new NoSuchAlgorithmException("No default traffic matrix available!");
            }
            catch(NullPointerException e) {
                throw new NoSuchAlgorithmException("No default domain available!");
            }
        }
        else {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                Entry entry = iter.next();
                if(entry.getName().equals(name)) {
                    return entry.getAlgo();
                }
            }
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
    }
    
    /**
     * Returns the instance of the algorithm <code>name</code>. We use default
     * value for TM ID if necessary. If the algorithm doesn't require an AS
     * ID, <code>asId</code> is ignored.
     * @param name The name of the algorithm.
     * @param asId The domain on which it operates.
     * @return The instance of the algorithm <code>name</code>.
     * @throws NoSuchAlgorithmException If the algorithm doesn't exist or if
     * it was not started, or if a default value is required but not
     * available.
     */
    public TotemAlgorithm getAlgo(String name, int asId) throws NoSuchAlgorithmException {
        List<Entry> list = algos.get(name);
        if(list == null || list.size() == 0) {
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
        
        if(list.get(0) instanceof DomainAlgorithmEntry) {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                DomainAlgorithmEntry entry = (DomainAlgorithmEntry) iter.next();
                if((entry.getName().equals(name)) && (entry.getASID() == asId)) {
                    return entry.getAlgo();
                }
            }
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
        else if(list.get(0) instanceof DomainTrafficAlgorithmEntry) {
            try {
                int tmId = TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID(asId);
                for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                    DomainTrafficAlgorithmEntry entry = (DomainTrafficAlgorithmEntry) iter.next();
                    if((entry.getName().equals(name)) && (entry.getASID() == asId) && (entry.getTMID() == tmId)) {
                        return entry.getAlgo();
                    }
                }
                throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
            }
            catch(TrafficMatrixException e) {
                throw new NoSuchAlgorithmException("No default traffic matrix available!");
            }
        }
        else {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                Entry entry = iter.next();
                if(entry.getName().equals(name)) {
                    return entry.getAlgo();
                }
            }
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
    }
    
    /**
     * Returns the instance of the algorithm <code>name</code>. If the
     * algorithm doesn't require an AS ID and/or a TM ID, <code>asId</code>
     * and/or <code>tmId</code> are ignored.
     * @param name The name of the algorithm.
     * @param asId The domain on which it operates.
     * @param tmId The traffic matrix on which it operates.
     * @return The instance of the algorithm <code>name</code>.
     * @throws NoSuchAlgorithmException If the algorithm doesn't exist or if
     * it was not started.
     */
    public TotemAlgorithm getAlgo(String name, int asId, int tmId) throws NoSuchAlgorithmException {
        List<Entry> list = algos.get(name);
        if(list == null) {
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
        
        if(list.get(0) instanceof DomainAlgorithmEntry) {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                DomainAlgorithmEntry entry = (DomainAlgorithmEntry) iter.next();
                if((entry.getName().equals(name)) && (entry.getASID() == asId)) {
                    return entry.getAlgo();
                }
            }
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
        else if(list.get(0) instanceof DomainTrafficAlgorithmEntry) {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                DomainTrafficAlgorithmEntry entry = (DomainTrafficAlgorithmEntry) iter.next();
                if((entry.getName().equals(name)) && (entry.getASID() == asId) && (entry.getTMID() == tmId)) {
                    return entry.getAlgo();
                }
            }
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
        else {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                Entry entry = iter.next();
                if(entry.getName().equals(name)) {
                    return entry.getAlgo();
                }
            }
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
    }
    
    /**
     * Stops all the started algorithms.
     */
    public void stopAlgorithms() {
        for (Iterator<List<Entry>> iter = algos.values().iterator(); iter.hasNext();) {
            List<Entry> list = iter.next();
            for (Iterator<Entry> iterator = list.iterator(); iterator.hasNext();) {
                Entry element = iterator.next();
                element.getAlgo().stop();
                iterator.remove();
            }
            iter.remove();
        }
    }

    public void stopAlgorithms(int asId) {
        for (Iterator<List<Entry>> iter = algos.values().iterator(); iter.hasNext();) {
            List<Entry> list = iter.next();
            if (list == null || list.size() <= 0) continue;
            if (list.get(0) instanceof DomainAlgorithmEntry) {
                for (Iterator<Entry> iterator = list.iterator(); iterator.hasNext();) {
                    DomainAlgorithmEntry entry = (DomainAlgorithmEntry) iterator.next();
                    if (entry.getASID() == asId) {
                        entry.getAlgo().stop();
                        iterator.remove();
                        notifyStopAlgo(entry.getAlgo());
                        break;
                    }
                }
            }
            else if (list.get(0) instanceof DomainTrafficAlgorithmEntry) {
                for (Iterator<Entry> iterator = list.iterator(); iterator.hasNext();) {
                    DomainTrafficAlgorithmEntry entry = (DomainTrafficAlgorithmEntry) iterator.next();
                    if (entry.getASID() == asId) {
                        entry.getAlgo().stop();
                        iterator.remove();
                        notifyStopAlgo(entry.getAlgo());
                        break;
                    }
                }
            }
        }
    }

    /**
     * Stops the algorithm <code>name</code>. We use default values for the AS
     * ID and the TM ID if necessary.
     * @param name The name of the algorithm to stop.
     * @throws NoSuchAlgorithmException If the algorithm doesn't exist, if it
     * was not started or if there is no default value for the AS ID and/or
     * the TM ID.
     */
    public void stopAlgorithm(String name) throws NoSuchAlgorithmException {
        List<Entry> list = algos.get(name);
        if(list == null) {
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
        
        if(list.get(0) instanceof DomainAlgorithmEntry) {
            try {
                int asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
                for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                    DomainAlgorithmEntry entry = (DomainAlgorithmEntry) iter.next();
                    if((entry.getName().equals(name)) && (entry.getASID() == asId)) {
                        entry.getAlgo().stop();
                        iter.remove();
                        notifyStopAlgo(entry.getAlgo());
                        return;
                    }
                }
                throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
            }
            catch(NullPointerException e) {
                throw new NoSuchAlgorithmException("No default domain available!");
            }
        }
        else if(list.get(0) instanceof DomainTrafficAlgorithmEntry) {
            try {
                int asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
                int tmId = TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID(asId);
                for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                    DomainTrafficAlgorithmEntry entry = (DomainTrafficAlgorithmEntry) iter.next();
                    if((entry.getName().equals(name)) && (entry.getASID() == asId) && (entry.getTMID() == tmId)) {
                        entry.getAlgo().stop();
                        iter.remove();
                        notifyStopAlgo(entry.getAlgo());
                        return;
                    }
                }
                throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
            }
            catch(TrafficMatrixException e) {
                throw new NoSuchAlgorithmException("No default traffic matrix available!");
            }
            catch(NullPointerException e) {
                throw new NoSuchAlgorithmException("No default domain available!");
            }
        }
        else {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                Entry entry = iter.next();
                if(entry.getName().equals(name)) {
                    entry.getAlgo().stop();
                    iter.remove();
                    notifyStopAlgo(entry.getAlgo());
                    return;
                }
            }
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
    }
    
    /**
     * Stops the algorithm <code>name</code>. We use a default value for the
     * TM ID if necessary.
     * @param name The name of the algorithm to stop.
     * @param asId The AS ID of the domain on which the algorithm operated.
     * @throws NoSuchAlgorithmException If the algorithm doesn't exist, if it
     * was not started or if there is no default value for the TM ID.
     */
    public void stopAlgorithm(String name, int asId) throws NoSuchAlgorithmException {
        List<Entry> list = algos.get(name);
        if(list == null) {
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
        
        if(list.get(0) instanceof DomainAlgorithmEntry) {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                DomainAlgorithmEntry entry = (DomainAlgorithmEntry) iter.next();
                if((entry.getName().equals(name)) && (entry.getASID() == asId)) {
                    entry.getAlgo().stop();
                    iter.remove();
                    notifyStopAlgo(entry.getAlgo());
                    return;
                }
            }
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
        else if(list.get(0) instanceof DomainTrafficAlgorithmEntry) {
            try {
                int tmId = TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID(asId);
                for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                    DomainTrafficAlgorithmEntry entry = (DomainTrafficAlgorithmEntry) iter.next();
                    if((entry.getName().equals(name)) && (entry.getASID() == asId) && (entry.getTMID() == tmId)) {
                        entry.getAlgo().stop();
                        iter.remove();
                        notifyStopAlgo(entry.getAlgo());
                        return;
                    }
                }
                throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
            }
            catch(TrafficMatrixException e) {
                throw new NoSuchAlgorithmException("No default traffic matrix available!");
            }
        }
        else {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                Entry entry = iter.next();
                if(entry.getName().equals(name)) {
                    entry.getAlgo().stop();
                    iter.remove();
                    notifyStopAlgo(entry.getAlgo());
                    return;
                }
            }
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }        
    }
    
    /**
     * Stops the algorithm <code>name</code>.
     * @param name The name of the algorithm to stop.
     * @param asId The AS ID of the domain on which the algorithm operated.
     * @param tmId The TM ID of the traffic matrix on which the algorithm
     * operated.
     * @throws NoSuchAlgorithmException If the algorithm doesn't exist or if
     * it was not started.
     */
    public void stopAlgorithm(String name, int asId, int tmId) throws NoSuchAlgorithmException {
        List<Entry> list = algos.get(name);
        if(list == null) {
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
        
        if(list.get(0) instanceof DomainAlgorithmEntry) {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                DomainAlgorithmEntry entry = (DomainAlgorithmEntry) iter.next();
                if((entry.getName().equals(name)) && (entry.getASID() == asId)) {
                    entry.getAlgo().stop();
                    iter.remove();
                    notifyStopAlgo(entry.getAlgo());
                    return;
                }
            }
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
        else if(list.get(0) instanceof DomainTrafficAlgorithmEntry) {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                DomainTrafficAlgorithmEntry entry = (DomainTrafficAlgorithmEntry) iter.next();
                if((entry.getName().equals(name)) && (entry.getASID() == asId) && (entry.getTMID() == tmId)) {
                    entry.getAlgo().stop();
                    iter.remove();
                    notifyStopAlgo(entry.getAlgo());
                    return;
                }
            }
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
        else {
            for (Iterator<Entry> iter = list.iterator(); iter.hasNext();) {
                Entry entry = iter.next();
                if(entry.getName().equals(name)) {
                    entry.getAlgo().stop();
                    iter.remove();
                    notifyStopAlgo(entry.getAlgo());
                    return;
                }
            }
            throw new NoSuchAlgorithmException("There is no algorithm "+name+" or it was not started.");
        }
    }

    /**
     * returns a list containing the class of each algo
     * @return
     */
    public List<Class> getAllTotemAlgos() {
        return getAllTotemAlgos(null);
    }

    /**
     * returns a list containing the class of each algo that inherits from the class filter
     * @param filter
     * @return
     */
    public List<Class> getAllTotemAlgos(Class filter) {
        List<Class> tmpList = new ArrayList<Class>();

        for (String ss : availableAlgos.values()) {
            Class clazz = null;
            try {
                clazz = Class.forName(ss);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            } catch (NoClassDefFoundError e) {
                logger.error("The class " + ss + " cannot be found.");
                continue;
            }
            if (filter == null || filter.isAssignableFrom(clazz))
                tmpList.add(clazz);
        }
        return tmpList;
   }

    /**
     * returns a list of all the started algorithms
     * @return
     */
   public List<TotemAlgorithm> getAllStartedAlgos() {
       return getAllStartedAlgos(null);
   }

   /**
    *  returns a list of all the started algorithms that inherits from the class filter
    * @param filter
    * @return
    */
   public List<TotemAlgorithm> getAllStartedAlgos(Class filter) {
       List<TotemAlgorithm> lst = new ArrayList<TotemAlgorithm>();

       for (List<Entry> algoLst : algos.values()) {
           for (Entry algoEntry : algoLst) {
               Class clazz = algoEntry.getAlgo().getClass();
               if (filter == null || filter.isAssignableFrom(clazz))
                    lst.add(algoEntry.getAlgo());
           }
       }

       return lst;
   }

    /**
     * returns all started algos of class <code>filter</code> that can be used on the given domain,
     * i.e. every DomainAlgorithm and DomainTrafficAlgorithm started on the given domain,
     * plus algorithms that are independant of any domain.
     * @param asId
     * @param filter
     * @return
     */
   public List<TotemAlgorithm> getAllStartedAlgos(int asId, Class filter) {
       List<TotemAlgorithm> lst = new ArrayList<TotemAlgorithm>();

       for (List<Entry> algoLst : algos.values()) {
           if (algoLst.size() <= 0) continue;
           if (algoLst.get(0) instanceof DomainAlgorithmEntry) {
                Class clazz = algoLst.get(0).getAlgo().getClass();
                if (filter != null && !filter.isAssignableFrom(clazz)) continue;
                for (Entry algoEntry : algoLst) {
                    if (((DomainAlgorithmEntry)algoEntry).getASID() == asId)
                        lst.add(algoEntry.getAlgo());
                }
           }
           else if (algoLst.get(0) instanceof DomainTrafficAlgorithmEntry) {
               Class clazz = algoLst.get(0).getAlgo().getClass();
               if (filter != null && !filter.isAssignableFrom(clazz)) continue;
               for (Entry algoEntry : algoLst) {
                   if (((DomainTrafficAlgorithmEntry)algoEntry).getASID() == asId)
                       lst.add(algoEntry.getAlgo());
               }
           }
           else {
               Class clazz = algoLst.get(0).getAlgo().getClass();
               if (filter != null && !filter.isAssignableFrom(clazz)) continue;
               for (Entry algoEntry : algoLst) {
                  lst.add(algoEntry.getAlgo());
               }
           }
       }

       return lst;
   }

    /**
     * returns all started algos that can be used on the given domain
     * i.e. every DomainAlgorithm and DomainTrafficAlgorithm started on the given domain,
     * plus algorithms that are independant of any domain.
     * @param asId
     * @return
     */
   public List<TotemAlgorithm> getAllStartedAlgos(int asId) {
        return getAllStartedAlgos(asId, null);
   }


    /**
     * Returns a list of the algorithm parameters used to start the algorithm
     *
     * @param algoName Algorithm name
     * @return
     * @throws NoSuchAlgorithmException if the algorithm was not found
     */
    public List<ParameterDescriptor> getAlgoParameters(String algoName) throws NoSuchAlgorithmException {
        if (algoInstances.get(algoName) == null) {
            throw new NoSuchAlgorithmException();
        }
        return algoInstances.get(algoName).getStartAlgoParameters();
    }


//  Interfaces and classes used to retrieve the algorithms' instances
    private interface Entry {
        public String getName();
        public TotemAlgorithm getAlgo();
    }
    private class AlgorithmEntry implements Entry {
        private String name;
        private TotemAlgorithm algo;
        public AlgorithmEntry(String name, TotemAlgorithm algo) {
            this.name = name;
            this.algo = algo;
        }
        public String getName() {
            return name;
        }
        public TotemAlgorithm getAlgo() {
            return algo;
        }
        public boolean equals(Object o) {
            if(!(o instanceof AlgorithmEntry)) {
                return false;
            }
            AlgorithmEntry e = (AlgorithmEntry) o;
            return e == this;
        }
        public int hashCode() {
            return name.hashCode();
        }
    }
    private class DomainAlgorithmEntry implements Entry {
        private int asId;
        private String name;
        private DomainSyncAlgorithm algo;
        public DomainAlgorithmEntry(String name, int asId, DomainSyncAlgorithm algo) {
            this.name = name;
            this.asId = asId;
            this.algo = algo;
        }
        public int getASID() {
            return asId;
        }
        public String getName() {
            return name;
        }
        public DomainSyncAlgorithm getAlgo() {
            return algo;
        }
        public boolean equals(Object o) {
            if(!(o instanceof DomainAlgorithmEntry)) {
                return false;
            }
            DomainAlgorithmEntry e = (DomainAlgorithmEntry) o;
            return (e.name.equals(this.name) && e.asId == this.asId);
        }
        public int hashCode() {
            return name.hashCode()*31 + (new Integer(asId)).hashCode();
        }
    }
    private class DomainTrafficAlgorithmEntry implements Entry {
        private int asId, tmId;
        private String name;
        private DomainTMSyncAlgorithm algo;
        public DomainTrafficAlgorithmEntry(String name, int asId, int tmId, DomainTMSyncAlgorithm algo) {
            this.name = name;
            this.asId = asId;
            this.tmId = tmId;
            this.algo = algo;
        }
        public int getTMID() {
            return tmId;
        }
        public int getASID() {
            return asId;
        }
        public String getName() {
            return name;
        }
        public DomainTMSyncAlgorithm getAlgo() {
            return algo;
        }
        public boolean equals(Object o) {
            if(!(o instanceof DomainTrafficAlgorithmEntry)) {
                return false;
            }
            DomainTrafficAlgorithmEntry e = (DomainTrafficAlgorithmEntry) o;
            return (e.name.equals(this.name) && e.asId == this.asId && e.tmId == this.tmId);
        }
        public int hashCode() {
            return name.hashCode()*31*31 + (new Integer(asId)).hashCode()*31 + (new Integer(tmId)).hashCode();
        }
    }
}
