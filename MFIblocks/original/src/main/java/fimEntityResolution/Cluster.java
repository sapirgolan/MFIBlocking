package fimEntityResolution;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.enerj.core.SparseBitSet;
import org.enerj.core.SparseBitSet.Iterator;

public class Cluster{

	private int id;
	private static int currId = 0;	
	private double score = -1;
	
	public Cluster(double score){
		this.id = currId++;		
		this.score = score;
	}
	
	public int getId(){
		return id;
	}
			
	public double getScore(){
		return score;
	}
	/*
	public static String newline = System.getProperty("line.separator");
	private String printSparseSB(SparseBitSet sbs){
		StringBuffer sb = new StringBuffer();
		Iterator it = sbs.getIterator();
		while(it.hasNext()){
			sb.append(it.next()).append(", ");
		}
		return sb.toString();
	}
	
	
	private SparseBitSet copySupportToSBS(List<Record> records){
		SparseBitSet sbs = new SparseBitSet(20);
		for (Record record : records) {
			sbs.set(record.getId());
		}
		if(!(records.size() == sbs.getNumBitsSet())){
			System.out.println("not all bits were set in cluster!!");
		}
		return sbs;
	}
	
	private SparseBitSet copySupportToSBS(BitSet bs){
		SparseBitSet sbs = new SparseBitSet(20);
		for(int i=bs.nextSetBit(0); i>=0; i=bs.nextSetBit(i+1)) {
			sbs.set(i);
		}		
		return sbs;
	}
	
	private static double numOfTimesItemAppearsInCluster(int item,BitSet support,Map<Integer,Record> records){
		double retVal = 0;		
		for(Integer recordId=support.nextSetBit(0); recordId>=0;
				recordId=support.nextSetBit(recordId+1)){
			//add number of times item appears in the transaction
			if(records.get(recordId).getItemsToFrequency().containsKey(item)){  //only if the transaction contains the item
				retVal += records.get(recordId).getItemsToFrequency().get(item);
			}	
		}
		assert retVal >= support.cardinality();
		return retVal;
	}
	*/
	/*
	@SuppressWarnings("unused")
	private double calculateGlobalScoreV2(){
		if(this.support.cardinality() < min_sup){
			return 0; // force the record to search for a new cluster if possible
		}
		double nominator = 0;
		for(Integer item=items.nextSetBit(0); item>=0;
			item=items.nextSetBit(item+1)){		
			FrequentItem fi = itemsMap.get(item);
			double itemWeight = fi.getWeight();
			double itemSupportSize = fi.getSupportSize();
			nominator += (numOfTimesItemAppearsInCluster(item,support,records)/((double)itemSupportSize))*itemWeight; //weighted tf-idf of common items
		}	
		double denominator = 0;
		//a map from the item to the number of times it appears in the union of transactions
		//this number must be at most this.support
		Map<Integer,Double> unionItems = new HashMap<Integer,Double>();
		for(Integer recordId=support.nextSetBit(0); recordId>=0;
					recordId=support.nextSetBit(recordId+1)){
			Set<Integer> transItems = records.get(recordId).getItemsToFrequency().keySet();
			for (Integer item : transItems) {
				if(unionItems.containsKey(item))
					continue;
				unionItems.put(item, numOfTimesItemAppearsInCluster(item,support,records));
			}			
		}
		
		for (Integer item : unionItems.keySet()) {
			assert (unionItems.get(item) >= this.getSupport().cardinality()): 
				"item " + item + " has a larger support in the itemset than in the union";
			FrequentItem fi = itemsMap.get(item);
			double itemWeight = fi.getWeight();			
			double itemSupportSize = (double)fi.getSupportSize();
			denominator += ((double)unionItems.get(item))/((double)itemSupportSize)*itemWeight;
		}
		
		return (nominator/denominator);
	}
	*/
	
/*	public static double calculateScore(BitSet support, int min_sup, 
			BitSet items,Map<Integer,FrequentItem> itemsMap,Map<Integer,Record> records ){
		if(support.cardinality() < min_sup){
			return 0; // force the record to search for a new cluster if possible
		}
		double nominator = 0;
		for(Integer item=items.nextSetBit(0); item>=0;
			item=items.nextSetBit(item+1)){		
			FrequentItem fi = itemsMap.get(item);
 			double itemWeight = fi.getWeight();
			double itemSupportSize = fi.getSupportSize();
			nominator += (numOfTimesItemAppearsInCluster(item,support,records)/((double)itemSupportSize))*itemWeight; //weighted tf-idf of common items
		}	
		double denominator = 0;
		//a map from the item to the number of times it appears in the union of transactions
		//this number must be at most this.support
		Map<Integer,Double> unionItems = new HashMap<Integer,Double>();
		for(Integer recordId=support.nextSetBit(0); recordId>=0;
					recordId=support.nextSetBit(recordId+1)){
			Set<Integer> transItems = records.get(recordId).getItemsToFrequency().keySet();
			for (Integer item : transItems) {
				if(unionItems.containsKey(item))
					continue;
				unionItems.put(item, numOfTimesItemAppearsInCluster(item,support,records));
			}			
		}
		
		for (Integer item : unionItems.keySet()) {
			assert (unionItems.get(item) >= support.cardinality()): 
				"item " + item + " has a larger support in the itemset than in the union";
			double itemWeight = fi.getWeight();			
			double itemSupportSize = (double)fi.getSupportSize();
			denominator += ((double)unionItems.get(item))/((double)itemSupportSize)*itemWeight;
		}
		
		return (nominator/denominator);
	}
	*/
	/*
	public double calculateLinkBetweenRecordAndCluster(int recordId){
		assert this.support.get(recordId);
		double nominator = 0;
		for(Integer item=items.nextSetBit(0); item>=0;
				item=items.nextSetBit(item+1)){		
			double itemSupportSize = (double)itemsMap.get(item).getSupportSize();
			int numOfTimesItemAppearsInRecord = records.get(recordId).getItemsToFrequency().containsKey(item) ?
					records.get(recordId).getItemsToFrequency().get(item): 0; // due to the fact that now also concumed records are parrt of the support
			nominator += numOfTimesItemAppearsInRecord/((double)itemSupportSize); //tf-idf of common items
		}	
		double denominator = 0;
		Set<Integer> recordItems = records.get(recordId).getItemsToFrequency().keySet();
		for (Integer item : recordItems) {			
			double itemSupportSize = (double)itemsMap.get(item).getSupportSize();
			int numOfTimesItemAppearsInRecord = records.get(recordId).getItemsToFrequency().get(item);
			denominator += numOfTimesItemAppearsInRecord/((double)itemSupportSize); //tf-idf of un-common items
		}
		return nominator/denominator;
	}
	*/
	/*
	public double getScore(){
		//do not let changes in the support influence the score
	
			Collection<Integer> items = Utilities.getIntSetFromBitSet(this.getItems());
			List<Record> FISupportRecords = Utilities.getRecords(this.getSupport());
			score = StringSimTools.softTFIDF(FISupportRecords,items);
	
		return score;
	}
	*/
	/*
	public int compareTo(Cluster other) {		
		if(this == other){
			return 0;
		}
		if(this.getScore() > other.getScore()){ //we want descending order
			return -1;
		}
		if(this.getScore() < other.getScore()){
			return 1;
		}
		return 0;
	}
	*/
	public int hashCode(){
		return id;
	}
	
