package il.ac.technion.ie.pools;

import il.ac.technion.ie.model.CandidatePairs;
import il.ac.technion.ie.utils.FIRunnable;

import java.util.LinkedList;
import java.util.List;

public class FIRunnablePool {

	private LinkedList<FIRunnable> runnables = new LinkedList<>();
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
	public FIRunnable getRunnable(List<Integer> currIS, int minSup,
                                  CandidatePairs CPs){
		synchronized(this){
			if(runnables.size() > 0){
				FIRunnable toReturn = runnables.remove();
				toReturn.setParams(currIS, minSup, CPs);
				return toReturn;
			}
			created++;			
			return new FIRunnable(currIS, minSup, CPs);
		}		
	}
	
	public void returnRunnable(FIRunnable toReturn){	
		toReturn.setParams(null, 0, null);
		synchronized(this){
			runnables.add(toReturn);
		}
	}
	
	public void restart(){
		System.out.println(" Clearing " + runnables.size() + " runnables");
		runnables = null;
		runnables =  new LinkedList<>();
	}
}
