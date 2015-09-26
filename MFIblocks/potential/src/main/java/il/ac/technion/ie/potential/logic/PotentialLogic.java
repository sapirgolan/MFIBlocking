package il.ac.technion.ie.potential.logic;

import com.google.common.collect.Sets;
import il.ac.technion.ie.model.AbstractBlock;
import il.ac.technion.ie.potential.model.*;
import il.ac.technion.ie.potential.utils.PotentialUtil;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public class PotentialLogic implements iPotentialLogic {
    static final Logger logger = Logger.getLogger(PotentialLogic.class);

    @Override
    public List<BlockPotential> getLocalPotential(List<? extends AbstractBlock> blocks) {
        List<BlockPotential> result = new ArrayList<>(blocks.size());
        for (AbstractBlock block : blocks) {
            if (block.size() > 1) {
                logger.debug("calculating local potential of Block: " + block.toString());
                result.add( new BlockPotential(block));
            }
        }
        logger.info(String.format("Calculated log local Potential in total of #%d blocks", result.size()));
        return result;
    }

    @Override
    public AdjustedMatrix calculateAdjustedMatrix(List<? extends AbstractBlock> blocks) {
        List<AbstractBlock> filteredBlocks = filterBlockBySize(blocks, 2);

        //A mapping for each recordID. For each record we store a Set with all
        //blocks is appears in.
        logger.info("Creating a MAP between each record and the block it is in");
        Map<Integer, Set<Integer>> recordBlockMap = buildMapBlock(filteredBlocks);
        logger.info("Building Adjusted Matrix from Blocks who have more than one record");
        return buildAdjustedMatrixFromMap(recordBlockMap, filteredBlocks);
    }

    @Override
    public List<SharedMatrix> getSharedMatrices(List<? extends AbstractBlock> blocks) {
        List<MatrixContext<SharedMatrix>> context = getMatrixContexts(blocks);
        return extractMatricesFromList(context);
    }

    @Override
    public List<MatrixContext<SharedMatrix>> getSharedMatricesWithContext(List<? extends AbstractBlock> blocks) {
        return this.getMatrixContexts(blocks);
    }

    private List<MatrixContext<SharedMatrix>> getMatrixContexts(List<? extends AbstractBlock> blocks) {
        List<AbstractBlock> filteredBlocks = filterBlockBySize(blocks, 2);

        //A mapping for each recordID. For each record we store a Set with all
        //blocks is appears in.
        logger.info("Creating a MAP between each record and the block it is in");
        Map<Integer, Set<Integer>> recordIdToBlockMap = buildMapBlock(filteredBlocks);
        return buildSharedMatrices(recordIdToBlockMap, filteredBlocks);
    }

    private <M extends AbstractPotentialMatrix> List<M> extractMatricesFromList(List<MatrixContext<M>> contexts) {
        ArrayList<M> matrices = new ArrayList<>();
        for (MatrixContext<M> context : contexts) {
            matrices.add(context.getMatrix());
        }
        return matrices;
    }

    private List<MatrixContext<SharedMatrix>> buildSharedMatrices(Map<Integer, Set<Integer>> recordIdToBlockMap,
                                                                  List<? extends AbstractBlock> filteredBlocks) {
        Map<Integer, AbstractBlock> blockIdToBlockMap = blockMapping(filteredBlocks);
        Map<BlockPair<Integer, Integer>, SharedMatrix> pairOfBlocksToMatrix = new HashMap<>();

        for (Map.Entry<Integer, Set<Integer>> entry : recordIdToBlockMap.entrySet()) {
            Set<Integer> blocksRecordAppearIn = entry.getValue();
            for (Integer outerElement : blocksRecordAppearIn) {
                for (Integer innerElement : blocksRecordAppearIn) {
                    if (!innerElement.equals(outerElement)) {
                        logger.debug(String.format("Both Blocks #%d ,#%d contain record %d",
                                outerElement, innerElement, entry.getKey()));
                        BlockPair<Integer, Integer> pair = new BlockPair<>(outerElement, innerElement);
                        SharedMatrix sharedMatrix = getSharedMatix(pair, pairOfBlocksToMatrix, blockIdToBlockMap);
                        sharedMatrix.setQuick(entry.getKey(), -10);
                    }
                }
            }
        }
        return buildMatricesContexts(pairOfBlocksToMatrix);
    }

    private <M extends AbstractPotentialMatrix> List<MatrixContext<M>> buildMatricesContexts
            (Map<BlockPair<Integer, Integer>, M> pairOfBlocksToMatrix) {
        //create return collection
        List<MatrixContext<M>> matricesContexts = new ArrayList<>();

        //iterate on each entry in the MAP
        for (Map.Entry<BlockPair<Integer, Integer>, M> entry : pairOfBlocksToMatrix.entrySet()) {
            MatrixContext<M> context = new MatrixContext<>(entry.getKey(), entry.getValue());
            matricesContexts.add(context);
        }
        return matricesContexts;


    }

    private Map<Integer, AbstractBlock> blockMapping(List<? extends AbstractBlock> filteredBlocks) {
        Map<Integer, AbstractBlock> blockMap = new HashMap<>();
        for (AbstractBlock block : filteredBlocks) {
            blockMap.put(block.getId(), block);
        }
        return blockMap;
    }

    private SharedMatrix getSharedMatix(BlockPair<Integer, Integer> pair,
                                        Map<BlockPair<Integer, Integer>, SharedMatrix> map,
                                        Map<Integer, AbstractBlock> blockMap) {
        SharedMatrix matrix;
        if (map.containsKey(pair)) {
            matrix = map.get(pair);
        } else {
            AbstractBlock blockOfRows = blockMap.get(pair.getLeft());
            AbstractBlock blockOfColumns = blockMap.get(pair.getRight());
            matrix = new SharedMatrix(blockOfRows, blockOfColumns);
            map.put(pair, matrix);
            return matrix;
        }
        return matrix;
    }

    /**
     * The method is responsible of building the {@link AdjustedMatrix}.
     * An AdjustedMatrix is a matric where rows and columns represents blocks.
     * If a record appear both in blockI and blockJ then AdjustedMatrix{i,j} = 1
     * @param recordBlockMap - a mapping between records and block. It tells for each record
     *                       the blocks it is in
     * @param filteredBlocks - Blocks that have more than one record
     * @return AdjustedMatrix
     */
    private AdjustedMatrix buildAdjustedMatrixFromMap(Map<Integer, Set<Integer>> recordBlockMap,
                                                      List<? extends AbstractBlock> filteredBlocks) {
        AdjustedMatrix adjustedMatrix = new AdjustedMatrix(filteredBlocks);
        for (Map.Entry<Integer, Set<Integer>> entry : recordBlockMap.entrySet()) {
            Set<Integer> blocksRecordAppearIn = entry.getValue();
            for (Integer outerElement : blocksRecordAppearIn) {
                for (Integer innerElement : blocksRecordAppearIn) {
                    if (!innerElement.equals(outerElement)) {
                        logger.debug(String.format("Both Blocks #%d ,#%d contain record %d",
                                outerElement, innerElement, entry.getKey()));
                        adjustedMatrix.setQuick(outerElement, innerElement, 1.0);
                    }
                }
            }
        }
        logger.info(String.format("%d records share a common block", adjustedMatrix.cardinality() / 2));
        return adjustedMatrix;
    }

    private Map<Integer, Set<Integer>> buildMapBlock(List<? extends AbstractBlock> filteredBlocks) {
        Map<Integer, Set<Integer>> recordBlockMap = new HashMap<>();
        for (AbstractBlock block : filteredBlocks) {
            int blockId = block.getId();
            List<Object> blockMembers = block.getMembers();
            for (Object member : blockMembers) {
                Integer memberId = PotentialUtil.convertToId(member);
                if (recordBlockMap.containsKey(memberId)) {
                    recordBlockMap.get(memberId).add(blockId);
                } else {
                    recordBlockMap.put(memberId, Sets.newHashSet(blockId));
                }
            }
        }
        return recordBlockMap;
    }

    private List<AbstractBlock> filterBlockBySize(List<? extends AbstractBlock> blocks, int filterSize) {
        final List<AbstractBlock> filtered = new ArrayList<>();
        for (AbstractBlock block : blocks) {
            if (block.size() >= filterSize) {
                filtered.add(block);
            }
        }
        logger.info("Total of #" + filtered.size() + " were kept after filtering");
        return filtered;
    }
}
