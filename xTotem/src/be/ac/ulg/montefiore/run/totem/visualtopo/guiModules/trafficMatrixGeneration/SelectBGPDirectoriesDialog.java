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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.trafficMatrixGeneration;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.bgp.BGPInfoChecker;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidFileException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/*
* Changes:
* --------
*
*/

/**
 * <Replace this by a description of the class>
 * <p/>
 * <p>Creation date: 27/06/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class SelectBGPDirectoriesDialog extends JDialog {
    final static private Logger logger = Logger.getLogger(SelectBGPDirectoriesDialog.class);

    final private JTextField BGPDirTf;
    final private JTextField RibFileSuffixTf;

    final private TrafficMatrixGenerationData info;

    final private Domain domain;

    public SelectBGPDirectoriesDialog(Domain domain, TrafficMatrixGenerationData info) {
        super(MainWindow.getInstance(), "BGP Measure parameters", false);
        BGPDirTf = new JTextField(30);
        RibFileSuffixTf = new JTextField(30);
        this.info = info;
        if (info.isSetBGPRibDir()) BGPDirTf.setText(info.getBGPRibDir());
        if (info.isSetBGPRibFileSuffix()) RibFileSuffixTf.setText(info.getBGPRibFileSuffix());
        this.domain = domain;
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

        contentPanel.add(new JLabel("BGP base directory:"), c);
        c.gridy++;
        contentPanel.add(new JLabel("Rib file suffix:"), c);

        c.gridy = 0;
        c.gridx++;
        c.weightx = 1.0;

        contentPanel.add(BGPDirTf, c);
        c.gridy++;
        contentPanel.add(RibFileSuffixTf, c);

        c.gridy = 0;
        c.gridx++;
        c.weightx = 0.0;

        JButton browse1 = new JButton("Browse...");
        browse1.addActionListener(new BGPDirBrowseActionListener());
        JButton browse3 = new JButton("Browse...");
        browse3.addActionListener(new RibFileSuffixBrowseActionListener());

        contentPanel.add(browse1, c);
        c.gridy++;
        contentPanel.add(browse3, c);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton acceptButton = new JButton("Accept");
        acceptButton.addActionListener(new AcceptActionListener());
        buttonPanel.add(acceptButton);

        getRootPane().setLayout(new BorderLayout());
        getRootPane().add(contentPanel, BorderLayout.CENTER);
        getRootPane().add(buttonPanel, BorderLayout.SOUTH);

    }

    private boolean checkRibSuffix(boolean idNaming) {
        Node sampleNode = domain.getAllNodes().get(0);
        String id = idNaming ? sampleNode.getId() : sampleNode.getRid();
        File f = new File(BGPDirTf.getText() + File.separator + id + File.separator + RibFileSuffixTf.getText());

        return BGPInfoChecker.isMRTFile(f.getAbsolutePath());
    }

    private class AcceptActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            boolean idNaming;
            try {
                idNaming = BGPInfoChecker.isIdNaming(domain, BGPDirTf.getText());
            } catch (InvalidFileException e1) {
                MainWindow.getInstance().errorMessage("Error in BGP directory.");
                return;
            }
            if (!checkRibSuffix(idNaming)) {
                MainWindow.getInstance().errorMessage("Error in Rib suffix.");
                return;
            }

            info.setBGPRibDir(BGPDirTf.getText());
            info.setBGPRibFileSuffix(RibFileSuffixTf.getText());
            info.setIdNaming(idNaming);
            dispose();
        }
    }

    private class BGPDirBrowseActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String domainURI = domain.getURI().getPath();

            JFileChooser fChooser = new JFileChooser(domainURI);
            fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int val = fChooser.showOpenDialog(MainWindow.getInstance());

            if (val == JFileChooser.APPROVE_OPTION) {
                String dirname = fChooser.getSelectedFile().getAbsolutePath();
                BGPDirTf.setText(dirname);
            } else {
                return;
            }
        }
    }

    private class RibFileSuffixBrowseActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JFileChooser fChooser = new JFileChooser(BGPDirTf.getText());
            int val = fChooser.showOpenDialog(MainWindow.getInstance());

            if (val == JFileChooser.APPROVE_OPTION) {
                String filename = fChooser.getSelectedFile().getAbsolutePath();
                if (filename.startsWith(BGPDirTf.getText())) {
                    String suffix = filename.substring(BGPDirTf.getText().length() + 1);
                    suffix = suffix.substring(suffix.indexOf(File.separator) + 1);
                    RibFileSuffixTf.setText(suffix);
                } else {
                    RibFileSuffixTf.setText("Please set BGP dir first.");
                }
            } else {
                return;
            }
        }
    }
}
