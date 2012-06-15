package uy.edu.fing.repository.rrloc.algorithms.optimal;

import org.apache.commons.collections15.Factory;

public class ExtendedLinkFactory implements Factory<ExtendedLink>{

	@Override
	public ExtendedLink create() {
		return new ExtendedLink(0,null,null);
	}
	

}
