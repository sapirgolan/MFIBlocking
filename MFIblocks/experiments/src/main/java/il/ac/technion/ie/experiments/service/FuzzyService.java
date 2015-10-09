package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.exception.NotImplementedYetException;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.RecordSplit;
import il.ac.technion.ie.model.Record;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by I062070 on 09/09/2015.
 */
public class FuzzyService {

    static final Logger logger = Logger.getLogger(FuzzyService.class);

    public FuzzyService() {
    }

    /**
     * The method split blocks in half if the block inner threshold is lower than the given threshold.
     * For each block it obtain a split probability .
     * If the split probability is smaller than the threshold {@code sampledProbability < threshold }then the block is split into two blocks.
     * The records of the block are randomly placed into the new blocks.
     * The True representative of the original block is placed in the two new blocks.
     *
     * @param originalBlocks
     * @param splitProbs
     * @param threshold
     */
    public List<BlockWithData> splitBlocks(List<BlockWithData> originalBlocks, Map<Integer, Double> splitProbs, double threshold) throws SizeNotEqualException {
        assertSize(originalBlocks, splitProbs);
        List<BlockWithData> newBlocks = new ArrayList<>(originalBlocks.size());
        for (BlockWithData origBlock : originalBlocks) {
            Double splitProbability = getSplitProbability(splitProbs, origBlock);
            if (splitProbability < threshold) {
                List<Record> blockOneRecords = new ArrayList<>(originalBlocks.size() / 2);
                List<Record> blockTwoRecords = new ArrayList<>(originalBlocks.size() / 2);
                collectRecordsForSplitedBlocks(origBlock, blockOneRecords, blockTwoRecords);
                newBlocks.add(new BlockWithData(blockOneRecords));
                newBlocks.add(new BlockWithData(blockTwoRecords));
            } else {
                newBlocks.add(origBlock);
            }
        }
        return newBlocks;
    }

    private Double getSplitProbability(Map<Integer, Double> splitProbs, BlockWithData origBlock) {
        Double splitProbability = splitProbs.get(origBlock.getId());
        if (splitProbability == null) {
            logger.info("Block with ID #" + origBlock.getId() + " has no split probability");
            splitProbability = 1.0;
        }
        return splitProbability;
    }

    private void assertSize(List<BlockWithData> originalBlocks, Map<Integer, Double> splitProbs) throws SizeNotEqualException {
        int splitSize = splitProbs.size();
        int blocksSize = originalBlocks.size();
        if (blocksSize != splitSize) {
            throw new SizeNotEqualException(String.format("Size of blocks (%d) and their split probability (%d) not equal",
                    blocksSize, splitSize));
        }
    }

    public void splitRecords(List<BlockWithData> blocks) {
        try {
            addProbabilityToRecords(blocks);
        } catch (SizeNotEqualException e) {
            logger.error("failed to add split probability to records", e);
        }
        throw new NotImplementedYetException("At this stage we do not support splitting of records from blocks");
    }

    private void addProbabilityToRecords(List<BlockWithData> blocks) throws SizeNotEqualException {
        for (BlockWithData block : blocks) {
            List<RecordSplit> newRecords = new ArrayList<>(block.getMembers().size());
            for (Record record : block.getMembers()) {
                RecordSplit recordWithSplitProbability = new RecordSplit(record);
                newRecords.add(recordWithSplitProbability);
            }
            block.replaceMembers(newRecords);
        }
    }

    private void collectRecordsForSplitedBlocks(BlockWithData origBlock, List<Record> blockOneRecords, List<Record> blockTwoRecords) {
        for (int i = 0; i < origBlock.size(); i++) {
            Record record = origBlock.getMembers().get(i);
            if (!origBlock.isRepresentative(record)) {
                if (i % 2 == 0) {
                    blockOneRecords.add(record);
                } else {
                    blockTwoRecords.add(record);
                }
            }
        }
        blockOneRecords.add(origBlock.getTrueRepresentative());
        blockTwoRecords.add(origBlock.getTrueRepresentative());
    }
}
