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

import be.ac.ulg.montefiore.run.totem.domain.model.DomainConvertor;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;

/*
* Changes:
* --------
*
*/

/**
* Maintain link information about some nodes in the network. Link information are based on another {@link BandwidthSharingTopologyData}.
* When a link information is needed, it is either taken from the current state of this object or cloned from the
* base {@link BandwidthSharingTopologyData} and added to the current state.
*
* <p>Creation date: 14/12/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class SnapshotBandwidthSharingTopologyData implements BandwidthSharingTopologyData {
    private BandwidthSharingTopologyData topoData;

    private BandwidthSharingLinkInfo[] linksInfo;

    private DomainConvertor convertor;

    public SnapshotBandwidthSharingTopologyData(BandwidthSharingTopologyData topoData) {
        this.topoData = topoData;
        convertor = topoData.getConvertor();
        linksInfo = new BandwidthSharingLinkInfo[convertor.getMaxLinkId()];
    }

    /**
     * Return a BandwidthSharingLinkInfo. If the information exists in the current state, it is returned.
     * If it is not, it is cloned from the underlying BandwidthSharingTopologyData and added to the current state.
     * @param linkId
     * @return
     */
    public BandwidthSharingLinkInfo getLinkInfo(int linkId) {
        if (linksInfo[linkId] != null) return linksInfo[linkId];
        BandwidthSharingLinkInfo info = topoData.getLinkInfo(linkId);
        if (info != null) linksInfo[linkId] = info.clone();
        return linksInfo[linkId];
    }

    /**
     * Return a BandwidthSharingLinkInfo. If the information exists in the current state, it is returned.
     * If it is not, it is cloned from the underlying BandwidthSharingTopologyData and added to the current state.      
     * @param linkId
     * @return
     */
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
