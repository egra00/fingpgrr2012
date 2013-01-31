package uy.edu.fing.repository.tools.CBGPDump;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import uy.edu.fing.repository.tools.CBGPDump.model.MsjMRT;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpNetwork;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.domain.model.impl.BgpNeighborImpl;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.BgpFilter.RuleType.ActionType;
import be.ac.ulg.montefiore.run.totem.domain.model.jaxb.impl.BgpRouterImpl;


public class CBGPDumpAlgorithm
{
	
	private Domain domain;
	private int domain_num; 
	private String mrtName;
	private String outName;
	private BufferedWriter bw;
	private HashMap<String, Link> linksById;
    private HashMap<String, Node> nodesById;
	private static Logger logger = Logger.getLogger(CBGPDumpAlgorithm.class);


	public void run(Domain _domain, String mrt_file) 
	{
		
		
    	domain = _domain;
    	if(domain == null)
    	{
        	logger.error("There is no default domain");
            return;
    	}
    	
    	mrtName = mrt_file;

    	
    	outName = domain.getURI().getPath();
    	outName = outName.endsWith(".xml") ? outName.substring(0, outName.length() - 4) : outName;
    	outName += "-" + "simulation.cli";
    	
		linksById = new HashMap<String, Link>();
		nodesById = new HashMap<String, Node>();
    	
		logger.debug("Starting CBGPDump");
		myrun();
		logger.debug("Ending CBGPDump");
		
		domain = null;
		outName = null;
	}

	
	
	private void errorControl(String descriptionError) throws IOException
	{
		/// Re-write file with error
		bw.close();
		bw = new BufferedWriter(new FileWriter(outName));
		bw.write("print \"*** ERROR: "+ descriptionError +" ***\\n\\n\""+"\n\n");
		bw.close();	
		
		/// Logger error
		logger.error("ERROR: " + descriptionError);
	}
	
	private void myrun()
	{
        try 
        {
        	boolean error;
        	domain_num = domain.getASID();
			bw = new BufferedWriter(new FileWriter(outName));
			
            error = initDescriptionTopology();
            if (!error) initIgpTopology();
            if (!error) initBgpTopology();
            if (!error) bw.close();
		} 
        catch (IOException e) 
		{
        	logger.error("ERROR: Unexpected error occurred while open/close/write file " + outName);
			e.printStackTrace();
			return;
		}
	}
	
	private boolean initDescriptionTopology() throws IOException
	{
    	bw.write("# ===================================================================\n");
    	bw.write("# C-BGP Export file (CLI)\n");
    	bw.write("# Domain AS "+ domain_num +"\n");
    	bw.write("# ===================================================================\n\n");
		return false;
	}
	
	private boolean initIgpTopology() throws IOException
	{	
		bw.write("# -------------------------------------------------------------------\n");
		bw.write("# Physical topology\n");
		bw.write("# -------------------------------------------------------------------\n");
        /// ADD NODES
		List<Node> lst_nodes = domain.getAllNodes();
		for(Node node : lst_nodes)
		{
			if (node.getRid() == null || node.getRid().equals(""))
			{
				errorControl("Node "+ node.getId() +" doesn't has Router ID (Rid == NULL || Rid == EMPTY)");
				return true;
			}
			else
			{
				bw.write("net add node "+node.getRid()+"\n");
				//bw.write("net node "+node.getRid()+ " domain "+ domain_num +"\n");
				nodesById.put(node.getRid(), node);
			}
		}
		/// END ADD NODE
		
		
		/// ADD LINKS
		List<Link> lst_links = domain.getAllLinks();
		for(Link link : lst_links)
		{
			try 
			{
				Node nodeSrc = link.getSrcNode();
				Node nodeDst = link.getDstNode();
				
	            String linkId = (nodeSrc.getRid().compareTo(nodeDst.getRid()) <= 0) ? nodeSrc.getRid() + ":" + nodeDst.getRid() : nodeDst.getRid() + ":" + nodeSrc.getRid();
	            if (!linksById.containsKey(linkId))
	            {
	            	bw.write("net add link "+ nodeSrc.getRid() + " " + nodeDst.getRid() + "\n");
	            	bw.write("net link "+ nodeSrc.getRid() + " " + nodeDst.getRid() + " igp-weight --bidir " + (int)link.getMetric() +"\n");
	            	linksById.put(linkId, link);
	            }
			} 
			catch (NodeNotFoundException e) 
			{
				errorControl("Could not add link " + link.getId() + " (" + e.getMessage() + ")");
				e.printStackTrace();
				return true;
			};
		}
		bw.write("\n");
		/// END ADD LINKS
		
		/// STATIC ROUTING
		bw.write("# -------------------------------------------------------------------\n");
		bw.write("# Static routing\n");
		bw.write("# -------------------------------------------------------------------\n");
		// Vacio
		bw.write("\n");
		/// END STATIC ROUTING
		
		/// IGP ROUTING
		bw.write("# -------------------------------------------------------------------\n");
		bw.write("# IGP routing\n");
		bw.write("# -------------------------------------------------------------------\n");
		
		bw.write("net add domain "+ domain_num +" igp" +"\n");
		
		lst_nodes = domain.getAllNodes();
		for(Node node : lst_nodes)
		{
			if (node.getRid() == null || node.getRid().equals(""))
			{
				errorControl("Node "+ node.getId() +" doesn't has Router ID (Rid == NULL || Rid == EMPTY)");
				return true;
			}
			else
				bw.write("net node "+node.getRid()+ " domain "+ domain_num +"\n");
			
		}
		bw.write("net domain "+ domain_num +" compute");
		bw.write("\n\n");
		/// END IGP ROUTING
		
		return false;
	}
	
