package il.ac.technion.ie.logic;

import com.google.common.collect.Lists;
import com.rits.cloning.Cloner;
import il.ac.technion.ie.model.NeighborsVector;
import il.ac.technion.ie.model.NeighborsVectorsCompare;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FindBlockAlgorithmTest {

    private FindBlockAlgorithm classUnderTest;
    private NeighborsVector one;
    private NeighborsVector two;
    private NeighborsVector three;
    private NeighborsVector four;
    private NeighborsVector five;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new FindBlockAlgorithm();
        one = new NeighborsVector(1, 5);
        two = new NeighborsVector(2, 5);
        three = new NeighborsVector(3, 5);
        four = new NeighborsVector(4, 5);
        five = new NeighborsVector(5, 5);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testFindBlocks() throws Exception {
        List<ArrayList<Integer>> trueBlocks = Lists.newArrayList(Lists.newArrayList(1, 5), Lists.newArrayList(2, 3, 4), Lists.newArrayList(1, 2, 3));
        one.exitsNeighbors(Lists.newArrayList(1, 2, 3, 5));
        two.exitsNeighbors(Lists.newArrayList(1, 2, 3, 4));
        three.exitsNeighbors(Lists.newArrayList(1, 2, 3, 4));
        four.exitsNeighbors(Lists.newArrayList(2, 3, 4));
        five.exitsNeighbors(Lists.newArrayList(1, 5));

        List<NeighborsVector> list = Lists.newArrayList(one, two, three, four, five);

        for (int i = 0; i < 6; i++) {

            Collections.shuffle(list);
            classUnderTest.sort(list, new NeighborsVectorsCompare());
            List<List<Integer>> blocks = classUnderTest.findBlocks(list);

            // create copy of trueBlocks
            Cloner cloner = new Cloner();
            List<ArrayList<Integer>> tempList = cloner.deepClone(trueBlocks);

            // assert
            trueBlocks.removeAll(blocks);
            assertThat(trueBlocks, is(empty()));

            trueBlocks = tempList;
            blocks.removeAll(trueBlocks);
            assertThat(blocks, is(empty()));
        }
    }
}