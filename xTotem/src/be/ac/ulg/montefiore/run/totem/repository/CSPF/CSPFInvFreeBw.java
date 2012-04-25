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
package be.ac.ulg.montefiore.run.totem.repository.CSPF;

import be.ac.ulg.montefiore.run.totem.domain.model.Link;

/*
 * Changes:
 * --------
 * 20-Mar-2006: add equals method (GMO).
 */

/**
 * A CSPF that use as metric the inverse of the reservable bandwidth.
 *
 * <p>Creation date: 03-Nov-2004
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class CSPFInvFreeBw extends CSPF {

    /**
     * Get the inverse of the reservable bandwidth
     *
     * @param link
     * @return
     */
    protected float getMetric(Link link) {
        return ((float) 1) / link.getReservableBandwidth();
    }

    public boolean equals(Object o) {
        if (!(o instanceof CSPFInvFreeBw))
            return false;
        return super.equals(o);
    }

}
