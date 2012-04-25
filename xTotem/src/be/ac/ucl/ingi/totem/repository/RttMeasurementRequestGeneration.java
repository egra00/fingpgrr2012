package be.ac.ucl.ingi.totem.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import be.ac.ucl.ingi.cbgp.IPAddress;
import be.ac.ucl.ingi.cbgp.bgp.Route;
import be.ac.ucl.ingi.totem.measurementRequest.model.jaxb.DnsServer;
import be.ac.ucl.ingi.totem.measurementRequest.model.jaxb.ObjectFactory;
import be.ac.ucl.ingi.totem.measurementRequest.model.jaxb.RttMeasurementRequest;
import be.ac.ucl.ingi.totem.measurementRequest.model.jaxb.RttMeasurementRequestType;
import be.ac.ucl.ingi.totem.measurementRequest.model.jaxb.Subnet;
import be.ac.ucl.ingi.totem.measurementRequest.model.jaxb.RttMeasurementRequestType.RequestListType;
import be.ac.ucl.ingi.totem.measurementRequest.model.jaxb.RttMeasurementRequestType.RequestListType.RequestType;
import be.ac.ucl.ingi.totem.measurementRequest.model.jaxb.RttMeasurementRequestType.RequestListType.SubnetDNSType;
import be.ac.ucl.ingi.totem.measurementRequest.model.jaxb.RttMeasurementRequestType.RequestListType.RequestType.PoissonParametersType;
import be.ac.ulg.montefiore.run.totem.domain.exception.DomainAlreadyExistException;
import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.RoutingException;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.generation.POPPOPTrafficMatrixGeneration;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.InterTMType;
import be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb.TrafficMatrixFileType;

/**
 * 
 * The RttMeasurementRequestGeneration class is used to generate RttMeasurementRequest from a given domain.
 * Given a domain to which all bgp message have been passed and for which we have the cluster data as specified
 * in POPPOPTrafficMatrixGeneration, given a association of subnet list and dns server and given a traffic matrix containing
 * information for egress data and without transit taffic. The class is used to calculate, for each egress node,
 * the data that will transit by that node and to create a measurement request for these egress nodes, with requests 
 * to probe all subnets to which data will exit from that node.
 * 
 * 
 * 
 * @author Thomas Dekens
 *          Simon Balon (update to comply with the new CBGP JNI interface)
 *         <p>
 *         creation 16 mar. 2007
 * 
 */

public class RttMeasurementRequestGeneration {

	Calendar startTime = Calendar.getInstance();

	boolean poissonDistribution = false, takeFirstMeasureDirectly = true,
			randomizeDNSInList = false;

	int numberOfObservations = 1, numberOfQueries = 1;

	double lamba = 1;

	String timingUnits = "s", measurementMethod = "dnsQuery";
	
	
	private static final String[] subSizeToMask={"0.0.0.0","128.0.0.0", "192.0.0.0","224.0.0.0","240.0.0.0", "248.0.0.0", "252.0.0.0", "254.0.0.0",
		"255.0.0.0","255.128.0.0","255.192.0.0","255.224.0.0","255.240.0.0","255.248.0.0","255.252.0.0","255.254.0.0",
		"255.255.0.0","255.255.128.0","255.255.192.0","255.255.224.0","255.255.240.0","255.255.248.0","255.255.252.0","255.255.254.0",
		"255.255.255.0","255.255.255.128","255.255.255.192","255.255.255.224","255.255.255.240","255.255.255.248","255.255.255.252","255.255.255.254", "255.255.255.255"};

