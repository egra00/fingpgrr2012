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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.impl;

import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.BadParametersException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIAction;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIActionPanel;
import be.ac.ulg.montefiore.run.totem.visualtopo.util.cellRenderer.DomainElementListCellRenderer;

import javax.swing.*;
import java.awt.*;

/*
* Changes:
* --------
*
*/

/**
* Panel to select a node to set up.
*
* <p>Creation date: 23/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class NodeUpWIActionPanel extends WIActionPanel {
    private JComboBox nodesCmb;

    public NodeUpWIActionPanel(Domain domain) {
        super(new GridLayout(2, 1, 5, 5));
        this.domain = domain;
        add(new JLabel("Select the node to set up."));

        Node[] node = new Node[0];
        nodesCmb = new JComboBox(domain.getAllNodes().toArray(node));
        nodesCmb.setRenderer(new DomainElementListCellRenderer());
        add(nodesCmb);

        setBorder(BorderFactory.createTitledBorder("Node Up"));
    }

    public WIAction createWIAction() throws BadParametersException {
        WIAction action;
        try {
            action = new NodeUpWIAction(domain, (Node)nodesCmb.getSelectedItem());
        } catch (NodeNotFoundException e) {
            throw new BadParametersException(e);
        }
        return action;
    }

    public String getWIActionName() {
        return "Node up";
    }

    /**
     * Destroys the panel. Mainly to remove listeners.
     */
    public void destroy() {
    }

}