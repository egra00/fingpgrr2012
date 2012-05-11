package be.ac.ulg.montefiore.run.totem.repository.rrloc.algorithms.optimal;

import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.iAlgorithm.BindAlgorithm;
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
}
