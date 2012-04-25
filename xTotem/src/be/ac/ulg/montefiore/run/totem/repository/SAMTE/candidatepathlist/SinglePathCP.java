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
package be.ac.ulg.montefiore.run.totem.repository.SAMTE.candidatepathlist;

import be.ac.ulg.montefiore.run.totem.domain.simplifiedDomain.SimplifiedPath;

/*
 * Changes:
 * --------
 * 
 */

/**
 * <p>Creation date: 25-Feb-2005 09:57:08
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class SinglePathCP extends CandidatePath {

    private SimplifiedPath path;

    public SinglePathCP(SimplifiedPath path) {
        this.path = path;
    }

    public SimplifiedPath getPath() {
        return path;
    }

    public void display() {
        System.out.println("Path : " + path.toString());
    }

    public String toString() {
        return path.toString();    
    }

}
