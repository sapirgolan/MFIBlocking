package il.ac.technion.ie.potential.logic;

import il.ac.technion.ie.model.Block;
import il.ac.technion.ie.potential.model.BlockPotential;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;

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
        List<Block> blocks = Arrays.asList(new Block(Arrays.asList(1, 7, 22, 2)),new Block(Arrays.asList(3)));

        //mocking
        PowerMockito.whenNew(BlockPotential.class).withAnyArguments().
                thenReturn(PowerMockito.mock(BlockPotential.class));
        
        //execution
        List<BlockPotential> localPotential = classUnderTest.getLocalPotential(blocks);
        MatcherAssert.assertThat(localPotential, Matchers.hasSize(1));

        //verify
        PowerMockito.verifyNew(BlockPotential.class, Mockito.times(1)).withArguments(Mockito.any(Block.class));
    }
}