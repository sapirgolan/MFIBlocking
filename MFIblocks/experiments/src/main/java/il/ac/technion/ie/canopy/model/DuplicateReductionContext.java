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
    private final int duplicatesRemoved;
    private final float dupReductionPercentage;
    private int representationDiff;

    public DuplicateReductionContext(int duplicatesRemoved, float dupReductionPercentage, float improvementPercentage) {
        this.duplicatesRemoved = duplicatesRemoved;
        this.dupReductionPercentage = dupReductionPercentage;
        this.improvementPercentage = improvementPercentage;
    }

    public float getImprovementPercentage() {
        return improvementPercentage * 100;
    }

    public int getDuplicatesRemoved() {
        return duplicatesRemoved;
    }

    public float getDupReductionPercentage() {
        return dupReductionPercentage * 100;
    }

    public int getRepresentationDiff() {
        return representationDiff;
    }

    public void setRepresentationDiff(int representationDiff) {
        this.representationDiff = representationDiff;
    }
}
