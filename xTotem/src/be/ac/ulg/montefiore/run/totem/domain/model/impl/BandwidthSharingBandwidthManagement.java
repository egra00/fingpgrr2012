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
package be.ac.ulg.montefiore.run.totem.domain.model.impl;

import be.ac.ulg.montefiore.run.totem.domain.exception.*;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import org.apache.log4j.Logger;

import java.util.*;

/*
* Changes:
* --------
* - 18-Apr-2007: add recomputeRbw(.) method (GMO)
* - 27-Apr-2007: bugfix: replace Node.getInLink() by Node.getAllInLink() (GMO)
* - 14-Aug-2007: bugfix in clone of LinkInfo (GMO)
* - 05-Dec-2007: take care of bypass lsps (incompatible with this bwSharing) (GMO)
* - 17-Dec-2007: Changes are undone if the lsp cannot be added to the topology (GMO)
* - 17-Dec-2007: Bypass LSP can now use dome bandwidth sharing (GMO)
* - 17-Dec-2007: Now use a BandwidthSharingTopologyData object. implement the snapshot functionality (GMO)
* - 18-Dec-2008: Fix bug with preemption level (GMO)
*/

/**
 * This class manages link bandwidth. Adding and removing lsps will permanently change the reservation in the links.
 * <br>
 * It uses bandwidth sharing on links. Backup-to-backup and primary-to-backup bandwidth sharing.<br>
 * This class can be used only on domain that uses only one class type.
 * <br>
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 * @author Gaël Monfort (monfort@run.montefiore.ulg.ac.be)
 * @see "A scalable and decentralized fast-rerouting scheme with efficient bandwidth sharing", S. Balon, L. Mélon and
 *      G. Leduc <i>in</i> Computer Networks, vol. 50, nb. 16, Nov. 2006, pp. 3043-3063
 *      <p/>
 *      <p/>
 *      <p>Creation date: 3/11/2006
 */
public class BandwidthSharingBandwidthManagement implements BandwidthManagement {
    private static final Logger logger = Logger.getLogger(BandwidthSharingBandwidthManagement.class);

    protected Domain domain;
    private DomainConvertor convertor;

    private int classType;

    protected BandwidthSharingTopologyData topoData;

    /**
     * Create a new BandwidthSharingBandwidthManagement object to use with the given domain.
     *
     * @param domain
     * @throws DiffServConfigurationException if the domain contains more than on class type
     */
    public BandwidthSharingBandwidthManagement(Domain domain) throws DiffServConfigurationException {
        if (domain.getNbCT() != 1) {
            throw new DiffServConfigurationException("Bandwidth Sharing supports only one class type. More are defined in the domain.");
        }
        this.classType = domain.getAllCTId()[0];

        this.domain = domain;
        this.convertor = domain.getConvertor();

        topoData = new SimpleBandwidthSharingTopologyData(domain);
    }

    /**
     * Creates a new BandwidthSharingBandwidthManagement object to use with the given domain. The object will use the
     * given {@link BandwidthSharingTopologyData}.
     * @param domain
     * @param topoData
     * @throws DiffServConfigurationException
     */
    protected BandwidthSharingBandwidthManagement(Domain domain, BandwidthSharingTopologyData topoData) throws DiffServConfigurationException {
        if (domain.getNbCT() != 1) {
            throw new DiffServConfigurationException("Bandwidth Sharing supports only one class type. More are defined in the domain.");
        }
        this.classType = domain.getAllCTId()[0];

        this.domain = domain;
        this.convertor = domain.getConvertor();

        this.topoData = topoData;
    }

    /**
     * Initialise the Bandwidth Management object with the lsps already present in the domain.
     * This must be called prior to use.
     *
     * @throws LinkCapacityExceededException if the calculated bandwidth exceed link capacity
     */
    public void init() throws LinkCapacityExceededException {
        int priority = domain.getMinPriority(classType);

        // sets all links reservation to 0
        for (Link l : domain.getAllLinks()) {
            l.removeReservation(l.getReservedBandwidth(priority), priority);
        }

        // Add to the internal structure the info for already existing LSPs
        for (Lsp lsp : domain.getAllLsps()) {
            if (lsp.getCT() == classType) {
                try {
                    addLsp(lsp);
                } catch (DiffServConfigurationException e) {
                    //should not happen
                    e.printStackTrace();
                } catch (LspNotFoundException e) {
                    logger.error("Error in domain: a backup lsp exists, but the corresponding primary cannot be found.");
                }
            }
        }
    }

