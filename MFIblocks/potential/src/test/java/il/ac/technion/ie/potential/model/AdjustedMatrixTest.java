package il.ac.technion.ie.potential.model;

import com.google.common.collect.HashBiMap;
import il.ac.technion.ie.model.Block;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by XPS_Sapir on 10/07/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(AdjustedMatrix.class)
public class AdjustedMatrixTest {

    private AdjustedMatrix classUnderTest;

    @Before
    public void setUp() throws Exception {
        PowerMockito.suppress(PowerMockito.constructor(AdjustedMatrix.class));
        classUnderTest = new AdjustedMatrix(PowerMockito.mock(List.class));
        Whitebox.setInternalState(classUnderTest, "blockIdToMatPosMap", HashBiMap.create());
    }

    @Test
    public void testCreateMatrixBlockMappping() throws Exception {
        //variables
        // A list of blocks, the ID of them is not sequential
        List<Block> blocks = Arrays.asList(
                new Block(Arrays.asList(1, 2), 1),
                new Block(Arrays.asList(2,3), 2),
                new Block(Arrays.asList(3), 4),
                new Block(Arrays.asList(5), 6)
                );

        //execute
        Whitebox.invokeMethod(classUnderTest, "createMatrixBlockMappping", blocks);

        //assertion - assert the mapping between each blockId and the corespondent matrix cell
        Map<Integer, Integer> blockIdToMatPosMap = Whitebox.getInternalState(classUnderTest, "blockIdToMatPosMap");
        MatcherAssert.assertThat(blockIdToMatPosMap.get(1), Matchers.equalTo(0));
        MatcherAssert.assertThat(blockIdToMatPosMap.get(2), Matchers.equalTo(1));
        MatcherAssert.assertThat(blockIdToMatPosMap.get(4), Matchers.equalTo(2));
        MatcherAssert.assertThat(blockIdToMatPosMap.get(6), Matchers.equalTo(3));
    }
}