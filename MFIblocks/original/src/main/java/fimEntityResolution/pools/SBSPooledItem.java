package fimEntityResolution.pools;

import il.ac.technion.ie.data.structure.Clearer;
import org.enerj.core.SparseBitSet;


public class SBSPooledItem implements Clearer {
	private SparseBitSet sbs;
	public SBSPooledItem(){
		sbs = new SparseBitSet();
	}

	@Override
	public void clearAll() {
		sbs.clear();
	}

}


