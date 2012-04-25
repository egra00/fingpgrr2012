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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.stats;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import javax.swing.*;

/*
* Changes:
* --------
* - 18-Jan-2007: add reservation for all CTs (GMO)
*/

/**
* Tabbed pane that displays statistics about the network reservation and load.
*
* <p>Creation date: 23 mars 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class NetworkStatTabbedPane extends JTabbedPane {

    public NetworkStatTabbedPane(Domain domain) {
        super(JTabbedPane.BOTTOM);
        add("Reservation", new JScrollPane(new ReservationNetworkStatTable(domain)));

        for (int ct : domain.getAllCTId()) {
            add("Reservation CT " + ct, new JScrollPane(new ReservationCTNetworkStatTable(domain, ct)));
        }

        for (Pair<String, LinkLoadComputer> pair : LinkLoadComputerManager.getInstance().getLinkLoadComputersWithId(domain)) {
            String id = pair.getFirst();
            LinkLoadComputer llc = pair.getSecond();
            add(id, new JScrollPane(new LoadNetworkStatTable(llc)));
        }
    }
}

