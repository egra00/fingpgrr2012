package be.ac.ucl.ingi.totem.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;


import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import be.ac.ucl.ingi.totem.measurementReport.model.jaxb.RttMeasurementReport;
import be.ac.ucl.ingi.totem.measurementReport.model.jaxb.Subnet;
import be.ac.ucl.ingi.totem.measurementReport.model.jaxb.SubnetMeasures;
import be.ac.ucl.ingi.totem.measurementReport.model.jaxb.SubnetMeasures.DnsServerMeasuresType;
import be.ac.ucl.ingi.totem.measurementReport.model.jaxb.SubnetMeasures.DnsServerMeasuresType.MeasureType;
import be.ac.ucl.ingi.totem.measurementReport.model.jaxb.SubnetMeasures.DnsServerMeasuresType.MeasureType.ResultType;

/**
 * 
 * The RttMeasurementReportInterpreter object represents the results of running the tests defined
 * using the RttMeasurementRequestGeneration. Specified with a directory, containing the results
 * and requests made, it analyzed the results with respect to the requests (IE: which nodes wanted to 
 * probe which subnet, and the importance of that subnet). It stores the results in lists for the
 * the whole topology and for each individual source node.
 * It contains a method to print out those lists in the directory to that the data can be used
 * in another location. 
 * 
 * @author Thomas Dekens
 *
 */
public class RttMeasurementReportInterpreter {

	public static final String requestDirName = "requests";

	public static final String nodesDirName = "nodes";

	public static final String resultsFilename = "results";

	public static final String reportFilename = "measurementReport.xml";

	public static final String requestFilename = "requests";
	
	public static final String AGGREGATE = "Aggregate";

	String baseDirName;

	Hashtable<String, List<RttResult>> results;

	List<RttResult> aggregatedResultList;
	
	String description;

