package uy.edu.fing.repository.tools.CBGPDump;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.exception.NodeNotFoundException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpNetwork;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;

@SuppressWarnings("unchecked")
public class CBGPDumpAlgorithm
{
	
	private Domain domain;
	private int domain_num; 
	private String fileName;
	private BufferedWriter bw;
	private HashMap<String, Link> linksById;
    private HashMap<String, Node> nodesById;
	private static Logger logger = Logger.getLogger(CBGPDumpAlgorithm.class);


	public void init() 
	{	
		linksById = new HashMap<String, Link>();
		nodesById = new HashMap<String, Node>();
	}


	public void run(String name) 
	{
    	domain = InterDomainManager.getInstance().getDefaultDomain();
    	if(domain == null)
    	{
        	logger.error("There is no default domain");
            return;
    	}
    	
    	if(name.equals("") || name == null)
    	{
        	logger.error("Invalid file name");
            return;
    	}
    	fileName = name;
    	
		logger.debug("Starting CBGPDump");
		myrun();
		logger.debug("Ending CBGPDump");
		
		domain = null;
		fileName = null;
	}

	
	
	private void errorControl(String descriptionError) throws IOException
	{
		/// Re-write file with error
		bw.close();
		bw = new BufferedWriter(new FileWriter(fileName));
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
			bw = new BufferedWriter(new FileWriter(fileName));
			
            error = initDescriptionTopology();
            if (!error) initIgpTopology();
            if (!error) initBgpTopology();
            if (!error) bw.close();
		} 
        catch (IOException e) 
		{
        	logger.error("ERROR: Unexpected error occurred while open/close/write file " + fileName);
			e.printStackTrace();
			return;
		}
	}
	
	private boolean initDescriptionTopology() throws IOException
	{
    	String description = ((domain.getDescription() == null || domain.getDescription().equals("")) ? "AS configuration IGP/iBGP" : domain.getDescription());
    	
    	bw.write("print \"*** "+ description +" ***\\n\\n\""+"\n\n");
		bw.write("# Domain AS"+ domain_num +"\n");
		
		return false;
	}
	
	private boolean initIgpTopology() throws IOException
	{
		bw.write("net add domain "+ domain_num +" igp" +"\n");
        
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
				bw.write("net node "+node.getRid()+ " domain "+ domain_num +"\n");
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
		/// END ADD LINKS
		
		return false;
	}
	
	private boolean initBgpTopology() throws IOException
	{
		/// ADD ROUTERS BGP AND SESSIONS iBGP
		List<BgpRouter> lst_bgps = domain.getAllBgpRouters();
		for(BgpRouter router : lst_bgps)
		{
			if (nodesById.containsKey(router.getRid()))
			{
				bw.write("bgp add router "+ domain_num + " " +router.getRid()+"\n");
				
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
	                    if(neighbor.isReflectorClient())
	                    {
	                        bw.write("add peer "+ domain_num +" "+ neighbor.getAddress() +" rr-client "+"\n");
	                    }
	                    else
	                    {
	                        bw.write("add peer "+ domain_num +" "+ neighbor.getAddress() +"\n");
	                    }
	
	                    bw.write("\t");
	                    bw.write("peer "+ neighbor.getAddress() +" up "+"\n");
	                }
	            }
	            bw.write("\t");
	            bw.write("exit"+"\n");
			}
		}
		/// END ROUTERS BGP AND SESSIONS iBGP

		return false;
	}
	
}


