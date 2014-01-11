package fimEntityResolution.bitsets;

import fimEntityResolution.interfaces.BitSetFactory;
import fimEntityResolution.interfaces.BitSetIF;

public class SingleBSFactory implements BitSetFactory{
	private static SingleBSFactory self = null;
	
	private SingleBSFactory(){}
	
	public static SingleBSFactory getInstance(){
		if(self == null){
			self = new SingleBSFactory();
		}
		return self;
	}

	@Override
	public BitSetIF createInstance() {
		return new SingleBitBS();
	}

}
