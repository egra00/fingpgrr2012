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

import be.ac.ucl.ingi.cbgp.CBGPException;
import be.ac.ucl.ingi.cbgp.IPTrace;
import be.ac.ucl.ingi.cbgp.bgp.Peer;
import be.ac.ucl.ingi.cbgp.bgp.Route;
import be.ac.ucl.ingi.cbgp.bgp.Router;
import be.ac.ucl.ingi.cbgp.net.IGPDomain;
import be.ac.ucl.ingi.totem.repository.model.CBGPSimulator;
import be.ac.ulg.montefiore.run.totem.core.PreferenceManager;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.*;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.prefs.Preferences;

/*
 * Changes:
 * --------
 *  29-Nov-2005: Added the possibility to obtain the algorithm parameters (getStartAlgoParameters()). (GMO)
 *  08-Dec-2005: Implement new getRunningParameters() from the TotemAlgorithm interface. (GMO)
 *  29-Nov-2006: Add a check on tmp file and a shutdown hook to delete this tmp file. (JLE)
 *  07-Dec-2006: bugfix: Remove listener on stop (GMO)
 *  14-Dec-2006: javadoc fix, remove unused variable (GMO)
 *  28-Jun-2007: call to simRun() after start() (GMO)
 *  24-Sep-2007: Use the new CBGP JNI interface. (SBA)
 *  26-Sep-2007: Prevent NullPointerException when starting and there is no default domain (GMO)
 *  22-Nov-2007: adapt to 4.4 version interface (GMO)
 */

// -----[ CBGP ]-----------------------------------------------------

/**
 * This class implements the Routing interface and is used to compute
 * the interdomain routes available from a node towards a destination.
 * The class relies on the C-BGP's JNI interface.
 *
 * @author Bruno Quoitin (bqu@info.ucl.ac.be)
 * @author Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 *         <p/>
 *         <p/>
 *         TODO:
 *         - enhance the error handling. There is currently no information
 *         provided by the JNI interface about the cause of errors.
 *         - support multiple domains (waiting for multiple domains support in
 *         the InterDomainManager class).
 *         - add all the C-BGP JNI methods...
 *         - the C-BGP JNI methods are currently not synchronized. It is the
 *         responsibility of the developper to avoid calling these methods
 *         simultaneously from multiple execution threads.
 *         <p/>
 *         ******************************************************************
 *         WARNING: DO NOT CALL THE METHODS DEFINED BELOW SIMULTANEOUSLY FROM
 *         MULTIPLE EXECUTION THREADS. UNEXPECTED RESULTS MAY OCCUR !
 *         ******************************************************************
 */
public class CBGP implements CBGPSimulator {

    private static final Logger logger = Logger.getLogger(CBGP.class);

    /* -=[ Options ]=- */
    protected boolean optVerbose; /* Should the methods be verbose
				   * (true) or not (false) */

    /* Reference to the C-BGP instance */
    private be.ac.ucl.ingi.cbgp.CBGP cbgpJNI = null; // made public quick hack

    /* Maintain nodes & links references */
    private Hashtable<String, Object> extNodesById = new Hashtable<String, Object>();
    private Hashtable<String, Node> nodesById = new Hashtable<String, Node>();
    private Hashtable<String, Link> linksById = new Hashtable<String, Link>();
    private Hashtable<String, be.ac.ucl.ingi.cbgp.net.Link> cbgpLinksById = new Hashtable<String, be.ac.ucl.ingi.cbgp.net.Link>();
    private Hashtable<String, be.ac.ucl.ingi.cbgp.net.Node> cbgpNodesById = new Hashtable<String, be.ac.ucl.ingi.cbgp.net.Node>();
    private Hashtable<Integer, IGPDomain> cbgpIgpDomains = new Hashtable<Integer, IGPDomain>();
    private Hashtable<String, Router> cbgpBgpRouters = new Hashtable<String, Router>();

    protected static final ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();

    private Domain domain = null;

    // -----[ internalLog ]------------------------------------------
    /**
     *
     */
    private void internalLog(String msg) {
        if (optVerbose) {
            System.out.println("CBGP: " + msg);

        }
    }

