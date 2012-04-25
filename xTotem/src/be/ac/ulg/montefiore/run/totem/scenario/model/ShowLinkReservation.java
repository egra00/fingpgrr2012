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

import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.ShowLinkReservationImpl;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;
import org.apache.log4j.Logger;

/*
* Changes:
* --------
*
*/

/**
* Show the current reservation. Reservation can be shown as aggregate information among all links, per link, per
* class type, per priority level depending on the parameters.  
*
* <p>Creation date: 19/02/2008
*
* @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
*/

public class ShowLinkReservation extends ShowLinkReservationImpl implements Event {
    private final static Logger logger = Logger.getLogger(ShowLinkReservation.class);

    private Domain domain;
    private StringBuffer sb;

    // corresponds to 2 digit decimal
    private final double precisionFactor = 100;

    public ShowLinkReservation() {
    }

    /**
     * Show the link reservation on a specified link
     * @param link
     */
    public ShowLinkReservation(Link link) {
        setLinkId(link.getId());
    }

    /**
     * Creates a new ShowLinkReservation object. It will display the link reservation for all links in the domain
     * identified by the given asId. If perLink is false, only aggregate information is shown.
     * @param asId
     * @param perLink
     */
    public ShowLinkReservation(int asId, boolean perLink) {
        setASID(asId);
        setPerLink(perLink);
    }

