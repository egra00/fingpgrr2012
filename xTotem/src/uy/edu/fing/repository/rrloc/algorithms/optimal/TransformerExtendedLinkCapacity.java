package uy.edu.fing.repository.rrloc.algorithms.optimal;

import org.apache.commons.collections15.Transformer;

public class TransformerExtendedLinkCapacity implements Transformer<ExtendedLink,Integer>{

	@Override
	public Integer transform(ExtendedLink link) {
		return link.getCapacity();
	}
}
