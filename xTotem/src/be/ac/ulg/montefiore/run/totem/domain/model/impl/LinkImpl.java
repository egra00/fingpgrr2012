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

import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.NodeInterface;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.*;
import be.ac.ulg.montefiore.run.totem.domain.diffserv.DiffServModelManager;
import be.ac.ulg.montefiore.run.totem.domain.diffserv.DiffServConstant;
import be.ac.ulg.montefiore.run.totem.domain.diffserv.DiffServModel;
import be.ac.ulg.montefiore.run.totem.domain.exception.*;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;

import java.util.List;
import java.util.Arrays;

/*
 * Changes:
 * --------
 *  - 03-Jun-05 : remove getBw method useless due to the getBandwidth method (FSK)
 *  - 21-Oct-2005 : Add getPreemptList method and change addReservation behavior. Now preemptions are done in
 *    Domain.addLsp and not in Link.addReservation anymore. (GMO)
 *  - 25-Oct-2005 : Dynamic part of igpLink now contains only defined priority (GMO)
 *  - 8-Nov-2005 : Add link notifications to domain observer (GMO).
 *  - 12-Jan-2006 : change the getReservedBandwidth() method. Now it returns the sum of the reserved bw for each
 *    priority level, instead of the reserved bw at minimum priority. (GMO)
 *  - 18-Jan-2006 : suppress an annoying warning in getBandwidth(). (GMO)
 *  - 08-Feb-2006: add getSrcInterface() & getDstInterface (JLE).
 *  - 08-Feb-2006: comments fixes (JLE).
 *  - 06-Mar-2006: add setSrcInterface and setDstInterface (JLE).
 *  - 23-Oct-2006: change getReservableBandwidth behaviour : if domain does not use preemption, the reservable bandwidth
 *                 at lowest priority (in the same class type) is always returned (GMO)
 *  - 24-Oct-2006: add getReservedBandwidthCT(.) (GMO)
 *  - 22-Nov-2006: add getTotalReservedBandwidth(), getRbw(), getDiffServModel(). Remove getPreemptList(.) (GMO)
 *  - 22-Nov-2006: removeReservation(.) now throws exception (GMO)
 *  - 22-Nov-2006: Rbw array is now always reinitialise when init() is called (GMO)
 *  - 18-Jan-2007: add getTotalReservableBandwidth() (GMO)
 *  - 06-Mar-2007: change constants in enum for Link type, add setLinkType(.) (GMO)
 *  - 18-Apr-2007: add setBC(.), setRbw(.), addPriority(.) and removePriority(.) methods (GMO)
 *  - 24-Apr-2007: add setMaximumBandwidth(.) and getMaximumBandwidth() (GMO)
 *  - 24-Apr-2007: setBandwidth now throws LinkCapacityException (GMO)
 *  - 25-Apr-2007: do not set status when callink getLinkStatus (GMO)
 *  - 11-May-2007: setting link status now disables LSPs rather than removing them (GMO)
 *  - 25-Sep-2007: setting link status now signals to the lsp using appropriate method (GMO)
 *  - 28-Sep-2007: fix bug when resetting rbw in setBandwidth(.), setBC(.), addPriority(.) (GMO)
 *  - 15-Jan-2008: maintain an array of current reservation per priority (GMO)
 *  - 15-Jan-2008: DS model constraints are checked on init and when bandwidth is changed (GMO)
 *  - 15-Jan-2008: Change DS model usage (new interface) when adding or removing reservation and when getting reserved or reservable bandwidth (GMO)
 */