    /**
     * This method must be implemented by each event. This method contains what must be done to
     * process the event.
     */
    public EventResult action() throws EventExecutionException {
        logger.debug("Processing a ShowLinkReservation event.");

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

        printHeader();
        if (isSetLinkId()) {
            try {
                Link l = domain.getLink(getLinkId());
                if (isSetClassType()) {
                    if (isSetPreemptionLevel()) {
                        if (domain.isExistingPriority(getPreemptionLevel(), getClassType())) {
                            int priority = domain.getPriority(getPreemptionLevel(), getClassType());
                            printPrio(l, priority);
                        } else {
                            throw new EventExecutionException("Priority does not exists for CT: " + getClassType() + " and pLevel: " + getPreemptionLevel());
                        }
                    } else { // no pl
                        boolean ctExist = false;
                        for (int ct : domain.getAllCTId()) {
                            if (ct == getClassType()) {
                                ctExist = true;
                                break;
                            }
                        }
                        if (!ctExist) throw new EventExecutionException("Specified Classtype does not exists: " + getClassType());
                        if (isSetPerPrio() && isPerPrio()) {
                            for (int prio : domain.getPrioritySameCT(getClassType())) {
                                printPrio(l, prio);
                            }
                        }
                        printCT(l, getClassType());
                    }
                } else { // no classtype set
                    if (isSetPreemptionLevel()) {
                        throw new EventExecutionException("Please also specify the classtyep when you specify a preemption level");
                    } else { // no ct, no pl
                        if (!isSetPerPrio() && !isSetPerCT() && domain.getAllCTId().length != 1) {
                            // use per ct
                            for (int ct : domain.getAllCTId()) {
                                printCT(l, ct);
                            }
                        } else {
                            if (isSetPerPrio() && isPerPrio()) {
                                // use per prio
                                for (int prio : domain.getPriorities()) {
                                    printPrio(l, prio);
                                }
                            }
                            if (isSetPerCT() && isPerCT()) {
                                // use per ct
                                for (int ct : domain.getAllCTId()) {
                                    printCT(l, ct);
                                }
                            }
                        }
                        // use total
                        printTotal(l);
                    }
                }
            } catch (LinkNotFoundException e) {
                throw new EventExecutionException("Link not found: " + getLinkId());
            }
        } else { // link not given
            if (!isSetPerLink() || isPerLink()) {
                //info per link
                if (isSetClassType()) {
                    if (isSetPreemptionLevel()) {
                        int priority;
                        if (domain.isExistingPriority(getPreemptionLevel(), getClassType())) {
                            priority = domain.getPriority(getPreemptionLevel(), getClassType());
                        } else {
                            throw new EventExecutionException("Priority does not exists for CT: " + getClassType() + " and pLevel: " + getPreemptionLevel());
                        }
                        for (Link l : domain.getAllLinks()) {
                            printPrio(l, priority);
                        }
                    } else { // no pl
                        boolean ctExist = false;
                        for (int ct : domain.getAllCTId()) {
                            if (ct == getClassType()) {
                                ctExist = true;
                                break;
                            }
                        }
                        if (!ctExist) throw new EventExecutionException("Specified Classtype does not exists: " + getClassType());
                        for (Link l : domain.getAllLinks()) {
                            if (isSetPerPrio() && isPerPrio()) {
                                for (int prio : domain.getPrioritySameCT(getClassType())) {
                                    printPrio(l, prio);
                                }
                            }
                            printCT(l, getClassType());
                        }
                    }
                } else { // no ct
                    if (isSetPreemptionLevel()) {
                        throw new EventExecutionException("Please also specify the classtyep when you specify a preemption level");
                    } else { // no ct, no pl
                        if (!isSetPerPrio() && !isSetPerCT() && domain.getAllCTId().length != 1) {
                            // use per ct
                            for (Link l : domain.getAllLinks()) {
                                for (int ct : domain.getAllCTId()) {
                                    printCT(l, ct);
                                }
                                // use total
                                printTotal(l);
                            }
                        } else {
                            for (Link l : domain.getAllLinks()) {
                                if (isSetPerPrio() && isPerPrio()) {
                                    // use per prio
                                    for (int prio : domain.getPriorities()) {
                                        printPrio(l, prio);
                                    }
                                }
                                if (isSetPerCT() && isPerCT()) {
                                    // use per ct
                                    for (int ct : domain.getAllCTId()) {
                                        printCT(l, ct);
                                    }
                                }
                                // use total
                                printTotal(l);
                            }
                        }
                    }
                }
            } else { // aggregate info
                if (isSetClassType()) {
                    if (isSetPreemptionLevel()) {
                        if (domain.isExistingPriority(getPreemptionLevel(), getClassType())) {
                            int priority = domain.getPriority(getPreemptionLevel(), getClassType());
                            printAgPrio(priority);
                        } else {
                            throw new EventExecutionException("Priority does not exists for CT: " + getClassType() + " and pLevel: " + getPreemptionLevel());
                        }
                    } else { // no pl
                        boolean ctExist = false;
                        for (int ct : domain.getAllCTId()) {
                            if (ct == getClassType()) {
                                ctExist = true;
                                break;
                            }
                        }
                        if (!ctExist) throw new EventExecutionException("Specified Classtype does not exists: " + getClassType());
                        if (isSetPerPrio() && isPerPrio()) {
                            for (int prio : domain.getPrioritySameCT(getClassType())) {
                                printAgPrio(prio);
                            }
                        }
                        printAgCT(getClassType());
                    }
                } else { // no classtype set
                    if (isSetPreemptionLevel()) {
                        throw new EventExecutionException("Please also specify the classtyep when you specify a preemption level");
                    } else { // no ct, no pl
                        if (!isSetPerPrio() && !isSetPerCT() && domain.getAllCTId().length != 1) {
                            // use per ct
                            for (int ct : domain.getAllCTId()) {
                                printAgCT(ct);
                            }
                        } else {
                            if (isSetPerPrio() && isPerPrio()) {
                                // use per prio
                                for (int prio : domain.getPriorities()) {
                                    printAgPrio(prio);
                                }
                            }
                            if (isSetPerCT() && isPerCT()) {
                                // use per ct
                                for (int ct : domain.getAllCTId()) {
                                    printAgCT(ct);
                                }
                            }
                        }
                        // use total
                        printAgTotal();
                    }
                }
            }
        }
        return new EventResult(null, sb.toString());
    }

    private double round(double value) {
        return Math.round(value * precisionFactor) / precisionFactor;
    }

