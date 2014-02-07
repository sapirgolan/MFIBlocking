package candidateMatches;

import java.util.Arrays;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.javamex.classmexer.MemoryUtil;
import com.javamex.classmexer.MemoryUtil.VisibilityFilter;

import fimEntityResolution.BitMatrix;
import fimEntityResolution.Utilities;
import fimEntityResolution.interfaces.SetPairIF;

/**
 * {@link #allMatches} is a {@link ConcurrentHashMap} with key representing record ID and value is {@link RecordMatches} which is all records that highest
 * similarity to current record ID
 *
 */
public class CandidatePairs implements SetPairIF{

	private ConcurrentHashMap<Integer,RecordMatches> allMatches;
	private int maxMatches;
	private double minThresh = 0.0;
	private boolean limited = true;
	
	public CandidatePairs(int maxMatches){
		allMatches = new ConcurrentHashMap<Integer, RecordMatches>();
		this.maxMatches = maxMatches;
		limited = true;
	}
	//unlimited
	public CandidatePairs(){
		allMatches = new ConcurrentHashMap<Integer, RecordMatches>();
		this.maxMatches = Integer.MAX_VALUE;
		limited = false;
	}
	
	public void addAll(final CandidatePairs other){
		for (Entry<Integer,RecordMatches> entry: other.allMatches.entrySet()) {
			if(!allMatches.containsKey(entry.getKey())){
				allMatches.put(entry.getKey(), entry.getValue());
			}
			else{
				RecordMatches currRM = allMatches.get(entry.getKey());
				RecordMatches otherRM = entry.getValue();
				for (CandidateMatch cm : otherRM.getCandidateMatches()) {
					currRM.addCandidate(cm.getRecordId(), cm.getScore());
				}
			}
			
		}
	}
	
	@Override
	public void setPair(int i, int j, double score) {
		if(score < minThresh)
			return;
		
		RecordMatches ri = getRecordMatch(i);
		ri.addCandidate(j, score);		
		RecordMatches rj = getRecordMatch(j);
		rj.addCandidate(i, score);
		minThresh = Math.max(minThresh,ri.getMinThresh());
		minThresh = Math.max(minThresh,rj.getMinThresh());
	}

	private synchronized RecordMatches getRecordMatch(int index){
		RecordMatches retVal = null;
		if(allMatches.containsKey(index)){
			retVal = allMatches.get(index);
		}
		else{
			if(limited){
				retVal = new RecordMatches(maxMatches);
			}
			else{
				retVal = new RecordMatches();
			}
			allMatches.put(index, retVal);
		}			
		return retVal;
	}
	
	public double getMinThresh(){
		return minThresh;
	}
	
	private void removeBelowThresh(){
		long start = System.currentTimeMillis();
		System.out.println("DEBUG: about to removeBelowThresh, minThresh: " + minThresh);
		System.out.println("DEBUG: about to removeBelowThresh, maxMatches: " + maxMatches);
		for (RecordMatches matches : allMatches.values()) {
			while(matches.size() > 0 && matches.minScore() < minThresh){
				matches.removeMin();
			}			
		}
		System.out.println("DEBUG: time to removeBelowThresh: " + (System.currentTimeMillis()-start)/1000.0 + " seconds");
		System.gc();
	}
	
	public double memoryUsage(){
		return (MemoryUtil.deepMemoryUsageOfAll(allMatches.values(), VisibilityFilter.ALL)/Math.pow(2, 30));
	}
	/***
	 * Removes pairs that didn't pass the threshold (min_th) and export to BitMatrix (Jonathan Svirsky)
	 * @return BitMatrix object
	 */
	public BitMatrix exportToBitMatrix(){
		long start = System.currentTimeMillis();
		System.out.println("DEBUG: total memory used by CandidatePairs: " +	memoryUsage() + " GB");
		
		removeBelowThresh();
		BitMatrix bm = new BitMatrix(Utilities.DB_SIZE);
		for (Entry<Integer, RecordMatches> entry: allMatches.entrySet()) {
			for(CandidateMatch cm: entry.getValue().getCandidateMatches()){
				bm.setPair(entry.getKey(), cm.getRecordId());
			}
		}
		System.out.println("DEBUG: time to exportToBM: " + (System.currentTimeMillis()-start)/1000.0 + " seconds");
		
		return bm;
	}
	