    /**
     * Returns an empty list. Preemption are not used.
     *
     * @param lsp
     * @return
     */
    public List<Lsp> getPreemptList(Lsp lsp) {
        return new ArrayList<Lsp>(0);
    }

    /**
     * Add lsp reservation. It adds some reservation to the links in the path of the lsp. Bandwidth sharing is used.
     *
     * @param lsp
     * @throws LinkCapacityExceededException
     * @throws DiffServConfigurationException
     * @throws LspNotFoundException           if the argument is a backup lsp and the primary lsp cannot be found in the domain
     */
    public void addLsp(Lsp lsp) throws DiffServConfigurationException, LspNotFoundException, LinkCapacityExceededException {
        updateLspInfo(lsp, false);
    }

    /**
     * Removes lsp reservation. It removes some reservation to the links in the path of the lsp.
     *
     * @param lsp
     * @throws DiffServConfigurationException
     * @throws LspNotFoundException
     * @throws LinkCapacityExceededException
     */
    public void removeLsp(Lsp lsp) throws DiffServConfigurationException, LspNotFoundException, LinkCapacityExceededException {
        updateLspInfo(lsp, true);
    }

    /**
     * Returns the maximum reservable bandwidth at priority level <code>priority</code> for a
     * lsp traversing the link <code>link</code>. If <code>protectedLinks</code> is given, the reservable bandwidth for
     * a backup lsp protecting those links is returned, otherwise, a primary lsp is assumed.
     * <br>
     * This method takes the temporarily added and removed lsps into account.
     * <br>
     * The reservable bandwidth for a particular lsp on the link ij is computed as follow:<br>
     * <ul>
     * <li>The reservable bandwidth on the link ij for primary lsps</li>
     * <li>The minimum <code>Cij - [Pij + Bij(Lmn) - Fij(Lmn)]</code> among protected links Lmn for backup lsps.
     * </ul>
     *
     * @param priority
     * @param link
     * @param protectedLinks
     * @return
     */
    public float getReservableBandwidth(int priority, Link link, Collection<Link> protectedLinks) {
        if (protectedLinks == null || protectedLinks.size() <= 0) {
            //primary lsp
            return link.getReservableBandwidth();
        } else {
            //backup lsp
            float minBw = Float.MAX_VALUE;

            try {
                BandwidthSharingLinkInfo info = topoData.getLinkInfo(link.getId());

                float CmF = info.getBw() - info.getPij();
                for (Link l : protectedLinks) {
                    int lId = convertor.getLinkId(l.getId());
                    float rbw = CmF - info.getBij(lId) + info.getFij(lId);
                    if (rbw < minBw) minBw = rbw;
                }

            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }

            logger.debug("Reservable bandwidth on link " + link.getId() + ": " + minBw);
            return minBw;
        }
    }

    /**
     * returns false. Bandwidth Sharing is not diffserv-aware and do not use preemption.
     *
     * @return
     */
    public boolean usePreemption() {
        return false;
    }

    /**
     * Recompute the reservable bandwidth (rbw array) of the link <code>link<code>.<br>
     * Warning: all rbw values should be set to 0 before calling this method.
     *
     * @param link
     * @throws LinkCapacityExceededException
     */
    public void recomputeRbw(Link link) throws LinkCapacityExceededException {
        for (Lsp lsp : domain.getLspsOnLink(link)) {

            int priority = domain.getMinPriority(lsp.getCT());

            /* Check for available bw */
            float value = lsp.getReservation();
            // don't use priority to check for bw
            if (link.getReservableBandwidth(priority) < value) {
                logger.error("Not enough available bandwidth on link " + link.getId());
                throw new LinkCapacityExceededException();
            }

            int truePrio = domain.getPriority(lsp.getHoldingPreemption(), lsp.getCT());

            link.addReservation(value, truePrio);
        }
    }

