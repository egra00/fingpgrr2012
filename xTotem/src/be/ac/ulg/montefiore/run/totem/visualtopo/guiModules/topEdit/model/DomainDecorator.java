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

import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.LinkIgp;

import java.util.List;


/*
* Changes:
* --------
* - 27-Nov-2007: Add renameNode(.) method (GMO)
*/

/**
 * Add some functionalities to a JAXB domain.
 * <p/>
 * <p>Creation date: 3/10/2007
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public interface DomainDecorator {

    /**
     * returns the associated jaxb domain
     * @return
     */
    public Domain getDomain();

    /**
     * returns a link identified by id if exists, null otherwise
     * @param id
     * @return
     */
    public LinkDecorator getLink(String id);

    /**
     * returns the node identified by id if exists, null otherwise
     * @param id
     * @return
     */
    public Node getNode(String id);

    /**
     * Add a link to the domain
     * @param linkDecorator
     */
    public void addLink(LinkDecorator linkDecorator);

    /**
     * add the igp part of a link to the domain
     * @param linkIgp
     */
    public void addLinkIgp(LinkIgp linkIgp);

    /**
     * remove a link from the domain (the link and the igp of linkdecorator)
     * @param linkDecorator
     */
    public void removeLink(LinkDecorator linkDecorator);

    /**
     * remove the igp part of a link
     * @param linkIgp
     */
    public void removeLinkIgp(LinkIgp linkIgp);

    public List<LinkDecorator> getAllLinks();

    /**
     * add a node to the domain
     * @param node
     */
    public void addNode(Node node);

    /**
     * remove a node from the domain
     * @param node
     */
    public void removeNode(Node node);

    /**
     * Call this method when a node of the domain was renamed, to update other references in the domain
     * @param oldId
     * @param newId
     */
    public void renameNode(String oldId, String newId);

}
