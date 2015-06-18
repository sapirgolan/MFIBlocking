package il.ac.technion.ie.measurements.utils;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest(MeasurUtils.class)
public class MeasurUtilsTest {

    private final DoubleFactory1D doubleFactory1D = DoubleFactory1D.sparse;

    @Test
    public void testConvertVectorToBitSet() throws Exception {
        DoubleMatrix1D matrix1D = doubleFactory1D.make(new double[]{0.2, 0, 0.7, 0, 0, 0.6, 0, 0.6, 1.2});
        BitSet vectorToBitSet = MeasurUtils.convertVectorToBitSet(matrix1D);

        MatcherAssert.assertThat(vectorToBitSet.cardinality(), Matchers.is(5));
        List<Integer> trueBits = new ArrayList<>();
        for (int i = vectorToBitSet.nextSetBit(0); i >= 0; i = vectorToBitSet.nextSetBit(i + 1)) {
            trueBits.add(i);
        }
        //check that the indices of elements > 1 in  matrix1D exists in the returned bitset
        MatcherAssert.assertThat(trueBits, Matchers.containsInAnyOrder(0, 2, 5, 7, 8));
    }
}