package il.ac.technion.ie.potential.logic;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.potential.model.*;
import org.apache.commons.lang3.tuple.Pair;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.*;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({PotentialLogic.class})
public class PotentialLogicTest {

    private PotentialLogic classUnderTest;
    @Before
    public void setUp() throws Exception {
        classUnderTest = new PotentialLogic();
    }

    @Test
    public void testGetLocalPotential() throws Exception {
        //variables
        classUnderTest = PowerMockito.spy(classUnderTest);
        List<Block> blocks = Arrays.asList(
                                new Block(Arrays.asList(1, 7, 22, 2), Block.RANDOM_ID),
                                new Block(Collections.singletonList(3), Block.RANDOM_ID));

        //mocking
        PowerMockito.whenNew(BlockPotential.class).withAnyArguments().
                thenReturn(PowerMockito.mock(BlockPotential.class));
        
        //execution
        List<BlockPotential> localPotential = classUnderTest.getLocalPotential(blocks);
        MatcherAssert.assertThat(localPotential, Matchers.hasSize(1));

        //verify
        PowerMockito.verifyNew(BlockPotential.class, Mockito.times(1)).withArguments(Mockito.any(Block.class));
    }

    @Test
    public void testBuildMapBlock() throws Exception {
        //variables
        List<Block> blocks = Arrays.asList(
                    new Block(Arrays.asList(1, 7, 22, 2), 1),
                    new Block(Arrays.asList(3, 7), 2));

        //execution
        Map<Integer, Set<Integer>> mapBlock = Whitebox.invokeMethod(classUnderTest, "buildMapBlock", blocks);

        //assertion
        MatcherAssert.assertThat(mapBlock.size(), Matchers.is(5));
        MatcherAssert.assertThat(mapBlock.values().size(), Matchers.is(5));
        MatcherAssert.assertThat(mapBlock.get(7), Matchers.containsInAnyOrder(1,2));
        MatcherAssert.assertThat(mapBlock.get(3), Matchers.containsInAnyOrder(2));
        MatcherAssert.assertThat(mapBlock.get(1), Matchers.containsInAnyOrder(1));
    }

    @Test
    public void testBuildAdjustedMatrixFromMap() throws Exception {
        //variables
        List<Block> filteredBlocks = Arrays.asList(
                new Block(Arrays.asList(1, 2), 1),
                new Block(Arrays.asList(2, 3), 2),
                new Block(Arrays.asList(3, 4, 2), 4)
        );

        HashMap<Integer, HashSet<Integer>> map = Maps.newHashMap(
                ImmutableMap.of(
                        1, Sets.newHashSet(1),
                        2, Sets.newHashSet(1, 2, 4),
                        3, Sets.newHashSet(2, 4),
                        4, Sets.newHashSet(4)
                ));

        AdjustedMatrix adjustedMatrix = Whitebox.invokeMethod(classUnderTest, "buildAdjustedMatrixFromMap",
                                                                map, filteredBlocks);

        List<MatrixCell<Double>> cells = adjustedMatrix.getCellsCongaingNonZeroValue();
        MatcherAssert.assertThat(cells, Matchers.hasSize(6));
        MatcherAssert.assertThat(cells, Matchers.containsInAnyOrder(
                new MatrixCell<Double>(1, 2, 1.0), new MatrixCell<Double>(1, 4, 1.0),
                new MatrixCell<Double>(2, 1, 1.0), new MatrixCell<Double>(4, 1, 1.0),
                new MatrixCell<Double>(2, 4, 1.0), new MatrixCell<Double>(4, 2, 1.0)
        ));
    }

