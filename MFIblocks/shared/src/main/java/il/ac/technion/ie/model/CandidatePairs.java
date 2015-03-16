package il.ac.technion.ie.model;

import il.ac.technion.ie.data.structure.SetPairIF;
import il.ac.technion.ie.data.structure.BitMatrix;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link #allMatches} is a {@link ConcurrentHashMap} with key representing record ID and value is {@link RecordMatches} which is all records that highest
 * similarity to current record ID
 *
 */
public class CandidatePairs implements SetPairIF {

	private static final long serialVersionUID = -3723310157735251353L;
	private ConcurrentHashMap<Integer,RecordMatches> allMatches;
	private int maxMatches;
	private double minThresh = 0.0;
	private boolean limited = true;

    static final Logger logger = Logger.getLogger(CandidatePairs.class);


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
	
	public ConcurrentHashMap<Integer, RecordMatches> getAllMatches() {
		return allMatches;
	}
	
	public Set<Entry<Integer, RecordMatches>> getAllMatchedEntries(){
		return allMatches.entrySet();
	}
	
	public Iterator<Entry<Integer, RecordMatches>> getIterator() {
		return allMatches.entrySet().iterator();
	}
	
	public void addAll(final CandidatePairs other){
        for (Entry<Integer, RecordMatches> entry : other.allMatches.entrySet()) {

            Integer recordId = entry.getKey();
            RecordMatches recordMatches = entry.getValue();
            if ( !recordHasAnyMatch(recordId) ) {
                // This is the first time we handle the possible matches of entry.getKey()
                allMatches.put(recordId, recordMatches);
            } else {
                // Add more possible matches for entry.getKey()
                RecordMatches currRM = allMatches.get(recordId);
                RecordMatches otherRM = recordMatches;
                for (CandidateMatch cm : otherRM.getCandidateMatches()) {
                    currRM.addCandidate(cm.getRecordId(), cm.getScore());
                }
            }
        }
    }

