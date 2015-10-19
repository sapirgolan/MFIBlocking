package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;

import java.util.List;

/**
 * Created by I062070 on 17/10/2015.
 */
public interface IMeasurements {
    void calculate(List<BlockWithData> blocks, double threshold);

    double getRankedValueByThreshold(double threshold);

    double getMRRByThreshold(double threshold);

    List<Double> getRankedValuesSortedByThreshold();

    List<Double> getMrrValuesSortedByThreshold();

    List<Double> getThresholdSorted();

    List<Double> getNormalizedMRRValuesSortedByThreshold();

    List<Double> getNormalizedRankedValuesSortedByThreshold();
}
