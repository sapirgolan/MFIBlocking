package il.ac.technion.ie.measurements.logic;

import cern.colt.matrix.DoubleMatrix2D;
import il.ac.technion.ie.model.Block;

/**
 * Created by I062070 on 18/06/2015.
 */
public interface iUpdateMatrixStrategy {
    double getRecordValue(int recordId, Block block);

    double calcCurrentCellValue(double currentValue, DoubleMatrix2D matrix, int rowIndex, int colIndex);

    String getUpdateLogMessage(int rowIndex, int colIndex, double currentValue, double posValue);
}
