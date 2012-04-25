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
import be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Lsp;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.NodeInterface;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.*;
import be.ac.ulg.montefiore.run.totem.repository.model.LSPDetourRoutingParameter;
import be.ac.ulg.montefiore.run.totem.util.IdGenerator;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.net.URI;
import java.util.*;

/*
 * Changes:
 * --------
 * - 29-Apr-2005: fix javadoc class comments & impl of removeAllLsps (JL).
 * - 31-May-2005 : Add the getLspStartingAtIngress and getLSP(ingress,egress) methods (FSK)
 * - 19-Oct-2005: fix implementation of addLsp (GMO)
 * - 25-Oct-2005: fix addLsp (GMO)
 * - 08-Nov-2005: add notification to domain observer (notifyAddLsp, notifyAddNode, notifyRemoveNode) (GMO)
 * - 07-Dec-2005: add get and set URI (GMO)
 * - 07-Dec-2005: correct javadoc (GMO)
 * - 23-Dec-2005: add getReverseLinks(Link link) and getReverseLinks(String linkId) (JLE).
 * - 08-Feb-2006: add getLinkFrom(String, String) & getLinkTo(String, String) (JLE).
 * - 08-Feb-2006: comments fixes (JLE).
 * - 06-Mar-2006: add getters and setters for author, title and date (JLE).
 * - 03-Apr-2006: init method now throw an exception if the domain fails to initialize (GMO).
 * - 11-Jul-2006: add getBandwidthUnit method and improve init() method to retrieve unit used for bandwidth (GMO).
 * - 12-Jul-2006: Add setBandwidthUnit method (GMO).
 * - 22-Aug-2006: Add setDomainBWSharing method (GMO).
 * - 23-Oct-2006: Add some diffserv methods, in addLsp and removeLsp, make use of bwSharing only for class type of
 *                minimum value (GMO)
 * - 26-Oct-2006: Add check for multiple link, node, router ids (GMO).
 * - 27-Oct-2006: Add getLinkBetweenNodes(String, String, String, String) (JLE).
 * - 22-Nov-2006: Remove getBandwidthSharing(), setBandwidthSharing(.), add getBandwidthManagement() (GMO)
 * - 22-Nov-2006: add generateDetourLspId(.), addLsp(.) now throws more exceptions, add the possibility to use
                  implicit preemption or not. (GMO)
 * - 22-Nov-2006: add Diffserv methods : getLowerPLsAllCTs(.) (GMO)
 * - 22-Nov-2006: When a lsp is removed, all backups are removed (GMO)
 * - 04-Dec-2006: Add getReverseLink method (GMO)
 * - 16-Jan-2006: Initialise nodes with incoming and outgoing links (GMO)
 * - 24-Jan-2007: add getAllPrimaryLsps() and getPrimaryLsps(ingress, egress) methods (GMO)
 * - 06-Apr-2007: add addPriorityLevel, removePriorityLevel, setClassType, setPreemptionLevel, getDelayUnit and setDelayUnit (JLE)
 * - 06-Apr-2007: fix javadoc comments (JLE)
 * - 06-Apr-2007: add getNodeByRid(.) (GMO)
 * - 18-Apr-2007: remove setClassType, setPreemptionLevel (GMO)
 * - 18-Apr-2007: rewrite addPriorityLevel and removePriorityLevel methods (GMO)
 * - 27-Apr-2007: bugfix: replace Node.getInLink() by Node.getAllInLink() in removeNode(.) method(GMO)
 * - 10-May-2007: bugfix in removeLsp(.) (GMO)
 * - 10-May-2007: add renameLsp(.) (GMO)
 * - 14-May-2007: add getActivatedLsps(). getLsps() and getPrimaryLsps() do not throw LspNotFound anymore (GMO)
 * - 01-Aug-2007: bugfix: build the list of backup lsps on init (GMO)
 * - 20-Sep-2007: add isSwitchingEnabled() and addBgpRouter() methods (GMO)
 * - 25-Sep-2007: use SwitchingMethod instead of RerouteMethod (GMO)
 * - 29-Nov-2007: add generateBypassLspId(.) (GMO)
 * - 17-Dec-2007: generateBypassLspId(.) adds the id to the convertor (GMO)
 * - 10-Jan-2008: rename setBandWidthUnit(.) setBandwidthUnit(.) (GMO)
 * - 10-Jan-2008: remove mpls section when no more lsps is established (GMO)
 * - 21-Feb-2008: prevent nullPointerException in removeBgpRouters() (GMO)
 * - 26-Feb-2008: add class of service methods (GMO)  
 */

/**
 * Implementation of a network domain.
 *
 * <p>Creation date : 19-Jan-2005 14:28:33
 *
 * @author Fabian Skivee (skivee@run.montefiore.ulg.ac.be)
 * @author Bruno Quoitin (bqu@info.ucl.ac.be)
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 */
public class DomainImpl extends be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.DomainImpl implements Domain {
    
    private static Logger logger = Logger.getLogger(DomainImpl.class);
    
    private DomainConvertor convertor;
    private DomainChangeObserver observer;
    private DomainValidator validator;
    private DomainStatistics statistics;
    private BandwidthManagement bwManagement;
    private SwitchingMethod switchingMethod = null;
    private SPFCache spfCache;
    private URI uri;
    private BandwidthUnit bandwidthUnit = null;
    private DelayUnit delayUnit = null;

    private final int priorityIndex = 0;
    private final int preemptionIndex = 1;
    private final int classtypeIndex = 2;
    
    
    private HashMap<String,Node> nodeIndex = null;
    private HashMap<String,Link> linkIndex = null;
    private HashMap<String,LinkIgp> igpLinkIndex = null;
    private HashMap<String,BgpRouter> bgpRouterIndex= null;
    private HashMap<String,Lsp> lspIndex = null;
    private int[][] priorityArray = null;
    
    /**
     * Simple constructor. Needed by JAXB
     */
    public DomainImpl() {}
    
    /**
     * Create a Domain with a specified ASID
     *
     * @param ASID
     */
    public DomainImpl(int ASID) {
        this.setASID(ASID);
        try {
            init(false, false);
        } catch (InvalidDomainException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get the domain convertor
     * @return
     */
    public DomainConvertor getConvertor() {
        return convertor;
    }

    /**
     * Get the observer
     *
     * @return
     */
    public DomainChangeObserver getObserver() {
        return observer;
    }
    
    /**
     * Get the validator
     *
     * @return
     */
    public DomainValidator getValidator() {
        return validator;
    }
    
    /**
     * Get the domain statistics
     *
     * @return
     */
    public DomainStatistics getDomainStatistics() {
        return statistics;
    }

    /**
     * Returns the bandwidth management object associated with the domain
     * @return
     */
    public BandwidthManagement getBandwidthManagement() {
        return bwManagement;
    }

    /**
     * returns true if the domain uses bandwidth management
     * @return
     */
    public boolean useBandwidthSharing() {
        return bwManagement instanceof BandwidthSharingBandwidthManagement;
    }


    /**
     * Get the SPFCache
     *
     * @return the SPFCache
     */
    public SPFCache getSPFCache() {
        return spfCache;
    }

    /**
     * Get the URI from which the domain was loaded
     *
     * @return the URI of the domain
     */
    public URI getURI() {
        return uri;
    }

    /**
     * Set the URI from which the domain was loaded
     *
     * @param uri
     */
    public void setURI(URI uri) {
        this.uri = uri.normalize();
    }

    /**
     * Returns the bandwidth unit used in the domain.
     */
    public BandwidthUnit getBandwidthUnit() {
        return bandwidthUnit;
    }

    /**
     * Change the bandwidth unit used in the domain. It DOES NOT convert the
     * units. This function is intended to be used only on new Domain object to
     * change default value (mbps).
     */
    public void setBandwidthUnit(BandwidthUnit unit) {
        if (unit == null || unit == BandwidthUnit.DEFAULT_UNIT) {
            throw new IllegalArgumentException("Bad bandwidth unit");
        }
        bandwidthUnit = unit;
        List units = this.getInfo().getUnits().getUnit();
        for (Iterator i = units.iterator(); i.hasNext();) {
            UnitType u = (UnitType) i.next();
            if (u.getType().equals(UnitsType.BANDWIDTH)) {
                u.setValue(BandwidthUnits.fromString(bandwidthUnit.toString().toLowerCase()));
                return;
            }
        }
        //this should never happen
        throw new IllegalStateException("Units not found for bandwidth");
    }

    /**
     * Returns the delay unit used in the domain.
     */
    public DelayUnit getDelayUnit() {
        return delayUnit;
    }
    
    /**
     * Change the delay unit used in the domain. It DOES NOT convert the
     * units. This function is intended to be used only on new Domain object to
     * change default value (ms).
     */
    public void setDelayUnit(DelayUnit unit) {
        if (unit == null || unit == DelayUnit.DEFAULT_UNIT) {
            throw new IllegalArgumentException("Bad delay unit");
        }
        delayUnit = unit;
        List units = this.getInfo().getUnits().getUnit();
        for (Iterator i = units.iterator(); i.hasNext();) {
            UnitType u = (UnitType) i.next();
            if (u.getType().equals(UnitsType.DELAY)) {
                u.setValue(DelayUnits.fromString(delayUnit.toString().toLowerCase()));
                return;
            }
        }
        //this should never happen
        throw new IllegalStateException("Unit not found for delay");
    }

    public void setSwitchingMethod(SwitchingMethod sm) {
        if (switchingMethod != null) {
            switchingMethod.stop();
        }
        this.switchingMethod = sm;
        sm.start();
    }

    public SwitchingMethod getSwitchingMethod() {
        return switchingMethod;
    }

    /**
     * Get the description of a domain
     * 
     * @return
     */
    public String getDescription() {
        if (this.getInfo() != null)
            return this.getInfo().getDescription();
        return null;
    }
    
    /**
     * Set the description of a domain
     *
     * @param description
     */
    public void setDescription(String description) {
        if(this.getInfo() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                Information info = factory.createInformation();
                info.setDescription(description);
                this.setInfo(info);
            }
            catch(JAXBException e) {
                logger.error("JAXBException in setDescription. Message: "+e.getMessage());
            }
        }
        else {
            this.getInfo().setDescription(description);
        }
    }
    
    public String getTitle() {
        if(this.getInfo() != null) {
            return this.getInfo().getTitle();
        }
        return null;
    }
    
    public void setTitle(String title) {
        if(this.getInfo() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                Information info = factory.createInformation();
                info.setTitle(title);
                this.setInfo(info);
            }
            catch(JAXBException e) {
                logger.error("JAXBException in setDescription. Message: "+e.getMessage());
            }
        }
        else {
            this.getInfo().setTitle(title);
        }        
    }
    
