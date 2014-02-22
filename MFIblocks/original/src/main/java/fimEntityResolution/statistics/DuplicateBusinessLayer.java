package fimEntityResolution.statistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import candidateMatches.CandidatePairs;
import candidateMatches.RecordMatches;

public class DuplicateBusinessLayer {

	private CandidatePairs groundTruth;
	private CandidatePairs algorithmOutput;
	private int numberOfDuplicatesInDataset;
	private Map<Integer, Duplicate> duplicateDetectorMap;
	private int numberOfDuplicatesFound;
	private int numberOfComparisons;
	
	
	
	public DuplicateBusinessLayer(CandidatePairs groundTruth,
			CandidatePairs algorithmOutput) {
		this.groundTruth = groundTruth;
		this.algorithmOutput = algorithmOutput;
		this.duplicateDetectorMap = new HashMap<Integer, Duplicate>();
		this.numberOfDuplicatesInDataset = 0;
		this.numberOfDuplicatesFound = 0;
		this.numberOfComparisons = 0;
		
		init();
	}

	private void init() {
		ConcurrentHashMap<Integer, RecordMatches> allTrueMatches = groundTruth.getAllMatches();
		//iterate over groundTruth and create Duplicate Object
		buildDuplicateDetectionMap(allTrueMatches);
		//iterate over algorithmOutput and decide it duplicates were detected
		ConcurrentHashMap<Integer,RecordMatches> allMatches = algorithmOutput.getAllMatches();
		checkIfDuplicatesFound(allMatches);
		
		//collect statistics: #DuplicatesFound, #comparisons
		HashSet<Duplicate> duplicatesInExperiment = new HashSet<Duplicate>(duplicateDetectorMap.values());
		for (Duplicate duplicate : duplicatesInExperiment) {
			if (duplicate.wasDuplicateDetected()) {
				numberOfDuplicatesFound++;
				numberOfComparisons += duplicate.getComparisonsMade();
			}
		}
		
		
	}

	private void checkIfDuplicatesFound(
			ConcurrentHashMap<Integer, RecordMatches> allMatches) {
		for ( Map.Entry<Integer, RecordMatches> entry : allMatches.entrySet() ) {
			Integer recordUnderExemination = entry.getKey();
			Duplicate duplicate = duplicateDetectorMap.get(recordUnderExemination);
			if (duplicate != null) {
				//This invocation makes duplicate object to find out wheather or not a duplicate was detected
				duplicate.decideIfDetected(entry.getValue(), recordUnderExemination);
			}
		}
	}

	private void buildDuplicateDetectionMap(ConcurrentHashMap<Integer, RecordMatches> allTrueMatches) {
		for ( Map.Entry<Integer, RecordMatches> entry : allTrueMatches.entrySet() ) {
			boolean isNewDuplicate = false;
			Set<Integer> ids = new HashSet<Integer>(entry.getValue().getMatchedIds());
			ids.add(entry.getKey() );
			Duplicate duplicate = new Duplicate(ids);
			for (Integer id : ids) {
				//if map doesn't contain a key for record with id
				if (!duplicateDetectorMap.containsKey(id)) {
					duplicateDetectorMap.put(id, duplicate);
					isNewDuplicate = true;
				}				
			}
			if (isNewDuplicate) {
				numberOfDuplicatesInDataset++;
			}
		}
	}
	
	public int getNumberOfDuplicatesInDataset() {
		return numberOfDuplicatesInDataset;
	}
	
	public int getNumberOfDuplicatesFound() {
		return numberOfDuplicatesFound;
	}

	public int getNumberOfComparisons() {
		return numberOfComparisons;
	}
}
