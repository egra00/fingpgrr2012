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

import be.ac.ucl.ingi.cbgp.IPTrace;
import be.ac.ucl.ingi.cbgp.bgp.Route;
import be.ac.ucl.ingi.totem.repository.CBGP;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpRouter;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ProgressBarPanel;

import javax.swing.*;
import java.util.*;

/**
 * @author Vanstals Thomas
 */

public class BGPDomainRecord{
	public int ASID;
	public Hashtable nodeHt = new Hashtable(); // va contenir les noeuds
	public Domain domain;
    private static MainWindow mainWindow = MainWindow.getInstance();
	
    /**
     * un BGPDomainRecord est un objet représentant un snapshot d'un domaine a un moment donné
     * @param d un domaine
     */
	public BGPDomainRecord(Domain d) {
		ASID = d.getASID();
		List lst = d.getAllBgpRouters(); 
		Iterator it = lst.iterator(); 
		BgpRouter r; 
		
        CBGP cbgp;
        try {   
			cbgp = (CBGP) RepositoryManager.getInstance().getAlgo("CBGP");
		} catch (NoSuchAlgorithmException e) {
            mainWindow.errorMessage("Please start the CBGP algorithm before using it!");
			return;
		}
		
        // create a progress bar
        ProgressBarPanel progressBar = new ProgressBarPanel(0, lst.size(), 400);
        JDialog dialog = mainWindow.showDialog(progressBar, new String("CBGP Snapshot : progress"));
        int progress = 0;
        progressBar.setMessage(progress+" node(s) recorded (remaining "+(lst.size()-progress)+")");
		while(it.hasNext()){
            // for every node
			r = (BgpRouter) it.next();
			Vector rib = null;
            Hashtable routeRecordHt = new Hashtable();
            Hashtable ribHt = new Hashtable();
			//Vector rt;
	        try {
                // get RIB
	            rib = cbgp.bgpRouterGetRib(r.getRid(), null);
	        } catch (RoutingException e) {
                System.out.println("Get Rib command failed");
                System.out.println("reason: "+e.getMessage());
	            return;
	        }
            
            Enumeration routesEnum = null;
            for (routesEnum = rib.elements(); routesEnum.hasMoreElements();) {
                // for every route in the rib of the node
                Route route = (Route) routesEnum.nextElement();
                ribHt.put(route.getPrefix().toString(),route);
                try {
                    // record the route to this prefix
                    IPTrace ipt = cbgp.netNodeRecordRoute(r.getRid(),route.getNexthop().toString());
                    // an save it
                    routeRecordHt.put(route.getNexthop().toString(),ipt);
                } catch (RoutingException e) {
                    System.out.println("Record route command failed");
                    System.out.println("reason: "+e.getMessage());
                }
            }
	        
            // everything is saved in an Object[]
	        Object[] a = new Object[5];
	        a[0]=r.getRid(); // save Router name
            a[1]=r.getId(); // save Router IPAdress
            a[2]=ribHt; // save RIB
            a[3]=routeRecordHt; // save the recorded routes
	        nodeHt.put(r.getRid(),a);
            
            progressBar.setValue(++progress); // update the bar progress
            progressBar.setMessage(progress+" node(s) recorded (remaining "+(lst.size()-progress)+")");
		}
        dialog.dispose();
	}
}