	public boolean equals(Object obj){
		if(this.hashCode()!= obj.hashCode()){
			return false;
		}	
		return true;
	}
	
	/*
	public boolean containsItems(Cluster other){
		boolean itemsContainment = Utilities.contains(this.getItems(),other.getItems());
		if(itemsContainment){
			boolean supportContainment = Utilities.contains(other.getSupport(),this.getSupport());
			if(!supportContainment){
				System.out.println("Invalid relationship!!");
				System.out.println("Cluster " + this.toString() + " contains all items in " + other.getItems());
				System.out.println("But there is no containment relationship between the supports!!");
			}
		}
		return itemsContainment;
	
	}
	*/
	/*
	public boolean containsSupport(Cluster other){
		return Utilities.contains(this.getSupport(),other.getSupport());
	}
	*/
	/*
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Cluster ").append(this.id)			
			.append(Utilities.NEW_LINE).append("items: ").append(Utilities.NEW_LINE).append(itemsAsString())
			.append(Utilities.NEW_LINE).append("support: ").append(Utilities.NEW_LINE).append(supportAsString()).append(Utilities.NEW_LINE)
			.append("score: " + this.getScore()).append("\r\n");
		return sb.toString();
	}
	
	
	private String supportAsString(){
		StringBuilder sb = new StringBuilder();
		for(Integer index=support.nextSetBit(0); index>=0;
				index=support.nextSetBit(index+1)){
			Record r = records.get(index);
			if(r == null){
				System.out.println("no record with id " + index);
			}
			sb.append(r.toString()).append(Utilities.NEW_LINE);
		}
		return sb.toString();
	}
	
	private String itemsAsString(){
		StringBuilder sb = new StringBuilder();
		for(Integer index=items.nextSetBit(0); index>=0;
				index=items.nextSetBit(index+1)){
			FrequentItem fi = itemsMap.get(index);
			if(fi == null){
				System.out.println("no Frequent item with id " + index);
			}
			sb.append(fi.toString()).append(Utilities.NEW_LINE);
		}
		return sb.toString();
	}
	*/
}
