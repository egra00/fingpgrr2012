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
package be.ac.ulg.montefiore.run.totem.visualtopo.graph;

import be.ac.ulg.montefiore.run.totem.domain.model.DomainChangeAdapter;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;

/*
* Changes:
* --------
* - 14-Jun-2007: take virtual links into account (GMO)
*/

/**
* Calculate the width of a link given its bandwidth.<p>
* The width is calculated relative to the minimum and maximum link capacity in the network. The returned width is
* between 1 and 6 pixel. The class listens to domain changes (add link, remove link, bandwidth change) and adapt the
* width accordingly. <br>
* The virtual links are not taken into account.
*
* <p>Creation date: 29/05/2007
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class MyWidthCalculator extends DomainChangeAdapter implements WidthCalculator {

    public final int MIN_WIDTH = 1;
    public final int MAX_WIDTH = 6;

    private float minBandwidth = Float.MAX_VALUE;
    private float maxBandwidth = 0.0f;

    private Domain domain;

    public MyWidthCalculator(Domain domain) {
        this.domain = domain;
        for (Link link : domain.getAllLinks()) {
            if (link.getLinkType() == Link.Type.VIRTUAL) continue;

            if (link.getBandwidth() < minBandwidth) {
                minBandwidth = link.getBandwidth();
            }
            if (link.getBandwidth() > maxBandwidth) {
                maxBandwidth = link.getBandwidth();
            }
        }
        domain.getObserver().addListener(this);
    }

    public int getWidth(float bwValue) {
        if (minBandwidth == maxBandwidth)
            return (MAX_WIDTH-MIN_WIDTH)/2;

        float val = (bwValue - minBandwidth) / maxBandwidth * (MAX_WIDTH-MIN_WIDTH);
        int width = Math.round(val) + MIN_WIDTH;
        if (width >= MAX_WIDTH) return MAX_WIDTH;
        if (width <= MIN_WIDTH) return MIN_WIDTH;
        return width;
    }

    public void stop() {
        domain.getObserver().removeListener(this);
    }

    public void addLinkEvent(Link link) {
        if (link.getLinkType() == Link.Type.VIRTUAL) return;
        if (link.getBandwidth() < minBandwidth) {
            minBandwidth = link.getBandwidth();
        }
        if (link.getBandwidth() > maxBandwidth) {
            maxBandwidth = link.getBandwidth();
        }
    }

    public void removeLinkEvent(Link link) {
        if (link.getLinkType() == Link.Type.VIRTUAL) return;
        if (link.getBandwidth() == minBandwidth || link.getBandwidth() == maxBandwidth) {
            for (Link l : domain.getAllLinks()) {
                if (l.getBandwidth() < minBandwidth) {
                    minBandwidth = l.getBandwidth();
                }
                if (l.getBandwidth() > maxBandwidth) {
                    maxBandwidth = l.getBandwidth();
                }
            }
        }
    }

    public void linkBandwidthChangeEvent(Link link) {
        if (link.getLinkType() == Link.Type.VIRTUAL) return;
        for (Link l : domain.getAllLinks()) {
            if (l.getLinkType() == Link.Type.VIRTUAL) continue;
            if (l.getBandwidth() < minBandwidth) {
                minBandwidth = l.getBandwidth();
            }
            if (l.getBandwidth() > maxBandwidth) {
                maxBandwidth = l.getBandwidth();
            }
        }
    }
}
