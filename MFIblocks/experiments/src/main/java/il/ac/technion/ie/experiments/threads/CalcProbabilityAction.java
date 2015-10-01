package il.ac.technion.ie.experiments.threads;

import il.ac.technion.ie.experiments.model.BlockWithData;
import il.ac.technion.ie.lprobability.SimilarityCalculator;
import il.ac.technion.ie.model.Record;

import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * Created by I062070 on 01/10/2015.
 */
public class CalcProbabilityAction extends ForkJoinTask<BlockWithData> {

    private BlockWithData blockWithData;
    private SimilarityCalculator similarityCalculator;

    public CalcProbabilityAction(BlockWithData blockWithData, SimilarityCalculator similarityCalculator) {
        this.blockWithData = blockWithData;
        this.similarityCalculator = similarityCalculator;
    }

    /**
     * Returns the result that would be returned by {@link #join}, even
     * if this task completed abnormally, or {@code null} if this task
     * is not known to have been completed.  This method is designed
     * to aid debugging, as well as to support extensions. Its use in
     * any other context is discouraged.
     *
     * @return the result, or {@code null} if not completed
     */
    @Override
    public BlockWithData getRawResult() {
        return this.blockWithData;
    }

    /**
     * Forces the given value to be returned as a result.  This method
     * is designed to support extensions, and should not in general be
     * called otherwise.
     *
     * @param value the value
     */
    @Override
    protected void setRawResult(BlockWithData value) {
        this.blockWithData = value;
    }

    /**
     * Immediately performs the base action of this task.  This method
     * is designed to support extensions, and should not in general be
     * called otherwise. The return value controls whether this task
     * is considered to be done normally. It may return false in
     * asynchronous actions that require explicit invocations of
     * {@link #complete} to become joinable. It may also throw an
     * (unchecked) exception to indicate abnormal exit.
     *
     * @return {@code true} if completed normally
     */
    @Override
    protected boolean exec() {
        calcBlockSimilarity(similarityCalculator, blockWithData);
        calcRecordsProbabilityInBlock(blockWithData);
        return true;
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
