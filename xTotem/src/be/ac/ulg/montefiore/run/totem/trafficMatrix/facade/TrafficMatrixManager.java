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

import java.util.*;
import java.io.File;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManagerListener;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.topgen.traffic.TrafficGenerationException;
import be.ac.ulg.montefiore.run.totem.topgen.traffic.TrafficGenerator;
import be.ac.ulg.montefiore.run.totem.topgen.traffic.model.TrafficModel;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.DataConsistencyException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.TrafficMatrixImpl;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.persistence.TrafficMatrixFactory;
import be.ac.ulg.montefiore.run.totem.util.IdGenerator;

/*
* Changes:
* --------
* 08-Mar-2005: Modifications in relation with intra/inter domain matrices. (ODE)
* 07-Jul-2005: add generateOnlyEdge functionnality in the generation (FSK)
* 05-Dec-2005: add getTrafficMatrices to obtain all the matrices for a domain (GMO)
* 12-Jan-2006: add observer and notification capability (GMO)
* 03-Feb-2006: bugfix when calling listeners, adapt to the new listeners interface (GMO)
* 20-Mar-2006: now sets the TrafficMatrix tmId property when adding matrix in manager and unsets it when removing (GMO)
* 24-Apr-2006: loadTrafficMatrix and generateTrafficMatrix now returns the loaded/generated matrix (GMO)
* 11-Jul-2006: Now listen to InterDomainManager events in order to remove the matrices from domains being unloaded (GMO)
* 11-Jul-2006: loadTrafficMatrix now throws InvalidTrafficMatrixException (GMO)
* 22-Nov-2006: First added traffic matrix now becomes default one (GMO)
* 19-Jan-2007: prevent NullPointerException in getDefaultTrafficMatrixId() (GMO)
* 23-Apr-2007: add createEmptyTrafficMatrix(.) methods (GMO)
* 30-Oct-2007: deprecatae use of generation functions (GMO)
*/

/**
 * The TrafficMatrixManager provides the access to all the traffic matrices.
 * This class is a singleton and the single instance can be obtain using
 * getInstance() method.  All the methods here are related to intra-domain traffic matrices.
 *
 * <p>Creation date: 27-janv.-2005
 * 
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 * @author  Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 */
public class TrafficMatrixManager extends TrafficMatrixManagerObserver implements InterDomainManagerListener {

    private static final Logger logger = Logger.getLogger(TrafficMatrixManager.class);
    private static TrafficMatrixManager instance = null;

    private HashMap<Integer, HashMap<Integer, TrafficMatrix>> trafficMatrices = null;
    private HashMap<Integer, Integer> defaultTrafficMatrices = null;

    private TrafficMatrixManager() {
        trafficMatrices = new HashMap<Integer, HashMap<Integer, TrafficMatrix>>();
        defaultTrafficMatrices = new HashMap<Integer, Integer>();
        InterDomainManager.getInstance().addListener(this);
    }

    /**
     * Returns the single instance of this class.
     * @return The single instance of this class.
     */
    public static TrafficMatrixManager getInstance() {
        if(instance == null) {
            instance = new TrafficMatrixManager();
        }
        return instance;
    }

    /**
     * Returns the TM ID of the default traffic matrix for the default domain of {@link InterDomainManager}.
     * @return The TM ID of the default traffic matrix for the default domain of {@link InterDomainManager}.
     * @throws InvalidTrafficMatrixException If the default traffic matrix is not set for the default domain.
     */
    public int getDefaultTrafficMatrixID() throws InvalidTrafficMatrixException {
        try {
            int asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
            return getDefaultTrafficMatrixID(asId);
        } catch (NullPointerException ex) {
            throw new InvalidTrafficMatrixException("There is no default domain");
        }
    }

