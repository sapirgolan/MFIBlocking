package il.ac.technion.ie.canopy.model;

import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.probability.ClusterSimilarity;
import il.ac.technion.ie.probability.SimilarityCalculator;
import il.ac.technion.ie.utils.MathUtils;
import org.apache.log4j.Logger;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

import java.util.*;

/**
 * Created by I062070 on 22/11/2015.
 */
public class CanopyCluster {
    private final Map<Integer, Record> candidateRecords;
    private final double t2;
    private final double t1;
    private List<Record> allRecords;

    private static final Logger logger = Logger.getLogger(CanopyCluster.class);


    public CanopyCluster(List<Record> candidateRecordsForCanopy, double t2, double t1) {
        candidateRecords = new HashMap<>();
        for (Record record : candidateRecordsForCanopy) {
            candidateRecords.put(record.getRecordID(), record);
        }
        this.t2 = t2;
        this.t1 = t1;
        allRecords = new ArrayList<>();
    }

    public void removeRecordsBelowT2() {
        Map<Integer, List<String>> integerListHashMap = getMapForSimilarityCalculation();
        Map<Integer, Float> recordIDToSimilarity = calcRecordSimilarityOnAllRecords(integerListHashMap);
        MathUtils.normilize(recordIDToSimilarity);
        removeLessThanLooseRecords(recordIDToSimilarity);
    }

    private void removeLessThanLooseRecords(Map<Integer, Float> recordIDToSimilarity) {
        for (Map.Entry<Integer, Float> entry : recordIDToSimilarity.entrySet()) {
            if (entry.getValue() >= t2) {
                Record record = candidateRecords.get(entry.getKey());
                allRecords.add(record);
            }
        }
    }

    private Map<Integer, Float> calcRecordSimilarityOnAllRecords(Map<Integer, List<String>> integerListHashMap) {
        Map<Integer, Float> recordIDToSimilarity = new HashMap<>();
        SimilarityCalculator similarityCalculator = new SimilarityCalculator(new JaroWinkler());
        for (Integer recordID : candidateRecords.keySet()) {
            float recordSimilarityInCluster = ClusterSimilarity.calcRecordSimilarityInCluster(recordID, integerListHashMap, similarityCalculator);
            logger.trace(String.format("Similarity of record with ID '%d' is: %s", recordID, recordSimilarityInCluster));
            recordIDToSimilarity.put(recordID, recordSimilarityInCluster);
        }
        return recordIDToSimilarity;
    }

    private Map<Integer, List<String>> getMapForSimilarityCalculation() {
        Map<Integer, List<String>> integerListHashMap = new HashMap<>();
        for (Map.Entry<Integer, Record> entry : candidateRecords.entrySet()) {
            integerListHashMap.put(entry.getKey(), entry.getValue().getEntries());
        }
        return integerListHashMap;
    }

    public List<Record> getAllRecords() {
        return Collections.unmodifiableList(allRecords);
    }
}
