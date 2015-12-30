package il.ac.technion.ie.canopy.model;

/**
 * Created by I062070 on 18/12/2015.
 * <p/>
 * The class has three members:
 * <ol>
 * <li>duplicatesRemoved - the number of representatives that used to represent several blocks but now represent a single block.
 * The value can be obtained by {@link #getDuplicatesRemoved()}</li>
 * <li>dupReductionPercentage - the ratio between the number of representatives removed and the number of duplicate representatives.
 * The value can be obtained by {@link #getDupReductionPercentage()} ()}</li>
 * <li>improvementPercentage - the ratio between the number of representatives removed and the number of real representatives.
 * The value can be obtained by {@link #getImprovementPercentage()} ()}</li>
 * </ol>
 * duplicatesRemoved
 */
public class DuplicateReductionContext {
    private final float improvementPercentage;
    private final float duplicatesRemoved;
    private final float dupReductionPercentage;
    private float representationDiff;
    private double represntativesPower;
    private double wisdomCrowds;

    public DuplicateReductionContext(int duplicatesRemoved, float dupReductionPercentage, float improvementPercentage) {
        this.duplicatesRemoved = duplicatesRemoved;
        this.dupReductionPercentage = dupReductionPercentage;
        this.improvementPercentage = improvementPercentage;
    }

    public DuplicateReductionContext(float improvementPercentage, float duplicatesRemoved, float dupReductionPercentage, float representationDiff, double represntativesPower, double wisdomCrowds) {
        this.improvementPercentage = improvementPercentage;
        this.duplicatesRemoved = duplicatesRemoved;
        this.dupReductionPercentage = dupReductionPercentage;
        this.representationDiff = representationDiff;
        this.represntativesPower = represntativesPower;
        this.wisdomCrowds = wisdomCrowds;
    }

    public float getImprovementPercentage() {
        return improvementPercentage * 100;
    }

    public float getDuplicatesRemoved() {
        return duplicatesRemoved;
    }

    public float getDupReductionPercentage() {
        return dupReductionPercentage * 100;
    }

    public float getRepresentationDiff() {
        return representationDiff;
    }

    public void setRepresentationDiff(int representationDiff) {
        this.representationDiff = representationDiff;
    }

    public double getRepresntativesPower() {
        return represntativesPower;
    }

    public void setRepresntativesPower(double representativePower) {
        this.represntativesPower = representativePower;
    }

    public void setWisdomCrowds(double wisdomCrowds) {
        this.wisdomCrowds = wisdomCrowds;
    }

    public double getWisdomCrowds() {
        return wisdomCrowds;
    }
}