    /**
     * Returns the TM ID of the default traffic matrix for the domain <code>asId</code>.
     * @param asId The target domain.
     * @return The TM ID of the default traffic matrix for the domain <code>asId</code>.
     * @throws InvalidTrafficMatrixException If there is no default traffic matrix for the domain <code>asId</code>.
     */
    public int getDefaultTrafficMatrixID(int asId) throws InvalidTrafficMatrixException {
        Integer tmId = defaultTrafficMatrices.get(asId);
        if(tmId == null) {
            throw new InvalidTrafficMatrixException("No default traffic matrix for the default domain.");
        }
        return tmId.intValue();
    }

    /**
     * Returns the default traffic matrix for the default domain of {@link InterDomainManager}.
     * @return The default traffic matrix for the default domain of {@link InterDomainManager}.
     * @throws InvalidTrafficMatrixException If the default traffic matrix is not set for the default domain.
     */
    public TrafficMatrix getDefaultTrafficMatrix() throws InvalidTrafficMatrixException {
        int asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
        Integer tmId = defaultTrafficMatrices.get(asId);
        if(tmId == null) {
            throw new InvalidTrafficMatrixException("No default traffic matrix for the default domain.");
        }
        return getTrafficMatrix(asId, tmId);
    }

    /**
     * Returns all the traffic matrices for a given domain asid
     * @param asId
     * @return all the traffic matrices loaded for the given domain asid
     */
    public List<Integer> getTrafficMatrices(int asId) {
        List<Integer> lst = new ArrayList<Integer>();
        HashMap<Integer, TrafficMatrix> tms = trafficMatrices.get(asId);

        if (tms == null) return lst;
        
        for (Integer i : tms.keySet()) {
            lst.add(i);
        };

        return lst;
    }

    /**
     * Returns the default traffic matrix for the domain <code>asId</code>.
     * @param asId The AS ID of the target domain.
     * @return The default traffic matrix for the domain <code>asId</code>.
     * @throws InvalidTrafficMatrixException If the default traffic matrix is not set for the domain <code>asId</code>.
     */
    public TrafficMatrix getDefaultTrafficMatrix(int asId) throws InvalidTrafficMatrixException {
        Integer tmId = defaultTrafficMatrices.get(asId);
        if(tmId == null) {
            throw new InvalidTrafficMatrixException("No default traffic matrix for the domain "+asId);
        }
        return getTrafficMatrix(asId, tmId);
    }

    /**
     * Returns the traffic matrix <code>tmId</code> for the domain <code>asId</code>.
     * @param asId The AS ID of the target domain.
     * @param tmId The TM ID of the traffic matrix to return.
     * @throws InvalidTrafficMatrixException If there is no traffic matrix <code>tmId</code> for domain <code>asId</code>.
     */
    public TrafficMatrix getTrafficMatrix(int asId, int tmId) throws InvalidTrafficMatrixException {
        //this.printAllRecordedTms();
        HashMap<Integer, TrafficMatrix> tms = trafficMatrices.get(asId);
        if(tms == null) {
            throw new InvalidTrafficMatrixException("No traffic matrix "+tmId+" for domain "+asId);
        }
        TrafficMatrix tm = tms.get(tmId);
        if(tm == null) {
            this.printAllRecordedTms();
            throw new InvalidTrafficMatrixException("No traffic matrix "+tmId+" for domain "+asId);
        }
        return tm;
    }