    /**
     * Returns a snapshot of the bandwidth sharing.
     * @return
     */
    public BandwidthManagement getSnapshot() {
        try {
            return new BandwidthSharingBandwidthManagementSnapshot(this);
        } catch (DiffServConfigurationException e) {
            // should not happen
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Really made change to the reservation on the given link.
     * @param link
     * @param incBw
     * @param priority
     * @throws LinkCapacityExceededException
     */
    protected void changeReservation(Link link, float incBw, int priority) throws LinkCapacityExceededException {
        logger.debug("link: " + link.getId());
        logger.debug("Inc Bw: " + incBw);
        logger.debug("Resvable bw: " + link.getReservableBandwidth(priority));

        if (incBw > 0) {
            link.addReservation(incBw, priority);
        } else if (incBw < 0) {
            link.removeReservation(-incBw, priority);
        }
    }

    private void updateLspInfo(Lsp lsp, boolean remove) throws DiffServConfigurationException, LspNotFoundException, LinkCapacityExceededException {
        if (lsp.getCT() != classType) {
            logger.error("LSP does not belong to the specified class type and thus cannot be used for bandwidth sharing");
            logger.error("LSP " + lsp.getId() + " not added/removed.");
            throw new DiffServConfigurationException("Only the specified class type can be used with bandwidth sharing.");
        }

        HashMap<Link, Float> changedReservation = new HashMap<Link, Float>();
        int priority = domain.getPriority(lsp.getHoldingPreemption(), lsp.getCT());

        try {

            if (!lsp.isBackupLsp()) {
                // The lsp is a primary lsp
                Path path = lsp.getLspPath();
                List<Link> linksPath = path.getLinkPath();

                for (Link link : linksPath) {

                    BandwidthSharingLinkInfo update = topoData.getLinkInfo(link.getId());

                    float oldBw = update.getRij();

                    if (!remove) {
                        //System.out.println("Adding Pij " + lsp.getReservation() + " to the link of id " + linkId);
                        update.addPij(lsp.getReservation());
                    } else {
                        update.subPij(lsp.getReservation());
                    }

                    float incBw = update.getRij() - oldBw;

                    changeReservation(link, incBw, priority);
                    if (incBw != 0) {
                        if (changedReservation.put(link, new Float(incBw)) != null) {
                            logger.error("Link bandwidth changed multiple times for link.");
                        }
                    }
                }
            } else if (lsp.isDetourLsp()) {
                // The lsp is a backup lsp
                Lsp primaryLsp = lsp.getProtectedLsp();
                List<Link> protectedLinks = lsp.getProtectedLinks();
                Path primaryLspPath = primaryLsp.getLspPath();
                List<Link> primaryLinks = primaryLspPath.getLinkPath();

                Path path = lsp.getLspPath();
                List<Link> linksPath = path.getLinkPath();

                Node ingress = path.getSourceNode();
                Node egress = path.getDestinationNode();

                // For all the links of the backup LSP
                for (Iterator<Link> it = linksPath.iterator(); it.hasNext();) {
                    Link link = it.next();
                    int linkId = convertor.getLinkId(link.getId());

                    BandwidthSharingLinkInfo update = topoData.getLinkInfo(linkId);

                    float oldBw = update.getRij();

                    for (Iterator<Link> it2 = protectedLinks.iterator(); it2.hasNext();) {
                        Link protectedLink = it2.next();
                        logger.debug("Protected link Id: " + protectedLink.getId());
                        int protectedLinkId = convertor.getLinkId(protectedLink.getId());
                        if (protectedLinkId != linkId) {
                            if (!remove) {
                                //System.out.println("Adding Bij " + lsp.getReservation() + " to the link of id " + linkId + " in case of failure of link " + protectedLinkId);
                                update.addBij(primaryLsp.getReservation(), protectedLinkId);
                            } else {
                                update.subBij(primaryLsp.getReservation(), protectedLinkId);
                            }
                        }
                    }

                    float incBw = update.getRij() - oldBw;
                    logger.debug("link: " + link.getId());
                    logger.debug("IncBW: " + incBw);

                    changeReservation(link, incBw, priority);
                    if (incBw != 0) {
                        if (changedReservation.put(link, new Float(incBw)) != null) {
                            logger.error("Link bandwidth changed multiple times for link.");
                        }
                    }
                }

                boolean in = false;

                // For all the links of the primary LSP
                for (Iterator<Link> it = primaryLinks.iterator(); it.hasNext();) {
                    Link link = it.next();
                    int linkId = convertor.getLinkId(link.getId());

                    BandwidthSharingLinkInfo update = topoData.getLinkInfo(linkId);

                    float oldBw = update.getRij();

                    if (link.getSrcNode().equals(ingress)) {
                        in = true;
                    }

                    if (in) {
                        // For all the links between the PLR and the MP...
                        for (Iterator<Link> it2 = protectedLinks.iterator(); it2.hasNext();) {
                            Link protectedLink = it2.next();
                            int protectedLinkId = convertor.getLinkId(protectedLink.getId());
                            if (linkId != protectedLinkId) {
                                if (!remove) {
                                    //System.out.println("Adding Fij " + lsp.getReservation() + " to the link of id " + linkId + " in case of failure of link " + protectedLinkId);
                                    update.addFij(primaryLsp.getReservation(), protectedLinkId);
                                } else {
                                    update.subFij(primaryLsp.getReservation(), protectedLinkId);
                                }
                            }
                        }

                        float incBw = update.getRij() - oldBw;

                        changeReservation(link, incBw, priority);
                        if (incBw != 0) {
                            if (changedReservation.put(link, new Float(incBw)) != null) {
                                logger.error("Link bandwidth changed multiple times for link.");
                            }
                        }

                        if (link.getDstNode().equals(egress)) {
                            in = false;
                            break;
                        }
                    }
                }
            } else {
                //LSP is a bypass.
                // Sharing can occur if the facility backup do protect different resources.
                for (Link link : lsp.getLspPath().getLinkPath()) {
                    int linkId = convertor.getLinkId(link.getId());

                    BandwidthSharingLinkInfo update = topoData.getLinkInfo(linkId);

                    float oldBw = update.getRij();
                    for (Link protectedLink : lsp.getProtectedLinks()) {
                        logger.debug("Protected link Id: " + protectedLink.getId());
                        int protectedLinkId = convertor.getLinkId(protectedLink.getId());
                        if (protectedLinkId != linkId) {
                            if (!remove) {
                                //System.out.println("Adding Bij " + lsp.getReservation() + " to the link of id " + linkId + " in case of failure of link " + protectedLinkId);
                                update.addBij(lsp.getReservation(), protectedLinkId);
                            } else {
                                update.subBij(lsp.getReservation(), protectedLinkId);
                            }
                        }
                    }

                    float incBw = update.getRij() - oldBw;

                    changeReservation(link, incBw, priority);
                    if (incBw != 0) {
                        if (changedReservation.put(link, new Float(incBw)) != null) {
                            logger.error("Link bandwidth changed multiple times for link.");
                        }
                    }
                }
            }
        } catch (LinkNotFoundException e) {
            logger.error("Error in domain: link not found");
        } catch (NodeNotFoundException e) {
            logger.error("Error in domain: node not found");
        } catch (LinkCapacityExceededException e) {
            logger.error("LinkCapacity exceeded");
            /* undo changes */
            for (Link l : changedReservation.keySet()) {
                Float bw = changedReservation.get(l);
                changeReservation(l, -bw.floatValue(), priority);
            }
            // rethrow exception
            throw e;
        }
    }


    public String getDomainLabelUse() {
        convertor = domain.getConvertor();
        List<Link> links = domain.getAllLinks();

        long nbLinks = domain.getNbLinks();

        long nbTotalPrim = 0;
        long nbTotalBackups = 0;
        long nbTotalBackupsWithSharing = 0;

        long nbMaxPrimLabels = 0;

        long nbMaxLabels = 0;
        long nbMaxPrim = 0;
        long nbMaxLabelsWithSharing = 0;
        long nbMaxPrimWithSharing = 0;

        for (Iterator<Link> it = links.iterator(); it.hasNext();) {
            Link link = it.next();
            int linkId = 0;
            try {
                linkId = convertor.getLinkId(link.getId());
            } catch (LinkNotFoundException e) {
                logger.fatal("Link not found in convertor: " + link.getId());
            }

            BandwidthSharingLinkInfo info = topoData.getLinkInfo(linkId);
            long nbPrim = info.getNbPrim();
            long nbBackups = info.getNbBackups();
            long nbMaxBackups = info.getMaxNbBackupsLij();

            nbTotalPrim += nbPrim;
            nbTotalBackups += nbBackups;
            nbTotalBackupsWithSharing += nbMaxBackups;

            long nbLabels = nbPrim + nbBackups;
            long nbLabelsWithSharing = nbPrim + nbMaxBackups;

            if (nbMaxLabels < nbLabels) {
                nbMaxLabels = nbLabels;
                nbMaxPrim = nbPrim;
            }

            if (nbMaxLabelsWithSharing < nbLabelsWithSharing) {
                nbMaxLabelsWithSharing = nbLabelsWithSharing;
                nbMaxPrimWithSharing = nbPrim;
            }

            if (nbMaxPrimLabels < nbPrim) {
                nbMaxPrimLabels = nbPrim;
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append("Report of label use :\n\n");
        sb.append("In the whole network :\n");
        sb.append("Number of labels used for primary LSPs = " + nbTotalPrim + " (mean value per link = " + (nbTotalPrim / nbLinks) + " )\n");
        sb.append("Number of labels used for backup LSPs = " + nbTotalBackups + " (mean value per link = " + (nbTotalBackups / nbLinks) + " )\n");
        sb.append("Number of labels used for backup LSPs (with label sharing) = " + nbTotalBackupsWithSharing + " (mean value per link = " + (nbTotalBackupsWithSharing / nbLinks) + " )\n\n");
        sb.append("For the maximum number of labels :\n");
        sb.append("Max number of primary labels per link = " + nbMaxPrimLabels + "\n");
        sb.append("Max number of labels per link = " + nbMaxLabels + " (out of which " + nbMaxPrim + " are primary labels)\n");
        sb.append("Max number of labels per link (with label sharing) = " + nbMaxLabelsWithSharing + " (out of which " + nbMaxPrimWithSharing + " are primary labels)\n");

        return sb.toString();
    }

    public void printDomainLabelUse() {
        System.out.println(getDomainLabelUse());
    }


    public float computeOversubscription() {
        convertor = domain.getConvertor();
        List<Link> links = domain.getAllLinks();


        float totalReservedBW = 0;
        float totalMaxBij = 0;
        float totalPrimaryBW = 0;
        float totalBW = 0;
        float maxLinkLoad = 0;
        float maxLinkLoadWoFij = 0;

        for (Iterator<Link> it = links.iterator(); it.hasNext();) {
            Link link = it.next();
            int linkId = 0;
            try {
                linkId = convertor.getLinkId(link.getId());
            } catch (LinkNotFoundException e) {
                logger.fatal("Link not found in convertor: " + link.getId());
            }

            BandwidthSharingLinkInfo info = topoData.getLinkInfo(linkId);
            float Rij = info.getRij();
            float MaxBij = info.getMaxBij();
            float Pij = info.getPij();

            totalReservedBW += Rij;
            totalPrimaryBW += Pij;
            totalMaxBij += MaxBij;
            totalBW += info.getBw();
            float linkLoad = (Rij / info.getBw());
            maxLinkLoad = ((linkLoad > maxLinkLoad) ? linkLoad : maxLinkLoad);
            linkLoad = ((Pij + MaxBij) / info.getBw());
            maxLinkLoadWoFij = ((linkLoad > maxLinkLoadWoFij) ? linkLoad : maxLinkLoadWoFij);

        }

        float overSubscription = (totalMaxBij / totalPrimaryBW);

        System.out.println("Total bandwidth reserved is " + totalReservedBW);
        System.out.println("Total primary bandwidth reserved is " + totalPrimaryBW);
        System.out.println("Oversubscription without Fij is equal to : " + overSubscription);
        System.out.println("Oversubscription with Fij is equal to : " + ((totalReservedBW - totalPrimaryBW) / totalPrimaryBW));
        System.out.println("Mean load of the network without Fij is equal to : " + ((totalPrimaryBW + totalMaxBij) / totalBW));
        System.out.println("Mean load of the network is equal to : " + (totalReservedBW / totalBW));
        System.out.println("Max link load without Fij is : " + maxLinkLoadWoFij);
        System.out.println("Max link load is : " + maxLinkLoad);

        return overSubscription;
    }
}
