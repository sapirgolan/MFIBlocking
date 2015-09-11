package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.lprobability.SimilarityCalculator;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

import java.util.List;

/**
 * Created by I062070 on 26/08/2015.
 */
public class ProbabilityService {

    public void calcProbabilitiesOfRecords(List<BlockWithData> blocks) {
        SimilarityCalculator similarityCalculator = new SimilarityCalculator(new JaroWinkler());
        //iterate on each block
        for (BlockWithData block : blocks) {
            calcBlockSimilarity(similarityCalculator, block);
            calcRecordsProbabilityInBlock(block);
        }
    }

    private void calcBlockSimilarity(SimilarityCalculator similarityCalculator, BlockWithData block) {
        List<Record> blockRecords = block.getMembers();
        //retrieve block Text attributes
        for (Record currentRecord : blockRecords) {

            float currentRecordSimilarity = calcRecordSimilarityInBlock(currentRecord, blockRecords, similarityCalculator);
            block.setMemberSimScore(currentRecord, currentRecordSimilarity);
        }
    }

    private void calcRecordsProbabilityInBlock(BlockWithData block) {
        float allScores = 0;
        for (Record record : block.getMembers()) {
            allScores += block.getMemberScore(record);
        }

        for (Record record : block.getMembers()) {
            block.setMemberProbability(record, block.getMemberScore(record) / allScores);
        }
    }

    /**
     * The method sumup the similarity of a record (currentRecordId) to all other records in a block.
     * It doesn't calculate the similarity of a record to itself.
     *
     * @param currentRecord        - the record whose similarity with other we want to measure.
     * @param blockRecords         - All records in the block
     * @param similarityCalculator - a calculator to calc similarity by.
     * @return
     */
    private float calcRecordSimilarityInBlock(Record currentRecord, final List<Record> blockRecords,
                                              SimilarityCalculator similarityCalculator) {
        float recordsSim = 0;
        //case block contains a single record
        if (blockRecords.size() == 1) {
            return 1;
        }
        for (Record next : blockRecords) {
            if (next != currentRecord) {
                recordsSim += similarityCalculator.calcRecordsSim(next.getEntries(), currentRecord.getEntries());
            }
        }
        return recordsSim;
    }
}