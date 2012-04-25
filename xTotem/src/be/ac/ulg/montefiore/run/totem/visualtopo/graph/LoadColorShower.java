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
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LoadData;

/*
* Changes:
* --------
* - 28-Feb-2008: Use a LoadDatta object instead of a LinkLoadComputer (GMO)
*/

/**
 * Shows the load of the links from the LoadData object given in the constructor.
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 * @see LoadData
 *      <p/>
 *      <p>Creation date: 16 mars 2006
 */
public class LoadColorShower implements LinkColorShower {
    private LoadData ldv = null;

    public LoadColorShower(LoadData ldv) {
        this.ldv = ldv;
    }

    public float getColorValue(Link lnk) {
        if (lnk.getLinkStatus() == Link.STATUS_DOWN)   //link is down
            return -1;
        float div = -1;
        try {
            div = (float) ldv.getUtilization(lnk);
        } catch (NullPointerException e1) {
        }
        return div;
    }
}
