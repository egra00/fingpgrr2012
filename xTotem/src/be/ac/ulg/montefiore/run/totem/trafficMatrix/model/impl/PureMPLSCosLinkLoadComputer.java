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

import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LoadData;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.CosMPLSLoadData;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.TrafficMatrixIdException;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidPathException;

import java.util.*;

import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
 * TODO: warning or error if 2 matrices are routed for different classes.
 *
 * This LinkLoadComputer computes MPLS load by routing some traffic matrices, each associated with a class of service.
 * An element (src, dst) of the traffic matrix is routed on a primary lsp if the lsp path is from src to dst and that
 * it accepts the associated cos (bandwidth reservation is not taken into account). 
 * 
 * <p>Creation date: 24/01/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 */
public class PureMPLSCosLinkLoadComputer extends AbstractLinkLoadComputer {
    private final static Logger logger = Logger.getLogger(PureMPLSCosLinkLoadComputer.class);

    private HashMap<String, TrafficMatrix> cosToTrafficMatrix;

    private SettableCosMPLSLoadData data;

    // is listening to events
    private boolean listening = false;

    public static enum LOAD_BALANCING_OPTION {
        EQUAL_SPLITTING
    }

    private LOAD_BALANCING_OPTION loadBalOption = LOAD_BALANCING_OPTION.EQUAL_SPLITTING;


    public PureMPLSCosLinkLoadComputer(Domain domain) {
        super(domain, null);
        changeListener = new LinkLoadComputerInvalidator(this){
            public void addLspEvent(Lsp lsp) {
                if (lsp.getLspStatus() == Lsp.STATUS_UP)
                    llc.invalidate();
            }

            public void lspWorkingPathChangeEvent(Lsp lsp) {
                if (lsp.getLspStatus() == Lsp.STATUS_UP)
                    llc.invalidate();
            }

            public void removeLspEvent(Lsp lsp) {
                if (lsp.getLspStatus() == Lsp.STATUS_UP)
                    llc.invalidate();
            }

            public void lspStatusChangeEvent(Lsp lsp) {
                llc.invalidate();
            }
        };
        data = new SettableCosMPLSLoadData(domain);
        cosToTrafficMatrix = new HashMap<String, TrafficMatrix>();
    }

    /**
     * Add a traffic matrix to be routed with the given class of service.
     * @param tm
     * @param cos
     */
    public void addTrafficMatrix(TrafficMatrix tm, String cos) {
        if (tm == null || cos == null) {
            throw new IllegalArgumentException("tm or cos is null");
        }

        if (tm.getASID() != domain.getASID()) {
            throw new IllegalArgumentException("Traffic matrix does not correpond to the domain.");
        }

        if (cosToTrafficMatrix.containsKey(cos)) {
            //TODO: throw another exception
            throw new IllegalArgumentException("Traffic already exists for that class of service");
        }
        if (listening) stopListening();
        cosToTrafficMatrix.put(cos, tm);
        if (listening) startListening();
        invalidate();
    }


    private void computeTraffic(String cos) {
        TrafficMatrix tm = cosToTrafficMatrix.get(cos);

        for (Node src : domain.getAllNodes()) {
            for (Node dst : domain.getAllNodes()) {
                if (src.equals(dst)) continue;
                try {
                    double traffic = tm.get(src.getId(), dst.getId());
                    List<Lsp> lsps = new ArrayList<Lsp>();

                    for (Lsp lsp : domain.getPrimaryLsps(src, dst)) {
                        try {
                            if (lsp.acceptClassOfService(cos)) {
                                lsp.getWorkingPath();
                                lsps.add(lsp);
                            }
                        } catch (InvalidPathException e) {
                            //e.printStackTrace();
                        }
                    }

                    if (lsps.size() <= 0) {
                        logger.warn("No lsps between nodes " + src.getId() + " and " + dst.getId() + " for class of service " + cos);
                        data.setDroppedTraffic(cos, src, dst, traffic);
                    } else if (lsps.size() == 1) {
                        data.setTraffic(cos, lsps.get(0), traffic);
                    } else {
                        switch (loadBalOption) {
                            case EQUAL_SPLITTING:
                                double trafficPerLsp = traffic / lsps.size();
                                for (Lsp lsp : lsps) {
                                    data.setTraffic(cos, lsp, trafficPerLsp);
                                }
                            default:
                                logger.fatal("Unknown loadbalancing option " + loadBalOption.toString());
                        }
                    }
                } catch (NodeNotFoundException e) {
                    e.printStackTrace();
                    logger.debug("Unexpected Node not found exception: " + e.getMessage());
                }
            }
        }

    }

    public void startListening() {
        if (!listening) {
            super.startListening();
            listening = true;
        }
    }

    public void stopListening() {
        if (listening) {
            super.stopListening();
            listening = false;
        }
    }

    /**
     * Recomputes the traffic for all given traffic matrices.
     */
    public void recompute() {
        if (data == null) {
            data = new SettableCosMPLSLoadData(domain);
        }
        data.clear();
        for (String cos : cosToTrafficMatrix.keySet()) {
            computeTraffic(cos);
        }
        dataChanged();
    }

    public List<TrafficMatrix> getTrafficMatrices() {
        ArrayList<TrafficMatrix> list = new ArrayList<TrafficMatrix>(cosToTrafficMatrix.values());
        return list;
    }

    public String getShortName() {
        return toString();
    }

    public CosMPLSLoadData getData() {
        return data;
    }

    public LoadData detachData() {
        SettableCosMPLSLoadData oldData = data;
        this.data = null;
        return oldData;
    }

    public String toString() {
        String name = "Cos Pure MPLS";

        for (Map.Entry<String, TrafficMatrix> entry : cosToTrafficMatrix.entrySet()) {
            try {
                name += " (cos:" + entry.getKey() + ", tm:" + entry.getValue().getTmId() + ")";
            } catch (TrafficMatrixIdException e) {
                logger.error(e);
            }
        }

        return name;
    }

    public void destroy() {
        super.destroy();
        cosToTrafficMatrix.clear();
    }

    public boolean equals(Object obj) {
        if (cosToTrafficMatrix == obj) return true;

        if (!(obj instanceof PureMPLSCosLinkLoadComputer))
            return false;

        if (!cosToTrafficMatrix.entrySet().equals(((PureMPLSCosLinkLoadComputer)obj).cosToTrafficMatrix.entrySet()))
            return false;

        return true;
    }
}


