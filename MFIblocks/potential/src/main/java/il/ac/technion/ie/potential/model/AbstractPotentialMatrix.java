package il.ac.technion.ie.potential.model;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by XPS_Sapir on 14/07/2015.
 */
public abstract class AbstractPotentialMatrix {
    protected DoubleMatrix2D matrix2D;

    /**
     * Returns the number of cells having non-zero values.
     * @return number of cells having non-zero values
     */
    public int cardinality() {
        return this.matrix2D.cardinality();
    }

    /**
     * @return number of rows\columns in matrix
     */
    public int size() {
        return this.matrix2D.rows();
    }

    protected DoubleMatrix2D matrixFactory(int numberOfRows, int numberOfColumns) {
        return DoubleFactory2D.sparse.make(numberOfRows, numberOfColumns);
    }

    public final List<MatrixCell<Double>> getCellsCongaingNonZeroValue() {
        List<MatrixCell<Double>> matrixCells = new ArrayList<>();
        IntArrayList rowList = new IntArrayList();
        IntArrayList columnList = new IntArrayList();
        DoubleArrayList valueList = new DoubleArrayList();
        matrix2D.getNonZeros(rowList, columnList, valueList);
        for (int i = 0; i < columnList.size(); i++) {
            matrixCells.add(new MatrixCell<>(getRecordIDRepresentsRowIndex(rowList.getQuick(i)),
                                            getRecordIDRepresentsColumnIndex(columnList.getQuick(i)),
                                            valueList.getQuick(i)));
        }
        return matrixCells;
    }

    protected abstract int getRecordIDRepresentsRowIndex(int rowIndex);
    protected abstract int getRecordIDRepresentsColumnIndex(int columnIndex);
}
