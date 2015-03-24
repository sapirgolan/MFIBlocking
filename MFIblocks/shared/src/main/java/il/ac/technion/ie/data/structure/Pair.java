package il.ac.technion.ie.data.structure;

import il.ac.technion.ie.model.RecordSet;
import il.ac.technion.ie.utils.StringSimTools;
import il.ac.technion.ie.utils.Utilities;

public class Pair {

	public int r1;
	public int r2;
	private double score = -1;
	private boolean covered = false;
	
	public Pair(int r1, int r2){
		this.r1 = r1;
		this.r2 = r2;
	}
	
	public boolean equals(Object other){
		Pair otherPair = (Pair)other;
		if(otherPair.r1 == r1 && otherPair.r2 == r2){
			return true;
		}
		if(otherPair.r1 == r2 && otherPair.r2 == r1){
			return true;
		}
		
		return false;
	}
	
	public int hashCode(){
		return r1 ^ r2;
	}
	
	public double getScore(){
		if(score < 0){
			score = StringSimTools.softTFIDF(RecordSet.values.get(r1), RecordSet.values.get(r2));
		}
		return score;
	}
	
	public void setCovered(boolean val){
		this.covered = val;
	}
	
	public boolean getCovered(){
		return covered;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(RecordSet.values.get(r1).toString()).append(Utilities.NEW_LINE).
				append(RecordSet.values.get(r2).toString()).append(Utilities.NEW_LINE);
		sb.append("with score: " + getScore());
		return sb.toString();
		
	}
	
	public boolean sameSource(){
		String src1 = RecordSet.values.get(r1).getSrc();
		String src2 = RecordSet.values.get(r2).getSrc();
		if(src1 == null || src2 == null){ //if null then assume different sources		
			return false;
		}
		return (src1.equalsIgnoreCase(src2));
	}
	
	public String simpleToString(){
		StringBuilder sb = new StringBuilder();
		sb.append(RecordSet.values.get(r1).getRecordStr()).append(Utilities.NEW_LINE)
		.append(RecordSet.values.get(r2).getRecordStr()).append(Utilities.NEW_LINE);
		return sb.toString();
		
	}
}
