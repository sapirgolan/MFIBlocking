package il.ac.technion.ie.measurements.logic;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import il.ac.technion.ie.exception.MatrixSizeException;
import il.ac.technion.ie.measurements.matchers.AbstractMatcher;
import il.ac.technion.ie.model.Block;

import java.util.List;

/**
 * Created by XPS_Sapir on 03/06/2015.
 */
public interface iMeasureLogic {
    DoubleMatrix2D convertBlocksToMatrix(List<Block> blocks);

    DoubleMatrix1D buildSimilarityVectorFromMatrix(DoubleMatrix2D matrix2D);

    double calcNonBinaryRecall(DoubleMatrix1D results, DoubleMatrix1D trueMatch) throws MatrixSizeException;

    double calcNonBinaryPrecision(DoubleMatrix1D results, DoubleMatrix1D trueMatch) throws MatrixSizeException;

    double calcBinaryRecall(final DoubleMatrix1D results, DoubleMatrix1D trueMatch, AbstractMatcher matcher) throws MatrixSizeException;

    double calcBinaryPrecision(final DoubleMatrix1D results, DoubleMatrix1D trueMatch, AbstractMatcher matcher) throws MatrixSizeException;


}