    public Calendar getDate() {
        if(this.getInfo() != null) {
            return this.getInfo().getDate();
        }
        return null;
    }
    
    public void setDate(Calendar date) {
        if(this.getInfo() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                Information info = factory.createInformation();
                info.setDate(date);
                this.setInfo(info);
            }
            catch(JAXBException e) {
                logger.error("JAXBException in setDescription. Message: "+e.getMessage());
            }
        }
        else {
            this.getInfo().setDate(date);
        }          
    }
    
    public String getAuthor() {
        if(this.getInfo() != null) {
            return this.getInfo().getAuthor();
        }
        return null;
    }
    
    public void setAuthor(String author) {
        if(this.getInfo() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                Information info = factory.createInformation();
                info.setAuthor(author);
                this.setInfo(info);
            }
            catch(JAXBException e) {
                logger.error("JAXBException in setDescription. Message: "+e.getMessage());
            }
        }
        else {
            this.getInfo().setAuthor(author);
        }
    }
    
    /**
     * Init all the index and give the Domain reference to Link, Node and Lsp.
     * Create the DomainConvertor.
     *
     * @param removeMultipleLink true if the multiple links must be removed (@see DomainValidator)
     * @throws InvalidDomainException If the domain is not valid (ex: if the one the LSPs has bad path)
     */
    public void init(boolean removeMultipleLink, boolean useBwSharing) throws InvalidDomainException {
        // Create the validator
        validator = new DomainValidatorImpl(this);
        validator.forceIGPInfo();
        
        logger.debug("Init domain");

        //init units list
        initUnits();

        // Init all the domain with default DiffServ constraints
        initDiffServ();
        
        // init node index hashmap
        nodeIndex = new HashMap<String,Node>(getNbNodes());
        if (getNbNodes() != 0) {
            List<Node> nodeList = getAllNodes();
            for (int i = 0; i < nodeList.size(); i++) {
                Node node = (Node) nodeList.get(i);
                if (nodeIndex.put(node.getId(),node) != null) {
                    throw new InvalidDomainException("Node id collision: " + node.getId());
                }
            }
        }
        // init link index hashmap
        linkIndex = new HashMap<String,Link>(getNbLinks());
        //if (getNbLinks() != 0) {
        List<Link> linkList = getAllLinks();
        for (int i = 0; i < linkList.size(); i++) {
            Link link = (Link) linkList.get(i);
            if (linkIndex.put(link.getId(),link) != null) {
                throw new InvalidDomainException("Link id collision: " + link.getId());
            }
        }
        //}
        // init igpLink index hashmap
        if ((this.getIgp() != null) && (this.getIgp().getLinks() !=null) && (this.getIgp().getLinks().getLink()!=null)) {
            igpLinkIndex = new HashMap<String,LinkIgp>(this.getIgp().getLinks().getLink().size());
            List igpLinkList = this.getIgp().getLinks().getLink();
            for (int i = 0; i < igpLinkList.size(); i++) {
                LinkIgp link = (LinkIgp) igpLinkList.get(i);
                if (igpLinkIndex.put(link.getId(),link) != null) {
                    throw new InvalidDomainException("IGP Link id collision: " + link.getId());
                }
            }
        } else {
            igpLinkIndex = new HashMap<String,LinkIgp>();
        }
        
        // init Bgp index hashmap
        initBgpRouters();

        // init lsp index hashmap
        lspIndex = new HashMap<String,Lsp>(getNbLsps());
        if (getNbLsps() != 0) {
            List<Lsp> lspList = getAllLsps();
            for (int i = 0; i < lspList.size(); i++) {
                Lsp lsp = (Lsp) lspList.get(i);
                if (lspIndex.put(lsp.getId(),lsp) != null) {
                    throw new InvalidDomainException("Lsp id collision: " + lsp.getId());
                }
            }
        }
        
        // init priority
        initPriority();
        
        // Add a reference to the domain in each node and in each interface
        List<Node> nodes = getAllNodes();
        for (int i = 0; i < nodes.size(); i++) {
            NodeImpl node = (NodeImpl) nodes.get(i);
            node.init(this);
            List<NodeInterface> interfs = node.getNodeInterfaces();
            for(NodeInterface nodeInterface : interfs) {
            	NodeInterfaceImpl interf = (NodeInterfaceImpl) nodeInterface;
            	interf.setDomain(this);
            	interf.setNode(node);
            }
        }

        // Add a reference to the domain in each link, add a reference to the link in the connected nodes
        List<Link> links = getAllLinks();
        for (int i = 0; i < links.size(); i++) {
            LinkImpl link = (LinkImpl) links.get(i);
            try {
                link.init(this);
            } catch (DiffServConfigurationException e) {
                throw new InvalidDomainException(e.getClass().getSimpleName());
            }
            try {
                ((NodeImpl)link.getSrcNode()).addOutLink(link);
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
            try {
                ((NodeImpl)link.getDstNode()).addInLink(link);
            } catch (NodeNotFoundException e) {
                e.printStackTrace();
            }
        }


        // Add a reference to the domain in each Lsp
        if (getNbLsps() != 0) {
            List<Lsp> lspList = getAllLsps();
            for (int i = 0; i < lspList.size(); i++) {
                LspImpl lsp = (LspImpl) lspList.get(i);
                try {
                    lsp.init(this);
                    if (lsp.isDetourLsp()) {
                        // add the lsp to the list of backups
                        lsp.getProtectedLsp().addBackupLsp(lsp);
                    }
                } catch (InvalidPathException e) {
                    throw new InvalidDomainException("Bad LSP path for lspid: " + lsp.getId());
                } catch (DiffServConfigurationException e) {
                    throw new InvalidDomainException("DiffServ exception for lsp \"" + lsp.getId() + "\": " + e.getMessage());
                } catch (LspNotFoundException e) {
                    throw new InvalidDomainException("Could not find primary lsp for lsp \"" + lsp.getId() + "\"");
                }
            }
        }

        // Create the Observer
        observer = new DomainChangeObserverImpl(this);

        if (removeMultipleLink) {
            validator.forceNoMultiGraph();
        }
        
        // Create the convertor
        convertor = new DomainConvertorImpl(this);
        
        // Create the statistics
        statistics = new DomainStatisticsImpl(this);
        
        if (useBwSharing) {
            try {
                bwManagement = new BandwidthSharingBandwidthManagement(this);
                bwManagement.init();
            } catch (LinkCapacityExceededException e) {
                logger.error("Bandwidth Sharing could not be started.");
                throw new InvalidDomainException("Bandwidth Sharing could not be started: LinkCapacityExceededException");
            } catch (DiffServConfigurationException e) {
                logger.error("Bandwidth Sharing could not be started: " + e.getMessage() +".");
                throw new InvalidDomainException("Bandwidth Sharing could not be started: DiffServConfigurationException");
            }
        }
        else {
            try {
                bwManagement = new DiffServBandwidthManagement(this);
                bwManagement.init();
            } catch (LinkCapacityExceededException e) {
                logger.error("Diffserv Bandwidth Managemenent could not be started: LinkCapacityExceededException " + e.getMessage());
                throw new InvalidDomainException("Diffserv Bandwidth Managemenent could not be started: LinkCapacityExceededException");
            }
        }

        spfCache = new SPFCacheImpl(this);
        observer.addListener(spfCache.getListener());
    }
    
    /**
     * Init DiffServ info field with default values (i.e. priority 0, CT0, PL0)
     * only if no priorities are already defined
     */
    private void initDiffServ(){
        ObjectFactory factory = new ObjectFactory();
        
        try{
            if (getInfo() == null){
                this.setInfo(factory.createInformation());
            }
            if (getInfo().getDiffServ() == null){
                getInfo().setDiffServ(factory.createInformationDiffServType());
            }
            List priorityList = getInfo().getDiffServ().getPriority();
            
            if (priorityList.size()==0){
                Information.DiffServType.PriorityType priority0 = factory.createInformationDiffServTypePriorityType();
                priority0.setId(0);
                priority0.setPreemption(0);
                priority0.setCt(0);
                priorityList.add(priority0);
            }
            
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Inits units info field whith default values (i.e. bandwidth=MBPS, delay=ms)
     * only if it is not defined. Also sets bandwidthUnit field to appropriate value.
     */
    private void initUnits() {
        ObjectFactory factory = new ObjectFactory();

        try {
            if (getInfo() == null) {
                this.setInfo(factory.createInformation());
            }
            if (getInfo().getUnits() == null) {
                getInfo().setUnits(factory.createInformationUnitsType());
            }
            List units = this.getInfo().getUnits().getUnit();
            boolean foundBw = false, foundDelay = false;
            for (Iterator i = units.iterator(); i.hasNext();) {
                UnitType u = (UnitType) i.next();
                if (u.getType().equals(UnitsType.BANDWIDTH)) {
                    bandwidthUnit = BandwidthUnit.valueOf(u.getValue().toString().toUpperCase());
                    foundBw = true;
                } else if (u.getType().equals(UnitsType.DELAY)) {
                    foundDelay = true;
                    delayUnit = DelayUnit.valueOf(u.getValue().toString().toUpperCase());
                }
            }

            if (!foundBw) {
                logger.info("Bandwidth units not found, using MBPS.");
                UnitType ut = factory.createUnitType();
                ut.setType(UnitsType.BANDWIDTH);
                ut.setValue(BandwidthUnits.fromString(BandwidthUnit.MBPS.toString().toLowerCase()));
                units.add(ut);
                bandwidthUnit = BandwidthUnit.MBPS;
            }

            if (!foundDelay) {
                logger.info("Delay units not found, using milliseconds (ms).");
                UnitType ut = factory.createUnitType();
                ut.setType(UnitsType.DELAY);
                ut.setValue(DelayUnits.fromString(DelayUnit.MS.toString().toLowerCase()));
                units.add(ut);
                delayUnit = DelayUnit.MS;
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    private void initPriority() {
        priorityArray = new int[getNbPriority()][3];
        if (getNbPriority() != 0){
            List<Information.DiffServType.PriorityType> priorityList = getAllPriorities();
            for (int i = 0; i < priorityList.size(); i++){
                Information.DiffServType.PriorityType priority = (Information.DiffServType.PriorityType)priorityList.get(i);
                priorityArray[i][priorityIndex] = priority.getId();
                priorityArray[i][preemptionIndex] = priority.getPreemption();
                priorityArray[i][classtypeIndex] = priority.getCt();
            }
        }
    }

    private void initBgpRouters() throws InvalidDomainException {
        if ((this.getBgp() != null)) {
            bgpRouterIndex= new HashMap<String,BgpRouter>();
            List<BgpRouter> bgpRoutersList= getAllBgpRouters();
            for (int i= 0; i < bgpRoutersList.size(); i++) {
                BgpRouter router= bgpRoutersList.get(i);
                if (bgpRouterIndex.put(router.getId(), router) != null) {
                    throw new InvalidDomainException("Router id collision: " + router.getId());
                }
            }
        }
    }

    /*********************************************************************************************
     * Methods dealing with nodes
     *********************************************************************************************/
    /**
     * Get the node of the specified id
     *
     * @param id
     * @return the node
     */
    public Node getNode(String id) throws NodeNotFoundException {
        if (nodeIndex == null) {
            NotInitialisedException e = new NotInitialisedException("nodeIndex not initialised - call domain.init()");
            e.printStackTrace();
        }
        if (nodeIndex.get(id) == null)
            throw new NodeNotFoundException(new StringBuffer().append("Node ").append(id).append(" not in the topology").toString());
        return nodeIndex.get(id);
    }

    /**
     * Returns the node that as the given IP address as router id.
     * @param IP ip address of the node
     * @return the node whose rid is <code>IP</code>.
     * @throws NodeNotFoundException when no node exists in the domain with the given IP address.
     */
    public Node getNodeByRid(String IP) throws NodeNotFoundException {
        for (Node n : getAllNodes()) {
            if (IP.equals(n.getRid())) {
                return n;
            }
        }
        throw new NodeNotFoundException("No node were found with IP " + IP);
    }

    /**
     * Add a node to the domain
     *
     * @param node
     * @throws NodeAlreadyExistException
     */
    public void addNode(Node node) throws NodeAlreadyExistException {
        // Check that the node not already exist in the domain
        if (nodeIndex.get(node.getId()) != null) {
            throw new NodeAlreadyExistException(new StringBuffer().append("Node ").append(node.getId()).append(" already exists in the domain").toString());
        }
        // Check that Topology section is present
        if (this.getTopology() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                this.setTopology(factory.createTopology());
                this.getTopology().setNodes(factory.createTopologyNodesType());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        } else if (this.getTopology().getNodes() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                this.getTopology().setNodes(factory.createTopologyNodesType());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        
        // Add the reference to the topology
        this.getTopology().getNodes().getNode().add(node);
        // Add the node ID conversion to the convertor
        this.convertor.addNodeId(node.getId());
        // Add the node in the index
        nodeIndex.put(node.getId(),node);
        // Notify add node
        observer.notifyAddNode(node);
    }
    
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
    public void removeNode(Node node) throws NodeNotFoundException, LinkNotFoundException {
        if (nodeIndex.get(node.getId()) == null)
            throw new NodeNotFoundException("Node " + node.getId() + " not found");
        // Remove in links
        List<Link> inLinks = node.getAllInLink();
        for (int i = 0; i < inLinks.size(); i++) {
            this.removeLink(inLinks.get(i));
        }
        // Remove out links
        List<Link> outLinks = node.getAllOutLink();
        for (int i = 0; i < outLinks.size(); i++) {
            this.removeLink(outLinks.get(i));
        }
        // Remove the node reference
        this.getTopology().getNodes().getNode().remove(node);
        // notify node remove
        observer.notifyRemoveNode(node);
        // Remove the node ID conversion from the convertor
        this.convertor.removeNodeId(node.getId());
        // Remove the node of the index
        nodeIndex.remove(node.getId());
    }
    
    /**
     * Return the number of nodes
     *
     * @return
     */
    public int getNbNodes() {
        if ((this.getTopology() != null) && (this.getTopology().getNodes() !=null)
                && (this.getTopology().getNodes().getNode() !=null))
            return this.getTopology().getNodes().getNode().size();
        return 0;
    }
    
    /**
     * Return a list of all nodes and an empty if there is no nodes
     * @return
     */
    public List<Node> getAllNodes() {
        if ((this.getTopology() != null) && (this.getTopology().getNodes() !=null)
                && (this.getTopology().getNodes().getNode() !=null))
            return this.getTopology().getNodes().getNode();
        return new ArrayList<Node>();
    }
    
    /**
     * Get the list of all up nodes.
     *
     * @return
     */
    public List<Node> getUpNodes() {
        List<Node> upNodeList = new ArrayList<Node>();
        if ((this.getTopology() != null) && (this.getTopology().getNodes() !=null)
                && (this.getTopology().getNodes().getNode() !=null)) {
            List<Node> nodeList = getAllNodes();
            for (int i = 0; i < nodeList.size(); i++) {
                if (nodeList.get(i).getNodeStatus() == Node.STATUS_UP)
                    upNodeList.add(nodeList.get(i));
            }
        }
        return upNodeList;
    }
    
    /**
     * Get the list of the UP links between srcNode and dstNode and a empty list if there is no links.
     *
     * @param srcNode
     * @param dstNode
     * @return the list of the links between srcNode and dstNode and a empty list if there is no links.
     * @throws NodeNotFoundException
     */
    public List<Link> getLinksBetweenNodes(Node srcNode, Node dstNode) throws NodeNotFoundException {
        List<Link> outLinks = srcNode.getOutLink();
        List<Link> links = new ArrayList<Link>();
        for (Link link : outLinks) {
            if (link.getDstNode().equals(dstNode))
                links.add(link);
            
        }
        return links;
    }

    /**
     * Get the list of the UP links between srcNode and dstNode and a empty list if there is no links.
     *
     * @param srcNodeId
     * @param dstNodeId
     * @return the list of the links between srcNode and dstNode and a empty list if there is no links.
     * @throws NodeNotFoundException
     */
    public List<Link> getLinksBetweenNodes(String srcNodeId, String dstNodeId) throws NodeNotFoundException {
        Node srcNode = this.getNode(srcNodeId);
        Node dstNode = this.getNode(dstNodeId);

        return getLinksBetweenNodes(srcNode, dstNode);
    }
    
    /**
     * Returns the link between srcNode and dstNode and using the specified interfaces.
     * @param srcNodeId The source node ID.
     * @param srcIfId The source interface ID.
     * @param dstNodeId The destination node ID.
     * @param dstIfId The destination interface ID.
     * @return The link between srcNode and dstNode and using the specified interfaces.
     */
    public Link getLinkBetweenNodes(String srcNodeId, String srcIfId, String dstNodeId, String dstIfId) throws LinkNotFoundException, NodeNotFoundException, NodeInterfaceNotFoundException {
        List<Link> links = getLinksBetweenNodes(srcNodeId, dstNodeId);
        for(Link link : links) {
            if(link.getSrcInterface().getId().equals(srcIfId) && link.getDstInterface().getId().equals(dstIfId)) {
                return link;
            }
        }
        throw new LinkNotFoundException("There is no link between "+srcNodeId+" and "+dstNodeId+", and using "+srcIfId+" and "+dstIfId);
    }
    
    /**
     * Returns the reverse link for link <code>link</code>. It is the opposite link that connects the same
     * interfaces if interfaces are defined or the link between the destination and the source node if there is only one.
     * Otherwise, null is returned.
     *
     * @param link The link to consider.
     * @return The list of reverse links for link <code>link</code>.
     * @throws NodeNotFoundException
     */
    public Link getReverseLink(Link link) throws NodeNotFoundException {
        List<Link> links = getLinksBetweenNodes(link.getDstNode(), link.getSrcNode());
        try {
            NodeInterface srcIf = link.getSrcInterface();
            NodeInterface dstIf = link.getDstInterface();
            for (Link l : links) {
                if (l.getSrcInterface().equals(dstIf) && l.getDstInterface().equals(srcIf)) {
                    return l;
                }
            }
        } catch (NodeInterfaceNotFoundException e) {
            if (links.size() > 1)
                logger.warn("Multiple reverse links found for link " + link.getId());
            else if (links.size() == 1)
                return links.get(0);
        }
        return null;
    }

    /**
     * Returns the list of reverse links for link <code>link</code>.
     * @param link The link to consider.
     * @return The list of reverse links for link <code>link</code>.
     * @throws NodeNotFoundException
     */
    public List<Link> getReverseLinks(Link link) throws NodeNotFoundException {
        return getLinksBetweenNodes(link.getDstNode(), link.getSrcNode());
    }

    /**
     * Returns the list of reverse links for link <code>linkId</code>.
     * @param linkId The ID of the link to consider.
     * @return The list of reverse links for link <code>linkId</code>.
     * @throws LinkNotFoundException
     * @throws NodeNotFoundException
     */
    public List<Link> getReverseLinks(String linkId) throws NodeNotFoundException, LinkNotFoundException {
        Link link = this.getLink(linkId);
        return getReverseLinks(link);
    }

    /*********************************************************************************************
     * Methods dealing with links
     *********************************************************************************************/
    /**
     * Return the link with the specified id
     *
     * @param id
     * @return
     */
    public Link getLink(String id) throws LinkNotFoundException {
        if (linkIndex == null) {
            NotInitialisedException e = new NotInitialisedException("linkIndex not initialised - call domain.init()");
            e.printStackTrace();
        }
        if (linkIndex.get(id) == null)
            throw new LinkNotFoundException(new StringBuffer().append("Link ").append(id).append(" not in the topology").toString());
        return linkIndex.get(id);
    }
    
    /**
     * Returns the link connecting the given interface on the given source node.
     * @return The link connecting the given interface on the given source node.
     */
    public Link getLinkFrom(String nodeId, String nodeInterfaceId) throws NodeNotFoundException, NodeInterfaceNotFoundException, LinkNotFoundException {
        Node node = this.getNode(nodeId);
        NodeInterface nodeInterface = node.getNodeInterface(nodeInterfaceId);
        for(Link link : node.getOutLink()) {
            if(link.getSrcInterface().equals(nodeInterface)) {
                return link;
            }
        }
        throw new LinkNotFoundException("There is no link connecting "+nodeInterfaceId+" on "+nodeId+".");
    }
    
    /**
     * Returns the link connecting the given interface on the given destination node.
     * @return The link connecting the given interface on the given destination node.
     */    
    public Link getLinkTo(String nodeId, String nodeInterfaceId) throws NodeNotFoundException, NodeInterfaceNotFoundException, LinkNotFoundException {
        Node node = this.getNode(nodeId);
        NodeInterface nodeInterface = node.getNodeInterface(nodeInterfaceId);
        for(Link link : node.getInLink()) {
            if(link.getDstInterface().equals(nodeInterface)) {
                return link;
            }
        }
        throw new LinkNotFoundException("There is no link connecting "+nodeInterfaceId+" on "+nodeId+".");
    }

    /**
     * Return the number of links
     * @return
     */
    public int getNbLinks() {
        if ((this.getTopology() != null) && (this.getTopology().getLinks() !=null)
                && (this.getTopology().getLinks().getLink() !=null))
            return this.getTopology().getLinks().getLink().size();
        return 0;
    }
    
    /**
     * Add a link to the domain
     *
     * @param link
     * @throws LinkAlreadyExistException if a link with the same id already exist in the domain
     */
    public void addLink(Link link) throws LinkAlreadyExistException, NodeNotFoundException {
        if (linkIndex.get(link.getId()) != null) {
            throw new LinkAlreadyExistException(new StringBuffer().append("Link ").append(link.getId()).append(" already exists in the domain").toString());
        }
        // Check that Topology and IGP section are present
        if (this.getTopology() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                this.setTopology(factory.createTopology());
                this.getTopology().setLinks(factory.createTopologyLinksType());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        } else if (this.getTopology().getLinks() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                this.getTopology().setLinks(factory.createTopologyLinksType());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }
        if (this.getIgp() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                this.setIgp(factory.createIgp());
                this.getIgp().setLinks(factory.createIgpIgpLinksType());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        } else if (this.getIgp().getLinks() == null) {
            ObjectFactory factory = new ObjectFactory();
            try {
                this.getIgp().setLinks(factory.createIgpIgpLinksType());
            } catch (JAXBException e) {
                e.printStackTrace();
            }
        }

        // Throw an exception if the src or dst node are not present
        // add a reference in the list of incoming and outcoming links
        ((NodeImpl)link.getSrcNode()).addOutLink(link);
        ((NodeImpl)link.getDstNode()).addInLink(link);
        
        // Add the reference to the topology
        this.getTopology().getLinks().getLink().add(link);
        // Add the IGP link reference
        LinkIgp linkIgp = ((LinkImpl) link).getIgpLink();
        this.getIgp().getLinks().getLink().add(linkIgp);
        // Add the link ID conversion to the convertor
        this.convertor.addLinkId(link.getId());
        // Add the link in the index
        linkIndex.put(link.getId(),link);

        // Notify link add
        this.getObserver().notifyAddLink(link);
    }
    
    /**
     * Remove a link from the domain.
     *
     * WARNING : use this method carrefully. This method removes all the LSP crossing the removed link. You
     * can change the status of a link if you want to simulate a failure of this link.
     *
     * @param link
     * @throws LinkNotFoundException if the link is not found in the domain
     */
    public void removeLink(Link link) throws LinkNotFoundException {
        logger.info("Remove link " + link.getId());
        if (linkIndex.get(link.getId()) == null)
            throw new LinkNotFoundException("Link " + link.getId() + " not found");

        // Remove reference in the connected nodes
        try {
            ((NodeImpl)link.getSrcNode()).delOutLink(link);
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }
        try {
            ((NodeImpl)link.getDstNode()).delInLink(link);
        } catch (NodeNotFoundException e) {
            e.printStackTrace();
        }

        // Remove all LSPs on the link
        List<Lsp> lspLinks = this.getLspsOnLink(link);
        for (int i = 0; i < lspLinks.size(); i++) {
            try {
                this.removeLsp(lspLinks.get(i));
            } catch (LspNotFoundException e) {
                e.printStackTrace();
            }
        }
        // Remove the link reference
        this.getTopology().getLinks().getLink().remove(link);
        // Remove the IGP link reference
        this.getIgp().getLinks().getLink().remove(this.getLinkIgp(link.getId()));
        // Remove the link ID conversion from the convertor
        //try {
        if (this.convertor != null)
            this.convertor.removeLinkId(link.getId());
        /*} catch (LinkNotFoundException e) {
         e.printStackTrace();
         } */
        // Remove the link of the index
        linkIndex.remove(link.getId());

        // Notify the link remove
        if (this.getObserver() != null)
            this.getObserver().notifyRemoveLink(link);
    }
    
    /**
     * Return a list of all links and a empty list if there is no links.
     * @return
     */
    public List<Link> getAllLinks() {
        if ((this.getTopology() != null) && (this.getTopology().getLinks() !=null)
                && (this.getTopology().getLinks().getLink() !=null))
            return this.getTopology().getLinks().getLink();
        return new ArrayList<Link>();
    }
    
    /**
     * Get all the up links
     *
     * @return
     */
    public List<Link> getUpLinks() {
        List<Link> upLinkList = new ArrayList<Link>();
        if ((this.getTopology() != null) && (this.getTopology().getLinks() !=null)
                && (this.getTopology().getLinks().getLink() !=null)) {
            List<Link> linkList = getAllLinks();
            for (int i = 0; i < linkList.size(); i++) {
                if (linkList.get(i).getLinkStatus() == Link.STATUS_UP)
                    upLinkList.add(linkList.get(i));
            }
        }
        return upLinkList;
    }
    
    /*********************************************************************************************
     * Methods dealing with LSPs
     *********************************************************************************************/
    /**
     * Generate a unique LSP id.
     *
     * @return
     */
    public String generateLspId() {
        String nextId = "LSP-0";
        do {
            nextId =  IdGenerator.getInstance().generateStringId("LSP-");
        } while (lspIndex.containsKey(nextId));
        /*
         try {
         this.convertor.addLspId(nextId);
         } catch (LspAlreadyExistException e) {
         logger.error("LSP " + nextId + " already present in the convertor");
         logger.error(e);
         }  */
        return nextId;
    }


    /**
     *  Generate an id for a new detour LSP, based on the detour lsp parameters
     *  A conversion for this new id is automatically added to the convertor.
     *  The format of the generated id is <br>
     *  ("GlobalBackup-" | "LocalBackup-") minimum_number "-" protected_lsp_id <br>
     *  The protection type is thus not used.
     *
     * @param protectedId id of the protected lsp
     * @param methodType
     * @param protectionType
     * @return a new id
     */
    public String generateDetourLspId(String protectedId, int methodType, int protectionType) {
        String id;
        String newId;
        int num = 0;
        if (methodType == LSPDetourRoutingParameter.GLOBAL) {
            id = "GlobalBackup-";
        } else if (methodType == LSPDetourRoutingParameter.LOCAL) {
            id = "LocalBackup-";
        } else throw new IllegalArgumentException("Bad method type");


        boolean stop = false;
        do {
            newId = id + num++ + "-" + protectedId;
            try {
                convertor.getLspId(newId);
            } catch (LspNotFoundException e) {
                stop = true;
            }
        } while (!stop);

         try {
            this.convertor.addLspId(newId);
         } catch (LspAlreadyExistException e) {
            logger.error("LSP " + newId + " already present in the convertor");
            logger.error(e);
         }

        return newId;
    }

    /**
     * Generate an unique id for a new bypass LSP. The id will be in the form "Bypass-" protectedResource ["-" number].<br>
     * A conversion for this new id is automatically added to the convertor.
     * @param protectedResource The name of the resource that the bypass will protect.
     * @return a new id
     */
    public String generateBypassLspId(String protectedResource) {
        String baseId = "Bypass-" + protectedResource;

        String id = baseId;
        int num = 0;

        try {
            while (true) {
                convertor.getLspId(id);
                id = baseId + "-" + num++;
            }
        } catch (LspNotFoundException e) {
            // id not found in convertor so use this one.
            try {
               this.convertor.addLspId(id);
            } catch (LspAlreadyExistException ex) {
               logger.error("LSP " + id + " already present in the convertor");
               logger.error(ex);
            }
            return id;
        }
    }

    /**
     * Return the Lsp with the specified id
     *
     * @param id
     * @return
     */
    public Lsp getLsp(String id) throws LspNotFoundException {
        if (lspIndex == null) {
            NotInitialisedException e = new NotInitialisedException("lspIndex not initialised - call domain.init()");
            e.printStackTrace();
        }
        if (lspIndex.get(id) == null)
            throw new LspNotFoundException(new StringBuffer().append("Lsp ").append(id).append(" not found").toString());
        return lspIndex.get(id);
    }

    /**
     * Get the list of LSPs between ingress node and egress node
     *
     * @param ingress the ingress node
     * @param egress  the egress node
     * @return the list of LSPs between ingress node and egress node
     */
    public List<Lsp> getLsps(Node ingress, Node egress) {
        List<Lsp> result = new ArrayList<Lsp>();
        Iterator<Lsp> it = this.getAllLsps().iterator();
        while (it.hasNext()) {
            Lsp lsp = it.next();
            if ((lsp.getLspPath().getSourceNode().equals(ingress))
                    && (lsp.getLspPath().getDestinationNode().equals(egress))) {
                result.add(lsp);
            }
        }
        return result;
    }

    public List<Lsp> getPrimaryLsps(Node ingress, Node egress) {
        List<Lsp> result = new ArrayList<Lsp>();
        for (Lsp lsp : getAllLsps()) {
            if (!lsp.isBackupLsp() && lsp.getLspPath().getSourceNode().equals(ingress)
                && lsp.getLspPath().getDestinationNode().equals(egress)) {
                result.add(lsp);
            }
        }
        return result;
    }

    /**
     * Add a Lsp in the domain
     * @param lsp
     * @throws LinkCapacityExceededException
     * @throws LspAlreadyExistException
     * @throws LspNotFoundException If it is a backup LSP and the primary LSP cannot be found.
     */
    public void addLsp(Lsp lsp) throws LinkCapacityExceededException, LspAlreadyExistException, LspNotFoundException, DiffServConfigurationException {
        addLsp(lsp, false);
    }

    /**
     * Add a Lsp in the domain
     *
     * @param lsp
     * @param preemption tells if preemption should be used
     * @throws be.ac.ulg.montefiore.run.totem.domain.exception.LinkCapacityExceededException
     *
     * @throws be.ac.ulg.montefiore.run.totem.domain.exception.LspAlreadyExistException
     *
     * @throws be.ac.ulg.montefiore.run.totem.domain.exception.LspNotFoundException
     *          If it is a backup LSP and the primary LSP cannot be found.
     */
    public void addLsp(Lsp lsp, boolean preemption) throws LinkCapacityExceededException, LspAlreadyExistException, LspNotFoundException, DiffServConfigurationException {

        //throws LspNotFoundException if the primary lsp cannot be found.
        Lsp primaryLsp = lsp.getProtectedLsp();

        if (lspIndex.get(lsp.getId()) != null) {
            // Comment this line -- It seems very strange -- Maybe a bug?
            // JLE - 2005-10-19
            //this.convertor.addLspId(lsp.getId());
            throw new LspAlreadyExistException();
        } else {
            try {
                this.convertor.addLspId(lsp.getId());
            } catch (LspAlreadyExistException e) {
                //do nothing
                //it happens with DAMOTE for example
                //but we know that lsp is not yet in the lsp index...
            }
        }

        // Index the LSP
        lspIndex.put(lsp.getId(), lsp);

        // Create the MPLS section if not exists
        if (this.getMpls() == null) {
            ObjectFactory of = new ObjectFactory();
            Mpls mpls = null;
            try {
                mpls = of.createMpls();
            } catch (JAXBException e) {
                e.printStackTrace();
            }
            this.setMpls(mpls);
        }

        // Add the LSP to the MPLS section
        this.getMpls().getLsp().add(lsp);

            try {
                if (preemption) {
                    List<Lsp> list = bwManagement.getPreemptList(lsp);
                    for (Lsp item : list) {
                        removeLsp(item);
                        logger.info("lsp \"" + lsp.getId() + "\" has been preempted");
                    }
                }

                bwManagement.addLsp(lsp);

                // add the backup to the list of backups of the primary lsp
                if (lsp.isDetourLsp()) {
                    primaryLsp.addBackupLsp(lsp);
                }

                observer.notifyAddLsp(lsp);

                StringBuffer sb = new StringBuffer("Add LSP ");
                sb.append(lsp.getId());
                sb.append(" from ");
                sb.append(lsp.getLspPath().getSourceNode().getId());
                sb.append(" to ");
                sb.append(lsp.getLspPath().getDestinationNode().getId());
                sb.append(" with reservation ");
                sb.append(lsp.getReservation());
                sb.append(" [");
                Iterator<Node> it = lsp.getLspPath().getNodePath().iterator();
                while (it.hasNext()) {
                    sb.append(" ");
                    sb.append(it.next().getId());
                }
                sb.append(" ]");
                logger.info(sb.toString());
            } catch (LinkCapacityExceededException e) {
                // roll back changes made to add this lsp
                // remove the lsp reference
                this.getMpls().getLsp().remove(lsp);
                // Remove the lsp ID conversion from the convertor
                if (convertor != null)
                    this.convertor.removeLspId(lsp.getId());
                // Remove the lsp of the index
                if (lspIndex != null)
                    lspIndex.remove(lsp.getId());

                // rethrow the exception
                throw e;
            } catch (DiffServConfigurationException e) {
                // roll back changes made to add this lsp
                // remove the lsp reference
                this.getMpls().getLsp().remove(lsp);
                // Remove the lsp ID conversion from the convertor
                if (convertor != null)
                    this.convertor.removeLspId(lsp.getId());
                // Remove the lsp of the index
                if (lspIndex != null)
                    lspIndex.remove(lsp.getId());

                // rethrow the exception
                throw e;
            }
    }

    /**
     * Remove a Lsp
     *
     * @param lsp
     * @throws LspNotFoundException
     */
    public void removeLsp(Lsp lsp) throws LspNotFoundException {
        removeLsp(lsp, false);
    }

    /**
     *
     * @param lsp
     * @param isReroute not used anymore
     * @throws LspNotFoundException
     */
    public void removeLsp(Lsp lsp, boolean isReroute) throws LspNotFoundException {
        logger.info("Remove lsp " + lsp.getId());

        //remove all backups lsps for this lsp.
        if (lsp.getBackups() != null) {
            for (Lsp backup : new HashSet<Lsp>(lsp.getBackups())) {
                try {
                    removeLsp(backup);
                } catch (LspNotFoundException e) {
                    logger.error("Could not remove backup lsp " + backup.getId() + " : lsp not found.");
                }
            }
        }

        if (lspIndex.get(lsp.getId()) == null)
            throw new LspNotFoundException("Lsp " + lsp.getId() + " not found");

        //Remove all associated reservations on link

            try {
                bwManagement.removeLsp(lsp);
            } catch (LinkCapacityExceededException e) {
                logger.error(e);
                e.printStackTrace();
            } catch (DiffServConfigurationException e) {
                logger.error(e);
                e.printStackTrace();
            }

        // Remove the lsp reference
        this.getMpls().getLsp().remove(lsp);
        //Remove mpls section if necessary
        if (this.getMpls().getLsp().size() <= 0) {
            this.unsetMpls();
        }

        // notify listeners before removing the LSP from the convertor
        // otherwise, LSPNotFoundException may be thrown...
        if (observer != null)
            observer.notifyRemoveLsp(lsp);
        // Remove the lsp ID conversion from the convertor
        if (convertor != null)
            this.convertor.removeLspId(lsp.getId());
        // Remove the lsp of the index
        if (lspIndex !=null)
            lspIndex.remove(lsp.getId());

        /* Changed on 05-May-2007: the reroute event has the backups (GMO) */
        if (lsp.isDetourLsp()) {
            try {
                lsp.getProtectedLsp().removeBackupLsp(lsp);
            } catch (LspNotFoundException e) {
                e.printStackTrace();
                logger.fatal("Protected lsp not found");
            }
        }

    }

    /**
     * Removes all the LSPs from the <code>Domain</code>.
     */
    public void removeAllLsps() {
        // Use an array instead of an iterator over getAllLsps() otherwise
        // a ConcurrentModificationException will be thrown.
        List<Lsp> listLsps = getAllLsps();
        Lsp[] lsps = new Lsp[listLsps.size()];
        listLsps.toArray(lsps);
        listLsps = null;
        for(int i = 0; i < lsps.length; ++i) {
            try {
                removeLsp(lsps[i]);
            }
            catch(LspNotFoundException e) {
                // should never occur...
                logger.error("Weird LspNotFoundException. Message: "+e.getMessage());
            }
        }
    }
    
    /**
     * Get the number of LSPs
     *
     * @return
     */
    public int getNbLsps() {
        if ((this.getMpls() != null) && (this.getMpls().getLsp() != null))
            return this.getMpls().getLsp().size();
        return 0;
    }
    
    /**
     * Get a list of all LSPs
     *
     * @return
     */
    public List<Lsp> getAllLsps() {
        if ((this.getMpls() != null) && (this.getMpls().getLsp() != null))
            return this.getMpls().getLsp();
        return new ArrayList<Lsp>();
    }

    /**
     * Get a list of all primary LSPs
     *
     * @return
     */
    public List<Lsp> getAllPrimaryLsps() {
        List<Lsp> result = new ArrayList<Lsp>();
        for (Lsp lsp : getAllLsps()) {
            if (!lsp.isBackupLsp()) {
                result.add(lsp);
            }
        }
        return result;
    }


    /**
     * Get all the backups Lsp of a primary LSP<br>
     * @deprecated use {@link Lsp#getBackups()}
     * @param primaryLsp
     * @return
     */
    public List<Lsp> getBackupsOfPrimary(Lsp primaryLsp) {
        List<Lsp> list = new ArrayList<Lsp>();
        if (primaryLsp.getBackups() != null) {
            list.addAll(primaryLsp.getBackups());
        }
        return list;
    }
    
    /**
     * Get all the LSPs that use the link
     *
     * @param link
     * @return
     */
    public List<Lsp> getLspsOnLink(Link link) {
        List<Lsp> result = new ArrayList<Lsp>();
        Iterator<Lsp> it = this.getAllLsps().iterator();
        while (it.hasNext()) {
            Lsp lsp = it.next();
            if (lsp.getLspPath().containsLink(link)) {
                result.add(lsp);
            }
        }
        return result;

    }

    /**
     * Get all the LSPs that begins at the specified ingress node
     *
     * @param ingress the ingress node
     * @return the list of LSPs begins at ingress node
     */
    public List<Lsp> getLspStartingAtIngress(Node ingress) {
        List<Lsp> result = new ArrayList<Lsp>();
        Iterator<Lsp> it = this.getAllLsps().iterator();
        while (it.hasNext()) {
            Lsp lsp = it.next();
            if (lsp.getLspPath().getSourceNode().equals(ingress)) {
                result.add(lsp);
            }
        }
        return result;
    }

    /**
     * Rename an lsp of the domain.
     * @param oldId
     * @param newId
     * @throws LspNotFoundException If the lsp cannot be found in the domain
     * @throws LspAlreadyExistException If the lsp already exists
     */
    public void renameLsp(String oldId, String newId) throws LspNotFoundException, LspAlreadyExistException {
        if (oldId.equals(newId)) return;
        
        Lsp lsp = getLsp(oldId);

        // Remove the lsp ID conversion from the convertor
        if (convertor != null)
            this.convertor.renameLspId(oldId, newId);
        // Remove the lsp of the index
        if (lspIndex !=null)
            lspIndex.remove(lsp.getId());

        try {
            lsp.setElementId(newId);
        } catch (IdException e) {
            e.printStackTrace();
        }

        // Index the LSP
        lspIndex.put(lsp.getId(), lsp);

        //TODO: maybe signal an event
    }

    public void renameNode(String oldId, String newId) throws NodeNotFoundException, IdException, NodeAlreadyExistException {
        if (oldId.equals(newId)) return;

        Node node = getNode(oldId);

        convertor.renameNodeId(oldId, newId);
        nodeIndex.remove(oldId);

        try {
            node.setElementId(newId);
            nodeIndex.put(node.getId(), node);
        } catch (IdException e) {
            /* revert change before throwing exception */
            nodeIndex.put(oldId, node);
            convertor.renameNodeId(newId, oldId);
            throw e;
        }
    }

    public void renameLink(String oldId, String newId) throws LinkNotFoundException, LinkAlreadyExistException, IdException {
        if (oldId.equals(newId)) return;

        Link link = getLink(oldId);

        convertor.renameLinkId(oldId, newId);
        linkIndex.remove(oldId);

        try {
            link.setElementId(newId);
            linkIndex.put(link.getId(), link);
        } catch (IdException e) {
            /* revert change before throwing exception */
            linkIndex.put(oldId, link);
            convertor.renameLinkId(newId, oldId);
            throw e;
        }
    }

    /*********************************************************************************************
     * Methods dealing with DiffServ
     *********************************************************************************************/
    
    
    /**
     * Returns the priority corresponding to a preemption level and a class type
     * @param preemption
     * @param classtype
     * @return
     */
    public int getPriority(int preemption, int classtype) {
        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][preemptionIndex]==preemption && priorityArray[i][classtypeIndex]==classtype){
                return priorityArray[i][priorityIndex];
            }
        }
        return 0;
    }

    /**
     * Return true if the domain can use preemption. Preemption cannot be used if bandwidth sharing is enabled.
     *
     * @return
     */
    public boolean usePreemption() {
        return bwManagement.usePreemption();
    }

    /**
     * Returns the preemption level corresponding to a priority
     * @param priority
     * @return
     */
    public int getPreemptionLevel(int priority) {
        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][priorityIndex]==priority){
                return priorityArray[i][preemptionIndex];
            }
        }
        return 0;
    }

    /**
     * Returns the preemption levels corresponding to a given class type
     * @param classType
     * @return
     */
    public List<Integer> getPreemptionLevels(int classType) {
        List<Integer> list = new ArrayList<Integer>();

        for (int i=0; i<getNbPriority();i++){
            if (priorityArray[i][classtypeIndex]==classType){
                list.add(priorityArray[i][preemptionIndex]);
            }
        }
        return list;
    }

    /**
     * Returns the class type corresponding to a priority
     * @param priority
     * @return
     */
    public int getClassType(int priority){
        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][priorityIndex]==priority){
                return priorityArray[i][classtypeIndex];
            }
        }
        return 0;
    }
    
    /**
     * Tells if the couple preemptionLevel/classType corresponds to an existing priority level
     * @param preemptionLevel
     * @param classType
     * @return
     */
    public boolean isExistingPriority(int preemptionLevel, int classType){
        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][preemptionIndex]==preemptionLevel && priorityArray[i][classtypeIndex]==classType)
                return true;
        }
        return false;
    }
    
    /**
     * Tells if this priority value exists or not
     * @param priority
     * @return
     */
    public boolean isExistingPriority(int priority){
        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][priorityIndex]==priority)
                return true;
        }
        return false;
    }
    
    /**
     * Gets the default minimum Priority (ie for lowest priority Class Type and lowest corresponding priority level);
     * @return
     */
    public int getMinPriority(){
        return getMinPriority(getMaxCTvalue());
    }

    /**
     * Gets the minimum priority for the given Class Type (ie lowest priority level for the class type)
     * @param ctValue
     * @return
     */
    public int getMinPriority(int ctValue) {

        int priorityValue = 0;
        int maxplValue = 0;
        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][classtypeIndex]==ctValue){
                if (priorityArray[i][preemptionIndex]>=maxplValue){
                    maxplValue = priorityArray[i][preemptionIndex];
                    priorityValue = priorityArray[i][priorityIndex];
                }
            }

        }
        return priorityValue;
    }

    /**
     * Obtain the maximum numerical value of CT (lowest priority)
     */
    public int getMaxCTvalue() {
        int maxCTvalue = 0;
        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][classtypeIndex]>maxCTvalue){
                maxCTvalue = priorityArray[i][classtypeIndex];
            }
        }
        return maxCTvalue;
    }

    /**
     * Obtain the minimum numerical value of CT (highest priority)
     */
    public int getMinCTValue() {
        int minCTValue = Integer.MAX_VALUE;
        for (int i = 0; i < getNbPriority(); i++) {
            if (priorityArray[i][classtypeIndex] < minCTValue) {
                minCTValue = priorityArray[i][classtypeIndex];
            }
        }
        return minCTValue;
    }

    /**
     * Obtain the maximum numerical value of preemption level for a given Class Type
     * @param ctValue
     * @return
     */
    public int getMaxPLValue(int ctValue) {
        int maxplValue = 0;
        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][classtypeIndex]==ctValue){
                if (priorityArray[i][preemptionIndex]>=maxplValue){
                    maxplValue = priorityArray[i][preemptionIndex];
                }
            }

        }
        return maxplValue;
    }

    /**
     * Obtain the maximum numerical value of preemption level (lowest priority)
     */
    public int getMaxPLvalue(){
        int maxPLvalue = 0;
        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][preemptionIndex]>maxPLvalue){
                maxPLvalue = priorityArray[i][preemptionIndex];
            }
        }
        return maxPLvalue;
    }

    public int getMinPLValue() {
        int minPLvalue = Integer.MAX_VALUE;
        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][preemptionIndex]<minPLvalue){
                minPLvalue = priorityArray[i][preemptionIndex];
            }
        }
        return minPLvalue;
    }

    /**
     * Gets the number of different class types
     * @return
     */
    public int getNbCT() {
        List<Integer> list = getCTs();
        return list.size();
    }
    
    /**
     * Returns an array of all available CTs
     * @return
     */
    public int[] getAllCTId() {
        List<Integer> list = getCTs();
        
        int array[] = new int[list.size()];
        
        for (int i=0; i<list.size(); i++){
            array[i]=(list.get(i)).intValue();
        }
        return array;
    }
    
    // Not in the interface
    /**
     * Get a LinkIgp from the specified linkId
     *
     * @param linkId
     * @return
     */
    public LinkIgp getLinkIgp(String linkId) throws LinkNotFoundException {
        if (igpLinkIndex == null) {
            throw new LinkNotFoundException("IgpLinkIndex not initialised - call domain.init()");
        }
        return igpLinkIndex.get(linkId);
    }
    
    /**
     * Get the number of priority levels
     * @return
     */
    public int getNbPriority() {
        if ((this.getInfo() != null) && (this.getInfo().getDiffServ() != null)
                && (this.getInfo().getDiffServ().getPriority() != null))
            return this.getInfo().getDiffServ().getPriority().size();
        
        System.out.println("Getnbpriority will return 0");
        return 0;
    }
    /**
     * Return a list of all priorities and null if there is no priorities
     * @return
     */
    public List<Information.DiffServType.PriorityType> getAllPriorities() {
        if ((this.getInfo() != null) && (this.getInfo().getDiffServ() !=null)
                && (this.getInfo().getDiffServ().getPriority() != null))
            return this.getInfo().getDiffServ().getPriority();
        return null;
    }

    /**
     * Return a list of all priorities identifiers
     * @return
     */
    public List<Integer> getPriorities() {
        List<Integer> list = new ArrayList<Integer>();
        if ((this.getInfo() != null) && (this.getInfo().getDiffServ() !=null)
                && (this.getInfo().getDiffServ().getPriority() != null)) {
            for (Object o : this.getInfo().getDiffServ().getPriority()) {
                Information.DiffServType.PriorityType p = (Information.DiffServType.PriorityType) o;
                list.add(p.getId());
            }
        }
        return list;
    }

    /**
     * Gets the maximum Preemption Level (lowest numerical value) for a given class type.
     * @return
     */
    public int getMaxPreemptionLevel(int CT){
        int minValue = Integer.MAX_VALUE;
        for (int i=0; i<getNbPriority();i++){
            if (priorityArray[i][classtypeIndex]==CT){
                if (priorityArray[i][preemptionIndex] < minValue){
                    minValue = priorityArray[i][preemptionIndex];
                }
            }
        }
        return minValue;
    }

    /**
     * Returns the priority corresponding to the same CT but a just higher preemption level (lower numerical value)
     * @param priority
     * @return
     */
    public int getPriorityHighPL(int priority) {
        int preemptionLevel=getPreemptionLevel(priority);
        int classType = getClassType(priority);
        
        int preemptionLevelValue = -1;
        int priorityValue = priority;
        for (int i=0; i<getNbPriority(); i++){
            
            if (priorityArray[i][classtypeIndex]==classType){
                if (priorityArray[i][preemptionIndex]<preemptionLevel && priorityArray[i][preemptionIndex]>preemptionLevelValue){
                    preemptionLevelValue = priorityArray[i][preemptionIndex];
                    priorityValue = priorityArray[i][priorityIndex];
                }
            }
        }
        return priorityValue;
    }
    
    /**
     * Returns all the priorities corresponding to same CT as priority but lower preemption level (higher numerical value)
     * @param priority
     * @return
     */
    public List<Integer> getLowerPLs(int priority){
        int preemptionLevel=getPreemptionLevel(priority);
        int classType = getClassType(priority);
        
        List<Integer> list = new ArrayList<Integer>();
        
        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][classtypeIndex]==classType){
                if (priorityArray[i][preemptionIndex]>preemptionLevel){
                    list.add(new Integer(priorityArray[i][priorityIndex]));
                }
            }
        }
        return list;
    }

    /**
     * Returns all the priorities at lower preemption level (higher numerical value)
     * @param priority
     * @return
     */
    public List<Integer> getLowerPLsAllCTs(int priority){
        int preemptionLevel=getPreemptionLevel(priority);

        List<Integer> list = new ArrayList<Integer>();

        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][preemptionIndex]>preemptionLevel){
                list.add(new Integer(priorityArray[i][priorityIndex]));
            }
        }
        return list;
    }

    /**
     * Returns all priorities corresponding to a same CT
     * @param CT
     * @return
     */
    public List<Integer> getPrioritySameCT(int CT){
        List<Integer> list = new ArrayList<Integer>();
        for (int i=0; i<getNbPriority(); i++){
            if (priorityArray[i][classtypeIndex]==CT){
                list.add(new Integer(priorityArray[i][priorityIndex]));
            }
        }
        return list;
        
    }

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
    public void addPriorityLevel(int priority, int classType, int preemptionLevel) throws DiffServConfigurationException {
        if((priority < 0) || (priority > 7)) {
            throw new DiffServConfigurationException("The priority level must be >= 0 and < 8!");
        }
        if((classType < 0) || (classType > 7)) {
            throw new DiffServConfigurationException("The class type must be >= 0 and < 8!");
        }
        if((preemptionLevel < 0) || (preemptionLevel > 7)) {
            throw new DiffServConfigurationException("The preemption level must be >= 0 and < 8!");
        }
        
        ObjectFactory factory = new ObjectFactory();
        
        try {
            List priorityList = getInfo().getDiffServ().getPriority();

            for(Object o : priorityList) {
                Information.DiffServType.PriorityType p = (Information.DiffServType.PriorityType) o;
                if(p.getId() == priority) {
                    throw new DiffServConfigurationException("The priority level id already exists!");
                }
                if (p.getCt() == classType && p.getPreemption() == preemptionLevel) {
                    throw new DiffServConfigurationException("A priority level with same class type and preemption level already exists!");
                }
            }

            Information.DiffServType.PriorityType newPriority = factory.createInformationDiffServTypePriorityType();
            newPriority.setId(priority);
            newPriority.setPreemption(preemptionLevel);
            newPriority.setCt(classType);
            priorityList.add(newPriority);

            //rebuild priority array
            initPriority();

            for (Link l : getAllLinks()) {
                l.addPriority(priority);
            }

        } catch(JAXBException e) {
            logger.error("A JAXBException occurred in addPriorityLevel. Message: "+e.getMessage());
            if(logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Removes the given priority level.
     * @param priority The ID of the priority level to remove.
     * @return <code>true</code> if the priority level was successfully removed
     * and <code>false</code> if the priority level did not exist.
     * @throws DiffServConfigurationException if the priority level is in used by some lsps
     */
    public boolean removePriorityLevel(int priority) throws DiffServConfigurationException {
        boolean found = false;
        List priorityList = getInfo().getDiffServ().getPriority();
        Iterator it;
        for(it = priorityList.iterator(); it.hasNext();) {
            Information.DiffServType.PriorityType p = (Information.DiffServType.PriorityType) it.next();
            if(p.getId() == priority) {
                found = true;
                break;
            }
        }

        if (found) {
            int ct = getClassType(priority);
            int pl = getPreemptionLevel(priority);
            for (Lsp lsp : getAllLsps()) {
                if (lsp.getCT() == ct && (lsp.getHoldingPreemption() == pl || lsp.getSetupPreemption() == pl)) {
                    throw new DiffServConfigurationException("Trying to remove a priority level that is used by some lsps.");
                }
            }

            it.remove();
            initPriority();
            for (Link l : getAllLinks()) {
                l.removePriority(priority);
            }
            return true;
        } else return false;
    }

    private List<Integer> getCTs(){
        List <Integer> ctIds = new ArrayList<Integer>();

        boolean add;

        for (int i=0; i<getNbPriority(); i++){
            add = true;
            for (int u=0; u<ctIds.size(); u++){
                if (priorityArray[i][classtypeIndex]==(ctIds.get(u)).intValue()){
                    add = false;
                    break;
                }
            }
            if (add == true){
                ctIds.add(new Integer(priorityArray[i][classtypeIndex]));
            }
        }
        return ctIds;

    }

    /*********************************************************************************************
     * Methods dealing with classes of service
     *********************************************************************************************/


    public void addClassOfService(String name) throws ClassOfServiceAlreadyExistException {
        ObjectFactory factory = new ObjectFactory();

        try {
            if (!getInfo().isSetClassesOfService()) {
                getInfo().setClassesOfService(factory.createInformationClassesOfServiceType());
            }

            for (Object o : getInfo().getClassesOfService().getCos()) {
                if (((Information.ClassesOfServiceType.CosType)o).getName().equals(name)) {
                    throw new ClassOfServiceAlreadyExistException("CoS " + name + " already exists.");
                }
            }

            Information.ClassesOfServiceType.CosType newCos = factory.createInformationClassesOfServiceTypeCosType();
            newCos.setName(name);
            getInfo().getClassesOfService().getCos().add(newCos);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public void removeClassOfService(String name) throws ClassOfServiceNotFoundException {
        if (!getInfo().isSetClassesOfService()) {
            throw new ClassOfServiceNotFoundException("No CoS defined for domain. Unable to remove CoS " + name);
        }

        for (Object o : getInfo().getClassesOfService().getCos()) {
            if (((Information.ClassesOfServiceType.CosType)o).getName().equals(name)) {
                getInfo().getClassesOfService().getCos().remove(o);
                if (getInfo().getClassesOfService().getCos().size() <= 0) {
                    // no more classes of service.
                    getInfo().unsetClassesOfService();
                }
                return;
            }
        }

        throw new ClassOfServiceNotFoundException("Class " + name + " not found.");
    }

    public List<String> getClassesOfService() {
        List<String> list = new ArrayList<String>();

        if (getInfo().isSetClassesOfService()) {
            for (Object o : getInfo().getClassesOfService().getCos()) {
                list.add(((Information.ClassesOfServiceType.CosType)o).getName());
            }
        }

        return list;
    }

    public List<String> getSubClasses(String classname) throws ClassOfServiceNotFoundException {
        if (!getInfo().isSetClassesOfService()) {
            throw new ClassOfServiceNotFoundException("No CoS defined for domain. Unable to remove CoS " + classname);
        }

        for (Object o : getInfo().getClassesOfService().getCos()) {
            Information.ClassesOfServiceType.CosType cos = (Information.ClassesOfServiceType.CosType)o;
            if (cos.getName().equals(classname)) {
                List<String> list = new ArrayList<String>();
                for (Object oo : cos.getSubClass()) {
                    list.add((String)oo);
                }
                return list;
            }
        }

        throw new ClassOfServiceNotFoundException("Class " + classname + " not found.");
    }

    public boolean isExistingClassOfService(String name) {
        if (getInfo().isSetClassesOfService()) {
            for (Object o : getInfo().getClassesOfService().getCos()) {
                if (((Information.ClassesOfServiceType.CosType)o).getName().equals(name))
                    return true;
            }
        }
        return false;
    }


    // *** BGP ***

    public void addBgpRouter(BgpRouter router) throws RouterAlreadyExistException {

        if (bgpRouterIndex == null)
            bgpRouterIndex= new HashMap<String,BgpRouter>();
        if (bgpRouterIndex.get(router.getId()) != null) {
            throw new RouterAlreadyExistException("Router id collision: " + router.getId());
        }

        ObjectFactory factory = new ObjectFactory();
        try {
            if (!isSetBgp()) {
                setBgp(factory.createBgp());
            }
            if (!getBgp().isSetRouters()) {
                getBgp().setRouters(factory.createBgpRoutersType());
            }
            getBgp().getRouters().getRouter().add(router);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        bgpRouterIndex.put(router.getId(), router);
    }

    /**
     * Returns the BGP router with the specified Id.
     *
     * @param id the Id of the requested BGP router.
     * @return the BGP router associated with the given Id, if it
     *         exists. Otherwise, returns null.
     */
    public BgpRouter getBgpRouter(String id)
    {
        /* If the index of BGP routers is initialized, look for the requested router */
        if (bgpRouterIndex != null)
            return bgpRouterIndex.get(id);
        return null;
    }
    
    /**
     * Returns the list of all BGP routers.
     *
     * @return the list of all BGP routers available in the domain.
     */
    public List<BgpRouter> getAllBgpRouters()
    {
        if ((this.getBgp() != null) && (this.getBgp().getRouters() != null) &&
                (this.getBgp().getRouters().getRouter() != null))
            return this.getBgp().getRouters().getRouter();
        return new ArrayList<BgpRouter>();
    }

    public void removeBgpRouters() {
        this.unsetBgp();
        if (bgpRouterIndex != null)
            bgpRouterIndex.clear();
    }

}
