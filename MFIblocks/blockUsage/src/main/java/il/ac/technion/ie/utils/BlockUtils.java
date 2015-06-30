package il.ac.technion.ie.utils;

import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.model.BlockDescriptor;
import il.ac.technion.ie.search.core.SearchEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockUtils {

    public static BlockDescriptor convertBlockToDescriptor(Block block, SearchEngine searchEngine) {
        BlockDescriptor blockDescriptor = new BlockDescriptor(block);
        for (Integer blockRecordID : blockDescriptor.getMembers()) {
            List<String> recordAttributes = searchEngine.getRecordAttributes(blockRecordID.toString());
            blockDescriptor.addTextRecord(blockRecordID, recordAttributes);
        }
        return blockDescriptor;
    }

    public static List<BlockDescriptor> convertBlocksToDescriptors(List<Block> blocks, SearchEngine searchEngine) {
        Map<Block, BlockDescriptor> cache = new HashMap<>();
        List<BlockDescriptor> blockDescriptors = new ArrayList<>();
        for (Block block : blocks) {
            if (cache.containsKey(block)) {
                blockDescriptors.add(cache.get(block));
            } else {
                BlockDescriptor blockDescriptor = BlockUtils.convertBlockToDescriptor(block, searchEngine);
                cache.put(block, blockDescriptor);
                blockDescriptors.add(blockDescriptor);
            }
        }
        cache.clear();
        return blockDescriptors;

    }
}