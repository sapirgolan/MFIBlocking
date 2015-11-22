package il.ac.technion.ie.probability;

import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 22/11/2015.
 */
public class ClusterSimilarity {

    /**
     * The method sump the similarity of a record (currentRecordId) to all other records in a block.
     * It doesn't calculate the similarity of a record to itself.
     *
     * @param currentRecordId      - the record whose similarity with other we want to measure.
     * @param blockAttributes      - Map where for each ID in it the corresponding list with all fields that the record has.
     * @param similarityCalculator - a calculator to calc similarity by.
     * @return
     */
    public static float calcRecordSimilarityInCluster(Integer currentRecordId, final Map<Integer, List<String>> blockAttributes,
                                                      SimilarityCalculator similarityCalculator) {
        float recordsSim = 0;
        //case block contains a single record
        if (blockAttributes.size() == 1) {
            return 1;
        }
        for (Integer next : blockAttributes.keySet()) {
            if (next != currentRecordId) {
                recordsSim += similarityCalculator.calcRecordsSim(blockAttributes.get(next), blockAttributes.get(currentRecordId));
            }
        }
        return recordsSim;
    }
}
