package il.ac.technion.ie.potential.model;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

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
}
