package candidateMatches;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class RecordMatches {

	private ConcurrentHashMap<Integer,CandidateMatch> candidateSet;
	private LimitedMinHeap<CandidateMatch> limitedMinHeap;
	private int maxSize;
	private double minThresh = 0.0;
	
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
	
	
	/**
	 * 
	 * @param recordId
	 * @param score
	 * @return the min score match for this record
	 */
	public synchronized boolean addCandidate(int recordId, double score){
		CandidateMatch cand = null;
		boolean added = true;
		if(candidateSet.containsKey(recordId)){		
			added = false;
			cand = candidateSet.get(recordId);
			if(cand.getScore() < score){ //update score only if larger
				cand.setScore(score);
				limitedMinHeap.increaseKey(cand.getHeapPos());				
			}
		}
		else{
			//will not be added. heap is already at max size
			//and won't accept candidates of smaller scores
			if(limitedMinHeap.size() >= maxSize && score <= minThresh){ 
				return added;
			}
			CandidateMatch cm = new CandidateMatch(recordId, score);
			if(limitedMinHeap.size() >= maxSize){ //means that score > minScore()
				//first delete former minimum from the hashSet
				int minId = minRecId();
				candidateSet.remove(minId);
			}							
			limitedMinHeap.insert(cm);			
			candidateSet.put(recordId, cm);			
		}
		if(limitedMinHeap.size() >= maxSize){
			minThresh = minScore();
		}
		return added;
	}

	public synchronized void removeMin(){
		CandidateMatch removed = (CandidateMatch) limitedMinHeap.pop();
		candidateSet.remove(removed.getRecordId());
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
	private final static String NEWLINE =System.getProperty("line.separator");
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
	
	public boolean isMatched(int recId){
		return candidateSet.containsKey(recId);
	}
	
	public static void main(String[] args){
		RecordMatches rm = new RecordMatches(5);
		rm.addCandidate(1, 0.2);
		System.out.println(rm.toString());		
		rm.addCandidate(5, 0.56);
		System.out.println(rm.toString());		
		rm.addCandidate(5, 0.78);
		System.out.println(rm.toString());		
		rm.addCandidate(8, 0.9);
		System.out.println(rm.toString());		
		rm.addCandidate(10, 0.1);
		System.out.println(rm.toString());		
		rm.addCandidate(11, 0.15);
		System.out.println(rm.toString());		
		rm.addCandidate(13, 0.44);
		System.out.println(rm.toString());
		System.out.println();
		rm.addCandidate(17, 0.05);
		System.out.println(rm.toString());
		rm.addCandidate(17, 0.1);
		System.out.println(rm.toString());
		rm.addCandidate(8, 0.67);
		System.out.println(rm.toString());
		rm.addCandidate(1, 0.4);
		System.out.println(rm.toString());
		rm.addCandidate(1, 0.7);
		rm.addCandidate(17, 33);
		System.out.println(rm.toString());
		
	}
}