    // -----[ string2ip ]--------------------------------------------
    /**
     * Converts a String into an IP address (a long integer whose
     * value is in the range [0, 2^32[.
     *
     * @param sAddress the IP address in dot format
     * @return a long integer in the range [0, 2^32[
     */
    private long string2ip(String sAddress) throws Exception {
        long lAddress = 0;
        StringTokenizer tokenizer = new StringTokenizer(sAddress, ".");
        if (tokenizer.countTokens() != 4)
            throw new Exception("Invalid IP address (needs 4 dot-separated parts) [" + sAddress + "]");
        while (tokenizer.hasMoreTokens()) {
            try {
                Short sPart = new Short(tokenizer.nextToken()).shortValue();
                if ((sPart < 0) || (sPart > 255))
                    throw new Exception("Invalid IP address (invalid part) [" + sAddress + "]");
                lAddress *= 256;
                lAddress += sPart;
            } catch (NumberFormatException e) {
                throw new Exception("Invalid IP address (invalid part) [" + sAddress + "]");
            }
        }
        return lAddress;
    }

    // -----[ ip2string ]--------------------------------------------
    /**
     * Converts a long integer into an IP address (dot format).
     *
     * @param lAddress the integer representation of the IP address
     * @return a String that represents the IP address in dot format
     */
    private String ip2string(long lAddress) throws Exception {
        String sAddress;

        if ((lAddress < 0) || (lAddress >= ((long) 1 << 32)))
            throw new Exception("Invalid IP address (not in range [0, 2^32[)");
        sAddress = String.valueOf(lAddress & 255);
        lAddress = lAddress >> 8;
        while (lAddress > 0) {
            sAddress = (lAddress & 255) + "." + sAddress;
            lAddress = lAddress >> 8;
        }
        return sAddress;
    }

    // -----[ computeSmallestPrefix ]--------------------------------
    /**
     * Computes the smallest prefix that includes all the domain's
     * nodes.
     */
    private String computeSmallestPrefix(Domain domain) throws Exception {
        long lNetwork = ((long) 1 << 32) - 1;
        byte bMask = -1;

        List<Node> nodes = domain.getAllNodes();
        for (Iterator<Node> iterNodes = nodes.iterator();
             iterNodes.hasNext();) {
            Node node = iterNodes.next();
            long lAddress = string2ip(node.getRid());

            if (bMask < 0) {
                lNetwork = lAddress;
                bMask = 32;
            }


            // If the node's IP address does not match the current
            // network prefix, make it less specific.
            for (byte bBit = 31; bBit >= (32 - bMask); bBit--) {
                if ((lAddress & (1 << bBit)) != (lNetwork & (1 << bBit))) {
                    bMask = (byte) (32 - (bBit + 1));
                    break;
                }
            }
        }

        return (ip2string(lNetwork) + "/" + bMask);
    }

    // -----[ buildInterdomainGraph ]--------------------------------
    /*
     * Build the graph of BGP routers (nodes) and BGP sessions
     * (edges).
     */
    private void buildInterdomainGraph() throws Exception {
        throw new Exception("Not yet implemented");
    }

    // -----[ initPreferences ]--------------------------------------
    /**
     * Load the preferences that are required by the CBGP class.
     */
    private void initPreferences() {
        Preferences prefs = PreferenceManager.getInstance().getPrefs();

        optVerbose = prefs.getBoolean("CBGP-verbose", false);
    }

    // -----[ initTopologyNodes ]------------------------------------
    /**
     * Add nodes into C-BGP instance. Maintain the association
     * between node Id's and nodes.
     */
    private void initTopologyNodes(Domain domain) throws RoutingException {
        List<Node> nodes = domain.getAllNodes();
        try {
            IGPDomain igpDomain = cbgpJNI.netAddDomain(domain.getASID());
            cbgpIgpDomains.put(new Integer(domain.getASID()), igpDomain);

            for (Iterator<Node> iterNodes = nodes.iterator(); iterNodes.hasNext();) {
                Node node = iterNodes.next();
                internalLog("add node " + node.getId());
                try {
                    be.ac.ucl.ingi.cbgp.net.Node cbgpNode = igpDomain.addNode(node.getRid());
                    cbgpNodesById.put(node.getRid(), cbgpNode);
                } catch (CBGPException e) {
                    throw new RoutingException("could not add node " + node.getRid() +
                            " (" + e.getMessage() + ")");
                }
                nodesById.put(node.getRid(), node);
            }

        } catch (CBGPException e) {
            throw new RoutingException("could add domain " + domain.getASID() + " (" + e.getMessage() + ")");
        }
    }