    public void printAllRecordedTms() {
        System.out.println("Printing all recorded tms");
        Set<Integer> asIds = trafficMatrices.keySet();
        for (Iterator<Integer> it = asIds.iterator(); it.hasNext();) {
            Integer currentAsId = it.next();
            HashMap<Integer, TrafficMatrix> tms = trafficMatrices.get(currentAsId);
            Set<Integer> tmIds = tms.keySet();
            for (Iterator<Integer> it2 = tmIds.iterator(); it2.hasNext();) {
                Integer currentTmId = it2.next();
                try {
                    System.out.println("AS " + currentAsId.toString() + " has a tm with id " + currentTmId.toString() + " ASID of the TM = " + tms.get(currentTmId).getASID() + " tm id of the tm = " + tms.get(currentTmId).getTmId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Sets the traffic matrix <code>tmId</code> as default traffic matrix for
     * the default domain specified in {@link InterDomainManager}.
     * @param tmId The TM ID of the default traffic matrix.
     * @throws InvalidTrafficMatrixException If there is no traffic matrix <code>tmId</code> for the default domain.
     */
    public void setDefaultTrafficMatrix(int tmId) throws InvalidTrafficMatrixException {
        int asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
        setDefaultTrafficMatrix(asId, tmId);
        notifyChangeDefaultTrafficMatrix(asId, getTrafficMatrix(asId, tmId));
    }

    /**
     * Sets the traffic matrix <code>tmId</code> as default traffic matrix for
     * the domain <code>asId</code>.
     * @param asId The AS ID of the target domain.
     * @param tmId The TM ID of the default traffic matrix.
     * @throws InvalidTrafficMatrixException If there is no traffic matrix <code>tmId</code> for the domain <code>asId</code>.
     */
    public void setDefaultTrafficMatrix(int asId, int tmId) throws InvalidTrafficMatrixException {
        HashMap<Integer, TrafficMatrix> tms = trafficMatrices.get(asId);
        if((tms == null) || (!tms.containsKey(tmId))) {
            throw new InvalidTrafficMatrixException("No traffic matrix "+tmId+" for domain "+asId);
        }
        defaultTrafficMatrices.put(asId, tmId);
        notifyChangeDefaultTrafficMatrix(asId, getTrafficMatrix(asId, tmId));
    }

    /**
     * Adds the traffic matrix <code>tm</code> with the TM ID <code>tmId</code>.
     *
     * @param tm The traffic matrix to add.
     * @param tmId The TM ID you wish to give to <code>tm</code>.
     * @throws TrafficMatrixAlreadyExistException If there is already a traffic matrix with the same TM ID for the same domain.
     * @throws InvalidDomainException If the specified domain doesn't exist.
     * @throws TrafficMatrixIdException If the traffic matrix id has already been set for the given matrix.
     */
    public void addTrafficMatrix(TrafficMatrix tm, int tmId) throws TrafficMatrixAlreadyExistException, InvalidDomainException, TrafficMatrixIdException {
        int asId = tm.getASID();
        InterDomainManager.getInstance().getDomain(asId);
        HashMap<Integer, TrafficMatrix> tms = trafficMatrices.get(asId);
        if((tms != null) && (tms.containsKey(tmId))) {
            throw new TrafficMatrixAlreadyExistException("There is already a traffic matrix "+tmId+" for the domain "+asId);
        }
        tm.setTmId(tmId);

        if (tms == null) {
            tms = new HashMap<Integer, TrafficMatrix>();
            trafficMatrices.put(asId, tms);
        }

        tms.put(tmId, tm);
        notifyAddTrafficMatrix(tm, tmId);
        
        try {
            getDefaultTrafficMatrix(asId);
        } catch (InvalidTrafficMatrixException e) {
            try {
                setDefaultTrafficMatrix(asId, tmId);
            } catch (InvalidTrafficMatrixException e1) {
                //should not happen
                e1.printStackTrace();
            }
        }
    }

    /**
     * Loads the intra-domain traffic matrix contained in the file <code>fileName</code>.
     * @param fileName The name of the file containing the traffic matrix.
     * @param tmId The TM ID you wish to give to the traffic matrix.
     * @param isDefaultTrafficMatrix <code>true</code> if the traffic matrix must be the default traffic matrix for its domain and <code>false</code> otherwise.
     * @return the loaded matrix
     * @throws TrafficMatrixAlreadyExistException If there is already a traffic matrix with the same TM ID for the same domain.
     * @throws InvalidDomainException If the specified domain doesn't exist.
     * @throws NodeNotFoundException If there is an unknown node in the traffic matrix.
     * @throws InvalidTrafficMatrixException If the matrix is not valid (file does not exist, xml error,...)
     */
    public TrafficMatrix loadTrafficMatrix(String fileName, int tmId, boolean isDefaultTrafficMatrix) throws TrafficMatrixAlreadyExistException, InvalidDomainException, NodeNotFoundException, InvalidTrafficMatrixException {
        TrafficMatrix tm = TrafficMatrixFactory.loadTrafficMatrix(fileName);
        try {
            addTrafficMatrix(tm, tmId);
        } catch (TrafficMatrixIdException e) {
            e.printStackTrace();
        }
        if(isDefaultTrafficMatrix) {
            defaultTrafficMatrices.put(tm.getASID(), tmId);
            notifyChangeDefaultTrafficMatrix(tm.getASID(), tm);
        }
        return tm;
    }

    /**
     * Loads the intra-domain traffic matrix contained in the given file.
     * @param file the file containing the traffic matrix.
     * @param tmId The TM ID you wish to give to the traffic matrix.
     * @param isDefaultTrafficMatrix <code>true</code> if the traffic matrix must be the default traffic matrix for its domain and <code>false</code> otherwise.
     * @return the loaded matrix
     * @throws InvalidTrafficMatrixException If the matrix is not valid (file does not exist, xml error,...)
     * @throws TrafficMatrixAlreadyExistException If there is already a traffic matrix with the same TM ID for the same domain.
     * @throws InvalidDomainException If the specified domain doesn't exist.
     * @throws NodeNotFoundException If there is an unknown node in the traffic matrix.
     */
    public TrafficMatrix loadTrafficMatrix(File file, int tmId, boolean isDefaultTrafficMatrix) throws TrafficMatrixAlreadyExistException, InvalidDomainException, NodeNotFoundException, InvalidTrafficMatrixException {
        TrafficMatrix tm = TrafficMatrixFactory.loadTrafficMatrix(file);
        try {
            addTrafficMatrix(tm, tmId);
        } catch (TrafficMatrixIdException e) {
            e.printStackTrace();
        }
        if(isDefaultTrafficMatrix) {
            defaultTrafficMatrices.put(tm.getASID(), tmId);
            notifyChangeDefaultTrafficMatrix(tm.getASID(), tm);
        }
        return tm;
    }


    /**
     * Loads the intra-domain traffic matrix contained in the file <code>fileName</code>.
     * This method generates a TM ID and sets the traffic matrix as default
     * traffic matrix.
     * @param fileName The name of the file containing the traffic matrix.
     * @return the loaded matrix
     * @throws InvalidTrafficMatrixException If the matrix is not valid (file does not exists, xml error,...)
     * @throws InvalidDomainException If the specified domain doesn't exist.
     * @throws NodeNotFoundException If there is an unknown node in the traffic matrix.
     */
    public TrafficMatrix loadTrafficMatrix(String fileName) throws InvalidDomainException, NodeNotFoundException, InvalidTrafficMatrixException {
        TrafficMatrix tm = TrafficMatrixFactory.loadTrafficMatrix(fileName);
        int tmId = generateTMID(tm.getASID());
        try {
            addTrafficMatrix(tm, tmId);
        }
        catch(TrafficMatrixAlreadyExistException e) {
            logger.error("Weird TrafficMatrixAlreadyExistException! Message: "+e.getMessage());
            return tm;
        } catch (TrafficMatrixIdException e) {
            e.printStackTrace();
        }
        defaultTrafficMatrices.put(tm.getASID(), tmId);
        notifyChangeDefaultTrafficMatrix(tm.getASID(), tm);
        return tm;
    }

    /**
     * Saves the traffic matrix <code>tmId</code> of the domain <code>asId</code> to the file <code>fileName</code>.
     * @param asId The AS ID of the target domain.
     * @param tmId The TM ID of the target traffic matrix.
     * @param fileName The name of the target file.
     * @throws InvalidTrafficMatrixException If there is no traffic matrix <code>tmId</code> for the domain <code>asId</code>.
     */
    public void saveTrafficMatrix(int asId, int tmId, String fileName) throws InvalidTrafficMatrixException {
        try {
            TrafficMatrixFactory.saveTrafficMatrix(fileName, getTrafficMatrix(asId, tmId));
        } // If all the code is correct, the exceptions below should never occur.
        catch(NodeNotFoundException e) {
            logger.error("NodeNotFoundException in saveTrafficMatrix. Message: "+e.getMessage());
        }
        catch(InvalidDomainException e) {
            logger.error("InvalidDomainException in saveTrafficMatrix. Message: "+e.getMessage());
        }
    }

    /**
     * Updates the intra-domain traffic matrix <code>tmId</code> with the information contained in the file <code>fileName</code>.
     * @param tmId The traffic matrix to update.
     * @param fileName The name of the file containing the update information.
     * @throws InvalidTrafficMatrixException If the traffic matrix to update can't be retrieved.
     * @throws NodeNotFoundException If the information contained in the file is not consistent with the traffic matrix to update.
     */
    public void updateTrafficMatrix(int tmId, String fileName) throws NodeNotFoundException, InvalidTrafficMatrixException {
        TrafficMatrixFactory.updateTrafficMatrix(fileName, tmId);
    }

    /**
     * Removes the default traffic matrix of the default domain.
     * @throws InvalidTrafficMatrixException If there is no default traffic matrix for the default domain.
     */
    public void removeDefaultTrafficMatrix() throws InvalidTrafficMatrixException {
        int asId = InterDomainManager.getInstance().getDefaultDomain().getASID();
        Integer tmId = defaultTrafficMatrices.get(asId);
        if(tmId == null) {
            throw new InvalidTrafficMatrixException("No default traffic matrix for the default domain.");
        }
        removeTrafficMatrix(asId, tmId);
        notifyChangeDefaultTrafficMatrix(asId, null);
    }

    /**
     * Removes the default traffic matrix of the domain <code>asId</code>.
     * @param asId The AS ID of the target domain.
     * @throws InvalidTrafficMatrixException If there is no default traffic matrix for the domain <code>asId</code>.
     */
    public void removeDefaultTrafficMatrix(int asId) throws InvalidTrafficMatrixException {
        Integer tmId = defaultTrafficMatrices.get(asId);
        if(tmId == null) {
            throw new InvalidTrafficMatrixException("No default traffic matrix for the domain "+asId);
        }
        removeTrafficMatrix(asId, tmId);
        notifyChangeDefaultTrafficMatrix(asId, null);
    }

    /**
     * Removes all traffic matrices for the domain given by its asId
     * @param asId
     */
    public void removeTrafficMatrices(int asId) {
        HashMap<Integer, TrafficMatrix> tms = trafficMatrices.get(asId);
        if (tms == null) return;
        for (Integer i : tms.keySet()) {
            try {
                removeTrafficMatrix(asId, i.intValue());
            } catch (InvalidTrafficMatrixException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Removes the traffic matrix <code>tmId</code> of the domain <code>asId</code>.
     * @param asId The AS ID of the target domain.
     * @param tmId The TM ID of the target traffic matrix.
     * @throws InvalidTrafficMatrixException If there is no traffic matrix <code>tmId</code> for the domain <code>asId</code>.
     */
    public void removeTrafficMatrix(int asId, int tmId) throws InvalidTrafficMatrixException {
        HashMap<Integer, TrafficMatrix> tms = trafficMatrices.get(asId);
        if((tms == null) || (!tms.containsKey(tmId))) {
            throw new InvalidTrafficMatrixException("No traffic matrix "+tmId+" for the domain "+asId);
        }
        TrafficMatrix tm = tms.remove(tmId);
        if(tms.size() == 0) {
            trafficMatrices.remove(asId);
        }
        Integer defaultTmId = defaultTrafficMatrices.get(asId);
        boolean suppressDefault = false;
        if((defaultTmId != null) && (defaultTmId.intValue() == tmId)) {
            defaultTrafficMatrices.remove(asId);
            suppressDefault = true;
        }
        tm.unsetTmId();
        notifyRemoveTrafficMatrix(tm, tmId);
        if (suppressDefault) notifyChangeDefaultTrafficMatrix(asId, null);
    }

    /**
     * Removes all the traffic matrices from the
     * <code>TrafficMatrixManager</code>.<br>
     * Warning: this method does not signal the listeners. Use with caution.
     */
    public void removeAllTrafficMatrices() {
        for (HashMap<Integer, TrafficMatrix> hash : trafficMatrices.values()) {
            if (hash == null) continue;
            for (TrafficMatrix tm : hash.values()) tm.unsetTmId();
        }
        trafficMatrices.clear();
        defaultTrafficMatrices.clear();
    }

    /**
     * Create a new traffic matrix with all values set to zero.
     *
     * @param asId asid of the domain for which the matrix is to be created
     * @param tmId desired traffic matrix id
     * @return The new traffic matrix.
     * @throws InvalidDomainException If the domain with given asid is not loaded
     * @throws TrafficMatrixAlreadyExistException If a matrix with the same tmId already exists
     */
    public TrafficMatrix createEmptyTrafficMatrix(int asId, int tmId) throws InvalidDomainException, TrafficMatrixAlreadyExistException {
        TrafficMatrix tm = new TrafficMatrixImpl(asId);
        try {
            addTrafficMatrix(tm, tmId);
        } catch (TrafficMatrixIdException e) {
            /* should not happen */
            logger.fatal("Unexpected error with the traffic matrix ID.");
            e.printStackTrace();
        }
        return tm;
    }

    /**
     * Create a new traffic matrix with all values set to zero.
     *
     * @param asId asid of the domain for which the matrix is to be created
     * @return The new traffic matrix.
     * @throws InvalidDomainException If the domain with given asid is not loaded
     */
    public TrafficMatrix createEmptyTrafficMatrix(int asId) throws InvalidDomainException {
        try {
            TrafficMatrix tm = createEmptyTrafficMatrix(asId, generateTMID(asId));
            return tm;
        } catch (TrafficMatrixAlreadyExistException e) {
            /* should not happen */
            logger.fatal("Unexpected error when creating empty traffix matrix.");
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generates a new traffic matrix <code>tmId</code> for the domain <code>asId</code>.
     * @deprecated Use {@link be.ac.ulg.montefiore.run.totem.topgen.traffic.TrafficGeneratorInterface} and sub-classes instead and {@link be.ac.ulg.montefiore.run.totem.topgen.traffic.TrafficGeneratorFactory}.
     * @param asId The target domain.
     * @param tmId The TM ID that the new traffic matrix will have.
     * @param model The model used to generate the matrix.
     * @param fraction The fraction of the nodes that generate traffic.
     * @throws TrafficMatrixAlreadyExistException If there is already a traffic matrix <code>tmId</code> for the domain <code>asId</code>.
     * @throws InvalidDomainException If the domain <code>asId</code> doesn't exist.
     * @throws RoutingException If an error occurred during the routing process.
     * @throws NoRouteToHostException If there is no route between a pair of nodes.
     * @throws TrafficGenerationException If the generator didn't succeed in generating traffic with the required parameters.
     */
    public void generateTrafficMatrix(int asId, int tmId, TrafficModel model, double fraction) throws TrafficMatrixAlreadyExistException, InvalidDomainException, RoutingException, NoRouteToHostException, TrafficGenerationException {
        generateTrafficMatrix(asId,tmId,model,fraction,false);
    }

    /**
     * Generates a new traffic matrix <code>tmId</code> for the domain <code>asId</code>. (FSK)
     *
     * @deprecated Use {@link be.ac.ulg.montefiore.run.totem.topgen.traffic.TrafficGeneratorInterface} and sub-classes instead and {@link be.ac.ulg.montefiore.run.totem.topgen.traffic.TrafficGeneratorFactory}.
     * @param asId The target domain.
     * @param tmId The TM ID that the new traffic matrix will have.
     * @param model The model used to generate the matrix.
     * @param fraction The fraction of the nodes that generate traffic.
     * @param generateOnlyEdgeTraffic true if the generator must only generate traffic for edge nodes
     * @return the generated matrix
     * @throws TrafficMatrixAlreadyExistException If there is already a traffic matrix <code>tmId</code> for the domain <code>asId</code>.
     * @throws InvalidDomainException If the domain <code>asId</code> doesn't exist.
     * @throws RoutingException If an error occurred during the routing process.
     * @throws NoRouteToHostException If there is no route between a pair of nodes.
     */
    public TrafficMatrix generateTrafficMatrix(int asId, int tmId, TrafficModel model, double fraction,
                                      boolean generateOnlyEdgeTraffic) throws InvalidDomainException, TrafficMatrixAlreadyExistException, NoRouteToHostException, RoutingException {
        /*
        HashMap<Integer, TrafficMatrix> tms = trafficMatrices.get(asId);
        //Domain domain = InterDomainManager.getInstance().getDomain(asId);
        if((tms != null) && (tms.containsKey(tmId))) {
        throw new TrafficMatrixAlreadyExistException("There is already a traffic matrix "+tmId+" for the domain "+asId);
        }

        //RoutingMatrix matrix = new BooleanRoutingMatrix(InterDomainManager.getInstance().getDomain(asId));
        //TrafficGenerator generator = new TrafficGenerator(model, matrix, fraction);
        TrafficGenerator generator = new TrafficGenerator(model,fraction);
        generator.setGenerateOnlyEdgeTraffic(generateOnlyEdgeTraffic);
        generator.generate();

        try {
        TrafficMatrix tm = new TrafficMatrixImpl(asId);
        double[] traffic = generator.getTraffic();
        for (int i = 0; i < traffic.length; ++i) {
        IntPair ids = generator.getIds(i);
        int src = ids.getFirstInteger();
        int dst = ids.getSecondInteger();
        tm.set(src, dst, (float) traffic[i]);
        }
        if(tms == null) {
        tms = new HashMap<Integer, TrafficMatrix>();
        trafficMatrices.put(asId, tms);
        }
        tms.put(tmId, tm);
        } catch (NodeNotFoundException e) {
        logger.error("NodeNotFoundException in generateTrafficMatrix. Message: "+e.getMessage());
        } */

        TrafficGenerator generator = new TrafficGenerator(asId,model,fraction);
        try {
            TrafficMatrix tm = generator.generateTM();
            this.addTrafficMatrix(tm,tmId);
            return tm;
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        } catch (TrafficMatrixIdException e) {
            e.printStackTrace();
        } catch (DataConsistencyException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generates a TM ID that can be used for the domain <code>asId</code>.
     * @param asId The target domain.
     * @return A TM ID that can be used for the domain <code>asId</code>.
     * @throws InvalidDomainException If there is no domain <code>asId</code>.
     */
    public int generateTMID(int asId) throws InvalidDomainException {
        InterDomainManager.getInstance().getDomain(asId);
        HashMap<Integer, TrafficMatrix> tms = trafficMatrices.get(asId);

        if(tms == null) {
            return 0;
        }

        do {
            int tmId = IdGenerator.getInstance().generateIntId();
            if(!tms.containsKey(tmId)) {
                return tmId;
            }
        } while(true);
    }

    /**
     * A new domain added.
     *
     * @param domain The new domain.
     */
    public void addDomainEvent(Domain domain) {
    }

    /**
     * A domain removed.
     *
     * @param domain A reference to the domain removed.
     */
    public void removeDomainEvent(Domain domain) {
        removeTrafficMatrices(domain.getASID());
    }

    /**
     * The default domain has changed for the specified domain.
     *
     * @param domain the new default domain.
     */
    public void changeDefaultDomainEvent(Domain domain) {
    }

    public void changeAsId(int oldASID, int newASID) {
        HashMap<Integer, TrafficMatrix> tms = trafficMatrices.get(oldASID);
        trafficMatrices.remove(oldASID);
        defaultTrafficMatrices.remove(oldASID);
        trafficMatrices.put(newASID, tms);
    }
}
