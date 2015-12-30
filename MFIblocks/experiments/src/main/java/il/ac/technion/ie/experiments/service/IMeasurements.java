package il.ac.technion.ie.experiments.service;

import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.FebrlMeasuresContext;
import il.ac.technion.ie.model.Record;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by I062070 on 17/10/2015.
 */
public interface IMeasurements {
    void calculate(List<BlockWithData> blocks, double threshold);

    double getRankedValueByThreshold(double threshold);

    double getMRRByThreshold(double threshold);

    List<Double> getRankedValuesSortedByThreshold();

    List<Double> getMrrValuesSortedByThreshold();

    List<Double> getThresholdSorted();

    List<Double> getNormalizedMRRValuesSortedByThreshold();

    List<Double> getNormalizedRankedValuesSortedByThreshold();

    FebrlMeasuresContext getFebrlMeasuresContext(Double threshold);

    /**
     * The method calculates the number of records that used to represent more than one block,
     * but now represent only a single block.
     * <p/>
     * <br><br>
     * Also know as measurement #4
     *
     * @param duplicates
     * @param cleaned
     * @param cleanGoal
     * @return
     */
    DuplicateReductionContext representativesDuplicateElimanation(Multimap<Record, BlockWithData> duplicates, Multimap<Record, BlockWithData> cleaned, int cleanGoal);

    /**
     * This method calculates the number of records in @param source that were not present in @param other.
     * The result is stored in @param reductionContext. <br><br>
     * <p/>
     * Also know as measurement #1
     *
     * @param source           source records, preferably the True representatives.
     * @param other            records from other source, preferably the representatives that were found in blocks
     * @param reductionContext context to store the result
     */
    void representationDiff(final Set<Record> source, final Set<Record> other, DuplicateReductionContext reductionContext);

    double calcPowerOfRep(final Map<Record, BlockWithData> trueRepsMap, final Multimap<Record, BlockWithData> convexBPRepresentatives, DuplicateReductionContext reductionContext);

    double calcWisdomCrowds(Set<BlockWithData> cleanBlocks, Set<BlockWithData> dirtyBlocks, DuplicateReductionContext reductionContext);
}
