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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.SAMTE;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.simulatedAnnealing.SolutionDisplayer;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SASolution;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.model.SAMTESolution;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.model.ExtendedPath;
import be.ac.ulg.montefiore.run.totem.domain.exception.*;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.DomainConvertor;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.LspImpl;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;

import java.util.List;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.apache.log4j.Logger;

import javax.swing.*;

/*
* Changes:
* --------
* - 09-Aug-2007: Add confirmation dialog for old LSPs suppression (GMO)
*
*/

/**
*
*
* <p>Creation date: 2 févr. 2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class SAMTESolutionDisplayer implements SolutionDisplayer {

    private static Logger logger = Logger.getLogger(SAMTESolutionDisplayer.class);

    Domain domain = null;
    JDialog dialog = null;
    Object[][] data = null;
    SAMTESolution sol = null;

    public SAMTESolutionDisplayer(Domain domain) {
        this.domain = domain;
    }

    public void display(SASolution solution) {
        try {
            sol = (SAMTESolution) solution;
        } catch (ClassCastException e) {
            logger.error("Not a SAMTE solution.");
            return;
        }

        JPanel generalPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        JPanel topPanel = new JPanel(new GridLayout(0, 2, 10, 10));

        generalPanel.add(buttonPanel, BorderLayout.SOUTH);
        generalPanel.add(topPanel, BorderLayout.NORTH);

        JButton okButton = new JButton("Establish LSPs");
        okButton.addActionListener(new AcceptListener());
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new CancelListener());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        topPanel.setBorder(BorderFactory.createTitledBorder("Summary"));

        topPanel.add(new JLabel("Objective function used:"));
        topPanel.add(new JLabel(sol.getObjectiveFunction().getName()));

        topPanel.add(new JLabel("Cost of this solution:"));
        topPanel.add(new JLabel(new Double(sol.evaluate()).toString()));

        DomainConvertor convertor = domain.getConvertor();

        List<ExtendedPath> lspList = sol.getLspList();
        data = new Object[lspList.size()][4];
        try {
            int i = 0;
            for (ExtendedPath ep : lspList) {
                data[i][0] = i;
                data[i][1] = convertor.getNodeId(ep.getIngress());
                data[i][2] = convertor.getNodeId(ep.getEgress());
                data[i][3] = ep.convert(domain);
                ++i;
            }
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
            logger.error("Error in SAMTE solution node conversion.");
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
            logger.error("Error in SAMTE solution link conversion.");
        } catch (InvalidPathException e) {
            e.printStackTrace();
            logger.error("Error in SAMTE solution: path is invalid.");
        }


        String[] columnNames = {"LSP", "Ingress", "Egress", "Path"};

        JTable lspTable = new JTable(data, columnNames);

        generalPanel.add(new JScrollPane(lspTable), BorderLayout.CENTER);


        dialog = MainWindow.getInstance().showDialog(generalPanel, "SAMTE computation result");

    }

    private class CancelListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            dialog.dispose();
        }
    }

    private class AcceptListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (domain.getNbLsps() > 0) {
                int n = JOptionPane.showConfirmDialog(MainWindow.getInstance(), "Do you want to remove all lsps established in the current domain prior to establishing new ones ?", "Confirm suppress LSPs",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (n == JOptionPane.YES_OPTION)
                    domain.removeAllLsps();
            }

            TrafficMatrix tm = sol.getTm();
            for (int i = 0; i < data.length; i++) {
                try {
                    Lsp lsp = new LspImpl(domain,domain.generateLspId(),tm.get((String)data[i][1],(String)data[i][2]),(Path)data[i][3]);
                    domain.addLsp(lsp);
                } catch (Exception e1) {
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), e1.getClass().getSimpleName() + ": " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                }
            }
            dialog.dispose();
        }
    }
}
