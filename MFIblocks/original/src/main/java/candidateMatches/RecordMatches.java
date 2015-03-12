package candidateMatches;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is responsible to store the Top K match candidates for a record.
 * The candidates are stored in candidateSet. <br>
 * The default threshold is 0 and it increases once the candidateSet has reached is Max size that is defined by maxSize
 *
 */
public class RecordMatches {

	private ConcurrentHashMap<Integer,CandidateMatch> candidateSet;
	private LimitedMinHeap<CandidateMatch> limitedMinHeap;
	private int maxSize;
	private double minThresh = 0.0;
	private final static String NEWLINE =System.getProperty("line.separator");
	
	public RecordMatches(int maxSize){
		this.maxSize = maxSize;
		candidateSet = new ConcurrentHashMap<Integer,CandidateMatch>(maxSize);
		limitedMinHeap = new LimitedMinHeap(CandidateMatchComparator.getInstance(), maxSize);		
	}
	
	public RecordMatches(){		
		this.maxSize = Integer.MAX_VALUE;
		candidateSet = new ConcurrentHashMap<Integer,CandidateMatch>();
		limitedMinHeap = new LimitedMinHeap(CandidateMatchComparator.getInstance());		
	}
	
	public int size(){
		return candidateSet.size();
	}
	
	public final Set<Integer> getMatchedRecordsIds() {
		return candidateSet.keySet();
	}
	
	/**
	 * 
	 * @param recordId
	 * @param score
	 * @return the min score match for this record
	 */
	public synchronized boolean addCandidate(int recordId, double score){
		boolean added = true;
		//if candidate exists
		if(isRecordExistsAsPotentialMatch(recordId)){
			added = false;
			CandidateMatch existingCandidate = candidateSet.get(recordId);
			// update score only if larger - can happen if both records exists in several clusters
			// and in one of them, the cluster's score is greater.
			// (the resemblance score of two records is the cluster score)
			if(existingCandidate.getScore() < score){
                increaseCandidateScore(score, existingCandidate);
			}
		}
		else{
			// If heap is at the max size and the resemblance score of given candidate is smaller
			// than the minimum resemblance score in the heap, candidate will not be added
			if(isHeapFull() ){
                // new candidate score is smaller than current smallest candidate
                if (score <= minThresh) {
                    return added;
                } else {
                    removeLowestSimMatch();
                }
            }
            CandidateMatch newCandidate = new CandidateMatch(recordId, score);
            limitedMinHeap.insert(newCandidate);
			candidateSet.put(recordId, newCandidate);			
		}
		if(isHeapFull()){
			minThresh = minScore();
		}
		return added;
	}

    private void removeLowestSimMatch() {
        int minId = minRecId();
        candidateSet.remove(minId);
    }

    private void increaseCandidateScore(double score, CandidateMatch existingCandidate) {
        existingCandidate.setScore(score);
        limitedMinHeap.increaseKey(existingCandidate.getHeapPos());
    }

    private boolean isRecordExistsAsPotentialMatch(int recordId) {
        return candidateSet.containsKey(recordId);
    }

    private boolean isHeapFull() {
        return limitedMinHeap.size() >= maxSize;
    }

    public synchronized void removeMin(){
		CandidateMatch removedMatched = (CandidateMatch) limitedMinHeap.pop();
		candidateSet.remove(removedMatched.getRecordId());
	}
	
	public double getMinThresh(){
		return minThresh;
	}
	
	public double minScore(){
		return ((CandidateMatch)limitedMinHeap.top()).getScore();
	}
	
	private int minRecId(){
		return ((CandidateMatch)limitedMinHeap.top()).getRecordId();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Min thresh " + minThresh).append(NEWLINE);
		for (Entry<Integer,CandidateMatch> entry : candidateSet.entrySet()) {
			sb.append(entry.getKey()).append(":").append(entry.getValue().toString()).append("   ");
		}
		sb.append(NEWLINE);
		sb.append(limitedMinHeap.toString()).append(NEWLINE).append(NEWLINE);
		return sb.toString();
	}
	
	public Collection<CandidateMatch> getCandidateMatches(){
		return candidateSet.values();
	}
	
	public ConcurrentHashMap<Integer,CandidateMatch> getCandidateSet() {
		return this.candidateSet;
	}
	
	public Set<Integer> getMatchedIds(){
		return candidateSet.keySet();
	}
	
	public boolean isMatched(int recId){
		return isRecordExistsAsPotentialMatch(recId);
	}
	
}
