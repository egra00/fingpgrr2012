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

import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManagerListener;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.domainTables.TrafficMatrixTable;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManagerListener;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;
import java.util.List;

/*
 * Changes:
 * --------
 * - 13-Dec.-2005 : add the automatic refresh capability (GMO).
 * - 13-Dec.-2005 : react to domain change and add view traffic matrix button. (GMO)
 * - 12-Jan.-2006 : Listen to traffic matrix and inter domain events, change structure. (GMO)
 * - 10-Feb.-2006 : Change layout. (GMO)
 * - 23-Oct-2006 : add button to save the traffic matrix to a file. (GMO)
 * - 23-Apr-2007 : add edit matrix capability (GMO)
 * - 09-Aug-2007 : fix bug when disposing traffic matrix editor dialog (GMO)
 */

/**
 * This class is responsible for managing the allready loaded traffic matrix (ie. select the active matrix, remove
 * unnecessary ones, ... )
 *
 * @author Olivier Materne (O.Materne@student.ulg.ac.be)
 */
public class RMguiMatrixHandler implements TrafficMatrixManagerListener, InterDomainManagerListener {
    private int nbManagedMatrices = 0;
    private List<Integer> managedMatrices = null;
    private int domainAsId = -1;

    private static RMguiMatrixHandler handler = null;
    private static final TrafficMatrixManager matrixManager = TrafficMatrixManager.getInstance();
    private static final GUIManager guiManager = GUIManager.getInstance();;
    private static final MainWindow mainWindow = MainWindow.getInstance();
    private static final InterDomainManager idm = InterDomainManager.getInstance();
    private JDialog dialog = null;

    private Domain domain = null;


    /**
     * A private constructor
     */
    private RMguiMatrixHandler() {
    }


    /**
     * return the instance of this class
     *
     * @return the instance of this class
     */
    public static RMguiMatrixHandler getInstance() {
        if (handler == null)
            handler = new RMguiMatrixHandler();
        return handler;
    }


    /**
     * Display a dialog that lists the managed matrices
     */
    public void show() {
        createPanel();
        matrixManager.addListener(this);
        idm.addListener(this);
        dialog.setVisible(true);
    }

    /**
     * Free the dialog and stop reacting to change
     */
    public void hide() {
        if (dialog != null) dialog.dispose();
        matrixManager.removeListener(this);
        idm.removeListener(this);
    }


