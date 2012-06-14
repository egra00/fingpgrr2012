package uy.edu.fing.repository.rrloc.algorithms.optimal;

import org.apache.commons.collections15.Transformer;

public class TransformerExtendedLink implements Transformer<ExtendedLink,Integer>{

	@Override
	public Integer transform(ExtendedLink link) {
		return 1;
	}
}
