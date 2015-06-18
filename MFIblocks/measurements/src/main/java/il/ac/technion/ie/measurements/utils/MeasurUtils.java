package il.ac.technion.ie.measurements.utils;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;

import java.util.BitSet;
import java.util.List;

/**
 * Created by I062070 on 18/06/2015.
 */
public class MeasurUtils {
    public static BitSet convertVectorToBitSet(DoubleMatrix1D matrix1D) {
        IntArrayList nonZeroIndices = new IntArrayList();
        matrix1D.getNonZeros(nonZeroIndices, new DoubleArrayList());

        BitSet bitSet = new BitSet(matrix1D.size());
        List<Integer> list = nonZeroIndices.toList();
        for (Integer index : list) {
            bitSet.set(index);
        }
        return bitSet;
    }
}
