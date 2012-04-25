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

import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidPathException;
import be.ac.ulg.montefiore.run.totem.domain.model.Path;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.DomainConvertor;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.PathImpl;

import java.util.List;
import java.util.ArrayList;

/*
 * Changes:
 * --------
 * 15-Feb-2005  : Add constructor with path as an ArrayList<Integer> of link Id (FSK)
 * 20-Mar-2006 : Add hashCode() method (GMO)
 * 31-Mar-2006 : convert now throws InvalidPathException (GMO)
 */

/**
 * Define a simple path as a list of link or node int ID.
 *
 * <p>Creation date: 26-Jan-2005 16:52:30
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class SimplifiedPath {

    protected int[] linkIdPath;
    protected SimplifiedDomain domain;

    public SimplifiedPath() {
        linkIdPath = null;
        domain = null;
    }

    public SimplifiedPath(SimplifiedDomain domain, int[] linkIdPath) {
        this.linkIdPath = linkIdPath;
        this.domain = domain;
    }

    public SimplifiedPath(SimplifiedDomain domain, ArrayList<Integer> linkIdPath) {
        this.domain = domain;
        this.linkIdPath = new int[linkIdPath.size()];
        for (int i = 0; i < linkIdPath.size(); i++) {
            this.linkIdPath[i] = linkIdPath.get(i).intValue();
        }
    }

    public int[] getLinkIdPath() {
        return linkIdPath;
    }

    public SimplifiedDomain getDomain() {
        return domain;
    }

    public boolean isDisjoint(SimplifiedPath p2) throws LinkNotFoundException {
        int path[] = this.getLinkIdPath();
        int path2[] = p2.getLinkIdPath();
        for (int i = 0; i < path.length; i++) {
            for (int j = 0; j < path2.length; j++) {
                if ((path[i] == path2[j]) || ((domain.getLinkSrc(path[i]) == domain.getLinkDst(path2[j])) &&
                        (domain.getLinkSrc(path2[j]) == domain.getLinkDst(path[i]))))
                    return false;

            }
        }
        return true;
    }

    public boolean equals(SimplifiedPath p2) {
        int path[] = this.getLinkIdPath();
        int path2[] = p2.getLinkIdPath();
        if (path.length != path2.length)
            return false;
        for (int i = 0; i < path.length; i++) {
            if (path[i] != path2[i])
                return false;
        }
        return true;
    }

    public boolean equals(Object o) {
        if (!(o instanceof SimplifiedPath)) return false;
        else return equals((SimplifiedPath) o);
    }

    public int hashCode() {
        int result = getLinkIdPath().length;
        for (int r : getLinkIdPath()) {
            result = 29 * result + r;
        }
        return result;
    }

    public Path convert(Domain domain) throws LinkNotFoundException, NodeNotFoundException, InvalidPathException {
        DomainConvertor convertor = domain.getConvertor();
        List<Link> linkList = new ArrayList<Link>(linkIdPath.length);
        for (int i = 0; i < linkIdPath.length; i++) {
            linkList.add(domain.getLink(convertor.getLinkId(linkIdPath[i])));
        }
        Path path = new PathImpl(domain);
        path.createPathFromLink(linkList);
        return path;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("[");
        for (int i = 0; i < linkIdPath.length; i++) {
            sb.append(" " + linkIdPath[i]);
        }
        sb.append(" ]");
        return sb.toString();
    }
}
