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

import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManagerListener;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemAlgorithm;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

/*
* Changes:
* --------
* - 13-Dec.-2005 : automatic refresh now possible through GUIListener (GMO).
* - 12-Jan.-2006: Now react to change in the RepositoryManager, change structure (GMO).
* - 03-Feb.-2006 : change layout from GridLayout to GridBagLayout (GMO). 
*/

/**
*  Class to display and manage the started algorithm.
*
*  Singleton class, a reference to this class can be obtained by the static method getinstance().
*
* <p>Creation date: 6 d�c. 2005
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class AlgorithmHandler implements RepositoryManagerListener {

    private JDialog dialog = null;
	private static AlgorithmHandler handler = null;
    private static RepositoryManager repo = RepositoryManager.getInstance();
    private static MainWindow mainWindow = MainWindow.getInstance();
    private AlgorithmHandler() {}

     /**
     * return the instance of this class
     *
     * @return the instance of this class
     */
    public static AlgorithmHandler getInstance() {
        if (handler == null)
            handler = new AlgorithmHandler();
        return handler;
    }

    /**
     * return a newly created Panel representing all started algos
     * @return
     */
	private JPanel setupUI() {
		JPanel generalPanel = new JPanel();

        generalPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.PAGE_START;
        c.insets = new Insets(5, 0, 5, 0);

		for (TotemAlgorithm algo: repo.getAllStartedAlgos()) {
			generalPanel.add(new AlgoPanel(algo), c);
		}
		return generalPanel;
	}

    /**
     * show the dialog containing all the started algorithms
     */
	public void show() {
        createPanel();
        repo.addListener(this);
        dialog.setVisible(true);
    }

    /**
     * Hide the panel and stop listenning to RepositoryManager
     */
    public void hide() {
        if (dialog != null) dialog.dispose();
        repo.removeListener(this);
    }

    /**
     * Create the dialog
     */
    private void createPanel() {
        if (dialog == null) {
            dialog = new JDialog(mainWindow, "Algorithms currently started");
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
        } else
            dialog.setContentPane(new JScrollPane(setupUI()));
    }

    /**
     * rebild the dialog
     */
    private void rebuild() {
        if (dialog != null && dialog.isVisible()) {
            createPanel();
            dialog.validate();
        }
    }

    public void startAlgoEvent(TotemAlgorithm algo) {
        rebuild();
    }

    public void stopAlgoEvent(TotemAlgorithm algo) {
        rebuild();
    }

    /**
     * Panel representing a single started algorithm (display algorithm name, parameters. Permit stopping the algorithm)
     */
    class AlgoPanel extends JPanel {
        private TotemAlgorithm algo = null;
        private JButton stopAlgoBtn = null;

        public AlgoPanel(TotemAlgorithm algo) {
			this.algo = algo;
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JLabel algoname = new JLabel("<html><b><i>" + algo.getClass().getSimpleName() + "</i></b>");
			this.add(algoname);

            if (algo.getRunningParameters() != null) {
                for (Object key : algo.getRunningParameters().keySet()) {
                    String skey = (String) key;
                    String value = (String) algo.getRunningParameters().get(key);
                    this.add(new JLabel("    " + skey + " : " + value));
                }
            }


            stopAlgoBtn = new JButton("Stop algorithm");
			stopAlgoBtn.addActionListener(new StopAlgoListener());
			this.add(stopAlgoBtn);

            this.setBorder(BorderFactory.createRaisedBevelBorder());
        }

        class StopAlgoListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                try {
                    String asId = "";
                    if (algo.getRunningParameters() != null && (asId = (String)algo.getRunningParameters().get("ASID")) != null) {
                        String tmId = "";
                        if ((tmId = (String)algo.getRunningParameters().get("TMID")) != null) {
                            repo.stopAlgorithm(algo.getClass().getSimpleName(), Integer.parseInt(asId), Integer.parseInt(tmId));
                        }
                        else repo.stopAlgorithm(algo.getClass().getSimpleName(), Integer.parseInt(asId));
                    }
                    else repo.stopAlgorithm(algo.getClass().getSimpleName());

                } catch (NoSuchAlgorithmException e1) {
                    //e1.printStackTrace();
                    mainWindow.errorMessage("Impossible to stop the algorithm: " + e1.getMessage());
                }

            }
        }


    }
}
