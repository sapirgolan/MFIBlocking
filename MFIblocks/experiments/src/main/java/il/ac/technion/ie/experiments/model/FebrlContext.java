package il.ac.technion.ie.experiments.model;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import il.ac.technion.ie.experiments.service.IMeasurements;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 22/10/2015.
 */
public class FebrlContext {

    private Table<Double, List<BlockWithData>, FebrlMeasuresContext> resultsTable; //<Row,Column,Value>

    public FebrlContext() {
        this.resultsTable = HashBasedTable.create();
    }

    public void add(Double threshold, List<BlockWithData> dataset, IMeasurements measurements) {
        FebrlMeasuresContext febrlMeasuresContext = measurements.getFebrlMeasuresContext(threshold);
        resultsTable.put(threshold, dataset, febrlMeasuresContext);
    }

    public Collection<List<BlockWithData>> getDataSet(double threshold) {
        Map<List<BlockWithData>, FebrlMeasuresContext> row = resultsTable.row(threshold);
        return row.keySet();
    }

    public FebrlMeasuresContext getMeasurments(double threshold, List<BlockWithData> blocks) {
        return resultsTable.get(threshold, blocks);
    }
}
