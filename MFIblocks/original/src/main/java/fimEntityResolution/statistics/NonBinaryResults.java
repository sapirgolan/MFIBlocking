package fimEntityResolution.statistics;

import cern.colt.matrix.DoubleMatrix1D;
import il.ac.technion.ie.exception.MatrixSizeException;
import il.ac.technion.ie.measurements.matchers.MaxMatcher;
import il.ac.technion.ie.measurements.service.MeasurService;
import il.ac.technion.ie.measurements.service.iMeasurService;
import il.ac.technion.ie.model.Block;
import org.apache.log4j.Logger;

import java.util.List;

/**
* Created by I062070 on 07/06/2015.
*/
public class NonBinaryResults {
    private static final Logger logger = Logger.getLogger(NonBinaryResults.class);
    private List<Block> algorithmBlocks;
    private List<Block> trueBlocks;
    private double nonBinaryPrecision;
    private double nonBinaryRecall;
    private double binaryPrecision;
    private double binaryRecall;


    public NonBinaryResults(List<Block> algorithmBlocks, List<Block> trueBlocks) {
        this.algorithmBlocks = algorithmBlocks;
        this.trueBlocks = trueBlocks;

        this.invoke();
    }

    private void invoke() {
        iMeasurService measurService = new MeasurService();
        DoubleMatrix1D algSimilarityVector = measurService.buildSimilarityVector(algorithmBlocks);
        DoubleMatrix1D trueSimilarityVector = measurService.buildSimilarityVector(trueBlocks);
        try {
            nonBinaryPrecision = measurService.calcNonBinaryPrecision(algSimilarityVector, trueSimilarityVector);
            nonBinaryRecall = measurService.calcNonBinaryRecall(algSimilarityVector, trueSimilarityVector);

            binaryPrecision = measurService.calcBinaryPrecision(algSimilarityVector, trueSimilarityVector, new MaxMatcher());
            binaryRecall = measurService.calcBinaryRecall(algSimilarityVector, trueSimilarityVector, new MaxMatcher());
        } catch (MatrixSizeException e) {
            logger.error("Failed to perform Precision\\Recall calculations on blocks", e);
            nonBinaryPrecision = nonBinaryRecall = binaryPrecision = binaryRecall = 0;
        }
    }

    public double getBinaryPrecision() {
        return binaryPrecision;
    }

    public double getBinaryRecall() {
        return binaryRecall;
    }

    public double getNonBinaryPrecision() {
        return nonBinaryPrecision;
    }

    public double getNonBinaryRecall() {
        return nonBinaryRecall;
    }
}
