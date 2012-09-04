package uy.edu.fing.repository.rrloc.algorithms.optimal;

import org.apache.commons.collections15.Transformer;

import be.ac.ulg.montefiore.run.totem.domain.model.Link;

public class TransformerLink implements Transformer<Link,Float>{

	@Override
	public Float transform(Link link) {
		return link.getMetric();
	}
}
