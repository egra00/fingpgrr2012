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

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.DomainAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 14-May-2007: add option to use the default reroute method (GMO).
* - 06-Jul-2007: bugfix: reroute method option was always enabled (GMO).
* - 25-Sep-2007: remove reroute method option (GMO)
* - 05-Dec-2007: catch all exceptions when loading a domain (GMO)
*/

/**
* Choose a topology to load and various options.
*
* <p>Creation date: 18/10/2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class LoadTopoDialog extends JDialog {
    static final private Logger logger = Logger.getLogger(LoadTopoDialog.class);

    static private File lastFile = new File(".");
    private JCheckBox removeMultipleLinks;
    private JCheckBox useBWSharing;
    private JTextField inputFileField;

    private JButton okButton;
    private JButton cancelButton;


    public LoadTopoDialog() {
        super(MainWindow.getInstance(), "Load a topology", true);
        setupUI();
        pack();
    }


    private void setupUI() {
        setLayout(new BorderLayout());

        JPanel filePanel;
        JPanel optionsPanel;
        JPanel buttonsPanel;

        /* build file panel */

        filePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        filePanel.setBorder(BorderFactory.createTitledBorder("Topology file name:"));
        String pathName;
        try {
            pathName = lastFile.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            pathName = lastFile.getAbsolutePath();
        }
        inputFileField = new JTextField(pathName, 30);
        JButton browseBtn = new JButton("Browse...");
        browseBtn.addActionListener(new BrowseActionListener(this));

        filePanel.add(inputFileField);
        filePanel.add(browseBtn);

        /* build options panel */

        optionsPanel = new JPanel(new GridLayout(0, 1, 10, 5));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
        removeMultipleLinks = new JCheckBox("Remove multiple links");
        removeMultipleLinks.setToolTipText("Check if you want that multiple links between same nodes are aggregated into only one.");
        useBWSharing = new JCheckBox("Use bandwidth sharing");
        useBWSharing.setToolTipText(
                "<html>Check if you want to use bandwidth sharing for backups LSP.<br>" +
                "If you need to use backup LSPs, this options should be checked.<br>" +
                "You cannnot use preemptions if you check this.");

        optionsPanel.add(removeMultipleLinks);
        optionsPanel.add(useBWSharing);

        /* build buttons panel */

        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        okButton = new JButton("Load topology");
        cancelButton = new JButton("Cancel");

        okButton.addActionListener(new LoadActionListener(this));

        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);

        add(filePanel, BorderLayout.NORTH);
        add(optionsPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);
    }

    private class LoadActionListener implements ActionListener {
        private Component parent;

        public LoadActionListener(Component parent) {
            this.parent = parent;
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {

            new Thread(new Runnable() {
                public void run() {
                    okButton.setEnabled(false);
                    cancelButton.setEnabled(false);

                    File file = new File(inputFileField.getText());

                    if (!file.exists() || file.isDirectory()) {
                        JOptionPane.showMessageDialog(parent, "Chosen file does not exist or is a directory.", "Error", JOptionPane.ERROR_MESSAGE);
                        logger.error("Domain file does not exist or is a directory.");
                        okButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        return;
                    }

                    try {
                        InterDomainManager.getInstance().loadDomain(file.getAbsolutePath(), true, removeMultipleLinks.isSelected(), useBWSharing.isSelected());
                        lastFile = file;
                        dispose();
                    } catch (InvalidDomainException e1) {
                        e1.printStackTrace();
                        String msg = "Invalid Domain file " + (e1.getMessage() == null ? "" : (": " + e1.getMessage()));
                        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
                        logger.error(msg);
                        okButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                    } catch (DomainAlreadyExistException e1) {
                        String msg = "A domain with the same ASID already exists.";
                        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
                        logger.error(msg);
                        okButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        String msg = "An unexpected error occurs: " + e.getClass().getSimpleName();
                        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
                        logger.error(msg);
                        okButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                    }
                }
            }).start();
        }

    }

    private class BrowseActionListener implements ActionListener {
        private Container container;

        public BrowseActionListener(Container container) {
            this.container = container;
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            //create a FileChooser
            lastFile = new File(inputFileField.getText());
            File tf = (new TopoChooser()).loadTopo(container, lastFile);
            if (tf == null) //cancel button has been pressed
                return;
            inputFileField.setText(tf.getAbsolutePath());
        }
    }

}
