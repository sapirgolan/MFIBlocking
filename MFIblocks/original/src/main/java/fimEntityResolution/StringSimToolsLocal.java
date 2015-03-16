package fimEntityResolution;

import il.ac.technion.ie.model.IFRecord;
import il.ac.technion.ie.context.MfiContext;
import il.ac.technion.ie.model.Record;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;
/***
 * Copied from StringSimTools but with some modifications - reads algorithm`s input files locally.
 * @author Jonathan Svirsky
 */
public class StringSimToolsLocal {
	
	public static String LEXICON_FILE = new String();
	public static String RECORDS_FILE=new String();
	public static String ORIGRECORDS_FILE=new String();
	
	public static Map<Integer, FrequentItem> globalItemsMap= new HashMap<Integer, FrequentItem>();
			
	//public static Map<Integer, FrequentItem> globalItemsMap=Utilities.parseLexiconFile(LEXICON_FILE);
	public static Map<Integer,Record> globalRecords=new HashMap<Integer, Record>();
			
	//public static Map<Integer,Record> globalRecords=Utilities.readRecords(RECORDS_FILE,ORIGRECORDS_FILE,"");
	
	private final static JaroWinkler jwMetric = new JaroWinkler();
	private final static double SIMILARITY_THRESHOLD = 0.9;
	/**
	 * This method returns a modified record s'. This record contains all tokens in s
	 * that are "close enough" to some token in string t. 
	 * @param S
	 * @param T
	 */
	private static Set<Integer> getCloseTerms(Record S, Record T){
		//first place all common items in the close words set
		//because clearly these items are identical and thus close
		Set<Integer> closeWords = getCommonItems(S,T);
		
		Set<Integer> unCommonValues_S = new HashSet<Integer>();
		for (Integer item : S.getItemsToFrequency().keySet()) {
			if(!closeWords.contains(item)){
				unCommonValues_S.add(item);
			}
		}
		
		Set<Integer> unCommonValues_T = new HashSet<Integer>();
		for (Integer item : T.getItemsToFrequency().keySet()) {
			if(!closeWords.contains(item)){
				unCommonValues_T.add(item);
			}
		}
		//perform the test only for uncommon items.
		for (Integer unCommonItem_S : unCommonValues_S) {
			for(Integer unCommonItem_T: unCommonValues_T){				
				String unCommon_S_str = globalItemsMap.get(unCommonItem_S).getItem();
				String unCommon_T_str = globalItemsMap.get(unCommonItem_T).getItem();
				float similarity = jwMetric.getSimilarity(unCommon_S_str, unCommon_T_str);
				if(similarity >= SIMILARITY_THRESHOLD){
					closeWords.add(unCommonItem_S);
				}
			}
		}		
		return closeWords;		
	}
	
	private static Map<Integer,Double> getCloseTerms3(final List<IFRecord> records,Collection<Integer> commonItems){
		if(commonItems == null){
			commonItems = getCommonItems(records);
		}
		Map<Integer,Double> closeWords = new HashMap<Integer, Double>();
		for (Integer item : commonItems) {
			closeWords.put(item,1.0);
		}
		
		//select a record at random - hopefully this will contain uncommon items
		//approximately similar to items in the rest of the records
		IFRecord theRecord = records.get(0);
		
		Set<Integer> recordUncommonItems = new  HashSet<Integer>(theRecord.getItemsToFrequency().keySet());		
		recordUncommonItems.removeAll(closeWords.keySet());
		records.remove(theRecord); //should be compared only against the other record, obviously not to itself
		
		HashMap<Integer, Double> uncommonItems = new HashMap<Integer, Double>(recordUncommonItems.size()) ;
		//now see if new items can be added to the "close items"
		for (Integer uncommonItem : recordUncommonItems) {
			uncommonItems.put(uncommonItem, 0.0); //initialize
			String unCommonItemWord = globalItemsMap.get(uncommonItem).getItem();
			
			for (IFRecord record : records) { //in each record locate the most similar item to 'uncommonItem'
				Set<Integer> currRecorditems = new HashSet<Integer>(record.getItemsToFrequency().keySet());
				currRecorditems.removeAll(closeWords.keySet()); //no need to run over items already deemed close
				
				double bestScore = 0.0;
				for (Integer item : currRecorditems) {		// find the item in the record that is closest to unCommonItemWord			
					String itemWord = globalItemsMap.get(item).getItem();
					double similarity = jwMetric.getSimilarity(unCommonItemWord, itemWord);
					bestScore = Math.max(bestScore, similarity);
					if(bestScore == 1.0){ // can't be a better score than this
						break;
					}
				}
				
				double currSimilarity = uncommonItems.get(uncommonItem) + bestScore;
				uncommonItems.put(uncommonItem, currSimilarity);
				if(currSimilarity >= SIMILARITY_THRESHOLD*records.size()){
					break; //we already know that value approximately appears in SIMILARITY_THRESHOLD of the records
				}
			}
		}
		
		//now run over the hashmap and see which of items are indeed common to all records
		for (Entry<Integer, Double> entry : uncommonItems.entrySet()) {
			if(entry.getValue() >= SIMILARITY_THRESHOLD*records.size()){ //value approximately appears in SIMILARITY_THRESHOLD of the records
				closeWords.put(entry.getKey(),entry.getValue()/records.size());
			}
		}
		return closeWords;	
	}
	
