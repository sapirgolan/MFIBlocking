package il.ac.technion.ie.output.strategy.block;

import il.ac.technion.ie.model.Block;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;

public class BlockCsvFormatTest {
    private BlockCsvFormat classUnderTest;

    @Before
    public void setUp() throws Exception {
        classUnderTest = new BlockCsvFormat();
    }

    @Test
    public void testToCsv_noRepresentatives() throws Exception {
        Block block = new Block(Arrays.asList(1, 7, 22, 2), 99);
        block.setMemberProbability(1, 0.26F);
        block.setMemberProbability(7, 0.26F);
        block.setMemberProbability(22, 0.25F);
        block.setMemberProbability(2, 0.23F);

        String expectedResult = "99|{1,7,22,2}|{0.26,0.26,0.25,0.23}| Block representatives are: |";

        MatcherAssert.assertThat(classUnderTest.format(block), Matchers.is(equalToIgnoringWhiteSpace(expectedResult)));
    }

    @Test
    public void testToCsv_withRepresentatives() throws Exception {
        Block block = new Block(Arrays.asList(1, 7, 22, 2), 77);
        block.setMemberProbability(1, 0.26F);
        block.setMemberProbability(7, 0.26F);
        block.setMemberProbability(22, 0.25F);
        block.setMemberProbability(2, 0.23F);
        block.findBlockRepresentatives();

        String expectedResult = "77|{1,7,22,2}|{0.26,0.26,0.25,0.23}| Block representatives are: |1,7";

        MatcherAssert.assertThat(classUnderTest.format(block), Matchers.is(equalToIgnoringWhiteSpace(expectedResult)));
    }
}