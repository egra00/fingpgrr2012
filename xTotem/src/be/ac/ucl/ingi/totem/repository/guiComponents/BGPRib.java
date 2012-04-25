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

import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ucl.ingi.cbgp.bgp.Route;

import javax.swing.*;
import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;


/**
 * @author Vanstals Thomas
 */

public class BGPRib {
	
	private static Node node;
	protected JTextArea contentText = null;
	private Vector routes = null;
	
	public BGPRib(Vector r, Node n) {
		routes = r;
		node = n;
		displayBGPRib();
	}
	
	private void displayBGPRib() {
		String title = "BGP Rib of "+node.getId();
		RTPanel rtp = new RTPanel(title, routes);
		rtp.setVisible(true);
	}
	
	
	public class RTPanel extends JFrame {
		
		String titles[] = new String[] {"Prefix", "Next hop", "Local pref", "MED", "AS Path", "Origin"};
		Vector routes = null;
		
		public RTPanel(String mainTitle, Vector r) {
			super(mainTitle);
			routes = r;
			setSize(500, 300);
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			
			Object[][] content = getContent();
			
			JTable jt = new JTable(content, titles);
			jt.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			jt.setColumnSelectionAllowed(true);
			
			JScrollPane jsp = new JScrollPane(jt);
			getContentPane().add(jsp, BorderLayout.CENTER);
		}
		
		public Object[][] getContent() {
			Object[][] results = new Object[routes.size()][titles.length];
			
			int i = 0;
			for (Enumeration routesEnum = routes.elements(); routesEnum.hasMoreElements();) {
				Route route = (Route) routesEnum.nextElement();
			    System.out.println(route.toString());
				results[i][0] = route.getPrefix();
				results[i][1] = route.getNexthop().toString();
				results[i][2] = route.getLocalPref();
				results[i][3] = route.getMED();
				results[i][4] = route.getPath().toString();
				if (route.getOrigin() == 0)
					results[i][5] = "i";
				else
					results[i][5] = "?";
				i++;
			}

			return results;
		}
	}
}