	/**
	 * This method returns a modified record s'. This record contains all tokens in s
	 * that are "close enough" to some token in string t. 
	 * @param S
	 * @param T
	 */
	private static Map<Integer,Double> getCloseTerms(Collection<IFRecord> records){
		//first place all common items in the close words set
		//because clearly these items are identical and thus close
		Set<Integer> commonItems = getCommonItems(records);
		Map<Integer,Double> closeWords = new HashMap<Integer,Double>();
		for (Integer item : commonItems) {
			closeWords.put(item,1.0); //appears in exactly all records
		}	
		
		Set<Integer> unCommonValues = getUnionItems(records);
		unCommonValues.removeAll(commonItems);
		
		List<Integer> unCommonList = new ArrayList<Integer>(unCommonValues.size());
		unCommonList.addAll(unCommonValues);
		
		//perform the test only for uncommon items.
		for (int i=0 ; i < unCommonList.size() ; i++) {
			for(Integer commonItem : commonItems){
				String uncommonWord = globalItemsMap.get(unCommonList.get(i)).getItem();
				//if word is similar enough to some common item then add it to the list of 
				//common words
				String commonWord = globalItemsMap.get(commonItem).getItem();
				double similarity = jwMetric.getSimilarity(uncommonWord, commonWord);
				if(similarity >= SIMILARITY_THRESHOLD){
					closeWords.put(unCommonList.get(i),similarity);					
				}
			}
		}
		
		return closeWords;
		
			
	}
	
	private static HashMap<Integer,Double> getCloseTerms2(Collection<IFRecord> records){
		//first place all common items in the close words set		
		HashMap<Integer,Double> retVal = new HashMap<Integer, Double>();
		Set<Integer> commonItems = getCommonItems(records);
		for (Integer integer : commonItems) {
			retVal.put(integer, (double)records.size()); //appears in all records
		}
		
		//select a record at random (the first in the collection)
		IFRecord theRecord = getRecordFromCollection(records);
		//run over its items
		for (Integer recordItem : theRecord.getItemsToFrequency().keySet()) {
			if(commonItems.contains(recordItem))
				continue; //already received a score in the hashMap
			
			retVal.put(recordItem, 0.0); //initialize the score
			String uncommonWord = globalItemsMap.get(recordItem).getItem();
			
			boolean closeToCommonWord = false;
			//first check if it is in common with some common item
			for(Integer commonItem : commonItems){				
				//if word is similar enough to some common item then add it to the list of 
				//common words
				String commonWord = globalItemsMap.get(commonItem).getItem();
				double similarity = jwMetric.getSimilarity(uncommonWord, commonWord);
				if(similarity >= SIMILARITY_THRESHOLD){
					retVal.put(recordItem, similarity*records.size());	//will be normalized later		
					closeToCommonWord = true;
					break; //found commonality, no need to search for more
				}				
			}
			if(closeToCommonWord) //already received a score in the hashmap
				continue;
			
			//if here, then this is an item which isn't part of the common items
			//and isn't similar to any of them either
			//now run over all the records in the collection, and see how many of them
			//have an item similar to recordItem
			for (IFRecord record : records) {
				double bestSim = 0; //similarity score of the most similar item to recordItem			
				for (Integer otherItem : record.getItemsToFrequency().keySet()) {
					if(commonItems.contains(otherItem)) //already handled
						continue;
					
					//some uncommon item
					String word = globalItemsMap.get(otherItem).getItem();
					double currSim = jwMetric.getSimilarity(uncommonWord, word);
					if(currSim > bestSim){
						bestSim = currSim;						
					}
				}
				double newSim = retVal.get(recordItem)+bestSim;
				retVal.put(recordItem, newSim);			
			}
			
		}
		//now normalize the scores in the hashMap
		//and remove entries with a low score 
		List<Integer> toDelete = new ArrayList<Integer>(retVal.keySet().size());
		for (Entry<Integer,Double> entry : retVal.entrySet()) {
			double normalizedScore = entry.getValue()/records.size();
			if(normalizedScore >= SIMILARITY_THRESHOLD){
				retVal.put(entry.getKey(), normalizedScore);
			}
			else{
				toDelete.add(entry.getKey());
			}				
		}
		for (Integer integer : toDelete) {
			retVal.remove(integer);
		}
	
	
		return retVal;
		
	}
	
