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

import be.ac.ucl.ingi.cbgp.IPPrefix;
import be.ac.ucl.ingi.cbgp.IPTrace;
import be.ac.ucl.ingi.cbgp.UnknownMetricException;
import be.ac.ucl.ingi.cbgp.bgp.Route;
import be.ac.ucl.ingi.totem.repository.CBGP;
import be.ac.ucl.ingi.totem.repository.model.CBGPSimulator;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ProgressBarPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.AbstractGUIModule;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.*;

/*
 * Changes:
 * --------
 * - 23-Aug-2006 : Optimize imports, remove unused variable, use Calendar rather than Date deprecated methods (GMO)
 * - 24-Oct-2006 : not singleton anymore, add a public constructor to accomodate new module loader (GMO)
 */


/**
 * TODO: translate comments and JavaDoc
 *
 * @author Thomas Vanstals
 *
 */
public class CBGPManager extends AbstractGUIModule {
    
    static final String TOTAL_CHANGES = "totalChanges";
    static final String AS_TOTAL_CHANGES = "asTotalChanges";
    // les différents types de changements possibles
    static final String SERIES1 = "prefix down";
    static final String SERIES2 = "prefix up";
    static final String SERIES3 = "peer change";
    static final String SERIES4 = "egress change";
    static final String SERIES5 = "intra cost change";
    static final String SERIES6 = "intra path change";
    static final String SERIES7 = "no change";
    static final String CHANGES_COUNT = "changes count";
    
    private JMenuItem diffMenuItem = null;
    private TreeMap snapshotHt = new TreeMap();
    private int totalSnapshot = 0;
    private snapshotChooser ssc = null;
    private static MainWindow mainWindow = MainWindow.getInstance();
    
    
    public CBGPManager() {

    }
    
    /**
     * Should the Module be loaded at GUI startup ? yes
     *
     * @return true
     */
    public boolean loadAtStartup() {
        return true;
    }
    
    /**
     * This method returns the module name ("CBGP")
     *
     * @return the module name
     */
    public String getName() {
        return "CBGP";
    }
    
    
    /**
     * returns the menu for this module
     *
     * @return the menu for this module
     */
    public JMenu getMenu() {
        JMenu menu = new JMenu("CBGP");
        menu.setMnemonic(KeyEvent.VK_G);

        JMenuItem menuItem = new JMenuItem("Take Snapshot");
        menu.add(menuItem);
        menuItem.addActionListener(new snapshotListener());

        menuItem = new JMenuItem("Diff snapshots");
        menuItem.setEnabled(false);
        menu.add(menuItem);
        menuItem.addActionListener(new diffListener());
        diffMenuItem = menuItem;
        
        menu.addSeparator();
        
        menuItem = new JMenuItem("Compute centrality");
        menu.add(menuItem);
        menuItem.addActionListener(new centralityListener());

        menu.addSeparator();
        
        menuItem = new JMenuItem("CGBP Run");
        menu.add(menuItem);
        menuItem.addActionListener(new CBGPRunListener());
        return menu;
    }
    
    /**
     * cette methode se charge d'effectuer un enregistrement des données C-BGP et ensuite de rafraichir le menu
     * @param comment un commentaire identifier le snapshot
     *
     */
    public void takeSnapshot(String comment){
        new recorder(comment);
        refreshMenu();
    }
    
    /**
     * cette methode se charge du rafraichissement du menu
     *
     */
    private void refreshMenu(){
        // pour avoir la possibilité d'effectuer une différence entre deux snapshot, il faut qu'il y en ait au moins 2 disponibles
        if(!diffMenuItem.isEnabled() && totalSnapshot>1)
            diffMenuItem.setEnabled(true);
    }
    
