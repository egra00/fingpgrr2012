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
package be.ac.ulg.montefiore.run.totem.topgen.traffic.model;

import java.util.List;
import java.util.Iterator;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;

/*
 * Changes:
 * --------
 *  - 27-Apr-2007: bugfix: replace Node.getOutLink() by Node.getAllOutLink() (GMO)
 */

/**
 * This class can be used as a repulsion factor.
 *
 * <p>Creation date: 2004
 *  
 * @author  Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class CapacityRepulsionFactor implements RepulsionFactor {

    private static final Logger logger = Logger.getLogger(CapacityRepulsionFactor.class);
    
    /**
     * Generates traffic from the node <code>node</code>. The amount of traffic
     * is equal to the sum of the capacity of the input links of
     * <code>node</code>.
     */
    public double generate(Node node) {
	    float ret = 0;
        List<Link> inLinks = node.getAllInLink();
        for(Iterator<Link> it = inLinks.iterator(); it.hasNext();) {
            ret += it.next().getBandwidth();
        }
	    return ret;
    }

}
