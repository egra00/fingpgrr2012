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
package be.ac.ulg.montefiore.run.totem.visualtopo.graph;

import java.util.HashMap;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManagerListener;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;

import javax.swing.*;

import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 30-Jun-2006: Use of GraphZoomScrollPane, add a button in the bottom-right corner to update node locations. (GMO)
* - 10-Jan-2007: Add highlight(Link) and unHighlight() method accessors (GMO)
* - 21-Jun-2007: Add highlight(Path) accessor (GMO)
*/

/**
* Take place of the panel representing the network.
* It maintains a MyVisualizationViewer for every loaded domain that has been displayed.
* It listens to InterDomain event, so that it changes the panel content to represent the default domain.
*
* <p>Creation date: 29 mars 2006
*
* @author Ga�l Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class GraphManager extends JPanel implements InterDomainManagerListener {

    private static Logger logger = Logger.getLogger(GraphManager.class);

    private HashMap<Integer, MyVisualizationViewer> domainPanels;
    private MyVisualizationViewer currentVV;

    private static GraphManager instance = null;

    private GraphManager() {
        setLayout(new BorderLayout());
        domainPanels = new HashMap<Integer, MyVisualizationViewer>();
        InterDomainManager.getInstance().addListener(this);
    }

    public static GraphManager getInstance() {
        if (instance == null)
            instance = new GraphManager();
        return instance;
    }

    public MyVisualizationViewer getVisualizationViewer() {
        return currentVV;
    }

    private MyVisualizationViewer createPanel(Domain domain) {
        logger.info("Building panel for domain: " + domain.getASID());

        /* First compute the graph */
        DomainGraph g = new DomainGraph(domain);

        //create nodes
        for (Node n : domain.getAllNodes()) {
            g.addVertex(n);
        }

        //Creating links
        for (Link link : domain.getAllLinks()) {
            g.addEdge(link);
        }
        /* create the layout */
        Layout layout = new LocalLayout(g);
        /* create the renderer */
        PluggableRenderer pr = new MyPluggableRenderer(domain);
        pr.setEdgeStringer(LinkLabeller.getInstance());
        pr.setVertexStringer(NodeLabeller.getInstance());

        /* create the VV */
        MyVisualizationViewer vv = new MyVisualizationViewer(layout, pr, domain, getSize());
        return vv;
    }

    private void display(Domain domain) {
        removeAll();
        MyVisualizationViewer vv = null;
        if (domain != null) {
            if ((vv = domainPanels.get(domain.getASID())) == null || vv.isOutDated()) {
                if (vv != null) vv.destroy();
                vv = createPanel(domain);
                domainPanels.put(domain.getASID(), vv);
            }
            GraphZoomScrollPane sc = new GraphZoomScrollPane(vv);
            final JButton b = new JButton();
            b.setToolTipText("Click to update node locations.");
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateLocation();
                }
            });
            sc.setCorner(b);
            add(sc, BorderLayout.CENTER);
        }
        this.currentVV = vv;
        MainWindow mainWindow = MainWindow.getInstance();
        if (domain != null) {
            mainWindow.setTitle("Totem - " + domain.getURI().toString());
        } else {
            mainWindow.setTitle("Totem");
        }
        //repaint the whole window. If not done, only a small part of the graph representation is painted.
        mainWindow.repaint();
    }

    /* current domain accessors */

    public void changeLayout(String s) {
        if (currentVV != null) currentVV.changeLayout(s);
    }
    public void updateLocation() {
        if (currentVV != null) currentVV.updateLocation();
    }

    public void highlight(Path path) {
        if (currentVV != null) currentVV.highlight(path);
    }

    public void highlight(Lsp lsp) {
        if (currentVV != null) currentVV.highlight(lsp);
    }

    public void highlight(Link link) {
        if (currentVV != null) currentVV.highlight(link);
    }

    public void unHighlight() {
        if (currentVV != null) currentVV.unHighlight();
    }

    public BufferedImage getImage() {
        Component component = this;

        if (component == null) {
            return null;
        }
        int width = component.getWidth();
        int height = component.getHeight();
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        component.paintAll(g);
        g.dispose();
        return image;
    }

    public void destroy() {
        InterDomainManager.getInstance().removeListener(this);
    }

    /*
      InterDomain events
    */

    public void addDomainEvent(Domain domain) {
        //do nothing, the VV will be created when the domainn is set to default
    }

    public void removeDomainEvent(Domain domain) {
        MyVisualizationViewer vv = domainPanels.remove(domain.getASID());
        if (vv != null) vv.destroy();
    }

    public void changeDefaultDomainEvent(Domain domain) {
        display(domain);
    }

}