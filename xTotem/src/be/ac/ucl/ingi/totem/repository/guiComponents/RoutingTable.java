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

import be.ac.ucl.ingi.cbgp.Route;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * cette classe se charge de l'affichage graphique de la table de routage
 * 
 * @author Vanstals Thomas
 */

public class RoutingTable{
	
	private static Node node;
	protected JTextArea contentText = null;
	private Vector routes = null;
	
	public RoutingTable(Vector r, Node n) {
		routes = r;
		node = n;
		displayRT();
	}
	
    /**
     * affiche la table de routage
     */
	private void displayRT() {
		String title = "Routing table of "+node.getId();
		RTPanel rtp = new RTPanel(title, routes);
        rtp.setLocationRelativeTo(rtp.getParent());
		rtp.setVisible(true);
	}
	
	
	public class RTPanel extends JFrame {
		
        // les différents champs
		String titles[] = new String[] {"Prefix", "Next hop", "Origin"};
		Vector routes = null;
		JTabbedPane jtp;
       
        
        /**
         * affiche la JFrame
         * @param mainTitle le titre de la fenetre
         * @param r le vecteur contenant les données à afficher
         */
        public RTPanel(String mainTitle, Vector r) {
			super(mainTitle);
			routes = r;
			setSize(400, 200);
            Container contents = getContentPane();
            jtp = new JTabbedPane();
            jtp.addTab("Routing Table", rtContent());
            contents.add(jtp);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setVisible(true);
		}
		
        /**
         * @return un JScrollPane contenant la table de routage
         */
        private JScrollPane rtContent(){
            Object[][] content = getRTContent();
            JTable jt = new JTable(content, titles);
            jt.setEnabled(false); // pas moyen d'éditer les cellules           
            return new JScrollPane(jt);
        }
        
        /**
         * 
         * @return un Object[][] contenant les informations relative à la table de routage qui sera passé en argument à une JTable
         */
		private Object[][] getRTContent() {
			Object[][] results = new Object[routes.size()][titles.length];
			int i = 0;
			for (Enumeration routesEnum = routes.elements();
			routesEnum.hasMoreElements();) {
				Route route = (Route) routesEnum.nextElement();
				results[i][0] = route.getPrefix();
				results[i][1] = route.getNexthop();
				StringTokenizer tokenizer = new StringTokenizer(route.toString());
				String lastToken = "";
				while (tokenizer.hasMoreTokens())
					lastToken = tokenizer.nextToken();
				results[i][2] = lastToken;
				i++;
			}
			return results;
		}
	}
}


