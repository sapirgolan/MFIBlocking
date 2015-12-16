package il.ac.technion.ie.experiments.service;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.model.Record;
import il.ac.technion.ie.probability.SimilarityCalculator;
import org.apache.log4j.Logger;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;

import java.util.List;

/**
 * Created by I062070 on 26/08/2015.
 */
public class ProbabilityService {

    static final Logger logger = Logger.getLogger(ProbabilityService.class);
    private final SimilarityCalculator calculator;

    public ProbabilityService() {
        calculator = new SimilarityCalculator(new JaroWinkler());
    }

    public void calcSimilaritiesAndProbabilitiesOfRecords(List<BlockWithData> blocks) {
        for (BlockWithData block : blocks) {
            calcBlockSimilarity(calculator, block);
            calcRecordsProbabilityInBlock(block);
        }
    }

    public void calcProbabilitiesOfRecords(List<BlockWithData> blocks) {
        for (BlockWithData block : blocks) {
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
            float memberScore = block.getMemberScore(record);
            if (memberScore != 0.0f) {
                allScores += memberScore;
            } else {
                logger.warn(record + " has similarity score of 0! Perhaps you didn't calculate the similarly!?");
            }
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