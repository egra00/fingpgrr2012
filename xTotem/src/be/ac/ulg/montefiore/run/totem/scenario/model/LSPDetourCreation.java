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

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.LSPDetourCreationImpl;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.*;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.LspBackupType;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.PathImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.LspImpl;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.*;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.*;
import be.ac.ulg.montefiore.run.totem.repository.model.*;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.util.*;

/*
* Changes:
* --------
* - 28-Feb-2008: separate LSPBackupCreation in LSPBypassCreation and LSPDetourCreation (GMO)
*/

/**
 *  This class implements a LSP detour creation event.
 *
 * <p>Creation date: 29/02/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class LSPDetourCreation extends LSPDetourCreationImpl implements Event {

    private final static Logger logger = Logger.getLogger(LSPDetourCreation.class);

    public LSPDetourCreation() {}

    private LSPDetourCreation(String algoName, HashMap algoParams) {
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
     * Creates a new <code>LSPDetourCreation</code> element. Note
     * that <code>algoParams</code> can be <code>null</code>.
     */
    public LSPDetourCreation(String protectedLSP, String algoName, HashMap algoParams) {
        this(algoName, algoParams);
        setProtectedLSP(protectedLSP);
    }

    public LSPDetourCreation(String protectedLSP, String algoName, HashMap algoParams, int asId) {
        this(protectedLSP, algoName, algoParams);
        setASID(asId);
    }

    /**
     * Creates a new <code>LSPDetourCreation</code> element. Note
     * that <code>algoParams</code> can be <code>null</code>.
     */
    public LSPDetourCreation(String lspId, float bw, String protectedLSP, String algoName, HashMap algoParams) {
        this(protectedLSP, algoName, algoParams);
        setLspId(lspId);
        setBw(bw);
    }

    public LSPDetourCreation(String lspId, float bw, String protectedLSP, String algoName, HashMap algoParams, int asId) {
        this(lspId, bw, protectedLSP, algoName, algoParams);
        setASID(asId);
    }

    /**
     * Creates a new <code>LSPDetourCreation</code> element. Note
     * that <code>algoParams</code> can be <code>null</code>.
     */
    public LSPDetourCreation(String lspId, float bw, String protectedLSP, MethodType methodType, ProtectionType protectionType, String algoName, HashMap algoParams) {
        this(lspId, bw, protectedLSP, algoName, algoParams);
        setMethodType(methodType);
        setProtectionType(protectionType);
    }

    public LSPDetourCreation(String lspId, float bw, String protectedLSP, MethodType methodType, ProtectionType protectionType, String algoName, HashMap algoParams, int asId) {
        this(lspId, bw, protectedLSP, methodType, protectionType, algoName, algoParams);
        setASID(asId);
    }



    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a LSP bypass creation event.");

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

        // lsp id
        if(isSetLspId()) {
            try {
                domain.getLsp(_LspId);
                logger.error("Bad parameter: existing LSP ID!");
                throw new EventExecutionException("Bad parameter: existing LSP ID!");
            }
            catch(LspNotFoundException e) {}
        } else {
            setLspId(domain.generateLspId());
        }

        // Try to find the routing algorithm
        LSPDetourRouting routingAlgo = null;
        String algoName = null;
        if (isSetRoutingAlgo()) {
            algoName = _RoutingAlgo.getName();
            try {
                if (this.isSetASID()) {
                    routingAlgo = (LSPDetourRouting) RepositoryManager.getInstance().getAlgo(algoName, this.getASID());
                } else {
                    routingAlgo = (LSPDetourRouting) RepositoryManager.getInstance().getAlgo(algoName);
                }
            } catch (NoSuchAlgorithmException e) {
                logger.error("Algorithm not started or unknown routing algorithm " + algoName);
                throw new EventExecutionException(e);
            } catch (ClassCastException e) {
                logger.error("The specified algorithm isn't a LSP bypass routing algorithm!");
                throw new EventExecutionException(e);
            }
        } else if (isSetPath() && !getPath().getType().toString().equals(LSPPathType._STRICT)) {
            logger.error("A routing algo must be specified if the complete path (STRICT) is not provided.");
            throw new EventExecutionException("A routing algo must be specified if the complete path (STRICT) is not provided.");
        }

        Lsp primaryLsp;
        try {
            primaryLsp = domain.getLsp(getProtectedLSP());
        } catch (LspNotFoundException e) {
            throw new EventExecutionException("The primary lsp to protect was not found.");
        }


        TotemActionList actionList;

        if (isSetPath()) {
            // creates the path object

            if (getPath().getType().toString().equals(LSPPathType._LOOSE)) {
                logger.error("Loose path is not yet implemented.");
                throw new EventExecutionException("Loose path is not yet implemented.");
            }
            //Compute the path
            if (isSetRoutingAlgo()) {
                logger.warn("Routing algo will be ignored since you provided a strict path");
            }

            Path path = new PathImpl(domain);

            try {
                if (getPath().isSetLink()) {
                    List<Link> lList;
                    try {
                        lList = new ArrayList<Link>(getPath().getLink().size());
                        for (String s : (List<String>) getPath().getLink()) {
                            lList.add(domain.getLink(s));
                        }
                    } catch (LinkNotFoundException e) {
                        logger.error("Incorrect Link path: Link not found.");
                        throw new EventExecutionException(e);
                    }
                    path.createPathFromLink(lList);
                } else {
                    List<Node> nList;
                    try {
                        nList = new ArrayList<Node>(getPath().getNode().size());
                        for (String s : (List<String>) getPath().getNode()) {
                            nList.add(domain.getNode(s));
                        }
                    } catch (NodeNotFoundException e) {
                        logger.error("Incorrect Node path: Node not found.");
                        throw new EventExecutionException(e);
                    }
                    path.createPathFromNode(nList);
                }
            } catch (NodeNotFoundException e) {
                logger.error("Error in the path provided.");
                throw new EventExecutionException(e);
            } catch (InvalidPathException e) {
                logger.error("Error in the path provided: path not continuous.");
                throw new EventExecutionException(e);
            }

            /* check path validity for a detour */
            LspBackupType type;
            if (isSetMethodType() && getMethodType() == MethodType.GLOBAL) {
                if (!path.getSourceNode().equals(primaryLsp.getLspPath().getSourceNode()) ||
                        !path.getDestinationNode().equals(primaryLsp.getLspPath().getDestinationNode())) {
                    throw new EventExecutionException("The given path cannot be a global detour for primary lsp: " + primaryLsp.getId());
                }
                type = LspBackupType.DETOUR_E_2_E;
            } else { //local or not set
                if (!primaryLsp.getLspPath().getNodePath().contains(path.getSourceNode()) ||
                        primaryLsp.getLspPath().getNodePath().contains(path.getDestinationNode())) {
                    throw new EventExecutionException("The given path cannot be a detour for primary lsp: " + primaryLsp.getId());
                }
                if (!isSetMethodType() && path.getSourceNode().equals(primaryLsp.getLspPath().getSourceNode()) &&
                        path.getDestinationNode().equals(primaryLsp.getLspPath().getDestinationNode())) {
                    type = LspBackupType.DETOUR_E_2_E;
                }
                type = LspBackupType.DETOUR_LOCAL;
            }

            List<Link> linkPath = primaryLsp.getLspPath().getLinkPath();
            List<Link> protectedLinks;
            if (type == LspBackupType.DETOUR_LOCAL) {
                List<Node> nodePath = primaryLsp.getLspPath().getNodePath();
                protectedLinks = new ArrayList<Link>(1);
                int i = nodePath.indexOf(path.getSourceNode());
                protectedLinks.add(linkPath.get(i));
            } else {
                protectedLinks = new ArrayList<Link>(linkPath.size());
                for (Link l : linkPath) {
                    protectedLinks.add(l);
                }
            }

            LspImpl lsp = new LspImpl(domain, primaryLsp.getId(), getLspId(), path, type, protectedLinks);

            if (isSetBw()) lsp.setBw(getBw());

            if (isSetDiffServ()) logger.warn("Diffserv parameters cannot be used for detour lsps. Those of primary are used.");
            if (isSetMaxRate()) lsp.setMaxRate(getMaxRate());
            if (isSetMetric()) lsp.setMetric(getMetric());
            if (isSetAcceptedCos()) {
                for (String s : (List<String>)getAcceptedCos().getCos()) {
                    try {
                        lsp.addAcceptedClassOfService(s);
                    } catch (ClassOfServiceNotFoundException e) {
                        throw new EventExecutionException(e);
                    } catch (ClassOfServiceAlreadyExistException e) {
                        throw new EventExecutionException(e);
                    }
                }
            }

            actionList = new TotemActionList();
            actionList.add(new AddLspAction(domain, lsp));

        } else { //path not given, use routing algo
            LSPDetourRoutingParameter params = new LSPDetourRoutingParameter(getLspId());
            if (isSetBw()) params.setBandwidth(getBw());
            if (isSetDiffServ()) {
                logger.warn("Diffsrev parameters cannot be given to the detour, values from the primary are used.");
            }
            if (isSetAcceptedCos()) {
                List<String> cosList = new ArrayList<String>(getAcceptedCos().getCos().size());
                for (String s : (List<String>)getAcceptedCos().getCos()) {
                    cosList.add(s);
                }
                params.setAcceptedCos(cosList);
            }

            if (isSetMaxRate()) logger.warn("Max rate not use for routing bypass lsps");
            if (isSetMetric()) logger.warn("Metric not use for routing bypass lsps");

            params.setProtectedLSP(getProtectedLSP());

            if (isSetMethodType()) params.setMethodType(getMethodType().getValue());
            if (isSetProtectionType()) params.setProtectionType(getProtectionType().getValue());

            if (routingAlgo == null) throw new EventExecutionException("Routing algo is null");

            try {
                actionList = routingAlgo.routeDetour(domain, params);
            } catch (RoutingException e) {
                e.printStackTrace();
                throw new EventExecutionException(e);
            } catch (NoRouteToHostException e) {
                e.printStackTrace();
                throw new EventExecutionException(e);
            } catch (LocalDatabaseException e) {
                e.printStackTrace();
                throw new EventExecutionException(e);
            }
        }

        /* execute actions */
        boolean establishLSP = isSetEstablishLSP() ? isEstablishLSP() : true;

        Lsp lsp = null;
        String msg = "";
        for (Iterator iter = actionList.iterator(); iter.hasNext();) {
            TotemAction action = (TotemAction) iter.next();
            if (action instanceof AddLspAction) {
                AddLspAction addLsp = (AddLspAction) action;
                //System.out.println("New LSP path:");
                //System.out.println(addLsp.getLsp().getLspPath());
                //build the result object
                lsp = addLsp.getLsp();
                msg += "Path for LSP " + lsp.getId() + ":  ";
                msg += addLsp.getLsp().getLspPath().toString();
                msg += "\n";
            } else if (action instanceof PreemptLspsAction) {
                PreemptLspsAction pAction = (PreemptLspsAction) action;
                if (!pAction.getLsps().isEmpty()) {
                    msg += "\n";
                    msg += "Following LSPs will be preempted:\n";
                    for (String lspId : pAction.getLsps()) {
                        msg += "\t" + lspId;
                    }
                    msg += "\n";
                }
            } else {
                //logger.warn("Another (unknown) action was found when routing the LSP.");
            }

            if (establishLSP) {
                try {
                    action.execute();
                } catch (TotemActionExecutionException e) {
                    e.printStackTrace();
                    throw new EventExecutionException(e);
                }
            }
        }

        return new EventResult(lsp, msg);
    }
}
