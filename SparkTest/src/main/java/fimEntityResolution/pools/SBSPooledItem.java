package fimEntityResolution.pools;

import org.enerj.core.SparseBitSet;

import fimEntityResolution.interfaces.Clearer;

public class SBSPooledItem implements Clearer{
	private SparseBitSet sbs;
	public SBSPooledItem(){
		sbs = new SparseBitSet();
	}

	
	public void clearAll() {
		sbs.clear();
	}

}


