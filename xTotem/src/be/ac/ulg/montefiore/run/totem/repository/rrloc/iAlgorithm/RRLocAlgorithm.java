package be.ac.ulg.montefiore.run.totem.repository.rrloc.iAlgorithm;

import java.util.HashMap;

public interface RRLocAlgorithm {
	
	public void init();
	public void setParameters(HashMap params);
	public void run();
	public void dump();
	public void log();
	public void stop();

}
