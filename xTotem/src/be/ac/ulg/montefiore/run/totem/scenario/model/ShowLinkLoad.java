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
package be.ac.ulg.montefiore.run.totem.scenario.model;

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.ShowLinkLoadImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.LinkLoadComputerManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadComputer;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LoadData;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.CosLoadData;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidLinkLoadComputerException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;
import org.apache.log4j.Logger;

import java.util.List;

/*
* Changes:
* --------
*/

/**
 *
 * Calculates and displays the load associated with a {@link LinkLoadComputer} that is present is the manager.
 * Load can be shown as aggregate information among all links, per link, per class of service depending on the parameters.
 * Aggregate is shown only for up links.
 *
 * TODO: IPLoad and MPLS load are not displayed separetely
 *
 * @see LinkLoadComputer
 * @see LinkLoadComputerManager
 *
 * <p>Creation date: 20/02/2008
 *
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 *
*/
public class ShowLinkLoad extends ShowLinkLoadImpl implements Event {
    private final static Logger logger = Logger.getLogger(ShowLinkLoad.class);

    private Domain domain;
    private StringBuffer sb;

    // corresponds to 2 digit decimal
    private final double precisionFactor = 100;

    public ShowLinkLoad() {
    }

    /**
     * Show the link load on a specified link for the default domain and the default linkLoadComputer
     * @param link
     */
    public ShowLinkLoad(Link link) {
        setLinkId(link.getId());
    }

    /**
     * Show the link load on a specified link for the default domain and the given linkLoadComputer
     * @param llcId The linkLoadComputer id from which to calculate load
     * @param link
     */
    public ShowLinkLoad(String llcId, Link link) {
        setLlcId(llcId);
        setLinkId(link.getId());
    }

    /**
     * Creates a new ShowLinkLoad object. It will display the link load for all links in the domain
     * identified by the given asId. If perLink is false, only aggregate information is shown.
     * @param asId
     * @param perLink
     */
    public ShowLinkLoad(int asId, boolean perLink) {
        setASID(asId);
        setPerLink(perLink);
    }

    /**
     * Creates a new ShowLinkLoad object. It will display the link load for all links in the domain
     * identified by the given asId. If perLink is false, only aggregate information is shown.
     * @param llcId
     * @param asId
     * @param perLink
     */
    public ShowLinkLoad(int asId, String llcId, boolean perLink) {
        setASID(asId);
        setLlcId(llcId);
        setPerLink(perLink);
    }

    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a ShowLinkLoad event.");

        sb = new StringBuffer();

        if(isSetASID()) {
            try {
                domain = InterDomainManager.getInstance().getDomain(getASID());
            } catch (InvalidDomainException e) {
                logger.error("Unknown domain "+getASID());
                throw new EventExecutionException(e);
            }
        } else {
            domain = InterDomainManager.getInstance().getDefaultDomain();
            if(domain == null) {
                logger.error("There is no default domain!");
                throw new EventExecutionException("No default domain.");
            }
        }

        LinkLoadComputer llc = null;
        if (isSetLlcId()) {
            try {
                llc = LinkLoadComputerManager.getInstance().getLinkLoadComputer(domain, getLlcId());
           } catch (InvalidLinkLoadComputerException e) {
                throw new EventExecutionException("Can't find link load computer with id: " + getLlcId());
           }
        } else {
            try {
                llc = LinkLoadComputerManager.getInstance().getDefaultLinkLoadComputer(domain);
            } catch (InvalidLinkLoadComputerException e) {
                throw new EventExecutionException("Can't find default link load computer for domain with id: " + domain.getASID());
            }
        }

        llc.update();

        LoadData data = llc.getData();

        printHeader();

