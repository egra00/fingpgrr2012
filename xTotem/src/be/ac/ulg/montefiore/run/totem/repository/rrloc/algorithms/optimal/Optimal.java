package be.ac.ulg.montefiore.run.totem.repository.rrloc.algorithms.optimal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import be.ac.ulg.montefiore.run.totem.domain.model.Domain;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.iAlgorithm.BindAlgorithm;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.iAlgorithm.RRLocAlgorithm;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

public class Optimal extends BindAlgorithm
{   
    public Optimal()
    {
    	algorithm =  new OptimalAlgorithm();
    	algorithm.init();
    	try 
    	{
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
			params.add(new ParameterDescriptor("File config", "Relative path", String.class, "config,cnf"));
		} 
    	catch (AlgorithmParameterException e) 
		{
			e.printStackTrace();
		}
    	
    }

	@Override
	public RRLocAlgorithm getAlgorithm() {

		return algorithm;
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

	@Override
	public void dumpResultInDomain(Object algorithmResult) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getAlgorithmParams(HashMap params) {
		// TODO Auto-generated method stub
		return null;
	}
}
