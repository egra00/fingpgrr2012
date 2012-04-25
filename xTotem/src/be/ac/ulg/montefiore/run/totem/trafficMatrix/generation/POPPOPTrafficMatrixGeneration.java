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
package be.ac.ulg.montefiore.run.totem.trafficMatrix.generation;

import be.ac.ucl.ingi.cbgp.IPAddress;
import be.ac.ucl.ingi.cbgp.bgp.Route;
import be.ac.ucl.ingi.totem.repository.CBGP;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.BandwidthUnit;
import be.ac.ulg.montefiore.run.totem.domain.bgp.BGPInfoChecker;
import be.ac.ulg.montefiore.run.totem.repository.facade.RepositoryManager;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.NoSuchAlgorithmException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidFileException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.exception.InvalidTrafficMatrixException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.TrafficMatrix;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.impl.TrafficMatrixImpl;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.InterTMType;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.TrafficMatrixFileType;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.BandwidthUnits;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.persistence.TrafficMatrixFactory;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* Changes:
* --------
* - 11-May-2005: updates towards genericity
* - 28-Jun-2007: rewrite code, domain is given in constructor, bgp instance is not given in method call anymore (GMO)
* - 20-Sep-2007: BGP dumps can be gzipped, make use of BGPInfoChecker class, move loadInterDomainTM to TrafficMatrixFatory (GMO)
*/

/**
 * This class generates a POP-POP XML intra-domain traffic matrix from an XML interdomain traffic matrix.
 * In an XML inter-domain traffic matrix, we have for each node some destinations prefixes for which
 * we need to know the corresponding egress node.  For this, we replay BGP dumps in C-BGP.
 * <p/>
 * <p>Creation date: 04-03-2005
 *
 * @author Olivier Delcourt (delcourt@run.montefiore.ulg.ac.be)
 * @author Simon Balon (balon@run.montefiore.ulg.ac.be)
 * @author Gael Monfort (monfort@run.montefiore.ulg.ac.be)
 */


public class POPPOPTrafficMatrixGeneration {

    private final static Logger logger = Logger.getLogger(POPPOPTrafficMatrixGeneration.class);

    public double notRoutedTraffic = 0;

    private Domain domain;
    private CBGP cbgp = null;