	private static IFRecord getRecordFromCollection(Collection<IFRecord> records){
		IFRecord retVal = null;
		for (IFRecord record : records) {
			retVal= record;
			break;
		}
		return retVal;
	}
	private static Set<Integer> getCommonItems(Record S, Record T){
		Set<Integer> commonItems = new HashSet<Integer>();	
		commonItems.addAll(S.getItemsToFrequency().keySet());
		commonItems.retainAll(T.getItemsToFrequency().keySet());
		return commonItems;
	}
	
	private static Set<Integer> getCommonItems(final Collection<IFRecord> records){
		Set<Integer> commonItems = new HashSet<Integer>();
		boolean first = true;
		for (IFRecord record : records) {
			if(first){				
				commonItems.addAll(record.getItemsToFrequency().keySet());
				first = false;
			}
			else{
				commonItems.retainAll(record.getItemsToFrequency().keySet());
			}
		}	
		return commonItems;
	}
	
	private static Set<Integer> getUnionItems(Collection<IFRecord> records){
		Set<Integer> unionItems = new HashSet<Integer>();
		for (IFRecord record : records) {
			unionItems.addAll(record.getItemsToFrequency().keySet());
		}
		return unionItems;
	}
	

	
	/**
	 * This method returns the score of the word most similar to some word in the provided set.
	 * If the word appears in the set then 1.0 is returned.
	 * @param wordId
	 * @param t
	 * @return
	 */
	private static double findSimilarityToClosestWord(int wordId, Set<Integer> items){		
		if(items.contains(wordId)){
			return 1.0;
		}
		double retVal = 0;
		String word = globalItemsMap.get(wordId).getItem();
		for (Integer itemId : items) {
			String item = globalItemsMap.get(itemId).getItem(); 
			retVal = Math.max(retVal, jwMetric.getSimilarity(word,item));
		}
		return retVal;
	}
	
	private static double IDF(int wordId){
		return ((double)globalRecords.size()/
					(double)globalItemsMap.get(wordId).getSupportSize());
	}
	
	private static double log2IDF(int wordId){
		double retVal = globalItemsMap.get(wordId).getLog2IDF();
		if(retVal <=0){
			retVal = ((double)globalRecords.size()/
					(double)globalItemsMap.get(wordId).getSupportSize());
			return logBase2(retVal);
		}
		return retVal;
	}
	
	private static int TF(int wordId, IFRecord S){
		int retVal = 0;
		if(S.getItemsToFrequency().containsKey(wordId)){
			retVal = S.getItemsToFrequency().get(wordId);
		}
		return retVal;
	}
	
	private static int TF(int wordId, Collection<IFRecord> records){
		int retVal = 0;
		for (IFRecord record : records) {
			if(record.getItemsToFrequency().containsKey(wordId)){
				retVal += record.getItemsToFrequency().get(wordId);
			}
		}		
		return retVal;
	}
	
	private static double Word_TFIDF(int wordId, IFRecord S){		
		int frequency = TF(wordId,S) + 1;
		return getTermWeight(wordId)*logBase2(frequency)*logBase2(IDF(wordId));
	}
	
	private static double Word_TFIDF(int wordId, Collection<IFRecord> records){		
		int frequency = TF(wordId,records) + 1;
		return getTermWeight(wordId)*logBase2(frequency)*log2IDF(wordId);
	}
	
	private static double normalized_Word_TFIDF(int wordId, Record S){
		double nominator = Word_TFIDF(wordId,S);
		double denominator = 0 ;
		for (Integer itemId : S.getItemsToFrequency().keySet()) {
			denominator+= Math.pow(Word_TFIDF(itemId, S), 2); 
		}
		denominator = Math.sqrt(denominator);
		return (nominator/denominator);
	}
	
	@SuppressWarnings("unused")
	private static double normalized_Word_TFIDF(int wordId, Collection<IFRecord> records){
		Set<Integer> commonItems = getCommonItems(records);
		double nominator = Word_TFIDF(wordId,records);
		double denominator = 0 ;
		for (Integer itemId : commonItems) {
			denominator+= Math.pow(Word_TFIDF(itemId, records), 2); 
		}
		denominator = Math.sqrt(denominator);
		return (nominator/denominator);
	}
	
