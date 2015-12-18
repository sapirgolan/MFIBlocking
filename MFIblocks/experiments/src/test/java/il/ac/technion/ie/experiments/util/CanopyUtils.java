package il.ac.technion.ie.experiments.util;

import com.google.common.collect.Lists;
import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.model.CanopyRecord;
import il.ac.technion.ie.model.Record;
import org.powermock.reflect.Whitebox;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by I062070 on 18/12/2015.
 */
public class CanopyUtils {

    public static CanopyCluster createCanopy(List<List<Record>> records) throws CanopyParametersException {

        List<CanopyRecord> canopyRecords = new ArrayList<>();
        for (List<Record> sublist : records) {
            for (Record record : sublist) {
                canopyRecords.add(new CanopyRecord(record, 0.0));
            }
        }
        CanopyCluster canopyCluster = new CanopyCluster(canopyRecords, 0.01, 0.2);
        Whitebox.setInternalState(canopyCluster, "allRecords", canopyRecords);

        return canopyCluster;
    }

    public static CanopyCluster createCanopy(List<List<Record>> records, List<Double> scores) throws CanopyParametersException {

        List<CanopyRecord> canopyRecords = new ArrayList<>();
        int index = 0;
        for (List<Record> sublist : records) {
            for (Record record : sublist) {
                canopyRecords.add(new CanopyRecord(record, scores.get(index)));
                index++;
            }
        }
        CanopyCluster canopyCluster = new CanopyCluster(canopyRecords, 0.01, 0.2);
        Whitebox.setInternalState(canopyCluster, "allRecords", canopyRecords);

        return canopyCluster;
    }

    public static CanopyCluster createCanopySingleList(List<Record> records) throws CanopyParametersException {
        return createCanopy(Lists.<List<Record>>newArrayList(records));
    }

    public static CanopyCluster createCanopySingleList(List<Record> records, List<Double> scores) throws CanopyParametersException {
        return createCanopy(Lists.<List<Record>>newArrayList(records), scores);
    }
}