        if (isSetLinkId()) {
            try {
                Link l = domain.getLink(getLinkId());
                if (isSetClassOfService()) {
                    if (!domain.isExistingClassOfService(getClassOfService())) {
                        throw new EventExecutionException("Class of service not found: " + getClassOfService());
                    }
                    if (!(data instanceof CosLoadData)) {
                        throw new EventExecutionException("The computed load do not include class of service data.");
                    }
                    printCos(l, (CosLoadData)data, getClassOfService());
                } else { //cos not set
                    if (isSetPerCos() && isPerCos()) {
                        if (!(data instanceof CosLoadData)) {
                            logger.error("Cannot display Cos load.");
                        } else {
                            for (String cos : domain.getClassesOfService()) {
                                printCos(l, (CosLoadData)data, cos);
                            }
                        }
                    }
                    printTotal(l, data);
                }
            } catch (LinkNotFoundException e) {
                throw new EventExecutionException("Link not found: " + getLinkId());
            }
        } else { // link Id not set
            if (!isSetPerLink() || isPerLink()) { // display per link
                if (isSetClassOfService()) {
                    if (!domain.isExistingClassOfService(getClassOfService())) {
                        throw new EventExecutionException("Class of service not found: " + getClassOfService());
                    }
                    if (!(data instanceof CosLoadData)) {
                        throw new EventExecutionException("The computed load do not include class of service data.");
                    }
                    for (Link l : domain.getAllLinks()) {
                        printCos(l, (CosLoadData)data, getClassOfService());
                    }
                } else { //cos not set
                    if (isSetPerCos() && isPerCos()) {
                        if (!(data instanceof CosLoadData)) {
                            logger.error("Cannot display Cos load.");
                            for (Link l : domain.getAllLinks()) {
                                printTotal(l, data);
                            }
                        } else {
                            for (Link l : domain.getAllLinks()) {
                                for (String cos : domain.getClassesOfService()) {
                                    printCos(l, (CosLoadData)data, cos);
                                }
                                printTotal(l, data);
                            }
                        }
                    } else {
                        for (Link l : domain.getAllLinks()) {
                            printTotal(l, data);
                        }
                    }
                }
            } else { //display aggregate info among all links
                if (isSetClassOfService()) {
                    if (!domain.isExistingClassOfService(getClassOfService())) {
                        throw new EventExecutionException("Class of service not found: " + getClassOfService());
                    }
                    if (!(data instanceof CosLoadData)) {
                        throw new EventExecutionException("The computed load do not include class of service data.");
                    }
                    printAgCos((CosLoadData)data, getClassOfService());
                } else { //cos not set
                    if (isSetPerCos() && isPerCos()) {
                        if (!(data instanceof CosLoadData)) {
                            logger.error("Cannot display Cos load.");
                        } else {
                            for (String cos : domain.getClassesOfService()) {
                                printAgCos((CosLoadData)data, cos);
                            }
                        }
                    }
                    printAgTotal(data);
                }
            }
        }

