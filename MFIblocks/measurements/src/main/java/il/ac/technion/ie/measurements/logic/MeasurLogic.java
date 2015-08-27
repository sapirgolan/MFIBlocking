package il.ac.technion.ie.measurements.logic;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import il.ac.technion.ie.exception.MatrixSizeException;
import il.ac.technion.ie.measurements.matchers.AbstractMatcher;
import il.ac.technion.ie.measurements.type.CellType;
import il.ac.technion.ie.measurements.utils.MeasurUtils;
import il.ac.technion.ie.model.AbstractBlock;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.service.BlockService;
import il.ac.technion.ie.service.iBlockService;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by XPS_Sapir on 03/06/2015.
 */
public class MeasurLogic implements iMeasureLogic {

    private static final Logger logger = Logger.getLogger(MeasurLogic.class);
    private final DoubleFactory1D sparseFactory = DoubleFactory1D.sparse;
    private iBlockService blockService = new BlockService();

    @Override
    public DoubleMatrix2D convertBlocksToMatrix(List<Block> blocks, CellType type) {
        int numberOfRecords = getBlocksCardinality(blocks);
        logger.debug("There are " + numberOfRecords + " combined in all Blocks");
        SparseDoubleMatrix2D matrix = new SparseDoubleMatrix2D(numberOfRecords, numberOfRecords);
        for (int i = 0; i < numberOfRecords; i++) {
            int recordId = getRecordIfFromMatrixPos(i);
            List<Block> blocksOfRecord = blockService.getBlocksOfRecord(blocks, recordId);
            for (Block block : blocksOfRecord) {
                logger.debug("Updating score of " + recordId + " in Block " + block.toString());
                this.updateScoreInMatrix(matrix, recordId, block, type);
            }
        }
        return matrix;
    }

    @Override
    public DoubleMatrix1D buildSimilarityVectorFromMatrix(DoubleMatrix2D matrix2D) {
        List<DoubleMatrix1D> list = new ArrayList<>();
        logger.info("Before creation of matrix there were " + matrix2D.cardinality() + " non Zeros elements");
        for (int rowIndex = 0; rowIndex < matrix2D.rows(); rowIndex++) {
            list.add(matrix2D.viewRow(rowIndex));
        }
        DoubleMatrix1D matrix1D = sparseFactory.make(list.toArray(new DoubleMatrix1D[list.size()]));
        logger.info("After creation of matrix there are " + matrix1D.cardinality() + " non Zeros elements");
        logger.debug(String.format("build a Similarity Vector with %d cells", matrix1D.size()));
        return matrix1D;
    }

    @Override
    public double calcNonBinaryRecall(DoubleMatrix1D results, DoubleMatrix1D trueMatch) throws MatrixSizeException {
        verifyVectorsSize(results, trueMatch);
        double product = results.zDotProduct(trueMatch);
        double manhattanL1Norm = trueMatch.zSum();
        return (product / manhattanL1Norm);
    }

    @Override
    public double calcNonBinaryPrecision(DoubleMatrix1D results, DoubleMatrix1D trueMatch) throws MatrixSizeException {
        verifyVectorsSize(results, trueMatch);
        double product = results.zDotProduct(trueMatch);
        double manhattanL1Norm = results.zSum();
        return (product / manhattanL1Norm);
    }

    @Override
    public double calcBinaryRecall(final DoubleMatrix1D results, DoubleMatrix1D trueMatch, AbstractMatcher matcher) throws MatrixSizeException {
        DoubleMatrix1D matchedResults = matcher.match(results);
        logger.debug(String.format("Matched similarity vector %s and obtained %s", results, matchedResults));
        logger.info("Finished matching process by Matcher, calling this.calcNonBinaryRecall");
        return this.calcNonBinaryRecall(matchedResults, trueMatch);
    }

    @Override
    public double calcBinaryPrecision(DoubleMatrix1D results, DoubleMatrix1D trueMatch, AbstractMatcher matcher) throws MatrixSizeException {
        DoubleMatrix1D matchedResults = matcher.match(results);
        logger.debug(String.format("Matched similarity vector %s and obtained %s", results, matchedResults));
        logger.info("Finished matching process by Matcher, calling this.calcNonBinaryPrecision");
        return this.calcNonBinaryPrecision(matchedResults, trueMatch);
    }