    // -----[ initTopologyLinks ]------------------------------------
    /**
     * Add links into C-BGP instance. Maintain the list of added
     * links in order to avoid adding duplicate links (remember
     * that in C-BGP, links are bidirectional while they are not
     * in the TOTEM XML file.
     */
    private void initTopologyLinks(Domain domain) throws RoutingException {
        List<Link> links = domain.getAllLinks();

        for (Iterator<Link> iterLinks = links.iterator();
             iterLinks.hasNext();) {
            Link link = iterLinks.next();

            Node nodeSrc;
            Node nodeDst;
            try {
                nodeSrc = link.getSrcNode();
                nodeDst = link.getDstNode();
            } catch (NodeNotFoundException e) {
                throw new RoutingException("Could not add link " + link.getId() +
                        " (" + e.getMessage() + ")");
            }

            int linkDelay = (new Float(link.getDelay())).intValue();
            int linkMetric = (new Float(link.getMetric())).intValue();

            String linkId = (nodeSrc.getRid().compareTo(nodeDst.getRid()) <= 0) ?
                    nodeSrc.getRid() + ":" + nodeDst.getRid() :
                    nodeDst.getRid() + ":" + nodeSrc.getRid();

            if (!linksById.containsKey(linkId)) {

                internalLog("add link " + linkId + " " + linkDelay + " " + linkMetric);
                try {
                    cbgpJNI.netAddLink(nodeSrc.getRid(), nodeDst.getRid(), linkDelay);
                } catch (CBGPException e) {
                    throw new RoutingException("Could not add link " + linkId);
                }
                linksById.put(linkId, link);

            }
        }

        try {
            Vector<be.ac.ucl.ingi.cbgp.net.Node> cbgpNodes = cbgpJNI.netGetNodes();
            for (be.ac.ucl.ingi.cbgp.net.Node currentNode : cbgpNodes) {
                Vector<be.ac.ucl.ingi.cbgp.net.Link> cbgpLinks = currentNode.getLinks();
                for (be.ac.ucl.ingi.cbgp.net.Link currentLink : cbgpLinks) {
                    String cbgpLinkId = currentNode.getAddress().toString() + ":" + currentLink.getNexthopIf().toString();
                    String linkId = (currentNode.getAddress().toString().compareTo(currentLink.getNexthopIf().toString()) <= 0) ?
                            currentNode.getAddress().toString() + ":" + currentLink.getNexthopIf().toString() :
                            currentLink.getNexthopIf().toString() + ":" + currentNode.getAddress().toString();
                    currentLink.setWeight(new Float(linksById.get(linkId).getMetric()).longValue());
                    cbgpLinksById.put(cbgpLinkId, currentLink);
                }
            }
        } catch (CBGPException e) {
            throw new RoutingException("Could not update the metrics of the links.");
        }

    }

    // -----[ updateLinksWeight ]-------------------------------------
    /**
     * Update Weights in CBGP with TE weights values
     * TODO: solve the problem with asymmetric links
     *
     * @param domain
     * @throws RoutingException
     */
    public void updateLinksWeights(Domain domain) throws RoutingException {
        List<Link> links = domain.getAllLinks();

        for (Iterator<Link> iterLinks = links.iterator(); iterLinks.hasNext();) {
            Link link = iterLinks.next();

            int linkMetric = (new Float(link.getTEMetric())).intValue(); // getting the TE metric!
            Node nodeSrc;
            Node nodeDst;
            try {
                nodeSrc = link.getSrcNode();
                nodeDst = link.getDstNode();
            } catch (NodeNotFoundException e) {
                throw new RoutingException("Could not change link metric " + link.getId() +
                        " (" + e.getMessage() + ")");
            }

            String linkId = (nodeSrc.getRid().compareTo(nodeDst.getRid()) <= 0) ?
                    nodeSrc.getRid() + ":" + nodeDst.getRid() :
                    nodeDst.getRid() + ":" + nodeSrc.getRid();

            if (cbgpLinksById.containsKey(linkId)) {
                try {
                    cbgpLinksById.get(linkId).setWeight(linkMetric);
                } catch (CBGPException e) {
                    throw new RoutingException("Could not change link metric " + link.getId() +
                            "(" + e.getMessage() + ")");
                }
            } else {
                throw new RoutingException("Could not change link metric : the link has not been added to CBGP");
            }
        }
    }


    // -----[ computeIGP ]-------------------------------------------
    /**
     * For each domain, for each node, compute the intradomain
     * routes.
     * <p/>
     * AT THIS TIME, COMPUTES THE IGP ROUTES AS IF THERE WAS A
     * SINGLE DOMAIN !
     */
    public void computeIGP(Domain domain) throws RoutingException {
        Integer asId = new Integer(domain.getASID());
        if (cbgpIgpDomains.containsKey(asId)) {
            try {
                cbgpIgpDomains.get(asId).compute();
            } catch (CBGPException e) {
                throw new RoutingException("could not compute domain prefix (" + e.getMessage() + ")");
            }
        } else {
            throw new RoutingException("Trying to compute the IGP for a domain which has not been loaded in CBGP");
        }
    }

