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

import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.PureMPLSCosLinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.LinkLoadComputerAlreadyExistsException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidLinkLoadComputerException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.LinkLoadComputerIdException;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/*
* Changes:
* --------
*
*/

/**
 * TODO: adapt to changes in the domain and the tms
 *
 * Dialog to compute the load based on multiples traffic matrix, one per class of service.
 * @see PureMPLSCosLinkLoadComputer
 *
 * <p>Creation date: 24/01/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class MPLSRoutingDialog extends JDialog {

    private final HashMap<String, JComboBox> cosToTm;
    private final TmCellRenderer comboRenderer;

    private CosPanel cosPanel;

    private Domain domain;

    public MPLSRoutingDialog() throws HeadlessException {
        super(MainWindow.getInstance(), "MPLS Routing");

        cosToTm = new HashMap<String, JComboBox>();
        comboRenderer = new TmCellRenderer();

        fillData();

        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        cosPanel = new CosPanel();

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton btn = new JButton("Accept");
        btn.addActionListener(new AcceptActionListener());
        buttonPanel.add(btn);

        btn = new JButton("Cancel");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(btn);

        add(cosPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private class CosPanel extends JPanel {
        public CosPanel() {
            super();
            setupUI();
        }

        private void setupUI() {
            setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5);

            add(new JLabel("Class of service"), c);
            c.gridx++;
            add(new JLabel("Traffic Matrix"), c);
            c.gridx = 0;
            c.gridy++;

            for (Map.Entry<String, JComboBox> entry : cosToTm.entrySet()) {
                add(new JLabel(entry.getKey()), c);
                c.gridx++;
                add(entry.getValue(), c);
                c.gridx = 0;
                c.gridy++;
            }

        }
    }

    private void fillData() {
        this.domain = GUIManager.getInstance().getCurrentDomain();
        cosToTm.clear();

        List<Integer> tms = TrafficMatrixManager.getInstance().getTrafficMatrices(domain.getASID());

        Integer[] tmsId = new Integer[tms.size()];
        int i = 0;
        for (Integer tm : tms)
            tmsId[i++] = tm;

        List<String> cos = domain.getClassesOfService();
        if (cos.size() == 0) {
            JComboBox comboBox = new JComboBox(tmsId);
            comboBox.setRenderer(comboRenderer);
            cosToTm.put("default", comboBox);
        } else {
            for (String classname : cos) {
                JComboBox comboBox = new JComboBox(tmsId);
                comboBox.setRenderer(comboRenderer);
                cosToTm.put(classname, comboBox);
            }
        }
    }

    private void domainChanged() {
        fillData();
        cosPanel = new CosPanel();
    }

    private class AcceptActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            PureMPLSCosLinkLoadComputer llc = new PureMPLSCosLinkLoadComputer(domain);

            try {
                for (Map.Entry<String, JComboBox> entry : cosToTm.entrySet()) {
                    TrafficMatrix tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(), (Integer)entry.getValue().getSelectedItem());
                    llc.addTrafficMatrix(tm, entry.getKey());
                }

                try {
                    String id = LinkLoadComputerManager.getInstance().generateId(domain, "MPLS");
                    LinkLoadComputerManager.getInstance().addLinkLoadComputer(llc, true, id);
                } catch (LinkLoadComputerAlreadyExistsException e1) {
                    try {
                        Pair<String, LinkLoadComputer> pair = LinkLoadComputerManager.getInstance().getLinkLoadComputer(llc);
                        llc = (PureMPLSCosLinkLoadComputer)pair.getSecond();
                        LinkLoadComputerManager.getInstance().setDefaultLinkLoadComputer(domain, pair.getFirst());
                    } catch (InvalidLinkLoadComputerException e2) {
                        JOptionPane.showMessageDialog(MPLSRoutingDialog.this, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        e1.printStackTrace();
                        return;
                    }
                } catch (LinkLoadComputerIdException e1) {
                    //should not happen
                    e1.printStackTrace();
                    return;
                }

                llc.recompute();

                dispose();
            } catch (InvalidTrafficMatrixException e1) {
                e1.printStackTrace();
                MainWindow.getInstance().errorMessage(e1);
            }
        }
    }

    /**
     * Traffic matrix number will appear in grey if it was already selected for a class of service
     */
    private class TmCellRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Integer) {
                for (JComboBox combo : cosToTm.values()) {
                    if (combo.getSelectedItem() != null && combo.getSelectedItem().equals(value)) {
                        setForeground(Color.gray);
                        break;
                    }
                }
            }
            return this;
        }
    }
}
