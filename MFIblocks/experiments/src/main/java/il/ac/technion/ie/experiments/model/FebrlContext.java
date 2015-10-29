package il.ac.technion.ie.experiments.model;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import il.ac.technion.ie.experiments.service.IMeasurements;

import java.util.Map;
import java.util.Set;

/**
 * Created by I062070 on 22/10/2015.
 */
public class FebrlContext {

    private Table<Double, Integer, FebrlMeasuresContext> resultsTable; //<Row,Column,Value>

    public FebrlContext() {
        this.resultsTable = HashBasedTable.create();
    }

    public void add(Double threshold, Integer febrlParameter, IMeasurements measurements) {
        FebrlMeasuresContext febrlMeasuresContext = measurements.getFebrlMeasuresContext(threshold);
        resultsTable.put(threshold, febrlParameter, febrlMeasuresContext);
    }

    public Set<Integer> getDataSet(double threshold) {
        Map<Integer, FebrlMeasuresContext> row = resultsTable.row(threshold);
        return row.keySet();
    }

    public FebrlMeasuresContext getMeasurments(double threshold, int febrlParameter) {
        return resultsTable.get(threshold, febrlParameter);
    }

    public Map<Integer, FebrlMeasuresContext> getMeasurments(Double threshold) {
        return resultsTable.row(threshold);
    }
}
