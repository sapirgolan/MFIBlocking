package il.ac.technion.ie.bitsets;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIterator;
import il.ac.technion.ie.data.structure.IFRecord;
import il.ac.technion.ie.data.structure.SetPairIF;
import il.ac.technion.ie.exception.NotImplementedYetException;
import il.ac.technion.ie.model.BitSetIF;
import il.ac.technion.ie.model.RecordSet;

import javax.transaction.NotSupportedException;
import java.util.ArrayList;
import java.util.List;

public class EWAH_BitSet implements BitSetIF{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2522966848401355528L;
	EWAHCompressedBitmap comBS = null;

	public EWAH_BitSet(){
		comBS = new EWAHCompressedBitmap();
	}
	@Override
	//this method creates a new object!!!
	public BitSetIF and(final BitSetIF other) {
		EWAH_BitSet otherEWAH = (EWAH_BitSet)other;
		comBS = comBS.and(otherEWAH.comBS);
		return this;
	}

	@Override
	public boolean get(int recordId) {
		throw new NotImplementedYetException("not implemented yet");
	}

	@Override
	//has linear running time!!!
	public int getCardinality() {
		return comBS.cardinality();
	}

	@Override
	public String getSupportString() {
		StringBuilder sb = new StringBuilder("{");
		IntIterator it = comBS.intIterator();
		boolean first = true;
		while(it.hasNext()){
			long next = it.next();
			if(first){
				sb.append(next);
				first = false;
			}
			else{
				sb.append(", ").append(next);
			}
		}
		sb.append("}");
		return sb.toString();
	}

	@Override
	public void set(int recordId) {
		comBS.set(recordId);
	}
	@Override
	//created a new object
	public void clearAll() {
		comBS = null;
		comBS = new EWAHCompressedBitmap();
	}
	@Override
	//this method creates a new object!!!
	public BitSetIF or(final BitSetIF other) throws NotSupportedException {
		EWAH_BitSet otherEWAH = (EWAH_BitSet)other;
		comBS = comBS.or(otherEWAH.comBS);
		return this;
	}
	@Override
	public List<IFRecord> getRecords() {
		List<IFRecord> retVal = new ArrayList<IFRecord>(comBS.cardinality());
		IntIterator iterator = comBS.intIterator();
		while(iterator.hasNext()){
			int index = iterator.next();
			retVal.add(RecordSet.values.get(index));
		}		
		return retVal;
	}
	@Override
	public int markPairs(SetPairIF spf, double score, List<Integer> items) {		
		int cnt =0;
		List<Integer> positions = comBS.getPositions();		
		for(int i=0 ; i < positions.size() ; i++){
			for(int j=i+1 ; j < positions.size() ; j++){
				spf.setPair(positions.get(i), positions.get(j),score);	
				spf.setColumnsSupport(items,positions.get(i),positions.get(j));
				cnt++;
			}
		}
		return cnt;
	}
	@Override
	public void orInto(BitSetIF other) {
		List<Integer> positions = comBS.getPositions();	
		for(int i=0 ; i < positions.size() ; i++){
			other.set(positions.get(i));
		}
		
	}
	@Override
	public List<Integer> getColumns() {
		List<Integer> retVal = new ArrayList<Integer>(comBS.cardinality());
		IntIterator iterator = comBS.intIterator();
		while(iterator.hasNext()){
			//int index = iterator.next();
			retVal.add(iterator.next());
		}		
		return retVal;
	}

}