    /**
     * return a panel that lists the managed matrices
     *
     * @return return a panel that lists the managed matrices
     */
    private JPanel setupUI() {
        final JPanel panel1 = new JPanel();

        panel1.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.PAGE_START;
        c.insets = new Insets(5, 0, 5, 0);


        ButtonGroup buttonGroup = new ButtonGroup();
        nbManagedMatrices = managedMatrices.size();

        //add a panel element for each managed Traffic Matrix
        for (int i = 0; i < nbManagedMatrices; i++) {
            int tmid = managedMatrices.get(i).intValue();
            final JPanel panel2 = new JPanel();
            panel2.setLayout(new BorderLayout());
            panel1.add(panel2, c);
            panel2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), null));
            JPanel buttonPanel = new JPanel(new FlowLayout());
            JPanel centerPanel = new JPanel();
            centerPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 5, 0));
            panel2.add(centerPanel, BorderLayout.CENTER);
            panel2.add(buttonPanel, BorderLayout.SOUTH);
            final JRadioButton radioButton1 = new JRadioButton();
            radioButton1.setText("TM id:");
            buttonGroup.add(radioButton1);
            try {
                if (matrixManager.getDefaultTrafficMatrixID() == tmid)
                    radioButton1.setSelected(true);
            } catch (InvalidTrafficMatrixException e) {
            }
            radioButton1.addActionListener(new SelectMatrixListener(tmid));
            radioButton1.setToolTipText("Select this button allow to set this matrix as the default one. Any further routing action will then be done using it. ");
            centerPanel.add(radioButton1);
            final JTextField textField1 = new JTextField();
            textField1.setColumns(20);
            textField1.setEditable(false);
            textField1.setText(managedMatrices.get(i).toString());
            centerPanel.add(textField1);

            final JButton button1 = new JButton();
            button1.setText("Edit Matrix");
            button1.addActionListener(new ShowMatrixListener(tmid));
            buttonPanel.add(button1);
            final JButton button2 = new JButton();
            button2.setText("Remove Matrix");
            button2.addActionListener(new RemoveMatrixListener(tmid));
            buttonPanel.add(button2);
            final JButton button3 = new JButton();
            button3.setText("Save Matrix");
            button3.addActionListener(new SaveTrafficMatrixListener(tmid));
            buttonPanel.add(button3);
        }

        return panel1;
    }

    /**
     * create the dialog
     */
    public void createPanel() {
        managedMatrices = guiManager.getManagedMatrices();
        domain = guiManager.getCurrentDomain();
        domainAsId = (domain == null) ? -1 : guiManager.getCurrentDomain().getASID();
        if (dialog == null) {
            dialog = new JDialog(mainWindow, "Matrices currently in GUI");
            dialog.setContentPane(new JScrollPane(setupUI()));
            dialog.setSize(400, 250);
            dialog.addWindowListener(new WindowListener() {
                public void windowOpened(WindowEvent e) {}
                public void windowClosing(WindowEvent e) {
                    hide();
                }
                public void windowClosed(WindowEvent e) {}
                public void windowIconified(WindowEvent e) {}
                public void windowDeiconified(WindowEvent e) {}
                public void windowActivated(WindowEvent e) {}
                public void windowDeactivated(WindowEvent e) {}
            });
        }
        else {
            dialog.setContentPane(new JScrollPane(setupUI()));
        }
    }

    /**
     * rebuild the dialog content
     */
    private void rebuild() {
        if (dialog != null && dialog.isVisible()) {
            createPanel();
            dialog.validate();
        }
    }

    public void addTrafficMatrixEvent(TrafficMatrix tm, int tmId) {
        rebuild();
    }

    public void removeTrafficMatrixEvent(TrafficMatrix tm, int tmId) {
        rebuild();
    }

    public void changeDefaultTrafficMatrixEvent(int asId, TrafficMatrix tm) {
        if (domain != null && asId == domain.getASID()) rebuild();
    }

    public void addDomainEvent(Domain domain) {
    }

    public void removeDomainEvent(Domain domain) {
    }

    public void changeDefaultDomainEvent(Domain domain) {
        rebuild();
    }

    public void editTrafficMatrix(TrafficMatrix tm) {
        int tmid = 0;
        try {
            tmid = tm.getTmId();
        } catch (TrafficMatrixIdException e) {
            e.printStackTrace();
        }
        final TrafficMatrixTable matrix = new TrafficMatrixTable(tm, tmid);
        matrix.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        matrix.setEditable();
        int asId = tm.getASID();
        JDialog dialog = new JDialog(mainWindow, "Traffic Matrix " + tmid + " of domain " + asId, false) {
            public void dispose() {
                super.dispose();
                matrix.destroy();
            }
        };
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setContentPane(new JScrollPane(matrix));
        mainWindow.showDialog(dialog);
    }

    /**
     * Listener used to display a traffic matrix
     */ 
    class ShowMatrixListener implements ActionListener {
        private int tmid = 0;

        public ShowMatrixListener(int tmid) {
            this.tmid = tmid;
        }

        public void actionPerformed(ActionEvent e) {
            try {
                TrafficMatrix tm = TrafficMatrixManager.getInstance().getTrafficMatrix(domainAsId, tmid);
                editTrafficMatrix(tm);
            } catch (InvalidTrafficMatrixException ex) {
                mainWindow.errorMessage("Cannot display traffic matrix");
            }
        }
    }


    /**
     * Listner used to handle remove matrices demands
     */
    class RemoveMatrixListener implements ActionListener {
        private int tmid = 0;

        public RemoveMatrixListener(int tmid) {
            this.tmid = tmid;
        }

        public void actionPerformed(ActionEvent e) {
            guiManager.removeTrafficMatrix(tmid);
        }
    }

    class SaveTrafficMatrixListener implements ActionListener {
        private int tmid;

        public SaveTrafficMatrixListener(int tmid) {
            this.tmid = tmid;
        }

        public void actionPerformed(ActionEvent e) {
            guiManager.saveTrafficMatrix(tmid);
        }
    }


    /**
     * Listner used to change the active matrices
     */
    class SelectMatrixListener implements ActionListener {
        private int tmid = 0;

        public SelectMatrixListener(int tmid) {
            this.tmid = tmid;
        }

        public void actionPerformed(ActionEvent e) {
            if (((JRadioButton) e.getSource()).isSelected()) {
                try {
                    matrixManager.setDefaultTrafficMatrix(tmid);
                } catch (InvalidTrafficMatrixException ex) {
                }

            }
        }
    }
}
