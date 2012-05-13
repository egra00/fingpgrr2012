package be.ac.ulg.montefiore.run.totem.repository.rrloc.tools.CBGPDump.iface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import be.ac.ulg.montefiore.run.totem.repository.model.TotemAlgorithm;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

@SuppressWarnings("unchecked")
public abstract class BindCBGPDumpAlgorithm implements TotemAlgorithm {
	
	protected ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();
	protected HashMap runningParams = null;
	protected ICBGPDumpAlgorithm algorithm = null;	
	
	@Override
	public void start(HashMap params) throws AlgorithmInitialisationException {
		runningParams = params;
        
		algorithm.setParameters(params);        
        algorithm.run();
        algorithm.dump();
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
