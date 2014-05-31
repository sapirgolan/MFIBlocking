package fimEntityResolution.entityResulution;

import java.util.HashSet;
import java.util.Set;

import candidateMatches.CandidatePairs;
import fimEntityResolution.comparison.ProfileComparison;

public class ExecuteJaccardComparisons implements IComparison{

	public long measureComparisonExecution(
			CandidatePairs groundTruthCandidatePairs,
			CandidatePairs algorithmObtainedPairs) {
		
		long startingTime = System.currentTimeMillis();
		HashSet<Integer> recordIds = getRecordIdsFromAlgorithm( algorithmObtainedPairs, groundTruthCandidatePairs );
		for (Integer recordId : recordIds) {
			Set<Integer> trueMatchedIds = getMatchedIds(groundTruthCandidatePairs, recordId);
			Set<Integer> algorithmMatchedIds = getMatchedIds(algorithmObtainedPairs, recordId);
			ProfileComparison.getJaccardSimilarity(trueMatchedIds, algorithmMatchedIds);
		}
		
		long endingTime = System.currentTimeMillis();
		return endingTime - startingTime;
	}

	public Set<Integer> getMatchedIds(CandidatePairs candidatePairs,
			Integer recordId) {
		HashSet<Integer> MatchedIds = new HashSet<Integer>( candidatePairs.getAllMatches().get(recordId).getCandidateSet().keySet() );
		return MatchedIds;
	}

	public HashSet<Integer> getRecordIdsFromAlgorithm(
			CandidatePairs algorithmObtainedPairs, CandidatePairs groundTruthCandidatePairs) {
		HashSet<Integer> recordIds = new HashSet<Integer>( algorithmObtainedPairs.getAllMatches().keySet() );
		removedIdsNotInResult(recordIds, groundTruthCandidatePairs);
		
		return recordIds;
	}
	
	private void removedIdsNotInResult(
			HashSet<Integer> recordIds, CandidatePairs groundTruthCandidatePairs) {
		recordIds.retainAll(groundTruthCandidatePairs.getAllMatches().keySet());
	}

}
