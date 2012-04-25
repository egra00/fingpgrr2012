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
package be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl;

import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.AbstractLoadData;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.IPLoadData;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import java.util.HashMap;
import java.util.Map;

/*
* Changes:
* --------
*
*/

/**
* Class to represent IP data.
*
* <p>Creation date: 30/01/2008
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class SettableIPLoadData extends AbstractLoadData implements IPLoadData {
    private Domain domain;

    private double[] load;
    // in this case, the traffic is always dropped at the ingress (source) node, as no admission control is performed.
    private HashMap<Pair<Node, Node>, Double> droppedTraffic;

    public SettableIPLoadData(Domain domain) {
        this.domain = domain;
        load = new double[domain.getConvertor().getMaxLinkId()];
        droppedTraffic = new HashMap<Pair<Node, Node>, Double>();
    }

    /**
     * Add traffic and the given link
     * @param l
     * @param traffic
     */
    public void addTraffic(Link l, double traffic) {
        try {
            int id = domain.getConvertor().getLinkId(l.getId());
            load[id] += traffic;
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add dropped traffic from flows from src to dst
     * @param src
     * @param dst
     * @param traffic
     */
    public void addDroppedTraffic(Node src, Node dst, double traffic) {
        Pair<Node, Node> pair = new Pair<Node, Node>(src, dst);
        Double exTraffic = droppedTraffic.get(pair);
        if (exTraffic == null) {
            droppedTraffic.put(pair, new Double(traffic));
        } else {
            droppedTraffic.put(pair, new Double(traffic + exTraffic.doubleValue()));
        }
    }

    /**
     * Clear the data and rebuild the data structure.
     */
    public void clear() {
        droppedTraffic.clear();
        load = new double[domain.getConvertor().getMaxLinkId()];
    }

    public double getIPLoad(Link link) {
        try {
            int id = domain.getConvertor().getLinkId(link.getId());
            return load[id];
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double[] getIPLoad() {
        return load.clone();
    }

    public double[] getLoad() {
        return getIPLoad();
    }

    public double getLoad(Link lnk) {
        return getIPLoad(lnk);
    }

    public double[] getUtilization() {
        double[] load = this.load.clone();
        for (Link l : domain.getAllLinks()) {
            try {
                int id = domain.getConvertor().getLinkId(l.getId());
                load[id] /= l.getBandwidth();
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }
        return load;
    }

    public double getUtilization(Link link) {
        return getIPLoad(link)/link.getBandwidth();
    }

    public TrafficMatrix getDroppedTrafficMatrix() {
        TrafficMatrix tm = new TrafficMatrixImpl(domain);

        for (Map.Entry<Pair<Node, Node>, Double> entry : droppedTraffic.entrySet()) {
            try {
                tm.set(entry.getKey().getFirst().getId(), entry.getKey().getSecond().getId(), entry.getValue().floatValue());
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
        }

        return tm;
    }

    public TrafficMatrix getDroppedTrafficMatrix(Node node) {
        TrafficMatrix tm = new TrafficMatrixImpl(domain);

        for (Map.Entry<Pair<Node, Node>, Double> entry : droppedTraffic.entrySet()) {
            try {
                if (node.getId().equals(entry.getKey().getFirst().getId())) {
                    tm.set(entry.getKey().getFirst().getId(), entry.getKey().getSecond().getId(), entry.getValue().floatValue());
                }
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
        }

        return tm;
    }
    
    public double getDroppedTraffic(Node node) {
        double value = 0;
        for (Map.Entry<Pair<Node, Node>, Double> entry : droppedTraffic.entrySet()) {
            Pair<Node, Node> pair = entry.getKey();
            if (pair.getFirst().equals(node)) {
                value += entry.getValue().doubleValue();
            }
        }
        return value;
    }

    public double getDroppedTraffic() {
        double value = 0;
        for (Double elem : droppedTraffic.values()) {
            value += elem.doubleValue();
        }
        return value;
    }
}
