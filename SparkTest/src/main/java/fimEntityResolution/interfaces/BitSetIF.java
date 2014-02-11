package fimEntityResolution.interfaces;

import java.util.List;

import javax.transaction.NotSupportedException;


public interface BitSetIF extends Clearer{

	public void set(int recordId);
	public int getCardinality();
	public String getSupportString();	
	public BitSetIF and(final BitSetIF other) throws NotSupportedException;
	public BitSetIF or(final BitSetIF other) throws NotSupportedException;
	public boolean get(int recordId) throws NotSupportedException;
	public List<IFRecord> getRecords();
	public int markPairs(SetPairIF spf, double score);
	/**
	 * Will set the current set bits into the other IF
	 * @param other
	 */
	public void orInto(BitSetIF other);
}