    // -----[ initTopologyRouters ]----------------------------------
    /**
     * Initialize the BGP routers. Add the originated networks and the
     * neighbors.
     */
    private void initTopologyRouters(Domain domain) throws RoutingException {
        boolean bNeedRecomputeIgp = false;

        IGPDomain neighboringVirtualDomain;
        if (cbgpIgpDomains.containsKey(domain.getASID() + 1)) {
            neighboringVirtualDomain = cbgpIgpDomains.get(domain.getASID() + 1);
        } else {
            try {
                neighboringVirtualDomain = cbgpJNI.netAddDomain(domain.getASID() + 1);
            } catch (CBGPException e) {
                throw new RoutingException("could not add a virtual neighboring domain  (" + e.getMessage() + ")");
            }
        }

        for (Enumeration<Node> enumNodes = nodesById.elements();
             enumNodes.hasMoreElements();) {
            Node node = enumNodes.nextElement();
            BgpRouter router = node.getBgpRouter();
            if (router != null) {
                // Add router in the C-BGP instance
                internalLog("add router " + router.getRid() + " " + router.getId() + " " + domain.getASID());
                try {
                    Router cbgpRouter = cbgpJNI.bgpAddRouter(router.getId(), router.getRid(), domain.getASID());
                    cbgpBgpRouters.put(router.getRid(), cbgpRouter);

                    // Add all originated networks
                    List<BgpNetwork> networks = router.getAllNetworks();
                    for (Iterator<BgpNetwork> iterNetworks = networks.iterator();
                         iterNetworks.hasNext();) {
                        BgpNetwork network = iterNetworks.next();
                        internalLog("add network " + network.getPrefix());
                        try {
                            cbgpRouter.addNetwork(network.getPrefix());
                        } catch (CBGPException e) {
                            throw new RoutingException("could not add network " + network.getPrefix() +
                                    " (" + e.getMessage() + ")");
                        }
                    }

                    // Add all neighbors and open the BGP session
                    List<BgpNeighbor> neighbors = router.getAllNeighbors();
                    for (Iterator<BgpNeighbor> iterNeighbors = neighbors.iterator();
                         iterNeighbors.hasNext();) {
                        BgpNeighbor neighbor = iterNeighbors.next();

                        boolean bVirtual = false;

                        /* Internal/external BGP neighbor */
                        if (neighbor.getASID() == domain.getASID()) {

                            /* Internal... */

                            /* Check that the neighbor node exists. Issue
                             * a warning if not. */
                            if (!nodesById.containsKey(neighbor.getAddress()))
                                internalLog("WARNING: no node for neighbor " + neighbor.getAddress());

                        } else {

                           /* External... */

                            /* Check that the neighbor node exists. Create
                             * it virtual if not. */
                            if (!extNodesById.containsKey(neighbor.getAddress())) {

                                internalLog("WARNING: create virtual node for neighbor " + neighbor.getAddress());

                                bVirtual = true;

                                try {

                                    /* Need to add a node and a link, the BGP
                                     * router will be virtual */
                                    be.ac.ucl.ingi.cbgp.net.Node cbgpNode = neighboringVirtualDomain.addNode(neighbor.getAddress());
                                    cbgpNodesById.put(neighbor.getAddress(), cbgpNode);

                                    extNodesById.put(neighbor.getAddress(), neighbor);
                                    cbgpJNI.netAddLink(router.getRid(), neighbor.getAddress(), 0);

                                    /* Add static routes */

                                    cbgpNodesById.get(router.getRid()).addRoute(neighbor.getAddress() + "/32", neighbor.getAddress(), 0);

                                    cbgpNode.addRoute(router.getRid() + "/32", router.getRid(), 0);
                                } catch (CBGPException e) {
                                    throw new RoutingException("could not add external virtual router " +
                                            neighbor.getAddress() +
                                            " (" + e.getMessage() + ")");
                                }

                            }

                        } // end of external

                        internalLog("add neighbor " + neighbor.getAddress() + " " + neighbor.getASID());
                        try {
                            int neighborASID = domain.getASID();
                            ;
                            if (domain.getASID() != neighbor.getASID()) {
                                neighborASID += 1;  // domain.getASID() + 1 is the ASID of the virtual IGPDomain in which
                                // neighboring nodes have been added.
                            }
                            Peer currentPeer = cbgpRouter.addPeer(neighbor.getAddress(), neighborASID);
                            if (neighbor.hasNextHopSelf()) {
                                currentPeer.setNextHopSelf(true);
                            }
                            if (bVirtual)
                                currentPeer.setVirtual();

                        } catch (CBGPException e) {
                            throw new RoutingException("could not configure router " + router.getRid() +
                                    " (" + e.getMessage() + ")");
                        }
                    }


                } catch (CBGPException e) {
                    throw new RoutingException("could not add BGP router " + router.getRid() +
                            " " + router.getId() + " " + domain.getASID() +
                            " (" + e.getMessage() + ")");
                }
            }

        }

        /* Recompute IGP if needed */
        if (bNeedRecomputeIgp) {
            computeIGP(domain);
        }

        /* Activate sessions... */
        for (Enumeration<Node> enumNodes = nodesById.elements();
             enumNodes.hasMoreElements();) {
            Node node = enumNodes.nextElement();
            BgpRouter router = node.getBgpRouter();
            if (router != null) {

                Router cbgpRouter = cbgpBgpRouters.get(router.getRid());

                // Add all neighbors and open the BGP session
                try {
                    for (Peer peer : cbgpRouter.getPeers()) {
                        internalLog("open session with neighbor " + router.getRid());
                        peer.openSession();
                    }
                } catch (CBGPException e) {
                    throw new RoutingException("could not open session with neighbor " +
                            router.getRid() +
                            " (" + e.getMessage() + ")");
                }

            }
        }

    }

