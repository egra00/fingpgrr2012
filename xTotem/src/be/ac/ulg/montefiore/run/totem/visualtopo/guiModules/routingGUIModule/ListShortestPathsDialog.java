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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.routingGUIModule;

import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.DomainElementListCellRenderer;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.ClassNameCellRenderer;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.NodePathCellRenderer;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.LinkPathCellRenderer;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.graph.GraphManager;
import be.ac.ulg.montefiore.run.totem.repository.model.SPF;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.ArrayList;

/*
* Changes:
* --------
* - 21-Jun-2007: move MutableJList inner class to public class PathMutableJList, select CSPF by default (GMO)
*/

/**
* Dialog that displays a list of shortest paths. Various parameters can be tuned such as the source and destination
* nodes, the routing algorithm and if the resulting paths should be displayed as lists of nodes or lists of links.<br>
*
* When a path is selected in the list, it is highlighted in the vizualization panel
* thanks to {@link PathMutableJList}.
*
* <p>Creation date: 15/06/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ListShortestPathsDialog extends JDialog {

    private JComboBox srcCmb;
    private JComboBox dstCmb;

    private JComboBox algoCmb;

    private JCheckBox allSrc;
    private JCheckBox allDst;
    private JCheckBox ecmpChk;

    private JRadioButton showNodes;
    private JRadioButton showLinks;

    private ButtonGroup viewBtnGroup;

    private PathMutableJList shortestPathsList;

    private Domain domain = null;

    private static final NodePathCellRenderer nodeRenderer = new NodePathCellRenderer();
    private static final LinkPathCellRenderer linkRenderer = new LinkPathCellRenderer();

    public ListShortestPathsDialog(Domain domain) {
        super(MainWindow.getInstance(), "Shortest Paths List", false);
        this.domain = domain;

        srcCmb = new JComboBox();
        srcCmb.setRenderer(new DomainElementListCellRenderer());
        dstCmb = new JComboBox();
        dstCmb.setRenderer(new DomainElementListCellRenderer());

        allSrc = new JCheckBox("All sources");
        allSrc.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                srcCmb.setEnabled(!allSrc.isSelected());
            }
        });
        allDst = new JCheckBox("All destinations");
        allDst.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dstCmb.setEnabled(!allDst.isSelected());
            }
        });

        ecmpChk = new JCheckBox("ECMP");
        ecmpChk.setSelected(true);

        List<Class> algos = RepositoryManager.getInstance().getAllTotemAlgos(SPF.class);
        algoCmb = new JComboBox(algos.toArray());
        algoCmb.setSelectedItem(CSPF.class);
        algoCmb.setRenderer(new ClassNameCellRenderer());

        viewBtnGroup = new ButtonGroup();
        showNodes = new JRadioButton("Node list");
        showNodes.setSelected(true);
        showLinks = new JRadioButton("Links list");
        showNodes.setActionCommand("ViewNodes");
        showLinks.setActionCommand("ViewLinks");
        showNodes.addActionListener(new ChangeViewActionListener());
        showLinks.addActionListener(new ChangeViewActionListener());
        viewBtnGroup.add(showNodes);
        viewBtnGroup.add(showLinks);

        shortestPathsList = new PathMutableJList();
        shortestPathsList.setCellRenderer(nodeRenderer);

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                GraphManager.getInstance().unHighlight();
            }
        });

        init(domain);
        setupUI();

        update();
    }

    public void init(Domain domain) {
        this.domain = domain;
        srcCmb.removeAllItems();
        dstCmb.removeAllItems();
        for (Node n : domain.getAllNodes()) {
            srcCmb.addItem(n);
            dstCmb.addItem(n);
        }
    }

    private void setupUI() {
        ActionListener fillList = new FillPathsActionListener();

        srcCmb.addActionListener(fillList);
        allSrc.addActionListener(fillList);
        dstCmb.addActionListener(fillList);
        allDst.addActionListener(fillList);
        algoCmb.addActionListener(fillList);
        ecmpChk.addActionListener(fillList);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel parametersPanel = new JPanel(new GridBagLayout());
        JPanel contentPanel = new JPanel(new BorderLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(5, 5, 5, 5);

        JPanel srcPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        srcPanel.setBorder(BorderFactory.createTitledBorder("Source"));
        srcPanel.add(srcCmb);
        srcPanel.add(allSrc);

        JPanel dstPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        dstPanel.setBorder(BorderFactory.createTitledBorder("Destination"));
        dstPanel.add(dstCmb);
        dstPanel.add(allDst);

        JPanel algoPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        algoPanel.setBorder(BorderFactory.createTitledBorder("Algorithm"));
        algoPanel.add(algoCmb);
        algoPanel.add(ecmpChk);

        JPanel viewPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        viewPanel.setBorder(BorderFactory.createTitledBorder("View"));
        viewPanel.add(showNodes);
        viewPanel.add(showLinks);

        parametersPanel.add(srcPanel, c);
        c.gridx++;
        parametersPanel.add(dstPanel, c);
        c.gridx++;
        parametersPanel.add(algoPanel, c);
        c.gridx++;
        parametersPanel.add(viewPanel, c);

        mainPanel.add(parametersPanel, BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);


        contentPanel.add(new JScrollPane(shortestPathsList), BorderLayout.CENTER);

        getRootPane().setContentPane(mainPanel);
    }

    private void update() {

        boolean ECMP = ecmpChk.isSelected();

        Class algo = (Class)algoCmb.getSelectedItem();

        SPF spfAlgo;
        try {
            RepositoryManager.getInstance().startAlgo(algo.getSimpleName(), null, domain.getASID());
        } catch (AlgorithmInitialisationException e) {
        }
        try {
            spfAlgo = (SPF)RepositoryManager.getInstance().getAlgo(algo.getSimpleName(), domain.getASID());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            MainWindow.getInstance().errorMessage("No such algorithm" + e.getMessage());
            return;
        }

        try {
            List<Path> foundPath = null;
            shortestPathsList.getModel().removeAllElements();

            if (spfAlgo.getClass().getName().equals("be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF")) {
                foundPath = new ArrayList<Path>();
                List<Node> nodeList = domain.getUpNodes();
                if (allSrc.isSelected() && allDst.isSelected()) {
                    for(Node src : nodeList) {
                        for(Node dst : nodeList) {
                            if(src == dst) {
                                continue;
                            }
                            foundPath.addAll(domain.getSPFCache().getPath(src, dst, ECMP));
                        }
                    }
                } else if (allSrc.isSelected()) {
                    Node dst = (Node) dstCmb.getSelectedItem();
                    for(Node src : nodeList) {
                        if(src == dst) {
                            continue;
                        }
                        foundPath.addAll(domain.getSPFCache().getPath(src, dst, ECMP));
                    }
                } else if (allDst.isSelected()) {
                    Node src = (Node) srcCmb.getSelectedItem();
                    for(Node dst : nodeList) {
                        if(src == dst) {
                            continue;
                        }
                        foundPath.addAll(domain.getSPFCache().getPath(src, dst, ECMP));
                    }
                } else {
                    Node src = (Node) srcCmb.getSelectedItem();
                    Node dst = (Node) dstCmb.getSelectedItem();
                    foundPath = domain.getSPFCache().getPath(src, dst, ECMP);
                }
            } else {
                if (allSrc.isSelected() && allDst.isSelected()) {
                    foundPath = spfAlgo.computeFullMeshSPF(domain, ECMP);
                } else if (allSrc.isSelected()) {
                    Node dst = (Node) dstCmb.getSelectedItem();
                    foundPath = spfAlgo.computeSPF(domain, false, dst.getId(), ECMP);
                } else if (allDst.isSelected()) {
                    Node src = (Node) srcCmb.getSelectedItem();
                    foundPath = spfAlgo.computeSPF(domain, src.getId(), ECMP);
                } else {
                    Node src = (Node) srcCmb.getSelectedItem();
                    Node dst = (Node) dstCmb.getSelectedItem();
                    foundPath = spfAlgo.computeSPF(domain, src.getId(), dst.getId(), ECMP);
                }
            }
            if (foundPath != null) {
                for (Path path : foundPath) {
                    shortestPathsList.getModel().addElement(path);
                }
            }

        } catch (NoRouteToHostException e1) {
            e1.printStackTrace();
            MainWindow.getInstance().errorMessage("No route to host. " + e1.getMessage());
        } catch (RoutingException e1) {
            e1.printStackTrace();
            MainWindow.getInstance().errorMessage("Routing Exception. " + e1.getMessage());
        }
    }

    private class FillPathsActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
           update();
        }
    }

    private class ChangeViewActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("ViewNodes"))
                shortestPathsList.setCellRenderer(nodeRenderer);
            else if (e.getActionCommand().equals("ViewLinks"))
                shortestPathsList.setCellRenderer(linkRenderer);
        }
    }

}
