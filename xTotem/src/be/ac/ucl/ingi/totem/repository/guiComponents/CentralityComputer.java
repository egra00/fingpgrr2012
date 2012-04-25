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

package be.ac.ucl.ingi.totem.repository.guiComponents;


import be.ac.ucl.ingi.cbgp.IPAddress;
import be.ac.ucl.ingi.cbgp.IPTrace;
import be.ac.ucl.ingi.cbgp.bgp.Route;
import be.ac.ucl.ingi.totem.repository.CBGP;
import be.ac.ucl.ingi.totem.repository.model.CBGPSimulator;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ProgressBarPanel;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * compute the centality whithin a domain
 * @author Thomas Vanstals
 *
 */
public class CentralityComputer
{
    private static MainWindow mainWindow;
    private static CBGPSimulator cbgp = null;
    
    static final String INTRA_NORMALIZATION_FACTOR = "intraNormalisationFactor";
    static final String EXTRA_NORMALIZATION_FACTOR = "extraNormalisationFactor";
    static final String NODE = "node";
    static final String LINK = "link";    
    
    public CentralityComputer()
    {
        mainWindow = MainWindow.getInstance();
        new Thread(new Runnable() {
            public void run() {
                Hashtable resultHt = compute();
                show(resultHt, "Node and link centrality");
            }
        }).start();
    }

    /**
     * show the chart
     * @param ht une table de hash contenant les données a afficher
     * @param title le titre de la fenetre
     */
    private void show(Hashtable ht, String title){        
        CentralityGraph graph = new CentralityGraph(ht, title);
        graph.pack();
        RefineryUtilities.centerFrameOnScreen(graph);
        graph.setVisible(true);
    }
    
