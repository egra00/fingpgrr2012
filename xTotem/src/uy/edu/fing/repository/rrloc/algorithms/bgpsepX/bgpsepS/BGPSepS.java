package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepS;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsep.BGPSep;
import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepD.BGPSepD;
import be.ac.ulg.montefiore.run.totem.repository.model.exception.AlgorithmParameterException;
import be.ac.ulg.montefiore.run.totem.util.ParameterDescriptor;

public class BGPSepS extends BGPSep {
	
	public BGPSepS() {
		my_logger = Logger.getLogger(BGPSepS.class);
		params = new ArrayList<ParameterDescriptor>();
		algorithm = new BGPSepSAlgorithm();
		
		try {
			params.add(new ParameterDescriptor("ASID", "Domain ASID (leave blank for default).", Integer.class, null));
		} catch (AlgorithmParameterException e) {
			e.printStackTrace();
		}
	}

}
