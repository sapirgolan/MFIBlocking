package fimEntityResolution.bitsets;

import fimEntityResolution.interfaces.BitSetFactory;
import fimEntityResolution.interfaces.BitSetIF;

public class Java_BitSet_Factory implements BitSetFactory {
	private static Java_BitSet_Factory self = null;

	private Java_BitSet_Factory() {
	}

	public static Java_BitSet_Factory getInstance() {
		if (self == null) {
			self = new Java_BitSet_Factory();
		}
		return self;
	}

	
	public BitSetIF createInstance() {
		return new Java_BitSet();
	}

}
