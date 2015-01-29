package fimEntityResolution;


import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.enerj.core.SparseBitSet;
import org.enerj.core.SparseBitSet.Iterator;

import fimEntityResolution.interfaces.Clearer;
import fimEntityResolution.interfaces.SetPairIF;
/** should be made parallalizable**/
public class BitMatrix implements Clearer,SetPairIF{
	
	private  SparseBitSet bs;
	private long n, m;
	private Map<Integer, Integer> NGs = new HashMap<Integer, Integer>();
	int maxNG = 0;
	private BitSet coveredRows = new BitSet();
	private int numSet = 0;
	
	
	
	public static int SizeForSBSImp(long n){		
		return (int) (Math.ceil((Math.cbrt(n/64.0))));
	}
	
	public BitMatrix(long n, long m){
		this.n = n;
		this.m = m;		
		int size = SizeForSBSImp(n*m);
		bs = new SparseBitSet(size);
		bs.clear();
		
	}
	
	public BitMatrix(int n){
		//this(n+1,(n+1)/2);
		this(n+1,n+1);
	}
	
	
	public SparseBitSet getSBS(){
		return bs;
	}
	public int getMaxNG(){
		return maxNG;
	}
	
	public BitSet getCoveredRows(){
		return coveredRows;
	}
	private void setIndex(long i, long j){
	/*	if((i<0 || i > n) || (j < 0 || j > m)){
			System.out.println("can set only indexes in the range [0," + (n) + "],[0," + (m) + "]");
		}*/
		if(!bs.get(i*m+j)){
			bs.set(i*m+j);			
			updateNG(i);
			updateNG(j);
			updatCoverage(i);
			updatCoverage(j);
			numSet++;
		}
	
	}
	

	private void updateNG(long index){
		int currNG = 0;
		int i_index = new Long(index).intValue();
		if(NGs.containsKey(i_index)){
			currNG = NGs.get(i_index);
		}	
		currNG++;
		NGs.put(i_index, currNG);
		if(maxNG < currNG){
			maxNG = currNG;
		}
	}
	
	private void updatCoverage(long index){
		int i_index = new Long(index).intValue();
		coveredRows.set(i_index);
	}
	
	private boolean getIndex(long i, long j){
		/*	if((i<0 || i > n) || (j < 0 || j > m)){
				System.out.println("can set only indexes in the range [0," + (n) + "],[0," + (m) + "]");
			}*/
			return bs.get(i*m+j);
		}
	
	public int[] getSetPairFromIndex(long index, int[] retVal){		
		int rowIndx = (int) (index/m);
		int colIdx= (int)index-((int)m)*rowIndx;
		if(rowIndx < colIdx){
			retVal[0] = rowIndx; retVal[1] = colIdx;
		}
		else{
			retVal[0]=colIdx; retVal[1] = rowIndx;
		}
		return retVal;
		
	}
	@Override
	public void setPair(int i, int j, double score) {
		setPair((long)i,(long)j);
	}
	
	
	public void setPair(long i, long j){
		if(i<j){
			setIndex(i,j);
		}
		else{
			setIndex(j,i);
		}
	}

	public boolean getPair(long i, long j){
		if(i<j){
			return getIndex(i,j);
		}
		else{
			return getIndex(j,i);
		}
	}
	public void clearAll(){
		if(numSet == 0)
			return; //cleared
		Iterator It = bs.getIterator();
		while(It.hasNext()){
			long index = It.next();
			bs.clear(index);			
		}		
		NGs = null;
		NGs = new HashMap<Integer, Integer>(); 
		maxNG = 0;
		coveredRows = null;
		coveredRows = new BitSet();		
		numSet =0;
	}
	
	public long numOfSet(){
		return numSet;
	}
	
	public long numOfUnset(){
		return m*n-numSet;
	}
	
	
	public static void main(String[] args){
		BitMatrix bm2 = new BitMatrix(6, 6);		
		BitMatrix bm1 = new BitMatrix(6);
		long mem0 =Runtime.getRuntime().freeMemory();
		BitMatrix bm3 = new BitMatrix(100000000,100000000);		
		long mem1 = Runtime.getRuntime().freeMemory();
		System.out.println("memory used: " + (mem0-mem1) + " bytes");		
		bm2.setIndex(1,5);
		bm2.setIndex(1,2);
		bm2.setIndex(2,4);
		bm2.setIndex(3,2);
		bm2.setIndex(3,4);
		bm2.setIndex(3,4);
		bm2.setIndex(2,4);
		Iterator It = bm2.getSBS().getIterator();
		int[] pair = new int[2];
		while(It.hasNext()){
			long index = It.next();
			bm2.getSetPairFromIndex(index,pair);
			System.out.println(Arrays.toString(pair));
		}
		BitMatrix bm4 = new BitMatrix(3,3);
		bm2.setIndex(1,2);
		
		
		double[] ret = BitMatrix.TrueAndFalsePositives(bm1,bm4);
		double ret2 = BitMatrix.FalseNegatives(bm1,bm4);
		String s;
	}
	
	public static SparseBitSet and(SparseBitSet bs1, SparseBitSet bs2){		
		Iterator It =bs1.getIterator();	
		while(It.hasNext()){
			long index = It.next();
			if(!bs2.get(index)){
				bs1.clear(index);
			}
		}
		return bs1;
	}
	
	//TP+ FP - 1 in both the Ground Truth and in the result
	public static double[] TrueAndFalsePositives(BitMatrix GTMatrix, BitMatrix ActualMatrix){
		long TP = 0;
		long FP = 0;
		Iterator It = ActualMatrix.bs.getIterator();
		while(It.hasNext()){
			long nextSetBit = It.next();
			if(GTMatrix.bs.get(nextSetBit)){
				TP++;
			}
			else{
				FP++;
			}
			
		}
		return new double[]{TP,FP};		
	}
	
	public static double FalseNegatives(BitMatrix GTMatrix, BitMatrix ActualMatrix){		
		long FN = 0;
		Iterator It = GTMatrix.bs.getIterator();
		while(It.hasNext()){
			long nextSetBit = It.next();
			if(!ActualMatrix.bs.get(nextSetBit)){
				FN++;
			}		
		}		
		return FN;		
	}
	
	public BitMatrix or(final BitMatrix otherMatrix){
		Iterator It = otherMatrix.bs.getIterator();
		while(It.hasNext()){
			long nextSetBit = It.next();
			this.bs.set(nextSetBit);
		}		
		return this;		
	}

	
	
}
