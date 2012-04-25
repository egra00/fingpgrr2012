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
package be.ac.ucl.ingi.totem.scenario.model;

import be.ac.ucl.ingi.cbgp.Route;
import be.ac.ucl.ingi.cbgp.bgp.Peer;
import be.ac.ucl.ingi.cbgp.net.Link;
import be.ac.ucl.ingi.totem.repository.CBGP;
import be.ac.ucl.ingi.totem.repository.model.CBGPSimulator;
import be.ac.ucl.ingi.totem.scenario.model.jaxb.impl.CBGPInfoImpl;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.model.Event;
import be.ac.ulg.montefiore.run.totem.scenario.model.EventResult;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.ParamType;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * This class implements a CBGP specific event.
 *
 * @author : Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * Contributor(s) : Bruno Quoitin (bqu@info.ucl.ac.be)
 *
 * Creation date : 10-Dec-2004
 *
 * Changes:
 * --------
 *
 */
public class CBGPInfo extends CBGPInfoImpl implements Event {
    
    private static final Logger logger = Logger.getLogger(CBGPInfo.class);
    
    // -----[ actionGetRT ]------------------------------------------
    /**
     * Dumps the routing table of the given router.
     *
     * Parameters:
     * - router [mandatory]
     * - prefix [optional]
     */
    public String actionGetRT(CBGPSimulator cbgp, HashMap<String,String> paramsIndex)
    throws Exception, RoutingException
    {
        if (!paramsIndex.containsKey("router"))
            throw new Exception("<router> parameter required");
        StringBuilder sb = new StringBuilder();
        String sRouterAddr= paramsIndex.get("router");
        
        sb.append("Routing table of "+sRouterAddr+":\n");
        Vector routes= cbgp.netNodeGetRT(sRouterAddr, null);
        if (routes != null) {
            for (Enumeration routesEnum= routes.elements();
            routesEnum.hasMoreElements();) {
                Route route= (Route) routesEnum.nextElement();
                sb.append(route);
                sb.append("\n");
            }
        } else {
            sb.append("(null)\n");
        }
        return sb.toString();
    }
    
    // -----[ actionGetRIB ]-----------------------------------------
    /**
     * Dumps the BGP Routing Information Base of the given router.
     *
     * Parameters:
     * - router [mandatory]
     * - prefix [optional]
     */
    public String actionGetRIB(CBGPSimulator cbgp, HashMap<String,String> paramsIndex)
    throws Exception, RoutingException
    {
        if (!paramsIndex.containsKey("router"))
            throw new Exception("<router> parameter required");
        StringBuilder sb = new StringBuilder();
        String sRouterAddr= paramsIndex.get("router");
        
        String sPrefix= null;
        if (paramsIndex.containsKey("prefix"))
            sPrefix= paramsIndex.get("prefix");
        
        sb.append("Routing Information Base (RIB) of "+sRouterAddr+":\n");
        Vector routes= cbgp.bgpRouterGetRib(sRouterAddr, sPrefix);
        if (routes != null) {
            for (Enumeration routesEnum= routes.elements();
            routesEnum.hasMoreElements();) {
                Route route= (Route) routesEnum.nextElement();
                sb.append(route);
                sb.append("\n");
            }
        } else {
           sb.append("(null)\n");
        }
        return sb.toString();
    }
    
    // -----[ actionGetAdjRIB ]--------------------------------------
    /**
     * Dumps the BGP Adjacent Routing Information Base(s) of the given
     * router.
     *
     * Parameters:
     * - router [mandatory]
     * - peer [optional]
     * - prefix [optional]
     * - in [optional, default=true]
     */
    public String actionGetAdjRIB(CBGPSimulator cbgp, HashMap<String,String> paramsIndex)
    throws Exception, RoutingException
    {
        if (!paramsIndex.containsKey("router"))
            throw new Exception("<router> parameter required");
        StringBuilder sb = new StringBuilder();
        String sRouterAddr= paramsIndex.get("router");
        
        String sPeerAddr= null;
        if (paramsIndex.containsKey("peer"))
            sPeerAddr= paramsIndex.get("peer");
        
        String sPrefix= null;
        if (paramsIndex.containsKey("prefix"))
            sPrefix= paramsIndex.get("prefix");
        
        boolean bIn= true;
        if (paramsIndex.containsKey("in"))
            bIn= Boolean.valueOf(paramsIndex.get("in"));
        
        sb.append("Adjacent Routing Information Bases (Adj-RIBs) of "+sRouterAddr+":\n");
        Vector routes= cbgp.bgpRouterGetAdjRib(sRouterAddr, sPeerAddr, sPrefix, bIn);
        if (routes != null) {
            for (Enumeration routesEnum= routes.elements();
            routesEnum.hasMoreElements();) {
                Route route= (Route) routesEnum.nextElement();
                sb.append(route);
                sb.append("\n");
            }
        } else {
            sb.append("(null)\n");
        }
        return sb.toString();
    }
    
