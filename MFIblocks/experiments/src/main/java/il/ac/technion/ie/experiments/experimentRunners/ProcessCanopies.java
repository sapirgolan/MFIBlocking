package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.*;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.parsers.SerializerUtil;
import il.ac.technion.ie.experiments.service.*;
import il.ac.technion.ie.experiments.threads.CommandExacter;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 * Created by I062070 on 23/10/2016.
 */
public class ProcessCanopies {

    private static final Logger logger = Logger.getLogger(ProcessCanopies.class);

    private CanopyService canopyService;
    private ConvexBPService convexBPService;
    private ProbabilityService probabilityService;
    private BiMap<File, List<BlockWithData>> fileToCanopies;
    private Table<String, String, Set<File>> allCanopies;
    private ExprimentsService exprimentsService;
    private IMeasurements measurements;
    private BiMap<Record, BlockWithData> trueRepsMap;

    public ProcessCanopies() {
        canopyService = new CanopyService();
        convexBPService = new ConvexBPService();
        probabilityService = new ProbabilityService();
        exprimentsService = new ExprimentsService();
    }

    public void runExperiments(String pathToCanopiesFolder, String pathToOriginalDatasetFile) {
        this.readAndInitCanopiesFromDir(pathToCanopiesFolder);
        Collection<File> allDatasetPermutations = new FilesReader(pathToOriginalDatasetFile).getAllDatasets();
        Multimap<String, DuplicateReductionContext> results = ArrayListMultimap.create();
        //for each dataset
        //for each permutation
        //for random generation of canopies
        /*In order to avoid performance penalty of extracting each dataset over & over
        * and not to save each extraction --> the iteration should be done by each datase*/
        for (String datasetStr_rowKey : allCanopies.rowKeySet()) {
            logger.info(String.format("running experiments on 'dataset' - %s ", datasetStr_rowKey));
            Map<String, Set<File>> permutationsToCanopies = allCanopies.row(datasetStr_rowKey);
            for (String permutationStr : permutationsToCanopies.keySet()) {
                logger.info(String.format("running experiments on permutation - '%s'", permutationStr));
                Set<File> canopiesFiles = permutationsToCanopies.get(permutationStr);
                File datasetFile = DatasetMapper.getDatasetFile(permutationStr, allDatasetPermutations);
                this.initMembersThatDependsOnOriginalDataset(datasetFile, permutationStr);
                for (File canopiesFile : canopiesFiles) {
                    logger.info(String.format("running experiments on canopy - '%s'", canopiesFile.getName()));
                    DuplicateReductionContext context = this.performExperimentComparison(canopiesFile);
                    results.put(datasetFile.getName(), context);
                }
            }
        }
        saveResultsToFS(results);
    }

    private void saveResultsToFS(Multimap<String, DuplicateReductionContext> results) {
        PersistResult.saveConvexBPResultsToCsv(results);
    }

    private DuplicateReductionContext performExperimentComparison(File canopiesFile) {
        List<BlockWithData> blocks = fileToCanopies.get(canopiesFile);
        this.calculateBaselineResults(blocks);
        Multimap<Record, BlockWithData> baselineRepresentatives = this.getRepresentatives(blocks);
        boolean continueExecution = this.executeConvexBP(blocks);
        if (continueExecution) {
            return this.calculateMeasurements(blocks, baselineRepresentatives);
        } else {
            logger.fatal(String.format("Can't continue with execution of experiment for %s since execution of BCP has failed",
                    fileToCanopies.inverse().get(blocks).getName()));
        }
        return null;
    }

    private void initMembersThatDependsOnOriginalDataset(File datasetFile, String permutationStr) {
        ParsingService parsingService = new ParsingService();
        if (datasetFile == null) {
            logger.error(String.format("no dataset exists for permutation %s", permutationStr));
            return;
        }
        List<BlockWithData> cleanBlocks = parsingService.parseDataset(datasetFile.getAbsolutePath());
        this.trueRepsMap = canopyService.getAllTrueRepresentatives(cleanBlocks);
        this.measurements = new Measurements(cleanBlocks.size());
    }

    private DuplicateReductionContext calculateMeasurements(List<BlockWithData> blocks, Multimap<Record, BlockWithData> baselineRepresentatives) {
        Multimap<Record, BlockWithData> bcbpRepresentatives = this.getRepresentatives(blocks);
        DuplicateReductionContext resultContext = measurements.representativesDuplicateElimination(
                baselineRepresentatives, bcbpRepresentatives);
        measurements.representationDiff(trueRepsMap.keySet(), bcbpRepresentatives.keySet(), resultContext);
        measurements.calcPowerOfRep(trueRepsMap, bcbpRepresentatives, resultContext);
        measurements.calcWisdomCrowds(trueRepsMap.values(), new HashSet<>(bcbpRepresentatives.values()), resultContext);
        measurements.calcAverageBlockSize(blocks, resultContext);
        double dupsRealRepresentatives = measurements.duplicatesRealRepresentatives(baselineRepresentatives, bcbpRepresentatives, trueRepsMap);

        resultContext.setNumberOfDirtyBlocks(blocks.size());
        resultContext.setDuplicatesRealRepresentatives(dupsRealRepresentatives);

        return resultContext;
    }

    private void readAndInitCanopiesFromDir(String dirPath) {
        FilesReader filesReader = new FilesReader(dirPath);
        allCanopies = filesReader.getAllCanopies();
        fileToCanopies = mapCanopyFileToBlocks();
    }

    private HashBiMap<File, List<BlockWithData>> mapCanopyFileToBlocks() {
        HashBiMap<File, List<BlockWithData>> canopyFileToBlocks = HashBiMap.create(allCanopies.size());
        //each combination of (parameterUnderTest + ParamValue) has several seized canopies
        for (Set<File> files : allCanopies.values()) {
            for (File file : files) {
                Collection<CanopyCluster> canopyClusters = SerializerUtil.deSerializeCanopies(file);
                List<BlockWithData> blockWithDatas = canopyService.convertCanopiesToBlocks(canopyClusters);
                canopyFileToBlocks.put(file, blockWithDatas);
                logger.info(String.format("Finished converting File '%s' to '%d' blocks", file.getName(), blockWithDatas.size()) );
            }
        }
        return canopyFileToBlocks;
    }

    private void calculateBaselineResults(List<BlockWithData> blocks) {
        probabilityService.calcSimilaritiesAndProbabilitiesOfRecords(blocks);

    }

    private boolean executeConvexBP(List<BlockWithData> blocks) {
        boolean convexBP = convexBPService.runConvexBP(new CommandExacter(), 0.0, blocks);
        if (!convexBP) {
            logger.error("Failed to run ConvexBP on canopy clusters");
            return false;
        }
        return true;
    }

    private Multimap<Record, BlockWithData> getRepresentatives(List<BlockWithData> blocks) {
        return exprimentsService.fetchRepresentatives(blocks);
    }
}