    // -----[ initTopology ]-----------------------------------------
    /**
     * Load the topology into the C-BGP instance. At the same time,
     * populate hashtables that will maintain references to each
     * C-BGP object (link/node).
     */
    private void initTopology() throws Exception {
        /*
        for (Enumeration domains= InternetManager.getInstance().getDomains();
             domains.hasMoreElements();) {
             Domain domain= (Domain) domains.nextElement();
        */

        domain = InterDomainManager.getInstance().getDefaultDomain();
        if (domain != null) {
            internalLog("Domain's AS number: " + domain.getASID());

            initTopologyNodes(domain);
            initTopologyLinks(domain);
            computeIGP(domain);
            initTopologyRouters(domain);

            // add a change listener
            domain.getObserver().addListener(CBGPChangeListener.getInstance());
        }
        /*
        }
        */
    }

    // -----[ start ]------------------------------------------------
    /**
     * Implements the 'start' method defined by the TotemAlgorithm
     * interface. The method retrieves the topology contained in the
     * topology manager, and setup nodes, links and BGP routers inside
     * the CBGP instance.
     */
    public void start(HashMap params) {
        cbgpJNI = new be.ac.ucl.ingi.cbgp.CBGP();
        try {
            File f = File.createTempFile("TOTEM_CBGP", ".log");
            f.deleteOnExit();
            if (!f.canWrite()) {
                logger.error("Unable to write in " + f.getPath() + "!");
                System.err.println("Unable to write in " + f.getPath() + "!");
                System.exit(-1);
            }
            cbgpJNI.init(f.getPath());
        } catch (IOException e) {
            logger.error("An IOException occurred when creating temp file! Message: " + e.getMessage());
            System.exit(-1);
        } catch (CBGPException e) {
            logger.error("Could not init CBGP.");
            System.exit(-1);
        }

        initPreferences();

        try {
            initTopology();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            simRun();
        } catch (RoutingException e) {
            e.printStackTrace();
        }

    }

    // -----[ start ]------------------------------------------------
    /**
     * Implements the 'start' method defined by the TotemAlgorithm
     * interface. The method retrieves the topology contained in the
     * topology manager, and setup nodes, links and BGP routers inside
     * the CBGP instance.
     */
    public void start() {
        start(null);
    }

    // -----[ stop ]-------------------------------------------------
    /**
     * Implements the 'stop' method defined by the TotemAlgorithm
     * interface.
     */
    public void stop() {
        if (domain != null) {
            domain.getObserver().removeListener(CBGPChangeListener.getInstance());
        }
        try {
            cbgpJNI.destroy();
        } catch (CBGPException e) {
            e.printStackTrace();
        }
    }

