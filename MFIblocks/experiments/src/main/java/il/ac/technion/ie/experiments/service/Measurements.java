package il.ac.technion.ie.experiments.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.FebrlMeasuresContext;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;
import il.ac.technion.ie.model.Record;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by I062070 on 15/10/2015.
 */
public class Measurements implements IMeasurements {

    static final Logger logger = Logger.getLogger(Measurements.class);

    public static final double VALUE_NOT_EXISTS = -1.0;
    private iMeasurService measurService;
    private ListMultimap<Double, Double> rankedValueMap;
    private ListMultimap<Double, Double> mrrValueMap;
    private ListMultimap<Double, Double> normalizedMRRValues;
    private int numberOfOriginalBlocks;
    private ListMultimap<Double, Double> normalizedRankedValues;


    public Measurements(int numOfOriginalBlocks) {
        measurService = new MeasurService();
        rankedValueMap = ArrayListMultimap.create();
        mrrValueMap = ArrayListMultimap.create();
        normalizedMRRValues = ArrayListMultimap.create();
        normalizedRankedValues = ArrayListMultimap.create();
        this.numberOfOriginalBlocks = numOfOriginalBlocks;
    }

    @Override
    public void calculate(List<BlockWithData> blocks, double threshold) {
        if (blocks != null) {
            logger.trace("calculating RankedValue and MRR for threshold " + threshold);
            calcRankedValue(blocks, threshold);

            calcMRR(blocks, threshold);
        }
    }

    private void calcMRR(List<BlockWithData> blocks, double threshold) {
        double mRRValue = measurService.calcMRR(blocks);
        logger.debug(String.format("%s, MRR, %s", blocks.toString(), mRRValue));
        mrrValueMap.put(threshold, mRRValue);
        double normMRR = mRRValue / numberOfBlocks(blocks.size());
        normalizedMRRValues.put(threshold, normMRR);
        logger.debug(String.format("%s, Norm MRR, %s", blocks.toString(), normMRR));
    }

    private void calcRankedValue(List<BlockWithData> blocks, double threshold) {
        double rankedValue = measurService.calcRankedValue(blocks);
        logger.debug(String.format("%s, Ranked Value, %s", blocks.toString(), rankedValue));
        rankedValueMap.put(threshold, rankedValue);
        double normRV = rankedValue / numberOfBlocks(blocks.size());
        logger.debug(String.format("%s, Norm RV, %s", blocks.toString(), normRV));
        normalizedRankedValues.put(threshold, normRV);
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
        return getMeasurmentByThreshold(threshold, rankedValueMap);
    }

    @Override
    public double getMRRByThreshold(double threshold) {
        return getMeasurmentByThreshold(threshold, mrrValueMap);
    }

    private double getMeasurmentByThreshold(double threshold, ListMultimap<Double, Double> listMultimap) {
        List<Double> values = listMultimap.get(threshold);
        return values.isEmpty() ? VALUE_NOT_EXISTS : values.get(values.size() - 1);
    }

    @Override
    public List<Double> getRankedValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(rankedValueMap);
    }

    @Override
    public List<Double> getMrrValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(mrrValueMap);
    }

    private List<Double> getMeasureSortedByThreshold(ListMultimap<Double, Double> measureValue) {
        TreeSet<Double> sortedKeys = new TreeSet<>(measureValue.keySet());
        List<Double> rankedValuesSortedByThreshold = new ArrayList<>();
        for (Double key : sortedKeys) {
            rankedValuesSortedByThreshold.add(measureValue.get(key).get(0));
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
    public List<Double> getNormalizedRankedValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(normalizedRankedValues);
    }

    @Override
    public FebrlMeasuresContext getFebrlMeasuresContext(Double threshold) {
        double averageRankedValue = this.getAverageRankedValue(threshold);
        double averageMRR = this.getAverageMRR(threshold);
        return new FebrlMeasuresContext(averageRankedValue, averageMRR);
    }

    @Override
    public DuplicateReductionContext representativesDuplicateElimanation(
            Multimap<Record, BlockWithData> duplicates, Multimap<Record, BlockWithData> cleaned, int cleanGoal) {
        logger.info("In 'dirtyBlocks', there are " + duplicates.keySet().size() + " representatives out of " + cleanGoal);
        logger.info("In 'cleanBlocks', there are " + cleaned.keySet().size() + " representatives out of " + cleanGoal);
        int millerSize = duplicates.size();
        int convexSize = cleaned.size();
        int duplicatesRemoved = millerSize - convexSize;
        float dupReductionPercentage = (millerSize - convexSize) / (float) millerSize;
        float improvementPercentage = (millerSize - convexSize) / (float) cleanGoal;

        return new DuplicateReductionContext(duplicatesRemoved, dupReductionPercentage, improvementPercentage);
    }

    private double getAverageRankedValue(double threshold) {
        return getAverageMeasurement(threshold, rankedValueMap);
    }

    private double getAverageMRR(double threshold) {
        return getAverageMeasurement(threshold, mrrValueMap);
    }

    private double getAverageMeasurement(double threshold, ListMultimap<Double, Double> listMultimap) {
        final List<Double> values = listMultimap.get(threshold);
        double[] valuesToPrimitive = ArrayUtils.toPrimitive(values.toArray(new Double[values.size()]));
        return StatUtils.sum(valuesToPrimitive) / values.size();
    }

    @Override
    public List<Double> getNormalizedMRRValuesSortedByThreshold() {
        return getMeasureSortedByThreshold(normalizedMRRValues);
    }
}
