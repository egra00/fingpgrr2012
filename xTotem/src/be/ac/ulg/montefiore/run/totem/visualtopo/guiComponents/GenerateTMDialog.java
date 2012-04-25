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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents;

import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import be.ac.ulg.montefiore.run.totem.topgen.traffic.TrafficGeneratorFactory;
import be.ac.ulg.montefiore.run.totem.topgen.traffic.TrafficGeneratorInterface;
import be.ac.ulg.montefiore.run.totem.topgen.traffic.TrafficGenerationException;
import be.ac.ulg.montefiore.run.totem.topgen.exception.InvalidParameterException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.HashMap;

/*
* Changes:
* --------
* - 28-Feb-2008: stop cell editing before accepting, change "cancel" button in "close" (GMO)
*/

/**
 * Dialog to generate traffic matrices
 *
 * @see TrafficGeneratorInterface
 *
 * <p>Creation date: 29/10/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class GenerateTMDialog extends JDialog {

    private final JComboBox generatorCmb;
    private final ParamTable paramTable;
    private final JButton cancelBtn;

    public GenerateTMDialog() {
        super(MainWindow.getInstance(), "Generate Traffic Matrix");

        generatorCmb = new JComboBox(TrafficGeneratorFactory.getAvailableGenerators());
        generatorCmb.addActionListener(new CmbListener());
        paramTable = new ParamTable(8);
        generatorCmb.setSelectedIndex(0);
        cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        setupUI();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton btn = new JButton("Generate");
        btn.addActionListener(new GenerateActionListener());
        buttonPanel.add(btn);

        buttonPanel.add(cancelBtn);

        add(buttonPanel, BorderLayout.SOUTH);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        add(mainPanel, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(0, 5, 5, 5);

        mainPanel.add(new JLabel("Generator"), c);
        c.gridy++;
        mainPanel.add(generatorCmb, c);
        c.gridy++;
        mainPanel.add(new JLabel("Parameters"), c);
        c.gridy++;
        mainPanel.add(new JScrollPane(paramTable), c);

    }


    private class GenerateActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                TrafficGeneratorInterface gen = TrafficGeneratorFactory.createGenerator((String)generatorCmb.getSelectedItem());

                if (paramTable.getCellEditor() != null && !paramTable.getCellEditor().stopCellEditing())
                    return;

                HashMap<String, String> params = paramTable.toHashMap();
                for (String param : params.keySet()) {
                    gen.setParam(param, params.get(param));
                }

                List<TrafficMatrix> list = gen.generate();

                if (list.size() == 0) {
                    JOptionPane.showMessageDialog(GenerateTMDialog.this, "No matrix generated.", "Info", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                for (TrafficMatrix tm : list) {
                    TrafficMatrixManager.getInstance().addTrafficMatrix(tm, TrafficMatrixManager.getInstance().generateTMID(tm.getASID()));
                }
                cancelBtn.setText("Close");
            } catch (InvalidParameterException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(GenerateTMDialog.this, "Invalid parameter: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (TrafficGenerationException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(GenerateTMDialog.this, "Error in generation: " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            } catch (InvalidDomainException e1) {
                e1.printStackTrace();
            } catch (TrafficMatrixAlreadyExistException e1) {
                e1.printStackTrace();
            } catch (TrafficMatrixIdException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class CmbListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String generator = (String)generatorCmb.getSelectedItem();

            List<ParameterDescriptor> params = TrafficGeneratorFactory.getParameters(generator);
            paramTable.empty();
            paramTable.fill(params);
            //pack();
        }
    }
}
