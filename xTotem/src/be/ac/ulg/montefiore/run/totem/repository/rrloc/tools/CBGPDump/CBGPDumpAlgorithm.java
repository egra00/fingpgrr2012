package be.ac.ulg.montefiore.run.totem.repository.rrloc.tools.CBGPDump;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpNeighbor;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpNetwork;
import be.ac.ulg.montefiore.run.totem.domain.model.BgpRouter;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.domain.model.Link;
import be.ac.ulg.montefiore.run.totem.domain.model.Node;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.iAlgorithm.RRLocAlgorithm;

@SuppressWarnings("unchecked")
public class CBGPDumpAlgorithm implements RRLocAlgorithm
{
	
	private Domain domain;
	private String fileName;
	private Hashtable<String, Link> linksById;
	private Hashtable<String, BgpRouter> cbgpBgpRouters;
    private Hashtable<String, Node> nodesById;
	private static Logger logger = Logger.getLogger(CBGPDumpAlgorithm.class);

	@Override
	public void dump() 
	{	
	}

	@Override
	public void init() 
	{	
		linksById = new Hashtable<String, Link>();
		cbgpBgpRouters = new Hashtable<String, BgpRouter>();
		nodesById = new Hashtable<String, Node>();
	}

	@Override
	public void log() 
	{
	}

	@Override
	public void run() 
	{
		logger.debug("Starting CBGPDump");
		myrun(domain, fileName);
		logger.debug("Ending CBGPDump");
	}

	@Override
	public void setParameters(HashMap params) 
	{	
        String asId = (String) params.get("ASID");
        if(asId == null) 
        {
        	domain = InterDomainManager.getInstance().getDefaultDomain();
        	if(domain == null)
        	{
	        	logger.error("There is no default domain");
	            return;
        	}
        } else 
        {
            try 
            {
                domain = InterDomainManager.getInstance().getDomain(Integer.parseInt(asId));
            } 
            catch(InvalidDomainException e) 
            {
                logger.error("Cannot load domain " + asId);
                return;
            }
        }
        
        fileName = (String) params.get("FILENAME");
        if(fileName == null || fileName.equals("")) 
        {
        	logger.error("Ivalid File name");
        	return;
        }
	}

	@Override
	public void stop() 
	{
		domain = null;
		fileName = null;
	}
	
	
	private void myrun(Domain domain, String fileName)
	{
		int domain_num = domain.getASID();
        try 
        {
            BufferedWriter bw = new BufferedWriter(new FileWriter("fileName"+".cli"));
            
            bw.write("print \"*** valid-igp-change ***\n\n\""+"\n\n");
            bw.write("# Domain AS"+ domain_num +"\n");
            bw.write("net add domain "+ domain_num +" igp" +"\n");
            
            /// ADD NODES
    		List<Node> lst_nodes = domain.getAllNodes();
    		for(Node node : lst_nodes)
    		{
				bw.write("net add node "+node.getRid()+"\n");
				bw.write("net node "+node.getRid()+ " domain "+ domain_num +"\n");
				nodesById.put(node.getRid(), node);
    		}
    		/// END ADD NODE
    		
    		
    		/// ADD LINKS
    		List<Link> lst_links = domain.getAllLinks();
    		for(Link link : lst_links)
    		{
                Node nodeSrc = link.getSrcNode();;
                Node nodeDst = link.getDstNode();
                
                String linkId = (nodeSrc.getRid().compareTo(nodeDst.getRid()) <= 0) ? nodeSrc.getRid() + ":" + nodeDst.getRid() : nodeDst.getRid() + ":" + nodeSrc.getRid();
                if (!linksById.containsKey(linkId))
                {
                	bw.write("net add link "+ nodeSrc + " " + nodeDst + "\n");
                	bw.write("net link "+ nodeSrc + " " + nodeDst + " igp-weight –-bidir" + (int)link.getMetric() +"\n");
                	linksById.put(linkId, link);
                }
                
    		}
    		/// END ADD LINKS
    	    		
    		
    		/// ADD ROUTERS BGP AND SESSIONS iBGP
    		List<BgpRouter> lst_bgps = domain.getAllBgpRouters();
    		for(BgpRouter router : lst_bgps)
    		{
    			cbgpBgpRouters.put(router.getRid(), router);
				bw.write("bgp add router "+ domain_num + " " +router.getRid()+"\n");
				
                // Add all originated networks
                List<BgpNetwork> networks = router.getAllNetworks();
                for (Iterator<BgpNetwork> iterNetworks = networks.iterator(); iterNetworks.hasNext();) 
                {
                    BgpNetwork network = iterNetworks.next();
                    bw.write("\t");
                    bw.write("add network "+ network+"\n");
                }
                
                // Add all neighbors and open the BGP session
                List<BgpNeighbor> neighbors = router.getAllNeighbors();
                for (Iterator<BgpNeighbor> iterNeighbors = neighbors.iterator(); iterNeighbors.hasNext();) 
                {
                    BgpNeighbor neighbor = iterNeighbors.next();
                    bw.write("\t");
                    
                    /* Internal/external BGP neighbor */
                    if (neighbor.getASID() == domain.getASID()) 
                    {

                        /* Internal... */

                        /* Check that the neighbor node exists. Issue
                         * a warning if not. */
                        if (!nodesById.containsKey(neighbor.getAddress()))
                        	logger.error("WARNING: no node for neighbor " + neighbor.getAddress());
                    }
                    else
                    	return;
                    
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
                bw.write("\t");
                bw.write("exit"+"\n");
				
    		}
    		/// END ROUTERS BGP AND SESSIONS iBGP

    		bw.write("net domain "+ domain_num +" compute "+"\n");
    		bw.write("sim run"+"\n"); 
    		
            bw.close();
        } 
        catch (Exception ex) 
        {
			ex.printStackTrace();
			return;
        }
	}
	
}


