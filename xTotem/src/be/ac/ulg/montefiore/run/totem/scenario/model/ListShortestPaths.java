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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.SPF;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.ListShortestPathsImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;

/*
 * Changes:
 * --------
 *
 * - 25-Feb-2006: add nodeList parameter (JLE).
 * - 11-Jun-2007: ecmp is true by default, do not print shortests path on standard output (GMO)
 */

/**
 * This class implements an event which lists the shortest paths of a domain
 * following several metrics.
 *
 * <p>Creation date: 19-sept.-2005
 *
 * 16 jan 2007 : print also the cost of the path
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class ListShortestPaths extends ListShortestPathsImpl implements Event {

    private static final Logger logger = Logger.getLogger(ListShortestPaths.class);
    
    public ListShortestPaths() {}
    
    public ListShortestPaths(int asId) {
        setASID(asId);
    }
    
    public ListShortestPaths(String src) {
        setSrc(src);
    }
    
    public ListShortestPaths(String src, String dst) {
        this(src);
        setDst(dst);
    }
    
    public ListShortestPaths(int asId, String src, String dst) {
        this(src, dst);
        setASID(asId);
    }
    
    public ListShortestPaths(int asId, String src, String dst, boolean ECMP) {
        this(asId, src, dst);
        setECMP(ECMP);
    }
    
    public ListShortestPaths(int asId, String src, String dst, boolean ECMP, SPF SPFType) {
        this(asId, src, dst, ECMP);
        setSPFtype(SPFType.getClass().getSimpleName());
    }

    /**
     * @see be.ac.ulg.montefiore.run.totem.scenario.model.Event#action()
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a listShortestPaths event.");
        
        Domain domain;
        if(isSetASID()) {
            try {
                domain = InterDomainManager.getInstance().getDomain(getASID());
            } catch (InvalidDomainException e) {
                logger.error("Unknown domain "+getASID());
                throw new EventExecutionException(e);
            }
        } else {
            domain = InterDomainManager.getInstance().getDefaultDomain();
            if(domain == null) {
                logger.error("There is no default domain!");
                throw new EventExecutionException("No default domain.");
            }
        }
        
        SPF algo;
        if(!isSetSPFtype()) {
            try {
                algo = (SPF) RepositoryManager.getInstance().getAlgo("CSPF", domain.getASID());
            } catch (NoSuchAlgorithmException e) {
                logger.error("It seems the CSPF algorithm is not started on domain "+domain.getASID());
                throw new EventExecutionException(e);
            }
        } else {
            try {
                algo = (SPF) RepositoryManager.getInstance().getAlgo(getSPFtype(), domain.getASID());
            } catch (NoSuchAlgorithmException e) {
                logger.error("Error while retrieving the algorithm "+getSPFtype()+". Message: "+e.getMessage());
                if(logger.isDebugEnabled()) {
                    e.printStackTrace();
                }
                throw new EventExecutionException(e);
            } catch (ClassCastException e) {
                logger.error("The specified algorithm doesn't implement the SPF interface.");
                throw new EventExecutionException(e);
            }
        }
        
        boolean ECMP = isSetECMP() ? isECMP() : true;
        
        List<Path> paths;
        try {
            if(algo.getClass().getName().equals("be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF")) {
                if(isSetSrc() && isSetDst()) {
                    paths = domain.getSPFCache().getPath(domain.getNode(getSrc()), domain.getNode(getDst()), ECMP);
                } else if(isSetSrc()) {
                    paths = new ArrayList<Path>();
                    List<Node> nodeList = domain.getUpNodes();
                    Node src = domain.getNode(getSrc());
                    for(Node dst : nodeList) {
                        if(src == dst) {
                            continue;
                        }
                        paths.addAll(domain.getSPFCache().getPath(src, dst, ECMP));
                    }
                } else if(isSetDst()) {
                    List<Node> nodeList = domain.getUpNodes();
                    paths = new ArrayList<Path>();
                    Node dst = domain.getNode(getDst());
                    for(Node src : nodeList) {
                        if(src == dst) {
                            continue;
                        }
                        paths.addAll(domain.getSPFCache().getPath(src, dst, ECMP));
                    }
                } else {
                    List<Node> nodeList = domain.getUpNodes();
                    paths = new ArrayList<Path>();
                    for(Node src : nodeList) {
                        for(Node dst : nodeList) {
                            if(src == dst) {
                                continue;
                            }
                            paths.addAll(domain.getSPFCache().getPath(src, dst, ECMP));
                        }
                    }
                }
            } else {
                if(isSetSrc() && isSetDst()) {
                    paths = algo.computeSPF(domain, getSrc(), getDst(), ECMP);
                } else if(isSetSrc()) {
                    paths = algo.computeSPF(domain, getSrc(), ECMP);
                } else if(isSetDst()) {
                    paths = algo.computeSPF(domain, false, getDst(), ECMP);
                } else {
                    paths = algo.computeFullMeshSPF(domain, ECMP);
                }
            }
        } catch (RoutingException e) {
            logger.error("RoutingException during the paths calculation. Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (NoRouteToHostException e) {
            logger.error("NoRouteToHostException during the paths calculation. Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        } catch (NodeNotFoundException e) {
            logger.error("NodeNotFoundException during the paths calculation. Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new EventExecutionException(e);
        }

        StringBuilder message = new StringBuilder();
        if(!isSetNodeList() || isNodeList()) {
            for(Path path : paths) {
                message.append(path.toString());
                float pathcost = 0;
                List<Link> links = path.getLinkPath();
                for(Link link : links) {
                    if (this._SPFtype.compareTo("CSPF") == 0) {
                        pathcost += link.getMetric();
                    } else if (this._SPFtype.compareTo("CSPFTEMetric") == 0) {
                        pathcost += link.getTEMetric();
                    }
                }
                message.append(" (cost = " + pathcost + ")\n");
            }
        } else {
            for(Path path : paths) {
                List<Link> links = path.getLinkPath();
                message.append("[ ");
                float pathcost = 0;
                for(Link link : links) {
                    message.append(link.getId());
                    message.append(" ");
                    pathcost += link.getMetric();
                }
                message.append("] (cost = " + pathcost + ")\n");
            }
        }
        //System.out.println(message.toString());
        return new EventResult(paths, message.toString());
    }

}
