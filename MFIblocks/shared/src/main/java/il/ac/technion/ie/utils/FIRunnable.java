package il.ac.technion.ie.utils;

import il.ac.technion.ie.data.structure.Clearer;
import il.ac.technion.ie.data.structure.IFRecord;
import il.ac.technion.ie.model.BitSetIF;
import il.ac.technion.ie.model.CandidatePairs;
import il.ac.technion.ie.pools.FIRunnablePool;

import java.util.List;

public class FIRunnable implements Runnable, Clearer {

	private List<Integer> currentItemSet = null;
	private int minSup;
    private CandidatePairs candidatePairs;

	public FIRunnable(List<Integer> currentItemSet, int minSup, CandidatePairs candidatePairs) {
		this.currentItemSet = currentItemSet;
		this.minSup = minSup;
        this.candidatePairs = candidatePairs;
	}

	public void setParams(List<Integer> currIS, int minSup, CandidatePairs candidatePairs) {
		this.currentItemSet = currIS;
		this.minSup = minSup;
        this.candidatePairs = candidatePairs;
	}

	@Override
	public void run() {
		
		BitSetIF support;
		try{
		
			support = Utilities.getItemsetSupport(currentItemSet);
			if (support.getCardinality() <  minSup) {
				Utilities.nonFIs.incrementAndGet();					
				return; // must be the case that the item appears minSup times
						// but in a number of records < minsup
			}			
			List<IFRecord> FISupportRecords = support.getRecords();
								
			long start = System.currentTimeMillis();
			
			double currClusterScore = StringSimTools.softTFIDF(
                    FISupportRecords, currentItemSet, Utilities.scoreThreshold);
            Utilities.timeSpentCalcScore.addAndGet(System.currentTimeMillis() - start);

			Utilities.clusterScores[cellForCluster(currClusterScore)].incrementAndGet();

			if (currClusterScore > Utilities.scoreThreshold) {
				Utilities.numOfFIs.incrementAndGet();
				support.markPairs(candidatePairs,currClusterScore,currentItemSet);
			}
		}
		finally{	
			FIRunnablePool.getInstance().returnRunnable(this); // completed running
		}

	}

	@Override
	public void clearAll() {
		this.setParams(null, 0, null);
	}
	
	private static int cellForCluster(final double score) {
		return (int) Math.ceil(score / 0.05);
	}


}