package il.ac.technion.ie.experiments.model;

/**
 * Created by I062070 on 10/02/2017.
 */
public class BlockResults {
    private final double recall;
    private final double precision;
    private final float trueRepsPercentage;
    private final int mrr;

    public BlockResults(double recall, double precision, float trueRepsPercentage, int mrr) {
        this.recall = recall;
        this.precision = precision;
        this.trueRepsPercentage = trueRepsPercentage;
        this.mrr = mrr;
    }

    public double getRecall() {
        return recall;
    }

    public double getPrecision() {
        return precision;
    }

    public float getTrueRepsPercentage() {
        return trueRepsPercentage;
    }

    public int getMrr() {
        return mrr;
    }
}
