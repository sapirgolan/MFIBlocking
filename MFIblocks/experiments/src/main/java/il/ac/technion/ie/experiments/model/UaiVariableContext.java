package il.ac.technion.ie.experiments.model;

import com.google.common.collect.*;
import il.ac.technion.ie.potential.model.BlockPair;
import il.ac.technion.ie.potential.model.MatrixContext;
import il.ac.technion.ie.potential.model.SharedMatrix;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by I062070 on 19/09/2015.
 */
public class UaiVariableContext {

    private static final Logger logger = Logger.getLogger(UaiVariableContext.class);

    private List<BlockWithData> blocks;
    private List<MatrixContext<SharedMatrix>> matricesWithContext;

    private TreeMultimap<Integer, Integer> variableIdToBlocksMultimap;
    private TreeMap<Integer, Integer> variableIdToSizeMap;
    private BiMap<Integer, Integer> variableIdToBlockId;

    private UaiVariableContext(List<BlockWithData> blocks, List<MatrixContext<SharedMatrix>> matricesWithContext) {
        this.blocks = blocks;
        this.matricesWithContext = matricesWithContext;
    }

    public static UaiVariableContext createUaiVariableContext(List<BlockWithData> blocks, List<MatrixContext<SharedMatrix>> matricesWithContext) {
        UaiVariableContext context = new UaiVariableContext(blocks, matricesWithContext);
        context.init();
        return context;
    }

    private void init() {
        variableIdToSizeMap = new TreeMap<>();
        variableIdToBlocksMultimap = TreeMultimap.create();
        variableIdToBlockId = HashBiMap.create(blocks.size());

        int variableIndex = 0;
        for (BlockWithData block : blocks) {
            variableIdToBlockId.put(variableIndex, block.getId());
            variableIdToSizeMap.put(variableIndex, block.size());
            variableIdToBlocksMultimap.put(variableIndex, block.getId());
            variableIndex++;
        }

        for (MatrixContext<SharedMatrix> sharedMatrixContext : matricesWithContext) {
            variableIdToSizeMap.put(variableIndex, sharedMatrixContext.getMatrix().size());
            BlockPair<Integer, Integer> pair = sharedMatrixContext.getPair();
            variableIdToBlocksMultimap.putAll(variableIndex, Lists.newArrayList(pair.getLeft(), pair.getRight()));
            variableIndex++;
        }
        logger.debug("There are total of '" + (variableIndex + 1) + "' variables");
    }

    public List<Integer> getSizeOfVariables() {
        List<Integer> sizes = new ArrayList<>(variableIdToSizeMap.size());
        for (Map.Entry<Integer, Integer> entry : variableIdToSizeMap.entrySet()) {
            sizes.add(entry.getValue());
        }
        return sizes;
    }

    public Multimap<Integer, Integer> getSizeAndIndexOfVariables() {
        int expectedSize = variableIdToBlocksMultimap.asMap().size();
        Multimap<Integer, Integer> multimap = ArrayListMultimap.create(expectedSize, expectedSize);
        int index = 0;
        logger.info("Adding #variables in blocks\\clique AND the variableID of that blocks\\clique");
        for (Map.Entry<Integer, Integer> entry : variableIdToBlocksMultimap.entries()) {
            Integer variableId = entry.getKey();
            if (isVariableAblock(variableId)) {
                multimap.put(1, index);
                logger.debug(String.format("Adding (1, %d)", index));
            } else {
                List<Integer> variableBlockIDs = getVariableBlockIDs(variableId);
                multimap.putAll(variableBlockIDs.size(), variableBlockIDs);
                logger.debug(String.format("Adding (%d, %d)", variableBlockIDs.size(), variableBlockIDs));
            }
        }
        return multimap;
    }

    private List<Integer> getVariableBlockIDs(Integer variableIdOfClique) {
        ArrayList<Integer> list = new ArrayList<>();
        NavigableSet<Integer> blocksIDs = variableIdToBlocksMultimap.get(variableIdOfClique);
        for (Integer blocksID : blocksIDs) {
            Integer variableID = variableIdToBlockId.inverse().get(blocksID);
            list.add(variableID);
        }
        return list;
    }

    /**
     * Return {@code True} if variableId represents a Block. Otherwise variableId
     * represents a Clique and return {@code False}
     *
     * @param variableId an ID that corresponds to a variable
     * @return
     */
    private boolean isVariableAblock(Integer variableId) {
        return variableIdToBlocksMultimap.get(variableId).size() == 1;
    }
}
