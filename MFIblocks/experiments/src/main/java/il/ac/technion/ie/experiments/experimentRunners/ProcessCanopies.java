package il.ac.technion.ie.experiments.experimentRunners;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import il.ac.technion.ie.canopy.model.CanopyCluster;
import il.ac.technion.ie.experiments.exception.NoValueExistsException;
import il.ac.technion.ie.experiments.exception.OSNotSupportedException;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.parsers.SerializerUtil;
import il.ac.technion.ie.experiments.service.CanopyService;
import il.ac.technion.ie.experiments.service.ExprimentsService;
import il.ac.technion.ie.experiments.service.ProbabilityService;
import il.ac.technion.ie.experiments.threads.CommandExacter;
import il.ac.technion.ie.model.Record;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by I062070 on 23/10/2016.
 */
public class ProcessCanopies extends AbstractExperiment {

    private static final Logger logger = Logger.getLogger(ProcessCanopies.class);

    private CanopyService canopyService;
    private ProbabilityService probabilityService;
    private BiMap<File, List<BlockWithData>> fileToCanopies;
    private Table<String, String, Set<File>> allCanopies;
    private ExprimentsService exprimentsService;

    public ProcessCanopies() {
        canopyService = new CanopyService();
        probabilityService = new ProbabilityService();
        exprimentsService = new ExprimentsService();
    }

    @Override
    public void runExperiments(String pathToDatasetFile) {
        String pathToDirFolder = pathToDatasetFile;
        this.readAndParseCanopiesFromDir(pathToDirFolder);
        //for each dataset
        //for each permutation
        //for random generation of canopies
        for (List<BlockWithData> blocks : fileToCanopies.values()) {
            this.calculateBaselineResults(blocks);
            this.getBaselineRepresentatives(blocks);
            this.executeConvexBP(blocks);
        }
        this.getBcbpRepresentatives(null);
        this.calculateMeasurements(null);

    }

    private void readAndParseCanopiesFromDir(String dirPath) {
        FilesReader filesReader = new FilesReader(dirPath);
        allCanopies = filesReader.getAllCanopies();
        fileToCanopies = HashBiMap.create(allCanopies.size());
        //each combination of (parameterUnderTest + ParamValue) has several seized canopies
        for (Set<File> files : allCanopies.values()) {
            for (File file : files) {
                Collection<CanopyCluster> canopyClusters = SerializerUtil.deSerializeCanopies(file);
                List<BlockWithData> blockWithDatas = canopyService.convertCanopiesToBlocks(canopyClusters);
                fileToCanopies.put(file, blockWithDatas);
                logger.info(String.format("Finished converting File '%s' to '%d' blocks", file.getName(), blockWithDatas.size()) );
            }
        }
    }

    private void calculateBaselineResults(List<BlockWithData> blocks) {
        probabilityService.calcSimilaritiesAndProbabilitiesOfRecords(blocks);

    }
    private Multimap<Record, BlockWithData> getBaselineRepresentatives(List<BlockWithData> blocks) {
        return exprimentsService.fetchRepresentatives(blocks);
    }

    private boolean executeConvexBP(List<BlockWithData> blocks) {
        boolean convexBP = super.runConvexBP(new CommandExacter(), 0.0, blocks);
        if (!convexBP) {
            logger.error("Failed to run ConvexBP on canopy clusters");
            return false;
        }
        return true;
    }

    private Multimap<Record, BlockWithData> getBcbpRepresentatives(List<BlockWithData> blocks) {
        return null;
    }

    private void calculateMeasurements(List<BlockWithData> blocks) {

    }
}
