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

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiComponents.MainWindow;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.ClassNameCellRenderer;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.NodePathCellRenderer;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.LinkPathCellRenderer;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.SPF;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoRouteToHostException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.CSPF.CSPF;

import javax.swing.*;
import java.util.List;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;

/*
* Changes:
* --------
*
*/

/**
* Dialog that displays a list ECMP paths. The algorithm can be chosen
* and the resulting paths can be displayed as lists of nodes or lists of links.<br>
*
* When a path is selected in the list, it is highlighted in the vizualization panel
* thanks to {@link PathMutableJList}.
*
* <p>Creation date: 21/06/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ECMPAnalysisDialog extends JDialog {
    private Domain domain;

    private JComboBox algoCmb;

    private JRadioButton showNodes;
    private JRadioButton showLinks;

    private ButtonGroup viewBtnGroup;

    private PathMutableJList pathList;

    private static final NodePathCellRenderer nodeRenderer = new NodePathCellRenderer();
    private static final LinkPathCellRenderer linkRenderer = new LinkPathCellRenderer();

    public ECMPAnalysisDialog(Domain domain) {
        super(MainWindow.getInstance(), "ECMP Analysis", false);
        this.domain = domain;


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

        pathList = new PathMutableJList();

        setupUI();

        update();
    }

    private void setupUI() {
        ActionListener fillList = new FillPathsActionListener();

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel parametersPanel = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.insets = new Insets(5, 5, 5, 5);

        JPanel algoPanel = new JPanel(new GridLayout(1, 1, 5, 5));
        algoPanel.setBorder(BorderFactory.createTitledBorder("Algorithm"));
        algoCmb.addActionListener(fillList);
        algoPanel.add(algoCmb);

        JPanel viewPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        viewPanel.setBorder(BorderFactory.createTitledBorder("View"));
        viewPanel.add(showNodes);
        viewPanel.add(showLinks);

        parametersPanel.add(algoPanel, c);
        c.gridx++;
        parametersPanel.add(viewPanel, c);

        mainPanel.add(parametersPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(pathList));

        getRootPane().setContentPane(mainPanel);
    }

    private void update() {
        try {
            pathList.getModel().removeAllElements();

            Class spfClass = (Class)algoCmb.getSelectedItem();
            List<List<Path>> allPaths = domain.getValidator().getEqualCostMultiPath(spfClass.getSimpleName());

            for (List<Path> paths : allPaths) {
                for (Path p : paths) {
                    pathList.getModel().addElement(p);
                }
                pathList.getModel().addElement("--------------------");
            }

        } catch (NoRouteToHostException e) {
            e.printStackTrace();
            MainWindow.getInstance().errorMessage("No route to host Exception. " + e.getMessage());
        } catch (RoutingException e) {
            e.printStackTrace();
            MainWindow.getInstance().errorMessage("Routing Exception. " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            MainWindow.getInstance().errorMessage("Algorithm cannot be found. " + e.getMessage());
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
                pathList.setCellRenderer(nodeRenderer);
            else if (e.getActionCommand().equals("ViewLinks"))
                pathList.setCellRenderer(linkRenderer);
        }
    }

}
