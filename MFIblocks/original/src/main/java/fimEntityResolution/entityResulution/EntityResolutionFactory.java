package fimEntityResolution.entityResulution;


import il.ac.technion.ie.search.core.SearchEngine;

public class EntityResolutionFactory {
	public static IComparison createComparison(EntityResulutionComparisonType type, SearchEngine engine) {
		switch (type) {
		case Jaccard:
			return new JaccardComparisons(engine);
		default:
			break;
		}
		return null;
		
	}
}