    private void printAgTotal() {
        int nbLinks = domain.getNbLinks();
        double[] capacities = new double[nbLinks];
        double[] reservedBw = new double[nbLinks];
        double[] reservableBw = new double[nbLinks];
        double[] util = new double[nbLinks];

        int i = 0;
        for (Link l : domain.getAllLinks()) {
            capacities[i] = l.getBandwidth();
            reservedBw[i] = l.getTotalReservedBandwidth();
            reservableBw[i] = l.getTotalReservableBandwidth();
            util[i] = (1- l.getTotalReservableBandwidth() / l.getBandwidth()) * 100;
            i++;
        }

        printStat("<ALL>", "<ALL>", "<ALL>", capacities, reservedBw, reservableBw, util);
    }

    private void printAgCT(int ct) {
        int nbLinks = domain.getNbLinks();
        double[] capacities = new double[nbLinks];
        double[] reservedBw = new double[nbLinks];
        double[] reservableBw = new double[nbLinks];
        double[] util = new double[nbLinks];

        int i = 0;
        for (Link l : domain.getAllLinks()) {
            capacities[i] = l.getBCs()[ct];
            reservedBw[i] = l.getReservedBandwidthCT(ct);
            reservableBw[i] = l.getReservableBandwidthCT(ct);
            util[i] = (1- l.getReservableBandwidthCT(ct) / l.getBCs()[ct]) * 100;
            i++;
        }

        printStat("<ALL>", String.valueOf(ct), "<ALL>", capacities, reservedBw, reservableBw, util);
    }

    private void printAgPrio(int priority) {
        int ct = domain.getClassType(priority);
        int pl = domain.getPreemptionLevel(priority);

        int nbLinks = domain.getNbLinks();
        double[] capacities = new double[nbLinks];
        double[] reservedBw = new double[nbLinks];
        double[] reservableBw = new double[nbLinks];
        double[] util = new double[nbLinks];

        int i = 0;
        for (Link l : domain.getAllLinks()) {
            capacities[i] = l.getBCs()[ct];
            reservedBw[i] = l.getReservedBandwidth(priority);
            reservableBw[i] = l.getReservableBandwidth(priority);
            util[i] = (1- l.getReservableBandwidth(priority) / l.getBCs()[ct]) * 100;
            i++;
        }

        printStat(String.valueOf(priority), String.valueOf(ct), String.valueOf(pl), capacities, reservedBw, reservableBw, util);
    }

    private void printHeader() {
        sb.append("#");
        sb.append("LinkId");
        sb.append("\t");
        sb.append("priority");
        sb.append("\t");
        sb.append("classType");
        sb.append("\t");
        sb.append("preemptionLevel");
        sb.append("\t");
        sb.append("Capacity");
        sb.append("\t");
        sb.append("ReservedBandwidth");
        sb.append("\t");
        sb.append("ReservableBandwidth");
        sb.append("\t");
        sb.append("util%");
        sb.append("\n");
    }

