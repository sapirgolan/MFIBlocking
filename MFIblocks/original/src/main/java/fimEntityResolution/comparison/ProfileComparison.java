package fimEntityResolution.comparison;

import java.util.HashSet;
import java.util.Set;

public class ProfileComparison {
	public static double getJaccardSimilarity(Set<Integer> trueMatchedIds, Set<Integer> algorithmMatchedIds) {
        HashSet<Integer> allIds = new HashSet<Integer>(trueMatchedIds);
        allIds.addAll(algorithmMatchedIds);
        
        Set<Integer> tempAlgorithmMatchedIds = new HashSet<Integer>(algorithmMatchedIds);
        
        tempAlgorithmMatchedIds.retainAll(trueMatchedIds);
        double size = (double)tempAlgorithmMatchedIds.size()/allIds.size();
        return size;
    }
}
