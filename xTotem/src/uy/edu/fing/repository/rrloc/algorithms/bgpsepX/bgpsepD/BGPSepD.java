package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepD;

import org.apache.log4j.Logger;

import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsep.BGPSep;

public class BGPSepD extends BGPSep {
	
	public BGPSepD() {
		super();
		
		logger = Logger.getLogger(BGPSepD.class);
		name = "BGPSepD";
		algorithm = new BGPSepDAlgorithm();
	}

}
