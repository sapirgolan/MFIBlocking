package fimEntityResolution;

import java.util.List;
import java.util.Map;

import candidateMatches.CandidatePairs;

import fimEntityResolution.interfaces.BitSetIF;
import fimEntityResolution.interfaces.Clearer;
import fimEntityResolution.interfaces.IFRecord;
import fimEntityResolution.pools.FIRunnablePool;

public class FIRunnable implements Runnable, Clearer {

	private List<Integer> currentItemSet = null;
	private int minSup;
	private Map<Integer, Record> records = null;
	private double NG_PARAM;
	private Map<Integer, BitMatrix> coverageIndex;
	private CandidatePairs candidatePairs;

	public FIRunnable(List<Integer> currentItemSet, int minSup, double NG_PARAM,Map<Integer, BitMatrix> coverageIndex, CandidatePairs candidatePairs) {
		this.currentItemSet = currentItemSet;
		this.minSup = minSup;
		this.NG_PARAM = NG_PARAM;
		this.coverageIndex = coverageIndex;
		this.candidatePairs = candidatePairs;
	}

	public void setParams(List<Integer> currIS, int minSup, double NG_PARAM,Map<Integer, BitMatrix> coverageIndex, CandidatePairs candidatePairs) {
		this.currentItemSet = currIS;
		this.minSup = minSup;
		this.NG_PARAM = NG_PARAM;
		this.coverageIndex = coverageIndex;
		this.candidatePairs = candidatePairs;
	}

	@Override
	public void run() {
		
		BitSetIF support = null;
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
			FISupportRecords = null;
			Utilities.timeSpentCalcScore.addAndGet(System.currentTimeMillis() - start);

			Utilities.clusterScores[cellForCluster(currClusterScore)].incrementAndGet();

			if (currClusterScore > Utilities.scoreThreshold) {
				Utilities.numOfFIs.incrementAndGet();
				support.markPairs(candidatePairs,currClusterScore);
				
			/*	int clusterCell = cellForCluster(currClusterScore);
				synchronized (FIRunnable.class) {
					int begIndex = Utilities.getIntForThresh(Utilities.scoreThreshold);
					for (int i = begIndex; i <= clusterCell; i++) {
						BitMatrix bm = coverageIndex.get(i);
						if (bm == null) {
							bm = new BitMatrix(records.size());							
						}
						support.markPairs(bm);		*/				
						/*
						 * NG constraint compromised: this means that a cluster
						 * of score currClusterScore caused the breach of the NG
						 * constraint on the matrix representing Clusters with
						 * scores above i*BottomUp.THRESH_STEP. This measn that
						 * that matrix cannot possibly represent the blocking
						 * result of our algorithm. The meaning is twofold: 1.
						 * There is not need to continue marking in this matrix
						 * 2. We know that the threshold needs to be increased
						 * because the set of clusters above score
						 * i*BottomUp.THRESH_STEP create large neighborhoods
						 */
			/*			if (bm.getMaxNG() > NG_PARAM * minSup) {
							double prevThresh = Utilities.scoreThreshold;
							Utilities.scoreThreshold = i * BottomUp.THRESH_STEP;
							System.out.println(" new score threshold: "
									+ Utilities.scoreThreshold);
							// remove all BMs located below the new threshold
							int delIndex =  Utilities.getIntForThresh(Utilities.scoreThreshold) - 1;
							for (int k =  Utilities.getIntForThresh(prevThresh); k <= delIndex; k++) {
								if (coverageIndex.containsKey(k)) {
									BitMatrix removed = coverageIndex.remove(k);
									if (removed != null) {
										removed = null;										
									}
								}
							}
						} else {
							coverageIndex.put(i, bm);
						}
					}
				}*/
			}			
		}
		finally{	
			FIRunnablePool.getInstance().returnRunnable(this); // completed running
		}

	}

	@Override
	public void clearAll() {
		this.setParams(null, 0, 0, null,null);
	}
	
	private static int cellForCluster(final double score) {
		return (int) Math.ceil(score / 0.05);
	}


}