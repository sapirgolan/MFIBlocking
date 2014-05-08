package fimEntityResolution.bitsets;

import fimEntityResolution.interfaces.BitSetFactory;
import fimEntityResolution.interfaces.BitSetIF;

public class SBS_BitSet_Factory implements BitSetFactory{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7728625456113494341L;
	private static SBS_BitSet_Factory self = null;
	private int size = 0;
	
	private SBS_BitSet_Factory(){}
	private SBS_BitSet_Factory(int size){
		this.size = size;
	}
	
	public static SBS_BitSet_Factory getInstance(){
		if(self == null){
			self = new SBS_BitSet_Factory();
		}
		return self;
	}
	
	public static SBS_BitSet_Factory getInstance(int size){
		if(self == null){
			self = new SBS_BitSet_Factory(size);
		}
		return self;
	}

	@Override
	public BitSetIF createInstance() {
		if(size > 0){
			return new SBS_BitSet(size);
		}
		return new SBS_BitSet();
	}
	
	
}
