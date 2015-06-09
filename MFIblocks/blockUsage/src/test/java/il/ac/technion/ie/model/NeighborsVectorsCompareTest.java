package il.ac.technion.ie.model;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;

public class NeighborsVectorsCompareTest {

    private NeighborsVectorsCompare classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new NeighborsVectorsCompare();
    }

    @Test
    public void testCompare_sameNumberOfElements() throws Exception {
        NeighborsVector neighborsVectorOne = new NeighborsVector(180, 2000);
        neighborsVectorOne.exitsNeighbor(181);
        NeighborsVector neighborsVectorTwo = new NeighborsVector(267, 2000);
        neighborsVectorTwo.exitsNeighbor(268);
        int compare = classUnderTest.compare(neighborsVectorOne, neighborsVectorTwo);
        MatcherAssert.assertThat(compare, is(-1));
    }

    @Test
    public void testCompare_differentNumberOfElements() throws Exception {
        NeighborsVector neighborsVectorOne = new NeighborsVector(180, 2000);
        neighborsVectorOne.exitsNeighbors(Arrays.asList(181, 182));
        NeighborsVector neighborsVectorTwo = new NeighborsVector(267, 2000);
        neighborsVectorTwo.exitsNeighbor(268);
        int compare = classUnderTest.compare(neighborsVectorOne, neighborsVectorTwo);
        MatcherAssert.assertThat(compare, is(1));

        compare = classUnderTest.compare(neighborsVectorTwo, neighborsVectorOne);
        MatcherAssert.assertThat(compare, is(-1));
    }

    @Test
    public void testCompare_exactlyEqual() throws Exception {
        NeighborsVector neighborsVectorOne = new NeighborsVector(267, 2000);
        neighborsVectorOne.exitsNeighbor(268);
        NeighborsVector neighborsVectorTwo = new NeighborsVector(267, 2000);
        neighborsVectorTwo.exitsNeighbor(268);
        int compare = classUnderTest.compare(neighborsVectorOne, neighborsVectorTwo);
        MatcherAssert.assertThat(compare, is(0));
    }
}