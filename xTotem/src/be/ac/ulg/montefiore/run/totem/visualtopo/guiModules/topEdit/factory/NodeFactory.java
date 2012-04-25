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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory;

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.NodeInterface;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.StatusType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.NodeType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.NodeImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.NodeInterfaceImpl;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.idGenerator.NodeIdGenerator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.factory.idGenerator.OrderedNodeIdGenerator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model.DomainDecorator;
import be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.exception.NotFoundException;

import java.util.HashMap;


/*
* Changes:
* --------
*
*/

/**
* <Replace this by a description of the class>
*
* <p>Creation date: 5/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class NodeFactory extends DomainElementFactory<Node> {

    private NodeIdGenerator nodeIdGenerator;

    public NodeFactory(DomainDecorator domain) {
        super();
        nodeIdGenerator = new OrderedNodeIdGenerator(domain);
    }

    public NodeFactory(NodeIdGenerator nodeIdGenerator) {
        super();
        this.nodeIdGenerator = nodeIdGenerator;
    }

    public Node createInstance(HashMap<String, String> params) {
        Node node = new NodeImpl();
        node.setId(nodeIdGenerator.generate(node));
        return node;
    }

    public Node createObject(String model, HashMap<String, String> params) throws NotFoundException {
        Node node = super.createObject(model, params);
        node.setId(nodeIdGenerator.generate(node));
        return node;
    }

    protected Object cloneObject(Object object) {
        Node clone = new NodeImpl();
        Node node = (Node)object;

        if (node.isSetDescription())
            clone.setDescription(node.getDescription());
        if (node.isSetId())
            clone.setId(node.getId());
        if (node.isSetInterfaces()) {
            clone.setInterfaces(new NodeImpl.InterfacesTypeImpl());
            if (node.getInterfaces().isSetInterface()) {
                for (Object o : node.getInterfaces().getInterface()) {
                    NodeInterface nif = (NodeInterface)o;
                    NodeInterface cloneIf = new NodeInterfaceImpl();
                    if (nif.isSetId()) {
                        cloneIf.setId(nif.getId());
                    }
                    if (nif.isSetIp()) {
                        cloneIf.setIp(new NodeInterfaceImpl.IpTypeImpl());
                        if (nif.getIp().isSetMask())
                            cloneIf.getIp().setMask(nif.getIp().getMask());
                        if (nif.getIp().isSetValue())
                            cloneIf.getIp().setValue(nif.getIp().getValue());
                    }
                    if (nif.isSetStatus())
                        cloneIf.setStatus(StatusType.fromValue(nif.getStatus().getValue()));
                    clone.getInterfaces().getInterface().add(cloneIf);
                }
            }
        }

        if (node.isSetLocation()) {
            clone.setLocation(new NodeImpl.LocationTypeImpl());
            if (node.getLocation().isSetLatitude())
                clone.getLocation().setLatitude(node.getLocation().getLatitude());
            if (node.getLocation().isSetLongitude())
                clone.getLocation().setLongitude(node.getLocation().getLongitude());
        }

        if (node.isSetRid())
            clone.setRid(node.getRid());

        if (node.isSetStatus())
            clone.setStatus(StatusType.fromValue(node.getStatus().getValue()));

        if (node.isSetType())
            clone.setType(NodeType.fromValue(node.getType().getValue()));
        
        return clone;
    }

}
