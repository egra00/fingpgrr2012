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

import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.model.ObjectiveFunction;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SANeighbourhood;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SAInitialSolutionGenerator;
import be.ac.ulg.montefiore.run.totem.repository.genericheuristics.simulatedAnnealing.model.SAParameter;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.simulatedAnnealing.SAGUI;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.AbstractGUIModule;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.model.*;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPLGenerator;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPL;
import be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist.SinglePathCPLGeneratorParameter;
import be.ac.ulg.montefiore.run.totem.repository.allDistinctRoutes.AllDistinctRoutesException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomainBuilder;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.*;
import java.util.ArrayList;

/*
* Changes:
* --------
* 24-Oct-2006 : not singleton anymore, add a public constructor to accomodate new module loader (GMO)
*
*/

/**
 *
 * <p/>
 * <p>Creation date: 3 fÃ¯Â¿Åvr. 2006
 *
 * @author GaÃ¯Â¿Ål Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class SAMTEGUIModule extends AbstractGUIModule {

    public SAMTEGUIModule() {
    }

    public JMenu getMenu() {
        JMenu menu = new JMenu("SAMTE");
        menu.setMnemonic(KeyEvent.VK_S);
        JMenuItem menuItem = new JMenuItem("SAMTE...");
        menu.add(menuItem);
        menuItem.addActionListener(new SimulatedAnnealingListener());
        return menu;
    }

    public String getName() {
        return "SAMTE";
    }

    public boolean loadAtStartup() {
        return true;
    }

    public void startAnnealing(int max_hops, int nb_shortest_path, int max_lsp, Domain domain, TrafficMatrix tm) {

        /* add the objective funcions */
        ArrayList<ObjectiveFunction> ofl = new ArrayList<ObjectiveFunction>();
        ofl.add(new SAMTELoadBalancingOF());
        ofl.add(new SAMTELoadBalancingLimitationOF());
        ofl.add(new SAMTEMaxLoadOF());

        ArrayList<SANeighbourhood> nbhl = new ArrayList<SANeighbourhood>();

        RandomOneChangeNBH rocnbh = new RandomOneChangeNBH();
        nbhl.add(rocnbh);

        ArrayList<SAInitialSolutionGenerator> isgl = new ArrayList<SAInitialSolutionGenerator>();
        SimplifiedDomain sDomain = SimplifiedDomainBuilder.build(domain);

        SinglePathCPLGenerator generator = new SinglePathCPLGenerator();

        SinglePathCPL cpl = null;
        try {
            cpl = generator.generate(new SinglePathCPLGeneratorParameter(max_hops, sDomain, nb_shortest_path), true);
        } catch (LinkNotFoundException e1) {
            e1.printStackTrace();
            return;
        } catch (NodeNotFoundException e1) {
            e1.printStackTrace();
            return;
        } catch (AllDistinctRoutesException e1) {
            e1.printStackTrace();
            return;
        }

        isgl.add(new RandomInitialSolutionGenerator(sDomain, tm, max_lsp, cpl));

        rocnbh.setCpl(cpl);


        /* these are default parameters taken from the scenario default */
        float T0 = 0.023333333f;
        int L = 5000;
        float alpha = 0.80f;
        float epsilon = 12f;
        int K = 4;
        SAParameter params = new SAParameter(T0, L, alpha, epsilon, K, true);

        new SAGUI(ofl, nbhl, isgl, null, new SAMTESolutionDisplayer(domain), params);
    }


    private class SimulatedAnnealingListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {

            GUIManager manager = GUIManager.getInstance();
            MainWindow mainWindow = MainWindow.getInstance();

            if (manager.getCurrentDomain() == null)
                mainWindow.errorMessage("A domain must be loaded to perform this action.");
            else {
                if (GUIManager.getInstance().getManagedMatrices().size() <= 0) {
                    mainWindow.errorMessage("A Traffic Matrix must be loaded to perform this action.");
                    return;
                }
                JDialog dialog = new SAMTEParametersDialog();
                mainWindow.showDialog(dialog);
            }
        }
    }

    private class SAMTEParametersDialog extends JDialog {
        private JTextField maxHopValue = null;
        private JTextField nbShortestPath = null;
        private JTextField maxLsp = null;
        private JList tmList = null;

        public SAMTEParametersDialog() {
            super(MainWindow.getInstance(), "SAMTE parameters");
            setupUI();
        }

        private void setupUI() {
            JPanel generalPanel = new JPanel(new BorderLayout());
            JPanel paramsPanel = new JPanel(new GridBagLayout());
            JPanel params2Panel = new JPanel(new GridBagLayout());
            JPanel buttonPanel = new JPanel(new FlowLayout());

            generalPanel.add(paramsPanel, BorderLayout.NORTH);
            generalPanel.add(params2Panel, BorderLayout.CENTER);
            generalPanel.add(buttonPanel, BorderLayout.SOUTH);

            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            c.insets = new Insets(5, 5, 0, 5);

            paramsPanel.setBorder(BorderFactory.createTitledBorder("Candidate Path List Generator:"));

            maxHopValue = new JTextField(new Integer(7).toString());
            nbShortestPath = new JTextField(new Integer(5).toString());

            paramsPanel.add(new JLabel("MAX_HOP :"), c);
            paramsPanel.add(maxHopValue, c);
            paramsPanel.add(new JLabel("NB_SORTEST_PATH :"), c);
            paramsPanel.add(nbShortestPath, c);

            params2Panel.setBorder(BorderFactory.createTitledBorder("Random Initial Solution Generator:"));

            maxLsp = new JTextField(new Integer(5).toString());

            tmList = new JList(GUIManager.getInstance().getManagedMatrices().toArray());
            tmList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            try {
                int defID = TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID();
                tmList.setSelectedValue(new Integer(defID), true);
            } catch (InvalidTrafficMatrixException e) {
                //e.printStackTrace();
            }

            params2Panel.add(new JLabel("Max Lsp :"), c);
            params2Panel.add(maxLsp, c);
            params2Panel.add(new JLabel("Traffic Matrix:"), c);
            params2Panel.add(tmList, c);

            JButton acceptBtn = new JButton("Accept Parameters");
            JButton cancelBtn = new JButton("Cancel");

            acceptBtn.addActionListener(new AcceptParameters());
            cancelBtn.addActionListener(new CancelListener());

            buttonPanel.add(acceptBtn);
            buttonPanel.add(cancelBtn);

            add(generalPanel);

        }

        private SAMTEParametersDialog getThis() {
            return this;
        }

        private class AcceptParameters implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                int max_hops = -1;
                int nb_shortest_path = -1;
                int max_lsp = -1;
                TrafficMatrix tm = null;
                Domain domain = null;

                try {
                    max_hops = Integer.parseInt(maxHopValue.getText());
                } catch (NumberFormatException e1) {
                    JOptionPane.showMessageDialog(getThis(), "Parse error in MAX_HOP", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    nb_shortest_path = Integer.parseInt(nbShortestPath.getText());
                } catch (NumberFormatException e1) {
                    JOptionPane.showMessageDialog(getThis(), "Parse error in NB_SORTEST_PATH", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    max_lsp = Integer.parseInt(maxLsp.getText());
                } catch (NumberFormatException e1) {
                    JOptionPane.showMessageDialog(getThis(), "Parse error in Max Lsp", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                try {
                    domain = InterDomainManager.getInstance().getDefaultDomain();
                    int asid = domain.getASID();
                    tm = TrafficMatrixManager.getInstance().getTrafficMatrix(asid, (Integer) tmList.getSelectedValue());
                } catch (InvalidTrafficMatrixException e1) {
                    JOptionPane.showMessageDialog(getThis(), "Traffic Matrix not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (NullPointerException e1) {
                    JOptionPane.showMessageDialog(getThis(), "No default domain.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                dispose();
                startAnnealing(max_hops, nb_shortest_path, max_lsp, domain, tm);
            }
        }

        private class CancelListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }

    }
}
