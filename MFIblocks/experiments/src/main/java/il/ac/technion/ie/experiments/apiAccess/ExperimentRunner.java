package il.ac.technion.ie.experiments.apiAccess;

import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.algorithm.Canopy;
import il.ac.technion.ie.canopy.exception.CanopyParametersException;
import il.ac.technion.ie.canopy.exception.InvalidSearchResultException;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.canopy.model.CanopyInteraction;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.Utils.ExpFileUtils;
import il.ac.technion.ie.experiments.exception.NoValueExistsException;
import il.ac.technion.ie.experiments.exception.OSNotSupportedException;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.*;
import il.ac.technion.ie.experiments.parsers.UaiBuilder;
import il.ac.technion.ie.experiments.service.*;
import il.ac.technion.ie.experiments.threads.CommandExacter;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;
import il.ac.technion.ie.model.Record;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by I062070 on 27/08/2015.
 */
public class ExperimentRunner {

    static final Logger logger = Logger.getLogger(ExperimentRunner.class);
    public static final int NUMBER_OF_EXPERIMENTS = 2;

    private final FuzzyService fuzzyService;
    private ParsingService parsingService;
    private ProbabilityService probabilityService;
    private iMeasurService measurService;
    private ExprimentsService exprimentsService;
    private IMeasurements measurements;
    private CanopyService canopyService;

    public ExperimentRunner() {
        parsingService = new ParsingService();
        probabilityService = new ProbabilityService();
        measurService = new MeasurService();
        exprimentsService = new ExprimentsService();
        fuzzyService = new FuzzyService();
        canopyService = new CanopyService();
    }

    public static void main(String[] args) {
        ArgumentsContext context = new ArgumentsContext(args).invoke();
        ExperimentRunner experimentRunner = new ExperimentRunner();
        if (context.size() == 1) {
            //        experimentRunner.runSimpleExp(context.getPathToDataset());
//            experimentRunner.runExperiments(context.getPathToDataset());
            try {
                experimentRunner.runExperimentsWithCanopy(context.getPathToDataset());
            } catch (CanopyParametersException | InvalidSearchResultException e) {
                logger.error("Failed to run Canopy experiment", e);
            }
        } else {
            experimentRunner.findStatisticsOnDatasets(context.getPathToDataset());
//            experimentRunner.runFebrlExperiments(context.getPathToDataset(), context.getThresholds());
        }
    }

    public void runSimpleExp(String datasetPath) {
        List<BlockWithData> blockWithDatas = parsingService.parseDataset(datasetPath);
        probabilityService.calcSimilaritiesAndProbabilitiesOfRecords(blockWithDatas);
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
        measurements = new Measurements(blockWithDatas.size());
        final Map<Integer, Double> splitProbabilityForBlocks = exprimentsService.sampleSplitProbabilityForBlocks(blockWithDatas);
        List<Double> thresholds = exprimentsService.getThresholdSorted(splitProbabilityForBlocks.values());

        CommandExacter commandExacter = new CommandExacter();
        logger.info("Will execute experiments on following split thresholds: " + StringUtils.join(thresholds, ','));
        for (Double threshold : thresholds) {
            logger.info("Executing experiment with threshold " + threshold);
            executeExperimentWithThreshold(blockWithDatas, splitProbabilityForBlocks, commandExacter, threshold);
        }
        calculateMillerResults(blockWithDatas);
        saveResultsToCsvFile();
    }

