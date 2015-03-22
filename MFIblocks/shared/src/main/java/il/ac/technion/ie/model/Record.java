package il.ac.technion.ie.model;


import il.ac.technion.ie.data.structure.IFRecord;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Record implements IFRecord {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private Map<Integer,Integer> itemToFrequency = new HashMap<Integer, Integer>();
	private String recordStr = null;
	private String NEW_LINE = System.getProperty("line.separator");
	private String src = null; // if we want to keep track of the record's source
//	private SparseBitSet ng = new SparseBitSet(20);
	private long currCardinality = 0;
	
		
	public Record(int id, String recordStr){
		this.id = id;
		this.recordStr = recordStr;
	}
	
	public String getRecordStr(){
		return recordStr;
	}
	
	public void addItem(int itemId){
		int freq = 1;
		if(itemToFrequency.containsKey(itemId)){
			freq = itemToFrequency.get(itemId) + 1;
		}
		itemToFrequency.put(itemId, freq);
	}
	
	public Map<Integer,Integer> getItemsToFrequency(){
		return itemToFrequency;
	}
	
	public int getId(){
		return id;
	}
	
	public String getSrc(){
		return src;
	}
	
	public void setSrc(String src){
		this.src = src;
	}
/*	public void clearNG(){	
		if(currCardinality > 0){
			ng.clear();
			currCardinality = 0;
		}
	}
	
	
	public void updateNG(SparseBitSet support){
		Iterator It = support.getIterator();
		while(It.hasNext()){
			long recordId = It.next();
			ng.set(recordId);
		}
		
		currCardinality = ng.getNumBitsSet();
	}
	
	public void updateNG(BitSet support){
		for(int recInd=support.nextSetBit(0); recInd>=0; recInd=support.nextSetBit(recInd+1)) {			
			ng.set(recInd);
		}
		
		currCardinality = ng.getNumBitsSet();
	}
	
	public long getNG(){
		return currCardinality;
	}
	*/
	private final static String WORD_SEP = " ";
	
	public int getSize(){
		return itemToFrequency.size();
	}
	
	/**
	 * Write only the items which passed the support constraints
	 * @param appropriateItems
	 * @return
	 */
	public String getNumericline(Set<Integer> appropriateItems){
		Set<Integer> uniqueValues=new java.util.HashSet<Integer>();
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<Integer,Integer> itemIdFreqPair : getItemsToFrequency().entrySet()) {
			if(appropriateItems.contains(itemIdFreqPair.getKey())){
				for(int i=0 ; i < itemIdFreqPair.getValue() ; i++){
					if (!uniqueValues.contains(itemIdFreqPair.getKey())){
						sb.append(itemIdFreqPair.getKey()).append(WORD_SEP);
						uniqueValues.add(itemIdFreqPair.getKey());
					}
					
				}
			}
		}
		return sb.toString();	
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(this.id).append(":");
		if(recordStr != null){
			sb.append(recordStr).append(NEW_LINE);
		}
		for (Map.Entry<Integer,Integer> itemIdFreqPair : getItemsToFrequency().entrySet()) {
			for(int i=0 ; i < itemIdFreqPair.getValue() ; i++){
				sb.append(itemIdFreqPair.getKey()).append(WORD_SEP);
			}
		}	
	
		return sb.toString();
	}
}
