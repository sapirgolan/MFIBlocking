package il.ac.technion.ie.bitsets;

import il.ac.technion.ie.model.BitSetIF;
import il.ac.technion.ie.data.structure.IFRecord;
import il.ac.technion.ie.model.RecordSet;
import il.ac.technion.ie.data.structure.SetPairIF;

import javax.transaction.NotSupportedException;
import java.util.Collections;
import java.util.List;

public class SingleBitBS implements BitSetIF{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5141946182144718806L;
	private int index=-1;
	private boolean set = false;

	@Override
	public BitSetIF and(BitSetIF other) throws NotSupportedException {
		if(set && !other.get(index)){
			set = false;
		}
		return this;
	}

	@Override
	public boolean get(int recordId) throws NotSupportedException {
		if(recordId == index){
			return set;
		}
		throw new NotSupportedException("class SingleBitBS contains status of bit " + index + " only");
	}

	@Override
	public int getCardinality() {
		if(set)
			return 1;
		return 0;
	}

	@Override
	public String getSupportString() {
		StringBuilder sb = new StringBuilder("{");
		sb.append(index);
		sb.append("}");
		return sb.toString();
	}

	@Override
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

	@Override
	public void clearAll() {
		set=false;
	}

	@Override
	public BitSetIF or(BitSetIF other) throws NotSupportedException {
		if(!set && other.get(index)){
			set = true;
		}
		return this;
	}

	@Override
	public List<IFRecord> getRecords() {
		IFRecord rec = RecordSet.values.get(index);
		return Collections.singletonList(rec);
	}

	public int markPairs(SetPairIF spf, double score) {
		return 0;//does nothing
	}

	@Override
	public void orInto(BitSetIF other) {
		if(set){
			other.set(index);
		}
	}

	@Override
	public List<Integer> getColumns() {
		return Collections.singletonList(index);
	}

	@Override
	public int markPairs(SetPairIF spf, double score, List<Integer> items) {
		return 0;//does nothing
	}

}
