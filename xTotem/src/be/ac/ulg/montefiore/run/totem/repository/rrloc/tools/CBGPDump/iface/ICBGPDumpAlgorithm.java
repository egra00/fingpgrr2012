package be.ac.ulg.montefiore.run.totem.repository.rrloc.tools.CBGPDump.iface;

import java.util.HashMap;

public interface ICBGPDumpAlgorithm {
	
	public void init();
	public void setParameters(HashMap params);
	public void run();
	public void dump();
	public void log();
	public void stop();

}
