package il.ac.technion.ie.model;


/**
 * Represents a candidate comparison for some record
 * @author Tal
 *
 */
public class CandidateMatch implements PositionUpdater {

	private final int recordId;
	private double score;
	private int heapPos;
	
	public CandidateMatch(int recordId, double score){
		this.recordId = recordId;
		this.score = score;	
	}
	
	public int getRecordId(){
		return recordId;
	}
	
	public int getHeapPos(){
		return heapPos;
	}
	
	public void setHeapPos(int heapPos){
		this.heapPos = heapPos;
	}
	
	public double getScore(){
		return score;
	}
	
	public void setScore(double newScore){
		this.score = newScore;
	}
	
	public String toString(){
		return Integer.toString(recordId)+":"+Double.toString(score)+","+Integer.toString(getHeapPos());
	}
	
	@Override
	public int hashCode(){
		return recordId;
	}

}