    @Test
    public void testBuildSharedMatrices() throws Exception {
        //variables
        List<Block> filteredBlocks = Arrays.asList(
                new Block(Arrays.asList(1, 3 ,5), 1),
                new Block(Arrays.asList(1, 2, 4, 6), 2),
                new Block(Arrays.asList(1, 8, 10, 5), 3)
        );

        HashMap<Integer, HashSet<Integer>> recordBlockMap = Maps.newHashMap(
                new ImmutableMap.Builder<Integer, HashSet<Integer>>()
                        .put(1, Sets.newHashSet(1, 2, 3))
                        .put(3, Sets.newHashSet(1))
                        .put(5, Sets.newHashSet(1, 3))
                        .put(2, Sets.newHashSet(2))
                        .put(4, Sets.newHashSet(2))
                        .put(6, Sets.newHashSet(2))
                        .put(8, Sets.newHashSet(3))
                        .put(10, Sets.newHashSet(3))
                        .build()
        );

        List<SharedMatrix> sharedMatrices = Whitebox.invokeMethod(classUnderTest, "buildSharedMatrices",
                                                                recordBlockMap, filteredBlocks);

        MatcherAssert.assertThat(sharedMatrices, Matchers.hasSize(3));

        //the representing matrix of Block1 && Block2
        SharedMatrix sharedMatrix = sharedMatrices.get(0);
        List<MatrixCell<Double>> cells = sharedMatrix.getCellsCongaingNonZeroValue();
        MatcherAssert.assertThat(cells, Matchers.hasSize(1));
        MatcherAssert.assertThat(cells, Matchers.containsInAnyOrder(
                new MatrixCell<Double>(1, 1, -10.0)
        ));
        //the representing matrix of Block2 && Block3
        sharedMatrix = sharedMatrices.get(2);
        cells = sharedMatrix.getCellsCongaingNonZeroValue();
        MatcherAssert.assertThat(cells, Matchers.hasSize(1));
        MatcherAssert.assertThat(cells, Matchers.containsInAnyOrder(
                new MatrixCell<Double>(1, 1, -10.0)
        ));
        //the representing matrix of Block1 && Block3
        sharedMatrix = sharedMatrices.get(1);
        cells = sharedMatrix.getCellsCongaingNonZeroValue();
        MatcherAssert.assertThat(cells, Matchers.hasSize(2));
        MatcherAssert.assertThat(cells, Matchers.containsInAnyOrder(
                new MatrixCell<Double>(1, 1, -10.0), new MatrixCell<Double>(5, 5, -10.0)
        ));
    }

    @Test
    public void testGetSharedMatrix_noPairExists() throws Exception {
        //variables
        BlockPair<Integer, Integer> pair = new BlockPair<>(2, 3);
        Map map = PowerMockito.mock(Map.class);
        Map blockMap = PowerMockito.mock(Map.class);
        Block rowBlock =  new Block(Arrays.asList(2, 4, 7, 6, 3), 2);
        Block columnBlock = new Block(Arrays.asList(1, 7, 3), 3);

        //behaviour
        PowerMockito.when(map.containsKey(Mockito.eq(pair))).thenReturn(false);
        PowerMockito.when(blockMap.get(Mockito.eq(2))).thenReturn(rowBlock);
        PowerMockito.when(blockMap.get(Mockito.eq(3))).thenReturn(columnBlock);

        //execution
        SharedMatrix sharedMatrix = Whitebox.invokeMethod(classUnderTest, "getSharedMatix", pair, map, blockMap);

        //assertion
        MatcherAssert.assertThat(sharedMatrix.size(), Matchers.equalTo(15));
    }

    @Test
    public void testGetSharedMatrix_pairExists() throws Exception {
        //variables
        BlockPair<Integer, Integer> existingPair = new BlockPair<>(3, 2);
        Map<Pair<Integer, Integer>, SharedMatrix> map = new HashMap<>();
        Block rowBlock = new Block(Arrays.asList(2, 4, 7, 6, 3), 2);
        Block columnBlock = new Block(Arrays.asList(1, 7, 3), 3);
        SharedMatrix sharedMatrix = new SharedMatrix(rowBlock, columnBlock);

        //mocks
        Map blockMap = PowerMockito.mock(Map.class);

        //behaviour
        map.put(existingPair, sharedMatrix);
        PowerMockito.when(blockMap.get(Mockito.eq(2))).thenReturn(rowBlock);
        PowerMockito.when(blockMap.get(Mockito.eq(3))).thenReturn(columnBlock);

        //execution
        BlockPair<Integer, Integer> newPair = new BlockPair<>(2, 3);
        SharedMatrix existingMatrix = Whitebox.invokeMethod(classUnderTest, "getSharedMatix", newPair, map, blockMap);

        //assertion
        MatcherAssert.assertThat(sharedMatrix, Matchers.equalTo(existingMatrix));
    }
}