    @Override
    public double calcTruePositiveRate(DoubleMatrix1D results, DoubleMatrix1D trueMatch) {
        BitSet resultsBS = MeasurUtils.convertVectorToBitSet(results);
        BitSet trueMatchBS = MeasurUtils.convertVectorToBitSet(trueMatch);
        int truePositive = calcTruePositive(resultsBS, trueMatchBS);
        int falseNegative = calcFalseNegative(resultsBS, trueMatchBS);
        return (double) truePositive / (double) (truePositive + falseNegative);
    }

    @Override
    public double calcFalsePositiveRate(DoubleMatrix1D results, DoubleMatrix1D trueMatch) {
        BitSet resultsBS = MeasurUtils.convertVectorToBitSet(results);
        BitSet trueMatchBS = MeasurUtils.convertVectorToBitSet(trueMatch);
        int falsePositive = calcFalsePositive(resultsBS, trueMatchBS);
        int trueNegative = calcTrueNegative(resultsBS, trueMatchBS, trueMatch.size());
        return (double) falsePositive / (double) (falsePositive + trueNegative);
    }

    @Override
    public <T> double calcRankedValue(List<AbstractBlock<T>> blocks) {
        double averageRakedValue = 0;
        for (AbstractBlock<T> block : blocks) {
            int numerator = block.getTrueRepresentativePosition() - 1;
            int denominator = block.size() - 1;
            averageRakedValue += (double) numerator / denominator;
        }
        return (averageRakedValue / blocks.size());
    }

    private int calcTrueNegative(BitSet results, BitSet trueMatch, int size) {
        BitSet clonedResults = (BitSet) results.clone();
        clonedResults.or(trueMatch);
        return size - clonedResults.cardinality();
    }

    private int calcFalsePositive(BitSet results, BitSet trueMatch) {
        int truePositive = this.calcTruePositive(results, trueMatch);
        return results.cardinality() - truePositive;
    }

    private int calcFalseNegative(BitSet results, BitSet trueMatch) {
        int truePositive = this.calcTruePositive(results, trueMatch);
        return trueMatch.cardinality() - truePositive;
    }

    private int calcTruePositive(BitSet results, BitSet trueMatch) {
        BitSet clonedResults = (BitSet) results.clone();
        clonedResults.and(trueMatch);
        return clonedResults.cardinality();
    }

    /**
     * The method updates the score in position (i,j)
     * There is a chance that two records share more than one block together. Therefore if it happens we cannot simply
     * override the existing value.
     *
     * @param matrix
     * @param recordId
     * @param block
     * @param type
     */
    private void updateScoreInMatrix(DoubleMatrix2D matrix, int recordId, Block block, CellType type) {
        iUpdateMatrixStrategy strategy = getRecordValueByType(type);
        double currentValue = strategy.getRecordValue(recordId, block);
        int rowIndex = getMatrixPosFromRecordID(recordId);
        for (Integer member : block.getMembers()) {
            if (member != recordId) {
                int colIndex = getMatrixPosFromRecordID(member);
                double posValue = strategy.calcCurrentCellValue(currentValue, matrix, rowIndex, colIndex);
                logger.debug(strategy.getUpdateLogMessage(rowIndex, colIndex, currentValue, posValue));
                matrix.setQuick(rowIndex, colIndex, posValue);
            }
        }
    }

    private iUpdateMatrixStrategy getRecordValueByType(CellType type) {
        iUpdateMatrixStrategy strategy;
        if (type.equals(CellType.PROBABILITY)) {
            strategy = new ProbabilityStrategy();
        } else {
            strategy = new SimilarityStrategy();
        }
        return strategy;
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

    private void verifyVectorsSize(DoubleMatrix1D results, DoubleMatrix1D trueMatch) throws MatrixSizeException {
        if (results.size() != trueMatch.size()) {
            throw new MatrixSizeException(
                    String.format("Alg results size: %d; TrueMatch Size: %d", results.size(), trueMatch.size()));
        }
    }


}