    public void runExperimentsWithCanopy(String datasetPath) throws CanopyParametersException, InvalidSearchResultException {
        List<BlockWithData> cleanBlocks = parsingService.parseDataset(datasetPath);
        List<Record> records = getRecordsFromBlcoks(cleanBlocks);
        Canopy canopy = new Canopy(records, 0.15, 0.05);
        canopy.initSearchEngine(new CanopyInteraction());
        List<CanopyCluster> canopies = canopy.createCanopies();
        List<BlockWithData> dirtyBlocks = canopyService.convertCanopiesToBlocks(canopies);
        logger.info("Converted " + dirtyBlocks.size() + " canopies to blocks. " + (canopies.size() - dirtyBlocks.size()) + " were of size 1 and therefore removed");
        this.measurements = new Measurements(cleanBlocks.size());
        calculateMillerResults(dirtyBlocks);
        Multimap<Record, BlockWithData> millerRepresentatives = exprimentsService.fetchRepresentatives(dirtyBlocks);
        try {
            boolean convexBP = runConvexBP(new CommandExacter(), 0.0, dirtyBlocks);
            if (!convexBP) {
                logger.warn("Failed to run ConvexBP on canopy clusters");
                return;
            }
            Multimap<Record, BlockWithData> convexBPRepresentatives = exprimentsService.fetchRepresentatives(dirtyBlocks);
            DuplicateReductionContext reductionContext = measurements.representativesDuplicateElimanation(
                    millerRepresentatives, convexBPRepresentatives, cleanBlocks.size());
            Multimap<Record, BlockWithData> trueRepsMap = exprimentsService.fetchRepresentatives(cleanBlocks);
            measurements.representationDiff(trueRepsMap.keySet(), convexBPRepresentatives.keySet(), reductionContext);
            measurements.calcPowerOfRep(trueRepsMap, convexBPRepresentatives, reductionContext);

            saveConvexBPResultsToCsv(reductionContext);
        } catch (SizeNotEqualException e) {
            logger.error("Failed to create probabilities matrices for convexBP");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Cannot create context for ConvexBP algorithm", e);
        } catch (OSNotSupportedException e) {
            logger.error("Cannot run ConvexBP algorithm on current machine", e);
        } catch (InterruptedException e) {
            logger.error("Failed to wait till the execution of ConvexBP algorithm has finished", e);
        } catch (NoValueExistsException e) {
            logger.error("Failed to consume new probabilities", e);
        }
        System.exit(0);
        return;
    }

    private List<Record> getRecordsFromBlcoks(List<BlockWithData> cleanBlocks) {
        Set<Record> set = new HashSet<>();
        for (BlockWithData cleanBlock : cleanBlocks) {
            set.addAll(cleanBlock.getMembers());
        }
        return new ArrayList<>(set);
    }

    private void executeExperimentWithThreshold(List<BlockWithData> blockWithDatas, Map<Integer, Double> splitProbabilityForBlocks, CommandExacter commandExacter, Double threshold) {
        try {
            logger.debug("splitting blocks");
            List<BlockWithData> splitedBlocks = fuzzyService.splitBlocks(blockWithDatas, splitProbabilityForBlocks, threshold);
            logger.debug("calculating probabilities on blocks after they were split");
            probabilityService.calcSimilaritiesAndProbabilitiesOfRecords(splitedBlocks);
            boolean convexBP = runConvexBP(commandExacter, threshold, splitedBlocks);
            if (convexBP) {
                logger.debug("Calculating measurements");
                measurements.calculate(splitedBlocks, threshold);
            }
        } catch (SizeNotEqualException e) {
            logger.error("Failed to split blocks since #blocs<>#splitProbabilities", e);
        } catch (IOException e) {
            logger.error("Cannot create context for ConvexBP algorithm", e);
        } catch (InterruptedException e) {
            logger.error("Failed to wait till the execution of ConvexBP algorithm has finished", e);
        } catch (OSNotSupportedException e) {
            logger.error("Cannot run ConvexBP algorithm on current machine", e);
        } catch (NoValueExistsException e) {
            logger.error("Failed to consume new probabilities", e);
        }
    }

