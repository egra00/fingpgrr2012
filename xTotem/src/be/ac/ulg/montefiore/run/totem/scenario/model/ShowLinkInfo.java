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

import be.ac.ulg.montefiore.run.totem.domain.diffserv.DiffServConstant;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BandwidthSharingBandwidthManagement;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.SPF;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.scenario.exception.EventExecutionException;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.InfoLinkType;
import be.ac.ulg.montefiore.run.totem.scenario.model.jaxb.impl.ShowLinkInfoImpl;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;

import be.ac.ulg.montefiore.run.totem.trafficMatrix.facade.TrafficMatrixManager;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.LinkLoadStrategy;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.*;
import be.ac.ulg.montefiore.run.totem.util.DoubleArrayAnalyse;
import be.ac.ulg.montefiore.run.totem.util.FloatArrayAnalyse;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;

/*
 * Changes:
 * --------
 * - 04-Feb-2005: add verbose and reservation/load versions
 * - 14-Feb-2005: add support for all SPF algorithms (load)
 * - 16-Feb-2005: readd verbose support
 * - 16-Feb-2005: delete the isSetType() test and update Javadoc (JL).
 * - 28-Mar-2005: the down links should not be taken into account (JL).
 * - 18-May-2005: add the ECMP parameter (JL).
 * - 23-May-2005: add the PRIMARY_BACKUP parameter (JL).
 * - 23-May-2005: fill the PRIMARY-BACKUP functionality (SBA).
 * - 16-Nov-2005: print percentile 90 and percentile 50 (JLE).
 * - 17-Nov-2005: print percentile 20 and percentile 30 (JLE).
 * - 20-Mar-2006: Use LinkLoadComputer to calculate load (GMO).
 * - 31-Mar-2006: Use LinkLoadComputerManager to calculate load (GMO).
 * - 12-Jan-2007: Add Overlay and IGPShortcut strategy (GMO).
 * - 26-Feb-2008: Use the new LinkLoadComputer interface. Deprecate use (GMO)
 */

/**
 * Event to show the load or the reservation of a link.
 *
 * <p>Creation date: 04-f�v.-2005
 *
 * @author Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 *
 * @deprecated Use {@link ShowLinkReservation} and {@link ShowLinkLoad} events.
 */
public class ShowLinkInfo extends ShowLinkInfoImpl implements Event {

    private static final Logger logger = Logger.getLogger(ShowLinkInfo.class);

    public ShowLinkInfo(){

    }

    public ShowLinkInfo(InfoLinkType type, boolean verbose, int ASID, int TMID){
        this.setASID(ASID);
        this.setTMID(TMID);
        this.setVerbose(verbose);
        this.setType(type);
    }

    public ShowLinkInfo(InfoLinkType type, boolean verbose, int ASID){
        this.setASID(ASID);
        this.setVerbose(verbose);
        this.setType(type);
    }

