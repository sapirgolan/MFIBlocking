package fimEntityResolution.bitsets;


import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import javax.transaction.NotSupportedException;

import org.enerj.core.SparseBitSet;
import org.enerj.core.SparseBitSet.Iterator;

import fimEntityResolution.BitMatrix;
import fimEntityResolution.RecordSet;
import fimEntityResolution.Utilities;
import fimEntityResolution.interfaces.BitSetIF;
import fimEntityResolution.interfaces.IFRecord;
import fimEntityResolution.interfaces.SetPairIF;

public class SBS_BitSet implements BitSetIF{	 
	/**
	 * 
	 */
	private static final long serialVersionUID = 5342431995617151960L;
	private SparseBitSet sbs = null;
	private int cardinality = 0;	
	
	private static int SizeForSBSImp(long n){		
		return (int) (Math.ceil((Math.cbrt(n/64.0))));
	}
	
	
	public SBS_BitSet(){
		int size = SizeForSBSImp(RecordSet.DB_SIZE);		
		sbs = new SparseBitSet(size);
		cardinality = 0;
	}
	
	public SBS_BitSet(final int size){
		sbs = new SparseBitSet(SizeForSBSImp(size));
		cardinality = 0;
	}
	
	@Override
	public synchronized BitSetIF and(final BitSetIF other) throws NotSupportedException {
		for(long i=getNextSetBitIndex(0); i >= 0; i=getNextSetBitIndex(i+1)) {
			if(!other.get((int) i)){
				sbs.clear(i);
				cardinality--;
			}
		}		
		return this;
	}

	@Override
	public int getCardinality() {
		return cardinality;
	}

	@Override
	public String getSupportString() {
		StringBuilder sb = new StringBuilder("{");
		Iterator it = sbs.getIterator();
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
	public synchronized void set(final int recordId) {
		if(sbs.get(recordId))
			return;
		sbs.set(recordId);
		cardinality++;	
	}

	public boolean get(final int recordId){
		return sbs.get(recordId);
	}

	@Override
	public synchronized void clearAll() {	
		Iterator It = sbs.getIterator();
		while(It.hasNext()){
			long index = It.next();
			sbs.clear(index);			
		}				
		cardinality = 0;
	}

	@Override
	public synchronized BitSetIF or(final BitSetIF other) throws NotSupportedException {
		SBS_BitSet otherBS = (SBS_BitSet)other;
		for(long i=otherBS.getNextSetBitIndex(0); i >= 0; i=otherBS.getNextSetBitIndex(i+1)) {
			this.set((int)i);
		}		
		return this;	
				
	}

	@Override
	public synchronized List<IFRecord> getRecords() {		
		List<IFRecord> retVal = new ArrayList<IFRecord>(this.getCardinality());
		for(long i=getNextSetBitIndex(0); i >= 0; i=getNextSetBitIndex(i+1)) {
			retVal.add(RecordSet.values.get((int)i));
		}
		if(retVal.size() == 0){
			System.out.println("inside getRecords: cardinality: " + this.getCardinality() + " retVal.size(): " + retVal.size());
		}
		return retVal;
	}

	@Override
	public synchronized int markPairs(SetPairIF spf, double score) {
		int cnt = 0;
		for(long i=getNextSetBitIndex(0); i >= 0; i=getNextSetBitIndex(i+1)) {
			for(long j=getNextSetBitIndex(i+1); j >= 0 ; j=getNextSetBitIndex(j+1)) {
				spf.setPair((int)i, (int)j,score);			
				cnt++;
			}
		}
		return cnt;
	}
	
	/**
	 * Determines if there are more set bits to get from the iterator.
	 * 
	 * @return true if there are more set bits to get from the iterator, else
	 *         false.
	 */
	private synchronized long getNextSetBitIndex(long mNextBitIndex) {
		long retVal;
		if (mNextBitIndex >= sbs.getMaxSize()) {
			return -1;
		}

		try {
			// Scan ahead and save position while we're at it.
			retVal = sbs.getNextSetBitIndex(mNextBitIndex);
			return retVal;
		} catch (NoSuchElementException e) {
			return -1;
		}
	}
    
	  public static void main(String[] args){
		    SBS_BitSet a = new SBS_BitSet(5000);
	    	a.set(1); a.set(1000); a.set(2000);
	    	System.out.println(a.getSupportString());
	    	int n = a.getCardinality();
	    	
	    	SBS_BitSet b = new SBS_BitSet(5000);
	    	b.set(1); b.set(1000); b.set(3000);
	    	System.out.println(b.getSupportString());
	    	
	    	BitMatrix bm = new BitMatrix(4000);
	    	b.markPairs(bm,0);
	    	boolean t1 = bm.getPair(1,1000);
	    	boolean t2 = bm.getPair(1,3000);
	    	boolean t3 = bm.getPair(1000,1);
	    	boolean t4 = bm.getPair(3000,1);
	    	boolean t5 = bm.getPair(3000,1000);
	    	boolean t6 = bm.getPair(1000,3000);
	    	boolean t7 = bm.getPair(2000,3000);
	    	
	    	BitSetIF d=null;
	    	try {
	    		d = a.or(b);
			} catch (NotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	System.out.println(d.getSupportString());
	    	
	    	BitSetIF bsf = new Java_BitSet();
	    	d.orInto(bsf);
	    	System.out.println(bsf.getSupportString());
	    	
	    }


	@Override
	public void orInto(BitSetIF other) {
		for(long i=getNextSetBitIndex(0); i >= 0; i=getNextSetBitIndex(i+1)) {
			other.set((int) i);
		}
		
	}
	
}
