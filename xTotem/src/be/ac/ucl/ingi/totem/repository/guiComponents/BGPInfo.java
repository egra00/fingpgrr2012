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

import be.ac.ucl.ingi.cbgp.CBGPException;
import be.ac.ucl.ingi.cbgp.bgp.Peer;
import be.ac.ucl.ingi.cbgp.bgp.Route;
import be.ac.ucl.ingi.totem.repository.CBGP;
import be.ac.ucl.ingi.totem.repository.model.CBGPSimulator;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;



/**
 * gére l'affichage des données BGP d'un noeud
 * @author Vanstals Thomas
 */

public class BGPInfo{
    
    private static MainWindow mainWindow = null;
    private static Node node;
    protected JTextArea contentText = null;
    
    public BGPInfo(Node n) {
        node = n;
        mainWindow = MainWindow.getInstance();
        displayBGPInfo();
    }
    
    private void displayBGPInfo() {
        String title = "BGP Info of "+node.getId();
        BGPInfoTab it = new BGPInfoTab(title, node);
        it.setLocationRelativeTo(it.getParent());
        it.setVisible(true);
    }
    
    /**
     * affiche une JFrame contentant toutes les informations
     * @author Thomas Vanstals
     *
     */
    public class BGPInfoTab extends JFrame {
        JTabbedPane jtp;
        public BGPInfoTab(String title, Node node) {
            super(title);
            setSize(500, 250);
            Container contents = getContentPane();
            jtp = new JTabbedPane();
            // les différents onglets de la fenetre
            jtp.addTab("Config", new configContent(node));
            jtp.addTab("Peers", peerContent(node));
            jtp.addTab("RIB", ribContent(node,"rib"));
            jtp.addTab("Adj-RIBs-In", ribContent(node, "in"));
            
            contents.add(jtp);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setVisible(true);
        }
        
    }
    
    /**
     * @param node le noeud pour lequel on veut les peers
     * @return un JScrollPane contenant les infos sur les peers
     */
    private JScrollPane peerContent(Node node) {
        // CBGP simulator available ?
        CBGPSimulator cbgp;
        try {   
            cbgp = (CBGP) RepositoryManager.getInstance().getAlgo("CBGP");
        } catch (NoSuchAlgorithmException e) {
            mainWindow.errorMessage("Please start the CBGP algorithm before using it!");
            return new JScrollPane();
        }
        Vector peers;
        try {
            peers = cbgp.bgpRouterGetPeers(node.getRid());
        } catch (RoutingException e) {
            peers = null;
            return new JScrollPane();
        }
        
        
        // titre des colonnes
        String titles[] = new String[] {"IP adress", "AS number", "virtual", "session state"};
        // contenu des colonnes
        Object[][] content = new Object[peers.size()][titles.length];
        int i = 0;
        for (Enumeration routesEnum = peers.elements(); routesEnum.hasMoreElements();) {
            Peer peer = (Peer) routesEnum.nextElement();
            content[i][0] = peer.getAddress();
            content[i][1] = peer.getAS();
            byte state = 0;
            content[i][2] = "error";
            try {
                if(peer.isVirtual())
                    content[i][2] = "yes";
                else
                    content[i][2] = "no";
                state = peer.getSessionState();
            } catch (CBGPException e) {
                e.printStackTrace();
            }
            content[i][3] = Peer.sessionStateToString(state);
            i++;
        }
        JTable jt = new JTable(content, titles);
        jt.setEnabled(false); // pas moyen d'éditer les cellules
        return new JScrollPane(jt);
    }
    
    /**
     * @param node  le noeud dulequel on veut la RIB/Adj-RIBs-In
     * @param type "rib" si on veut la RIB et "in" si on veut l'Adj-RIBs-In
     * @return un JScrollPane contenant les info de la RIB ou de l'Adj-RIBs-In
     */
    private JScrollPane ribContent(Node node, String type) {
        
        String titles[] = new String[] {"Prefix", "Next hop", "Local pref", "MED", "AS Path", "Origin"};
        CBGPSimulator cbgp;
        Vector routes;
        try {   
            cbgp = (CBGP) RepositoryManager.getInstance().getAlgo("CBGP");
            if(type.equals("in")) // si Adj-RIBs-In demandée
                routes = cbgp.bgpRouterGetAdjRib(node.getRid(), null, null, true);
            else // sinon il s'agit de la RIB
                routes = cbgp.bgpRouterGetRib(node.getRid(), null);
            
        } catch (NoSuchAlgorithmException e) {
            mainWindow.errorMessage("Please start the CBGP algorithm before using it!");
            routes = null;
        } catch (RoutingException e) {
            routes = null;
        }
        Object[][] content = getContent(routes, titles);
        JTable jt = new JTable(content, titles);
        jt.setEnabled(false); // pas moyen d'éditer les cellules
        return new JScrollPane(jt);     
    }
    
    private Object[][] getContent(Vector routes,  String[] titles) {
        Object[][] results = new Object[routes.size()][titles.length];
        int i = 0;
        for (Enumeration routesEnum = routes.elements(); routesEnum.hasMoreElements();) {
            Route route = (Route) routesEnum.nextElement();
            results[i][0] = route.getPrefix();
            results[i][1] = route.getNexthop().toString();
            results[i][2] = route.getLocalPref();
            results[i][3] = route.getMED();
            results[i][4] = route.getPath().toString();
            if (route.getOrigin() == 0)
                results[i][5] = "igp";
            else if (route.getOrigin() == 1)
                results[i][5] = "egp";
            else
                results[i][5] = "incomplete";
            i++;
        }
        return results;
    }
    

    
    private class configContent extends JPanel {
        
        /**
         * retourne les informations de configuration BGP d'un noeud
         * 
         * @param node le neoud duquel on veut les informations de configurations
         */
        public configContent(Node node) {
            JFormattedTextField ftf[] = new JFormattedTextField[2];
            String des[] = new String[ftf.length]; // description of each field
            
            InterDomainManager idm = InterDomainManager.getInstance();
            Domain domain = idm.getDefaultDomain();
            
            des[0] = "AS Number";
            ftf[0] = new JFormattedTextField(""+domain.getASID());
            ftf[0].setEditable(false); // pas myen d'éditer
            
            des[1] = "Router ID"; 
            ftf[1] = new JFormattedTextField(node.getRid());
            ftf[1].setEditable(false); // pas myen d'éditer
            
            // add each ftf[] to a BoxLayout
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            for (int j=0; j < ftf.length; j+=1) {
                JPanel borderPanel = new JPanel(new java.awt.BorderLayout());
                borderPanel.setBorder(new javax.swing.border.TitledBorder(des[j]));
                borderPanel.add(ftf[j], java.awt.BorderLayout.CENTER);
                add(borderPanel);
            }
        }
    }
}

