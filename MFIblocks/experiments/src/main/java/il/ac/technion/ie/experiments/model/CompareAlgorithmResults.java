package il.ac.technion.ie.experiments.model;

/**
 * Created by I062070 on 10/02/2017.
 */
public class CompareAlgorithmResults {
    private final int removedGroundTruthReps;
    private final int newAddedReps;
    private final double drr;

    public CompareAlgorithmResults(int removedGroundTruthReps, int newAddedReps, double drr) {
        this.removedGroundTruthReps = removedGroundTruthReps;
        this.newAddedReps = newAddedReps;
        this.drr = drr;
    }

    public int getRemovedGroundTruthReps() {
        return removedGroundTruthReps;
    }

    public int getNewAddedReps() {
        return newAddedReps;
    }

    public double getDrr() {
        return drr;
    }
}
