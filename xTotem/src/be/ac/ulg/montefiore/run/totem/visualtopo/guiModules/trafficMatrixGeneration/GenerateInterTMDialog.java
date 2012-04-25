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
import be.ac.ulg.montefiore.run.totem.trafficMatrix.generation.InterDomainTrafficMatrixGeneration;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidFileException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.TrafficMatrixFile;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.ProgressBarPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.TopoChooser;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.SwingWorker;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/*
* Changes:
* --------
* - 20-Sept-2007: Use BGPInfoChecker class, use TrafficMatrixGenerationData
*/

/**
 * <Replace this by a description of the class>
 * <p/>
 * <p>Creation date: 27/06/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */

public class GenerateInterTMDialog extends JDialog {
    final static private Logger logger = Logger.getLogger(GenerateInterTMDialog.class);

    final private JTextField netFlowDirTf;
    final private JTextField netFlowSuffixTf;
    final private JTextField matrixFileTf;
    final private JTextField samplingRateTf;
    final private JTextField minutesTf;
    final private JCheckBox saveMatrixChk;
    final private JCheckBox useRawChk;

    final InterDomainTrafficMatrixGeneration tmGen;

    final TrafficMatrixGenerationData info;

    public GenerateInterTMDialog(InterDomainTrafficMatrixGeneration tmGen, TrafficMatrixGenerationData info) {
        super(MainWindow.getInstance(), "NetFlow parameters", false);
        netFlowDirTf = new JTextField(30);
        netFlowSuffixTf = new JTextField(30);
        saveMatrixChk = new JCheckBox("Save matrix:");
        useRawChk = new JCheckBox("Use raw units");
        matrixFileTf = new JTextField(30);
        samplingRateTf = new JTextField(10);
        minutesTf = new JTextField(10);
        this.tmGen = tmGen;
        this.info = info;
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

        contentPanel.add(new JLabel("NetFlow base directory:"), c);
        c.gridy++;
        contentPanel.add(new JLabel("NetFlow file suffix:"), c);
        c.gridy++;
        saveMatrixChk.setSelected(false);
        saveMatrixChk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                matrixFileTf.setEnabled(saveMatrixChk.isSelected());
            }
        });
        matrixFileTf.setEnabled(false);
        contentPanel.add(saveMatrixChk, c);

        c.gridy = 0;
        c.gridx++;
        c.weightx = 1.0;

        contentPanel.add(netFlowDirTf, c);
        c.gridy++;
        contentPanel.add(netFlowSuffixTf, c);
        c.gridy++;
        contentPanel.add(matrixFileTf, c);

        c.gridy = 0;
        c.gridx++;
        c.weightx = 0.0;

        JButton browse1 = new JButton("Browse...");
        browse1.addActionListener(new NetFlowDirBrowseActionListener());
        JButton browse2 = new JButton("Browse...");
        browse2.addActionListener(new NetFlowFileSuffixBrowseActionListener());
        JButton browse3 = new JButton("Browse...");
        browse3.addActionListener(new MatrixFileSuffixBrowseActionListener());

        contentPanel.add(browse1, c);
        c.gridy++;
        contentPanel.add(browse2, c);
        c.gridy++;
        contentPanel.add(browse3, c);

        JPanel samplingPanel = new JPanel(new GridBagLayout());
        samplingPanel.setBorder(BorderFactory.createTitledBorder("Sampling parameters"));
        GridBagConstraints innerC = new GridBagConstraints();
        innerC.gridx = 0;
        innerC.gridy = 0;
        innerC.fill = GridBagConstraints.BOTH;
        innerC.weightx = 1.0;
        innerC.weighty = 1.0;
        innerC.insets = new Insets(5, 5, 5, 5);

        samplingPanel.add(new JLabel("Sampling Rate:"), innerC);
        innerC.gridy++;
        samplingPanel.add(new JLabel("Minutes:"), innerC);
        innerC.gridy = 0;
        innerC.gridx++;
        samplingRateTf.setText("100");
        samplingPanel.add(samplingRateTf, innerC);
        innerC.gridy++;
        minutesTf.setText("5");
        samplingPanel.add(minutesTf, innerC);

        c.gridy++;
        c.gridwidth = 3;
        c.gridx = 0;
        useRawChk.setSelected(false);
        useRawChk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                samplingRateTf.setEnabled(!useRawChk.isSelected());
                minutesTf.setEnabled(!useRawChk.isSelected());
            }
        });
        contentPanel.add(useRawChk, c);
        c.gridy++;
        contentPanel.add(samplingPanel, c);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        JButton acceptButton = new JButton("Accept");
        acceptButton.addActionListener(new AcceptActionListener());
        buttonPanel.add(acceptButton);

        getRootPane().setLayout(new BorderLayout());
        getRootPane().add(contentPanel, BorderLayout.CENTER);
        getRootPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private boolean checkNetFlowDir() {
        try {
            BGPInfoChecker.isIdNaming(tmGen.getDomain(), netFlowDirTf.getText());
            return true;
        } catch (InvalidFileException e) {
            return false;
        }
    }

    private boolean checkNetFlowSuffix() {
        boolean idNaming;
        try {
            idNaming = BGPInfoChecker.isIdNaming(tmGen.getDomain(), netFlowDirTf.getText());
        } catch (InvalidFileException e) {
            return false;
        }

        Domain domain = tmGen.getDomain();
        Node sampleNode = domain.getAllNodes().get(0);
        String id = idNaming ? sampleNode.getId() : sampleNode.getRid();
        File f = new File(netFlowDirTf.getText() + File.separator + id + File.separator + netFlowSuffixTf.getText());

        return f.exists() && f.isFile();
    }

    private class AcceptActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (!checkNetFlowDir()) {
                MainWindow.getInstance().errorMessage("Error in NetFlow directory.");
                return;
            }
            if (!checkNetFlowSuffix()) {
                MainWindow.getInstance().errorMessage("Error in NetFlow suffix.");
                return;
            }

            if (!useRawChk.isSelected()) {
                try {
                    final int minutes = Integer.parseInt(minutesTf.getText());
                    final int samplingRate = Integer.parseInt(samplingRateTf.getText());
                    tmGen.setSamplingParams(minutes, samplingRate);
                } catch (NumberFormatException ex) {
                    MainWindow.getInstance().errorMessage(ex);
                    return;
                }
            } else tmGen.useRaw();

            final ProgressBarPanel progressBar = new ProgressBarPanel(0, 100);
            progressBar.setMessage("It can take several minutes for big Domains");
            progressBar.setCancelable(false);
            progressBar.getProgressBar().setIndeterminate(true);
            progressBar.getProgressBar().setSize(500, 60);

            final JDialog pDialog = MainWindow.getInstance().showDialog(progressBar, "Generating Inter Domain traffic matrix ...");
            pDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            SwingWorker sw = new SwingWorker() {
                boolean ok = false;

                public Object construct() {
                    final String[] suf = {""};
                    TrafficMatrixFile tmFile = null;

                    try {
                        if (saveMatrixChk.isSelected())
                            tmFile = tmGen.generateXMLTrafficMatrixfromNetFlow(netFlowDirTf.getText(), netFlowSuffixTf.getText(), suf, matrixFileTf.getText());
                        else tmFile = tmGen.generateXMLTrafficMatrixfromNetFlow(netFlowDirTf.getText(), netFlowSuffixTf.getText(), suf);
                        ok = true;
                    } catch (Exception e1) {
                        e1.printStackTrace();
                        logger.error("Unable to generate inter TM.");
                    }
                    info.setInterTm(tmFile);
                    return tmFile;
                }

                public void finished() {
                    pDialog.dispose();
                    if (ok) {
                        setVisible(false);
                        if (saveMatrixChk.isSelected())
                            JOptionPane.showMessageDialog(MainWindow.getInstance(), "TrafficMatrix created and saved as " + matrixFileTf.getText());
                        else JOptionPane.showMessageDialog(MainWindow.getInstance(), "TrafficMatrix created.");
                    } else {
                        MainWindow.getInstance().errorMessage("Error while generating matrix.");
                    }

                }
            };

            sw.start();

        }
    }

    private class NetFlowDirBrowseActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            String domainURI = tmGen.getDomain().getURI().getPath();

            JFileChooser fChooser = new JFileChooser(domainURI);
            fChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int val = fChooser.showOpenDialog(MainWindow.getInstance());

            if (val == JFileChooser.APPROVE_OPTION) {
                String dirname = fChooser.getSelectedFile().getAbsolutePath();
                netFlowDirTf.setText(dirname);
            } else {
                return;
            }
        }
    }

    private class NetFlowFileSuffixBrowseActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            JFileChooser fChooser = new JFileChooser(netFlowDirTf.getText());
            int val = fChooser.showOpenDialog(MainWindow.getInstance());

            if (val == JFileChooser.APPROVE_OPTION) {
                String filename = fChooser.getSelectedFile().getAbsolutePath();
                if (filename.startsWith(netFlowDirTf.getText())) {
                    String suffix = filename.substring(netFlowDirTf.getText().length() + 1);
                    suffix = suffix.substring(suffix.indexOf(File.separator) + 1);
                    netFlowSuffixTf.setText(suffix);
                } else {
                    netFlowSuffixTf.setText("Please set NetFlow dir first.");
                }
            } else {
                return;
            }

        }
    }

    private class MatrixFileSuffixBrowseActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            TopoChooser topoC = new TopoChooser();

            File f = topoC.saveTopo(GenerateInterTMDialog.this);
            if (f == null) return;

            matrixFileTf.setText(f.getAbsolutePath());
        }
    }
}