    // -----[ runCmd ]------------------------------------------------
    /**
     * Send a "raw" command to the C-BGP instance.
     *
     * @param sCommand the command
     */
    public void runCmd(String sCommand)
            throws RoutingException {
        try {
            cbgpJNI.runCmd(sCommand);
        } catch (CBGPException e) {
            throw new RoutingException("an error occured during the command execution (" +
                    e.getMessage() + ")");
        }
    }

    // -----[ simRun ]-----------------------------------------------
    /**
     * Run the C-BGP simulator.
     */
    public void simRun()
            throws RoutingException {
        try {
            cbgpJNI.simRun();
        } catch (CBGPException e) {
            throw new RoutingException("an error occured during the simulation (" +
                    e.getMessage() + ")");
        }
    }

    // -----[ netNodeGetLinks ]--------------------------------------
    /**
     * Returns a vector with the links adjacent to this node. Each
     * element in the vector is an object of class
     * be.ac.ucl.ingi.cbgp.Link
     *
     * @param sNodeAddr the IP address of the node
     * @return a vector with Link objects describing the node's links
     */
    public Vector netNodeGetLinks(String sNodeAddr)
            throws RoutingException {
        try {
            if (cbgpNodesById.containsKey(sNodeAddr)) {
                be.ac.ucl.ingi.cbgp.net.Node cbgpNode = cbgpNodesById.get(sNodeAddr);
                return cbgpNode.getLinks();
            } else {
                throw new RoutingException("could not get routing table of " + sNodeAddr + " : the node has not been added to CBGP");
            }
        } catch (CBGPException e) {
            throw new RoutingException("could not get links of " + sNodeAddr +
                    " (" + e.getMessage() + ")");
        }
    }

    // -----[ netNodeGetRT ]-----------------------------------------
    /**
     * Returns a vector with the routes currently in the routing table
     * of this node. Each element in the vector is an object of class
     * be.ac.ucl.ingi.cbgp.IPRoute
     *
     * @param sNodeAddr the IP address of the node
     * @param sPrefix   a filter for routes to return
     * @return a vector with IPRoute objects describing the node's
     *         routes
     */
    public Vector netNodeGetRT(String sNodeAddr, String sPrefix)
            throws RoutingException {
        try {
            if (cbgpNodesById.containsKey(sNodeAddr)) {
                be.ac.ucl.ingi.cbgp.net.Node cbgpNode = cbgpNodesById.get(sNodeAddr);
                return cbgpNode.getRT(sPrefix);
            } else {
                throw new RoutingException("could not get routing table of " + sNodeAddr + " : the node has not been added to CBGP");
            }
        } catch (CBGPException e) {
            throw new RoutingException("could not get routing table of " + sNodeAddr +
                    " (" + e.getMessage() + ")");
        }
    }

    // -----[ netNodeRecordRoute ]-----------------------------------
    /**
     * Returns an IPTrace object that contains the record-route
     * status, the list of IP address hops, the total delay and the
     * total IGP weight.
     *
     * @param sNodeAddr the IP address of the source node
     * @param sDstAddr  the IP address of the destination node
     * @return an IPTrace object containing the results
     */
    public IPTrace netNodeRecordRoute(String sNodeAddr, String sDstAddr)
            throws RoutingException {
        try {
            if (cbgpNodesById.containsKey(sNodeAddr)) {
                be.ac.ucl.ingi.cbgp.net.Node cbgpNode = cbgpNodesById.get(sNodeAddr);
                return cbgpNode.recordRoute(sDstAddr);
            } else {
                throw new RoutingException("could not trace route from " + sNodeAddr + " : the node has not been added to CBGP");
            }
        } catch (CBGPException e) {
            throw new RoutingException("could not trace route from " + sNodeAddr +
                    " to " + sDstAddr +
                    " (" + e.getMessage() + ")");
        }
    }

    // -----[ bgpDomainRescan ]--------------------------------------
    /**
     * Used to rescan the routes after IGP changes
     *
     * @param domain
     * @throws RoutingException
     */
    public void bgpDomainRescan(Domain domain)
            throws RoutingException {
        try {
            if (cbgpIgpDomains.containsKey(domain.getASID())) {
                cbgpIgpDomains.get(domain.getASID()).compute();
            } else {
                throw new RoutingException("could not rescan BGP domain : the domain has not been added to CBGP");
            }
        } catch (CBGPException e) {
            throw new RoutingException("could not rescan BGP domain " +
                    domain.getASID() +
                    " (" + e.getMessage() + ")");
        }
    }
    
