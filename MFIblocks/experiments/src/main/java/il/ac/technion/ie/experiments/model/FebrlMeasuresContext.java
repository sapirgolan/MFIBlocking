package il.ac.technion.ie.experiments.model;

import il.ac.technion.ie.experiments.service.IMeasurements;

/**
 * Created by I062070 on 22/10/2015.
 */
public class FebrlMeasuresContext {

    private final double averageRankedValue;
    private final double averageMrr;

    public FebrlMeasuresContext(IMeasurements measurements, Double threshold) {
        this.averageRankedValue = measurements.getAverageRankedValue(threshold);
        this.averageMrr = measurements.getAverageMRR(threshold);
    }

    public double getAverageRankedValue() {
        return averageRankedValue;
    }

    public double getAverageMRR() {
        return averageMrr;
    }
}
