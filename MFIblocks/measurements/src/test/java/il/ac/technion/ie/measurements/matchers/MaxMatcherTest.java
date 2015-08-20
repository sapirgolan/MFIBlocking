package il.ac.technion.ie.measurements.matchers;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleMatrix1D;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

/**
 * Created by XPS_Sapir on 06/06/2015.
 */
public class MaxMatcherTest {

    private final DoubleFactory1D doubleFactory1D = DoubleFactory1D.sparse;
    private MaxMatcher classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new MaxMatcher();
    }

    @Test
    public void testMatch() throws Exception {
        DoubleMatrix1D vectorToTest = doubleFactory1D.make(new double[]{0, 0.2, 0.7, 0, 0, 0.6, 1.2, 0.6, 0});
        DoubleMatrix1D matchedVector = classUnderTest.match(vectorToTest);

        MatcherAssert.assertThat(matchedVector.zSum(), is(2.5));
        MatcherAssert.assertThat(matchedVector.cardinality(), is(3));

        IntArrayList indexList = new IntArrayList();
        DoubleArrayList valueList = new DoubleArrayList();
        matchedVector.getNonZeros(indexList, valueList);

        MatcherAssert.assertThat((List<Integer>) indexList.toList(), contains(2, 5, 6));
        MatcherAssert.assertThat((List<Double>) valueList.toList(), contains(0.7, 0.6, 1.2));
    }
}