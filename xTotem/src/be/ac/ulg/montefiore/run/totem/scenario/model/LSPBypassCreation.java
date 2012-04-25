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

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.LSPBypassCreationImpl;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.RoutingAlgo;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ParamType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.LSPPathType;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.PathImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.LspImpl;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.*;
import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.TotemActionExecutionException;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.util.*;

/*
* Changes:
* --------
* - 28-Feb-2008: separate LSPBackupCreation in LSPBypassCreation and LSPDetourCreation (GMO)
*/

/**
 *  * This class implements a LSP bypass creation event.
 *
 * <p>Creation date: 28/02/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class LSPBypassCreation extends LSPBypassCreationImpl implements Event {

    private final static Logger logger = Logger.getLogger(LSPBypassCreation.class);

    public LSPBypassCreation() {}

    /**
     * Creates a new <code>LSPBypassCreation</code> element. Note
     * that <code>algoParams</code> can be <code>null</code> and the list must contain the IDs of the
     * protected links.
     */
    public LSPBypassCreation(List protectedLinks, String algoName, HashMap algoParams) {
        this(algoName, algoParams);
        ObjectFactory factory = new ObjectFactory();
        try {
            for (Iterator iter = protectedLinks.iterator(); iter.hasNext();) {
                String pLink = (String) iter.next();
                ProtectedLinkType protectedLink = factory.createLSPBypassCreationTypeProtectedLinkType();
                protectedLink.setLinkId(pLink);
                getProtectedLink().add(protectedLink);
            }
        }
        catch(JAXBException e) {
            logger.error("JAXBException in a constructor of LSPBackupCreation. Reason: "+e.getMessage());
        }
    }

    public LSPBypassCreation(List protectedLinks, String algoName, HashMap algoParams, int asId) {
        this(protectedLinks, algoName, algoParams);
        setASID(asId);
    }

    /**
     * Creates a new <code>LSPBypassCreation</code> element. Note
     * that <code>algoParams</code> can be <code>null</code> and the list must contain the IDs of the
     * protected links.
     */
    public LSPBypassCreation(String lspId, float bw, List protectedLinks, String algoName, HashMap algoParams) {
        this(protectedLinks, algoName, algoParams);
        setLspId(lspId);
        setBw(bw);
    }

    public LSPBypassCreation(String lspId, float bw, List protectedLinks, String algoName, HashMap algoParams, int asId) {
        this(lspId, bw, protectedLinks, algoName, algoParams);
        setASID(asId);
    }

    /**
     * Private base constructor
     * @param algoName
     * @param algoParams
     */
    private LSPBypassCreation(String algoName, HashMap algoParams) {
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
        LSPBypassRouting routingAlgo = null;
        String algoName = null;
        if (isSetRoutingAlgo()) {
            algoName = _RoutingAlgo.getName();
            try {
                if (this.isSetASID()) {
                    routingAlgo = (LSPBypassRouting) RepositoryManager.getInstance().getAlgo(algoName, this.getASID());
                } else {
                    routingAlgo = (LSPBypassRouting) RepositoryManager.getInstance().getAlgo(algoName);
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

        // gets the list of protected links
        List<Link> protectedLinks = new ArrayList<Link>(getProtectedLink().size());
        for (Iterator iter = getProtectedLink().iterator(); iter.hasNext();) {
            ProtectedLinkType link = (ProtectedLinkType) iter.next();
            try {
                Link dLink = domain.getLink(link.getLinkId());
                protectedLinks.add(dLink);
            }
            catch(LinkNotFoundException e) {
                logger.error("Unknown link "+link.getLinkId());
                throw new EventExecutionException(e);
            }
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

            if (isSetDst() && !getDst().equals(path.getDestinationNode().getId())) {
                throw new EventExecutionException("The destination node do not match the end of path.");
            }

            LspImpl lsp;
            if (isSetDiffServ()) {
                try {
                    lsp = new LspImpl(domain, getLspId(), isSetBw() ? getBw() : 0, path, protectedLinks, getDiffServ().getCt(), getDiffServ().getPreemption().getHolding(), getDiffServ().getPreemption().getSetup());
                } catch (DiffServConfigurationException e) {
                    logger.error("Error in Diffserv configuration.");
                    throw new EventExecutionException(e);
                }
            } else {
                lsp = new LspImpl(domain, getLspId(), isSetBw() ? getBw() : 0, path, protectedLinks);
            }

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

        } else { // path is not given, use routing algo
            LSPBypassRoutingParameter params = new LSPBypassRoutingParameter(getLspId());
            if (isSetBw()) params.setBandwidth(getBw());
            if (isSetDiffServ()) {
                params.setClassType(getDiffServ().getCt());
                params.setSetup(getDiffServ().getPreemption().getSetup());
                params.setHolding(getDiffServ().getPreemption().getHolding());
            }
            if (isSetAcceptedCos()) {
                List<String> cosList = new ArrayList<String>(getAcceptedCos().getCos().size());
                for (String s : (List<String>)getAcceptedCos().getCos()) {
                    cosList.add(s);
                }
                params.setAcceptedCos(cosList);
            }
            if (isSetDst()) params.setDstNode(getDst());
            if (isSetMaxRate()) logger.warn("Max rate not use for routing bypass lsps");
            if (isSetMetric()) logger.warn("Metric not use for routing bypass lsps");

            if (routingAlgo == null) throw new EventExecutionException("Routing algo is null");

            try {
                actionList = routingAlgo.routeBypass(domain, params);
            } catch (RoutingException e) {
                e.printStackTrace();
                throw new EventExecutionException(e);
            } catch (NoRouteToHostException e) {
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
