package il.ac.technion.ie.utils;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class MathUtilsTest {

    @Test
    public void testNormilize() throws Exception {
        int[] rangeNumbersPrimitive = Ints.toArray(ContiguousSet.create(Range.closed(1, 10), DiscreteDomain.integers()));
        HashMap<Integer, Float> idToValue = new HashMap<>();
        for (int index = 0; index < rangeNumbersPrimitive.length; index++) {
            idToValue.put(index, (float) rangeNumbersPrimitive[index]);
        }
        HashMap<Integer, Float> copyMap = new HashMap<>(idToValue);
        MathUtils.normilize(idToValue);

        //assert range
        for (Float normValue : idToValue.values()) {
            assertThat((double) normValue, allOf(lessThanOrEqualTo(1.0), greaterThanOrEqualTo(0.0)));
        }
        //assert values
        for (Map.Entry<Integer, Float> entry : idToValue.entrySet()) {
            Integer key = entry.getKey();
            assertThat((double) entry.getValue(), closeTo((copyMap.get(key) - 1.0) / (9), 0.0001));
        }
    }
}