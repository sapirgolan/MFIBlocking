package il.ac.technion.ie.canopy.model;

import il.ac.technion.ie.experiments.model.BlockResults;
import il.ac.technion.ie.experiments.model.CompareAlgorithmResults;

/**
 * Created by I062070 on 18/12/2015.
 * <p/>
 * The class has four members. each one store a value that was calculated by {@link il.ac.technion.ie.experiments.service.IMeasurements}
 * <ol>
 * <li>duplicatesRemoved - calculated by {@link il.ac.technion.ie.experiments.service.IMeasurements#representativesDuplicateElimination}</li>
 * <li>missingRealRepresentatives - calculated by {@link il.ac.technion.ie.experiments.service.IMeasurements#missingRealRepresentatives(java.util.Set, java.util.Set, DuplicateReductionContext)}</li>
 * <li>representativesPower - calculated by {@link il.ac.technion.ie.experiments.service.IMeasurements#calcPowerOfRep_Recall(java.util.Map, com.google.common.collect.Multimap, DuplicateReductionContext)}</li>
 * <li>wisdomCrowds - calculated by {@link il.ac.technion.ie.experiments.service.IMeasurements#calcWisdomCrowd_Precision(java.util.Set, java.util.Set, DuplicateReductionContext)}  </li>
 * </ol>
 * duplicatesRemoved
 */
public class DuplicateReductionContext {
    private final float duplicatesRemoved;
    private double representationDiff;
    private double representativesPower;
    private double wisdomCrowds;
    private double numberOfDirtyBlocks;
    private double duplicatesRealRepresentatives;
    private double averageBlockSize;
    private long baselineDuration;
    private long bcbpDuration;
    private int baselineMrr;
    private int bcbpMrr;
    private double baselineRecall;
    private double bcbpRecall;
    private double baselinePrecision;
    private double bcbpPrecision;
    private BlockResults baselineResults;
    private BlockResults bcbpResults;
    private CompareAlgorithmResults compareAlgsResults;

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

    public double getRepresentationDiff() {
        return representationDiff;
    }

    public void setRepresentationDiff(double representationDiff) {
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

    public void setNumberOfDirtyBlocks(double size) {
        this.numberOfDirtyBlocks = size;
    }

    public double getNumberOfDirtyBlocks() {
        return numberOfDirtyBlocks;
    }

    public void setDuplicatesRealRepresentatives(double duplicatesRealRepresentatives) {
        this.duplicatesRealRepresentatives = duplicatesRealRepresentatives;
    }

    public double getDuplicatesRealRepresentatives() {
        return duplicatesRealRepresentatives;
    }

    public double getAverageBlockSize() {
        return averageBlockSize;
    }

    public void setAverageBlockSize(double averageBlockSize) {
        this.averageBlockSize = averageBlockSize;
    }

    public void setBaselineDuration(long baselineDuration) {
        this.baselineDuration = baselineDuration;
    }

    public long getBaselineDuration() {
        return baselineDuration;
    }

    public long getBcbpDuration() {
        return bcbpDuration;
    }

    public void setBcbpDuration(long bcbpDuration) {
        this.bcbpDuration = bcbpDuration;
    }

    public void setBaselineMrr(int baselineMrr) {
        this.baselineMrr = baselineMrr;
    }

    public void setBcbpMrr(int bcbpMrr) {
        this.bcbpMrr = bcbpMrr;
    }

    public void setBaselineRecall(double baselineRecall) {
        this.baselineRecall = baselineRecall;
    }

    public void setBcbpRecall(double bcbpRecall) {
        this.bcbpRecall = bcbpRecall;
    }

    public void setBaselinePrecision(double baselinePrecision) {
        this.baselinePrecision = baselinePrecision;
    }

    public void setBcbpPrecision(double bcbpPrecision) {
        this.bcbpPrecision = bcbpPrecision;
    }

    public int getBaselineMrr() {
        return baselineMrr;
    }

    public int getBcbpMrr() {
        return bcbpMrr;
    }

    public double getBaselineRecall() {
        return baselineRecall;
    }

    public double getBcbpRecall() {
        return bcbpRecall;
    }

    public double getBaselinePrecision() {
        return baselinePrecision;
    }

    public double getBcbpPrecision() {
        return bcbpPrecision;
    }

    public void setBaselineResults(BlockResults baselineResults) {
        this.baselineResults = baselineResults;
    }

    public void setBcbpResults(BlockResults bcbpResults) {
        this.bcbpResults = bcbpResults;
    }

    public void setCompareAlgsResults(CompareAlgorithmResults compareAlgsResults) {
        this.compareAlgsResults = compareAlgsResults;
    }

    public BlockResults getBaselineResults() {
        return baselineResults;
    }

    public BlockResults getBcbpResults() {
        return bcbpResults;
    }

    public CompareAlgorithmResults getCompareAlgsResults() {
        return compareAlgsResults;
    }
}