    /**
     * 
     * @return une table de hash contenant les données a afficher
     */
    private Hashtable compute(){
        
        int intraNormalisationFactor = 0;
        int extraNormalisationFactor = 0;
        
        // CBGPSimulator available ? 
        try {
            cbgp = (CBGP) RepositoryManager.getInstance().getAlgo("CBGP");
        } catch (NoSuchAlgorithmException e) {
            mainWindow.errorMessage("Please start the CBGP algorithm before using it!");
            return null;
        }
        
        // results
        Hashtable nodeCentralityHt, linkCentralityHt, rid2idHt, prefixHt;
        prefixHt = new Hashtable();
        InterDomainManager idm = InterDomainManager.getInstance();
        Domain domain = idm.getDefaultDomain();
        // a hashtable will contain all the result
        nodeCentralityHt = new Hashtable();
        rid2idHt = new Hashtable(); // containt a mapping nodeIp -> nodeName 
        java.util.List<BgpRouter> bgpRoutersList= domain.getAllBgpRouters();
        // initializing nodeCentralityHt & mappingHt
        for (int i= 0; i < bgpRoutersList.size(); i++) {
            BgpRouter node = bgpRoutersList.get(i);
            rid2idHt.put(node.getRid(),node.getId()); // filling rid2idHt
            int[] centrality = new int [2];
            // centrality[0] = node intra-centrality
            // centrality[1] = node extra-centrality
            nodeCentralityHt.put(node.getId(),centrality);
        }
        // initializing linkCentralityHt
        linkCentralityHt = new Hashtable();
        java.util.List<Link> linksList= domain.getAllLinks();
        // initializing linkCentralityHt
        for (int i= 0; i < linksList.size(); i++) {
            Link link = linksList.get(i);
            int[] centrality = new int [2];
            // centrality[0] = link intra-centrality
            // centrality[1] = link extra-centrality
            try{
                linkCentralityHt.put(link.getSrcNode().getId()+" - "+link.getDstNode().getId(),centrality);
            } catch (Exception e){
                continue;
            }
        }
        
        // filling nodeCentralityHt & linkCentralityHt
        bgpRoutersList = domain.getAllBgpRouters();
        // create a progress bar
        ProgressBarPanel progressBar = new ProgressBarPanel(0, bgpRoutersList.size(), 400);
        JDialog dialog = mainWindow.showDialog(progressBar, "Computing centrality : progress");
        progressBar.setMessage("Computing centrality");
        int progress = 0;
        progressBar.setMessage("Centrality computed for "+progress+" node(s) ("+(bgpRoutersList.size()-progress)+" node(s) remaining)");
        for (int i= 0; i < bgpRoutersList.size(); i++) {
            BgpRouter node = bgpRoutersList.get(i);
            // for all BGPRouter, find reachable prefixes
            Vector routes = null;
            try {
                routes = cbgp.bgpRouterGetRib(node.getRid(), null); 
            } catch (RoutingException e) {
                return null;
            }
            Route r = null;
            Enumeration<Route> routeEnum = null;
            for (routeEnum = routes.elements(); routeEnum.hasMoreElements();) {
                // node & link extra-centality 
                r = routeEnum.nextElement();
                prefixHt.put(r.getPrefix().toString(),""); // keeping name of every ipp
                IPTrace ipt = null;
                try {
                    ipt = cbgp.netNodeRecordRoute(node.getRid(),r.getNexthop().toString()); 
                } catch (RoutingException e) {
                    return null;
                }
                int j = 0;
                IPAddress hopIp;
                java.util.List nodeList= new Vector();
                while (true){
                    try{
                        hopIp = ipt.getHop(j);
                        nodeList.add(j, hopIp);
                        j++;
                    } catch (Exception e){
                        // end of IPTrace
                        break;
                    }                    
                }
                // nodeList containt the list of all ip
                // computing node extra-centrality
                // extrem node are excluded
                for (int k=1; k<nodeList.size()-1;k++){
                    extraNormalisationFactor++;
                    ((int [])nodeCentralityHt.get(rid2idHt.get(nodeList.get(k).toString())))[1]++; // (node extra-centrality)++
                    ((int [])linkCentralityHt.get(rid2idHt.get(nodeList.get(k).toString())+" - "+rid2idHt.get(nodeList.get(k+1).toString())))[1]++; 
                    // (link extra-centrality)++
                }
            }
            // node intra-centality
            for (int m = 0; m < bgpRoutersList.size(); m++) {
                // compute the intra path toward every other intra node
                BgpRouter dest = bgpRoutersList.get(m);
                if(node.getRid().equals(dest.getRid()))
                    break;
                // else node are differents
                IPTrace ipt = null;
                try {
                    ipt = cbgp.netNodeRecordRoute(node.getRid(),dest.getRid()); 
                } catch (RoutingException e) {
                    return null;
                }
                int j = 0;
                IPAddress hopIp;
                java.util.List nodeList= new Vector();
                while (true){
                    try{
                        hopIp = ipt.getHop(j);
                        nodeList.add(j, hopIp);
                        j++;
                    } catch (Exception e){
                        // end of IPTrace
                        break;
                    }                    
                }
                // nodeList containt the list of all ip
                // computing node intra-centrality
                // extrem node are excluded
                int max = nodeList.size()-1;
                for (int k=1; k<max;k++){
                    intraNormalisationFactor++;
                    ((int [])nodeCentralityHt.get(rid2idHt.get(nodeList.get(k).toString())))[0]++; // (node intra-centrality)++
                    ((int [])linkCentralityHt.get(rid2idHt.get(nodeList.get(k).toString())+" - "+rid2idHt.get(nodeList.get(k+1).toString())))[0]++;
                }
            }
            
            progressBar.setValue(++progress); // update the bar progress
            progressBar.setMessage("Centrality computed for "+progress+" node(s) ("+(bgpRoutersList.size()-progress)+" node(s) remaining)");
        }
        Hashtable resultHt = new Hashtable();
        resultHt.put(NODE,nodeCentralityHt);
        resultHt.put(LINK,linkCentralityHt);
        resultHt.put("nPrefix",(Integer) prefixHt.size());
        resultHt.put(INTRA_NORMALIZATION_FACTOR,(Integer) intraNormalisationFactor);
        resultHt.put(EXTRA_NORMALIZATION_FACTOR,(Integer) extraNormalisationFactor);
        dialog.dispose();
        return resultHt;
    }
}