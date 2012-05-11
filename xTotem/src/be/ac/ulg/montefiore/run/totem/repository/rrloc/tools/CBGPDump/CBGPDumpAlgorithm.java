package be.ac.ulg.montefiore.run.totem.repository.rrloc.tools.CBGPDump;

import java.util.HashMap;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.iAlgorithm.RRLocAlgorithm;

@SuppressWarnings("unchecked")
public class CBGPDumpAlgorithm implements RRLocAlgorithm
{
	
	private Domain domain;
	private String fileName;
	private static Logger logger = Logger.getLogger(CBGPDumpAlgorithm.class);

	@Override
	public void dump() 
	{	
	}

	@Override
	public void init() 
	{	
	}

	@Override
	public void log() 
	{
	}

	@Override
	public void run() 
	{
		logger.debug("Starting CBGPDump");
		
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

}
