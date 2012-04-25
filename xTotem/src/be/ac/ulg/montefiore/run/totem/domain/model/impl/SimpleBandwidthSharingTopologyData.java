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
package be.ac.ulg.montefiore.run.totem.domain.model.impl;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.DomainConvertor;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
* Maintain information about reservation for each link in the network and also topology information.
*
* <p>Creation date: 14/12/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class SimpleBandwidthSharingTopologyData implements BandwidthSharingTopologyData {
    private final static Logger logger = Logger.getLogger(SimpleBandwidthSharingTopologyData.class);

    private int linksInNode[][];
    private BandwidthSharingLinkInfo linksInfo[];

    private DomainConvertor convertor;


    public SimpleBandwidthSharingTopologyData(Domain domain) {
        convertor = domain.getConvertor();
        linksInfo = new BandwidthSharingLinkInfo[convertor.getMaxLinkId()];
        linksInNode = new int[convertor.getMaxNodeId()][];

        try {
            // Initialize the linksInNode table
            for (Iterator<Node> it = domain.getAllNodes().iterator(); it.hasNext();) {
                Node node = it.next();
                int nodeId = convertor.getNodeId(node.getId());
                List<Link> inLinks = node.getAllInLink();
                linksInNode[nodeId] = new int[inLinks.size()];

                int i = 0;
                for (Iterator<Link> it2 = inLinks.iterator(); it2.hasNext();) {
                    Link link = it2.next();
                    linksInNode[nodeId][i++] = convertor.getLinkId(link.getId());
                }
            }

            // Create all the LinkInfo objects
            for (Iterator<Link> it = domain.getAllLinks().iterator(); it.hasNext();) {
                Link link = it.next();
                int linkId = convertor.getLinkId(link.getId());
                BandwidthSharingLinkInfo linkInfo = new BandwidthSharingLinkInfo(linkId, link.getBandwidth(), convertor.getMaxLinkId(), linksInNode);
                linksInfo[linkId] = linkInfo;
            }

        } catch (LinkNotFoundException e) {
            logger.fatal("Error in domain: link not found.");
        } catch (NodeNotFoundException e) {
            logger.fatal("Error in domain: node not found.");
        }

    }

    public BandwidthSharingLinkInfo getLinkInfo(int linkId) {
        if (linkId >= 0 && linkId < linksInfo.length)
            return linksInfo[linkId];
        return null;
    }

    public BandwidthSharingLinkInfo getLinkInfo(String linkId) {
        try {
            return getLinkInfo(convertor.getLinkId(linkId));
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public DomainConvertor getConvertor() {
        return convertor;
    }
}
