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

import java.util.*;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.*;
import be.ac.ulg.montefiore.run.totem.repository.model.*;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ObjectFactory;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.RoutingAlgo;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ParamType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.LSPPathType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.LSPCreationImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.LspImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.PathImpl;
import be.ac.ulg.montefiore.run.totem.domain.exception.*;

/*
* Changes:
* --------
* 9-May-05: log the time needed to compute a LSP (FSK).
* 31-Mar-2006: Adapt to the new XML schema: added the possibility to specify an explicit route, and to calculate the path without adding the LSP. (GMO)
* 23-Oct-2006: Add bandwidth availability check for explicit route LSPs. (GMO)
*
*/

/**
 * TODO: implement loose routes.
 * This class implements a LSP creation event.
 * <p/>
 * <p>Creation date: 01-déc.-2004
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class LSPCreation extends LSPCreationImpl implements Event {

    private static final Logger logger = Logger.getLogger(LSPCreation.class);

    public LSPCreation() {
    }

    /**
     * Creates a new <code>LSPCreation</code> element. Note that <code>algoParams</code> can be null.
     */
    public LSPCreation(String srcNode, String dstNode, String algoName, HashMap algoParams) {
        setSrc(srcNode);
        setDst(dstNode);

        ObjectFactory factory = new ObjectFactory();
        try {
            RoutingAlgo routingAlgo = factory.createRoutingAlgo();
            routingAlgo.setName(algoName);
            setRoutingAlgo(routingAlgo);

            if (algoParams == null) {
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
        } catch (JAXBException e) {
            logger.error("JAXBException in a constructor of LSPCreation. Reason: " + e.getMessage());
        }
    }

    /**
     * Creates a new <code>LSPCreation</code> element. Note that <code>algoParams</code> can be null.
     */
    public LSPCreation(String srcNode, String dstNode, String lspId, String algoName, HashMap algoParams) {
        this(srcNode, dstNode, algoName, algoParams);
        setLspId(lspId);
    }

    /**
     * Creates a new <code>LSPCreation</code> element. Note that <code>algoParams</code> can be null.
     */
    public LSPCreation(String srcNode, String dstNode, String lspId, float bw, String algoName, HashMap algoParams) {
        this(srcNode, dstNode, lspId, algoName, algoParams);
        setBw(bw);
    }

    /**
     * Creates a new <code>LSPCreation</code> element. Note that <code>algoParams</code> can be null.
     */
    public LSPCreation(String srcNode, String dstNode, String lspId, float bw, float metric, float maxRate, int classType, int setup, int holding, String algoName, HashMap algoParams) {
        this(srcNode, dstNode, lspId, bw, algoName, algoParams);
        setMetric(metric);
        setMaxRate(maxRate);

        ObjectFactory factory = new ObjectFactory();
        try {
            DiffServType diffServ = factory.createBaseLSPCreationTypeDiffServType();
            setDiffServ(diffServ);
            diffServ.setCt(classType);
            DiffServType.PreemptionType preemption = factory.createBaseLSPCreationTypeDiffServTypePreemptionType();
            diffServ.setPreemption(preemption);
            preemption.setHolding(holding);
            preemption.setSetup(setup);
        } catch (JAXBException e) {
            logger.error("A JAXB exception occurred. Reason: " + e.getMessage());
        }
    }

    /**
     * This action will create an LSP on the domain specifies by the ASID or if not define on the default domain
     *
     * @see Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a LSP creation event - Source: " + _Src + " - Destination: " + _Dst);

        // Check the domain
        Domain domain = null;
        try {
            if (this.isSetASID()) {
                domain = InterDomainManager.getInstance().getDomain(_ASID);
            } else {
                domain = InterDomainManager.getInstance().getDefaultDomain();
            }
        } catch (InvalidDomainException e) {
            logger.error("Unknown domain with ASID " + _ASID);
            throw new EventExecutionException(e);
        }

        try {
            domain.getNode(_Src);
        } catch (NodeNotFoundException e) {
            logger.error("Node " + _Src + " not found");
            throw new EventExecutionException(e);
        }

        try {
            domain.getNode(_Dst);
        } catch (NodeNotFoundException e) {
            logger.error("Node " + _Dst + " not found");
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
        LSPPrimaryRouting routingAlgo = null;
        String algoName = null;
        if (isSetRoutingAlgo()) {
            algoName = _RoutingAlgo.getName();
            try {
                if (this.isSetASID()) {
                    routingAlgo = (LSPPrimaryRouting) RepositoryManager.getInstance().getAlgo(algoName, this.getASID());
                } else {
                    routingAlgo = (LSPPrimaryRouting) RepositoryManager.getInstance().getAlgo(algoName);
                }
            } catch (NoSuchAlgorithmException e) {
                logger.error("Algorithm not started or unknown routing algorithm " + algoName);
                throw new EventExecutionException(e);
            } catch (ClassCastException e) {
                logger.error("The specified algorithm isn't a LSP primary routing algorithm!");
                throw new EventExecutionException(e);
            }
        } else if (isSetPath() && !getPath().getType().toString().equals(LSPPathType._STRICT)) {
            logger.error("A routing algo must be specified if the complete path (STRICT) is not provided.");
            throw new EventExecutionException("A routing algo must be specified if the complete path (STRICT) is not provided.");
        }


        TotemActionList actionList;

        if (isSetPath()) {
            if (getPath().getType().toString().equals(LSPPathType._LOOSE)) {
                logger.error("Loose path is not yet implemented.");
                throw new EventExecutionException("Loose path is not yet implemented.");
            }
            //Compute the path
            if (isSetRoutingAlgo()) {
                logger.warn("Routing algo will be ignored since you provided a strict path");
            }

            Path path = new PathImpl(domain);

            int priorityLevel = isSetDiffServ() ? domain.getPriority(_DiffServ.getPreemption().getSetup(), _DiffServ.getCt()) : domain.getMinPriority();
            try {
                if (getPath().isSetLink()) {
                    List<Link> lList;
                    try {
                        lList = new ArrayList<Link>(getPath().getLink().size());
                        for (String s : (List<String>) getPath().getLink()) {
                            Link l = domain.getLink(s);
                            // check for bandwidth availability
                            if (isSetBw() && l.getReservableBandwidth(priorityLevel) < getBw()) {
                                throw new EventExecutionException("Not enough reservable bandwidth.");
                            }
                            lList.add(l);
                        }
                    } catch (LinkNotFoundException e) {
                        logger.error("Incorrect Link path: Link not found.");
                        throw new EventExecutionException(e);
                    }
                    if (!lList.get(0).getSrcNode().getId().equals(_Src)
                     || !lList.get(lList.size()-1).getDstNode().getId().equals(_Dst)) {
                        logger.error("Incorrect Link path: source or destination does not match path extremity.");
                        throw new EventExecutionException("Incorrect Link path: source or destination does not match path extremity.");
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
                    if (!nList.get(0).getId().equals(_Src)
                     || !nList.get(nList.size()-1).getId().equals(_Dst)) {
                        logger.error("Incorrect Node path: source or destination does not match path extremity.");
                        throw new EventExecutionException("Incorrect Node path: source or destination does not match path extremity.");
                    }
                    path.createPathFromNode(nList);

                    // check for bandwidth availability
                    if (isSetBw()) {
                        for (Link l : path.getLinkPath()) {
                            if (l.getReservableBandwidth(priorityLevel) < getBw()) {
                                throw new EventExecutionException("Not enough reservable bandwidth.");
                            }
                        }
                    }
                }
            } catch (NodeNotFoundException e) {
                logger.error("Error in the path provided.");
                throw new EventExecutionException(e);
            } catch (InvalidPathException e) {
                logger.error("Error in the path provided: path not continuous.");
                throw new EventExecutionException(e);
            }

            Lsp lsp;
            if (isSetDiffServ())
                try {
                    lsp = new LspImpl(domain, _LspId, isSetBw() ? _Bw : 0, path, _DiffServ.getCt(), _DiffServ.getPreemption().getHolding(), _DiffServ.getPreemption().getSetup());
                } catch (DiffServConfigurationException e) {
                    logger.error("Error in Diffserv configuration.");
                    throw new EventExecutionException(e);
                }
            else {
                lsp = new LspImpl(domain, _LspId, isSetBw() ? _Bw : 0, path);
            }

            actionList = new TotemActionList();
            actionList.add(new AddLspAction(domain, lsp));

        } else {

            // Fetch the parameters
            LSPPrimaryRoutingParameter params;
            params = new LSPPrimaryRoutingParameter(_Src, _Dst, _LspId);
            params.setBandwidth((this.isSetBw()) ? _Bw : 0);
            if (this.isSetMetric()) params.setMetric(_Metric);
            if (this.isSetMaxRate()) params.setMaxRate(_MaxRate);
            if (_DiffServ != null) {
                params.setClassType(_DiffServ.getCt());
                params.setSetup(_DiffServ.getPreemption().getSetup());
                params.setHolding(_DiffServ.getPreemption().getHolding());
            } else {
                params.setClassType(domain.getClassType(domain.getMinPriority()));
                params.setSetup(domain.getPreemptionLevel(domain.getMinPriority()));
                params.setHolding(domain.getPreemptionLevel(domain.getMinPriority()));
            }

            List algoParams = _RoutingAlgo.getParam();
            for (Iterator iter = algoParams.iterator(); iter.hasNext();) {
                ParamType param = (ParamType) iter.next();
                params.putRoutingAlgorithmParameter(param.getName(), param.getValue());
            }
            long time = 0;
            // Find a route for the LSP and execute the returned actions
            try {
                time = System.currentTimeMillis();
                actionList = routingAlgo.routeLSP(domain, params);
                time = System.currentTimeMillis() - time;
            } catch (RoutingException e) {
                logger.error("An error occurred during the routing of the LSP. Reason: " + e.getMessage());
                throw new EventExecutionException(e);
            } catch (NoRouteToHostException e) {
                logger.error("The routing algorithm was unable to find a route between the source and the destination that met the requirements.");
                throw new EventExecutionException(e);
            } catch (LocalDatabaseException e) {
                logger.error("The algorithm specific database is not up-to-date : " + e.getMessage());
                throw new EventExecutionException(e);
            }
            logger.info("LSP creation with algo " + algoName + " takes " + time + " ms");
        }


        /* execute actions */
        boolean establishLSP = true;
        if(isSetEstablishLSP()) {
        	establishLSP = isEstablishLSP();
        }

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

            if (establishLSP)
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
