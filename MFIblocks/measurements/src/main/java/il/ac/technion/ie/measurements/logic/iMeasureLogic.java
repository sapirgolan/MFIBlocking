package il.ac.technion.ie.measurements.logic;

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
public interface iMeasureLogic {
    DoubleMatrix2D convertBlocksToMatrix(List<Block> blocks, CellType type);

    DoubleMatrix1D buildSimilarityVectorFromMatrix(DoubleMatrix2D matrix2D);

    double calcNonBinaryRecall(DoubleMatrix1D results, DoubleMatrix1D trueMatch) throws MatrixSizeException;

    double calcNonBinaryPrecision(DoubleMatrix1D results, DoubleMatrix1D trueMatch) throws MatrixSizeException;

    double calcBinaryRecall(final DoubleMatrix1D results, DoubleMatrix1D trueMatch, AbstractMatcher matcher) throws MatrixSizeException;

    double calcBinaryPrecision(final DoubleMatrix1D results, DoubleMatrix1D trueMatch, AbstractMatcher matcher) throws MatrixSizeException;

    /**
     * Also know as Sensitivity. This is (TP / TP + FN).
     * TP - cases with the disease correctly classified as positive.
     * FN - cases with the disease will be classified negative. (the actual value is 1 but I have thought it is 0)
     *
     * @param results
     * @param trueMatch
     * @return
     */

    double calcTruePositiveRate(final DoubleMatrix1D results, DoubleMatrix1D trueMatch);

    /**
     * Also know as  1 - specificity. This is (FP / FP + TN).
     * FP - cases without the disease will be classified as positive (cases we thought were 1 but are actually 0)
     * TN - cases without the disease will be correctly classified as negative (cases we thought were 0 and are also 0)
     *
     * @param results
     * @param trueMatch
     * @return
     */
    double calcFalsePositiveRate(final DoubleMatrix1D results, DoubleMatrix1D trueMatch);

    /**
     * The method return the Average Ranked Value score of a List of Blocks
     *
     * @param blocks
     * @return average value of "Ranked Value"
     */
    double calcRankedValue(final List<? extends AbstractBlock> blocks);

    /**
     * This method calculate the Mean Reciprocal Rank score of a List of Blocks
     *
     * @param blocks
     * @return MRR score
     */
    double calcMRR(List<? extends AbstractBlock> blocks);
}
