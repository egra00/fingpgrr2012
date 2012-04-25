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

import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ProgressBarPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.SwingWorker;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.generation.POPPOPTrafficMatrixGeneration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.io.File;

/*
* Changes:
* --------
* - 18-Oct-2007: add simrun() call (GMO)
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 18/09/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ReadClusterDialog extends JDialog {
    final private JTextField clusterFilenameTf;
    final private Domain domain;
    final private TrafficMatrixGenerationData info;

    public ReadClusterDialog(Domain domain, TrafficMatrixGenerationData info) {
        super(MainWindow.getInstance(), "BGP Measure parameters", false);
        this.domain = domain;
        this.info = info;

        clusterFilenameTf = new JTextField(30);
        if (info.isSetClusterFilename()) {
            clusterFilenameTf.setText(info.getClusterFilename());
        }
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

        contentPanel.add(new JLabel("Cluster file:"), c);
        c.gridx++;

        contentPanel.add(clusterFilenameTf, c);
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
            String domainURI = domain.getURI().getPath();

            JFileChooser fChooser = new JFileChooser(domainURI);
            int val = fChooser.showOpenDialog(MainWindow.getInstance());

            if (val == JFileChooser.APPROVE_OPTION) {
                String filename = fChooser.getSelectedFile().getAbsolutePath();
                clusterFilenameTf.setText(filename);
            } else {
                return;
            }

        }
    }

    private class AcceptActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            if (!info.isSetBGPRibDir() || !info.isSetBGPRibFileSuffix()) {
                MainWindow.getInstance().errorMessage("Please select BGP directories first");
                return;
            }

            File f = new File(clusterFilenameTf.getText());
            if (!f.exists()) {
                MainWindow.getInstance().errorMessage("Specified cluster file does not exists");
                return;
            }

            ProgressBarPanel progressBar = new ProgressBarPanel(0, 100);
            progressBar.setMessage("It can take several minutes for big domains");
            progressBar.setCancelable(false);

            progressBar.getProgressBar().setIndeterminate(true);
            progressBar.getProgressBar().setSize(500, 60);

            final JDialog pDialog = MainWindow.getInstance().showDialog(progressBar, "Generating intra domain TM ...");
            pDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);


            final SwingWorker sw = new SwingWorker() {
                Exception error = null;
                HashMap<String, String> clusters = null;

                public Object construct() {
                    try {
                        POPPOPTrafficMatrixGeneration tmGen = new POPPOPTrafficMatrixGeneration(domain);
                        clusters = tmGen.readCluster(clusterFilenameTf.getText(), info.getBGPRibDir(), info.getBGPRibFileSuffix());
                        tmGen.simRun();
                        return clusters;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        error = e1;
                        return null;
                    }
                }

                public void finished() {
                    pDialog.dispose();
                    if (error != null) {
                        MainWindow.getInstance().errorMessage("Impossible to read cluster file: " + error.getMessage());
                        return;
                    }
                    info.setPrefixesMap(clusters);
                    dispose();
                    JOptionPane.showMessageDialog(MainWindow.getInstance(), "Cluster file read and CBGP feeded. See output for potential errors.", "Information", JOptionPane.INFORMATION_MESSAGE);
                }
            };
            sw.start();
        }
    }

}
