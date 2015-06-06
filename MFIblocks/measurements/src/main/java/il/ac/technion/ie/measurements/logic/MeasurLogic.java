package il.ac.technion.ie.measurements.logic;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import il.ac.technion.ie.measurements.matchers.AbstractMatcher;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.service.BlockService;
import il.ac.technion.ie.service.iBlockService;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by XPS_Sapir on 03/06/2015.
 */
public class MeasurLogic implements iMeasureLogic {

    private static final Logger logger = Logger.getLogger(MeasurLogic.class);
    private final DoubleFactory1D sparseFactory = DoubleFactory1D.sparse;
    private iBlockService blockService = new BlockService();

    @Override
    public DoubleMatrix2D convertBlocksToMatrix(List<Block> blocks) {
        int numberOfRecords = getBlocksCardinality(blocks);
        logger.debug("There are " + numberOfRecords + " combined in all Blocks");
        SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(numberOfRecords, numberOfRecords);
        for (int i = 0; i < numberOfRecords; i++) {
            int recordId = getRecordIfFromMatrixPos(i);
            List<Block> blocksOfRecord = blockService.getBlocksOfRecord(blocks, recordId);
            for (Block block : blocksOfRecord) {
                logger.debug("Updating score of " + recordId + " in Block " + block.toString());
                this.updateScoreInMatrix(matrix, recordId, block);
            }
        }
        return matrix;
    }

    @Override
    public DoubleMatrix1D buildSimilarityVectorFromMatrix(DoubleMatrix2D matrix2D) {
        List<DoubleMatrix1D> list = new ArrayList<>();
        for (int rowIndex = 0; rowIndex < matrix2D.rows(); rowIndex++) {
            list.add(matrix2D.viewRow(rowIndex));
        }
        DoubleMatrix1D matrix1D = sparseFactory.make(list.toArray(new DoubleMatrix1D[list.size()]));
        logger.debug(String.format("bailed a Similarity Vector with %d cells", matrix1D.size()));
        return matrix1D;
    }

    @Override
    public double calcNonBinaryRecall(DoubleMatrix1D results, DoubleMatrix1D trueMatch) {
        double product = results.zDotProduct(trueMatch);
        double manhattanL1Norm = trueMatch.zSum();
        return (product / manhattanL1Norm);
    }

    @Override
    public double calcNonBinaryPrecision(DoubleMatrix1D results, DoubleMatrix1D trueMatch) {
        double product = results.zDotProduct(trueMatch);
        double manhattanL1Norm = results.zSum();
        return (product / manhattanL1Norm);
    }

    @Override
    public double calcBinaryRecall(final DoubleMatrix1D results, DoubleMatrix1D trueMatch, AbstractMatcher matcher) {
        DoubleMatrix1D matchedResults = matcher.match(results);
        logger.debug(String.format("Matched similarity vector %s and obtained %s", results, matchedResults));
        logger.info("Finished matching process by Matcher, calling this.calcNonBinaryRecall");
        return this.calcNonBinaryRecall(matchedResults, trueMatch);
    }

    @Override
    public double calcBinaryPrecision(DoubleMatrix1D results, DoubleMatrix1D trueMatch, AbstractMatcher matcher) {
        DoubleMatrix1D matchedResults = matcher.match(results);
        logger.debug(String.format("Matched similarity vector %s and obtained %s", results, matchedResults));
        logger.info("Finished matching process by Matcher, calling this.calcNonBinaryPrecision");
        return this.calcNonBinaryPrecision(matchedResults, trueMatch);
    }

    private void updateScoreInMatrix(DoubleMatrix2D matrix, int recordId, Block block) {
        float score = block.getMemberScore(recordId);
        int rawIndex = getMatrixPosFromRecordID(recordId);
        for (Integer member : block.getMembers()) {
            if (member != recordId) {
                int colIndex = getMatrixPosFromRecordID(member);
                double posScore = score + matrix.getQuick(rawIndex, colIndex);
                logger.debug(String.format("Increasing score at pos (%d,%d) from by %s to %s",
                        rawIndex, colIndex, score, posScore));
                matrix.setQuick(rawIndex, colIndex, posScore);
            }
        }
    }

    private int getBlocksCardinality(List<Block> blocks) {
        Set<Integer> set = new HashSet<>();
        for (Block block : blocks) {
            set.addAll(block.getMembers());
        }
        return set.size();
    }

    private int getRecordIfFromMatrixPos(int matrixPos) {
        return (matrixPos + 1);
    }

    private int getMatrixPosFromRecordID(int recordID) {
        return (recordID - 1);
    }


}
