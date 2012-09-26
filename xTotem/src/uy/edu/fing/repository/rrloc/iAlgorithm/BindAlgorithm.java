package uy.edu.fing.repository.rrloc.iAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.repository.model.TotemAlgorithm;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class BindAlgorithm implements Runnable, TotemAlgorithm {
	
	protected Logger logger;
	protected HashMap runningParams = null;
	protected Thread thread;
	protected RRLocAlgorithm algorithm;
	protected ArrayList<ParameterDescriptor> params;
    
    Object algorithmParams;
    Object algorithmResult;
	
	/*
	 * Used for initialize the parameters of input
	 * 
	 * @param domain
	 * @return the algorithm parameters 
	 * 
	 */
	public abstract Object getAlgorithmParams(HashMap params);
	
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
	public abstract void dumpResultInDomain(Object algorithmResult) throws Exception;
	
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
        logger.debug("Starting...");
        
        algorithmParams = getAlgorithmParams(params);
        algorithmResult = initAlgorithmResult();
        
        
		if(algorithmParams != null && algorithmResult != null)
		{
		    thread.start();        
		    logger.debug("Finish");
		}
		else
			logger.debug("Incorrect initialization of input parameters or output parameters");
	}

	@Override
	public void stop() {
		runningParams = null;
		thread.interrupt();
	}

	@Override
	public void run() 
	{
        algorithm.run(algorithmParams, algorithmResult);
        
        log(algorithmResult);
        
        try {
        	dumpResultInDomain(algorithmResult);
		} catch (Exception e) {
			logger.error("Dumping iBGP topology in Totem domain");
			e.printStackTrace();
		}
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
