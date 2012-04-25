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
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManagerListener;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.visualtopo.facade.GUIManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

/*
* Changes:
* --------
* - 13-Dec.-2005 : automatic refresh now possible (GMO).
* - 12-Jan.-2006 : now react to the change in the loaded domains (GMO).
* - 03-Feb.-2006 : change layout from GridLayout to GridBagLayout (GMO). 
*/

/**
*  Class to display and manage the loaded domains.
*
*  Singleton class, a reference to this class can be obtained by the static method getinstance().
*
* <p>Creation date: 6 d�c. 2005
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class DomainHandler implements InterDomainManagerListener {
	private static MainWindow mainWindow = MainWindow.getInstance();
	private static InterDomainManager idm = InterDomainManager.getInstance();
	private static DomainHandler handler = null;

	private JDialog dialog = null;

	//private JPanel panel = null;
	private ButtonGroup btGroup = null;

	/**
	 * Empty private constructor
	 *
	 */
	private DomainHandler() {}
	
    /**
     * return the instance of this class
     *
     * @return the instance of this class
     */
    public static DomainHandler getInstance() {
        if (handler == null)
            handler = new DomainHandler();
        return handler;
    }

    /**
     * return the panel
     * @return
     */
	private JPanel setupUI() {
		JPanel generalPanel = new JPanel();
        btGroup = new ButtonGroup();

        /*
        generalPanel.setLayout(new GridLayout(0, 1));

		for (Domain domain : idm.getAllDomains()) {
			generalPanel.add(new DomainPanel(domain));
		}
        */
        
        //TODO: find a good layout

		generalPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridheight = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.PAGE_START;
        c.insets = new Insets(5, 0, 5, 0);

    	for (Domain domain : idm.getAllDomains()) {
			generalPanel.add(new DomainPanel(domain), c);
		}


        /*
        generalPanel.setLayout(new BoxLayout(generalPanel, BoxLayout.Y_AXIS));
		for (Domain domain : idm.getAllDomains()) {
			generalPanel.add(new DomainPanel(domain));
		}
        */

		return generalPanel;
	}

    /**
     * Show the dialog
     */
    public void show() {
        createPanel();
        idm.addListener(this);
        dialog.setVisible(true);
    }

    /**
     * Dispose the dialog and stop listennig to the loaded domains changements
     */
    public void hide() {
        if (dialog != null) dialog.dispose();
        idm.removeListener(this);
    }

    /**
     * Build the dialog containing the loaded domains
     */
	private void createPanel() {
        JScrollPane jsc = new JScrollPane(setupUI());
        if (dialog == null) {
            dialog = new JDialog(mainWindow, "Domains currently loaded");
            dialog.setContentPane(jsc);
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
        } else {
            dialog.setContentPane(jsc);
        }
    }


    private void rebuild() {
        if (dialog != null && dialog.isVisible()) {
            createPanel();
            dialog.validate();
        }
    }

    public void addDomainEvent(Domain domain) {
        rebuild();
    }

    public void removeDomainEvent(Domain domain) {
        rebuild();
    }

    public void changeDefaultDomainEvent(Domain domain) {
        rebuild();
    }


    /**
     * Visual representation of a single domain. Display ASID, description and permit removing (unloading).
     */
	class DomainPanel extends JPanel {
		
		private JButton removeDomainBtn = null;

		public DomainPanel(Domain domain) {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JRadioButton btn = new JRadioButton("ASID : " + domain.getASID());
			this.add(btn);
            btn.setSelected(idm.getDefaultDomain() != null ? idm.getDefaultDomain().getASID() == domain.getASID() : false);
			btGroup.add(btn);
            btn.addActionListener(new SelectDomainListener(domain.getASID()));

			this.add(new JLabel(domain.getDescription()));

            removeDomainBtn = new JButton("Remove Domain");
			removeDomainBtn.addActionListener(new RemoveDomainListener(domain.getASID()));
			this.add(removeDomainBtn);

            this.setBorder(BorderFactory.createRaisedBevelBorder());
		}
    }


    class RemoveDomainListener implements ActionListener {
        private int asid = 0;

        public RemoveDomainListener(int asid) {
            this.asid = asid;
        }

        public void actionPerformed(ActionEvent e) {
                GUIManager.getInstance().closeDomain(asid);
        }
    }

    class SelectDomainListener implements ActionListener {
        private int ASID = 0;

        public SelectDomainListener(int ASID) {
            this.ASID = ASID;
        }

        public void actionPerformed(ActionEvent e) {
            if (((JRadioButton) e.getSource()).isSelected()) {
                try {
                    idm.setDefaultDomain(ASID);
                } catch (InvalidDomainException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