	/**
	 * Given the specified domain, interdomain traffic matrix and instance of
	 * cbgp corresponding to the domain and to which all messages have been
	 * passed, this method creates directory outputBaseDirectoyr if it doesn't
	 * exist and creates for each egress router in the domain a sub directory
	 * containing measurements requests. The method also creates directories for
	 * the results and stores data later used to interpretate the results.
	 * 
	 * 
	 * This method is partly based on
	 * be.ac.ulg.montefiore.run.totem.trafficMatrix.generation.POPPOPTraffixMatrixGeneration.generateTrafficMatrix
	 * by O. Delcourt and S.Balon
	 * 
	 * TODO throw exceptions rather than catch and return null
	 * TODO due to a Jaxb problem, output of marshalling had to be filtered to enable correct 
	 * subsequent unmarshalling, as explained at the end. This might be fixed with newer versions of jaxb.
	 * 
	 * @param domain
	 *            the concerned domain
	 * @param netflowTrafficMatrixFilename
	 *            the inter-domain XML traffic matrix generated using NetFlow,
	 *            without transit traffic to other nodes and for each flow, src
	 *            being a local destinatio and dst a distant one, the bandwidth
	 *            is the sum of the bandwidth in both directions
	 * @param subnetDNS
	 *            a hashtable of type <String, String[2]> linking a subnet to a
	 *            {dnsName, dnsAddress} in that subnet, or close to that subnet
	 * @param cbgpInstance
	 *            an instance of CBGP
	 * @param outputBaseDirectory
	 *            the output directory
	 */
	public void generateRttMeasurementRequests(Domain domain,
			String netflowTrafficMatrixFilename,
			Hashtable<String, String[]> subnetDNS, CBGP cbgpInstance,
			HashMap clust, String outputBaseDirectory) {

		// create outputBaseDirectory, check for write access. otherwise useless
		// to work
		File tmpFile = new File(outputBaseDirectory);

		if (!tmpFile.isDirectory()) {
			if (!tmpFile.mkdir()) {
				System.out.println("invalid directory " + outputBaseDirectory);
				return;
			}
		}
		if (!tmpFile.canWrite()) {
			System.out.println("cannot write to directory "
					+ outputBaseDirectory);
			return;
		}

		// create temp storage for the data for each egress node (subnet to
		// ping)
		Hashtable<String, HashSet<String>> egress = new Hashtable<String, HashSet<String>>(
				domain.getNbNodes());

		// create temp storage for the data for each source node, the prefix to
		// ping and the egress router used + weight
		Hashtable<String, Hashtable<String, EgressWeight>> nodeOut = new Hashtable<String, Hashtable<String, EgressWeight>>(
				domain.getNbNodes());

		// get InterTM, check that it's an interTM and for this AS

		JAXBContext jc;
		TrafficMatrixFileType tmFile;
		try {
			jc = JAXBContext
					.newInstance("be.ac.ulg.montefiore.run.totem.trafficMatrix.model.jaxb");
			Unmarshaller u = jc.createUnmarshaller();
			tmFile = (TrafficMatrixFileType) u.unmarshal(new File(
					netflowTrafficMatrixFilename));

		} catch (JAXBException e) {
			System.out.println("Error unmarshalling file: "
					+ netflowTrafficMatrixFilename);
			return;
		}

		if (!tmFile.isSetInterTM()) {
			System.out.println("File " + netflowTrafficMatrixFilename
					+ " is not an XML inter-domain traffic matrix!");
			return;
		}

		if (tmFile.getInterTM().getASID() != domain.getASID()) {
			System.out.println("Traffic matrix in file "
					+ netflowTrafficMatrixFilename
					+ " is not for the AS of specified domain");
			return;
		}

		// get nodes
		List<InterTMType.NodeType> nodesList = tmFile.getInterTM().getNode();

		// go through nodes in interTM, check that dest is in subnetDNSFile,
		// find a route for that dest, add resquest in list for that egress
		String curNode;
		List<InterTMType.NodeType.SrcType> srcList;
		List<InterTMType.NodeType.SrcType.DstType> dstList;
		String clustPrefix, dst;
		Vector<Route> bgpRoutes;
		List<Node> nodes = domain.getAllNodes();
		for (int i = 0; i < nodesList.size(); i++) {
			if (nodesList.get(i).isSetId()) {
				curNode = nodesList.get(i).getId();
			} else {
				curNode = "";
			}
			try {
				domain.getNode(curNode);
			} catch (NodeNotFoundException e) {
				System.out.println("Node " + curNode
						+ " present in the inter-domain traffic matrix "
						+ netflowTrafficMatrixFilename
						+ " is not present in the domain");
				return;
			}
			
			//ht is hashtable of {subnet, EgressWeight} for the current node. used for easer lookup afterwards 
			Hashtable<String, EgressWeight> ht = nodeOut.get(curNode);
			if(ht == null){
				ht = new Hashtable<String, EgressWeight>();
				nodeOut.put(curNode, ht);
			}
			

			if (nodesList.get(i).isSetSrc()) {
				srcList = nodesList.get(i).getSrc();
				for (int j = 0; j < srcList.size(); j++) {
					if (srcList.get(j).isSetDst()) {
						dstList = srcList.get(j).getDst();
						for (int k = 0; k < dstList.size(); k++) {
							if (dstList.get(k).isSetPrefix()) {
								dst = dstList.get(k).getPrefix();
								clustPrefix = (String) clust.get(dst);
								if(subnetDNS.get(dst) != null){
								if (clustPrefix != null) {
									try {
										bgpRoutes = cbgpInstance
												.bgpRouterGetRib(domain
														.getNode(curNode)
														.getRid(), clustPrefix);

										if (bgpRoutes.size() > 0) {
											if (bgpRoutes.size() > 1) {
												System.out
														.println("Multiple routes to clustered prefix "
																+ clustPrefix
																+ " from Node "
																+ curNode
																+ " will take first one");
											}

											IPAddress ipAddress = bgpRoutes
													.get(0).getNexthop();
											String rid = ipAddress.toString();
											String nodeID=null;
											boolean found = false;
											for (int l = 0; l < nodes.size(); l++) {
												if (nodes.get(l).getRid()
														.equals(rid)) {
													nodeID = nodes.get(l).getId();
													found = true;
													break;
												}
											}

											// rid not found -> next-hop not in
											// domain -> sent from here (this is egress node)
											if (!found) {
												nodeID = curNode;
											}
											
											HashSet<String> hs = egress.get(nodeID);
											if(hs==null){
												hs = new HashSet<String>();
												egress.put(nodeID,hs);
											}
											hs.add(dst);
											
											EgressWeight ew = ht.get(dst);
											if(ew == null){
												ew = new EgressWeight(nodeID, dstList.get(k).getValue());
												ht.put(dst, ew);
											}else{
												ew.addWeight(dstList.get(k).getValue());
											}
											

										} else {
											System.out
													.println("No route for clustered prefix "
															+ clustPrefix
															+ " from node "
															+ curNode);
										}
									} catch (NodeNotFoundException e) {
										System.out.println("Node not found "
												+ curNode);
									} catch (RoutingException e) {
										System.out.println("Routing exception");
										e.printStackTrace();
										return;
									}
								} else {
									System.out.println("Prefix "
											+ dstList.get(k).getPrefix()
											+ " not present in cluster list");
								}}
								else{
									System.out.println("no dsn server for " + dst);
								}
							}
						}
					}
				}
			}
		}

		// for each egress node, create a measurementRequest in distinct
		// directory
		ObjectFactory factory = new ObjectFactory();
		Enumeration<String> egressNodesEnum = egress.keys();
		String currentEgressNode;
		RttMeasurementRequest complexMeasurementRequest;
		RttMeasurementRequestType measurementRequest;
		List<RequestListType> reqList;
		RequestListType reqListType;
		Iterator<String> subnetIter;
		String subnet;
		SubnetDNSType subdns; 
		Subnet sub;
		DnsServer dns;
		StringTokenizer st;
		String[] dnsInfo;
		RequestType requestType;
		JAXBContext jContext;
		Marshaller marshaller;
		try {
			jContext = JAXBContext.newInstance("be.ac.ucl.ingi.totem.measurementRequest.model.jaxb");
			marshaller = jContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
					Boolean.TRUE);

			requestType = generateRequestType();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		while (egressNodesEnum.hasMoreElements()) {

			currentEgressNode = egressNodesEnum.nextElement();

			tmpFile = new File(outputBaseDirectory + "/requests/"
					+ currentEgressNode);
			if (tmpFile.mkdirs()) {
				tmpFile = new File(tmpFile, "measurementRequest.xml");
				try {
					tmpFile.createNewFile();
					try {
						complexMeasurementRequest = factory
								.createRttMeasurementRequest();
						measurementRequest = factory
								.createRttMeasurementRequestType();
						
						complexMeasurementRequest.getRequestList().add(
								measurementRequest);
						

						
						reqList = measurementRequest.getRequestList();
				
						subnetIter = egress.get(currentEgressNode).iterator();
						reqListType = factory
								.createRttMeasurementRequestTypeRequestListType();
						reqList.add(reqListType);
						File bufferFile = File.createTempFile("rtt", null);
						


						while (subnetIter.hasNext()) {
							subnet = subnetIter.next();
							subdns = factory
									.createRttMeasurementRequestTypeRequestListTypeSubnetDNSType();
							sub = factory.createSubnet();
							st = new StringTokenizer(subnet, "/");
							sub.setAddress(st.nextToken());
							sub.setMask(subSizeToMask[Integer.parseInt(st.nextToken())]);
							subdns.setSubnet(sub);
							dns = factory.createDnsServer();
							// over here, we know that there is a dns server for
							// subnet in subnetDNS hashtable
							dnsInfo = subnetDNS.get(subnet);
							dns.setDnsName(dnsInfo[0]);
							dns.setDnsAddress(dnsInfo[1]);
							subdns.getDnsServer().add(dns);
							reqListType.getSubnetDNS().add(subdns);
						}
						reqListType.getRequest().add(requestType);

						marshaller.marshal(complexMeasurementRequest,
								new FileOutputStream(bufferFile, false));
						
						/*
						 * Problem with marshaller who adds an extra <requestList> and </requestList>
						 * at beginning and end of xml, can't seem to find how to fix this, jaxb 2.0
						 * might fix this. Will filter for now.
						 */
						BufferedReader br = new BufferedReader(new FileReader(bufferFile));
						BufferedWriter bw = new BufferedWriter(new FileWriter(tmpFile));
						String l1=null, l2;
						boolean foundRL = false;
						while(!foundRL){
							l1 = br.readLine();
							if(l1.trim().startsWith("<requestList>")){
								foundRL = true;
							}else{
								bw.write(l1);
								bw.newLine();
							}
						}
						foundRL = false;
						l1 = br.readLine();
						while(!foundRL){
							l2 = l1;
							l1 = br.readLine();
							if(l1.trim().startsWith("</rttMeasurementRequest>")){
								bw.write(l1);
								bw.newLine();
								foundRL = true;
							}else{
								bw.write(l2);
								bw.newLine();
							}
						}
						l1 = br.readLine();
						while(l1 != null){
							bw.write(l1);
							bw.newLine();
							l1= br.readLine();
						}
						bw.close();
						br.close();
						

					} catch (JAXBException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						return;
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("unable to make directory ");
			}
		}

		// for each source node, create list of {subnetprefix, egressrouter,
		// weight} in distinct directory
		tmpFile = new File(outputBaseDirectory + "/nodes/");
		if (tmpFile.mkdir()) {
			Enumeration<String> nodeOutEnum = nodeOut.keys();
			BufferedWriter bw;
			while (nodeOutEnum.hasMoreElements()) {
				curNode = nodeOutEnum.nextElement();
				tmpFile = new File(outputBaseDirectory + "/nodes/" + curNode);
				if (tmpFile.mkdir()) {
					tmpFile = new File(tmpFile, "requests");
					try {
						tmpFile.createNewFile();
						bw = new BufferedWriter(new FileWriter(tmpFile));
						Hashtable<String, EgressWeight> ht = nodeOut
								.get(curNode);
						Enumeration<String> subnetEnum = ht.keys();
						while (subnetEnum.hasMoreElements()) {
							subnet = subnetEnum.nextElement();
							EgressWeight ew = ht.get(subnet);
							bw.write(subnet + " " + ew.getId() + " "
									+ ew.weight);
							bw.newLine();
						}
						bw.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("unable to create directory nodes/"
							+ curNode);
				}
			}
		} else {
			System.out.println("unable to create directory: nodes");
		}
	}

	private RequestType generateRequestType() throws JAXBException {
		ObjectFactory factory = new ObjectFactory();
		RequestType req = factory
				.createRttMeasurementRequestTypeRequestListTypeRequestType();
		req.setMeasurementMethod(measurementMethod);
		req.setNumberOfQueries(BigInteger.valueOf(numberOfQueries));
		req.setRandomizeDnsInList(randomizeDNSInList);
		req.setStartTime(startTime);
		PoissonParametersType poiss = factory.createRttMeasurementRequestTypeRequestListTypeRequestTypePoissonParametersType();
		poiss.setLambda(BigDecimal.valueOf(lamba));
		poiss.setNumberOfObservations(BigInteger.valueOf(numberOfObservations));
		poiss.setPoissonDistribution(poissonDistribution);
		poiss.setTakeFirstMeasureDirectly(takeFirstMeasureDirectly);
		poiss.setTimingUnits(timingUnits);
		req.setPoissonParameters(poiss);
		return req;
	}
	
	
	/**
	 * Reads the file at fileLoc of format "subnet dnsName dnsIP" and returns a hashtable {subnet, {dnsName, dnsIp}}
	 * @param fileLoc the location of the file containing lines of the type :"subnet dnsName dnsIP"
	 * @return a hashtable with items : {subnet, [dnsName, dnsIp]}
	 * @throws FileNotFoundException
     * @throws IOException
	 */
	public static Hashtable<String, String[]> readSubnetDnsFile(String fileLoc) throws FileNotFoundException, IOException{
		Hashtable<String, String[]> out = new Hashtable<String, String[]>(); 
		BufferedReader br = new BufferedReader(new FileReader(fileLoc));
		String line;
		StringTokenizer st;
		line = br.readLine();
		String[] val;
		String key;
		while(line != null){
			st = new StringTokenizer(line, "|");
			key = st.nextToken();
			val = new String[2];
			val[0] = st.nextToken();
			val[1] = st.nextToken();
			out.put(key, val);
			line = br.readLine();
		}
		br.close();
		return out;		
	}
	
	
	
	
	public static void main(String[] args) throws DomainAlreadyExistException, InvalidDomainException, AlgorithmInitialisationException, FileNotFoundException, Exception{
		/*
		//Totem.init();
		InterDomainManager idm = InterDomainManager.getInstance();
		idm.loadDomain("/biom/biom1/tdekens/workspace/totem/examples/abilene/cbgp/abilene_topo.xml", true, true);
		
		RepositoryManager rm = RepositoryManager.getInstance();
		rm.startAlgo("CBGP", null);
		
		
		ScenarioManager sm = ScenarioManager.getInstance();
		sm.loadScenario("/biom/biom1/tdekens/workspace/totem/examples/abilene/cbgp/abilene_100rbn.xml");
		sm.executeScenario();
		
		//ScenarioManager sm = ScenarioManager.getInstance();
		//sm.loadScenario("/biom/biom1/tdekens/workspace/totem/examples/abilene/cbgp/abilene_load_topo_scenario.xml");
		//sm.executeScenario();
		
		
		
		//Hashmap clust 
		*/
		String loc = "/biom/biom1/tdekens/workspace/totem/examples/abilene/cbgp/";
		InterDomainManager.getInstance().loadDomain(loc + "abilene_topo.xml", true, true);
		Domain domain = InterDomainManager.getInstance().getDefaultDomain();
		CBGP cbgp = new CBGP();
		cbgp.start(null);
		
		
		
		POPPOPTrafficMatrixGeneration pop = new POPPOPTrafficMatrixGeneration(domain);
		HashMap clust = pop.readCluster(loc+"bgp/cluster.txt", loc + "bgp/", "2005/2005-01/2005-01-01/rib.20050101");
		Hashtable<String, String[]> subnetDns = readSubnetDnsFile(loc+ "tmp/subs");
		RttMeasurementRequestGeneration rmrg = new RttMeasurementRequestGeneration();
		rmrg.generateRttMeasurementRequests(domain, loc +"trafficmatrix/interDomain.2005-01-01.0000.xml", subnetDns, cbgp, clust, loc + "tmp/");
		
		
	}

	private class EgressWeight {
		String id;

		float weight;

		public EgressWeight(String id, float weight) {
			this.id = id;
			this.weight = weight;
		}

		public float getWeight() {
			return weight;
		}

		public void addWeight(float f) {
			this.weight += f;
		}

		public String getId() {
			return id;
		}
	}

	/**
	 * @return the lamba
	 */
	public double getLamba() {
		return lamba;
	}

	/**
	 * @param lamba the lamba to set
	 */
	public void setLamba(double lamba) {
		this.lamba = lamba;
	}

	/**
	 * @return the measurementMethod
	 */
	public String getMeasurementMethod() {
		return measurementMethod;
	}

	/**
	 * @param measurementMethod the measurementMethod to set
	 */
	public void setMeasurementMethod(String measurementMethod) {
		this.measurementMethod = measurementMethod;
	}

	/**
	 * @return the numberOfObservations
	 */
	public int getNumberOfObservations() {
		return numberOfObservations;
	}

	/**
	 * @param numberOfObservations the numberOfObservations to set
	 */
	public void setNumberOfObservations(int numberOfObservations) {
		this.numberOfObservations = numberOfObservations;
	}

	/**
	 * Return the numbers of rtt measures that are taken at each observation
	 * @return the numberOfQueries
	 */
	public int getNumberOfQueries() {
		return numberOfQueries;
	}

	/**
	 * Sets the number of rtt measures that are taken at each observation
	 * @param numberOfQueries the numberOfQueries to set
	 */
	public void setNumberOfQueries(int numberOfQueries) {
		this.numberOfQueries = numberOfQueries;
	}

	/**
	 * @return the poissonDistribution
	 */
	public boolean isPoissonDistribution() {
		return poissonDistribution;
	}

	/**
	 * @param poissonDistribution the poissonDistribution to set
	 */
	public void setPoissonDistribution(boolean poissonDistribution) {
		this.poissonDistribution = poissonDistribution;
	}

	/**
	 * @return the randomizeDNSInList
	 */
	public boolean isRandomizeDNSInList() {
		return randomizeDNSInList;
	}

	/**
	 * @param randomizeDNSInList the randomizeDNSInList to set
	 */
	public void setRandomizeDNSInList(boolean randomizeDNSInList) {
		this.randomizeDNSInList = randomizeDNSInList;
	}

	/**
	 * @return the startTime
	 */
	public Calendar getStartTime() {
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	/**
	 * @return the takeFirstMeasureDirectly
	 */
	public boolean isTakeFirstMeasureDirectly() {
		return takeFirstMeasureDirectly;
	}

	/**
	 * @param takeFirstMeasureDirectly the takeFirstMeasureDirectly to set
	 */
	public void setTakeFirstMeasureDirectly(boolean takeFirstMeasureDirectly) {
		this.takeFirstMeasureDirectly = takeFirstMeasureDirectly;
	}

	/**
	 * @return the timingUnits
	 */
	public String getTimingUnits() {
		return timingUnits;
	}

	/**
	 * @param timingUnits the timingUnits to set
	 */
	public void setTimingUnits(String timingUnits) {
		this.timingUnits = timingUnits;
	}
}
