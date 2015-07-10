package il.ac.technion.ie.potential.logic;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.BlockPotential;
import il.ac.technion.ie.potential.model.MatrixCell;
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
                                new Block(Arrays.asList(3), Block.RANDOM_ID));

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
//  private AdjustedMatrix buildAdjustedMatrixFromMap(Map<Integer, Set<Integer>> recordBlockMap,
//        List<Block> filteredBlocks)

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
}