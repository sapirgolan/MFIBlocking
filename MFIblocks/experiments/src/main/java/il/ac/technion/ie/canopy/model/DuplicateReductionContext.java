package il.ac.technion.ie.canopy.model;

/**
 * Created by I062070 on 18/12/2015.
 * <p/>
 * The class has three members:
 * <ol>
 * <li>duplicatesRemoved - the number of representatives that used to represent several blocks but now represent a single block.
 * The value can be obtained by {@link #getDuplicatesRemoved()}</li>
 * </ol>
 * duplicatesRemoved
 */
public class DuplicateReductionContext {
    private final float duplicatesRemoved;
    private float representationDiff;
    private double represntativesPower;
    private double wisdomCrowds;

    public DuplicateReductionContext(int duplicatesRemoved) {
        this.duplicatesRemoved = duplicatesRemoved;
    }

    public DuplicateReductionContext(float duplicatesRemoved, float representationDiff, double represntativesPower, double wisdomCrowds) {
        this.duplicatesRemoved = duplicatesRemoved;
        this.representationDiff = representationDiff;
        this.represntativesPower = represntativesPower;
        this.wisdomCrowds = wisdomCrowds;
    }

    public float getDuplicatesRemoved() {
        return duplicatesRemoved;
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
