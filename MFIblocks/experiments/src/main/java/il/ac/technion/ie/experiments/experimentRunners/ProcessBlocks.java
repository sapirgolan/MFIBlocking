package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.canopy.model.DuplicateReductionContext;
import il.ac.technion.ie.experiments.model.BlockPair;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.BlocksMapper;
import il.ac.technion.ie.experiments.parsers.SerializerUtil;
import il.ac.technion.ie.experiments.utils.ExperimentUtils;
import il.ac.technion.ie.model.Record;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by I062070 on 10/02/2017.
 */
public class ProcessBlocks extends AbstractProcessor{
    private static final Logger logger = Logger.getLogger(ProcessBlocks.class);

    public void runExperiments(String pathToBlocksFolder, String pathToOriginalDatasetFile) {
        Multimap<String, DuplicateReductionContext> results = ArrayListMultimap.create();
        BlocksMapper blocksMapper =  new FilesReader(pathToBlocksFolder).getAllBlocks();
        Collection<File> allDatasetPermutations = new FilesReader(pathToOriginalDatasetFile).getAllDatasets();

        for (File datasetPermutation : allDatasetPermutations) {
            logger.info(String.format("running experiments on 'dataset' - %s ", datasetPermutation.getName()));
            String blockMapperKey = DatasetMapper.getBlockMapperKey(datasetPermutation);
            BlockPair blockPair;
            while ((blockPair = blocksMapper.getNext(blockMapperKey)) !=null ) {
                logger.info(String.format("running experiment on %s", blockPair.getName()));
                logger.info("Before parsing dataset. HeapSize = " + ExperimentUtils.humanReadableByteCount());
                this.initMembersThatDependsOnOriginalDataset(datasetPermutation, datasetPermutation.getName());

                logger.info("Before deSerialize baseline blocks. HeapSize = " + ExperimentUtils.humanReadableByteCount());
                List<BlockWithData> baselineBlocks = new ArrayList<>(SerializerUtil.<BlockWithData>deSerialize(blockPair.getBaseline()));

                logger.info("Before finding baseline representatives. HeapSize = " + ExperimentUtils.humanReadableByteCount());
                Multimap<Record, BlockWithData> baselineRepresentatives = this.getRepresentatives(baselineBlocks);

                logger.info("Before deSerialize bcbp blocks. HeapSize = " + ExperimentUtils.humanReadableByteCount());
                List<BlockWithData> bcbpBlocks = new ArrayList<>(SerializerUtil.<BlockWithData>deSerialize(blockPair.getBcbp()));

                logger.info("Before calculating measurements. HeapSize = " + ExperimentUtils.humanReadableByteCount());
                DuplicateReductionContext reductionContext = this.calculateMeasurements(bcbpBlocks, baselineRepresentatives);

                String baseName = FilenameUtils.getBaseName(datasetPermutation.getName());
                results.put(baseName, reductionContext);
            }
        }
        saveResultsToFS(results);
    }
}
