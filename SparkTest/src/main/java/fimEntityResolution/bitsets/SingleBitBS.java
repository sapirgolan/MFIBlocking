package fimEntityResolution.bitsets;

import java.util.Collections;
import java.util.List;

import javax.transaction.NotSupportedException;

import fimEntityResolution.Utilities;
import fimEntityResolution.interfaces.BitSetIF;
import fimEntityResolution.interfaces.IFRecord;
import fimEntityResolution.interfaces.SetPairIF;

public class SingleBitBS implements BitSetIF{
	private int index=-1;
	private boolean set = false;

	
	public BitSetIF and(BitSetIF other) throws NotSupportedException {
		if(set && !other.get(index)){
			set = false;
		}
		return this;
	}

	
	public boolean get(int recordId) throws NotSupportedException {
		if(recordId == index){
			return set;
		}
		throw new NotSupportedException("class SingleBitBS contains status of bit " + index + " only");
	}

	
	public int getCardinality() {
		if(set)
			return 1;
		return 0;
	}

	
	public String getSupportString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append(index);
		sb.append("}");
		return sb.toString();
	}

	
	public void set(int recordId) {
		if(index < 0){
			this.index = recordId;
			this.set = true;
			return;
		}
		if(index == recordId){
			this.set = true;
			return;
		}
		System.out.println("attempting to use SingleBitBS for setting more than one record Ids - ignoring");
				
	}

	
	public void clearAll() {
		set=false;
	}

	
	public BitSetIF or(BitSetIF other) throws NotSupportedException {
		if(!set && other.get(index)){
			set = true;
		}
		return this;
	}

	
	public List<IFRecord> getRecords() {
		IFRecord rec = Utilities.globalRecords.get(index);
		return Collections.singletonList(rec);
	}

	
	public int markPairs(SetPairIF spf, double score) {
		return 0;//does nothing
	}

	
	public void orInto(BitSetIF other) {
		if(set){
			other.set(index);
		}
	}

}
