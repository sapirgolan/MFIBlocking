package fimEntityResolution.bitsets;

import il.ac.technion.ie.model.BitSetFactory;
import il.ac.technion.ie.model.BitSetIF;

public class Java_BitSet_Factory implements BitSetFactory {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8610454563329669590L;
	private static Java_BitSet_Factory self = null;

	private Java_BitSet_Factory() {
	}

	public static Java_BitSet_Factory getInstance() {
		if (self == null) {
			self = new Java_BitSet_Factory();
		}
		return self;
	}

	@Override
	public BitSetIF createInstance() {
		return new Java_BitSet();
	}

}
