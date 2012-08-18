package uy.edu.fing.repository.tools.iBGPViewer.model;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

public class MyJFrame extends JFrame 
{
	private static final long serialVersionUID = -3184088561047086230L;
	private int _key;
	private int ibgp_key;
	
	public MyJFrame(int id)
	{
		set_key(id);
	}

	public int get_key() {
		return _key;
	}

	public void set_key(int _key) {
		this._key = _key;
	}
	
	
	public AbstractAction Accion_CTRLD()
	{
		return new AbstractAction() { 
			public void actionPerformed(ActionEvent e) 
			{ 
				//System.out.println("//// KEY " + _key); 
				ManagerDomainsView.getInstance().show(_key);
			} 
		};
	}
	
	
	public AbstractAction Accion_CTRLK()
	{
		return new AbstractAction() { 
			public void actionPerformed(ActionEvent e) 
			{ 
			} 
		};
	}

	public int getIbgp_key() {
		return ibgp_key;
	}

	public void setIbgp_key(int ibgp_key) {
		this.ibgp_key = ibgp_key;
	}
	
}
