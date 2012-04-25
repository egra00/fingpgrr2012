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
package be.ac.ulg.montefiore.run.totem.scenario.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.*;
import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.LSPBackupCreationType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.MethodType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ParamType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ProtectionType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.RoutingAlgo;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.LSPBackupCreationImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

/*
 * Changes:
 * --------
 * - 19-Dec-2006: bugfix wrong default value if protectionType or methodType not set. (GMO)
 * - 29-Nov-2007: adapt to new algorithm backup routing interfaces (separate LSPDetourRouting and LSPBypassRouting). (GMO)
 * - 29-Feb-2008: deprectaed usage (GMO)
 */

/**
 * This class implements a LSP backup creation event.
 *
 * <p>Creation date: 02-d�c.-2004
 *
 * @deprecated Use LSPDetourCreation and LSPBypassCreation instead. This implementation might be outdated.
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class LSPBackupCreation extends LSPBackupCreationImpl implements Event {

    private final static Logger logger = Logger.getLogger(LSPBackupCreation.class);
    
    public LSPBackupCreation() {}

    private LSPBackupCreation(String algoName, HashMap algoParams) {
        ObjectFactory factory = new ObjectFactory();
        try {
            RoutingAlgo routingAlgo = factory.createRoutingAlgo();
            routingAlgo.setName(algoName);
            setRoutingAlgo(routingAlgo);
            
            if(algoParams == null) {
                return;
            }
            
            Set set = algoParams.entrySet();
            for (Iterator iter = set.iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                ParamType param = factory.createParamType();
                param.setName((String) entry.getKey());
                param.setValue((String) entry.getValue());
                routingAlgo.getParam().add(param);
            }
        }
        catch(JAXBException e) {
            logger.error("JAXBException in a constructor of LSPBackupCreation. Reason: "+e.getMessage());
        }
    }
    
    /**
     * Creates a new <code>LSPBackupCreation</code> element with a <code>Detour</code> subelement. Note
     * that <code>algoParams</code> can be <code>null</code>.
     */
    public LSPBackupCreation(String protectedLSP, String algoName, HashMap algoParams) {
        this(algoName, algoParams);
        ObjectFactory factory = new ObjectFactory();
        try {
            DetourType detour = factory.createLSPBackupCreationTypeDetourType();
            setDetour(detour);
            detour.setProtectedLSP(protectedLSP);
        }
        catch(JAXBException e) {
            logger.error("JAXBException in a constructor of LSPBackupCreation. Reason: "+e.getMessage());
        }
    }
    
    public LSPBackupCreation(String protectedLSP, String algoName, HashMap algoParams, int asId) {
        this(protectedLSP, algoName, algoParams);
        setASID(asId);
    }

    /**
     * Creates a new <code>LSPBackupCreation</code> element with a <code>Detour</code> subelement. Note
     * that <code>algoParams</code> can be <code>null</code>.
     */
    public LSPBackupCreation(String lspId, float bw, String protectedLSP, String algoName, HashMap algoParams) {
        this(protectedLSP, algoName, algoParams);
        setLspId(lspId);
        setBw(bw);
    }
    
    public LSPBackupCreation(String lspId, float bw, String protectedLSP, String algoName, HashMap algoParams, int asId) {
        this(lspId, bw, protectedLSP, algoName, algoParams);
        setASID(asId);
    }

    /**
     * Creates a new <code>LSPBackupCreation</code> element with a <code>Detour</code> subelement. Note
     * that <code>algoParams</code> can be <code>null</code>.
     */
    public LSPBackupCreation(String lspId, float bw, String protectedLSP, MethodType methodType, ProtectionType protectionType, String algoName, HashMap algoParams) {
        this(lspId, bw, protectedLSP, algoName, algoParams);
        getDetour().setMethodType(methodType);
        getDetour().setProtectionType(protectionType);
    }
    
    public LSPBackupCreation(String lspId, float bw, String protectedLSP, MethodType methodType, ProtectionType protectionType, String algoName, HashMap algoParams, int asId) {
        this(lspId, bw, protectedLSP, methodType, protectionType, algoName, algoParams);
        setASID(asId);
    }

    /**
     * Creates a new <code>LSPBackupCreation</code> element with a <code>Bypass</code> subelement. Note
     * that <code>algoParams</code> can be <code>null</code> and the list must contain the IDs of the
     * protected links.
     */
    public LSPBackupCreation(List protectedLinks, String algoName, HashMap algoParams) {
        this(algoName, algoParams);
        ObjectFactory factory = new ObjectFactory();
        try {
            BypassType bypass = factory.createLSPBackupCreationTypeBypassType();
            setBypass(bypass);
            
            for (Iterator iter = protectedLinks.iterator(); iter.hasNext();) {
                String pLink = (String) iter.next();
                BypassType.ProtectedLinkType protectedLink = factory.createLSPBackupCreationTypeBypassTypeProtectedLinkType();
                protectedLink.setLinkId(pLink);
                bypass.getProtectedLink().add(protectedLink);
            }
        }
        catch(JAXBException e) {
            logger.error("JAXBException in a constructor of LSPBackupCreation. Reason: "+e.getMessage());
        }
    }

    public LSPBackupCreation(List protectedLinks, String algoName, HashMap algoParams, int asId) {
        this(protectedLinks, algoName, algoParams);
        setASID(asId);
    }
    
    /**
     * Creates a new <code>LSPBackupCreation</code> element with a <code>Bypass</code> subelement. Note
     * that <code>algoParams</code> can be <code>null</code> and the list must contain the IDs of the
     * protected links.
     */
    public LSPBackupCreation(String lspId, float bw, List protectedLinks, String algoName, HashMap algoParams) {
        this(protectedLinks, algoName, algoParams);
        setLspId(lspId);
        setBw(bw);
    }
    
    public LSPBackupCreation(String lspId, float bw, List protectedLinks, String algoName, HashMap algoParams, int asId) {
        this(lspId, bw, protectedLinks, algoName, algoParams);
        setASID(asId);
    }
    
    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a LSP backup creation event.");
        logger.warn("The LSPBackupCreation event is deprecated.");

        Domain domain = null;
        try {
            if(isSetASID()) {
                domain = InterDomainManager.getInstance().getDomain(_ASID);
            }
            else {
                domain = InterDomainManager.getInstance().getDefaultDomain();
            }
        }
        catch(InvalidDomainException e) {
            logger.error("Unknown domain "+_ASID);
            throw new EventExecutionException(e);
        }
        
        if(_Bw < 0)  {
            logger.error("Bad parameter: bw < 0!");
            throw new EventExecutionException("Bad parameter: bw < 0!");
        }
        
        if(isSetLspId()) {
            try {
                domain.getLsp(_LspId);
                logger.error("Bad parameter: existing LSP ID!");
                throw new EventExecutionException("Bad parameter: existing LSP ID!");
            }
            catch(LspNotFoundException e) {}
        }
        
        
        // Try to find the routing algorithm
        String algoName = _RoutingAlgo.getName();
        LSPBackupRouting routingAlgo = null;
        try {
            if(this.isSetASID()) {
                routingAlgo = (LSPBackupRouting) RepositoryManager.getInstance().getAlgo(algoName, this.getASID());
            }
            else {
                routingAlgo = (LSPBackupRouting) RepositoryManager.getInstance().getAlgo(algoName);
            }
        }
        catch(NoSuchAlgorithmException e) {
            logger.error("Algorithm not started or unknown routing algorithm "+algoName);
            throw new EventExecutionException(e);
        }
        catch(ClassCastException e) {
            logger.error("The specified algorithm isn't a LSP backup routing algorithm!");
            throw new EventExecutionException(e);
        }
        
        // Fetch the common parameters
        LSPRoutingParameter params;
        if(isSetBypass()) {
            if(isSetLspId()) {
                params = new LSPBypassRoutingParameter(_LspId);
            }
            else {
                params = new LSPBypassRoutingParameter(domain.generateLspId());
            }
        }
        else {
            if(isSetLspId()) {
                params = new LSPDetourRoutingParameter(_LspId);
            }
            else {
                params = new LSPDetourRoutingParameter(domain.generateLspId());
            }
        }

        params.setBandwidth(_Bw);

        List algoParams = _RoutingAlgo.getParam();
        for (Iterator iter = algoParams.iterator(); iter.hasNext();) {
            ParamType param = (ParamType) iter.next();
            params.putRoutingAlgorithmParameter(param.getName(), param.getValue());
        }

        // Fetch the proper parameters and call the proper method.
        TotemActionList actionList;
        if(isSetBypass()) {
            if (getBypass().isSetDst()) {
                try {
                    Node n = domain.getNode(getBypass().getDst());
                    ((LSPBypassRoutingParameter)params).setDstNode(n.getId());
                } catch (NodeNotFoundException e) {
                    logger.error("Unknown node "+getBypass().getDst());
                    throw new EventExecutionException(e);
                }
            }
            List links = getBypass().getProtectedLink();
            for (Iterator iter = links.iterator(); iter.hasNext();) {
                LSPBackupCreationType.BypassType.ProtectedLinkType link = (LSPBackupCreationType.BypassType.ProtectedLinkType) iter.next();
                try {
                    domain.getConvertor().getLinkId(link.getLinkId());
                }
                catch(LinkNotFoundException e) {
                    logger.error("Unknown link "+link.getLinkId());
                    throw new EventExecutionException(e);
                }
                ((LSPBypassRoutingParameter)params).addProtectedLink(link.getLinkId());
            }
            try {
                actionList = ((LSPBypassRouting)routingAlgo).routeBypass(domain, (LSPBypassRoutingParameter)params);
            }
            catch(RoutingException e) {
                logger.error("An error occurred during the routing of the LSP. Reason: "+e.getMessage());
                throw new EventExecutionException(e);
            }
            catch(NoRouteToHostException e) {
                logger.error("The routing algorithm was unable to find a route that met the requirements.");
                throw new EventExecutionException(e);
            }
            catch(ClassCastException e) {
               logger.error("The specified algorithm isn't a Bypass LSP backup routing algorithm!");
                throw new EventExecutionException(e);
            }
        }
        else {
            try {
                domain.getLsp(getDetour().getProtectedLSP());
            }
            catch(LspNotFoundException e) {
                logger.error("The LSP to protect "+getDetour().getProtectedLSP()+" was not found!");
                throw new EventExecutionException(e);
            }
            ((LSPDetourRoutingParameter)params).setProtectedLSP(getDetour().getProtectedLSP());
            if (getDetour().isSetProtectionType())
                ((LSPDetourRoutingParameter)params).setProtectionType(getDetour().getProtectionType().getValue());
            if (getDetour().isSetMethodType())
                ((LSPDetourRoutingParameter)params).setMethodType(getDetour().getMethodType().getValue());


            try {
                actionList = ((LSPDetourRouting)routingAlgo).routeDetour(domain, (LSPDetourRoutingParameter)params);
            }
            catch(RoutingException e) {
                logger.error("An error occurred during the routing of the LSP. Reason: "+e.getMessage());
                throw new EventExecutionException(e);
            } catch(NoRouteToHostException e) {
                logger.error("The routing algorithm was unable to find a route that met the requirements.");
                throw new EventExecutionException(e);
            } catch (LocalDatabaseException e) {
                logger.error("Local Database Exception: " + e.getMessage());
                throw new EventExecutionException(e);
            } catch(ClassCastException e) {
                logger.error("The specified algorithm isn't a Detour LSP backup routing algorithm!");
                throw new EventExecutionException(e);
            }
        }

        // Execute the returned actions.
        Lsp lsp = null;
        String msg = "";
        for (Iterator iter = actionList.iterator(); iter.hasNext();) {
            TotemAction action = (TotemAction) iter.next();
            if (action instanceof AddLspAction) {
                AddLspAction addLsp = (AddLspAction) action;
                lsp = addLsp.getLsp();
                msg += addLsp.getLsp().getLspPath().toString();
            } else if (action instanceof PreemptLspsAction) {
                PreemptLspsAction pAction = (PreemptLspsAction) action;
                if (!pAction.getLsps().isEmpty()) {
                    msg += "\n";
                    //System.out.println("Following LSPs will be preempted:");
                    msg += "Following LSPs will be preempted:\n";
                    for (String lspId : pAction.getLsps()) {
                        //System.out.println("\t" + lspId);
                        msg += "\t" + lspId;
                    }
                    //System.out.println("EOL");
                }
            } else {
                logger.warn("Another (unknown) action was found when routing the LSP.");
            }
            try {
                action.execute();
            } catch (TotemActionExecutionException e) {
                e.printStackTrace();
                throw new EventExecutionException(e);
            }
        }

        return new EventResult(lsp, msg);
    }
}
