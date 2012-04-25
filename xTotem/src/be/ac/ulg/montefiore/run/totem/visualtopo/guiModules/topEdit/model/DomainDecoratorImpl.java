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
package be.ac.ulg.montefiore.run.totem.visualtopo.guiModules.topEdit.model;

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.*;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import javax.xml.bind.JAXBException;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
* - 27-Nov-2007: Add renameNode(.) method (GMO)
*/

/**
* Add some functionnalities to a JAXB domain. The old functionnalities of the domain are not directly
* accessible from this class but by can be accessed by using {@link #getDomain()}. 
* <br>
* @todo TODO: Maybe implement an index of nodes and links
*
* <p>Creation date: 3/10/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class DomainDecoratorImpl implements DomainDecorator {
    private final static Logger logger = Logger.getLogger(DomainDecoratorImpl.class);

    private final static ObjectFactory factory = new ObjectFactory();

    private Domain domain;

    public DomainDecoratorImpl(Domain domain) {
        this.domain = domain;
    }

    public Domain getDomain() {
        return domain;
    }

    public LinkDecorator getLink(String id) {
        Link foundLink = null;
        if (domain.isSetTopology() && domain.getTopology().isSetLinks()) {
            for (Object o : domain.getTopology().getLinks().getLink()) {
                Link link = (Link) o;
                if (link.getId().equals(id)) {
                    foundLink = link;
                    break;
                }
            }
        }

        if (foundLink == null) return null;

        LinkIgp foundLinkIgp = null;
        if (domain.isSetIgp() && domain.getIgp().isSetLinks()) {
            for (Object o : domain.getIgp().getLinks().getLink()) {
                LinkIgp linkIgp = (LinkIgp) o;
                if (linkIgp.getId().equals(id)) {
                    foundLinkIgp = linkIgp;
                    break;
                }
            }
        }

        LinkDecorator dec = new LinkDecoratorImpl(foundLink, foundLinkIgp);
        return dec;
    }

    public Node getNode(String id) {
        if (domain.isSetTopology() && domain.getTopology().isSetNodes()) {
            for (Object o : domain.getTopology().getNodes().getNode()) {
                Node node = (Node)o;
                if (node.getId().equals(id)) {
                    return node;
                }
            }
        }
        return null;
    }

    public void addLink(LinkDecorator linkDecorator) {
        try {
            if (!domain.getTopology().isSetLinks()) {
                domain.getTopology().setLinks(factory.createTopologyLinksType());
            }

            domain.getTopology().getLinks().getLink().add(linkDecorator.getLink());

            addLinkIgp(linkDecorator.getLinkIgp());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public void addLinkIgp(LinkIgp linkIgp) {
        if (linkIgp == null) return;
        try {
            if (!domain.isSetIgp()) {
                domain.setIgp(factory.createIgp());
            }

            if (!domain.getIgp().isSetLinks()) {
                domain.getIgp().setLinks(factory.createIgpIgpLinksType());
            }

            domain.getIgp().getLinks().getLink().add(linkIgp);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public void removeLink(LinkDecorator linkDecorator) {
        if (domain.isSetTopology() && domain.getTopology().isSetLinks()) {
            domain.getTopology().getLinks().getLink().remove(linkDecorator.getLink());

            if (domain.getTopology().getLinks().getLink().size() == 0) {
                domain.getTopology().unsetLinks();
            }
        }
        removeLinkIgp(linkDecorator.getLinkIgp());
    }

    public void removeLinkIgp(LinkIgp linkIgp) {
        if (linkIgp == null) return;
        if (domain.isSetIgp() && domain.getIgp().isSetLinks()) {
            domain.getIgp().getLinks().getLink().remove(linkIgp);

            if (domain.getIgp().getLinks().getLink().size() == 0) {
                domain.getIgp().unsetLinks();
            }
        }
    }

    public List<LinkDecorator> getAllLinks() {
        HashMap<String, Pair<Link, LinkIgp>> map = new HashMap<String, Pair<Link, LinkIgp>>();

        if (domain.isSetTopology() && domain.getTopology().isSetLinks()) {
            for (Link link : (List<Link>)domain.getTopology().getLinks().getLink()) {
                map.put(link.getId(), new Pair<Link, LinkIgp>(link, null));
            }
        }

        if (domain.isSetIgp() && domain.getIgp().isSetLinks()) {
            for (LinkIgp igp : (List<LinkIgp>)domain.getIgp().getLinks().getLink()) {
                Pair<Link, LinkIgp> pair = map.get(igp.getId());
                if (pair != null) {
                    pair.setSecond(igp);
                } else {
                    logger.error("Link IGP found but no corresponding link: " + igp.getId());
                }
            }
        }

        List<LinkDecorator> list = new ArrayList<LinkDecorator>(map.size());
        for (Pair<Link, LinkIgp> pair : map.values()) {
            list.add(new LinkDecoratorImpl(pair.getFirst(), pair.getSecond()));
        }

        return list;
    }

    public void addNode(Node node) {
        try {
            if (!domain.getTopology().isSetNodes()) {
                domain.getTopology().setNodes(factory.createTopologyNodesType());
            }

            domain.getTopology().getNodes().getNode().add(node);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public void removeNode(Node node) {
        if (domain.isSetTopology() && domain.getTopology().isSetNodes()) {
            domain.getTopology().getNodes().getNode().remove(node);
        }
    }

    /**
     * Call this method when a node of the domain was renamed, to update other references in the domain.
     * Updates references in from and to link properties.
     * @param oldId
     * @param newId
     */
    public void renameNode(String oldId, String newId) {
        if (oldId.equals(newId)) return;

        if (domain.isSetTopology() && domain.getTopology().isSetLinks()) {
            for (Link link : (List<Link>)domain.getTopology().getLinks().getLink()) {
                if (link.getFrom().isSetNode() && link.getFrom().getNode().equals(oldId)) {
                    link.getFrom().setNode(newId);
                }

                if (link.getTo().isSetNode() && link.getTo().getNode().equals(oldId)) {
                    link.getTo().setNode(newId);
                }
            }
        }
    }    
}
