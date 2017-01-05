package il.ac.technion.ie.experiments.experimentRunners;

import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.*;
import il.ac.technion.ie.experiments.threads.CommandExacter;
import il.ac.technion.ie.measurements.service.MeasurService;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 30/12/2015.
 */
public abstract class AbstractExperiment {

    protected final ParsingService parsingService;
    protected final ProbabilityService probabilityService;
    protected final MeasurService measurService;
    protected final ExprimentsService exprimentsService;
    protected final ConvexBPService convexBPService;
    private final FuzzyService fuzzyService;

    protected IMeasurements measurements;

    private static final Logger logger = Logger.getLogger(AbstractExperiment.class);

    public AbstractExperiment() {
        parsingService = new ParsingService();
        probabilityService = new ProbabilityService();
        measurService = new MeasurService();
        exprimentsService = new ExprimentsService();
        fuzzyService = new FuzzyService();
        convexBPService = new ConvexBPService();
    }

    public abstract void runExperiments(String pathToDatasetFile);

    protected void executeExperimentWithThreshold(List<BlockWithData> blockWithDatas, Map<Integer, Double> splitProbabilityForBlocks, CommandExacter commandExacter, Double threshold) {
        try {
            logger.debug("splitting blocks");
            List<BlockWithData> splitedBlocks = fuzzyService.splitBlocks(blockWithDatas, splitProbabilityForBlocks, threshold);
            logger.debug("calculating probabilities on blocks after they were split");
            probabilityService.calcSimilaritiesAndProbabilitiesOfRecords(splitedBlocks);
            boolean convexBP = convexBPService.runConvexBP(commandExacter, threshold, splitedBlocks);
            if (convexBP) {
                logger.debug("Calculating measurements");
                measurements.calculate(splitedBlocks, threshold);
            }
        } catch (SizeNotEqualException e) {
            logger.error("Failed to split blocks since #blocs<>#splitProbabilities", e);
        }
    }

    protected void calculateMillerResults(List<BlockWithData> blockWithDatas) {
        probabilityService.calcSimilaritiesAndProbabilitiesOfRecords(blockWithDatas);
        measurements.calculate(blockWithDatas, 0.0);
    }
}
