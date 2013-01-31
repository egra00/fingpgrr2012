package uy.edu.fing.repository.tools.CBGPDump;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.persistence.DomainFactory;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.TopoChooser;

public class CBGPDumpGUI extends JDialog {

	private static final long serialVersionUID = 1L;

	static final private Logger logger = Logger.getLogger(CBGPDumpGUI.class);

    static private File lastFile1 = new File(".");
    static private File lastFile2 = new File(".");
    private JTextField inputFileField1;
    private JTextField inputFileField2;

    private JButton okButton;
    private JButton cancelButton;


    public CBGPDumpGUI() {
        super(MainWindow.getInstance(), "Export C-BGP", true);
        setupUI();
        pack();
    }


    private void setupUI() {
        setLayout(new BorderLayout());

        JPanel  filePanel1;
        JPanel  filePanel2;
        JPanel buttonsPanel;

        /* build file panel */

        filePanel1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        filePanel1.setBorder(BorderFactory.createTitledBorder("Topology folder/file (.xml)"));
        String folder_file;
        try {
        	folder_file = lastFile1.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            folder_file = lastFile1.getAbsolutePath();
        }
        inputFileField1 = new JTextField(folder_file, 30);
        JButton folder_file_browseBtn = new JButton("Browse...");
        folder_file_browseBtn.addActionListener(new BrowseActionListener1(this));

        filePanel1.add(inputFileField1);
        filePanel1.add(folder_file_browseBtn);
        
        filePanel2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        filePanel2.setBorder(BorderFactory.createTitledBorder("MRT* file name (.tra)"));
        String mrt_file;
        try {
        	mrt_file = lastFile2.getCanonicalPath();
        } catch (IOException e) {
            e.printStackTrace();
            mrt_file = lastFile2.getAbsolutePath();
        }
        inputFileField2 = new JTextField(mrt_file, 30);
        JButton mrt_file_browseBtn = new JButton("Browse...");
        mrt_file_browseBtn.addActionListener(new BrowseActionListener2(this));

        filePanel2.add(inputFileField2);
        filePanel2.add(mrt_file_browseBtn);

        buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        okButton = new JButton("Accept");
        cancelButton = new JButton("Cancel");

        okButton.addActionListener(new AccepActionListener(this));

        cancelButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        
        add(filePanel1, BorderLayout.NORTH);
        add(filePanel2, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);
    }

    private class AccepActionListener implements ActionListener {
        private Component parent;

        public AccepActionListener(Component parent) {
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

                    File file_dir = new File(inputFileField1.getText());

                    if(file_dir.isDirectory()) {	
                        
                    	lastFile1 = file_dir;
                    	 
                    	for(File file: file_dir.listFiles()) {
                    		if(file.getAbsolutePath().endsWith(".xml")) {
								 try {
								 	Domain domain = DomainFactory.loadDomain(file.getAbsolutePath(), true, false);
								 	CBGPDumpAlgorithm cbgp= new CBGPDumpAlgorithm();
								 	
								 	String mrt_file = inputFileField2.getText();
								 	File file2 = new File(mrt_file);
									if(!file2.exists() || file2.isDirectory()) {
										mrt_file = null;
									}
									else {
										 lastFile2 = file2;
									}
								
								 	cbgp.run(domain, mrt_file);
								
								 } catch (InvalidDomainException e1) {
									 String msg = "An unexpected error occurs: " + e1.getClass().getSimpleName();
								     logger.error(msg);
								     okButton.setEnabled(true);
								     cancelButton.setEnabled(true);
								 } catch (Exception e2) {
								     String msg = "An unexpected error occurs: " + e2.getClass().getSimpleName();
								     logger.error(msg);
								     okButton.setEnabled(true);
								     cancelButton.setEnabled(true);
								 }
                    		}
                    	}
                    	
                    	dispose();
                    	
                    }
                    else if(!file_dir.exists()) {
                    	JOptionPane.showMessageDialog(parent, "Chosen file does not exist or is a directory.", "Error", JOptionPane.ERROR_MESSAGE);
                        logger.error("Domain file does not exist or is a directory.");
                        okButton.setEnabled(true);
                        cancelButton.setEnabled(true);
                        return;
                    }
                    else {
                        try {
                        	Domain domain = DomainFactory.loadDomain(file_dir.getAbsolutePath(), true, false);
                        	CBGPDumpAlgorithm cbgp= new CBGPDumpAlgorithm();
                        	
                        	String mrt_file = inputFileField2.getText();
                        	File file2 = new File(mrt_file);
                        	if(!file2.exists() || file2.isDirectory()) {
                        		mrt_file = null;
                        	}
                        	else {
                        		 lastFile2 = file2;
                        	}

                        	cbgp.run(domain, mrt_file);

                            lastFile1 = file_dir;
                            dispose();
                        } catch (InvalidDomainException e1) {
                            e1.printStackTrace();
                            String msg = "Invalid Domain file " + (e1.getMessage() == null ? "" : (": " + e1.getMessage()));
                            logger.error(msg);
                            okButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                        } catch (Exception e) {
                            e.printStackTrace();
                            String msg = "An unexpected error occurs: " + e.getClass().getSimpleName();
                            logger.error(msg);
                            okButton.setEnabled(true);
                            cancelButton.setEnabled(true);
                        }
                    }
                }
            }).start();
        }

    }

    private class BrowseActionListener1 implements ActionListener {
        private Container container;

        public BrowseActionListener1(Container container) {
            this.container = container;
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            //create a FileChooser
            lastFile1 = new File(inputFileField1.getText());
            File tf1 = (new TopoChooser()).loadTopo(container, lastFile1);
            if (tf1 == null) //cancel button has been pressed
                return;

            inputFileField1.setText(tf1.getAbsolutePath());
        }
    }
    
    
    private class BrowseActionListener2 implements ActionListener {
        private Container container;

        public BrowseActionListener2(Container container) {
            this.container = container;
        }

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            //create a FileChooser
            lastFile2 = new File(inputFileField2.getText());
            File tf2 = (new TopoChooser()).loadTopo(container, lastFile2);
            if (tf2 == null) //cancel button has been pressed
                return;

            inputFileField2.setText(tf2.getAbsolutePath());
        }
    }

}

