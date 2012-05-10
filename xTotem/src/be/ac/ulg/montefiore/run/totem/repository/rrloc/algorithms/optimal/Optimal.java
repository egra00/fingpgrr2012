package be.ac.ulg.montefiore.run.totem.repository.rrloc.algorithms.optimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.iface.RRLocAlgorithm;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.iface.RunByGuiAlgorithm;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

public class Optimal extends RunByGuiAlgorithm
{
	private static final ArrayList<ParameterDescriptor> my_params = new ArrayList<ParameterDescriptor>();
	
    static {
        try {
        	
        	my_params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
        	my_params.add(new ParameterDescriptor("File config", "Relative path", String.class, "config,cnf"));
        } catch (AlgorithmParameterException e) {
            e.printStackTrace();
        }
    }
    
	public List<ParameterDescriptor> getStartAlgoParameters() {
		
		return (List<ParameterDescriptor>) my_params.clone();
	}

	@Override
	public void dumpResultInDomain(Domain domain, Object algorithmResult)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public RRLocAlgorithm getAlgorithm() {

		return new OptimalAlgorithm();
	}

	@Override
	public Object getAlgorithmParams(Domain domain) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object initAlgorithmResult() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void log(Object algorithmResult) {
		// TODO Auto-generated method stub
		
	}
}
