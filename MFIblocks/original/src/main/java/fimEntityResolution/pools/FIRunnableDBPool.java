package fimEntityResolution.pools;

import fimEntityResolution.FIRunnableDB;
import fimEntityResolution.GDS_NG;
import il.ac.technion.ie.model.Record;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FIRunnableDBPool {

	private LinkedList<FIRunnableDB> runnables = new LinkedList<FIRunnableDB>();
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
	public FIRunnableDB getRunnable(List<Integer> currIS,int minSup, Map<Integer,Record> records, double NG_PARAM,
			int expectedSupportSize,Map<Integer, GDS_NG> coverageIndexDB){	
		synchronized(this){
			if(runnables.size() > 0){
				FIRunnableDB toReturn = runnables.remove();
				toReturn.setParams(currIS, minSup, records, NG_PARAM,expectedSupportSize, coverageIndexDB);
				return toReturn;
			}
			created++;			
			return new FIRunnableDB(currIS, minSup, records, NG_PARAM,expectedSupportSize, coverageIndexDB);
		}		
	}
	
	
	public void returnRunnable(FIRunnableDB toReturn){	
		toReturn.setParams(null, 0, null, 0,0,null);		
		synchronized(this){
			runnables.add(toReturn);
		}
	}
	
	public void restart(){
		System.out.println(" Clearing " + runnables.size() + " runnables");
		runnables = null;
		runnables =  new LinkedList<FIRunnableDB>();
	}
}
