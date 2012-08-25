package uy.edu.fing.repository.tools.iBGPViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import uy.edu.fing.repository.tools.iBGPViewer.model.LocalLayout;
import uy.edu.fing.repository.tools.iBGPViewer.model.MyJFrame;
import uy.edu.fing.repository.tools.iBGPViewer.model.MyLink;
import uy.edu.fing.repository.tools.iBGPViewer.model.MyNode;
import uy.edu.fing.repository.tools.iBGPViewer.model.TypeMylink;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpNeighborImpl;
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

public class ManagerGUIViewer 
{
	private static ManagerGUIViewer instance = null;
	private Logger my_logger;
	private int _maxX;
	private int _maxY;
	private HashMap<String, Node> nodesById;
	private List<String> linksById;
	private HashMap<String, MyNode> mynodesById;
	private final int MARCO = 150;
	private Image app_icon;
	private MyJFrame frame;
	private VisualizationViewer<MyNode,MyLink> default_vv;
	private HashMap<Integer, Data> hash;
	private HashMap<Integer, MyJFrame> frames;
	private int keys_ibgps;
	private int keys_frames;
	
	public class Data
	{
		public int key;
		public Domain domain;
		public String description;
		public Graph<MyNode, MyLink> ibgp;
	}
	
	private ManagerGUIViewer()
	{
		_maxX = 750;
		_maxY = 350;
		my_logger = Logger.getLogger(ManagerGUIViewer.class);
		app_icon = new ImageIcon(MainWindow.class.getResource("/resources/img/icon.gif")).getImage();
		keys_ibgps = 1;
		keys_frames = 1;
		hash = new HashMap<Integer, Data>();
		frames = new HashMap<Integer, MyJFrame>();
		frame = new MyJFrame(keys_frames);
		frames.put(keys_frames, frame);
		frame.setIconImage(app_icon);
		default_vv = null;
		keys_frames++;
	}
	
	public static ManagerGUIViewer getInstance()
	{
		if (instance == null)
			instance = new ManagerGUIViewer();
		
		return instance;
	}
	
	
	public HashMap<Integer, Data> getMapConfigurations()
	{
		return hash;
	}
	
	public HashMap<Integer, MyJFrame> getMapFrames()
	{
		return frames;
	}
	
	
	public void change(int current_window, int cahange_key)
	{	
		MyJFrame frame = frames.get(current_window);
		Data change_data = hash.get(cahange_key);
		
		Layout<MyNode,MyLink> layout = new  LocalLayout(change_data.ibgp);
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
		
		KeyStroke ctrl_D = KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK);
		KeyStroke ctrl_K = KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK);
		ActionMap mapaAccion = vv.getActionMap();
		InputMap map = vv.getInputMap(JComponent.WHEN_FOCUSED);
		
		map.put(ctrl_D , "Domains");
		mapaAccion.put("Domains",frame.Accion_CTRLD());
		
		map.put(ctrl_K , "Konsole");
		mapaAccion.put("Konsole",frame.Accion_CTRLK());
		
		
		frame.invalidate();
		frame.getContentPane().removeAll();
		frame.setTitle(change_data.description);
		frame.setIbgp_key(cahange_key);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.validate();
		frame.repaint(); 
		frame.setVisible(true);
	}


	public void disassociate(int current_window, int diss_key)
	{
		
		Data diss_data = hash.get(diss_key);
		MyJFrame new_frame = new MyJFrame(keys_frames);
		frames.put(keys_frames, new_frame);
		
		Layout<MyNode,MyLink> layout = new  LocalLayout(diss_data.ibgp);
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
		
		KeyStroke ctrl_D = KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK);
		KeyStroke ctrl_K = KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK);
		ActionMap mapaAccion = vv.getActionMap();
		InputMap map = vv.getInputMap(JComponent.WHEN_FOCUSED);
		
		map.put(ctrl_D , "Domains");
		mapaAccion.put("Domains",new_frame.Accion_CTRLD());
		
		map.put(ctrl_K , "Konsole");
		mapaAccion.put("Konsole",new_frame.Accion_CTRLK());
		
		new_frame.setIconImage(new ImageIcon(MainWindow.class.getResource("/resources/img/icon.gif")).getImage());
		new_frame.setTitle(diss_data.description);
		new_frame.setIbgp_key(diss_key);
		new_frame.getContentPane().add(vv);
		new_frame.pack();
		new_frame.setVisible(true);
		
		
		keys_frames++;
	}
	
	public void associate(int current_window, int new_key)
	{
		
	}
	
	public void showSessionsIbgp(Domain domain)
	{
		Graph<MyNode, MyLink> ibgp = toGraphPrintable(domain);
		Layout<MyNode,MyLink> layout = new  LocalLayout(ibgp);
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
		
		frame.invalidate();
		if (default_vv == null)
			default_vv = vv;
		else
		{
			frame.getContentPane().remove(default_vv);
			default_vv = vv;
		}
		

		KeyStroke ctrl_D = KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK);
		KeyStroke ctrl_K = KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK);
		ActionMap mapaAccion = vv.getActionMap();
		InputMap map = vv.getInputMap(JComponent.WHEN_FOCUSED);
		
		map.put(ctrl_D , "Domains");
		mapaAccion.put("Domains",frame.Accion_CTRLD());
		
		map.put(ctrl_K , "Konsole");
		mapaAccion.put("Konsole",frame.Accion_CTRLK());
		
		
		
		Data data = new Data();
		data.key = keys_ibgps;
		data.domain = domain;
		data.ibgp = ibgp; 
		data.description = "iBGP configuration "+ String.valueOf(keys_ibgps) + " - " + "Domain "+ String.valueOf(domain.getASID());
		
		frame.setIbgp_key(keys_ibgps);
		hash.put(keys_ibgps, data);
		
		frame.setTitle("iBGP configuration "+ String.valueOf(keys_ibgps) +" - "+ "Domain "+ String.valueOf(domain.getASID()));
		frame.getContentPane().add(default_vv);
		frame.pack();
		frame.validate();
		frame.repaint(); 
		frame.setVisible(true);
		keys_ibgps++;
	}
	
	private Graph<MyNode, MyLink> toGraphPrintable(Domain _domain)
	{	
		
		nodesById = new HashMap<String, Node>();
		linksById = new LinkedList<String>();
		mynodesById = new HashMap<String, MyNode>();
		
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
				my_logger.error("Node "+ node.getId() +" doesn't has Router ID (Rid == NULL || Rid == EMPTY)");
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
	                    if (!nodesById.containsKey(neighbor.getAddress())) my_logger.error("WARNING: no node for neighbor " + neighbor.getAddress());
	                    
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
	                    if (!nodesById.containsKey(neighbor.getAddress())) my_logger.error("WARNING: no node for neighbor " + neighbor.getAddress());
	                    
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
