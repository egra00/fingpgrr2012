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

import java.util.Calendar;
import java.util.List;
import java.net.URI;

/*
 * Changes:
 * --------
 *
 * - 29-Apr-2005: add the removeAllLsps method (JLE).
 * - 20-May-2005 : Add the getBWSharing method (SBA)
 * - 31-May-2005 : Add the getLspStartingAtIngress and getLSP(ingress,egress) methods (FSK)
 * - 07-Dec-2005: add get and set URI (GMO)
 * - 07-Dec-2005: correct javadoc (GMO)
 * - 23-Dec-2005: add getReverseLinks(Link link) and getReverseLinks(String linkId) (JLE).
 * - 08-Feb-2006: add getLinkFrom(String, String) & getLinkTo(String, String) (JLE).
 * - 08-Feb-2006: comments fixes (JLE).
 * - 06-Mar-2006: add getters and setters for author, title and date (JLE).
 * - 03-Apr-2006: init method now throw an exception if the domain fails to initialize (GMO).
 * - 11-Jul-2006: Add getBandwidthUnit method (GMO).
 * - 12-Jul-2006: Add setBandwidthUnit method (GMO).
 * - 22-Aug-2006: Add setDomainBWSharing method (GMO).
 * - 23-Oct-2006: Add useBandwidthSharing method and usePreemption method(GMO).
 * - 23-Oct-2006: Add diffserv methods : getPreemptionLevels(.), getMinPriority(.), getMinCTValue(), getMaxPLValue(.),
                  getMaxPreemptionLevel(.), getPriorities() (GMO)
 * - 27-Oct-2006: Add getLinkBetweenNodes(String, String, String, String) (JLE).
 * - 22-Nov-2006: Remove getBandwidthSharing(), setBandwidthSharing(.), add getBandwidthManagement() (GMO)
 * - 22-Nov-2006: add generateDetourLspId(.), addLsp(.) now throws more exceptions, add the possibility to use
                  implicit preemption or not. (GMO)
 * - 22-Nov-2006: add Diffserv methods : getPriorityHighPL(.), getLowerPLs(.), getLowerPLsAllCTs(.) (GMO)
 * - 04-Dec-2006: Add getReverseLink method (GMO)
 * - 16-Jan-2007: update Javadoc (GMO)
 * - 24-Jan-2007: add getAllPrimaryLsps() and getPrimaryLsps(ingress, egress) methods (GMO)
 * - 06-Apr-2007: add addPriorityLevel, removePriorityLevel, getDelayUnit, setClassType, setPreemptionLevel and setDelayUnit (JLE)
 * - 06-Apr-2007: fix javadoc comments (JLE)
 * - 06-Apr-2007: add getNodeByRid(.) (GMO)
 * - 18-Apr-2007: remove setClassType, setPreemptionLevel (GMO)
 * - 18-Apr-2007: change addPriorityLevel and removePriorityLevel specification (GMO)
 * - 11-May-2007: add renameLsp(.) method (GMO)
 * - 14-May-2007: add getActivatedLsps(). getLsps() and getPrimaryLsps() do not throw LspNotFound anymore (GMO)
 * - 06-Sep-2007: remove getActivatedLsps(.) method (GMO)
 * - 20-Sep-2007: add isSwitchingEnabled() and addBgpRouter() methods (GMO)
 * - 25-Sep-2007: remove isSwitchingEnabled(), add setSwitchingMethod(.) and getSwitchingMethod() (GMO)
 * - 29-Nov-2007: add generateBypassLspId(.) (GMO)
 * - 10-Jan-2008: rename setBandWidthUnit(.) setBandwidthUnit(.) (GMO)
 * - 26-Feb-2008: add class of service methods (GMO) 
 */

