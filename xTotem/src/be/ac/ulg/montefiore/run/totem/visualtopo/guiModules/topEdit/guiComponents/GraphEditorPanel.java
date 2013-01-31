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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.guiComponents;

import edu.uci.ics.jung.visualization.*;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.GraphMousePlugin;
import edu.uci.ics.jung.utils.UserData;
import edu.uci.ics.jung.graph.Vertex;

import java.util.Set;

import javax.swing.*;
import javax.xml.bind.JAXBException;
import java.awt.*;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.LinkFactory;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.graph.*;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.NodeFactory;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.CoordMapper;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.ObjectFactory;

/*
* Changes:
* --------
* - 17-Dec-2007: Add setNodeLocations() (GMO)
*/

/**
* Panel to display a graph.
*
* <p>Creation date: 3/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class GraphEditorPanel extends JPanel {
    private MyDirectedSparseGraph graph;
    private MyStaticLayout layout;
    private VisualizationViewer vv;
    private SettableVertexLocationFunction vertexLocations;
    private /*final*/ MyEditingModalGraphMouse graphMouse;

    private JLabel xLabel;
    private JLabel yLabel;

    private JLabel realXLabel;
    private JLabel realYLabel;

    private DomainDecorator domainDecorator;

    private LinkFactory linkFactory;
    private NodeFactory nodeFactory;

    private /*final*/ CoordMapper mapper;

    /* */
    final JPanel contentPanel;

    public GraphEditorPanel(DomainDecorator domainDecorator) {
        super(new BorderLayout());
        this.domainDecorator = domainDecorator;

        xLabel = new JLabel("X:");
        yLabel = new JLabel("Y:");
        JPanel coordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
        coordPanel.add(xLabel);
        coordPanel.add(yLabel);

        //debug
        coordPanel.add(realXLabel = new JLabel());
        coordPanel.add(realYLabel = new JLabel());

        add(coordPanel, BorderLayout.SOUTH);

        contentPanel = new JPanel(new BorderLayout());
        add(contentPanel, BorderLayout.CENTER);
        contentPanel.setBorder(BorderFactory.createTitledBorder("Edition Zone"));

    }

    public void init() {
        Dimension dim = new Dimension();
        dim.setSize(contentPanel.getSize().getWidth()-60, contentPanel.getSize().getHeight()-60);

        linkFactory = new LinkFactory(domainDecorator);
        nodeFactory = new NodeFactory(domainDecorator);

        graph = new MyDirectedSparseGraph(domainDecorator, nodeFactory, linkFactory);

        graph.addUserDatum(MyDirectedSparseGraph.KEY, domainDecorator, UserData.SHARED);

        mapper = createCoordMapper(dim, domainDecorator.getDomain());

        vertexLocations = new MySettableVertexLocationFunction(mapper);

        layout = new MyStaticLayout(graph, mapper);
        layout.initialize(dim, vertexLocations);

        graph.setLayout(layout);

        PluggableRenderer pr = new MyPluggableRenderer();
        pr.setVertexStringer(NodeLabeller.getInstance());

        vv = new VisualizationViewer(layout, pr, dim);
        vv.setBackground(Color.white);
        vv.setPickSupport(new ShapePickSupport());

        final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
        graphMouse = new MyEditingModalGraphMouse();

        // the EditingGraphMouse will pass mouse event coordinates to the
        // vertexLocations function to set the locations of the vertices as
        // they are created
        graphMouse.setVertexLocations(vertexLocations);
        vv.setGraphMouse(graphMouse);

        graphMouse.add(new MyEditingPopupGraphMousePlugin(vertexLocations));
        graphMouse.add(new UpdateCoordinateGraphMousePlugin());
        graphMouse.setMode(ModalGraphMouse.Mode.EDITING);
        graphMouse.setBatchMode(false);

        contentPanel.add(panel, BorderLayout.CENTER);
    }

    private CoordMapper createCoordMapper(Dimension d, Domain domain) {
        float minLong = 0;
        float minLat = -90;
        float maxLong = 180;
        float maxLat = 90;

        if (domain.isSetTopology() && domain.getTopology().getNodes().getNode().size() > 0) {
            minLong = Float.MAX_VALUE;
            minLat = Float.MAX_VALUE;
            maxLong = -Float.MAX_VALUE;
            maxLat = -Float.MAX_VALUE;
            //get geographical bounds of network
            for (Object o : domain.getTopology().getNodes().getNode()) {
                Node node = (Node) o;
                if (node.isSetLocation()) {
                    float lat = node.getLocation().getLatitude();
                    float longi = node.getLocation().getLongitude();
                    if (longi < minLong) minLong = longi;
                    if (lat < minLat) minLat = lat;
                    if (longi > maxLong) maxLong = longi;
                    if (lat > maxLat) maxLat = lat;
                }
            }
        }
        float XRange = maxLong - minLong;
        float YRange = maxLat - minLat;

        if (XRange <= 0) {
            XRange = (float) d.getWidth();
            minLong = 0;
        }
        if (YRange <= 0) {
            YRange = (float) d.getHeight();
            minLat = 0;
        }
        //add 20 so that the extremity of node is visible
        return new CoordMapper(XRange, minLong, YRange, minLat, d.getWidth()-40, 20, d.getHeight()-40, 20);

        /* Identity coord mapper
        return new CoordMapper(XRange, minLong, YRange, minLat, d.getWidth()-40, 20, d.getHeight()-40, 20) {
            public Coordinates map(double srcX, double srcY) {
                return new Coordinates(srcX, srcY);
            }

            public Coordinates unmap(double dstX, double dstY) {
                return new Coordinates(dstX, dstY);
            }
        };
        */
    }

    public void setNodeLocations() {
        ObjectFactory factory = new ObjectFactory();
        for (Vertex v : (Set<Vertex>)graph.getVertices()) {
            Node n = (Node)v.getUserDatum(MyDirectedSparseGraph.KEY);
            try {
                n.setLocation(factory.createNodeLocationType());
                Point2D point = layout.getLocation(v);
                Coordinates c = mapper.map(point.getX(), point.getY());
                n.getLocation().setLatitude((float)c.getY());
                n.getLocation().setLongitude((float)c.getX());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
    }
    
    

	public void setNumberingNetwork() {
    	
    	
    	if(graph.numVertices()<=256*256) { //clase C
    		numering192_168_0_0to192_168_255_255(graph);
    	}
    	else if(graph.numVertices()<=16*256*256) { // clese B
    		numering172_16_0_0to172_31_255_255(graph);
    	} 
    	else { // clase A
    		numering10_0_0_0to10_255_255_255(graph);
    	}
    }

    @SuppressWarnings("unchecked")
    private void numering10_0_0_0to10_255_255_255(MyDirectedSparseGraph graph) {
		int cant = 0;
		int part1;
		int part2;
		int part3;
		String part1str;
		String part2str;
		String part3str;
		String fix = "10";
		
        for (Vertex v : (Set<Vertex>)graph.getVertices()) {
        	Node n = (Node)v.getUserDatum(MyDirectedSparseGraph.KEY);
            part1 = cant%256;
            part2 = cant%(256*256);
            part3 = cant/(256*256*256);
            
            part1str = String.valueOf(part1);
        	part2str = String.valueOf(part2);
        	part3str = String.valueOf(part3);
        	
            n.setRid(fix+ "." + part3str + "." + part2str + "." + part1str);
            cant++;
        }
		
	}
    
    @SuppressWarnings("unchecked")
	private void numering172_16_0_0to172_31_255_255(MyDirectedSparseGraph graph) {
		int cant = 0;
		int part1;
		int part2;
		int part3;
		String part1str;
		String part2str;
		String part3str;
		String fix = "172";
		
        for (Vertex v : (Set<Vertex>)graph.getVertices()) {
        	Node n = (Node)v.getUserDatum(MyDirectedSparseGraph.KEY);
            part1 = cant%256;
            part2 = cant%(256*256);
            part3 = cant/(16*256*256);
            
            part1str = String.valueOf(part1);
        	part2str = String.valueOf(part2);
        	part3str = String.valueOf(part3 + 16);
        	
            n.setRid(fix+ "." + part3str + "." + part2str + "." + part1str);
            cant++;
        }
		
	}

	@SuppressWarnings("unchecked")
	private void numering192_168_0_0to192_168_255_255(MyDirectedSparseGraph graph) {
		
		int cant = 0;
		int part1;
		int part2;
		String part1str;
		String part2str;
		String fix = "192.168";
		
        for (Vertex v : (Set<Vertex>)graph.getVertices()) {
        	Node n = (Node)v.getUserDatum(MyDirectedSparseGraph.KEY);
            part1 = cant%256;
            part2 = cant/(256*256);
            
            part1str = String.valueOf(part1);
        	part2str = String.valueOf(part2);
        	
            n.setRid(fix + "." + part2str + "." + part1str);
            cant++;
        }
	}


	final static DecimalFormat df = new DecimalFormat();
    static {
        df.setMaximumFractionDigits(2);
    }
	/**
	 * Show the position of the mouse cursor
	 *
	 * @param longitude
	 * @param latitude
	 */
	public void setCoord(double longitude, double latitude) {
		xLabel.setText("X: " + df.format(longitude));
		yLabel.setText("Y: " + df.format(latitude));

        // debug
        Coordinates c = mapper.unmap(longitude, latitude);
        realXLabel.setText("Longitude: " + df.format(c.getX()));
        realYLabel.setText("Latitude: " + df.format(c.getY()));
	}

    public DomainDecorator getDomainDecorator() {
        return domainDecorator;
    }

    public LinkFactory getLinkFactory() {
        return linkFactory;
    }

    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public void setMode(ModalGraphMouse.Mode mode) {
        graphMouse.setMode(mode);
    }

    public void setBatchMode(boolean useBatchMode) {
        graphMouse.setBatchMode(useBatchMode);
    }

    /**
     * Plugin that updates the label indicating the current mouse cursor.
     */
    private class UpdateCoordinateGraphMousePlugin implements GraphMousePlugin, MouseMotionListener {

        public int getModifiers() {
            return 0;
        }

        public void setModifiers(int modifiers) {
        }

        public boolean checkModifiers(MouseEvent e) {
            return true;
        }

        public void mouseDragged(MouseEvent e) {
            action(e);
        }

        public void mouseMoved(MouseEvent e) {
            action(e);
        }

        private void action(MouseEvent e) {
            try {
                final VisualizationViewer vv = (VisualizationViewer)e.getSource();
                Point2D point = vv.inverseTransform(e.getPoint());
                setCoord(point.getX(), point.getY());

            } catch (Exception ex) {};
        }
    }

}
