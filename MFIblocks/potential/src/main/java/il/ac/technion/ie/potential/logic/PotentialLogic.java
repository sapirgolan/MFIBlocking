package il.ac.technion.ie.potential.logic;

import com.google.common.collect.Sets;
import il.ac.technion.ie.model.AbstractBlock;
import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.BlockPair;
import il.ac.technion.ie.potential.model.BlockPotential;
import il.ac.technion.ie.potential.model.SharedMatrix;
import org.apache.commons.lang3.tuple.Pair;
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
        List<AbstractBlock> filteredBlocks = filterBlockBySize(blocks, 2);

        //A mapping for each recordID. For each record we store a Set with all
        //blocks is appears in.
        logger.info("Creating a MAP between each record and the block it is in");
        Map<Integer, Set<Integer>> recordBlockMap = buildMapBlock(filteredBlocks);
        return buildSharedMatrices(recordBlockMap, filteredBlocks);
    }

    private List<SharedMatrix> buildSharedMatrices(Map<Integer, Set<Integer>> recordBlockMap,
                                                   List<? extends AbstractBlock> filteredBlocks) {
        Map<Integer, AbstractBlock> blockMap = blockMapping(filteredBlocks);
        Map<Pair<Integer, Integer>, SharedMatrix> map = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : recordBlockMap.entrySet()) {
            Set<Integer> blocksRecordAppearIn = entry.getValue();
            for (Integer outerElement : blocksRecordAppearIn) {
                for (Integer innerElement : blocksRecordAppearIn) {
                    if (!innerElement.equals(outerElement)) {
                        logger.debug(String.format("Both Blocks #%d ,#%d contain record %d",
                                outerElement, innerElement, entry.getKey()));
                        BlockPair<Integer, Integer> pair = new BlockPair<>(outerElement, innerElement);
                        SharedMatrix sharedMatrix = getSharedMatix(pair, map, blockMap);
                        sharedMatrix.setQuick(entry.getKey(), -10);
                    }
                }
            }
        }
        return new ArrayList<>(map.values());
    }

    private Map<Integer, AbstractBlock> blockMapping(List<? extends AbstractBlock> filteredBlocks) {
        Map<Integer, AbstractBlock> blockMap = new HashMap<>();
        for (AbstractBlock block : filteredBlocks) {
            blockMap.put(block.getId(), block);
        }
        return blockMap;
    }

    private SharedMatrix getSharedMatix(BlockPair<Integer, Integer> pair,
                                        Map<Pair<Integer, Integer>, SharedMatrix> map,
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
            List<Integer> blockMembers = block.getMembers();
            for (Integer memberId : blockMembers) {
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
