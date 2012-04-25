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
import be.ac.ulg.montefiore.run.totem.domain.exception.StatusTypeException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.exception.ActionExecutionException;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.whatIfScenarios.model.action.WIAction;
import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
* Action to set a node up.
*
* <p>Creation date: 23/04/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class NodeUpWIAction implements WIAction {

    private static final Logger logger = Logger.getLogger(NodeUpWIAction.class);

    private Node node = null;
    private Domain domain = null;

    public NodeUpWIAction(Domain domain, Node node) throws NodeNotFoundException {
        this.domain = domain;
        setNode(node);
    }

    public void setNode(Node node) throws NodeNotFoundException {
        domain.getNode(node.getId());
        this.node = node;
    }

    public void execute() throws ActionExecutionException {
        if (node.getNodeStatus() == Node.STATUS_UP) {
            logger.warn("Trying to set up a node that is already up.");
        }

        try {
            node.setNodeStatus(Node.STATUS_UP);
        } catch (StatusTypeException e) {
            e.printStackTrace();
            throw new ActionExecutionException(e);
        }
    }

    public String getName() {
        return "Node up: " + node.getId();
    }
}