    /**
	 * Tries to create a pair of records with IDs <b>i</b> and <b>j</b>.<br>
	 * It tries to add to the block of record <b>i</b> record <b>j</b> and to the block of record <b>j</b> record <b>i</b><br>
	 * If record with ID <b>i</b> already has a record with id <b>j</b> than does nothing.<br>
	 * <p>
	 * If block with seed ID <b>i</b> reached the maximum of records than does the following:<br>
	 * <ul>
	 * <li>
	 * 	if there is a record whose score < <code>score</code> than:
	 * 	<ol>
	 * 		<li>this record is removed and record j is added</li>
	 * 		<li>minThresh of block whose seed is record i is modified</li>
	 * 	</ol>
	 * </li>
	 * <li>if <code>score</code> < all records score of block, does nothing</li>
	 * </ul>
	 * 
	 * 
	 * </p>
	 **/
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
		RecordMatches retVal;
		if(recordHasAnyMatch(index)){
			retVal = allMatches.get(index);
		} else {
            retVal = createNewRecordMatch(index);
		}			
		return retVal;
	}

    private RecordMatches createNewRecordMatch(int index) {
        RecordMatches retVal;
        if(limited){
            retVal = new RecordMatches(maxMatches);
        } else{
            retVal = new RecordMatches();
        }
        allMatches.put(index, retVal);
        return retVal;
    }

    public double getMinThresh(){
		return minThresh;
	}
	
	private void removeBelowThresh(){
		long start = System.currentTimeMillis();
        logger.debug("about to a record. minThresh:" + minThresh +" maxMatches: " + maxMatches);
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
	
	/***
	 * Removes pairs that didn't pass the threshold (min_th) and export to BitMatrix (Jonathan Svirsky)
	 * @return BitMatrix object
	 */
	public BitMatrix exportToBitMatrix(){
		long start = System.currentTimeMillis();

		removeBelowThresh();
		BitMatrix bm = new BitMatrix(RecordSet.DB_SIZE);
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
		if(recordHasAnyMatch(sourceRecordId)){
			RecordMatches recordMatches = allMatches.get(sourceRecordId);
			retVal = recordMatches.isMatched(comparedRecordId);
		}
		if(!retVal){
			if(recordHasAnyMatch(comparedRecordId)){
				RecordMatches recordMatches = allMatches.get(comparedRecordId);
				retVal = recordMatches.isMatched(sourceRecordId);
			}
		}
		return retVal;
	}

    private boolean recordHasAnyMatch(int sourceRecordId) {
        return allMatches.containsKey(sourceRecordId);
    }

    //TODO: CHECK IT
	//TP+ FP - 1 in both the Ground Truth and in the result
	public long[] calcTrueAndFalsePositives(CandidatePairs actualCPs) throws NullPointerException{
        long TP = 0;
        long FP = 0;
        long FN = 0;

        Set<Set<Integer>> truePairs = new HashSet<>();
        Set<Set<Integer>> actualPairs = new HashSet<>();
        for (Entry<Integer, RecordMatches> entry : actualCPs.allMatches.entrySet()) { //run over all records
            for (CandidateMatch cm : entry.getValue().getCandidateMatches()) { //for each record, check out its match
                Set<Integer> temp = new HashSet<Integer>();
                temp.add(cm.getRecordId());
                temp.add(entry.getKey());
                actualPairs.add(temp);
            }
        }
        for (Entry<Integer, RecordMatches> entry : this.allMatches.entrySet()) { //run over all records
            for (CandidateMatch cm : entry.getValue().getCandidateMatches()) { //for each record, check out its match
                //count++;
                Set<Integer> temp = new HashSet<Integer>();
                temp.add(cm.getRecordId());
                temp.add(entry.getKey());
                truePairs.add(temp);
            }
        }

        Set<Set<Integer>> tempTruePairs = new HashSet<>();
        tempTruePairs.addAll(truePairs);
        tempTruePairs.removeAll(actualPairs);
        FN = tempTruePairs.size();
        //intersection between truePairs and actualPairs
        truePairs.retainAll(actualPairs);
        TP = truePairs.size();
        //remove intersection from actualPairs
        actualPairs.removeAll(truePairs);
        FP = actualPairs.size();
        return new long[]{TP, FP, FN};
    }
	
	public static double FalseNegatives(CandidatePairs trueCPs, CandidatePairs actualCPs){		
		long FN = 0;
		for (Entry<Integer,RecordMatches> entry: trueCPs.getAllMatches().entrySet()) { //run over all records
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
		CandidatePairs cps = new CandidatePairs(10);
		cps.setPair(1, 2, 0.3);
		cps.setPair(1, 3, 0.4);
		cps.setPair(1, 4, 0.5);
		cps.setPair(1, 7, 0.5);		
		cps.setPair(2, 7, 0.5);
		cps.setPair(2, 4, 0.3);
		cps.isPairSet(2, 1);
		cps.isPairSet(2, 7);
		cps.isPairSet(4, 7);
		cps.isPairSet(1, 4);		
		System.out.println("minth: " + cps.getMinThresh());
		
		CandidatePairs gt = new CandidatePairs();
		gt.setPair(1, 2,0);
		gt.setPair(1, 4,0);
		gt.setPair(7, 2,0);
		gt.setPair(4, 2,0);
		gt.setPair(4, 5,0);
		gt.setPair(5, 6,0);
		gt.isPairSet(2,7);
		gt.isPairSet(7,2);
		long[] TPFP = gt.calcTrueAndFalsePositives(cps);
		double FN = FalseNegatives(gt,cps);
		System.out.println("TPFP: " + Arrays.toString(TPFP));
		System.out.println("FN: " + FN);
		
		cps.addAll(gt);
		cps.isPairSet(5, 4);
		cps.isPairSet(4,5);
		cps.isPairSet(6,5);
		cps.isPairSet(1,7);
		cps.isPairSet(4,2);
		cps.isPairSet(4,8);
	}
}
