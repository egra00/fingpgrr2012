package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepD;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsep.BGPSep;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

public class BGPSepD extends BGPSep {
	
	public BGPSepD() {
		logger = Logger.getLogger(BGPSepD.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new BGPSepDAlgorithm();
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}

}
