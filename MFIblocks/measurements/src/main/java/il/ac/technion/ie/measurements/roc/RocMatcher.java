package il.ac.technion.ie.measurements.roc;

import cern.colt.function.DoubleFunction;

/**
 * Created by I062070 on 18/06/2015.
 */
public class RocMatcher implements DoubleFunction {
    private double threshold;

    public RocMatcher() {
        this.threshold = 0;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    @Override
    public double apply(double cellValue) {
        if (cellValue > threshold) {
            return 1;
        }
        return 0;
    }
}
