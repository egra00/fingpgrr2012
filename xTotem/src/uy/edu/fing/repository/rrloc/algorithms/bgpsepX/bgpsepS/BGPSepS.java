package uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsepS;

import org.apache.log4j.Logger;

import uy.edu.fing.repository.rrloc.algorithms.bgpsepX.bgpsep.BGPSep;

public class BGPSepS extends BGPSep {
	
	public BGPSepS() {
		super();
		
		logger = Logger.getLogger(BGPSepS.class);
		name = "BGPSepS";
		algorithm = new BGPSepSAlgorithm();
	}

}
