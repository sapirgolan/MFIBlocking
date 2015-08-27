package il.ac.technion.ie.measurements.service;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import il.ac.technion.ie.exception.MatrixSizeException;
import il.ac.technion.ie.measurements.matchers.AbstractMatcher;
import il.ac.technion.ie.measurements.type.CellType;
import il.ac.technion.ie.model.AbstractBlock;
import il.ac.technion.ie.model.Block;

import java.util.List;

/**
 * Created by XPS_Sapir on 03/06/2015.
 */
public interface iMeasurService {
    DoubleMatrix2D buildMatrixFromBlocks(List<Block> blocks, CellType type);

    DoubleMatrix1D buildSimilarityVector(DoubleMatrix2D similarityMatrix);

    DoubleMatrix1D buildSimilarityVector(List<Block> blocks, CellType type);

    double calcNonBinaryRecall(DoubleMatrix1D results, DoubleMatrix1D trueMatch) throws MatrixSizeException;

    double calcNonBinaryPrecision(DoubleMatrix1D results, DoubleMatrix1D trueMatch) throws MatrixSizeException;

    double calcBinaryRecall(final DoubleMatrix1D results, DoubleMatrix1D trueMatch, AbstractMatcher matcher) throws MatrixSizeException;

    double calcBinaryPrecision(final DoubleMatrix1D results, DoubleMatrix1D trueMatch, AbstractMatcher matcher) throws MatrixSizeException;

    double calcTruePositiveRate(final DoubleMatrix1D results, DoubleMatrix1D trueMatch);

    double calcFalsePositiveRate(final DoubleMatrix1D results, DoubleMatrix1D trueMatch);

    double calcRankedValue(final List<? extends AbstractBlock> blocks);

    /**
     * please refer to https://en.wikipedia.org/wiki/Mean_reciprocal_rank
     *
     * @param blocks
     * @return the Mean Reciprocal Rank score
     */
    double calcMRR(final List<? extends AbstractBlock> blocks);
}
