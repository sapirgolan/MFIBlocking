package il.ac.technion.ie.pools;

import il.ac.technion.ie.utils.FIRunnableDB;
import il.ac.technion.ie.utils.GDS_NG;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FIRunnableDBPool {

	private LinkedList<FIRunnableDB> runnables = new LinkedList<>();
	private int created = 0;
	private static FIRunnableDBPool self = null;

	private FIRunnableDBPool() {
	};

	public static FIRunnableDBPool getInstance() {
		if (self == null) {
			self = new FIRunnableDBPool();
		}
		return self;
	}
	
	public int getNumCreated(){
		return created;
	}
	
	//assumption is all bms are the same size
	public FIRunnableDB getRunnable(List<Integer> currIS, int minSup, double NG_PARAM,
                                    Map<Integer, GDS_NG> coverageIndexDB){
		synchronized(this){
			if(runnables.size() > 0){
				FIRunnableDB toReturn = runnables.remove();
				toReturn.setParams(currIS, minSup, NG_PARAM, coverageIndexDB);
				return toReturn;
			}
			created++;			
			return new FIRunnableDB(currIS, minSup, NG_PARAM, coverageIndexDB);
		}		
	}
	
	
	public void returnRunnable(FIRunnableDB toReturn){	
		toReturn.setParams(null, 0, 0, null);
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
