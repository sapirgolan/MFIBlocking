package il.ac.technion.ie.experiments.model;

import com.google.common.collect.*;
import il.ac.technion.ie.experiments.exception.KeyNotExistException;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.model.Record;
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

    private Map<Integer, BlockWithData> blockIdToBlockMap;
    private Map<Integer, SharedMatrix> variableIdToSharedMatrixMap;

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
        blockIdToBlockMap = new HashMap<>();
        variableIdToSharedMatrixMap = new HashMap<>();

        int variableIndex = 0;
        for (BlockWithData block : blocks) {
            variableIdToBlockId.put(variableIndex, block.getId());
            variableIdToSizeMap.put(variableIndex, block.size());
            variableIdToBlocksMultimap.put(variableIndex, block.getId());
            blockIdToBlockMap.put(block.getId(), block);
            variableIndex++;
        }

        for (MatrixContext<SharedMatrix> sharedMatrixContext : matricesWithContext) {
            variableIdToSizeMap.put(variableIndex, sharedMatrixContext.getMatrix().size());
            variableIdToSharedMatrixMap.put(variableIndex, sharedMatrixContext.getMatrix());
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
        for (Integer key : variableIdToBlocksMultimap.keySet()) {
            Integer variableId = key;
            if (isVariableAblock(variableId)) {
                multimap.put(1, index);
                logger.debug(String.format("Adding (1, %d)", index));
                index++;
            } else {
                List<Integer> variableBlockIDs = getVariableBlockIDs(variableId);
                multimap.putAll(variableBlockIDs.size(), variableBlockIDs);
                logger.debug(String.format("Adding (%d, %s)", variableBlockIDs.size(), variableBlockIDs));
            }
        }
        return multimap;
    }

    /**
     * Returned a sorted {@link java.util.List - List} of variablesIDs that corresponds to a given {@code CliqueID}.
     * If the {@code CliqueID} is of a single block then the variablesID who matches that block is returned.
     * else, If the {@code CliqueID} is of a Clique, then the
     * variablesIDs who match those blocks who are part of that clique are returned.
     *
     * @param variableIdOfClique
     * @return a Sorted List of variablesIDs
     */
    private List<Integer> getVariableBlockIDs(Integer variableIdOfClique) {
        ArrayList<Integer> list = new ArrayList<>();
        NavigableSet<Integer> blocksIDs = variableIdToBlocksMultimap.get(variableIdOfClique);
        for (Integer blocksID : blocksIDs) {
            Integer variableID = variableIdToBlockId.inverse().get(blocksID);
            list.add(variableID);
        }
        Collections.sort(list);
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

    public final BiMap<Integer, Integer> getVariableIdToBlockId() {
        return variableIdToBlockId;
    }

    public List<Double> getProbsOfBlockByID(Integer blockID) throws KeyNotExistException {
        List<Double> probabilities = new ArrayList<>();
        BlockWithData blockWithData = blockIdToBlockMap.get(blockID);
        if (blockWithData == null) {
            logger.error(String.format("Tried to obtain block with ID '%d' that doesn't exist", blockID));
            throw new KeyNotExistException("Key " + blockID + " doesn't exists in blockIdToBlockMap");
        }
        List<Record> members = blockWithData.getMembers();
        for (Record record : members) {
            float recordProbability = blockWithData.getMemberProbability(record);
            probabilities.add((double) recordProbability);
        }
        return probabilities;
    }

    public int getSizeOfBlockById(Integer cliqueID) {
        Integer blockID = variableIdToBlockId.inverse().get(cliqueID);
        return variableIdToSizeMap.get(blockID);
    }

    public final List<Integer> getVariablesIdsSorted() {
        return new ArrayList<>(variableIdToSizeMap.keySet());
    }

    public int getSharedMatrixSizeByVariableId(Integer variablesId) throws SizeNotEqualException {
        int sharedMatrixSize = variableIdToSharedMatrixMap.get(variablesId).size();
        Integer variableSize = variableIdToSizeMap.get(variablesId);

        if (sharedMatrixSize != variableSize) {
            throw new SizeNotEqualException(String.format("size of variableID '%d' is: %d and not :%d",
                    variablesId, sharedMatrixSize, variableSize));
        }
        return sharedMatrixSize;
    }

    public SharedMatrix getSharedMatrixByVariableId(Integer variableId) {
        return variableIdToSharedMatrixMap.get(variableId);
    }

    public List<Integer> getVariablesIdsWithSharedMatricesSorted() {
        final Set<Integer> variablesWithSharedMatrices = variableIdToSharedMatrixMap.keySet();
        final Set<Integer> allVariables = variableIdToSizeMap.keySet();
        Set<Integer> result = new HashSet<>(allVariables);
        result.retainAll(variablesWithSharedMatrices);
        return new ArrayList<>(result);
    }
}
