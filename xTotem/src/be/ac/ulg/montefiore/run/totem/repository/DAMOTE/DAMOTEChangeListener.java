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
package be.ac.ulg.montefiore.run.totem.repository.DAMOTE;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.LinkNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AddDBException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;

/*
 * Changes:
 * --------
 * - 05-Dec-2007: invalidate the database if an LSP can't be added/removed (GMO)
 * - 26-Feb-2008: invalidate database when the LSP change its status (GMO)
 */

/**
 * 
 *
 * <p>Creation date: 02-mai-2005
 *
 * @author Jean Lepropre (lepropre@run.montefiore.ulg.ac.be)
 */
public class DAMOTEChangeListener implements DomainChangeListener {

    private static final Logger logger = Logger.getLogger(DAMOTEChangeListener.class);
    private Domain domain;
    private DAMOTE instance;
    
    public DAMOTEChangeListener(Domain domain, DAMOTE instance) {
        this.domain = domain;
        this.instance = instance;
    }
    
    public void addNodeEvent(Node node) {
        try {
        	instance.getJniInstance().jniaddNode(domain.getConvertor().getNodeId(node.getId()));
        }
        catch(AddDBException e) {
            instance.invalidateDB();
            logger.error("Error while trying to add the node "+node.getId()+" to the DAMOTE database!");
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
        catch(NodeNotFoundException e) {
            logger.error("Unknown node "+node.getId());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }
    
    public void removeNodeEvent(Node node) {
        try {
        	instance.getJniInstance().jniremoveNode(domain.getConvertor().getNodeId(node.getId()));
        } catch (AddDBException e) {
            instance.invalidateDB();
            logger.error("Error while trying to remove the node "+node.getId()+" from the DAMOTE database!");
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        } catch (NodeNotFoundException e) {
            logger.error("Unknown node "+node.getId());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }
    
    public void nodeStatusChangeEvent(Node node) {
        /*
        // there is no mean to put a node up/down in DAMOTE so we have to
        // add/remove this...
        if(node.getNodeStatus() == Node.STATUS_DOWN) {
            this.removeNodeEvent(node);
        }
        else {
            this.addNodeEvent(node);
        }
        */
        instance.invalidateDB();
    }

    public void nodeLocationChangeEvent(Node node) {
    }

    public void addLinkEvent(Link link) {
        try {
            int linkId = domain.getConvertor().getLinkId(link.getId());
            int srcId = domain.getConvertor().getNodeId(link.getSrcNode().getId());
            int dstId = domain.getConvertor().getNodeId(link.getDstNode().getId());
            int nbOA = domain.getMaxCTvalue() + 1;
            int nbPL = domain.getMaxPLvalue() + 1;
            float[][] reservedbwArray = new float[nbOA][nbPL];
            for (int classType=0;classType<nbOA;classType++){
                for (int preemptionLevel=0;preemptionLevel<nbPL;preemptionLevel++){
                    if (domain.isExistingPriority(preemptionLevel,classType)){
                        int priority = domain.getPriority(preemptionLevel,classType);
                        reservedbwArray[classType][preemptionLevel] = link.getReservedBandwidth(priority);
                    }
                    else reservedbwArray[classType][preemptionLevel] = 0;
                }
            }
            instance.getJniInstance().jniaddLink(linkId, 0, srcId, dstId, link.getBCs(), reservedbwArray, null, null, null);
        } catch (LinkNotFoundException e) {
            logger.error("Unknown link "+link.getId());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        } catch (NodeNotFoundException e) {
            logger.error("A node of the link "+link.getId()+" is unknown!");
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        } catch (AddDBException e) {
            instance.invalidateDB();
            logger.error("Error while trying to add the link "+link.getId()+" to the DAMOTE database!");
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    public void removeLinkEvent(Link link) {
        try {
            instance.getJniInstance().jniremoveLink(domain.getConvertor().getNodeId(link.getSrcNode().getId()), domain.getConvertor().getNodeId(link.getDstNode().getId()));
        } catch (AddDBException e) {
            instance.invalidateDB();
            logger.error("Error while trying to remove the link "+link.getId()+" from the DAMOTE database!");
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        } catch (NodeNotFoundException e) {
            logger.error("A node of the link "+link.getId()+" is unknown!");
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    public void linkStatusChangeEvent(Link link) {
        /*
        // there is no mean to put a link up/down in DAMOTE so we have to
        // add/remove this...
        if(link.getLinkStatus() == Link.STATUS_DOWN) {
            this.removeLinkEvent(link);
        }
        else {
            this.addLinkEvent(link);
        }
        */
        instance.invalidateDB();
    }

    public void linkMetricChangeEvent(Link link) {
        instance.invalidateDB();
    }

    public void linkTeMetricChangeEvent(Link link) {
        instance.invalidateDB();
    }

    public void linkBandwidthChangeEvent(Link link) {
        instance.invalidateDB();
    }

    public void linkReservedBandwidthChangeEvent(Link link) {
        //instance.invalidateDB();
    }

    public void linkDelayChangeEvent(Link link) {
        instance.invalidateDB();
    }

    public void addLspEvent(Lsp lsp) {
        try {
            instance.addLSP(lsp);
        }
        catch(RoutingException e) {
            instance.invalidateDB();
            logger.warn("Error while trying to add the LSP "+lsp.getId()+" to the DAMOTE database!");
            logger.warn("Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    public void removeLspEvent(Lsp lsp) {
        try {
            instance.getJniInstance().jniremoveLSP(domain.getConvertor().getLspId(lsp.getId()));
        } catch (AddDBException e) {
            instance.invalidateDB();
            logger.warn("Error while trying to remove the LSP "+lsp.getId()+" from the DAMOTE database!");
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        } catch (LspNotFoundException e) {
            logger.warn("Unknown LSP "+lsp.getId());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    public void lspReservationChangeEvent(Lsp lsp) {
        instance.invalidateDB();
    }

    /**
     * Notify a change in the working path of the lsp
     *
     * @param lsp
     */
    public void lspWorkingPathChangeEvent(Lsp lsp) {
    }

    /**
     * Notify a change in the status of a lsp
     *
     * @param lsp
     */
    public void lspStatusChangeEvent(Lsp lsp) {
        instance.invalidateDB();
    }

}
