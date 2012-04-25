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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.constraint;

import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.NodeInterface;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Link;

import java.util.HashMap;
import java.util.List;

/*
* Changes:
* --------
*
*/

/**
* Check that every link is connected to an interface if the node has interfaces. Also check uniqueness of interface use.
*
* <p>Creation date: 23/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class InterfacesDomainConstraint implements DomainConstraint {
    private final static String description = "Check that every link is connected to an interface if the node has interfaces. Also check uniqueness of interface use.";
    private String msg;

    public boolean validate(DomainDecorator domain) {
        msg = "SUCCESS";

        //associate a node id to a list of its interfaces to a value representing the connected link id.
        HashMap<String, HashMap<String, Link>> map = new HashMap<String, HashMap<String, Link>>();

        for (Node node : (List<Node>)domain.getDomain().getTopology().getNodes().getNode()) {
            if (map.get(node.getId()) == null) {
                if (node.isSetInterfaces()) {
                    //create the hashMap associating an interface id to a link id
                    HashMap<String, Link> ifMap = new HashMap<String, Link>();
                    for (NodeInterface nif : (List<NodeInterface>)node.getInterfaces().getInterface()) {
                        ifMap.put(nif.getId(), null);
                    }
                    map.put(node.getId(), ifMap);
                } else {
                    map.put(node.getId(), null);
                }
            } else {
                msg = "Multiple nodes with the same id. ID: " + node.getId();
                return false;
            }
        }

        if (domain.getDomain().getTopology().isSetLinks()) {
            for (Link link : (List<Link>)domain.getDomain().getTopology().getLinks().getLink()) {
                //from
                String fromId = link.getFrom().getNode();
                if (!map.containsKey(fromId)) {
                    msg = "A link is connected to an non existing node. link id: " + link.getId() + " to node " + fromId;
                    return false;
                } else {
                    HashMap<String, Link> ifMap = map.get(fromId);
                    if (link.getFrom().isSetIf()) {
                        String ifFromId = link.getFrom().getIf();
                        if (!ifMap.containsKey(ifFromId)) {
                            msg = "Link " + link.getId() + " is connected to a non existing interface (" + ifFromId + ") on node " + fromId +".";
                            return false;
                        } else {
                            Link oldLink = ifMap.put(ifFromId, link);
                            if (oldLink != link && oldLink != null) {
                                if (!oldLink.getFrom().getNode().equals(link.getTo().getNode()) || !oldLink.getTo().getNode().equals(link.getFrom().getNode())) {
                                    msg = "Multiple links connected to a single interface (" + ifFromId + ") " + " Links " + oldLink.getId() + " and " + link.getId();
                                    return false;
                                }
                            }
                        }
                    } else { //not connected to an interface
                        if (ifMap != null) {
                            // the node had interfaces
                            msg = "Node " + fromId + " has interface but the link " + link.getId() + " is not connected to one.";
                            return false;
                        }
                    }
                }
                //to
                String toId = link.getTo().getNode();
                if (!map.containsKey(toId)) {
                    msg = "A link is connected to an non existing node. link id: " + link.getId() + " to node " + toId;
                    return false;
                } else {
                    HashMap<String, Link> ifMap = map.get(toId);
                    if (link.getTo().isSetIf()) {
                        String ifToId = link.getTo().getIf();
                        if (!ifMap.containsKey(ifToId)) {
                            msg = "Link " + link.getId() + " is connected to a non existing interface (" + ifToId + ") on node " + toId +".";
                            return false;
                        } else {
                            Link oldLink = ifMap.put(ifToId, link);
                            if (oldLink != link && oldLink != null) {
                                if (!oldLink.getTo().getNode().equals(link.getFrom().getNode()) || !oldLink.getFrom().getNode().equals(link.getTo().getNode())) {
                                    msg = "Multiple links connected to a single interface (" + ifToId + ") " + " Links " + oldLink.getId() + " and " + link.getId();
                                    return false;
                                }
                            }
                        }
                    } else { //not connected to an interface
                        if (ifMap != null) {
                            // the node had interfaces
                            msg = "Node " + toId + " has interface but the link " + link.getId() + " is not connected to one.";
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    public String getMessage() {
        return msg;
    }

    public String getDescription() {
        return description;
    }
}