    private boolean runConvexBP(CommandExacter commandExacter, Double threshold, List<BlockWithData> splitedBlocks) throws SizeNotEqualException, IOException, OSNotSupportedException, InterruptedException, NoValueExistsException {
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
            return true;
        }
        return false;
    }

    private void runFebrlExperiments(String pathToDir, List<Double> thresholds) {
        Collection<File> datasets = exprimentsService.findDatasets(pathToDir, false);

        Map<List<BlockWithData>, Integer> datasetToFebrlParamMap = parseDatasetsToListsOfBlocks(datasets);

        CommandExacter commandExacter = new CommandExacter();

        FebrlContext febrlContext = new FebrlContext();
        for (Double threshold : thresholds) {
            for (List<BlockWithData> blocks : datasetToFebrlParamMap.keySet()) {
                measurements = new Measurements(blocks.size());

                for (int i = 0; i < NUMBER_OF_EXPERIMENTS; i++) {
                    logger.debug(String.format("Executing #%d out of %d experiments with threshold: %s", i, NUMBER_OF_EXPERIMENTS, threshold));
                    final Map<Integer, Double> splitProbabilityForBlocks = exprimentsService.sampleSplitProbabilityForBlocks(blocks);
                    this.executeExperimentWithThreshold(blocks, splitProbabilityForBlocks, commandExacter, threshold);
                }
                febrlContext.add(threshold, datasetToFebrlParamMap.get(blocks), measurements);
            }
            saveFebrlResultsToCsv(febrlContext, threshold);
        }

    }

    private void findStatisticsOnDatasets(String pathToDir) {
        Collection<File> datasets = exprimentsService.findDatasets(pathToDir, true);
        List<DatasetStatistics> datasetStatisticses = calculateStatistics(datasets);
        saveStatisticsToCsv(datasetStatisticses);
    }

    private void saveStatisticsToCsv(List<DatasetStatistics> datasetStatisticses) {
        File expResults = ExpFileUtils.createOutputFile("dataSetsStatistics.csv");
        if (expResults != null) {
            parsingService.writeStatistics(datasetStatisticses, expResults);
        } else {
            logger.warn("Failed to create file for statistics, therefore no results are results will be given");
        }
    }

    private List<DatasetStatistics> calculateStatistics(Collection<File> datasets) {
        List<DatasetStatistics> statisticsList = new ArrayList<>();
        for (File dataset : datasets) {
            DatasetStatistics statistics = new DatasetStatistics(dataset.getName());
            List<BlockWithData> blocks = parsingService.parseDataset(dataset.getAbsolutePath());
            statistics.setNumberOfBlocks(blocks.size());
            statistics.setAvgBlockSize(exprimentsService.calcAvgBlockSize(blocks));
            statisticsList.add(statistics);
        }
        return statisticsList;
    }

    private Map<List<BlockWithData>, Integer> parseDatasetsToListsOfBlocks(Collection<File> datasets) {
        Map<List<BlockWithData>, Integer> listIntegerHashMap = new HashMap<>();
        for (File dataset : datasets) {
            Integer febrlParamValue = exprimentsService.getParameterValue(dataset);
            if (febrlParamValue != null) {
                List<BlockWithData> blocks = parsingService.parseDataset(dataset.getAbsolutePath());
                listIntegerHashMap.put(blocks, febrlParamValue);
            } else {
                logger.error("Failed to determine febrlParamValue, therefore will not process file named " + dataset.getAbsolutePath());
            }
        }
        return listIntegerHashMap;
    }

    private void calculateMillerResults(List<BlockWithData> blockWithDatas) {
        probabilityService.calcSimilaritiesAndProbabilitiesOfRecords(blockWithDatas);
        measurements.calculate(blockWithDatas, 0.0);
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

    private void saveFebrlResultsToCsv(FebrlContext febrlContext, double threshold) {
        File expResults = ExpFileUtils.createOutputFile("FebrlExpResults.csv");
        Map<Integer, FebrlMeasuresContext> measurments = febrlContext.getMeasurments(threshold);
        if (expResults != null) {
            parsingService.writeExperimentsMeasurements(measurments, expResults);
        } else {
            logger.warn("Failed to create file for measurements therefore no results are results will be given");
        }
    }

    private void saveConvexBPResultsToCsv(DuplicateReductionContext duplicateReductionContext) {
        File expResults = ExpFileUtils.createOutputFile("convexBPResults.csv");
        if (expResults != null) {
            logger.info("saving results of ExperimentsWithCanopy");
            parsingService.writeExperimentsMeasurements(duplicateReductionContext, expResults);
        } else {
            logger.warn("Failed to create file for measurements therefore no results are results will be given");
        }
        logger.info("Finished saving results of ExperimentsWithCanopy");
    }

}