    public EventResult action() throws EventExecutionException {
        logger.warn("ShowLinkInfo event is deprecated. Use ShowLinkLoad and ShowLinkReservation instead");

        long time = System.currentTimeMillis();
        Domain domain = null;
        if (this.isSetASID()) {
            try {
                domain = InterDomainManager.getInstance().getDomain(_ASID);
            } catch (InvalidDomainException e) {
                e.printStackTrace();
            }
        } else {
            domain = InterDomainManager.getInstance().getDefaultDomain();
        }


        InfoLinkType infoLink = isSetType() ? this.getType() : InfoLinkType.LOAD;
        StringBuffer sb = new StringBuffer();
        if (infoLink.equals(InfoLinkType.LOAD) || infoLink.equals(InfoLinkType.LOAD_BIS) || infoLink.equals(InfoLinkType.LOAD_OVERLAY) || infoLink.equals(InfoLinkType.LOAD_IS)) {
            TrafficMatrix trafficMatrix = null;
            if (this.isSetTMID()){
                try {
                    trafficMatrix = TrafficMatrixManager.getInstance().getTrafficMatrix(domain.getASID(),_TMID);
                } catch (InvalidTrafficMatrixException e){
                    e.printStackTrace();
                    throw new EventExecutionException(e);
                }
            } else {
                try{
                    trafficMatrix = TrafficMatrixManager.getInstance().getDefaultTrafficMatrix(domain.getASID());
                } catch (InvalidTrafficMatrixException e){
                    e.printStackTrace();
                    throw new EventExecutionException(e);
                }
            }

            LinkLoadStrategy lls = null;
            if (infoLink.equals(InfoLinkType.LOAD)) {
                lls = new SPFLinkLoadStrategy(domain, trafficMatrix);
            } else if (infoLink.equals(InfoLinkType.LOAD_BIS)) {
                lls = new BasicIGPShortcutStrategy(domain, trafficMatrix);
            } else if (infoLink.equals(InfoLinkType.LOAD_OVERLAY)) {
                lls = new OverlayStrategy(domain, trafficMatrix);
            } else if (infoLink.equals(InfoLinkType.LOAD_IS)) {
                lls = new IGPShortcutStrategy(domain, trafficMatrix);
            }

            boolean ECMP = isSetECMP() ? isECMP() : false;
            lls.setECMP(ECMP);
            if (ECMP) {
                System.out.println("ECMP Used");
            }

            double[] fullLinkLoad = null;
            if (this.isSetSPFtype()){
                SPF spf = null;
                try {
                    spf = (SPF) RepositoryManager.getInstance().getAlgo(this.getSPFtype());
                    lls.setSPFAlgo(spf);
                }
                catch(NoSuchAlgorithmException e){
                    logger.error("Algorithm specified in SPFType for ShowLinkInfo event not found!");
                    logger.error("Using default SPF instead");
                }
                catch(ClassCastException e) {
                    logger.error("The specified algorithm isn't a SPF algorithm!");
                    logger.error("Using default SPF instead");
                }
            }

            lls.recompute();
            fullLinkLoad = lls.getData().getLoad();

            //fullLinkLoad contains information about down links, this is removed in the sequel.
            List<Link> upLinks = domain.getUpLinks();
            float[] linkCapacities = new float[upLinks.size()];
            double[] linkUtilisations = new double[upLinks.size()]; // contains only utilisation for up links
            double[] linkLoads = new double[upLinks.size()]; // contains only utilisation for up links
            int i = 0;
            boolean virtualLinksPresent = false;
            for (Iterator<Link> it = upLinks.iterator(); it.hasNext(); ++i) {
                Link link = it.next();
                if (link.getLinkType() == Link.Type.VIRTUAL) {
                    virtualLinksPresent = true;
                }
                linkCapacities[i] = link.getBandwidth();
                try {
                    linkUtilisations[i] = fullLinkLoad[domain.getConvertor().getLinkId(link.getId())] / link.getBandwidth();
                    linkLoads[i] = fullLinkLoad[domain.getConvertor().getLinkId(link.getId())];
                } catch (LinkNotFoundException e) {
                    e.printStackTrace();
                }
            }

            if (this.isVerbose()) {
                for(i = 0; i < upLinks.size(); ++i) {
                    try {
                        Link l = upLinks.get(i);
                        sb.append("Link ");
                        sb.append(l.getId());
                        sb.append(" from ");
                        sb.append(l.getSrcNode().getId());
                        sb.append(" to ");
                        sb.append(l.getDstNode().getId());
                        sb.append(" bw ");
                        sb.append(l.getBandwidth());
                        sb.append(" metric ");
                        sb.append(l.getMetric());
                        sb.append(" TE-metric ");
                        sb.append(l.getTEMetric());
                        sb.append(" load ");
                        sb.append(linkUtilisations[i]);
                        sb.append("\n");
                    } catch (NodeNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            time = System.currentTimeMillis() - time;
            sb.append("Link Info (max: " + (Math.round(DoubleArrayAnalyse.getMaximum(linkUtilisations)* 10000f) / 100f)
                    + " %, mean: " + (Math.round(DoubleArrayAnalyse.getMeanValue(linkUtilisations)* 10000f) / 100f)
                    + " %, std: " + (Math.round(DoubleArrayAnalyse.getStandardDeviation(linkUtilisations) * 10000f) / 100f)
                    + " %, percentile10: " + (Math.round(DoubleArrayAnalyse.getPercentile10(linkUtilisations)* 10000f) / 100f)
                    + " %, percentile20: " + (Math.round(DoubleArrayAnalyse.getPercentile(linkUtilisations, 80)* 10000f) / 100f)
                    + " %, percentile30: " + (Math.round(DoubleArrayAnalyse.getPercentile(linkUtilisations, 70)* 10000f) / 100f)
                    + " %, percentile50: " + (Math.round(DoubleArrayAnalyse.getPercentile(linkUtilisations, 50)* 10000f) / 100f)
                    + " %, percentile90: " + (Math.round(DoubleArrayAnalyse.getPercentile(linkUtilisations, 10)* 10000f) / 100f)
//                    + " %, Fortz: " + Math.round(DoubleArrayAnalyse.getFortz(linkLoads,linkCapacities))
                    + " %) in " + time + " ms\n");

            if (virtualLinksPresent) {
                double intraMaxUtil = 0;
                double interMaxUtil = 0;
                for (i=0; i < upLinks.size(); i++) {
                    Link link = upLinks.get(i);
                    if (link.getLinkType() == Link.Type.INTRA) {
                        if (linkUtilisations[i] > intraMaxUtil) {
                            intraMaxUtil = linkUtilisations[i];
                        }
                    } else if (link.getLinkType() == Link.Type.INTER) {
                        if (linkUtilisations[i] > interMaxUtil) {
                            interMaxUtil = linkUtilisations[i];
                        }
                    }

                }
                sb.append("Intradomain Max Util = " + intraMaxUtil * 100 + "% and Interdomain Max Util = " + interMaxUtil * 100 + "%\n");
            }

        } else if (infoLink.equals(InfoLinkType.RESERVATION)) {

            List<Link> linkList = domain.getUpLinks();
            float linkUtilisations[] = new float[linkList.size()];

            int[] cts = domain.getAllCTId();

            // this is in some way specific to MAM and it requires that all links have the MAM model
            for (int i=0; i<linkList.size(); i++){
                if (linkList.get(i).getDiffServBCM()!=DiffServConstant.DSMODEL_MAM){
                    System.out.println("Show Link Info not implemented when MAM model is not used for all links");
                    throw new EventExecutionException("Show Link Info not implemented when MAM model is not used for all links");
                }
            }

            for (int j=0; j<cts.length; j++){
                float linkCapacities[] = new float[linkUtilisations.length];
                for (int i = 0; i < linkList.size(); i++) {

                    List <Integer> prioritySameCT = domain.getPrioritySameCT(cts[j]);
                    float totalBandwidthforCT=0;
                    for (int k=0; k<prioritySameCT.size(); k++){
                        totalBandwidthforCT+=linkList.get(i).getReservedBandwidth((prioritySameCT.get(k)).intValue());
                    }
                    float load = totalBandwidthforCT/linkList.get(i).getBCs()[j];
                    linkUtilisations[i] = load;

                    linkCapacities[i] = linkList.get(i).getBandwidth();
                }
                if (this.isVerbose()) {
                    for(int i = 0; i < linkList.size(); ++i) {
                        try {
                            Link l = linkList.get(i);
                            sb.append("Link ");
                            sb.append(l.getId());
                            sb.append(" from ");
                            sb.append(l.getSrcNode().getId());
                            sb.append(" to ");
                            sb.append(l.getDstNode().getId());
                            sb.append(" bw ");
                            sb.append(l.getBandwidth());
                            sb.append(" metric ");
                            sb.append(l.getMetric());
                            sb.append(" TE-metric ");
                            sb.append(l.getTEMetric());
                            sb.append(" load ");
                            sb.append(linkUtilisations[i]);
                            sb.append("\n");
                        } catch (NodeNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
                sb.append("Link Info for CT: " + cts[j] + " (max: "
                        + (FloatArrayAnalyse.getMaximum(linkUtilisations) * 100) + " %, mean: "
                        + (FloatArrayAnalyse.getMeanValue(linkUtilisations)* 100) + " % , std: "
                        + (FloatArrayAnalyse.getStandardDeviation(linkUtilisations) * 100) + " %, percentile10: "
                        + (FloatArrayAnalyse.getPercentile10(linkUtilisations) * 100) + " %, percentile20: "
                        + (FloatArrayAnalyse.getPercentile(linkUtilisations, 80) * 100) + "%, percentile30: "
                        + (FloatArrayAnalyse.getPercentile(linkUtilisations, 70) * 100) + "%, percentile50: "
                        + (FloatArrayAnalyse.getPercentile(linkUtilisations, 50) * 100) + "%, percentile90: "
                        + (FloatArrayAnalyse.getPercentile(linkUtilisations, 10) * 100) + "%)\n");

                //System.out.println("Link Info for CT: " + cts[j] + " (max: " + (FloatArrayAnalyse.getMaximum(linkUtilisations) * 100) + " %, mean: " + (FloatArrayAnalyse.getMeanValue(linkUtilisations)* 100) + " % , std: " + (FloatArrayAnalyse.getStandardDeviation(linkUtilisations) * 100) + " %, min ResBw: " + FloatArrayAnalyse.getMinResidualBandwidth(linkUtilisations, linkCapacities) + ", mean RUN: " + FloatArrayAnalyse.getRUNObjFunc(linkUtilisations, linkCapacities) + ", Fortz: " + FloatArrayAnalyse.getIGPWOObjectiveFunctionValue(linkUtilisations, linkCapacities) + ", percentile10: " + (FloatArrayAnalyse.getPercentile10(linkUtilisations) * 100) + " %)");
            }
        } else if(infoLink.equals(InfoLinkType.PRIMARY_BACKUP)) {
                if (!domain.useBandwidthSharing()) {
                    logger.error("no DomainBWSharing !");
                    throw new EventExecutionException("no DomainBWSharing !");
                } else {
                    BandwidthSharingBandwidthManagement bwm = (BandwidthSharingBandwidthManagement) domain.getBandwidthManagement();
                    bwm.computeOversubscription();
                    sb.append(bwm.getDomainLabelUse());
                }
        } else {
            throw new EventExecutionException("Info type unknown: " + infoLink.toString());
        }
        return new EventResult(null, sb.toString());
    }
}