	private boolean initBgpTopology() throws IOException
	{
		/// BGP ROUTING
		bw.write("# -------------------------------------------------------------------\n");
		bw.write("# BGP routing\n");
		bw.write("# -------------------------------------------------------------------\n");
		bw.write("bgp options msg-monitor " + "AS" + domain_num + "-trace.bgp" +"\n\n");
		
		/// ADD ROUTERS BGP AND SESSIONS iBGP
		List<BgpRouter> lst_bgps = domain.getAllBgpRouters();
		for(BgpRouter router : lst_bgps)
		{
			if (nodesById.containsKey(router.getRid()))
			{	
				bw.write("bgp add router "+ domain_num + " " +router.getRid()+"\n");
				bw.write("bgp router "+router.getRid()+"\n");
				
	            // Add all originated networks
	            List<BgpNetwork> networks = router.getAllNetworks();
	            for (Iterator<BgpNetwork> iterNetworks = networks.iterator(); iterNetworks.hasNext();) 
	            {
	                BgpNetwork network = iterNetworks.next();
	                bw.write("\t");
	                bw.write("add network "+ network.getPrefix() +"\n");
	            }
	            
	            // Add all neighbors and open the BGP session
	            List<BgpNeighbor> neighbors = router.getAllNeighbors();
	            for (Iterator<BgpNeighbor> iterNeighbors = neighbors.iterator(); iterNeighbors.hasNext();) 
	            {
	            	BgpNeighbor neighbor = iterNeighbors.next();
	            	
	                /* Internal BGP neighbor */
	                if (neighbor.getASID() == domain_num) 
	                {
	                    /* Check that the neighbor node exists. Issue a warning if not. */
	                    if (!nodesById.containsKey(neighbor.getAddress()))
	                    	logger.error("WARNING: no node for neighbor " + neighbor.getAddress());
	                
	                    bw.write("\t");
	                    bw.write("add peer "+ domain_num +" "+ neighbor.getAddress() +"\n");
	                    
	                    if(((BgpRouterImpl)router).isReflector())
	                    {
	                    	bw.write("\t");
	                        bw.write("peer "+ neighbor.getAddress() +" rr-client"+"\n");
	                    }

	                    bw.write("\t");
	                    bw.write("peer "+ neighbor.getAddress() +" up"+"\n");
	                }
	            }
	            bw.write("\t");
	            bw.write("exit"+"\n");
			}
		}
		bw.write("\n");
		/// END ROUTERS BGP AND SESSIONS iBGP
		
		/// START BGP FILTERS
		
		bw.write("# -------------------------------------------------------------------\n");
		bw.write("# BGP filters\n");
		bw.write("# -------------------------------------------------------------------\n");
		bw.write("\n");
		
		for(BgpRouter router : lst_bgps) {
			// For all neighbor
			for (BgpNeighbor n : router.getAllNeighbors()) {
				BgpNeighborImpl neighbor = (BgpNeighborImpl)n;
				
				if (neighbor.getFilters() == null) {
					continue;
				}
				
				// In
				if (neighbor.getFilters().getInFilter() != null && neighbor.getFilters().getInFilter().getRule() != null) {
					for (Object a : neighbor.getFilters().getInFilter().getRule().getAction()) {
						ActionType action = (ActionType)a;
						bw.write(action.getValue() + "\n");
					}
				}
				
				//Out
				if (neighbor.getFilters().getOutFilter() != null && neighbor.getFilters().getOutFilter().getRule() != null) {
					for (Object a : neighbor.getFilters().getOutFilter().getRule().getAction()) {
						ActionType action = (ActionType)a;
						bw.write(action.getValue() + "\n");
					}
				}
			}
		}
		bw.write("\n");
		/// END BGP FILTERS
		
		//SCENARIO TO SIMULATE
		simulationSecction();
		/// END SCENARIO TO SIMULATE
		
		
		/// START SIMULATION
		bw.write("# -------------------------------------------------------------------\n");
		bw.write("# Start simulation\n");
		bw.write("# -------------------------------------------------------------------\n");
		bw.write("sim run\n");
		bw.close();
		/// END START SIMULATION
		
		return false;
	}



