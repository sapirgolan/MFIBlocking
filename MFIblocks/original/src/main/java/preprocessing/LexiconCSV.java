package preprocessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import fimEntityResolution.FrequentItem;
import fimEntityResolution.bitsets.SBS_BitSet_Factory;

public class LexiconCSV {

	private Map<Integer,AttributeItems> attIdToItems = new HashMap<Integer,AttributeItems>();	
	private Map<Integer,Double> attIdToWeights = new HashMap<Integer, Double>();
	private Map<Integer,Integer> columnIndexToId = new HashMap<Integer, Integer>();
	//The length of the value taken into account (first 10,30 letters etc.).
	//if value is not available or negative the full length of the value is taken into account
	private Map<Integer,Integer> attIdToPrefixLength = new HashMap<Integer, Integer>();
	private int currItemId = 1;
	private double avgWordSize = 0;
	private int numOfWords = 0;
	private int minWordSize = Integer.MAX_VALUE;
	private int maxWordSize = 0;
	public LexiconCSV(File weightsPropertiesFile){
		FileReader fr;
		try {
			fr = new FileReader(weightsPropertiesFile);
			Properties props = new Properties();
			props.load(fr);
			for (Object key : props.keySet()) {
				String columnIndex = (String)key;   //the column number - NOT id
				String strVal = props.getProperty(columnIndex);
				String[] idWeightPrefix = strVal.split(","); //0 - id, 1-weight, 2- prefix		
				int id = Integer.parseInt(idWeightPrefix[0]);
				columnIndexToId.put(Integer.parseInt(columnIndex), id);
				attIdToWeights.put(id, Double.parseDouble(idWeightPrefix[1].trim()));
				attIdToPrefixLength.put(id, 0);
				if(idWeightPrefix.length > 2){
					int prefixLength = Integer.parseInt(idWeightPrefix[2].trim());
					if(prefixLength > 0){
						attIdToPrefixLength.put(id, prefixLength);
					}				
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public int getAttId(int columnIndex){
		if(!columnIndexToId.containsKey(columnIndex)){
			System.out.println("There is no id defined for column number " + columnIndex);
		}
		return columnIndexToId.get(columnIndex);
	}
	
	public int getPrefixLengthForColumn(int columnIndex){
		int attId = getAttId(columnIndex);
		return attIdToPrefixLength.get(attId);
	}
	
	public double getColumnWeight(int columnIndex)  {
		int attId = getAttId(columnIndex);
		return attIdToWeights.get(attId);
	}
	
	
	private final static String DEFAULT_WEIGHTS = "0.99999";
	private final static String SEP = ",";
	public LexiconCSV(String weightsProperty){
		if(weightsProperty == null || weightsProperty.length() == 0){
			weightsProperty=DEFAULT_WEIGHTS;
		}
		String[] weights = weightsProperty.trim().split(SEP);		
		int i=1;
		for (String weight:weights) {				
				attIdToWeights.put(i++, Double.parseDouble(weight));
			}				
	}
	
	
	public int addWord(int columnIndex, int recordId, String word){
		int attId = getAttId(columnIndex);
		AttributeItems attFIs = null;
		//do not want to waste time on this word, or have it participate in the algorithm
		//return 0
		if(attIdToWeights.get(attId) == 0){
			return -1;
		}
		if(attIdToItems.containsKey(attId)){
			attFIs = attIdToItems.get(attId);
		}
		else{
			attFIs = new AttributeItems();
		}
		FrequentItem wordItem = null;
		word = word.trim().toLowerCase();
		Map<String,Integer> wordsToIds = attFIs.getwordsToIds();
		if(wordsToIds.containsKey(word)){
			int wordId = wordsToIds.get(word);
			wordItem = attFIs.getItems().get(wordId);
			if(wordItem == null){
				System.out.println("word " + word + " was in wordsToIds but doesnt have a frequentitem object as expected");
			}
		}
		else{ //first time this word is seen for this  attribute
			int wordId = currItemId++;
			wordsToIds.put(word, wordId);
			//wordItem = new SparseFrequentItem(wordId, word,attIdToWeights.get(attId));
			wordItem = new FrequentItem(wordId, word, attIdToWeights.get(attId), SBS_BitSet_Factory.getInstance(2*csvFile.DB_Size));
		}
		wordItem.addColumn(columnIndex);
		wordItem.addSupport(recordId);
		attFIs.getItems().put(wordItem.getId(), wordItem);
		attIdToItems.put(attId, attFIs);
		
		minWordSize = Math.min(minWordSize, word.length());
		maxWordSize = Math.max(maxWordSize, word.length());
		avgWordSize += word.length();
		numOfWords++;
		return wordItem.getId();
	}
	
	private final static double Log_10_2=Math.log10(2);
	private static double logBase2(double x){
		return (Math.log10(x)/Log_10_2);
	}
	
	/**
	 * Removes top 10% most frequent items
	 * and also items whose support is exactly 1 
	 * @param maxSupport
	 */
	public void removeFrequentItems(double DBSize, double thresh){
		System.out.println("Entered removeFrequentItems: DBSize=" + DBSize + " thresh=" + thresh);
		int beforePruning = 0;
	
		List<FrequentItem> FreqItemList = new ArrayList<FrequentItem>();
		
		for (Entry<Integer, AttributeItems> attIdToitemsEntry: attIdToItems.entrySet()) {
			Map<Integer,FrequentItem> attItems = attIdToitemsEntry.getValue().attItems;
			beforePruning += attItems.size();
			Iterator<Entry<Integer, FrequentItem>> iterator = attItems.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<Integer, FrequentItem> idToItem = iterator.next();
				FreqItemList.add(idToItem.getValue());
			}			
		}
		//sorty in ascending order --> most frequent will be at the end
		Collections.sort(FreqItemList);
		int numToPrune = (int) Math.ceil(FreqItemList.size()*thresh);
		System.out.println("FreqItemList.size()="+ FreqItemList.size());
		System.out.println("numToPrune="+ numToPrune);
		double pruneThresh = FreqItemList.get(FreqItemList.size()-numToPrune-1).getSupportSize();
		System.out.println("pruneThresh="+ pruneThresh);
		int pruned = 0;
		int supp1Pruned =0;
		
		for (Entry<Integer, AttributeItems> attIdToitemsEntry: attIdToItems.entrySet()) {
			Map<Integer,FrequentItem> attItems = attIdToitemsEntry.getValue().attItems;			
			Iterator<Entry<Integer, FrequentItem>> iterator = attItems.entrySet().iterator();
			while(iterator.hasNext()) {
				Entry<Integer, FrequentItem> idToItem = iterator.next();
				long suppSize = idToItem.getValue().getSupportSize();
				if(suppSize > pruneThresh){
					iterator.remove();
					pruned++;
				}
				if(suppSize < 2){
					//iterator.remove();
					supp1Pruned++;
				}
			}			
		}
		
		
		System.out.println(" before pruning itesm with support > " + pruneThresh + " there were: " + beforePruning + " items");				
		System.out.println(" A total of " + pruned + " items were removed due to too large support");
		System.out.println(" A total of " + supp1Pruned + " items were removed due support of 1");
	}
	
	public int wordExists(int columnIndex, String word){
		int attId = getAttId(columnIndex);
		AttributeItems attFIs = null;
		//do not want to waste time on this word, or have it participate in the algorithm
		//return 0
		if(attIdToWeights.get(attId) == 0){
			return -1;
		}
		if(attIdToItems.containsKey(attId)){
			attFIs = attIdToItems.get(attId);
		}
		else{
			System.out.println("Attribute " + columnIndex + " doeasn't exist");
			return -1;
		}
		word = word.trim().toLowerCase();
		Map<String,Integer> wordsToIds = attFIs.getwordsToIds();
		if(wordsToIds.containsKey(word)){
			int wordid = wordsToIds.get(word);
			if(attFIs.attItems.containsKey(wordid))
			{
				FrequentItem fi = attFIs.attItems.get(wordid);
				if(fi.getSupportSize() > 1){
					return wordid;
				}
				else{
					return -1;
				}
			}
		}
		return -1;
		
	}
	public void exportToPropFile(String propFile){
		Properties props = exportToProps();
		OutputStream os;
		try {
			os = new FileOutputStream(new File(propFile));
			props.store(os, "lexicon file");	
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		System.out.println("Average word size is " + (avgWordSize/numOfWords));
		System.out.println("MIN word size is " + minWordSize);
		System.out.println("MAX word size is " + maxWordSize);
			
	}
	
	public int getNumOfitems(){
		int retval = 0;
		for (AttributeItems ais : attIdToItems.values()) {
			Map<Integer,FrequentItem> items = ais.getItems();
			retval += items.size();
		}
		return retval;
	}
	
	public Properties exportToProps(){
		System.out.println("there are " + getNumOfitems() + " items");
		Properties props = new Properties();
		for (AttributeItems ais : attIdToItems.values()) {
			Map<Integer,FrequentItem> items = ais.getItems();
			StringBuilder propVal = new StringBuilder();
			for (FrequentItem fi : items.values()) {
				propVal.setLength(0); //clear the stringBuilder		
				propVal.append(fi.getItem()).append(",").
						append(Double.toString(fi.getWeight())).append(",").
							append(fi.getSupportString()).append(fi.getColumnsString());
				
				props.put(Integer.toString(fi.getId()), propVal.toString());
				//props.put(Integer.toString(fi.get()), propVal.toString());
			}
		}
		return props;
	}
	
	public List<Integer> getNGramIds(List<String> nGrams, int recordId, int columnIndex){		
		List<Integer> retVal = new ArrayList<Integer>(nGrams.size());		
		for (String nGram : nGrams) {
			retVal.add(addWord(columnIndex, recordId, nGram));
		}
		return retVal;
	}
	
	
	public class AttributeItems{
		private Map<Integer,FrequentItem> attItems;
		private Map<String,Integer> wordToId;
	
		public AttributeItems(){
			attItems = new HashMap<Integer, FrequentItem>();	
			wordToId = new HashMap<String, Integer>();			
		}		
		
		public Map<Integer,FrequentItem> getItems(){
			return attItems;
		}
		
		public Map<String, Integer> getwordsToIds(){
			return wordToId;
		}		
		
	}
	
}
