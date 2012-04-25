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
package be.ac.ulg.montefiore.run.totem.repository.SAMTE.model;

import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedDomain;
import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedPath;

/*
 * Changes:
 * --------
 * 
 */

/**
 * <p>Creation date: 24-Feb-2005 15:28:16
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class ExtendedPath extends SimplifiedPath implements Cloneable {

    private FEC fec = null;

    public ExtendedPath(SimplifiedDomain domain, int[] linkIdPath, FEC fec) {
        super.domain = domain;
        super.linkIdPath = linkIdPath;
        this.fec = fec;
    }

    public int getIngress() throws LinkNotFoundException {
        return domain.getLinkSrc(linkIdPath[0]);
    }

    public int getEgress() throws LinkNotFoundException {
        return domain.getLinkDst(linkIdPath[linkIdPath.length-1]);
    }

    public FEC getFec() {
        return fec;
    }

    public boolean match(TrafficDescriptor td) {
        return fec.match(td);   
    }

    public Object clone() {
        FEC fec = (FEC) ((IntDstNodeFEC) this.fec).clone();
        int[] newLinkIdPath = new int[linkIdPath.length];
        for (int i = 0; i < newLinkIdPath.length; i++) {
            newLinkIdPath[i] = this.getLinkIdPath()[i];
        }
        ExtendedPath ePath = new ExtendedPath(domain,newLinkIdPath,fec);
        return ePath;
    }

    public boolean equals(ExtendedPath ePath) {
        if ((!super.equals(ePath)) || (!fec.equals(ePath.getFec())))
            return false;
        return true;
    }


}
