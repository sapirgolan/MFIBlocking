package il.ac.technion.ie.measurements.service;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import il.ac.technion.ie.exception.MatrixSizeException;
import il.ac.technion.ie.measurements.logic.MeasurLogic;
import il.ac.technion.ie.measurements.logic.iMeasureLogic;
import il.ac.technion.ie.measurements.matchers.AbstractMatcher;
import il.ac.technion.ie.measurements.type.CellType;
import il.ac.technion.ie.model.AbstractBlock;
import il.ac.technion.ie.model.Block;

import java.util.List;

/**
 * Created by XPS_Sapir on 03/06/2015.
 */
public class MeasurService implements iMeasurService {
    private iMeasureLogic measureLogic;

    public MeasurService() {
        measureLogic = new MeasurLogic();
    }
    @Override
    public DoubleMatrix2D buildMatrixFromBlocks(List<Block> blocks, CellType type) {
        return measureLogic.convertBlocksToMatrix(blocks, type);
    }

    @Override
    public DoubleMatrix1D buildSimilarityVector(DoubleMatrix2D similarityMatrix) {
        return measureLogic.buildSimilarityVectorFromMatrix(similarityMatrix);
    }

    @Override
    public DoubleMatrix1D buildSimilarityVector(List<Block> blocks, CellType type) {
        DoubleMatrix2D similarityMatrix = this.buildMatrixFromBlocks(blocks, type);
        return this.buildSimilarityVector(similarityMatrix);
    }

    @Override
    public double calcNonBinaryRecall(DoubleMatrix1D results, DoubleMatrix1D trueMatch) throws MatrixSizeException {
        return measureLogic.calcNonBinaryRecall(results, trueMatch);
    }

    @Override
    public double calcNonBinaryPrecision(final DoubleMatrix1D results, DoubleMatrix1D trueMatch) throws MatrixSizeException {
        return measureLogic.calcNonBinaryPrecision(results, trueMatch);
    }

    @Override
    public double calcBinaryRecall(final DoubleMatrix1D results, DoubleMatrix1D trueMatch, AbstractMatcher matcher) throws MatrixSizeException {
        return measureLogic.calcBinaryRecall(results, trueMatch, matcher);
    }

    @Override
    public double calcBinaryPrecision(DoubleMatrix1D results, DoubleMatrix1D trueMatch, AbstractMatcher matcher) throws MatrixSizeException {
        return  measureLogic.calcBinaryPrecision(results, trueMatch, matcher);
    }

    @Override
    public double calcTruePositiveRate(DoubleMatrix1D results, DoubleMatrix1D trueMatch) {
        return measureLogic.calcTruePositiveRate(results, trueMatch);
    }

    @Override
    public double calcFalsePositiveRate(DoubleMatrix1D results, DoubleMatrix1D trueMatch) {
        return measureLogic.calcFalsePositiveRate(results, trueMatch);
    }

    @Override
    public double calcRankedValue(final List<? extends AbstractBlock> blocks) {
        return measureLogic.calcRankedValue(blocks);
    }
}
