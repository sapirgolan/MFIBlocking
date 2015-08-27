package il.ac.technion.ie.lprobability;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;

import java.util.List;

/**
 * Created by I062070 on 08/05/2015.
 */
public class SimilarityCalculator {
    private final AbstractStringMetric stringMetric;

    public SimilarityCalculator(AbstractStringMetric metric) {
        this.stringMetric = metric;
    }

    public float calcRecordsSim(List<String> left, List<String> right) {
        float fieldsComparisonScore = this.compareFields(left, right);
        return fieldsComparisonScore / Math.max(left.size(), right.size());
    }

    public float compareFields(List<String> left, List<String> right) {
        if (left == null || right == null) {
            return 0;
        }
        String[] leftArray = left.toArray(new String[left.size()]);
        String[] rightArray = right.toArray(new String[right.size()]);

        return this.batchCompare(leftArray, rightArray);
    }

    private float batchCompare(String[] arr1, String[] arr2) {
        float[] batchCompareSets = stringMetric.batchCompareSets(arr1, arr2);
        float totalSimilarity = 0;
        for (float batchCompareSet : batchCompareSets) {
            totalSimilarity += batchCompareSet;
        }
        return totalSimilarity;
    }
}
