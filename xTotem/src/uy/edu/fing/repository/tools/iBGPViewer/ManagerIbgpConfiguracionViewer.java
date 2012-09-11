package uy.edu.fing.repository.tools.iBGPViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import uy.edu.fing.repository.tools.iBGPViewer.model.LocalLayout;
import uy.edu.fing.repository.tools.iBGPViewer.model.MyLink;
import uy.edu.fing.repository.tools.iBGPViewer.model.MyNode;
import uy.edu.fing.repository.tools.iBGPViewer.model.TypeMylink;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpRouterImpl;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import edu.uci.ics.jung2.algorithms.layout.Layout;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.SparseMultigraph;
import edu.uci.ics.jung2.graph.util.EdgeType;
import edu.uci.ics.jung2.visualization.VisualizationViewer;
import edu.uci.ics.jung2.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung2.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung2.visualization.decorators.ToStringLabeller;

public class ManagerIbgpConfiguracionViewer {
	
	private static ManagerIbgpConfiguracionViewer handler = null;

	private JDialog dialog;
	private ButtonGroup btGroup;
	private JFrame _frame;
	private final int MARCO = 150;
	private Image _app_icon;
	private Logger _logger;
	private int _maxX;
	private int _maxY;
	private List<Graph<MyNode, MyLink>> _list_configurations_ibgp;
	private List<Data> _list_description;
	private Map<Integer, Integer> _hash_configurations_ibgp;

	public class Data
	{
		String description;
		int id;
	}
	
	private ManagerIbgpConfiguracionViewer() 
	{
		_maxX = 750;
		_maxY = 350;
		dialog = null;
		_frame = new JFrame();
		btGroup = null;
		_app_icon = new ImageIcon(MainWindow.class.getResource("/resources/img/icon.gif")).getImage();
		_frame.setIconImage(_app_icon);
		_logger = Logger.getLogger(ManagerIbgpConfiguracionViewer.class);
		_hash_configurations_ibgp = new HashMap<Integer, Integer>();
		_list_configurations_ibgp = new LinkedList<Graph<MyNode,MyLink>>();
		_list_description = new LinkedList<Data>();
	}

    public static ManagerIbgpConfiguracionViewer getInstance() {
        if (handler == null)
            handler = new ManagerIbgpConfiguracionViewer();
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

    	for (int i=0 ; i<_list_configurations_ibgp.size(); i++) {
			generalPanel.add(new DomainPanel(i), c);
		}
		return generalPanel;
	}

	
    public void showPanel() 
    { 	
    	createPanel();
        dialog.setVisible(true);
    }
	
