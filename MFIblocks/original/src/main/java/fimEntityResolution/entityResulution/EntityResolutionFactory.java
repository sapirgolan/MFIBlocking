package fimEntityResolution.entityResulution;

public class EntityResolutionFactory {
	public static IComparison createComparison(EntityResulutionComparisonType type) {
		switch (type) {
		case Jaccard:
			return new ExecuteJaccardComparisons();
		default:
			break;
		}
		return null;
		
	}
}
