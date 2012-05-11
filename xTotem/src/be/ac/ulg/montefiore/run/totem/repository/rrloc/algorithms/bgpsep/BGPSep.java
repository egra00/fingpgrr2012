package be.ac.ulg.montefiore.run.totem.repository.rrloc.algorithms.bgpsep;

import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.iAlgorithm.BindAlgorithm;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

public class BGPSep extends BindAlgorithm {
		
    public BGPSep()
    {
    	algorithm =  new BGPSepAlgorithm();
    	algorithm.init();
    	try 
    	{
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
		} 
    	catch (AlgorithmParameterException e) 
		{
			e.printStackTrace();
		}
    	
    }
}