    public void show(Domain domain) 
    {
    	Graph<MyNode,MyLink> ibgp = toGraphPrintable(domain);
		_list_configurations_ibgp.add(0, ibgp);
		if (_hash_configurations_ibgp.get(domain.getASID()) == null)
			_hash_configurations_ibgp.put(domain.getASID(), 0);
		
		int aux = _hash_configurations_ibgp.get(domain.getASID());
		
		aux++;
		_hash_configurations_ibgp.put(domain.getASID(), aux);
		
		Data data = new Data();
		data.id = domain.getASID();
		data.description = "Configuration IBGP: "+ aux;
		_list_description.add(0, data);
		
    	showIbgp();
    	
    	rebuild();
    }
    
    
	private void createPanel() {
        JScrollPane jsc = new JScrollPane(setupUI());
        if (dialog == null) {
        	dialog = new JDialog(_frame, "Domains currently loaded");
        	dialog.setContentPane(jsc);
        	dialog.setSize(200, 250);
            dialog.addWindowListener(new WindowListener() {
                public void windowOpened(WindowEvent e) {}
                public void windowClosing(WindowEvent e) {}
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

		public DomainPanel(int _domain_pos) {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			Data data = _list_description.get(_domain_pos);
			
            JRadioButton btn = new JRadioButton("ASID: " + data.id);
			this.add(btn);
            btn.setSelected(_domain_pos == 0);
			btGroup.add(btn);
            btn.addActionListener(new SelectDomainListener(_domain_pos));

			this.add(new JLabel(data.description));

           /* removeDomainBtn = new JButton("Remove Domain");
			removeDomainBtn.addActionListener(new RemoveDomainListener(_domain_pos));
			this.add(removeDomainBtn);*/
			
			disassociateBtn = new JButton("Disassociate Domain");
			disassociateBtn.addActionListener(new DisassociateDomainListener(_domain_pos));
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
			disassociate(id);
			rebuild();
        }
    }
    

  /*  class RemoveDomainListener implements ActionListener {
        private int id = 0;

        public RemoveDomainListener(int id) {
            this.id = id;
        }

        public void actionPerformed(ActionEvent e) {
			remove(id);
			rebuild();
        }
    }*/

    class SelectDomainListener implements ActionListener {
        private int id = 0;

        public SelectDomainListener(int id) {
            this.id = id;
        }

        public void actionPerformed(ActionEvent e) {
            if (((JRadioButton) e.getSource()).isSelected()) 
            {
				change(id);
            }
        }
    }
    
  
	public void showIbgp()
	{	
		Layout<MyNode,MyLink> layout = new  LocalLayout(_list_configurations_ibgp.get(0));
		layout.setSize(new Dimension(_maxX, _maxY));
		VisualizationViewer<MyNode,MyLink> vv = new VisualizationViewer<MyNode, MyLink>(layout);
		vv.setPreferredSize(new Dimension(_maxX + MARCO, _maxY + MARCO));
		
		Transformer<MyNode, Paint> vertexPaint = new Transformer<MyNode, Paint>() {
			@Override
			public Paint transform(MyNode node) 
			{
				return (node.isRr() ?   Color.BLUE : Color.GREEN);
			}
		};
		
		Transformer<MyLink, Paint> edgeStrokeTransformer = new Transformer<MyLink, Paint>() {
	
					@Override
					public Paint transform(MyLink link) 
					{
						return (link.getType() == TypeMylink.client ? Color.BLACK : Color.RED);
					}
		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeDrawPaintTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<MyNode>());
		vv.setBackground(Color.WHITE);
		
		DefaultModalGraphMouse<MyNode, MyLink> gm = new DefaultModalGraphMouse<MyNode, MyLink>();
		gm.setMode(Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		
		KeyStroke ctrl_K = KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK);
		ActionMap mapaAccion = vv.getActionMap();
		InputMap map = vv.getInputMap(JComponent.WHEN_FOCUSED);
		
		map.put(ctrl_K , "Domains");
		mapaAccion.put("Domains", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                ManagerIbgpConfiguracionViewer.getInstance().showPanel();
            }
        });
		
		Data data = _list_description.get(0);
		
		_frame.invalidate();
		_frame.setTitle("Domain: "+ data.id + " - " +data.description);
		_frame.getContentPane().removeAll();
		_frame.getContentPane().add(vv);
		_frame.pack();
		_frame.validate();
		_frame.repaint(); 
		_frame.setVisible(true);
	}
    
	
	public void change(int _pos)
	{	
		Layout<MyNode,MyLink> layout = new  LocalLayout(_list_configurations_ibgp.get(_pos));
		layout.setSize(new Dimension(_maxX, _maxY));
		VisualizationViewer<MyNode,MyLink> vv = new VisualizationViewer<MyNode, MyLink>(layout);
		vv.setPreferredSize(new Dimension(_maxX + MARCO, _maxY + MARCO));
		
		Transformer<MyNode, Paint> vertexPaint = new Transformer<MyNode, Paint>() {
			@Override
			public Paint transform(MyNode node) 
			{
				return (node.isRr() ?   Color.BLUE : Color.GREEN);
			}
		};
		
		Transformer<MyLink, Paint> edgeStrokeTransformer = new Transformer<MyLink, Paint>() {
	
					@Override
					public Paint transform(MyLink link) 
					{
						return (link.getType() == TypeMylink.client ? Color.BLACK : Color.RED);
					}
		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeDrawPaintTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<MyNode>());
		vv.setBackground(Color.WHITE);
		
		DefaultModalGraphMouse<MyNode, MyLink> gm = new DefaultModalGraphMouse<MyNode, MyLink>();
		gm.setMode(Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		
		KeyStroke ctrl_K = KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK);
		ActionMap mapaAccion = vv.getActionMap();
		InputMap map = vv.getInputMap(JComponent.WHEN_FOCUSED);
		
		map.put(ctrl_K , "Domains");
		mapaAccion.put("Domains", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                ManagerIbgpConfiguracionViewer.getInstance().showPanel();
            }
        });
		
		
		Data data = _list_description.get(_pos);
		
