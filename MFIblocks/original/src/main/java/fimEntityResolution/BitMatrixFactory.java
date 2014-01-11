package fimEntityResolution;

import fimEntityResolution.interfaces.Clearer;
import fimEntityResolution.interfaces.ClearerFactory;

public class BitMatrixFactory implements ClearerFactory{

	private static BitMatrixFactory self = null;
	private BitMatrixFactory(){}
	
	public static BitMatrixFactory getInstance(){
		if(self == null){
			self = new BitMatrixFactory();
		}
		return self;
	}
	
	
	@Override
	public Clearer createInstance() {
		return new BitMatrix(Utilities.DB_SIZE);
	}

	
}
