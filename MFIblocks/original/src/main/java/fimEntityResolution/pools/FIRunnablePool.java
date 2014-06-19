package fimEntityResolution.pools;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import candidateMatches.CandidatePairs;

import fimEntityResolution.BitMatrix;
import fimEntityResolution.FIRunnable;
import fimEntityResolution.Record;

public class FIRunnablePool {

	private LinkedList<FIRunnable> runnables = new LinkedList<FIRunnable>();
	private int created = 0;
	private static FIRunnablePool self = null;
	
	private FIRunnablePool(){};
	
	public static FIRunnablePool getInstance(){
		if(self == null){
			self = new FIRunnablePool();
		}
		return self;
	}
	
	public int getNumCreated(){
		return created;
	}
	
	//assumption is all bms are the same size
	public FIRunnable getRunnable(List<Integer> currIS,int minSup, 
			double NG_PARAM, Map<Integer, BitMatrix> coverageIndex, CandidatePairs CPs){	
		synchronized(this){
			if(runnables.size() > 0){
				FIRunnable toReturn = runnables.remove();
				toReturn.setParams(currIS, minSup, NG_PARAM,coverageIndex,CPs);
				return toReturn;
			}
			created++;			
			return new FIRunnable(currIS, minSup, NG_PARAM,coverageIndex, CPs);
		}		
	}
	
	public void returnRunnable(FIRunnable toReturn){	
		toReturn.setParams(null, 0, 0,null,null);		
		synchronized(this){
			runnables.add(toReturn);
		}
	}
	
	public void restart(){
		System.out.println(" Clearing " + runnables.size() + " runnables");
		runnables = null;
		runnables =  new LinkedList<FIRunnable>();
	}
}