    // -----[ bgpRouterGetPeers ]------------------------------------
    /**
     * Returns a vector with the neighbors of this router. Each
     * element in the vector is an object of class
     * be.ac.ucl.ingi.cbgp.BGPPeer
     *
     * @param sRouterAddr the IP address of the BGP router
     * @return a vector with BGPPeer objects describing the router's
     *         neighbors
     */
    public Vector<Peer> bgpRouterGetPeers(String sRouterAddr)
            throws RoutingException {
        try {
            if (cbgpBgpRouters.containsKey(sRouterAddr)) {
                return cbgpBgpRouters.get(sRouterAddr).getPeers();
            } else {
                throw new RoutingException("could not get AdjRIB of " + sRouterAddr + " : the router has not been added to CBGP");
            }
        } catch (CBGPException e) {
            throw new RoutingException("could not get peers of " + sRouterAddr +
                    " (" + e.getMessage() + ")");
        }
    }

    // -----[ bgpRouterGetRib ]--------------------------------------
    /**
     * Returns a vector with the BGP routes in the RIB of this
     * node. Each element in the vector is an object of class
     * be.ac.ucl.ingi.cbgp.BGPRoute
     *
     * @param sRouterAddr the IP address of the BGP router
     * @param sPrefix     a filter for the routes to return
     * @return a vector with BGPRoute objects describing the router's
     *         BGP routes
     */
    public Vector bgpRouterGetRib(String sRouterAddr, String sPrefix)
            throws RoutingException {
        try {
            if (cbgpBgpRouters.containsKey(sRouterAddr)) {
                return cbgpBgpRouters.get(sRouterAddr).getRIB(sPrefix);
            } else {
                throw new RoutingException("could not get AdjRIB of " + sRouterAddr + " : the router has not been added to CBGP");
            }
        } catch (CBGPException e) {
            throw new RoutingException("could not get RIB of " + sRouterAddr +
                    " (" + e.getMessage() + ")");
        }
    }

    // -----[ bgpRouterGetAdjRib ]-----------------------------------
    /**
     * Returns a vector with the BGP routes in the Adj-RIB(s) of this
     * router. Each element in the vector is an object of class
     * be.ac.ucl.ingi.cbgp.BGPRoute
     *
     * @param sRouterAddr the IP address of the BGP router
     * @param sPeerAddr   the IP address of one peer of the router (may
     *                    be null if the Adj-RIB of all peers mut be
     *                    returned)
     * @param sPrefix     a filter for the routes to return
     * @return a vector with BGPRoute objects describing the router's
     *         BGP routes
     */
    public Vector<Route> bgpRouterGetAdjRib(String sRouterAddr, String sPeerAddr,
                                            String sPrefix, boolean bIn)
            throws RoutingException {
        try {
            if (cbgpBgpRouters.containsKey(sRouterAddr)) {
                return cbgpBgpRouters.get(sRouterAddr).getAdjRIB(sPeerAddr, sPrefix, bIn);
            } else {
                throw new RoutingException("could not get AdjRIB of " + sRouterAddr + " : the router has not been added to CBGP");
            }
        } catch (CBGPException e) {
            throw new RoutingException("could not get AdjRIB of " + sRouterAddr +
                    " (" + e.getMessage() + ")");
        }
    }

    // -----[ bgpRouterPeerRecv ]------------------------------------
    /**
     * Send a BGP message in MRT format to the given router, through
     * the given peer.
     *
     * @param sRouterAddr the IP address of the BGP router
     * @param sPeerAddr   the IP address of the peer
     * @param sMsg        the BGP message in MRT format
     */
    public void bgpRouterPeerRecv(String sRouterAddr, String sPeerAddr, String sMsg)
            throws RoutingException {
        try {
            if (cbgpBgpRouters.containsKey(sRouterAddr)) {
                Vector<Peer> peers = cbgpBgpRouters.get(sRouterAddr).getPeers();
                boolean peerFound = false;
                for (Peer peer : peers) {
                    if (peer.getAddress().toString().compareTo(sPeerAddr) == 0) {
                        peerFound = true;
                        if (peer.isVirtual()) {
                            //System.out.println("Calling the recv method on a virtual peer session (router = " + sRouterAddr + " and peer = " + sPeerAddr + ")");
                            peer.recv(sMsg);
                        } else {
                            //System.out.println(sMsg);
                            //throw new RoutingException("Trying to call the recv method on a non-virtual peer (router = " + sRouterAddr + " and peer = " + sPeerAddr + ") !");
                        }
                    }
                }
                if (!peerFound) {
                    throw new RoutingException("could not receive message : peer not found");
                }
            } else {
                throw new RoutingException("could not receive message : the BGP router has not been added in CBGP");
            }
        } catch (CBGPException e) {
            throw new RoutingException("could not receive message \"" + sMsg + "\" from " +
                    sPeerAddr + " in router " + sRouterAddr +
                    " (" + e.getMessage() + ")");
        }
    }

