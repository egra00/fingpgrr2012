package be.ac.ulg.montefiore.run.totem.repository.rrloc.iAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import be.ac.ulg.montefiore.run.totem.repository.model.TotemAlgorithm;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class BindAlgorithm implements TotemAlgorithm {
	
	protected ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();
	protected HashMap runningParams = null;
	protected RRLocAlgorithm algorithm = null;
	
	/*
	 * Used for create a instance of the algorithm 
	 *
	 * @return an instance of RRLocAlgorithm interface
	 * 
	 */
	public abstract RRLocAlgorithm getAlgorithm();

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
	
	
	@Override
	public void start(HashMap params) throws AlgorithmInitialisationException {
		runningParams = params;
        
        Object algorithmParams = getAlgorithmParams(params);
        Object algorithmResult = initAlgorithmResult();
        
        algorithm.run(algorithmParams, algorithmResult);
        
        log(algorithmResult);
        try {
			dumpResultInDomain(algorithmResult);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		runningParams = null;
		algorithm.stop();
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
