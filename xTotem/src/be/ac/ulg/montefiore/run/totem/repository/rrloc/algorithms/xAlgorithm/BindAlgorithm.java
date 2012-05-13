package be.ac.ulg.montefiore.run.totem.repository.rrloc.algorithms.xAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.exception.InvalidDomainException;
import be.ac.ulg.montefiore.run.totem.domain.facade.InterDomainManager;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.model.TotemAlgorithm;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class BindAlgorithm implements TotemAlgorithm {
	
	private Logger logger = Logger.getLogger(BindAlgorithm.class);
	private HashMap runningParams = null;
	
	protected RRLocAlgorithm algorithm;
	protected ArrayList<ParameterDescriptor> params;
    
	
	/*
	 * Used for initialize the parameters of input
	 * 
	 * @param domain
	 * @return the algorithm parameters 
	 * 
	 */
	public abstract Object getAlgorithmParams(Domain domain);
	
	/*
	 * It is called for initialize the result parameter
	 * 
	 * @return the parameter result
	 * 
	 */
	public abstract Object initAlgorithmResult();
	
	/*
	 * It is called when a algorithm end and is necessary impact
	 * the changes in the domain
	 * 
	 * @param domain
	 * @param algorithmResult is the result of the algorithm
	 * 
	 */
	public abstract void dumpResultInDomain(Domain domain, Object algorithmResult) throws Exception;
	
	/*
	 * Used in debug mode, log the result of algorithm in logger
	 * 
	 * @param algorithmResult is the result of the algorithm
	 * 
	 */
	public abstract void log(Object algorithmResult);
	
	
	public RRLocAlgorithm getAlgorithm()
	{
		return algorithm;
	}
	
	@Override
	public void start(HashMap params) throws AlgorithmInitialisationException {
		runningParams = params;

        String asId = (String) params.get("ASID");
        Domain domain;
        if(asId == null) {
        	domain = InterDomainManager.getInstance().getDefaultDomain();
        	if(domain == null){
	        	logger.error("There is no default domain");
	            return;
        	}
        } else {
            try {
                domain = InterDomainManager.getInstance().getDomain(Integer.parseInt(asId));
            } catch(InvalidDomainException e) {
                logger.error("Cannot load domain " + asId);
                return;
            }
        }
        logger.debug("Starting...");
        
        Object algorithmParams = getAlgorithmParams(domain);
        Object algorithmResult = initAlgorithmResult();
        
        algorithm.run(algorithmParams, algorithmResult);
        
        log(algorithmResult);
        
        try {
        	dumpResultInDomain(domain, algorithmResult);
		} catch (Exception e) {
			logger.error("Dumping iBGP topology in Totem domain");
			e.printStackTrace();
		}
        
        logger.debug("Finish");
	}

	@Override
	public void stop() {
		runningParams = null;
	}

	@Override
	public HashMap getRunningParameters() {
		return (runningParams == null) ? null : (HashMap)runningParams.clone();
	}

	@Override
	public List<ParameterDescriptor> getStartAlgoParameters() {
		
		return (List<ParameterDescriptor>) params.clone();
	}
}
