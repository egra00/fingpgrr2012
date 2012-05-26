package uy.edu.fing.repository.rrloc.algorithms.optimal;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import uy.edu.fing.repository.rrloc.algorithms.iAlgorithm.BindAlgorithm;
import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

public class Optimal extends BindAlgorithm {
	private Logger my_logger;

	public Optimal() {
		my_logger = Logger.getLogger(Optimal.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new OptimalAlgorithm();
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
			params.add(new ParameterDescriptor("FILEPATH", "Relative file path.", String.class, "conf.cnf"));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void dumpResultInDomain(Domain domain, Object algorithmResult)
			throws Exception {
		// TODO Auto-generated method stub
		
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
