package il.ac.technion.ie.canopy.model;

/**
 * Created by I062070 on 18/12/2015.
 * <p/>
 * The class has four members. each one store a value that was calculated by {@link il.ac.technion.ie.experiments.service.IMeasurements}
 * <ol>
 * <li>duplicatesRemoved - calculated by {@link il.ac.technion.ie.experiments.service.IMeasurements#representativesDuplicateElimination}</li>
 * <li>representationDiff - calculated by {@link il.ac.technion.ie.experiments.service.IMeasurements#representationDiff(java.util.Set, java.util.Set, DuplicateReductionContext)}</li>
 * <li>representativesPower - calculated by {@link il.ac.technion.ie.experiments.service.IMeasurements#calcPowerOfRep(java.util.Map, com.google.common.collect.Multimap, DuplicateReductionContext)}</li>
 * <li>wisdomCrowds - calculated by {@link il.ac.technion.ie.experiments.service.IMeasurements#calcWisdomCrowds(java.util.Set, java.util.Set, DuplicateReductionContext)}  </li>
 * </ol>
 * duplicatesRemoved
 */
public class DuplicateReductionContext {
    private final float duplicatesRemoved;
    private float representationDiff;
    private double representativesPower;
    private double wisdomCrowds;

    public DuplicateReductionContext(int duplicatesRemoved) {
        this.duplicatesRemoved = duplicatesRemoved;
    }

    public DuplicateReductionContext(float duplicatesRemoved, float representationDiff, double representativesPower, double wisdomCrowds) {
        this.duplicatesRemoved = duplicatesRemoved;
        this.representationDiff = representationDiff;
        this.representativesPower = representativesPower;
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

    public double getRepresentativesPower() {
        return representativesPower;
    }

    public void setRepresentativesPower(double representativePower) {
        this.representativesPower = representativePower;
    }

    public void setWisdomCrowds(double wisdomCrowds) {
        this.wisdomCrowds = wisdomCrowds;
    }

    public double getWisdomCrowds() {
        return wisdomCrowds;
    }
}
