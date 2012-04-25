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
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidPathException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.HybridLoadData;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.AbstractLoadData;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
 *
 * Class to represent IP and MPLS load separately.
 *
 * With this class, traffic is always considered to be dropped at source node.
 *
 * <p>Creation date: 29/01/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/
public class SettableHybridLoadData extends AbstractLoadData implements HybridLoadData {

    private double[] lspToTraffic;
    private double[] ipLoad;
    private double[] mplsLoad;
    // in this case, the traffic is always dropped at the ingress (source) node, as no admission control is performed.
    private HashMap<Pair<Node, Node>, Double> droppedTraffic;

    private Domain domain;

    private static final Logger logger = Logger.getLogger(SettableHybridLoadData.class);

    public SettableHybridLoadData(Domain domain) {
        this.domain = domain;
        clear();
    }

    /**
     * Clear the data and rebuild data structures.
     */
    public void clear() {
        lspToTraffic = new double[domain.getConvertor().getMaxLspId()];
        ipLoad = new double[domain.getConvertor().getMaxLinkId()];
        droppedTraffic = new HashMap<Pair<Node, Node>, Double>();
        mplsLoad = new double[domain.getConvertor().getMaxLinkId()];
    }

    /**
     * Add traffic follow the working path of the lsp.
     * @param lsp
     * @param traffic
     */
    public void addMPLSTraffic(Lsp lsp, double traffic) {
        if (lsp.isBackupLsp()) {
            logger.warn("Trying to add MPLS traffic to a backup lsp");
            return;
        }

        try {
            for (Link l : lsp.getWorkingPath().getLinkPath()) {
                int intId = domain.getConvertor().getLinkId(l.getId());
                mplsLoad[intId] += traffic;
            }

            try {
                int id = domain.getConvertor().getLspId(lsp.getId());
                lspToTraffic[id] += traffic;
            } catch (LspNotFoundException e) {
                e.printStackTrace();
            }

            for (Lsp bLsp : lsp.getActivatedBackups()) {
                try {
                    int id = domain.getConvertor().getLspId(bLsp.getId());
                    lspToTraffic[id] += traffic;
                } catch (LspNotFoundException e) {
                    e.printStackTrace();
                }
            }
        } catch (InvalidPathException e) {
            e.printStackTrace();
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add some ip traffic on the given link.
     * @param link
     * @param traffic
     */
    public void addIPTraffic(Link link, double traffic) {
        try {
            int id = domain.getConvertor().getLinkId(link.getId());
            ipLoad[id] += traffic;
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add traffic from flows from src to dst dropped at node src.
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


    public double[] getLoad() {
        double[] load = new double[domain.getConvertor().getMaxLinkId()];

        for (Link link : domain.getAllLinks()) {
            int intId = 0;
            try {
                intId = domain.getConvertor().getLinkId(link.getId());
                load[intId] = getIPLoad(link) + getMPLSLoad(link);
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }
        return load;
    }

    public double getLoad(Link lnk) {
        return getIPLoad(lnk) + getMPLSLoad(lnk);
    }

    public double[] getUtilization() {
        double[] load = getLoad();
        for (Link link : domain.getAllLinks()) {
            int intId = 0;
            try {
                intId = domain.getConvertor().getLinkId(link.getId());
                load[intId] /= link.getBandwidth();
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }
        return load;
    }

    public double getUtilization(Link link) {
        return getLoad(link)/link.getBandwidth();
    }

    public double[] getIPLoad() {
        double[] linkTraffic = new double[domain.getConvertor().getMaxLinkId()];
        for (Link link : domain.getAllLinks()) {
            try {
                linkTraffic[domain.getConvertor().getLinkId(link.getId())] = getIPLoad(link);
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }

        return linkTraffic;
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

    public double getIPLoad(Link link) {
        int intId = 0;
        try {
            intId = domain.getConvertor().getLinkId(link.getId());
            return ipLoad[intId];
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double[] getMPLSLoad() {
        double[] linkTraffic = new double[domain.getConvertor().getMaxLinkId()];
        for (Link link : domain.getAllLinks()) {
            try {
                linkTraffic[domain.getConvertor().getLinkId(link.getId())] = getMPLSLoad(link);
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }

        return linkTraffic;
    }

    public double getMPLSLoad(Link link) {
        try {
            int intId = domain.getConvertor().getLinkId(link.getId());
            if (intId >= mplsLoad.length) {
                logger.debug("Link not found, update the data");
                return 0;
            }
            return mplsLoad[intId];
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
            logger.fatal("Link not found!");
            return 0;
        }
    }

    public double getMPLSLoad(Lsp lsp) {
        try {
            int intId = domain.getConvertor().getLspId(lsp.getId());
            return lspToTraffic[intId];
        } catch (LspNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
