package il.ac.technion.ie.output.writers;

import com.google.common.collect.Lists;
import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.potential.model.AdjustedMatrix;
import il.ac.technion.ie.potential.model.BlockPotential;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.List;

/**
 * Created by XPS_Sapir on 11/07/2015.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AdjustedMatrix.class, BlockPotential.class})
public class PotentialWriterTest {

    private PotentialWriter classUnderTest;

    @Before
    public void setUp() throws Exception {
        this.classUnderTest = new PotentialWriter();
    }

    @Test
    public void testIsType_clazz() throws Exception {
        //execution
        String isType = Whitebox.invokeMethod(classUnderTest, "isType", "someText", String.class);

        //assertion
        MatcherAssert.assertThat(isType, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.instanceOf(String.class),
                Matchers.equalTo("someText")
        ));
    }

    @Test
    public void testIsType_notSameType() throws Exception {
        //execution
        String isType = Whitebox.invokeMethod(classUnderTest, "isType", 44, String.class);

        //assertion
        MatcherAssert.assertThat(isType, Matchers.is(Matchers.nullValue()));
    }

    @Test
    public void testIsType_adjustedMatrix() throws Exception {
        //variables
        PowerMockito.suppress(PowerMockito.constructor(AdjustedMatrix.class));
        AdjustedMatrix adjustedMatrix = new AdjustedMatrix(PowerMockito.mock(List.class));

        //execution
        AdjustedMatrix isType = Whitebox.invokeMethod(classUnderTest, "isType", adjustedMatrix, AdjustedMatrix.class);

        //assertion
        MatcherAssert.assertThat(isType, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.instanceOf(AdjustedMatrix.class),
                Matchers.equalTo(adjustedMatrix)
        ));
    }

    @Test
    public void testisBlockPotential() throws Exception {
        //variables
        PowerMockito.suppress(PowerMockito.constructor(BlockPotential.class));
        BlockPotential blockPotential = new BlockPotential(PowerMockito.mock(Block.class));
        List<BlockPotential> blockPotentialList = Lists.newArrayList(blockPotential);

        //execution
        List<BlockPotential> isType = Whitebox.invokeMethod(classUnderTest, "isBlockPotential",blockPotentialList);

        //assertion
        MatcherAssert.assertThat(isType, Matchers.allOf(
                Matchers.notNullValue(),
                Matchers.instanceOf(List.class),
                Matchers.equalTo(blockPotentialList)
        ));
    }
}