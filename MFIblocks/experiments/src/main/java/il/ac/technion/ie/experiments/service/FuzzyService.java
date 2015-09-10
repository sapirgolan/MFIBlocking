package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.exception.NotImplementedYetException;
import il.ac.technion.ie.experiments.exception.SizeNotEqualException;
import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.experiments.model.Record;
import il.ac.technion.ie.experiments.model.RecordSplit;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by I062070 on 09/09/2015.
 */
public class FuzzyService {

    private UniformRealDistribution splitBlockProbThresh;
    static final Logger logger = Logger.getLogger(FuzzyService.class);


    public FuzzyService() {
        splitBlockProbThresh = new UniformRealDistribution();
    }


    /**
     * The method split blocks in half if the block inner threshold is lower than the given threshold.
     *
     * @param originalBlocks
     * @param threshold
     */
    public List<BlockWithData> splitBlocks(List<BlockWithData> originalBlocks, double threshold) {
        List<BlockWithData> newBlocks = new ArrayList<>(originalBlocks.size());
        for (BlockWithData origBlock : originalBlocks) {
            double splitProbability = splitBlockProbThresh.sample();
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
