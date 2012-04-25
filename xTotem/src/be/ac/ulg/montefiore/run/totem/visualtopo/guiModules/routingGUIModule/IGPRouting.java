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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.routingGUIModule;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.*;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.AbstractLinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
* Changes:
* --------
* - 20-Mar-2006: Add ECMP option (GMO).
* - 20-Mar-2006: Use LinkLoadComputerManager to stck calculated load (GMO).
* - 27-Mar-2006: Suppress InvFreeBw from panel (not IP routing). (GMO)
* - 10-Jan-2007: Add Overlay strategy (GMO)
* - 31-May-2007: Refactor in different panels (IGPRoutingOptionsPanel) (GMO)
* - 13-Aug-2007: Change Link Load Computer default name (GMO) 
*/

/**
*
* Class responsible of link load calculation (using IP routing or hybrid routing)
*
* <p>Creation date: 3 f�vr. 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class IGPRouting {
    private JDialog dialog = null;
    private JList mList = null;
    private Domain domain = null;
    private IGPRoutingOptionsPanel optPanel = null;

    public IGPRouting() {
        dialog = MainWindow.getInstance().showDialog(setupUI(), "IGP routing");
    }

    private JPanel setupUI() {
        JPanel generalPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JPanel contentPanel = new JPanel(new BorderLayout());

        generalPanel.add(buttonPanel, BorderLayout.SOUTH);
        generalPanel.add(contentPanel, BorderLayout.CENTER);

        optPanel = new IGPRoutingOptionsPanel();
        contentPanel.add(optPanel);

        JPanel matrixPanel = new JPanel(new BorderLayout());
        matrixPanel.setBorder(BorderFactory.createTitledBorder("Select Traffic Matrix"));
        domain = GUIManager.getInstance().getCurrentDomain();
        mList = new JList(GUIManager.getInstance().getManagedMatrices().toArray(new Integer[0]));
        try {
            int defID = TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID();
            mList.setSelectedValue(new Integer(defID), true);
        } catch (InvalidTrafficMatrixException e) {
            //e.printStackTrace();
        }

        mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        matrixPanel.add(mList, BorderLayout.CENTER);

        contentPanel.add(matrixPanel, BorderLayout.SOUTH);

        JButton button = new JButton("Compute Load");
        button.addActionListener(new AcceptListener());
        buttonPanel.add(button);
        button = new JButton("Cancel");
        button.addActionListener(new CancelListener());
        buttonPanel.add(button);

        return generalPanel;
    }

    private class AcceptListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            TrafficMatrix tm = null;
            try {
                tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(), (Integer)mList.getSelectedValue());
            } catch (InvalidTrafficMatrixException e1) {
                JOptionPane.showMessageDialog(dialog, "Invalid Traffic Matrix", "Error", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
                return;
            } catch (NullPointerException e1) {
                JOptionPane.showMessageDialog(dialog, "Invalid Traffic Matrix", "Error", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
                return;
            }


            LinkLoadStrategy lls = optPanel.getStrategy(domain);
            lls.setTm(tm);

            LinkLoadComputer llc = lls;
            try {
                String algoName = lls.getSPFAlgo().getClass().getSimpleName().substring(1);
                String id = LinkLoadComputerManager.getInstance().generateId(domain, algoName);
                LinkLoadComputerManager.getInstance().addLinkLoadComputer(lls, true, id);
            } catch (LinkLoadComputerAlreadyExistsException e1) {
                try {
                    Pair<String, LinkLoadComputer> pair = LinkLoadComputerManager.getInstance().getLinkLoadComputer(lls);
                    llc = pair.getSecond();
                    LinkLoadComputerManager.getInstance().setDefaultLinkLoadComputer(domain, pair.getFirst());
                } catch (InvalidLinkLoadComputerException e2) {
                    JOptionPane.showMessageDialog(dialog, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                    return;
                }
            } catch (LinkLoadComputerIdException e1) {
                JOptionPane.showMessageDialog(dialog, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                e1.printStackTrace();
                return;
            }

            llc.recompute();

            dialog.dispose();
        }
    }

    private class CancelListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            dialog.dispose();
        }
    }

}
