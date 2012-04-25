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
package be.ac.ulg.montefiore.run.totem.domain.model;

import be.ac.ulg.montefiore.run.totem.domain.exception.*;

/*
 * Changes:
 * --------
 *
 */

/**
 * Define all the methods that a convertor must implements i.e.
 *  - convert node id from String to int and inversely
 *  - convert link id from String to int and inversely
 *  - convert lsp id from String to int and inversely
 *
 * <p>Creation date: 12-Jan-2005 17:49:13
 *
 * @author  Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 */
public interface DomainConvertor {

    // Node conversion
    /**
     * Get the max node int id
     *
     * @return
     */
    public int getMaxNodeId();

    /**
     * Convert a String node id in a int node id
     *
     * @param id
     * @return
     * @throws NodeNotFoundException
     */
    public int getNodeId(String id) throws NodeNotFoundException;

    /**
     * Convert a int node id in a String node id
     *
     * @param id
     * @return
     * @throws NodeNotFoundException
     */
    public String getNodeId(int id) throws NodeNotFoundException;

    /**
     * Add a conversion for the nodeId node
     *
     * @param nodeId
     * @throws NodeAlreadyExistException
     */
    public void addNodeId(String nodeId) throws NodeAlreadyExistException;

    /**
     * Remove the conversion for the node nodeId
     *
     * @param nodeId
     * @throws NodeNotFoundException
     */
    public void removeNodeId(String nodeId) throws NodeNotFoundException;

    // Link conversion
    /**
     * Get the max link int id
     * @return
     */
    public int getMaxLinkId();

    /**
     * Convert a String link id in a int link id
     *
     * @param id
     * @return
     * @throws LinkNotFoundException
     */
    public int getLinkId(String id) throws LinkNotFoundException;

    /**
     * Convert a int link id in a String link id
     *
     * @param id
     * @return
     * @throws LinkNotFoundException
     */
    public String getLinkId(int id) throws LinkNotFoundException;

    /**
     * Add a conversion for the linkId link
     *
     * @param linkId
     * @throws LinkAlreadyExistException
     */
    public void addLinkId(String linkId) throws LinkAlreadyExistException;

    /**
     * Remove the conversion for the link linkId
     *
     * @param linkId
     * @throws LinkNotFoundException
     */
    public void removeLinkId(String linkId) throws LinkNotFoundException;

    // LSP conversion
    /**
     * Get the max LSP int id
     * @return
     */
    public int getMaxLspId();

    /**
     * Convert a String LSP id in a int LSP id
     *
     * @param id
     * @return
     * @throws LspNotFoundException
     */
    public int getLspId(String id) throws LspNotFoundException;

    /**
     * Convert a int lsp id in a String lsp id
     *
     * @param id
     * @return
     * @throws LspNotFoundException
     */
    public String getLspId(int id) throws LspNotFoundException;

    /**
     * Add a conversion for the lspId LSP
     *
     * @param lspId
     * @throws LspAlreadyExistException
     */
    public void addLspId(String lspId) throws LspAlreadyExistException;

    /**
     * Remove the conversion for the lspId LSP
     *
     * @param lspId
     * @throws LspNotFoundException
     */
    public void removeLspId(String lspId) throws LspNotFoundException;

    /**
     * Rename an LSP without changing its int id
     * @param oldId
     * @param newId
     * @throws LspNotFoundException if the oldId not in the index
     * @throws LspAlreadyExistException If the newId already in the index
     */
    public void renameLspId(String oldId, String newId) throws LspNotFoundException, LspAlreadyExistException;

    /**
     * Rename a Node without changing its int id
     * @param oldId
     * @param newId
     * @throws NodeNotFoundException if the oldId not in the index
     * @throws NodeAlreadyExistException If the newId already in the index
     */
    public void renameNodeId(String oldId, String newId) throws NodeAlreadyExistException, NodeNotFoundException;

    /**
     * Rename a Link without changing its int id
     * @param oldId
     * @param newId
     * @throws LinkNotFoundException if the oldId not in the index
     * @throws LinkAlreadyExistException If the newId already in the index
     */
    public void renameLinkId(String oldId, String newId) throws LinkAlreadyExistException, LinkNotFoundException;
}