    public POPPOPTrafficMatrixGeneration(Domain domain) {
        this.domain = domain;

        if (domain == null) throw new IllegalArgumentException("Domain is null");

        try {
            RepositoryManager.getInstance().startAlgo("CBGP", null, domain.getASID());
        } catch (AlgorithmInitialisationException e) {
        }
        try {
            cbgp = (CBGP) RepositoryManager.getInstance().getAlgo("CBGP");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }


    /**
     * Run cbgp. Call this when you have done reading clusters, dump and updates.
     * @throws RoutingException
     */
    public void simRun() throws RoutingException {
        cbgp.simRun();
    }

    /**
     * This method loads prefixes from BGP dumps into an existing CBGP instance.
     *
     * @param BGPbaseDirectory the directories containing dumps
     * @param BGPdirFileName   the directory and filename of the dumps found in BGPbaseDirectory/id/ or BGPbaseDirectory/rid/
     * @param prefixes         the list of prefixes that must be loaded. If null, all prefixes will be loaded.
     * @throws InvalidFileException If the directories are not named by id or by rid
     */
    public void loadDump(String BGPbaseDirectory, String BGPdirFileName, Collection<String> prefixes) throws InvalidFileException {

        boolean idNaming = BGPInfoChecker.isIdNaming(domain, BGPbaseDirectory);

        for (Node node : domain.getAllNodes()) {
            String id = idNaming ? node.getId() : node.getRid();
            File mrtFile = new File(BGPbaseDirectory + File.separator + id + File.separator + BGPdirFileName);

            boolean gzipped = mrtFile.getPath().endsWith(".gz");
            // Now trying opening corresponding dumps which must be in MRT ASCII MACHINE READABLE FORMAT
            BufferedReader in = null;
            try {
                if (gzipped) {
                    FileInputStream fis = new FileInputStream(mrtFile);
                    GZIPInputStream gis = new GZIPInputStream(fis);
                    InputStreamReader isr = new InputStreamReader(gis);
                    in = new BufferedReader(isr);
                } else {
                    FileReader fr = new FileReader(mrtFile);
                    in = new BufferedReader(fr);
                }
                String line = in.readLine();

                if (!BGPInfoChecker.MRTFormatPattern.matcher(line).find()) {
                    logger.error("File " + mrtFile.getAbsolutePath() + " not in MRT ASCII MACHINE READABLE FORMAT! Use route_btoa utily to convert it!");
                    break;
                }

                do {
                    String[] stringArray = line.split("\\|");

                    /*for (int j=0; j<stringArray.length; j++){
                    System.out.println("Field " + j + " Value : " + stringArray[j]);
                    }*/

                    if (prefixes == null || prefixes.contains(stringArray[5])) {
                        // Adding the corresponding eBGP sessions
                        String peerIP = stringArray[8];

                        // replacing |B| (best route) par |A| (announce)
                        Matcher match = Pattern.compile("\\|B\\|").matcher(line);
                        line = match.replaceAll("|A|");

                        // replacing TABLE_DUMP by BGP4
                        match = Pattern.compile("TABLE_DUMP").matcher(line);
                        line = match.replaceAll("BGP4");

                        // replacing first field with main IP (routers often have multiple IP addresses, we
                        // chose to just deal with one main IP, found in the RID field.  If router has
                        // multiple addresses, they must be added to the XML topology format using
                        // interfaces and their Mask/IP field.
                        match = Pattern.compile("\\|A\\|(\\d{1,3}\\.?){4,4}\\|").matcher(line);
                        line = match.replaceAll("|A|".concat(node.getRid()).concat("|"));

                        // replacing last field with main IP of the corresponding node...
                        for (Node nodek : domain.getAllNodes()) {
                            List<String> ipList = nodek.getallIPs();

                            int u = 0;
                            for (u = 0; u < ipList.size(); u++) {
                                if (ipList.get(u).equals(peerIP)) {
                                    peerIP = nodek.getRid();
                                    match = Pattern.compile("\\|IGP\\|(\\d{1,3}\\.?){4,4}\\|").matcher(line);
                                    if (match.find()) {
                                        line = match.replaceAll("|IGP|".concat(peerIP).concat("|"));
                                        break;
                                    } else {
                                        match = Pattern.compile("\\|EGP\\|(\\d{1,3}\\.?){4,4}\\|").matcher(line);
                                        if (match.find()) {
                                            line = match.replaceAll("|EGP|".concat(peerIP).concat("|"));
                                            break;
                                        } else {
                                            match = Pattern.compile("\\|INCOMPLETE\\|(\\d{1,3}\\.?){4,4}\\|").matcher(line);
                                            if (match.find()) {
                                                line = match.replaceAll("|INCOMPLETE|".concat(peerIP).concat("|"));
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                            if (u < ipList.size()) break;
                        }

                        if (!peerIP.equals(node.getRid())) {
                            /*
                            boolean inDomain = false;
                            for (int z=0; z<nodes.size(); z++){
                            if (peerIP.equals(nodes.get(z).getRid())) {
                            inDomain = true;
                            break;
                            }
                            }
                            */

                            //System.out.println("Sending msg to : " + node.getRid() + " through peer " + peerIP + " msg " + line);
                            // if (inDomain == false){
                            try {
                                cbgp.bgpRouterPeerRecv(node.getRid(), peerIP, line);
                            } catch (Exception e) {
                                e.printStackTrace();
                                logger.error("RoutingException when executing bgpRouterPeerRecv: " + e.getMessage());
                            }
                            // }
                        } else {
                            logger.warn("PEER and NEXT-HOP is the same, msg not sent: " + node.getRid() + " msg: " + line);
                        }
                    }
                } while ((line = in.readLine()) != null);
            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                logger.error("File not found for node " + node. getId() + ": " + mrtFile.getAbsolutePath());
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("IOException for file: " + mrtFile.getAbsolutePath());
            } finally {
                try {
                    if (in != null) in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * This method can be used to generate an XML traffic matrix from a text-file traffic matrix.
     * <p>Typical format:<br>
     * <code>src_node dst_node flow_size</code><br>
     * where src_node and dst_node must be IP addresses corresponding to RIDs in the XML topology format.
     *
     * @param trafficMatrixFileName
     * @return
     * @throws IOException
     */
    public TrafficMatrix generatePOPPOPTrafficMatrix(String trafficMatrixFileName) throws IOException /*throws Exception*/ {
        TrafficMatrix tm = null;
        try {
            tm = new TrafficMatrixImpl(domain.getASID());
        } catch (InvalidDomainException e) {
            logger.error(e);
            throw new IllegalStateException("Invalid domain");
        }

        FileReader fr = new FileReader(trafficMatrixFileName);
        BufferedReader in = new BufferedReader(fr);
        String line;
        try {
            while ((line = in.readLine()) != null) {
                Pattern urlPattern = Pattern.compile("((\\d{1,3}\\.?){4,4})\\s+((\\d{1,3}\\.?){4,4})\\s+(\\d+\\.*\\d*)");
                Matcher matcher = urlPattern.matcher(line);
                if (matcher.find()) {
                    // check if source and destination is in the topology!

                    String srcIP = matcher.group(1);
                    String dstIP = matcher.group(3);
                    String srcID = null;
                    String dstID = null;
                    try {
                        srcID = domain.getNodeByRid(srcIP).getId();
                        dstID = domain.getNodeByRid(dstIP).getId();
                        tm.set(srcID, dstID, (Float.parseFloat(matcher.group(5))) / 7.2f);  // ad-hoc conversion, please adapt
                    } catch (NodeNotFoundException e) {
                        logger.error(srcIP + " or " + dstIP + " not found in domain.");
                    }
                }
            }
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fr != null) fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return tm;
    }


    /**
     * This method reads a cluster file resulting from bgp- sum2.pl and loads all cluster prefixes
     * in an existing C-BGP instance.
     *
     * @param clusterFileName  The name of the clustering file resulting from the perl script.
     * @param BGPbaseDirectory
     * @param BGPdirFileName
     * @return
     * @throws IOException If an IOException occurs when reading the cluster file.
     * @throws InvalidFileException If the directories are not named by id or by rid
     */
    public HashMap<String, String> readCluster(String clusterFileName, String BGPbaseDirectory, String BGPdirFileName) throws IOException, InvalidFileException {

        File clusterFile = new File(clusterFileName);

        HashMap<String, String> hashMap = new HashMap<String, String>();
        List<String> atomsList = new ArrayList<String>();

        FileReader fr = new FileReader(clusterFile);
        BufferedReader in = new BufferedReader(fr);
        String line;

        try {
            while ((line = in.readLine()) != null) {
                // first find all the atoms
                String[] stringArray = line.split("\\s");

                if (stringArray[0].equals("ATOM")) {
                    atomsList.add(stringArray[1]);
                } else if (stringArray[0].equals("PREFIX")) {
                    // add this prefix to the HashMap
                    hashMap.put(stringArray[1], stringArray[2]);
                } else {
                    // skip
                }
            }
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fr != null) fr.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // reading is done
        // now calling loadDump with List of prefixes
        logger.info("Sending all prefixes to CBGP");
        loadDump(BGPbaseDirectory, BGPdirFileName, atomsList);
        logger.info("Done");

        return hashMap;
    }



    /**
     * This methods takes a CBGP instance to which all messages (dumps & updates) have been passed and generates an intra-domain traffic matrix
     * from an interdomain XML traffic matrix.
     *
     * @param tm       this parameter can be an existing traffic matrix to which you want to add new traffic (for example to generate a traffic matrix for two hours...)
     * @param clusters an hashMap mapping a prefix to a cluster (which has been advertised in C-BGP)
     * @param interTm  the inter-domain traffic matrix
     * @return
     * @throws InvalidDomainException
     * @throws InvalidTrafficMatrixException
     */
    public TrafficMatrix generateTrafficMatrix(TrafficMatrix tm, HashMap<String, String> clusters, TrafficMatrixFileType interTm) throws InvalidDomainException, InvalidTrafficMatrixException {
        notRoutedTraffic = 0;

        // check if ASID is ok
        if (interTm.getInterTM().getASID() != domain.getASID()) {
            throw new InvalidDomainException("Inter-domain traffic matrix has not been measured on the correct AS!");
        }

        List<InterTMType.NodeType> nodesList = interTm.getInterTM().getNode();
        // creating the intra-domain traffic matrix

        BandwidthUnits interBwUnits = null;
        try {
            interBwUnits = interTm.getInfo().getUnits().getUnit().getValue();
            if (tm != null) {
                BandwidthUnit intraUnit = tm.getUnit();
                if (intraUnit == null || intraUnit == BandwidthUnit.DEFAULT_UNIT)
                    intraUnit = domain.getBandwidthUnit();
                if (!interBwUnits.getValue().toUpperCase().equals(intraUnit.toString())) {
                    //TODO : add conversion rather than throwing an exception
                    throw new InvalidTrafficMatrixException("Units in interDomain traffic Matrix does not correspond to domain in the intra TM matrix");
                }
            }
        } catch (NullPointerException e) {
            //units not found
        }

        if (tm == null) {
            tm = new TrafficMatrixImpl(domain.getASID());
        }

        boolean stop = true;
        for (int j = 0; j < nodesList.size() && stop; j++) {
            String nodeId = nodesList.get(j).getId();
            try {
                domain.getNode(nodeId);
            } catch (NodeNotFoundException e) {
                throw new InvalidTrafficMatrixException("One of the nodes in the inter-domain traffic matrix is not present in the domain concerned!");
            }

            // not optimal way, we'll just look at all destination prefix and ask for all of them
            // would be interesting to have either a intra-interdomain traffic matrix
            // where "src" is a node ID and "dst" a prefix; or at least to store the next-hop
            // for a given prefix on a given node from the concerned AS.

            List<InterTMType.NodeType.SrcType> srcTypeList = nodesList.get(j).getSrc();
            for (int k = 0; k < srcTypeList.size() && stop; k++) {
                List<InterTMType.NodeType.SrcType.DstType> dstTypeList = srcTypeList.get(k).getDst();

                for (int l = 0; l < dstTypeList.size(); l++) {
                    float bwValue = dstTypeList.get(l).getValue();
                    String prefix = dstTypeList.get(l).getPrefix();
                    //finding the egress node for the network
                    //System.out.println("Noeud : " +domain.getNode(nodeId).getRid() + "Value " + bwValue);
                    //System.out.println("Searching the next hop for the prefix : " + prefix);

                    String clusterPrefix = null;

                    try {
                        if ((clusterPrefix = clusters.get(prefix)) == null) {
                            notRoutedTraffic += bwValue;
                            logger.error("Error, it appears that this prefix is not part of one of the clusters! " + prefix + " (" + bwValue + ")");
                            continue;
                        }


                        Vector<Route> bgpRoutes = cbgp.bgpRouterGetRib(domain.getNode(nodeId).getRid(), clusterPrefix);

                        if (bgpRoutes.size() > 1) {
                            logger.warn("Multiple routes for prefix: " + clusterPrefix + " at node " + nodeId + " Choosing first one!");
                        }

                        if (bgpRoutes.size() == 0) {
                            //throw new Exception("No routes for this prefix!, aborting");
                            logger.warn("No routes for prefix: " + clusterPrefix + " at node " + nodeId + " Skipping!");

                        } else {
                            //System.out.println("OK traffic to prefix " + clusterPrefix + " has been routed (" + bwValue + ")");
                            IPAddress ipAddress = bgpRoutes.get(0).getNexthop();

                            String rid = ipAddress.toString();

                            //System.out.println("Valeur du RID: " + rid);

                            // check if this next-hop is the concerned domain
                            // not optimal too!
                            // if it is, add it this flow to the intra-domain traffic matrix
                            try {
                                Node node = domain.getNodeByRid(rid);
                                tm.set(nodeId, node.getId(), tm.get(nodeId, node.getId()) + bwValue);
                            } catch (NodeNotFoundException e) {
                                //System.out.println("Next-hop " + rid + " not found in Geant, setting next-hop to the node itself! " + nodeId);
                                tm.set(nodeId, nodeId, tm.get(nodeId, nodeId) + bwValue);
                            }
                        }
                    } catch (NodeNotFoundException e) {
                        e.printStackTrace();
                    } catch (RoutingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // if the units were set in the inter tm, set the same unit to the intra tm
        if (interBwUnits != null)
            tm.setUnit(BandwidthUnit.valueOf(interBwUnits.getValue().toUpperCase()));
        return tm;
    }


    /**
     * This methods takes a CBGP instance to which all messages (dumps & updates) have been passed and generates an intra-domain traffic matrix
     * from an interdomain XML traffic matrix.
     *
     * @param tm      this parameter can be an existing traffic matrix to which you want to add new traffic (for example to generate a traffic matrix for two hours...)
     * @param clusters an hashMap mapping a prefix to a cluster (which has been advertised in C-BGP)
     * @param netflowXMLTrafficMatrixFileName
     *                the inter-domain XML traffic matrix generated using NetFlow
     * @return
     * @throws InvalidDomainException
     * @throws InvalidTrafficMatrixException
     */
    public TrafficMatrix generateTrafficMatrix(TrafficMatrix tm, HashMap<String, String> clusters, String netflowXMLTrafficMatrixFileName) throws InvalidDomainException, InvalidTrafficMatrixException {
        TrafficMatrixFileType tmFile = null;
        try {
            tmFile = TrafficMatrixFactory.loadInterDomainMatrix(netflowXMLTrafficMatrixFileName);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new InvalidTrafficMatrixException("File not in the right format");
        }
        return generateTrafficMatrix(tm, clusters, tmFile);
    }

    /**
     * This methods loads all the updates corresponding to a certain time interval into a specified CBGP instance.  // TODO: update this method to get it working
     *
     * @param updatesBaseDirectory the base directory containing updates, then stored in subdirectories according routers names
     * @param YYYYMMDDHHMM         updates should be stored in files updatesYYYYMMDDHHMM
     * @throws Exception
     */
    public void loadUpdates(String updatesBaseDirectory, String YYYYMMDDHHMM) throws Exception {
        List<Node> nodes = domain.getAllNodes();
        String id = nodes.get(0).getId();

        boolean idNaming = false;

        File tempFile = new File(updatesBaseDirectory + "/" + id);
        if (tempFile.exists() && tempFile.isDirectory()) {
            idNaming = true;
            // directories named according id
        } else {
            id = nodes.get(0).getRid();
            tempFile = new File(updatesBaseDirectory + "/" + id);
            if (tempFile.exists() && tempFile.isDirectory()) {
                // directories named according rid
                idNaming = false;
            } else {
                throw new Exception("Sorry directories have to be named according routers id or rid!, aborting");
            }
        }

        // Now trying opening corresponding dumps which must be in MRT ASCII MACHINE READABLE FORMAT

        tempFile = new File(updatesBaseDirectory + "/" + id + "/" + "updates" + YYYYMMDDHHMM);
        if (!tempFile.exists()) {
            throw new Exception("MRT file not found!  For router " + id + " it should be in directory " + updatesBaseDirectory + "/" + id + "/" + " and named: updates " + YYYYMMDDHHMM);
        }


        // Little test with regular expressions (to enhance) to see if format is MRT ASCII READABLE FORMAT
        FileReader fr = new FileReader(tempFile);
        BufferedReader in = new BufferedReader(fr);
        String line;
        if ((line = in.readLine()) != null) {
            Pattern urlPattern = Pattern.compile("\\|[A-Za-z]+\\|((\\d{1,3}\\.?){4,4})\\|\\d+\\|[\\d\\./]*\\|(\\d+)(\\s\\d+)*\\|[A-Za-z]+\\|((\\d{1,3}\\.?){4,4})\\|\\d+\\|");
            Matcher matcher = urlPattern.matcher(line);
            if (matcher.find()) {
                if (matcher.group(1) == null || matcher.group(3) == null || matcher.group(5) == null) {
                    throw new Exception("File seems to be in MRT ASCII MACHINE READABLE FORMAT but not some fields seem missing");
                }
            } else
                throw new Exception("File not in MRT ASCII MACHINE READABLE FORMAT! Use route_btoa utily to convert it!");
        } else
            throw new Exception("File is empty!");


        // Now sending all messages to the CBGP instance ;-)
        for (int i = 0; i < nodes.size(); i++) {
            if (idNaming == true) {
                id = nodes.get(i).getId();
            } else
                id = nodes.get(i).getRid();
            tempFile = new File(updatesBaseDirectory + "/" + id + "/" + "updates" + YYYYMMDDHHMM);
            if (!tempFile.exists()) {
                System.out.println("Pay attention: MRT file not found for router " + id);
                //throw new Exception("MRT file not found!  For router "+ id + " it should be in directory " + baseDirectory + "/" + id + "/" + " and named: rib " + YYYYMMDDHHMM);
                continue;
            }


            fr = new FileReader(tempFile);
            in = new BufferedReader(fr);
            while ((line = in.readLine()) != null) {

                Pattern urlPattern = Pattern.compile("\\|[A-Za-z]+\\|((\\d{1,3}\\.?){4,4})\\|\\d+\\|[\\d\\./]*\\|(\\d+)(\\s\\d+)*\\|[A-Za-z]+\\|((\\d{1,3}\\.?){4,4})\\|\\d+\\|");
                Matcher matcher = urlPattern.matcher(line);
                if (matcher.find()) {
                    if (!matcher.group(1).equals(nodes.get(i).getRid()))
                        throw new Exception("Peer IP seems different from router ID!, aborting");

                    String peerIP = matcher.group(5);

                    System.out.println("Sending msg to : " + nodes.get(i).getRid() + " through peer " + peerIP + " msg " + line);
                    cbgp.bgpRouterPeerRecv(nodes.get(i).getRid(), peerIP, line);
                } else {
                    System.out.println("Other line with unrecognized format, skipping");
                    // withdraw message typically
                }
            }
        }

    }

    public Domain getDomain() {
        return domain;
    }
}
