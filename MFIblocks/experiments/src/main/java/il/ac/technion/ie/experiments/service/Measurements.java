package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by I062070 on 15/10/2015.
 */
public class Measurements implements IMeasurements {

    static final Logger logger = Logger.getLogger(Measurements.class);

    public static final double VALUE_NOT_EXISTS = -1.0;
    private iMeasurService measurService;
    private Map<Double, Double> rankedValueMap;
    private Map<Double, Double> mrrValueMap;
    private Map<Double, Double> normalizedMRRValues;
    private int numberOfOriginalBlocks;
    private Map<Double, Double> normalizedRankedValues;


    public Measurements(int numOfOriginalBlocks) {
        measurService = new MeasurService();
        rankedValueMap = new HashMap<>();
        mrrValueMap = new HashMap<>();
        normalizedMRRValues = new HashMap<>();
        normalizedRankedValues = new HashMap<>();
        this.numberOfOriginalBlocks = numOfOriginalBlocks;
    }

    @Override
    public void calculate(List<BlockWithData> blocks, double threshold) {
        if (blocks != null) {
            logger.trace("calculating RankedValue and MRR for threshold " + threshold);
            double rankedValue = measurService.calcRankedValue(blocks);
            rankedValueMap.put(threshold, rankedValue);
            normalizedRankedValues.put(threshold, rankedValue / numberOfBlocks(blocks.size()));

            double mRRValue = measurService.calcMRR(blocks);
            mrrValueMap.put(threshold, mRRValue);
            normalizedMRRValues.put(threshold, mRRValue / numberOfBlocks(blocks.size()));
        }
    }

    private int numberOfBlocks(int numberOfSpitedBlocks) {
        int delta = numberOfSpitedBlocks - numberOfOriginalBlocks;
        if (delta == 0) {
            delta = 1;
        }
        return delta;
    }

    @Override
    public double getRankedValueByThreshold(double threshold) {
        Double rankedValue = rankedValueMap.get(threshold);
        return rankedValue != null ? rankedValue : VALUE_NOT_EXISTS;
    }

    @Override
    public double getMRRByThreshold(double threshold) {
        Double rankedValue = mrrValueMap.get(threshold);
        return rankedValue != null ? rankedValue : VALUE_NOT_EXISTS;
    }

    @Override
    public List<Double> getRankedValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(rankedValueMap);
    }

    @Override
    public List<Double> getMrrValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(mrrValueMap);
    }

    private List<Double> getMeasureSortedByThreshold(Map<Double, Double> measureValue) {
        TreeSet<Double> sortedKeys = new TreeSet<>(measureValue.keySet());
        List<Double> rankedValuesSortedByThreshold = new ArrayList<>();
        for (Double key : sortedKeys) {
            rankedValuesSortedByThreshold.add(measureValue.get(key));
        }
        return rankedValuesSortedByThreshold;
    }

    @Override
    public List<Double> getThresholdSorted() {
        List<Double> list = new ArrayList<>(rankedValueMap.keySet());
        Collections.sort(list);
        return list;
    }

    @Override
    public void calculateMillerResults(List<BlockWithData> blockWithDatas) {
        this.calculate(blockWithDatas, 0.0);
    }

    @Override
    public List<Double> getNormalizedRankedValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(normalizedRankedValues);
    }

    @Override
    public List<Double> getNormalizedMRRValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(normalizedMRRValues);
    }
}
