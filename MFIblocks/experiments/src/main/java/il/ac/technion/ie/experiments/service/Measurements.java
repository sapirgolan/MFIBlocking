package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 15/10/2015.
 */
public class Measurements {
    public static final double VALUE_NOT_EXISTS = -1.0;
    private iMeasurService measurService;
    private Map<Double, Double> rankedValueMap;


    public Measurements() {
        measurService = new MeasurService();
        rankedValueMap = new HashMap<>();
    }

    public void calculate(List<BlockWithData> blocks, double threshold) {
        if (blocks != null) {
            rankedValueMap.put(threshold, measurService.calcRankedValue(blocks));
        }
    }

    public double getRankedValueByThreshold(double threshold) {
        Double rankedValue = rankedValueMap.get(threshold);
        return rankedValue != null ? rankedValue : VALUE_NOT_EXISTS;
    }
}
