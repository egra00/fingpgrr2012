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

package it.unina.repository.MIRA;

import org.apache.log4j.Logger;
import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.*;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.PathImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.LspImpl;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.util.IdGenerator;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

/*
 * Changes:
 * --------
 * - 8-Nov-2005 : listeners bugfix & take addLsp parameter into account. (GMO)
 * - 29-Nov-2005 : Added the possibility to obtain the algorithm parameters (getStartAlgoParameters()). (GMO)
 * - 29-Nov-2005 : Added the possibility to obtain the additional routing parameters (getPrimaryRoutingParameters()). (GMO)
 * - 08-Dec-2005 : Implement new getRunningParameters() from the TotemAlgorithm interface. (GMO)
 * - 23-Aug-2006 : Invalidate database if backups are used in conjonction with BW Sharing, better error handling. (GMO)
 * - 05-Mar-2007 : Load library on algo start (GMO)
 * - 17-Sep-2007: set DB valid when starting/stopping the algorithm (GMO)
 */

/**
 * This class implements the integration of MIRA (Unina).
 *
 * This is the algorithms described in :
 *
 * NewMira (Wang, B., Su, X., Chen, C.: A New Bandwidth Guaranteed Routing Algorithm for MPLS Traffic Engineering.
 * In Proceedings of IEEE International Conference on Communications, ICC 2002)
 *
 * Simple MIRA (Iliadis, I., Bauer, D.: A New Class of Online Minimum-Interference Routing Algorithms.
 * NETWORKING 2002, LNCS 2345 (2002) 959-971)
 *
 * <p/>
 * <p>Creation date : 6 juin 2005 14:55:06
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class MIRA implements LSPPrimaryRouting, DomainSyncAlgorithm {
    private static final Logger logger = Logger.getLogger(MIRA.class);

    private static DomainConvertor convertor = null;
    private static Domain domain = null;
    private static MIRAChangeListener changeListener = null;

    // MIRA does not record the LSPs. We have to do it here for it.
    private static HashMap<String, Lsp> listOfLsp;

    private static HashMap runningParams = null;
    private static final ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();
    private static final ArrayList<ParameterDescriptor> routingParams = new ArrayList<ParameterDescriptor>();

    static {
        try {
            params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
            params.add(new ParameterDescriptor("version", "Version to use (SMIRA or NEWMIRA).", String.class, "SMIRA", new String[] {"SMIRA", "NEWMIRA"}));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
        try {
            routingParams.add(new ParameterDescriptor("addLSP", "Tell whether computed LSP should be directly added to local algorithm-specific DB.", Boolean.class, new Boolean(false)));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    private static boolean DBValid = true;
    
    public void start(HashMap params) throws AlgorithmInitialisationException {
        try {
            System.loadLibrary("mira");
        } catch (UnsatisfiedLinkError e) {
            throw new LibraryInitialisationException("Cannot load library mira.");
        }


        int ASID=0;

        runningParams = params;

        // Initialize the list of LSPs
        listOfLsp = new HashMap<String, Lsp>();

        // if MIRA is not used with scenarios, the ASID might not be passed adequately
        if (params.get("ASID")==null){
            domain = InterDomainManager.getInstance().getDefaultDomain();
            ASID = domain.getASID();
            logger.warn("You've not specified a domain when starting MIRA, default domain with ASID " + ASID + " was used");

        }else{
            ASID = Integer.parseInt((String)params.get("ASID"));
            try{
                domain = InterDomainManager.getInstance().getDomain(ASID);
            }catch(InvalidDomainException e){
                e.printStackTrace();
            }
        }

        changeListener = new MIRAChangeListener(domain, this);
        domain.getObserver().addListener(changeListener);

        int miraVersion = 0; // specify if we want to use s-MIRA (type = 0) or new-MIRA (type = 1)
        if (params.get("version")!=null) {
            if (((String) params.get("version")).equals("SMIRA")) {
                logger.info("SMIRA is used");
                miraVersion = 0;
            }
            else if (((String) params.get("version")).equals("NEWMIRA")) {
                logger.info("NEWMIRA is used");
                miraVersion = 1;
            }
            else {
                logger.warn("The version of MIRA you entered is not known...Taking SMIRA by default !");
            }
        }
        else {
            logger.warn("No version of MIRA found...Taking SMIRA by default !");
        }

        try{
            JNIMIRA.jniinitMira(miraVersion);
        }
        catch(Exception e){
            e.printStackTrace();
            throw new LibraryInitialisationException(e.getMessage());
        }


        convertor = domain.getConvertor();

        //logger.info("Adding nodes");

        // adding nodes
        List<Node> nodes = domain.getUpNodes();

        for(Iterator<Node> it1 = nodes.iterator(); it1.hasNext();) {
            Node currentNode = it1.next();
            String nodeId = currentNode.getId();

            int type = 1;
            if (currentNode.getNodeType() == Node.Type.CORE)
                type = 0;


            try{

                int intnodeId = convertor.getNodeId(nodeId);
                //System.out.println("Call of JNI add node");
                JNIMIRA.jniaddNode(intnodeId, type);
            }
            catch(Exception e){
                e.printStackTrace();
            }

        }

        //logger.info("Adding links");
        // adding links
        List<Link> links = domain.getUpLinks();

        for(Iterator<Link> it1 = links.iterator(); it1.hasNext();){
            Link link = it1.next();

            try{

                int srcnodeId = convertor.getNodeId(link.getSrcNode().getId());
                int dstnodeId = convertor.getNodeId(link.getDstNode().getId());

                //System.out.println("call of JNI add link");
                JNIMIRA.jniaddLink(srcnodeId, dstnodeId, link.getBandwidth(), link.getReservedBandwidth(), link.getMetric());

            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        //logger.info("Adding lsps");
        // adding lsps if they exist
        if (domain.getAllLsps().size() != 0){

            List<Lsp> lspsList = domain.getAllLsps();

            for (Iterator<Lsp> it = lspsList.iterator(); it.hasNext();){

                try{
                    //logger.info("Add lsp is called");
                    //System.out.println("Call of JNI add LSP");
                    addLSP(it.next());

                }
                catch(RoutingException e)
                {
                    logger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
        }
        DBValid = true;
    }

    /**
     * Cleans all Mira data structures
     */
    public void stop() {
        runningParams = null;
        domain.getObserver().removeListener(changeListener);
        changeListener = null;
        DBValid = false;
        try{
            //System.out.println("Call of kill MIRA");
            JNIMIRA.jnikillMira();
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Computes paths with MIRA for a list of demands.
     * This method just calls the routeLSP method for each demand.
     *
     * @param param the list of the demand specified as a list of LSPPrimaryRoutingParameter
     * @return a list of AddLspAction
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException
     * @throws be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException
     */
    public TotemActionList routeNLSP(Domain dom, List<LSPPrimaryRoutingParameter> param) throws RoutingException, NoRouteToHostException, LocalDatabaseException {
        if (domain.getASID() != dom.getASID()){
            throw new LocalDatabaseException("ERROR: ASID from route computation differs from the one loaded into MIRA DB");
        }
        TotemActionList fullActionList = new TotemActionList();
        TotemActionList currentActionList;
        for (int i = 0; i < param.size(); i++) {
            currentActionList = routeLSP(domain,param.get(i));
            fullActionList.add(currentActionList.get(0));
        }

        return fullActionList;
    }

    /**
     * Computes a path with MIRA for one demand from a source node
     * to a destination node with a bandwidth requirement
     * @param param contains the source, destination, bandwidth, PL, OA, ...
     * @return a list of actions
     * @throws RoutingException
     * @throws NoRouteToHostException
     */
    public TotemActionList routeLSP(Domain dom, LSPPrimaryRoutingParameter param) throws RoutingException, NoRouteToHostException, LocalDatabaseException {
        if (!isDBValid()) {
            throw new LocalDatabaseException("Database is invalid. Please restart the algorithm.");
        }

        if (domain.getASID() != dom.getASID()){
            throw new LocalDatabaseException("ERROR: ASID from route computation differs from the one loaded into MIRA DB");
        }

        boolean addLSP = false;
        if (param.getRoutingAlgorithmParameter("addLSP")!=null){
            addLSP = Boolean.parseBoolean((String)param.getRoutingAlgorithmParameter("addLSP"));
        }

        String srcNode = param.getSrcNode();
        String dstNode = param.getDstNode();
        float bw = param.getBandwidth();
        int[] path = null;

        String lspId = null;
        if (param.getLspId()==null){
            lspId = IdGenerator.getInstance().generateStringId("Lsp");
        } else {
            lspId = param.getLspId();
        }

        try {
            convertor.addLspId(lspId);

            int srcnodeId = convertor.getNodeId(srcNode);
            int dstnodeId = convertor.getNodeId(dstNode);

            int addLSPtoDB = 0;
            if (addLSP) {
                addLSPtoDB = 1;
            }

            //System.out.println("Call of JNI compute path");
            path = JNIMIRA.jnicomputePath(srcnodeId, dstnodeId, bw, addLSPtoDB);
        }
        catch (AddDBException e){
            logger.warn("This primary path failed to add to MIRA database, or preempted LSPs can't be removed!");
            if (logger.isDebugEnabled()){
                e.printStackTrace();
            }
            throw new RoutingException();
        }
        catch (LspAlreadyExistException e){
            logger.warn("Error with lsp ids string to int conversion");
            if (logger.isDebugEnabled()){
                e.printStackTrace();
            }
            throw new RoutingException();
        }
        catch (NoRouteToHostException e){
            logger.warn("Impossible to compute a path for this LSP!");
            if (logger.isDebugEnabled()){
                e.printStackTrace();
            }
            throw new NoRouteToHostException();
        }
        catch (RoutingException e){
            logger.warn("Problem with the preemptList!");
            if (logger.isDebugEnabled()){
                e.printStackTrace();
            }
            throw new RoutingException();
        }
        catch (NodeNotFoundException e){
            logger.warn("Node not found!");
            if (logger.isDebugEnabled()){
                e.printStackTrace();
            }
            throw new RoutingException();
        }


        Path returnPath = null;
        try{
            List<Node> nodeList = new ArrayList<Node>(path.length);

            for (int i=0;i<path.length;i++){

                nodeList.add(domain.getNode(convertor.getNodeId(path[i])));

            }

            returnPath = new PathImpl(domain);
            returnPath.createPathFromNode(nodeList);

        }catch(Exception e){
            e.printStackTrace();
        }



        LspImpl lsp = new LspImpl(domain,lspId,bw,returnPath);
        lsp.setInitParameters(param);

        TotemAction addLsp = new AddLspAction(domain,lsp);

        TotemActionList actionList = new TotemActionList();

        actionList.add(addLsp);

        if (addLSP) {
            listOfLsp.put(lsp.getId(), lsp);
        }

        return actionList;
    }

    /**
     * Adds an LSP to MIRA database
     * @param lsp the LSP to be added
     * @throws RoutingException
     */
    public void addLSP(Lsp lsp) throws RoutingException {

        if (lsp.isBackupLsp() && domain.useBandwidthSharing()) {
            invalidateDB();
            throw new RoutingException("MIRA doesn't support bandwidth sharing.");
        }

        try{
            List<Node> pathnodeList = lsp.getLspPath().getNodePath();


            int[] pathArray = new int[pathnodeList.size()];

            int i=0;
            //logger.info("Path: ");
            for (Iterator<Node> it = pathnodeList.iterator(); it.hasNext(); ){

                pathArray[i++]=convertor.getNodeId(it.next().getId());
                //logger.info(" " + pathArray[i-1]);
            }

            listOfLsp.put(lsp.getId(), lsp);

            //System.out.println("Call of JNI add path");
            JNIMIRA.jniaddPath(pathArray,lsp.getReservation());

        } catch(AddDBException e) {
            e.printStackTrace();
            throw new RoutingException("Failed to add lsp " + lsp.getId() + " to MIRA database");
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove an LSP from MIRA database
     * @param lspid
     * @throws RoutingException
     */
    public void removeLSP(String lspid) throws RoutingException{
        try{
            Lsp lspToRemove = listOfLsp.get(lspid);

            if (lspToRemove == null) {
                throw new AddDBException("The lsp you want to remove from MIRA DB is not present in MIRA DB !");
            }

            List<Node> pathnodeList = lspToRemove.getLspPath().getNodePath();


            int[] pathArray = new int[pathnodeList.size()];

            int i=0;
            //logger.info("Path: ");
            for (Iterator<Node> it = pathnodeList.iterator(); it.hasNext(); ){

                pathArray[i++]=convertor.getNodeId(it.next().getId());
                //logger.info(" " + pathArray[i-1]);

            }


            //System.out.println("Call of JNI remove path");
            JNIMIRA.jniremovePath(pathArray, lspToRemove.getReservation());
        }catch(AddDBException e){
            e.printStackTrace();
            logger.warn("Failed to remove lsp " + lspid + " from MIRA database");
            throw new RoutingException();

        }
        catch(Exception e){
            e.printStackTrace();
        }


    }
    
    public List<ParameterDescriptor> getStartAlgoParameters() {
    	return (List<ParameterDescriptor>)params.clone();
    }

    public HashMap getRunningParameters() {
        return (runningParams == null) ? null : (HashMap)runningParams.clone();
    }

    public List<ParameterDescriptor> getPrimaryRoutingParameters() {
        return (List<ParameterDescriptor>) routingParams.clone();
    }

    public boolean isDBValid() {
        return DBValid;
    }

    public void invalidateDB() {
        DBValid = false;
    }

    public void restart() {
        HashMap params = getRunningParameters();
        stop();
        try {
            start(params);
        } catch (AlgorithmInitialisationException e) {
            e.printStackTrace();
        }
    }
}