    // -----[ bgpRouterPeerUp ]--------------------------------------
    /**
     * Change the status of the BGP session between a router and one
     * of its neighbors.
     *
     * @param sRouterAddr the IP address of the BGP router
     * @param sPeerAddr   the IP address of the BGP router's neighbor
     * @param bUp         a boolean indicating the requested status. True
     *                    means the session up and false means the session down.
     */
    public void bgpRouterPeerUp(String sRouterAddr, String sPeerAddr, boolean bUp)
            throws RoutingException {
        try {
            if (cbgpBgpRouters.containsKey(sRouterAddr)) {
                Vector<Peer> peers = cbgpBgpRouters.get(sRouterAddr).getPeers();
                boolean peerFound = false;
                for (Peer peer : peers) {
                    if (peer.getAddress().toString().compareTo(sPeerAddr) == 0) {
                        peerFound = true;
                        if (bUp) {
                            peer.openSession();
                        } else {
                            peer.closeSession();
                        }
                    }
                }
                if (!peerFound) {
                    throw new RoutingException("could not change state of peering : peer not found");
                }
            } else {
                throw new RoutingException("could not change state of peering : the BGP router has not been added in CBGP");
            }
        } catch (CBGPException e) {
            throw new RoutingException("could not change state of peering between " +
                    sRouterAddr + " and " + sPeerAddr +
                    " (" + e.getMessage() + ")");
        }
    }

    // -----[ bgpRouterLoadRib ]-------------------------------------
    /**
     * Load the RIB contained in the given filename into the given
     * router's RIB. The RIB must be provided in ASCII MRT (not
     * compressed). There are also restrictions on the BGP routes that
     * can be loaded into the router (check on the next-hop,
     * etc). Have a look at C-BGP's documentation for more
     * information!
     *
     * @param sRouterAddr the IP address of the BGP router
     * @param sFileName   the name of the file that contains the RIB.
     */
    public void bgpRouterLoadRib(String sRouterAddr, String sFileName)
            throws RoutingException {
        try {
            cbgpBgpRouters.get(sRouterAddr).loadRib(sFileName, false);
        } catch (CBGPException e) {
            throw new RoutingException("could not load RIB \"" + sFileName +
                    "\" into router " + sRouterAddr +
                    " (" + e.getMessage() + ")");
        }
    }

    public List<ParameterDescriptor> getStartAlgoParameters() {
        return (List<ParameterDescriptor>) params.clone();
    }

    public HashMap getRunningParameters() {
        return null;
    }

    /**
     * Change the status of a link
     *
     * @param sSrcAddr l'adresse IP du routeur source du lien
     * @param sDstAddr l'adresse IP du routeur destination du lien
     * @param bUp      le status du noeud (true = UP)
     */
    public void netLinkUp(String sSrcAddr, String sDstAddr, boolean bUp) {

        String linkId = (sSrcAddr.compareTo(sDstAddr) <= 0) ?
                sSrcAddr + ":" + sDstAddr :
                sDstAddr + ":" + sSrcAddr;

        if (cbgpLinksById.containsKey(linkId)) {
            try {
                cbgpLinksById.get(linkId).setState(true);
            } catch (CBGPException e) {
                System.out.println("could not change link status : " + e.getMessage());
            }
        }
    }

    /**
     * Change the metric of a link
     *
     * @param sSrcAddr l'adresse IP du routeur source du lien
     * @param sDstAddr l'adresse IP du routeur destination du lien
     * @param iWeight  la nouvelle métrique
     */
    public void netLinkWeight(String sSrcAddr, String sDstAddr, int iWeight) throws RoutingException {
        String linkId = (sSrcAddr.compareTo(sDstAddr) <= 0) ?
                sSrcAddr + ":" + sDstAddr :
                sDstAddr + ":" + sSrcAddr;

        if (cbgpLinksById.containsKey(linkId)) {
            try {
                cbgpLinksById.get(linkId).setWeight(iWeight);
            } catch (CBGPException e) {
                throw new RoutingException("Could not change link metric " + linkId + "(" + e.getMessage() + ")");
            }
        } else {
            throw new RoutingException("Could not change link metric : the link has not been added to CBGP");
        }

    }
    
    /**
     * Many more methods to come...
     */

}