        return new EventResult(null, sb.toString());
    }

    private double round(double value) {
        return Math.round(value * precisionFactor) / precisionFactor;
    }

    private void printHeader() {
        sb.append("#");
        sb.append("LinkId");
        sb.append("\t");
        sb.append("Cos");
        sb.append("\t");
        sb.append("Capacity");
        sb.append("\t");
        sb.append("Load");
        sb.append("\t");
        sb.append("Util%");
        sb.append("\t");
        //sb.append("Dropped");
        sb.append("\n");
    }

    private void printCos(Link l, CosLoadData data, String cos) {
        sb.append(l.getId());
        sb.append("\t");
        sb.append(cos);
        sb.append("\t");
        sb.append(round(l.getMaximumBandwidth()));
        sb.append("\t");
        sb.append(round(data.getLoad(cos, l)));
        sb.append("\t");
        sb.append(round(data.getLoad(cos, l)/l.getMaximumBandwidth() * 100));
        sb.append("\t");
        try {
            sb.append(round(data.getDroppedTraffic(cos, l.getSrcNode())));
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
        sb.append("\n");
    }

    private void printTotal(Link l, LoadData data) {
        sb.append(l.getId());
        sb.append("\t");
        sb.append("<ALL>");
        sb.append("\t");
        sb.append(round(l.getMaximumBandwidth()));
        sb.append("\t");
        sb.append(round(data.getLoad(l)));
        sb.append("\t");
        sb.append(round(data.getLoad(l)/l.getMaximumBandwidth() * 100));
        sb.append("\t");
        try {
            sb.append(round(data.getDroppedTraffic(l.getSrcNode())));
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
        sb.append("\n");
    }

    private void printAgCos(CosLoadData data, String cos) {
        double[] load = data.getLoad(cos);
        double[] capacities = new double[domain.getConvertor().getMaxLinkId()];
        for (Link l : domain.getAllLinks()) {
            try {
                capacities[domain.getConvertor().getLinkId(l.getId())] = l.getMaximumBandwidth();
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }

        printStat(cos, load, capacities, data.getDroppedTraffic());
    }

    private void printAgTotal(LoadData data) {
        double[] capacities = new double[domain.getConvertor().getMaxLinkId()];
        for (Link l : domain.getAllLinks()) {
            try {
                capacities[domain.getConvertor().getLinkId(l.getId())] = l.getMaximumBandwidth();
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }

        printStat("<ALL>", data.getLoad(), capacities, data.getDroppedTraffic());
    }

    private void printStat(String cos, double[] load, double[] capacities, double droppedTraffic) {
        // use only up links
        List<Link> upLinks = domain.getUpLinks();
        double[] upLoad = new double[upLinks.size()];
        double[] upUtil = new double[upLinks.size()];
        double[] upCapa = new double[upLinks.size()];
        int i = 0;
        for (Link l : upLinks) {
            try {
                int id = domain.getConvertor().getLinkId(l.getId());
                upUtil[i] = load[id] / capacities[id] * 100;
                upLoad[i] = load[id];
                upCapa[i] = capacities[id];
                i++;
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }

        sb.append("MAX");
        sb.append("\t");
        sb.append(cos);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMaximum(upCapa)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMaximum(upLoad)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMaximum(upUtil)));
        sb.append("\t");
        sb.append("\n");

        sb.append("MEAN");
        sb.append("\t");
        sb.append(cos);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMeanValue(upCapa)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMeanValue(upLoad)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMeanValue(upUtil)));
        sb.append("\t");
        sb.append("\n");

        sb.append("STD");
        sb.append("\t");
        sb.append(cos);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getStandardDeviation(upCapa)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getStandardDeviation(upLoad)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getStandardDeviation(upUtil)));
        sb.append("\t");
        sb.append("\n");

        sb.append("percentile10");
        sb.append("\t");
        sb.append(cos);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile10(upCapa)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile10(upLoad)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile10(upUtil)));
        sb.append("\t");
        sb.append("\n");

        sb.append("percentile20");
        sb.append("\t");
        sb.append(cos);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(upCapa, 80)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(upLoad, 80)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(upUtil, 80)));
        sb.append("\t");
        sb.append("\n");

        sb.append("percentile30");
        sb.append("\t");
        sb.append(cos);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(upCapa, 70)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(upLoad, 70)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(upUtil, 70)));
        sb.append("\t");
        sb.append("\n");

        sb.append("percentile50");
        sb.append("\t");
        sb.append(cos);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(upCapa, 50)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(upLoad, 50)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(upUtil, 50)));
        sb.append("\t");
        sb.append("\n");

        sb.append("percentile90");
        sb.append("\t");
        sb.append(cos);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(upCapa, 10)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(upLoad, 10)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(upUtil, 10)));
        sb.append("\t");
        sb.append("\n");

        sb.append("\n");
        sb.append("Total Dropped traffic:");
        sb.append("\t");
        sb.append(droppedTraffic);
        sb.append("\n");
    }
}
