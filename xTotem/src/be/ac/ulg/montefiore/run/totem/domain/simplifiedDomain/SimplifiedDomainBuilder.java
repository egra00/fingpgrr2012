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
package be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.DomainConvertor;
import be.ac.ulg.montefiore.run.totem.domain.exception.*;

import java.util.List;

/*
 * Changes:
 * --------
 *
 */

/**
 * Transform a Domain into a SimplifiedDomain and inversely
 *
 * <p>Creation date: 13-Jan-2005 10:39:15
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class SimplifiedDomainBuilder {

    /**
     * Build a SimplifiedDomain from a Domain
     *
     * @param domain
     * @return
     */
    public static SimplifiedDomain build(Domain domain) {
        String name = domain.getName();
        if (name == null)
            name = new String("Domain " + domain.getASID());
        DomainConvertor convertor = domain.getConvertor();
        SimplifiedDomain sd = new SimplifiedDomain(name,convertor.getMaxNodeId(),convertor.getMaxLinkId());
        List<Link> linkList = domain.getUpLinks();
        for (int i = 0; i < linkList.size(); i++) {
            Link link = linkList.get(i);
            try {
                sd.addLink(convertor.getLinkId(link.getId()),
                        convertor.getNodeId(link.getSrcNode().getId()),
                        convertor.getNodeId(link.getDstNode().getId()),
                        link.getBandwidth(),
                        link.getMetric(),
                        link.getDelay());
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
        }
        return sd;
    }

    /**
     * Upload a SimplifiedDomain in a Domain.
     *
     * WARNING : this method must be use carrefully. This method suppose that each link is the SimplifiedDomain
     * exists also in the Domain with the same src and dst nodes otherwise a SimplifiedDomainException is throwed.
     * This method only update the bandwidth, metric and delay of the link in the Domain.
     *
     * TO DO : remove or add the nodes and links from the SimplifiedDomain to the Domain.
     *
     * @param sDomain
     * @param domain
     * @throws SimplifiedDomainException
     */
    public static void upload(SimplifiedDomain sDomain, Domain domain) throws SimplifiedDomainException {
        DomainConvertor convertor = domain.getConvertor();

        SimplifiedDomain.Link[] sDomainLinks =  sDomain.getLinks();
        for (int i = 0; i < sDomainLinks.length; i++) {
            SimplifiedDomain.Link sDomainLink = sDomainLinks[i];
            if (sDomainLink != null) {
                Link link = null;
                // Check that the link is present in the domain with good src and dst nodes
                try {
                    link = domain.getLink(convertor.getLinkId(sDomainLink.getId()));
                } catch (LinkNotFoundException e) {
                    throw new SimplifiedDomainException("Link " + link.getId() + " not in the domain");
                }
                try {
                    if (sDomainLink.getSrcNode() != convertor.getNodeId(link.getSrcNode().getId())) {
                        throw new SimplifiedDomainException("Link " + link.getId() + " has not the same src node");
                    }
                } catch (NodeNotFoundException e) {
                    throw new SimplifiedDomainException("Node " + sDomainLink.getSrcNode() + " not in the domain");
                }
                try {
                    if (sDomainLink.getDstNode() != convertor.getNodeId(link.getDstNode().getId())) {
                        throw new SimplifiedDomainException("Link " + link.getId() + " has not the same dst node");
                    }
                } catch (NodeNotFoundException e) {
                    throw new SimplifiedDomainException("Node " + sDomainLink.getDstNode() + " not in the domain");
                }
                // Set bandwidth, metric and delay in the domain
                try {
                    link.setBandwidth(sDomainLink.getCapacity());
                } catch (LinkCapacityExceededException e) {
                    throw new SimplifiedDomainException("Cannot set Link bandwidth to " + sDomainLink.getCapacity() + " for link " + link.getId());
                } catch (DiffServConfigurationException e) {
                    throw new SimplifiedDomainException("Cannot set Link bandwidth to " + sDomainLink.getCapacity() + " for link " + link.getId());
                }
                link.setMetric(sDomainLink.getMetric());
                link.setDelay(sDomainLink.getDelay());
            }
        }

    }

}
