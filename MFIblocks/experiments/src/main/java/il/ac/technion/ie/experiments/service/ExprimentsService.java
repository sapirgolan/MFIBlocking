package il.ac.technion.ie.experiments.service;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.ConvexBPContext;
import il.ac.technion.ie.experiments.model.UaiVariableContext;
import il.ac.technion.ie.model.Record;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

/**
 * Created by I062070 on 27/08/2015.
 */
public class ExprimentsService {

    static final Logger logger = Logger.getLogger(ExprimentsService.class);
    public static final String PARAMETER_NAME = "parameter=";
    public static final String CLOSE = ".";
    public static final int WAIT_INTERVAL_IN_SECONDS = 5;
    public static final String DCBP_DIR = "C:\\technion";

    /**
     * The method filters the blocks whose chosen representative is not the True Representative of the block.
     * It doesn't modify the input collection.
     *
     * @param blocks - A {@link java.util.Collection} of blocks
     * @return {@link java.util.Collection} of blocks. This collection shouldn't be modified
     */
    public final List<BlockWithData> filterBlocksWhoseTrueRepIsNotFirst(final Collection<BlockWithData> blocks) {
        List<BlockWithData> filteredBlocks = new ArrayList<>();
        for (BlockWithData block : blocks) {
            if (block.getTrueRepresentativePosition() != 1) {
                filteredBlocks.add(block);
            }
        }
        return filteredBlocks;
    }

    /**
     * The method return a MAP with the split probability of each block.
     * The split probability is sampled from Uniform Real Distribution {@link UniformRealDistribution} Uniform distribution~[0,1]
     *
     * @param blocks - a Collection of blocks
     * @return Map<Integer, Double>
     */
    public final Map<Integer, Double> sampleSplitProbabilityForBlocks(final Collection<BlockWithData> blocks) {
        UniformRealDistribution uniformRealDistribution = new UniformRealDistribution();
        Map<Integer, Double> splitProbMap = new HashMap<>();
        for (BlockWithData block : blocks) {
            splitProbMap.put(block.getId(), uniformRealDistribution.sample());
        }
        return splitProbMap;
    }

    public List<Double> getThresholdSorted(Collection<Double> values) {
        List<Double> thresholds = new ArrayList<>(values);
        Collections.sort(thresholds);
        thresholds.remove(0);
        return thresholds;
    }

    public ConvexBPContext createConvexBPContext(UaiVariableContext context) throws IOException {
        File uaiVariableContextFile = context.getUaiFile();
        if (uaiVariableContextFile == null || !uaiVariableContextFile.exists()) {
            throw new FileNotFoundException("uaiVariableContextFile doesn't exist in given UaiVariableContext");
        }
        try {
            FileUtils.copyFileToDirectory(uaiVariableContextFile, new File(DCBP_DIR));
        } catch (IOException e) {
            logger.error("Failed to copy uaiVariableContextFile to " + DCBP_DIR, e);
            throw e;
        }
        String uaiFileName = uaiVariableContextFile.getName();
        logger.trace("uaiFileName is: " + uaiFileName);
        String outputFileName = "Out_" + System.nanoTime() + ".txt";
        logger.trace("outputFileName for binary file is: " + outputFileName);
        logger.debug("Create ConvexBPContext with waitIntervalInSeconds of " + WAIT_INTERVAL_IN_SECONDS);
        return new ConvexBPContext(DCBP_DIR, uaiFileName, outputFileName, WAIT_INTERVAL_IN_SECONDS);
    }

    public Collection<File> findDatasets(String pathToDir, boolean recursiveSearch) {
        Collection<File> files = null;
        File sourceDir = new File(pathToDir);
        if (sourceDir.isDirectory()) {
            files = FileUtils.listFiles(sourceDir, new String[]{"csv"}, recursiveSearch);
        }
        logger.info(String.format("There are %d files under %s", files.size(), pathToDir));
        return files;
    }

    public Integer getParameterValue(File dataset) {
        Integer numericParamValue = null;
        String fileName = dataset.getName().toLowerCase();
        String paramValue = StringUtils.substringBetween(fileName, PARAMETER_NAME, CLOSE);

        try {
            numericParamValue = Integer.valueOf(paramValue);
        } catch (NumberFormatException e) {
            logger.error("Didn't find Febrl parameter value in file name");
        }
        return numericParamValue;
    }

    public double calcAvgBlockSize(List<BlockWithData> blocks) {
        double sum = 0;
        for (BlockWithData block : blocks) {
            sum += block.size();
        }
        return sum / blocks.size();
    }

    public Multimap<Record, BlockWithData> fetchRepresentatives(List<BlockWithData> blocks) {
        Multimap<Record, BlockWithData> multimap = ArrayListMultimap.create();
        for (BlockWithData block : blocks) {
            Map<Record, Float> blockRepresentatives = block.reFindBlockRepresentatives();
            for (Record record : blockRepresentatives.keySet()) {
                multimap.put(record, block);
            }
        }
        return multimap;
    }
}
