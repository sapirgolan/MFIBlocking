package il.ac.technion.ie.experiments.model;

import il.ac.technion.ie.model.Field;
import il.ac.technion.ie.model.Record;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 09/09/2015.
 */
public class RecordSplit extends Record {

    private final UniformRealDistribution uniformRealDistribution;
    private Map<Field, Double> splitProbability;

    private RecordSplit(List<String> fieldsName, List<String> values, int id) {
        super(fieldsName, values, id);
        uniformRealDistribution = new UniformRealDistribution();
        splitProbability = new HashMap<>();
        sampleProbabilities();
    }

    public RecordSplit(Record origRecord) {
        super();
        copyFromOrigRecord(origRecord);
        uniformRealDistribution = new UniformRealDistribution();
        splitProbability = new HashMap<>();
        sampleProbabilities();
    }

    private void copyFromOrigRecord(Record origRecord) {
        fields = origRecord.getFields();
        recordID = origRecord.getRecordID();
        recordID = origRecord.getRecordID();

    }

    private void sampleProbabilities() {
        for (Field field : fields.values()) {
            splitProbability.put(field, uniformRealDistribution.sample());
        }
    }
}
