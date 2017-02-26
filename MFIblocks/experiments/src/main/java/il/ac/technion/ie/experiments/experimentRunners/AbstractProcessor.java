package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.*;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.model.BlockResults;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.BlocksPair;
import il.ac.technion.ie.experiments.model.CompareAlgorithmResults;
import il.ac.technion.ie.experiments.service.*;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * Created by I062070 on 12/02/2017.
 */
public class AbstractProcessor {
    private static final Logger logger = Logger.getLogger(AbstractProcessor.class);

    protected IMeasurements measurements;
    protected CanopyService canopyService;
    protected ProbabilityService probabilityService;
    protected ExprimentsService exprimentsService;

    protected BiMap<Record, BlockWithData> trueRepsMap;

    public AbstractProcessor() {
        this.canopyService = new CanopyService();
        this.probabilityService = new ProbabilityService();
        this.exprimentsService = new ExprimentsService();
    }

    protected void initMembersThatDependsOnOriginalDataset(File datasetFile, String permutationStr) {
        ParsingService parsingService = new ParsingService();
        if (datasetFile == null) {
            logger.error(String.format("no dataset exists for permutation %s", permutationStr));
            return;
        }
        List<BlockWithData> cleanBlocks = parsingService.parseDataset(datasetFile.getAbsolutePath());
        this.trueRepsMap = canopyService.getAllTrueRepresentatives(cleanBlocks);
        this.measurements = new Measurements(cleanBlocks.size());
    }

    protected void calculateBaselineResults(List<BlockWithData> blocks) {
        probabilityService.calcSimilaritiesAndProbabilitiesOfRecords(blocks);

    }

    protected Multimap<Record, BlockWithData> getRepresentatives(List<BlockWithData> blocks) {
        return exprimentsService.fetchRepresentatives(blocks);
    }

    protected DuplicateReductionContext calculateMeasurements(List<BlockWithData> babpBlocks, final Multimap<Record, BlockWithData> baselineReps) {
        Multimap<Record, BlockWithData> bcbpReps = this.getRepresentatives(babpBlocks);
        Multimap<Record, BlockWithData> baselineFiltered = filterBaselineByFrequency(baselineReps);

        DuplicateReductionContext resultContext = measurements.representativesDuplicateElimination(baselineReps, bcbpReps);

        BlockResults baselineBlockResults = measurements.calculateBlockResults(trueRepsMap, baselineReps);
        resultContext.setBaselineResults(baselineBlockResults);

        BlockResults bcbpBlockResults = measurements.calculateBlockResults(trueRepsMap, bcbpReps);
        resultContext.setBcbpResults(bcbpBlockResults);

        //compares between baseline and BCBP representatives
        CompareAlgorithmResults compareAlgResults = measurements.compareBaselineToBcbp(baselineReps, bcbpReps, trueRepsMap);
        resultContext.setCompareAlgsResults(compareAlgResults);

        //Bcbp block evaluation
        BlocksPair baselineBlocksForEvaluation = measurements.findBaselineBlocksForEvaluation(baselineFiltered);
        double blockShouldRemainPulled = measurements.percentageOfRecordsPulledFromGroundTruth(bcbpReps, baselineBlocksForEvaluation.getBlocksThatShouldRemain(), trueRepsMap);
        double blockShouldNotRemainPulled = measurements.percentageOfRecordsPulledFromGroundTruth(bcbpReps, baselineBlocksForEvaluation.getBlocksThatShouldNotRemain(), trueRepsMap);
        resultContext.setBlockShouldRemainPulled(blockShouldRemainPulled);
        resultContext.setBlockShouldNotRemainPulled(blockShouldNotRemainPulled);

        measurements.calcAverageBlockSize(babpBlocks, resultContext);
        resultContext.setNumberOfDirtyBlocks(babpBlocks.size());

        return resultContext;
    }

    protected Multimap<Record, BlockWithData> filterBaselineByFrequency(Multimap<Record, BlockWithData> baselineReps) {
        ImmutableListMultimap<Integer, Record> repsByFrequency = this.repsByFrequencyThatAppearsIn(baselineReps, trueRepsMap.keySet());
        final Set<Record> groundTruthRepsRepresentDualBlocks = this.getRecordsRepresentMoreThan(repsByFrequency, 1);

        return Multimaps.filterKeys(baselineReps, new Predicate<Record>() {
                @Override
                public boolean apply(Record input) {
                    return groundTruthRepsRepresentDualBlocks.contains(input);
                }
            });
    }

    protected void saveResultsToFS(Multimap<String, DuplicateReductionContext> results) {
        PersistResult.saveConvexBPResultsToCsv(results, false);
    }

    protected ImmutableListMultimap<Integer, Record> repsByFrequencyThatAppearsIn(final Multimap<Record, BlockWithData> baselineReps, Set<Record> records) {
        final Sets.SetView<Record> groundTruthRepsChosenInBaseline = Sets.intersection(baselineReps.keySet(), records);
        Function<Record, Integer> blocksCounter = new Function<Record, Integer>() {
            @Override
            public Integer apply(Record input) {
                if (groundTruthRepsChosenInBaseline.contains(input)) {
                    return baselineReps.get(input).size();
                }
                return -1;
            }
        };
        return Multimaps.index(baselineReps.keySet(), blocksCounter);
    }

    protected Set<Record> getRecordsRepresentMoreThan(Multimap<Integer, Record> repsByFrequency, int threshold) {
        Set<Record> groundTruthRepsRepresentMoreThan = new HashSet<>();
        for (Integer frequency : repsByFrequency.keySet()) {
            if (frequency > threshold) {
                groundTruthRepsRepresentMoreThan.addAll(repsByFrequency.get(frequency));
            }
        }
        return groundTruthRepsRepresentMoreThan;
    }
}