/**
 * Represent a network domain
 *
 * <p>Creation date: 12-Jan-2005 17:24:56
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public interface Domain {

    /**
     * Init all the index and give the Domain reference to Link, Node and Lsp.
     * Create the DomainConvertor.
     *
     * @param removeMultipleLink true if the multiple links must be removed (@see DomainValidator)
     * @throws InvalidDomainException If the domain is invalid (like bad lsps path).
     */
    void init(boolean removeMultipleLink, boolean useBwSharing) throws InvalidDomainException;

    /**
     * Get the domain convertor
     *
     * @return the domain convertor
     */
    public DomainConvertor getConvertor();

    /**
     * Get the observer
     *
     * @return the observer
     */
    public DomainChangeObserver getObserver();

    /**
     * Get the validator
     *
     * @return the validator
     */
    public DomainValidator getValidator();

    /**
     * Get the domain statistics
     *
     * @return the domain statistics
     */
    public DomainStatistics getDomainStatistics();

    /**
     * Returns the bandwidth management object associated with the domain
     * @return
     */
    public BandwidthManagement getBandwidthManagement();

    /**
     * returns true if the domain uses bandwidth management
     * @return
     */
    public boolean useBandwidthSharing();

    /**
     * Get the SPFCache
     *
     * @return the SPFCache
     */
    public SPFCache getSPFCache();

    /**
     * Get the URI from which the domain was loaded
     *
     * @return the URI of the domain
     */
    public URI getURI();

    /**
     * Set the URI from which the domain was loaded
     *
     * @param uri
     */
    public void setURI(URI uri);

    /**
     * Returns the bandwidth unit used in the domain.
     */
    public BandwidthUnit getBandwidthUnit();

    /**
     * Change the bandwidth unit used in the domain. It DOES NOT convert the
     * units. This function is intended to be used only on new Domain object to
     * change default value (mbps).
     */
    public void setBandwidthUnit(BandwidthUnit unit);

    /**
     * Returns the delay unit used in the domain.
     */
    public DelayUnit getDelayUnit();
    
    /**
     * Change the delay unit used in the domain. It DOES NOT convert the units.
     * This function is intended to be used only on new Domain object to change
     * default value (ms).
     */
    public void setDelayUnit(DelayUnit unit);

    /**
     * Set a switching method for the domain. The old switching method is stopped by calling {@link SwitchingMethod#stop()}
     *  and the new one is started ({@link SwitchingMethod#start()}).
     * @param sm
     */
    public void setSwitchingMethod(SwitchingMethod sm);

    /**
     * Returns the current active switching method
     * @return
     */
    public SwitchingMethod getSwitchingMethod();


    // INFORMATION
    /**
     * Get the Autonomous System ID
     *
     * @return ASID the AS ID
     */
    public int getASID();

    public void setASID(int ASID);

    /**
     * Get the description of a domain
     *
     * @return the description of a domain
     */
    public String getDescription();

    /**
     * Set the description of a domain
     *
     * @param description
     */
    public void setDescription(String description);

    /**
     * Get the name of the domain
     * @return
     */
    public String getName();

    /**
     * Set the name of the domain
     * @param name
     */
    public void setName(String name);
    
    /**
     * Sets the title of the domain.
     * @param title
     */
    public void setTitle(String title);
    
    /**
     * Returns the title of the domain.
     * @return The title of the domain.
     */
    public String getTitle();

    /**
     * Sets the date of the domain.
     * @param date
     */
    public void setDate(Calendar date);
    
    /**
     * Returns the date of the domain.
     * @return
     */
    public Calendar getDate();
    
    /**
     * Sets the author of the domain.
     * @param author
     */
    public void setAuthor(String author);
    
    /**
     * Returns the author of the domain.
     * @return The author of the domain.
     */
    public String getAuthor();
    
    // NODE
    /**
     * Get the node of the specified id
     *
     * @param id
     * @return the node
     */
    public Node getNode(String id) throws NodeNotFoundException;

    /**
     * Returns the node that as the given IP address as router id.
     * @param IP ip address of the node
     * @return the node whose rid is <code>IP</code>.
     * @throws NodeNotFoundException when no node exists in the domain with the given IP address.
     */
    public Node getNodeByRid(String IP) throws NodeNotFoundException;

    /**
     * Add a node to the domain
     *
     * @param node
     * @throws NodeAlreadyExistException
     */
    public void addNode(Node node) throws NodeAlreadyExistException;

    /**
     * Remove a node from the domain.
     *
     * WARNING : use this method carrefully. When removing a node, all the in and out links
     * will be remove. Prefer using the node.setNodeStatus(NODE.STATUS_DOWN) method.
     *
     * @param node
     * @throws NodeNotFoundException
     * @throws LinkNotFoundException
     */
    public void removeNode(Node node) throws NodeNotFoundException, LinkNotFoundException;

    /**
     * Return the number of nodes
     *
     * @return
     */
    public int getNbNodes();

    /**
     * Return a list of all nodes and an empty if there is no nodes
     * @return
     */
    public List<Node> getAllNodes();

    /**
     * Get the list of all up nodes.
     *
     * @return
     */
    public List<Node> getUpNodes();

    /**
     * Get the list of the UP links between srcNode and dstNode and a empty list if there is no links.
     *
     * @param srcNode
     * @param dstNode
     * @return the list of the links between srcNode and dstNode and a empty list if there is no links.
     * @throws NodeNotFoundException
     */
    public List<Link> getLinksBetweenNodes(Node srcNode, Node dstNode) throws NodeNotFoundException;

    /**
     * Get the list of the UP links between srcNode and dstNode and a empty list if there is no links.
     *
     * @param srcNodeId
     * @param dstNodeId
     * @return the list of the links between srcNode and dstNode and a empty list if there is no links.
     * @throws NodeNotFoundException
     */
    public List<Link> getLinksBetweenNodes(String srcNodeId, String dstNodeId) throws NodeNotFoundException;

    /**
     * Returns the link between srcNode and dstNode and using the specified interfaces.
     * @param srcNodeId The source node ID.
     * @param srcIfId The source interface ID.
     * @param dstNodeId The destination node ID.
     * @param dstIfId The destination interface ID.
     * @return The link between srcNode and dstNode and using the specified interfaces.
     */
    public Link getLinkBetweenNodes(String srcNodeId, String srcIfId, String dstNodeId, String dstIfId) throws LinkNotFoundException, NodeNotFoundException, NodeInterfaceNotFoundException;

    /**
     * Returns the reverse link for link <code>link</code>. It is the opposite link that connects the same
     * interfaces if interfaces are defined or the link between the destination and the source node if there is only one.
     * Otherwise, null is returned.
     *
     * @param link The link to consider.
     * @return The reverse link for link <code>link</code>.
     * @throws NodeNotFoundException
     */
    public Link getReverseLink(Link link) throws NodeNotFoundException;

    /**
     * Returns the list of reverse links for link <code>link</code>.
     * @param link The link to consider.
     * @return The list of reverse links for link <code>link</code>.
     * @throws NodeNotFoundException
     */
    public List<Link> getReverseLinks(Link link) throws NodeNotFoundException;
    
    /**
     * Returns the list of reverse links for link <code>linkId</code>.
     * @param linkId The ID of the link to consider.
     * @return The list of reverse links for link <code>linkId</code>.
     * @throws LinkNotFoundException
     * @throws NodeNotFoundException
     */
    public List<Link> getReverseLinks(String linkId) throws LinkNotFoundException, NodeNotFoundException;
    
    // LINK
    /**
     * Return the link with the specified id
     *
     * @param id
     * @return
     */
    public Link getLink(String id) throws LinkNotFoundException;

    /**
     * Returns the link connecting the given interface on the given source node.
     * @return The link connecting the given interface on the given source node.
     */
    public Link getLinkFrom(String nodeId, String nodeInterfaceId) throws NodeNotFoundException, NodeInterfaceNotFoundException, LinkNotFoundException;
    
    /**
     * Returns the link connecting the given interface on the given destination node.
     * @return The link connecting the given interface on the given destination node.
     */    
    public Link getLinkTo(String nodeId, String nodeInterfaceId) throws NodeNotFoundException, NodeInterfaceNotFoundException, LinkNotFoundException;
    
    /**
     * Return the number of links
     * @return
     */
    public int getNbLinks();

    /**
     * Add a link to the domain
     *
     * @param link
     * @throws LinkAlreadyExistException if a link with the same id already exist in the domain
     */
    public void addLink(Link link) throws LinkAlreadyExistException, NodeNotFoundException;

    /**
     * Remove a link from the domain.
     *
     * WARNING : use this method carrefully. This method removes all the LSP crossing the removed link. You
     * can change the status of a link if you want to simulate a failure of this link.
     *
     * @param link
     * @throws LinkNotFoundException if the link is not found in the domain
     */
    public void removeLink(Link link) throws LinkNotFoundException;

    /**
     * Return a list of all links and a empty list if there is no links.
     * @return
     */
    public List<Link> getAllLinks();

    /**
     * Get all the up links
     *
     * @return
     */
    public List<Link> getUpLinks();

    // LSP
    /**
     * Generate a unique LSP id.
     *
     * @return
     */
    public String generateLspId();

    /**
     *  Generate an id for a new detour LSP, based on the detour lsp parameters
     *  A conversion for this new id is automatically added to the convertor.
     *
     * @param protectedId id of the protected lsp
     * @param methodType
     * @param protectionType
     * @return a new id
     */
    public String generateDetourLspId(String protectedId, int methodType, int protectionType);

    /**
     * Generate an unique id for a new bypass LSP.
     * @param protectedResource The name of the resource that the bypass will protect
     * @return a new id
     */
    public String generateBypassLspId(String protectedResource);

    /**
     * Return the Lsp with the specified id
     *
     * @param id
     * @return the Lsp with the specified id
     */
    public Lsp getLsp(String id) throws LspNotFoundException;

    /**
     * Get the list of LSPs between ingress node and egress node
     *
     * @param ingress the ingress node
     * @param egress the egress node
     * @return the list of LSPs between ingress node and egress node
     */
    public List<Lsp> getLsps(Node ingress, Node egress);

    /**
     * Get the list of primary LSPs between ingress node and egress node
     * @param ingress the ingress node
     * @param egress the egress node
     * @return the list of primary LSPs between ingress node and egress node
     */
    public List<Lsp> getPrimaryLsps(Node ingress, Node egress);

    /**
     * Add a Lsp in the domain without using implicit preemption
     * 
     * @param lsp
     * @throws LinkCapacityExceededException
     * @throws LspAlreadyExistException
     * @throws LspNotFoundException If it is a backup LSP and the primary LSP cannot be found.
     */
    public void addLsp(Lsp lsp) throws LinkCapacityExceededException, LspAlreadyExistException, LspNotFoundException, DiffServConfigurationException;

    /**
     * Add a Lsp in the domain
     *
     * @param lsp
     * @param preemption tells if implicit preemption should be used
     * @throws LinkCapacityExceededException
     * @throws LspAlreadyExistException
     * @throws LspNotFoundException If it is a backup LSP and the primary LSP cannot be found.
     */
    public void addLsp(Lsp lsp, boolean preemption) throws LinkCapacityExceededException, LspAlreadyExistException, LspNotFoundException, DiffServConfigurationException;


    /**
     * Remove a Lsp
     *
     * @param lsp
     * @throws LspNotFoundException
     */
    public void removeLsp(Lsp lsp) throws LspNotFoundException;

    /**
     * Remove a Lsp
     *
     * @param lsp the LSP to remove
     * @param isReroute specify if the LSP is a rerouted LSP. If true, a LSP reroute notification
     * will be send to the NetController. Otherwise, the LSP will be simply removed without notification.
     * @throws LspNotFoundException
     */
    public void removeLsp(Lsp lsp, boolean isReroute) throws LspNotFoundException;

    /**
     * Removes all the LSPs from the <code>Domain</code>.
     */
    public void removeAllLsps();

    /**
     * Get the number of LSPs
     *
     * @return
     */
    public int getNbLsps();

    /**
     * Get a list of all LSPs
     *
     * @return
     */
    public List<Lsp> getAllLsps();

    /**
     * Get a list of all primary LSPs
     *
     * @return
     */
    public List<Lsp> getAllPrimaryLsps();

    /**
     * Get all the backups Lsp of a primary LSP
     * @param primaryLsp
     * @deprecated use {@link Lsp#getBackups()}
     * @return
     */
    public List<Lsp> getBackupsOfPrimary(Lsp primaryLsp);

    /**
     * Get all the LSPs that use the link
     *
     * @param link
     * @return
     */
    public List<Lsp> getLspsOnLink(Link link);


    /**
     * Get all the LSPs that begins at the specified ingress node
     *
     * @param ingress the ingress node
     * @return the list of LSPs begins at ingress node
     */
    public List<Lsp> getLspStartingAtIngress(Node ingress);

    /**
     * Rename an lsp of the domain.
     * @param oldId
     * @param newId
     * @throws LspNotFoundException If the lsp cannot be found in the domain
     * @throws LspAlreadyExistException If the lsp already exists
     */
    public void renameLsp(String oldId, String newId) throws LspNotFoundException, LspAlreadyExistException;

    /**
     * Rename a node of the domain.
     * @param oldId
     * @param newId
     * @throws NodeNotFoundException
     * @throws IdException
     * @throws NodeAlreadyExistException
     */
    public void renameNode(String oldId, String newId) throws NodeNotFoundException, IdException, NodeAlreadyExistException;

    public void renameLink(String oldId, String newId) throws LinkNotFoundException, LinkAlreadyExistException, IdException;

    // DiffServ
    /**
     * Returns the priority corresponding to a preemption level and a class type
     * @param preemption
     * @param classtype
     * @return
     */
    public int getPriority(int preemption, int classtype);

    /**
     * Return true if the domain can use preemption. Preemption cannot be used if bandwidth sharing is enabled.
     * @return
     */
    public boolean usePreemption();

    /**
     * Returns the preemption level corresponding to a priority
     * @param priority
     * @return
     */
    public int getPreemptionLevel(int priority);

    /**
     * Returns the preemption levels corresponding to a given class type
     * @param classType
     * @return
     */
    public List<Integer> getPreemptionLevels(int classType);

    /**
     * Returns the class type corresponding to a priority
     * @param priority
     * @return
     */
    public int getClassType(int priority);

    /**
     * Tells if the couple preemptionLevel/classType corresponds to an existing priority level
     * @param preemptionLevel
     * @param classType
     * @return
     */
    public boolean isExistingPriority(int preemptionLevel, int classType);

    /**
     * Tells if the priority corresponds to an existing priority level
     * @param priority
     * @return
     */
    public boolean isExistingPriority(int priority);

    /**
     * Gets the default minimum Priority (ie for lowest priority Class Type and lowest corresponding priority level);
     * @return
     */
    public int getMinPriority();

    /**
     * Gets the minimum priority for the given Class Type (ie lowest priority level)
     * @param CT
     * @return
     */
    public int getMinPriority(int CT);

    /**
     * Obtain the maximum numerical value of CT
     * @return
     */
    public int getMaxCTvalue();

    /**
     * Obtain the minimum numerical value of CT (highest priority)
     * @return
     */
    public int getMinCTValue();

    /**
     * Obtain the maximum numerical value of preemption level for a given Class Type
     * @param ctValue
     * @return
     */
    public int getMaxPLValue(int ctValue);

    /**
     * Obtain the maximum numerical value of preemption level
     * @return
     */
    public int getMaxPLvalue();

    /**
     * Obtain the minimum numerical value of preemption level
     * @return
     */
    public int getMinPLValue();

    /**
     * Gets the maximum Preemption Level (lowest numerical value) for a given class type.
     * @return
     */
    public int getMaxPreemptionLevel(int CT);

    /**
     * Returns the priority corresponding to the same CT but a just higher preemption level (lower numerical value)
     * @param priority
     * @return
     */
    public int getPriorityHighPL(int priority);

    /**
     * Gets the number of different class types
     * @return
     */
    public int getNbCT();

    /**
     * Returns an array of all available CTs
     * @return
     */
    public int[] getAllCTId();

    /**
     * Return a list of all priorities identifiers
     * @return
     */
    public List<Integer> getPriorities();


    /**
     * Returns all the priorities corresponding to same CT as priority but lower preemption level (higher numerical value)
     * @param priority
     * @return
     */
    public List<Integer> getLowerPLs(int priority);

    /**
     * Returns all the priorities at lower preemption level (higher numerical value)
     * @param priority
     * @return
     */
    public List<Integer> getLowerPLsAllCTs(int priority);

    /**
     * Returns all priorities corresponding to a same CT
     * @param CT
     * @return
     */
    public List<Integer> getPrioritySameCT(int CT);

    /**
     * Adds a priority level.
     * @param priority The ID of the newly created priority level.
     * @param classType The class type of the newly created priority level.
     * @param preemptionLevel The preemption level of the newly created priority
     * level.
     * @throws DiffServConfigurationException If the priority level already
     * exists (with same id or with same class type and preemption level) or if one of <code>priority</code>,
     * <code>classType</code>, <code>preemptionLevel</code> is not >= 0 and < 8.
     */
    public void addPriorityLevel(int priority, int classType, int preemptionLevel) throws DiffServConfigurationException;

    /**
     * Removes the given priority level.
     * @param priority The ID of the priority level to remove.
     * @return <code>true</code> if the priority level was successfully removed
     * and <code>false</code> if the priority level did not exist.
     * @throws DiffServConfigurationException if the priority level is in used by some lsps.
     */
    public boolean removePriorityLevel(int priority) throws DiffServConfigurationException;

    // Classes of service

    /**
     * Defines a new class of service identified by <code>name</code>
     * @param name
     * @throws ClassOfServiceAlreadyExistException If the class is already defined
     */
    public void addClassOfService(String name) throws ClassOfServiceAlreadyExistException;

    /**
     * Remove a class of service from the domain. Must also remove all references to the class (in the lsps,...)
     * @param name
     * @throws ClassOfServiceNotFoundException If the class was not defined
     */
    public void removeClassOfService(String name) throws ClassOfServiceNotFoundException;

    /**
     * Returns a list of all defined classes of service.
     * @return
     */
    public List<String> getClassesOfService();

    /**
     * Returns a list of defined sub-classes for the specified class.
     * @param classname
     * @return
     */
    public List<String> getSubClasses(String classname) throws ClassOfServiceNotFoundException;

    /**
     * Tells if the class of service is defined.
     * @param name
     * @return
     */
    public boolean isExistingClassOfService(String name);


    /////////////////////////////////////////////////////////////////
    //
    // BGP section
    //
    /////////////////////////////////////////////////////////////////

    public void addBgpRouter(BgpRouter router) throws RouterAlreadyExistException;

    /**
     * Returns the BGP router with the specified Id.
     *
     * @param id the Id of the requested BGP router.
     * @return the BGP router associated with the given Id, if it
     *         exists. Otherwise, returns null.
     */
    public BgpRouter getBgpRouter(String id);

    /**
     * Returns the list of all BGP routers.
     *
     * @return the list of all BGP routers available in the domain.
     */
    public List<BgpRouter> getAllBgpRouters();

}