    /**
     * le listener charge de l'entrée "CBGPRun" du menu
     * @author Thomas Vanstals
     *
     */
    private class CBGPRunListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            try {
                CBGPSimulator cbgp = (CBGP) RepositoryManager.getInstance().getAlgo("CBGP");
                try {
                    cbgp.simRun();
                } catch (RoutingException e) {
                    System.out.println("CBGPRun command failed");
                    System.out.println("reason: "+e.getMessage());
                }
            } catch (NoSuchAlgorithmException e) {
                mainWindow.errorMessage("Please start the CBGP algorithm before using it!");
                return;
            }
        }
    }
    
    /**
     * le listener chargé de l'entée "Take Snapshot" du menu
     * @author Thomas Vanstals
     *
     */
    private class snapshotListener implements ActionListener {
        
        InterDomainManager idm = InterDomainManager.getInstance();
        
        public void actionPerformed(ActionEvent ae) {
            new commentWaiter();
            refreshMenu();
        }
    }
    
    
    /**
     * le listener chargé de l'entée "Diff Snapshot" du menu
     * @author Thomas Vanstals
     *
     */
    private class diffListener implements ActionListener {
        
        public void actionPerformed(ActionEvent aE) {
            if(totalSnapshot>1){
                ssc = new snapshotChooser();
            }
        }
    }
    
    /**
     * le listener chargé de l'entée "Compute centrality" du menu
     * @author Thomas Vanstals
     *
     */
    private class centralityListener implements ActionListener {
        
        public void actionPerformed(ActionEvent aE) {
            new CentralityComputer();
        }
    }
    
    
    /**
     * Se charge de faire un snapshot de la topologie
     * 
     * @author Thomas Vanstals
     *
     */
    private class recorder {
        
        /**
         * 
         * @param comment un commentaire pour identifier le snapshot parmis les autres
         */
        public recorder(String comment) {  
            InterDomainManager idm = InterDomainManager.getInstance();
            final Hashtable ht = new Hashtable();
            
            if (idm.getNbDomains()==0){
                mainWindow.errorMessage("There are no domain loaded!");
                return;
            }
            /*
             for (Domain domain : idm.getAllDomains())
             {
             ht.put(domain.getASID(),new BGPDomainRecord(domain));
             }
             */
            if (idm.getNbDomains()>1){
                // on averti que seul le domaine par défaut sera sauvegardé
                Object[] options = { "OK", "CANCEL" };
                int n = JOptionPane.showOptionDialog(null, "Only the defaut domain will be recorded!", "Warning",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                        null, options, options[0]);
                if(n==1){ // CANCEL
                    return;
                }
            }
            
            final Domain domain = idm.getDefaultDomain();
            new Thread(new Runnable() {
                public void run() {
                    /**/Date d1 = new Date();
                    ht.put(""+domain.getASID(), new BGPDomainRecord(domain));
                    /**/Date d2 = new Date();
                    /**/System.out.println("Snapshot : "+(d2.getTime() - d1.getTime())+" ms");
                }
            }).start();

            Calendar c = Calendar.getInstance();
            String now = ""+c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE)+":"+c.get(Calendar.SECOND);
            snapshotHt.put(totalSnapshot+1+" - "+comment+" ("+now+")",ht);
            totalSnapshot++;
        }
    }
    
    private class diff {
        
        /**
         * cette methode calcule les différences entre deux snapshots
         * 
         * @param d1 l'index dans la table de hash result du premier snapshot
         * @param d2 l'index dans la table de hash result du deuxième snapshot
         * @param result la table de hash contenant les tous les snapshots
         */
        public diff(String d1, String d2, Hashtable result) {
            
            Enumeration ehtAtStart = ((Hashtable) snapshotHt.get(d1)).elements();
            Hashtable ehtAtStop = ((Hashtable) snapshotHt.get(d2));
            
            while (ehtAtStart.hasMoreElements()) {
                /* on exécute une seule fois cette boucle pour le moment 
                 * car le snapshot est réalise sur le domaine par défaut
                 */
                // on prend le domaine
                BGPDomainRecord dr1 = (BGPDomainRecord) ehtAtStart.nextElement();
                // on trouve le 2ème snapshot de ce domaine
                BGPDomainRecord dr2 = (BGPDomainRecord) ehtAtStop.get(""+dr1.ASID);
                
                // initialisation de la table de hash qui sera renvoyée comme résultats
                Hashtable asResult = new Hashtable();
                result.put("AS N°"+dr1.ASID,asResult);
                Hashtable changeCountHt = new Hashtable();
                asResult.put(CHANGES_COUNT,changeCountHt);
                
                Hashtable nhh1 = dr1.nodeHt;
                
                int asTotalChanges = 0; // le nombre total de chagements pour l'as
                
                // create a progress bar
                int nNode = nhh1.size();
                ProgressBarPanel progressBar = new ProgressBarPanel(0, nNode, 400);
                JDialog dialog = mainWindow.showDialog(progressBar, "Computing differences : progress");
                int progress = 0;
                progressBar.setMessage("computed for "+progress+" node(s) (remaining "+(nNode-progress)+")");
                
                for (Enumeration e2 = nhh1.elements(); e2.hasMoreElements();) {
                    // on prend le premier routeur
                    Object[] o1 = (Object[]) e2.nextElement();
                    String r1Id = (String) o1[0];
                    String r1Name = (String) o1[1];
                    //Vector v1 = (Vector) o1[2];
                    Hashtable h1 = (Hashtable) o1[2];
                    Hashtable routeRecordHtBefore = (Hashtable) o1[3];
                    // on trouve le 2ème snapshot de ce routeur
                    Hashtable nhh2 = dr2.nodeHt;
                    
                    Hashtable routeRecordHtAfter = null;
                    Hashtable h2 = null;
                    if (nhh2.containsKey(r1Id)){
                        Object[] o2 = (Object[]) nhh2.get(r1Id);
                        routeRecordHtAfter = (Hashtable) o2[3];
                        h2 = (Hashtable) o2[2];
                    }
                    else{
                        break;
                    }
                    
                    // on a maintenant les 2 snapshots du routeur
                    Hashtable routerResult = new Hashtable();
                    String nodeId = r1Id.toString()+" ("+r1Name+")";
                    asResult.put(nodeId,routerResult);
                    Hashtable downPrefixHt = new Hashtable();
                    routerResult.put(SERIES1,downPrefixHt);
                    Hashtable upPrefixHt = new Hashtable();
                    routerResult.put(SERIES2,upPrefixHt);
                    Hashtable peerChangeHt = new Hashtable();
                    routerResult.put(SERIES3,peerChangeHt);
                    Hashtable egressChangeHt = new Hashtable();
                    routerResult.put(SERIES4,egressChangeHt);
                    Hashtable intraCostChangeHt = new Hashtable();
                    routerResult.put(SERIES5,intraCostChangeHt);
                    Hashtable intraPathChangeHt = new Hashtable();
                    routerResult.put(SERIES6,intraPathChangeHt);
                    Hashtable noChangeChangeHt = new Hashtable();
                    routerResult.put(SERIES7,noChangeChangeHt);
                    
                    
                    int totalChanges = 0; // le nombre total de changements pour ce router
                    
                    // CBGP simulator available ? 
                    try {
                        CBGPSimulator cbgp = (CBGP) RepositoryManager.getInstance().getAlgo("CBGP");
                    } catch (NoSuchAlgorithmException e) {
                        mainWindow.errorMessage("Please start the CBGP algorithm before using it!");
                        return;
                    }
                    
                    Hashtable prefixList = new Hashtable(); // tous les préfixes rencontrés
                    Hashtable newPrefixList = new Hashtable(); // nouveau préfixes up lors du deuxième snapshot
                    Hashtable stilUpPrefixList = new Hashtable(); // préfixes encore up lors du deuxième snapshot
                    Enumeration routesEnum = null;
                    // 1. création d'une liste de tous les préfixes
                    for (routesEnum = h1.elements(); routesEnum.hasMoreElements();) {
                        Route route = (Route) routesEnum.nextElement();
                        prefixList.put(route.getPrefix().toString(),route.getPrefix());
                    }
                    for (routesEnum = h2.elements(); routesEnum.hasMoreElements();) {
                        Route route = (Route) routesEnum.nextElement();
                        if(!prefixList.containsKey(route.getPrefix().toString())){
                            // nouveau prefixe
                            newPrefixList.put(route.getPrefix().toString(),route.getPrefix());
                            // 2. Liste des prefix up
                            upPrefixHt.put(route.getPrefix().toString(),"");
                            totalChanges++;
                        }
                        else // prefixe deja up lors du premier snapshot
                            stilUpPrefixList.put(route.getPrefix().toString(),route.getPrefix()); 
                    }   
                    
                    // 3. Liste des prefix down (ceux qui sont dans prefixList et pas dans stilUpPrefixList)
                    Enumeration downPre = null;
                    for (downPre = prefixList.elements(); downPre.hasMoreElements();) {
                        IPPrefix ipp = (IPPrefix) downPre.nextElement();
                        // si pas dans stilUpPrefixList, prefix down 
                        if(!stilUpPrefixList.containsKey(ipp.toString())){
                            downPrefixHt.put(ipp.toString(),"");
                            totalChanges++;
                        }
                    }
                    
                    //else{ // prefix stil up
                    // 4. Parmis les prefix toujours up, next AS encore identique ?
                    Enumeration stilUPre = null;
                    int asn1 = 0;
                    int asn2 = 0;
                    IPPrefix ipp = null;
                    Route routeBefore = null;
                    Route routeAfter = null;
                    for (stilUPre = stilUpPrefixList.elements(); stilUPre.hasMoreElements();) {
                        ipp = (IPPrefix) stilUPre.nextElement();
                        // trouver la route vers ce prefixe lors du premier snapshot
                        routeBefore = (Route) h1.get(ipp.toString());
                        try{
                            StringTokenizer st = new StringTokenizer(routeBefore.getPath().getSegment(0).toString());
                            Integer temp = new Integer(st.nextToken());
                            asn1 = temp.intValue();
                            // break; // route est la bonne route
                        } catch (Exception e){
                            break;
                        }
                        // trouver la route vers ce prefixe lors du second snapshot
                        routeAfter = (Route) h2.get(ipp.toString());
                        try{
                            StringTokenizer st = new StringTokenizer(routeAfter.getPath().getSegment(0).toString());
                            Integer temp = new Integer(st.nextToken()); // premier entrée dans l'AS path
                            asn2 = temp.intValue();
                        } catch (Exception e){
                            break;
                        }
                        // as number identiques ? 
                        if(asn1!=asn2){
                            String[] changes = {"From : "+asn1, "To : "+asn2};
                            peerChangeHt.put(ipp.toString(),changes);
                            totalChanges++;
                        }
                        else{
                            // 5. next AS pas change, routeur egress change ? 
                            String nhBefore = routeBefore.getNexthop().toString();
                            String nhAfter = routeAfter.getNexthop().toString();
                            if(!(nhBefore.equals(nhAfter))){
                                String[] changes = {"From : "+nhBefore, "To : "+nhAfter};
                                egressChangeHt.put(ipp.toString(),changes);
                                totalChanges++;
                            }
                            else {
                                // 6. si routeur egress identique, intra cost identique ? 
                                IPTrace iptBefore = null;
                                IPTrace iptAfter = null;
                                iptBefore = (IPTrace) routeRecordHtBefore.get(routeBefore.getNexthop().toString());
                                iptAfter = (IPTrace) routeRecordHtAfter.get(routeAfter.getNexthop().toString());
                                try {
                                    if(iptBefore.getWeight()!=iptAfter.getWeight()){
                                        String[] changes = {"From : "+iptBefore.getWeight(), "To : "+iptAfter.getWeight()};
                                        intraCostChangeHt.put(ipp.toString(),changes);
                                        totalChanges++;
                                    }
                                    else{
                                        // 7. si intra cost identique, intra path identique ?
                                        if(!(iptBefore.toString().equals(iptAfter.toString()))){
                                            String[] changes = {"From : "+iptBefore.toString(), "To : "+iptAfter.toString()};
                                            intraPathChangeHt.put(ipp.toString(),changes);
                                            totalChanges++;
                                        }
                                        else {
                                            // 8. si intra path identique, alors il n'y a pas eu de changements
                                            noChangeChangeHt.put(ipp.toString(),"");
                                        }
                                    }
                                } catch (UnknownMetricException e) {

                                }
                            }
                        }
                    }
                    progressBar.setValue(++progress); // update the bar progress
                    progressBar.setMessage("computed for "+progress+" node(s) (remaining "+(nNode-progress)+")");
                    changeCountHt.put(nodeId, (Integer) totalChanges);
                    asTotalChanges = asTotalChanges + totalChanges;
                }
                changeCountHt.put(AS_TOTAL_CHANGES, (Integer) asTotalChanges);
                dialog.dispose();
            }           
        }
    }
    
    /**
     * Display a window wherein the snapshots to be compared can be chosen
     * @author Thomas Vanstals
     *
     */
    private class snapshotChooser implements ActionListener{
        //private JComboBox source = null;
        private JComboBox from = null;
        //private JComboBox dest = null;
        private JComboBox to = null;
        private JComboBox outputType = null;
        
        private JDialog dialog = null;
        
        public snapshotChooser() {
            dialog = new JDialog(MainWindow.getInstance(), "Compare snapshots");
            dialog.setContentPane(setupUI());
            dialog.pack();
            dialog.setLocationRelativeTo(MainWindow.getInstance());
            dialog.setVisible(true);
        }
        
        private JPanel setupUI() {
            JPanel jp = new JPanel();
            //dialog.setSize(new Dimension(300, 350));
            
            jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
            
            // String nodes[] = manager.getNodesList();
            String diffKeys[] = new String[snapshotHt.size()];
            Iterator keys = null;
            int i = 0;
            for (keys = snapshotHt.keySet().iterator(); keys.hasNext();) {
                diffKeys[i]=(String) keys.next();
                i++;
            }
            
            from = new JComboBox(diffKeys);
            to = new JComboBox(diffKeys);
            jp.add(new JLabel("From snapshot"));
            jp.add(from);
            jp.add(new JLabel("To snapshot"));
            jp.add(to);
            
            jp.add(new JLabel("Output"));
            String op[] = new String[4];
            op[0] = "Graph (compact results)";
            op[1] = "Tree (change indexed)";
            op[2] = "Tree (prefix indexed)";
            op[3] = "Tree (prefix indexed, changes only)";
            outputType = new JComboBox(op);
            jp.add(outputType);
            
            JButton accept = new JButton("Accept");
            accept.addActionListener(this);
            jp.add(accept);
            dialog.getRootPane().setDefaultButton(accept);
            return jp;
        }
        
        void hide() {
            dialog.setVisible(false);
        }
        
        /**
         * Lance la comparaison entre snapshots
         *
         */
        void compare() {
            
            Hashtable<String, Hashtable> resultHt = new Hashtable<String, Hashtable>();
            
            /**/Date d1 = new Date();
            new diff(from.getSelectedItem().toString(),to.getSelectedItem().toString(), resultHt);
            /**/Date d2 = new Date();
            /**/System.out.println("Compare : "+(d2.getTime() - d1.getTime())+" ms");
            
            // compact view
            if(outputType.getSelectedIndex()==0){
                hide();
                SnapshotResultGraph graph = new SnapshotResultGraph(resultHt, "Impact on BGP routing");
                //graph.pack();
                RefineryUtilities.centerFrameOnScreen(graph);
                graph.setVisible(true);
                
            }
            // verbose view
            if(outputType.getSelectedIndex()==1){
                hide();
                // clean our big hastable
                Enumeration<String> e = resultHt.keys();
                while(e.hasMoreElements()){
                    Hashtable ht =  (Hashtable) resultHt.get(e.nextElement());
                    ht.remove(CHANGES_COUNT); // ne doit pas être affiché
                }
                new ResultTree(resultHt);
            }
            // second verbose view
            if(outputType.getSelectedIndex()>1){
                // cleaning the result hastable
                Enumeration<String> e = resultHt.keys();
                while(e.hasMoreElements()){
                    Hashtable ht =  (Hashtable) resultHt.get(e.nextElement());
                    ht.remove(CHANGES_COUNT); // le nombre de changements ne doit pas être affiché
                }
                // il faut indicer les resultats au niveau des routeurs par préfixe
                Hashtable prefixIndexedHt = new Hashtable(); // la table de hash transformée
                Enumeration asE = resultHt.elements(); // as level
                Enumeration asK = resultHt.keys();
                while(asK.hasMoreElements()){
                    String asNumber=asK.nextElement().toString();
                    Hashtable routerHt=(Hashtable)resultHt.get(asNumber);
                    
                    Hashtable asHt = new Hashtable();
                    prefixIndexedHt.put(asNumber, asHt); // on ajoute l'as dans le result
                    // idem pour les routers
                    Enumeration routerK = routerHt.keys();
                    while(routerK.hasMoreElements()){
                        String routerId=routerK.nextElement().toString();
                        Hashtable newRouterHt = new Hashtable(); // on va y mettre les préfixes
                        asHt.put(routerId, newRouterHt);
                        Hashtable changeHt=(Hashtable)routerHt.get(routerId);
                        // on prend tout les préfixes concernant un changement
                        Enumeration changeK = changeHt.keys();
                        while(changeK.hasMoreElements()){
                            String changeType=changeK.nextElement().toString(); // le type de changement
                            Hashtable prefixHt = (Hashtable)changeHt.get(changeType);
                            // on les ajoute les prefixes a newRouterHt avec comme valeur le type de changement
                            Enumeration prefixK = prefixHt.keys();
                            while(prefixK.hasMoreElements()){
                                String prefixId=prefixK.nextElement().toString(); // le type de changement
                                Hashtable type=new Hashtable();
                                if(changeType.equals(SERIES7)){
                                    if(outputType.getSelectedIndex()!=3){
                                        type.put(changeType,"");
                                        newRouterHt.put(prefixId, type);
                                    }
                                }
                                else{
                                    type.put(changeType,"");
                                    newRouterHt.put(prefixId, type);
                                }
                            }
                        }
                    }
                }
                hide();
                new ResultTree(prefixIndexedHt);
            }
            // System.out.println(new Date().toString());
        }
        
        public void actionPerformed(ActionEvent e) {
            new Thread(new Runnable() {
                public void run() {
                    ssc.compare();
                }
            }).start();
        }
    }
    
    /**
     * Display a window asking a comment fot the snapshot and then do the snapshot
     * @author Thomas Vanstals
     *
     */
    private class commentWaiter implements ActionListener{
        private JTextField commentField = null;
        private JDialog dialog = null;
        
        public commentWaiter() {
            dialog = new JDialog(MainWindow.getInstance(), "Enter a comment");
            dialog.setSize(200,70);
            dialog.setContentPane(setupUI());
            dialog.setLocationRelativeTo(dialog.getParent());
            dialog.setVisible(true);
            // dialog.pack();
        }
        
        private JPanel setupUI() {
            JPanel jp = new JPanel();
            jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));
            commentField = new JTextField();
            jp.add(commentField);
            
            JButton accept = new JButton("Ok");
            accept.addActionListener(this);
            jp.add(accept);
            dialog.getRootPane().setDefaultButton(accept);
            return jp;
        }
        
        void hide() {
            dialog.setVisible(false);
        }
        
        public void actionPerformed(ActionEvent e) {
            hide();
            new recorder(commentField.getText());
            refreshMenu();
        }
    }
    
    
    /**
     * build a window displaying a tree view for the snapshot comparaison
     * @author Thomas Vanstals
     *
     */
    private class ResultTree extends JFrame {
        JTree tree;
        JScrollPane jsp;
        ResultTree(Hashtable ht) {
            super("Comparison results");
            setSize(700, 600);
            // Build our tree out of our big hashtable
            tree = new JTree(ht);
            jsp = new JScrollPane(tree);
            getContentPane().add(jsp);
            this.setLocationRelativeTo(this.getParent());
            this.setVisible(true);
        }
    }
    
}