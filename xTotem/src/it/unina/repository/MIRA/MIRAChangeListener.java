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

package it.unina.repository.MIRA;

import org.apache.log4j.Logger;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AddDBException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;

/*
 * Changes:
 * --------
 */

/**
 *
 * <p/>
 * <p>Creation date : 9 juin 2005 14:19:53
 *
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class MIRAChangeListener implements DomainChangeListener {
    private static Logger logger = Logger.getLogger(MIRAChangeListener.class);
    private Domain domain;
    private MIRA instance;

    public MIRAChangeListener(Domain domain, MIRA instance) {
        this.domain = domain;
        this.instance = instance;
    }

    public void addNodeEvent(Node node) {
        int type = 1;
        if (node.getNodeType() == Node.Type.CORE)
            type = 0;

        try {
            JNIMIRA.jniaddNode(domain.getConvertor().getNodeId(node.getId()), type);
        }
        catch(AddDBException e) {
            logger.error("Error while trying to add the node "+node.getId()+" to the MIRA database!");
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
            JNIMIRA.jniremoveNode(domain.getConvertor().getNodeId(node.getId()));
        } catch (AddDBException e) {
            logger.error("Error while trying to remove the node "+node.getId()+" from the MIRA database!");
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
        // there is no mean to put a node up/down in MIRA so we have to
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
            int srcId = domain.getConvertor().getNodeId(link.getSrcNode().getId());
            int dstId = domain.getConvertor().getNodeId(link.getDstNode().getId());

            JNIMIRA.jniaddLink(srcId, dstId, link.getBandwidth(), link.getReservedBandwidth(), link.getMetric());
        } catch (NodeNotFoundException e) {
            logger.error("A node of the link "+link.getId()+" is unknown!");
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        } catch (AddDBException e) {
            logger.error("Error while trying to add the link "+link.getId()+" to the MIRA database!");
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    public void removeLinkEvent(Link link) {
        try {
            JNIMIRA.jniremoveLink(domain.getConvertor().getNodeId(link.getSrcNode().getId()), domain.getConvertor().getNodeId(link.getDstNode().getId()));
        } catch (AddDBException e) {
            logger.error("Error while trying to remove the link "+link.getId()+" from the MIRA database!");
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
        // there is no mean to put a link up/down in MIRA so we have to
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
            logger.error("Error while trying to add the LSP "+lsp.getId()+" to the MIRA database!");
            logger.error("Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    public void removeLspEvent(Lsp lsp) {
        try {
            instance.removeLSP(lsp.getId());
        } catch (RoutingException e) {
            logger.error("Error while trying to remove the LSP "+lsp.getId()+" from the MIRA database!");
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }

    public void lspReservationChangeEvent(Lsp lsp) {
        instance.invalidateDB();
    }

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