	private final static double Log_10_2=Math.log10(2);
	public static double logBase2(double x){
		return (Math.log10(x)/Log_10_2);
	}
	
	public static double softTFIDF(IFRecord S, IFRecord T){
		
		List<IFRecord> records = new ArrayList<IFRecord>(2);
		records.add(S);
		records.add(T);
		return softTFIDF(records,null,1.0);
	}
	
	@SuppressWarnings("unused")
	private static double TFIDF(Record S, Record T){
		double retVal = 0;
		Set<Integer> commonTerms = getCommonItems(S, T);
		for (Integer term : commonTerms) {			
			retVal += normalized_Word_TFIDF(term,S)*normalized_Word_TFIDF(term,T)
								*findSimilarityToClosestWord(term,commonTerms);
		}
		return retVal;
	}
	
	private static double getTermWeight(int id){
		return globalItemsMap.get(id).getWeight();
	}
	public static AtomicLong timeInRough=new AtomicLong(0);
	public static AtomicLong timeInGetClose=new AtomicLong(0);
	public static AtomicLong numOfRoughMFIs=new AtomicLong(0);
	public static AtomicLong numOfMFIs=new AtomicLong(0);
	
	private final static double DENY_THRESH = 0.05;
	private final static double MAX_ACCEPT_THRESH = 0.6;
	public static double softTFIDF(List<IFRecord> records, Collection<Integer> commonItems, final double threshold){
		numOfMFIs.incrementAndGet();
		if(commonItems == null){
			commonItems = getCommonItems(records);
		}
		//TODO: changed
		long start = System.currentTimeMillis();
		double minPossibleScore = roughSoftTFIDF(records,commonItems);
		timeInRough.addAndGet(System.currentTimeMillis()-start);
		//soft score should be calculated only for "maybe" MFIS. Those with medium scores.
		//Those with either very high or very low scores need not go through with the computations
		if(minPossibleScore >= MAX_ACCEPT_THRESH || minPossibleScore < DENY_THRESH){ // soft score can only be higher
			numOfRoughMFIs.incrementAndGet();
			return minPossibleScore; //and if score is lower than half the threshold then we assume it won't be able
										//to rise high enough to withstand the thresh
		}
		
		
		start = System.currentTimeMillis();
		double nominator = 0;
		//contains common terms as well as terms which are similar to the common terms 
		//TODO: cjanged
		Map<Integer,Double> closeTerms = getCloseTerms3(records,commonItems);
		timeInGetClose.addAndGet(System.currentTimeMillis()-start);
		for (Entry<Integer,Double> term : closeTerms.entrySet()) {
			nominator += Word_TFIDF(term.getKey(),records)
							*term.getValue(); //if term is common then this will be 1.0
		}
		Set<Integer> unCommonTerms = getUnionItems(records);
		unCommonTerms.removeAll(closeTerms.keySet());
		double denominator = nominator;
		for (Integer term  : unCommonTerms) {			
			denominator += Word_TFIDF(term,records);
		}
		return (nominator/denominator);
	}
	
	public static double roughSoftTFIDF(Collection<IFRecord> records,Collection<Integer> commonItems){
		double nominator = 0;
		int recordsSize = records.size();
		for (Integer term : commonItems) {
			nominator += getTermWeight(term)*logBase2(recordsSize)*log2IDF(term); //"rough - as in - assume term appears only once in a record
		
		}
		Set<Integer> unCommonTerms = getUnionItems(records);
		unCommonTerms.removeAll(commonItems);
		double denominator = nominator;
		for (Integer term  : unCommonTerms) {
			denominator += getTermWeight(term)*logBase2(recordsSize)*log2IDF(term);
		
		}
		return (nominator/denominator);
	}
	
	public static double MaxScore(int recordsSize,Collection<Integer> commonItems, int minRecordSize){
		double nominator = 0;	
		double minTermScore = 1000;
		for (Integer term : commonItems) {
			double termScore = getTermWeight(term)*logBase2(recordsSize)*log2IDF(term);
			if(termScore < minTermScore){
				minTermScore = termScore;
			}
			nominator += termScore; //"rough - as in - assume term appears only once in a record
		}
		
		double denominator = recordsSize*minRecordSize*minTermScore;	
		return (nominator/denominator);
	}

	public static void init(MfiContext c) {
		LEXICON_FILE = c.getLexiconFile();
		RECORDS_FILE = c.getRecordsFile();
		ORIGRECORDS_FILE = c.getOriginalFile();
	}
	
}
