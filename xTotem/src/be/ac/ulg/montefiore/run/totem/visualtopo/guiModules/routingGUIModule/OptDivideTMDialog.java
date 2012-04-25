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

import be.ac.ulg.montefiore.run.totem.repository.optDivideTM.MTRWO.*;
import be.ac.ulg.montefiore.run.totem.repository.optDivideTM.OptDivideTMLoadComputer;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.LinkLoadComputerAlreadyExistsException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidLinkLoadComputerException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.LinkLoadComputerIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/*
* Changes:
* --------
* - 05-Feb-2007: WMeanDelay is now the default obj function (GMO)
*
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 18/01/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class OptDivideTMDialog extends JDialog {
    //first one is default value
    static final TEObjectiveFunction[] availableObjectiveFunctions = {new WMeanDelayOF(), new InvCapOF(), new MeanDelayOF(), new MinHopOF(), new NLFortzOF()};


    private JTextField NField;
    private JComboBox objCombo;
    private JList tmList;
    private JCheckBox fullmeshCheckBox;
    private JCheckBox verboseCheckBox;


    public OptDivideTMDialog() {
        super(MainWindow.getInstance(), "OptDivideTm parameters", false);
        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        JPanel parametersPanel = new JPanel(new GridBagLayout());
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        JButton okBtn = new JButton("Compute");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(new ComputeOptDivideTm());
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonsPanel.add(okBtn);
        buttonsPanel.add(cancelBtn);

        JPanel northPanel = new JPanel(new GridBagLayout());
        JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        JPanel tmPanel = new JPanel(new BorderLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(5, 5, 5, 5);

        NField = new JTextField("3");
        objCombo = new JComboBox(availableObjectiveFunctions);
        objCombo.setSelectedIndex(0);

        tmList = new JList(GUIManager.getInstance().getManagedMatrices().toArray());
        tmList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        try {
            int defID = TrafficMatrixManager.getInstance().getDefaultTrafficMatrixID();
            tmList.setSelectedValue(new Integer(defID), true);
        } catch (InvalidTrafficMatrixException e) {
            //e.printStackTrace();
        }

        northPanel.add(new JLabel("N:"), c);
        northPanel.add(NField, c);
        northPanel.add(new JLabel("Objective function:"), c);
        northPanel.add(objCombo, c);

        northPanel.setBorder(BorderFactory.createTitledBorder("Parameters:"));

        tmPanel.add(tmList, BorderLayout.CENTER);

        tmPanel.setBorder(BorderFactory.createTitledBorder("Select Traffic Matrix:"));

        fullmeshCheckBox = new JCheckBox("Establish fullmesh of LSPs");
        verboseCheckBox = new JCheckBox("Verbose");
        optionsPanel.add(fullmeshCheckBox);
        optionsPanel.add(verboseCheckBox);

        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options:"));


        GridBagConstraints c2 = new GridBagConstraints();
        c2.fill = GridBagConstraints.BOTH;
        c2.weightx = 1;
        c2.weighty = 1;
        c2.gridx = 0;
        c2.gridy = 0;
        c2.insets = new Insets(5, 5, 5, 5);

        parametersPanel.add(northPanel, c2);
        c2.gridy++;
        parametersPanel.add(tmPanel, c2);
        c2.gridy++;
        parametersPanel.add(optionsPanel, c2);
        c2.gridy++;

        add(buttonsPanel, BorderLayout.SOUTH);
        add(parametersPanel, BorderLayout.CENTER);

        getRootPane().setDefaultButton(okBtn);
    }

    private class ComputeOptDivideTm implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            int N;
            Domain domain;
            TrafficMatrix tm;

            try {
                N = Integer.valueOf(NField.getText());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Field N must be an integer.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            domain = GUIManager.getInstance().getCurrentDomain();
            if (domain == null) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "No domain.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int tmId = (Integer)tmList.getSelectedValue();
            try {
                tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(), tmId);
            } catch (InvalidTrafficMatrixException e1) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Invalid Traffic Matrix", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            OptDivideTMLoadComputer opt = new OptDivideTMLoadComputer(domain, tm);
            opt.setN(N);
            opt.setObjectiveFunction((TEObjectiveFunction)objCombo.getSelectedItem());
            opt.setVerbose(verboseCheckBox.isSelected());
            opt.setECMP(!fullmeshCheckBox.isSelected());


            try {
                String id = LinkLoadComputerManager.getInstance().generateId(domain, "Opt");
                LinkLoadComputerManager.getInstance().addLinkLoadComputer(opt, true, id);
            } catch (LinkLoadComputerAlreadyExistsException e1) {
                try {
                    Pair<String, LinkLoadComputer> pair = LinkLoadComputerManager.getInstance().getLinkLoadComputer(opt);
                    opt = (OptDivideTMLoadComputer)pair.getSecond();
                    LinkLoadComputerManager.getInstance().setDefaultLinkLoadComputer(domain, pair.getFirst());
                } catch (InvalidLinkLoadComputerException e2) {
                    JOptionPane.showMessageDialog(OptDivideTMDialog.this, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    e1.printStackTrace();
                    return;
                }
            } catch (LinkLoadComputerIdException e1) {
                //should not happen
                e1.printStackTrace();
                return;
            }

            opt.recompute();

            try {
                if (fullmeshCheckBox.isSelected()) {
                    opt.establishFullmeshes();
                }
            } catch (NoRouteToHostException ex) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "No route to Host: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (RoutingException ex) {
                JOptionPane.showMessageDialog(MainWindow.getInstance(), "Routing exception: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            dispose();
        }
    }
}
