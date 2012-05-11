package be.ac.ulg.montefiore.run.totem.repository.rrloc.tools.CBGPDump;

import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.repository.rrloc.iAlgorithm.BindAlgorithm;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

public class CBGPDump extends BindAlgorithm 
{
    public CBGPDump()
    {
    	algorithm =  new CBGPDumpAlgorithm();
    	algorithm.init();
    	try 
    	{
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default)", Integer.class, null));
			params.add(new ParameterDescriptor("FILENAME", "Name out file", String.class, "scriptCBGP.cli"));
		} 
    	catch (AlgorithmParameterException e) 
		{
			e.printStackTrace();
		}
    	
    }
}
