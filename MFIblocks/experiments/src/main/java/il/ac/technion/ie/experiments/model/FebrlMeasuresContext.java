package il.ac.technion.ie.experiments.model;

/**
 * Created by I062070 on 22/10/2015.
 */
public class FebrlMeasuresContext {

    private final double averageRankedValue;
    private final double averageMrr;

    public FebrlMeasuresContext(double averageRankedValue, double averageMrr) {
        this.averageRankedValue = averageRankedValue;
        this.averageMrr = averageMrr;
    }


    public double getAverageRankedValue() {
        return averageRankedValue;
    }

    public double getAverageMRR() {
        return averageMrr;
    }
}