	/**
	 * 
	 * @param baseDirName
	 * @throws JAXBException
	 */
	public RttMeasurementReportInterpreter(String baseDirName)
			throws JAXBException {

		this.baseDirName = baseDirName;
		description = baseDirName;

		File[] nodeDirs = (new File(baseDirName + "/" + requestDirName))
				.listFiles();
		Hashtable<String, SortedMap<Subnet, Result>> requests = new Hashtable<String, SortedMap<Subnet, Result>>(
				nodeDirs.length);
		Comparator<Subnet> comp = new SubnetComparator();
		SortedMap<Subnet, RttResult> aggregatedResults;

		// jaxb objects
		JAXBContext jContext = JAXBContext
				.newInstance("be.ac.ucl.ingi.totem.measurementReport.model.jaxb");
		Unmarshaller unmarshaller = jContext.createUnmarshaller();

		// objects from measurement reports
		RttMeasurementReport measurementRequests;
		Subnet subnet;
		Result result;
		Iterator<SubnetMeasures> subnetMeasuresIterator;
		SubnetMeasures subnetMeasures;
		Iterator<DnsServerMeasuresType> dnsServerMeasuresIterator;
		DnsServerMeasuresType dnsServerMeasures;
		Iterator<MeasureType> measureIterator;
		MeasureType measure;
		SortedMap<Subnet, Result> subnetResultMap;

		/*
		 * go through directories of egress nodes. Get results in xml file and
		 * add them to the requests hashtable
		 */
		for (int i = 0; i < nodeDirs.length; i++) {
			if (nodeDirs[i].isDirectory()) {
				subnetResultMap = new TreeMap<Subnet, Result>(comp);
				try {
					measurementRequests = (RttMeasurementReport) unmarshaller
							.unmarshal(new FileInputStream(new File(
									nodeDirs[i], reportFilename)));
					subnetMeasuresIterator = measurementRequests.getMeasures()
							.iterator();
					while (subnetMeasuresIterator.hasNext()) {
						subnetMeasures = subnetMeasuresIterator.next();
						subnet = subnetMeasures.getSubnet();
						result = subnetResultMap.get(subnet);
						if (result == null) {
							result = new Result();
							subnetResultMap.put(subnet, result);
						}
						dnsServerMeasuresIterator = subnetMeasures
								.getDnsServerMeasures().iterator();
						while (dnsServerMeasuresIterator.hasNext()) {
							dnsServerMeasures = dnsServerMeasuresIterator
									.next();
							measureIterator = dnsServerMeasures.getMeasure()
									.iterator();
							while (measureIterator.hasNext()) {
								measure = measureIterator.next();
								result.add(measure.getResult());
							}
						}
					}
				} catch (FileNotFoundException e) {
					System.out.println("Report file " + reportFilename
							+ " not found in directory " + nodeDirs[i]);
				} catch (JAXBException e) {
					System.out
							.println("Jaxb exception unmarshalling report file in directory: "
									+ nodeDirs[i]);
				}
				requests.put(nodeDirs[i].getName(), subnetResultMap);
			}
		}
		/*
		 * We now go through the source dirs to see the RTTs they requested and
		 * store results concerning those.
		 */
		nodeDirs = (new File(baseDirName + "/" + nodesDirName)).listFiles();
		StringTokenizer st;
		BufferedReader br;
		String curLine;
		String egress;
		ABCDXSubnet sub;
		double weight;
		results = new Hashtable<String, List<RttResult>>(nodeDirs.length);
		aggregatedResults = new TreeMap<Subnet, RttResult>(comp);
		List<RttResult> rttResultList;
		RttResult aggRttResult;
		RttResult curRttResult;

		for (int i = 0; i < nodeDirs.length; i++) {
			if (nodeDirs[i].isDirectory()) {
				try {
					rttResultList = new LinkedList<RttResult>();
					br = new BufferedReader(new FileReader(new File(
							nodeDirs[i], requestFilename)));
					curLine = br.readLine();
					while (curLine != null) {
						st = new StringTokenizer(curLine);
						sub = new ABCDXSubnet(st.nextToken());
						egress = st.nextToken();
						weight = Double.valueOf(st.nextToken());
						subnetResultMap = requests.get(egress);
						if (subnetResultMap != null) {
							result = subnetResultMap.get(sub);
							if (result != null) {
								curRttResult = new RttResult(sub.getABCDX(),
										weight, result);
								rttResultList.add(curRttResult);
								aggRttResult = aggregatedResults.get(sub);
								if (aggRttResult == null) {
									aggregatedResults.put(sub, (RttResult)curRttResult.clone());
								} else {
									aggRttResult.aggregate(curRttResult);
								}

							} else {
								System.out
										.println("No rtt results from egress node "
												+ egress
												+ " for subnet "
												+ sub.getABCDX());
							}
						} else {
							System.out.println("no results from egress node "
									+ egress);
						}
						curLine = br.readLine();
					}
					results.put(nodeDirs[i].getName(), rttResultList);
				} catch (FileNotFoundException e) {
					System.out.println("Could not find request file: "
							+ requestFilename + " in directory " + nodeDirs[i]);
				} catch (IOException e) {
					System.out.println("Error reading file " + requestFilename
							+ " in directory " + nodeDirs[i]);
				}
			}
		}
		aggregatedResultList = new LinkedList<RttResult>(aggregatedResults
				.values());
	}
	
	/**
	 * This method is used to retrieve the List of RttResults for a specified node. If the specified node
	 * is null then the aggregate of all the RttResults contained in this object is returned. 
	 * @param node the name of the node for which the list of RttResult is requested
	 * @return the aggregate RttResult for this object if node is null, the list of RttResult corresponding
	 * to node if it exists or null if this node does not have RttResults
	 */
	public List<RttResult> getRttResult(String node){
		if(node.equals(AGGREGATE)){
			return aggregatedResultList;
		}
		return results.get(node);
	}
	
	public Set<String> getNodeNames(){
		return results.keySet();
	}
/**
 * This method prints out the results represented by RTTMeasurementReportInterpreter in the 
 * directories for each node and in the base directory for the aggregate data in the file specified by
 * REPORT_FILENAME
 * 
 * The file contains a row for each subnet with columns of the tye:
 * SUBNET WEIGHT AVGRTT MAXRTT MINRTT MAD AVGStandardDeviation NUM SUC
 * 
 * NUM and SUC are not relevant in the agregate format
 *
 */
	public void writeResults() {

		BufferedWriter bw;
		Enumeration<String> keyEnum = results.keys();
		String node;
		Iterator<RttResult> resultIter;

		while (keyEnum.hasMoreElements()) {
			node = keyEnum.nextElement();
			try {
				bw = new BufferedWriter(new FileWriter(baseDirName + "/"
						+ nodesDirName + "/" + node+"/"+ resultsFilename));
				resultIter = results.get(node).iterator();
				while (resultIter.hasNext()) {
					bw.write(resultIter.next().toString());
					bw.newLine();
				}
				bw.close();
			} catch (IOException e) {
				System.out.println("Could not write to file " + baseDirName
						+ "/" + nodesDirName + "/" + resultsFilename);
			}

		}
		
		resultIter = aggregatedResultList.iterator();
		try{
			bw = new BufferedWriter(new FileWriter(baseDirName + "/" + resultsFilename));
			while (resultIter.hasNext()) {
				bw.write(resultIter.next().toString());
				bw.newLine();
			}
			bw.close();
		}catch(IOException e ){
			System.out.println("Could not write to file " + baseDirName + "/" + resultsFilename);
		}
	}

