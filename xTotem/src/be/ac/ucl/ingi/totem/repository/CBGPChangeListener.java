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

package be.ac.ucl.ingi.totem.repository;

import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;

/**
 * Tient CBGP au courant des evenements 
 * @author Thomas Vanstals
 *
 */
public class CBGPChangeListener extends DomainChangeAdapter {

    private static CBGPChangeListener cBGPChangeListener = null;
    private static InterDomainManager interDomainManager = null;
    private Domain domain = null;
    private CBGP cbgp = null;
    
    /**
     * A private Constructor
     */
    private CBGPChangeListener() {
        domain = InterDomainManager.getInstance().getDefaultDomain();
        interDomainManager = InterDomainManager.getInstance();
        // CBGP simulator available ?? 
        try {
            cbgp = (CBGP) RepositoryManager.getInstance().getAlgo("CBGP");
        } catch (NoSuchAlgorithmException e) {
            cBGPChangeListener = null;
        }
        domain = interDomainManager.getDefaultDomain();
    }
    
    /**
     * Return a single instance of the CBGPDomainChangeAdapter
     *
     * @return an instance of this class
     */
    public static CBGPChangeListener getInstance() {
        if (cBGPChangeListener == null) {
            cBGPChangeListener = new CBGPChangeListener();
        }
        return cBGPChangeListener;
    }
    
    /**
     * Notify a add node event
     *
     * @param node
     */
    public void addNodeEvent(Node node) {}

    /**
     * Notify a remove node event
     *
     * @param node
     */
    public void removeNodeEvent(Node node) {}

    /**
     * Notify a node status change event
     * 
     * @param node
     */
    public void nodeStatusChangeEvent(Node node) {
        // for every in/out link, set link down
        boolean up = true;
        if (node.getNodeStatus() == Node.STATUS_DOWN){
            // node become down
            up = false;
        } // else node become up
        java.util.List<Link> inLinkList = node.getAllInLink();
        java.util.List<Link> outLinkList = node.getAllOutLink();
        int i;
        for (i= 0; i < inLinkList.size(); i++) {
            // for every inLink
            Link link = inLinkList.get(i);
            try { 
                // System.out.println(link.getSrcNode().getRid()+" - "+link.getDstNode().getRid());
                // update link status
                cbgp.netLinkUp(link.getSrcNode().getRid(), link.getDstNode().getRid(), up);
                cbgp.computeIGP(domain);
                cbgp.bgpDomainRescan(domain);
                cbgp.simRun();
            } catch (Exception e){
                System.out.println("error while updating link status");
                e.printStackTrace();
            }
        }
        
        for (i= 0; i < outLinkList.size(); i++) {
            // for every outLink
            Link link = inLinkList.get(i);
            try { 
                // update link status
                cbgp.netLinkUp(link.getSrcNode().getRid(), link.getDstNode().getRid(), up);
                cbgp.computeIGP(domain);
                cbgp.bgpDomainRescan(domain);
                cbgp.simRun();
            } catch (Exception e){
                System.out.println("error while updating link status");
                e.printStackTrace();
            }
        }
    }

    public void nodeLocationChangeEvent(Node node) {
    }

    /**
     * Notify a add link event
     *
     * @param link
     */
    public void addLinkEvent(Link link) {
    }

    /**
     * Notify a remove link event
     *
     * @param link
     */
    public void removeLinkEvent(Link link) {}

    /**
     * Notify a link status change event
     *
     * @param link
     */
    public void linkStatusChangeEvent(Link link) {
        boolean up = true; // link up 
        if (link.getLinkStatus() == 1) // link down
            up = false;
        try {
            // update link status
            cbgp.netLinkUp(link.getSrcNode().getRid(), link.getDstNode().getRid(), up);
            cbgp.computeIGP(domain);
            cbgp.bgpDomainRescan(domain);
            cbgp.simRun();
        } catch (Exception e){
            System.out.println("error while updating link status");
        }
    }

    /**
     * Notify a link metric change event
     *
     * @param link
     */
    public void linkMetricChangeEvent(Link link) {
        int iWeight = ((Float) link.getMetric()).intValue();
        try {
            // update link weight
            cbgp.netLinkWeight(link.getSrcNode().getRid(), link.getDstNode().getRid(), iWeight);
            cbgp.computeIGP(domain);
            cbgp.bgpDomainRescan(domain);
            cbgp.simRun();
        } catch (Exception e){
            System.out.println("error while updating link metric");
        }
    }

    /**
     * Notify a link TE metric change event
     *
     * @param link
     */
    public void linkTeMetricChangeEvent(Link link) {}

    /**
     * Notify a link bandwidth change event
     *
     * @param link
     */
    public void linkBandwidthChangeEvent(Link link) {}

    /**
     * Notify a link reserved bandwidth change event
     *
     * @param link
     */
    public void linkReservedBandwidthChangeEvent(Link link) {}

    /**
     * Notify a link delay change event
     *
     * @param link
     */
    public void linkDelayChangeEvent(Link link) {}

    /**
     * Notify a add LSP event
     *
     * @param lsp
     */
    public void addLspEvent(Lsp lsp) {}

    /**
     * Notify a remove LSP event
     *
     * @param lsp
     */
    public void removeLspEvent(Lsp lsp) {}
    
    /**
     * Notify a LSP reservation change event
     *
     * @param lsp
     */
    public void lspReservationChangeEvent(Lsp lsp) {}
}