    // -----[ actionGetLinks ]---------------------------------------
    /**
     * Dumps the list of links of the given router.
     *
     * Parameters:
     * - router [mandatory]
     */
    public String actionGetLinks(CBGPSimulator cbgp, HashMap<String,String> paramsIndex)
    throws Exception, RoutingException
    {
        if (!paramsIndex.containsKey("router"))
            throw new Exception("<router> parameter required");
        StringBuilder sb = new StringBuilder();
        String sRouterAddr= paramsIndex.get("router");
        
        sb.append("Links of "+sRouterAddr+":\n");
        Vector links= cbgp.netNodeGetLinks(sRouterAddr);
        if (links != null) {
            for (Enumeration linksEnum= links.elements();
            linksEnum.hasMoreElements();) {
                Link link= (Link) linksEnum.nextElement();
                sb.append(link);
                sb.append("\n");
            }
        } else {
            sb.append("(null)\n");
        }
        return sb.toString();
    }
    
    // -----[ actionGetPeers ]---------------------------------------
    /**
     * Dumps the neighbors of the given router.
     *
     * Parameters:
     * - router [mandatory]
     */
    public String actionGetPeers(CBGPSimulator cbgp, HashMap<String,String> paramsIndex)
    throws Exception, RoutingException
    {
        if (!paramsIndex.containsKey("router"))
            throw new Exception("<router> parameter required");
        StringBuilder sb = new StringBuilder();
        String sRouterAddr= paramsIndex.get("router");
        
        sb.append("Peers of "+sRouterAddr+":\n");
        Vector peers= cbgp.bgpRouterGetPeers(sRouterAddr);
        if (peers != null) {
            for (Enumeration peersEnum= peers.elements();
            peersEnum.hasMoreElements();) {
                Peer peer= (Peer) peersEnum.nextElement();
                sb.append(peer);
                sb.append("\n");
            }
        } else {
            sb.append("(null)\n");
        }
        return sb.toString();
    }
    
    // -----[ actionRecordRoute ]------------------------------------
    /**
     * Dumps the traced-route from a source node to a destination
     * node.
     *
     * Parameters:
     * - src [mandatory]
     * - dst [mandatory]
     */
    public String actionRecordRoute(CBGPSimulator cbgp, HashMap<String,String> paramsIndex)
    throws Exception, RoutingException
    {
        if (!paramsIndex.containsKey("src"))
            throw new Exception("<src> parameter required");
        StringBuilder sb = new StringBuilder();
        String sSrcAddr= paramsIndex.get("src");
        
        if (!paramsIndex.containsKey("dst"))
            throw new Exception("<dst> parameter required");
        String sDstAddr= paramsIndex.get("dst");
        
        sb.append("Record route from "+sSrcAddr+" to "+sDstAddr+" :\n");
        sb.append(cbgp.netNodeRecordRoute(sSrcAddr, sDstAddr));
        return sb.toString();
    }
    
    // -----[ action ]-----------------------------------------------
    /**
     *
     */
    public EventResult action() throws EventExecutionException {
        
        List<ParamType> params = this.getParam();
        HashMap<String,String> paramsIndex= new HashMap<String,String>();
        
        // Index all params...
        for (Iterator<ParamType> iterParams= params.iterator();
        iterParams.hasNext(); ) {
            ParamType param= iterParams.next();
            paramsIndex.put(param.getName(), param.getValue());
        }
        
        // CBGP simulator available ?
        CBGPSimulator cbgp;
        try {	
            cbgp = (CBGP) RepositoryManager.getInstance().getAlgo("CBGP");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Please start the algorithm before using it!");
            throw new EventExecutionException(e);
        }
        
        // Which info is requested ?
        String sInfo= this.getInfo();

        String infoTxt;
        try {
            if (sInfo.equals("RT")) {
                
                infoTxt = actionGetRT(cbgp, paramsIndex);
                
            } else if (sInfo.equals("RIB")) {
                
                infoTxt = actionGetRIB(cbgp, paramsIndex);
                
            } else if (sInfo.equals("AdjRIB")) {
                
                infoTxt = actionGetAdjRIB(cbgp, paramsIndex);
                
            } else if (sInfo.equals("Links")) {
                
                infoTxt = actionGetLinks(cbgp, paramsIndex);
                
            } else if (sInfo.equals("Peers")) {
                
                infoTxt = actionGetPeers(cbgp, paramsIndex);
                
            } else if (sInfo.equals("RecordRoute")) {
                
                infoTxt = actionRecordRoute(cbgp, paramsIndex);
                
            } else {
                
                System.out.println("CBGPInfo info="+sInfo+" is not supported. Aborted.");
                throw new EventExecutionException("CBGPInfo info="+sInfo+" is not supported. Aborted.");

            }
            return new EventResult(null, infoTxt);
        } catch (Exception e) {
            System.out.println("CBGPInfo command failed");
            System.out.println("reason: "+e.getMessage());
            throw new EventExecutionException(e);
        }
        
    }
    
}
