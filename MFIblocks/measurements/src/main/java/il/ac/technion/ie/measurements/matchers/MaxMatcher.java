package il.ac.technion.ie.measurements.matchers;

import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by XPS_Sapir on 04/06/2015.
 */
public class MaxMatcher extends AbstractMatcher {
    private static final Logger logger = Logger.getLogger(MaxMatcher.class);

    @Override
    public DoubleMatrix1D match(DoubleMatrix1D matrix1D) {
        SparseDoubleMatrix1D matchedMatrix = new SparseDoubleMatrix1D(matrix1D.size());
        int windowSize = getWindowSize(matrix1D);
        for (int startigIndex = 0; startigIndex < matrix1D.size(); startigIndex+=windowSize) {
            DoubleMatrix1D row = matrix1D.viewPart(startigIndex, windowSize);
            Map.Entry<Double, Integer> maxScoreEntry = getMaxEntryInRow(windowSize, startigIndex, row);
            matchedMatrix.setQuick(maxScoreEntry.getValue(), maxScoreEntry.getKey());
        }
        return matchedMatrix;
    }

    private Map.Entry<Double, Integer> getMaxEntryInRow(int windowSize, int startigIndex, DoubleMatrix1D row) {
        TreeMap<Double, Integer> map = new TreeMap<>();
        for (int i = 0; i < windowSize; i++) {
            map.put(row.getQuick(i), i + startigIndex);
        }
        return map.lastEntry();
    }

}