	public static void main(String[] args) throws JAXBException {
		RttMeasurementReportInterpreter r = new RttMeasurementReportInterpreter("/biom/biom1/tdekens/workspace/totem/examples/abilene/cbgp/tmp");
		r.writeResults();
		
	}

	public class RttResult implements Cloneable {
		


		double weight;

		String subnet;

		double average, min, max, mad, avgsd;

		int num, suc;

		public double getAverage() {
			return average;
		}

		public double getAvgsd() {
			return avgsd;
		}

		public double getMad() {
			return mad;
		}

		public double getMax() {
			return max;
		}

		public double getMin() {
			return min;
		}

		public int getNum() {
			return num;
		}

		public String getSubnet() {
			return subnet;
		}

		public int getSuc() {
			return suc;
		}

		public double getWeight() {
			return weight;
		}
		
		public Object clone(){
			return new RttResult(this);
		}
		
		private RttResult(RttResult rtt){
			this.subnet = rtt.getSubnet();
			this.weight = rtt.getWeight();
			this.average = rtt.getAverage();
			this.min = rtt.getMin();
			this.max = rtt.getMax();
			this.mad = rtt.getMad();
			this.avgsd = rtt.getAvgsd();
			this.num = rtt.getNum();
			this.suc= rtt.getSuc();
		}

		public RttResult(String subnet, double weight, Result result) {
			this.subnet = subnet;
			this.weight = weight;

			average = result.getAverage();
			min = result.getMin();
			max = result.getMax();
			mad = result.getMad();
			avgsd = result.getAvgsd();
			num = result.getNum();
			suc = result.getSuc();

		}

		public void aggregate(RttResult res) {
			double totWeight = weight + res.getWeight();
			double w1, w2;
			w1 = weight / totWeight;
			w2 = res.weight / totWeight;
			weight = totWeight;
			average = average * w1 + res.getAverage() * w2;
			mad = mad * w1 + res.getMad() * w2;
			avgsd = avgsd * w1 + res.getAvgsd() * w2;
			num = num + res.getNum();
			suc = suc + res.getSuc();
			max = Math.max(max, res.getMax());
			min = Math.min(min, res.getMin());
		}
		
		public String toString(){
			return subnet +" "+ weight +" "+ average +" "+ max +" "+ min +" "+ mad +" "+ avgsd +" "+ num +" "+ suc;
		}
	}

	private class Result {

		double average, min, max, mad, avgsd;

		int num, suc;

		public Result(ResultType rt) {
			average = rt.getAverage().doubleValue();
			min = rt.getMinimum().doubleValue();
			max = rt.getMaximum().doubleValue();
			mad = rt.getMeanAbsoluteDeviation().doubleValue();
			avgsd = rt.getStandardDeviation().doubleValue();
			num = rt.getNumberOfObservations().intValue();
			suc = rt.getSuccessfulObservations().intValue();

		}

		public Result() {
			average = 0;
			min = Double.MAX_VALUE;
			max = Double.MIN_VALUE;
			mad = 0;
			avgsd = 0;
			num = 0;
			suc = 0;
		}

		public void add(ResultType rt) {
			int rtsuc = rt.getSuccessfulObservations().intValue();
			average = (average) * suc / (suc + rtsuc)
					+ (rt.getAverage().doubleValue()) * rtsuc / (suc + rtsuc);
			min = Math.min(min, rt.getMinimum().doubleValue());
			max = Math.max(max, rt.getMaximum().doubleValue());
			mad = (mad) * suc / (suc + rtsuc)
					+ (rt.getMeanAbsoluteDeviation().doubleValue()) * rtsuc
					/ (suc + rtsuc);
			avgsd = (avgsd) * suc / (suc + rtsuc)
					+ (rt.getStandardDeviation().doubleValue()) * rtsuc
					/ (suc + rtsuc);
			num += rt.getNumberOfObservations().intValue();
			suc += rt.getSuccessfulObservations().intValue();
		}

