package be.ac.ulg.montefiore.run.totem.repository.RRLoc.Algorithms.MiAlgoritmo1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import be.ac.ulg.montefiore.run.totem.repository.model.TotemAlgorithm;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmInitialisationException;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

public class MiAlgoritmo1 implements TotemAlgorithm
{
	private static final ArrayList<ParameterDescriptor> params = new ArrayList<ParameterDescriptor>();
	private static HashMap runningParams = null;
    
    static {
        try {
        	params.add(new ParameterDescriptor("ASID", "Domain ASID", Integer.class, null));
            params.add(new ParameterDescriptor("STRING", "Parametro de tipo STRING", String.class, "VALOR"));
            params.add(new ParameterDescriptor("BOOLEAN", "Parametro de tipo BOOLEAN", Boolean.class, new Boolean(false)));
            params.add(new ParameterDescriptor("DOUBLE", "Parametro de tipo DOUBLE", Double.class, new Double(0.0)));
            params.add(new ParameterDescriptor("Lista de opciones", "Lista de opciones", String.class, "VALOR1", new String[] {"VALOR1", "VALOR2", "VALOR3"}));
        } catch (AlgorithmParameterException e) {
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

	@Override
	public void start(HashMap params) throws AlgorithmInitialisationException 
	{
		runningParams = params;
		///////////////////////
		/// ACA VA EL ALGORITMO
		///////////////////////
		
		System.out.println("ALGORITMO 1: PUTOS..............");
	}

	@Override
	public void stop() 
	{
		runningParams = null;
	}

		
}
