package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.model.BlockResults;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.CompareAlgorithmResults;
import il.ac.technion.ie.experiments.service.*;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

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

    protected DuplicateReductionContext calculateMeasurements(List<BlockWithData> babpBlocks, Multimap<Record, BlockWithData> baselineReps) {
        Multimap<Record, BlockWithData> bcbpReps = this.getRepresentatives(babpBlocks);
        DuplicateReductionContext resultContext = measurements.representativesDuplicateElimination(baselineReps, bcbpReps);

        BlockResults baselineBlockResults = measurements.calculateBlockResults(trueRepsMap, baselineReps);
        resultContext.setBaselineResults(baselineBlockResults);

        BlockResults bcbpBlockResults = measurements.calculateBlockResults(trueRepsMap, bcbpReps);
        resultContext.setBcbpResults(bcbpBlockResults);

        //compares between baseline and BCBP representatives
        CompareAlgorithmResults compareAlgResults = measurements.compareBaselineToBcbp(baselineReps, bcbpReps, trueRepsMap);
        resultContext.setCompareAlgsResults(compareAlgResults);

        measurements.calcAverageBlockSize(babpBlocks, resultContext);
        resultContext.setNumberOfDirtyBlocks(babpBlocks.size());

        return resultContext;
    }

    protected void saveResultsToFS(Multimap<String, DuplicateReductionContext> results) {
        PersistResult.saveConvexBPResultsToCsv(results, false);
    }
}