		public Double getAverage() {
			return average;
		}

		public Double getAvgsd() {
			return avgsd;
		}

		public Double getMad() {
			return mad;
		}

		public Double getMax() {
			return max;
		}

		public Double getMin() {
			return min;
		}

		public int getNum() {
			return num;
		}

		public int getSuc() {
			return suc;
		}
	}

	private class ABCDXSubnet implements Subnet {

		String address;

		String mask;

		String abcdx;

		public ABCDXSubnet(String sub) {
			final String[] subSizeToMask = { "0.0.0.0", "128.0.0.0",
					"192.0.0.0", "224.0.0.0", "240.0.0.0", "248.0.0.0",
					"252.0.0.0", "254.0.0.0", "255.0.0.0", "255.128.0.0",
					"255.192.0.0", "255.224.0.0", "255.240.0.0", "255.248.0.0",
					"255.252.0.0", "255.254.0.0", "255.255.0.0",
					"255.255.128.0", "255.255.192.0", "255.255.224.0",
					"255.255.240.0", "255.255.248.0", "255.255.252.0",
					"255.255.254.0", "255.255.255.0", "255.255.255.128",
					"255.255.255.192", "255.255.255.224", "255.255.255.240",
					"255.255.255.248", "255.255.255.252", "255.255.255.254",
					"255.255.255.255" };
			abcdx = sub;
			StringTokenizer st = new StringTokenizer(sub, "/", false);
			address = st.nextToken();
			mask = subSizeToMask[Integer.valueOf(st.nextToken())];
		}

		public String getAddress() {
			// TODO Auto-generated method stub
			return address;
		}

		public String getMask() {
			// TODO Auto-generated method stub
			return mask;
		}

		public void setAddress(String value) {
			// TODO Auto-generated method stub
			this.address = value;

		}

		public void setMask(String value) {
			// TODO Auto-generated method stub
			mask = value;

		}

		public String getABCDX() {
			return abcdx;
		}

	}

	private class SubnetComparator implements Comparator<Subnet> {

		public int compare(Subnet o1, Subnet o2) {

			int[] a1, a2, m1, m2;
			int i;
			StringTokenizer st = new StringTokenizer(o1.getAddress(), ".",
					false);
			a1 = new int[st.countTokens()];
			i = 0;
			while (st.hasMoreTokens()) {
				a1[i] = Integer.parseInt(st.nextToken());
				i++;
			}
			st = new StringTokenizer(o2.getAddress(), ".", false);
			a2 = new int[st.countTokens()];
			i = 0;
			while (st.hasMoreTokens()) {
				a2[i] = Integer.parseInt(st.nextToken());
				i++;
			}
			st = new StringTokenizer(o1.getMask(), ".", false);
			m1 = new int[st.countTokens()];
			i = 0;
			while (st.hasMoreTokens()) {
				m1[i] = Integer.parseInt(st.nextToken());
				i++;
			}
			st = new StringTokenizer(o1.getMask(), ".", false);
			m2 = new int[st.countTokens()];
			i = 0;
			while (st.hasMoreTokens()) {
				m2[i] = Integer.parseInt(st.nextToken());
				i++;
			}

			if (a1.length != m1.length) {
				return -1;
			} else {
				if (a2.length != m2.length) {
					return +1;
				}
			}

			for (i = 0; i < a1.length; i++) {
				a1[i] = a1[i] & m1[i];
			}
			for (i = 0; i < a2.length; i++) {
				a2[i] = a2[i] & m2[i];
			}

			if (a1.length != a2.length) {
				return a1.length - a2.length;
			}
			for (i = 0; i < a1.length; i++) {
				if (a1[i] != a2[i]) {
					return a1[i] - a2[i];
				}
			}

			if (o1.getMask().equals(o2.getMask())) {
				return 0;
			} else {
				for (i = 0; i < java.lang.Math.min(m1.length, m2.length); i++) {
					if (m1[i] != m2[i]) {
						return m1[i] - m2[i];
					}
				}
				if (m1.length != m2.length) {
					return m1.length - m2.length;
				}
				return 0;
			}
		}

		public boolean equals(Subnet o1, Subnet o2) {
			return compare(o1, o2) == 0;
		}
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