		_frame.invalidate();
		_frame.setTitle("Domain: "+ data.id + " - " +data.description);
		_frame.getContentPane().removeAll();
		_frame.getContentPane().add(vv);
		_frame.pack();
		_frame.validate();
		_frame.repaint(); 
		_frame.setVisible(true);
	}
	
    
	public void disassociate(int _pos)
	{	
		Layout<MyNode,MyLink> layout = new  LocalLayout(_list_configurations_ibgp.get(_pos));
		layout.setSize(new Dimension(_maxX, _maxY));
		VisualizationViewer<MyNode,MyLink> vv = new VisualizationViewer<MyNode, MyLink>(layout);
		vv.setPreferredSize(new Dimension(_maxX + MARCO, _maxY + MARCO));
		
		Transformer<MyNode, Paint> vertexPaint = new Transformer<MyNode, Paint>() {
			@Override
			public Paint transform(MyNode node) 
			{
				return (node.isRr() ?   Color.BLUE : Color.GREEN);
			}
		};
		
		Transformer<MyLink, Paint> edgeStrokeTransformer = new Transformer<MyLink, Paint>() {
	
					@Override
					public Paint transform(MyLink link) 
					{
						return (link.getType() == TypeMylink.client ? Color.BLACK : Color.RED);
					}
		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeDrawPaintTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<MyNode>());
		vv.setBackground(Color.WHITE);
		
		DefaultModalGraphMouse<MyNode, MyLink> gm = new DefaultModalGraphMouse<MyNode, MyLink>();
		gm.setMode(Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		
		Data data = _list_description.get(_pos);

		JFrame _frame = new JFrame();
		_frame.setTitle("Domain: "+ data.id + " - " +data.description);
		_frame.setIconImage(new ImageIcon(MainWindow.class.getResource("/resources/img/icon.gif")).getImage());
		_frame.getContentPane().add(vv);
		_frame.pack();
		_frame.validate();
		_frame.repaint(); 
		_frame.setVisible(true);
	}
	
	
	public void remove(int _pos)
	{	
		
	}
    
	private Graph<MyNode, MyLink> toGraphPrintable(Domain _domain)
	{	
		
		HashMap<String, Node> nodesById = new HashMap<String, Node>();
		List<String> linksById = new LinkedList<String>();
		HashMap<String, MyNode> mynodesById = new HashMap<String, MyNode>();
		
		//Coordenadas mas alejadas del origen
		double x = 0;
		double y = 0;
		
		//Coordenadas mas cercanas al origen
		double x1 = Double.MAX_VALUE;
		double y1 = Double.MAX_VALUE;
		
		double nx = 0;
		double ny = 0;
		
		int domain_num = _domain.getASID();
		
		for(Node node : _domain.getAllNodes())
		{
			double xx = node.getLongitude();
			double yy = node.getLatitude();
			
			if (xx < 0 && Math.abs(nx) < Math.abs(xx)) nx = xx;
			
			if (yy < 0 && Math.abs(ny) < Math.abs(yy)) ny = yy;
		}	
		
		nx = Math.ceil(Math.abs(nx));
		ny = Math.ceil(Math.abs(ny));
		
		
		for(Node node : _domain.getAllNodes())
		{
			double xx = node.getLongitude() + nx;
			double yy = node.getLatitude() + ny;
			
			if (xx > x) x = xx;
			if (yy > y) y = yy;
			
			if (xx < x1) x1 = xx;
			if (yy < y1) y1 = yy;
		}
		
		Graph<MyNode, MyLink> ibgp = new SparseMultigraph<MyNode, MyLink>();
		
		double escalaX = _maxX / (x - x1);
		double escalaY = _maxY / (y - y1);
		
		for(Node node : _domain.getAllNodes())
		{
			if (node.getRid() == null || node.getRid().equals(""))
			{
				_logger.error("Node "+ node.getId() +" doesn't has Router ID (Rid == NULL || Rid == EMPTY)");
			}
			else
			{
				double xx = node.getLongitude() + nx;
				double yy = node.getLatitude() + ny;
				double auxX = (xx * escalaX) - x1*_maxX/(x - x1);
				double auxY = (yy * escalaY) - y1*_maxY/(y - y1);
				
				MyNode n = new MyNode(node.getId(), (MARCO/2)+auxX , (MARCO/2)+auxY, (node.getBgpRouter() != null && ((BgpRouterImpl)node.getBgpRouter()).isReflector() ? true: false));
				mynodesById.put(node.getRid(), n);
				nodesById.put(node.getRid(), node);
				ibgp.addVertex(n);
			}
		}
		
		/// ADD ROUTERS BGP AND SESSIONS iBGP
		List<BgpRouter> lst_bgps = _domain.getAllBgpRouters();
		for(BgpRouter router : lst_bgps)
		{
			if (nodesById.containsKey(router.getRid()))
			{	            
	            List<BgpNeighbor> neighbors = router.getAllNeighbors();
	            for (Iterator<BgpNeighbor> iterNeighbors = neighbors.iterator(); iterNeighbors.hasNext();) 
	            {
	            	BgpNeighbor neighbor = iterNeighbors.next();
	            	
	                if (neighbor.getASID() == domain_num) 
	                {
	                    if (!nodesById.containsKey(neighbor.getAddress())) _logger.error("WARNING: no node for neighbor " + neighbor.getAddress());
	                    
	                    String linkId = (router.getRid().compareTo(neighbor.getAddress()) <= 0) ? router.getRid() + ":" + neighbor.getAddress() : neighbor.getAddress() + ":" + router.getRid();
	                    
	                    if (neighbor.isReflectorClient())
	                    {
	                    	if (!linksById.contains(linkId))
	                    	{
	                    		linksById.add(linkId);
	                    		ibgp.addEdge(new MyLink(router.getId(), nodesById.get(neighbor.getAddress()).getId(), TypeMylink.client), mynodesById.get(router.getRid()), mynodesById.get(neighbor.getAddress()), EdgeType.DIRECTED);
	                    	}
	                    }              	
	                }     
	            }
			}
		}
		
		for(BgpRouter router : lst_bgps)
		{
			if (nodesById.containsKey(router.getRid()))
			{	            
	            List<BgpNeighbor> neighbors = router.getAllNeighbors();
	            for (Iterator<BgpNeighbor> iterNeighbors = neighbors.iterator(); iterNeighbors.hasNext();) 
	            {
	            	BgpNeighbor neighbor = iterNeighbors.next();
	            	
	                if (neighbor.getASID() == domain_num) 
	                {
	                    if (!nodesById.containsKey(neighbor.getAddress())) _logger.error("WARNING: no node for neighbor " + neighbor.getAddress());
	                    
	                    String linkId = (router.getRid().compareTo(neighbor.getAddress()) <= 0) ? router.getRid() + ":" + neighbor.getAddress() : neighbor.getAddress() + ":" + router.getRid();
	                    
                    	if (!linksById.contains(linkId))
                    	{
                    		linksById.add(linkId);
                    		ibgp.addEdge(new MyLink(router.getId(), nodesById.get(neighbor.getAddress()).getId(), TypeMylink.peer), mynodesById.get(router.getRid()), mynodesById.get(neighbor.getAddress()));
                    	}           	
	                }     
	            }
			}
		}
		
		/// END ROUTERS BGP AND SESSIONS iBGP
		
		return ibgp;
	}
    
}

