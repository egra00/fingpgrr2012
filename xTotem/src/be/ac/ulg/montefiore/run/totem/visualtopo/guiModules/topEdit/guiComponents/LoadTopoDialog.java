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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.guiComponents;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.XMLFactory;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.TopoChooser;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Domain;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;

/*
* Changes:
* --------
*
*/

/**
* Simple dialog to load a domain from an xml file.
*
* <p>Creation date: 19/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LoadTopoDialog extends JDialog {
    final private JTextField filenameTf;

    public LoadTopoDialog() {
        super(TopEditGUI.getInstance(), "Load Topology", false);

        filenameTf = new JTextField(30);
        setupUI();
    }

    private void setupUI() {
        JPanel contentPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.0;
        c.weighty = 1.0;
        c.insets = new Insets(5, 5, 5, 5);

        contentPanel.add(new JLabel("Topology file:"), c);
        c.gridx++;

        contentPanel.add(filenameTf, c);
        c.gridx++;

        JButton browse1 = new JButton("Browse...");
        browse1.addActionListener(new BrowseActionListener());

        contentPanel.add(browse1, c);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton acceptButton = new JButton("Accept");
        acceptButton.addActionListener(new AcceptActionListener());
        buttonPanel.add(acceptButton);

        getRootPane().setLayout(new BorderLayout());
        getRootPane().add(contentPanel, BorderLayout.CENTER);
        getRootPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private class BrowseActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            File f = (new TopoChooser()).loadTopo(LoadTopoDialog.this);
            if (f != null) {
                filenameTf.setText(f.getAbsolutePath());
            }
        }
    }

    private class AcceptActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Domain domain = null;
            try {
                domain = XMLFactory.loadDomain(filenameTf.getText());
            } catch (JAXBException e1) {
                e1.printStackTrace();
                String msg;
                if (e1.getLinkedException() != null) {
                    msg = e1.getLinkedException().getMessage();
                } else msg = e1.getMessage();

                JOptionPane.showMessageDialog(TopEditGUI.getInstance(), "Unable to load domain: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
            }
            TopEditGUI.getInstance().newEdition(domain);
            dispose();
        }
    }
}
