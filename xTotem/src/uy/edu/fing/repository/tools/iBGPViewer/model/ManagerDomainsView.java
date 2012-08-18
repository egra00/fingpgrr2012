package uy.edu.fing.repository.tools.iBGPViewer.model;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import uy.edu.fing.repository.tools.iBGPViewer.ManagerGUIViewer;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;

public class ManagerDomainsView {
	private int _key;
	private static ManagerDomainsView handler = null;

	private JDialog dialog = null;
	private ButtonGroup btGroup = null;


	private ManagerDomainsView() 
	{
	}

    public static ManagerDomainsView getInstance() {
        if (handler == null)
            handler = new ManagerDomainsView();
        return handler;
    }

	private JPanel setupUI() {
		JPanel generalPanel = new JPanel();
        btGroup = new ButtonGroup();

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

    	for (ManagerGUIViewer.Data data : ManagerGUIViewer.getInstance().getMapConfigurations().values()) {
			generalPanel.add(new DomainPanel(data), c);
		}
		return generalPanel;
	}


    public void show(int key) {
    	_key = key; 	
        createPanel();
        dialog.setVisible(true);
    }

    public void hide() {
        if (dialog != null) dialog.dispose();
    }


	private void createPanel() {
        JScrollPane jsc = new JScrollPane(setupUI());
        if (dialog == null) {
            dialog = new JDialog(ManagerGUIViewer.getInstance().getMapFrames().get(_key), "Domains currently loaded");
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


	class DomainPanel extends JPanel {
		
		private static final long serialVersionUID = 7512329941803417113L;
		//private JButton removeDomainBtn = null;
		private JButton disassociateBtn = null;

		public DomainPanel(ManagerGUIViewer.Data data) {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            JRadioButton btn = new JRadioButton("ASID: " + data.domain.getASID());
			this.add(btn);
            btn.setSelected(ManagerGUIViewer.getInstance().getMapFrames().get(_key).getIbgp_key() == data.key);
			btGroup.add(btn);
            btn.addActionListener(new SelectDomainListener(data.key));

			this.add(new JLabel(data.description));

          /*  removeDomainBtn = new JButton("Remove Domain");
			removeDomainBtn.addActionListener(new RemoveDomainListener(data.key));
			this.add(removeDomainBtn);*/
			
			disassociateBtn = new JButton("Disassociate Domain");
			disassociateBtn.addActionListener(new DisassociateDomainListener(data.key));
			this.add(disassociateBtn);

            this.setBorder(BorderFactory.createRaisedBevelBorder());
		}
    }

	
    class DisassociateDomainListener implements ActionListener {
        private int id = 0;

        public DisassociateDomainListener(int id) {
            this.id = id;
        }

        public void actionPerformed(ActionEvent e) {
			ManagerGUIViewer.getInstance().disassociate(_key, id);
			rebuild();
        }
    }
    

    class RemoveDomainListener implements ActionListener {
        private int id = 0;

        public RemoveDomainListener(int id) {
            this.id = id;
        }

        public void actionPerformed(ActionEvent e) {
			//ManagerGUIViewer.getInstance().remove(_key, id);
			//rebuild();
        }
    }

    class SelectDomainListener implements ActionListener {
        private int ID = 0;

        public SelectDomainListener(int ID) {
            this.ID = ID;
        }

        public void actionPerformed(ActionEvent e) {
            if (((JRadioButton) e.getSource()).isSelected()) 
            {
				ManagerGUIViewer.getInstance().change(_key, ID);
            }
        }
    }
}

