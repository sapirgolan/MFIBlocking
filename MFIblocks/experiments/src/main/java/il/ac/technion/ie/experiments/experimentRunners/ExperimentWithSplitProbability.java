package il.ac.technion.ie.experiments.experimentRunners;

import il.ac.technion.ie.experiments.Utils.ExpFileUtils;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.service.Measurements;
import il.ac.technion.ie.experiments.threads.CommandExacter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 30/12/2015.
 */
public class ExperimentWithSplitProbability extends AbstractExperiment {

    static final Logger logger = Logger.getLogger(ExperimentWithSplitProbability.class);

    public void runExperiments(String pathToDatasetFile) {
        final List<BlockWithData> blockWithDatas = runExperiment(pathToDatasetFile);
        calculateMillerResults(blockWithDatas);
        saveResultsToCsvFile();
    }

    private List<BlockWithData> runExperiment(String pathToDatasetFile) {
        final List<BlockWithData> blockWithDatas = parsingService.parseDataset(pathToDatasetFile);
        measurements = new Measurements(blockWithDatas.size());
        final Map<Integer, Double> splitProbabilityForBlocks = exprimentsService.sampleSplitProbabilityForBlocks(blockWithDatas);
        List<Double> thresholds = exprimentsService.getThresholdSorted(splitProbabilityForBlocks.values());

        CommandExacter commandExacter = new CommandExacter();
        logger.info("Will execute experiments on following split thresholds: " + StringUtils.join(thresholds, ','));
        for (Double threshold : thresholds) {
            logger.info("Executing experiment with threshold " + threshold);
            executeExperimentWithThreshold(blockWithDatas, splitProbabilityForBlocks, commandExacter, threshold);
        }
        return blockWithDatas;
    }

    private void saveResultsToCsvFile() {
        try {
            File expResults = ExpFileUtils.createOutputFile("expResults.csv");
            if (expResults != null) {
                parsingService.writeExperimentsMeasurements(measurements, expResults);
            } else {
                logger.warn("Failed to create file for measurements therefore no results are results will be given");
            }
        } catch (SizeNotEqualException e) {
            logger.error("Failed to write measurements of Experiment", e);
        }
    }

}
