package il.ac.technion.ie.potential.logic;

import com.google.common.collect.Sets;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.BlockPotential;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public class PotentialLogic implements iPotentialLogic {
    static final Logger logger = Logger.getLogger(PotentialLogic.class);

    @Override
    public List<BlockPotential> getLocalPotential(List<Block> blocks) {
        List<BlockPotential> result = new ArrayList<>(blocks.size());
        for (Block block : blocks) {
            if (block.size() > 1) {
                logger.debug("calculating local potential of Block: " + block.toString());
                result.add( new BlockPotential(block));
            }
        }
        logger.info(String.format("Calculated log local Potential in total of #%d blocks", result.size()));
        return result;
    }

    @Override
    public AdjustedMatrix calculateAdjustedMatrix(List<Block> blocks) {
        List<Block> filteredBlocks = filterBlockBySize(blocks, 2);

        //A mapping for each recordID. For each record we store a Set with all
        //blocks is appears in.
        Map<Integer, Set<Integer>> recordBlockMap = buildMapBlock(filteredBlocks);
        return buildAdjustedMatrixFromMap(recordBlockMap, filteredBlocks);
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
                                                      List<Block> filteredBlocks) {
        AdjustedMatrix adjustedMatrix = new AdjustedMatrix(filteredBlocks);
        for (Map.Entry<Integer, Set<Integer>> entry : recordBlockMap.entrySet()) {
            Set<Integer> blocksRecordAppearIn = entry.getValue();
            for (Integer outerElement : blocksRecordAppearIn) {
                for (Integer innerElement : blocksRecordAppearIn) {
                    if (!innerElement.equals(outerElement)) {
                        adjustedMatrix.setQuick(outerElement, innerElement, 1.0);
                    }
                }
            }
        }

        return adjustedMatrix;
    }

    private Map<Integer, Set<Integer>> buildMapBlock(List<Block> filteredBlocks) {
        Map<Integer, Set<Integer>> recordBlockMap = new HashMap<>();
        for (Block block : filteredBlocks) {
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

    private List<Block> filterBlockBySize(List<Block> blocks, int filterSize) {
        final List<Block> filtered = new ArrayList<>();
        for (Block block : blocks) {
            if (block.size() >= filterSize) {
                filtered.add(block);
            }
        }
        return filtered;
    }
}
