package il.ac.technion.ie.potential.model;

import il.ac.technion.ie.model.Block;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by XPS_Sapir on 09/07/2015.
 */
public class BlockPotentialTest {

    @Before
    public void setUp() throws Exception {


    }

    @Test
    public void testGetPotentialValues() throws Exception {
        Block block = new Block(Arrays.asList(1, 3, 2));
        block.setMemberProbability(1, 0.6F);
        block.setMemberProbability(3, 0.3F);
        block.setMemberProbability(2, 0.1F);
        BlockPotential blockPotential = new BlockPotential(block);
        List<Double> potentialValues = blockPotential.getPotentialValues();

        MatcherAssert.assertThat(potentialValues, Matchers.containsInAnyOrder(
                Matchers.closeTo(-0.5108, 0.0001),
                Matchers.closeTo(-1.2039, 0.0001),
                Matchers.closeTo(-2.30259, 0.0001)
        ));
    }
}