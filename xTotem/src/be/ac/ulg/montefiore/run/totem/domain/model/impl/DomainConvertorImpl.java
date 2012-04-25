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
import be.ac.ulg.montefiore.run.totem.domain.exception.*;

import java.util.HashMap;
import java.util.List;

/*
 * Changes:
 * --------
 * - 11-May-2007: Add renameLspId(.) method. (GMO) 
 */

/**
 * This convertor allow to convert any node,link or LSP id from int to String.
 * These conversions are needed in each class that works on int id instead of String id.
 *
 * WARNING : there is no assumption that the integer id are consecutive. When removing
 * a node, link or Lsp elements, we remove the element but we not reuse its int id for other elements.
 * Instead, the next integer id is used i.e. a not already used integer.
 *
 * <p>Creation date: 19-Jan-2005 18:40:01
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public class DomainConvertorImpl implements DomainConvertor {

    private HashMap<String,Integer> nodeString2Int;
    private HashMap<Integer,String> nodeInt2String;
    private int maxNodeIntId;
    private HashMap<String,Integer> linkString2Int;
    private HashMap<Integer,String> linkInt2String;
    private int maxLinkIntId;
    private HashMap<String,Integer> lspString2Int;
    protected HashMap<Integer,String> lspInt2String;
    private int maxLspIntId;

    /**
     * Create all the conversion table from a domain
     *
     * @param domain
     */
    public DomainConvertorImpl(Domain domain) {
        // Create HashMap objects
        nodeString2Int = new HashMap<String,Integer>(domain.getNbNodes());
        nodeInt2String = new HashMap<Integer,String>(domain.getNbNodes());
        linkString2Int = new HashMap<String,Integer>(domain.getNbLinks());
        linkInt2String = new HashMap<Integer,String>(domain.getNbLinks());
        lspString2Int = new HashMap<String,Integer>(domain.getNbLsps());
        lspInt2String = new HashMap<Integer,String>(domain.getNbLsps());
        maxNodeIntId = 0;
        maxLinkIntId = 0;
        maxLspIntId = 0;

        // Populate the nodes conversion tables
        List<Node> nodeList = domain.getAllNodes();
        if (nodeList !=null) {
            for (int i = 0; i < nodeList.size(); i++) {
                Node node = nodeList.get(i);
                Integer nodeIntegerId = new Integer(i);
                nodeString2Int.put(node.getId(),nodeIntegerId);
                nodeInt2String.put(nodeIntegerId,node.getId());
            }
            maxNodeIntId = nodeList.size();
        }

        // Populate the links conversion tables
        List<Link> linkList = domain.getAllLinks();
        if (linkList !=null) {
            for (int i = 0; i < linkList.size(); i++) {
                Link link = linkList.get(i);
                Integer linkIntegerId = new Integer(i);
                linkString2Int.put(link.getId(),linkIntegerId);
                linkInt2String.put(linkIntegerId,link.getId());
            }
            maxLinkIntId = linkList.size();
        }

        // Populate the LSPs conversion tables
        List<Lsp> lspList = domain.getAllLsps();
        if (lspList != null) {
            for (int i = 0; i < lspList.size(); i++) {
                Lsp lsp = lspList.get(i);
                Integer lspIntegerId = new Integer(i);
                lspString2Int.put(lsp.getId(),lspIntegerId);
                lspInt2String.put(lspIntegerId,lsp.getId());
            }
            maxLspIntId = lspList.size();
        }

    }

    /**
     * Get the max node int id
     *
     * @return
     */
    public int getMaxNodeId() {
        return maxNodeIntId;
    }

    /**
     * Convert a String node id in a int node id
     *
     * @param id
     * @return
     * @throws NodeNotFoundException
     */
    public int getNodeId(String id) throws NodeNotFoundException {
        if (!nodeString2Int.containsKey(id)) {
            throw new NodeNotFoundException(new StringBuffer().append("Node ").append(id).append(" not found in the conversion table").toString());
        }
        return nodeString2Int.get(id).intValue();
    }

    /**
     * Convert a int node id in a String node id
     *
     * @param id
     * @return
     * @throws NodeNotFoundException
     */
    public String getNodeId(int id) throws NodeNotFoundException {
        Integer i = new Integer(id);
        if (nodeInt2String.get(i) == null) {
            throw new NodeNotFoundException(new StringBuffer().append("Node ").append(id).append(" not found in the conversion table").toString());
        }
        return nodeInt2String.get(i);
    }


    /**
     * Add a conversion for the nodeId node
     *
     * @param nodeId
     * @throws NodeAlreadyExistException
     */
    public void addNodeId(String nodeId) throws NodeAlreadyExistException {
        Integer i = new Integer(maxNodeIntId);
        maxNodeIntId++;
        if (nodeString2Int.containsKey(nodeId)) {
            throw new NodeAlreadyExistException(new StringBuffer().append("Node ").append(nodeId).append(" already in the conversion table").toString());
        }
        if (nodeInt2String.containsKey(i)) {
            throw new NodeAlreadyExistException(new StringBuffer().append("Node ").append(nodeId).append(" already in the conversion table").toString());
        }
        nodeString2Int.put(nodeId,i);
        nodeInt2String.put(i,nodeId);
    }

    /**
     * Remove the conversion for the node nodeId
     *
     * @param nodeId
     * @throws NodeNotFoundException
     */
    public void removeNodeId(String nodeId) throws NodeNotFoundException {
        if (!nodeString2Int.containsKey(nodeId)) {
            throw new NodeNotFoundException(new StringBuffer().append("Node ").append(nodeId).append(" not found in the conversion table").toString());
        }
        if (!nodeInt2String.containsKey(nodeString2Int.get(nodeId))) {
            throw new NodeNotFoundException(new StringBuffer().append("Node ").append(nodeId).append(" not found in the conversion table").toString());
        }
        nodeInt2String.remove(nodeString2Int.get(nodeId));
        nodeString2Int.remove(nodeId);
    }


    /**
     * Get the max link int id
     * @return
     */
    public int getMaxLinkId() {
        return maxLinkIntId;
    }

    /**
     * Convert a String link id in a int link id
     *
     * @param id
     * @return
     * @throws LinkNotFoundException
     */
    public int getLinkId(String id) throws LinkNotFoundException {
        if (linkString2Int.get(id) == null) {
            throw new LinkNotFoundException(new StringBuffer().append("Link ").append(id).append(" not found in the conversion table").toString());
        }
        return linkString2Int.get(id).intValue();
    }

    /**
     * Convert a int link id in a String link id
     *
     * @param id
     * @return
     * @throws LinkNotFoundException
     */
    public String getLinkId(int id) throws LinkNotFoundException {
        Integer i = new Integer(id);
        if (linkInt2String.get(i) == null) {
            throw new LinkNotFoundException(new StringBuffer().append("Link ").append(id).append(" not found in the conversion table").toString());
        }
        return linkInt2String.get(i);
    }

    /**
     * Add a conversion for the linkId link
     *
     * @param linkId
     * @throws LinkAlreadyExistException
     */
    public void addLinkId(String linkId) throws LinkAlreadyExistException{
        Integer i = new Integer(maxLinkIntId);
        maxLinkIntId++;
        if (linkString2Int.containsKey(linkId)) {
            throw new LinkAlreadyExistException(new StringBuffer().append("Link ").append(linkId).append(" already in the conversion table").toString());
        }
        if (linkInt2String.containsKey(i)) {
            throw new LinkAlreadyExistException(new StringBuffer().append("Link ").append(linkId).append(" already in the conversion table").toString());
        }
        linkString2Int.put(linkId,i);
        linkInt2String.put(i,linkId);
    }

    /**
     * Remove the conversion for the link linkId
     *
     * @param linkId
     * @throws LinkNotFoundException
     */
    public void removeLinkId(String linkId) throws LinkNotFoundException {
        if (!linkString2Int.containsKey(linkId)) {
            throw new LinkNotFoundException(new StringBuffer().append("Link ").append(linkId).append(" not found in the conversion table").toString());
        }
        if (!linkInt2String.containsKey(linkString2Int.get(linkId))) {
            throw new LinkNotFoundException(new StringBuffer().append("Link ").append(linkId).append(" not found in the conversion table").toString());
        }
        linkInt2String.remove(linkString2Int.get(linkId));
        linkString2Int.remove(linkId);
    }

    /**
     * Get the max LSP int id
     * @return
     */
    public int getMaxLspId() {
        return maxLspIntId;
    }

    /**
     * Convert a String LSP id in a int LSP id
     *
     * @param id
     * @return
     * @throws LspNotFoundException
     */
    public int getLspId(String id) throws LspNotFoundException {
        if (lspString2Int.get(id) == null) {
            throw new LspNotFoundException(new StringBuffer().append("LSP ").append(id).append(" not found in the conversion table").toString());
        }
        return lspString2Int.get(id).intValue();
    }

    /**
     * Convert a int lsp id in a String lsp id
     *
     * @param id
     * @return
     * @throws LspNotFoundException
     */
    public String getLspId(int id) throws LspNotFoundException {
        Integer i = new Integer(id);
        if (lspInt2String.get(i) == null) {
            throw new LspNotFoundException(new StringBuffer().append("LSP ").append(id).append(" not found in the conversion table").toString());
        }
        return lspInt2String.get(i);
    }

    /**
     * Add a conversion for the lspId LSP
     *
     * @param lspId
     * @throws LspAlreadyExistException
     */
    public void addLspId(String lspId) throws LspAlreadyExistException{
        Integer i = new Integer(maxLspIntId);
        maxLspIntId++;
        if (lspString2Int.containsKey(lspId)) {
            throw new LspAlreadyExistException(new StringBuffer().append("LSP ").append(lspId).append(" already in the conversion table").toString());
        }
        if (lspInt2String.containsKey(i)) {
            throw new LspAlreadyExistException(new StringBuffer().append("LSP ").append(lspId).append(" already in the conversion table").toString());
        }
        lspString2Int.put(lspId,i);
        lspInt2String.put(i,lspId);
    }

    /**
     * Remove the conversion for the lspId LSP
     *
     * @param lspId
     * @throws LspNotFoundException
     */
    public void removeLspId(String lspId) throws LspNotFoundException {
        if (!lspString2Int.containsKey(lspId)) {
            throw new LspNotFoundException(new StringBuffer().append("Lsp ").append(lspId).append(" not found in the conversion table").toString());
        }
        if (!lspInt2String.containsKey(lspString2Int.get(lspId))) {
            throw new LspNotFoundException(new StringBuffer().append("Lsp ").append(lspId).append(" not found in the conversion table").toString());
        }
        lspInt2String.remove(lspString2Int.get(lspId));
        lspString2Int.remove(lspId);
    }

    /**
     * Rename an LSP without changing its int id
     * @param oldId
     * @param newId
     * @throws LspNotFoundException if the oldId not in the index
     * @throws LspAlreadyExistException If the newI already in the index
     */
    public void renameLspId(String oldId, String newId) throws LspNotFoundException, LspAlreadyExistException {
        if (!lspString2Int.containsKey(oldId)) {
            throw new LspNotFoundException(new StringBuffer().append("Lsp ").append(oldId).append(" not found in the conversion table").toString());
        }
        if (lspString2Int.containsKey(newId)) {
            throw new LspAlreadyExistException(new StringBuffer().append("LSP ").append(newId).append(" already in the conversion table").toString());
        }

        int intId = lspString2Int.remove(oldId);
        lspString2Int.put(newId, intId);
        lspInt2String.put(intId, newId);

    }

    public void renameNodeId(String oldId, String newId) throws NodeAlreadyExistException, NodeNotFoundException {
        if (!nodeString2Int.containsKey(oldId)) {
            throw new NodeNotFoundException(new StringBuffer().append("Node ").append(oldId).append(" not found in the conversion table").toString());
        }
        if (nodeString2Int.containsKey(newId)) {
            throw new NodeAlreadyExistException(new StringBuffer().append("Node ").append(newId).append(" already in the conversion table").toString());
        }

        int intId = nodeString2Int.remove(oldId);
        nodeString2Int.put(newId, intId);
        nodeInt2String.put(intId, newId);
    }

    public void renameLinkId(String oldId, String newId) throws LinkAlreadyExistException, LinkNotFoundException {
        if (!linkString2Int.containsKey(oldId)) {
            throw new LinkNotFoundException(new StringBuffer().append("Link ").append(oldId).append(" not found in the conversion table").toString());
        }
        if (linkString2Int.containsKey(newId)) {
            throw new LinkAlreadyExistException(new StringBuffer().append("Link ").append(newId).append(" already in the conversion table").toString());
        }

        int intId = linkString2Int.remove(oldId);
        linkString2Int.put(newId, intId);
        linkInt2String.put(intId, newId);
    }

}
