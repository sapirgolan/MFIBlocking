package il.ac.technion.ie.model;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.hamcrest.core.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;

public class BlockTest {

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testEquals_true() throws Exception {
        Block blockOne = new Block(Arrays.asList(1,2,3));
        Block blockTwo = new Block(Arrays.asList(1,2,3));
        MatcherAssert.assertThat(blockOne, is(equalTo(blockTwo)));
    }

    @Test
    public void testEquals_sameDifferentOrder() throws Exception {
        Block blockOne = new Block(Arrays.asList(1,2,3));
        Block blockTwo = new Block(Arrays.asList(2,3,1));
        MatcherAssert.assertThat(blockOne, is(equalTo(blockTwo)));
    }

    @Test
    public void testEquals_secondSmaller() throws Exception {
        Block blockOne = new Block(Arrays.asList(1,2,3));
        Block blockTwo = new Block(Arrays.asList(1,2));
        MatcherAssert.assertThat(blockOne, IsNot.not(equalTo(blockTwo)));
    }

    @Test
    public void testEquals_secondBigger() throws Exception {
        Block blockOne = new Block(Arrays.asList(1,2));
        Block blockTwo = new Block(Arrays.asList(1,2,3));
        MatcherAssert.assertThat(blockOne, IsNot.not(equalTo(blockTwo)));
    }

    @Test
    public void testEquals_firstAndLastMembersAreTheSame() throws Exception {
        Block blockOne = new Block(Arrays.asList(1,7,220,2));
        Block blockTwo = new Block(Arrays.asList(1,9,2));
        MatcherAssert.assertThat(blockOne, IsNot.not(equalTo(blockTwo)));
    }

    @Test
    public void testHasMember() throws Exception {
        List<Integer> members = Arrays.asList(1, 7, 220, 2);
        Block block = new Block(members);
        for (Integer member : members) {
            MatcherAssert.assertThat(block.hasMember(member), is(true));
        }
        MatcherAssert.assertThat(block.hasMember(5), is(false));
    }

    @Test
    public void testGetMemberAvgSimilarity() throws Exception {
        Block block = new Block(Arrays.asList(1, 7, 220, 2));
        block.setMemberSimScore(1, 2.2F);
        block.setMemberSimScore(7, 2F);
        block.setMemberSimScore(220, 2.1F);
        block.setMemberSimScore(2, 2.6F);

        MatcherAssert.assertThat(block.getMemberAvgSimilarity(1), closeTo(0.7333, 0.001));
        MatcherAssert.assertThat(block.getMemberAvgSimilarity(7), closeTo(0.6666, 0.001));
        MatcherAssert.assertThat(block.getMemberAvgSimilarity(220), closeTo(0.7, 0.001));
        MatcherAssert.assertThat(block.getMemberAvgSimilarity(2), closeTo(0.8666, 0.001));
    }

    @Test
    public void testFindRep() throws Exception {
        Block block = new Block(Arrays.asList(1, 7, 22, 2));
        block.setMemberProbability(1, 0.26F);
        block.setMemberProbability(7, 0.26F);
        block.setMemberProbability(22, 0.25F);
        block.setMemberProbability(2, 0.23F);

        Map<Integer, Float> blockRepresentatives = block.findBlockRepresentatives();
        MatcherAssert.assertThat(blockRepresentatives.size(), Matchers.is(2));
    }

    @Test
    public void testToString() throws Exception {
        Block block = new Block(Arrays.asList(1, 7, 22, 2));
        block.setMemberProbability(1, 0.26F);
        block.setMemberProbability(7, 0.26F);
        block.setMemberProbability(22, 0.25F);
        block.setMemberProbability(2, 0.23F);

        String expectedResult = "Block{1,7,22,2}\n" +
                "Probs{0.26,0.26,0.25,0.23}\n" +
                "Block representative is: recordIDs 1,7 Probability 0.26";

        MatcherAssert.assertThat(block.toString(), Matchers.is(equalToIgnoringWhiteSpace(expectedResult)));
    }

    @Test
    public void testToCsv() throws Exception {
        Block block = new Block(Arrays.asList(1, 7, 22, 2));
        block.setMemberProbability(1, 0.26F);
        block.setMemberProbability(7, 0.26F);
        block.setMemberProbability(22, 0.25F);
        block.setMemberProbability(2, 0.23F);

        String expectedResult = "{1,7,22,2}|{0.26,0.26,0.25,0.23}| Block representatives are: |1,7";

        MatcherAssert.assertThat(block.toCsv(), Matchers.is(equalToIgnoringWhiteSpace(expectedResult)));
    }
}