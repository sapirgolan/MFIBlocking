package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.*;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.model.BlockResults;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.BlocksMapper;
import il.ac.technion.ie.experiments.model.CompareAlgorithmResults;
import il.ac.technion.ie.experiments.parsers.SerializerUtil;
import il.ac.technion.ie.experiments.service.*;
import il.ac.technion.ie.experiments.threads.ApacheExecutor;
import il.ac.technion.ie.experiments.utils.ExperimentUtils;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by I062070 on 23/10/2016.
 */
public class ProcessCanopies extends AbstractProcessor {

    private static final Logger logger = Logger.getLogger(ProcessCanopies.class);

    private ConvexBPService convexBPService;
    private BiMap<File, List<BlockWithData>> fileToCanopies;
    private Table<String, String, Set<File>> allCanopies;

    public ProcessCanopies() {
        super();
        convexBPService = new ConvexBPService();
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
                    logger.info(String.format("running experiments on canopy - '%s'. HeapSize = %s",
                            canopiesFile.getName(), ExperimentUtils.humanReadableByteCount()));
                    DuplicateReductionContext context = this.performExperimentComparison(canopiesFile, datasetFile.getName());
                    if (context != null) {
                        results.put(datasetFile.getName(), context);
                    }
                }
            }
        }
        saveResultsToFS(results);
    }

    private DuplicateReductionContext performExperimentComparison(File canopiesFile, String datasetName) {
        List<BlockWithData> blocks = fileToCanopies.get(canopiesFile);
        long start = System.nanoTime();
        logger.info("Starting to process baseline. HeapSize = " + ExperimentUtils.humanReadableByteCount());
        this.calculateBaselineResults(blocks);
        long baselineDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        PersistResult.saveBlocksToDisk(blocks, datasetName, BlocksMapper.BASELINE_PREFIX);
        logger.info("finding baseline representatives. HeapSize = " + ExperimentUtils.humanReadableByteCount());
        Multimap<Record, BlockWithData> baselineRepresentatives = this.getRepresentatives(blocks);
        logger.info("Finished processing baseline");
        ExperimentUtils.printBlocks(blocks, "Blocks and their Representatives according to Miller");
        start = System.nanoTime();
        logger.debug("Starting to run ConvexBP. HeapSize = " + ExperimentUtils.humanReadableByteCount());
        boolean continueExecution = this.executeConvexBP(blocks);
        long bcbpDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        if (continueExecution) {
            PersistResult.saveBlocksToDisk(blocks, datasetName, "bcbp");
            DuplicateReductionContext duplicateReductionContext = this.calculateMeasurements(blocks, baselineRepresentatives);
            duplicateReductionContext.setBaselineDuration(baselineDuration);
            duplicateReductionContext.setBcbpDuration(bcbpDuration);
            return duplicateReductionContext;
        } else {
            logger.fatal(String.format("Can't continue with execution of experiment for %s since execution of BCP has failed",
                    fileToCanopies.inverse().get(blocks).getName()));
        }
        return null;
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
                logger.info(String.format("Finished converting File '%s' to '%d' blocks", file.getName(), blockWithDatas.size()));
            }
        }
        return canopyFileToBlocks;
    }

    private boolean executeConvexBP(List<BlockWithData> blocks) {
        boolean convexBP = convexBPService.runConvexBP(new ApacheExecutor(), 0.0, blocks);
        if (!convexBP) {
            logger.error("Failed to run ConvexBP on canopy clusters");
            return false;
        }
        return true;
    }

}