/**
 * Represent a Link in a Domain
 *
 * <p>Creation date: 12-Jan-2005 17:27:03
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class LinkImpl extends be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.LinkImpl implements Link {

    private static Logger logger = Logger.getLogger(LinkImpl.class.getName());
    private LinkIgp igpLink;
    private DomainImpl domain;
    private int[] rbwMapping=null;

    private float[] reservedBandwidth;

    public LinkImpl() { }

    /**
     * Basic constructor
     *
     * @param domain
     * @param id
     * @param src id of the src node
     * @param dst id the dst node
     * @param bandwidth
     */
    public LinkImpl(Domain domain, String id, String src, String dst, float bandwidth) {
        ObjectFactory factory = new ObjectFactory();
        try {
            FromType fromType = factory.createLinkFromType();
            fromType.setNode(src);
            //fromType.setIf();

            ToType toType = factory.createLinkToType();
            toType.setNode(dst);
            //toType.setIf();

            this.setFrom(fromType);
            this.setTo(toType);

            this.setBw(bandwidth);

            this.setId(id);
            // create IGP link
            igpLink = factory.createLinkIgp();
            igpLink.setId(id);

            // init IGP static section
            createIGPStatic(bandwidth);

            float rbw = (float) (bandwidth / (float) domain.getNbCT());
            List bcList = igpLink.getStatic().getDiffServ().getBc();
            int CTId[] = domain.getAllCTId();

            if (CTId.length != domain.getNbCT()) {
                DiffServConfigurationException e = new DiffServConfigurationException("domain.getAllCTId().length != domain.getNbCT()");
                e.printStackTrace();
            }
            for (int i = 0; i < domain.getNbCT(); i++) {
                LinkIgp.StaticType.DiffServType.BcType b = factory.createLinkIgpStaticTypeDiffServTypeBcType();
                b.setId(CTId[i]);
                b.setValue(rbw);
                bcList.add(b);
            }
            // init IGP dynamic section
            igpLink.setDynamic(factory.createLinkIgpDynamicType());
            igpLink.getDynamic().setRbw(factory.createLinkIgpDynamicTypeRbwType());
            List priorityList = igpLink.getDynamic().getRbw().getPriority();

            // changed by GMO: only defined priority shown in igp link
            /* for each CT */
            for (int i = 0; i < domain.getNbCT(); i++) {
                /* for each priority */
                for (Integer prio : domain.getPrioritySameCT(CTId[i])) {
                    LinkIgp.DynamicType.RbwType.PriorityType p = factory.createLinkIgpDynamicTypeRbwTypePriorityType();
                    p.setId(prio.intValue());
                    p.setValue(rbw);
                    priorityList.add(p);
                }

            }

            this.init(domain);
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (DiffServConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void createIGPStatic(float bandwidth) throws JAXBException {
        ObjectFactory factory = new ObjectFactory();

        igpLink.setStatic(factory.createLinkIgpStaticType());
        igpLink.getStatic().setAdmingroup(0);
        igpLink.getStatic().setMetric(0);
        igpLink.getStatic().setTeMetric(0);
        igpLink.getStatic().setMbw(bandwidth);
        igpLink.getStatic().setMrbw(bandwidth);
        igpLink.getStatic().setDiffServ(factory.createLinkIgpStaticTypeDiffServType());
        igpLink.getStatic().getDiffServ().setBcm(BcmType.MAM);
    }

    /**
     * Get the IGP link associated to the link
     *
     * @return
     */
    public LinkIgp getIgpLink() {
        return igpLink;
    }

    /**
     * Initialize a link.
     * Create the domain reference and populate the rbwMapping structure
     *
     * @param domain
     */
    public void init(Domain domain) throws DiffServConfigurationException {

        this.domain = (DomainImpl) domain;
        if (igpLink == null) {
            try {
                igpLink = this.domain.getLinkIgp(getId());
            } catch (LinkNotFoundException e) {
                e.printStackTrace();
            }
        }

        // init Diff-Serv
        try {
            initDiffServ();
        } catch (NotInitialisedException e) {
            e.printStackTrace();
        }

        // init the rbw mapping
        rbwMapping = new int[DiffServConstant.MAX_NB_PRIORITY];
        for (int i=0; i<this.domain.getNbPriority(); i++){

            int priorityId = ((LinkIgp.DynamicType.RbwType.PriorityType)igpLink.getDynamic().getRbw().getPriority().get(i)).getId();
            rbwMapping[priorityId] = i;

        }

        reservedBandwidth = new float[DiffServConstant.MAX_NB_PRIORITY];
        Arrays.fill(reservedBandwidth, 0);

    }

    /**
     * Init Diff-Serv static and dynamic linkIgp fields
     * according to the info field
     */
    private void initDiffServ() throws NotInitialisedException, DiffServConfigurationException {
        ObjectFactory factory = new ObjectFactory();
        try{
            if ((igpLink == null) || (igpLink.getStatic()==null)){
                throw new NotInitialisedException("IgpLink not present for link " + this.getId());
            }
            if (igpLink.getStatic().getDiffServ() == null){
                igpLink.getStatic().setDiffServ(factory.createLinkIgpStaticTypeDiffServType());
            }

            if (igpLink.getStatic().getDiffServ().getBcm() == null){
                igpLink.getStatic().getDiffServ().setBcm(BcmType.MAM);
            }

            int CTId[] = domain.getAllCTId();
            float rbw = (float) (getBandwidth() / (float) domain.getNbCT());

            if (igpLink.getStatic().getDiffServ().getBc() == null || igpLink.getStatic().getDiffServ().getBc().size()==0){

                List bcList = igpLink.getStatic().getDiffServ().getBc();

                if (CTId.length != domain.getNbCT()) {
                    DiffServConfigurationException e = new DiffServConfigurationException("domain.getAllCTId().length != domain.getNbCT()");
                    e.printStackTrace();
                }
                for (int i = 0; i < domain.getNbCT(); i++) {
                    LinkIgp.StaticType.DiffServType.BcType b = factory.createLinkIgpStaticTypeDiffServTypeBcType();
                    b.setId(CTId[i]);
                    b.setValue(rbw);
                    bcList.add(b);
                }
            }

            //check diff serv model constraints
            if (!getDiffServModel().checkConstraints(domain, getBCs(), getBandwidth())) {
                throw new DiffServConfigurationException("Model constraints are not satisfied for link " + getId());
            }

            // init IGP dynamic section
            if (igpLink.getDynamic() == null){
                igpLink.setDynamic(factory.createLinkIgpDynamicType());
            }


            // commented lines by GMO on 2006-11-17
            // rbw is always reinitialised
            //if (igpLink.getDynamic().getRbw() == null){
                igpLink.getDynamic().setRbw(factory.createLinkIgpDynamicTypeRbwType());
            //}

            //if (igpLink.getDynamic().getRbw().getPriority() == null || igpLink.getDynamic().getRbw().getPriority().size()==0){


                List priorityList = igpLink.getDynamic().getRbw().getPriority();


                for (int i=0; i<CTId.length; i++){
                    List<Integer> list = domain.getPrioritySameCT(CTId[i]);
                    for (int j=0; j<list.size(); j++){
                        LinkIgp.DynamicType.RbwType.PriorityType p = factory.createLinkIgpDynamicTypeRbwTypePriorityType();
                        p.setId(list.get(j).intValue());
                        //searching corresponding BC
                        List<LinkIgp.StaticType.DiffServType.BcType> bcList = igpLink.getStatic().getDiffServ().getBc();

                        for (int k=0; k<bcList.size(); k++){
                            if (bcList.get(k).getId()==CTId[i]){
                                rbw = bcList.get(k).getValue();
                            }
                        }
                        
                        p.setValue(rbw);
                        priorityList.add(p);
                    }

                }
            //}
            //else TODO: check that priorities are set correctly
        }

        catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the source node.
     * @return The source node.
     */
    public Node getSrcNode() throws NodeNotFoundException {
        return domain.getNode(this.getFrom().getNode());
    }

    /**
     * Returns the destination node.
     * @return The destination node.
     */
    public Node getDstNode() throws NodeNotFoundException {
        return domain.getNode(this.getTo().getNode());
    }

    /**
     * Returns the interface of the source node used by this link.
     * @return The interface of the source node used by this link.
     */
    public NodeInterface getSrcInterface() throws NodeNotFoundException, NodeInterfaceNotFoundException {
        return domain.getNode(this.getFrom().getNode()).getNodeInterface(this.getFrom().getIf());
    }

    /**
     * Returns the interface of the destination node used by this link.
     * @return The interface of the destination node used by this link.
     */
    public NodeInterface getDstInterface() throws NodeNotFoundException, NodeInterfaceNotFoundException {
        return domain.getNode(this.getTo().getNode()).getNodeInterface(this.getTo().getIf());
    }

    /**
     * Sets the source interface.
     */
    public void setSrcInterface(String srcInterfaceId) throws NodeNotFoundException, NodeInterfaceNotFoundException {
        this.getSrcNode().getNodeInterface(srcInterfaceId);
        this.getFrom().setIf(srcInterfaceId);
    }
    
    /**
     * Sets the destination interface.
     */
    public void setDstInterface(String dstInterfaceId) throws NodeNotFoundException, NodeInterfaceNotFoundException {
        this.getDstNode().getNodeInterface(dstInterfaceId);
        this.getTo().setIf(dstInterfaceId);
    }

    /**
     * Return the maximum reservable bandwidth of the link if defined. Otherwise, it returns the bw field of the link.
     *
     * @return the maximum reservable bandwidth of the link if defined. Otherwise, it returns the bw field of the link.
     */
    public float getBandwidth() {
        //commented because it generates too much warning when using GUI
        //if((getStatus() != null) && (getStatus().equals(StatusType.DOWN))) {
        //    logger.warn(new StringBuffer().append("getBandwidth called on the link ").append(this.getId()).append(" which is down!").toString());
        //}

        // Return the maximum reservable bandwidth if defined
        if ((igpLink !=null) && (igpLink.getStatic() != null))
            return igpLink.getStatic().getMrbw();

        // If the MRBW is not defined, return bw
        return this.getBw();
    }

    /**
     * Sets the maximum reservable bandwidth of the link.<p>
     * If the domain has only one classtype and the associated bandwidth constraint is equal to the the bandwidth value,
     * the constraint is also modified.<br>
     * For example, this permits reducing link capacity with the MAM model (as the bandwidth constraints must be inferior
     * or equal to the mrbw)
     *
     * @param bw the new bandwidth
     * @throws LinkCapacityExceededException If all the LSPs cannot be established with the new bandwidth value
     * @throws DiffServConfigurationException If the diffserv constraints are not satisfied anymore
     */
    public void setBandwidth(float bw) throws LinkCapacityExceededException, DiffServConfigurationException {
        if ((getStatus() != null) && (getStatus().equals(StatusType.DOWN))) {
            logger.warn(new StringBuffer().append("setBandwidth called on the link ").append(this.getId()).append(" which is down!").toString());
        }

        if (igpLink == null) {
            logger.warn(new StringBuffer().append("setBandwidth called on the link ").append(this.getId()).append(" which has no IGP link").toString());
        } else {

            float oldMrbw;
            // Create igp static section if not exist
            if (igpLink.getStatic() == null) {
                oldMrbw = getBandwidth();
                try {
                    createIGPStatic(bw);
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            } else {
                oldMrbw = igpLink.getStatic().getMrbw();
            }

            igpLink.getStatic().setMrbw(bw);

            boolean bcChanged = false;
            if (domain.getNbCT() == 1) {
                // if we have only one classtype and a bc value corresponding to the mrbw, we also change the bc value.
                int ct = domain.getAllCTId()[0];
                if (oldMrbw == getBCs()[ct]) {
                    setBC(ct, bw);
                    bcChanged = true;
                }
            }

            if (!getDiffServModel().checkConstraints(domain, getBCs(), bw)) {
                /* revert changes */
                igpLink.getStatic().setMrbw(oldMrbw);
                if (bcChanged) {
                    setBC(domain.getAllCTId()[0], oldMrbw);
                }
                throw new DiffServConfigurationException("Model constraints are not satisfied for link " + getId());
            }

            /* Can lsps still exists ? */
            // save rbw
            float[] oldRbw = getRbw();
            // reset rbw
            resetRbw();
            
            //add reservation
            BandwidthManagement bwm = domain.getBandwidthManagement();
            try {
                bwm.recomputeRbw(this);
            } catch (LinkCapacityExceededException e) {
                /* revert changes */
                setRbw(oldRbw);
                igpLink.getStatic().setMrbw(oldMrbw);
                if (bcChanged) {
                    setBC(domain.getAllCTId()[0], oldMrbw);
                }
                // rethrow exception
                throw e;
            }
            domain.getObserver().notifyLinkBandwidthChange(this);
        }
    }

    /**
     * Sets the bandwidth of the link. This sets the true link capacity. It corresponds to mbw (Maximum Bandwidth)
     * in IGP and bw in normal link.
     * <br>It does not change the reservable bandwidth of the link (mrbw in igp).
     * @param value
     */
    public void setMaximumBandwidth(float value) {
        if (igpLink == null) {
            logger.warn(new StringBuffer().append("setBandwidth called on the link ").append(this.getId()).append(" which has no IGP link").toString());
        } else {
            // Create igp static section if not exist
            if (igpLink.getStatic() == null) {
                try {
                    createIGPStatic(value);
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            } else {
                igpLink.getStatic().setMbw(value);
                setBw(value);
            }
        }
    }

    /**
     * Return the maximum bandwidth of the link. It is the true link capacity.
     * @return Returns the Mbw value of igp static section if exists. Otherwise returns the link bw.
     */
    public float getMaximumBandwidth() {
        if (igpLink == null || igpLink.getStatic() == null) {
            return getBw();
        }
        if (igpLink.getStatic().getMbw() != getBw()) {
            logger.warn("Link bandwidth does not match igp mbw on link " + getId());
        }
        return igpLink.getStatic().getMbw();
    }

    /**
     * Get the status of the link. Returns down if the source or destination node is down or link is down.
     *
     * @return Link.STATUS_UP if the link is UP and Link.STATUS_DOWN otherwise
     */
    public int getLinkStatus() {
        try {
            if (getSrcNode().getNodeStatus() == Node.STATUS_DOWN || getDstNode().getNodeStatus() == Node.STATUS_DOWN) {
                return Link.STATUS_DOWN;
            }
        } catch (NodeNotFoundException e) {
            logger.fatal("Src or Dst Node not found.");
            e.printStackTrace();
        }
        if (this.getStatus() != null && this.getStatus().getValue().equals("DOWN")) {
            return Link.STATUS_DOWN;
        }
        return Link.STATUS_UP;
    }

    /**
     * Set the status of a link
     *
     * @param status
     * @throws StatusTypeException if status is neither STATUS_DOWN nor STATUS_UP
     */
    public void setLinkStatus(int status) throws StatusTypeException {
        logger.info("Link " + getId() + " set status " + ((status == STATUS_DOWN) ? "DOWN" : "UP"));
        if (status == getLinkStatus()) return;
        if (status == Link.STATUS_DOWN) {

            this.setStatus(StatusType.DOWN);

            for (Lsp lsp : domain.getLspsOnLink(this)) {
                lsp.linkDownEvent(this);
            }

            // notify the listeners
            domain.getObserver().notifyLinkStatusChange(this);            
        }
        else if (status == Link.STATUS_UP) {

            this.setStatus(StatusType.UP);

            for (Lsp lsp : domain.getLspsOnLink(this)) {
                lsp.linkUpEvent(this);
            }

            // notify the listeners
            domain.getObserver().notifyLinkStatusChange(this);

        } else {
            throw new StatusTypeException(new StringBuffer().append("Status ").append(status).append(" not allowed").toString());
        }
    }

    /**
     * Get the type of the node.
     * @return Return a member of the enum <code>Link.Type</code>. <code>Link.Type.INTRA</code> if type is not set.
     */
    public Type getLinkType() {
        Type type;

        if (this.getType() == null) {
            //this.setType(LinkType.INTRA);
            type = Type.INTRA;
        } else if (this.getType() == LinkType.INTRA) {
            type = Type.INTRA;
        } else if (this.getType() == LinkType.INTER) {
            type = Type.INTER;
        } else if (this.getType() == LinkType.ACCESS) {
            type = Type.ACCESS;
        } else if (this.getType() == LinkType.PEERING) {
            type = Type.PEERING;
        } else if (this.getType() == LinkType.VIRTUAL) {
            type = Type.VIRTUAL;
        } else {
            logger.error("Link type unknown. Assuming INTRA.");
            type = Type.INTRA;
        }

        return type;
    }

    public void setLinkType(Link.Type type) {
        switch (type) {
            case INTRA:
                this.setType(LinkType.INTRA);
                break;
            case INTER:
                this.setType(LinkType.INTER);
                break;
            case ACCESS:
                this.setType(LinkType.ACCESS);
                break;
            case PEERING:
                this.setType(LinkType.PEERING);
                break;
            case VIRTUAL:
                this.setType(LinkType.VIRTUAL);
                break;
        }
    }

    /**
     * Return the metric of the link
     *
     * @return
     */
    public float getMetric() {
        return igpLink.getStatic().getMetric();
    }
    
    /**
     * Sets the metric of the link.
     * @param metric The new metric of the link.
     */
    public void setMetric(float metric) {
        igpLink.getStatic().setMetric(metric);
        domain.getObserver().notifyLinkMetricChange(this);
    }

    /**
     * Return the TE metric of the link
     *
     * @return
     */
    public float getTEMetric() {
        return igpLink.getStatic().getTeMetric();
    }
    
    /**
     * Sets the TE metric of the link.
     * @param metric The new TE metric of the link.
     */
    public void setTEMetric(float metric) {
        igpLink.getStatic().setTeMetric(metric);
        domain.getObserver().notifyLinkTeMetricChange(this);
    }

    /**
     * Return the reservable bandwidth of the link (for the classType corresponding to minimum priority)
     * @return
     */
    public float getReservableBandwidth() {
        return getReservableBandwidth(domain.getMinPriority());
    }

    /**
     * Return the reserved bandwidth of the link for the classType corresponding to minimum priority
     * @return
     */
    public float getReservedBandwidth() {
        int ct = domain.getClassType(domain.getMinPriority());
        return getReservedBandwidthCT(ct);
    }

    /**
     * Returns a copy of the reservation array.
     * @return a new array indexed by priority level representing the current reservations.
     */
    public float[] getReservations() {
        return reservedBandwidth.clone();
    }

    public float getTotalReservedBandwidth() {
        float totalBw = 0;
        for (int ct : domain.getAllCTId()) {
            totalBw += getReservedBandwidthCT(ct);
        }
        return totalBw;
    }

    public float getTotalReservableBandwidth() {
        float totalBw = 0;
        for (int ct : domain.getAllCTId()) {
            totalBw += getReservableBandwidthCT(ct);
        }
        return totalBw;
    }

    /**
     * Return the reserved bandwidth of the link for the given classType
     * @param ct
     * @return
     */
    public float getReservedBandwidthCT(int ct) {
        float totalBW = 0;
        for (int q : domain.getPrioritySameCT(ct)) {
            totalBW += getReservedBandwidth(q);
        }
        return totalBW;
    }

    public float getReservableBandwidthCT(int ct) {
        return getReservableBandwidth(domain.getMinPriority(ct));
    }

    /**
     * Return the reservable bandwidth of the link for a given priority (found in database)
     * If domain does not use preemptions, the reservable bandwidth for minimum priority in the same class type is returned.
     * @param priority
     * @return
     */
    public float getReservableBandwidth(int priority) {
        if (!domain.usePreemption())
            priority = domain.getMinPriority(domain.getClassType(priority));
        return getRbw()[priority];
    }

    /**
     * Return the reserved bandwidth of the link for a given priority (bandwidth constraints model is used)
     * @param priority
     * @return
     */
    public float getReservedBandwidth(int priority) {
        return reservedBandwidth[priority];
    }

    /**
     * Adds a reservation on the link
     * As no priority is specified, this reservation is added to lowest prioritary class type and preemption level
     */
    public void addReservation(float bw) throws LinkCapacityExceededException {
        addReservation(bw, domain.getMinPriority());
    }

    /**
     * Adds a reservation on the link for a given priority (constraints are checked)
     * @param bw
     * @param priority
     */
    public void addReservation(float bw, int priority) throws LinkCapacityExceededException {

        logger.debug("Adding reservation (" + bw + ") to link " + getId() + " at priority " + priority);

        DiffServModel diffServModel = DiffServModelManager.getModel(getDiffServBCM());

        float[] rbwArray = getRbw();

        float[] bcArray = getBCs();

        if (rbwArray[priority] < bw) {
            throw new LinkCapacityExceededException();
        }
        reservedBandwidth[priority] += bw;

        rbwArray = diffServModel.getReservableBandwidth(domain, bcArray, reservedBandwidth, getBandwidth());
        setRbw(rbwArray);

        domain.getObserver().notifyLinkReservedBandwidthChange(this);

    }


    public int getDiffServBCM() {
        if ((igpLink !=null) && (igpLink.getStatic() != null) && (igpLink.getStatic().getDiffServ() != null)) {
            if (igpLink.getStatic().getDiffServ().getBcm().equals(BcmType.MAM))
                return DiffServConstant.DSMODEL_MAM;
            else if (igpLink.getStatic().getDiffServ().getBcm().equals(BcmType.RDM))
                return DiffServConstant.DSMODEL_RDM;
        }
        return DiffServConstant.DSMODEL_MAM;
    }

    public DiffServModel getDiffServModel() {
        return DiffServModelManager.getModel(getDiffServBCM());
    }

    /**
     * Removes a reservation from the default priority, and update reservable bandwidths accordingly
     * @param bw
     */
    public void removeReservation(float bw) throws LinkCapacityExceededException {
        removeReservation(bw, domain.getMinPriority());
    }

    /**
     * Removes a reservation from the priority specified, and update reservable bandwidths accordingly
     * @param bw
     * @param priority
     */
    public void removeReservation(float bw, int priority) throws LinkCapacityExceededException {
        logger.debug("Removing reservation (" + bw + ") to link " + getId() + " at priority " + priority);

        DiffServModel diffServModel = DiffServModelManager.getModel(getDiffServBCM());

        float[] rbwArray = getRbw();

        float[] bcArray = getBCs();

        //rbwArray = diffServModel.removeReservation(domain, priority, bw, bcArray, rbwArray, getBandwidth())
        if (reservedBandwidth[priority] < bw) {
            throw new LinkCapacityExceededException();
        }
        reservedBandwidth[priority] -= bw;

        rbwArray = diffServModel.getReservableBandwidth(domain, bcArray, reservedBandwidth, getBandwidth());

        setRbw(rbwArray);

        domain.getObserver().notifyLinkReservedBandwidthChange(this);

    }

    /** Gets the BCs, should not be needed for most algorithms
     *
     * @return an array indexed with BC ids and corresponding float values
     */
    public float[] getBCs(){
        float[] bcArray = new float[8];
        for (int i=0; i<bcArray.length; i++) bcArray[i]=0;
        for (int i=0; i < igpLink.getStatic().getDiffServ().getBc().size(); i++){
            int bcId = ((LinkIgp.StaticType.DiffServType.BcType) igpLink.getStatic().getDiffServ().getBc().get(i)).getId();
            float bcValue = ((LinkIgp.StaticType.DiffServType.BcType) igpLink.getStatic().getDiffServ().getBc().get(i)).getValue();
            bcArray[bcId]=bcValue;
        }
        return bcArray;
    }

    /**
     * Set the BC to the given value.
     * @param ct class type
     * @param value
     * @throws DiffServConfigurationException when the classType cannot be found in the domain.
     * @throws LinkCapacityExceededException If the new BC value do not leave enough bandwidth for the current established LSPs.
     */
    public void setBC(int ct, float value) throws DiffServConfigurationException, LinkCapacityExceededException {
        boolean found = false;
        for (int fct : domain.getAllCTId()) {
            if (fct == ct) {
                found = true;
                break;
            }
        }
        if (!found) throw new DiffServConfigurationException("Non existing class type.");

        float bcs[] = getBCs();
        bcs[ct] = value;
        //check diff serv model constraints
        if (!getDiffServModel().checkConstraints(domain, bcs, getBandwidth())) {
            throw new DiffServConfigurationException("Model constraints are not satisfied for link " + getId());
        }

        LinkIgp.StaticType.DiffServType.BcType bc = null;
        float oldValue = -1;
        for (Object o : igpLink.getStatic().getDiffServ().getBc()) {
            if (((LinkIgp.StaticType.DiffServType.BcType)o).getId() == ct) {
                bc = (LinkIgp.StaticType.DiffServType.BcType)o;
                oldValue = bc.getValue();
            }
        }

        bc.setValue(value);

        logger.info("Setting BC for CT " + ct + " value: " + value);

        /* Can lsps still exists ? */
        // reset reservation
        resetRbw();

        //add reservation
        BandwidthManagement bwm = domain.getBandwidthManagement();
        try {
            bwm.recomputeRbw(this);
        } catch (LinkCapacityExceededException e) {
            /* revert changes */
            bc.setValue(oldValue);
            resetRbw();
            bwm.recomputeRbw(this);
            // rethrow exception
            throw e;
        }
    }

    public float[] getRbw() {
        float[] rbwArray = new float[DiffServConstant.MAX_NB_PRIORITY];
        for (int i=0; i<rbwArray.length; i++) rbwArray[i]=-1;
        for (int i=0; i < domain.getNbPriority(); i++){
            int priorityId = ((LinkIgp.DynamicType.RbwType.PriorityType)igpLink.getDynamic().getRbw().getPriority().get(i)).getId();
            float rbwValue = ((LinkIgp.DynamicType.RbwType.PriorityType)igpLink.getDynamic().getRbw().getPriority().get(i)).getValue();
            rbwArray[priorityId]=rbwValue;
        }
        return rbwArray;
    }

    public void setRbw(float[] rbw) {
        for (int i=0; i<rbw.length; i++) {
            if (rbw[i] != -1) {
                ((LinkIgp.DynamicType.RbwType.PriorityType)igpLink.getDynamic().getRbw().getPriority().get(rbwMapping[i])).setValue(rbw[i]);
            }
        }
    }

    /**
     * Reset the bandwidth values of rbw to the bw values defined for BCs
     * <br> Also reset the reserved bandwidth array to 0.
     */
    private void resetRbw() {
        Arrays.fill(reservedBandwidth, 0);
        for (int cct : domain.getAllCTId()) {
            boolean found = false;
            for (Object bcObj : igpLink.getStatic().getDiffServ().getBc()) {
                LinkIgp.StaticType.DiffServType.BcType bc = (LinkIgp.StaticType.DiffServType.BcType)bcObj;
                if (bc.getId() == cct) {
                    float cbcValue = bc.getValue();
                    for (int prio : domain.getPrioritySameCT(cct)) {
                        ((LinkIgp.DynamicType.RbwType.PriorityType)igpLink.getDynamic().getRbw().getPriority().get(rbwMapping[prio])).setValue(cbcValue);
                    }
                    found = true;
                    break;
                }
            }
            if (!found) logger.error("BC not found for classtype " + cct);
        }
    }

    /**
     * This function should be called after a priority is added to the domain. The new priority is added to
     * the rbw array and the rbw array is refreshed (thanks to the BandwidthManagement).
     * If the priority corresponds to a new class type, a correponding BC is added (with 0 bandwidth).<br>
     * Warning: this method is not intended to be called directly.
     * @param priority the priority that was added.
     */
    public void addPriority(int priority) {
        ObjectFactory factory = new ObjectFactory();

        List priorityList = igpLink.getDynamic().getRbw().getPriority();
        try {

            int classType = domain.getClassType(priority);

            /* check BC existence. If not found, add the BC */
            boolean foundBcId = false;
            for (int i = 0; i < igpLink.getStatic().getDiffServ().getBc().size(); i++) {
                int bcId = ((LinkIgp.StaticType.DiffServType.BcType)igpLink.getStatic().getDiffServ().getBc().get(i)).getId();
                if (bcId == classType) {
                    foundBcId = true;
                    break;
                }
            }

            if (!foundBcId) {
                LinkIgp.StaticType.DiffServType.BcType newBc = factory.createLinkIgpStaticTypeDiffServTypeBcType();
                newBc.setId(classType);
                newBc.setValue(0);
                igpLink.getStatic().getDiffServ().getBc().add(newBc);
            }

            LinkIgp.DynamicType.RbwType.PriorityType p = factory.createLinkIgpDynamicTypeRbwTypePriorityType();
            p.setId(priority);
            priorityList.add(p);

            //rebuild rbwMapping
            rbwMapping = new int[DiffServConstant.MAX_NB_PRIORITY];
            for (int i = 0; i < this.domain.getNbPriority(); i++) {
                int priorityId = ((LinkIgp.DynamicType.RbwType.PriorityType) priorityList.get(i)).getId();
                rbwMapping[priorityId] = i;
            }

            // reset rbw
            resetRbw();

            //recompute rbw
            BandwidthManagement bwm = domain.getBandwidthManagement();
            try {
                bwm.recomputeRbw(this);
            } catch (LinkCapacityExceededException e) {
                /* should not happen here since we only add a priority level */
                logger.fatal("Error while recomputing rbw after adding a priority level");
                e.printStackTrace();
            }

        } catch (JAXBException e) {
            logger.error("Impossible to add the priority " + priority + "on link " + getId());
            e.printStackTrace();
        }
    }

    /**
     * This function should be called after a priority is removed from the domain. The priority is removed from
     * the rbw array and the rbw array is refreshed (thanks to the BandwidthManagement).
     * @param priority
     */
    public void removePriority(int priority) {
        List priorityList = igpLink.getDynamic().getRbw().getPriority();
        int index = rbwMapping[priority];
        priorityList.remove(index);

        //rebuild rbwMapping
        rbwMapping = new int[DiffServConstant.MAX_NB_PRIORITY];
        for (int i = 0; i < this.domain.getNbPriority(); i++) {
            int priorityId = ((LinkIgp.DynamicType.RbwType.PriorityType) priorityList.get(i)).getId();
            rbwMapping[priorityId] = i;
        }

        // reset rbw
        resetRbw();

        //recompute rbw
        BandwidthManagement bwm = domain.getBandwidthManagement();
        try {
            bwm.recomputeRbw(this);
        } catch (LinkCapacityExceededException e) {
            logger.fatal("Error while recomputing rbw after removing a priority level");
            e.printStackTrace();
        }
    }


    /**
     * Two links are equal if they belong to the same domain and have the same
     * id.
     */
    public boolean equals(Object o) {
    	if(!(o instanceof LinkImpl)) {
    		return false;
    	}
    	
    	LinkImpl link = (LinkImpl) o;
    	return (link.domain.getASID() == this.domain.getASID()) && (link.getId().equals(this.getId()));
    }
    
    public int hashCode() {
    	return domain.getASID() + getId().hashCode();
    }

    public void setElementId(String id) throws IdException {
        try {
            domain.getLink(id);
            throw new IdException("Cannot set Id when element is in the domain.");
        } catch (LinkNotFoundException e) {
            super.setId(id);
            igpLink.setId(id);
        }
    }

    public Domain getDomain() {
        return domain;
    }
}
