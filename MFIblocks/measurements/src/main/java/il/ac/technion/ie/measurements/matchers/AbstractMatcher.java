package il.ac.technion.ie.measurements.matchers;

import cern.colt.matrix.DoubleMatrix1D;
import il.ac.technion.ie.exception.MatrixSizeException;
import org.apache.log4j.Logger;

/**
 * Created by XPS_Sapir on 04/06/2015.
 */
public abstract class AbstractMatcher {

    private static final Logger logger = Logger.getLogger(AbstractMatcher.class);

    /**
     * The matcher is responsible to choose the matching score of the two records that are the most likely to fit.
     * given a vector with scores {0.2, 0, 0.7} that matcher will set all entries to 0 and leave only one non-zero
     * value.
     *
     * @param matrix1D
     * @return matrix1D
     */
    public abstract DoubleMatrix1D match(DoubleMatrix1D matrix1D);

    protected int getWindowSize(DoubleMatrix1D matrix1D) {
        double windowSizeDouble = Math.sqrt(matrix1D.size());
        int windowSize = (int) windowSizeDouble;
        if (windowSizeDouble - (double) windowSize > 0.000001) {
            MatrixSizeException exception = new MatrixSizeException("Matrix size is not square");
            logger.error(String.format("Similarity Vector has %d cells. It cannot be generated from NxN matrix",
                    matrix1D.size()), exception);
            throw exception;
        }
        return windowSize;
    }
}