    // unlike the other methods, the given arrays contains only the links (no blank, index not identified by getConvertor().getLinkId()).
    private void printStat(String priority, String ct, String pl, double[] capacities, double[] reservedBw, double[] reservableBw, double[] util) {
        //max
        sb.append("MAX");
        sb.append("\t");
        sb.append(priority);
        sb.append("\t");
        sb.append(ct);
        sb.append("\t");
        sb.append(pl);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMaximum(capacities)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMaximum(reservedBw)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMaximum(reservableBw)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMaximum(util)));
        sb.append("\n");

        // MEAN
        sb.append("MEAN");
        sb.append("\t");
        sb.append(priority);
        sb.append("\t");
        sb.append(ct);
        sb.append("\t");
        sb.append(pl);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMeanValue(capacities)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMeanValue(reservedBw)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMeanValue(reservableBw)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getMeanValue(util)));
        sb.append("\n");

        // STD
        sb.append("STD");
        sb.append("\t");
        sb.append(priority);
        sb.append("\t");
        sb.append(ct);
        sb.append("\t");
        sb.append(pl);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getStandardDeviation(capacities)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getStandardDeviation(reservedBw)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getStandardDeviation(reservableBw)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getStandardDeviation(util)));
        sb.append("\n");

        // perce 10
        sb.append("percentile10");
        sb.append("\t");
        sb.append(priority);
        sb.append("\t");
        sb.append(ct);
        sb.append("\t");
        sb.append(pl);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile10(capacities)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile10(reservedBw)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile10(reservableBw)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile10(util)));
        sb.append("\n");

        // perce 20
        sb.append("percentile20");
        sb.append("\t");
        sb.append(priority);
        sb.append("\t");
        sb.append(ct);
        sb.append("\t");
        sb.append(pl);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(capacities, 80)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(reservedBw, 80)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(reservableBw, 80)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(util, 80)));
        sb.append("\n");

        // perce 30
        sb.append("percentile30");
        sb.append("\t");
        sb.append(priority);
        sb.append("\t");
        sb.append(ct);
        sb.append("\t");
        sb.append(pl);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(capacities, 70)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(reservedBw, 70)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(reservableBw, 70)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(util, 70)));
        sb.append("\n");

        // perce 50
        sb.append("percentile50");
        sb.append("\t");
        sb.append(priority);
        sb.append("\t");
        sb.append(ct);
        sb.append("\t");
        sb.append(pl);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(capacities, 50)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(reservedBw, 50)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(reservableBw, 50)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(util, 50)));
        sb.append("\n");

        // perce 90
        sb.append("percentile90");
        sb.append("\t");
        sb.append(priority);
        sb.append("\t");
        sb.append(ct);
        sb.append("\t");
        sb.append(pl);
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(capacities, 10)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(reservedBw, 10)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(reservableBw, 10)));
        sb.append("\t");
        sb.append(round(DoubleArrayAnalyse.getPercentile(util, 10)));
        sb.append("\n");
    }

    private void printTotal(Link link) {
        sb.append(link.getId());
        sb.append("\t");
        sb.append("<ALL>");
        sb.append("\t");
        sb.append("<ALL>");
        sb.append("\t");
        sb.append("<ALL>");
        sb.append("\t");
        sb.append(round(link.getBandwidth()));
        sb.append("\t");
        sb.append(round(link.getTotalReservedBandwidth()));
        sb.append("\t");
        sb.append(round(link.getTotalReservableBandwidth()));
        sb.append("\t");
        sb.append(round((1-link.getTotalReservableBandwidth() / link.getBandwidth()) * 100));
        sb.append("\n");
    }

    private void printPrio(Link link, int priority) {
        int ct = domain.getClassType(priority);
        int pl = domain.getPreemptionLevel(priority);
        sb.append(link.getId());
        sb.append("\t");
        sb.append(priority);
        sb.append("\t");
        sb.append(ct);
        sb.append("\t");
        sb.append(pl);
        sb.append("\t");
        sb.append(round(link.getBCs()[ct]));
        sb.append("\t");
        sb.append(round(link.getReservedBandwidth(priority)));
        sb.append("\t");
        sb.append(round(link.getReservableBandwidth(priority)));
        sb.append("\t");
        sb.append(round((1- link.getReservableBandwidth(priority) / link.getBCs()[ct]) * 100));
        sb.append("\n");
    }

    private void printCT(Link link, int ct) {
        sb.append(link.getId());
        sb.append("\t");
        sb.append("<ALL>");
        sb.append("\t");
        sb.append(ct);
        sb.append("\t");
        sb.append("<ALL>");
        sb.append("\t");
        sb.append(round(link.getBCs()[ct]));
        sb.append("\t");
        sb.append(round(link.getReservedBandwidthCT(ct)));
        sb.append("\t");
        sb.append(round(link.getReservableBandwidthCT(ct)));
        sb.append("\t");
        sb.append(round((1- link.getReservableBandwidthCT(ct) / link.getBCs()[ct]) * 100));
        sb.append("\n");
    }
}