	private void simulationSecction() throws IOException{
		
		bw.write("# -------------------------------------------------------------------\n");
		bw.write("# Scenario to simulate\n");
		bw.write("# -------------------------------------------------------------------\n");
		
		if(mrtName != null) {
			File file = new File(mrtName);
			if(file.exists() && !file.isDirectory()) {
				 try
				 {
					  // Open the file that is the first 
					  // command line parameter
					  FileInputStream fstream = new FileInputStream(mrtName);
					  // Get the object of DataInputStream
					  DataInputStream in = new DataInputStream(fstream);
					  BufferedReader br = new BufferedReader(new InputStreamReader(in));
					  String strLine;
					  int pos;
					  //Read File Line By  
					  int as_number = domain_num + 1;
					  while ((strLine = br.readLine()) != null)   {
						  
						  StringTokenizer token = new StringTokenizer(strLine, ";|");
						  
						  //System.out.println(strLine);
						  
						  String border_router = token.nextToken();
						  String virtual_router = token.nextToken();
						  
						  MsjMRT msj = new MsjMRT();
						  pos=0;
						  while(token.hasMoreTokens()) {
							  msj.setAttribute(pos, token.nextToken());
							  pos++;
						  }
						  
						  if(msj.getOrigin() != null) msj.setOrigin(virtual_router);
						  if(msj.getPath() != null) msj.setPath(as_number + " " + msj.getPath());
						  						  
						  bw.write("net add node "+ virtual_router +"\n");
						  bw.write("net add domain " + as_number + " igp" +"\n");
						  bw.write("net node " + virtual_router + " domain " + as_number +"\n");
						  bw.write("net add link " + border_router + " " + virtual_router +"\n");
						  bw.write("net node " + border_router +" route add --oif="+ virtual_router + "/32 0" +"\n");
						  bw.write("net node " +virtual_router +" route add --oif="+ border_router + "/32 0" +"\n");
						  bw.write("bgp router " + border_router +"\n");
						  	bw.write("\tadd peer " + as_number + " " + virtual_router +"\n");
						  	bw.write("\tpeer " + virtual_router + " virtual" +"\n");
						  	bw.write("\tpeer " + virtual_router + " next-hop-self" +"\n");
						  	bw.write("\tpeer " + virtual_router + " up" +"\n");
						  	bw.write("\tpeer " + virtual_router + " recv \"" + msj.toString() +"\"\n");
						  	bw.write("\texit" +"\n");
						  	
						  bw.write("\n");
						  as_number++;
						  
					  }
					  
					  //Close the input stream
					  in.close();
				 }
				 catch (Exception e){//Catch exception if any
					 
					 e.printStackTrace();
					  System.err.println("Error jjjjj: " + e.getMessage());
				 }
			}
		}
		
	
	
		bw.write("\n");
		
	}
	
}


