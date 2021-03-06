package il.ac.technion.ie.model;


import il.ac.technion.ie.data.structure.Clearer;
import il.ac.technion.ie.data.structure.IFRecord;
import il.ac.technion.ie.data.structure.SetPairIF;

import javax.transaction.NotSupportedException;
import java.util.List;


public interface BitSetIF extends Clearer {

	public void set(int recordId);
	public int getCardinality();
	public String getSupportString();	
	public BitSetIF and(final BitSetIF other) throws NotSupportedException;
	public BitSetIF or(final BitSetIF other) throws NotSupportedException;
	public boolean get(int recordId) throws NotSupportedException;
	public List<IFRecord> getRecords();
	public List<Integer> getColumns();
	public int markPairs(SetPairIF spf, double score, List<Integer> items);
	/**
	 * Will set the current set bits into the other IF
	 * @param other
	 */
	public void orInto(BitSetIF other);
}
