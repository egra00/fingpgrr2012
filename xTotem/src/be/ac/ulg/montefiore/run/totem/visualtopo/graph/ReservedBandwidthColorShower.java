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

import be.ac.ulg.montefiore.run.totem.domain.model.Link;

/*
* Changes:
* --------
* - 24-Oct-2006: add Class Type parameter (GMO)
* - 22-Nov-2006: fix bug, remove constructor with no parameter (GMO)
* - 15-Jan-2008: the color value now depends on the reservable bandwidth instead of on the reserved bandwidth (GMO)
*/

/**
* Shows the reserved bandwidth of the links for a given class type
* It supposes that link use a Diffserv model with MaxBC=MaxCT (MAM model and Russian dolls are in this category).
*
* <p>Creation date: 16 mars 2006
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class ReservedBandwidthColorShower implements LinkColorShower {

    private int classType;

    public ReservedBandwidthColorShower(int classType) {
        this.classType = classType;
    }

    /**
     * Returns the proportion of the link bandwidth that is not reservable.
     * @param lnk
     * @return
     */
    public float getColorValue(Link lnk) {
        if (lnk.getLinkStatus() == Link.STATUS_DOWN)   //link is down
            return -1;
        return (1- lnk.getReservableBandwidthCT(classType) / lnk.getBCs()[classType]);
        //old behaviour
        //return lnk.getReservedBandwidthCT(classType) / lnk.getBCs()[classType];
    }
}
