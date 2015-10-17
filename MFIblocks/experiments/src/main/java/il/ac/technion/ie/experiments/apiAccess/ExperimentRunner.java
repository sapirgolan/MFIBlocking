package il.ac.technion.ie.experiments.apiAccess;

import il.ac.technion.ie.experiments.Utils.ExpFileUtils;
import il.ac.technion.ie.experiments.exception.NoValueExistsException;
import il.ac.technion.ie.experiments.exception.OSNotSupportedException;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.ConvexBPContext;
import il.ac.technion.ie.experiments.model.UaiVariableContext;
import il.ac.technion.ie.experiments.parsers.UaiBuilder;
import il.ac.technion.ie.experiments.service.*;
import il.ac.technion.ie.experiments.threads.CommandExacter;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 27/08/2015.
 */
public class ExperimentRunner {

    static final Logger logger = Logger.getLogger(ExperimentRunner.class);

    private final FuzzyService fuzzyService;
    private ParsingService parsingService;
    private ProbabilityService probabilityService;
    private iMeasurService measurService;
    private ExprimentsService exprimentsService;
    private IMeasurements measurements;

    public ExperimentRunner() {
        parsingService = new ParsingService();
        probabilityService = new ProbabilityService();
        measurService = new MeasurService();
        exprimentsService = new ExprimentsService();
        fuzzyService = new FuzzyService();
        measurements = new Measurements();
    }

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.err.println("There are no file arguments!");
            System.exit(-1);
        }
        String datasetFile = args[0];
        ExperimentRunner experimentRunner = new ExperimentRunner();
//        experimentRunner.runSimpleExp(datasetFile);
        experimentRunner.runExperiments(datasetFile);
    }

    public void runSimpleExp(String datasetPath) {
        List<BlockWithData> blockWithDatas = parsingService.parseDataset(datasetPath);
        probabilityService.calcProbabilitiesOfRecords(blockWithDatas);
        double rankedValue = measurService.calcRankedValue(blockWithDatas);
        double mrr = measurService.calcMRR(blockWithDatas);
        System.out.println("The RankedValue is: " + rankedValue);
        System.out.println("The MRR score is: " + mrr);
        String allBlocksFilePath = ExpFileUtils.getOutputFilePath("AllBlocks", ".csv");
        parsingService.writeBlocks(blockWithDatas, allBlocksFilePath);
        if (rankedValue > 0 || mrr < 1) {
            List<BlockWithData> filteredBlocks = exprimentsService.filterBlocksWhoseTrueRepIsNotFirst(blockWithDatas);
            String outputFilePath = ExpFileUtils.getOutputFilePath("BlocksWhereMillerWasWrong", ".csv");
            parsingService.writeBlocks(filteredBlocks, outputFilePath);
            System.out.print("Total of " + filteredBlocks.size() + " blocks representative is wrong. ");
            System.out.println("output file can be found at: " + outputFilePath);
        }
    }

    public void runExperiments(String pathToDatasetFile) {
        final List<BlockWithData> blockWithDatas = parsingService.parseDataset(pathToDatasetFile);
        final Map<Integer, Double> splitProbabilityForBlocks = exprimentsService.sampleSplitProbabilityForBlocks(blockWithDatas);
        List<Double> thresholds = exprimentsService.getThresholdSorted(splitProbabilityForBlocks.values());


        CommandExacter commandExacter = new CommandExacter();
        logger.info("Will execute experiments on following split thresholds: " + StringUtils.join(thresholds, ','));
        for (Double threshold : thresholds) {
            logger.info("Executing experiment with threshold " + threshold);
            try {
                logger.debug("splitting blocks");
                List<BlockWithData> splitedBlocks = fuzzyService.splitBlocks(blockWithDatas, splitProbabilityForBlocks, threshold);
                logger.debug("calculating probabilities on blocks after they were split");
                probabilityService.calcProbabilitiesOfRecords(splitedBlocks);
                UaiBuilder uaiBuilder = new UaiBuilder(splitedBlocks);
                logger.debug("creating UAI file");
                UaiVariableContext uaiVariableContext = uaiBuilder.createUaiContext();
                logger.debug("UAI file was created at: " + uaiVariableContext.getUaiFile().getAbsoluteFile());
                ConvexBPContext convexBPContext = exprimentsService.createConvexBPContext(uaiVariableContext);
                convexBPContext.setThreshold(threshold);
                //critical section - cannot be multi-thread
                File outputFile = commandExacter.execute(convexBPContext);
                if (outputFile.exists()) {
                    logger.debug("Binary output of convexBP was created on: " + outputFile.getAbsolutePath());
                    UaiConsumer uaiConsumer = new UaiConsumer(uaiVariableContext, outputFile);
                    uaiConsumer.consumePotentials();
                    FileUtils.forceDeleteOnExit(outputFile);
                    logger.debug("Applying new probabilities on blocks");
                    uaiConsumer.applyNewProbabilities(splitedBlocks);
                    logger.debug("Calculating measurements");
                    measurements.calculate(splitedBlocks, threshold);
                }
            } catch (SizeNotEqualException e) {
                logger.error("Failed to split blocks since #blocs<>#splitProbabilities", e);
            } catch (IOException e) {
                logger.error("Cannot create context for ConvexBP algorithm", e);
            } catch (InterruptedException e) {
                logger.error("Failed to wait till the execution of ConvexBP algorithm has finished", e);
                e.printStackTrace();
            } catch (OSNotSupportedException e) {
                logger.error("Cannot run ConvexBP algorithm on current machine", e);
            } catch (NoValueExistsException e) {
                logger.error("Failed to consume new probabilities", e);
            }
        }
        try {
            File expResults = new File("expResults.csv");
            boolean isNewFileCreated = expResults.createNewFile();
            if (isNewFileCreated) {
                parsingService.writeExperimentsMeasurements(measurements, expResults);
            } else {
                logger.warn("Failed to create file for measurements therefore no results are results will be given");
            }
        } catch (SizeNotEqualException e) {
            logger.error("Failed to write measurements of Experiment", e);
        } catch (IOException e) {
            logger.error("Failed to create file for measurements of Experiment", e);
        }
    }

}
