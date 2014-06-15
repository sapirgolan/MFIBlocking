package fimEntityResolution.comparison;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProfileComparison {
	public static <T> double getJaccardSimilarity(Set<T> setOne, Set<T> setTwo) {
        HashSet<T> allIds = new HashSet<T>(setOne);
        allIds.addAll(setTwo);
        
        Set<T> tempAlgorithmMatchedIds = new HashSet<T>(setTwo);
        
        tempAlgorithmMatchedIds.retainAll(setOne);
        double size = (double)tempAlgorithmMatchedIds.size()/allIds.size();
        return size;
    }
	
	public static <T> double getJaccardSimilarity(List<T> listOne, List<T> listTwo) {
		HashSet<T> setOne = new HashSet<T>(listOne);
		HashSet<T> setTwo = new HashSet<T>(listTwo);
		
		return getJaccardSimilarity(setOne, setTwo);
	}
	
}
