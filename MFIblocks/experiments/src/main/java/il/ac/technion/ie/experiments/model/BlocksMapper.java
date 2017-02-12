package il.ac.technion.ie.experiments.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by I062070 on 10/02/2017.
 */
public class BlocksMapper {
    public static final String BASELINE_PREFIX = "baseline";

    private static final int FILE_SIZE = 5;
    private Map<String, BiMap<Integer, File>> datasetPermutationToBaselineBlocks;
    private Map<String, BiMap<Integer, File>> datasetPermutationToBcbpBlocks;
    private final Pattern pattern = Pattern.compile("^\\w+_(\\d)");
    private Map<String, Deque<BlockPair>> pairDequeMap;
//    private Deque<BlockPair> pairDeque;
    private static final Logger logger = Logger.getLogger(BlocksMapper.class);

    public BlocksMapper() {
        this.datasetPermutationToBaselineBlocks = new HashMap<>();
        this.datasetPermutationToBcbpBlocks = new HashMap<>();
        pairDequeMap = new HashMap<>();
//        pairDeque = new LinkedList<>();
    }

    public void add(File blockFile) {
//        String datasetPermutationName = FilenameUtils.getBaseName(blockFile.getParentFile().getName());
        String datasetPermutationName = blockFile.getParentFile().getName();
        boolean wasInserted = addBlock(blockFile, datasetPermutationName);
        if (wasInserted) {
            createBlockPair(blockFile, datasetPermutationName);
        }
    }

    /**
     * The method creates a BlockPair if possible.
     * That is, if the given block file file was inserted to the internal collection and it already has a "complementary"
     * file in the other collection
     * @param blockFile
     * @param datasetPermutationName
     */
    private void createBlockPair(File blockFile, String datasetPermutationName) {
        String blockFileName = blockFile.getName();
        int index = getBlockIndex(blockFileName);
        Map<String, BiMap<Integer, File>> otherFileCollection = getOtherFileCollection(blockFileName);
        BiMap<Integer, File> biMap = otherFileCollection.get(datasetPermutationName);

        boolean hasComplementaryFile = biMap != null && biMap.containsKey(index);
        if (hasComplementaryFile) {
            File otherBlockFile = biMap.get(index);
            createAndInsertPair(blockFile, datasetPermutationName, otherBlockFile);
        }
    }

    private void createAndInsertPair(File blockFile, String datasetPermutationName, File otherBlock) {
        Deque<BlockPair> pairDeque = getPairDeque(datasetPermutationName);
        if (isBaselineBlock(blockFile.getName())) {
            pairDeque.addLast(new BlockPair(blockFile, otherBlock));
        } else {
            pairDeque.addLast(new BlockPair(otherBlock, blockFile));
        }
    }

    private Deque<BlockPair> getPairDeque(String datasetPermutationName) {
        Deque<BlockPair> pairDeque;
        if (pairDequeMap.containsKey(datasetPermutationName)) {
            pairDeque = pairDequeMap.get(datasetPermutationName);
        } else {
            pairDeque = new LinkedList<>();
            pairDequeMap.put(datasetPermutationName, pairDeque);
        }
        return pairDeque;
    }

    private Map<String,BiMap<Integer,File>> getFileCollection(String blockFileName) {
        boolean isBaselineBlock = isBaselineBlock(blockFileName);
        return isBaselineBlock ? datasetPermutationToBaselineBlocks : datasetPermutationToBcbpBlocks;
    }

    private Map<String,BiMap<Integer,File>> getOtherFileCollection(String blockFileName) {
        boolean isBaselineBlock = isBaselineBlock(blockFileName);
        return isBaselineBlock ? datasetPermutationToBcbpBlocks : datasetPermutationToBaselineBlocks;
    }

    private boolean addBlock(File blockFile, String datasetPermutationName) {
        Map<String, BiMap<Integer, File>> map = getFileCollection(blockFile.getName());
        int index = getBlockIndex(blockFile.getName());
        if (index == -1 || index >= FILE_SIZE) {
            logger.warn(blockFile + " has an invalid index");
            return false;
        }
        boolean mappingExists = map.containsKey(datasetPermutationName);
        if (mappingExists) {
            map.get(datasetPermutationName).put(index, blockFile);
        } else {
            BiMap<Integer, File> biMap = HashBiMap.create(FILE_SIZE);
            biMap.put(index, blockFile);
            map.put(datasetPermutationName, biMap);
        }
        return true;
    }

    private int getBlockIndex(String blockFileName) {
        Matcher matcher = pattern.matcher(blockFileName);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    private boolean isBaselineBlock(String blockFileName) {
        return blockFileName.contains(BASELINE_PREFIX);
    }

    public BlockPair getNext(String datasetPermutationName) {
        Deque<BlockPair> pairDeque = pairDequeMap.get(datasetPermutationName);
        if (pairDeque == null || pairDeque.isEmpty()) {
            return null;
        }
        return pairDeque.removeFirst();
    }

}
