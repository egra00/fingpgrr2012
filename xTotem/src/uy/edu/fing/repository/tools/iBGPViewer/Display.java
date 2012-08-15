package uy.edu.fing.repository.tools.iBGPViewer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Paint;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
import org.apache.log4j.Logger;

import uy.edu.fing.repository.tools.iBGPViewer.model.LocalLayout;
import uy.edu.fing.repository.tools.iBGPViewer.model.MyLink;
import uy.edu.fing.repository.tools.iBGPViewer.model.MyNode;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpNeighborImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpRouterImpl;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import edu.uci.ics.jung2.algorithms.layout.Layout;
import edu.uci.ics.jung2.graph.Graph;
import edu.uci.ics.jung2.graph.UndirectedSparseMultigraph;
import edu.uci.ics.jung2.visualization.VisualizationViewer;
import edu.uci.ics.jung2.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung2.visualization.control.ModalGraphMouse.Mode;
import edu.uci.ics.jung2.visualization.decorators.ToStringLabeller;

public class Display 
{
	private Logger my_logger;
	private Domain _domain;
	private int _maxX;
	private int _maxY;
	private HashMap<String, Node> nodesById;
	private List<String> linksById;
	private HashMap<String, MyNode> mynodesById;
	private final int MARCO = 150;
	private Image app_icon;

	
	public Display(int maxX, int maxY)
	{
		_maxX = maxX;
		_maxY = maxY;
		my_logger = Logger.getLogger(Display.class);
		_domain = InterDomainManager.getInstance().getDefaultDomain();
		app_icon = new ImageIcon(MainWindow.class.getResource("/resources/img/icon.gif")).getImage();
		nodesById = new HashMap<String, Node>();
		linksById = new LinkedList<String>();
		mynodesById = new HashMap<String, MyNode>();
		
	}
	
	
	public void printTopologyIBGP()
	{
		Graph<MyNode, MyLink> ibgp = toGraphPrintable();
		
		Layout<MyNode,MyLink> layout = new  LocalLayout(ibgp);
		layout.setSize(new Dimension(_maxX, _maxY));
		VisualizationViewer<MyNode,MyLink> vv = new VisualizationViewer<MyNode, MyLink>(layout);
		vv.setPreferredSize(new Dimension(_maxX + MARCO, _maxY + MARCO));
		
		Transformer<MyNode, Paint> vertexPaint = new Transformer<MyNode, Paint>() {
				@Override
				public Paint transform(MyNode node) 
				{
					BgpRouterImpl router1 = (BgpRouterImpl)node.getNode().getBgpRouter();
					return (router1!= null && router1.isReflector() ?   Color.BLUE : Color.GREEN);
				}
		};
		
		Transformer<MyLink, Paint> edgeStrokeTransformer = new Transformer<MyLink, Paint>() {

					@Override
					public Paint transform(MyLink link) 
					{
						BgpNeighborImpl router = ((BgpNeighborImpl)link.getNeighbor());
						return (router != null && router.isReflectorClient()? Color.BLACK : Color.GRAY);
					}
		};
		vv.getRenderContext().setVertexFillPaintTransformer(vertexPaint);
		vv.getRenderContext().setEdgeDrawPaintTransformer(edgeStrokeTransformer);
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<MyNode>());

		DefaultModalGraphMouse<MyNode, MyLink> gm = new DefaultModalGraphMouse<MyNode, MyLink>();
		gm.setMode(Mode.TRANSFORMING);
		vv.setGraphMouse(gm);
		
		JFrame frame = new JFrame("Viewer Sessions iBGP @Run");
		frame.setIconImage(app_icon);
		frame.getContentPane().add(vv);
		frame.pack();
		frame.setVisible(true);
	}
	
	private Graph<MyNode, MyLink> toGraphPrintable()
	{		
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
		
		Graph<MyNode, MyLink> ibgp = new UndirectedSparseMultigraph<MyNode, MyLink>();
		
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
				
				MyNode n = new MyNode(node, (MARCO/2)+auxX , (MARCO/2)+auxY);
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
	                    		ibgp.addEdge(new MyLink(router, neighbor), mynodesById.get(router.getRid()), mynodesById.get(neighbor.getAddress()));
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
                    		ibgp.addEdge(new MyLink(router, neighbor), mynodesById.get(router.getRid()), mynodesById.get(neighbor.getAddress()));
                    	}           	
	                }     
	            }
			}
		}
		
		/// END ROUTERS BGP AND SESSIONS iBGP
		
		return ibgp;
	}
}
	