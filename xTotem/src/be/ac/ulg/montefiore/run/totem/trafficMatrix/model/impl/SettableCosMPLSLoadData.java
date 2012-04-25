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

import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.CosMPLSLoadData;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.AbstractLoadData;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidPathException;
import be.ac.ulg.montefiore.run.totem.util.Pair;

import java.util.*;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
 * Used to represent mpls load for different classes of service.
 *
 * With this class, the traffic dropped is always cosidered to be dropped at source node.
 *
 *
 * <p>Creation date: 30/01/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class SettableCosMPLSLoadData extends AbstractLoadData implements CosMPLSLoadData {
    private final static Logger logger = Logger.getLogger(SettableCosMPLSLoadData.class);

    // traffic carried on the lsp, the path used by the traffic on the lsp might not be the lsp path or the current working path (if data are outdated)
    private List<SettableCosMPLSLoadData.Elem> lspToTraffic[];
    // working path of each lsp
    //private Path lspToPath[];

    // in this case, the traffic is always dropped at the ingress (source) node, as no admission control is performed.
    private HashMap<Pair<Node, Node>, List<Elem>> droppedTraffic;

    // associate a cos to the traffic for each link of the domain
    private HashMap<String, double[]> cosToLinkTraffic;

    private Domain domain;

    private class Elem {
        public Elem(String cos, double traffic) {
            this.cos = cos;
            this.traffic = traffic;
        }

        private String cos;
        private double traffic;

        public boolean equals(Object obj) {
            return cos.equals(cos);
        }

        public String getCos() {
            return cos;
        }

        public double getTraffic() {
            return traffic;
        }

        public void setTraffic(double traffic) {
            this.traffic = traffic;
        }
    }

    public SettableCosMPLSLoadData(Domain domain) {
        this.domain = domain;
        clear();
    }

    /**
     * Clear the data and rebuild data structures.
     */
    public void clear() {
        droppedTraffic = new HashMap<Pair<Node, Node>, List<Elem>>();
        cosToLinkTraffic = new HashMap<String, double[]>(8);
        lspToTraffic = new List[domain.getConvertor().getMaxLspId()];
        //lspToPath = new Path[domain.getConvertor().getMaxLspId()];
    }

    public double getMPLSLoad(String cos, Lsp lsp) {
        try {
            int lspIntId = domain.getConvertor().getLspId(lsp.getId());

            if (lspIntId >= lspToTraffic.length) {
                logger.debug("Inexistent LSP in db: " + lsp.getId());
                return 0;
            }
            List<Elem> list = lspToTraffic[lspIntId];

            if (list == null)
                return 0;

            for (Elem elem : list) {
                if (elem.getCos().equals(cos)) {
                    return elem.getTraffic();
                }
            }
        } catch (LspNotFoundException e) {
            e.printStackTrace();
            logger.error("Unexpected Lsp not found exception: " + e.getMessage());
        }

        return 0;
    }

    public double getMPLSLoad(String cos, Link l) {
        double[] linksLoad = cosToLinkTraffic.get(cos);
        if (linksLoad == null) {
            return 0;
        }

        try {
            int intId = domain.getConvertor().getLinkId(l.getId());
            if (intId >= linksLoad.length) {
                logger.debug("Link not found, update the data");
                return 0;
            }
            return linksLoad[intId];
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
            logger.fatal("Link not found!");
            return 0;
        }
    }

    public double[] getMPLSLoad(String cos) {
        double[] load = new double[domain.getConvertor().getMaxLinkId()];
        for (Link link : domain.getAllLinks()) {
            try {
                load[domain.getConvertor().getLinkId(link.getId())] = getMPLSLoad(cos, link);
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }
        return load;
    }

    public double getMPLSLoad(Link link) {
        double traffic = 0;

        for (String cos : cosToLinkTraffic.keySet()) {
            traffic += getMPLSLoad(cos, link);
        }
        return traffic;
    }

    public double getMPLSLoad(Lsp lsp) {
        double traffic = 0;

        try {
            int lspIntId = domain.getConvertor().getLspId(lsp.getId());

            if (lspIntId >= lspToTraffic.length) {
                logger.debug("Lsp not found in db for lsp: " + lsp.getId());
                return 0;
            }
            List<Elem> list = lspToTraffic[lspIntId];

            if (list != null) {
                for (Elem elem : list) {
                    traffic += elem.getTraffic();
                }
            }
        } catch (LspNotFoundException e) {
            e.printStackTrace();
            logger.error("Unexpected Lsp not found exception: " + e.getMessage());
        }

        return traffic;
    }

    public double[] getMPLSLoad() {
        double[] load = new double[domain.getConvertor().getMaxLinkId()];
        for (Link link : domain.getAllLinks()) {
            try {
                load[domain.getConvertor().getLinkId(link.getId())] = getMPLSLoad(link);
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }
        return load;
    }

    public double[] getLoad() {
        return getMPLSLoad();
    }

    public double getLoad(Link lnk) {
        return getMPLSLoad(lnk);
    }

    public double[] getUtilization() {
        double[] load = new double[domain.getConvertor().getMaxLinkId()];

        for (Link l : domain.getAllLinks()) {
            try {
                load[domain.getConvertor().getLinkId(l.getId())] = getUtilization(l);
            } catch (LinkNotFoundException e) {
                logger.error("Link not found");
            }
        }

        return load;
    }

    public double getUtilization(Link link) {
        return getLoad(link)/link.getBandwidth();
    }

    public TrafficMatrix getDroppedTrafficMatrix(Node node) {
        TrafficMatrix tm = new TrafficMatrixImpl(domain);

        for (Map.Entry<Pair<Node, Node>, List<Elem>> entry : droppedTraffic.entrySet()) {
            Pair<Node, Node> pair = entry.getKey();
            if (pair.getFirst().equals(node)) {
                double value = 0;
                for (Elem elem : entry.getValue()) {
                    value += elem.getTraffic();
                }
                try {
                    if (tm.get(node.getId(), pair.getSecond().getId()) != 0.0f) {
                        logger.error("Multiple values found for src/dst " + node.getId() + "/"  + pair.getSecond().getId());
                    }
                    tm.set(node.getId(), pair.getSecond().getId(), (float)value);
                } catch (NodeNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return tm;
    }

    public TrafficMatrix getDroppedTrafficMatrix() {
        TrafficMatrix tm = new TrafficMatrixImpl(domain);

        for (Map.Entry<Pair<Node, Node>, List<Elem>> entry : droppedTraffic.entrySet()) {
            Pair<Node, Node> pair = entry.getKey();
            double value = 0;
            for (Elem elem : entry.getValue()) {
                value += elem.getTraffic();
            }
            try {
                if (tm.get(pair.getFirst().getId(), pair.getSecond().getId()) != 0.0f) {
                    logger.error("Multiple values found for src/dst " + pair.getFirst().getId() + "/" + pair.getSecond().getId());
                }
                tm.set(pair.getFirst().getId(), pair.getSecond().getId(), (float) value);
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
        }

        return tm;
    }

    public double getDroppedTraffic(Node node) {
        double value = 0;
        for (Map.Entry<Pair<Node, Node>, List<Elem>> entry : droppedTraffic.entrySet()) {
            Pair<Node, Node> pair = entry.getKey();
            if (pair.getFirst().equals(node)) {
                for (Elem elem : entry.getValue()) {
                    value += elem.getTraffic();
                }
            }
        }
        return value;
    }

    public double getDroppedTraffic() {
        double value = 0;
        for (List<Elem> elems : droppedTraffic.values()) {
            for (Elem elem : elems) {
                value += elem.getTraffic();
            }
        }
        return value;
    }

    public double[] getLoad(String cos) {
        return getMPLSLoad(cos);
    }

    public double getLoad(String cos, Link lnk) {
        return getMPLSLoad(cos, lnk);
    }

    public TrafficMatrix getDroppedTrafficMatrix(String cos, Node node) {
        TrafficMatrix tm = new TrafficMatrixImpl(domain);

        for (Map.Entry<Pair<Node, Node>, List<Elem>> entry : droppedTraffic.entrySet()) {
            Pair<Node, Node> pair = entry.getKey();
            if (pair.getFirst().equals(node)) {
                double value = 0;
                for (Elem elem : entry.getValue()) {
                    if (elem.getCos().equals(cos)) {
                        value = elem.getTraffic();
                    }
                }
                if (value != 0) {
                    try {
                        if (tm.get(node.getId(), pair.getSecond().getId()) != 0.0f) {
                            logger.error("Multiple values found for src/dst " + node.getId() + "/"  + pair.getSecond().getId());
                        }
                        tm.set(node.getId(), pair.getSecond().getId(), (float)value);
                    } catch (NodeNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return tm;
    }

    public TrafficMatrix getDroppedTrafficMatrix(String cos) {
        TrafficMatrix tm = new TrafficMatrixImpl(domain);

        for (Map.Entry<Pair<Node, Node>, List<Elem>> entry : droppedTraffic.entrySet()) {
            Pair<Node, Node> pair = entry.getKey();
                double value = 0;
                for (Elem elem : entry.getValue()) {
                    if (elem.getCos().equals(cos)) {
                        value = elem.getTraffic();
                    }
                }
                if (value != 0) {
                    try {
                        if (tm.get(pair.getFirst().getId(), pair.getSecond().getId()) != 0.0f) {
                            logger.error("Multiple values found for src/dst " + pair.getFirst().getId() + "/"  + pair.getSecond().getId());
                        }
                        tm.set(pair.getFirst().getId(), pair.getSecond().getId(), (float)value);
                    } catch (NodeNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        return tm;
    }

    public double getDroppedTraffic(String cos, Node node) {
        double value = 0;
        for (Map.Entry<Pair<Node, Node>, List<Elem>> entry : droppedTraffic.entrySet()) {
            Pair<Node, Node> pair = entry.getKey();
            if (pair.getFirst().equals(node)) {
                for (Elem elem : entry.getValue()) {
                    if (elem.getCos().equals(cos)) {
                        value += elem.getTraffic();
                    }
                }
            }
        }
        return value;
    }

    public double getDroppedTraffic(String cos) {
        double value = 0;
        for (Map.Entry<Pair<Node, Node>, List<Elem>> entry : droppedTraffic.entrySet()) {
                for (Elem elem : entry.getValue()) {
                    if (elem.getCos().equals(cos)) {
                        value += elem.getTraffic();
                    }
                }
        }
        return value;
    }


    /**
     * Set traffic dropped from flows between src and dst with class of service cos. The traffic dropped is always
     * cosidered to be dropped at source node.
     *
     * @param cos
     * @param src
     * @param dst
     * @param traffic
     */
    public void setDroppedTraffic(String cos, Node src, Node dst, double traffic) {
        Elem elem = new Elem(cos, traffic);
        Pair<Node, Node> pair = new Pair<Node, Node>(src, dst);
        List<Elem> list = droppedTraffic.get(pair);
        if (list == null) {
            list = new ArrayList<Elem>();
            droppedTraffic.put(pair, list);
        }
        if (list.contains(elem)) {
            logger.error("Dropped traffic already contains a value for src/dst pair: " + src.getId() + " " + dst.getId());
        }

        list.add(elem);
    }

    /**
     * Sets the traffic on the lsp for the given cos. The traffic is considered to be flowing on the lsp working path
     * at the moment of the call.
     *
     * @param cos
     * @param lsp
     * @param traffic
     */
    public void setTraffic(String cos, Lsp lsp, double traffic) {
        Elem newElem = new Elem(cos, traffic);

        try {

            double[] linksLoad = cosToLinkTraffic.get(cos);
            if (linksLoad == null) {
                linksLoad = new double[domain.getConvertor().getMaxLinkId()];
                cosToLinkTraffic.put(cos, linksLoad);
            }
            for (Link l : lsp.getWorkingPath().getLinkPath()) {
                int intId = domain.getConvertor().getLinkId(l.getId());
                linksLoad[intId] += traffic;
            }

            int lspIntId = domain.getConvertor().getLspId(lsp.getId());

            List<Elem> list = lspToTraffic[lspIntId];

            if (list == null) {
                list = new ArrayList<Elem>();
                lspToTraffic[lspIntId] = list;
            }

            if (list.contains(newElem)) {
                logger.warn("Traffic for cos " + cos + " already exists on lsp " + lsp + ". It will be replaced.");
            }

            list.add(newElem);
        } catch (LspNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidPathException e) {
            e.printStackTrace();
        } catch (LinkNotFoundException e) {
            e.printStackTrace();
        }
    }
}
