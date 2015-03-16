package fimEntityResolution.entityResulution;

import fimEntityResolution.comparison.ProfileComparison;
import il.ac.technion.ie.model.CandidatePairs;
import il.ac.technion.ie.model.RecordMatches;
import lucene.search.SearchEngine;

import java.util.*;
import java.util.Map.Entry;

public class JaccardComparisons implements IComparison{

	private SearchEngine engine;
	private Map<Integer, Integer> cache;
	
	public JaccardComparisons(SearchEngine engine) {
		this.engine = engine;
		this.cache = new HashMap<Integer, Integer>();
	}
	
	
	public long measureComparisonExecution(
			CandidatePairs algorithmObtainedPairs) {
		
		long startingTime = System.currentTimeMillis();
		
		cache.clear();
		Iterator<Entry<Integer, RecordMatches>> iterator = algorithmObtainedPairs.getIterator();
		for (; iterator.hasNext();) {
			Entry<Integer, RecordMatches> entry = iterator.next();
			
			List<String> blockSeedAttributes = engine.getRecordAttributes(entry.getKey().toString());
			final Set<Integer> matchedRecordsIds = entry.getValue().getMatchedIds();
			for (Integer recordId : matchedRecordsIds) {
				if (!didComparisonOccur(recordId, entry.getKey())) {
					List<String> recordAttributes = engine.getRecordAttributes(recordId.toString());
					ProfileComparison.getJaccardSimilarity(blockSeedAttributes, recordAttributes);
					this.addToCache(entry.getKey(), recordId);
				}
			}
		}
		
		long endingTime = System.currentTimeMillis();
		return endingTime - startingTime;
	}

	private void addToCache(Integer valueOne, Integer valueTwo) {
		cache.put(valueOne, valueTwo);
		cache.put(valueTwo, valueOne);
	}
	
	private boolean didComparisonOccur(Integer valueOne, Integer valueTwo) {
		Integer actuallValueTwo = cache.get(valueOne);
		if (actuallValueTwo != null) {
			return actuallValueTwo.equals(valueTwo);
		}
		return false;
	}
}