	/**
	 * Checks if either <b>sourceRecordId</b> as a matching who is <b>comparedRecordId</b> or the other way.<br>
	 * It is possible that record i won't have any matches, but record j will have a match who is record i 
	 * @param sourceRecordId
	 * @param comparedRecordId
	 * @return
	 */
	public boolean isPairSet(int sourceRecordId, int comparedRecordId){
		boolean retVal = false;
		//if has any matches for sourceRecordId
		if( allMatches.containsKey(sourceRecordId) ){
			RecordMatches recordMatches = allMatches.get(sourceRecordId);
			retVal = recordMatches.isMatched(comparedRecordId);
		}
		if(!retVal){
			if(allMatches.containsKey(comparedRecordId)){
				RecordMatches recordMatches = allMatches.get(comparedRecordId);
				retVal = retVal || recordMatches.isMatched(sourceRecordId);
			}
		}
		return retVal;
	}
	
	//TP+ FP - 1 in both the Ground Truth and in the result
	public static double[] TrueAndFalsePositives(CandidatePairs trueCPs, CandidatePairs actualCPs){
		long truePositive = 0;
		long falsePositive = 0;
		//loop for each recored that you have found some items that might represent the same entity as he.
		for (Entry<Integer,RecordMatches> candidatePair: actualCPs.allMatches.entrySet()) { //run over all records
			int recId = candidatePair.getKey();
			//obtain all the records that might refer the same entity as current record
			for (CandidateMatch candidateMatch : candidatePair.getValue().getCandidateMatches()) { //for each record, check out its matches
				int otherRecId = candidateMatch.getRecordId();
				//the if will prevent us from comparing the same pair ot triple several times
				if(recId < otherRecId){ //we assume this is how trueCPs is built
					if(trueCPs.isPairSet(recId, otherRecId)){
						truePositive++;
					}
					else{
						falsePositive++;
					}
				}
			}
		}
		return new double[]{truePositive,falsePositive};	
		
	}
	
	public static double FalseNegatives(CandidatePairs trueCPs, CandidatePairs actualCPs){		
		long FN = 0;
		for (Entry<Integer,RecordMatches> entry: trueCPs.allMatches.entrySet()) { //run over all records
			int recId = entry.getKey();
			for (CandidateMatch cm : entry.getValue().getCandidateMatches()) { //for each record, check out its matches
				int otherRecId = cm.getRecordId();
				if(recId < otherRecId){
					if(!actualCPs.isPairSet(recId, otherRecId)){
						FN++;
					}
				}
			}
		}
		return FN;	
	
	}
	
	
	public static void main(String[] args){
		boolean f;
		CandidatePairs cps = new CandidatePairs(10);
		cps.setPair(1, 2, 0.3);
		cps.setPair(1, 3, 0.4);
		cps.setPair(1, 4, 0.5);
		cps.setPair(1, 7, 0.5);		
		cps.setPair(2, 7, 0.5);
		cps.setPair(2, 4, 0.3);
		f=cps.isPairSet(2, 1);
		f=cps.isPairSet(2, 7);
		f=cps.isPairSet(4, 7);
		f=cps.isPairSet(1, 4);		
		System.out.println("minth: " + cps.getMinThresh());
		
		CandidatePairs gt = new CandidatePairs();
		gt.setPair(1, 2,0);
		gt.setPair(1, 4,0);
		gt.setPair(7, 2,0);
		gt.setPair(4, 2,0);
		gt.setPair(4, 5,0);
		gt.setPair(5, 6,0);
		f = gt.isPairSet(2,7);
		f= gt.isPairSet(7,2);
		double[] TPFP = TrueAndFalsePositives(gt, cps);
		double FN = FalseNegatives(gt,cps);
		System.out.println("TPFP: " + Arrays.toString(TPFP));
		System.out.println("FN: " + FN);
		
		cps.addAll(gt);
		f = cps.isPairSet(5, 4);
		f = cps.isPairSet(4,5);
		f = cps.isPairSet(6,5);
		f = cps.isPairSet(1,7);
		f = cps.isPairSet(4,2);
		f = cps.isPairSet(4,8);
		
	}
}
